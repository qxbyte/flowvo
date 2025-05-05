<template>
  <div class="customer-list">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">新增客户</el-button>
      <el-select v-model="customerLevel" placeholder="客户等级" style="width: 120px; margin-left: 16px">
        <el-option label="全部" value="" />
        <el-option label="普通客户" value="normal" />
        <el-option label="VIP客户" value="vip" />
        <el-option label="重要客户" value="important" />
      </el-select>
      <el-input
        v-model="searchQuery"
        placeholder="搜索客户名称/联系方式"
        style="width: 200px; margin-left: 16px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 客户列表表格 -->
    <el-table :data="customerList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="客户ID" width="120" />
      <el-table-column prop="name" label="客户名称" width="120" />
      <el-table-column prop="contact" label="联系人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="level" label="客户等级" width="100">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.level)">
            {{ getLevelText(row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastOrderTime" label="最近下单" width="180" />
      <el-table-column prop="totalOrders" label="总订单数" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
const customerLevel = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 模拟数据
const customerList = ref([
  {
    id: 'CUS001',
    name: '上海科技有限公司',
    contact: '张经理',
    phone: '13800138000',
    level: 'vip',
    lastOrderTime: '2024-01-20 10:00:00',
    totalOrders: 28
  },
  {
    id: 'CUS002',
    name: '北京创新科技',
    contact: '李总',
    phone: '13900139000',
    level: 'important',
    lastOrderTime: '2024-01-19 15:30:00',
    totalOrders: 15
  }
])

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

// 处理函数
const handleAdd = () => {
  console.log('新增客户')
}

const handleEdit = (row) => {
  console.log('编辑客户', row)
}

const handleView = (row) => {
  console.log('查看客户', row)
}

const handleDelete = (row) => {
  console.log('删除客户', row)
}

const handleSizeChange = (val) => {
  console.log('每页条数:', val)
}

const handleCurrentChange = (val) => {
  console.log('当前页:', val)
}
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