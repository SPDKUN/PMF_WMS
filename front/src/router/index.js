import { createRouter, createWebHashHistory } from 'vue-router'

import Login from '@/pages/login/index.vue'
import Regist from '@/pages/regist/index.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import Home from '@/pages/home/index.vue'
import Manage from '@/pages/manage/index.vue'
import Query from '@/pages/query/index.vue'
import Tasks from '@/pages/tasks/index.vue'
import AiChat from '@/pages/ai/index.vue'
import Profile from '@/pages/profile/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/regist',
    name: 'Regist',
    component: Regist
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/login',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true }
      },
      {
        path: 'manage',
        name: 'Manage',
        component: Manage,
        meta: { requiresAuth: true, requirePosition: '主管' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'query',
        name: 'Query',
        component: Query,
        meta: { requiresAuth: true }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: Tasks,
        meta: { requiresAuth: true }
      },
      {
        path: 'ai',
        name: 'AiChat',
        component: AiChat,
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      next({ name: 'Login' })
      return
    }
    const requirePosition = to.meta.requirePosition
    if (requirePosition) {
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        try {
          const user = JSON.parse(stored)
          if (user.position !== requirePosition) {
            next({ name: 'Home' })
            return
          }
        } catch (e) { /* ignore */ }
      }
    }
    next()
  } else {
    next()
  }
})

export default router
