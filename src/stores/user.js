import { defineStore } from 'pinia'
import { authApi } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => {
    try {
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');

      // 如果有token，验证其有效性
      if (token) {
        return {
          token: token,
          user: userStr ? JSON.parse(userStr) : null
        }
      } else {
        return {
          token: '',
          user: null
        }
      }
    } catch (error) {
      console.warn('Failed to parse user from localStorage:', error);
      // 清理损坏的数据
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      return {
        token: '',
        user: null
      }
    }
  },

  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => state.user?.role || '',
    username: (state) => state.user?.username || ''
  },

  actions: {
    async login(loginData) {
      try {
        const response = await authApi.login(loginData)

        if (response.token) {
          this.token = response.token
          this.user = response.user

          localStorage.setItem('token', response.token)
          localStorage.setItem('user', JSON.stringify(response.user))

          return { success: true }
        }

        return { success: false, message: '登录失败' }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    async register(registerData) {
      try {
        await authApi.register(registerData)
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    async getCurrentUser() {
      try {
        const user = await authApi.getCurrentUser()
        this.user = user
        localStorage.setItem('user', JSON.stringify(user))
        return user
      } catch (error) {
        this.logout()
        throw error
      }
    }
  }
})