import { createRouter, createWebHistory } from 'vue-router'
import MainMenu from '../views/MainMenu.vue'
import LobbyTemp from '../views/LobbyTemp.vue';
import LoginView from '../views/LoginView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: MainMenu },
    { path: '/lobby', component: LobbyTemp },
    { path: '/login', component: LoginView }
  ],
})

export default router
