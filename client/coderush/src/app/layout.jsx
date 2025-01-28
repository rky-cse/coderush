'use client'
import { Provider } from 'react-redux';
import { store } from '../redux/store';
import Navbar from '@/components/Navbar';
import './globals.css';

const Layout = ({ children }) => {
  return (
    <html lang="en">
      <body>
        <Provider store={store}>
          <div className="min-h-screen bg-gray-100">
            <Navbar />
            <main className="container mx-auto py-8 px-4">
              {children}
            </main>
          </div>
        </Provider>

      </body>

    </html>

  );
};

export default Layout;