<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { LANGS } from "../../i18n";
import ViewSelect from "../weight/ViewSelect.vue";
import { skinsName, useSkinStore } from "../skin/skinStore";

const { t, locale } = useI18n();

const langs = ref(LANGS);
const theme = ref(
  skinsName.map((s) => {
    return { name: s };
  })
);

function updateLang(lang: string) {
  locale.value = lang;
}
function updateTheme(theme: string) {
  useSkinStore().updateSkin(theme);
}
</script>

<template>
  <span class="text-page text-skin-base">{{ t("str.language") }}:</span>
  <ViewSelect
    :opts="langs"
    class="ml-2"
    @update-value="(v) => updateLang(v.value)"
  />
  <br />
  <span class="text-page text-skin-base">{{ t("str.theme") }}:</span>
  <ViewSelect
    :opts="theme"
    class="ml-2"
    @update-value="(v) => updateTheme(v.name)"
  />
</template>

<style scoped></style>
