import { NextResponse } from 'next/server';

export function middleware(req) {
  const token = req.cookies.get('token')?.value; // Get token from cookies

  // If user is not logged in and tries to access restricted pages, redirect to /login
  if (!token && req.nextUrl.pathname !== '/login') {
    return NextResponse.redirect(new URL('/login', req.url));
  }

  return NextResponse.next(); // Allow access
}

// Define protected routes
export const config = {
  matcher: ['/dashboard', '/createTournament', '/joinTournament','/createTestcase','/createQuestion'], // Add restricted pages
};
