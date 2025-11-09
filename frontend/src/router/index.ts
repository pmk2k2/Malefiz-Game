import { createRouter, createWebHistory } from 'vue-router'
import DiceView from '../views/DiceView.vue'
import PlayingFieldView from '@/views/PlayingFieldView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dice',
      component: DiceView,
    },
    {
      path: '/field',
      name: 'field',
      component: PlayingFieldView,
    },
  ],
})

export default router
