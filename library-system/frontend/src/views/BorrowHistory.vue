<template>
  <div>
    <h2 style="margin-bottom: 16px;">借还历史</h2>

    <!-- 筛选栏 -->
    <el-card style="margin-bottom: 16px;">
      <el-form :model="filter" inline>
        <el-form-item label="读者证号">
          <el-input v-model="filter.readerId" placeholder="输入读者证号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="filter.bookIsbn" placeholder="输入ISBN" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.returned" placeholder="全部" clearable style="width: 120px;">
            <el-option label="未归还" :value="false" />
            <el-option label="已归还" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="records" v-loading="loading" stripe border>
        <el-table-column prop="recordId" label="记录ID" width="90" align="center" />
        <el-table-column prop="readerId" label="读者证号" width="120" />
        <el-table-column prop="readerName" label="读者姓名" width="100" />
        <el-table-column prop="bookIsbn" label="ISBN" width="145" />
        <el-table-column prop="bookTitle" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="borrowDate" label="借出日期" width="110" align="center" />
        <el-table-column prop="dueDate" label="应还日期" width="110" align="center" />
        <el-table-column prop="returnDate" label="实还日期" width="110" align="center">
          <template #default="{ row }">
            {{ row.returnDate ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="!row.returned" type="primary">借阅中</el-tag>
            <el-tag v-else-if="row.overdueDays" type="danger">超期归还</el-tag>
            <el-tag v-else type="success">正常归还</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="超期天数" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.overdueDays" style="color: #f56c6c; font-weight: bold;">
              {{ row.overdueDays }}天
            </span>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end;"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { borrowApi } from '@/api/borrow'
import type { BorrowHistoryVO } from '@/types/api'

const loading = ref(false)
const records = ref<BorrowHistoryVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const filter = reactive<{ readerId: string; bookIsbn: string; returned: boolean | undefined }>({
  readerId: '',
  bookIsbn: '',
  returned: undefined
})

async function loadData() {
  loading.value = true
  try {
    const res = await borrowApi.listHistory({
      readerId: filter.readerId || undefined,
      bookIsbn: filter.bookIsbn || undefined,
      returned: filter.returned,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res) {
      records.value = res.records
      total.value = res.total
    }
  } catch {
    ElMessage.error('加载历史记录失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filter.readerId = ''
  filter.bookIsbn = ''
  filter.returned = undefined
  pageNum.value = 1
  loadData()
}

onMounted(loadData)
</script>
