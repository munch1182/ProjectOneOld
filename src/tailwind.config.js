/** @type {import('tailwindcss').Config} */

import { textColor, backgroundColor, fontSize } from "./src/stylecss";

export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  // 保留未使用的主题避免被优化导致无法动态更换主题
  safelist: ["theme-dark", "theme-sky"],
  theme: {
    extend: {
      textColor,
      backgroundColor,
      fontSize,
    },
  },
  plugins: [],
};
