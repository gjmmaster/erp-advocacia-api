import axios from 'axios';
import { createContext, useMemo, useState } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token'));

  const login = async (email, password) => {
    try {
      // --- INÍCIO DA ALTERAÇÃO ---
      // Adicionamos o objeto de configuração com os headers na chamada do axios
      const { data } = await axios.post(
        '/auth/login',          // 1. URL do endpoint
        { email, password },    // 2. Corpo (body) da requisição
        {                       // 3. Objeto de configuração da requisição
          headers: {
            'X-Tenant-Subdomain': 'advocacia-teste-final'
          }
        }
      );
      // --- FIM DA ALTERAÇÃO ---

      if (data.token) {
        localStorage.setItem('token', data.token);
        setToken(data.token);
        return true;
      }
    } catch (error) {
      console.error('Failed to login:', error);
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
  };

  const authContextValue = useMemo(
    () => ({
      token,
      login,
      logout,
    }),
    [token]
  );

  return (
    <AuthContext.Provider value={authContextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
