<script setup lang="ts">
import WindowSkinContainer from "./skin/WindowSkinContainer.vue";
import PartNav from "./weight/PartNav.vue";
import PartTitleBar from "./weight/PartTitleBar.vue";
import PartWindowFun from "./weight/PartWindowFun.vue";
</script>

<template>
  <WindowSkinContainer>
    <!-- todo: 高度不应该占满 -->
    <div
      data-tauri-drag-region
      class="flex bg-skin-titlebarsmall sm:bg-skin-titlebardef"
    >
      <PartTitleBar class="h-[var(--size-title-bar-height)] w-full">
        首页
      </PartTitleBar>
      <PartWindowFun />
    </div>
    <div>
      <PartNav />
      <RouterView />
    </div>
  </WindowSkinContainer>
</template>
