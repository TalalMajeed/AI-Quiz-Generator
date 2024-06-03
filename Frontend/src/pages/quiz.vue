<template>
    <div v-if="auth" class="test">
        <NavBar :button="false" :user="user" />
        <div v-if="phase == 0">
            <div class="title-nav">
                <div class="title">Attempt Quiz</div>
                <v-btn class="button">Dashboard</v-btn>
            </div>

            <v-card class="card" v-if="found">
                <div class="card-heading">Quiz Details</div>
                <div class="stat-info"><b>Quiz Title: </b>{{ name }}</div>
                <div class="stat-info"><b>Created by: </b>{{ creator }}</div>
                <div class="stat-info"><b>Questions: </b>{{ total }} MCQS</div>
                <div class="stat-info"><b>Total Time: </b>{{ duration }} Minutes</div>
            </v-card>
            <v-card class="card" v-if="found">
                <div class="card-heading">User Details</div>
                <div class="stat-info"><b>Full Name: </b>{{ attemptUser.name }}</div>
                <div class="stat-info"><b>Student ID: </b>{{ attemptUser.id }}</div>
            </v-card>
            <v-card class="card" v-if="found">
                <div class="card-heading">Start Attempt</div>
                <v-responsive class="input" v-if="password.length != 0">
                    <v-text-field v-text-field v-model="passwordInput" label="Quiz Password" type="password" variant="outlined"></v-text-field>
                </v-responsive>
                <div class="button-holder">
                    <v-btn class="button2">Start</v-btn>
                    <v-btn class="button2">Back</v-btn>
                </div>
                <v-alert v-show="error" class="login-error" variant="tonal" color="error"
                    text="Incorrect Password!"></v-alert>

            </v-card>
            <v-card class="card" v-if="!found && !loading">
                <div class="card-heading">Quiz Details</div>
                <div class="stat-info"><b>ID Error</b>Quiz Not Found :/</div>
            </v-card>
            <br>
        </div>
        <div v-if="phase == 1">
            <v-card class="card" v-if="found">
                <div class="card-heading"><b>{{ name }}</b></div>
                <div class="stat-info"><b>Full Name: </b>{{ attemptUser.name }}</div>
                <div class="stat-info"><b>Attempted: </b>{{ attempted }} of {{total}}</div>
                <div class="stat-info"><b>Time Left: </b>0</div>
            </v-card>
            <v-card class="card" v-if="found">
                <div class="card-heading" :style="color"><b>Question {{ mindex + 1 }} of {{total}}</b></div>
                <div class="stat-info">{{mcq["Q"]}}</div>
            </v-card>
            <v-card class="card" v-if="found">
                <v-radio-group v-model="selection">
                <div class="radio-group" @click="selection = '0'">
                    <v-radio class="radio" value="0"></v-radio>
                    <div class="radio-div">{{ mcq["O"][0] }}</div>
                </div>
                <div class="radio-group" @click="selection = '1'">
                    <v-radio class="radio" value="1"></v-radio>
                    <div class="radio-div">{{ mcq["O"][1] }}</div>
                </div>
                <div class="radio-group" @click="selection = '2'">
                    <v-radio class="radio" value="2"></v-radio>
                    <div class="radio-div">{{ mcq["O"][2] }}</div>
                </div>
                <div class="radio-group" @click="selection = '3'">
                    <v-radio class="radio" value="3"></v-radio>
                    <div class="radio-div">{{ mcq["O"][3] }}</div>
                </div>
                </v-radio-group>
            </v-card>
            <v-card class="card" v-if="found">
                <div class="button-holder">
                    <v-btn class="button2" @click="previous">Previous</v-btn>
                    <v-btn class="button2" @click="next">Next</v-btn>
                    <v-btn class="button2" @click="save">Save</v-btn>
                    <v-btn class="button2">Submit</v-btn>
                </div>
            </v-card>
        </div>
        <div v-if="phase == 2">
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

const phase = ref(1);

const quizdata = ref([]);
const creator = ref("");
const total = ref(0);
const name = ref("");
const password = ref("");
const review = ref("");
const duration = ref(0);
const found = ref(false);
const loading = ref(false);
const passwordInput = ref("");
const id = ref(String(route.query.id).toUpperCase());
const error = ref(false);


//MCQ Sheet Resolver
const mindex = ref(0);
const mcq = ref({});
const answers = ref([]);
const selection = ref();
const attempted = ref(0);
const color = ref({ color: "red" });

const attemptUser = ref(null);

const next = () => {
    if(mindex.value < total.value - 1) {
        mindex.value += 1;
    }
}

const save = () => {
    if(selection.value == null) {
        return;
    }
    answers.value[mindex.value] = parseInt(selection.value);
    attempted.value = 0;

    answers.value.forEach((e)=>{
        if(e != -1) {
            attempted.value++;
        }
    });

    if (answers.value[mindex.value] == -1) {
        color.value = { color: "red" };
    } else {
        color.value = { color: "green" };
    }
};

const previous = () => {
    if(mindex.value > 0) {
        mindex.value -= 1;
    }
}

onMounted(() => {
    SETCURRENT('/quiz?id=' + route.query.id);
    if(!GETSTUDENT()) router.push('/login');
    if(!GETTOKEN()) router.push('/login');
    authenticate();
    setInterval(authenticate, 60 * 20 * 1000);

    attemptUser.value = GETSTUDENT();
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
        console.log(data)

        if (data.status == 200) {
            quizdata.value = JSON.parse(data.quiz.data)["MCQs"];
            name.value = data.quiz.name;
            password.value = data.quiz.password;
            review.value = data.quiz.review;
            duration.value = data.quiz.duration;
            creator.value = data.quiz.creator;
            total.value = quizdata.value.length;
            found.value = true;
            mindex.value = 0;
            mcq.value = quizdata.value[mindex.value]

            for(let i in quizdata.value) {
                answers.value.push(-1);
            }
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
    finally {
        loading.value = false;
    }
}

watch(() => route.query.id, (newId) => {
    id.value = String(newId);
});

watch(mindex, (newIndex) => {
    mcq.value = quizdata.value[newIndex];
    if(answers.value[newIndex] != -1) {
        console.log(''+newIndex);
        selection.value = answers.value[newIndex].toString();
    }
    else {
        selection.value = null;
    }


    if(answers.value[newIndex] == -1) {
        color.value = {
            color: "red"
        };
    } else {
        color.value = {
            color: "green"
        };
    }
});

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

.radio-group {
    display: flex;
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: start !important;
    cursor: pointer;
}

.radio-div {
    font-size: 18px;
    margin-left: 10px;

}

.radio {
    max-width: 50px;
    height: 50px;
    width: 20px !important;
}
</style>