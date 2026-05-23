'use client';
import React, { useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';
import { setUser, clearUser } from '@/redux/slices/userSlice';
import HomeGrid from '@/components/HomeGrid';
import { getCookie } from 'cookies-next';
import api from '@/services/api';

export default function HomePage() {
  const dispatch = useDispatch();
  const [token] = useState(getCookie('token'));

  useEffect(() => {
    if (!token) {
      dispatch(clearUser());
      return;
    }

    api
      .get('/api/user/me')
      .then((res) => dispatch(setUser(res.data)))
      .catch(() => dispatch(clearUser()));
  }, [token, dispatch]);

  return <HomeGrid />;
}
