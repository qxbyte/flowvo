import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Container,
  Heading,
  Text,
  Button,
  Input,
  FormControl,
  FormLabel,
  VStack,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Avatar,
  useToast,
  useColorModeValue,
  CircularProgress,
  Flex
} from '@chakra-ui/react';
import { FiEdit2 } from 'react-icons/fi';
import { authApi, userSettingsApi, type UserSettings } from '../../utils/api';

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

  // 颜色配置
  const bgColor = useColorModeValue('white', '#2D3748');
  const borderColor = useColorModeValue('gray.200', 'gray.600');
  const textColor = useColorModeValue('gray.700', 'gray.200');
  const mutedTextColor = useColorModeValue('gray.500', 'gray.400');
  const inputBg = useColorModeValue('white', 'gray.700');
  
  // 新增颜色变量
  const mainBgColor = useColorModeValue('gray.50', '#1B212C');
  const tabSelectedColor = useColorModeValue('blue.600', 'blue.300');
  const tabSelectedBg = useColorModeValue('white', 'gray.600');
  const inputHoverBorder = useColorModeValue('blue.300', 'blue.500');
  const inputFocusBorder = useColorModeValue('blue.500', 'blue.400');
  const inputFocusShadow = useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400');
  const buttonBg = useColorModeValue('blue.500', 'blue.600');
  const buttonHoverBg = useColorModeValue('blue.600', 'blue.500');
  const avatarBg = useColorModeValue('gray.100', 'gray.600');
  const uploadButtonBg = useColorModeValue('teal.500', 'teal.600');
  const uploadButtonHoverBg = useColorModeValue('teal.600', 'teal.500');

  // 页面加载时获取用户设置
  useEffect(() => {
    loadUserSettings();
  }, []);

  const loadUserSettings = async () => {
    try {
      setLoading(true);
      console.log('开始加载用户设置...');
      
      // 首先尝试使用用户设置API
      try {
        const response = await userSettingsApi.getUserSettings();
        console.log('用户设置API响应:', response.data);
        const settings = response.data;
        
        setUserSettings(settings);
        setNickname(settings.nickname);
        setEmail(settings.email);
        setPreviewUrl(settings.avatarUrl || '');
        return;
      } catch (settingsError) {
        console.log('用户设置API失败，尝试使用认证API:', settingsError);
        
        // 如果用户设置API失败，尝试使用认证API
        const authResponse = await authApi.getCurrentUser();
        console.log('认证API响应:', authResponse.data);
        
        const userInfo = authResponse.data;
        const settings: UserSettings = {
          username: userInfo.username || '',
          nickname: userInfo.name || '',
          email: userInfo.email || '',
          avatarUrl: userInfo.avatar || ''
        };
        
        setUserSettings(settings);
        setNickname(settings.nickname);
        setEmail(settings.email);
        setPreviewUrl(settings.avatarUrl || '');
      }
    } catch (error) {
      console.error('加载用户设置失败:', error);
      toast({
        title: '加载失败',
        description: '无法获取用户设置信息，请检查登录状态',
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
      const response = await userSettingsApi.updateNickname(nickname);
      
      // 更新本地状态
      setUserSettings(prev => ({ ...prev, nickname }));
      
      // 更新localStorage中的用户信息
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const parsed = JSON.parse(userInfo);
        parsed.name = nickname;
        localStorage.setItem('userInfo', JSON.stringify(parsed));
      }
      
      toast({
        title: '更新成功',
        description: response.data.message || '昵称已更新',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error: any) {
      console.error('更新昵称失败:', error);
      toast({
        title: '更新失败',
        description: error.response?.data?.message || '昵称更新失败，请稍后重试',
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
      const response = await userSettingsApi.updateEmail(email);
      
      // 更新本地状态
      setUserSettings(prev => ({ ...prev, email }));
      
      toast({
        title: '更新成功',
        description: response.data.message || '邮箱已更新',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error: any) {
      console.error('更新邮箱失败:', error);
      toast({
        title: '更新失败',
        description: error.response?.data?.message || '邮箱更新失败，请稍后重试',
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
        description: '请填写所有密码字段',
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
      const response = await userSettingsApi.updatePassword(currentPassword, newPassword, confirmNewPassword);

      // 清空密码字段
      setCurrentPassword('');
      setNewPassword('');
      setConfirmNewPassword('');

      toast({
        title: '更新成功',
        description: response.data.message || '密码已更新',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error: any) {
      console.error('更新密码失败:', error);
      toast({
        title: '更新失败',
        description: error.response?.data || '密码更新失败，请检查当前密码是否正确',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setPasswordLoading(false);
    }
  };

  // 头像文件选择处理
  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        toast({
          title: '文件类型错误',
          description: '请选择图片文件',
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
        return;
      }

      // 验证文件大小 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast({
          title: '文件过大',
          description: '图片大小不能超过5MB',
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
        title: '请选择头像文件',
        status: 'warning',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      setAvatarLoading(true);
      const response = await userSettingsApi.uploadAvatar(selectedFile);

      // 更新状态
      setUserSettings(prev => ({ ...prev, avatarUrl: response.data.avatarUrl }));
      setPreviewUrl(response.data.avatarUrl);
      setSelectedFile(null);

      toast({
        title: '上传成功',
        description: response.data.message || '头像已更新',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error: any) {
      console.error('上传头像失败:', error);
      toast({
        title: '上传失败',
        description: error.response?.data || '头像上传失败，请稍后重试',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setAvatarLoading(false);
    }
  };

  // 触发头像选择
  const triggerAvatarUpload = () => {
    inputRef.current?.click();
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minH="50vh">
        <VStack>
          <CircularProgress isIndeterminate color="blue.300" />
          <Text>加载用户设置中...</Text>
        </VStack>
      </Box>
    );
  }

  return (
    <Box p={8} maxWidth="800px" mx="auto" bg={mainBgColor} minH="100vh">
      <Box bg={bgColor} p={8} borderRadius="xl" boxShadow="lg" borderWidth="1px" borderColor={borderColor}>
        <Heading as="h1" size="xl" mb={8} textAlign="center" color={textColor}>
          用户设置
        </Heading>

        <Tabs variant="enclosed-colored" colorScheme="blue">
          <TabList mb="1em" bg={bgColor}>
            <Tab color={mutedTextColor} _selected={{ color: tabSelectedColor, bg: tabSelectedBg }}>个人资料</Tab>
            <Tab color={mutedTextColor} _selected={{ color: tabSelectedColor, bg: tabSelectedBg }}>账户安全</Tab>
            <Tab color={mutedTextColor} _selected={{ color: tabSelectedColor, bg: tabSelectedBg }}>头像设置</Tab>
          </TabList>
          <TabPanels>
            <TabPanel p={6}>
              <VStack spacing={6} align="stretch">
                <FormControl id="nickname">
                  <FormLabel color={textColor}>昵称</FormLabel>
                  <Input
                    type="text"
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    placeholder="输入您的昵称"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{ borderColor: inputHoverBorder }}
                    _focus={{ borderColor: inputFocusBorder, boxShadow: inputFocusShadow }}
                  />
                </FormControl>
                <Button 
                  colorScheme="blue" 
                  onClick={handleUpdateNickname}
                  isLoading={nicknameLoading}
                  loadingText="保存中..."
                  bg={buttonBg}
                  _hover={{ bg: buttonHoverBg }}
                >
                  保存昵称
                </Button>

                <FormControl id="username">
                  <FormLabel color={textColor}>用户名 (不可更改)</FormLabel>
                  <Input
                    type="text"
                    value={userSettings.username}
                    isReadOnly
                    disabled 
                    bg={avatarBg}
                    borderColor={borderColor}
                    color={mutedTextColor}
                  />
                </FormControl>
              </VStack>
            </TabPanel>

            <TabPanel p={6}>
              <VStack spacing={6} align="stretch">
                <FormControl id="email">
                  <FormLabel color={textColor}>邮箱</FormLabel>
                  <Input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="输入您的邮箱"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{ borderColor: inputHoverBorder }}
                    _focus={{ borderColor: inputFocusBorder, boxShadow: inputFocusShadow }}
                  />
                </FormControl>
                <Button 
                  colorScheme="blue" 
                  onClick={handleUpdateEmail}
                  isLoading={emailLoading}
                  loadingText="保存中..."
                  bg={buttonBg}
                  _hover={{ bg: buttonHoverBg }}
                >
                  保存邮箱
                </Button>

                <FormControl id="current-password">
                  <FormLabel color={textColor}>当前密码</FormLabel>
                  <Input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    placeholder="输入当前密码"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{ borderColor: inputHoverBorder }}
                    _focus={{ borderColor: inputFocusBorder, boxShadow: inputFocusShadow }}
                  />
                </FormControl>

                <FormControl id="new-password">
                  <FormLabel color={textColor}>新密码</FormLabel>
                  <Input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="输入新密码（至少6位）"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{ borderColor: inputHoverBorder }}
                    _focus={{ borderColor: inputFocusBorder, boxShadow: inputFocusShadow }}
                  />
                </FormControl>

                <FormControl id="confirm-new-password">
                  <FormLabel color={textColor}>确认新密码</FormLabel>
                  <Input
                    type="password"
                    value={confirmNewPassword}
                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                    placeholder="再次输入新密码"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{ borderColor: inputHoverBorder }}
                    _focus={{ borderColor: inputFocusBorder, boxShadow: inputFocusShadow }}
                  />
                </FormControl>

                <Button 
                  colorScheme="blue" 
                  onClick={handleUpdatePassword}
                  isLoading={passwordLoading}
                  loadingText="更新中..."
                  bg={buttonBg}
                  _hover={{ bg: buttonHoverBg }}
                >
                  更新密码
                </Button>
              </VStack>
            </TabPanel>
            
            <TabPanel p={6}>
              <VStack spacing={6} align="center">
                <Text fontSize="lg" mb={4} color={textColor}>更换头像</Text>
                <Avatar 
                  size="2xl" 
                  name={userSettings.nickname || userSettings.username} 
                  src={previewUrl || userSettings.avatarUrl} 
                  mb={4}
                  borderRadius="full"
                  borderWidth="2px"
                  borderColor={borderColor}
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
                  bg={uploadButtonBg}
                  _hover={{ bg: uploadButtonHoverBg }}
                >
                  选择新头像
                </Button>
                {selectedFile && (
                  <Text fontSize="sm" color={mutedTextColor}>
                    已选择: {selectedFile.name}
                  </Text>
                )}
                <Text fontSize="sm" color={mutedTextColor} mt={2} textAlign="center">
                  支持 JPG、PNG 格式，文件大小不超过 5MB
                </Text>
                <Button 
                  colorScheme="blue" 
                  mt={6}
                  onClick={handleUploadAvatar}
                  isLoading={avatarLoading}
                  loadingText="上传中..."
                  isDisabled={!selectedFile}
                  bg={buttonBg}
                  _hover={{ bg: buttonHoverBg }}
                >
                  保存头像
                </Button>
              </VStack>
            </TabPanel>

          </TabPanels>
        </Tabs>
      </Box>
    </Box>
  );
};

export default SettingsPage;
