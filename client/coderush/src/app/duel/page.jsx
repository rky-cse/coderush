'use client';
import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import { Card, CardContent } from '@/components/ui/card';
import { Timer, Check, Clock, User, Sword, Sparkles, ChevronDown, AlertTriangle } from 'lucide-react';
import webSocketService from '@/services/webSocketService';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';

export default function DuelPage() {
  const timeControls = [5, 15, 25, 45, 60, 75, 90, 100, 120];
  const tournamentTypes = ['FreeStyle', 'Classic'];
  const router = useRouter();

  const token = getCookie('token');
  const user = useSelector((state) => state.user);
  const userId = user.id;
  const userName = user.name;
  const userRating = user.rating;

  const [selectedTime, setSelectedTime] = useState(null);
  const [selectedTournamentType, setSelectedTournamentType] = useState('FreeStyle');
  const [showTournamentDropdown, setShowTournamentDropdown] = useState(false);
  const [pendingMatchId, setPendingMatchId] = useState(null);
  const [opponentInfo, setOpponentInfo] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [countdown, setCountdown] = useState(null);
  const [searchingForMatch, setSearchingForMatch] = useState(false);
  const [matchFound, setMatchFound] = useState(false);
  const [waitingForOpponent, setWaitingForOpponent] = useState(false);
  const [matchConfirmed, setMatchConfirmed] = useState(false);
  const [confirmTimerId, setConfirmTimerId] = useState(null);
  const [confirmCountdown, setConfirmCountdown] = useState(0);
  const confirmIntervalRef = useRef(null);

  const wsUrl = `${process.env.NEXT_PUBLIC_API_URL}/ws`;

  useEffect(() => {
    if (!token || !userId) return;
    webSocketService.connect(wsUrl, token);
    webSocketService.subscribe('/user/queue/match-notifications', handleWebSocketMessage);
    return () => {
      webSocketService.unsubscribe('/user/queue/match-notifications');
      clearConfirmTimers();
    };
  }, [token, userId]);

  const clearConfirmTimers = () => {
    if (confirmTimerId) clearTimeout(confirmTimerId);
    if (confirmIntervalRef.current) clearInterval(confirmIntervalRef.current);
  };

  const handleWebSocketMessage = (msg) => {
    const { status, matchId, player1Id, player1Name, player2Name, player1Rating, player2Rating, player2Id, pendingMatchId } = msg;
    console.log('WebSocket message received:', msg);
    console.log("\n\n\n Attributes Set :- " + "status: " + status + ", matchId: " + matchId + ", player1Id: " + player1Id + ", player1Name: " + player1Name + ", player2Name: " + player2Name + ", player1Rating: " + player1Rating + ", player2Rating: " + player2Rating + ", player2Id: " + player2Id + ", pendingMatchId: " + pendingMatchId);
    switch (status) {
      case 'MATCH_FOUND': {
        clearConfirmTimers();
        setPendingMatchId(pendingMatchId);
        setMatchFound(true);
        const isPlayer1 = player1Id === userId;
        const opponentName = isPlayer1 ? player2Name : player1Name;
        const opponentRating = isPlayer1 ? player2Rating : player1Rating;
        const opponentId = isPlayer1 ? player2Id : player1Id;
        setOpponentInfo({ 
          userId: opponentId,
          userName: opponentName,
          rating: opponentRating
        });
        setShowModal(true);
        // 15s confirm countdown
        setConfirmCountdown(15);
        const interval = setInterval(() => {
          setConfirmCountdown((c) => {
            if (c <= 1) {
              clearConfirmTimers();
              handleCancel();
              return 0;
            }
            return c - 1;
          });
        }, 1000);
        confirmIntervalRef.current = interval;
        const timeout = setTimeout(() => handleCancel(), 15000);
        setConfirmTimerId(timeout);
        break;
      }
      case 'MATCH_CANCELLED': {
        alert('Opponent cancelled the match.');
        resetMatchState();
        break;
      }
      case 'MATCH_OK': {
        clearConfirmTimers();
        setWaitingForOpponent(false);
        setMatchConfirmed(true);
        break;
      }
      case 'MATCH_CREATED': {
        clearConfirmTimers();
        setCountdown(10);
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer);
              router.push(`/duelTournamentPage/${matchId}`);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
        resetMatchState();
        break;
      }
      default:
        console.log('Unhandled status:', status);
    }
  };

  const resetMatchState = () => {
    clearConfirmTimers();
    setPendingMatchId(null);
    setOpponentInfo(null);
    setShowModal(false);
    setCountdown(null);
    setSelectedTime(null);
    setSearchingForMatch(false);
    setMatchFound(false);
    setWaitingForOpponent(false);
    setMatchConfirmed(false);
    setShowTournamentDropdown(false);
    setConfirmTimerId(null);
    setConfirmCountdown(0);
  };

  const handleTimeControlClick = async (time) => {
    setSelectedTime(time);
    setSearchingForMatch(true);
    setShowModal(true);
    if (!token) {
      alert('You are not logged in.');
      resetMatchState();
      return;
    }
    try {
      await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/api/match/request`,
        { userId, rating: userRating, requestTime: time, timeControl: time, tournamentType: selectedTournamentType },
        { headers: { Authorization: `Bearer ${token}` } }
      );
    } catch (error) {
      console.error('Error requesting match:', error);
      alert('Failed to request match.');
      resetMatchState();
    }
  };

  const handleConfirm = () => {
    if (!pendingMatchId || !token) return;
    clearConfirmTimers();
    webSocketService.send('/app/match/confirm', { pendingMatchId, userId });
    setWaitingForOpponent(true);
  };

  const handleCancel = () => {
    clearConfirmTimers();
    if (searchingForMatch && !matchFound) {
      webSocketService.send('/app/match/remove', userId);
      resetMatchState();
      return;
    }
    if (!token) return;
    webSocketService.send('/app/match/cancel', { pendingMatchId, userId });
    resetMatchState();
  };

  // Circular progress component for countdown
  const CircularCountdown = ({ seconds, total = 15 }) => {
    const progress = ((total - seconds) / total) * 100;
    const circumference = 2 * Math.PI * 36; // radius = 36
    const strokeOffset = circumference - (progress / 100) * circumference;
    return (
      <div className="relative w-20 h-20 flex items-center justify-center">
        <svg className="w-20 h-20 transform -rotate-90" viewBox="0 0 80 80">
          {/* Background circle */}
          <circle
            cx="40"
            cy="40"
            r="36"
            stroke="rgba(99, 102, 241, 0.1)"
            strokeWidth="4"
            fill="none"
          />
          {/* Progress circle */}
          <circle
            cx="40"
            cy="40"
            r="36"
            stroke={seconds <= 5 ? "rgb(239, 68, 68)" : "rgb(99, 102, 241)"}
            strokeWidth="4"
            fill="none"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={strokeOffset}
            className="transition-all duration-1000 ease-linear"
          />
        </svg>
        <div className="absolute inset-0 flex items-center justify-center">
          <span className={`text-2xl font-bold ${seconds <= 5 ? 'text-red-500 animate-pulse' : 'text-indigo-600'}`}>
            {seconds}
          </span>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-8 relative overflow-hidden"> 
      {/* Animated background elements */}
      <div className="absolute inset-0 opacity-20">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-gradient-to-r from-blue-200/30 to-purple-200/30 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute top-1/3 right-1/4 w-64 h-64 bg-indigo-200/20 rounded-full blur-2xl animate-pulse delay-1000"></div>
        <div className="absolute bottom-20 left-1/3 w-48 h-48 bg-purple-200/20 rounded-full blur-xl animate-pulse delay-1500"></div>
      </div>

      <div className="max-w-5xl mx-auto relative z-10">
        <div className="text-center mb-12 space-y-4">
          <div className="inline-flex items-center justify-center gap-4 bg-white/80 px-8 py-4 rounded-full border border-indigo-100 mb-8 backdrop-blur-sm">
            <Sparkles className="w-8 h-8 text-indigo-500 animate-pulse" />
            <h1 className="text-5xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent">
              Code Duel Arena
            </h1>
            <Sparkles className="w-8 h-8 text-blue-500 animate-pulse" />
          </div>
          <p className="text-xl text-indigo-600/80 font-medium">
            Where coding skills clash in epic timed battles
          </p>
        </div>

        <Card className="bg-white/90 backdrop-blur-lg shadow-lg border border-indigo-100 hover:border-indigo-200 transition-all duration-300 rounded-2xl">
          <CardContent className="p-10">
            <div className="flex items-center justify-between mb-10">
              <h2 className="text-2xl font-semibold text-indigo-800">
                Choose Your Battle Duration
              </h2>

              {/* Tournament Type Dropdown */}
              <div className="relative">
                <button
                  onClick={() => setShowTournamentDropdown(!showTournamentDropdown)}
                  className="flex items-center gap-3 px-6 py-3 bg-white border border-indigo-200 rounded-xl hover:border-indigo-300 hover:bg-indigo-50/50 transition-all duration-200 shadow-sm"
                >
                  <div className="flex items-center gap-2">
                    <div className={`w-3 h-3 rounded-full ${selectedTournamentType === 'FreeStyle' ? 'bg-emerald-400' : 'bg-blue-400'}`}></div>
                    <span className="font-medium text-indigo-700">{selectedTournamentType}</span>
                  </div>
                  <ChevronDown className={`w-4 h-4 text-indigo-500 transition-transform ${showTournamentDropdown ? 'rotate-180' : ''}`} />
                </button>

                {showTournamentDropdown && (
                  <div className="absolute top-full mt-2 right-0 w-48 bg-white border border-indigo-200 rounded-xl shadow-xl z-50 overflow-hidden backdrop-blur-sm">
                    {tournamentTypes.map((type) => (
                      <button
                        key={type}
                        onClick={() => {
                          setSelectedTournamentType(type);
                          setShowTournamentDropdown(false);
                        }}
                        className={`w-full flex items-center gap-3 px-4 py-3 text-left hover:bg-indigo-50 transition-colors ${selectedTournamentType === type ? 'bg-indigo-50/80' : ''
                          }`}
                      >
                        <div className={`w-3 h-3 rounded-full ${type === 'FreeStyle' ? 'bg-emerald-400' : 'bg-blue-400'}`}></div>
                        <span className="font-medium text-indigo-700">{type}</span>
                        {selectedTournamentType === type && (
                          <Check className="w-4 h-4 text-indigo-500 ml-auto" />
                        )}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-3 gap-6">
              {timeControls.map((time) => (
                <button
                  key={time}
                  onClick={() => handleTimeControlClick(time)}
                  className={`group relative h-44 rounded-xl overflow-hidden
                    border ${selectedTime === time ? 'border-indigo-400' : 'border-indigo-100'}
                    hover:border-indigo-300 transition-all duration-300
                    bg-gradient-to-b from-white to-indigo-50
                    shadow-md hover:shadow-lg
                    before:absolute before:inset-0 before:bg-gradient-to-br 
                    before:from-indigo-100/30 before:to-blue-100/30 before:opacity-0
                    hover:before:opacity-100 before:transition-opacity
                  `}
                >
                  <div className="relative z-10 flex flex-col items-center justify-center h-full p-6">
                    <div className="mb-3 flex items-center gap-2">
                      <Timer className="w-6 h-6 text-indigo-500 group-hover:text-indigo-600 transition-colors" />
                      <span className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent">
                        {time}
                      </span>
                    </div>
                    <span className="text-sm font-medium text-indigo-500/80 group-hover:text-indigo-600 transition-colors">
                      {time === 5 ? 'Lightning Round' : time <= 15 ? 'Speed Challenge' : 'Strategic Combat'}
                    </span>
                  </div>
                </button>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Countdown Timer */}
      {countdown !== null && (
        <div className="fixed bottom-8 right-8 bg-white/90 backdrop-blur-sm shadow-lg p-4 rounded-xl border border-indigo-100">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-100 rounded-full">
              <Clock className="w-5 h-5 text-indigo-600 animate-pulse" />
            </div>
            <div>
              <p className="text-sm font-medium text-indigo-700">Starting in</p>
              <p className="text-xl font-bold text-indigo-800">{countdown}s</p>
            </div>
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-indigo-900/30 backdrop-blur-sm z-50">
          <div className="relative bg-white/90 rounded-2xl shadow-2xl p-8 w-full max-w-xl mx-4 animate-modal-enter border border-indigo-100 overflow-hidden">
            {/* Water Ripple Effect */}
            {searchingForMatch && !matchFound && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="water-ripple animate-ripple-1"></div>
                <div className="water-ripple animate-ripple-2"></div>
                <div className="water-ripple animate-ripple-3"></div>
              </div>
            )}

            <div className="relative z-10">
              {matchConfirmed ? (
                <div className="text-center space-y-8">
                  <Sparkles className="w-16 h-16 text-indigo-400 mx-auto animate-sparkle" />
                  <h2 className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent">
                    Duel Initiated!
                  </h2>
                  <div className="space-y-6">
                    <div className="grid grid-cols-3 items-center gap-6 px-8">
                      {/* User info display */}
                      <div className="flex items-center gap-4 justify-end">
                        <User className="w-8 h-8 text-indigo-400" />
                        <div className="text-right min-w-0 flex-1">
                          <div className="text-lg text-indigo-700 truncate" title={userName || 'You'}>
                            {userName || 'You'}
                          </div>
                          <div className="text-sm text-indigo-500 truncate" title={`Rating: ${userRating || 'N/A'}`}>
                            Rating: {userRating || 'N/A'}
                          </div>
                        </div>
                      </div>
                      <div className="text-center py-4 bg-indigo-50 rounded-xl">
                        <span className="text-2xl font-bold text-indigo-600">{selectedTime}</span>
                        <span className="text-indigo-500 ml-2">mins</span>
                        <div className="text-xs text-indigo-400 mt-1">{selectedTournamentType}</div>
                      </div>
                      {/* Opponent info display */}
                      <div className="flex items-center gap-4">
                        <div className="text-left min-w-0 flex-1">
                          <div className="text-lg text-blue-700 truncate" title={opponentInfo?.userName || 'Opponent'}>
                            {opponentInfo?.userName || 'Opponent'}
                          </div>
                          <div className="text-sm text-blue-500 truncate" title={`Rating: ${opponentInfo?.rating || 'N/A'}`}>
                            Rating: {opponentInfo?.rating || 'N/A'}
                          </div>
                        </div>
                        <User className="w-8 h-8 text-blue-400" />
                      </div>
                    </div>
                    <div className="h-2 bg-indigo-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-indigo-400 to-blue-400 transition-all duration-1000 ease-out"
                        style={{ width: `${countdown ? (10 - countdown) * 10 : 100}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              ) : matchFound && opponentInfo && !waitingForOpponent ? (
                <div className="space-y-8">
                  {/* Countdown Timer at the top */}
                  <div className="absolute top-4 right-4 flex flex-col items-center gap-2">
                    <CircularCountdown seconds={confirmCountdown} />
                    <div className="flex items-center gap-1 text-xs text-gray-500">
                      <AlertTriangle className="w-3 h-3" />
                      <span>Auto-cancel</span>
                    </div>
                  </div>

                  <div className="text-center space-y-4 pt-8">
                    <div className="inline-flex relative">
                      <Sword className="w-12 h-12 text-indigo-500 animate-clash" />
                      <Sword className="w-12 h-12 text-blue-500 animate-clash-reverse -ml-4" />
                    </div>
                    <h2 className="text-3xl font-bold text-indigo-800">Opponent Located!</h2>
                    <p className="text-indigo-600/80">Prepare for an epic coding showdown</p>
                    
                    {/* Urgency indicator when time is low */}
                    {confirmCountdown <= 5 && (
                      <div className="bg-red-50 border border-red-200 rounded-lg p-3 mt-4">
                        <div className="flex items-center justify-center gap-2 text-red-600">
                          <AlertTriangle className="w-4 h-4 animate-pulse" />
                          <span className="text-sm font-medium">
                            Decide quickly! Auto-cancel in {confirmCountdown}s
                          </span>
                        </div>
                      </div>
                    )}
                  </div>
                  
                  <div className="grid grid-cols-2 gap-8 mt-8">
                    <div className="bg-indigo-50/80 p-6 rounded-xl border border-indigo-200">
                      <div className="flex items-center gap-4 mb-4">
                        <div className="p-3 bg-indigo-100 rounded-xl">
                          <User className="w-6 h-6 text-indigo-500" />
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="text-sm text-indigo-500 truncate">Your Name</p>
                          <p className="font-semibold text-indigo-700 truncate" title={userName || 'You'}>
                            {userName || 'You'}
                          </p>
                          <p className="text-xs text-indigo-500 truncate" title={`Rating: ${userRating || 'N/A'}`}>
                            Rating: {userRating || 'N/A'}
                          </p>
                        </div>
                      </div>
                      <div className="h-px bg-indigo-200/50 my-4" />
                      <div className="flex items-center gap-4 mb-4">
                        <div className="p-3 bg-blue-100 rounded-xl">
                          <Timer className="w-6 h-6 text-blue-500" />
                        </div>
                        <div>
                          <p className="text-sm text-blue-500">Time Control</p>
                          <p className="font-semibold text-blue-700">{selectedTime} mins</p>
                        </div>
                      </div>
                      <div className="h-px bg-indigo-200/50 my-4" />
                      <div className="flex items-center gap-4">
                        <div className={`p-3 rounded-xl ${selectedTournamentType === 'FreeStyle' ? 'bg-emerald-100' : 'bg-blue-100'}`}>
                          <div className={`w-4 h-4 rounded-full ${selectedTournamentType === 'FreeStyle' ? 'bg-emerald-400' : 'bg-blue-400'}`}></div>
                        </div>
                        <div>
                          <p className="text-sm text-indigo-500">Tournament Type</p>
                          <p className="font-semibold text-indigo-700">{selectedTournamentType}</p>
                        </div>
                      </div>
                    </div>
                    <div className="bg-indigo-50/80 p-6 rounded-xl border border-blue-200 relative">
                      <div className="absolute top-4 right-4 text-xs px-2 py-1 bg-blue-100 text-blue-600 rounded-full">
                        Opponent
                      </div>
                      <div className="flex items-center gap-4 mb-6">
                        <div className="p-3 bg-blue-100 rounded-xl">
                          <User className="w-6 h-6 text-blue-500" />
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="text-sm text-blue-500 truncate">Player Name</p>
                          <p className="font-semibold text-blue-700 truncate" title={opponentInfo?.userName || 'Unknown'}>
                            {opponentInfo?.userName || 'Unknown'}
                          </p>
                          <p className="text-xs text-blue-500 truncate" title={`Rating: ${opponentInfo?.rating || 'N/A'}`}>
                            Rating: {opponentInfo?.rating || 'N/A'}
                          </p>
                        </div>
                      </div>
                      <div className="h-px bg-indigo-200/50 my-4" />
                      <div className="text-center">
                        <p className="text-sm text-indigo-500">Ready Status</p>
                        <div className="inline-flex items-center gap-2 mt-2">
                          <div className="w-3 h-3 bg-emerald-400/80 rounded-full animate-pulse"></div>
                          <span className="text-emerald-600 text-sm">Waiting</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="flex justify-center gap-6">
                    <button
                      onClick={handleCancel}
                      className="px-8 py-3.5 bg-gray-100 hover:bg-gray-200 border border-gray-300 rounded-xl text-gray-700 hover:text-gray-800 transition-all duration-300"
                    >
                      Decline
                    </button>
                    <button
                      onClick={handleConfirm}
                      className={`px-8 py-3.5 bg-gradient-to-b from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 border border-indigo-400 rounded-xl text-white transition-all duration-300 ${
                        confirmCountdown <= 5 ? 'animate-pulse ring-2 ring-indigo-300' : ''
                      }`}
                    >
                      Accept Challenge
                    </button>
                  </div>
                </div>
              ) : waitingForOpponent ? (
                <div className="text-center space-y-8">
                  <div className="relative inline-block">
                    <div className="w-32 h-32 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
                      <User className="w-16 h-16 text-indigo-400/60" />
                    </div>
                    <div className="absolute top-0 right-0 -mr-4 -mt-4">
                      <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center animate-ping-slow">
                        <div className="w-8 h-8 bg-blue-200 rounded-full"></div>
                      </div>
                    </div>
                  </div>
                  <h2 className="text-3xl font-bold text-indigo-800">Awaiting Confirmation</h2>
                  <p className="text-indigo-600/80 px-12">
                    Your challenge has been issued to <span className="font-semibold">{opponentInfo?.userName || 'your opponent'}</span>...
                  </p>
                  <div className="text-sm text-indigo-500">
                    {selectedTime} mins • {selectedTournamentType}
                  </div>
                  <button
                    onClick={handleCancel}
                    className="px-8 py-3.5 bg-gray-100 hover:bg-gray-200 border border-gray-300 rounded-xl text-gray-700 hover:text-gray-800 transition-all duration-300"
                  >
                    Retract Challenge
                  </button>
                </div>
              ) : (
                <div className="text-center space-y-8">
                  <h2 className="text-3xl font-bold text-indigo-800">Scanning the Arena</h2>
                  <p className="text-indigo-600/80 px-12">
                    Seeking worthy opponents for {selectedTime}-minute {selectedTournamentType} duel...
                  </p>
                  <div className="flex justify-center gap-4">
                    <div className="flex items-center gap-2 text-indigo-500/80 text-sm">
                      <div className="w-2 h-2 bg-indigo-400 rounded-full animate-pulse"></div>
                      Analyzing player skills
                    </div>
                    <div className="flex items-center gap-2 text-blue-500/80 text-sm">
                      <div className="w-2 h-2 bg-blue-400 rounded-full animate-pulse delay-300"></div>
                      Matching time controls
                    </div>
                  </div>
                  <div className="pt-8">
                    <button
                      onClick={() => {
                        handleCancel();
                      }}
                      className="px-8 py-3.5 bg-gray-100 hover:bg-gray-200 border border-gray-300 rounded-xl text-gray-700 hover:text-gray-800 transition-all duration-300"
                    >
                      Abandon Search
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
      <style jsx global>{`
        @keyframes ripple {
          0% {
            transform: scale(0);
            opacity: 1;
            border: 2px solid rgba(99, 102, 241, 0.8);
          }
          100% {
            transform: scale(3);
            opacity: 0;
            border: 2px solid rgba(99, 102, 241, 0);
          }
        }
        .water-ripple {
          position: absolute;
          width: 100px;
          height: 100px;
          border-radius: 50%;
          box-shadow: 0 0 20px rgba(99, 102, 241, 0.3);
        }
        .animate-ripple-1 {
          animation: ripple 2s infinite;
        }
        .animate-ripple-2 {
          animation: ripple 2s infinite 0.5s;
        }
        .animate-ripple-3 {
          animation: ripple 2s infinite 1s;
        }
        @keyframes modal-enter {
          from { transform: scale(0.96) translateY(20px); opacity: 0; }
          to { transform: scale(1) translateY(0); opacity: 1; }
        }
        .animate-modal-enter {
          animation: modal-enter 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        @keyframes sparkle {
          0% { opacity: 0; transform: scale(0); }
          50% { opacity: 1; transform: scale(1); }
          100% { opacity: 0; transform: scale(1.5); }
        }
        .animate-sparkle {
          animation: sparkle 1.5s infinite;
        }
        @keyframes clash {
          0% { transform: translateX(0); }
          50% { transform: translateX(4px); }
          100% { transform: translateX(0); }
        }
        .animate-clash {
          animation: clash 0.8s ease-in-out infinite;
        }
        .animate-clash-reverse {
          animation: clash 0.8s ease-in-out infinite reverse;
        }
        .animate-ping-slow {
          animation: ping 3s cubic-bezier(0, 0, 0.2, 1) infinite;
        }
        @keyframes ping {
          75%, 100% { transform: scale(2); opacity: 0; }
        }
      `}</style>
    </div>
  );
}