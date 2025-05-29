import React from 'react';
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
  Text
} from '@chakra-ui/react';
import { FiEdit2 } from 'react-icons/fi'; // Icon for edit

const SettingsPage: React.FC = () => {
  // Placeholder state and handlers for now
  const [username, setUsername] = React.useState('currentUsername');
  const [nickname, setNickname] = React.useState('currentNickname');
  const [email, setEmail] = React.useState('current.email@example.com');
  const [currentPassword, setCurrentPassword] = React.useState('');
  const [newPassword, setNewPassword] = React.useState('');
  const [confirmNewPassword, setConfirmNewPassword] = React.useState('');
  const [userAvatar, setUserAvatar] = React.useState('/person.svg'); // Default or current avatar

  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setUserAvatar(e.target?.result as string);
      };
      reader.readAsDataURL(event.target.files[0]);
    }
  };

  const inputRef = React.useRef<HTMLInputElement>(null);

  const triggerAvatarUpload = () => {
    inputRef.current?.click();
  };

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
              <FormControl id="username">
                <FormLabel>用户名 (不可更改)</FormLabel>
                <Input
                  type="text"
                  value={username}
                  isReadOnly // Username is typically not changeable
                  disabled 
                />
              </FormControl>
              <Button colorScheme="blue" mt={4}>
                保存个人资料更改
              </Button>
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
                <Button size="sm" variant="link" colorScheme="blue" mt={1}>
                  更改邮箱
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
              <Button colorScheme="blue" mt={4}>
                更改密码
              </Button>
            </VStack>
          </TabPanel>
          
          <TabPanel>
            <VStack spacing={6} align="center">
              <Text fontSize="lg" mb={4}>更换头像</Text>
              <Avatar size="2xl" name={nickname || username} src={userAvatar} mb={4} />
              <input
                type="file"
                accept="image/*"
                onChange={handleAvatarChange}
                ref={inputRef}
                style={{ display: 'none' }}
              />
              <Button leftIcon={<FiEdit2 />} colorScheme="teal" onClick={triggerAvatarUpload}>
                上传新头像
              </Button>
              <Text fontSize="sm" color="gray.500" mt={2}>
                点击上传新图片。
              </Text>
              <Button colorScheme="blue" mt={6}>
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
