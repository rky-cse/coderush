'use client';
import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'next/navigation';
import api from '@/services/api';

export default function Profile() {
  const { userName } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchUser = useCallback(async () => {
    if (!userName) return;
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/api/user/${userName}`);
      setUser(response.data);
    } catch (err) {
      setError(err.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  }, [userName]);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  if (loading) return <div className="p-6 text-sm text-gray-500">Loading…</div>;

  if (error) {
    return (
      <div className="p-6 text-center text-sm text-gray-500">
        <p className="mb-2">Couldn't load profile. {error}</p>
        <button
          onClick={fetchUser}
          className="px-3 py-1 text-xs bg-indigo-600 text-white rounded hover:bg-indigo-700"
        >
          Retry
        </button>
      </div>
    );
  }

  if (!user) return null;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Profile</h1>
      <p>Username: {user.userName}</p>
      <p>Name: {user.firstName} {user.lastName}</p>
      <p>Rating: {user.rating}</p>
    </div>
  );
}
