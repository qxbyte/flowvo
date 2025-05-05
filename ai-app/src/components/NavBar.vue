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
  height: 5vh;
  background-color: #dddddc;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 1rem 0;
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