'use client';
import { createSlice } from '@reduxjs/toolkit';

// The shape is based on your server's UserEntity
const initialState = {
  id: null,
  userName: null,
  firstName: null,
  lastName: null,
  email: null,
  roles: [],
  rating: 0,
  isLoggedIn: false,
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    // Set user data from server
    setUser: (state, action) => {
      const {
        id,
        userName,
        firstName,
        lastName,
        email,
        roles,
        rating,
      } = action.payload;

      state.id = id;
      state.userName = userName;
      state.firstName = firstName;
      state.lastName = lastName;
      state.email = email;
      state.roles = roles || [];
      state.rating = rating || 0;
      state.isLoggedIn = true;
    },
    // Clear user data on logout
    clearUser: (state) => {
      state.id = null;
      state.userName = null;
      state.firstName = null;
      state.lastName = null;
      state.email = null;
      state.roles = [];
      state.rating = 0;
      state.isLoggedIn = false;
    },
  },
});

export const { setUser, clearUser } = userSlice.actions;
export default userSlice.reducer;
