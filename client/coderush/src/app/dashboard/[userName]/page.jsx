'use client';
import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import axios from 'axios';
import { getCookie } from 'cookies-next';

export default function Profile() {
  const { userName } = useParams(); // Corrected useParams usage
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (!userName) return; // Ensure userName exists before making the request

    const fetchUser = async () => {
      try {
        const token = getCookie('token');
        console.log('Token:', token);

        if (!token) {
          console.warn('No token found, redirecting to login...');
          return;
        }

        const response = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/user/${userName}`, {
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
  }, [userName]); // Added dependency to useEffect

  if (!user) return <div>Loading...</div>;

  return (
    <div>
      <h1>Profile</h1>
      <p>Username: {user.userName}</p> {/* Ensure correct property access */}
      <p>Name: {user.firstName} {user.lastName}</p>
      <p>Rating: {user.rating}</p>
    </div>
  );
}
