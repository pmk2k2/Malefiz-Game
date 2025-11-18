import { createRouter, createWebHistory } from 'vue-router'
import DiceView from '../views/DiceView.vue'
import LoppyView from '@/views/LoppyView.vue'

import MainMenu from '../views/MainMenu.vue'
import LobbyTemp from '../views/LobbyTemp.vue';
import LoginView from '../views/LoginView.vue';
import Beitrittscode from '@/views/Beitrittscode.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/dice',
      name: 'dice',
      component: DiceView,
    },
    { path: '/', component: LoginView },
    { path: '/join', component: Beitrittscode },
    { path: '/lobby', component: LoppyView },
    { path: '/main', component: MainMenu }
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
