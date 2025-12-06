import { createRouter, createWebHistory } from 'vue-router'
import LobbyView from '@/views/LobbyView.vue'
import MainMenu from '../views/MainMenu.vue'
import LoginView from '../views/LoginView.vue'
import Beitrittscode from '@/views/Beitrittscode.vue'
import GameView from '../views/GameView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: LoginView },
    { path: '/join', component: Beitrittscode },
    { path: '/lobby', component: LobbyView },
    { path: '/main', component: MainMenu },
    { path: '/game', component: GameView },
  ],
})
router.beforeEach((to, from, next) => {
  const protectedPages = ['/main', '/join', '/lobby']
  const loggedIn = !!localStorage.getItem('playerName')
  const gameCode = !!localStorage.getItem('gameCode')

  if (protectedPages.includes(to.path) && !loggedIn) {
    return next('/')
  }
  if (to.path == '/' && gameCode) {
    return next('/lobby')
  }

  if (to.path == '/' && loggedIn) {
    return next('/main')
  }

  next()
})

export default router
