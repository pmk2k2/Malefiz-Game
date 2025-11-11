import { createRouter, createWebHistory } from 'vue-router'
import DiceView from '../components/DiceView.vue'
import GridView from '../components/GridView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dice',
      component: DiceView,
    },
    {
      path: '/grid',
      name: 'grid',
      component: GridView,
    },
  ],
})

export default router
