import { createRouter, createWebHistory } from 'vue-router'
import LoppyView from '@/views/LoppyView.vue'

import MainMenu from '../views/MainMenu.vue'
import LobbyTemp from '../views/LobbyTemp.vue';
import LoginView from '../views/LoginView.vue';
import Beitrittscode from '@/views/Beitrittscode.vue';
import GridView from '@/components/GridView.vue';
import PlayingFieldView from '@/views/PlayingFieldView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: LoginView },
    { path: '/join', component: Beitrittscode },
    { path: '/lobby', component: LoppyView },
    { path: '/main', component: MainMenu },
    { path: '/grid', component: GridView },
    { path: '/field', component: PlayingFieldView }
  ],
})
router.beforeEach((to, from, next) => {
  const protectedPages = ['/main', '/join', '/lobby']
  const loggedIn = !!localStorage.getItem("playerName")

  if (protectedPages.includes(to.path) && !loggedIn) {
    return next('/')
  }

  next()
})
export default router
