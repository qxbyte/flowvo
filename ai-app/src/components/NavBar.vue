<template>
  <nav class="navbar">
    <ul>
      <li><a href="#">Home</a></li>
      <li><a href="#">About</a></li>
      <li><a href="#">Contact</a></li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
// You can add any script logic here if needed
</script>

<style scoped>
.navbar {
  height: 3vh;
  background-color: #f8f9fa;
  display: flex;
  justify-content: center;
  align-items: center;
}

.navbar ul {
  list-style: none;
  display: flex;
  gap: 1rem;
}

.navbar li a {
  text-decoration: none;
  color: #333;
}
</style>