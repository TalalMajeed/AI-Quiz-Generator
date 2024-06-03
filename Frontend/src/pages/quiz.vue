<template>
    <div v-if="auth" class="test">
        <NavBar :button="false" :user="user" />
        <div>
        {{id}}
        </div>
    </div>

</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import router from '../router';
import { SETCURRENT,GETSTUDENT, SETSTUDENT, API, SETTOKEN, GETTOKEN } from '../main';

const route = useRoute();
const auth = ref(true);
const user = ref("");

const data = ref(null);
const name = ref("");
const password = ref("");
const review = ref("");

const id = ref(String(route.query.id).toUpperCase());

onMounted(() => {
    SETCURRENT('/quiz?id=' + route.query.id);
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
        console.log(data);

        if (data.status == 200) {
            SETTOKEN(data.token);
            user.value = GETSTUDENT().name;
            console.log("Authentication Successful")
            auth.value = true;
            getQuiz();
        } else {
            throw new Error('Authentication Failed');
        }
    }
    catch (e) {
        auth.value = false;
        console.log(e);
        SETSTUDENT(null);
        SETTOKEN(null);
        router.push('/login');
    }
}

async function getQuiz() {
    try {
        console.log("Requesting Quiz")
        const response = await fetch(API + '/get/quiz', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                id: id.value
            })
        });

        const data = await response.json();

        if (data.status == 200) {
            data.value = JSON.parse(data.quiz.data)["MCQs"];
            name.value = data.quiz.name;
            password.value = data.quiz.password;
            review.value = data.quiz.review;
            console.log(data.value)
        }
        else if(data.status == 400) {
            console.log("Quiz Not Found");
        }
        else {
            throw new Error('Quiz Not Found');
        }
    }
    catch (e) {
        console.log(e);
    }
}



watch(() => route.query.id, (newId) => {
    id.value = String(newId);
});
</script>

<style scoped>

.test {
    width: 100%;
    height: 100%;
    background-color: rgb(220,220,220);

}
</style>