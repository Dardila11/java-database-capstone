import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  base: "/react/",
  resolve: {
    alias: { "@": path.resolve(__dirname, "./src") },
  },
  server: {
    port: 5173,
    proxy: {
      "/admin": "http://localhost:8080",
      "/doctor": "http://localhost:8080",
      "/patient": "http://localhost:8080",
      "/appointments": "http://localhost:8080",
      "/prescription": "http://localhost:8080",
    },
  },
  build: {
    outDir: "../app/src/main/resources/static/react",
    emptyOutDir: true,
  },
});
