<script setup lang="ts">
import { PAGES } from "../../router";
</script>

<template>
  <li class="list-none bg-skin-page" v-for="p in PAGES">
    <a href="#">
      <span class="leading-24 inline-block h-14 max-h-none w-24 text-skin-base">
        {{ p }}
        <!-- i18n -->
      </span>
    </a>
  </li>
</template>

<style scoped></style>
