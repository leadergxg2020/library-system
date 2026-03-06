<template>
  <!-- 登录页：不显示侧边栏 -->
  <router-view v-if="$route.name === 'Login'" />

  <!-- 主布局：登录后显示 -->
  <el-container v-else class="app-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>图书借阅管理</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/books">
          <el-icon><Reading /></el-icon>
          <span>图书管理</span>
        </el-menu-item>
        <el-menu-item index="/readers">
          <el-icon><User /></el-icon>
          <span>读者管理</span>
        </el-menu-item>
        <el-menu-item index="/borrow">
          <el-icon><DocumentAdd /></el-icon>
          <span>借还管理</span>
        </el-menu-item>
        <el-menu-item index="/overdue">
          <el-icon><Warning /></el-icon>
          <span>逾期列表</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><List /></el-icon>
          <span>借还历史</span>
        </el-menu-item>
      </el-menu>
      <div class="user-bar">
        <span class="username">{{ authStore.currentUser?.username }}</span>
        <el-button size="small" type="danger" plain @click="handleLogout">退出</el-button>
      </div>
    </el-aside>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, User, DocumentAdd, Warning, List } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authApi.logout()
  authStore.clearUser()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  height: 100vh;
}
.sidebar {
  background-color: #304156;
  overflow-y: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo h2 {
  color: #fff;
  font-size: 16px;
  margin: 0;
}
.user-bar {
  position: absolute;
  bottom: 20px;
  left: 0;
  width: 200px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-sizing: border-box;
}
.username {
  color: #bfcbd9;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100px;
}
.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>
