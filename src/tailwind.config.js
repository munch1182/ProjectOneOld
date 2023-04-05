/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      textColor: {
        skin: {
          base: `var(--color-text-base)`,
        },
      },
      backgroundColor: {
        skin: {
          titlebardef: `var(--color-title-bar-default)`,
          titlebarsmall: `var(--color-title-bar-small)`,
          page: "var(--color-page-background)",
        },
      },
    },
  },
  plugins: [],
};
