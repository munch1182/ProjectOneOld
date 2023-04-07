<!-- 提供换肤功能，需要包裹在最外层 -->
<script setup lang="ts">
import { useSkinStore } from "./skinStore";
const skinStore = useSkinStore();
</script>

<template>
  <div class="bg-skin-page" :class="skinStore.skinClass()">
    <slot></slot>
  </div>
</template>
