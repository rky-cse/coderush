'use client';
import { useEffect } from 'react';
import { Provider, useDispatch } from 'react-redux';
import { store } from '../redux/store';
import { setAuthFromStorage } from '../redux/slices/authSlice';
import Navbar from '@/components/Navbar';
import { WebSocketProvider } from '@/context/WebSocketContext';
import Head from 'next/head';
import './globals.css';
import { Toaster } from 'react-hot-toast';
import { getCookie } from 'cookies-next';
import { usePathname } from 'next/navigation';

const InitAuth = () => {
  const dispatch = useDispatch();
  
  useEffect(() => {
    dispatch(setAuthFromStorage()); // Load auth state from localStorage
  }, [dispatch]);

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
              <Toaster position="top-right" reverseOrder={false} />
              {children}
            </main>
          </div>
        </Provider>
      </body>
    </html>
  );
};

export default Layout;
