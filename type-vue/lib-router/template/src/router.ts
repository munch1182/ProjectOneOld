import { createRouter, createWebHashHistory } from 'vue-router';

const routes = [
    {
        path: '/',
        redirect: '/home'
    },
    {
        path: '/home',
        name: 'home',
        // 需要导入实际文件地址
        component: () => import('/components/Home.vue')
    }
]

export default createRouter({
    history: createWebHashHistory(),
    routes
})