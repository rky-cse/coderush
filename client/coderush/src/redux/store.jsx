'use client';
import { configureStore } from '@reduxjs/toolkit';
import indexReducer from './slices/indexSlice';
import editorReducer from './slices/codeSlice';
import questionReducer from './slices/questionSlice';
import testcaseReducer from './slices/testcaseSlice';
import websocketReducer from './slices/websocketSlice';
import authReducer from './slices/authSlice';
import tournamentEndTimeReducer from './slices/tournamentEndTimeSlice';


export const store = configureStore({
  reducer: {
    auth: authReducer,
    index: indexReducer,
    editor: editorReducer,
    question: questionReducer,
    testcase: testcaseReducer,
    websocket: websocketReducer,
    tournamentEndTime: tournamentEndTimeReducer,

  },
});

export default store;
