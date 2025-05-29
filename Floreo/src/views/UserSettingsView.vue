<template>
  <div class="user-settings-container">
    <h1>User Settings</h1>
    <form @submit.prevent="saveSettings">
      <div class="form-group">
        <label for="username">Username</label>
        <input type="text" id="username" v-model="form.username" class="form-control">
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input type="email" id="email" v-model="form.email" class="form-control">
      </div>
      <div class="form-group">
        <label for="password">New Password</label>
        <input type="password" id="password" v-model="form.password" class="form-control">
      </div>
      <div class="form-group">
        <label for="confirm_password">Confirm New Password</label>
        <input type="password" id="confirm_password" v-model="form.confirm_password" class="form-control">
      </div>
      <button type="submit" class="btn btn-primary">Save Settings</button>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, onMounted } from 'vue';
import axios from 'axios'; // Assuming axios is used for API calls

export default defineComponent({
  name: 'UserSettingsView',
  setup() {
    const form = reactive({
      username: '',
      email: '',
      password: '',
      confirm_password: '',
    });

    onMounted(async () => {
      // Fetch current user settings when component mounts
      try {
        const response = await axios.get('/api/user/settings', {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        if (response.data) {
          form.username = response.data.username;
          form.email = response.data.email;
        }
      } catch (error) {
        console.error('Error fetching user settings:', error);
        // Handle error (e.g., show notification)
      }
    });

    const saveSettings = async () => {
      if (form.password !== form.confirm_password) {
        alert('Passwords do not match!');
        return;
      }
      try {
        const payload: any = {
          username: form.username,
          email: form.email,
        };
        if (form.password) {
          payload.password = form.password;
        }
        await axios.post('/api/user/settings', payload, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        alert('Settings saved successfully!');
        // Optionally, refresh settings or redirect
      } catch (error) {
        console.error('Error saving user settings:', error);
        // Handle error (e.g., show notification)
        alert('Failed to save settings.');
      }
    };

    return {
      form,
      saveSettings,
    };
  },
});
</script>

<style scoped>
.user-settings-container {
  max-width: 600px;
  margin: auto;
  padding: 20px;
}
.form-group {
  margin-bottom: 15px;
}
.form-control {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.btn {
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-primary {
  background-color: #007bff;
  color: white;
}
</style>
