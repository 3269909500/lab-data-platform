import request from '../utils/request'

export const alarmApi = {
  // 确认告警
  confirm(alarmId) {
    return request({
      url: `/monitor-service/alarm-management/alarms/${alarmId}/confirm`,
      method: 'put'
    })
  },

  // 解决告警
  resolve(alarmId, data) {
    return request({
      url: `/monitor-service/alarm-management/alarms/${alarmId}/resolve`,
      method: 'put',
      data
    })
  },

  // 忽略告警
  ignore(alarmId) {
    return request({
      url: `/monitor-service/alarm-management/alarms/${alarmId}/ignore`,
      method: 'put'
    })
  },

  // 查询告警历史
  getHistory(params) {
    return request({
      url: '/monitor-service/alarm-management/alarms/history',
      method: 'get',
      params
    })
  },

  // 获取未处理告警统计
  getUnhandledStats() {
    return request({
      url: '/monitor-service/alarm-management/alarms/unhandled-stats',
      method: 'get'
    })
  },

  // 批量操作告警
  batchAction(data) {
    return request({
      url: '/monitor-service/alarm-management/alarms/batch-action',
      method: 'post',
      data
    })
  }
}