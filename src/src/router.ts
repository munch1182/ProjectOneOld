import { createRouter, createWebHashHistory } from 'vue-router'
import Main from './components/Home.vue'

const routes = [
    { path: '/', redirect: '/home' },
    { path: '/home', component: Main }
]

export default createRouter({
    history: createWebHashHistory(),
    routes,
})