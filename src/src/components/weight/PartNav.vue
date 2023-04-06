<script setup lang="ts">
import { ref } from "vue";
import { PAGES } from "../../router";
import { useRouter, useRoute } from "vue-router";

const curr = ref(useRoute().path);
const router = useRouter();
function navigation(name: string) {
  curr.value = `/win/${name}`;
  console.log(curr.value);

  router.push(curr.value);
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
        {{ p }}
      </a>
    </li>
  </ul>
</template>
