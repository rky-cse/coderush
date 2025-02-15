'use client';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';

export default function Profile() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const token = getCookie('token'); // Get token from cookies
        console.log('Token:', token);

        if (!token) {
          console.warn('No token found, redirecting to login...');
          return;
        }

        const response = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/user/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        console.log('Response:', response.data);
        setUser(response.data);
      } catch (error) {
        console.error('Error fetching user:', error);
        if (error.response) {
          console.error('Status:', error.response.status);
          console.error('Response data:', error.response.data);
        } else if (error.request) {
          console.error('No response received:', error.request);
        }
      }
    };

    fetchUser();
  }, []);

  if (!user) return <div>Loading...</div>;

  return (
    <div>
      {/* console.log(user) */}
      <h1>Profile</h1>
      <p>Username: {user}</p>
    </div>
  );
}
