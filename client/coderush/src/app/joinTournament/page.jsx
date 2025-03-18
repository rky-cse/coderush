'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import { useDispatch } from 'react-redux';
import { setTournamentData } from '@/redux/slices/tournamentSlice';
import { setTournamentEndTime } from '@/redux/slices/tournamentEndTimeSlice';

const JoinTournamentPage = () => {
  const [tournamentId, setTournamentId] = useState('');
  const [tournamentData, setTournamentDataLocal] = useState(null);
  const [startTime, setStartTime] = useState(null);
  const [timeLeft, setTimeLeft] = useState({ days: 0, hours: 0, minutes: 0, seconds: 0 });
  const dispatch = useDispatch();
  const router = useRouter();

  useEffect(() => {
    if (!tournamentData || !startTime) return;

    const updateCountdown = () => {
      const now = Date.now();
      const diff = startTime - now;

      if (diff <= 0) {
        setTimeLeft({ days: 0, hours: 0, minutes: 0, seconds: 0 });
      } else {
        setTimeLeft({
          days: Math.floor(diff / (1000 * 60 * 60 * 24)),
          hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
          minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
          seconds: Math.floor((diff % (1000 * 60)) / 1000),
        });
      }
    };

    updateCountdown();
    const timer = setInterval(updateCountdown, 1000);
    return () => clearInterval(timer);
  }, [tournamentData, startTime]);

  const handleJoinTournament = async () => {
    const token = typeof window !== 'undefined' ? getCookie('token') : null;

    if (!tournamentId || !token) {
      alert('Please enter Tournament ID and ensure you are logged in.');
      return;
    }

    try {
      const response = await axios.get(
        `${process.env.NEXT_PUBLIC_API_URL}/api/tournament/mtm/joinTournament/${tournamentId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      // Store data in local component state
      setTournamentDataLocal(response.data);
      setStartTime(response.data.tournament.startTime);

      // Store tournament data in Redux
      dispatch(setTournamentData(response.data));
      console.log("yahi hai"+response.data);
      dispatch(
        setTournamentEndTime(
          response.data.tournament.startTime +
            response.data.tournament.durationInSeconds * 1000
        )
      );
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
          <p>
            <strong>Name:</strong> {tournamentData.tournament.name}
          </p>
          <p>
            <strong>Description:</strong> {tournamentData.tournament.description}
          </p>
          <p>
            <strong>Creator:</strong> {tournamentData.tournament.creatorUserName}
          </p>
          <p>
            <strong>Start Time:</strong>{' '}
            {new Date(tournamentData.tournament.startTime).toLocaleString()}
          </p>
          <p>
            <strong>Rated:</strong> {tournamentData.tournament.rated ? 'Yes' : 'No'}
          </p>
          <p>
            <strong>Min Rating:</strong> {tournamentData.tournament.minRatingReq}
          </p>
          <p>
            <strong>Max Rating:</strong> {tournamentData.tournament.maxRatingReq}
          </p>
          <p>
            <strong>Duration:</strong>{' '}
            {Math.floor(tournamentData.tournament.durationInSeconds / 60)} minutes
          </p>

          <h3 className="mt-4 font-semibold">Participants</h3>
          <ul>
            {tournamentData.tournamentPlayerList.map((player) => (
              <li key={player.id}>{player.playerUserName}</li>
            ))}
          </ul>

          <div className="mt-4 flex space-x-2">
            <p className="font-semibold">You can Start the Tournament in:</p>
            {Object.entries(timeLeft).map(([key, value]) => (
              <span
                key={key}
                className="px-2 py-1 bg-blue-500 text-white rounded-md text-xs font-semibold flex items-center justify-center w-8 h-8"
              >
                {value}
                <span className="ml-1 text-[10px] text-gray-200">{key[0]}</span>
              </span>
            ))}
          </div>

          <button
            className="mt-4 w-full bg-green-600 text-white py-2 rounded-md disabled:bg-gray-400"
            onClick={handleStartTournament}
            disabled={
              startTime - Date.now() > 0 ||
              startTime +
                tournamentData.tournament.durationInSeconds * 1000 -
                Date.now() <=
                0
            }
          >
            {startTime +
              tournamentData.tournament.durationInSeconds * 1000 -
              Date.now() <=
            0
              ? 'Tournament Ended'
              : startTime - Date.now() <= 0
              ? 'Start Tournament'
              : 'Waiting for Start Time'}
          </button>
        </div>
      )}
    </div>
  );
};

export default JoinTournamentPage;
