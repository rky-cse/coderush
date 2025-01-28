'use client';
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { increment, decrement } from '../redux/slices/exampleSlice';
import HomeGrid from '@/components/HomeGrid';

export default function HomePage() {
  const dispatch = useDispatch();
  const count = useSelector((state) => state.example.value);

  return (
    <div>
      <HomeGrid />
      <div className="mt-8">
        <h3>Redux Example</h3>
        <p>Count: {count}</p>
        <button
          onClick={() => dispatch(increment())}
          className="bg-blue-500 text-white px-4 py-2 rounded mr-2"
        >
          Increment
        </button>
        <button
          onClick={() => dispatch(decrement())}
          className="bg-red-500 text-white px-4 py-2 rounded"
        >
          Decrement
        </button>
      </div>
    </div>
  );
}