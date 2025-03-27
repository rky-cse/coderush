'use client';
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setQuestion, clearQuestion } from '@/redux/slices/questionSlice';
import { setTestcase, clearTestcase } from '@/redux/slices/testcaseSlice';
import webSocketService from '@/services/webSocketService';
import { getCookie } from 'cookies-next';
import { FaQuestionCircle, FaClipboardList, FaInfoCircle, FaCode, FaFileAlt } from 'react-icons/fa';

// Component for section headers with consistent styling
const SectionHeader = ({ icon: Icon, title }) => (
  <div className="flex items-center border-b border-gray-200 dark:border-gray-700 pb-3 mb-4">
    <Icon className="text-xl mr-3 text-indigo-500 dark:text-indigo-400" />
    <h2 className="text-xl font-semibold tracking-tight">{title}</h2>
  </div>
);

// Component for code blocks with consistent styling
const CodeBlock = ({ label, content }) => (
  <div className="mb-4">
    <h3 className="text-md font-medium mb-2 text-gray-700 dark:text-gray-300 flex items-center">
      <FaCode className="mr-2 text-indigo-500 dark:text-indigo-400" /> {label}
    </h3>
    <pre className="p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-md text-sm font-mono text-gray-800 dark:text-gray-200 whitespace-pre-wrap overflow-auto shadow-sm">
      {content}
    </pre>
  </div>
);

// Component for the problem description
const ProblemDescription = ({ question, index }) => (
  <div className="mb-8 bg-white dark:bg-gray-900 p-6 rounded-lg shadow-sm border border-gray-100 dark:border-gray-800">
    <SectionHeader 
      icon={FaQuestionCircle} 
      title={`Q${index+1}: ${question?.name || 'Problem Statement'}`} 
    />
    <p className="text-base leading-relaxed whitespace-pre-wrap text-gray-700 dark:text-gray-300 mb-6">
      {question?.legend || 'Loading problem description...'}
    </p>
    
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <CodeBlock label="Input Format" content={question?.inputFormat || 'Loading...'} />
      <CodeBlock label="Output Format" content={question?.outputFormat || 'Loading...'} />
    </div>
    
    {question?.notes && (
      <div className="mt-6 border-l-4 border-indigo-500 dark:border-indigo-400 pl-4 bg-indigo-50 dark:bg-indigo-900/30 p-3 rounded-r-md">
        <h3 className="text-md font-medium flex items-center text-gray-700 dark:text-gray-300">
          <FaInfoCircle className="mr-2 text-indigo-500 dark:text-indigo-400" /> Note
        </h3>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">{question.notes}</p>
      </div>
    )}
  </div>
);

// Component for test cases
const TestCases = ({ testcase }) => (
  <div className="bg-white dark:bg-gray-900 p-6 rounded-lg shadow-sm border border-gray-100 dark:border-gray-800">
    <SectionHeader icon={FaClipboardList} title="Test Cases" />
    
    {testcase ? (
      <div className="space-y-5">
        <CodeBlock label="Input" content={testcase.input} />
        <CodeBlock label="Expected Output" content={testcase.expectedOutput} />
      </div>
    ) : (
      <div className="animate-pulse space-y-4">
        <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-md"></div>
        <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-md"></div>
      </div>
    )}
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

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 text-gray-800 dark:text-gray-200 transition-all duration-300 py-6 px-4">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Navigation breadcrumb */}
        <div className="text-sm text-gray-500 dark:text-gray-400 mb-4 flex items-center">
          <FaFileAlt className="mr-2" />
          <span>Tournament / Question {index + 1}</span>
        </div>
        
        {/* Main content sections */}
        <ProblemDescription question={question} index={index} />
        <TestCases testcase={testcase} />
      </div>
    </div>
  );
}