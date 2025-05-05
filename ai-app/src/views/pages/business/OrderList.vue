<template>
  <div class="order-list">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-select v-model="orderStatus" placeholder="订单状态" style="width: 120px">
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
      />
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单号/客户名称"
        style="width: 200px; margin-left: 16px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 订单列表表格 -->
    <el-table :data="orderList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="customerName" label="客户名称" width="120" />
      <el-table-column prop="amount" label="订单金额" width="120">
        <template #default="{ row }">
          ¥{{ row.amount.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button 
            v-if="row.status === 'pending'"
            link 
            type="primary" 
            @click="handleProcess(row)"
          >处理</el-button>
          <el-button 
            v-if="row.status === 'pending'"
            link 
            type="danger" 
            @click="handleCancel(row)"
          >取消</el-button>
        </template>
      </el-table-column>
    </el-table>

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
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

// 搜索相关
const searchQuery = ref('')
const orderStatus = ref('')
const dateRange = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 模拟数据
const orderList = ref([
  {
    orderNo: 'ORD202401200001',
    customerName: '张三',
    amount: 1299.00,
    status: 'pending',
    createTime: '2024-01-20 10:00:00'
  },
  {
    orderNo: 'ORD202401200002',
    customerName: '李四',
    amount: 2599.00,
    status: 'paid',
    createTime: '2024-01-20 11:30:00'
  }
])

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
  console.log('查看订单', row)
}

const handleProcess = (row) => {
  console.log('处理订单', row)
}

const handleCancel = (row) => {
  console.log('取消订单', row)
}

const handleSizeChange = (val) => {
  console.log('每页条数:', val)
}

const handleCurrentChange = (val) => {
  console.log('当前页:', val)
}
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
</style>