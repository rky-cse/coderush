// 'use client';
// import { useEffect } from 'react';
// import { Provider, useDispatch } from 'react-redux';
// import { store } from '../redux/store';
// import { setAuthFromStorage } from '../redux/slices/authSlice';
// import Navbar from '@/components/Navbar';
// import { WebSocketProvider } from '@/context/WebSocketContext';
// import Head from 'next/head';
// import './globals.css';
// import { Toaster } from 'react-hot-toast';

// import { getCookie } from 'cookies-next';
// const InitAuth = () => {
//   const dispatch = useDispatch();
  
//   useEffect(() => {
//     dispatch(setAuthFromStorage()); // Load auth state from localStorage
//   }, [dispatch]);

//   return null;
// };

// const Layout = ({ children }) => {
//   return (
//     <html lang="en">
//       <Head>
//         <title>My App</title>
//         <link rel="icon" href="/favicon.ico" />
//       </Head>
//       <body>
//         <Provider store={store}>
//           <InitAuth />
//           <div className="min-h-screen bg-gray-100">
//             <Navbar />
//             <main className="container mx-auto py-8 px-4">
//             <Toaster position="top-right" reverseOrder={false} />
//             {children}
//             </main>
//           </div>
//         </Provider>
//       </body>
//     </html>
//   );
// };

// export default Layout;


'use client';
import React, { useEffect } from 'react';
import { Provider, useDispatch } from 'react-redux';
import { store } from '../redux/store';
import { setAuthFromStorage } from '../redux/slices/authSlice';
import { setUser, clearUser } from '../redux/slices/userSlice'; // <-- import user actions
import axios from 'axios'; // for the user fetch
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
    axios
      .get(`${process.env.NEXT_PUBLIC_API_URL}/api/user/me`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        // If successful, store user in Redux
        console.log("Syncing user data in redux --> ",res.data);
        dispatch(setUser(res.data));
      })
      .catch((err) => {
        console.error(err);
        // If error, clear user
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
