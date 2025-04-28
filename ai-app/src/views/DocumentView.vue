<template>
  <div class="document-page">
    <div class="document-container">
      <h1>文档管理</h1>

      <div
        class="upload-area"
        @dragover.prevent
        @drop.prevent="handleFileDrop"
        @click="triggerFileInput"
      >
        <input
          type="file"
          ref="fileInput"
          style="display: none"
          @change="handleFileSelect"
          accept=".doc,.docx,.pdf,.txt"
        >
        <div class="upload-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
        </div>
        <p>拖拽文件到此处或点击上传</p>
        <p class="upload-hint">支持 .doc, .docx, .pdf, .txt 格式</p>
      </div>

      <div class="document-list">
        <div v-if="documents.length === 0" class="no-documents">
          暂无文档
        </div>
        <div v-else class="document-grid">
          <div v-for="doc in documents" :key="doc.id" class="document-item">
            <div class="document-icon">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <div class="document-info">
              <h3>{{ doc.fileName }}</h3>
              <p>{{ formatDate(doc.uploadTime) }}</p>
              <span class="document-type">{{ doc.fileExtension }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-pagination
        v-if="total > pageSize"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
        style="margin: 24px auto 0; text-align:center;"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const fileInput = ref<HTMLInputElement | null>(null)
const documents = ref<Array<{
  id: string
  fileName: string
  fileExtension: string
  uploadTime: string
}>>([])

const page = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const files = (event.target as HTMLInputElement).files
  if (files) handleFiles(files)
}

const handleFileDrop = (event: DragEvent) => {
  const files = event.dataTransfer?.files
  if (files) handleFiles(files)
}

const handleFiles = async (files: FileList) => {
  const allowedTypes = ['.doc', '.docx', '.pdf', '.txt']
  const formData = new FormData()
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
    if (!allowedTypes.includes(fileExtension)) {
      alert(`不支持的文件类型：${fileExtension}\n请上传 .doc, .docx, .pdf, .txt 格式的文件`)
      continue
    }
    formData.append('file', file)
  }
  try {
    loading.value = true
    const response = await fetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })
    if (!response.ok) throw new Error('上传失败')
    await fetchDocuments()
  } catch (error) {
    console.error('上传错误:', error)
    alert('文件上传失败，请重试')
  } finally {
    loading.value = false
  }
}

// 关键：分页接口
const fetchDocuments = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/files/list?page=${page.value-1}&size=${pageSize.value}`)
    if (!response.ok) throw new Error('获取文档列表失败')
    const data = await response.json()
    // 假设后端返回 Page 对象格式：{ content: [], totalElements: 100, ... }
    documents.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    console.error('获取文档列表错误:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchDocuments()
}

onMounted(fetchDocuments)

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.document-page {
  padding: 2rem;
  margin: 0 auto;
}

.document-container {
  background-color: white;
  border-radius: 0.5rem;
  padding: 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.document-container h1 {
  margin-bottom: 2rem;
  font-size: 1.875rem;
  font-weight: 600;
  color: #111827;
}

.upload-area {
  border: 2px dashed #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
  text-align: center;
  cursor: pointer;
  margin-bottom: 2rem;
}

.upload-area:hover {
  border-color: #4f46e5;
}

.upload-icon svg {
  width: 32px;
  height: 32px;
  color: #4f46e5;
  margin-bottom: 0.5rem;
}

.upload-hint {
  color: #6b7280;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.document-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1.5rem;
  padding: 1rem;
}

.document-item {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.2s;
  min-width: 0;
}

.document-item:hover {
  border-color: #4f46e5;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.document-icon svg {
  width: 32px;
  height: 32px;
  color: #4f46e5;
  margin-bottom: 0.5rem;
}

.document-info {
  text-align: center;
}

.document-info h3 {
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
  color: #111827;
}

.document-info p {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.document-type {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background-color: #f3f4f6;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  color: #4b5563;
}

.no-documents {
  text-align: center;
  color: #6b7280;
  padding: 2rem;
}
</style>