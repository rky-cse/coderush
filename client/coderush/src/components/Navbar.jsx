'use client';
import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useRouter } from 'next/navigation';
import { logout } from '../redux/slices/authSlice';
import Link from 'next/link';
import { deleteCookie } from 'cookies-next';
import { FiMenu, FiX, FiChevronDown, FiUser, FiLogOut, FiAward, FiPlusCircle, FiFileText } from 'react-icons/fi';

// Logo component with link to home
const Logo = () => (
  <Link href="/" className="flex items-center space-x-2 text-indigo-600 transition-colors hover:text-indigo-700">
    <svg className="h-8 w-8" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M7 8L3 12L7 16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M17 8L21 12L17 16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M14 4L10 20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
    <span className="text-xl font-bold tracking-tight">Coderush</span>
  </Link>
);

// NavLink component for consistent link styling
const NavLink = ({ href, icon: Icon, children }) => (
  <Link 
    href={href} 
    className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 rounded-md transition-all duration-200 hover:bg-indigo-50 hover:text-indigo-600"
  >
    {Icon && <Icon className="mr-2 h-4 w-4" />}
    {children}
  </Link>
);

// User dropdown menu
const UserMenu = ({ user, handleLogout }) => {
  const [isOpen, setIsOpen] = useState(false);
  
  return (
    <div className="relative ml-3">
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center text-sm font-medium text-gray-700 rounded-full hover:text-indigo-600 focus:outline-none"
      >
        <span className="mr-2">{user}</span>
        <div className="flex items-center justify-center h-8 w-8 rounded-full bg-indigo-100 text-indigo-600">
          <FiUser className="h-4 w-4" />
        </div>
        <FiChevronDown className={`ml-1 h-4 w-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>
      
      {isOpen && (
        <div className="absolute right-0 mt-2 w-48 py-2 bg-white rounded-md shadow-lg border border-gray-100 z-10">
          <Link 
            href={`/dashboard/${user}`} 
            className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600"
            onClick={() => setIsOpen(false)}
          >
            <FiUser className="mr-2 h-4 w-4" />
            Dashboard
          </Link>
          <button
            onClick={() => {
              handleLogout();
              setIsOpen(false);
            }}
            className="flex items-center w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600"
          >
            <FiLogOut className="mr-2 h-4 w-4" />
            Logout
          </button>
        </div>
      )}
    </div>
  );
};

export default function Navbar() {
  const { user } = useSelector((state) => state.auth);
  const router = useRouter();
  const dispatch = useDispatch();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 10);
    };
    
    window.addEventListener('scroll', handleScroll);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  const handleLogout = () => {
    dispatch(logout());  // Clear Redux state
    deleteCookie('token'); // Remove token from cookies
    deleteCookie('username'); // Remove stored username
    router.push('/login'); // Redirect to login page
  };

  const closeMenu = () => setIsMenuOpen(false);

  return (
    <nav className={`sticky top-0 z-50 bg-white transition-shadow duration-300 ${
      scrolled ? 'shadow-md' : ''
    }`}>
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <Logo />
          
          {/* Mobile menu button */}
          <button 
            className="md:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-700 hover:text-indigo-600 hover:bg-indigo-50"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            {isMenuOpen ? (
              <FiX className="h-6 w-6" />
            ) : (
              <FiMenu className="h-6 w-6" />
            )}
          </button>
          
          {/* Desktop menu */}
          <div className="hidden md:flex md:items-center md:space-x-1">
            {user ? (
              <>
                <NavLink href="/tournaments" icon={FiAward}>
                  Tournaments
                </NavLink>
                <NavLink href="/create" icon={FiPlusCircle}>
                  Create
                </NavLink>
                <NavLink href="/myQuestions" icon={FiFileText}>
                  My Questions
                </NavLink>
                <UserMenu user={user} handleLogout={handleLogout} />
              </>
            ) : (
              <NavLink href="/login">
                Login
              </NavLink>
            )}
          </div>
        </div>
        
        {/* Mobile menu */}
        {isMenuOpen && (
          <div className="md:hidden py-2 border-t border-gray-100">
            {user ? (
              <div className="space-y-1 px-2 pt-2 pb-3">
                <NavLink href={`/dashboard/${user}`} icon={FiUser} onClick={closeMenu}>
                  Dashboard
                </NavLink>
                <NavLink href="/tournaments" icon={FiAward} onClick={closeMenu}>
                  Tournaments
                </NavLink>
                <NavLink href="/create" icon={FiPlusCircle} onClick={closeMenu}>
                  Create Tournament
                </NavLink>
                <NavLink href="/myQuestions" icon={FiFileText} onClick={closeMenu}>
                  My Questions
                </NavLink>
                <button
                  onClick={handleLogout}
                  className="flex items-center w-full text-left px-3 py-2 text-sm font-medium text-gray-700 rounded-md transition-all duration-200 hover:bg-indigo-50 hover:text-indigo-600"
                >
                  <FiLogOut className="mr-2 h-4 w-4" />
                  Logout
                </button>
              </div>
            ) : (
              <div className="px-2 pt-2 pb-3 space-y-1">
                <NavLink href="/login" onClick={closeMenu}>
                  Login
                </NavLink>
              </div>
            )}
          </div>
        )}
      </div>
    </nav>
  );
}