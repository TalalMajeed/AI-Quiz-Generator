<template>
    <div v-show="auth">
        <NavBar :button="true" @trigger="setOpenMenu" :setPage="setPage" :user="user" />
        <div class="main" v-if="user">
            <div class="left" :style="shiftWidth()">
                <LeftBar @trigger="setPage" :page="currentPage" />
            </div>
            <div class="center">
                <Dashboard v-show="currentPage == 0" :page="currentPage" />
                <Profile v-show="currentPage == 1" :page="currentPage" />
                <Library v-show="currentPage == 2" :page="currentPage" />
                <Creator v-show="currentPage == 3" :page="currentPage" />
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, watch, defineProps, onMounted } from 'vue';
import { SETCURRENT, GETSTUDENT, SETSTUDENT, API, SETTOKEN, GETTOKEN, ATTEMPT, SETATTEMPT } from '../main';
import router from '../router';

const currentPage = ref(0);
const user = ref("");
const auth = ref(false);
const openMenu = ref(true);


onMounted(() => {
    SETCURRENT('/panel');
    if(!GETSTUDENT()) router.push('/login');
    if(!GETTOKEN()) router.push('/login');
    authenticate();
    setInterval(authenticate, 60 * 20 * 1000);
});


async function authenticate() {
    try {
        const response = await fetch(API + '/check/user', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                email: GETSTUDENT().email,
                id: GETSTUDENT().id
            })
        });

        const data = await response.json();

        if (data.status == 200) {
            SETTOKEN(data.token);
            user.value = GETSTUDENT().name;

            console.log(data);

            if(data.hasOwnProperty('attempt') && Object.keys(data.attempt).length !== 0) {
                console.log(data.attempt);
                let temp = JSON.parse(data.attempt);
                temp = temp.attempt;
                SETATTEMPT(temp);
                router.push('/quiz?id=' + temp.quizId);
            }
            console.log(data);
            console.log("Authentication Successful")
            auth.value = true;
        } else {
            throw new Error('Authentication Failed');
        }
    }
    catch (e) {
        auth.value = false;
        console.log(e);
        SETSTUDENT(null);
        SETTOKEN(null);
        SETATTEMPT(null);
        router.push('/login');
    }
}

const setPage = (p) => {
    currentPage.value = p;
}

const setOpenMenu = () => {
    openMenu.value = !openMenu.value;
}

const shiftWidth = () => {
    return {
        width: openMenu.value ? "65px" : "250px",
        minWidth: openMenu.value ? "65px" : "250px",
    }
}

</script>

<style scoped>
.left {
    height: 100%;
    overflow: hidden;
    transition: width 0.2s, min-width 0.2s;
}

.main {
    display: flex;
    height: 100svh;
}

.center {
    width: 100%;
    height: 100%;
    display: flex;
    background-color: rgb(220,220,220);
}
</style>