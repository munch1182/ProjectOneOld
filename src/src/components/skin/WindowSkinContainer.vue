<!-- 提供换肤功能，需要包裹在最外层 -->
<script setup lang="ts">
import { useSkinStore } from "./skinStore";
const skinStore = useSkinStore();
</script>

<template>
  <div :class="skinStore.skin.valueOf()">
    <slot></slot>
  </div>
</template>
