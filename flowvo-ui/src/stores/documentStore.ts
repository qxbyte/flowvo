import { create } from 'zustand';
import { subscribeWithSelector } from 'zustand/middleware';
import { documentApi, type Document, type DocumentSearchResult } from '../utils/api';

interface DocumentState {
  // 状态
  documents: Document[];
  searchResults: DocumentSearchResult[];
  supportedTypes: string[];
  loading: boolean;
  uploadProgress: number;
  error: string | null;
  
  // 搜索状态
  searchQuery: string;
  searchLoading: boolean;
  
  // 选中的文档
  selectedDocument: Document | null;
  
  // 操作方法
  fetchUserDocuments: (userId: string) => Promise<void>;
  uploadDocument: (file: File, userId: string, tags?: string[], description?: string) => Promise<Document | null>;
  deleteDocument: (documentId: string, userId: string) => Promise<boolean>;
  updateDocument: (documentId: string, userId: string, updates: Partial<Document>) => Promise<Document | null>;
  searchDocuments: (query: string, userId?: string, limit?: number, threshold?: number) => Promise<void>;
  reprocessDocument: (documentId: string, userId: string) => Promise<Document | null>;
  fetchSupportedTypes: () => Promise<void>;
  
  // 辅助方法
  setSelectedDocument: (document: Document | null) => void;
  clearError: () => void;
  setSearchQuery: (query: string) => void;
  clearSearchResults: () => void;
  resetUploadState: () => void;
}

export const useDocumentStore = create<DocumentState>()(
  subscribeWithSelector((set, get) => ({
    // 初始状态
    documents: [],
    searchResults: [],
    supportedTypes: [],
    loading: false,
    uploadProgress: 0,
    error: null,
    searchQuery: '',
    searchLoading: false,
    selectedDocument: null,

    // 获取用户文档列表
    fetchUserDocuments: async (userId: string) => {
      set({ loading: true, error: null });
      
      try {
        const response = await documentApi.getUserDocuments(userId);
        
        // app端返回包装的响应 { success: true, documents: Document[], count: number }
        if (response.data && response.data.success && response.data.documents) {
          set({ documents: response.data.documents, loading: false });
        } else {
          set({ 
            error: response.data?.message || '获取文档列表失败',
            loading: false 
          });
        }
      } catch (error: any) {
        console.error('获取文档列表失败:', error);
        set({ 
          error: error.response?.data?.message || error.message || '网络错误',
          loading: false 
        });
      }
    },

    // 上传文档
    uploadDocument: async (file: File, userId: string, tags?: string[], description?: string) => {
      set({ loading: true, uploadProgress: 0, error: null });
      
      console.log('documentStore上传文档，用户ID:', userId);
      
      try {
        const response = await documentApi.uploadDocument({
          file,
          userId,
          tags,
          description
        });
        
        // app端返回包装的响应 { success: true, message: string, document: Document }
        if (response.data && response.data.success && response.data.document) {
          // 更新文档列表
          const { documents } = get();
          set({ 
            documents: [response.data.document, ...documents],
            loading: false,
            uploadProgress: 100
          });
          
          // 重置上传进度
          setTimeout(() => set({ uploadProgress: 0 }), 2000);
          
          return response.data.document;
        } else {
          set({ 
            error: response.data?.message || '文档上传失败',
            loading: false,
            uploadProgress: 0
          });
          return null;
        }
      } catch (error: any) {
        console.error('文档上传失败:', error);
        
        let errorMessage = '文档上传失败';
        
        if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
          errorMessage = '文档处理超时，请检查网络连接或稍后重试。大文件可能需要更长时间处理。';
        } else if (error.response) {
          // 服务器响应错误
          if (error.response.status === 413) {
            errorMessage = '文件太大，请选择小于100MB的文件';
          } else if (error.response.status === 415) {
            errorMessage = '不支持的文件类型，请选择支持的文档格式';
          } else {
            errorMessage = error.response.data?.message || `服务器错误 (${error.response.status})`;
          }
        } else if (error.request) {
          // 网络请求失败
          errorMessage = '网络连接失败，请检查网络连接后重试';
        } else {
          // 其他错误
          errorMessage = error.message || '未知错误';
        }
        
        set({ 
          error: errorMessage,
          loading: false,
          uploadProgress: 0
        });
        return null;
      }
    },

    // 删除文档
    deleteDocument: async (documentId: string, userId: string) => {
      set({ loading: true, error: null });
      
      try {
        const response = await documentApi.deleteDocument(documentId, userId);
        
        // app端返回包装的响应 { success: true, message: string }
        if (response.data && response.data.success) {
          // 从列表中移除文档
          const { documents } = get();
          set({ 
            documents: documents.filter(doc => doc.id !== documentId),
            loading: false
          });
          
          // 如果删除的是当前选中的文档，清除选中状态
          const { selectedDocument } = get();
          if (selectedDocument?.id === documentId) {
            set({ selectedDocument: null });
          }
          
          return true;
        } else {
          set({ 
            error: response.data?.message || '文档删除失败',
            loading: false 
          });
          return false;
        }
      } catch (error: any) {
        console.error('文档删除失败:', error);
        set({ 
          error: error.response?.data?.message || error.message || '网络错误',
          loading: false 
        });
        return false;
      }
    },

    // 更新文档
    updateDocument: async (documentId: string, userId: string, updates: Partial<Document>) => {
      set({ loading: true, error: null });
      
      try {
        const response = await documentApi.updateDocument(documentId, userId, updates);
        
        // app端返回包装的响应 { success: true, message: string, document: Document }
        if (response.data && response.data.success && response.data.document) {
          // 更新文档列表中的文档
          const { documents } = get();
          const updatedDocuments = documents.map(doc => 
            doc.id === documentId ? response.data.document : doc
          );
          
          set({ 
            documents: updatedDocuments,
            loading: false
          });
          
          // 如果更新的是当前选中的文档，更新选中状态
          const { selectedDocument } = get();
          if (selectedDocument?.id === documentId) {
            set({ selectedDocument: response.data.document });
          }
          
          return response.data.document;
        } else {
          set({ 
            error: response.data?.message || '文档更新失败',
            loading: false 
          });
          return null;
        }
      } catch (error: any) {
        console.error('文档更新失败:', error);
        set({ 
          error: error.response?.data?.message || error.message || '网络错误',
          loading: false 
        });
        return null;
      }
    },

    // 搜索文档
    searchDocuments: async (query: string, userId?: string, limit?: number, threshold?: number) => {
      set({ searchLoading: true, error: null, searchQuery: query });
      
      try {
        const response = await documentApi.searchDocuments({
          query,
          userId,
          limit,
          threshold
        });
        
        // app端返回包装的响应 { success: true, query: string, results: SearchResult[], count: number }
        if (response.data && response.data.success && response.data.results) {
          set({ 
            searchResults: response.data.results,
            searchLoading: false
          });
        } else {
          set({ 
            error: response.data?.message || '搜索失败',
            searchLoading: false,
            searchResults: []
          });
        }
      } catch (error: any) {
        console.error('文档搜索失败:', error);
        set({ 
          error: error.response?.data?.message || error.message || '网络错误',
          searchLoading: false,
          searchResults: []
        });
      }
    },

    // 重新处理文档
    reprocessDocument: async (documentId: string, userId: string) => {
      set({ loading: true, error: null });
      
      try {
        const response = await documentApi.reprocessDocument(documentId, userId);
        
        // app端返回包装的响应 { success: true, message: string, document: Document }
        if (response.data && response.data.success && response.data.document) {
          // 更新文档列表中的文档
          const { documents } = get();
          const updatedDocuments = documents.map(doc => 
            doc.id === documentId ? response.data.document : doc
          );
          
          set({ 
            documents: updatedDocuments,
            loading: false
          });
          
          return response.data.document;
        } else {
          set({ 
            error: response.data?.message || '重新处理失败',
            loading: false 
          });
          return null;
        }
      } catch (error: any) {
        console.error('重新处理文档失败:', error);
        set({ 
          error: error.response?.data?.message || error.message || '网络错误',
          loading: false 
        });
        return null;
      }
    },

    // 获取支持的文件类型
    fetchSupportedTypes: async () => {
      try {
        const response = await documentApi.getSupportedTypes();
        
        // app端返回包装的响应 { success: true, supportedTypes: string[] }
        if (response.data && response.data.success && response.data.supportedTypes) {
          set({ supportedTypes: response.data.supportedTypes });
        }
      } catch (error: any) {
        console.error('获取支持的文件类型失败:', error);
      }
    },

    // 辅助方法
    setSelectedDocument: (document: Document | null) => {
      set({ selectedDocument: document });
    },

    clearError: () => {
      set({ error: null });
    },

    setSearchQuery: (query: string) => {
      set({ searchQuery: query });
    },

    clearSearchResults: () => {
      set({ searchResults: [], searchQuery: '' });
    },

    // 重置上传状态 - 清理错误、进度等
    resetUploadState: () => {
      set({ error: null, uploadProgress: 0, loading: false });
    }
  }))
); 