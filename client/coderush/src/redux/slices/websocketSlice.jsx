// slices/websocketSlice.js
import { createSlice } from '@reduxjs/toolkit';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const initialState = {
  stompClient: null,
  isConnected: false,
  error: null,
};

const websocketSlice = createSlice({
  name: 'websocket',
  initialState,
  reducers: {
    setStompClient: (state, action) => {
      state.stompClient = action.payload;
    },
    setConnected: (state, action) => {
      state.isConnected = action.payload;
    },
    setError: (state, action) => {
      state.error = action.payload;
    },
  },
});

export const { setStompClient, setConnected, setError } = websocketSlice.actions;
export default websocketSlice.reducer;