'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';

export default function TournamentsPage() {
  const [liveTournaments, setLiveTournaments] = useState([]);
  const [upcomingTournaments, setUpcomingTournaments] = useState([]);
  const [registeredIds, setRegisteredIds] = useState([]); // list of tournament IDs the user is registered in
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedTab, setSelectedTab] = useState('live');
  const [currentTime, setCurrentTime] = useState(Date.now());

  const router = useRouter();
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  // Update current time every second
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(Date.now());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = getCookie('token');
        const headers = token ? { Authorization: `Bearer ${token}` } : {};

        // Fetch tournaments and registered IDs concurrently
        const [liveRes, upcomingRes, registeredRes] = await Promise.all([
          axios.get(`${baseUrl}/api/tournament/mtm/getLiveMTMTournaments`, { headers }),
          axios.get(`${baseUrl}/api/tournament/mtm/getUpcomingMTMTournaments`, { headers }),
          axios.get(`${baseUrl}/api/tournament/mtm/registeredTournamentsByUser`, { headers }),
        ]);

        setLiveTournaments(liveRes.data);
        setUpcomingTournaments(upcomingRes.data);
        setRegisteredIds(registeredRes.data); // assuming an array of IDs is returned
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Error fetching tournaments.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [baseUrl]);

  // Helper: format time remaining (ms) to "dd:hh:mm:ss"
  const formatTimeRemaining = (ms) => {
    if (ms <= 0) return '00:00:00';
    const totalSeconds = Math.floor(ms / 1000);
    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${days > 0 ? days + 'd ' : ''}${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  // Direct registration handler (no modal for register)
  const handleRegister = async (tournament) => {
    try {
      const token = getCookie('token');
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      const tournamentId = tournament.tournamentId || tournament.id;
      await axios.get(`${baseUrl}/api/tournament/mtm/joinTournament/${tournamentId}`, { headers });
      alert("Registration successful!");
      // Optionally update the registeredIds state to include this tournament ID
      setRegisteredIds((prev) => [...prev, tournamentId]);
    } catch (err) {
      console.error('Error registering for tournament:', err);
      alert("Error registering for tournament.");
    }
  };

  // Date conversion options for local display including time zone
  const dateOptions = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit',
    timeZoneName: 'short'
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-lg">Loading tournaments...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-lg text-red-500">{error}</p>
      </div>
    );
  }

  // Choose tournaments based on selected tab and sort (most recent on top)
  const tournaments = selectedTab === 'live' ? liveTournaments : upcomingTournaments;
  const sortedTournaments = [...tournaments].sort((a, b) => b.startTime - a.startTime);

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-4xl mx-auto bg-white shadow rounded-lg p-6">
        <h1 className="text-2xl font-bold mb-4 text-center">Tournaments</h1>
        <div className="flex justify-center mb-6">
          <button
            onClick={() => setSelectedTab('live')}
            className={`px-4 py-2 mr-2 rounded transition-colors ${
              selectedTab === 'live'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
            }`}
          >
            Live Tournaments
          </button>
          <button
            onClick={() => setSelectedTab('upcoming')}
            className={`px-4 py-2 rounded transition-colors ${
              selectedTab === 'upcoming'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
            }`}
          >
            Upcoming Tournaments
          </button>
        </div>
        {sortedTournaments.length > 0 ? (
          <ul className="divide-y divide-gray-200">
            {sortedTournaments.map((tournament) => {
              const tournamentId = tournament.tournamentId || tournament.id;
              const isRegistered = registeredIds.includes(tournamentId);
              // Calculate remaining time
              const remainingMs =
                selectedTab === 'live'
                  ? (tournament.startTime + tournament.durationInSeconds * 1000) - currentTime
                  : tournament.startTime - currentTime;
              return (
                <li
                  key={tournamentId}
                  className="py-4 flex flex-col md:flex-row md:justify-between md:items-center"
                >
                  <div className="flex-1">
                    <button
                      onClick={() => router.push(`/tournament/${tournamentId}`)}
                      className="text-lg font-semibold text-blue-600 hover:underline"
                    >
                      {tournament.name}
                    </button>
                    <p className="text-sm text-gray-600">
                      Starts at: {new Date(tournament.startTime).toLocaleString(undefined, dateOptions)}
                    </p>
                    <p className="text-sm text-gray-500 mt-1">{tournament.description}</p>
                    <p className="text-sm text-gray-500 mt-1">
                      <span className="font-medium">Type:</span> {tournament.tournamentType}
                    </p>
                    <p className="text-sm text-gray-500 mt-1">
                      {selectedTab === 'live'
                        ? `Ends in: ${formatTimeRemaining(remainingMs)}`
                        : `Starts in: ${formatTimeRemaining(remainingMs)}`}
                    </p>
                  </div>
                  <div className="mt-4 md:mt-0 flex space-x-2">
                    {selectedTab === 'live' ? (
                      isRegistered ? (
                        <button
                          onClick={() => router.push(`/tournamentPage/${tournamentId}`)}
                          className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors"
                        >
                          Start Tournament
                        </button>
                      ) : (
                        <button
                          onClick={() => handleRegister(tournament)}
                          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                        >
                          Register
                        </button>
                      )
                    ) : (
                      // For upcoming tournaments: show "Registered" if already registered; else show Register button.
                      isRegistered ? (
                        <span className="px-4 py-2 bg-gray-300 text-gray-800 rounded">
                          Registered
                        </span>
                      ) : (
                        <button
                          onClick={() => handleRegister(tournament)}
                          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                        >
                          Register
                        </button>
                      )
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="text-center text-gray-600">
            {selectedTab === 'live'
              ? 'No live tournaments at the moment.'
              : 'No upcoming tournaments available.'}
          </p>
        )}
      </div>
    </div>
  );
}
