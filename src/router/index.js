import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Login from '../views/Login.vue'
import Layout from '../components/Layout.vue'
import Laboratory from '../views/Laboratory.vue'
import Dashboard from '../views/Dashboard.vue'
import Monitor from '../views/Monitor.vue'
import Alarms from '../views/Alarms.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { requiresAuth: true }
      },
      {
        path: 'laboratory',
        name: 'Laboratory',
        component: Laboratory,
        meta: { requiresAuth: true }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: Monitor,
        meta: { requiresAuth: true }
      },
      {
        path: 'alarms',
        name: 'Alarms',
        component: Alarms,
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 如果路由需要认证但用户未登录，跳转到登录页
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 如果已访问登录页面且用户已登录，重定向到仪表盘
  else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }

  // 允许导航继续
  else {
    next()
  }
})

export default router