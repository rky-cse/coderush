'use client';
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setUser, clearUser } from '@/redux/slices/userSlice';
import HomeGrid from '@/components/HomeGrid';
import { getCookie } from 'cookies-next';
import axios from 'axios';

export default function HomePage() {
  const dispatch = useDispatch();
  const [token, setToken] = useState(getCookie('token'));
  
  useEffect(() => {
    if (!token) {
      dispatch(clearUser());
      return;
    }

    console.log("Idhar dekh bc run ho rha hai");
    axios
      .get(`${process.env.NEXT_PUBLIC_API_URL}/api/user/me`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        console.log("Setting data in userSlice bc", res.data);
        dispatch(setUser(res.data));
      })
      .catch((err) => {
        console.error(err);
        dispatch(clearUser());
      });
  }, [token, dispatch]);

  return (
    <>
      <HomeGrid />
    </>
  );
}
