import { createRouter, createWebHistory } from 'vue-router'
import LobbyView from '@/views/LobbyView.vue'

import MainMenu from '../views/MainMenu.vue'
import LobbyTemp from '../views/LobbyTemp.vue';
import LoginView from '../views/LoginView.vue';
import Beitrittscode from '@/views/Beitrittscode.vue';
import PlayingFieldView from '@/views/PlayingFieldView.vue';
import GridView from '../components/GridView.vue'
import DiceView from '@/views/DiceView.vue'
import { useGameStore } from '@/stores/gamestore';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: LoginView },
    { path: '/join', component: Beitrittscode },
    { path: '/lobby', component: LobbyView },
    { path: '/main', component: MainMenu },
    { path: '/game', component: GridView },
    { path: '/field', component: PlayingFieldView }
  ],
})
router.beforeEach((to, from, next) => {
  const protectedPages = ['/main', '/join', '/lobby']
  const gameStore = useGameStore()
  const loggedIn = !!gameStore.gameData.playerName
  const gameCode = !!gameStore.gameData.gameCode

  if (protectedPages.includes(to.path) && !loggedIn) {
    return next('/')
  }
  if(to.path ==  '/' && gameCode){
    return next('/lobby')
  }
  
  if(to.path ==  '/' && loggedIn){
    return next('/main')
  }
  
  next()
})


export default router
