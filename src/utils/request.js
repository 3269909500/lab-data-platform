import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data

    if (code === 200) {
      return data
    } else if (code === 401) {
      ElMessage.error(message || '登录失败')
      return Promise.reject(new Error(message || '登录失败'))
    } else {
      ElMessage.error('登录失败，状态码：' + code)
      return Promise.reject(new Error('请求失败，状态码：' + code))
    }
  },
  error => {
    if (error.response?.status === 401) {
      // 清除本地存储的用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')

      // 简单重定向到登录页面，避免在登录页面重复跳转
      if (window.location.pathname !== '/login') {
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request