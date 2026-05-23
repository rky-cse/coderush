'use client';

import React, { useState, useEffect } from 'react';
import api from '@/services/api';
import notify from '@/services/notify';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';
import { FaClock, FaCalendarAlt, FaTrophy, FaUserCheck, FaPlay, FaSignInAlt, FaCheck, FaChevronRight, FaFilter, FaSort } from 'react-icons/fa';

export default function TournamentsPage() {
  const [liveTournaments, setLiveTournaments] = useState([]);
  const [upcomingTournaments, setUpcomingTournaments] = useState([]);
  const [registeredIds, setRegisteredIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedTab, setSelectedTab] = useState('live');
  const [currentTime, setCurrentTime] = useState(Date.now());
  const [sortOrder, setSortOrder] = useState('recent'); // recent or oldest

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

        // Fetch tournaments and registered IDs concurrently. The api singleton
        // attaches the bearer token automatically; no need to pass headers.
        const [liveRes, upcomingRes, registeredRes] = await Promise.all([
          api.get('/api/tournament/mtm/getLiveMTMTournaments'),
          api.get('/api/tournament/mtm/getUpcomingMTMTournaments'),
          token ? api.get('/api/tournament/mtm/registeredTournamentsByUser') : { data: [] },
        ]);

        setLiveTournaments(liveRes.data);
        setUpcomingTournaments(upcomingRes.data);
        setRegisteredIds(registeredRes.data || []);
      } catch (err) {
        notify.error(err.message || 'Could not load tournaments.');
        setError(err.message || 'Error fetching tournaments.');
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
    
    if (days > 0) {
      return `${days}d ${hours.toString().padStart(2, '0')}h ${minutes.toString().padStart(2, '0')}m`;
    }
    
    return `${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  // Direct registration handler
  const handleRegister = async (tournament) => {
    try {
      setLoading(true);
      const token = getCookie('token');
      
      if (!token) {
        router.push('/login?redirect=/tournaments');
        return;
      }

      const tournamentId = tournament.tournamentId || tournament.id;

      await api.get(`/api/tournament/mtm/joinTournament/${tournamentId}`);

      setRegisteredIds((prev) => [...prev, tournamentId]);
      notify.success('Registered for the tournament.');
    } catch (err) {
      notify.error(err.message || 'Error registering for tournament.');
    } finally {
      setLoading(false);
    }
  };

  // Format date for display
  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleDateString(undefined, {
      weekday: 'short',
      month: 'short', 
      day: 'numeric',
      hour: '2-digit', 
      minute: '2-digit'
    });
  };

  // Get tournaments and sort them
  const getSortedTournaments = () => {
    const tournaments = selectedTab === 'live' ? liveTournaments : upcomingTournaments;
    return [...tournaments].sort((a, b) => {
      if (sortOrder === 'recent') {
        return b.startTime - a.startTime;
      } else {
        return a.startTime - b.startTime;
      }
    });
  };

  // Determine progress percentage for live tournaments
  const getProgressPercentage = (tournament) => {
    const totalDuration = tournament.durationInSeconds * 1000;
    const elapsed = currentTime - tournament.startTime;
    const percentage = Math.min(100, Math.max(0, (elapsed / totalDuration) * 100));
    return percentage;
  };

  // Get tournament type display name
  const getTournamentTypeDisplay = (type) => {
    switch (type) {
      case 'FREE_STYLE':
        return 'Free Style';
      case 'CLASSIC':
        return 'Classic';
      default:
        return type;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="relative w-16 h-16">
          <div className="absolute top-0 left-0 w-full h-full border-4 border-indigo-200 dark:border-indigo-900 rounded-full"></div>
          <div className="absolute top-0 left-0 w-full h-full border-4 border-transparent border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin"></div>
        </div>
        <p className="mt-4 text-lg text-gray-700 dark:text-gray-300">Loading tournaments...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center p-8 max-w-md">
          <div className="text-red-500 dark:text-red-400 mb-4">
            <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-gray-800 dark:text-gray-200 mb-2">Failed to Load Tournaments</h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  const sortedTournaments = getSortedTournaments();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-4 sm:p-6">
      {/* Success notification */}
      <div 
        id="notification" 
        className="fixed top-4 right-4 z-50 px-4 py-3 rounded-lg bg-green-100 text-green-800 shadow-lg hidden transform transition-all duration-300 max-w-sm"
      >
        Successfully registered for tournament!
      </div>
      
      <div className="max-w-5xl mx-auto">
        {/* Page Header */}
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-gray-800 dark:text-white mb-2">
            Tournaments
          </h1>
          <p className="text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            Explore ongoing and upcoming coding competitions. Register, participate, and showcase your skills!
          </p>
        </div>
        
        {/* Tab Navigation */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 space-y-4 sm:space-y-0">
          <div className="inline-flex rounded-md shadow-sm">
            <button
              onClick={() => setSelectedTab('live')}
              className={`relative px-4 py-2 text-sm font-medium rounded-l-lg ${
                selectedTab === 'live'
                  ? 'bg-indigo-600 text-white hover:bg-indigo-700'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700'
              } focus:z-10 focus:outline-none focus:ring-2 focus:ring-indigo-500`}
            >
              <span className="flex items-center">
                <FaPlay className="mr-1.5 text-xs" />
                Live 
                {liveTournaments.length > 0 && (
                  <span className="ml-1.5 bg-white dark:bg-gray-700 text-indigo-600 dark:text-indigo-300 text-xs px-1.5 py-0.5 rounded-full">
                    {liveTournaments.length}
                  </span>
                )}
              </span>
            </button>
            <button
              onClick={() => setSelectedTab('upcoming')}
              className={`relative px-4 py-2 text-sm font-medium rounded-r-lg ${
                selectedTab === 'upcoming'
                  ? 'bg-indigo-600 text-white hover:bg-indigo-700'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700'
              } focus:z-10 focus:outline-none focus:ring-2 focus:ring-indigo-500`}
            >
              <span className="flex items-center">
                <FaCalendarAlt className="mr-1.5 text-xs" />
                Upcoming
                {upcomingTournaments.length > 0 && (
                  <span className="ml-1.5 bg-white dark:bg-gray-700 text-indigo-600 dark:text-indigo-300 text-xs px-1.5 py-0.5 rounded-full">
                    {upcomingTournaments.length}
                  </span>
                )}
              </span>
            </button>
          </div>
          
          {/* Sort Controls */}
          <div className="flex items-center">
            <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
              <FaSort className="mr-1.5" />
              <span>Sort:</span>
              <button
                onClick={() => setSortOrder(sortOrder === 'recent' ? 'oldest' : 'recent')}
                className="ml-2 px-3 py-1 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-indigo-500"
              >
                {sortOrder === 'recent' ? 'Recent First' : 'Oldest First'}
              </button>
            </div>
          </div>
        </div>
        
        {/* Tournaments List */}
        {sortedTournaments.length > 0 ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {sortedTournaments.map((tournament) => {
              const tournamentId = tournament.tournamentId || tournament.id;
              const isRegistered = registeredIds.includes(tournamentId);
              
              // Calculate remaining time
              const isLive = selectedTab === 'live';
              const remainingMs = isLive
                ? (tournament.startTime + tournament.durationInSeconds * 1000) - currentTime
                : tournament.startTime - currentTime;
              
              // Determine card status colors
              let statusColor = isLive ? 'emerald' : 'blue';
              let statusText = isLive ? 'Live Now' : 'Upcoming';
              
              if (isLive && remainingMs < 3600000) { // Less than 1 hour remaining
                statusColor = 'amber';
              }
              
              if (isLive && remainingMs <= 0) {
                statusColor = 'gray';
                statusText = 'Ended';
              }
              
              return (
                <div 
                  key={tournamentId}
                  className="bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-shadow border border-gray-100 dark:border-gray-700 overflow-hidden"
                >
                  {/* Card Header with Status */}
                  <div className="px-5 pt-5 pb-3 relative">
                    {isRegistered && (
                      <div className="absolute top-4 right-4">
                        <span className="flex items-center text-xs font-medium text-indigo-800 dark:text-indigo-300 bg-indigo-100 dark:bg-indigo-900/30 rounded-full px-2.5 py-0.5">
                          <FaUserCheck className="mr-1" /> Registered
                        </span>
                      </div>
                    )}
                    
                    <div className="flex items-center mb-1.5">
                      <span className={`inline-flex items-center text-xs font-medium rounded-full px-2 py-0.5 bg-${statusColor}-100 dark:bg-${statusColor}-900/30 text-${statusColor}-800 dark:text-${statusColor}-300`}>
                        <span className={`w-2 h-2 mr-1 rounded-full ${isLive && remainingMs > 0 ? 'animate-pulse' : ''} bg-${statusColor}-500 dark:bg-${statusColor}-400`}></span>
                        {statusText}
                      </span>
                    </div>
                    
                    <h2 className="text-lg font-semibold text-gray-900 dark:text-white hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors">
                      <button
                        onClick={() => router.push(`/tournament/${tournamentId}`)}
                        className="hover:underline focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 rounded"
                      >
                        {tournament.name}
                      </button>
                    </h2>
                    
                    <div className="mt-1 text-sm text-gray-500 dark:text-gray-400 line-clamp-2">
                      {tournament.description || "No description available."}
                    </div>
                  </div>
                  
                  {/* Progress Bar for Live Tournaments */}
                  {isLive && remainingMs > 0 && (
                    <div className="px-5">
                      <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-1.5">
                        <div 
                          className="bg-indigo-600 dark:bg-indigo-500 h-1.5 rounded-full" 
                          style={{ width: `${getProgressPercentage(tournament)}%` }}
                        ></div>
                      </div>
                    </div>
                  )}
                  
                  {/* Tournament Details */}
                  <div className="px-5 py-3 bg-gray-50 dark:bg-gray-800/60 space-y-2">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <FaCalendarAlt className="text-gray-400 dark:text-gray-500 mr-1.5 flex-shrink-0" />
                        <span>{formatDate(tournament.startTime)}</span>
                      </div>
                      
                      <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <FaTrophy className="text-gray-400 dark:text-gray-500 mr-1.5 flex-shrink-0" />
                        <span>{getTournamentTypeDisplay(tournament.tournamentType)}</span>
                      </div>
                    </div>
                    
                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                      <FaClock className="text-gray-400 dark:text-gray-500 mr-1.5 flex-shrink-0" />
                      <span className="font-medium">
                        {isLive
                          ? remainingMs > 0 ? `Ends in: ${formatTimeRemaining(remainingMs)}` : 'Tournament has ended'
                          : `Starts in: ${formatTimeRemaining(remainingMs)}`}
                      </span>
                    </div>
                  </div>
                  
                  {/* Card Actions */}
                  <div className="px-5 py-3 bg-gray-50 dark:bg-gray-800/90 border-t border-gray-100 dark:border-gray-700 flex justify-between items-center">
                    <button
                      onClick={() => router.push(`/tournament/${tournamentId}`)}
                      className="text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium flex items-center focus:outline-none focus:underline"
                    >
                      View Details
                      <FaChevronRight className="ml-1 text-xs" />
                    </button>
                    
                    {isLive && remainingMs > 0 ? (
                      isRegistered ? (
                        <button
                          onClick={() => router.push(`/tournamentPage/${tournamentId}`)}
                          className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md text-white bg-emerald-600 hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-emerald-500"
                        >
                          <FaPlay className="mr-1.5 text-xs" />
                          Start
                        </button>
                      ) : (
                        <button
                          onClick={() => handleRegister(tournament)}
                          className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-indigo-500"
                          disabled={loading}
                        >
                          <FaSignInAlt className="mr-1.5 text-xs" />
                          {loading ? 'Registering...' : 'Register'}
                        </button>
                      )
                    ) : remainingMs > 0 ? (
                      isRegistered ? (
                        <span className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700">
                          <FaCheck className="mr-1.5 text-xs" />
                          Registered
                        </span>
                      ) : (
                        <button
                          onClick={() => handleRegister(tournament)}
                          className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-indigo-500"
                          disabled={loading}
                        >
                          <FaSignInAlt className="mr-1.5 text-xs" />
                          {loading ? 'Registering...' : 'Register'}
                        </button>
                      )
                    ) : (
                      <span className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-gray-700">
                        Ended
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center p-10 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-100 dark:border-gray-700">
            <div className="text-gray-400 dark:text-gray-500 mb-4">
              <svg className="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M8 16l2.879-2.879m0 0a3 3 0 104.243-4.242 3 3 0 00-4.243 4.242zM21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-800 dark:text-gray-200 mb-1">
              {selectedTab === 'live' 
                ? 'No Live Tournaments Currently' 
                : 'No Upcoming Tournaments Available'}
            </h3>
            <p className="text-gray-600 dark:text-gray-400 text-center max-w-sm mb-4">
              {selectedTab === 'live'
                ? 'There are no tournaments running at the moment. Check back later or view upcoming tournaments.'
                : 'No tournaments are scheduled yet. Check back later for new tournament announcements.'}
            </p>
            {selectedTab === 'live' && upcomingTournaments.length > 0 && (
              <button
                onClick={() => setSelectedTab('upcoming')}
                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
              >
                View Upcoming Tournaments
              </button>
            )}
            {selectedTab === 'upcoming' && liveTournaments.length > 0 && (
              <button
                onClick={() => setSelectedTab('live')}
                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
              >
                View Live Tournaments
              </button>
            )}
          </div>
        )}
        
        {/* Show count at bottom */}
        <div className="mt-6 text-center text-sm text-gray-600 dark:text-gray-400">
          Showing {sortedTournaments.length} {selectedTab === 'live' ? 'live' : 'upcoming'} tournament{sortedTournaments.length !== 1 ? 's' : ''}
        </div>
      </div>
    </div>
  );
}