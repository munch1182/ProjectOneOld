<script setup lang="ts">
import { ref, watch } from "vue";

export interface Opt {
  name: string;
  value?: string;
}

const input = defineProps<{ opts: Opt[]; value?: string }>();
const select = ref<Opt>();

const output = defineEmits(["updateValue"]);
watch(select, (v) => output("updateValue", v));
</script>

<template>
  <select class="text-page" v-model="select">
    <option class="text-page" v-for="opt in input.opts" :value="opt">
      {{ opt.name }}
    </option>
  </select>
</template>

<style scoped></style>
