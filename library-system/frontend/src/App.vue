<template>
  <el-container class="app-container">
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
    </el-aside>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { Reading, User, DocumentAdd, Warning, List } from '@element-plus/icons-vue'
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
