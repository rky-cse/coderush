'use client';
import { createContext, useContext, useEffect, useState } from 'react';
import { setCookie, getCookie, deleteCookie } from 'cookies-next';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = getCookie('token');
    const username = getCookie('username');

    if (token && username) {
      setUser({ username, token });
    }
  }, []);

  const login = (username, token) => {
    setCookie('token', token, { path: '/' });
    setCookie('username', username, { path: '/' });
    setUser({ username, token });
  };

  const logout = () => {
    deleteCookie('token');
    deleteCookie('username');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};
