'use client';
import { configureStore } from '@reduxjs/toolkit';
import exampleReducer from './slices/exampleSlice';
import editorReducer from './slices/codeSlice';

export const store = configureStore({
  reducer: {
    example: exampleReducer,
    editor: editorReducer,
  },
});

export default store;
