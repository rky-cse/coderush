// features/questionSlice.js
import { createSlice } from '@reduxjs/toolkit';

const questionSlice = createSlice({
  name: 'question',
  initialState: {
    data: null,
  },
  reducers: {
    setQuestion: (state, action) => {
      state.data = action.payload;
    },
    clearQuestion: (state) => {
      state.data = null;
    },
  },
});

export const { setQuestion, clearQuestion } = questionSlice.actions;
export default questionSlice.reducer;