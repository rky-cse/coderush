'use client';
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useRouter } from 'next/navigation';
import { getCookie } from 'cookies-next';

const TournamentPage = ({ params: { tournamentId } }) => {
  const router = useRouter();
  const [tournament, setTournament] = useState(null);
  const [registeredTournaments, setRegisteredTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [timeRemaining, setTimeRemaining] = useState(0);
  const token = getCookie('token');

  useEffect(() => {
    const fetchData = async () => {
      if (tournamentId) {
        const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
        const headers = token ? { Authorization: `Bearer ${token}` } : {};
        try {
          const tournamentResponse = await axios.get(
            `${baseUrl}/api/tournament/mtm/getTournamentById/${tournamentId}`,
            { headers }
          );
          const registeredResponse = await axios.get(
            `${baseUrl}/api/tournament/mtm/registeredTournamentsByUser`,
            { headers }
          );
          setTournament(tournamentResponse.data);
          setRegisteredTournaments(registeredResponse.data);
        } catch (err) {
          console.error(err);
          setError(err.message);
        } finally {
          setLoading(false);
        }
      }
    };
    fetchData();
  }, [tournamentId, token]);

  // Setup timer countdown based on tournament.startTime
  useEffect(() => {
    let timerId;
    if (tournament) {
      timerId = setInterval(() => {
        const remaining = tournament.startTime - Date.now();
        setTimeRemaining(remaining > 0 ? remaining : 0);
      }, 1000);
    }
    return () => clearInterval(timerId);
  }, [tournament]);

  const handleRegister = async () => {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const headers = token ? { Authorization: `Bearer ${token}` } : {};
    try {
      await axios.get(
        `${baseUrl}/api/tournament/mtm/joinTournament/${tournament.tournamentId}`,
        { headers }
      );
      alert("Registration successful!");
      // Add the tournament id to the registeredTournaments list.
      setRegisteredTournaments(prev => [...prev, tournament.tournamentId]);
    } catch (err) {
      console.error("Error registering for tournament:", err);
      alert("Error registering for tournament.");
    }
  };

  const formatTime = (ms) => {
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${hours.toString().padStart(2,'0')}:${minutes.toString().padStart(2,'0')}:${seconds.toString().padStart(2,'0')}`;
  };

  if (loading)
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-xl text-gray-700">Loading...</p>
      </div>
    );
  if (error)
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-xl text-red-600">Error: {error}</p>
      </div>
    );
  if (!tournament)
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-xl text-gray-700">Tournament not found</p>
      </div>
    );

  // Define tournament live status:
  // Live if current time is between startTime and (startTime + durationInSeconds * 1000)
  const now = Date.now();
  const tournamentStart = tournament.startTime; // ms value
  const tournamentEnd = tournamentStart + tournament.durationInSeconds * 1000;
  const isLive = now >= tournamentStart && now <= tournamentEnd;

  // Check registration (assuming registeredTournaments is an array of tournament IDs)
  const isRegistered = registeredTournaments.includes(tournament.tournamentId);

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-3xl mx-auto bg-white shadow-md rounded-lg p-6">
        <h1 className="text-3xl font-bold text-gray-800 mb-4">{tournament.name}</h1>
        <p className="text-gray-700 mb-4">{tournament.description}</p>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          <div>
            <p className="font-medium text-gray-600">Start Time:</p>
            <p className="text-gray-800">{new Date(tournament.startTime).toLocaleString()}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Duration:</p>
            <p className="text-gray-800">{tournament.durationInSeconds} seconds</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Visibility:</p>
            <p className="text-gray-800">{tournament.visibility}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Tournament Type:</p>
            <p className="text-gray-800">{tournament.tournamentType}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Penalty Factor:</p>
            <p className="text-gray-800">{tournament.penaltyFactor}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Creator ID:</p>
            <p className="text-gray-800">{tournament.creatorId}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Rated:</p>
            <p className="text-gray-800">{tournament.rated ? 'Yes' : 'No'}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Min Rating Requirement:</p>
            <p className="text-gray-800">{tournament.minRatingReq}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Max Rating Requirement:</p>
            <p className="text-gray-800">{tournament.maxRatingReq}</p>
          </div>
          <div>
            <p className="font-medium text-gray-600">Team Style:</p>
            <p className="text-gray-800">{tournament.teamStyle ? 'Yes' : 'No'}</p>
          </div>
        </div>

        <div className="flex flex-col items-center">
          {isRegistered ? (
            isLive ? (
              <button
                onClick={() => router.push(`/tournamentPage/${tournamentId}`)}
                className="px-6 py-3 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-md transition-colors"
              >
                Start Tournament
              </button>
            ) : (
              <>
                <button
                  disabled
                  className="px-6 py-3 bg-gray-400 text-white font-semibold rounded-md cursor-not-allowed"
                >
                  Registered
                </button>
                {timeRemaining > 0 && (
                  <p className="mt-2 text-gray-600">
                    Tournament starts in: {formatTime(timeRemaining)}
                  </p>
                )}
              </>
            )
          ) : (
            <button
              onClick={handleRegister}
              className="px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-md transition-colors"
            >
              Register
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default TournamentPage;
