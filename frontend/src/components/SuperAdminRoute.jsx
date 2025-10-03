import { jwtDecode } from 'jwt-decode';
import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';

const SuperAdminRoute = ({ children }) => {
  // 1. Pega o token do contexto de autenticação global
  const { token } = useContext(AuthContext);

  // 2. Se não houver token, o usuário não está logado. Redireciona para o login.
  if (!token) {
    return <Navigate to="/super-admin/login" />;
  }

  try {
    // 3. Tenta decodificar o token para ler as informações (payload)
    const decodedToken = jwtDecode(token);
    
    // 4. Verifica se a 'role' dentro do token é 'super-admin'.
    //    Se não for, redireciona para o login, mesmo que o token seja válido.
    if (decodedToken.role !== 'super-admin') {
      return <Navigate to="/super-admin/login" />;
    }
  } catch (error) {
    // 5. Se o token for inválido (expirado, malformado, etc.),
    //    a biblioteca jwt-decode vai gerar um erro. Capturamos o erro e
    //    redirecionamos para o login.
    console.error('Token inválido ou expirado:', error);
    return <Navigate to="/super-admin/login" />;
  }

  // 6. Se todas as verificações acima passarem, renderiza o componente filho
  //    (neste caso, a página SuperAdminDashboardPage).
  return children;
};

export default SuperAdminRoute;
