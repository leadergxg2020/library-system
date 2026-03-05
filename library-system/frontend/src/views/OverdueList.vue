<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { borrowApi } from '@/api/borrow'
import type { OverdueRecordVO } from '@/types/api'

const loading = ref(false)
const overdueList = ref<OverdueRecordVO[]>([])

const fetchOverdue = async () => {
  loading.value = true
  try {
    const res = await borrowApi.listOverdue()
    overdueList.value = res
  } catch {
    // 错误已由 request 拦截器统一处理并弹出提示
  } finally {
    loading.value = false
  }
}

const overdueTagType = (days: number): 'danger' | 'warning' | 'info' => {
  if (days > 30) return 'danger'
  if (days > 7) return 'warning'
  return 'info'
}

onMounted(fetchOverdue)
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <div class="summary">
        当前逾期未还：
        <el-tag type="danger" size="large">{{ overdueList.length }} 条</el-tag>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="fetchOverdue">刷新</el-button>
    </div>

    <el-table
      :data="overdueList"
      v-loading="loading"
      stripe
      border
      style="width: 100%"
      empty-text="暂无逾期记录"
    >
      <el-table-column prop="recordId" label="借阅ID" width="90" align="center" />
      <el-table-column prop="readerId" label="读者证号" width="120" />
      <el-table-column prop="readerName" label="读者姓名" width="100" />
      <el-table-column prop="bookIsbn" label="图书ISBN" width="150" />
      <el-table-column prop="bookTitle" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="borrowDate" label="借出日期" width="115" align="center" />
      <el-table-column prop="dueDate" label="应还日期" width="115" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">{{ row.dueDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="overdueDays" label="超期天数" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="overdueTagType(row.overdueDays)" effect="dark">
            {{ row.overdueDays }} 天
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.summary {
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
