<template>
  Hello World
</template>

<script setup>
import router from '../router';
import { onMounted } from 'vue';


onMounted(() => {
  console.log("Hello World")
  router.push("/welcome");
});
</script>
