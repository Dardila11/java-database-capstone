/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ["class"],
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Plus Jakarta Sans", "sans-serif"],
      },
      colors: {
        teal: "var(--teal)",
        "teal-light": "var(--teal-light)",
        "teal-mid": "var(--teal-mid)",
        indigo: "var(--indigo)",
        "indigo-light": "var(--indigo-light)",
        bg: "var(--bg)",
        surface: "#ffffff",
        ink: "var(--ink)",
        "ink-2": "var(--ink-2)",
        "ink-3": "var(--ink-3)",
        border: "var(--border)",
      },
      borderRadius: {
        DEFAULT: "var(--radius)",
        sm: "var(--radius-sm)",
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
      },
      boxShadow: {
        sm: "var(--shadow-sm)",
        md: "var(--shadow-md)",
        lg: "var(--shadow-lg)",
      },
      animation: {
        "fade-in": "fadeIn 0.2s ease",
        "slide-up": "slideUp 0.25s cubic-bezier(0.16,1,0.3,1)",
        "slide-in": "slideIn 0.2s ease",
        "pop-in": "popIn 0.25s cubic-bezier(0.16,1,0.3,1)",
      },
      keyframes: {
        fadeIn: { from: { opacity: "0" }, to: { opacity: "1" } },
        slideUp: { from: { opacity: "0", transform: "translateY(22px)" }, to: { opacity: "1", transform: "none" } },
        slideIn: { from: { opacity: "0", transform: "translateX(32px)" }, to: { opacity: "1", transform: "none" } },
        popIn: { "0%": { opacity: "0", transform: "scale(0.92)" }, "100%": { opacity: "1", transform: "scale(1)" } },
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
};
