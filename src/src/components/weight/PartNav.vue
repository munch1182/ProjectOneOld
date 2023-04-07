<script setup lang="ts">
import { ref } from "vue";
import { PAGES } from "../../router";
import { useRouter, useRoute } from "vue-router";
import { useI18n } from "vue-i18n";

const curr = ref(useRoute().path);
const router = useRouter();
const t = useI18n().t;

function navigation(name: string) {
  curr.value = `/win/${name}`;
  router.push(curr.value);
}

// todo 将此内容放在router中
function trans(name: string) {
  switch (name) {
    case "home":
      return t("nav.home");
    case "about":
      return t("nav.about");
    case "set":
      return t("nav.set");
    default:
      break;
  }
  return name;
}
</script>

<template>
  <ul class="w-32">
    <li
      class="h-14 cursor-pointer list-none"
      :class="curr.includes(p) ? 'bg-skin-selected' : 'hover:bg-skin-hover'"
      v-for="p in PAGES"
    >
      <a
        @click.preevent="navigation(p)"
        class="block text-center leading-[3.5rem] text-skin-base"
      >
        {{ trans(p) }}
      </a>
    </li>
  </ul>
</template>
