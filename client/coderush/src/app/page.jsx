'use client';
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import HomeGrid from '@/components/HomeGrid';
import { getCookie } from 'cookies-next';
export default function HomePage() {
 

  return (
    <>
      <HomeGrid />
      
    </>
  );
}