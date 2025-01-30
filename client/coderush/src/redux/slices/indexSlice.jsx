import { createSlice } from '@reduxjs/toolkit';

const indexSlice = createSlice({
  name: 'index',
  initialState: 0, // Initial index value
  reducers: {
    increment: (state) => Math.min(state + 1,4),
    decrement: (state) => Math.max(state - 1, 0),
  },
});

export const { increment, decrement } = indexSlice.actions;
export default indexSlice.reducer;