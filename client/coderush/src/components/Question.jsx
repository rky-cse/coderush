'use client';
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setQuestion, clearQuestion } from '@/redux/slices/questionSlice';
import { setTestcase, clearTestcase } from '@/redux/slices/testcaseSlice';
import webSocketService from '@/services/webSocketService';

export default function Question({ tournamentId }) {
  const dispatch = useDispatch();
  const question = useSelector((state) => state.question.data);
  const testcase = useSelector((state) => state.testcase.data);
  const index = useSelector((state) => state.index ?? 0); // Default to 0 if undefined
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (!token) return;

    // Establish WebSocket connection
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}/ws`, token);

    // Cleanup function to disconnect WebSocket when the component unmounts
    return () => {
      webSocketService.disconnect();
    };
  }, [token]);

  useEffect(() => {
    if (!tournamentId || index == null || index === undefined || !token) return;

    const parsedTournamentId = Number(tournamentId);
    const parsedIndex = Number(index);

    if (isNaN(parsedTournamentId) || isNaN(parsedIndex)) {
      console.error('Invalid tournamentId or index');
      return;
    }

    const destination = `/topic/tournament/getQuestionWithTestcase/${parsedTournamentId}/${parsedIndex}`;

    const handleMessage = (data) => {
      console.log('Received data:', data);
      if (data.question && data.testcase) {
        dispatch(setQuestion(data.question));
        dispatch(setTestcase(data.testcase));
      } else {
        console.error('Invalid data format:', data);
      }
    };

    // Subscribe to the new destination when the index changes
    webSocketService.subscribe(destination, handleMessage);

    // Send a new message to the WebSocket server when the index changes
    webSocketService.send('/app/tournament/getQuestionWithTestcase', `${parsedTournamentId}/${parsedIndex}`);

    // Cleanup function to unsubscribe from the previous destination
    return () => {
      webSocketService.unsubscribe(destination);
      dispatch(clearQuestion());
      dispatch(clearTestcase());
    };
  }, [tournamentId, index, token, dispatch]);

  return (
    <div className="p-4">
      <h2 className="font-bold text-xl mb-4">Question</h2>
      {!question ? (
        <p>Loading question...</p>
      ) : (
        <div className="mb-4">
          <p>Name: {question.name}</p>
          <p>Legend: {question.legend}</p>
          <p>Input Format: {question.inputFormat}</p>
          <p>Output Format: {question.outputFormat}</p>
          <p>Note: {question.notes}</p>
        </div>
      )}

      <h2 className="font-bold text-xl mb-4">Testcase</h2>
      {!testcase ? (
        <p>Loading testcase...</p>
      ) : (
        <div>
          <p>Input: {testcase.input}</p>
          <p>Expected Output: {testcase.expectedOutput}</p>
        </div>
      )}
    </div>
  );
}