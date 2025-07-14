'use client';
import React, { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";
import { useParams } from "next/navigation";
import webSocketService from "@/services/webSocketService";
import { getCookie } from "cookies-next";

import {FaCopy, FaUser, FaStar, FaShieldAlt, FaClock, FaTrophy, FaSignOutAlt, FaChevronDown, 
         FaCheck, FaTimes, FaCode, FaClock as FaClockSolid, FaMemory, FaAngleDown, 
         FaHistory, FaCodeBranch, FaCalendarAlt } from "react-icons/fa";
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';

// Utility function to format time
const formatTime = (ms) => {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${hours.toString().padStart(2,'0')}:${minutes.toString().padStart(2,'0')}:${seconds.toString().padStart(2,'0')}`;
};

// Format timestamp to readable date/time
const formatDateTime = (timestamp) => {
  if (!timestamp) return 'N/A';
  const date = new Date(timestamp);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
};

// Format date for submission header
const formatSubmissionDate = (timestamp) => {
  if (!timestamp) return 'N/A';
  const date = new Date(timestamp);
  return date.toLocaleString([], { 
    month: 'short', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  });
};

// Utility to get language display name
const getLanguageDisplayName = (language) => {
  const languageMap = {
    'c++': 'C++',
    'c': 'C',
    'java': 'Java',
    'python': 'Python',
    'python3': 'Python 3',
    'javascript': 'JavaScript',
    'kotlin': 'Kotlin',
    'go': 'Go',
    'rust': 'Rust'
  };
  
  return languageMap[language?.toLowerCase()] || language;
};

// Utility to get language highlight mode
const getLanguageHighlightMode = (language) => {
  const modeMap = {
    'c++': 'cpp',
    'c': 'c',
    'java': 'java',
    'python': 'python',
    'python3': 'python',
    'javascript': 'javascript',
    'kotlin': 'kotlin',
    'go': 'go',
    'rust': 'rust'
  };
  
  return modeMap[language?.toLowerCase()] || 'text';
};

// Utility to get verdict class and icon
const getVerdictInfo = (verdict) => {
  switch (verdict) {
    case 'AC':
      return { 
        icon: <FaCheck className="mr-1.5" />, 
        text: 'Accepted', 
        bgClass: 'bg-emerald-100 dark:bg-emerald-900/40', 
        textClass: 'text-emerald-600 dark:text-emerald-400' 
      };
    case 'WA':
      return { 
        icon: <FaTimes className="mr-1.5" />, 
        text: 'Wrong Answer', 
        bgClass: 'bg-red-100 dark:bg-red-900/40', 
        textClass: 'text-red-600 dark:text-red-400' 
      };
    case 'TLE':
      return { 
        icon: <FaClockSolid className="mr-1.5" />, 
        text: 'Time Limit Exceeded', 
        bgClass: 'bg-yellow-100 dark:bg-yellow-900/40', 
        textClass: 'text-yellow-600 dark:text-yellow-400' 
      };
    case 'MLE':
      return { 
        icon: <FaMemory className="mr-1.5" />, 
        text: 'Memory Limit Exceeded', 
        bgClass: 'bg-yellow-100 dark:bg-yellow-900/40', 
        textClass: 'text-yellow-600 dark:text-yellow-400' 
      };
    case 'CE':
      return { 
        icon: <FaCode className="mr-1.5" />, 
        text: 'Compilation Error', 
        bgClass: 'bg-orange-100 dark:bg-orange-900/40', 
        textClass: 'text-orange-600 dark:text-orange-400' 
      };
    case 'RE':
      return { 
        icon: <FaTimes className="mr-1.5" />, 
        text: 'Runtime Error', 
        bgClass: 'bg-purple-100 dark:bg-purple-900/40', 
        textClass: 'text-purple-600 dark:text-purple-400' 
      };
    default:
      return { 
        icon: <FaClockSolid className="mr-1.5" />, 
        text: verdict || 'Pending', 
        bgClass: 'bg-gray-100 dark:bg-gray-700', 
        textClass: 'text-gray-600 dark:text-gray-400' 
      };
  }
};
const SubmissionCard = ({ submission }) => {
  const [expanded, setExpanded] = useState(false);
  const [copied, setCopied] = useState(false);
  const verdictInfo = getVerdictInfo(submission.verdict);
  const languageDisplay = getLanguageDisplayName(submission.language);
  const highlightLanguage = getLanguageHighlightMode(submission.language);
  
  const copyCode = () => {
    navigator.clipboard.writeText(submission.code || '')
      .then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      })
      .catch(err => console.error('Failed to copy code:', err));
  };
  
  return (
    <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden mb-4 shadow-sm transition-all hover:shadow-md">
      {/* Submission Header */}
      <div className="p-4 flex flex-wrap items-center justify-between gap-3 border-b border-gray-100 dark:border-gray-700">
        <div className="flex items-center">
          <div className={`inline-flex items-center px-3 py-1.5 rounded-full text-xs font-medium ${verdictInfo.bgClass} ${verdictInfo.textClass}`}>
            {verdictInfo.icon}
            {verdictInfo.text}
          </div>
          
          <div className="ml-3 flex items-center text-sm text-gray-500 dark:text-gray-400">
            <FaCalendarAlt className="mr-1.5 text-xs" />
            {formatSubmissionDate(submission.submissionTime)}
          </div>
        </div>
        
        <div className="flex items-center gap-4">
          {/* Language Badge */}
          <div className="flex items-center">
            <FaCodeBranch className="text-gray-400 dark:text-gray-500 mr-1.5 text-sm" />
            <span className="text-sm font-medium">{languageDisplay}</span>
          </div>
          
          {/* Metrics - Always show even if zero, with proper formatting */}
          <div className="flex space-x-3">
            <div className="flex items-center text-xs text-gray-500 dark:text-gray-400">
              <FaClockSolid className="mr-1" />
              <span>{submission.maxTimeTaken || 0} ms</span>
            </div>
            
            <div className="flex items-center text-xs text-gray-500 dark:text-gray-400">
              <FaMemory className="mr-1" />
              <span>{Math.round((submission.maxMemoryUsed || 0) / 1024)} KB</span>
            </div>
          </div>
          
          {/* Toggle Button */}
          <button 
            onClick={() => setExpanded(!expanded)}
            className="text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300 transition-colors"
          >
            <FaAngleDown className={`transform transition-transform ${expanded ? 'rotate-180' : ''}`} />
          </button>
        </div>
      </div>
      
      {/* Code Section (Collapsible) */}
      {expanded && (
        <div className="overflow-hidden transition-all bg-gray-50 dark:bg-gray-900/50">
          <div className="relative text-xs overflow-auto" style={{ maxHeight: '400px' }}>
            {/* Copy Code Button */}
            <div className="absolute top-2 right-2 z-10">
              <button
                onClick={copyCode}
                className="px-2 py-1 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded text-xs flex items-center hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
              >
                {copied ? (
                  <>
                    <FaCheck className="mr-1 text-green-500" />
                    <span>Copied!</span>
                  </>
                ) : (
                  <>
                    <FaCopy className="mr-1" />
                    <span>Copy Code</span>
                  </>
                )}
              </button>
            </div>
            
            <SyntaxHighlighter 
              language={highlightLanguage} 
              style={tomorrow} 
              showLineNumbers={true}
              wrapLongLines={true}
              customStyle={{ 
                margin: 0,
                padding: '1rem',
                borderRadius: 0,
                fontSize: '0.8rem',
                backgroundColor: 'transparent'
              }}
            >
              {submission.code || '// No code available'}
            </SyntaxHighlighter>
          </div>
        </div>
      )}
    </div>
  );
};
const SubmissionList = ({ submissions, loading }) => {
  // Group submissions by problem index
  const submissionsByProblem = useMemo(() => {
    const grouped = {};
    
    if (submissions && submissions.length > 0) {
      submissions.forEach(submission => {
        const index = submission.index;
        if (!grouped[index]) {
          grouped[index] = [];
        }
        grouped[index].push(submission);
      });
      
      // Sort each problem's submissions by time (newest first)
      Object.keys(grouped).forEach(index => {
        grouped[index].sort((a, b) => b.submissionTime - a.submissionTime);
      });
    }
    
    return grouped;
  }, [submissions]);
  
  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <div className="animate-pulse flex flex-col items-center">
          <div className="flex space-x-2">
            <div className="h-3 w-3 bg-gray-300 dark:bg-gray-600 rounded-full animate-bounce"></div>
            <div className="h-3 w-3 bg-gray-400 dark:bg-gray-500 rounded-full animate-bounce delay-100"></div>
            <div className="h-3 w-3 bg-gray-500 dark:bg-gray-400 rounded-full animate-bounce delay-200"></div>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium mt-3">Loading submissions...</p>
        </div>
      </div>
    );
  }
  
  if (Object.keys(submissionsByProblem).length === 0) {
    return (
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-8 text-center">
        <div className="flex justify-center mb-4">
          <div className="p-3 bg-gray-100 dark:bg-gray-700 rounded-full">
            <FaHistory className="text-xl text-gray-400 dark:text-gray-500" />
          </div>
        </div>
        <h3 className="text-gray-700 dark:text-gray-300 font-medium mb-1">No submissions yet</h3>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Your submissions will appear here when you make them
        </p>
      </div>
    );
  }
  
  return (
    <div>
      {Object.keys(submissionsByProblem)
        .sort((a, b) => Number(a) - Number(b))
        .map(problemIndex => (
          <div key={problemIndex} className="mb-6">
            <div className="flex items-center mb-2">
              <h3 className="text-md font-semibold text-gray-800 dark:text-gray-200">
                Problem {Number(problemIndex) + 1}
              </h3>
              <div className="ml-3 px-2 py-0.5 text-xs bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 rounded">
                {submissionsByProblem[problemIndex].length} submission{submissionsByProblem[problemIndex].length !== 1 ? 's' : ''}
              </div>
            </div>
            
            <div className="space-y-0">
              {submissionsByProblem[problemIndex].map(submission => (
                <SubmissionCard key={submission.id} submission={submission} />
              ))}
            </div>
          </div>
        ))}
    </div>
  );
};

const TournamentControlBox = () => {
  const router = useRouter();
  // Get tournamentId from route params
  const params = useParams();
  const tournamentId = params?.tournamentId;
  console.log("tournamentId from params in tournament control box: ", tournamentId);
  const [tournamentEndTime, setTournamentEndTime] = useState(null);
  const [tournamentData, setTournamentData] = useState(null);
  const [timeRemaining, setTimeRemaining] = useState(0);
  const [isExpanded, setIsExpanded] = useState(false);
  const [loading, setLoading] = useState(true);
  const [classicSubmissions, setClassicSubmissions] = useState([]);
  const [loadingSubmissions, setLoadingSubmissions] = useState(false);

  const userName = useSelector((state) => state.auth?.user);
  const token = getCookie("token");

  useEffect(() => {
    if (!token || !userName || !tournamentId) return;
    
    const wsUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/ws`;
    webSocketService.connect(wsUrl, token);
    
    const destination = `/topic/userRank/${tournamentId}/${userName}`;
    console.log("📡 Subscribing to user rank topic:", destination);
    webSocketService.subscribe(destination, (message) => {
      if (message && message.endTime) {
        setTournamentEndTime(message.endTime);
        setTournamentData(message);
        console.log("📊 Received tournament data:", message);
        setLoading(false);
      }
    });
   

    webSocketService.send('/app/userRank', `${tournamentId}/${userName}`);

    return () => {
      webSocketService.unsubscribe(destination);
    };
  }, [token, userName, tournamentId]);

  // Subscribe to classic submissions when dropdown is expanded
  useEffect(() => {
    if (!isExpanded || !token || !userName || !tournamentId) return;
    
    setLoadingSubmissions(true);
    console.log("📊 Requesting classic submissions for tournament:", tournamentId);
    
    // Subscribe to classic submissions topic
    const submissionsDestination = `/topic/tournament/classicSubmissions/${tournamentId}/${userName}`;
    
    // Subscribe first
    webSocketService.subscribe(submissionsDestination, (submissions) => {
      console.log("📬 Received submissions data:", submissions);
      if (submissions && Array.isArray(submissions)) {
        setClassicSubmissions(submissions);
      } else {
        console.warn("⚠️ Received invalid submissions data", submissions);
        setClassicSubmissions([]);
      }
      setLoadingSubmissions(false);
    });
    
    // UPDATED APPROACH: Send tournamentId as a payload, not path variable
    webSocketService.send(
      '/app/tournament/getClassicSubmissionsByTournamentIdAndUsername',
      tournamentId  // The tournamentId is now sent as the message payload
    );
    
    // Add debug logging
    console.log("🔄 Sent request with payload:", tournamentId);
    
    // Set a fallback timeout
    const timeoutId = setTimeout(() => {
      if (loadingSubmissions) {
        console.log("⏱️ Timeout reached, ending loading state");
        setLoadingSubmissions(false);
      }
    }, 5000);
    
    return () => {
      console.log("🧹 Cleaning up submissions subscription");
      webSocketService.unsubscribe(submissionsDestination);
      clearTimeout(timeoutId);
    };
  }, [isExpanded, token, userName, tournamentId]);

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

  const { currentRank, rankWithSubmissionDTO } = tournamentData;
  const { score, penalty, submissionDTOS } = rankWithSubmissionDTO || {};
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
              {submissionDTOS && submissionDTOS.map((submission, index) => {
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
              {(!submissionDTOS || submissionDTOS.length === 0) && (
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
      
      {/* Expanded Details Panel - Simplified to show only submissions */}
{isExpanded && (
  <div className="absolute top-[8vh] left-0 right-0 bg-white dark:bg-gray-800 shadow-md border-b border-gray-200 dark:border-gray-700 p-4 z-50 animate-fadeIn overflow-x-hidden overflow-y-auto max-h-[80vh]">
    {/* Submission History Section */}
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-800 dark:text-gray-200">
          Submission History
        </h2>
        
        <div className="text-xs text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded">
          {classicSubmissions.length} total submission{classicSubmissions.length !== 1 ? 's' : ''}
        </div>
      </div>
      
      <SubmissionList 
        submissions={classicSubmissions} 
        loading={loadingSubmissions} 
      />
    </div>
  </div>
)}
    </div>
  );
};

export default TournamentControlBox;