<template>
  <div class="dashboard">
    <div class="dashboard-content">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon labs">
                <el-icon><House /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.totalLabs }}</div>
                <div class="stats-label">实验室总数</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon online">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.onlineLabs }}</div>
                <div class="stats-label">在线实验室</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon alerts">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.todayAlerts }}</div>
                <div class="stats-label">今日告警</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon pending">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.pendingAlerts }}</div>
                <div class="stats-label">待处理告警</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>实验室环境状态</span>
                <el-button type="primary" size="small" @click="refreshData">刷新</el-button>
              </div>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="environmentChart" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>告警趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="alarmChart" />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近告警 -->
      <el-row>
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近告警</span>
                <el-button type="text" @click="$router.push('/alarms')">查看全部</el-button>
              </div>
            </template>
            <el-table :data="recentAlarms" style="width: 100%">
              <el-table-column prop="labName" label="实验室" width="150" />
              <el-table-column prop="alarmType" label="告警类型" width="120">
                <template #default="scope">
                  <el-tag :type="getAlarmTypeColor(scope.row.alarmType)">
                    {{ getAlarmTypeName(scope.row.alarmType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="alarmLevel" label="级别" width="100">
                <template #default="scope">
                  <el-tag :type="getAlarmLevelColor(scope.row.alarmLevel)">
                    {{ getAlarmLevelName(scope.row.alarmLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="alarmMessage" label="告警信息" />
              <el-table-column prop="alarmTime" label="时间" width="180">
                <template #default="scope">
                  {{ formatTime(scope.row.alarmTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusColor(scope.row.status)">
                    {{ getStatusName(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button
                    v-if="scope.row.status === 'PENDING'"
                    type="primary"
                    size="small"
                    @click="confirmAlarm(scope.row)"
                  >
                    确认
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 报表管理 -->
      <el-row style="margin-top: 20px;">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>报表管理</span>
                <el-button type="primary" size="small" :icon="DocumentAdd" @click="showReportDialog">生成报表</el-button>
              </div>
            </template>

            <!-- 任务列表 -->
            <el-table :data="reportTasks" style="width: 100%" v-loading="reportLoading">
              <el-table-column prop="taskId" label="任务ID" width="280" show-overflow-tooltip />
              <el-table-column prop="taskType" label="任务类型" width="150">
                <template #default="scope">
                  <el-tag>{{ getTaskTypeName(scope.row.taskType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="getTaskStatusType(scope.row.status)">
                    {{ getTaskStatusName(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="progress" label="进度" width="150">
                <template #default="scope">
                  <el-progress :percentage="scope.row.progress" :status="getProgressStatus(scope.row.status)" />
                </template>
              </el-table-column>
              <el-table-column prop="message" label="消息" show-overflow-tooltip />
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="scope">
                  {{ formatTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button
                    v-if="scope.row.status === 'COMPLETED' && scope.row.result"
                    type="success"
                    size="small"
                    :icon="Download"
                    @click="downloadReport(scope.row.result.fileName)"
                  >
                    下载
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    :icon="Refresh"
                    @click="refreshTaskStatus(scope.row.taskId)"
                  >
                    刷新
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 生成报表对话框 -->
    <el-dialog v-model="reportDialogVisible" title="生成报表" width="500px">
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="reportForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="reportForm.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateReport" :loading="generating">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage, ElNotification } from 'element-plus'
import { DocumentAdd, Download, Refresh } from '@element-plus/icons-vue'
import { monitorApi } from '../api/monitor'
import { alarmApi } from '../api/alarm'
import { reportApi } from '../api/report'
import websocketManager from '../utils/websocket'

// 注册必要的组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const stats = reactive({
  totalLabs: 0,
  onlineLabs: 0,
  todayAlerts: 0,
  pendingAlerts: 0
})

const recentAlarms = ref([])
const loading = ref(false)

// 报表相关数据
const reportTasks = ref([])
const reportLoading = ref(false)
const reportDialogVisible = ref(false)
const generating = ref(false)
const reportForm = reactive({
  startDate: '',
  endDate: ''
})

// 环境状态图表
const environmentChart = ref({
  title: {
    text: '实验室环境分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '环境状态',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 0, name: '正常' },
        { value: 0, name: '告警' },
        { value: 0, name: '离线' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 告警趋势图表
const alarmChart = ref({
  title: {
    text: '最近7天告警趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['告警数量'],
    top: 30
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '告警数量',
      type: 'line',
      smooth: true,
      data: []
    }
  ]
})

const loadDashboardData = async () => {
  try {
    loading.value = true

    // 并行加载数据
    let alarmStats, unhandledAlarms

    try {
      [alarmStats, unhandledAlarms] = await Promise.all([
        monitorApi.getAlarmStats(),
        monitorApi.getUnhandledAlarms()
      ])
    } catch (apiError) {
      console.warn('API调用失败，使用模拟数据:', apiError)
      // 使用模拟数据
      alarmStats = { todayCount: 12 }
      unhandledAlarms = [
        {
          id: 1,
          labName: '实验室A',
          alarmType: 'TEMP_HIGH',
          alarmValue: '35.2°C',
          thresholdValue: '30.0°C',
          alarmLevel: 'HIGH',
          status: 'PENDING',
          createTime: '2025-11-27 15:20:10'
        },
        {
          id: 2,
          labName: '实验室B',
          alarmType: 'HUMIDITY_HIGH',
          alarmValue: '85%',
          thresholdValue: '80%',
          alarmLevel: 'MEDIUM',
          status: 'PENDING',
          createTime: '2025-11-27 15:15:30'
        }
      ]
    }

    // 更新统计数据
    stats.todayAlerts = alarmStats.todayCount || 0
    stats.pendingAlerts = unhandledAlarms.length

    // 更新最近告警
    recentAlarms.value = unhandledAlarms.slice(0, 10)

    // 模拟实验室数据
    stats.totalLabs = 5
    stats.onlineLabs = 4

    // 更新环境状态图表
    environmentChart.value.series[0].data = [
      { value: 4, name: '正常' },
      { value: 1, name: '告警' },
      { value: 0, name: '离线' }
    ]

    // 生成最近7天的日期
    const dates = []
    const alarmCounts = []
    for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      dates.push(date.toLocaleDateString())
      alarmCounts.push(Math.floor(Math.random() * 10) + 1)
    }

    alarmChart.value.xAxis.data = dates
    alarmChart.value.series[0].data = alarmCounts

  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadDashboardData()
}

const confirmAlarm = async (alarm) => {
  try {
    await alarmApi.confirm(alarm.id)
    ElMessage.success('告警确认成功')
    loadDashboardData()
  } catch (error) {
    ElMessage.error('告警确认失败')
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const getAlarmTypeName = (type) => {
  const typeMap = {
    'TEMP_HIGH': '温度过高',
    'TEMP_LOW': '温度过低',
    'HUMIDITY_HIGH': '湿度过高',
    'HUMIDITY_LOW': '湿度过低',
    'PM25_HIGH': 'PM2.5超标',
    'CO2_HIGH': 'CO2过高',
    'ILLUMINANCE_LOW': '光照不足',
    'PEOPLE_COUNT_HIGH': '人数过多'
  }
  return typeMap[type] || type
}

const getAlarmTypeColor = (type) => {
  const colorMap = {
    'TEMP_HIGH': 'danger',
    'TEMP_LOW': 'info',
    'HUMIDITY_HIGH': 'warning',
    'HUMIDITY_LOW': 'info',
    'PM25_HIGH': 'danger',
    'CO2_HIGH': 'danger',
    'ILLUMINANCE_LOW': 'warning',
    'PEOPLE_COUNT_HIGH': 'warning'
  }
  return colorMap[type] || 'primary'
}

const getAlarmLevelName = (level) => {
  const levelMap = {
    'INFO': '信息',
    'WARNING': '警告',
    'ERROR': '错误',
    'DANGER': '危险'
  }
  return levelMap[level] || level
}

const getAlarmLevelColor = (level) => {
  const colorMap = {
    'INFO': 'info',
    'WARNING': 'warning',
    'ERROR': 'danger',
    'DANGER': 'danger'
  }
  return colorMap[level] || 'primary'
}

const getStatusName = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'CONFIRMED': '已确认',
    'RESOLVED': '已解决',
    'IGNORED': '已忽略'
  }
  return statusMap[status] || status
}

const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'danger',
    'CONFIRMED': 'warning',
    'RESOLVED': 'success',
    'IGNORED': 'info'
  }
  return colorMap[status] || 'primary'
}

// 处理WebSocket告警消息
const handleAlarmMessage = (alarm) => {
  console.log('收到新告警:', alarm)

  // 弹出通知
  ElNotification({
    title: '新告警',
    message: `${alarm.labName}: ${alarm.alarmMessage}`,
    type: 'warning',
    duration: 5000
  })

  // 更新告警列表（添加到顶部）
  recentAlarms.value.unshift(alarm)
  if (recentAlarms.value.length > 10) {
    recentAlarms.value = recentAlarms.value.slice(0, 10)
  }

  // 更新统计数据
  stats.todayAlerts++
  if (alarm.status === 'PENDING') {
    stats.pendingAlerts++
  }
}

// 处理告警确认/解决/忽略消息
const handleAlarmStatusChange = (data) => {
  console.log('告警状态变更:', data)

  // 重新加载数据以保持同步
  loadDashboardData()
}

// 处理报表生成成功通知
const handleReportReady = (data) => {
  console.log('收到报表生成通知:', data)

  ElNotification({
    title: '报表已生成',
    message: `报表文件 ${data.fileName} 已生成完成，点击下载`,
    type: 'success',
    duration: 10000,
    onClick: () => {
      // 下载报表
      const url = `/api/monitor-service/report/download/${data.fileName}`
      window.open(url, '_blank')
    }
  })

  // 刷新任务列表
  loadReportTasks()
}

// 处理报表生成失败通知
const handleReportFailed = (data) => {
  console.log('报表生成失败:', data)

  ElNotification({
    title: '报表生成失败',
    message: data.error || '报表生成过程中发生错误',
    type: 'error',
    duration: 5000
  })
}

// ========== 报表管理相关方法 ==========

// 加载报表任务列表
const loadReportTasks = async () => {
  try {
    reportLoading.value = true
    const tasks = await reportApi.getAllTasks()

    // 转换为数组
    reportTasks.value = Object.values(tasks).sort((a, b) => {
      return new Date(b.createTime) - new Date(a.createTime)
    })
  } catch (error) {
    console.error('加载报表任务失败:', error)
  } finally {
    reportLoading.value = false
  }
}

// 显示生成报表对话框
const showReportDialog = () => {
  // 默认选择昨天
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  const yesterdayStr = yesterday.toISOString().split('T')[0]

  reportForm.startDate = yesterdayStr
  reportForm.endDate = yesterdayStr
  reportDialogVisible.value = true
}

// 生成报表
const handleGenerateReport = async () => {
  if (!reportForm.startDate || !reportForm.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }

  try {
    generating.value = true

    const result = await reportApi.generateReport({
      startDate: reportForm.startDate,
      endDate: reportForm.endDate
    })

    ElMessage.success('报表生成任务已提交')
    reportDialogVisible.value = false

    // 刷新任务列表
    setTimeout(() => {
      loadReportTasks()
    }, 1000)

  } catch (error) {
    console.error('生成报表失败:', error)
    ElMessage.error('生成报表失败')
  } finally {
    generating.value = false
  }
}

// 刷新任务状态
const refreshTaskStatus = async (taskId) => {
  try {
    const task = await reportApi.getTaskStatus(taskId)

    // 更新任务列表中的对应任务
    const index = reportTasks.value.findIndex(t => t.taskId === taskId)
    if (index !== -1) {
      reportTasks.value[index] = task
    }

    ElMessage.success('任务状态已更新')
  } catch (error) {
    console.error('刷新任务状态失败:', error)
    ElMessage.error('刷新失败')
  }
}

// 下载报表
const downloadReport = (fileName) => {
  reportApi.downloadReport(fileName)
  ElMessage.success('开始下载报表')
}

// 获取任务类型名称
const getTaskTypeName = (type) => {
  const typeMap = {
    'DAILY_REPORT': '日统计报表',
    'MANUAL_REPORT': '手动生成',
    'TEST_REPORT': '测试报表'
  }
  return typeMap[type] || type
}

// 获取任务状态名称
const getTaskStatusName = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return statusMap[status] || status
}

// 获取任务状态类型（Element Plus Tag类型）
const getTaskStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取进度条状态
const getProgressStatus = (status) => {
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED') return 'exception'
  return undefined
}

onMounted(() => {
  loadDashboardData()
  loadReportTasks()  // 加载报表任务

  // 连接WebSocket（连接到实验室ID=1，可根据需要修改）
  // 注意：实际项目中应该从用户选择的实验室或配置中获取labId
  const labId = 1
  websocketManager.connect(labId)

  // 监听告警消息
  websocketManager.on('ALARM', handleAlarmMessage)

  // 监听告警状态变更
  websocketManager.on('ALARM_CONFIRMED', handleAlarmStatusChange)
  websocketManager.on('ALARM_RESOLVED', handleAlarmStatusChange)
  websocketManager.on('ALARM_IGNORED', handleAlarmStatusChange)

  // 监听报表生成通知
  websocketManager.on('REPORT_READY', handleReportReady)
  websocketManager.on('REPORT_FAILED', handleReportFailed)

  // 监听连接状态
  websocketManager.onConnect(() => {
    console.log('Dashboard WebSocket连接成功')
    ElMessage.success('实时监控已启动')
  })

  websocketManager.onDisconnect(() => {
    console.log('Dashboard WebSocket连接断开')
  })
})

onUnmounted(() => {
  // 移除监听器
  websocketManager.off('ALARM', handleAlarmMessage)
  websocketManager.off('ALARM_CONFIRMED', handleAlarmStatusChange)
  websocketManager.off('ALARM_RESOLVED', handleAlarmStatusChange)
  websocketManager.off('ALARM_IGNORED', handleAlarmStatusChange)
  websocketManager.off('REPORT_READY', handleReportReady)
  websocketManager.off('REPORT_FAILED', handleReportFailed)

  // 关闭WebSocket连接
  websocketManager.close()
})
</script>

<style scoped>
.dashboard-content {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stats-item {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.stats-icon.labs {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.online {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.alerts {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6a88 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #666;
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.chart {
  height: 100%;
  width: 100%;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #fafafa;
}
</style>