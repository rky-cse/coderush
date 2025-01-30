'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';

const JoinTournament = () => {
  const [tournamentId, setTournamentId] = useState('');
  const [tournamentData, setTournamentData] = useState(null);
  const [remainingTime, setRemainingTime] = useState(null);
  const [isTimerExpired, setIsTimerExpired] = useState(false);
  const router = useRouter();

  useEffect(() => {
    let timer;
    if (remainingTime !== null && remainingTime > 0) {
      timer = setInterval(() => {
        setRemainingTime((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            setIsTimerExpired(true);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else if (remainingTime === 0) {
      setIsTimerExpired(true);
    }
    return () => clearInterval(timer);
  }, [remainingTime]);

  const handleJoinTournament = async () => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    if (!tournamentId || !token) {
      alert('Please enter Tournament ID and ensure you are logged in.');
      return;
    }

    try {
      const response = await axios.get(`http://localhost:8080/api/tournament/joinTournament/${tournamentId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setTournamentData(response.data);
      const startTime = new Date(response.data.tournament.startTime).getTime();
      const currentTime = Date.now();
      const timeDiff = Math.max(0, Math.floor((startTime - currentTime) / 1000));
      setRemainingTime(timeDiff);
    } catch (error) {
      alert('Failed to join tournament. Please check the ID or your network connection.');
    }
  };

  const handleStartTournament = () => {
    router.push(`/tournamentPage/${tournamentId}`);
  };

  return (
    <div className="min-h-screen p-4 bg-gray-50 flex flex-col items-center">
      <h1 className="text-2xl font-bold mb-4">Join Tournament</h1>
      <div className="w-full max-w-md mb-6">
        <input
          type="text"
          placeholder="Enter Tournament ID"
          value={tournamentId}
          onChange={(e) => setTournamentId(e.target.value)}
          className="w-full p-2 border rounded-md"
        />
        <button
          className="mt-4 w-full bg-blue-600 text-white py-2 rounded-md"
          onClick={handleJoinTournament}
        >
          Join Tournament
        </button>
      </div>

      {tournamentData && (
        <div className="w-full max-w-lg p-4 shadow-lg rounded-xl bg-white">
          <h2 className="text-xl font-semibold">Tournament Details</h2>
          <p><strong>Name:</strong> {tournamentData.tournament.name}</p>
          <p><strong>Description:</strong> {tournamentData.tournament.description}</p>
          <p><strong>Creator:</strong> {tournamentData.tournament.creatorUserName}</p>
          <p><strong>Start Time:</strong> {new Date(tournamentData.tournament.startTime).toLocaleString()}</p>
          <p><strong>Rated:</strong> {tournamentData.tournament.rated ? 'Yes' : 'No'}</p>
          <p><strong>Min Rating:</strong> {tournamentData.tournament.minRatingReq}</p>
          <p><strong>Max Rating:</strong> {tournamentData.tournament.maxRatingReq}</p>
          <p><strong>Duration:</strong> {Math.floor(tournamentData.tournament.durationInSeconds / 60)} minutes</p>
          
          <h3 className="mt-4 font-semibold">Participants</h3>
          <ul>
            {tournamentData.tournamentPlayerList.map(player => (
              <li key={player.id}>{player.playerUserName}</li>
            ))}
          </ul>

          <div className="mt-4">
            {remainingTime !== null && (
              <p className="text-lg">
                Time Remaining: {`${Math.floor(remainingTime / 60)}m ${remainingTime % 60}s`}
              </p>
            )}
          </div>

          <button
            className="mt-4 w-full bg-green-600 text-white py-2 rounded-md disabled:bg-gray-400"
            onClick={handleStartTournament}
            disabled={!isTimerExpired}
          >
            {isTimerExpired ? 'Start Tournament' : 'Waiting for Start Time'}
          </button>
        </div>
      )}
    </div>
  );
};

export default JoinTournament;