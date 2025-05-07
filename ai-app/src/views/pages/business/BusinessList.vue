<template>
  <div class="business-list">
    <!-- 顶部操作栏 -->
    <div class="operation-bar" style="margin-top: -10px;">
      <el-button type="primary" @click="dialogVisible = true; dialogType = 'add'; resetForm()">新增业务</el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索业务"
        style="width: 200px; margin-left: 16px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 业务列表表格 -->
    <el-table :data="businessList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="业务ID" width="150" />
      <el-table-column prop="name" label="业务名称" width="200" />
      <el-table-column prop="type" label="业务类型" width="150" />

      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'paused' ? 'warning' : 'danger'">
            {{ row.status === 'active' ? '运行中' : row.status === 'paused' ? '暂停' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="200">
        <template #default="{ row }">
          {{ row.createTime.replace('T', ' ') }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <!-- <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleView(row)">查看</el-button> -->
          <div class="flex flex-wrap items-center mb-4">
            <el-button size="small" tpye="primary" @click="handleEdit(row)" round>编辑</el-button>
            <el-button size="small" tpye="primary" @click="handleView(row)" round>查看</el-button>
            <el-button plain @click="openPayConfirm(row)" size="small" type="danger" round>删除</el-button>

          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="centerDialogVisible"
      title="确认"
      width="300"
      align-center
    >
      <span>确认删除？</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="centerDialogVisible = false">Cancel</el-button>
          <el-button type="danger" @click="handleDelete(selectedRow)">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 业务表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      center
      :style="{ borderRadius: '10px' }"
    >
      <el-form :model="businessForm" label-width="100px">
        <el-form-item label="业务名称" required>
          <el-input v-model="businessForm.name" placeholder="请输入业务名称" :disabled="dialogType === 'view'"></el-input>
        </el-form-item>
        <el-form-item label="业务类型" required>
          <el-select v-model="businessForm.type" placeholder="请选择业务类型" style="width: 100%" :disabled="dialogType === 'view'">
            <el-option label="类型1" value="类型1"></el-option>
            <el-option label="类型2" value="类型2"></el-option>
            <el-option label="类型3" value="类型3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业务状态" required>
          <el-select v-model="businessForm.status" placeholder="请选择业务状态" style="width: 100%" :disabled="dialogType === 'view'">
            <el-option label="进行中" value="active"></el-option>
            <el-option label="暂停" value="paused"></el-option>
            <el-option label="已结束" value="ended"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button v-if="dialogType !== 'view'" type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'


// 搜索相关
const searchQuery = ref('')
const businessType = ref('')
const businessStatus = ref('')

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add, edit, view
const businessForm = ref({
  id: '',
  name: '',
  type: '类型1',
  status: 'active'
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 业务数据
const businessList = ref([])
const loading = ref(false)

// 获取业务列表数据
const fetchBusinessList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value
    }

    // 添加搜索条件
    if (searchQuery.value) {
      params.name = searchQuery.value
    }
    if (businessType.value) {
      params.type = businessType.value
    }
    if (businessStatus.value) {
      params.status = businessStatus.value
    }

    const response = await axios.get('/api/business/list', { params })
    businessList.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('获取业务列表失败:', error)
    ElMessage({
      message: '获取业务列表失败',
      type: 'error',
      showClose: true,
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

// 计算属性
const dialogTitle = computed(() => {
  if (dialogType.value === 'add') return '新增业务'
  if (dialogType.value === 'edit') return '编辑业务'
  return '查看业务'
})

// 处理函数
const resetForm = () => {
  businessForm.value = {
    id: '',
    name: '',
    type: '类型1',
    status: 'active'
  }
}

const submitForm = () => {
  if (!businessForm.value.name) {
    ElMessage({
      message: '请输入业务名称',
      type: 'warning',
      plain: true,
    })
    return
  }

  if (dialogType.value === 'add') {
    // 新增业务
    axios.post('/api/business', businessForm.value)
      .then(response => {
        ElMessage({
          message: '新增业务成功',
          type: 'success',
          plain: true,
        })
        dialogVisible.value = false
        fetchBusinessList()
      })
      .catch(error => {
        console.error('新增业务失败:', error)
        ElMessage({
          message: '新增业务失败',
          type: 'error',
          plain: true,
        })
      })
  } else if (dialogType.value === 'edit') {
    // 编辑业务
    const updatedBusiness = {
      ...businessForm.value,
      updateTime: new Date().toISOString()
    }
    axios.put(`/api/business/${businessForm.value.id}`, updatedBusiness)
      .then(response => {
        ElMessage({
          message: '编辑业务成功',
          type: 'success',
          plain: true,
        })
        dialogVisible.value = false
        fetchBusinessList()
      })
      .catch(error => {
        console.error('编辑业务失败:', error)
        ElMessage({
          message: '编辑业务失败',
          type: 'error',
          plain: true,
        })
      })
  }
}

const handleEdit = (row) => {
  businessForm.value = { ...row }
  dialogType.value = 'edit'
  dialogVisible.value = true
}

const handleView = (row) => {
  businessForm.value = { ...row }
  dialogType.value = 'view'
  dialogVisible.value = true
  // 设置对话框样式，确保与其他操作保持一致
  // setTimeout(() => {
  //   const dialogHeader = document.querySelector('.el-dialog__header')
  //   if (dialogHeader) {
  //     dialogHeader.style.backgroundColor = '#f2f6fc'
  //     dialogHeader.style.borderBottom = '1px solid #e6ebf5'
  //     dialogHeader.style.padding = '15px'
  //   }

  //   const dialogTitle = document.querySelector('.el-dialog__title')
  //   if (dialogTitle) {
  //     dialogTitle.style.color = '#409eff'
  //     dialogTitle.style.fontWeight = 'bold'
  //   }
  // }, 10)
}

const centerDialogVisible = ref(false)
const selectedRow = ref(null)
const openPayConfirm = (row) => {
  centerDialogVisible.value = true
  selectedRow.value = row
}

const handleDelete = (row) => {
  axios.delete(`/api/business/${row.id}`)
    .then(() => {
      ElMessage({
        message: '删除成功',
        type: 'success',
        plain: true,
      })
      fetchBusinessList()
    })
    .catch(error => {
      console.error('删除失败:', error)
      ElMessage({
        message: '删除成功',
        type: 'success',
        plain: true,
      })
    })
  centerDialogVisible.value = false  // ✅ 关闭对话框
}


const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBusinessList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBusinessList()
}

// 监听搜索条件变化
const handleSearch = () => {
  currentPage.value = 1 // 重置为第一页
  fetchBusinessList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchBusinessList()
})
</script>

<style scoped>
.business-list {
  padding: 20px;
}

.operation-bar {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

<style>
</style>
