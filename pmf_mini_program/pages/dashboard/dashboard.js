var api = require('../../utils/api');

var screenW = 0;
var dpr = 1;

// 存储各图表的数据和布局信息，供 tap 事件查找
var chartMeta = {
  trend: null,
  goods: null,
  th: null
};

Page({
  data: {
    capacityList: [],
    goodsHasData: false,
    goodsCanvasW: 0,
    thHasData: false,
    thCanvasW: 0,
    // 各图表 tooltip
    trendTip:  { show: false, x: 0, y: 0, text: '' },
    goodsTip:  { show: false, x: 0, y: 0, text: '' },
    thTip:     { show: false, x: 0, y: 0, text: '' }
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

      var self = this;
      setTimeout(function() {
        self.initTrendChart(results[0]);
        self.initGoodsChart(results[1]);
        self.initTHChart(results[2]);
      }, 400);

    } catch (e) {
      console.error('加载看板数据失败:', e);
    }
    wx.hideLoading();
  },

  // ========== 出入库趋势折线图 ==========
  initTrendChart: function(lineData) {
    if (!lineData || !lineData.dates || lineData.dates.length === 0) return;

    var query = wx.createSelectorQuery();
    query.select('#trendCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return;
      var canvas = res[0].node;
      var w = res[0].width;
      var h = res[0].height;
      canvas.width = w * dpr;
      canvas.height = h * dpr;
      var ctx = canvas.getContext('2d');
      ctx.scale(dpr, dpr);

      var pad = { top: 30, right: 16, bottom: 30, left: 44 };
      var cw = w - pad.left - pad.right;
      var ch = h - pad.top - pad.bottom;

      var dates = lineData.dates;
      var inbound = lineData.inbound || [];
      var outbound = lineData.outbound || [];

      var allVals = inbound.concat(outbound);
      var maxVal = Math.max.apply(null, allVals) || 1;
      maxVal = Math.ceil(maxVal * 1.15);

      // 网格
      ctx.strokeStyle = '#f0f0f0';
      ctx.lineWidth = 0.5;
      var ySteps = 4;
      for (var i = 0; i <= ySteps; i++) {
        var gy = pad.top + (ch / ySteps) * i;
        ctx.beginPath();
        ctx.moveTo(pad.left, gy);
        ctx.lineTo(w - pad.right, gy);
        ctx.stroke();
        ctx.fillStyle = '#94A3B8';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(Math.round(maxVal - (maxVal / ySteps) * i), pad.left - 6, gy + 4);
      }

      // X轴标签
      ctx.textAlign = 'center';
      var xStep = cw / Math.max(dates.length - 1, 1);
      for (var j = 0; j < dates.length; j++) {
        var gx = pad.left + xStep * j;
        if (j % Math.ceil(dates.length / 7) === 0 || j === dates.length - 1) {
          ctx.fillText(String(dates[j]).slice(5), gx, h - pad.bottom + 16);
        }
      }

      var inPts = drawLine(ctx, inbound, pad, xStep, ch, maxVal, '#5ab1ef', true);
      var outPts = drawLine(ctx, outbound, pad, xStep, ch, maxVal, '#b6a2de', true);
      drawLegend(ctx, w, pad.top, ['入库数量', '出库数量'], ['#5ab1ef', '#b6a2de']);

      chartMeta.trend = {
        pad: pad, cw: cw, ch: ch, xStep: xStep,
        maxVal: maxVal, dates: dates,
        series: [
          { name: '入库数量', data: inbound, pts: inPts, color: '#5ab1ef' },
          { name: '出库数量', data: outbound, pts: outPts, color: '#b6a2de' }
        ]
      };
    });
  },

  // ========== 货物库存数量柱状图 ==========
  initGoodsChart: function(barData) {
    if (!barData || !barData.names || barData.names.length === 0) return;

    var names = barData.names;
    var quantities = barData.quantities || [];
    var many = names.length > 4;
    var barW = many ? 24 : 30;
    var bottomPad = many ? 56 : 36;
    var labelFS = many ? 9 : 10;
    var needW = names.length * (barW + (many ? 24 : 16)) + 60;
    var cw = screenW - 40;

    if (needW > cw) {
      this.setData({ goodsCanvasW: needW, goodsHasData: true });
    } else {
      needW = cw;
      this.setData({ goodsCanvasW: 0, goodsHasData: false });
    }

    var self = this;
    setTimeout(function() {
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

        var pad = { top: 16, right: 16, bottom: bottomPad, left: 40 };
        var chartW = w - pad.left - pad.right;
        var chartH = h - pad.top - pad.bottom;

        var maxQty = Math.max.apply(null, quantities) || 1;
        maxQty = Math.ceil(maxQty * 1.15);

        ctx.strokeStyle = '#f0f0f0';
        ctx.lineWidth = 0.5;
        var ySteps = 4;
        for (var i = 0; i <= ySteps; i++) {
          var gy = pad.top + (chartH / ySteps) * i;
          ctx.beginPath();
          ctx.moveTo(pad.left, gy);
          ctx.lineTo(w - pad.right, gy);
          ctx.stroke();
          ctx.fillStyle = '#94A3B8';
          ctx.font = '10px sans-serif';
          ctx.textAlign = 'right';
          ctx.fillText(Math.round(maxQty - (maxQty / ySteps) * i), pad.left - 6, gy + 4);
        }

        var xStep = chartW / names.length;
        var bars = [];
        for (var j = 0; j < names.length; j++) {
          var bx = pad.left + xStep * j + (xStep - barW) / 2;
          var barH = (quantities[j] / maxQty) * chartH;
          var by = pad.top + chartH - barH;
          var grad = ctx.createLinearGradient(bx, by, bx, pad.top + chartH);
          grad.addColorStop(0, '#10b981');
          grad.addColorStop(1, '#6ee7b7');
          ctx.fillStyle = grad;
          roundRect(ctx, bx, by, barW, barH, 4);
          ctx.fill();
          bars.push({ x: bx, y: by, w: barW, h: barH });

          ctx.fillStyle = '#64748B';
          ctx.font = labelFS + 'px sans-serif';
          if (many) {
            ctx.save();
            ctx.textAlign = 'right';
            ctx.translate(bx + barW / 2, h - pad.bottom + 20);
            ctx.rotate(-0.6);
            ctx.fillText(names[j], 0, 0);
            ctx.restore();
          } else {
            ctx.textAlign = 'center';
            var lbl = names[j].length > 5 ? names[j].slice(0, 5) + '..' : names[j];
            ctx.fillText(lbl, bx + barW / 2, h - pad.bottom + 16);
          }
        }

        chartMeta.goods = {
          pad: pad, chartW: chartW, chartH: chartH,
          maxQty: maxQty, names: names, quantities: quantities,
          bars: bars, xStep: xStep, barW: barW
        };
      });
    }, 200);
  },

  // ========== 仓库温湿度柱状图（支持负温度、零线对齐）==========
  initTHChart: function(thData) {
    if (!thData || !thData.names || thData.names.length === 0) return;

    var names = thData.names;
    var temperatures = thData.temperatures || [];
    var humidities = thData.humidities || [];
    var barW = 16;
    var groupW = barW * 2 + 4;
    var needW = names.length * (groupW + 20) + 60;
    var cw = screenW - 40;

    if (needW > cw) {
      this.setData({ thCanvasW: needW, thHasData: true });
    } else {
      needW = cw;
      this.setData({ thCanvasW: 0, thHasData: false });
    }

    var self = this;
    setTimeout(function() {
      var query = wx.createSelectorQuery();
      query.select('#thCanvas').fields({ node: true, size: true }).exec(function(res) {
        if (!res || !res[0] || !res[0].node) return;
        var canvas = res[0].node;
        var w = res[0].width || needW;
        var h = res[0].height || 280;
        canvas.width = w * dpr;
        canvas.height = h * dpr;
        var ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);

        var pad = { top: 30, right: 40, left: 48, bottom: 40 };
        var chartW = w - pad.left - pad.right;
        var chartH = h - pad.top - pad.bottom;

        // ---- 温度范围（含负值）----
        var allT = temperatures.filter(function(v) { return v != null && !isNaN(v); });
        var tMax = allT.length > 0 ? Math.max.apply(null, allT) : 30;
        var tMin = allT.length > 0 ? Math.min.apply(null, allT) : 0;
        var tRange = tMax - tMin || 10;
        tMax = tMax + tRange * 0.15;
        tMin = tMin - tRange * 0.15;

        // ---- 湿度范围（min=0）----
        var allH = humidities.filter(function(v) { return v != null && !isNaN(v); });
        var hMax = allH.length > 0 ? Math.max.apply(null, allH) : 100;
        hMax = Math.ceil(hMax * 1.2);
        var hMin = 0;

        // ---- 对齐零线 ----
        // 零在轴上的位置比例 (0=图表顶部, 1=图表底部)
        var tZeroR = (tMax - 0) / (tMax - tMin);
        var hZeroR = (hMax - 0) / (hMax - hMin); // = 1.0（湿度0始终在底部）

        // 选较高的零线位置（比例值更小的那个），使零线在图表中更高
        var Z = Math.min(tZeroR, hZeroR);
        // 将零线较低的那个轴向下延伸，使两条零线平齐
        // 公式: newMin = max * (Z - 1) / Z
        if (Z > 0) {
          if (Z < tZeroR) tMin = tMax * (Z - 1) / Z;
          if (Z < hZeroR) hMin = hMax * (Z - 1) / Z;
        }

        var zeroY = pad.top + chartH * Z; // 零线的 Y 坐标

        // ---- 网格线 ----
        ctx.strokeStyle = '#f0f0f0';
        ctx.lineWidth = 0.5;
        var ySteps = 4;
        for (var i = 0; i <= ySteps; i++) {
          var gy = pad.top + (chartH / ySteps) * i;
          ctx.beginPath();
          ctx.moveTo(pad.left, gy);
          ctx.lineTo(w - pad.right, gy);
          ctx.stroke();

          var tVal = tMax - ((tMax - tMin) / ySteps) * i;
          ctx.fillStyle = '#ff3b3b';
          ctx.font = '10px sans-serif';
          ctx.textAlign = 'right';
          ctx.fillText(tVal.toFixed(1) + '°', pad.left - 4, gy + 4);

          var hVal = hMax - ((hMax - hMin) / ySteps) * i;
          ctx.fillStyle = '#5ab1ef';
          ctx.textAlign = 'left';
          if (hVal >= 0) {
            ctx.fillText(Math.round(hVal) + '%', w - pad.right + 6, gy + 4);
          }
        }

        // ---- 零线虚线 ----
        ctx.strokeStyle = '#bbb';
        ctx.lineWidth = 1;
        ctx.setLineDash([4, 3]);
        ctx.beginPath();
        ctx.moveTo(pad.left, zeroY);
        ctx.lineTo(w - pad.right, zeroY);
        ctx.stroke();
        ctx.setLineDash([]);

        // ---- 柱子 ----
        var xStep = chartW / names.length;
        var tBars = [], hBars = [];
        for (var j = 0; j < names.length; j++) {
          var gx = pad.left + xStep * j + (xStep - groupW) / 2;

          // 温度柱（从零线向上或向下）
          var tv = temperatures[j] || 0;
          var tH = (tv / (tMax - tMin)) * chartH;
          var tY, tDrawH;
          if (tH >= 0) { tY = zeroY - tH; tDrawH = tH; }
          else         { tY = zeroY;      tDrawH = -tH; }
          tDrawH = Math.max(0, tDrawH);
          if (tDrawH > 1) {
            var gradT = ctx.createLinearGradient(gx, tY, gx, tY + tDrawH);
            gradT.addColorStop(0, '#ff3b3b');
            gradT.addColorStop(1, '#ff9999');
            ctx.fillStyle = gradT;
            roundRect(ctx, gx, tY, barW, tDrawH, 3);
            ctx.fill();
          }
          tBars.push({ x: gx, y: tY, w: barW, h: tDrawH, val: tv, zeroY: zeroY });

          // 湿度柱（从零线向上）
          var hv = humidities[j] || 0;
          var hH = (hv / (hMax - hMin)) * chartH;
          hH = Math.max(0, hH);
          var hx = gx + barW + 4;
          var hy = zeroY - hH;
          if (hH > 1) {
            var gradH = ctx.createLinearGradient(hx, hy, hx, zeroY);
            gradH.addColorStop(0, '#5ab1ef');
            gradH.addColorStop(1, '#add8f7');
            ctx.fillStyle = gradH;
            roundRect(ctx, hx, hy, barW, hH, 3);
            ctx.fill();
          }
          hBars.push({ x: hx, y: hy, w: barW, h: hH, val: hv, zeroY: zeroY });

          // X轴标签（旋转）
          ctx.fillStyle = '#64748B';
          ctx.font = '9px sans-serif';
          ctx.save();
          ctx.textAlign = 'right';
          ctx.translate(gx + groupW / 2, h - pad.bottom + 22);
          ctx.rotate(-0.6);
          var lbl = names[j].length > 6 ? names[j].slice(0, 6) + '..' : names[j];
          ctx.fillText(lbl, 0, 0);
          ctx.restore();
        }

        drawLegend(ctx, w, pad.top, ['温度(°C)', '湿度(%)'], ['#ff3b3b', '#5ab1ef']);

        chartMeta.th = {
          pad: pad, chartW: chartW, chartH: chartH,
          names: names, tBars: tBars, hBars: hBars,
          tMax: tMax, tMin: tMin, hMax: hMax, hMin: hMin,
          zeroY: zeroY, xStep: xStep
        };
      });
    }, 200);
  },

  // ========== Tap 事件：点击覆盖层显示 tooltip ==========

  onTrendTap: function(e) {
    var meta = chartMeta.trend;
    if (!meta) return;
    var tx = (e.detail || {}).x, ty = (e.detail || {}).y;
    if (tx == null || ty == null) return;
    var best = null, bestD = Infinity;
    for (var s = 0; s < meta.series.length; s++) {
      var pts = meta.series[s].pts;
      for (var i = 0; i < pts.length; i++) {
        var dx = tx - pts[i].x, dy = ty - pts[i].y;
        var d = dx * dx + dy * dy;
        if (d < bestD) { bestD = d; best = { series: meta.series[s].name, date: meta.dates[i], value: meta.series[s].data[i], px: pts[i].x, py: pts[i].y }; }
      }
    }
    if (best && bestD < 3600) {
      this.setData({ trendTip: { show: true, x: best.px, y: best.py - 8, text: best.date + '\n' + best.series + ': ' + best.value } });
      clearTimeout(this._tt0);
      this._tt0 = setTimeout(function() { this.setData({ 'trendTip.show': false }); }.bind(this), 3000);
    }
  },

  onGoodsTap: function(e) {
    var meta = chartMeta.goods;
    if (!meta) return;
    var tx = (e.detail || {}).x, ty = (e.detail || {}).y;
    if (tx == null || ty == null) return;
    for (var i = 0; i < meta.bars.length; i++) {
      var b = meta.bars[i];
      if (tx >= b.x && tx <= b.x + b.w && ty >= b.y && ty <= b.y + b.h) {
        var cx = b.x + b.w / 2, cy = b.y;
        this.setData({ goodsTip: { show: true, x: cx, y: cy - 8, text: meta.names[i] + '\n库存数量: ' + meta.quantities[i] } });
        clearTimeout(this._tt1);
        this._tt1 = setTimeout(function() { this.setData({ 'goodsTip.show': false }); }.bind(this), 3000);
        return;
      }
    }
    this.setData({ 'goodsTip.show': false });
  },

  onTHTap: function(e) {
    var meta = chartMeta.th;
    if (!meta) return;
    var tx = (e.detail || {}).x, ty = (e.detail || {}).y;
    if (tx == null || ty == null) return;
    for (var i = 0; i < meta.tBars.length; i++) {
      var b = meta.tBars[i];
      if (tx >= b.x && tx <= b.x + b.w && ty >= b.y && ty <= b.y + b.h) {
        var cx = b.x + b.w / 2, cy = b.y;
        this.setData({ thTip: { show: true, x: cx, y: cy - 8, text: meta.names[i] + '\n温度: ' + b.val + '°C' } });
        clearTimeout(this._tt2);
        this._tt2 = setTimeout(function() { this.setData({ 'thTip.show': false }); }.bind(this), 3000);
        return;
      }
    }
    for (var i = 0; i < meta.hBars.length; i++) {
      var hb = meta.hBars[i];
      if (tx >= hb.x && tx <= hb.x + hb.w && ty >= hb.y && ty <= hb.y + hb.h) {
        var cx2 = hb.x + hb.w / 2, cy2 = hb.y;
        this.setData({ thTip: { show: true, x: cx2, y: cy2 - 8, text: meta.names[i] + '\n湿度: ' + hb.val + '%' } });
        clearTimeout(this._tt2);
        this._tt2 = setTimeout(function() { this.setData({ 'thTip.show': false }); }.bind(this), 3000);
        return;
      }
    }
    this.setData({ 'thTip.show': false });
  },

  preventBubble: function() {}
});

// ========== Canvas 绘图工具函数 ==========

function drawLine(ctx, data, pad, xStep, chartH, maxVal, color, fill) {
  if (!data || data.length === 0) return [];
  ctx.strokeStyle = color;
  ctx.lineWidth = 2;
  ctx.lineJoin = 'round';
  ctx.beginPath();
  var pts = [];
  for (var i = 0; i < data.length; i++) {
    var x = pad.left + xStep * i;
    var y = pad.top + chartH - (data[i] / maxVal) * chartH;
    if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    pts.push({ x: x, y: y });
  }
  ctx.stroke();
  if (fill && pts.length > 1) {
    ctx.lineTo(pts[pts.length - 1].x, pad.top + chartH);
    ctx.lineTo(pts[0].x, pad.top + chartH);
    ctx.closePath();
    ctx.fillStyle = hexToRgba(color, 0.12);
    ctx.fill();
  }
  for (var j = 0; j < pts.length; j++) {
    ctx.beginPath();
    ctx.arc(pts[j].x, pts[j].y, 3, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 1;
    ctx.stroke();
  }
  return pts;
}

function drawLegend(ctx, w, topY, names, colors) {
  var tw = 0;
  ctx.font = '11px sans-serif';
  for (var i = 0; i < names.length; i++) tw += ctx.measureText(names[i]).width + 28;
  var lx = (w - tw) / 2;
  for (var j = 0; j < names.length; j++) {
    ctx.fillStyle = colors[j];
    ctx.fillRect(lx, topY - 14, 12, 12);
    ctx.fillStyle = '#606266';
    ctx.textAlign = 'left';
    ctx.fillText(names[j], lx + 16, topY - 4);
    lx += ctx.measureText(names[j]).width + 28;
  }
}

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

function hexToRgba(hex, alpha) {
  var r = parseInt(hex.slice(1, 3), 16);
  var g = parseInt(hex.slice(3, 5), 16);
  var b = parseInt(hex.slice(5, 7), 16);
  return 'rgba(' + r + ',' + g + ',' + b + ',' + alpha + ')';
}

function getCapColor(sat) {
  if (sat == null) return '#10b981';
  if (sat < 25) return '#10b981';
  if (sat < 50) return '#e6a23c';
  if (sat < 75) return '#f59e0b';
  return '#f56c6c';
}
