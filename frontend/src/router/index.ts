import { createRouter, createWebHistory } from 'vue-router'
import DiceView from '../views/DiceView.vue'
import LoppyView from '@/views/LoppyView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dice',
      component: LoppyView,
    },
  ],
})

export default router
