import { createRouter, createWebHistory } from 'vue-router'

import Index from '../pages/index.vue'
import Welcome from '../pages/welcome.vue'

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
    path: '/:pathMatch(.*)*',
    redirect: '/welcome'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router;
