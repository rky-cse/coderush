// src/components/Navbar.jsx
'use client';
import Link from 'next/link';
import { useState } from 'react';
// Removed Menu-related imports due to resolution issues.
// Removed Button import for now as it couldn't be resolved. Please use a standard button component if necessary or confirm library.

export default function Navbar() {
  const [isLoggedIn, setIsLoggedIn] = useState(false); // Replace with actual auth state

  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-4 py-3 flex justify-between items-center">
        <Link href="/" className="text-xl font-bold text-gray-700">
          Coderush
        </Link>
        <div className="flex items-center space-x-4">
          <Link href="/dashboard" className="text-gray-600 hover:text-gray-800">
            Dashboard
          </Link>
          <Link href="/createTournament" className="text-gray-600 hover:text-gray-800">
            Create Tournament
          </Link>
          <Link href="/joinTournament" className="text-gray-600 hover:text-gray-800">
            Join Tournament
          </Link>
          {isLoggedIn ? (
            <div><button onClick={() => setIsLoggedIn(false)} className='text-gray-600'>Logout</button></div>
          ) : (
            <Link href="/login" className="text-gray-600 hover:text-gray-800">
              Login
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}
