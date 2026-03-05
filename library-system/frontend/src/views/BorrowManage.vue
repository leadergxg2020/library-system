<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowApi } from '@/api/borrow'
import { readerApi } from '@/api/reader'
import { bookApi } from '@/api/book'
import type { BorrowSuccessVO, ReturnResultVO, ReaderVO, BookVO, BorrowHistoryVO } from '@/types/api'

// ===== 借书 =====
const borrowReaderId = ref('')
const borrowIsbn = ref('')
const borrowLoading = ref(false)
const borrowResult = ref<BorrowSuccessVO | null>(null)

const borrowReaderOptions = ref<ReaderVO[]>([])
const borrowReaderLoading = ref(false)
const borrowBookOptions = ref<BookVO[]>([])
const borrowBookLoading = ref(false)

async function searchBorrowReaders(query: string) {
  borrowReaderLoading.value = true
  try {
    const res = await readerApi.list({ keyword: query || undefined, pageNum: 1, pageSize: 30 })
    borrowReaderOptions.value = (res as any)?.records ?? []
  } finally {
    borrowReaderLoading.value = false
  }
}

async function searchBorrowBooks(query: string) {
  borrowBookLoading.value = true
  try {
    const res = await bookApi.list({ keyword: query || undefined, pageNum: 1, pageSize: 30 })
    borrowBookOptions.value = (res as any)?.records ?? []
  } finally {
    borrowBookLoading.value = false
  }
}

const handleBorrow = async () => {
  if (!borrowReaderId.value) { ElMessage.warning('请选择读者'); return }
  if (!borrowIsbn.value) { ElMessage.warning('请选择图书'); return }
  borrowLoading.value = true
  borrowResult.value = null
  try {
    const res = await borrowApi.borrow({ readerId: borrowReaderId.value, bookIsbn: borrowIsbn.value })
    borrowResult.value = res as any
    ElMessage.success('借书成功')
    // 刷新图书可借数量
    searchBorrowBooks('')
  } catch {
  } finally {
    borrowLoading.value = false
  }
}

// ===== 还书 =====
const returnReaderId = ref('')
const returnIsbn = ref('')
const returnLoading = ref(false)
const returnResult = ref<ReturnResultVO | null>(null)

const returnReaderOptions = ref<ReaderVO[]>([])
const returnReaderLoading = ref(false)
const returnBookOptions = ref<BorrowHistoryVO[]>([])
const returnBookLoading = ref(false)

async function searchReturnReaders(query: string) {
  returnReaderLoading.value = true
  try {
    const res = await readerApi.list({ keyword: query || undefined, pageNum: 1, pageSize: 30 })
    returnReaderOptions.value = (res as any)?.records ?? []
  } finally {
    returnReaderLoading.value = false
  }
}

async function loadUnreturnedBooks(readerId: string) {
  returnBookLoading.value = true
  try {
    const res = await borrowApi.listHistory({ readerId, returned: false, pageNum: 1, pageSize: 50 })
    returnBookOptions.value = (res as any)?.records ?? []
  } finally {
    returnBookLoading.value = false
  }
}

// 选定读者后自动加载其未还书目
watch(returnReaderId, (val) => {
  returnIsbn.value = ''
  returnBookOptions.value = []
  if (val) loadUnreturnedBooks(val)
})

const handleReturn = async () => {
  if (!returnReaderId.value) { ElMessage.warning('请选择读者'); return }
  if (!returnIsbn.value) { ElMessage.warning('请选择图书'); return }
  returnLoading.value = true
  returnResult.value = null
  try {
    const res = await borrowApi.return({ readerId: returnReaderId.value, bookIsbn: returnIsbn.value })
    returnResult.value = res as any
    // 还书成功后刷新该读者未还书目，清空 ISBN 选择
    await loadUnreturnedBooks(returnReaderId.value)
    returnIsbn.value = ''
    if ((res as any).overdue) {
      ElMessage.warning(`还书成功，但已超期 ${(res as any).overdueDays} 天，禁借截止：${(res as any).banUntil}`)
    } else {
      ElMessage.success('还书成功')
    }
  } catch {
  } finally {
    returnLoading.value = false
  }
}

// 初始加载部分选项，避免下拉框一开始为空
onMounted(() => {
  searchBorrowReaders('')
  searchBorrowBooks('')
  searchReturnReaders('')
})
</script>

<template>
  <div class="page-container">
    <el-row :gutter="24">
      <!-- 借书区域 -->
      <el-col :span="12">
        <el-card header="借书操作" shadow="never">
          <el-form label-width="90px">
            <el-form-item label="读者证号">
              <el-select
                v-model="borrowReaderId"
                filterable
                remote
                :remote-method="searchBorrowReaders"
                :loading="borrowReaderLoading"
                placeholder="输入证号或姓名搜索"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="r in borrowReaderOptions"
                  :key="r.readerId"
                  :label="`${r.readerId}  ${r.name}`"
                  :value="r.readerId"
                >
                  <span style="float: left">{{ r.readerId }}</span>
                  <span style="float: right; color: #909399; font-size: 13px">{{ r.name }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="图书ISBN">
              <el-select
                v-model="borrowIsbn"
                filterable
                remote
                :remote-method="searchBorrowBooks"
                :loading="borrowBookLoading"
                placeholder="输入ISBN或书名搜索"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="b in borrowBookOptions"
                  :key="b.isbn"
                  :label="`${b.isbn}  ${b.title}`"
                  :value="b.isbn"
                  :disabled="b.availableQuantity <= 0"
                >
                  <span style="float: left">{{ b.isbn }}</span>
                  <span style="float: right; font-size: 13px" :style="{ color: b.availableQuantity > 0 ? '#67c23a' : '#f56c6c' }">
                    {{ b.title }}（可借 {{ b.availableQuantity }}）
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="borrowLoading" @click="handleBorrow" style="width: 100%">
                确认借书
              </el-button>
            </el-form-item>
          </el-form>

          <el-alert v-if="borrowResult" type="success" :closable="false" style="margin-top: 8px">
            <template #title>
              <span style="font-size: 15px; font-weight: bold">借书成功 ✓</span>
            </template>
            <div class="result-detail">
              <div><span class="label">读者：</span>{{ borrowResult.readerName }}（{{ borrowResult.readerId }}）</div>
              <div><span class="label">图书：</span>{{ borrowResult.bookTitle }}（{{ borrowResult.bookIsbn }}）</div>
              <div><span class="label">借出日期：</span>{{ borrowResult.borrowDate }}</div>
              <div><span class="label">应还日期：</span><strong style="color: #e6a23c">{{ borrowResult.dueDate }}</strong></div>
            </div>
          </el-alert>
        </el-card>
      </el-col>

      <!-- 还书区域 -->
      <el-col :span="12">
        <el-card header="还书操作" shadow="never">
          <el-form label-width="90px">
            <el-form-item label="读者证号">
              <el-select
                v-model="returnReaderId"
                filterable
                remote
                :remote-method="searchReturnReaders"
                :loading="returnReaderLoading"
                placeholder="输入证号或姓名搜索"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="r in returnReaderOptions"
                  :key="r.readerId"
                  :label="`${r.readerId}  ${r.name}`"
                  :value="r.readerId"
                >
                  <span style="float: left">{{ r.readerId }}</span>
                  <span style="float: right; color: #909399; font-size: 13px">{{ r.name }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="图书ISBN">
              <el-select
                v-model="returnIsbn"
                filterable
                :loading="returnBookLoading"
                :placeholder="returnReaderId ? '选择该读者借阅中的图书' : '请先选择读者'"
                :disabled="!returnReaderId"
                clearable
                style="width: 100%"
              >
                <template v-if="returnBookOptions.length === 0 && returnReaderId && !returnBookLoading">
                  <el-option disabled value="" label="该读者暂无未还记录" />
                </template>
                <el-option
                  v-for="b in returnBookOptions"
                  :key="b.bookIsbn"
                  :label="`${b.bookIsbn}  ${b.bookTitle}`"
                  :value="b.bookIsbn"
                >
                  <span style="float: left">{{ b.bookIsbn }}</span>
                  <span style="float: right; color: #909399; font-size: 13px">{{ b.bookTitle }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="returnLoading" @click="handleReturn" style="width: 100%">
                确认还书
              </el-button>
            </el-form-item>
          </el-form>

          <el-alert v-if="returnResult && !returnResult.overdue" type="success" :closable="false" style="margin-top: 8px">
            <template #title>
              <span style="font-size: 15px; font-weight: bold">还书成功 ✓</span>
            </template>
            <div class="result-detail">
              <div><span class="label">读者：</span>{{ returnResult.readerName }}</div>
              <div><span class="label">图书：</span>{{ returnResult.bookTitle }}</div>
              <div><span class="label">借出日期：</span>{{ returnResult.borrowDate }}</div>
              <div><span class="label">归还日期：</span>{{ returnResult.returnDate }}</div>
            </div>
          </el-alert>

          <el-alert v-if="returnResult && returnResult.overdue" type="warning" :closable="false" style="margin-top: 8px">
            <template #title>
              <span style="font-size: 15px; font-weight: bold">还书成功（已超期）</span>
            </template>
            <div class="result-detail">
              <div><span class="label">读者：</span>{{ returnResult.readerName }}</div>
              <div><span class="label">图书：</span>{{ returnResult.bookTitle }}</div>
              <div><span class="label">借出日期：</span>{{ returnResult.borrowDate }}</div>
              <div><span class="label">应还日期：</span>{{ returnResult.dueDate }}</div>
              <div><span class="label">归还日期：</span>{{ returnResult.returnDate }}</div>
              <div><span class="label">超期天数：</span><strong style="color: #e6a23c">{{ returnResult.overdueDays }} 天</strong></div>
              <div><span class="label">禁借截止：</span><strong style="color: #f56c6c">{{ returnResult.banUntil }}</strong></div>
            </div>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}
.result-detail {
  margin-top: 8px;
  line-height: 1.9;
  font-size: 14px;
}
.result-detail .label {
  color: #909399;
  display: inline-block;
  width: 80px;
}
</style>
