'use client';
import axios from 'axios';
import { getCookie, deleteCookie } from 'cookies-next';

/**
 * Central axios instance.
 *
 * - Auto-attaches the JWT bearer token from cookies on every request.
 * - Normalises every error into a consistent shape so pages can do
 *     toast.error(err.message)
 *   without remembering the response envelope.
 * - Auto-handles TOKEN_EXPIRED by clearing cookies and redirecting to /login.
 *
 * Backend error shape (from ApiError.java):
 *   { code: string, message: string, field: string|null, timestamp: number }
 */

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 15_000,
});

// Attach bearer token on every request.
api.interceptors.request.use(
  (config) => {
    const token = getCookie('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Normalise responses + handle global error cases.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data;
    const code = data?.code;
    const message = data?.message;

    // Session expired or invalid token: drop client-side state and bounce to login.
    if (code === 'TOKEN_EXPIRED' || code === 'TOKEN_INVALID') {
      deleteCookie('token');
      deleteCookie('username');
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // Build a normalised error so callers can do `err.message`, `err.code`, etc.
    const normalised = new Error(
      message ||
        (error.code === 'ECONNABORTED'
          ? 'Request timed out. Please try again.'
          : 'Could not reach the server. Please check your connection.')
    );
    normalised.code = code || (error.response ? 'UNKNOWN_ERROR' : 'NETWORK_ERROR');
    normalised.field = data?.field ?? null;
    normalised.status = error.response?.status ?? 0;
    normalised.raw = error;

    return Promise.reject(normalised);
  }
);

export default api;
