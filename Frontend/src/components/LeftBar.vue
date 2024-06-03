<template>
    <v-btn-toggle v-model="activeIndex" mandatory shaped class="left-bar">
        <v-btn variant="text" v-for="(item, i) in items" :key="i" color="indigo 500" class="bar-item"
            :prepend-icon="item.icon">
            {{ item.title }}
        </v-btn>
    </v-btn-toggle>
</template>

<script setup>
import { ref, watch, defineEmits, defineProps } from 'vue';

const emit = defineEmits(["trigger"]);

const props = defineProps({
    page: Number
});

const items = ref([
    { title: 'Dashboard', icon: 'mdi-view-dashboard' },
    { title: 'Profile', icon: 'mdi-account' },
    { title: 'Library', icon: 'mdi-notebook' },
    { title: 'Creator', icon: 'mdi-pen' },
]);

const activeIndex = ref(0);
watch(activeIndex, (newValue, oldValue) => {
    emit("trigger", newValue);
});

watch(() => props.page, (newValue, oldValue) => {
    activeIndex.value = newValue;
});
</script>

<style scoped>
.left-bar {
    height: 100%;
    padding-top: 0;
    display: flex;
    flex-direction: column;
    border-radius: 0%;
}

.bar-item {
    height: 65px !important;
    justify-content: start;
    padding-left: 24px;
    font-weight: 400 !important;
    font-size: 18px;
    letter-spacing: normal;
    text-transform: none;
    gap: 24px;
    color: rgb(100,100,100);
}
</style>
