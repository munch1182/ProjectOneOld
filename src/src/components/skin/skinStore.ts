import { defineStore } from "pinia";
import { ref } from "vue";

export const useSkinStore = defineStore("skin", () => {
  enum Skin {
    NONE = "",
    DARK = "theme-dark",
  }
  const skinRef = ref(Skin.NONE);

  function updateSkin(skin: Skin) {
    skinRef.value = skin;
  }

  return { skin: skinRef, Skin, updateSkin };
});
