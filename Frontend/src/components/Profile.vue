<template>
    <div class="container">
        <div class="title">Profile</div>
        <v-card class="card">
            <div class="heading">Information</div>
            <div class="divider">
                <div class="profilecontainer">
                    <div class="usertitle">{{ user["name"] }}</div>
                    <div class="userid"><b>Email:</b> {{ user["email"] }}</div>
                    <div class="userid"><b>Student ID:</b> {{ user["id"] }}</div>
                </div>
            </div>
        </v-card>
        <br>
        <v-card class="card">
            <v-form class="form" @submit.prevent>
                <div class="heading">Modify Information</div>
                <br>
                <div class="divider">
                    <div class="divider-left">
                        <div class="info-container">
                            <div class="label">Full Name: </div>
                            <v-responsive class="input-bar">
                                <v-text-field class="input-handler" :rules="[required]" v-model="nameInput"
                                    :disabled="enable == false" label="Full Name" variant="outlined"></v-text-field>
                            </v-responsive>
                        </div>
                        <div class="info-container">
                            <div class="label">Education: </div>
                            <v-responsive class="input-bar">
                                <v-text-field class="input-handler" :rules="[required]" v-model="educationInput"
                                    :disabled="enable == false" label="Education" variant="outlined"></v-text-field>
                            </v-responsive>
                        </div>
                        <div class="info-container">
                            <div class="label">Gender: </div>
                            <v-responsive class="input-bar">
                                <v-autocomplete v-model="genderInput" class="input-handler" label="Gender"
                                    :disabled="enable == false" :rules="[required]" :items="['Male', 'Female']"
                                    variant="outlined"></v-autocomplete>
                            </v-responsive>
                        </div>
                        <div class="text-container">
                            <div class="label2">Description: </div>
                            <v-responsive class="text-bar">
                                <v-textarea rows="3" no-resize class="input-handler" :disabled="enable == false"
                                    v-model="descriptionInput" label="About Yourself" variant="outlined"></v-textarea>
                            </v-responsive>
                        </div>
                    </div>
                    <div class="divider-right">
                        <input type="file" id="file" style="display: none" />
                        <div class="profile" @mouseover="imgopen = true" @mouseleave="imgopen = false"
                            @click="uploadImg">
                            <div class="cover" v-show="imgopen"><v-icon v-if="image== null">mdi-camera</v-icon><v-icon v-if="image != null">mdi-delete</v-icon></div>
                            <v-icon v-if="image == null">mdi-account</v-icon>
                            <img class="imghandler" v-if="image != null" v-bind:src="image" cover></img>
                        </div>
                        <div class="divider-right-sub">
                            <v-btn class="button" @click="setEnable">Enable Edit</v-btn>
                            <v-btn class="button" :loading="loading" @click="submit">Save Changes</v-btn>
                        </div>
                    </div>

                </div>
                <br>
            </v-form>
        </v-card>
        <br>
        <v-card class="card">
            <div class="heading">Reset & Delete</div>

            <div class="divider2" style="margin-top:12px;">
                <div>Click the Button to Delete your Account!</div>
                <v-btn class="button" @click="deleteAccount">Delete Account</v-btn>
            </div>
        </v-card>

        <v-dialog v-model="showWarning" max-width="400" persistent>
            <v-card prepend-icon="mdi-alert" text="Your Account will be deleted permanently! It cannot be recovered"
                title="Delete Account?">
                <template v-slot:actions>
                    <v-spacer></v-spacer>

                    <v-btn @click="showWarning = false">
                        Go Back
                    </v-btn>

                    <v-btn :loading="deleteLoad" @click="sendDelete">
                        Delete
                    </v-btn>
                </template>
            </v-card>
        </v-dialog>
    </div>

</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { GETTOKEN, API, SETTOKEN, SETSTUDENT, GETSTUDENT } from '@/main';
import router from '../router';

const user = ref({name: "", email: "", id: ""});

const props = defineProps({
    page: Number,
});

const loading = ref(false);

const required = (v) => !!v || '';

const imgopen = ref(false);
const image = ref(null);

const nameInput = ref(null);
const educationInput = ref(null);
const genderInput = ref(null);
const descriptionInput = ref(null);
const enable = ref(false);
const showWarning = ref(false);
const deleteLoad = ref(false);

const setEnable = () => {
    enable.value = true;
}

watch(() => props.page, (newVal) => {
    if (newVal == 1) {
        enable.value = false;
    }
});

const submit = async () => {
    if (nameInput.value == "" || educationInput.value == "" || genderInput.value == "") {
        return;
    }
    if (enable.value == false && (image.value == user.value["image"])) {
        console.log("This is the Same");
        return;
    }
    loading.value = true;
    try {
        console.log(image);
        const response = await fetch(`${API}/update/user`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${GETTOKEN()}`
            },
            body: JSON.stringify({
                "id": GETSTUDENT().id,
                "name": nameInput.value,
                "education": educationInput.value,
                "community": genderInput.value,
                "description": descriptionInput.value,
                "image": image.value
            })
        });

        const data = await response.json();
        console.log(data);
        if (data["status"] == 200) {
            enable.value = false;
            SETSTUDENT({
                "id": GETSTUDENT().id,
                "name": nameInput.value,
                "email": GETSTUDENT().email,
                "education": educationInput.value,
                "community": genderInput.value,
                "description": descriptionInput.value,
                "image": image.value
            });
            console.log(GETSTUDENT());
            user.value = GETSTUDENT();
        }
        loading.value = false;
    }
    catch (e) {
        console.log(e);
        loading.value = false;
    }
}

const resetPassword = () => {
    loading.value = true;
    try {
        setRESET(true);
        router.push("/forgot");
    }
    catch (e) {
        console.log(e);
    }
    finally {
        loading.value = false;
    }
}

onMounted(() => {

    if (!GETTOKEN()) router.push("/login");
    if (!GETSTUDENT()) router.push("/login");
    user.value = GETSTUDENT();
    nameInput.value = user.value["name"];
    educationInput.value = user.value["education"];
    genderInput.value = user.value["community"];
    descriptionInput.value = user.value["description"];

    if(!GETSTUDENT().hasOwnProperty("description")) user.value["description"] = "";
    if(!GETSTUDENT().hasOwnProperty("image")) user.value["image"] = null;

    image.value = user.value["image"];
});
const uploadImg = () => {

    if(image.value != null) {
        console.log(image.value);
        image.value = null;
        return;
    }
    document.getElementById('file').click();

    document.getElementById('file').onchange = (e) => {
        const file = e.target.files[0];
        const reader = new FileReader();

        reader.onload = () => {
            const img = new Image();
            img.src = reader.result;

            img.onload = () => {
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');

                let newWidth, newHeight;
                if (img.width > img.height) {
                    newWidth = 500;
                    newHeight = (img.height / img.width) * 500;
                } else {
                    newHeight = 500;
                    newWidth = (img.width / img.height) * 500;
                }

                canvas.width = newWidth;
                canvas.height = newHeight;

                ctx.drawImage(img, 0, 0, newWidth, newHeight);
                const resizedImage = canvas.toDataURL('image/jpeg');
                image.value = resizedImage;
                imgopen.value = false;

                //Remove all previous file input values
                document.getElementById('file').value = "";
            };
        };

        reader.readAsDataURL(file);
    };
};

const deleteAccount = () => {
    showWarning.value = true;
}

const sendDelete = async () => {
    deleteLoad.value = true;
    try {
        const response = await fetch(`${API}/delete/user`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${GETTOKEN()}`
            },
            body: JSON.stringify({
                "id": user.value["id"]
            })
        });

        const data = await response.json();
        console.log(data)
        if (data["status"] == 200) {
            SETTOKEN(null);
            SETSTUDENT(null);
            deleteLoad.value = false
            router.push("/welcome");
        }
        else {
            throw new Error("Failed to Delete Account");
        }
    }
    catch (e) {
        console.log(e);
        deleteLoad.value = false
    }

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
    font-size: 1rem;
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

.divider2 {
    margin-top: 20px;
    display: flex;
    width: 100%;
    gap: 40px;
    align-items: center;
    font-weight: bold;
    font-size: 18px;
    justify-content: space-between;
}

.button {
    color: white;
    font-weight: 300;
    width: 200px;
    height: 45px !important;
    background-color: var(--primary);
}

.divider-left {
    width: 100%;
}

.info-container {
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 20px;
}

.text-container {
    width: 100%;
    display: flex;
    flex-direction: row;
    gap: 20px;
    align-items: start;
}

.label2 {
    margin-top: 20px;
    min-width: 120px;
    font-size: 18px;
    font-weight: 600;
}

.input-bar {
    height: 70px;
    padding-top: 7px;
}

.input-handler {
    opacity: 1 !important;
}


.text-bar {
    height: 115px;
    padding-top: 7px;
}

.label {
    min-width: 120px;
    font-size: 18px;
    font-weight: 600;
}

.profile {
    background-color: var(--primary);
    color: white;
    max-width: 150px;
    max-height: 150px;
    min-width: 150px;
    min-height: 150px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 50px;
    cursor: pointer;
    overflow: hidden;
}


.divider-right {
    display: flex;
    flex-direction: column;
    gap: 20px;
    justify-content: space-between;
    align-items: center;
}

.divider-right-sub {
    display: flex;
    flex-direction: column;
    gap: 12px;
    justify-content: space-around;
    align-items: center;
    margin-bottom: 5px;
}

.cover {
    background-color: rgb(48, 22, 120);
    width: 150px;
    height: 150px;
    border-radius: 50%;
    position: absolute;
    z-index: 1;
    opacity: 0.5;
    display: flex;
    justify-content: center;
    align-items: center;
    pointer-events: none;
}

.imghandler {
    width: 100%;
    height: 100%;

}
</style>