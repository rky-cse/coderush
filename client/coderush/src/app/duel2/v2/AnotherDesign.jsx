

// 'use client';
// import React, { useState, useEffect } from 'react';
// import { useSelector } from 'react-redux';
// import { Card, CardContent } from '@/components/ui/card';
// import { Timer, Loader2, AlertCircle, Check, Clock, User, Sword, Sparkles, Zap } from 'lucide-react';
// import webSocketService from '@/services/webSocketService';
// import axios from 'axios';
// import { getCookie } from 'cookies-next';
// import { useRouter } from 'next/navigation';

// export default function DuelPage() {
//   const timeControls = [5, 15, 25, 45, 60, 75, 90, 100, 120];
//   const router = useRouter();
  
//   const token = getCookie('token');
//   const user = useSelector((state) => state.user);
//   const userId = user.id;

//   const [selectedTime, setSelectedTime] = useState(null);
//   const [pendingMatchId, setPendingMatchId] = useState(null);
//   const [opponentInfo, setOpponentInfo] = useState(null);
//   const [showModal, setShowModal] = useState(false);
//   const [countdown, setCountdown] = useState(null);
//   const [searchingForMatch, setSearchingForMatch] = useState(false);
//   const [matchFound, setMatchFound] = useState(false);
//   const [waitingForOpponent, setWaitingForOpponent] = useState(false);
//   const [matchConfirmed, setMatchConfirmed] = useState(false);

//   const wsUrl = `${process.env.NEXT_PUBLIC_API_URL}/ws`;

//   useEffect(() => {
//     if (!token) {
//       console.log('No token found in Redux. Not connecting to WebSocket.');
//       return;
//     }
//     console.log('Connecting to WebSocket with token:', token);
//     webSocketService.connect(wsUrl, token);
//     webSocketService.subscribe('/user/queue/match-notifications', (message) => {
//       console.log('Received WS message:', message);
//       handleWebSocketMessage(message);
//     });
//     return () => {
//       webSocketService.unsubscribe('/user/queue/match-notifications');
//     };
//   }, [token]);

//   const handleWebSocketMessage = (msg) => {
//     const { status, matchId, player1Id, player2Id, startTime, pendingMatchId } = msg;
//     console.log("Status:", status, "MatchID:", matchId, "Player1ID:", player1Id, "Player2ID:", player2Id, "StartTime:", startTime, "PendingMatchID:", pendingMatchId);
//     switch (status) {
//       case 'MATCH_FOUND': {
//         setPendingMatchId(pendingMatchId);
//         setMatchFound(true);
//         const opponent = (player1Id === userId) ? player2Id : player1Id;
//         setOpponentInfo({ userId: opponent });
//         setShowModal(true);
//         break;
//       }
//       case 'MATCH_CANCELLED': {
//         alert('Opponent cancelled the match.');
//         resetMatchState();
//         break;
//       }
//       case 'MATCH_OK': {
//         setWaitingForOpponent(false);
//         setMatchConfirmed(true);
//         setCountdown(10);
//         const timer = setInterval(() => {
//           setCountdown((prev) => {
//             if (prev <= 1) {
//               clearInterval(timer);
//               router.push('/duel/gameboard');
//               return 0;
//             }
//             return prev - 1;
//           });
//         }, 1000);
//         break;
//       }
//       case 'MATCH_CREATED': {
//         resetMatchState();
//         break;
//       }
//       default:
//         console.log('Unhandled status:', status);
//     }
//   };

//   const resetMatchState = () => {
//     setPendingMatchId(null);
//     setOpponentInfo(null);
//     setShowModal(false);
//     setCountdown(null);
//     setSelectedTime(null);
//     setSearchingForMatch(false);
//     setMatchFound(false);
//     setWaitingForOpponent(false);
//     setMatchConfirmed(false);
//   };

//   const handleTimeControlClick = async (time) => {
//     setSelectedTime(time);
//     setSearchingForMatch(true);
//     setShowModal(true);
//     console.log('handleTimeControlClick', time, userId, token);
//     if (!token) {
//       alert('You are not logged in or have no token. Cannot request match.');
//       setSearchingForMatch(false);
//       setShowModal(false);
//       return;
//     }
//     try {
//       await axios.post(
//         `${process.env.NEXT_PUBLIC_API_URL}/api/match/request`,
//         { userId, rating: 0, requestTime: time },
//         { headers: { Authorization: `Bearer ${token}` } }
//       );
//     } catch (error) {
//       console.error('Error requesting match:', error);
//       alert('Failed to request match. See console for details.');
//       setSearchingForMatch(false);
//       setShowModal(false);
//     }
//   };

//   const handleConfirm = () => {
//     if (!pendingMatchId || !token) return;
//     console.log('Sending confirm for match:', pendingMatchId);
//     webSocketService.send('/app/match/confirm', { pendingMatchId, userId });
//     setWaitingForOpponent(true);
//   };

//   const handleCancel = () => {
//     if (searchingForMatch && !matchFound) {
//       setSearchingForMatch(false);
//       setShowModal(false);
//       setSelectedTime(null);
//       return;
//     }
//     if (!pendingMatchId || !token) return;
//     webSocketService.send('/app/match/cancel', { pendingMatchId, userId });
//     resetMatchState();
//   };

//   return (
//     <div className="min-h-screen bg-gradient-to-br from-blue-900 to-blue-800 p-8 relative overflow-hidden rounded-3xl">
//       {/* Animated background elements */}
//       <div className="absolute inset-0 opacity-20">
//         <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-gradient-to-r from-blue-500/30 to-blue-400/30 rounded-full blur-3xl animate-pulse"></div>
//         <div className="absolute top-1/3 right-1/4 w-64 h-64 bg-blue-400/20 rounded-full blur-2xl animate-pulse delay-1000"></div>
//         <div className="absolute bottom-20 left-1/3 w-48 h-48 bg-blue-300/20 rounded-full blur-xl animate-pulse delay-1500"></div>
//       </div>

//       <div className="max-w-5xl mx-auto relative z-10">
//         <div className="text-center mb-12 space-y-4">
//           <div className="inline-flex items-center justify-center gap-4 bg-white/5 px-8 py-4 rounded-full border border-white/10 mb-8">
//             <Zap className="w-8 h-8 text-amber-400 animate-pulse" />
//             <h1 className="text-5xl font-bold bg-gradient-to-r from-blue-400 to-blue-500 bg-clip-text text-transparent">
//               Code Duel Arena
//             </h1>
//             <Sparkles className="w-8 h-8 text-blue-300 animate-pulse" />
//           </div>
//           <p className="text-xl text-blue-100/80 font-light">
//             Where coding skills clash in epic timed battles
//           </p>
//         </div>

//         <Card className="bg-white/5 backdrop-blur-xl shadow-2xl border border-white/10 hover:border-white/20 transition-all duration-300 rounded-3xl">
//           <CardContent className="p-10">
//             <h2 className="text-2xl font-semibold text-center mb-10">
//               <span className="bg-gradient-to-r from-blue-400 to-blue-500 bg-clip-text text-transparent">
//                 Choose Your Battle Duration
//               </span>
//             </h2>
//             <div className="grid grid-cols-3 gap-6">
//               {timeControls.map((time) => (
//                 <button
//                   key={time}
//                   onClick={() => handleTimeControlClick(time)}
//                   className={`
//                     group relative h-44 rounded-2xl overflow-hidden
//                     border ${selectedTime === time ? 'border-cyan-400/50' : 'border-white/10'}
//                     hover:border-cyan-300/50 transition-all duration-300
//                     bg-gradient-to-b from-white/5 to-transparent
//                     shadow-lg hover:shadow-2xl
//                     before:absolute before:inset-0 before:bg-gradient-to-br 
//                     before:from-cyan-500/10 before:to-blue-500/10 before:opacity-0
//                     hover:before:opacity-100 before:transition-opacity
//                   `}
//                 >
//                   <div className="relative z-10 flex flex-col items-center justify-center h-full p-6">
//                     <div className="mb-3 flex items-center gap-2">
//                       <Timer className="w-6 h-6 text-cyan-300 group-hover:text-cyan-200 transition-colors" />
//                       <span className="text-4xl font-bold bg-gradient-to-r from-cyan-300 to-blue-300 bg-clip-text text-transparent">
//                         {time}
//                       </span>
//                     </div>
//                     <span className="text-sm font-medium text-cyan-100/70 group-hover:text-cyan-100 transition-colors">
//                       {time === 5 ? 'Lightning Round' : time <= 15 ? 'Speed Challenge' : 'Strategic Combat'}
//                     </span>
//                     <div className="absolute bottom-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
//                       <Sword className="w-6 h-6 text-blue-300/50" />
//                     </div>
//                   </div>
//                 </button>
//               ))}
//             </div>
//           </CardContent>
//         </Card>
//       </div>

//       {/* Countdown Timer */}
//       {countdown !== null && (
//         <div className="fixed bottom-8 right-8 backdrop-blur-xl bg-cyan-500/10 p-6 rounded-2xl border border-cyan-400/20 shadow-2xl">
//           <div className="flex items-center gap-4">
//             <div className="p-3 bg-cyan-500/20 rounded-full">
//               <Clock className="w-6 h-6 text-cyan-300 animate-pulse" />
//             </div>
//             <div>
//               <p className="font-semibold text-cyan-100">Match starts in</p>
//               <p className="text-2xl font-bold text-cyan-400">{countdown}s</p>
//             </div>
//           </div>
//         </div>
//       )}

//       {/* Modal */}
//       {showModal && (
//         <div className="fixed inset-0 flex items-center justify-center bg-blue-900/60 backdrop-blur-2xl z-50 rounded-3xl">
//           <div className="relative bg-gradient-to-br from-blue-900 to-blue-800 rounded-3xl shadow-2xl p-10 w-full max-w-2xl mx-4 animate-modal-enter border border-white/10">
//             {/* Pulsating effect emanating from center */}
//             {searchingForMatch && !matchFound && (
//               <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
//                 <div className="w-32 h-32 rounded-full bg-blue-500 opacity-30 animate-pulse-out"></div>
//               </div>
//             )}
//             {/* Animated border effect */}
//             <div className="absolute inset-0 rounded-3xl border-2 border-cyan-400/20 animate-border-pulse pointer-events-none"></div>

//             {matchConfirmed ? (
//               // Confirmed Match View
//               <div className="text-center space-y-8">
//                 <div className="animate-confetti">
//                   <Sparkles className="w-16 h-16 text-cyan-400 mx-auto mb-6 animate-sparkle" />
//                 </div>
//                 <h2 className="text-4xl font-bold bg-gradient-to-r from-cyan-400 to-blue-400 bg-clip-text text-transparent">
//                   Duel Initiated!
//                 </h2>
//                 <div className="space-y-6">
//                   <div className="grid grid-cols-3 items-center gap-6 px-8">
//                     <div className="flex items-center gap-4 justify-end">
//                       <User className="w-8 h-8 text-cyan-400" />
//                       <span className="text-lg text-cyan-100">You</span>
//                     </div>
//                     <div className="text-center py-4 bg-white/5 rounded-xl">
//                       <span className="text-2xl font-bold text-blue-400">{selectedTime}</span>
//                       <span className="text-cyan-300 ml-2">mins</span>
//                     </div>
//                     <div className="flex items-center gap-4">
//                       <span className="text-lg text-cyan-100">Opponent</span>
//                       <User className="w-8 h-8 text-blue-400" />
//                     </div>
//                   </div>
//                   <div className="space-y-4">
//                     <div className="h-2 bg-cyan-500/10 rounded-full overflow-hidden">
//                       <div 
//                         className="h-full bg-gradient-to-r from-cyan-400 to-blue-400 transition-all duration-1000 ease-out"
//                         style={{ width: `${(10 - countdown) * 10}%` }}
//                       ></div>
//                     </div>
//                     <p className="text-cyan-300/80 font-light">
//                       Redirecting to battlefield in {countdown} seconds...
//                     </p>
//                   </div>
//                 </div>
//               </div>
//             ) : matchFound && opponentInfo && !waitingForOpponent ? (
//               // Match Found View
//               <div className="space-y-8">
//                 <div className="text-center space-y-4">
//                   <div className="inline-flex relative">
//                     <Sword className="w-12 h-12 text-cyan-400 animate-clash" />
//                     <Sword className="w-12 h-12 text-blue-400 animate-clash-reverse -ml-4" />
//                   </div>
//                   <h2 className="text-3xl font-bold text-cyan-100">Opponent Located!</h2>
//                   <p className="text-cyan-100/70">Prepare for an epic coding showdown</p>
//                 </div>
//                 <div className="grid grid-cols-2 gap-8">
//                   <div className="bg-white/5 p-6 rounded-xl border border-cyan-400/20">
//                     <div className="flex items-center gap-4 mb-4">
//                       <div className="p-3 bg-cyan-500/10 rounded-xl">
//                         <User className="w-6 h-6 text-cyan-400" />
//                       </div>
//                       <div>
//                         <p className="text-sm text-cyan-300">Your ID</p>
//                         <p className="font-semibold text-cyan-100">{userId}</p>
//                       </div>
//                     </div>
//                     <div className="h-px bg-white/10 my-4" />
//                     <div className="flex items-center gap-4">
//                       <div className="p-3 bg-blue-500/10 rounded-xl">
//                         <Timer className="w-6 h-6 text-blue-400" />
//                       </div>
//                       <div>
//                         <p className="text-sm text-blue-300">Time Control</p>
//                         <p className="font-semibold text-blue-100">{selectedTime} mins</p>
//                       </div>
//                     </div>
//                   </div>
//                   <div className="bg-white/5 p-6 rounded-xl border border-blue-400/20 relative">
//                     <div className="absolute top-4 right-4 text-xs px-2 py-1 bg-blue-500/20 text-blue-300 rounded-full">
//                       Opponent
//                     </div>
//                     <div className="flex items-center gap-4 mb-6">
//                       <div className="p-3 bg-blue-500/10 rounded-xl">
//                         <User className="w-6 h-6 text-blue-400" />
//                       </div>
//                       <div>
//                         <p className="text-sm text-blue-300">Player ID</p>
//                         <p className="font-semibold text-blue-100">{opponentInfo.userId}</p>
//                       </div>
//                     </div>
//                     <div className="h-px bg-white/10 my-4" />
//                     <div className="text-center">
//                       <p className="text-sm text-cyan-300">Ready Status</p>
//                       <div className="inline-flex items-center gap-2 mt-2">
//                         <div className="w-3 h-3 bg-emerald-400/80 rounded-full animate-pulse"></div>
//                         <span className="text-emerald-400 text-sm">Waiting</span>
//                       </div>
//                     </div>
//                   </div>
//                 </div>
//                 <div className="flex justify-center gap-6">
//                   <button
//                     onClick={handleCancel}
//                     className="px-8 py-3.5 bg-gradient-to-b from-slate-600/20 to-slate-700/10 hover:from-slate-700/30 hover:to-slate-800/20 border border-slate-500/30 rounded-xl text-slate-300 hover:text-slate-200 transition-all duration-300 flex items-center gap-2"
//                   >
//                     <span>Decline</span>
//                   </button>
//                   <button
//                     onClick={handleConfirm}
//                     className="px-8 py-3.5 bg-gradient-to-b from-cyan-500/20 to-cyan-600/10 hover:from-cyan-600/30 hover:to-cyan-700/20 border border-cyan-400/30 rounded-xl text-cyan-300 hover:text-cyan-200 transition-all duration-300 flex items-center gap-2"
//                   >
//                     <Zap className="w-5 h-5" />
//                     <span>Accept Challenge</span>
//                   </button>
//                 </div>
//               </div>
//             ) : waitingForOpponent ? (
//               // Waiting for Opponent View
//               <div className="text-center space-y-8">
//                 <div className="relative inline-block">
//                   <div className="w-32 h-32 bg-cyan-500/10 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
//                     <User className="w-16 h-16 text-cyan-400/60" />
//                   </div>
//                   <div className="absolute top-0 right-0 -mr-4 -mt-4">
//                     <div className="w-12 h-12 bg-blue-500/10 rounded-full flex items-center justify-center animate-ping-slow">
//                       <div className="w-8 h-8 bg-blue-400/20 rounded-full"></div>
//                     </div>
//                   </div>
//                 </div>
//                 <h2 className="text-3xl font-bold text-cyan-100">Awaiting Rival's Confirmation</h2>
//                 <p className="text-cyan-100/70 px-12">Your challenge has been issued. Waiting for opponent to accept...</p>
//                 <div className="flex justify-center">
//                   <button
//                     onClick={handleCancel}
//                     className="px-8 py-3.5 bg-gradient-to-b from-slate-600/20 to-slate-700/10 hover:from-slate-700/30 border border-slate-500/30 rounded-xl text-slate-300 hover:text-slate-200 transition-all duration-300"
//                   >
//                     Retract Challenge
//                   </button>
//                 </div>
//               </div>
//             ) : (
//               // Searching View
//               <div className="text-center space-y-8">
//                 <div className="relative inline-block">
//                   <div className="w-32 h-32 bg-gradient-to-br from-cyan-500/10 to-blue-500/10 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
//                     <Loader2 className="w-16 h-16 text-cyan-400 animate-spin-slow" />
//                   </div>
//                   <Sparkles className="absolute top-0 right-0 -mt-4 -mr-4 w-8 h-8 text-blue-400 animate-sparkle-delayed" />
//                 </div>
//                 <h2 className="text-3xl font-bold text-cyan-100">Scanning the Arena</h2>
//                 <p className="text-cyan-100/70 px-12">
//                   Seeking worthy opponents for a {selectedTime}-minute coding duel...
//                 </p>
//                 <div className="flex justify-center gap-4">
//                   <div className="flex items-center gap-2 text-cyan-300/70 text-sm">
//                     <div className="w-2 h-2 bg-cyan-400 rounded-full animate-pulse"></div>
//                     Analyzing player skills
//                   </div>
//                   <div className="flex items-center gap-2 text-blue-300/70 text-sm">
//                     <div className="w-2 h-2 bg-blue-400 rounded-full animate-pulse delay-300"></div>
//                     Matching time controls
//                   </div>
//                 </div>
//                 <div className="pt-8">
//                   <button
//                     onClick={handleCancel}
//                     className="px-8 py-3.5 bg-gradient-to-b from-slate-600/20 to-slate-700/10 hover:from-slate-700/30 border border-slate-500/30 rounded-xl text-slate-300 hover:text-slate-200 transition-all duration-300"
//                   >
//                     Abandon Search
//                   </button>
//                 </div>
//               </div>
//             )}
//           </div>
//         </div>
//       )}

//       <style jsx global>{`
//         @keyframes modal-enter {
//           from { transform: scale(0.96) translateY(20px); opacity: 0; }
//           to { transform: scale(1) translateY(0); opacity: 1; }
//         }
//         .animate-modal-enter {
//           animation: modal-enter 0.3s cubic-bezier(0.4, 0, 0.2, 1);
//         }

//         @keyframes border-pulse {
//           0% { opacity: 0; }
//           50% { opacity: 1; }
//           100% { opacity: 0; }
//         }
//         .animate-border-pulse {
//           animation: border-pulse 2s infinite;
//         }

//         @keyframes sparkle {
//           0% { opacity: 0; transform: scale(0); }
//           50% { opacity: 1; transform: scale(1); }
//           100% { opacity: 0; transform: scale(1.5); }
//         }
//         .animate-sparkle {
//           animation: sparkle 1.5s infinite;
//         }
//         .animate-sparkle-delayed {
//           animation: sparkle 1.5s infinite 0.5s;
//         }

//         @keyframes clash {
//           0% { transform: translateX(0); }
//           50% { transform: translateX(4px); }
//           100% { transform: translateX(0); }
//         }
//         .animate-clash {
//           animation: clash 0.8s ease-in-out infinite;
//         }
//         .animate-clash-reverse {
//           animation: clash 0.8s ease-in-out infinite reverse;
//         }

//         .animate-ping-slow {
//           animation: ping 3s cubic-bezier(0, 0, 0.2, 1) infinite;
//         }
//         @keyframes ping {
//           75%, 100% { transform: scale(2); opacity: 0; }
//         }

//         .animate-spin-slow {
//           animation: spin 3s linear infinite;
//         }
//         @keyframes spin {
//           to { transform: rotate(360deg); }
//         }

//         /* Custom pulse effect from center */
//         @keyframes pulse-out {
//           0% {
//             transform: scale(0.8);
//             opacity: 0.7;
//           }
//           50% {
//             transform: scale(1.2);
//             opacity: 0.3;
//           }
//           100% {
//             transform: scale(1.4);
//             opacity: 0;
//           }
//         }
//         .animate-pulse-out {
//           animation: pulse-out 2s ease-out infinite;
//         }
//       `}</style>
//     </div>
//   );
// }











// 'use client';
// import React, { useState, useEffect } from 'react';
// import { useSelector } from 'react-redux';
// import { Card, CardContent } from '@/components/ui/card';
// import { Timer, Loader2 } from 'lucide-react';
// import webSocketService from '@/services/webSocketService';
// import axios from 'axios';
// import { getCookie } from 'cookies-next';

// /**
//  * This page uses user data (userId, token) from Redux (userSlice).
//  * Make sure userSlice has fields: id, token, etc.
//  */
// export default function DuelPage() {
//   const timeControls = [5, 15, 25, 45, 60, 75, 90, 100, 120];

//   // 1) Retrieve user info from Redux
//   const token = getCookie('token');
//   const user = useSelector((state) => state.user);
//   const userId = user.id;

//   // 2) Local state for the matchmaking flow
//   const [selectedTime, setSelectedTime] = useState(null);
//   const [pendingMatchId, setPendingMatchId] = useState(null);
//   const [opponentInfo, setOpponentInfo] = useState(null);
//   const [showModal, setShowModal] = useState(false);
//   const [countdown, setCountdown] = useState(null); // for the 10s timer after MATCH_OK
//   const [searchingForMatch, setSearchingForMatch] = useState(false); // New state for search modal
//   const [matchFound, setMatchFound] = useState(false); // Track if match is found

//   // Build your WebSocket URL from env variable
//   const wsUrl = `${process.env.NEXT_PUBLIC_API_URL}/ws`;

//   // 3) Connect to WebSocket on mount and subscribe
//   useEffect(() => {
//     if (!token) {
//       // If no token, user might not be logged in; you can handle accordingly
//       console.log('No token found in Redux. Not connecting to WebSocket.');
//       return;
//     }

//     console.log('Connecting to WebSocket with token:', token);
//     webSocketService.connect(wsUrl, token);

//     // Subscribe to personal queue
//     webSocketService.subscribe('/user/queue/match-notifications', (message) => {
//       console.log('Received WS message:', message);
//       handleWebSocketMessage(message);
//     });

//     // Cleanup on unmount
//     return () => {
//       webSocketService.unsubscribe('/user/queue/match-notifications');
//       // Optionally webSocketService.disconnect();
//     };
//   }, [token]);

//   // 4) Handle incoming WebSocket messages
//   const handleWebSocketMessage = (msg) => {
//     const { status, matchId, player1Id, player2Id, startTime, pendingMatchId } = msg;
//     console.log( "Status: ", status , "MatchID: ", matchId, "Player1ID: ", player1Id, "Player2ID: ", player2Id, "StartTime: ", startTime, "PendingMatchID: ", pendingMatchId);

//     switch (status) {
//       case 'MATCH_FOUND': {
//         // Show a modal with opponent info
//         setPendingMatchId(pendingMatchId);
//         setMatchFound(true);

//         // Opponent is whichever is not "us"
//         const opponent = (player1Id === userId) ? player2Id : player1Id;
//         setOpponentInfo({
//           userId: opponent,
//           // ... any other fields from the message (like rating, name)
//         });
//         console.log("Opponent info:", opponent);
//         setShowModal(true);
//         break;
//       }
//       case 'MATCH_CANCELLED': {
//         alert('Opponent cancelled the match.');
//         resetMatchState();
//         break;
//       }
//       case 'MATCH_OK': {
//         // Both players confirmed, start 10s countdown
//         setCountdown(10);
//         const timer = setInterval(() => {
//           setCountdown((prev) => {
//             if (prev <= 1) {
//               clearInterval(timer);
//               return 0;
//             }
//             return prev - 1;
//           });
//         }, 1000);
//         break;
//       }
//       case 'MATCH_CREATED': {
//         // After 10s, the backend created the match
//         alert(`Match created! ID: ${matchId}. Starting now...`);
//         resetMatchState();
//         break;
//       }
//       default:
//         console.log('Unhandled status:', status);
//     }
//   };

//   const resetMatchState = () => {
//     setPendingMatchId(null);
//     setOpponentInfo(null);
//     setShowModal(false);
//     setCountdown(null);
//     setSelectedTime(null);
//     setSearchingForMatch(false);
//     setMatchFound(false);
//   };

//   // 5) On user clicking a time control -> POST to backend
//   const handleTimeControlClick = async (time) => {
//     setSelectedTime(time);
//     setSearchingForMatch(true); // Show the searching modal immediately
//     setShowModal(true);

//     console.log('handleTimeControlClick', time, userId, token);

//     if (!token) {
//       alert('You are not logged in or have no token. Cannot request match.');
//       setSearchingForMatch(false);
//       setShowModal(false);
//       return;
//     }

//     console.log(`Sending match request for ${time} minute game.`);
//     try {
//       await axios.post(
//         `${process.env.NEXT_PUBLIC_API_URL}/api/match/request`,
//         {
//           userId,
//           rating: 0, // example rating
//           requestTime: time,
//         },
//         {
//           headers: {
//             Authorization: `Bearer ${token}`,
//           },
//         }
//       );
//       // If success, user is queued; the rest is handled by WebSockets
//     } catch (error) {
//       console.error('Error requesting match:', error);
//       alert('Failed to request match. See console for details.');
//       setSearchingForMatch(false);
//       setShowModal(false);
//     }
//   };

//   // 6) Confirm or Cancel in the modal
//   const handleConfirm = () => {
//     if ( !pendingMatchId || !token) { console.log("BYE BYE"); return;}
//     // Send "confirm" via WebSocket
//     console.log('Sending confirm for match:', pendingMatchId);
//     webSocketService.send('/app/match/confirm', {
//       pendingMatchId,
//       userId,
//     });
//     setShowModal(false); // hide modal, wait for MATCH_OK
//   };

//   const handleCancel = () => {
//     if (searchingForMatch && !matchFound) {
//       // For now, just close the modal if we're still searching
//       // In a real implementation, you would cancel the search request here
//       setSearchingForMatch(false);
//       setShowModal(false);
//       setSelectedTime(null);
//       return;
//     }
    
//     if (!pendingMatchId || !token) return;
//     // Send "cancel" via WebSocket
//     webSocketService.send('/app/match/cancel', {
//       pendingMatchId,
//       userId,
//     });
//     resetMatchState();
//   };

//   // 7) Render UI
//   return (
//     <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-8">
//       <div className="max-w-4xl mx-auto">
//         <div className="text-center mb-12">
//           <h1 className="text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600 mb-4">
//             Duel
//           </h1>
//           <p className="text-gray-600 text-lg">
//             Select your preferred time control to start the game
//           </p>
//         </div>

//         <Card className="bg-white/80 backdrop-blur-sm shadow-2xl border-0">
//           <CardContent className="p-8">
//             <div className="grid grid-cols-3 gap-6">
//               {timeControls.map((time) => (
//                 <button
//                   key={time}
//                   onClick={() => handleTimeControlClick(time)}
//                   className={`
//                     relative h-36 rounded-xl 
//                     border-2 ${
//                       selectedTime === time ? 'border-blue-400' : 'border-gray-100'
//                     }
//                     hover:border-blue-300 transition-all duration-300
//                     flex flex-col items-center justify-center
//                     bg-white hover:bg-blue-50/50
//                     shadow-lg hover:shadow-xl
//                     group
//                   `}
//                 >
//                   <Timer className="w-8 h-8 mb-3 text-blue-500 group-hover:text-blue-600 transition-colors" />
//                   <span className="text-2xl font-semibold text-gray-700 group-hover:text-gray-900">
//                     {time} mins
//                   </span>
//                   <span className="text-sm text-gray-400 mt-1">
//                     {time === 5 ? 'Bullet' : time <= 15 ? 'Blitz' : 'Rapid'}
//                   </span>
//                 </button>
//               ))}
//             </div>
//           </CardContent>
//         </Card>
//       </div>

//       {/* If there's a countdown after MATCH_OK */}
//       {countdown !== null && (
//         <div className="fixed bottom-4 right-4 bg-white shadow-md p-4 rounded">
//           <p className="text-xl font-semibold">Match confirmed!</p>
//           <p>Starting in {countdown} seconds...</p>
//         </div>
//       )}

//       {/* Searching for match / opponent info modal */}
//       {showModal && (
//         <div className="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
//           <div className="bg-white rounded-xl shadow-2xl p-8 w-full max-w-xl mx-4 animate-fadeIn">
//             {matchFound && opponentInfo ? (
//               // Match found view
//               <>
//                 <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">Match Found!</h2>
//                 <div className="bg-blue-50 rounded-lg p-6 mb-6">
//                   <h3 className="text-lg font-semibold text-gray-700 mb-2">Opponent Details</h3>
//                   <div className="flex items-center justify-between">
//                     <div>
//                       <p className="text-gray-700 mb-1">
//                         <span className="font-medium">ID:</span> {opponentInfo.userId}
//                       </p>
//                       {/* Add more opponent details here as they become available */}
//                       <p className="text-gray-700 mb-1">
//                         <span className="font-medium">Time Control:</span> {selectedTime} minutes
//                       </p>
//                     </div>
//                     {/* Could add opponent avatar here */}
//                   </div>
//                 </div>
//                 <div className="flex justify-center space-x-4">
//                   <button
//                     onClick={handleCancel}
//                     className="px-6 py-3 bg-gray-200 text-gray-800 font-medium rounded-lg hover:bg-gray-300 transition-colors"
//                   >
//                     Decline
//                   </button>
//                   <button
//                     onClick={handleConfirm}
//                     className="px-6 py-3 bg-blue-500 text-white font-medium rounded-lg hover:bg-blue-600 transition-colors"
//                   >
//                     Accept Match
//                   </button>
//                 </div>
//               </>
//             ) : (
//               // Searching view
//               <>
//                 <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">Finding a Match</h2>
//                 <div className="flex flex-col items-center mb-8">
//                   <div className="flex items-center justify-center h-24 w-24 bg-blue-50 rounded-full mb-6">
//                     <Loader2 className="h-12 w-12 text-blue-500 animate-spin" />
//                   </div>
//                   <p className="text-gray-600 text-center">
//                     Searching for players with {selectedTime} minute time control...
//                   </p>
//                 </div>
//                 <div className="flex justify-center">
//                   <button
//                     onClick={handleCancel}
//                     className="px-6 py-3 bg-gray-200 text-gray-800 font-medium rounded-lg hover:bg-gray-300 transition-colors"
//                   >
//                     Cancel
//                   </button>
//                 </div>
//               </>
//             )}
//           </div>
//         </div>
//       )}
//     </div>
//   );
// }




















