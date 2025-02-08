import { createSlice } from '@reduxjs/toolkit';
import { setCookie, getCookie, deleteCookie } from 'cookies-next';  

const initialState = {
  user: null,
  token: null,
};

// Function to get cookies manually (fallback)
const getCookieValue = (name) => {
    const cookies = document.cookie.split('; ');
    for (let cookie of cookies) {
      const [key, value] = cookie.split('=');
      if (key === name) return decodeURIComponent(value);
    }
    return null;
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    login: (state, action) => {
      state.user = action.payload.user;
      state.token = action.payload.token;

      // Store token & username in cookies
      setCookie('token', action.payload.token, { path: '/' });
      setCookie('username', action.payload.user, { path: '/' });
    },
    
    logout: (state) => {
      state.user = null;
      state.token = null;

      // Remove token & username from cookies
      deleteCookie('token');
      deleteCookie('username');
    },
    
    setAuthFromStorage: (state) => {
      // Fetch from cookies
      const token = getCookie('token') || getCookieValue('token');
      const username = getCookie('username') || getCookieValue('username');

      if (token && username) {
        state.user = username;
        state.token = token;
      }
    },
  },
});

export const { login, logout, setAuthFromStorage } = authSlice.actions;
export default authSlice.reducer;
