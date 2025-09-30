import axios from 'axios';
import { createContext, useMemo, useState } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token'));

  const login = async (email, password) => {
    try {
      const { data } = await axios.post('/auth/login', { email, password });
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