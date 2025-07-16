'use client';
import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import { Card, CardContent } from '@/components/ui/card';
import { Timer, Check, Clock, User, Sword, ChevronDown, AlertTriangle } from 'lucide-react';
import webSocketService from '@/services/webSocketService';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';

export default function DuelPage() {
  const timeControls = [5, 15, 25, 45, 60, 75, 90, 100, 120];
  const router = useRouter();
  const token = getCookie('token');
  const user = useSelector((state) => state.user);
  const { id: userId, name: userName, rating: userRating } = user;

  const [selectedTime, setSelectedTime] = useState(null);
  const [selectedTournamentType, setSelectedTournamentType] = useState('Classic');
  const [pendingMatchId, setPendingMatchId] = useState(null);
  const [opponentInfo, setOpponentInfo] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [countdown, setCountdown] = useState(null);
  const [searchingForMatch, setSearchingForMatch] = useState(false);
  const [matchFound, setMatchFound] = useState(false);
  const [waitingForOpponent, setWaitingForOpponent] = useState(false);
  const [matchConfirmed, setMatchConfirmed] = useState(false);
  const [confirmCountdown, setConfirmCountdown] = useState(0);
  const confirmIntervalRef = useRef(null);
  const confirmTimerId = useRef(null);

  const wsUrl = `${process.env.NEXT_PUBLIC_API_URL}/ws`;

  useEffect(() => {
    if (!token || !userId) return;
    webSocketService.connect(wsUrl, token);
    webSocketService.subscribe('/user/queue/match-notifications', handleWebSocketMessage);
    return () => {
      webSocketService.unsubscribe('/user/queue/match-notifications');
      clearTimers();
    };
  }, [token, userId]);

  const clearTimers = () => {
    if (confirmTimerId.current) clearTimeout(confirmTimerId.current);
    if (confirmIntervalRef.current) clearInterval(confirmIntervalRef.current);
  };

  const handleWebSocketMessage = (msg) => {
    const { status, matchId, player1Id, player1Name, player2Name, player1Rating, player2Rating, player2Id, pendingMatchId,timeControl,tournamentType} = msg;
    
    switch (status) {
      case 'PENDING_REQUEST':
        // Handle pending request response - show searching interface
        setSelectedTime(timeControl);
        setSelectedTournamentType(tournamentType);
        setSearchingForMatch(true);
        setShowModal(true);
        break;
      case 'MATCH_FOUND':
        clearTimers();
        setPendingMatchId(pendingMatchId);
        setMatchFound(true);
        const isPlayer1 = player1Id === userId;
        setOpponentInfo({
          userId: isPlayer1 ? player2Id : player1Id,
          userName: isPlayer1 ? player2Name : player1Name,
          rating: isPlayer1 ? player2Rating : player1Rating
        });
        setShowModal(true);
        startConfirmTimer();
        break;
      case 'MATCH_CANCELLED':
        alert('Opponent cancelled the match.');
        resetState();
        break;
      case 'MATCH_OK':
        clearTimers();
        setWaitingForOpponent(false);
        setMatchConfirmed(true);
        break;
      case 'MATCH_CREATED':
        clearTimers();
        startGameTimer(matchId);
        break;
    }
  };

  const startConfirmTimer = () => {
    setConfirmCountdown(15);
    confirmIntervalRef.current = setInterval(() => {
      setConfirmCountdown((c) => {
        if (c <= 1) {
          clearTimers();
          handleCancel();
          return 0;
        }
        return c - 1;
      });
    }, 1000);
    confirmTimerId.current = setTimeout(handleCancel, 15000);
  };

  const startGameTimer = (matchId) => {
    setCountdown(5);
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
    resetState();
  };

  const resetState = () => {
    clearTimers();
    setPendingMatchId(null);
    setOpponentInfo(null);
    setShowModal(false);
    setCountdown(null);
    setSelectedTime(null);
    setSearchingForMatch(false);
    setMatchFound(false);
    setWaitingForOpponent(false);
    setMatchConfirmed(false);
    setConfirmCountdown(0);
  };

  const handleTimeControlClick = async (time) => {
    setSelectedTime(time);
    setSearchingForMatch(true);
    setShowModal(true);
    
    if (!token) {
      alert('You are not logged in.');
      resetState();
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
      resetState();
    }
  };

  const handleConfirm = () => {
    if (!pendingMatchId || !token) return;
    clearTimers();
    webSocketService.send('/app/match/confirm', { pendingMatchId, userId });
    setWaitingForOpponent(true);
  };

  const handleCancel = () => {
    clearTimers();
    if (searchingForMatch && !matchFound) {
      webSocketService.send('/app/match/remove', userId);
    } else if (token) {
      webSocketService.send('/app/match/cancel', { pendingMatchId, userId });
    }
    resetState();
  };

  const CircularCountdown = ({ seconds, total = 15 }) => {
    const progress = ((total - seconds) / total) * 100;
    const circumference = 2 * Math.PI * 30;
    const strokeOffset = circumference - (progress / 100) * circumference;
    
    return (
      <div className="relative w-16 h-16 flex items-center justify-center">
        <svg className="w-16 h-16 transform -rotate-90" viewBox="0 0 64 64">
          <circle cx="32" cy="32" r="30" stroke="rgb(229, 231, 235)" strokeWidth="3" fill="none" />
          <circle
            cx="32" cy="32" r="30"
            stroke={seconds <= 5 ? "rgb(239, 68, 68)" : "rgb(59, 130, 246)"}
            strokeWidth="3" fill="none" strokeLinecap="round"
            strokeDasharray={circumference} strokeDashoffset={strokeOffset}
            className="transition-all duration-1000"
          />
        </svg>
        <span className={`absolute text-lg font-bold ${seconds <= 5 ? 'text-red-500' : 'text-blue-600'}`}>
          {seconds}
        </span>
      </div>
    );
  };

  return (
    <div className="min-h-[calc(100vh-64px)] bg-gray-50 p-4"> {/* Adjusted for navbar height */}
      <div className="max-w-5xl mx-auto"> {/* Increased max width */}
        {/* Tournament Type Toggle with Description */}
        <div className="mb-4"> {/* Reduced margin */}
          <div className="grid grid-cols-2 gap-3"> {/* Reduced gap */}
            {[
              {
                type: 'Classic',
                description: 'Complete coding challenge mode. Write and submit your full code solution.'
              },
              {
                type: 'FreeStyle', 
                description: 'Flexible problem-solving mode. Submit direct output or write code.'
              }
            ].map(({ type, description }) => (
              <button
                key={type}
                onClick={() => setSelectedTournamentType(type)}
                className={`p-3 rounded-lg border-2 transition-all text-left ${
                  selectedTournamentType === type
                    ? 'border-blue-500 bg-blue-50 shadow-md'
                    : 'border-gray-200 hover:border-gray-300 bg-white hover:shadow-sm'
                }`}
              >
                <div className="flex items-center gap-2 mb-1"> {/* Reduced gap and margin */}
                  <Sword className="w-4 h-4 text-blue-500" /> {/* Smaller icon */}
                  <h3 className="text-md font-semibold text-gray-900">{type} Mode</h3> {/* Smaller text */}
                </div>
                <p className="text-xs text-gray-600 leading-relaxed"> {/* Smaller text */}
                  {description}
                </p>
              </button>
            ))}
          </div>
        </div>

        {/* Time Controls */}
        <Card className="bg-white shadow-sm border-0 mb-4"> {/* Reduced margin */}
          <CardContent className="p-4"> {/* Reduced padding */}
            <div className="grid grid-cols-3 gap-3"> {/* Reduced gap */}
              {timeControls.map((time) => (
                <button
                  key={time}
                  onClick={() => handleTimeControlClick(time)}
                  className={`p-4 rounded-lg border-2 transition-all hover:shadow-md hover:scale-105 ${
                    selectedTime === time
                      ? 'border-blue-500 bg-blue-50 shadow-md'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="flex items-center justify-center gap-1 mb-1"> {/* Reduced gap and margin */}
                    <Timer className="w-4 h-4 text-blue-500" /> {/* Smaller icon */}
                    <span className="text-xl font-bold text-gray-900">{time}</span> {/* Smaller text */}
                  </div>
                  <div className="text-xs font-medium text-gray-600"> {/* Smaller text */}
                    {time} minutes
                  </div>
                  <div className="text-xs text-gray-500 px-1 py-0.5 bg-gray-100 rounded-full"> {/* Smaller text */}
                    {time <= 15 ? 'Quick' : time <= 60 ? 'Standard' : 'Marathon'}
                  </div>
                </button>
              ))}
            </div>
          </CardContent>
        </Card>

      </div>

      {/* Game Starting Countdown */}
      {countdown !== null && (
        <div className="fixed bottom-4 right-4 bg-white shadow-lg p-3 rounded-lg border"> {/* Smaller and tighter */}
          <div className="flex items-center gap-2"> {/* Reduced gap */}
            <Clock className="w-4 h-4 text-blue-500" /> {/* Smaller icon */}
            <div>
              <p className="text-xs text-gray-600">Starting in</p> {/* Smaller text */}
              <p className="text-md font-bold text-gray-900">{countdown}s</p> {/* Smaller text */}
            </div>
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-4"> {/* Reduced padding */}
            {matchConfirmed ? (
              <div className="text-center space-y-3"> {/* Reduced space */}
                <Sword className="w-10 h-10 text-blue-500 mx-auto" /> {/* Smaller icon */}
                <h2 className="text-xl font-bold text-gray-900">Match Starting!</h2> {/* Smaller text */}
                <div className="bg-gray-50 p-3 rounded-lg"> {/* Reduced padding */}
                  <div className="flex justify-between items-center mb-1"> {/* Reduced margin */}
                    <span className="font-medium text-sm">{userName}</span> {/* Smaller text */}
                    <span className="text-gray-500 text-sm">vs</span> {/* Smaller text */}
                    <span className="font-medium text-sm">{opponentInfo?.userName}</span> {/* Smaller text */}
                  </div>
                  <div className="text-xs text-gray-600"> {/* Smaller text */}
                    {selectedTime} min • {selectedTournamentType}
                  </div>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-1.5"> {/* Thinner bar */}
                  <div
                    className="bg-blue-500 h-1.5 rounded-full transition-all duration-1000" /* Thinner bar */
                    style={{ width: `${countdown ? (5 - countdown) * 20 : 100}%` }}
                  />
                </div>
              </div>
            ) : matchFound && opponentInfo && !waitingForOpponent ? (
              <div className="space-y-3"> {/* Reduced space */}
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-lg font-bold text-gray-900">Match Found!</h2> {/* Smaller text */}
                    <p className="text-sm text-gray-600">Accept the challenge?</p> {/* Smaller text */}
                  </div>
                  <CircularCountdown seconds={confirmCountdown} />
                </div>
                
                <div className="bg-gray-50 p-3 rounded-lg space-y-2"> {/* Reduced padding and space */}
                  <div className="flex justify-between text-sm"> {/* Smaller text */}
                    <span className="text-gray-600">Opponent:</span>
                    <span className="font-medium">{opponentInfo.userName}</span>
                  </div>
                  <div className="flex justify-between text-sm"> {/* Smaller text */}
                    <span className="text-gray-600">Rating:</span>
                    <span className="font-medium">{opponentInfo.rating}</span>
                  </div>
                  <div className="flex justify-between text-sm"> {/* Smaller text */}
                    <span className="text-gray-600">Time:</span>
                    <span className="font-medium">{selectedTime} min</span>
                  </div>
                  <div className="flex justify-between text-sm"> {/* Smaller text */}
                    <span className="text-gray-600">Type:</span>
                    <span className="font-medium">{selectedTournamentType}</span>
                  </div>
                </div>

                {confirmCountdown <= 5 && (
                  <div className="bg-red-50 border border-red-200 rounded-lg p-2"> {/* Reduced padding */}
                    <div className="flex items-center gap-1 text-xs text-red-600"> {/* Smaller text and gap */}
                      <AlertTriangle className="w-3 h-3" /> {/* Smaller icon */}
                      <span>Auto-decline in {confirmCountdown}s</span>
                    </div>
                  </div>
                )}

                <div className="flex gap-2"> {/* Reduced gap */}
                  <button
                    onClick={handleCancel}
                    className="flex-1 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" /* Smaller */
                  >
                    Decline
                  </button>
                  <button
                    onClick={handleConfirm}
                    className="flex-1 px-3 py-1.5 bg-blue-500 text-sm text-white rounded-lg hover:bg-blue-600" /* Smaller */
                  >
                    Accept
                  </button>
                </div>
              </div>
            ) : waitingForOpponent ? (
              <div className="text-center space-y-3"> {/* Reduced space */}
                <div className="w-14 h-14 bg-gray-100 rounded-full flex items-center justify-center mx-auto"> {/* Smaller */}
                  <User className="w-6 h-6 text-gray-400 animate-pulse" /> {/* Smaller icon */}
                </div>
                <h2 className="text-lg font-bold text-gray-900">Waiting for Response</h2> {/* Smaller text */}
                <p className="text-sm text-gray-600"> {/* Smaller text */}
                  Challenge sent to {opponentInfo?.userName}
                </p>
                <button
                  onClick={handleCancel}
                  className="w-full px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" /* Smaller */
                >
                  Cancel
                </button>
              </div>
            ) : (
              <div className="text-center space-y-3"> {/* Reduced space */}
                <div className="w-14 h-14 bg-blue-100 rounded-full flex items-center justify-center mx-auto"> {/* Smaller */}
                  <Timer className="w-6 h-6 text-blue-500 animate-pulse" /> {/* Smaller icon */}
                </div>
                <h2 className="text-lg font-bold text-gray-900">Finding Opponent</h2> {/* Smaller text */}
                <p className="text-sm text-gray-600"> {/* Smaller text */}
                  Searching for {selectedTime}-minute {selectedTournamentType} match
                </p>
                <button
                  onClick={handleCancel}
                  className="w-full px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" /* Smaller */
                >
                  Cancel Search
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}