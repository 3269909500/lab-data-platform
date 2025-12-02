<template>
  <div class="laboratory">
    <div class="laboratory-content">
        <!-- 页面头部 -->
        <div class="page-header">
          <h2>实验室管理</h2>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            新增实验室
          </el-button>
        </div>

        <!-- 搜索栏 -->
        <el-card class="search-card">
          <el-form :model="searchForm" inline>
            <el-form-item label="实验室名称">
              <el-input
                v-model="searchForm.name"
                placeholder="请输入实验室名称"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              />
            </el-form-item>

            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
                @clear="handleSearch"
                @change="handleSearch"
              >
                <el-option label="运行中" :value="1" />
                <el-option label="已停止" :value="0" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 数据表格 -->
        <el-card>
          <el-table
            v-loading="loading"
            :data="tableData"
            style="width: 100%"
            stripe
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="labName" label="实验室名称" min-width="150" />
            <el-table-column prop="labCode" label="实验室编码" width="120" />
            <el-table-column prop="location" label="位置" width="150" />
            <el-table-column prop="capacity" label="容量" width="100">
              <template #default="scope">
                {{ scope.row.capacity }}人
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '运行中' : '已停止' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createdTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEdit(scope.row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  @click="handleView(scope.row)"
                >
                  查看
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog
          v-model="dialogVisible"
          :title="dialogTitle"
          width="600px"
          @close="handleDialogClose"
        >
          <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            label-width="100px"
          >
            <el-form-item label="实验室名称" prop="labName">
              <el-input v-model="form.labName" placeholder="请输入实验室名称" />
            </el-form-item>

            <el-form-item label="实验室编码" prop="labCode">
              <el-input v-model="form.labCode" placeholder="请输入实验室编码" />
            </el-form-item>

            <el-form-item label="位置" prop="location">
              <el-input v-model="form.location" placeholder="请输入实验室位置" />
            </el-form-item>

            <el-form-item label="容量" prop="capacity">
              <el-input-number
                v-model="form.capacity"
                :min="1"
                :max="200"
                placeholder="请输入容量"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">运行中</el-radio>
                <el-radio :label="0">已停止</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="请输入实验室描述"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <span class="dialog-footer">
              <el-button @click="dialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                确定
              </el-button>
            </span>
          </template>
        </el-dialog>

        <!-- 查看详情对话框 -->
        <el-dialog
          v-model="viewDialogVisible"
          title="实验室详情"
          width="600px"
        >
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
            <el-descriptions-item label="实验室名称">{{ viewData.labName }}</el-descriptions-item>
            <el-descriptions-item label="实验室编码">{{ viewData.labCode }}</el-descriptions-item>
            <el-descriptions-item label="位置">{{ viewData.location }}</el-descriptions-item>
            <el-descriptions-item label="容量">{{ viewData.capacity }}人</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
                {{ viewData.status === 1 ? '运行中' : '已停止' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ viewData.description || '暂无描述' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ formatTime(viewData.createdTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-dialog>
      </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { laboratoryApi } from '../api/laboratory'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  status: null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  labName: '',
  labCode: '',
  location: '',
  capacity: 0,
  status: 1,
  description: ''
})

// 查看数据
const viewData = ref({})

// 表单验证规则
const formRules = {
  labName: [
    { required: true, message: '请输入实验室名称', trigger: 'blur' },
    { min: 2, message: '实验室名称至少2个字符', trigger: 'blur' }
  ],
  labCode: [
    { required: true, message: '请输入实验室编码', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入实验室位置', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入实验室容量', trigger: 'blur' },
    { type: 'number', min: 1, message: '容量至少为1人', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑实验室' : '新增实验室')

// 加载数据
const loadData = async () => {
  try {
    loading.value = true

    const params = {
      current: pagination.current,
      size: pagination.size,
      name: searchForm.name || undefined,
      status: searchForm.status
    }

    const result = await laboratoryApi.getPage(params)

    tableData.value = result.records || []
    pagination.total = result.total || 0

  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: null
  })
  pagination.current = 1
  loadData()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

// 当前页改变
const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    labName: '',
    labCode: '',
    location: '',
    capacity: 0,
    status: 1,
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    labName: row.labName,
    labCode: row.labCode,
    location: row.location,
    capacity: row.capacity,
    status: row.status,
    description: row.description || ''
  })
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除实验室"${row.labName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await laboratoryApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await laboratoryApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await laboratoryApi.create(form)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    loadData()

  } catch (error) {
    if (error.fields) {
      // 表单验证失败
      return
    }
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  } finally {
    submitting.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.laboratory-content {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #fafafa;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
}
</style>