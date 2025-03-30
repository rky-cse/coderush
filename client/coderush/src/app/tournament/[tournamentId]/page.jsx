'use client';
import React, { useState, useEffect, use } from 'react';
import axios from 'axios';
import { useRouter } from 'next/navigation';
import { getCookie } from 'cookies-next';
import { 
  FaCalendarAlt, FaClock, FaEye, FaShieldAlt, FaTrophy, 
  FaExclamationTriangle, FaLock, FaStar, FaUsers, FaUser
} from 'react-icons/fa';
import {useDispatch, useSelector } from 'react-redux';
import { setTournamentData } from '@/redux/slices/tournamentSlice';

// Utility function to format time
const formatTime = (ms) => {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${hours.toString().padStart(2,'0')}:${minutes.toString().padStart(2,'0')}:${seconds.toString().padStart(2,'0')}`;
};

// Format duration in a human-readable way
const formatDuration = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  
  if (hours > 0) {
    return `${hours} hour${hours > 1 ? 's' : ''}${minutes > 0 ? ` ${minutes} min` : ''}`;
  }
  return `${minutes} minute${minutes > 1 ? 's' : ''}`;
};

// Format penalty in a human-readable way
const formatPenalty = (seconds) => {
  if (seconds === 0) return "None";
  const minutes = Math.floor(seconds / 60);
  return `${minutes} minute${minutes > 1 ? 's' : ''}`;
};

// Tournament Header component with enhanced styling
const TournamentHeader = ({ name, description, startTime, isRegistered, isLive }) => {
  const formattedDate = new Date(startTime).toLocaleDateString(undefined, { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  });
  
  const formattedTime = new Date(startTime).toLocaleTimeString(undefined, {
    hour: '2-digit',
    minute: '2-digit'
  });
  
  let statusLabel = "Upcoming";
  let statusClass = "bg-blue-100 text-blue-800";
  
  if (isLive) {
    statusLabel = "Live Now";
    statusClass = "bg-green-100 text-green-800 animate-pulse";
  } else if (new Date(startTime) < new Date()) {
    statusLabel = "Ended";
    statusClass = "bg-gray-100 text-gray-800";
  }
  
  return (
    <div className="relative overflow-hidden">
      {/* Background pattern */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-600 to-purple-700"></div>
        <div className="absolute inset-0" style={{ 
          backgroundImage: 'url("data:image/svg+xml,%3Csvg width=\'60\' height=\'60\' viewBox=\'0 0 60 60\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cg fill=\'none\' fill-rule=\'evenodd\'%3E%3Cg fill=\'%23ffffff\' fill-opacity=\'0.2\'%3E%3Cpath d=\'M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z\'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")',
          backgroundSize: '20px'
        }}></div>
      </div>
      
      <div className="relative bg-gradient-to-r from-indigo-600 to-purple-700 text-white px-6 py-8 md:px-10 md:py-12">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between">
          <div className="mb-6 md:mb-0 md:mr-6 md:max-w-2xl">
            <div className="flex items-center mb-3">
              <span className={`text-xs font-medium px-2.5 py-0.5 rounded-full ${statusClass}`}>
                {statusLabel}
              </span>
              {isRegistered && (
                <span className="ml-2 text-xs font-medium px-2.5 py-0.5 rounded-full bg-indigo-200 text-indigo-800">
                  Registered
                </span>
              )}
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight mb-2">{name}</h1>
            <p className="text-indigo-100 text-sm sm:text-base font-light">{description}</p>
          </div>
          
          <div className="flex flex-col space-y-2">
            <div className="flex items-center text-indigo-100">
              <FaCalendarAlt className="mr-2 text-indigo-200" />
              <span className="text-sm">{formattedDate}</span>
            </div>
            <div className="flex items-center text-indigo-100">
              <FaClock className="mr-2 text-indigo-200" />
              <span className="text-sm">{formattedTime}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Info Card component with icons
const InfoCard = ({ label, value, icon: Icon }) => (
  <div className="bg-white dark:bg-gray-800 p-4 rounded-lg border border-gray-100 dark:border-gray-700 hover:shadow-md transition-all duration-200">
    <div className="flex items-start">
      <div className="flex-shrink-0 mr-3">
        <div className="p-2 bg-indigo-50 dark:bg-indigo-900/30 rounded-full">
          <Icon className="text-indigo-500 dark:text-indigo-400" size={16} />
        </div>
      </div>
      <div>
        <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wider font-medium mb-1">{label}</p>
        <p className="text-sm font-medium text-gray-800 dark:text-gray-200">{value}</p>
      </div>
    </div>
  </div>
);

// Tournament information grid with enhanced layout
const TournamentInfoGrid = ({ tournament }) => {
  const infoItems = [
    { 
      label: 'Type', 
      value: tournament.tournamentType === 'FREE_STYLE' ? 'Free Style' : 'Classic',
      icon: FaTrophy
    },
    { 
      label: 'Duration', 
      value: formatDuration(tournament.durationInSeconds),
      icon: FaClock
    },
    { 
      label: 'Visibility', 
      value: tournament.visibility === 'PUBLIC' ? 'Public' : 'Private',
      icon: tournament.visibility === 'PUBLIC' ? FaEye : FaLock
    },
    { 
      label: 'Penalty', 
      value: formatPenalty(tournament.penaltyFactor),
      icon: FaExclamationTriangle
    },
    { 
      label: 'Rating Status', 
      value: tournament.rated ? 'Rated' : 'Unrated',
      icon: FaStar
    },
    { 
      label: 'Team Style', 
      value: tournament.teamStyle ? 'Team Competition' : 'Individual',
      icon: FaUsers
    }
  ];

  // Only show rating requirements if they exist
  if (tournament.minRatingReq > 0 || tournament.maxRatingReq > 0) {
    let ratingText = '';
    if (tournament.minRatingReq > 0 && tournament.maxRatingReq > 0) {
      ratingText = `${tournament.minRatingReq} - ${tournament.maxRatingReq}`;
    } else if (tournament.minRatingReq > 0) {
      ratingText = `Min ${tournament.minRatingReq}`;
    } else if (tournament.maxRatingReq > 0) {
      ratingText = `Max ${tournament.maxRatingReq}`;
    }
    
    if (ratingText) {
      infoItems.push({
        label: 'Rating Requirement',
        value: ratingText,
        icon: FaUser
      });
    }
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {infoItems.map(({ label, value, icon }) => (
        <InfoCard key={label} label={label} value={value} icon={icon} />
      ))}
    </div>
  );
};

// Enhanced tournament action button with status info
const TournamentActionButton = ({ 
  isRegistered, 
  isLive,
  isEnded,
  timeRemaining, 
  onRegister, 
  onStart 
}) => {
  const actionClasses = "w-full py-3 px-4 rounded-lg font-medium transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-offset-2 flex items-center justify-center";

  if (isEnded) {
    return (
      <div className="text-center space-y-3">
        <button 
          disabled 
          className={`${actionClasses} bg-gray-100 text-gray-500 cursor-not-allowed`}
        >
          Tournament Ended
        </button>
        <p className="text-sm text-gray-500">This tournament has already concluded.</p>
      </div>
    );
  }

  if (isRegistered) {
    if (isLive) {
      return (
        <div className="text-center space-y-3">
          <button 
            onClick={onStart} 
            className={`${actionClasses} bg-emerald-500 hover:bg-emerald-600 text-white focus:ring-emerald-500`}
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Start Tournament
          </button>
          <div className="bg-green-50 dark:bg-green-900/20 py-2 px-4 rounded-md">
            <p className="text-sm text-green-700 dark:text-green-400 font-medium">
              Tournament is live! Good luck!
            </p>
          </div>
        </div>
      );
    }
    
    return (
      <div className="text-center space-y-3">
        <div className="relative">
          <button 
            disabled 
            className={`${actionClasses} bg-indigo-100 text-indigo-700 dark:bg-indigo-900/40 dark:text-indigo-300 cursor-default`}
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            Registered Successfully
          </button>
        </div>
        {timeRemaining > 0 && (
          <div className="bg-indigo-50 dark:bg-indigo-900/20 py-3 px-4 rounded-md">
            <p className="text-sm text-indigo-700 dark:text-indigo-400 font-medium mb-1">
              Tournament starts in:
            </p>
            <div className="font-mono text-xl font-semibold text-indigo-800 dark:text-indigo-300 tracking-wide">
              {formatTime(timeRemaining)}
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <button 
      onClick={onRegister} 
      className={`${actionClasses} bg-indigo-600 hover:bg-indigo-700 text-white focus:ring-indigo-500`}
    >
      <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
      </svg>
      Register for Tournament
    </button>
  );
};

// Enhanced loading state with subtle animation
const LoadingState = () => (
  <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
    <div className="text-center p-8 rounded-xl bg-white dark:bg-gray-800 shadow-lg">
      <div className="relative h-16 w-16 mx-auto mb-4">
        <div className="absolute top-0 left-0 h-full w-full border-4 border-indigo-200 dark:border-indigo-900 rounded-full opacity-25"></div>
        <div className="absolute top-0 left-0 h-full w-full border-4 border-transparent border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin"></div>
      </div>
      <p className="text-lg text-gray-700 dark:text-gray-300 font-medium">Loading Tournament Details</p>
      <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Please wait while we fetch the information...</p>
    </div>
  </div>
);

// Error state component with better visual feedback
const ErrorState = ({ message }) => (
  <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
    <div className="bg-white dark:bg-gray-800 p-8 rounded-xl shadow-lg text-center max-w-md">
      <div className="text-red-500 dark:text-red-400 mb-4">
        <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <p className="text-xl text-gray-800 dark:text-gray-200 font-semibold mb-2">Error Occurred</p>
      <p className="text-gray-600 dark:text-gray-400">{message}</p>
      <button 
        onClick={() => window.location.reload()}
        className="mt-4 px-4 py-2 bg-indigo-100 text-indigo-700 dark:bg-indigo-900/40 dark:text-indigo-300 rounded-md hover:bg-indigo-200 dark:hover:bg-indigo-900/60 transition-colors"
      >
        Try Again
      </button>
    </div>
  </div>
);

// Not found state component with helpful actions
const NotFoundState = () => (
  <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
    <div className="bg-white dark:bg-gray-800 p-8 rounded-xl shadow-lg text-center max-w-md">
      <div className="text-gray-400 mb-4">
        <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <p className="text-xl text-gray-800 dark:text-gray-200 font-semibold mb-2">Tournament Not Found</p>
      <p className="text-gray-600 dark:text-gray-400">The tournament you're looking for doesn't exist or has been removed.</p>
      <div className="mt-6 flex flex-col sm:flex-row justify-center gap-3">
        <a 
          href="/"
          className="px-4 py-2 bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
        >
          Return Home
        </a>
        <a 
          href="/tournaments"
          className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
        >
          Browse Tournaments
        </a>
      </div>
    </div>
  </div>
);

const TournamentPage = ({ params: { tournamentId } }) => {
  const router = useRouter();
  const [tournament, setTournament] = useState(null);
  const [registeredTournaments, setRegisteredTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [timeRemaining, setTimeRemaining] = useState(0);
  const token = getCookie('token');
  const dispatch=useDispatch();

  useEffect(() => {
    const fetchTournamentData = async () => {
      if (!tournamentId) return;
      
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      
      try {
        // Fetch tournament details
        const { data: tournamentData } = await axios.get(
          `${baseUrl}/api/tournament/mtm/getTournamentById/${tournamentId}`,
          { headers }
        );
        
        if (!tournamentData) {
          setError('Tournament not found');
          setLoading(false);
          return;
        }
        
        setTournament(tournamentData);
        dispatch(setTournamentData(tournamentData));
        
        // Fetch registered tournaments
        if (token) {
          const { data: userTournaments } = await axios.get(
            `${baseUrl}/api/tournament/mtm/registeredTournamentsByUser`,
            { headers }
          );
          
          if (userTournaments) {
            const registeredIds = userTournaments;
            setRegisteredTournaments(registeredIds);
          }
        }
      } catch (err) {
        console.error("Error fetching tournament data:", err);
        setError(err.response?.data?.message || 'Failed to load tournament details');
      } finally {
        setLoading(false);
      }
    };
    
    fetchTournamentData();
  }, [tournamentId, token]);
  
  // Update countdown timer
  useEffect(() => {
    if (!tournament) return;
    
    const updateTimer = () => {
      const now = Date.now();
      const start = tournament.startTime;
      
      if (now < start) {
        setTimeRemaining(start - now);
      } else {
        setTimeRemaining(0);
      }
    };
    
    updateTimer();
    const timerId = setInterval(updateTimer, 1000);
    
    return () => clearInterval(timerId);
  }, [tournament]);

  const handleRegister = async () => {
    if (!token) {
      // Redirect to login if not authenticated
      router.push('/login?redirect=/tournament/' + tournamentId);
      return;
    }
    
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    try {
      setLoading(true);
      await axios.get(
        `${baseUrl}/api/tournament/mtm/joinTournament/${tournament.tournamentId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      // Show success notification and update state
      setRegisteredTournaments(prev => [...prev, tournament.tournamentId]);
    } catch (err) {
      console.error("Error registering for tournament:", err);
      const errorMsg = err.response?.data?.message || 'Failed to register for tournament';
      alert(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingState />;
  if (error) return <ErrorState message={error} />;
  if (!tournament) return <NotFoundState />;

  // Tournament status checks
  const now = Date.now();
  const tournamentStart = tournament.startTime;
  const tournamentEnd = tournamentStart + tournament.durationInSeconds * 1000;
  const isLive = now >= tournamentStart && now <= tournamentEnd;
  const isEnded = now > tournamentEnd;
  const isRegistered = registeredTournaments.includes(Number(tournament.tournamentId));
  console.log(registeredTournaments, tournament, isRegistered);
  console.log("tournament end", tournamentEnd, "now", now, "isLive", isLive, "isEnded", isEnded);
  

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 py-8 px-4">
      <div className="max-w-5xl mx-auto overflow-hidden bg-white dark:bg-gray-800 rounded-xl shadow-xl">
        <TournamentHeader 
          name={tournament.name} 
          description={tournament.description} 
          startTime={tournament.startTime}
          isRegistered={isRegistered}
          isLive={isLive}
        />
        
        <div className="p-6 md:p-8">
          <div className="mb-8">
            <h2 className="text-xl font-semibold text-gray-800 dark:text-gray-200 mb-4">Tournament Details</h2>
            <TournamentInfoGrid tournament={tournament} />
          </div>
          
          {/* Action section */}
          <div className="mt-10 max-w-md mx-auto">
            <TournamentActionButton 
              isRegistered={isRegistered}
              isLive={isLive}
              isEnded={isEnded}
              timeRemaining={timeRemaining}
              onRegister={handleRegister}
              onStart={() => router.push(`/tournamentPage/${tournamentId}`)}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default TournamentPage;