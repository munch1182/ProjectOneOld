<script setup lang="ts">
import WindowSkinContainer from "./skin/WindowSkinContainer.vue";
import PartNav from "./weight/PartNav.vue";
import PartTitleBar from "./weight/PartTitleBar.vue";
import PartWindowFun from "./weight/PartWindowFun.vue";
</script>

<template>
  <WindowSkinContainer class="flex flex-col">
    <header
      data-tauri-drag-region
      class="bg-header sm:bg-skin-titlebardef flex"
    >
      <PartTitleBar
        class="h-[var(--size-title-bar-height)] w-full min-w-[6rem]"
      >
        <span class="p-6">Home</span>
      </PartTitleBar>
      <PartWindowFun />
    </header>
    <main class="flex flex-auto">
      <nav class="flex h-full flex-col">
        <PartNav />
      </nav>
      <div class="h-content w-[1px] bg-[var(--color-default-grey)]"></div>
      <article class="h-content flex flex-auto p-[var(--size-padding-default)]">
        <div>
          <RouterView />
        </div>
      </article>
    </main>
  </WindowSkinContainer>
</template>

<style scoped>
.h-content {
  height: calc(100vh - var(--size-title-bar-height));
}
.bg-header {
  background-image: linear-gradient(
    to right bottom,
    var(--color-title-bar-start-skin),
    var(--color-title-bar-end-skin)
  );
}
</style>
