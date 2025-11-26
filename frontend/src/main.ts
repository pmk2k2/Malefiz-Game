import './style.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Create Vue app
const app = createApp(App)

// Create Pinia instance
const pinia = createPinia()

// Install Pinia and Router
app.use(pinia)
app.use(router)

// Mount the app
app.mount('#app')

