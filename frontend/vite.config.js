import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // 
  // --- ADIÇÃO CRÍTICA PARA DEPLOY ---
  // Força o Vite a usar caminhos de asset relativos no build final.
  // Isso resolve problemas de "tela em branco" em ambientes de produção
  // ao garantir que o index.html consiga encontrar os arquivos JS e CSS.
  base: './' 
})
