import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  tournamentEndTime: null, // Stores end time in milliseconds
};

const tournamentEndTimeSlice = createSlice({
  name: "tournamentEndTime",
  initialState,
  reducers: {
    setTournamentEndTime: (state, action) => {
      state.tournamentEndTime = action.payload; // Update tournament end time
    },
  },
});

export const { setTournamentEndTime } = tournamentEndTimeSlice.actions;
export const selectTournamentEndTime = (state) => state.tournamentEndTime.tournamentEndTime;

export default tournamentEndTimeSlice.reducer;
