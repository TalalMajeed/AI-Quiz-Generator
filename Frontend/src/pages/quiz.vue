<template>
    <div>This is an Active Quiz</div>
    <div>{{ id }}</div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const id = ref(Number(route.query.id));

watch(() => route.query.id, (newId) => {
    id.value = Number(newId);
});
</script>