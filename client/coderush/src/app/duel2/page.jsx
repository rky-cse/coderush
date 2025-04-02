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

//   const removeUserFromRedis = () => {
//     if (!token) return;
//     console.log("Removing user from Redis <----------");
//     webSocketService.send('/app/match/remove',  userId );
//   }



//   useEffect(() => {
//     const handleBeforeUnload = (e) => {
//       sendCancelRequest();
//       // e.preventDefault();
//       // e.returnValue
//       //   = 'Are you sure you want to leave? You will be removed from the match queue.';
//     };
  
//     window.addEventListener('beforeunload', handleBeforeUnload);
//     return () => {
//       window.removeEventListener('beforeunload', handleBeforeUnload);
//     };
//   }, [searchingForMatch]);





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
//         { userId, rating: 0, requestTime: time, timeControl: time },
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

  

//   // handleCancel function when you are cancelling based on clicks asycnronously
//   const handleCancel = () => {
//     if (searchingForMatch && !matchFound) {
      

//       setSearchingForMatch(false);
//       setShowModal(false);
//       setSelectedTime(null);
//       return;
//     }
//     if (!token) return;
    
//     webSocketService.send('/app/match/cancel', { pendingMatchId, userId });

//     resetMatchState();
//   };

//   // sendCancelBeacon function when you are cancelling based on page refresh synchronously
//   const sendCancelRequest = () => {
//     // If the user is still searching and hasn't found a match, remove them from the queue only.
//     if (searchingForMatch && !matchFound) {
//       console.log("Removing user from Redis on page refresh (no match found) with fetch keepalive");
//       fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/match/remove`, {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         body: JSON.stringify(userId),
//         keepalive: true,
//         credentials: 'include'
//       });
//       setSearchingForMatch(false);
//       setShowModal(false);
//       setSelectedTime(null);
//       return;
//     }
    
//     if (!token || !pendingMatchId) return;
    
//     // Cancel the pending match.
//     console.log("Canceling pending match on page refresh with fetch keepalive");
//     fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/match/cancel`, {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//         'Authorization': `Bearer ${token}`
//       },
//       body: JSON.stringify({ pendingMatchId, userId }),
//       keepalive: true,
//       credentials: 'include'
//     });
    
//     // Also remove the user from the queue.
//     console.log("Removing user from Redis on page refresh with fetch keepalive");
//     fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/match/remove`, {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//         'Authorization': `Bearer ${token}`
//       },
//       body: JSON.stringify(userId),
//       keepalive: true,
//       credentials: 'include'
//     });
    
//     resetMatchState();
//   };
  
  




//   return (
//     <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-8 relative overflow-hidden">
//       {/* Animated background elements */}
//       <div className="absolute inset-0 opacity-20">
//         <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-gradient-to-r from-blue-200/30 to-purple-200/30 rounded-full blur-3xl animate-pulse"></div>
//         <div className="absolute top-1/3 right-1/4 w-64 h-64 bg-indigo-200/20 rounded-full blur-2xl animate-pulse delay-1000"></div>
//         <div className="absolute bottom-20 left-1/3 w-48 h-48 bg-purple-200/20 rounded-full blur-xl animate-pulse delay-1500"></div>
//       </div>

//       <div className="max-w-5xl mx-auto relative z-10">
//         <div className="text-center mb-12 space-y-4">
//           <div className="inline-flex items-center justify-center gap-4 bg-white/80 px-8 py-4 rounded-full border border-indigo-100 mb-8 backdrop-blur-sm">
//             <Sparkles className="w-8 h-8 text-indigo-500 animate-pulse" />
//             <h1 className="text-5xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent">
//               Code Duel Arena
//             </h1>
//             <Sparkles className="w-8 h-8 text-blue-500 animate-pulse" />
//           </div>
//           <p className="text-xl text-indigo-600/80 font-medium">
//             Where coding skills clash in epic timed battles
//           </p>
//         </div>

//         <Card className="bg-white/90 backdrop-blur-lg shadow-lg border border-indigo-100 hover:border-indigo-200 transition-all duration-300 rounded-2xl">
//           <CardContent className="p-10">
//             <h2 className="text-2xl font-semibold text-center mb-10 text-indigo-800">
//               Choose Your Battle Duration
//             </h2>
//             <div className="grid grid-cols-3 gap-6">
//               {timeControls.map((time) => (
//                 <button
//                   key={time}
//                   onClick={() => handleTimeControlClick(time)}
//                   className={`
//                                     group relative h-44 rounded-xl overflow-hidden
//                                     border ${selectedTime === time ? 'border-indigo-400' : 'border-indigo-100'}
//                                     hover:border-indigo-300 transition-all duration-300
//                                     bg-gradient-to-b from-white to-indigo-50
//                                     shadow-md hover:shadow-lg
//                                     before:absolute before:inset-0 before:bg-gradient-to-br 
//                                     before:from-indigo-100/30 before:to-blue-100/30 before:opacity-0
//                                     hover:before:opacity-100 before:transition-opacity
//                                 `}
//                 >
//                   <div className="relative z-10 flex flex-col items-center justify-center h-full p-6">
//                     <div className="mb-3 flex items-center gap-2">
//                       <Timer className="w-6 h-6 text-indigo-500 group-hover:text-indigo-600 transition-colors" />
//                       <span className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent">
//                         {time}
//                       </span>
//                     </div>
//                     <span className="text-sm font-medium text-indigo-500/80 group-hover:text-indigo-600 transition-colors">
//                       {time === 5 ? 'Lightning Round' : time <= 15 ? 'Speed Challenge' : 'Strategic Combat'}
//                     </span>
//                   </div>
//                 </button>
//               ))}
//             </div>
//           </CardContent>
//         </Card>
//       </div>

//       {/* Countdown Timer */}
//       {countdown !== null && (
//         <div className="fixed bottom-8 right-8 bg-white/90 backdrop-blur-sm shadow-lg p-4 rounded-xl border border-indigo-100">
//           <div className="flex items-center gap-3">
//             <div className="p-2 bg-indigo-100 rounded-full">
//               <Clock className="w-5 h-5 text-indigo-600 animate-pulse" />
//             </div>
//             <div>
//               <p className="text-sm font-medium text-indigo-700">Starting in</p>
//               <p className="text-xl font-bold text-indigo-800">{countdown}s</p>
//             </div>
//           </div>
//         </div>
//       )}

//       {/* Modal */}
//       {showModal && (
//         <div className="fixed inset-0 flex items-center justify-center bg-indigo-900/30 backdrop-blur-sm z-50">
//           <div className="relative bg-white/90 rounded-2xl shadow-2xl p-8 w-full max-w-xl mx-4 animate-modal-enter border border-indigo-100 overflow-hidden">
//             {/* Water Ripple Effect */}
//             {searchingForMatch && !matchFound && (
//               <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
//                 <div className="water-ripple animate-ripple-1"></div>
//                 <div className="water-ripple animate-ripple-2"></div>
//                 <div className="water-ripple animate-ripple-3"></div>
//               </div>
//             )}

//             <div className="relative z-10">
//               {matchConfirmed ? (
//                 <div className="text-center space-y-8">
//                   <Sparkles className="w-16 h-16 text-indigo-400 mx-auto animate-sparkle" />
//                   <h2 className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent">
//                     Duel Initiated!
//                   </h2>
//                   <div className="space-y-6">
//                     <div className="grid grid-cols-3 items-center gap-6 px-8">
//                       <div className="flex items-center gap-4 justify-end">
//                         <User className="w-8 h-8 text-indigo-400" />
//                         <span className="text-lg text-indigo-700">You</span>
//                       </div>
//                       <div className="text-center py-4 bg-indigo-50 rounded-xl">
//                         <span className="text-2xl font-bold text-indigo-600">{selectedTime}</span>
//                         <span className="text-indigo-500 ml-2">mins</span>
//                       </div>
//                       <div className="flex items-center gap-4">
//                         <span className="text-lg text-indigo-700">Opponent</span>
//                         <User className="w-8 h-8 text-blue-400" />
//                       </div>
//                     </div>
//                     <div className="h-2 bg-indigo-100 rounded-full overflow-hidden">
//                       <div
//                         className="h-full bg-gradient-to-r from-indigo-400 to-blue-400 transition-all duration-1000 ease-out"
//                         style={{ width: `${(10 - countdown) * 10}%` }}
//                       ></div>
//                     </div>
//                   </div>
//                 </div>
//               ) : matchFound && opponentInfo && !waitingForOpponent ? (
//                 <div className="space-y-8">
//                   <div className="text-center space-y-4">
//                     <div className="inline-flex relative">
//                       <Sword className="w-12 h-12 text-indigo-500 animate-clash" />
//                       <Sword className="w-12 h-12 text-blue-500 animate-clash-reverse -ml-4" />
//                     </div>
//                     <h2 className="text-3xl font-bold text-indigo-800">Opponent Located!</h2>
//                     <p className="text-indigo-600/80">Prepare for an epic coding showdown</p>
//                   </div>
//                   <div className="grid grid-cols-2 gap-8">
//                     <div className="bg-indigo-50/80 p-6 rounded-xl border border-indigo-200">
//                       <div className="flex items-center gap-4 mb-4">
//                         <div className="p-3 bg-indigo-100 rounded-xl">
//                           <User className="w-6 h-6 text-indigo-500" />
//                         </div>
//                         <div>
//                           <p className="text-sm text-indigo-500">Your ID</p>
//                           <p className="font-semibold text-indigo-700">{userId}</p>
//                         </div>
//                       </div>
//                       <div className="h-px bg-indigo-200/50 my-4" />
//                       <div className="flex items-center gap-4">
//                         <div className="p-3 bg-blue-100 rounded-xl">
//                           <Timer className="w-6 h-6 text-blue-500" />
//                         </div>
//                         <div>
//                           <p className="text-sm text-blue-500">Time Control</p>
//                           <p className="font-semibold text-blue-700">{selectedTime} mins</p>
//                         </div>
//                       </div>
//                     </div>
//                     <div className="bg-indigo-50/80 p-6 rounded-xl border border-blue-200 relative">
//                       <div className="absolute top-4 right-4 text-xs px-2 py-1 bg-blue-100 text-blue-600 rounded-full">
//                         Opponent
//                       </div>
//                       <div className="flex items-center gap-4 mb-6">
//                         <div className="p-3 bg-blue-100 rounded-xl">
//                           <User className="w-6 h-6 text-blue-500" />
//                         </div>
//                         <div>
//                           <p className="text-sm text-blue-500">Player ID</p>
//                           <p className="font-semibold text-blue-700">{opponentInfo.userId}</p>
//                         </div>
//                       </div>
//                       <div className="h-px bg-indigo-200/50 my-4" />
//                       <div className="text-center">
//                         <p className="text-sm text-indigo-500">Ready Status</p>
//                         <div className="inline-flex items-center gap-2 mt-2">
//                           <div className="w-3 h-3 bg-emerald-400/80 rounded-full animate-pulse"></div>
//                           <span className="text-emerald-600 text-sm">Waiting</span>
//                         </div>
//                       </div>
//                     </div>
//                   </div>
//                   <div className="flex justify-center gap-6">
//                     <button
//                       onClick={handleCancel}
//                       className="px-8 py-3.5 bg-gray-100 hover:bg-gray-200 border border-gray-300 rounded-xl text-gray-700 hover:text-gray-800 transition-all duration-300"
//                     >
//                       Decline
//                     </button>
//                     <button
//                       onClick={handleConfirm}
//                       className="px-8 py-3.5 bg-gradient-to-b from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 border border-indigo-400 rounded-xl text-white transition-all duration-300"
//                     >
//                       Accept Challenge
//                     </button>
//                   </div>
//                 </div>
//               ) : waitingForOpponent ? (
//                 <div className="text-center space-y-8">
//                   <div className="relative inline-block">
//                     <div className="w-32 h-32 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
//                       <User className="w-16 h-16 text-indigo-400/60" />
//                     </div>
//                     <div className="absolute top-0 right-0 -mr-4 -mt-4">
//                       <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center animate-ping-slow">
//                         <div className="w-8 h-8 bg-blue-200 rounded-full"></div>
//                       </div>
//                     </div>
//                   </div>
//                   <h2 className="text-3xl font-bold text-indigo-800">Awaiting Confirmation</h2>
//                   <p className="text-indigo-600/80 px-12">Your challenge has been issued...</p>
//                   <button
//                     onClick={handleCancel}
//                     className="px-8 py-3.5 bg-gray-100 hover:bg-gray-200 border border-gray-300 rounded-xl text-gray-700 hover:text-gray-800 transition-all duration-300"
//                   >
//                     Retract Challenge
//                   </button>
//                 </div>
//               ) : (
//                 <div className="text-center space-y-8">
//                   <h2 className="text-3xl font-bold text-indigo-800">Scanning the Arena</h2>
//                   <p className="text-indigo-600/80 px-12">
//                     Seeking worthy opponents for {selectedTime}-minute duel...
//                   </p>
//                   <div className="flex justify-center gap-4">
//                     <div className="flex items-center gap-2 text-indigo-500/80 text-sm">
//                       <div className="w-2 h-2 bg-indigo-400 rounded-full animate-pulse"></div>
//                       Analyzing player skills
//                     </div>
//                     <div className="flex items-center gap-2 text-blue-500/80 text-sm">
//                       <div className="w-2 h-2 bg-blue-400 rounded-full animate-pulse delay-300"></div>
//                       Matching time controls
//                     </div>
//                   </div>
//                   <div className="pt-8">
//                   <button
//                     onClick={() => {
//                       handleCancel();

//                     }}
//                     className="px-8 py-3.5 bg-gray-100 hover:bg-gray-200 border border-gray-300 rounded-xl text-gray-700 hover:text-gray-800 transition-all duration-300"
//                   >
//                     Abandon Search
//                   </button>
//                   </div>
//                 </div>
//               )}
//             </div>
//           </div>
//         </div>
//       )}

//       <style jsx global>{`
//             @keyframes ripple {
//                 0% {
//                     transform: scale(0);
//                     opacity: 1;
//                     border: 2px solid rgba(99, 102, 241, 0.8);
//                 }
//                 100% {
//                     transform: scale(3);
//                     opacity: 0;
//                     border: 2px solid rgba(99, 102, 241, 0);
//                 }
//             }
//             .water-ripple {
//                 position: absolute;
//                 width: 100px;
//                 height: 100px;
//                 border-radius: 50%;
//                 box-shadow: 0 0 20px rgba(99, 102, 241, 0.3);
//             }
//             .animate-ripple-1 {
//                 animation: ripple 2s infinite;
//             }
//             .animate-ripple-2 {
//                 animation: ripple 2s infinite 0.5s;
//             }
//             .animate-ripple-3 {
//                 animation: ripple 2s infinite 1s;
//             }
//             @keyframes modal-enter {
//                 from { transform: scale(0.96) translateY(20px); opacity: 0; }
//                 to { transform: scale(1) translateY(0); opacity: 1; }
//             }
//             .animate-modal-enter {
//                 animation: modal-enter 0.3s cubic-bezier(0.4, 0, 0.2, 1);
//             }
//             @keyframes sparkle {
//                 0% { opacity: 0; transform: scale(0); }
//                 50% { opacity: 1; transform: scale(1); }
//                 100% { opacity: 0; transform: scale(1.5); }
//             }
//             .animate-sparkle {
//                 animation: sparkle 1.5s infinite;
//             }
//             @keyframes clash {
//                 0% { transform: translateX(0); }
//                 50% { transform: translateX(4px); }
//                 100% { transform: translateX(0); }
//             }
//             .animate-clash {
//                 animation: clash 0.8s ease-in-out infinite;
//             }
//             .animate-clash-reverse {
//                 animation: clash 0.8s ease-in-out infinite reverse;
//             }
//             .animate-ping-slow {
//                 animation: ping 3s cubic-bezier(0, 0, 0.2, 1) infinite;
//             }
//             @keyframes ping {
//                 75%, 100% { transform: scale(2); opacity: 0; }
//             }
//         `}</style>
//     </div>
//   );
// }



'use client';
import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { Card, CardContent } from '@/components/ui/card';
import { Timer, Loader2, AlertCircle, Check, Clock, User, Sword, Sparkles, Zap } from 'lucide-react';
import webSocketService from '@/services/webSocketService';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';

export default function DuelPage() {
  const timeControls = [5, 15, 25, 45, 60, 75, 90, 100, 120];
  const router = useRouter();

  const token = getCookie('token');
  const user = useSelector((state) => state.user);
  const userId = user.id;

  const [selectedTime, setSelectedTime] = useState(null);
  const [pendingMatchId, setPendingMatchId] = useState(null);
  const [opponentInfo, setOpponentInfo] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [countdown, setCountdown] = useState(null);
  const [searchingForMatch, setSearchingForMatch] = useState(false);
  const [matchFound, setMatchFound] = useState(false);
  const [waitingForOpponent, setWaitingForOpponent] = useState(false);
  const [matchConfirmed, setMatchConfirmed] = useState(false);

  const wsUrl = `${process.env.NEXT_PUBLIC_API_URL}/ws`;


  // Connect to WebSocket and subscribe to match notifications
  // This is the first thing that happens when the component is mounted
  // Basically helps you to communicate via websockets
  useEffect(() => {
    if (!token || !userId) {
      console.log('User ID is not available yet.');
      return;
    }
    console.log('Connecting to WebSocket with token:', token);
    webSocketService.connect(wsUrl, token);
    webSocketService.subscribe('/user/queue/match-notifications', (message) => {
      console.log('Received WS message:', message);
      handleWebSocketMessage(message);
    });
    return () => {
      webSocketService.unsubscribe('/user/queue/match-notifications');
    };
  }, [token, userId]);




  // Save pendingMatchId to localStorage when it changes
  // Here i am stroing the pendingMatchId in the local storage so that it can be used even after the page refresh
  useEffect(() => {
    if (pendingMatchId) {
      localStorage.setItem('pendingMatchId', pendingMatchId);
    } else {
      localStorage.removeItem('pendingMatchId');
    }
  }, [pendingMatchId]);


  // Edge Case Alert !!!
  // Main Purpose : To remove the user from the queue and cancel the pending match on page refresh
  // Suppose two users found a match and one of them refreshes the page, the other user should be informed and the pending match should be cancelled.
  // This sendCancelRequest function is implemented and is ran when the user refreshes the page
  // This function is called when the user refreshes the page, it sends a fetch request(with keepalive) to the server to cancel the pending match
  useEffect(() => {
    const handleBeforeUnload = (e) => {
      sendCancelRequest();
      // Uncomment below if you want to show a confirmation dialog
      // e.preventDefault();
      // e.returnValue = 'Are you sure you want to leave? You will be removed from the match queue.';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [searchingForMatch, pendingMatchId, token]);



  // Handle incoming WebSocket messages
  // Incoming messages are of the form { status, matchId, player1Id, player2Id, startTime, pendingMatchId }
  // Now status are the things to look at, it can have values like MATCH_FOUND, MATCH_CANCELLED, MATCH_OK, MATCH_CREATED
  // This function is called when the server sends a message to the client via websockets
  // Takes the corresponding action based on the status of the message
  const handleWebSocketMessage = (msg) => {
    const { status, matchId, player1Id, player2Id, startTime, pendingMatchId } = msg;
    console.log("Status:", status, "MatchID:", matchId, "Player1ID:", player1Id, "Player2ID:", player2Id, "StartTime:", startTime, "PendingMatchID:", pendingMatchId);
    switch (status) {
      case 'MATCH_FOUND': {
        setPendingMatchId(pendingMatchId); // localStorage updated in useEffect
        setMatchFound(true);
        
        // Why my userID is getting null at this point ??
        const opponent = (player1Id == userId) ? player2Id : player1Id;
        console.log("Opponent:", opponent, "Player1ID:", player1Id, "Player2ID:", player2Id, "UserID:", userId);
        setOpponentInfo({ userId: opponent });
        setShowModal(true);
        break;
      }
      case 'MATCH_CANCELLED': {
        alert('Opponent cancelled the match.');
        resetMatchState();
        break;
      }
      case 'MATCH_OK': {
        setWaitingForOpponent(false);
        setMatchConfirmed(true);
        break;
      }
      case 'MATCH_CREATED': {
        setCountdown(10);
        console.log("Redirecting to gameboard with ID" , matchId);
        console.log("Match ID:", matchId, "Pending Match ID:", pendingMatchId, "User ID:", userId );
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


  // Reset the match state to initial values
  // This function is called when the match is cancelled or completed
  const resetMatchState = () => {
    setPendingMatchId(null);
    setOpponentInfo(null);
    setShowModal(false);
    setCountdown(null);
    setSelectedTime(null);
    setSearchingForMatch(false);
    setMatchFound(false);
    setWaitingForOpponent(false);
    setMatchConfirmed(false);
    localStorage.removeItem('pendingMatchId');
  };



  // This is the staring point of the match request
  // This function is called when you click on the time control button, it sends the request to the server and than server adds the user to the queue and does it's part.
  // This function is calling a POST request, all communication after this is done via websockets
  // This function is also setting the state to show the modal and searching for match
  const handleTimeControlClick = async (time) => {
    setSelectedTime(time);
    setSearchingForMatch(true);
    setShowModal(true);
    console.log('handleTimeControlClick', time, userId, token);
    if (!token) {
      alert('You are not logged in or have no token. Cannot request match.');
      setSearchingForMatch(false);
      setShowModal(false);
      return;
    }
    try {
      await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/api/match/request`,
        { userId, rating: 0, requestTime: time, timeControl: time },
        { headers: { Authorization: `Bearer ${token}` } }
      );
    } catch (error) {
      console.error('Error requesting match:', error);
      alert('Failed to request match. See console for details.');
      setSearchingForMatch(false);
      setShowModal(false);
    }
  };



  // Basic function to handle the confirm request when you are confirming the match you found
  // This function is called when you click the "Accept Challenge" button in the modal
  const handleConfirm = () => {
    if (!pendingMatchId || !token) return;
    console.log('Sending confirm for match:', pendingMatchId);
    webSocketService.send('/app/match/confirm', { pendingMatchId, userId });
    setWaitingForOpponent(true);
  };



  // handleCancel function when you are cancelling based on clicks asynchronously
  // This handles the cancel request when the user clicks the "Decline"/"Abandon" button when you found a match or are searching
  // Easier to implement because all the state information is available in the component
  const handleCancel = () => {
    if (searchingForMatch && !matchFound) {
      webSocketService.send('/app/match/remove', userId);
      setSearchingForMatch(false);
      setShowModal(false);
      setSelectedTime(null);
      return;
    }
    if (!token) return;
    
    webSocketService.send('/app/match/cancel', { pendingMatchId, userId });
    resetMatchState();
  };




  // Edge Case Alert !!!
  // Main Purpose : To remove the user from the queue and cancel the pending match on page refresh
  // When you are cancelling based on page refresh you send this fetch request synchronously
  // Maybe This can also work if we use websocket to send the cancel request on refreshing as well
  // But earlier due to some other errors I encountered I thought of using fetch as GPT was insisting on fetch with keepalive
  // Maybe in future we can try to use websocket to send the cancel request on page refresh
  // Currently this is working fine with fetch keepalive and it is removing the user from the queue and cancelling the pending match 
  // To make this happen i had 2 choices : Store the pendingMatchId in localStorage and second one being to map the pendingMatchId to the userId in Redis/In-memory/...
  // I chose the first one as it was easier to implement and also it was working fine
  const sendCancelRequest = () => {
    // If the user is still searching and hasn't found a match, remove them from the queue only.
    if (searchingForMatch && !matchFound) {
      console.log("Removing user from Redis on page refresh (no match found) with fetch keepalive");
      fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/match/remove`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(userId),
        keepalive: true,
        credentials: 'include'
      });
      setSearchingForMatch(false);
      setShowModal(false);
      setSelectedTime(null);
      return;
    }
    
    if (!token) return;
    
    // Retrieve the pendingMatchId from localStorage
    const storedPendingMatchId = localStorage.getItem('pendingMatchId');
    if (storedPendingMatchId) {
      console.log("Canceling pending match on page refresh with fetch keepalive");
      fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/match/cancel`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ pendingMatchId: storedPendingMatchId, userId }),
        keepalive: true,
        credentials: 'include'
      });
      
      console.log("Removing user from Redis on page refresh with fetch keepalive");
      fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/match/remove`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(userId),
        keepalive: true,
        credentials: 'include'
      });
    }
    
    resetMatchState();
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
            <h2 className="text-2xl font-semibold text-center mb-10 text-indigo-800">
              Choose Your Battle Duration
            </h2>
            <div className="grid grid-cols-3 gap-6">
              {timeControls.map((time) => (
                <button
                  key={time}
                  onClick={() => handleTimeControlClick(time)}
                  className={`
                    group relative h-44 rounded-xl overflow-hidden
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
                      <div className="flex items-center gap-4 justify-end">
                        <User className="w-8 h-8 text-indigo-400" />
                        <span className="text-lg text-indigo-700">You</span>
                      </div>
                      <div className="text-center py-4 bg-indigo-50 rounded-xl">
                        <span className="text-2xl font-bold text-indigo-600">{selectedTime}</span>
                        <span className="text-indigo-500 ml-2">mins</span>
                      </div>
                      <div className="flex items-center gap-4">
                        <span className="text-lg text-indigo-700">Opponent</span>
                        <User className="w-8 h-8 text-blue-400" />
                      </div>
                    </div>
                    <div className="h-2 bg-indigo-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-indigo-400 to-blue-400 transition-all duration-1000 ease-out"
                        style={{ width: `${(10 - countdown) * 10}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              ) : matchFound && opponentInfo && !waitingForOpponent ? (
                <div className="space-y-8">
                  <div className="text-center space-y-4">
                    <div className="inline-flex relative">
                      <Sword className="w-12 h-12 text-indigo-500 animate-clash" />
                      <Sword className="w-12 h-12 text-blue-500 animate-clash-reverse -ml-4" />
                    </div>
                    <h2 className="text-3xl font-bold text-indigo-800">Opponent Located!</h2>
                    <p className="text-indigo-600/80">Prepare for an epic coding showdown</p>
                  </div>
                  <div className="grid grid-cols-2 gap-8">
                    <div className="bg-indigo-50/80 p-6 rounded-xl border border-indigo-200">
                      <div className="flex items-center gap-4 mb-4">
                        <div className="p-3 bg-indigo-100 rounded-xl">
                          <User className="w-6 h-6 text-indigo-500" />
                        </div>
                        <div>
                          <p className="text-sm text-indigo-500">Your ID</p>
                          <p className="font-semibold text-indigo-700">{userId}</p>
                        </div>
                      </div>
                      <div className="h-px bg-indigo-200/50 my-4" />
                      <div className="flex items-center gap-4">
                        <div className="p-3 bg-blue-100 rounded-xl">
                          <Timer className="w-6 h-6 text-blue-500" />
                        </div>
                        <div>
                          <p className="text-sm text-blue-500">Time Control</p>
                          <p className="font-semibold text-blue-700">{selectedTime} mins</p>
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
                        <div>
                          <p className="text-sm text-blue-500">Player ID</p>
                          <p className="font-semibold text-blue-700">{opponentInfo.userId}</p>
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
                      className="px-8 py-3.5 bg-gradient-to-b from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 border border-indigo-400 rounded-xl text-white transition-all duration-300"
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
                  <p className="text-indigo-600/80 px-12">Your challenge has been issued...</p>
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
                    Seeking worthy opponents for {selectedTime}-minute duel...
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
