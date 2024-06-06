<template>
    <div class="container">
        <div v-if="phase==0">
            <div class="title">Quiz Creator</div>
            <v-card class="card">
            <v-form class="login-form" @submit.prevent>
            <div class="divider">
                <div class="heading">Create Quiz</div>
                <v-responsive class="checkbox">
                    <v-checkbox v-model="passwordToggle" class="checkbox-button" label="Require Password"></v-checkbox>
                </v-responsive>
                <v-responsive class="checkbox">
                    <v-checkbox v-model="reviewToggle" class="checkbox-button" label="Allow Review"></v-checkbox>
                </v-responsive>
            </div>
            <div class="divider3">
                <v-responsive v-responsive class="input input2">
                    <v-text-field :rules="[required]" v-text-field v-model="nameInput" label="Quiz Name" variant="outlined"></v-text-field>
                </v-responsive>
                <v-responsive v-responsive class="input input2">
                    <v-text-field :rules="[required]" v-text-field type="number" v-model="timeInput" label="Time Limit" variant="outlined"></v-text-field>
                </v-responsive>
            </div>

            <v-responsive class="input" v-if="passwordToggle">
                <v-text-field :rules="[required]" v-text-field v-model="passwordInput" label="Quiz Password" variant="outlined"></v-text-field>
            </v-responsive>

            <div class="button-holder">
                <v-btn type="submit" @click="creatorShift" class="button2">Create</v-btn>
                <v-btn class="button2">Library</v-btn>
            </div>
            </v-form>
            </v-card>
            <v-card class="card">
                <div class="heading">My Quizes</div>
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
            <div class="button-holder">
            <v-btn class="quiz-button2" @click="()=>{openQuiz(i.quiz)}">View</v-btn>
            <v-btn class="quiz-button2" @click="()=>{deleteQuiz(i.quiz.id)}">Delete</v-btn>
            </div>
            </v-card>
            </div>
        </div>
        <div v-if="phase==1">
        <div class="divider2">
            <div class="title">Quiz Creator</div>
            <div class="divider2-buttons">
                <v-btn class="button2" @click="phase = 0">Back</v-btn>
                <v-btn class="button2" @click="addMCQDialog">Add MCQ</v-btn>
                <v-btn class="button2" @click="generateDialog = true">QuizAI</v-btn>
                <v-btn class="button2" :loading="saveLoading" @click="saveQuiz">Save</v-btn>
            </div>
        </div>

        <v-card class="card">
                <div class="heading">Quiz Info</div>
                <div class="stat-info"><b>Quiz Title: </b>{{ nameInput }}</div>
                <div class="stat-info"><b>Password: </b>{{ passwordToggle ? "Yes" : "No" }}</div>
                <div class="stat-info"><b>Review: </b>{{ reviewToggle ? "Allowed" : "Not Allowed" }}</div>
                <div class="stat-info"><b>Total Time: </b>{{ timeInput }} Minutes</div>
        </v-card>
        <v-card class="card" v-for="(item, index) in quizdata">
        <div class="divider4">
            <div class="heading">Question {{ index + 1 }}</div>
            <div class="mcq-button-holder">
                <v-btn class="button3" icon="mdi-pen" @click="updateQuestion(index)"></v-btn>
                <v-btn class="button3" icon="mdi-delete" @click="deleteQuestion(index)"></v-btn>
            </div>
            
        </div>
            <div v-for="t in (item.Q || '').replace(/ /g, '&nbsp;').split('\n')">
                <b>{{ t }}</b>
            </div>
            <div :style="{ backgroundColor: item.A != 0 ? 'white' : '#ceffc9' }" class="stat-info2">A. {{ item.O[0] }}</div>
            <div :style="{ backgroundColor: item.A != 1 ? 'white' : '#ceffc9' }" class="stat-info2">B. {{ item.O[1] }}</div>
            <div :style="{ backgroundColor: item.A != 2 ? 'white' : '#ceffc9' }" class="stat-info2">C. {{ item.O[2] }}</div>
            <div :style="{ backgroundColor: item.A != 3 ? 'white' : '#ceffc9' }" class="stat-info2">D. {{ item.O[3] }}</div>
        </v-card>
        </div>
        <v-dialog max-width="500" v-model="addDialog">
        <v-card title="Add MCQ">
            <v-responsive class="text-resp">
                <v-text-field v-model="addQuestion" class="text" label="Question" variant="outlined"></v-text-field>
                <v-text-field v-model="addOptionA" class="text" label="Option A" variant="outlined"></v-text-field>
                <v-text-field v-model="addOptionB" class="text" label="Option B" variant="outlined"></v-text-field>
                <v-text-field v-model="addOptionC" class="text" label="Option C" variant="outlined"></v-text-field>
                <v-text-field v-model="addOptionD" class="text" label="Option D" variant="outlined"></v-text-field>
                <v-select :items="['A','B','C','D']" v-model="addAnswer" class="text" label="Answer" variant="outlined"></v-select>
            </v-responsive>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn text="Back" @click="addDialog = false"></v-btn>
                <v-btn text="Save" @click="addMCQ"></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
    <v-dialog max-width="500" v-model="generateDialog">
        <v-card title="AI Generation">
            <v-responsive class="text-resp">
                <v-text-field v-model="aisubject" class="text" label="Subject / Field" variant="outlined"></v-text-field>
                <v-text-field v-model="aiAmount" class="text" type="Number" label="Amount" variant="outlined"></v-text-field>
                <v-text-field v-model="aidifficulty" class="text" type="Number" label="Difficulty Age" variant="outlined"></v-text-field>
                <v-textarea rows="2" v-model="aidetails" class="text" label="Generation Details" variant="outlined"></v-textarea>
                <v-alert v-show="error != ''" class="login-error" variant="tonal" color="error"
                    :text="error"></v-alert>

            </v-responsive>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn text="Back" @click="generateDialog = false"></v-btn>
                <v-btn :loading="ailoading" text="Generate" @click="generateMCQs"></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
    </div>

</template>

<script setup>
import { ref, onMounted, watch, defineProps } from 'vue';
import { GETTOKEN, API, SETTOKEN, SETSTUDENT, GETSTUDENT } from '@/main';
import router from '../router';

const user = ref({name: "", email: "", id: "", description: "", image: null});
const loading = ref(false);

const props = defineProps({
    page: Number
});

watch(() => props.page, (newVal) => {
    recieveUserQuizzes();
});

const trim = str => str.trim().length > 50 ? str.trim().substring(0, 47) + '...' : str.trim();



const nameInput = ref("");
const passwordInput = ref("");
const searchInput = ref("");
const passwordToggle = ref(false);
const phase = ref(0);
const timeInput = ref(20);
const reviewToggle = ref(false);

const addDialog = ref(false);
const addQuestion = ref("");
const addOptionA = ref("");
const addOptionB = ref("");
const addOptionC = ref("");
const addOptionD = ref("");
const addAnswer = ref("A");
const isUpdate = ref(-1);

const aiAmount = ref(null);
const generateDialog = ref(false);
const aidifficulty = ref(null);
const aisubject = ref(null);
const aidetails = ref(null);
const error = ref("");
const ailoading = ref(false);
const saveLoading = ref(false);

const updatingID = ref(null);

const addMCQDialog = () => {
    addDialog.value = true;
    addQuestion.value = "";
    addOptionA.value = "";
    addOptionB.value = "";
    addOptionC.value = "";
    addOptionD.value = "";
    addAnswer.value = "A";
    isUpdate.value = -1;
}

const quizData2 = ref([]);
const filteredData = ref([]);

function openQuiz(item) {
    phase.value = 1;
    nameInput.value = item.name;
    passwordToggle.value = item.password ? true : false;
    passwordInput.value = item.password;
    reviewToggle.value = item.review;
    timeInput.value = item.duration;
    quizdata.value = JSON.parse(item.data);
    updatingID.value = item.id;

}

watch(() => searchInput.value, (newVal) => {
    if(newVal == "") {
        filteredData.value = quizData2.value;
    } else {
        filteredData.value = quizData2.value.filter((i) => {
            return i.quiz.name.toLowerCase().includes(newVal.toLowerCase());
        });
    }
});

const deleteQuiz = async (id) => {
    try {
        const response = await fetch(API + '/delete/quiz', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                id: id
            })
        });

        const data = await response.json();
        if(data.status == 200) {
            console.log("Quiz Deleted");
            recieveUserQuizzes();
        }
    } catch (error) {
        console.log(error);
    }
}

const recieveUserQuizzes = async () => {
    try {
        const response = await fetch(API + '/user/quizzes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                id: GETSTUDENT().id
            })
        });

        const data = await response.json();
        if(data.status == 200) {
            quizData2.value = data.quizzes;
            filteredData.value = data.quizzes;
        }
    } catch (error) {
        console.log(error);
    }
}


const generateMCQs = async () => {
    if(!aisubject.value || !aiAmount.value || !aidifficulty.value || !aidetails.value) {
        error.value = "All Fields Required";
        return;
    }

    if(aiAmount.value < 1 || aiAmount.value > 20) {
        error.value = "Amount must be between 1 and 20";
        return;
    }
    if(aidifficulty.value < 4 || aidifficulty.value > 24) {
        error.value = "Difficulty must be between 4 and 24";
        return;
    }
    ailoading.value = true;
    try {
        const response = await fetch(API + '/generate/quiz', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + GETTOKEN(),
            },
            body: JSON.stringify({
                subject: aisubject.value,
                amount: aiAmount.value,
                difficulty: aidifficulty.value,
                details: aidetails.value
            })
        });

        const data = await response.json();

        if (data.status == 200) {
            console.log("Generation Successful");
            console.log(data.MCQs);
            let x = JSON.parse(data.MCQs.toString());
            for(let i = 0; i < x.length; i++) {
                x[i].I = quizdata.value.length + 1;
                quizdata.value.push(x[i]);
            }
            generateDialog.value = false;
        } else {
            throw new Error('Generation Failed');
        }
    }
    catch (e) {
        error.value = "Generation Failed";
        console.log(e);
    }
    finally {
        ailoading.value = false;
        aiAmount.value = null;
        aidifficulty.value = null;
        aisubject.value = null;
        aidetails.value = null;
        error.value = "";
    }
}

const deleteQuestion = (index) => {
    quizdata.value.splice(index, 1);
    for(let i = 0; i < quizdata.value.length; i++) {
        quizdata.value[i].I = i + 1;
    }
}

const updateQuestion = (index) => {
    addQuestion.value = quizdata.value[index].Q;
    addOptionA.value = quizdata.value[index].O[0];
    addOptionB.value = quizdata.value[index].O[1];
    addOptionC.value = quizdata.value[index].O[2];
    addOptionD.value = quizdata.value[index].O[3];
    addAnswer.value = ["A","B","C","D"][quizdata.value[index].A];
    addDialog.value = true;
    isUpdate.value = index;
}

const saveQuiz = async() => {
    if(!nameInput.value || !timeInput.value || !quizdata.value || (quizdata.value == [])) return;
    
    saveLoading.value = true;
    let data = {
        "name": nameInput.value,
        "creator": GETSTUDENT().id,
        "duration": timeInput.value,
        "password": passwordToggle.value ? passwordInput.value : null,
        "review": reviewToggle.value ? true : false,
        "data": JSON.stringify(quizdata.value),
        "id": updatingID.value
    }

    console.log(data);
    try {
        const response = await fetch(API + '/save/quiz', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                "Authorization": "Bearer " + GETTOKEN(),
            },
            body: JSON.stringify(data)
        });

        const res = await response.json();
        console.log(res)

        if(res.status == 200) {
            console.log("Quiz Saved");
        }
        else {
            throw new Error("Quiz Save Failed");
        }
    }
    catch(e) {
        console.log(e);
    }
    finally {
        saveLoading.value = false;
        recieveUserQuizzes();
        updatingID.value = null;
        phase.value = 0;
    }
}

const addMCQ = () => {

    if(!addQuestion.value || !addOptionA.value || !addOptionB.value || !addOptionC.value || !addOptionD.value) return;
    let answer = 0;
    if (addAnswer.value == "B") answer = 1;
    if (addAnswer.value == "C") answer = 2;
    if (addAnswer.value == "D") answer = 3;

    if(isUpdate.value != -1) {
        quizdata.value[isUpdate.value] = {
            "I": isUpdate.value + 1,
            "Q": addQuestion.value,
            "O": [addOptionA.value, addOptionB.value, addOptionC.value, addOptionD.value],
            "A": answer
        };
    }
    else {
        quizdata.value.push({
        "I": quizdata.value.length + 1,
        "Q": addQuestion.value,
        "O": [addOptionA.value, addOptionB.value, addOptionC.value, addOptionD.value],
        "A": answer
    });
    }

    addDialog.value = false;
    addQuestion.value = "";
    addOptionA.value = "";
    addOptionB.value = "";
    addOptionC.value = "";
    addOptionD.value = "";
    addAnswer.value = "A";
    isUpdate.value = -1;
}

const quizdata = ref([
]);

const required = (v) => !!v || '';

const creatorShift = () => {
    if(!nameInput.value) return;
    if(passwordToggle.value && !passwordInput.value) return;
    addQuestion.value = "";
    addOptionA.value = "";
    addOptionB.value = "";
    addOptionC.value = "";
    addOptionD.value = "";
    addAnswer.value = "A";
    isUpdate.value = -1;
    phase.value = 1;
    quizdata.value = [];
    updatingID.value = null;
}

</script>

<style scoped>
.container {

    width: 100%;
    overflow-y: scroll;
    padding: 20px;
    padding-bottom: 120px;
}

.title {
    font-size: 30px;
    font-weight: bold;
}

.card {
    padding: 20px;
    width: 100%;
    margin-top: 20px;
    font-size: 18px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.heading {
    font-size: 20px;
    font-weight: 400;
    text-transform: uppercase;
    margin-bottom: 10px;
}


.button {
    color: white;
    font-weight: 300;
    width: 160px;
    height: 45px !important;
    background-color: var(--primary);
}

.dashboard-divider {
    display: flex;
    flex-direction: row;
    gap: 20px;
}

.divider {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}

.divider3 {
    display: flex;
    flex-direction: row;
    gap: 10px;
    width: 100%;
}

.input {
    padding-top: 5px;
    height: 70px;
}

.input2 {
    width: calc(50% - 5px);
}

.divider {
    display: flex;
    flex-direction: row;
    gap: 20px;
    justify-content: space-between;
}

.divider4 {
    display: flex;
    flex-direction: row;
    gap: 20px;
    justify-content: space-between;
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

.button-holder {
    margin-top: 10px;
    display: flex;
    gap: 20px;
}

.divider {
    display: flex;
    flex-direction: row;
    justify-content: start;
    align-items: center;
    gap: 30px;
}

.checkbox {
    flex-grow: 0;
}

.checkbox-button {
    width: 180px;
    height: 65px;
}

.divider2 {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}

.divider2-buttons {
    display: flex;
    flex-direction: row;
    gap: 20px;
}

.stat-info b {
    display:inline-block;
    width: 120px;
}

.button3 {
    color: white;
    background-color: var(--primary);
    height: 50px;
    width: 50px;
}

.stat-info2 {
    padding: 10px;
    border-radius: 5px;
}

.text {
    padding-top: 5px;
    margin: 0 20px;
}

.mcq-button-holder {
    display: flex;
    gap: 15px;
}

.login-error {
    margin: 0 20px 20px 20px;
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
    margin: 20px 0;
    width: 100%;
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

.button-holder {
    display: flex;
    gap: 20px;
    width: 100%;
}

.quiz-button2 {
    color: white;
    background-color: var(--primary);
    height: 50px;
    width: calc(50% - 10px);
}

</style>