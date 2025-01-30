// features/testcaseSlice.js
import { createSlice } from '@reduxjs/toolkit';

const testcaseSlice = createSlice({
  name: 'testcase',
  initialState: {
    data: null,
  },
  reducers: {
    setTestcase: (state, action) => {
      state.data = action.payload;
    },
    clearTestcase: (state) => {
      state.data = null;
    },
  },
});

export const { setTestcase, clearTestcase } = testcaseSlice.actions;
export default testcaseSlice.reducer;