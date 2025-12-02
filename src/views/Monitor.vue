<template>
  <div class="monitor">
    <div class="monitor-content">
        <!-- 页面头部 -->
        <div class="page-header">
          <h2>环境监控</h2>
          <div class="header-actions">
            <el-button type="success" @click="testSendNormal">
              <el-icon><Refresh /></el-icon>
              发送正常数据
            </el-button>
            <el-button type="warning" @click="testSendAlarm">
              <el-icon><Warning /></el-icon>
              发送告警数据
            </el-button>
            <el-button type="primary" @click="showUploadDialog">
              <el-icon><Upload /></el-icon>
              上传数据
            </el-button>
          </div>
        </div>

        <!-- 实验室选择和状态 -->
        <el-row :gutter="20" class="status-row">
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-icon normal">
                  <el-icon><Connection /></el-icon>
                </div>
                <div class="status-info">
                  <div class="status-title">在线实验室</div>
                  <div class="status-number">{{ onlineLabs }}</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-icon warning">
                  <el-icon><Warning /></el-icon>
                </div>
                <div class="status-info">
                  <div class="status-title">告警数量</div>
                  <div class="status-number">{{ alarmCount }}</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-icon success">
                  <el-icon><User /></el-icon>
                </div>
                <div class="status-info">
                  <div class="status-title">当前人数</div>
                  <div class="status-number">{{ totalPeople }}</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-icon info">
                  <el-icon><Monitor /></el-icon>
                </div>
                <div class="status-info">
                  <div class="status-title">设备在线</div>
                  <div class="status-number">{{ onlineDevices }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 实时数据表格 -->
        <el-card>
          <template #header>
            <div class="card-header">
              <span>实时监控数据</span>
              <div class="header-controls">
                <el-select
                  v-model="selectedLabId"
                  placeholder="选择实验室"
                  clearable
                  @change="loadMonitorData"
                  style="width: 200px; margin-right: 10px;"
                >
                  <el-option
                    v-for="lab in laboratoryList"
                    :key="lab.id"
                    :label="lab.labName"
                    :value="lab.id"
                  />
                </el-select>
                <el-button type="primary" @click="loadMonitorData">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="monitorData"
            style="width: 100%"
            stripe
          >
            <el-table-column prop="labName" label="实验室" width="150" />
            <el-table-column prop="temperature" label="温度(°C)" width="100">
              <template #default="scope">
                <el-tag :type="getTemperatureType(scope.row.temperature)">
                  {{ scope.row.temperature }}°C
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="humidity" label="湿度(%)" width="100">
              <template #default="scope">
                <el-tag :type="getHumidityType(scope.row.humidity)">
                  {{ scope.row.humidity }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="pm25" label="PM2.5" width="100">
              <template #default="scope">
                <el-tag :type="getPM25Type(scope.row.pm25)">
                  {{ scope.row.pm25 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="illuminance" label="光照(lux)" width="120">
              <template #default="scope">
                <el-tag :type="getIlluminanceType(scope.row.illuminance)">
                  {{ scope.row.illuminance }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="co2" label="CO₂(ppm)" width="100">
              <template #default="scope">
                <el-tag :type="getCO2Type(scope.row.co2)">
                  {{ scope.row.co2 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentPeopleCount" label="当前人数" width="100">
              <template #default="scope">
                <el-tag :type="getPeopleCountType(scope.row.currentPeopleCount)">
                  {{ scope.row.currentPeopleCount }}人
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="onlineDeviceCount" label="设备状态" width="120">
              <template #default="scope">
                <el-progress
                  :percentage="getDevicePercentage(scope.row)"
                  :color="getDeviceColor(scope.row)"
                  :show-text="true"
                  :format="deviceFormat"
                />
              </template>
            </el-table-column>
            <el-table-column prop="monitorTime" label="更新时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.monitorTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="showHistoryDialog(scope.row)"
                >
                  历史数据
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>

        <!-- 数据上传对话框 -->
        <el-dialog
          v-model="uploadDialogVisible"
          title="上传环境数据"
          width="600px"
        >
          <el-form
            ref="uploadFormRef"
            :model="uploadForm"
            :rules="uploadRules"
            label-width="120px"
          >
            <el-form-item label="实验室" prop="labId">
              <el-select
                v-model="uploadForm.labId"
                placeholder="请选择实验室"
                style="width: 100%"
              >
                <el-option
                  v-for="lab in laboratoryList"
                  :key="lab.id"
                  :label="lab.labName"
                  :value="lab.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="温度(°C)" prop="temperature">
              <el-input-number
                v-model="uploadForm.temperature"
                :precision="1"
                :step="0.1"
                :min="-50"
                :max="100"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="湿度(%)" prop="humidity">
              <el-input-number
                v-model="uploadForm.humidity"
                :precision="1"
                :step="0.1"
                :min="0"
                :max="100"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="PM2.5" prop="pm25">
              <el-input-number
                v-model="uploadForm.pm25"
                :precision="1"
                :step="0.1"
                :min="0"
                :max="500"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="光照(lux)" prop="illuminance">
              <el-input-number
                v-model="uploadForm.illuminance"
                :precision="1"
                :step="1"
                :min="0"
                :max="10000"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="CO₂(ppm)" prop="co2">
              <el-input-number
                v-model="uploadForm.co2"
                :precision="1"
                :step="1"
                :min="0"
                :max="5000"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="当前人数" prop="currentPeopleCount">
              <el-input-number
                v-model="uploadForm.currentPeopleCount"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="设备总数" prop="totalDeviceCount">
              <el-input-number
                v-model="uploadForm.totalDeviceCount"
                :min="1"
                :max="100"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="在线设备数" prop="onlineDeviceCount">
              <el-input-number
                v-model="uploadForm.onlineDeviceCount"
                :min="0"
                :max="uploadForm.totalDeviceCount"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <span class="dialog-footer">
              <el-button @click="uploadDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleUpload" :loading="uploading">
                上传
              </el-button>
            </span>
          </template>
        </el-dialog>

        <!-- 历史数据对话框 -->
        <el-dialog
          v-model="historyDialogVisible"
          title="历史数据"
          width="80%"
          top="5vh"
        >
          <div class="history-header">
            <el-date-picker
              v-model="historyDateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="loadHistoryData"
            />
            <el-button type="primary" @click="loadHistoryData">查询</el-button>
          </div>

          <el-table
            v-loading="historyLoading"
            :data="historyData"
            style="width: 100%"
            max-height="400"
          >
            <el-table-column prop="labName" label="实验室" width="150" />
            <el-table-column prop="temperature" label="温度(°C)" width="100" />
            <el-table-column prop="humidity" label="湿度(%)" width="100" />
            <el-table-column prop="pm25" label="PM2.5" width="100" />
            <el-table-column prop="illuminance" label="光照(lux)" width="120" />
            <el-table-column prop="co2" label="CO₂(ppm)" width="100" />
            <el-table-column prop="currentPeopleCount" label="人数" width="100" />
            <el-table-column prop="dataSource" label="数据来源" width="100">
              <template #default="scope">
                <el-tag>{{ scope.row.dataSource }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="monitorTime" label="时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.monitorTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-dialog>
      </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { monitorApi } from '../api/monitor'
import { laboratoryApi } from '../api/laboratory'

const loading = ref(false)
const historyLoading = ref(false)
const uploading = ref(false)
const uploadDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const uploadFormRef = ref()

// 状态数据
const onlineLabs = ref(0)
const alarmCount = ref(0)
const totalPeople = ref(0)
const onlineDevices = ref(0)

// 监控数据
const monitorData = ref([])
const laboratoryList = ref([])
const selectedLabId = ref(null)
const historyData = ref([])
const historyDateRange = ref([])
const currentLab = ref(null)

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 上传表单
const uploadForm = reactive({
  labId: null,
  temperature: 22.0,
  humidity: 55.0,
  pm25: 35.0,
  illuminance: 500.0,
  co2: 650.0,
  currentPeopleCount: 20,
  totalDeviceCount: 25,
  onlineDeviceCount: 23
})

// 上传表单验证规则
const uploadRules = {
  labId: [
    { required: true, message: '请选择实验室', trigger: 'change' }
  ],
  temperature: [
    { required: true, message: '请输入温度', trigger: 'blur' },
    { type: 'number', min: -50, max: 100, message: '温度范围-50~100°C', trigger: 'blur' }
  ],
  humidity: [
    { required: true, message: '请输入湿度', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '湿度范围0~100%', trigger: 'blur' }
  ],
  pm25: [
    { required: true, message: '请输入PM2.5', trigger: 'blur' },
    { type: 'number', min: 0, max: 500, message: 'PM2.5范围0~500', trigger: 'blur' }
  ],
  illuminance: [
    { required: true, message: '请输入光照强度', trigger: 'blur' },
    { type: 'number', min: 0, max: 10000, message: '光照范围0~10000lux', trigger: 'blur' }
  ],
  co2: [
    { required: true, message: '请输入CO2浓度', trigger: 'blur' },
    { type: 'number', min: 0, max: 5000, message: 'CO2范围0~5000ppm', trigger: 'blur' }
  ],
  currentPeopleCount: [
    { required: true, message: '请输入当前人数', trigger: 'blur' },
    { type: 'number', min: 0, message: '人数不能为负数', trigger: 'blur' }
  ],
  totalDeviceCount: [
    { required: true, message: '请输入设备总数', trigger: 'blur' },
    { type: 'number', min: 1, message: '设备总数至少为1', trigger: 'blur' }
  ],
  onlineDeviceCount: [
    { required: true, message: '请输入在线设备数', trigger: 'blur' },
    { type: 'number', min: 0, message: '在线设备数不能为负数', trigger: 'blur' }
  ]
}

// 加载监控数据
const loadMonitorData = async () => {
  try {
    loading.value = true

    const params = {
      current: pagination.current,
      size: pagination.size,
      labId: selectedLabId.value
    }

    const result = await monitorApi.getPageData(params)
    monitorData.value = result.records || []
    pagination.total = result.total || 0

    // 更新统计数据
    updateStats()

  } catch (error) {
    console.error('加载监控数据失败:', error)
    ElMessage.error('加载监控数据失败')
  } finally {
    loading.value = false
  }
}

// 加载实验室列表
const loadLaboratoryList = async () => {
  try {
    const result = await laboratoryApi.getRunning()
    laboratoryList.value = result || []
    onlineLabs.value = laboratoryList.value.length
  } catch (error) {
    console.error('加载实验室列表失败:', error)
  }
}

// 更新统计数据
const updateStats = () => {
  let totalPeopleCount = 0
  let totalOnlineDevices = 0
  let totalDevices = 0

  monitorData.value.forEach(item => {
    totalPeopleCount += item.currentPeopleCount || 0
    totalOnlineDevices += item.onlineDeviceCount || 0
    totalDevices += item.totalDeviceCount || 0
  })

  totalPeople.value = totalPeopleCount
  onlineDevices.value = totalOnlineDevices
}

// 测试发送正常数据
const testSendNormal = async () => {
  try {
    await monitorApi.testSendNormal()
    ElMessage.success('已发送正常测试数据')
    loadMonitorData()
  } catch (error) {
    ElMessage.error('发送测试数据失败')
  }
}

// 测试发送告警数据
const testSendAlarm = async () => {
  try {
    await monitorApi.testSendAlarm()
    ElMessage.success('已发送告警测试数据')
    loadMonitorData()
  } catch (error) {
    ElMessage.error('发送告警数据失败')
  }
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialogVisible.value = true
}

// 处理上传
const handleUpload = async () => {
  if (!uploadFormRef.value) return

  try {
    await uploadFormRef.value.validate()
    uploading.value = true

    // 构造数据
    const lab = laboratoryList.value.find(l => l.id === uploadForm.labId)
    const data = {
      labId: uploadForm.labId,
      labName: lab?.labName || '',
      temperature: uploadForm.temperature,
      humidity: uploadForm.humidity,
      pm25: uploadForm.pm25,
      illuminance: uploadForm.illuminance,
      co2: uploadForm.co2,
      currentPeopleCount: uploadForm.currentPeopleCount,
      totalDeviceCount: uploadForm.totalDeviceCount,
      onlineDeviceCount: uploadForm.onlineDeviceCount,
      dataSource: 'MANUAL'
    }

    await monitorApi.uploadData(data)
    ElMessage.success('数据上传成功')
    uploadDialogVisible.value = false
    loadMonitorData()

  } catch (error) {
    if (error.fields) {
      // 表单验证失败
      return
    }
    ElMessage.error('数据上传失败')
  } finally {
    uploading.value = false
  }
}

// 显示历史数据对话框
const showHistoryDialog = (row) => {
  currentLab.value = row
  historyDialogVisible.value = true

  // 设置默认时间范围为最近24小时
  const now = new Date()
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000)
  historyDateRange.value = [
    formatDate(yesterday),
    formatDate(now)
  ]

  loadHistoryData()
}

// 加载历史数据
const loadHistoryData = async () => {
  if (!currentLab.value || !historyDateRange.value || historyDateRange.value.length !== 2) {
    return
  }

  try {
    historyLoading.value = true

    const params = {
      startTime: historyDateRange.value[0],
      endTime: historyDateRange.value[1]
    }

    const result = await monitorApi.getHistoryData(currentLab.value.labId, params)
    historyData.value = result || []

  } catch (error) {
    console.error('加载历史数据失败:', error)
    ElMessage.error('加载历史数据失败')
  } finally {
    historyLoading.value = false
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadMonitorData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadMonitorData()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const formatDate = (date) => {
  return date.toISOString().slice(0, 19).replace('T', ' ')
}

// 数据状态判断方法
const getTemperatureType = (temp) => {
  if (temp >= 35 || temp <= 10) return 'danger'
  if (temp >= 30 || temp <= 15) return 'warning'
  return 'success'
}

const getHumidityType = (humidity) => {
  if (humidity >= 80 || humidity <= 30) return 'danger'
  if (humidity >= 70 || humidity <= 40) return 'warning'
  return 'success'
}

const getPM25Type = (pm25) => {
  if (pm25 >= 150) return 'danger'
  if (pm25 >= 75) return 'warning'
  return 'success'
}

const getIlluminanceType = (illuminance) => {
  if (illuminance <= 200) return 'danger'
  if (illuminance <= 300) return 'warning'
  return 'success'
}

const getCO2Type = (co2) => {
  if (co2 >= 1500) return 'danger'
  if (co2 >= 1000) return 'warning'
  return 'success'
}

const getPeopleCountType = (count) => {
  if (count >= 50) return 'danger'
  if (count >= 40) return 'warning'
  return 'success'
}

const getDevicePercentage = (row) => {
  const total = row.totalDeviceCount || 1
  return Math.round((row.onlineDeviceCount || 0) / total * 100)
}

const getDeviceColor = (row) => {
  const percentage = getDevicePercentage(row)
  if (percentage < 50) return '#f56c6c'
  if (percentage < 80) return '#e6a23c'
  return '#67c23a'
}

const deviceFormat = (percentage) => {
  return `${percentage}%`
}

onMounted(() => {
  loadLaboratoryList()
  loadMonitorData()
})
</script>

<style scoped>
.monitor-content {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.status-row {
  margin-bottom: 20px;
}

.status-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.status-item {
  display: flex;
  align-items: center;
}

.status-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.status-icon.normal {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.status-icon.warning {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.status-icon.success {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.status-icon.info {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.status-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.history-header {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #fafafa;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-progress-bar__outer) {
  background-color: #f0f0f0;
}

:deep(.el-progress-bar__inner) {
  transition: all 0.3s ease;
}
</style>