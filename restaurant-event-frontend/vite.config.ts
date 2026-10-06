/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig({ plugins: [react()], server: { port: 5174, strictPort: true, proxy: { '/api': 'http://127.0.0.1:8081' },
  // node_modules is a link to node_modules.nosync (kept out of iCloud); never watch it.
  watch: { ignored: ['**/node_modules.nosync/**'] } },
  // Only run this project's tests, not tests shipped inside node_modules.nosync.
  test: { include: ['src/**/*.test.ts'] } });
