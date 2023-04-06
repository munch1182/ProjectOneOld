<script setup lang="ts">
import { appWindow } from "@tauri-apps/api/window";

async function hide() {
  await appWindow.minimize();
}

async function minMax() {
  if (await appWindow.isMaximized()) {
    await appWindow.unmaximize();
  } else {
    await appWindow.maximize();
  }
}

async function exit() {
  await appWindow.close();
}
</script>

<template>
  <div class="flex h-[30px]">
    <!-- https://learn.microsoft.com/zh-cn/windows/apps/design/style/segoe-ui-symbol-font -->
    <div class="fun" alt="minimize" @click="hide">
      <span>&#xE949;</span>
    </div>
    <div class="fun" alt="maximize" @click="minMax">
      <span class="max">&#xE739;</span>
    </div>
    <div class="fun close" alt="close" @click="exit">
      <span>&#xE711;</span>
    </div>
  </div>
</template>

<style scoped>
span {
  color: black;
  margin: auto auto;
  font-size: 16px;
  font-family: Segoe MDL2 Assets;
}
.max {
  font-size: 4px;
}
.fun {
  position: relative;
  display: flex;
  width: 48px;
  height: 30px;
}
.fun::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 48px;
  height: 0px;
  background-color: rgba(0, 0, 0, 0.3);
  opacity: 0.5;
}
.fun:hover::after {
  height: 100%;
}
.close:hover {
  background-color: #c42b1c;
}
.close:hover span {
  color: white;
}
</style>
