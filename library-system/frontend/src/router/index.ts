import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/books'
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('@/views/BookList.vue'),
    meta: { title: '图书管理' }
  },
  {
    path: '/readers',
    name: 'Readers',
    component: () => import('@/views/ReaderList.vue'),
    meta: { title: '读者管理' }
  },
  {
    path: '/borrow',
    name: 'Borrow',
    component: () => import('@/views/BorrowManage.vue'),
    meta: { title: '借还管理' }
  },
  {
    path: '/overdue',
    name: 'Overdue',
    component: () => import('@/views/OverdueList.vue'),
    meta: { title: '逾期列表' }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/BorrowHistory.vue'),
    meta: { title: '借还历史' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
