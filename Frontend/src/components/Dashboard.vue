<template>
    <div class="container">
        <div class="title">Dashboard</div>
        <div class="dashboard-divider">
            <v-card class="card">
            <div class="heading">Student Information</div>
            <div class="divider">
                <div class="profilecontainer">
                    <div class="usertitle">{{ user["name"] }}</div>
                    <div class="userid"><b>Email:</b> {{ user["email"] }}</div>
                    <div class="userid"><b>Student ID:</b> {{ user["id"] }}</div>
                    <div class="userid">{{ user["description"] }}</div>
                </div>
                <div class="datacontainer">
                    <div class="profile">
                        <v-icon v-if="image == null">mdi-account</v-icon>
                        <img img class="imghandler" v-if="image != null" v-bind:src="image" cover></img>
                    </div>
                    <v-btn class="button" @click="signout">Sign Out</v-btn>
                </div>

            </div>
        </v-card>
        <v-card class="card">
            <div class="heading">Student Analytics</div>
            <div class="divider">
                <div class="profilecontainer">
                    <div class="usertitle">Study Details</div>
                    <div class="userid"><b>Total Quizes:</b> </div>
                    <div class="userid"><b>Total Solved:</b> </div>
                    <div class="userid"><b>Average Grade:</b> </div>
                </div>
                <div class="graphcontainer">
                    <Bar id="my-chart-id" :options="chartOptions" :data="chartData"  width="300" height="200" />
                </div>    
            </div>
        </v-card>
    </div>
    </div>

</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { GETTOKEN, API, SETTOKEN, SETSTUDENT, GETSTUDENT } from '@/main';
import router from '../router';

import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'
ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const chartData = {
  labels: ['Total Solved', 'Total Quizes','Created by Me'],
  datasets: [{
    data: [10,30,5],
    backgroundColor: [
      'rgba(155, 155, 155, 0.2)',
      'rgba(232, 224, 255)',
      'rgba(155, 155, 155, 0.2)'
    ],
    borderColor: [
      'rgba(155, 155, 155, 1)',
      'rgba(150, 121, 237, 1)',
      'rgba(155, 155, 155, 1)'
    ],
    borderWidth: 1
  }]
};
const chartOptions = {
  responsive: true,
  plugins: {
    legend: {
      position: ''
    }
  }
}


const user = ref({name: "", email: "", id: "", description: "", image: null});

const loading = ref(false);

const required = (v) => !!v || '';

const props = defineProps({
    page: Number
});

const signout = async () => {
    SETSTUDENT(null);
    SETTOKEN(null);
    router.push('/login');
}

watch(() => props.page, (value) => {
    if (!GETTOKEN()) router.push("/login");
    if (!GETSTUDENT()) router.push("/login");
    user.value = GETSTUDENT();
    if(!GETSTUDENT().hasOwnProperty("description")) user.value["description"] = "";
    if(!GETSTUDENT().hasOwnProperty("image")) user.value["image"] = null;
    image.value = user.value["image"];

    console.log(user.value);
});

const imgopen = ref(false);
const image = ref(null);
onMounted(() => {
    if (!GETTOKEN()) router.push("/login");
    if (!GETSTUDENT()) router.push("/login");
    user.value = GETSTUDENT();
    if(!GETSTUDENT().hasOwnProperty("description")) user.value["description"] = "";
    if(!GETSTUDENT().hasOwnProperty("image")) user.value["image"] = null;
    image.value = user.value["image"];

    console.log(user.value);
});

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
}

.heading {
    font-size: 20px;
    font-weight: 400;
    text-transform: uppercase;
    margin-bottom: 10px;
}


.usertitle {
    font-size: 1.8rem;
    font-weight: bold;
    color: var(--secondary);
    margin-bottom: 10px;
}

.userid {
    font-size: 1.1rem;
    font-weight: 400;
    color: var(--secondary);
    margin-bottom: 10px;
}

.divider {
    display: flex;
    width: 100%;
    gap: 40px;
    justify-content: space-between;
}

.button {
    color: white;
    font-weight: 300;
    width: 160px;
    height: 45px !important;
    background-color: var(--primary);
}

.profile {
    background-color: var(--primary);
    color: white;
    max-width: 120px;
    max-height: 120px;
    min-width: 120px;
    min-height: 120px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 50px;
    overflow: hidden;
}

.imghandler {
    width: 100%;
    height: 100%;

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

.datacontainer {
    display: flex;
    flex-direction: column;
    gap: 20px;
    align-items: center;
}

.profilecontainer {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.graphcontainer {
    width: 300px;
    height: 200px;
}

</style>