/**
 * WebSocket连接管理类
 * 功能：实时接收后端推送的环境数据、告警信息、统计数据
 * 特性：自动重连、心跳检测、消息类型分发
 */

import { ElMessage } from 'element-plus'

class WebSocketManager {
  constructor() {
    this.ws = null
    this.url = ''
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000 // 3秒
    this.heartbeatInterval = 30000 // 30秒心跳
    this.isManualClose = false

    // 消息监听器
    this.listeners = {
      'ENVIRONMENT_DATA': [],
      'ALARM': [],
      'STATISTICS': [],
      'ALARM_CONFIRMED': [],
      'ALARM_RESOLVED': [],
      'ALARM_IGNORED': []
    }

    // 连接状态回调
    this.onConnectCallbacks = []
    this.onDisconnectCallbacks = []
  }

  /**
   * 连接WebSocket
   * @param {number} labId - 实验室ID
   */
  connect(labId) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接，无需重复连接')
      return
    }

    // 根据环境确定WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname
    const port = import.meta.env.DEV ? '8083' : window.location.port
    this.url = `${protocol}//${host}:${port}/ws/realtime/${labId}`

    console.log('正在连接WebSocket:', this.url)
    this.isManualClose = false

    try {
      this.ws = new WebSocket(this.url)
      this.setupEventHandlers()
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.reconnect(labId)
    }
  }

  /**
   * 设置WebSocket事件处理器
   */
  setupEventHandlers() {
    this.ws.onopen = () => {
      console.log('✅ WebSocket连接成功')
      this.reconnectAttempts = 0

      // 触发连接成功回调
      this.onConnectCallbacks.forEach(callback => callback())

      // 启动心跳
      this.startHeartbeat()
    }

    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        console.log('📨 收到WebSocket消息:', message)

        // 根据消息类型分发
        this.dispatchMessage(message)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error, event.data)
      }
    }

    this.ws.onerror = (error) => {
      console.error('❌ WebSocket错误:', error)
    }

    this.ws.onclose = (event) => {
      console.log('WebSocket连接关闭:', event.code, event.reason)

      // 停止心跳
      this.stopHeartbeat()

      // 触发断开连接回调
      this.onDisconnectCallbacks.forEach(callback => callback())

      // 非手动关闭则尝试重连
      if (!this.isManualClose) {
        const labId = this.extractLabIdFromUrl()
        if (labId) {
          this.reconnect(labId)
        }
      }
    }
  }

  /**
   * 分发消息到对应的监听器
   */
  dispatchMessage(message) {
    const { type, data } = message

    if (this.listeners[type]) {
      this.listeners[type].forEach(callback => {
        try {
          callback(data, message)
        } catch (error) {
          console.error(`执行${type}监听器失败:`, error)
        }
      })
    } else {
      console.warn('未知的消息类型:', type)
    }
  }

  /**
   * 添加消息监听器
   * @param {string} type - 消息类型
   * @param {function} callback - 回调函数
   */
  on(type, callback) {
    if (!this.listeners[type]) {
      this.listeners[type] = []
    }
    this.listeners[type].push(callback)
  }

  /**
   * 移除消息监听器
   * @param {string} type - 消息类型
   * @param {function} callback - 回调函数
   */
  off(type, callback) {
    if (!this.listeners[type]) return

    const index = this.listeners[type].indexOf(callback)
    if (index > -1) {
      this.listeners[type].splice(index, 1)
    }
  }

  /**
   * 添加连接成功回调
   */
  onConnect(callback) {
    this.onConnectCallbacks.push(callback)
  }

  /**
   * 添加断开连接回调
   */
  onDisconnect(callback) {
    this.onDisconnectCallbacks.push(callback)
  }

  /**
   * 发送消息
   * @param {object} message - 消息对象
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'PING' })
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 重连
   */
  reconnect(labId) {
    if (this.reconnectTimer || this.isManualClose) return

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('❌ WebSocket重连次数已达上限')
      ElMessage.error('实时连接失败，请刷新页面重试')
      return
    }

    this.reconnectAttempts++
    console.log(`尝试第 ${this.reconnectAttempts} 次重连...`)

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect(labId)
    }, this.reconnectInterval)
  }

  /**
   * 从URL提取labId
   */
  extractLabIdFromUrl() {
    const match = this.url.match(/\/ws\/realtime\/(\d+)/)
    return match ? parseInt(match[1]) : null
  }

  /**
   * 关闭连接
   */
  close() {
    this.isManualClose = true
    this.stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    console.log('WebSocket已手动关闭')
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }

  /**
   * 获取连接状态文本
   */
  getReadyStateText() {
    if (!this.ws) return 'DISCONNECTED'

    switch (this.ws.readyState) {
      case WebSocket.CONNECTING: return 'CONNECTING'
      case WebSocket.OPEN: return 'CONNECTED'
      case WebSocket.CLOSING: return 'CLOSING'
      case WebSocket.CLOSED: return 'CLOSED'
      default: return 'UNKNOWN'
    }
  }
}

// 导出单例实例
export default new WebSocketManager()
