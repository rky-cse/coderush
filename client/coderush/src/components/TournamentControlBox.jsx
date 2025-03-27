'use client';
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";
import { useParams } from "next/navigation";
import webSocketService from "@/services/webSocketService";
import { getCookie } from "cookies-next";
import { FaUser, FaStar, FaShieldAlt, FaClock, FaTrophy, FaSignOutAlt, FaChevronDown, FaCheck, FaTimes } from "react-icons/fa";

// Utility function to format time
const formatTime = (ms) => {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${hours.toString().padStart(2,'0')}:${minutes.toString().padStart(2,'0')}:${seconds.toString().padStart(2,'0')}`;
};

const TournamentControlBox = () => {
  const router = useRouter();
  // Get tournamentId from route params
  const params = useParams();
  const tournamentId = params?.tournamentId;
  
  const [tournamentEndTime, setTournamentEndTime] = useState(null);
  const [tournamentData, setTournamentData] = useState(null);
  const [timeRemaining, setTimeRemaining] = useState(0);
  const [isExpanded, setIsExpanded] = useState(false);
  const [loading, setLoading] = useState(true);

  const userName = useSelector((state) => state.auth?.user);
  const token = getCookie("token");

  useEffect(() => {
    if (!token || !userName || !tournamentId) return;
    
    const wsUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/ws`;
    webSocketService.connect(wsUrl, token);
    
    const destination = `/topic/userRank/${tournamentId}/${userName}`;
    webSocketService.subscribe(destination, (message) => {
      if (message && message.endTime) {
        setTournamentEndTime(message.endTime);
        setTournamentData(message);
        setLoading(false);
      }
    });

    webSocketService.send('/app/userRank', `${tournamentId}/${userName}`);

    return () => {
      webSocketService.unsubscribe(destination);
    };
  }, [token, userName, tournamentId]);

  useEffect(() => {
    if (!tournamentEndTime) return;
    
    const updateTimeRemaining = () => {
      const now = Date.now();
      const diff = Math.max(0, tournamentEndTime - now);
      setTimeRemaining(diff);
    };

    updateTimeRemaining();
    const timer = setInterval(updateTimeRemaining, 1000);
    return () => clearInterval(timer);
  }, [tournamentEndTime]);

  if (loading || !tournamentData) {
    return (
      <div className="flex h-full w-full items-center justify-center py-3 px-4 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
        <div className="animate-pulse flex items-center space-x-3">
          <div className="h-3 w-3 bg-gray-300 dark:bg-gray-600 rounded-full animate-bounce"></div>
          <div className="h-3 w-3 bg-gray-400 dark:bg-gray-500 rounded-full animate-bounce delay-100"></div>
          <div className="h-3 w-3 bg-gray-500 dark:bg-gray-400 rounded-full animate-bounce delay-200"></div>
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium ml-2">Loading tournament data...</p>
        </div>
      </div>
    );
  }

  const { currentRank, rankWithFreeStyleSubmissionDTO } = tournamentData;
  const { score, penalty, freeStyleSubmissionDTOS } = rankWithFreeStyleSubmissionDTO || {};
  const isTournamentEnded = timeRemaining <= 0;

  return (
    <div className="relative w-full">
      {/* Main Header View - Increased height and padding */}
      <div className="flex items-center justify-between px-4 py-3 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 h-[8vh] min-h-[60px]">
        {/* Stats Section */}
        <div className="flex items-center space-x-5 lg:space-x-6">
          {/* Essential stats with larger text and icons */}
          <div className="flex items-center space-x-2">
            <div className="flex items-center justify-center h-7 w-7 bg-indigo-100 dark:bg-indigo-900/40 rounded-full">
              <FaUser className="text-sm text-indigo-600 dark:text-indigo-400" />
            </div>
            <span className="text-sm font-medium">Rank: {currentRank || '-'}</span>
          </div>
          
          <div className="flex items-center space-x-2 md:flex hidden">
            <div className="flex items-center justify-center h-7 w-7 bg-amber-100 dark:bg-amber-900/40 rounded-full">
              <FaStar className="text-sm text-amber-600 dark:text-amber-400" />
            </div>
            <span className="text-sm font-medium">Score: {score || 0}</span>
          </div>
          
          <div className="hidden lg:flex items-center space-x-2">
            <div className="flex items-center justify-center h-7 w-7 bg-blue-100 dark:bg-blue-900/40 rounded-full">
              <FaShieldAlt className="text-sm text-blue-600 dark:text-blue-400" />
            </div>
            <span className="text-sm font-medium">Penalty: {penalty || 0}</span>
          </div>
          
          {/* Submission status indicators - Larger and more readable */}
          <div className="flex overflow-x-auto max-w-[180px] sm:max-w-[240px] md:max-w-[300px] lg:max-w-[340px] scrollbar-hide">
            <div className="flex space-x-2 whitespace-nowrap">
              {freeStyleSubmissionDTOS && freeStyleSubmissionDTOS.map((submission, index) => {
                const Icon = submission.solved ? FaCheck : FaTimes;
                const bgColor = submission.solved ? "bg-emerald-100 dark:bg-emerald-900/40" : "bg-red-100 dark:bg-red-900/40";
                const textColor = submission.solved ? "text-emerald-600 dark:text-emerald-400" : "text-red-600 dark:text-red-400";
                
                return (
                  <div 
                    key={index} 
                    className={`inline-flex items-center ${bgColor} ${textColor} px-2 py-1 rounded-full text-sm`}
                  >
                    <Icon className="text-xs mr-1" />
                    <span className="font-medium">{index+1}</span>
                    <span className="ml-1 text-xs">({submission.numberOfAttempts})</span>
                  </div>
                );
              })}
              {(!freeStyleSubmissionDTOS || freeStyleSubmissionDTOS.length === 0) && (
                <span className="text-sm text-gray-500 dark:text-gray-400">No submissions</span>
              )}
            </div>
          </div>
        </div>
        
        {/* Time and Actions */}
        <div className="flex items-center space-x-3 sm:space-x-4">
          {/* Timer - Larger and more visible */}
          <div className="flex items-center space-x-2 bg-gray-50 dark:bg-gray-700 px-3 py-1.5 rounded-md">
            <FaClock className="text-sm text-gray-600 dark:text-gray-300" />
            <span className="text-sm font-mono font-medium">
              {isTournamentEnded ? "Ended" : formatTime(timeRemaining)}
            </span>
          </div>
          
          {/* Action Buttons - Larger with better spacing */}
          <div className="flex space-x-2">
            <button
              onClick={() => tournamentId && router.push(`/ranklist/${tournamentId}`)}
              className="flex items-center text-sm bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 px-3 py-1.5 rounded hover:bg-indigo-100 dark:hover:bg-indigo-900/50 transition-colors"
            >
              <FaTrophy className="text-sm" />
              <span className="hidden md:inline ml-1.5">Standing</span>
            </button>
            
            <button
              onClick={() => router.push('/')}
              className="flex items-center text-sm bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 px-3 py-1.5 rounded hover:bg-red-100 dark:hover:bg-red-900/50 transition-colors"
            >
              <FaSignOutAlt className="text-sm" />
              <span className="hidden md:inline ml-1.5">Exit</span>
            </button>
          </div>
          
          {/* Expand/Collapse Toggle - Larger and more visible */}
          <button 
            onClick={() => setIsExpanded(!isExpanded)}
            className="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 transition-colors p-1.5"
          >
            <FaChevronDown className={`text-sm transform transition-transform ${isExpanded ? 'rotate-180' : ''}`} />
          </button>
        </div>
      </div>
      
      {/* Expanded Details Panel - Improved spacing and readability */}
      {isExpanded && (
        <div className="absolute top-[8vh] left-0 right-0 bg-white dark:bg-gray-800 shadow-md border-b border-gray-200 dark:border-gray-700 p-4 z-50 animate-fadeIn">
          {/* Detailed View of Submissions */}
          <div className="mb-4">
            <p className="text-sm text-gray-500 dark:text-gray-400 font-medium mb-2">Submission Details:</p>
            <div className="overflow-x-auto pb-2">
              <div className="flex space-x-2 whitespace-nowrap">
                {freeStyleSubmissionDTOS && freeStyleSubmissionDTOS.map((submission, index) => {
                  const Icon = submission.solved ? FaCheck : FaTimes;
                  const bgColor = submission.solved ? "bg-emerald-100 dark:bg-emerald-900/40" : "bg-red-100 dark:bg-red-900/40";
                  const textColor = submission.solved ? "text-emerald-600 dark:text-emerald-400" : "text-red-600 dark:text-red-400";
                  
                  return (
                    <div 
                      key={index} 
                      className={`inline-flex items-center ${bgColor} ${textColor} px-3 py-1.5 rounded text-sm`}
                    >
                      <Icon className="mr-1.5 text-sm" />
                      <span className="font-medium">Q{index+1}</span>
                      <span className="ml-1.5 text-xs opacity-80">({submission.numberOfAttempts})</span>
                    </div>
                  );
                })}
                {(!freeStyleSubmissionDTOS || freeStyleSubmissionDTOS.length === 0) && (
                  <span className="text-sm text-gray-500 dark:text-gray-400">No submissions yet</span>
                )}
              </div>
            </div>
          </div>
          
          {/* Full Stats Display - More spacious and readable */}
          <div className="grid grid-cols-3 gap-3 mb-4">
            <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded">
              <p className="text-sm text-gray-500 dark:text-gray-400">Rank</p>
              <p className="font-medium text-lg">{currentRank || '-'}</p>
            </div>
            <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded">
              <p className="text-sm text-gray-500 dark:text-gray-400">Score</p>
              <p className="font-medium text-lg">{score || 0}</p>
            </div>
            <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded">
              <p className="text-sm text-gray-500 dark:text-gray-400">Penalty</p>
              <p className="font-medium text-lg">{penalty || 0}</p>
            </div>
          </div>
          
          {/* Detailed Timer */}
          {!isTournamentEnded && (
            <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded">
              <p className="text-sm text-gray-500 dark:text-gray-400 font-medium mb-1">Time Remaining:</p>
              <div className="text-lg font-mono font-medium text-indigo-700 dark:text-indigo-400">
                {formatTime(timeRemaining)}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default TournamentControlBox;