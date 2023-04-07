import { createI18n } from "vue-i18n";
import en from "./locales/en.json";
import cn from "./locales/cn.json";

export function i18n() {
  return createI18n({
    legacy: false,
    locale: "cn",
    messages: { en, cn },
  });
}

export const LANGS = [
  { name: "English", value: "en" },
  { name: "中文", value: "cn" },
];
