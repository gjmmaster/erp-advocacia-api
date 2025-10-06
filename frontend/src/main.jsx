// frontend/src/main.jsx
import axios from 'axios'; // Importe o axios aqui
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext.jsx';
import './index.css';

// Configure a URL base para todas as chamadas futuras do axios
// Ele usará a variável de ambiente em produção ou localhost em desenvolvimento
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
);
