import { createRouter, createWebHashHistory } from "vue-router";
import WindowVue from "./components/Window.vue";

const routes = [
  {
    path: "/",
    redirect: "/win/home",
  },
  {
    path: "/win",
    name: "win",
    component: WindowVue,
    children: [
      {
        path: "home",
        name: "home",
        component: () => import("./components/PageHome.vue"),
      },
      {
        path: "about",
        name: "about",
        component: () => import("./components/PageAbout.vue"),
      },
      {
        path: "set",
        name: "set",
        component: () => import("./components/set/PageSet.vue"),
      },
    ],
  },
];

export default createRouter({
  history: createWebHashHistory(),
  routes,
});

export const PAGES = routes
  .find((r) => r.path === "/win")
  ?.children?.map((r) => r.name);
