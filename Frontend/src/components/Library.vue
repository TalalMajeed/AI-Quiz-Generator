<template>
        <div class="test">
            <div class="title">Quiz Library</div>
            <v-card class="card">
                <div class="heading">Search Library</div>
                <v-responsive   v-responsive class="input">
                    <v-text-field v-text-field v-model="searchInput" label="Search Quiz" variant="outlined"></v-text-field>
                </v-responsive>
            </v-card>
            <div class="quiz-line">
                <v-card class="quiz" v-for="i in filteredData">
            <div class="quiz-id">Quiz {{i.quiz.id}}</div>
            <div class="quiz-title">{{trim(i.quiz.name)}}</div>

            <div class="quiz-text"><div>Amount</div><div>{{JSON.parse(i.quiz.data).length}} Questions</div></div>
            <div class="quiz-author"><div>Time</div><div>{{i.quiz.duration}} Minutes</div></div>
            <div class="quiz-author"><div>Creator</div><div>{{ i.quiz.creator }}</div></div>
            <div class="spacer"></div>
            <v-btn class="quiz-button" @click="()=>{openQuiz(i.quiz.id)}">View</v-btn>
            </v-card>
            </div>

        </div>
</template>

<script setup>
import { ref, defineProps, watch, onMounted } from 'vue';
import { API, GETTOKEN, GETSTUDENT } from '../main';
import router from '../router';

const searchInput = ref("");
const quizData = ref([]);
const filteredData = ref([]);

const trim = str => str.trim().length > 50 ? str.trim().substring(0, 47) + '...' : str.trim();


const props = defineProps({
    page: Number,
});

watch(() => props.page, (newVal) => {
    requestQuizData();
});

watch(() => searchInput.value, (newVal) => {
    if(newVal == "") {
        filteredData.value = quizData.value;
    } else {
        filteredData.value = quizData.value.filter((i) => {
            return i.quiz.name.toLowerCase().includes(newVal.toLowerCase());
        });
    }
});


async function requestQuizData() {
    console.log("REQUESTING");
    try {
        const response = await fetch(API + '/get/quizzes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
            })
        });

        const data = await response.json();
        if(data.status == 200) {
            quizData.value = data.quizzes;
            filteredData.value = data.quizzes;
        }
    } catch (error) {
        console.log(error);
    }
}

onMounted(() => {
    requestQuizData();
});

function openQuiz(id) {
    router.push('/quiz?id=' + id);
}



</script>

<style scoped>

.test {
    width: 100svw;
    height: 100svh;
    background-color: rgb(220,220,220);
    overflow-y: scroll;
    padding-bottom: 50px;
}

.title-nav {
    display: flex;
    justify-content: space-between;
    margin: 20px;
    align-items: center;
}

.title {
    margin: 20px;
    font-size: 30px;
    font-weight: bold;
}

.card {
    margin: 20px;
    padding: 20px;
    background-color: white;
    border-radius: 0px;
    box-shadow: 0 0 10px rgba(0,0,0,0.1);
    display: flex;
    flex-direction: column;
    gap: 20px;
    font-size: 18px;
    width: calc(100% - 100px);
}

.card-heading {
    font-size: 20px;
    font-weight: 400;
    text-transform: uppercase;
    margin-bottom: 10px;
}

.button {
    color: white;
    background-color: var(--primary);
    height: 50px;
    width: 160px;
}

.button2 {
    color: white;
    background-color: var(--primary);
    height: 50px;
    width: 160px;
}

.input {
    padding-top: 5px;
    height: 70px;
}

.button-holder {
    display: flex;
    gap: 20px;
}


.stat-info b {
    display:inline-block;
    width: 120px;
}

.login-error {
    margin-top: 5px;
}

.quiz {
    width: 350px;
    height: 400px;
    padding: 20px 30px;
    display: flex;
    flex-direction: column;
}

.quiz-line {
    display: flex;
    gap: 20px;
    border-radius: 0px;
    font-size: 18px;
    align-items: center;
    margin: 20px;
    width: calc(100% - 100px);
    flex-wrap: wrap;
}

.quiz-title {
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    margin-top: 20px;
}

.quiz-id {
    text-align: center;
    margin-top: 15px;
    font-size: 18px;
    text-transform: uppercase;
    letter-spacing: 2px;
}

.quiz-text {
    margin-top: 60px;
    text-align: left;
    font-size: 18px;
    text-align: center;
    width: 100%;
    display: flex;
    padding: 0 5px;
    justify-content: space-between;
}

.quiz-author {
    text-align: left;
    margin-top: 10px;
    font-size: 18px;
    text-align: center;
    width: 100%;
    display: flex;
    padding: 0 5px;
    justify-content: space-between;
}

.quiz-button {
    color: white;
    background-color: var(--primary);
    height: 50px;
    width: 100%;
}

.spacer {
    flex-grow: 1;
}


</style>