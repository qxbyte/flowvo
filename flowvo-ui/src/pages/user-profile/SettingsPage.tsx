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
        User Settings
      </Heading>

      <Tabs variant="enclosed-colored" colorScheme="blue">
        <TabList mb="1em">
          <Tab>Profile</Tab>
          <Tab>Account Security</Tab>
          <Tab>Avatar</Tab>
        </TabList>
        <TabPanels>
          <TabPanel>
            <VStack spacing={6} align="stretch">
              <FormControl id="nickname">
                <FormLabel>Nickname</FormLabel>
                <Input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="Enter your nickname"
                />
              </FormControl>
              <FormControl id="username">
                <FormLabel>Username (cannot be changed)</FormLabel>
                <Input
                  type="text"
                  value={username}
                  isReadOnly // Username is typically not changeable
                  disabled 
                />
              </FormControl>
              <Button colorScheme="blue" mt={4}>
                Save Profile Changes
              </Button>
            </VStack>
          </TabPanel>

          <TabPanel>
            <VStack spacing={6} align="stretch">
              <FormControl id="email">
                <FormLabel>Email Address</FormLabel>
                <Input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email address"
                />
                <Button size="sm" variant="link" colorScheme="blue" mt={1}>
                  Change Email
                </Button>
              </FormControl>
              
              <Heading as="h3" size="md" mt={6} mb={3}>Change Password</Heading>
              <FormControl id="current-password">
                <FormLabel>Current Password</FormLabel>
                <Input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="Enter your current password"
                />
              </FormControl>
              <FormControl id="new-password">
                <FormLabel>New Password</FormLabel>
                <Input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter your new password"
                />
              </FormControl>
              <FormControl id="confirm-new-password">
                <FormLabel>Confirm New Password</FormLabel>
                <Input
                  type="password"
                  value={confirmNewPassword}
                  onChange={(e) => setConfirmNewPassword(e.target.value)}
                  placeholder="Confirm your new password"
                />
              </FormControl>
              <Button colorScheme="blue" mt={4}>
                Change Password
              </Button>
            </VStack>
          </TabPanel>
          
          <TabPanel>
            <VStack spacing={6} align="center">
              <Text fontSize="lg" mb={4}>Change Your Avatar</Text>
              <Avatar size="2xl" name={nickname || username} src={userAvatar} mb={4} />
              <input
                type="file"
                accept="image/*"
                onChange={handleAvatarChange}
                ref={inputRef}
                style={{ display: 'none' }}
              />
              <Button leftIcon={<FiEdit2 />} colorScheme="teal" onClick={triggerAvatarUpload}>
                Upload New Avatar
              </Button>
              <Text fontSize="sm" color="gray.500" mt={2}>
                Click to upload a new image.
              </Text>
              <Button colorScheme="blue" mt={6}>
                Save Avatar
              </Button>
            </VStack>
          </TabPanel>

        </TabPanels>
      </Tabs>
    </Box>
  );
};

export default SettingsPage;
