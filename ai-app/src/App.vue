<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<!-- ✅ 局部样式（作用于 .app-container） -->
<style scoped>
.app-container {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg-color);  /* ✅ 使用变量 */
  color: var(--text-color);
  padding-top: 5rem; /* 为固定导航栏留出空间 */
}
</style>

<!-- ✅ 全局样式变量 -->
<style>
:root {
  --bg-color: #f9fafb;
  --text-color: #111827;
  --card-bg: #ffffff;
  --border-color: #e5e7eb;
}

@media (prefers-color-scheme: dark) {
  :root {
    --bg-color: #1e1e1e;
    --text-color: #c3c3c3;
    --card-bg: #2c2c2c;
    --border-color: #3a3a3a;
  }
}
</style>

