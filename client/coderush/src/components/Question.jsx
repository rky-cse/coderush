'use client';
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setQuestion, clearQuestion } from '@/redux/slices/questionSlice';
import { setTestcase, clearTestcase } from '@/redux/slices/testcaseSlice';
import webSocketService from '@/services/webSocketService';
import { getCookie } from 'cookies-next';

// Simplified section header
const SectionHeader = ({ title }) => (
  <h2 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-3">
    {title}
  </h2>
);

// Minimal code block with content focus
const CodeBlock = ({ label, content }) => (
  <div className="mb-4">
    <div className="text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">
      {label}
    </div>
    <pre className="p-2 bg-gray-50 dark:bg-gray-800 border-l-2 border-gray-300 dark:border-gray-600 text-sm font-mono text-gray-800 dark:text-gray-200 whitespace-pre-wrap overflow-auto">
      {content}
    </pre>
  </div>
);

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

  // Load state indicator
  if (!question) {
    return (
      <div className="max-w-4xl mx-auto p-4">
        <div className="animate-pulse space-y-3">
          <div className="h-6 w-1/3 bg-gray-200 dark:bg-gray-700 rounded"></div>
          <div className="h-4 w-2/3 bg-gray-200 dark:bg-gray-700 rounded"></div>
          <div className="h-4 w-full bg-gray-200 dark:bg-gray-700 rounded"></div>
          <div className="h-4 w-full bg-gray-200 dark:bg-gray-700 rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-3 py-2 text-gray-800 dark:text-gray-200">
      {/* Problem header with question number */}
      <div className="mb-2 pb-2 border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100">
          Q{index+1}: {question?.name || 'Problem Statement'}
        </h1>
        <div className="text-xs text-gray-500 dark:text-gray-400">
          Problem {index + 1}
        </div>
      </div>
      
      {/* Problem statement */}
      <div className="mb-6">
        <div className="text-base text-gray-700 dark:text-gray-300 whitespace-pre-wrap mb-6 leading-relaxed">
          {question?.legend || 'Loading problem description...'}
        </div>
        
        {/* Input/Output formats */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div>
            <SectionHeader title="Input Format" />
            <div className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap bg-gray-50 dark:bg-gray-800 p-2 rounded border border-gray-200 dark:border-gray-700">
              {question?.inputFormat || 'Not provided'}
            </div>
          </div>
          
          <div>
            <SectionHeader title="Output Format" />
            <div className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap bg-gray-50 dark:bg-gray-800 p-2 rounded border border-gray-200 dark:border-gray-700">
              {question?.outputFormat || 'Not provided'}
            </div>
          </div>
        </div>
        
        {/* Notes section - only shown if notes exist */}
        {question?.notes && (
          <div className="mt-4">
            <SectionHeader title="Notes" />
            <div className="text-sm text-gray-700 dark:text-gray-300 bg-gray-50 dark:bg-gray-800 p-2 rounded border border-gray-200 dark:border-gray-700">
              {question.notes}
            </div>
          </div>
        )}
      </div>
      
      {/* Test cases section */}
      <div className="pt-2 mt-2 border-t border-gray-200 dark:border-gray-700">
        <SectionHeader title="Test Cases" />
        
        {testcase ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <CodeBlock label="Input" content={testcase.input} />
            <CodeBlock label="Expected Output" content={testcase.expectedOutput} />
          </div>
        ) : (
          <div className="text-sm text-gray-500 dark:text-gray-400">
            Test cases not available
          </div>
        )}
      </div>
    </div>
  );
}