<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Upload, UploadFilled } from '@element-plus/icons-vue'
import { bookApi } from '@/api/book'
import type { BookVO, BookCreateDTO, BookUpdateDTO, ImportResultVO } from '@/types/api'

// ===== 列表状态 =====
const loading = ref(false)
const books = ref<BookVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await bookApi.list({ keyword: keyword.value || undefined, pageNum: pageNum.value, pageSize: pageSize.value })
    books.value = res.records
    total.value = res.total
  } catch {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  pageNum.value = 1
  fetchBooks()
}

const onPageChange = (page: number) => {
  pageNum.value = page
  fetchBooks()
}

const onSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  fetchBooks()
}

// ===== 删除 =====
const handleDelete = async (row: BookVO) => {
  try {
    await ElMessageBox.confirm(`确认删除图书「${row.title}」？`, '警告', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bookApi.delete(row.isbn)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 错误已由拦截器处理
    }
  }
}

// ===== 新增/编辑对话框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增图书')
const isEdit = ref(false)
const currentIsbn = ref('')
const formRef = ref()
const form = ref<BookCreateDTO & { isbn?: string }>({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  totalQuantity: 1
})

const formRules = {
  isbn: [
    { required: true, message: 'ISBN不能为空', trigger: 'blur' },
    { pattern: /^\d{10}(\d{3})?$/, message: 'ISBN格式不正确，应为10位或13位数字', trigger: 'blur' }
  ],
  title: [{ required: true, message: '书名不能为空', trigger: 'blur' }],
  author: [{ required: true, message: '作者不能为空', trigger: 'blur' }],
  publisher: [{ required: true, message: '出版社不能为空', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '总库存不能为空', trigger: 'blur' }]
}

const openCreate = () => {
  isEdit.value = false
  dialogTitle.value = '新增图书'
  form.value = { isbn: '', title: '', author: '', publisher: '', totalQuantity: 1 }
  dialogVisible.value = true
}

const openEdit = (row: BookVO) => {
  isEdit.value = true
  dialogTitle.value = '编辑图书'
  currentIsbn.value = row.isbn
  form.value = {
    isbn: row.isbn,
    title: row.title,
    author: row.author,
    publisher: row.publisher,
    totalQuantity: row.totalQuantity
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  try {
    if (isEdit.value) {
      await bookApi.update(currentIsbn.value, {
        title: form.value.title,
        author: form.value.author,
        publisher: form.value.publisher,
        totalQuantity: form.value.totalQuantity
      })
      ElMessage.success('修改成功')
    } else {
      await bookApi.create({
        isbn: form.value.isbn!,
        title: form.value.title,
        author: form.value.author,
        publisher: form.value.publisher,
        totalQuantity: form.value.totalQuantity
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } catch {
    // 错误已由拦截器处理
  }
}

// ===== CSV 导入对话框 =====
const importDialogVisible = ref(false)
const importResult = ref<ImportResultVO | null>(null)
const importLoading = ref(false)

const openImport = () => {
  importResult.value = null
  importDialogVisible.value = true
}

const handleFileChange = async (file: any) => {
  importLoading.value = true
  try {
    const result = await bookApi.importCsv(file.raw)
    importResult.value = result
    if (result.successCount > 0 || result.accumulatedCount > 0) {
      fetchBooks()
    }
  } catch {
    // 错误已由拦截器处理
  } finally {
    importLoading.value = false
  }
  return false // 阻止自动上传
}

onMounted(fetchBooks)
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="keyword"
          placeholder="搜索书名/作者/ISBN"
          clearable
          style="width: 280px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="onSearch" />
          </template>
        </el-input>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增图书</el-button>
        <el-button :icon="Upload" @click="openImport">批量导入CSV</el-button>
      </div>
    </div>

    <el-table :data="books" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="isbn" label="ISBN" width="150" />
      <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
      <el-table-column prop="publisher" label="出版社" min-width="140" show-overflow-tooltip />
      <el-table-column prop="totalQuantity" label="总库存" width="90" align="center" />
      <el-table-column prop="availableQuantity" label="可借数量" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.availableQuantity > 0 ? 'success' : 'danger'">
            {{ row.availableQuantity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="入库时间" width="170">
        <template #default="{ row }">
          {{ row.createdAt?.replace('T', ' ')?.slice(0, 19) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" :disabled="isEdit" placeholder="10位或13位数字" />
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="最长100字符" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="最长50字符" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="最长100字符" />
        </el-form-item>
        <el-form-item label="总库存" prop="totalQuantity">
          <el-input-number v-model="form.totalQuantity" :min="1" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- CSV 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入图书" width="540px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        <p>CSV文件格式：第一行表头 <code>isbn,title,author,publisher,quantity</code></p>
        <p>ISBN重复时将累加库存，格式错误的行将跳过</p>
      </el-alert>
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        accept=".csv"
        :show-file-list="false"
        v-loading="importLoading"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽CSV文件到此处，或<em>点击选择</em></div>
      </el-upload>

      <div v-if="importResult" style="margin-top: 16px">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="新增">
            <el-tag type="success">{{ importResult.successCount }} 本</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="库存累加">
            <el-tag type="warning">{{ importResult.accumulatedCount }} 本</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <el-tag type="danger">{{ importResult.failCount }} 行</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="importResult.failDetails?.length" style="margin-top: 12px">
          <div style="font-weight: bold; margin-bottom: 6px; color: #f56c6c">失败详情：</div>
          <el-scrollbar max-height="150px">
            <div v-for="(detail, i) in importResult.failDetails" :key="i" style="color: #606266; font-size: 13px; line-height: 1.8">
              {{ detail }}
            </div>
          </el-scrollbar>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
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
  gap: 12px;
}
.toolbar-left, .toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
