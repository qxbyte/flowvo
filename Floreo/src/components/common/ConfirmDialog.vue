<!-- 通用确认弹窗组件 -->
<template>
  <div class="confirm-dialog-overlay" v-if="visible" @click.self="handleCancel">
    <div class="confirm-dialog" :class="{ 'danger': isDanger }">
      <div class="close-button" @click="handleCancel">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </div>
      
      <div class="confirm-dialog-content">
        <div class="icon-container">
          <div class="icon" :class="{ 'danger-icon': isDanger }">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          </div>
        </div>
        <h3>{{ title }}</h3>
        <p>{{ message }}</p>
      </div>
      
      <div class="confirm-dialog-buttons">
        <button class="cancel-button" @click="handleCancel">{{ cancelText }}</button>
        <button class="confirm-button" :class="{ 'danger-button': isDanger }" @click="handleConfirm">{{ confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';

export default defineComponent({
  name: 'ConfirmDialog',
  props: {
    title: {
      type: String,
      default: '确认操作'
    },
    message: {
      type: String,
      default: '是否确认执行此操作？'
    },
    confirmText: {
      type: String,
      default: '确认'
    },
    cancelText: {
      type: String,
      default: '取消'
    },
    type: {
      type: String,
      default: 'default'
    }
  },
  emits: ['confirm', 'cancel'],
  setup(props, { emit }) {
    const visible = ref(false);
    
    // 根据类型判断是否为危险操作（如删除）
    const isDanger = computed(() => props.type === 'danger');

    const show = () => {
      visible.value = true;
    };

    const hide = () => {
      visible.value = false;
    };

    const handleConfirm = () => {
      emit('confirm');
      hide();
    };

    const handleCancel = () => {
      emit('cancel');
      hide();
    };

    return {
      visible,
      isDanger,
      show,
      hide,
      handleConfirm,
      handleCancel
    };
  }
});
</script>

<style scoped>
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.confirm-dialog {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  width: 85%;
  max-width: 360px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  position: relative;
}

.close-button {
  position: absolute;
  top: 8px;
  right: 8px;
  cursor: pointer;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
}

.close-button:hover {
  color: #333;
}

.confirm-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 0;
}

.icon-container {
  margin-bottom: 12px;
}

.icon {
  width: 36px;
  height: 36px;
  color: #f5a623;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon.danger-icon {
  color: #ff4d4f;
}

.confirm-dialog h3 {
  font-size: 1.1rem;
  margin-bottom: 8px;
  font-weight: 500;
  text-align: center;
}

.confirm-dialog p {
  color: #666;
  text-align: center;
  margin: 0;
  font-size: 0.9rem;
}

.confirm-dialog-buttons {
  display: flex;
  justify-content: space-between;
  padding-top: 8px;
}

.cancel-button, .confirm-button {
  flex: 1;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.cancel-button {
  background-color: #f5f5f5;
  color: #333;
  margin-right: 10px;
}

.confirm-button {
  background-color: #4399ff;
  color: white;
}

.danger-button {
  background-color: #ff4d4f;
  color: white;
}

.cancel-button:hover {
  background-color: #e5e5e5;
}

.confirm-button:hover {
  background-color: #3385e5;
}

.danger-button:hover {
  background-color: #ff1f1f;
}
</style> 