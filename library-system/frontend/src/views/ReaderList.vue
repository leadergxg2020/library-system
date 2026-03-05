<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { readerApi } from '@/api/reader'
import type { ReaderVO, ReaderCreateDTO } from '@/types/api'

const loading = ref(false)
const readers = ref<ReaderVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const fetchReaders = async () => {
  loading.value = true
  try {
    const res = await readerApi.list({
      keyword: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    readers.value = res.records
    total.value = res.total
  } catch {
    // 错误已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  pageNum.value = 1
  fetchReaders()
}

const onPageChange = (page: number) => {
  pageNum.value = page
  fetchReaders()
}

const onSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  fetchReaders()
}

const handleDelete = async (row: ReaderVO) => {
  try {
    await ElMessageBox.confirm(
      `确认删除读者「${row.name}（${row.readerId}）」？`,
      '警告',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await readerApi.delete(row.readerId)
    ElMessage.success('删除成功')
    fetchReaders()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 其他错误已由请求拦截器处理
    }
  }
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增读者')
const isEdit = ref(false)
const currentReaderId = ref('')
const formRef = ref()
const form = ref<ReaderCreateDTO & { maxBorrowCount: number }>({
  readerId: '',
  name: '',
  contact: '',
  maxBorrowCount: 5
})

const formRules = {
  readerId: [{ required: true, message: '读者证号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '读者姓名不能为空', trigger: 'blur' }],
  contact: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  maxBorrowCount: [{ required: true, message: '最大借阅数量不能为空', trigger: 'blur' }]
}

const openCreate = () => {
  isEdit.value = false
  dialogTitle.value = '新增读者'
  form.value = { readerId: '', name: '', contact: '', maxBorrowCount: 5 }
  dialogVisible.value = true
}

const openEdit = (row: ReaderVO) => {
  isEdit.value = true
  dialogTitle.value = '编辑读者'
  currentReaderId.value = row.readerId
  form.value = {
    readerId: row.readerId,
    name: row.name,
    contact: row.contact || '',
    maxBorrowCount: row.maxBorrowCount
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  try {
    if (isEdit.value) {
      await readerApi.update(currentReaderId.value, {
        name: form.value.name,
        contact: form.value.contact,
        maxBorrowCount: form.value.maxBorrowCount
      })
      ElMessage.success('修改成功')
    } else {
      await readerApi.create({
        readerId: form.value.readerId,
        name: form.value.name,
        contact: form.value.contact,
        maxBorrowCount: form.value.maxBorrowCount
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchReaders()
  } catch {
    // 错误已由请求拦截器统一处理
  }
}

onMounted(fetchReaders)
</script>

<template>
  <div class="page-container">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名/读者证号"
          clearable
          style="width: 240px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="onSearch" />
          </template>
        </el-input>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增读者</el-button>
      </div>
    </div>

    <!-- 读者列表 -->
    <el-table :data="readers" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="readerId" label="读者证号" width="130" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="contact" label="联系方式" width="140" />
      <el-table-column prop="maxBorrowCount" label="最大借阅" width="90" align="center" />
      <el-table-column label="当前借阅" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.currentBorrowCount >= row.maxBorrowCount ? 'danger' : 'info'">
            {{ row.currentBorrowCount }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="禁借状态" width="120" align="center">
        <template #default="{ row }">
          <el-tooltip
            v-if="row.banned"
            :content="`禁借至 ${row.banUntil}（因 ${row.banReasonDate} 超期还书）`"
            placement="top"
          >
            <el-tag type="danger">禁借中</el-tag>
          </el-tooltip>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">
          {{ row.createdAt?.replace('T', ' ')?.slice(0, 19) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="读者证号" prop="readerId">
          <el-input
            v-model="form.readerId"
            :disabled="isEdit"
            placeholder="唯一标识，最长50字符"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="最长30字符" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="11位手机号（可选）" />
        </el-form-item>
        <el-form-item label="最大借阅数" prop="maxBorrowCount">
          <el-input-number v-model="form.maxBorrowCount" :min="1" :max="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
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
}

.toolbar-left,
.toolbar-right {
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
