import { createRouter, createWebHistory } from 'vue-router'
import DiceView from '../views/DiceView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'dice',
      component: DiceView,
    },
  ],
})

export default router
