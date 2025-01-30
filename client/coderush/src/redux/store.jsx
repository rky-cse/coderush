'use client';
import { configureStore } from '@reduxjs/toolkit';
import indexReducer from './slices/indexSlice';
import editorReducer from './slices/codeSlice';
import questionReducer from './slices/questionSlice';
import testcaseReducer from './slices/testcaseSlice';
import websocketReducer from './slices/websocketSlice';

export const store = configureStore({
  reducer: {
    index: indexReducer,
    editor: editorReducer,
    question: questionReducer,
    testcase: testcaseReducer,
    websocket: websocketReducer,

  },
});

export default store;
