import { createRouter, createWebHistory } from 'vue-router'
import GridView from '../components/GridView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'grid',
      component: GridView,
    },
  ],
})

export default router
