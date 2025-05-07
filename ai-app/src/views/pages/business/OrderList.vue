<template>
  <div class="order-list">
    <!-- 顶部操作栏 -->
    <div class="operation-bar" style="width: 1000px; margin-top: -10px;">
      <el-button type="primary" @click="dialogVisible = true; resetForm()">新增订单</el-button>
      <el-select v-model="orderStatus" placeholder="订单状态" style="width: 120px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待付款" value="pending" />
        <el-option label="已付款" value="paid" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="margin-left: 16px"
        @change="handleSearch"
      />
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单号/客户名称"
        style="width: 200px; margin-left: 16px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 订单列表表格 -->
    <el-table :data="orderList" style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="customerName" label="客户名称" width="180">
        <template #default="{ row }">
          {{ row.customer ? row.customer.name : '未知客户' }}
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="订单金额" width="150">
        <template #default="{ row }">
          ¥{{ row.amount.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200">
        <template #default="{ row }">
          {{ row.createTime.replace('T', ' ') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="402">
        <template #default="{ row }">
          <el-button size="small" tpye="primary" @click="handleView(row)" round>查看</el-button>
          <el-button plain v-if="row.status === 'pending'" type="primary" @click="openPayConfirm_1(row)" size="small" round>处理</el-button>
          <el-button plain v-if="row.status === 'pending'" type="danger" @click="openPayConfirm_2(row)" size="small" round>取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="centerDialogVisible_1"
      title="确认"
      width="500"
      align-center
    >
      <span>
        <p>您确定要将订单 <strong>{{ selectedOrderNo }}</strong> 标记为已付款吗？</p>
      </span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="centerDialogVisible_1 = false">取消</el-button>
          <el-button type="danger" @click="handleProcess(selectedRow)">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="centerDialogVisible_2"
      title="确认"
      width="500"
      align-center
    >
      <span>
        <p>您确定要将订单 <strong>{{ selectedOrderNo }}</strong> 取消吗？</p>
      </span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="centerDialogVisible_2 = false">取消</el-button>
          <el-button type="danger" @click="handleCancel(selectedRow)">确认</el-button>
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

    <!-- 新增订单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增订单"
      width="500px"
      center
      :style="{ borderRadius: '10px' }"
      @open="resetForm"
    >
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="客户名称" required>
          <el-select v-model="orderForm.customerId" placeholder="请选择客户" style="width: 100%" filterable>
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.name"
              :value="customer.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="订单金额" required>
          <el-input-number v-model="orderForm.amount" :precision="2" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看订单对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="查看订单"
      width="500px"
      center
      :style="{ borderRadius: '10px' }"
    >
      <div class="order-detail">
        <div class="detail-item">
          <span class="label">订单号:</span>
          <span class="value">{{ viewOrderData.orderNo }}</span>
        </div>
        <div class="detail-item">
          <span class="label">客户名称:</span>
          <span class="value">{{ viewOrderData.customerName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">订单金额:</span>
          <span class="value">¥{{ viewOrderData.amount }}</span>
        </div>
        <div class="detail-item">
          <span class="label">订单状态:</span>
          <span class="value">{{ viewOrderData.status }}</span>
        </div>
        <div class="detail-item">
          <span class="label">创建时间:</span>
          <span class="value">{{ viewOrderData.createTime }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 搜索相关
const searchQuery = ref('')
const orderStatus = ref('')
const dateRange = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 订单数据
const orderList = ref([])
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const orderForm = ref({
  customerId: '',
  amount: 0
})
const customerList = ref([])

// 查看订单对话框相关
const viewDialogVisible = ref(false)
const viewOrderData = ref({
  orderNo: '',
  customerName: '',
  amount: '',
  status: '',
  createTime: ''
})

// 获取订单列表数据
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value
    }

    // 添加搜索条件
    if (searchQuery.value) {
      params.orderNo = searchQuery.value
    }
    if (orderStatus.value) {
      params.status = orderStatus.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0].toISOString()
      params.endTime = dateRange.value[1].toISOString()
    }

    const response = await axios.get('/api/order/list', { params })
    orderList.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage({
      message: '获取订单列表失败',
      type: 'error',
      plain: true,
    })
  } finally {
    loading.value = false
  }
}

// 状态处理函数
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    paid: 'success',
    completed: 'info',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    paid: '已付款',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 处理函数
const handleView = (row) => {
  viewOrderData.value = {
    orderNo: row.orderNo,
    customerName: row.customer ? row.customer.name : '未知客户',
    amount: row.amount.toFixed(2),
    status: getStatusText(row.status),
    createTime: row.createTime
  }
  viewDialogVisible.value = true
}


const centerDialogVisible_1 = ref(false)
const centerDialogVisible_2 = ref(false)
const selectedOrderNo = ref('')
const selectedRow = ref(null)
const openPayConfirm_1 = (row) => {
  selectedOrderNo.value = row.orderNo
  centerDialogVisible_1.value = true
  selectedRow.value = row
}

const openPayConfirm_2 = (row) => {
  selectedOrderNo.value = row.orderNo
  centerDialogVisible_2.value = true
  selectedRow.value = row
}

const handleProcess = (row) => {
  axios.put(`/api/order/${row.id}/status`, null, {
    params: {
      status: 'paid'
    }
  })
  .then(() => {
    ElMessage({
      message: '订单状态已更新',
      type: 'success',
      plain: true,
    })
    fetchOrderList()
  })
  .catch(error => {
    console.error('更新订单状态失败:', error)
    ElMessage({
      message: '更新订单状态失败',
      type: 'error',
      plain: true,
    })
  })
  .catch(error => {
    console.error('操作失败:', error)
    ElMessage({
      message: '操作失败，请稍后重试',
      type: 'error',
      plain: true,
    })
  })
  centerDialogVisible_1.value = false  // ✅ 关闭对话框
}

const handleCancel = (row) => {
  axios.put(`/api/order/${row.id}/status`, null, {
    params: {
      status: 'cancelled'
    }
  })
  .then(() => {
    ElMessage({
      message: '订单已取消',
      type: 'success',
      plain: true,
    })
    fetchOrderList()
  })
  .catch(error => {
    console.error('取消订单失败:', error)
    ElMessage({
      message: '取消订单失败',
      type: 'error',
      plain: true,
    })
  })
  centerDialogVisible_2.value = false  // ✅ 关闭对话框
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrderList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrderList()
}

// 监听搜索条件变化
const handleSearch = () => {
  currentPage.value = 1 // 重置为第一页
  fetchOrderList()
}

// 新增订单相关函数
const resetForm = () => {
  orderForm.value = {
    customerId: '',
    amount: 0
  }
  // 获取客户列表
  fetchCustomerList()
}

// 确保页面加载时也获取客户列表
onMounted(() => {
  fetchOrderList()
  fetchCustomerList()
})

const fetchCustomerList = async () => {
  try {
    const response = await axios.get('/api/customer/list', { params: { size: 1000 } })
    if (response.data && response.data.content) {
      customerList.value = response.data.content
      console.log('已加载客户列表，数量:', customerList.value.length)
    } else {
      console.error('获取客户列表返回数据格式不正确:', response.data)
      customerList.value = []
      ElMessage({
        message: '客户数据加载异常，请刷新页面重试',
        type: 'warning',
        plain: true,
      })

    }
  } catch (error) {
    console.error('获取客户列表失败:', error)
    customerList.value = []
    ElMessage({
      message: '获取客户列表失败',
      type: 'error',
      plain: true,
    })
  }
}

const submitForm = async () => {
  if (!orderForm.value.customerId) {
    ElMessage({
      message: '请选择客户',
      type: 'warning',
      plain: true,
    })
    return
  }

  if (orderForm.value.amount <= 0) {
    ElMessage({
      message: '请输入有效的订单金额',
      type: 'warning',
      plain: true,
    })
    return
  }

  try {
    // 查找选中的客户对象
    const selectedCustomer = customerList.value.find(c => c.id === orderForm.value.customerId)

    if (!selectedCustomer) {
      ElMessage({
        message: '所选客户不存在',
        type: 'warning',
        plain: true,
      })
      return
    }

    const newOrder = {
      customer: { id: selectedCustomer.id }, // 使用customer对象，只需要传id即可
      amount: orderForm.value.amount,
      status: 'pending'
    }

    await axios.post('/api/order', newOrder)
    ElMessage({
      message: '新增订单成功',
      type: 'success',
      plain: true,
    })
    dialogVisible.value = false
    fetchOrderList()
  } catch (error) {
    console.error('新增订单失败:', error)
    ElMessage({
      message: '新增订单失败',
      type: 'error',
      plain: true,
    })
  }
}

// 页面加载时获取数据已在上方定义
</script>

<style scoped>
.order-list {
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

.order-detail {
  padding: 10px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  line-height: 24px;
}

.detail-item .label {
  width: 100px;
  text-align: right;
  margin-right: 15px;
  font-weight: bold;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #333;
}
</style>

<style>

/* 查看订单详情弹窗样式 */
.view-detail-dialog .el-message-box__header {
  padding: 15px;
  background-color: #f2f6fc;
  border-bottom: 1px solid #e6ebf5;
}

.view-detail-dialog .el-message-box__title {
  color: #409eff;
  font-weight: bold;
}

.view-detail-dialog .el-message-box__content {
  padding: 20px;
}

.view-detail-dialog .el-message-box__btns {
  padding: 10px 20px 15px;
}

.process-confirm-dialog .el-button--primary {
  background-color: #67c23a;
  border-color: #67c23a;
}

.process-confirm-dialog .el-button--primary:hover,
.process-confirm-dialog .el-button--primary:focus {
  background-color: #85ce61;
  border-color: #85ce61;
}

</style>
