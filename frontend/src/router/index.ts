import { createRouter, createWebHistory } from 'vue-router'
import GridView from '../components/GridView.vue'
import PlayingFieldView from '@/views/PlayingFieldView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'grid',
      component: GridView,
    },
    {
      path: '/field',
      name: 'field',
      component: PlayingFieldView,
    },
  ],
})

export default router
