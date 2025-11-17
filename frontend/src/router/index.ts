import { createRouter, createWebHistory } from 'vue-router'
import MainMenu from '../views/MainMenu.vue'
import LobbyTemp from '../views/LobbyTemp.vue';
import LoginView from '../views/LoginView.vue';
import Beitrittscode from '@/views/Beitrittscode.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: LoginView },
    { path: '/join', component: Beitrittscode },
    { path: '/lobby', component: LobbyTemp },
    { path: '/main', component: MainMenu }
  ],
})
router.beforeEach((to, from, next) => {
  if (to.path === '/main' && !to.query.playerName) {
    return next('/')
  }
  next()
})
export default router
