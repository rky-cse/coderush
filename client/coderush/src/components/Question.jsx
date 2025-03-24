'use client';
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setQuestion, clearQuestion } from '@/redux/slices/questionSlice';
import { setTestcase, clearTestcase } from '@/redux/slices/testcaseSlice';
import webSocketService from '@/services/webSocketService';
import { getCookie } from 'cookies-next';
import { FaQuestionCircle, FaClipboardList, FaInfoCircle } from 'react-icons/fa';

export default function Question({ tournamentId }) {
  const dispatch = useDispatch();
  const question = useSelector((state) => state.question.data);
  const testcase = useSelector((state) => state.testcase.data);
  const index = useSelector((state) => state.index ?? 0);
  const token = getCookie('token');

  useEffect(() => {
    if (!token) return;
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}/ws`, token);
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
      if (data.question && data.testcase) {
        dispatch(setQuestion(data.question));
        dispatch(setTestcase(data.testcase));
      } else {
        console.error('Invalid data format:', data);
      }
    };

    webSocketService.subscribe(destination, handleMessage);
    webSocketService.send('/app/tournament/getQuestionWithTestcase', `${parsedTournamentId}/${parsedIndex}`);

    return () => {
      webSocketService.unsubscribe(destination);
    };
  }, [tournamentId, index, token, dispatch]);

  return (
    <div className="min-h-screen text-gray-800 dark:text-gray-200 transition-all duration-300">
      <div className="max-w-3xl mx-auto">
        {/* Question Section */}
        <div className="mb-6">
          <div className="flex items-center border-b border-gray-300 pb-2 mb-4">
            <FaQuestionCircle className="text-2xl mr-2 text-indigo-500" />
            <h2 className="text-2xl font-semibold">
              {question?.name || 'Problem Statement'}
            </h2>
          </div>
          <p className="text-base leading-relaxed whitespace-pre-wrap">
            {question?.legend}
          </p>
        </div>

        {/* Input/Output Formats */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          <div>
            <h3 className="text-lg font-medium flex items-center">
              <FaInfoCircle className="mr-2 text-indigo-500" /> Input Format
            </h3>
            <pre className="mt-2 p-2 bg-gray-100 rounded text-sm font-mono whitespace-pre-wrap">
              {question?.inputFormat}
            </pre>
          </div>
          <div>
            <h3 className="text-lg font-medium flex items-center">
              <FaInfoCircle className="mr-2 text-indigo-500" /> Output Format
            </h3>
            <pre className="mt-2 p-2 bg-gray-100 rounded text-sm font-mono whitespace-pre-wrap">
              {question?.outputFormat}
            </pre>
          </div>
        </div>

        {/* Note Section */}
        {question?.notes && (
          <div className="mb-6 border-l-4 border-indigo-500 pl-4">
            <h3 className="text-lg font-medium flex items-center">
              <FaInfoCircle className="mr-2" /> Note
            </h3>
            <p className="mt-2 text-sm">{question.notes}</p>
          </div>
        )}

        {/* Test Cases Section */}
        <div>
          <div className="flex items-center border-b border-gray-300 pb-2 mb-4">
            <FaClipboardList className="text-2xl mr-2 text-indigo-500" />
            <h2 className="text-2xl font-semibold">Test Cases</h2>
          </div>
          {testcase ? (
            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-medium mb-1">Input</h3>
                <pre className="p-2 bg-gray-100 rounded text-sm font-mono whitespace-pre-wrap break-all">
                  {testcase.input}
                </pre>
              </div>
              <div>
                <h3 className="text-lg font-medium mb-1">Expected Output</h3>
                <pre className="p-2 bg-gray-100 rounded text-sm font-mono whitespace-pre-wrap break-all">
                  {testcase.expectedOutput}
                </pre>
              </div>
            </div>
          ) : (
            <div className="animate-pulse space-y-4">
              <div className="h-10 bg-gray-200 rounded"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
