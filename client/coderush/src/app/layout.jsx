


'use client';
import React, { useEffect } from 'react';
import { Provider, useDispatch } from 'react-redux';
import { store } from '../redux/store';
import { setAuthFromStorage } from '../redux/slices/authSlice';
import { setUser, clearUser } from '../redux/slices/userSlice'; // <-- import user actions
import api from '@/services/api';
import Navbar from '@/components/Navbar';
import { WebSocketProvider } from '@/context/WebSocketContext';
import Head from 'next/head';
import './globals.css';
import { Toaster } from 'react-hot-toast';
import { getCookie } from 'cookies-next';
import { usePathname } from 'next/navigation';

const InitAuth = () => {
  const dispatch = useDispatch();
  const token = getCookie('token');

  useEffect(() => {
    // 1) Existing logic: load auth from localStorage
    dispatch(setAuthFromStorage());

    // 2) Check for token cookie
    
    if (!token) {
      // If no token, clear user data
      dispatch(clearUser());
      return;
    }

    // 3) If token, fetch user data from server
    api
      .get('/api/user/me')
      .then((res) => {
        dispatch(setUser(res.data));
      })
      .catch(() => {
        // The api interceptor already auto-redirects to /login on TOKEN_INVALID,
        // so we just clear local state here.
        dispatch(clearUser());
      });
  }, []);

  return null;
};

const Layout = ({ children }) => {
  // Get current route pathname
  const pathname = usePathname();
  
  // Determine if the Navbar should be hidden on tournament pages
  const hideNavbar = pathname.startsWith('/tournamentPage');

  return (
    <html lang="en">
      <Head>
        <title>My App</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>
      <body>
        <Provider store={store}>
          <InitAuth />
          <div className="min-h-screen bg-gray-100">
            {/* Conditionally render Navbar */}
            { !hideNavbar && <Navbar /> }
            <main className="container mx-auto">
              <Toaster position="top-center" reverseOrder={false} />
              {children}
            </main>
          </div>
        </Provider>
      </body>
    </html>
  );
};

export default Layout;
