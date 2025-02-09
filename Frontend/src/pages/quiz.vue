<template>
    <div v-if="auth" class="test">
        <NavBar :button="false" :user="user" />
        <div v-if="phase == 0">
            <div class="title-nav">
                <div class="title">Attempt Quiz</div>
                <v-btn class="button" @click="toDashboard">Dashboard</v-btn>
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
                <v-responsive class="input" v-if="password">
                    <v-text-field v-text-field v-model="passwordInput" label="Quiz Password" type="password" variant="outlined"></v-text-field>
                </v-responsive>
                <div class="button-holder">
                    <v-btn class="button2" @click="startAttempt">Start</v-btn>
                    <v-btn class="button2" @click="toDashboard">Back</v-btn>
                </div>
                <v-alert v-show="error" class="login-error" variant="tonal" color="error"
                    text="Incorrect Password!"></v-alert>
            </v-card>
            <v-card class="card" v-if="found">
                <div class="card-heading">USER RESULTS</div>
                <div>
                <div class="result-list">
                    <div class="r-index"><b>#</b></div>
                    <div class="r-date"><b>Date</b></div>
                    <div class="r-score"><b>My Score</b></div>
                    <div class="r-total"><b>Total Score</b></div>
                </div>
                <div class="result-list" v-for="(i, index) in resultData">
                    <div class="r-index">{{ index + 1 }}</div>
                    <div class="r-date">{{ i.date }}</div>
                    <div class="r-score">{{ i.score }}</div>
                    <div class="r-total">{{i.total}}</div>
                </div>
                </div>

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
                <div class="stat-info"><b>Time Left: </b>{{  currentTime }}</div>
            </v-card>
            <v-card class="card" v-if="found">
                <div class="card-heading" :style="color"><b>Question {{ mindex + 1 }} of {{total}}</b></div>
                <div class="stat-info" v-for="t in (mcq.Q || '').replace(/ /g, '&nbsp;').split('\n')">
                {{ t }}
            </div>
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
                    <v-btn class="button2" @click="save" :loading="saveLoading">Save</v-btn>
                    <v-btn class="button2" @click="previous">Previous</v-btn>
                    <v-btn class="button2" @click="next">Next</v-btn>
                    <v-btn class="button2" @click="showWarning = true">Submit</v-btn>
                </div>
            </v-card>
        </div>
        <div v-if="phase == 2">
            <v-card class="card" v-if="found">
                <div class="card-heading">Submission Details</div>
                <div>Your Quiz has been submitted successfully.</div>
                <div class="stat-info"><b>Quiz Title: </b>{{ name }}</div>
                <div class="stat-info"><b>Total Time: </b>{{ duration }} Minutes</div>
            </v-card>
            <v-card class="card" v-if="found">
                <div>You can now view your Result in the Dashboard</div>
                <div class="button-holder">
                    <v-btn class="button2" @click="()=>{phase = 0, requestResult();}">Result</v-btn>
                    <v-btn class="button2" @click="toDashboard">Dashboard</v-btn>
                </div>
            </v-card>
        </div>
        <v-dialog v-model="showWarning" max-width="400" persistent>
            <v-card prepend-icon="mdi-update" text="Your Answers will be Submitted and you will not be able to change them. Are you sure you want to submit?"
                title="Submit Quiz?">
                <template v-slot:actions>
                    <v-spacer></v-spacer>

                    <v-btn @click="showWarning = false">
                        Go Back
                    </v-btn>

                    <v-btn :loading="submitLoad" @click="sendComplete">
                        Save
                    </v-btn>
                </template>
            </v-card>
        </v-dialog>

    </div>

</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import router from '../router';
import { SETCURRENT,GETSTUDENT, SETSTUDENT, API, SETTOKEN, GETTOKEN, ATTEMPT, SETATTEMPT } from '../main';

const route = useRoute();
const auth = ref(true);
const user = ref("");

const phase = ref(0);

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
const startTime = ref(0);
const saveLoading = ref(false);

const submitLoad = ref(false);
const showWarning = ref(false);



const mindex = ref(0);
const mcq = ref({});
const answers = ref([]);
const selection = ref();
const attempted = ref(0);
const color = ref({ color: "red" });

const attemptUser = ref(null);
const currentTime = ref(0);
const attemptid = ref(null);

const resultData = ref([]);

const requestResult = async() => {
    try {
        const req = await fetch(API + '/get/user/attempt', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                studentid: GETSTUDENT().id,
                quizid: id.value
            })
        });

        const res = await req.json();
        console.log(res);
        let data =  [];
        if(res.status == 200) {
            for(let i in res.attempts) {
                let sum = 0;
                for(let j in JSON.parse(res.attempts[i].attempt.data)) {
                    for(let k in quizdata.value) {
                        if(quizdata.value[k].A == JSON.parse(res.attempts[i].attempt.data)[j] && k == j) {
                            sum += 1;
                        }
                    }
                }

                data.push({
                    date: new Date(res.attempts[i].attempt.end).toLocaleString(),
                    score: sum,
                    total: quizdata.value.length
                });
            }

            resultData.value = data;
        }
    }
    catch(e) {
        console.log(e);
    }
}

const toDashboard = () => {
    router.push('/panel');
}
const next = () => {
    if(mindex.value < total.value - 1) {
        mindex.value += 1;
    }
}

const sendComplete = async() => {
    submitLoad.value = true;
    try {
        const req = await fetch(API + '/set/completed', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                id: attemptid.value,
            })
        });

        const res = await req.json();
        console.log(res);
        if(res.status == 200) {
            showWarning.value = false;
            phase.value = 2;
        }
    }
    catch(e) {
        console.log(e);
    }
    finally {
        submitLoad.value = false;
    }
}

const startAttempt = async() => {
    let x = new Date().getTime();
    if(password.value == passwordInput.value || password.value == null) {
        try {
            const req = await fetch(API + '/create/attempt', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                quizid: id.value,
                studentid: GETSTUDENT().id,
                page: 0,
                data: JSON.stringify(answers.value),
                end: x
            })
        });

        const res = await req.json();
        console.log(res);
        if(res.status == 200) {
            phase.value = 1;
            startTime.value = x;
            attemptid.value = res.attempt.id;
            shiftTime();
        }
        }
        catch(e) {
            console.log(e);
        }
    }
    else {
        error.value = true;
    }
}

const updateAttempt = async() => {

    saveLoading.value = true;

    try{
        const req = await fetch(API + '/update/attempt', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + GETTOKEN(),
        },
        body: JSON.stringify({
            id: attemptid.value,
            page: mindex.value,
            data: JSON.stringify(answers.value),
        })
    });

    const res = await req.json();
    console.log(res);
    if(res.status == 200) {
        console.log("Attempt Updated");
        answers.value = JSON.parse(res.attempt.data);
    }
    }
    catch(e) {
        console.log(e);
    }
    finally {
        saveLoading.value = false;
    }

}

const save = async() => {
    if(selection.value == null) {
        return;
    }

    console.log("Saving");

    answers.value[mindex.value] = parseInt(selection.value);
    await updateAttempt();
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

const shiftTime = () => {
    let x = new Date().getTime();
    let diff = x - startTime.value;
    let time = duration.value * 60 * 1000 - diff;
    let minutes = Math.floor((time % (1000 * 60 * 60)) / (1000 * 60));
    let seconds = Math.floor((time % (1000 * 60)) / 1000);

    if(minutes < 0 || seconds < 0) {
        minutes = 0;
        seconds = 0;
    }
    currentTime.value = minutes + "m " + seconds + "s";

    if(minutes < 0 && seconds < 0 && phase.value == 1) {
        sendComplete();
    }

    setTimeout(shiftTime, 1000);
}

onMounted(() => {
    SETCURRENT('/quiz?id=' + route.query.id);
    if(!GETSTUDENT()) router.push('/login');
    if(!GETTOKEN()) router.push('/login');
    authenticate();
    setInterval(authenticate, 60 * 20 * 1000);
    attemptUser.value = GETSTUDENT();

    if(ATTEMPT != null) {
    attemptid.value = ATTEMPT.id;
    answers.value = JSON.parse(ATTEMPT.data);
    mindex.value = ATTEMPT.page;
    phase.value = 1;
    startTime.value = ATTEMPT.end;
    attempted.value = 0;
    answers.value.forEach((e)=>{
        if(e != -1) {
            attempted.value++;
        }
    });
    shiftTime();
    if(answers.value[mindex.value] == -1) {
        color.value = {
            color: "red"
        };
    } else {
        color.value = {
            color: "green"
        };
    }
    
    }
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
            console.log("ATTEMPT:q23e");
            console.log(ATTEMPT);
            if(data.hasOwnProperty('attempt') && Object.keys(data.attempt).length !== 0 && ATTEMPT == null) {
                router.push('/panel');
            }
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
        SETATTEMPT(null);
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
            quizdata.value = JSON.parse(data.quiz.data);
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
            requestResult();
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

.result-list {
    display: flex;
    flex-direction: row;
    padding: 20px;
    background-color: rgb(236, 236, 236);
    text-align: center;
}

.r-index {
    width: 40px;
}

.r-date {
    width: 350px;
}

.r-score {
    width: 150px;
}

.r-total {
    width: 150px;
}

.result-list:nth-child(odd) {
    background-color: #e5dbfd;
}
</style>