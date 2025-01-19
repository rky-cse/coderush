'use client'
import React from 'react';

// Adjust the import path as necessary

import TournamentFormModal from '@components/TournamentFormModal';

const CreateTournament = () => {
  const handleFormSubmit = (tournamentData) => {
    // Handle form submission logic here
    console.log('Tournament Created:', tournamentData);
  };

  return (
    <div>
      <h1>Create Tournament</h1>
      <TournamentFormModal onSubmit={handleFormSubmit} />
    </div>
  );
};

export default CreateTournament;
