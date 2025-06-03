import React, { useState, useCallback, useEffect } from 'react';
import {
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalCloseButton,
  VStack,
  HStack,
  Box,
  Text,
  Button,
  Input,
  Textarea,
  Select,
  Progress,
  Alert,
  AlertIcon,
  AlertTitle,
  AlertDescription,
  CloseButton,
  useToast,
  useColorModeValue,
  Badge,
  Tag,
  TagLabel,
  TagCloseButton,
  Icon
} from '@chakra-ui/react';
import { useDropzone } from 'react-dropzone';
import { FiUpload, FiFile, FiX } from 'react-icons/fi';
import { useAuth } from '../hooks/useAuth';
import { useDocumentStore } from '../stores/documentStore';
import { knowledgeQaApi, type DocumentCategory } from '../utils/api';

interface DocumentUploadProps {
  isOpen?: boolean;
  onUploadComplete?: () => void;
  onUploadSuccess?: () => void;
  onClose?: () => void;
}

const DocumentUpload: React.FC<DocumentUploadProps> = ({ isOpen, onUploadComplete, onUploadSuccess, onClose }) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [description, setDescription] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const [newTag, setNewTag] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  // 简化的默认分类列表，与DocumentsPage保持一致
  const defaultCategories = [
    { id: 'cat_user_manual', name: '用户手册' },
    { id: 'cat_technical_doc', name: '技术文档' },
    { id: 'cat_training_material', name: '培训材料' },
    { id: 'cat_faq', name: '常见问题' },
    { id: 'cat_policy', name: '政策制度' },
    { id: 'cat_other', name: '其他' }
  ];

  const { userInfo } = useAuth();
  const { uploadDocument, loading, uploadProgress, error, supportedTypes, resetUploadState, clearError } = useDocumentStore();
  const toast = useToast();
  
  const borderColor = useColorModeValue('gray.300', 'gray.600');
  const hoverBorderColor = useColorModeValue('blue.400', 'blue.300');
  const bgColor = useColorModeValue('gray.50', 'gray.700');
  const hoverBgColor = useColorModeValue('blue.50', 'blue.900');

  // 组件挂载时清理上传状态和加载分类
  useEffect(() => {
    resetUploadState();
  }, [resetUploadState]);

  // 文件拖拽处理
  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      setSelectedFile(acceptedFiles[0]);
      clearError();
    }
  }, [clearError]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false,
    accept: {
      'application/pdf': ['.pdf'],
      'application/msword': ['.doc'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'application/vnd.ms-excel': ['.xls'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
      'application/vnd.ms-powerpoint': ['.ppt'],
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': ['.pptx'],
      'text/plain': ['.txt'],
      'text/csv': ['.csv'],
      'text/markdown': ['.md', '.markdown'],
      'application/json': ['.json'],
      'application/xml': ['.xml'],
      'text/yaml': ['.yaml', '.yml'],
      'text/log': ['.log']
    }
  });

  // 添加标签
  const addTag = () => {
    if (newTag.trim() && !tags.includes(newTag.trim())) {
      setTags([...tags, newTag.trim()]);
      setNewTag('');
    }
  };

  // 删除标签
  const removeTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  // 处理按键事件
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      addTag();
    }
  };

  // 上传文档
  const handleUpload = async () => {
    if (!selectedFile) {
      toast({
        title: '上传失败',
        description: '请选择文件',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    if (!userInfo) {
      console.log('用户信息不可用:', userInfo);
      toast({
        title: '上传失败',
        description: '请先登录',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      console.log('开始上传文档，用户信息:', { id: userInfo.id, username: userInfo.username });
      
      const result = await uploadDocument(
        selectedFile,
        userInfo.id,
        tags.length > 0 ? tags : undefined,
        description.trim() || undefined,
        selectedCategory || undefined
      );

      if (result) {
        toast({
          title: '上传成功',
          description: `文档 "${result.name}" 已成功上传`,
          status: 'success',
          duration: 3000,
          isClosable: true,
        });

        // 重置表单
        setSelectedFile(null);
        setTags([]);
        setDescription('');
        setNewTag('');
        setSelectedCategory('');

        // 回调
        onUploadComplete?.();
        onUploadSuccess?.();
      }
    } catch (err) {
      console.error('上传失败:', err);
    }
  };

  // 清除选中文件
  const clearSelectedFile = () => {
    setSelectedFile(null);
    clearError();
  };

  // 获取文件大小的可读格式
  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <Modal isOpen={isOpen || false} onClose={onClose || (() => {})}>
      <ModalOverlay />
      <ModalContent>
        <ModalHeader>上传文档</ModalHeader>
        <ModalCloseButton />
        <ModalBody>
          <VStack spacing={6} align="stretch">
            {/* 错误提示 */}
            {error && (
              <Alert status="error" borderRadius="md">
                <AlertIcon />
                <Box flex="1">
                  <AlertTitle fontSize="sm">上传失败!</AlertTitle>
                  <AlertDescription fontSize="sm">{error}</AlertDescription>
                </Box>
                <CloseButton onClick={clearError} />
              </Alert>
            )}

            {/* 文件选择区域 */}
            {!selectedFile ? (
              <Box
                {...getRootProps()}
                border="2px dashed"
                borderColor={isDragActive ? hoverBorderColor : borderColor}
                borderRadius="lg"
                p={8}
                textAlign="center"
                bg={isDragActive ? hoverBgColor : bgColor}
                cursor="pointer"
                transition="all 0.2s"
                _hover={{
                  borderColor: hoverBorderColor,
                  bg: hoverBgColor
                }}
              >
                <input {...getInputProps()} />
                <VStack spacing={4}>
                  <Icon as={FiUpload} w={12} h={12} color={useColorModeValue('gray.400', 'gray.500')} />
                  <Text fontSize="lg" fontWeight="medium">
                    {isDragActive ? '放开以上传文件' : '拖拽文件到此处或点击选择'}
                  </Text>
                  <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>
                    支持 PDF, Word, Excel, PowerPoint, 文本文件等
                  </Text>
                  {supportedTypes.length > 0 && (
                    <HStack wrap="wrap" spacing={1} justify="center">
                      {supportedTypes.slice(0, 8).map((type) => (
                        <Badge key={type} variant="outline" colorScheme="blue" fontSize="xs">
                          {type.toUpperCase()}
                        </Badge>
                      ))}
                      {supportedTypes.length > 8 && (
                        <Badge variant="outline" colorScheme="gray" fontSize="xs">
                          +{supportedTypes.length - 8}
                        </Badge>
                      )}
                    </HStack>
                  )}
                </VStack>
              </Box>
            ) : (
              <Box
                p={4}
                border="1px solid"
                borderColor={borderColor}
                borderRadius="md"
                bg={bgColor}
              >
                <HStack justify="space-between">
                  <HStack>
                    <Icon as={FiFile} color="blue.500" />
                    <VStack align="start" spacing={0}>
                      <Text fontSize="sm" fontWeight="medium">{selectedFile.name}</Text>
                      <Text fontSize="xs" color={useColorModeValue('gray.500', 'gray.400')}>
                        {formatFileSize(selectedFile.size)}
                      </Text>
                    </VStack>
                  </HStack>
                  <Button size="sm" variant="ghost" onClick={clearSelectedFile}>
                    <FiX />
                  </Button>
                </HStack>
              </Box>
            )}

            {/* 标签输入 */}
            <VStack align="stretch" spacing={3}>
              <Text fontSize="sm" fontWeight="medium">标签（可选）</Text>
              <HStack>
                <Input
                  placeholder="添加标签"
                  value={newTag}
                  onChange={(e) => setNewTag(e.target.value)}
                  onKeyPress={handleKeyPress}
                  size="sm"
                />
                <Button 
                  size="sm" 
                  onClick={addTag} 
                  colorScheme="blue" 
                  variant="outline"
                  borderColor={useColorModeValue('blue.500', 'blue.300')}
                  color={useColorModeValue('blue.500', 'blue.300')}
                  _hover={{
                    bg: useColorModeValue('blue.50', 'blue.900'),
                    borderColor: useColorModeValue('blue.600', 'blue.200')
                  }}
                >
                  添加
                </Button>
              </HStack>
              {tags.length > 0 && (
                <HStack wrap="wrap" spacing={2}>
                  {tags.map((tag) => (
                    <Tag key={tag} size="sm" colorScheme="blue" variant="solid">
                      <TagLabel>{tag}</TagLabel>
                      <TagCloseButton onClick={() => removeTag(tag)} />
                    </Tag>
                  ))}
                </HStack>
              )}
            </VStack>

            {/* 描述输入 */}
            <VStack align="stretch" spacing={3}>
              <Text fontSize="sm" fontWeight="medium">描述（可选）</Text>
              <Textarea
                placeholder="为文档添加描述..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                size="sm"
                rows={3}
              />
            </VStack>

            {/* 分类选择 */}
            <VStack align="stretch" spacing={3}>
              <Text fontSize="sm" fontWeight="medium">分类（可选）</Text>
              <Select
                placeholder="选择分类"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                {defaultCategories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </Select>
            </VStack>

            {/* 上传进度 */}
            {loading && uploadProgress > 0 && (
              <VStack align="stretch" spacing={2}>
                <Text fontSize="sm">上传中...</Text>
                <Progress value={uploadProgress} colorScheme="blue" borderRadius="full" />
              </VStack>
            )}

            {/* 操作按钮 */}
            <HStack justify="end" spacing={3}>
              {onClose && (
                <Button 
                  variant="ghost" 
                  onClick={onClose}
                  color={useColorModeValue('gray.600', 'gray.300')}
                  _hover={{
                    bg: useColorModeValue('gray.100', 'gray.600')
                  }}
                >
                  取消
                </Button>
              )}
              <Button
                colorScheme="blue"
                onClick={handleUpload}
                isLoading={loading}
                loadingText="上传中"
                isDisabled={!selectedFile}
                bg={useColorModeValue('blue.500', 'blue.600')}
                color="white"
                _hover={{
                  bg: useColorModeValue('blue.600', 'blue.500')
                }}
                _active={{
                  bg: useColorModeValue('blue.700', 'blue.400')
                }}
              >
                上传文档
              </Button>
            </HStack>
          </VStack>
        </ModalBody>
      </ModalContent>
    </Modal>
  );
};

export default DocumentUpload; 