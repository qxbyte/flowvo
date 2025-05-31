import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Heading,
  FormControl,
  FormLabel,
  Input,
  Button,
  VStack,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Avatar,
  IconButton,
  Flex,
  Text,
  useToast,
  Alert,
  AlertIcon,
  AlertTitle,
  AlertDescription,
  CloseButton,
  CircularProgress
} from '@chakra-ui/react';
import { FiEdit2 } from 'react-icons/fi';

interface UserSettings {
  username: string;
  nickname: string;
  email: string;
  avatarUrl?: string;
}

const SettingsPage: React.FC = () => {
  const toast = useToast();
  const [loading, setLoading] = useState(false);
  const [userSettings, setUserSettings] = useState<UserSettings>({
    username: '',
    nickname: '',
    email: '',
    avatarUrl: ''
  });

  // 个人资料状态
  const [nickname, setNickname] = useState('');
  const [nicknameLoading, setNicknameLoading] = useState(false);

  // 邮箱状态
  const [email, setEmail] = useState('');
  const [emailLoading, setEmailLoading] = useState(false);

  // 密码状态
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);

  // 头像状态
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [avatarLoading, setAvatarLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  // 获取JWT Token
  const getAuthToken = () => {
    return localStorage.getItem('token');
  };

  // 构建请求头
  const getAuthHeaders = () => {
    const token = getAuthToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    };
  };

  // 构建文件上传请求头
  const getFileUploadHeaders = () => {
    const token = getAuthToken();
    return {
      'Authorization': token ? `Bearer ${token}` : ''
    };
  };

  // 页面加载时获取用户设置
  useEffect(() => {
    loadUserSettings();
  }, []);

  const loadUserSettings = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/user/settings', {
        method: 'GET',
        headers: getAuthHeaders()
      });

      if (response.ok) {
        const data: UserSettings = await response.json();
        setUserSettings(data);
        setNickname(data.nickname || '');
        setEmail(data.email || '');
        setPreviewUrl(data.avatarUrl || '');
      } else {
        toast({
          title: '加载失败',
          description: '无法获取用户设置信息',
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      toast({
        title: '网络错误',
        description: '请检查网络连接',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setLoading(false);
    }
  };

  // 更新昵称
  const handleUpdateNickname = async () => {
    if (!nickname.trim()) {
      toast({
        title: '输入错误',
        description: '昵称不能为空',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      setNicknameLoading(true);
      const response = await fetch('/api/user/settings/nickname', {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ nickname })
      });

      if (response.ok) {
        const data = await response.json();
        setUserSettings(prev => ({ ...prev, nickname: data.nickname }));
        toast({
          title: '更新成功',
          description: '昵称已更新',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
      } else {
        const errorMessage = await response.text();
        toast({
          title: '更新失败',
          description: errorMessage,
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      toast({
        title: '网络错误',
        description: '请检查网络连接',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setNicknameLoading(false);
    }
  };

  // 更新邮箱
  const handleUpdateEmail = async () => {
    if (!email.trim()) {
      toast({
        title: '输入错误',
        description: '邮箱不能为空',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    // 简单的邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      toast({
        title: '输入错误',
        description: '请输入有效的邮箱地址',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      setEmailLoading(true);
      const response = await fetch('/api/user/settings/email', {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ email })
      });

      if (response.ok) {
        const data = await response.json();
        setUserSettings(prev => ({ ...prev, email: data.email }));
        toast({
          title: '更新成功',
          description: '邮箱已更新',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
      } else {
        const errorMessage = await response.text();
        toast({
          title: '更新失败',
          description: errorMessage,
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      toast({
        title: '网络错误',
        description: '请检查网络连接',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setEmailLoading(false);
    }
  };

  // 更新密码
  const handleUpdatePassword = async () => {
    if (!currentPassword || !newPassword || !confirmNewPassword) {
      toast({
        title: '输入错误',
        description: '所有密码字段都必须填写',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    if (newPassword !== confirmNewPassword) {
      toast({
        title: '输入错误',
        description: '新密码与确认密码不匹配',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    if (newPassword.length < 6) {
      toast({
        title: '输入错误',
        description: '新密码长度至少为6位',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      setPasswordLoading(true);
      const response = await fetch('/api/user/settings/password', {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({
          currentPassword,
          newPassword,
          confirmPassword: confirmNewPassword
        })
      });

      if (response.ok) {
        setCurrentPassword('');
        setNewPassword('');
        setConfirmNewPassword('');
        toast({
          title: '更新成功',
          description: '密码已更新',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
      } else {
        const errorMessage = await response.text();
        toast({
          title: '更新失败',
          description: errorMessage,
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      toast({
        title: '网络错误',
        description: '请检查网络连接',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setPasswordLoading(false);
    }
  };

  // 处理头像文件选择
  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        toast({
          title: '文件错误',
          description: '只能选择图片文件',
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
        return;
      }

      // 检查文件大小 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast({
          title: '文件错误',
          description: '文件大小不能超过5MB',
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
        return;
      }

      setSelectedFile(file);
      
      // 创建预览URL
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreviewUrl(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // 上传头像
  const handleUploadAvatar = async () => {
    if (!selectedFile) {
      toast({
        title: '选择文件',
        description: '请先选择要上传的头像',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      setAvatarLoading(true);
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await fetch('/api/user/settings/avatar', {
        method: 'POST',
        headers: getFileUploadHeaders(),
        body: formData
      });

      if (response.ok) {
        const data = await response.json();
        setUserSettings(prev => ({ ...prev, avatarUrl: data.avatarUrl }));
        setSelectedFile(null);
        toast({
          title: '上传成功',
          description: '头像已更新',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
      } else {
        const errorMessage = await response.text();
        toast({
          title: '上传失败',
          description: errorMessage,
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      toast({
        title: '网络错误',
        description: '请检查网络连接',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setAvatarLoading(false);
    }
  };

  const triggerAvatarUpload = () => {
    inputRef.current?.click();
  };

  if (loading) {
    return (
      <Box p={8} maxWidth="800px" mx="auto" textAlign="center">
        <CircularProgress isIndeterminate color="blue.300" />
        <Text mt={4}>加载中...</Text>
      </Box>
    );
  }

  return (
    <Box p={8} maxWidth="800px" mx="auto">
      <Heading as="h1" size="xl" mb={8} textAlign="center">
        用户设置
      </Heading>

      <Tabs variant="enclosed-colored" colorScheme="blue">
        <TabList mb="1em">
          <Tab>个人资料</Tab>
          <Tab>账户安全</Tab>
          <Tab>头像</Tab>
        </TabList>
        <TabPanels>
          <TabPanel>
            <VStack spacing={6} align="stretch">
              <FormControl id="nickname">
                <FormLabel>昵称</FormLabel>
                <Input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="输入您的昵称"
                />
              </FormControl>
              <Button 
                colorScheme="blue" 
                onClick={handleUpdateNickname}
                isLoading={nicknameLoading}
                loadingText="保存中..."
              >
                保存昵称
              </Button>

              <FormControl id="username">
                <FormLabel>用户名 (不可更改)</FormLabel>
                <Input
                  type="text"
                  value={userSettings.username}
                  isReadOnly
                  disabled 
                />
              </FormControl>
            </VStack>
          </TabPanel>

          <TabPanel>
            <VStack spacing={6} align="stretch">
              <FormControl id="email">
                <FormLabel>邮箱地址</FormLabel>
                <Input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="输入您的邮箱地址"
                />
                <Button 
                  size="sm" 
                  colorScheme="blue" 
                  mt={2}
                  onClick={handleUpdateEmail}
                  isLoading={emailLoading}
                  loadingText="更新中..."
                >
                  更新邮箱
                </Button>
              </FormControl>
              
              <Heading as="h3" size="md" mt={6} mb={3}>更改密码</Heading>
              <FormControl id="current-password">
                <FormLabel>当前密码</FormLabel>
                <Input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="输入您的当前密码"
                />
              </FormControl>
              <FormControl id="new-password">
                <FormLabel>新密码</FormLabel>
                <Input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="输入您的新密码"
                />
              </FormControl>
              <FormControl id="confirm-new-password">
                <FormLabel>确认新密码</FormLabel>
                <Input
                  type="password"
                  value={confirmNewPassword}
                  onChange={(e) => setConfirmNewPassword(e.target.value)}
                  placeholder="确认您的新密码"
                />
              </FormControl>
              <Button 
                colorScheme="blue"
                onClick={handleUpdatePassword}
                isLoading={passwordLoading}
                loadingText="更新中..."
              >
                更改密码
              </Button>
            </VStack>
          </TabPanel>
          
          <TabPanel>
            <VStack spacing={6} align="center">
              <Text fontSize="lg" mb={4}>更换头像</Text>
              <Avatar 
                size="2xl" 
                name={userSettings.nickname || userSettings.username} 
                src={previewUrl || userSettings.avatarUrl} 
                mb={4}
                borderRadius="full"
              />
              <input
                type="file"
                accept="image/*"
                onChange={handleAvatarChange}
                ref={inputRef}
                style={{ display: 'none' }}
              />
              <Button 
                leftIcon={<FiEdit2 />} 
                colorScheme="teal" 
                onClick={triggerAvatarUpload}
              >
                选择新头像
              </Button>
              {selectedFile && (
                <Text fontSize="sm" color="gray.600">
                  已选择: {selectedFile.name}
                </Text>
              )}
              <Text fontSize="sm" color="gray.500" mt={2}>
                支持 JPG、PNG 格式，文件大小不超过 5MB
              </Text>
              <Button 
                colorScheme="blue" 
                mt={6}
                onClick={handleUploadAvatar}
                isLoading={avatarLoading}
                loadingText="上传中..."
                isDisabled={!selectedFile}
              >
                保存头像
              </Button>
            </VStack>
          </TabPanel>

        </TabPanels>
      </Tabs>
    </Box>
  );
};

export default SettingsPage;
