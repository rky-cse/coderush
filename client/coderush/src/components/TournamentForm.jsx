import React from 'react';

const TournamentForm = ({
  tournamentId,
  setTournamentId,
  onJoinTournament,
  onStartTournament,
  isStartButtonDisabled,
}) => {
  return (
    <div>
      <input
        type="text"
        placeholder="Enter Tournament ID"
        value={tournamentId}
        onChange={(e) => setTournamentId(e.target.value)}
        className="w-full p-2 border border-gray-300 rounded mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <button
        onClick={onJoinTournament}
        className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 mb-4"
      >
        Join Tournament
      </button>
      <button
        onClick={onStartTournament}
        disabled={isStartButtonDisabled}
        className={`w-full py-2 rounded text-white ${
          isStartButtonDisabled ? 'bg-gray-400' : 'bg-green-500 hover:bg-green-600'
        }`}
      >
        Start Tournament
      </button>
    </div>
  );
};

export default TournamentForm;
