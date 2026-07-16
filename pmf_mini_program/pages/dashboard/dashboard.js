var api = require('../../utils/api');

// 每屏宽度
var screenW = 0;
var dpr = 1;

Page({
  data: {
    capacityList: [],
    goodsHasData: false,
    goodsCanvasW: 0,
    thHasData: false,
    thCanvasW: 0
  },

  onLoad() {
    var sys = wx.getSystemInfoSync();
    screenW = sys.windowWidth;
    dpr = sys.pixelRatio || 1;
    this.fetchAllData();
  },

  async fetchAllData() {
    wx.showLoading({ title: '加载看板数据...' });
    try {
      var results = await Promise.all([
        api.dashboardApi.weeklyInboundOutbound().catch(function() { return null; }),
        api.dashboardApi.goodsQuantity().catch(function() { return null; }),
        api.dashboardApi.warehouseTempHumidity().catch(function() { return null; }),
        api.dashboardApi.warehouseCapacity().catch(function() { return null; })
      ]);

      // 处理容量饱和度
      var capData = results[3];
      var capacityList = [];
      if (capData) {
        var warehouses = capData.warehouses || [];
        for (var i = 0; i < warehouses.length; i++) {
          var w = warehouses[i];
          var sat = w.saturation != null ? w.saturation : 0;
          capacityList.push({
            warehouseName: w.warehouseName || w.warehouse_name || '-',
            usedSlots: w.usedSlots || w.used_slots || 0,
            totalSlots: w.totalSlots || w.total_slots || 0,
            saturation: sat,
            color: getCapColor(sat)
          });
        }
      }
      this.setData({ capacityList: capacityList });

      // 延迟初始化canvas图表（等待DOM渲染完成）
      var self = this;
      setTimeout(function() {
        self.initTrendChart(results[0]);
        self.initGoodsChart(results[1]);
        self.initTHChart(results[2]);
      }, 300);

    } catch (e) {
      console.error('加载看板数据失败:', e);
    }
    wx.hideLoading();
  },

  // ========== 出入库趋势折线图 ==========
  initTrendChart: function(lineData) {
    if (!lineData || !lineData.dates || lineData.dates.length === 0) return;

    var query = wx.createSelectorQuery();
    var self = this;
    query.select('#trendCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return;
      var canvas = res[0].node;
      var w = res[0].width;
      var h = res[0].height;
      canvas.width = w * dpr;
      canvas.height = h * dpr;
      var ctx = canvas.getContext('2d');
      ctx.scale(dpr, dpr);

      var pad = { top: 30, right: 16, bottom: 30, left: 40 };
      var cw = w - pad.left - pad.right;
      var ch = h - pad.top - pad.bottom;

      var dates = lineData.dates;
      var inbound = lineData.inbound || [];
      var outbound = lineData.outbound || [];

      // 找最大最小值
      var allVals = inbound.concat(outbound);
      var maxVal = Math.max.apply(null, allVals) || 1;
      maxVal = Math.ceil(maxVal * 1.15);

      // 绘制网格和轴
      ctx.strokeStyle = '#f0f0f0';
      ctx.lineWidth = 0.5;
      var ySteps = 4;
      for (var i = 0; i <= ySteps; i++) {
        var y = pad.top + (ch / ySteps) * i;
        ctx.beginPath();
        ctx.moveTo(pad.left, y);
        ctx.lineTo(w - pad.right, y);
        ctx.stroke();
        // Y轴标签
        ctx.fillStyle = '#94A3B8';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(Math.round(maxVal - (maxVal / ySteps) * i), pad.left - 6, y + 4);
      }

      // X轴标签
      ctx.textAlign = 'center';
      var xStep = cw / Math.max(dates.length - 1, 1);
      for (var j = 0; j < dates.length; j++) {
        var x = pad.left + xStep * j;
        if (j % Math.ceil(dates.length / 7) === 0 || j === dates.length - 1) {
          ctx.fillText(String(dates[j]).slice(5), x, h - pad.bottom + 16);
        }
      }

      // 绘制折线 - 入库
      drawLine(ctx, dates, inbound, pad, xStep, ch, maxVal, '#5ab1ef', true);
      // 绘制折线 - 出库
      drawLine(ctx, dates, outbound, pad, xStep, ch, maxVal, '#b6a2de', true);

      // 图例
      drawLegend(ctx, w, pad.top, ['入库数量', '出库数量'], ['#5ab1ef', '#b6a2de']);
    });
  },

  // ========== 货物库存数量柱状图 ==========
  initGoodsChart: function(barData) {
    if (!barData || !barData.names || barData.names.length === 0) return;

    var names = barData.names;
    var quantities = barData.quantities || [];
    var barW = 30;
    var gap = 16;
    var needW = names.length * (barW + gap) + 60;
    var cw = screenW - 40; // 40 = padding

    if (needW > cw) {
      this.setData({ goodsCanvasW: needW, goodsHasData: true });
    } else {
      needW = cw;
      this.setData({ goodsCanvasW: 0, goodsHasData: false });
    }

    var self = this;
    var query = wx.createSelectorQuery();
    query.select('#goodsCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return;
      var canvas = res[0].node;
      var w = res[0].width || needW;
      var h = res[0].height || 240;
      canvas.width = w * dpr;
      canvas.height = h * dpr;
      var ctx = canvas.getContext('2d');
      ctx.scale(dpr, dpr);

      var pad = { top: 16, right: 16, bottom: 36, left: 40 };
      var chartW = w - pad.left - pad.right;
      var chartH = h - pad.top - pad.bottom;

      var maxQty = Math.max.apply(null, quantities) || 1;
      maxQty = Math.ceil(maxQty * 1.15);

      // 网格线
      ctx.strokeStyle = '#f0f0f0';
      ctx.lineWidth = 0.5;
      var ySteps = 4;
      for (var i = 0; i <= ySteps; i++) {
        var y = pad.top + (chartH / ySteps) * i;
        ctx.beginPath();
        ctx.moveTo(pad.left, y);
        ctx.lineTo(w - pad.right, y);
        ctx.stroke();
        ctx.fillStyle = '#94A3B8';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(Math.round(maxQty - (maxQty / ySteps) * i), pad.left - 6, y + 4);
      }

      // 柱子
      var xStep = chartW / names.length;
      for (var j = 0; j < names.length; j++) {
        var x = pad.left + xStep * j + (xStep - barW) / 2;
        var barH = (quantities[j] / maxQty) * chartH;
        var by = pad.top + chartH - barH;

        // 渐变
        var grad = ctx.createLinearGradient(x, by, x, pad.top + chartH);
        grad.addColorStop(0, '#10b981');
        grad.addColorStop(1, '#6ee7b7');
        ctx.fillStyle = grad;

        // 圆角矩形
        roundRect(ctx, x, by, barW, barH, 4);
        ctx.fill();

        // X轴标签
        ctx.fillStyle = '#64748B';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        var label = names[j];
        if (label.length > 4) label = label.slice(0, 4) + '..';
        ctx.fillText(label, x + barW / 2, h - pad.bottom + 16);
      }
    });
  },

  // ========== 仓库温湿度柱状图 ==========
  initTHChart: function(thData) {
    if (!thData || !thData.names || thData.names.length === 0) return;

    var names = thData.names;
    var temperatures = thData.temperatures || [];
    var humidities = thData.humidities || [];
    var barW = 18;
    var gap = 24;
    var needW = names.length * (barW * 2 + gap) + 60;
    var cw = screenW - 40;

    if (needW > cw) {
      this.setData({ thCanvasW: needW, thHasData: true });
    } else {
      needW = cw;
      this.setData({ thCanvasW: 0, thHasData: false });
    }

    var query = wx.createSelectorQuery();
    query.select('#thCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return;
      var canvas = res[0].node;
      var w = res[0].width || needW;
      var h = res[0].height || 240;
      canvas.width = w * dpr;
      canvas.height = h * dpr;
      var ctx = canvas.getContext('2d');
      ctx.scale(dpr, dpr);

      var pad = { top: 30, right: 36, left: 40, bottom: 30 };
      var chartW = w - pad.left - pad.right;
      var chartH = h - pad.top - pad.bottom;

      // 温度范围
      var allT = temperatures.filter(function(v) { return v != null && !isNaN(v); });
      var maxT = allT.length > 0 ? Math.ceil(Math.max.apply(null, allT) * 1.2) : 50;
      // 湿度范围 (百分比)
      var allH = humidities.filter(function(v) { return v != null && !isNaN(v); });
      var maxH = allH.length > 0 ? Math.ceil(Math.max.apply(null, allH) * 1.2) : 100;

      // 网格
      ctx.strokeStyle = '#f0f0f0';
      ctx.lineWidth = 0.5;
      var ySteps = 4;
      for (var i = 0; i <= ySteps; i++) {
        var y = pad.top + (chartH / ySteps) * i;
        ctx.beginPath();
        ctx.moveTo(pad.left, y);
        ctx.lineTo(w - pad.right, y);
        ctx.stroke();
        // 左轴 - 温度
        ctx.fillStyle = '#ff3b3b';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(Math.round(maxT - (maxT / ySteps) * i) + '°', pad.left - 6, y + 4);
        // 右轴 - 湿度
        ctx.fillStyle = '#5ab1ef';
        ctx.textAlign = 'left';
        ctx.fillText(Math.round(maxH - (maxH / ySteps) * i) + '%', w - pad.right + 6, y + 4);
      }

      // 柱子
      var groupW = barW * 2 + 4; // 每组宽度
      var xStep = chartW / names.length;
      for (var j = 0; j < names.length; j++) {
        var gx = pad.left + xStep * j + (xStep - groupW) / 2;

        // 温度柱子
        var tH = ((temperatures[j] || 0) / maxT) * chartH;
        tH = Math.max(0, tH);
        var gradT = ctx.createLinearGradient(gx, pad.top + chartH - tH, gx, pad.top + chartH);
        gradT.addColorStop(0, '#ff3b3b');
        gradT.addColorStop(1, '#ff9999');
        ctx.fillStyle = gradT;
        roundRect(ctx, gx, pad.top + chartH - tH, barW, tH, 3);
        ctx.fill();

        // 湿度柱子
        var hH = ((humidities[j] || 0) / maxH) * chartH;
        hH = Math.max(0, hH);
        var gradH = ctx.createLinearGradient(gx + barW + 4, pad.top + chartH - hH, gx + barW + 4, pad.top + chartH);
        gradH.addColorStop(0, '#5ab1ef');
        gradH.addColorStop(1, '#add8f7');
        ctx.fillStyle = gradH;
        roundRect(ctx, gx + barW + 4, pad.top + chartH - hH, barW, hH, 3);
        ctx.fill();

        // X轴标签
        ctx.fillStyle = '#64748B';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        var label = names[j];
        if (label.length > 5) label = label.slice(0, 5) + '..';
        ctx.fillText(label, gx + groupW / 2, h - pad.bottom + 16);
      }

      // 图例
      drawLegend(ctx, w, pad.top, ['温度(°C)', '湿度(%)'], ['#ff3b3b', '#5ab1ef']);
    });
  }
});

// ========== Canvas 绘图工具函数 ==========

// 绘制折线
function drawLine(ctx, dates, data, pad, xStep, chartH, maxVal, color, fillArea) {
  if (!data || data.length === 0) return;

  ctx.strokeStyle = color;
  ctx.lineWidth = 2;
  ctx.lineJoin = 'round';
  ctx.beginPath();

  var points = [];
  for (var i = 0; i < data.length; i++) {
    var x = pad.left + xStep * i;
    var y = pad.top + chartH - (data[i] / maxVal) * chartH;
    if (i === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
    points.push({ x: x, y: y });
  }
  ctx.stroke();

  // 填充区域
  if (fillArea && points.length > 1) {
    ctx.lineTo(points[points.length - 1].x, pad.top + chartH);
    ctx.lineTo(points[0].x, pad.top + chartH);
    ctx.closePath();
    ctx.fillStyle = color.replace(')', ',0.12)').replace('rgb', 'rgba');
    if (color.startsWith('#')) {
      ctx.fillStyle = hexToRgba(color, 0.12);
    }
    ctx.fill();
  }

  // 数据点
  for (var j = 0; j < points.length; j++) {
    ctx.beginPath();
    ctx.arc(points[j].x, points[j].y, 3, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 1;
    ctx.stroke();
  }
}

// 图例
function drawLegend(ctx, w, topY, names, colors) {
  var totalW = 0;
  ctx.font = '11px sans-serif';
  for (var i = 0; i < names.length; i++) {
    totalW += ctx.measureText(names[i]).width + 28;
  }
  var lx = (w - totalW) / 2;
  for (var j = 0; j < names.length; j++) {
    // 色块
    ctx.fillStyle = colors[j];
    ctx.fillRect(lx, topY - 14, 12, 12);
    // 文字
    ctx.fillStyle = '#606266';
    ctx.textAlign = 'left';
    ctx.fillText(names[j], lx + 16, topY - 4);
    lx += ctx.measureText(names[j]).width + 28;
  }
}

// 圆角矩形
function roundRect(ctx, x, y, w, h, r) {
  if (h <= 0) return;
  r = Math.min(r, w / 2, h);
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.lineTo(x + w - r, y);
  ctx.arcTo(x + w, y, x + w, y + r, r);
  ctx.lineTo(x + w, y + h);
  ctx.lineTo(x, y + h);
  ctx.lineTo(x, y + r);
  ctx.arcTo(x, y, x + r, y, r);
  ctx.closePath();
}

// Hex转RGBA
function hexToRgba(hex, alpha) {
  var r = parseInt(hex.slice(1, 3), 16);
  var g = parseInt(hex.slice(3, 5), 16);
  var b = parseInt(hex.slice(5, 7), 16);
  return 'rgba(' + r + ',' + g + ',' + b + ',' + alpha + ')';
}

// 容量颜色
function getCapColor(sat) {
  if (sat == null) return '#10b981';
  if (sat < 25) return '#10b981';
  if (sat < 50) return '#e6a23c';
  if (sat < 75) return '#f59e0b';
  return '#f56c6c';
}
