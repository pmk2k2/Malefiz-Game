import { createRouter, createWebHistory } from 'vue-router'
import LoppyView from '@/views/LoppyView.vue'

import MainMenu from '../views/MainMenu.vue'
import LobbyTemp from '../views/LobbyTemp.vue';
import LoginView from '../views/LoginView.vue';
import Beitrittscode from '@/views/Beitrittscode.vue';
import PlayingFieldView from '@/views/PlayingFieldView.vue';
import GridView from '../components/GridView.vue'
import DiceView from '@/views/DiceView.vue'

const router = createRouter({
  history: createWebHistory(),
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
  const gameCode = !!localStorage.getItem("gameCode")

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
