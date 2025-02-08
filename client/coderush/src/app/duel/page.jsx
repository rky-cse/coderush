'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import webSocketService from '@/services/webSocketService';

export default function Matchmaking() {
    const [status, setStatus] = useState('idle');
    const [match, setMatch] = useState(null);
    const [error, setError] = useState('');
    const [queueTime, setQueueTime] = useState(0);
    const router = useRouter();

    useEffect(() => {
        let interval;
        if (status === 'loading') {
            interval = setInterval(() => {
                setQueueTime((prev) => prev + 1);
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [status]);

    useEffect(() => {
        const token = typeof window !== 'undefined' ? getCookie('token') : null;

        if (token) {
            webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}/ws`, token);
            webSocketService.subscribe('/user/queue/match', async (message) => {
                console.log('Match found:', message);
                
                if( message.opponent === "YES"){
                    const response = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/tournament/joinTournament/${message.tournamentID}`, {
                        headers: {
                          Authorization: `Bearer ${token}`,
                        },
                      })
                }
                setTimeout(() => {
                    setMatch({ players: [message.player1, message.player2], matchId: message.tournamentID || 'N/A' , opponent: message.opponent});
                    setStatus('success');
                    router.push(`/tournamentPage/${message.tournamentID}`);
                }, 6000);
                
            });
        }
        return () => {
            webSocketService.unsubscribe('user/queue/match');
        };
    }, []);

    const handlePlay1v1 = async () => {
        const token = typeof window !== 'undefined' ? getCookie('token') : null;
        if (!token) {
            setError('Please log in first');
            return;
        }

        setStatus('loading');
        setError('');
        setMatch(null);
        setQueueTime(0);

        try {
            await axios.post(
                `${process.env.NEXT_PUBLIC_API_URL}/api/matchmaking/join`,
                {},
                {
                    headers: { Authorization: `Bearer ${token}` },
                }
            );
        } catch (error) {
            console.error('Matchmaking error:', error);
            setStatus('error');
            setError(error.response?.data?.message || 'Failed to join queue. Please try again.');
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-900 to-blue-900 flex items-center justify-center p-4">
            <div className="glass-container bg-white bg-opacity-10 backdrop-blur-lg rounded-2xl p-8 shadow-2xl w-full max-w-md">
                <h2 className="text-3xl font-bold text-center text-white mb-6">🎮 1v1 Duel</h2>
                
                <div className="space-y-6">
                    <button 
                        onClick={handlePlay1v1}
                        disabled={status === 'loading'}
                        className="w-full bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600 text-white font-semibold py-4 px-8 rounded-xl transition-all duration-300 transform hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {status === 'loading' ? (
                            <div className="flex items-center justify-center space-x-2">
                                <PulseLoader />
                                <span>Searching ({queueTime}s)</span>
                            </div>
                        ) : 'Find Opponent'}
                    </button>

                    {error && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg flex items-center space-x-2">
                            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clipRule="evenodd" />
                            </svg>
                            <span>{error}</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

const PulseLoader = () => (
    <div className="relative w-5 h-5">
        <div className="absolute inset-0 border-2 border-white rounded-full animate-ping"></div>
        <div className="absolute inset-0 border-2 border-white rounded-full"></div>
    </div>
);


// Add these CSS animations
<style jsx global>{`
    @keyframes match-found {
        0% { opacity: 0; transform: translateY(20px); }
        100% { opacity: 1; transform: translateY(0); }
    }
    .animate-match-found {
        animation: match-found 0.6s cubic-bezier(0.22, 1, 0.36, 1);
    }
    .glass-container {
        border: 1px solid rgba(255, 255, 255, 0.1);
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    }
`}</style>
