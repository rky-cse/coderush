import { useState, useEffect } from 'react';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useTournament = () => {
  const [tournamentId, setTournamentId] = useState('');
  const [timer, setTimer] = useState(null);
  const [isStartButtonDisabled, setIsStartButtonDisabled] = useState(true);
  const [tournamentData, setTournamentData] = useState(null);
  const [questionData, setQuestionData] = useState(null);
  const [error, setError] = useState(null);
  const [client, setClient] = useState(null);

  useEffect(() => {
    return () => client?.deactivate(); // Cleanup WebSocket connection
  }, [client]);

  const handleJoinTournament = async () => {
    setError(null);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `${process.env.API_URL}/api/tournament/joinTournament/${tournamentId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (response.data?.tournament?.startTime) {
        startCountdown(response.data.tournament.startTime);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred');
    }
  };

  const startCountdown = (startTime) => {
    const targetTime = new Date(startTime).getTime();
    const interval = setInterval(() => {
      const currentTime = new Date().getTime();
      const timeLeft = targetTime - currentTime;
      if (timeLeft <= 0) {
        clearInterval(interval);
        setTimer('00:00:00');
        setIsStartButtonDisabled(false);
      } else {
        const hours = Math.floor((timeLeft / (1000 * 60 * 60)) % 24);
        const minutes = Math.floor((timeLeft / (1000 * 60)) % 60);
        const seconds = Math.floor((timeLeft / 1000) % 60);
        setTimer(`${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`);
      }
    }, 1000);
  };

  const handleStartTournament = () => {
    if (!isStartButtonDisabled) {
      const token = localStorage.getItem('token');
      const stompClient = new Client({
        webSocketFactory: () => new SockJS(`${process.env.API_URL}/ws`),
        connectHeaders: { Authorization: `Bearer ${token}` },
        onConnect: () => {
          stompClient.subscribe(`/topic/rankList${tournamentId}`, (message) => {
            setTournamentData(JSON.parse(message.body));
          });
          stompClient.subscribe(`/topic/tournament/getQuestionWithTestcase/${tournamentId}/0`, (message) => {
            setQuestionData(JSON.parse(message.body));
          });
        },
      });
      stompClient.activate();
      setClient(stompClient);
    }
  };

  return {
    tournamentId,
    setTournamentId,
    timer,
    isStartButtonDisabled,
    handleJoinTournament,
    handleStartTournament,
    tournamentData,
    questionData,
    error,
  };
};
