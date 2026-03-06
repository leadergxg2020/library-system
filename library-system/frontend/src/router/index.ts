import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/books'
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('@/views/BookList.vue'),
    meta: { title: '图书管理', requiresAuth: true }
  },
  {
    path: '/readers',
    name: 'Readers',
    component: () => import('@/views/ReaderList.vue'),
    meta: { title: '读者管理', requiresAuth: true }
  },
  {
    path: '/borrow',
    name: 'Borrow',
    component: () => import('@/views/BorrowManage.vue'),
    meta: { title: '借还管理', requiresAuth: true }
  },
  {
    path: '/overdue',
    name: 'Overdue',
    component: () => import('@/views/OverdueList.vue'),
    meta: { title: '逾期列表', requiresAuth: true }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/BorrowHistory.vue'),
    meta: { title: '借还历史', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫：未登录时跳转到登录页
router.beforeEach(async (to) => {
  if (to.meta.requiresAuth === false) return true
  const authStore = useAuthStore()
  if (!authStore.isLoggedIn) {
    await authStore.fetchMe()
  }
  if (!authStore.isLoggedIn) {
    return { name: 'Login' }
  }
  return true
})

export default router
