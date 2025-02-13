'use client';
import { useEffect } from 'react';
import { Provider, useDispatch } from 'react-redux';
import { store } from '../redux/store';
import { setAuthFromStorage } from '../redux/slices/authSlice';
import Navbar from '@/components/Navbar';
import { WebSocketProvider } from '@/context/WebSocketContext';
import Head from 'next/head';
import './globals.css';

const InitAuth = () => {
  const dispatch = useDispatch();
  
  useEffect(() => {
    dispatch(setAuthFromStorage()); // Load auth state from localStorage
  }, [dispatch]);

  return null;
};

const Layout = ({ children }) => {
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
            <Navbar />
            <main className="container mx-auto py-8 px-4">
              {/* <WebSocketProvider>  Uncomment if using WebSockets */}
                {children}
              {/* </WebSocketProvider> */}
            </main>
          </div>
        </Provider>
      </body>
    </html>
  );
};

export default Layout;
