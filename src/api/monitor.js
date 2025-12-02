import request from '../utils/request'

export const monitorApi = {
  // 上传环境数据
  uploadData(data) {
    return request({
      url: '/monitor-service/lab-monitor/data',
      method: 'post',
      data
    })
  },

  // 批量上传环境数据
  uploadBatchData(dataList) {
    return request({
      url: '/monitor-service/lab-monitor/data/batch',
      method: 'post',
      data: dataList
    })
  },

  // 获取最新监测数据
  getLatestData(labId) {
    return request({
      url: `/monitor-service/lab-monitor/latest/${labId}`,
      method: 'get'
    })
  },

  // 查询历史数据
  getHistoryData(labId, params) {
    return request({
      url: `/monitor-service/lab-monitor/history/${labId}`,
      method: 'get',
      params
    })
  },

  // 分页查询监测数据
  getPageData(params) {
    return request({
      url: '/monitor-service/lab-monitor/page',
      method: 'get',
      params
    })
  },

  // 获取告警列表
  getAlarmList(params) {
    return request({
      url: '/monitor-service/lab-monitor/alarms/list',
      method: 'get',
      params
    })
  },

  // 获取未处理告警
  getUnhandledAlarms() {
    return request({
      url: '/monitor-service/lab-monitor/alarms/unhandled',
      method: 'get'
    })
  },

  // 获取某个实验室的告警
  getAlarmsByLab(labId) {
    return request({
      url: `/monitor-service/lab-monitor/alarms/station/${labId}`,
      method: 'get'
    })
  },

  // 获取告警统计
  getAlarmStats() {
    return request({
      url: '/monitor-service/lab-monitor/alarms/stats',
      method: 'get'
    })
  },

  // 获取今日统计
  getTodayStats() {
    return request({
      url: '/monitor-service/lab-monitor/stats/today',
      method: 'get'
    })
  },

  // 获取某个实验室的统计
  getStatsByLab(labId, params) {
    return request({
      url: `/monitor-service/lab-monitor/stats/lab/${labId}`,
      method: 'get',
      params
    })
  },

  // 获取统计列表
  getStatsList(params) {
    return request({
      url: '/monitor-service/lab-monitor/stats/list',
      method: 'get',
      params
    })
  },

  // 健康检查
  health() {
    return request({
      url: '/monitor-service/lab-monitor/health',
      method: 'get'
    })
  },

  // 测试接口
  test() {
    return request({
      url: '/monitor-service/lab-monitor/test',
      method: 'get'
    })
  },

  // 发送正常测试数据
  testSendNormal() {
    return request({
      url: '/monitor-service/lab-monitor/test-send-normal',
      method: 'get'
    })
  },

  // 发送告警测试数据
  testSendAlarm() {
    return request({
      url: '/monitor-service/lab-monitor/test-send-alarm',
      method: 'get'
    })
  },

  // 批量发送测试数据
  testSendBatch(params) {
    return request({
      url: '/monitor-service/lab-monitor/test-send-batch',
      method: 'get',
      params
    })
  },

  // WebSocket连接统计
  getWebSocketStats() {
    return request({
      url: '/monitor-service/lab-monitor/websocket/stats',
      method: 'get'
    })
  }
}