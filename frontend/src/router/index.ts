import { createRouter, createWebHistory } from 'vue-router'
import GridView from '../components/GridView.vue'
import DiceView from '@/views/DiceView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'grid',
      component: GridView,
    },
    {
      path: '/dice',
      name: 'dice',
      component: DiceView,
    },
  ],
})

export default router
