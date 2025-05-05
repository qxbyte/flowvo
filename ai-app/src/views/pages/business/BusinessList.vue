<template>
  <div class="business-list">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">新增业务</el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索业务"
        style="width: 200px; margin-left: 16px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 业务列表表格 -->
    <el-table :data="businessList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="业务ID" width="120" />
      <el-table-column prop="name" label="业务名称" width="180" />
      <el-table-column prop="type" label="业务类型" width="120" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
            {{ row.status === 'active' ? '运行中' : '已停止' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
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

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 模拟数据
const businessList = ref([
  {
    id: '1',
    name: '业务A',
    type: '类型1',
    status: 'active',
    createTime: '2024-01-20 10:00:00'
  },
  {
    id: '2',
    name: '业务B',
    type: '类型2',
    status: 'inactive',
    createTime: '2024-01-19 15:30:00'
  }
])

// 处理函数
const handleAdd = () => {
  console.log('新增业务')
}

const handleEdit = (row) => {
  console.log('编辑业务', row)
}

const handleView = (row) => {
  console.log('查看业务', row)
}

const handleDelete = (row) => {
  console.log('删除业务', row)
}

const handleSizeChange = (val) => {
  console.log('每页条数:', val)
}

const handleCurrentChange = (val) => {
  console.log('当前页:', val)
}
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