import request from '../utils/request'

/**
 * 报表相关API
 */
export const reportApi = {
  /**
   * 手动触发报表生成
   * @param {Object} params - { startDate, endDate }
   */
  generateReport(params) {
    return request({
      url: '/monitor-service/report/generate',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询任务状态
   * @param {String} taskId - 任务ID
   */
  getTaskStatus(taskId) {
    return request({
      url: `/monitor-service/report/task/${taskId}`,
      method: 'get'
    })
  },

  /**
   * 获取所有任务
   */
  getAllTasks() {
    return request({
      url: '/monitor-service/report/tasks',
      method: 'get'
    })
  },

  /**
   * 下载报表文件
   * @param {String} fileName - 文件名
   */
  downloadReport(fileName) {
    // 直接使用window.open下载，因为需要触发浏览器下载
    const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/monitor-service/report/download/${fileName}`
    window.open(url, '_blank')
  },

  /**
   * 测试接口 - 生成昨日报表
   */
  testGenerateYesterday() {
    return request({
      url: '/monitor-service/report/test/generate-yesterday',
      method: 'get'
    })
  }
}
