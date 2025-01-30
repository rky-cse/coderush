'use client';
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import HomeGrid from '@/components/HomeGrid';

export default function HomePage() {
  const dispatch = useDispatch();
  const count = useSelector((state) => state.example.value);

  return (
    <div>
      <HomeGrid />
      
    </div>
  );
}