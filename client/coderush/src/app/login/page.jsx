'use client';
import { useState } from 'react';
import notify from '@/services/notify';
import { useDispatch } from 'react-redux';
import { useRouter } from 'next/navigation';
import api from '@/services/api';
import { login } from '@/redux/slices/authSlice';

export default function Login() {
  const dispatch = useDispatch();
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setLoading(true);
    try {
      const response = await api.post('/api/auth/login', {
        userName: username,
        password,
      });
      dispatch(login({ user: username, token: response.data.token }));
      router.push(`/dashboard/${username}`);
    } catch (err) {
      notify.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-50">
      <div className="w-full max-w-md p-8 space-y-6 bg-white shadow-md rounded-xl">
        <h1 className="text-2xl font-bold text-gray-900 text-center">Login</h1>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700">Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="Enter your username"
              required
              disabled={loading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="Enter your password"
              required
              disabled={loading}
            />
          </div>
          <button
            type="submit"
            className={`w-full px-4 py-2 font-semibold text-white bg-indigo-600 rounded-md focus:outline-none hover:bg-indigo-700 ${
              loading ? 'opacity-70 cursor-not-allowed' : ''
            }`}
            disabled={loading}
          >
            {loading ? 'Logging in…' : 'Login'}
          </button>
        </form>
        <div className="text-center">
          <p className="text-sm text-gray-600">
            Don&apos;t have an account?
            <button
              onClick={() => router.push('/register')}
              className="text-indigo-600 hover:underline ml-1"
            >
              Register
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
