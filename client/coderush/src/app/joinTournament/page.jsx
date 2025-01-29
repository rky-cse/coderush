"use client";

import React from 'react';
import TournamentForm from '@/components/TournamentForm';
import { useTournament } from '@/hooks/useTournament';

const JoinTournament = () => {
  const {
    tournamentId,
    setTournamentId,
    timer,
    isStartButtonDisabled,
    handleJoinTournament,
    handleStartTournament,
    tournamentData,
    questionData,
    error,
  } = useTournament();

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="max-w-md w-full p-6 bg-white shadow-md rounded-xl">
        <h1 className="text-3xl font-semibold mb-6 text-center text-blue-700">
          Join Tournament
        </h1>

        <TournamentForm
          tournamentId={tournamentId}
          setTournamentId={setTournamentId}
          onJoinTournament={handleJoinTournament}
          isStartButtonDisabled={isStartButtonDisabled}
          onStartTournament={handleStartTournament}
        />

        {timer && (
          <div className="mt-4 text-center">
            <p className="text-lg font-bold">Starts In:</p>
            <p className="text-xl text-indigo-700 font-mono">{timer}</p>
          </div>
        )}

        {error && (
          <div className="mt-4 p-4 bg-red-50 text-red-700 rounded">
            {error}
          </div>
        )}

        {tournamentData && (
          <pre className="mt-4 bg-gray-200 p-3 rounded text-sm">
            {JSON.stringify(tournamentData, null, 2)}
          </pre>
        )}

        {questionData && (
          <pre className="mt-4 bg-green-200 p-3 rounded text-sm">
            {JSON.stringify(questionData, null, 2)}
          </pre>
        )}
      </div>
    </div>
  );
};

export default JoinTournament;
