<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>文档管理</h2>
        <button class="close-button" @click="$emit('close')">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

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
              <h3>{{ doc.name }}</h3>
              <p>{{ formatDate(doc.uploadTime) }}</p>
              <span class="document-type">{{ doc.type }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const fileInput = ref<HTMLInputElement | null>(null)
const documents = ref<Array<{
  id: number
  name: string
  uploadTime: string
  type: string
}>>([]) // 这里暂时使用静态数据，后续需要从后端获取

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const files = (event.target as HTMLInputElement).files
  if (files) {
    handleFiles(files)
  }
}

const handleFileDrop = (event: DragEvent) => {
  const files = event.dataTransfer?.files
  if (files) {
    handleFiles(files)
  }
}

const handleFiles = async (files: FileList) => {
  // 验证文件类型
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
    const response = await fetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) {
      throw new Error('上传失败')
    }

    const result = await response.json()
    // 更新文档列表
    await fetchDocuments()
  } catch (error) {
    console.error('上传错误:', error)
    alert('文件上传失败，请重试')
  }
}

const fetchDocuments = async () => {
  try {
    const response = await fetch('/api/files/list')
    if (!response.ok) {
      throw new Error('获取文档列表失败')
    }
    documents.value = await response.json()
  } catch (error) {
    console.error('获取文档列表错误:', error)
  }
}

onMounted(async () => {
  await fetchDocuments()
})
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 0.5rem;
  padding: 2rem;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.close-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.close-button svg {
  width: 24px;
  height: 24px;
  color: #6b7280;
}

.upload-area {
  border: 2px dashed #e5e7eb;
  border-radius: 0.5rem;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  margin-bottom: 2rem;
}

.upload-area:hover {
  border-color: #4f46e5;
}

.upload-icon svg {
  width: 48px;
  height: 48px;
  color: #4f46e5;
  margin-bottom: 1rem;
}

.upload-hint {
  color: #6b7280;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.document-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.document-item {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
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