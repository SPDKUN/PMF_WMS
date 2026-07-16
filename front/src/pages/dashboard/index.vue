<template>
  <div class="dashboard">
    <!-- 上半部分：图表视图切换 -->
    <div class="chart-card">
      <div class="chart-tab-row">
        <!-- 两个切换按钮 -->
         <!-- 点击调用trend/goods，activeChart为trend/goods时高亮 -->
        <button :class="{ active: activeChart === 'trend' }" @click="switchChart('trend')">出入库趋势</button>
        <button :class="{ active: activeChart === 'goods' }" @click="switchChart('goods')">货物库存数量</button>
      </div>
      <!-- 折线图 -->
      <div v-show="activeChart === 'trend'" ref="lineChartRef" class="chart-container"></div>
      <!-- 柱状图 -->
      <div v-show="activeChart === 'goods'" ref="barChartRef" class="chart-container"></div>
    </div>

    <!-- 下半部分：温湿度 + 容量饱和度 -->
    <div class="bottom-section">
      <div class="dashboard-card">
        <h3 class="card-title">仓库温湿度记录</h3>
        <div ref="tempHumidityChartRef" class="chart-container chart-sm"></div>
      </div>
      <div class="dashboard-card">
        <h3 class="card-title">仓库容量饱和度</h3>
        <div class="capacity-list">
          <!-- 遍历数组，每条数据生成一个容量条目 -->
          <div v-for="item in capacityList" :key="item.warehouseName" class="capacity-item">
            <!-- 包裹仓库名称和使用量信息 -->
            <div class="capacity-label">
              <span>{{ item.warehouseName }}</span>
              <span>{{ item.usedSlots }} / {{ item.totalSlots }}</span>
            </div>
            <!-- 进度条 -->
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: item.saturation + '%', backgroundColor: getCapacityColor(item.saturation) }"
              ></div>
            </div>
            <span class="capacity-percent">{{ item.saturation }}%</span>
          </div>
          <div v-if="capacityList.length === 0" class="empty-hint">暂无仓库数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request.js'

export default {
  name: 'Dashboard',
  data() {
    return {
      activeChart: 'trend',//当前上半部分显示图形，默认为出入库趋势
      lineChart: null,//出入库趋势折线图
      barChart: null,//货物数量柱状图
      tempHumidityChart: null,//温湿度柱状图
      lineChartData: null,//出入库趋势图数据
      barChartData: null,//货物数量数据
      tempHumidityData: null,//温湿度数据
      capacityList: [],//仓库数据列表
      barChartReady: false,//标记货物库存图表是否已初始化，避免重复初始化
    }
  },
  //生命期钩子
  mounted() {
    //请求所有看板数据
    this.fetchAllData()
    //监听窗口大小变化事件，以便图表自适应大小
    window.addEventListener('resize', this.handleResize)
  },
  //组件卸载前的生命周期钩子
  beforeUnmount() {
    //移除窗口大小监听器，以防内存泄漏
    window.removeEventListener('resize', this.handleResize)
    //如果图表存在，摧毁图表并释放资源
    if (this.lineChart) this.lineChart.dispose()
    if (this.barChart) this.barChart.dispose()
    if (this.tempHumidityChart) this.tempHumidityChart.dispose()
  },
  methods: {
    async fetchAllData() {
      try {
        //请求所有数据接口
        const [lineRes, barRes, tempRes, capRes] = await Promise.all([
          request.get('/dashboard/weeklyInboundOutbound'),
          request.get('/dashboard/goodsQuantity'),
          request.get('/dashboard/warehouseTempHumidity'),
          request.get('/dashboard/warehouseCapacity'),
        ])

        //返回值为200
        if (lineRes.code === 200) {
          this.lineChartData = lineRes.data
          //等 DOM 更新后（$nextTick）再初始化图表，确保图表容器已渲染。
          this.$nextTick(() => this.initLineChart())
        }
        if (barRes.code === 200) {
          this.barChartData = barRes.data
        }
        if (tempRes.code === 200) {
          this.tempHumidityData = tempRes.data
          //保存温湿度数据并在 DOM 更新后初始化图表。
          this.$nextTick(() => this.initTempHumidityChart())
        }
        if (capRes.code === 200) {
          this.capacityList = capRes.data.warehouses || []
        }
      } catch (e) {
        console.error('获取看板数据失败:', e)
      }
    },

    //初始化出入库趋势折线图
    initLineChart() {
      //无数据就直接返回
      if (!this.lineChartData) return
      const container = this.$refs.lineChartRef //通过ref获取DOM容器
      if (!container) return //容器不存在则返回，避免错误

      //如果已经存在旧实例，那么先摧毁旧的
      if (this.lineChart) this.lineChart.dispose()
      //初始化
      this.lineChart = echarts.init(container)

      //提取日期，出入库数量
      const { dates, inbound, outbound } = this.lineChartData
      this.lineChart.setOption({
        //提示框配置：触发类型（鼠标悬停触发），背景颜色，边框颜色，文字颜色
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#5ab1ef', textStyle: { color: '#333' } },
        //图例组件：数据，距离容器顶部距离，文字颜色
        legend: { data: ['入库数量', '出库数量'], top: 4, textStyle: { color: '#606266' } },
        //绘图网格
        grid: { left: '3%', right: '4%', top: 30, bottom: 0, containLabel: true },
        xAxis: {
          type: 'category',//类目轴
          data: dates,//x轴为日期
          axisLabel: { formatter: (v) => v.slice(5) },//取日期字符串后五位
        },
        yAxis: { type: 'value', name: '数量' },//y轴为数量
        series: [//包含两个折线图
          {
            name: '入库数量',
            type: 'line',
            data: inbound,
            smooth: true,//平滑曲线
            lineStyle: { color: '#5ab1ef', width: 2 },
            itemStyle: { color: '#5ab1ef' },
            areaStyle: { color: 'rgba(90,177,239,0.15)' },
          },
          {
            name: '出库数量',
            type: 'line',
            data: outbound,
            smooth: true,
            lineStyle: { color: '#b6a2de', width: 2 },
            itemStyle: { color: '#b6a2de' },
            areaStyle: { color: 'rgba(182,162,222,0.15)' },
          },
        ],
      })
    },

    //初始化货物数量柱状图
    initBarChart() {
      //无数据直接返回
      if (!this.barChartData) return
      const container = this.$refs.barChartRef//获取DOM容器
      //容器为空直接返回
      if (!container) return

      //如果旧表存在，摧毁旧表
      if (this.barChart) this.barChart.dispose()
      // 初始化
      this.barChart = echarts.init(container)

      const { names, quantities } = this.barChartData
      this.barChart.setOption({
        //提示框
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#10b981', textStyle: { color: '#333' } },
        //绘图网格
        grid: { left: '3%', right: '4%', top: 20, bottom: 35, containLabel: true },
        xAxis: {
          type: 'category',//x轴为类别
          data: names,
          //如果货物名称超过6个，就使标签旋转30度
          axisLabel: { rotate: names.length > 6 ? 30 : 0, fontSize: 12 },
        },
        yAxis: { type: 'value', name: '数量' },//y轴为数量
        series: [
          {
            name: '库存数量',
            type: 'bar',
            data: quantities,
            barMaxWidth: 36,//最大宽度
            itemStyle: {
              //设置渐变颜色
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#10b981' },
                { offset: 1, color: '#6ee7b7' },
              ]),
              //圆角矩形
              borderRadius: [4, 4, 0, 0],
            },
          },
        ],
      })
      this.barChartReady = true//标记已经初始化
    },

    //初始化温湿度柱状图
    initTempHumidityChart() {
      //无数据，返回
      if (!this.tempHumidityData) return
      const container = this.$refs.tempHumidityChartRef//获取DOM容器
      //容器为空，返回
      if (!container) return

      //有旧表先摧毁旧表
      if (this.tempHumidityChart) this.tempHumidityChart.dispose()
      //初始化
      this.tempHumidityChart = echarts.init(container)

      const { names, temperatures, humidities } = this.tempHumidityData

      // ---- 计算双Y轴范围，使温度与湿度的零刻度线对齐 ----
      const tVals = temperatures.filter(v => v != null && !isNaN(v))
      const hVals = humidities.filter(v => v != null && !isNaN(v))

      // 数据边界（确保包含0）
      const tDataMin = tVals.length > 0 ? Math.min(0, ...tVals) : 0
      const tDataMax = tVals.length > 0 ? Math.max(0, ...tVals) : 0
      const hDataMin = hVals.length > 0 ? Math.min(0, ...hVals) : 0
      const hDataMax = hVals.length > 0 ? Math.max(0, ...hVals) : 0

      // 加 10% 内边距，让条形图顶端留白
      const pad = (min, max) => Math.max((max - min) * 0.1, 2)
      let tMin = tDataMin - pad(tDataMin, tDataMax)
      let tMax = tDataMax + pad(tDataMin, tDataMax)
      let hMin = hDataMin - pad(hDataMin, hDataMax)
      let hMax = hDataMax + pad(hDataMin, hDataMax)

      // 计算零刻度在各轴上的比例位置（0 = 轴底部, 1 = 轴顶部）
      const zeroRatio = (min, max) => {
        const range = max - min
        if (range <= 0) return 0
        return (0 - min) / range
      }
      const tZeroR = zeroRatio(tMin, tMax)
      const hZeroR = zeroRatio(hMin, hMax)

      // 统一零刻度线比例（取零上空间需求更大的一方）
      const targetR = Math.max(tZeroR, hZeroR)

      // 调整 min 使零线对齐：targetR = (0 - newMin) / (max - newMin)
      // → newMin = targetR × max / (targetR - 1)
      const adjustMin = (max, r) => {
        if (r <= 0) return 0  // 零在底部，无需向下扩展
        return (r * max) / (r - 1)
      }

      if (targetR > tZeroR && targetR < 1) {
        tMin = adjustMin(tMax, targetR)
      }
      if (targetR > hZeroR && targetR < 1) {
        hMin = adjustMin(hMax, targetR)
      }

      this.tempHumidityChart.setOption({
        //提示框
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#67e0e3', textStyle: { color: '#333' } },
        //图例
        legend: { data: ['温度(°C)', '湿度(%)'], top: 4, textStyle: { color: '#606266' } },
        //绘图网格
        grid: { left: '3%', right: '4%', top: 30, bottom: 0, containLabel: true },
        xAxis: { type: 'category', data: names },//x轴为仓库名称
        yAxis: [
          { type: 'value', name: '°C', min: tMin, max: tMax },
          {
            type: 'value', name: '%', min: hMin, max: hMax,
            axisLabel: {
              formatter: (v) => v < 0 ? '' : v  // 湿度没有负数
            }
          },
        ],
        series: [
          {
            name: '温度(°C)',
            type: 'bar',
            yAxisIndex: 0,//使用左边的y轴
            barWidth: '35%',//柱子宽度
            barGap: '30%',//间隔
            data: temperatures,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#ff3b3b' },
                { offset: 1, color: '#ff9999' },
              ]),
              borderRadius: [4, 4, 0, 0],
            },
          },
          {
            name: '湿度(%)',
            type: 'bar',
            barWidth: '35%',
            yAxisIndex: 1,//使用右边的y轴
            data: humidities,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#5ab1ef' },
                { offset: 1, color: '#add8f7' },
              ]),
              borderRadius: [4, 4, 0, 0],
            },
          },
        ],
      })
    },

    // 切换视图
    switchChart(name) {
      this.activeChart = name
      if (name === 'trend') {
        this.$nextTick(() => {
          if (this.lineChart) this.lineChart.resize()
        })
      } else if (name === 'goods') {
        //未初始化则先初始化
        if (!this.barChartReady) {
          this.$nextTick(() => this.initBarChart())
        } else {
          this.$nextTick(() => {
            if (this.barChart) this.barChart.resize()
          })
        }
      }
    },

    //浏览器大小改变时要跟随变化大小
    handleResize() {
      if (this.lineChart) this.lineChart.resize()
      if (this.barChart) this.barChart.resize()
      if (this.tempHumidityChart) this.tempHumidityChart.resize()
    },

    //根据仓库饱和度返回进度条颜色
    getCapacityColor(saturation) {
      if (saturation < 25) return '#10b981'//绿色
      if (saturation < 50) return '#e6a23c'//橙色
      if (saturation < 75) return '#f59e0b'//琥珀色
      return '#f56c6c'//红色
    },
  },
}
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 上半部分 */
.chart-card {
  background: var(--card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  padding: 12px 16px 0;
  box-shadow: var(--shadow-sm);
}

.chart-tab-row {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}
.chart-tab-row button {
  padding: 6px 16px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--foreground-regular);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.chart-tab-row button:hover {
  background: var(--border-light);
}
.chart-tab-row button.active {
  background: var(--primary-bg);
  color: var(--primary);
}

.chart-container {
  width: 100%;
  height: 220px;
  overflow: hidden;
}
.chart-sm { height: 240px; }

/* 下半部分 */
.bottom-section {
  display: flex;
  gap: 16px;
}

.dashboard-card {
  flex: 1;
  background: var(--card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  padding: 14px 18px;
  box-shadow: var(--shadow-sm);
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--foreground);
  margin: 0 0 10px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.capacity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.capacity-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.capacity-label {
  width: 120px;
  font-size: 13px;
  color: var(--foreground-regular);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.capacity-label span:last-child {
  font-size: 11px;
  color: var(--foreground-muted);
}

.progress-bar {
  flex: 1;
  height: 16px;
  background: hsl(0, 0%, 95%);
  border-radius: 8px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 8px;
  transition: width 0.6s ease;
}
.capacity-percent {
  width: 50px;
  font-size: 13px;
  font-weight: 500;
  color: var(--foreground-regular);
  text-align: right;
}

.empty-hint {
  text-align: center;
  color: var(--foreground-muted);
  padding: 40px 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .bottom-section {
    flex-direction: column;
  }
  .capacity-label {
    width: 90px;
  }
}
</style>
