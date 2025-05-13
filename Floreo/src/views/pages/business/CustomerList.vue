<template>
  <div class="customer-list">
    <!-- 顶部操作栏 -->
    <div class="operation-bar" style="margin-top: -10px;">
      <el-button type="primary" @click="dialogVisible = true; dialogType = 'add'; resetForm()">新增客户</el-button>
      <el-select v-model="customerLevel" placeholder="客户等级" style="width: 120px; margin-left: 16px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="普通客户" value="normal" />
        <el-option label="VIP客户" value="vip" />
        <el-option label="重要客户" value="important" />
      </el-select>
      <el-input
        v-model="searchQuery"
        placeholder="搜索客户名称/联系方式"
        style="width: 200px; margin-left: 16px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 客户列表表格 -->
    <el-table :data="customerList" style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="id" label="客户ID" width="150" />
      <el-table-column prop="name" label="客户名称" width="180" />
      <el-table-column prop="contactPerson" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="150" />
      <el-table-column prop="level" label="客户等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.level)">
            {{ getLevelText(row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastOrderTime" label="最近下单" width="200">
        <template #default="{ row }">
          {{ row.lastOrderTime ? row.lastOrderTime.replace('T', ' ') : '暂无订单' }}
        </template>
      </el-table-column>
      <el-table-column prop="totalOrder" label="总订单数" width="120" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <!-- <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleView(row)">查看</el-button> -->
          <div class="flex flex-wrap items-center mb-4">
            <el-button size="small" tpye="primary" @click="handleEdit(row)" round>编辑</el-button>
            <el-button size="small" tpye="primary" @click="handleView(row)" round>查看</el-button>
            <el-button plain @click="centerDialogVisible = true;selectedRow = row" size="small" type="danger" round>删除</el-button>
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

    <!-- 客户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      center
      :style="{ borderRadius: '10px' }"
    >
      <el-form :model="customerForm" label-width="100px">
        <el-form-item label="客户名称" required>
          <el-input v-model="customerForm.name" placeholder="请输入客户名称" :disabled="dialogType === 'view'"></el-input>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="customerForm.contactPerson" placeholder="请输入联系人姓名" :disabled="dialogType === 'view'"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="customerForm.contactPhone" placeholder="请输入联系电话" :disabled="dialogType === 'view'"></el-input>
        </el-form-item>
        <el-form-item label="客户等级" required>
          <el-select v-model="customerForm.level" placeholder="请选择客户等级" style="width: 100%" :disabled="dialogType === 'view'">
            <el-option label="普通客户" value="normal"></el-option>
            <el-option label="VIP客户" value="vip"></el-option>
            <el-option label="重要客户" value="important"></el-option>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { InfoFilled } from '@element-plus/icons-vue'

const clicked = ref(false)
function onCancel() {
  clicked.value = true
}

// 搜索相关
const searchQuery = ref('')
const customerLevel = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 客户数据
const customerList = ref([])
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add, edit, view
const customerForm = ref({
  id: '',
  name: '',
  contactPerson: '',
  contactPhone: '',
  level: 'normal'
})

// 获取客户列表数据
const fetchCustomerList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value
    }

    // 添加搜索条件
    if (searchQuery.value) {
      // 搜索客户名称或联系电话
      if (searchQuery.value.match(/^\d+$/)) {
        params.contactPhone = searchQuery.value
      } else {
        params.name = searchQuery.value
      }
    }
    if (customerLevel.value) {
      params.level = customerLevel.value
    }

    const response = await axios.get('/api/customer/list', { params })
    customerList.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

// 等级处理函数
const getLevelType = (level) => {
  const levelMap = {
    normal: 'info',
    vip: 'success',
    important: 'warning'
  }
  return levelMap[level] || 'info'
}

const getLevelText = (level) => {
  const levelMap = {
    normal: '普通客户',
    vip: 'VIP客户',
    important: '重要客户'
  }
  return levelMap[level] || '未知等级'
}

// 计算属性
const dialogTitle = computed(() => {
  if (dialogType.value === 'add') return '新增客户'
  if (dialogType.value === 'edit') return '编辑客户'
  return '查看客户'
})

// 处理函数
const resetForm = () => {
  customerForm.value = {
    id: '',
    name: '',
    contactPerson: '',
    contactPhone: '',
    level: 'normal'
  }
}

const submitForm = () => {
  if (!customerForm.value.name) {
    ElMessage({
      message: '请输入客户名称',
      type: 'warning',
      plain: true,
    })
    return
  }

  if (dialogType.value === 'add') {
    // 新增客户
    axios.post('/api/customer', customerForm.value)
      .then(response => {
        ElMessage({
          message: '新增客户成功',
          type: 'success',
          plain: true,
        })
        dialogVisible.value = false
        fetchCustomerList()
      })
      .catch(error => {
        console.error('新增客户失败:', error)
        ElMessage({
          message: '新增客户失败',
          type: 'error',
          plain: true,
        })
      })
  } else if (dialogType.value === 'edit') {
    // 编辑客户
    axios.put(`/api/customer/${customerForm.value.id}`, customerForm.value)
      .then(response => {
        ElMessage({
          message: '编辑客户成功',
          type: 'success',
          plain: true,
        })
        dialogVisible.value = false
        fetchCustomerList()
      })
      .catch(error => {
        console.error('编辑客户失败:', error)
        ElMessage({
          message: '编辑客户失败: ' + (error.response?.data?.message || error.message),
          type: 'error',
          plain: true,
        })
      })
  }
}

const handleEdit = (row) => {
  customerForm.value = {
    id: row.id,
    name: row.name,
    contactPerson: row.contactPerson,
    contactPhone: row.contactPhone,
    level: row.level
  }
  dialogType.value = 'edit'
  dialogVisible.value = true
}

const handleView = (row) => {
  customerForm.value = {
    id: row.id,
    name: row.name,
    contactPerson: row.contactPerson,
    contactPhone: row.contactPhone,
    level: row.level
  }
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

const handleDelete = (row) => {
  axios.delete(`/api/customer/${row.id}`)
    .then(() => {
      ElMessage({
        message: '删除成功',
        type: 'success',
        plain: true,
      })
      fetchCustomerList()
    })
    .catch(error => {
      console.error('删除客户失败:', error)
      ElMessage({
        message: '删除失败，请稍后重试',
        type: 'error',
        plain: true,
      })
    })
  centerDialogVisible.value = false  // ✅ 关闭对话框
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCustomerList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCustomerList()
}

// 监听搜索条件变化
const handleSearch = () => {
  currentPage.value = 1 // 重置为第一页
  fetchCustomerList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchCustomerList()
})
</script>

<style scoped>
.customer-list {
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
