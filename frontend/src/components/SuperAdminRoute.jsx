import { jwtDecode } from 'jwt-decode';
import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';

const SuperAdminRoute = ({ children }) => {
  const { token } = useContext(AuthContext);

  if (!token) {
    return <Navigate to="/super-admin/login" />;
  }

  try {
    const decodedToken = jwtDecode(token);
    if (decodedToken.role !== 'super-admin') {
      // Token exists but is not for a super-admin, boot them out
      return <Navigate to="/super-admin/login" />;
    }
  } catch (error) {
    // If token is invalid or expired, jwt-decode will throw an error
    console.error('Invalid token:', error);
    return <Navigate to="/super-admin/login" />;
  }

  return children;
};

export default SuperAdminRoute;