import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  tournamentData: null,
};

const tournamentSlice = createSlice({
  name: 'tournament',
  initialState,
  reducers: {
    setTournamentData: (state, action) => {
      state.tournamentData = action.payload;
    },
    clearTournamentData: (state) => {
      state.tournamentData = null;
    },
  },
});

export const { setTournamentData, clearTournamentData } = tournamentSlice.actions;
export default tournamentSlice.reducer;
