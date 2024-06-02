import { createRouter, createWebHistory } from 'vue-router'

import Index from '../pages/index.vue'
import Welcome from '../pages/welcome.vue'
import Login from '../pages/login.vue'

const routes = [
  {
    path: '/',
    name: 'Index',
    component: Index
  },
  {
    path: '/welcome',
    name: 'Welcome',
    component: Welcome
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/welcome'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router;
