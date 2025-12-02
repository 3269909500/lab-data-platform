import request from '../utils/request'

export const laboratoryApi = {
  // 获取实验室列表（分页）
  getPage(params) {
    return request({
      url: '/system-service/laboratory/page',
      method: 'get',
      params
    })
  },

  // 获取运行中的实验室
  getRunning() {
    return request({
      url: '/system-service/laboratory/running',
      method: 'get'
    })
  },

  // 根据ID获取实验室
  getById(id) {
    return request({
      url: `/system-service/laboratory/${id}`,
      method: 'get'
    })
  },

  // 新增实验室
  create(data) {
    return request({
      url: '/system-service/laboratory',
      method: 'post',
      data
    })
  },

  // 更新实验室
  update(data) {
    return request({
      url: '/system-service/laboratory',
      method: 'put',
      data
    })
  },

  // 删除实验室
  delete(id) {
    return request({
      url: `/system-service/laboratory/${id}`,
      method: 'delete'
    })
  }
}