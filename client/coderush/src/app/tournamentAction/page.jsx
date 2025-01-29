'use client';
import React, { useEffect, useState, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { increment, decrement } from '../redux/slices/exampleSlice';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function TournamentAction() {
  const [tournamentId, setTournamentId] = useState('');
  const [index, setIndex] = useState(0);
  const [userOutput, setUserOutput] = useState('');
  const [submissionResult, setSubmissionResult] = useState(null);
  const [connected, setConnected] = useState(false);

  const stompClientRef = useRef(null);
  const dispatch = useDispatch();
  const count = useSelector((state) => state.example.value);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClientRef.current = stompClient;

    stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
      console.log('Connected to WebSocket');
      setConnected(true);

      stompClient.subscribe(`/topic/tournament/submit/${localStorage.getItem('username')}/${index}`, (message) => {
        setSubmissionResult(JSON.parse(message.body));
      });
    }, (error) => {
      console.error('WebSocket connection error:', error);
      setConnected(false);
    });

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.disconnect();
      }
    };
  }, [index]);

  const handleGetQuestion = () => {
    if (!connected || !stompClientRef.current) {
      console.error('STOMP client is not connected');
      return;
    }
    const token = localStorage.getItem('token');
    stompClientRef.current.send(
      '/app/tournament/getQuestion',
      { Authorization: `Bearer ${token}` },
      `${tournamentId}/${index}`
    );
  };

  const handleSubmit = () => {
    if (!connected || !stompClientRef.current) {
      console.error('STOMP client is not connected');
      return;
    }
    const token = localStorage.getItem('token');
    const now = Date.now();
    const requestBody = {
      index: parseInt(index, 10),
      tournamentId: parseInt(tournamentId, 10),
      submissionTime: now,
      userOutput,
    };
    stompClientRef.current.send(
      '/app/tournament/submit',
      { Authorization: `Bearer ${token}` },
      JSON.stringify(requestBody)
    );
  };

  return (
    <div>
      <input
        type="text"
        placeholder="Tournament ID"
        value={tournamentId}
        onChange={(e) => setTournamentId(e.target.value)}
      />
      <input
        type="number"
        placeholder="Index"
        value={index}
        onChange={(e) => setIndex(e.target.value)}
      />

      <button onClick={handleGetQuestion}>
        Get Question
      </button>

      <textarea
        placeholder="User output"
        value={userOutput}
        onChange={(e) => setUserOutput(e.target.value)}
      />
      <button onClick={handleSubmit}>
        Submit
      </button>

      {submissionResult && (
        <div>Submission Result: {submissionResult.toString()}</div>
      )}

      <div>
        <h3>Redux Example</h3>
        <p>Count: {count}</p>
        <button onClick={() => dispatch(increment())}>Increment</button>
        <button onClick={() => dispatch(decrement())}>Decrement</button>
      </div>
    </div>
  );
}