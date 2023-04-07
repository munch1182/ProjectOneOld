import { defineStore } from "pinia";
import { ref } from "vue";

export enum Skin {
  DEFAULT = "default",
  DARK = "dark",
  SKY = "sky",
}

export const skinsName = Object.values(Skin);

export const useSkinStore = defineStore("skin", () => {
  const skinRef = ref(Skin.DEFAULT);

  function updateSkin(skin: Skin | string) {
    console.log(skin);

    if (typeof skin === "string") {
      skinRef.value = <Skin>skin.toUpperCase();
    } else {
      skinRef.value = skin;
    }
  }

  function skinClass() {
    return `theme-${skinRef.value}`.toLocaleLowerCase();
  }

  return { skinClass, Skin, updateSkin };
});
