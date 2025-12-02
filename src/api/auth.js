import request from '../utils/request'

export const authApi = {
  // 用户登录
  login(data) {
    return request({
      url: '/auth-service/auth/login',
      method: 'post',
      data
    })
  },

  // 用户注册
  register(data) {
    return request({
      url: '/auth-service/auth/register',
      method: 'post',
      data
    })
  },

  // 验证token
  validateToken(token) {
    return request({
      url: '/auth-service/auth/validate',
      method: 'post',
      params: { token }
    })
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request({
      url: '/auth-service/auth/userinfo',
      method: 'get'
    })
  },

  // 健康检查
  health() {
    return request({
      url: '/auth-service/auth/health',
      method: 'get'
    })
  }
}