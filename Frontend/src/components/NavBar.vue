<template>
    <v-app-bar class="container" :elevation="2">
        <template v-slot:prepend>
            <v-app-bar-nav-icon style="margin-left: 4px;" @click="shift"></v-app-bar-nav-icon>
        </template>
        <v-app-bar-title>QuizCore</v-app-bar-title>
        <v-spacer></v-spacer>
        <div class="user">{{ props.user }}</div>
        <v-menu>
            <template v-slot:activator="{ props }">
                <v-btn icon="mdi-dots-vertical" v-bind="props"></v-btn>
            </template>
            <v-list class="list">
                <v-btn :prepend-icon="item.icon" @click="item.onClick" v-for="(item, i) in items" :key="i"
                    class="button" variant="text">{{
                        item.title }}
                </v-btn>
            </v-list>
        </v-menu>
    </v-app-bar>
</template>

<script setup>
import { defineEmits, defineProps, onMounted, watch } from "vue";
import router from "../router";
import { SETSTUDENT, SETTOKEN } from "../main";

const props = defineProps({ setPage: Function, user: Array });
const emit = defineEmits(["trigger"]);

function shift() {
    emit("trigger");
}

watch(() => props.user, () => {
    console.log(props.user);
});

const items = [
    { title: "Dashboard", onClick: () => props.setPage(0), icon: "mdi-view-dashboard" },
    {
        title: "Sign Out", onClick: () => {
            SETTOKEN(null);
            SETSTUDENT(null);
            router.push("/login");
        }, icon: "mdi-logout",
    }
];
</script>

<style scoped>
.container {
    background-color: var(--primary-dark) !important;
    color: white !important;
    box-shadow: none !important;
    border: none !important;
}

.list {
    display: flex;
    flex-direction: column;
}

.button {
    height: 50px !important;
    border-radius: 0%;
    text-transform: none;
    font-weight: 500;
    font-size: 18px;
    letter-spacing: 0px;
    justify-content: start;
    width: 220px !important;
    gap: 10px;
}

.user {
    margin-right: 30px;
    font-size: 1.2rem;
    text-overflow: ellipsis;
}

@media screen and (max-width: 600px) {
    .shifticon {
        display: none;
    }

}
</style>