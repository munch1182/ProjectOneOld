/** @type {import('tailwindcss').Config} */

import { textColor, backgroundColor } from "./src/stylecss.ts";

export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      textColor,
      backgroundColor,
    },
  },
  plugins: [],
};
