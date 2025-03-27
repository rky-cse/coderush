'use client';
import React, { useState, useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getCookie } from 'cookies-next';
import TournamentControlBox from '@/components/TournamentControlBox';
import Question from '@/components/Question';
import CodeEditor from '@/components/CodeEditor';
import IOBox from '@/components/IOBox';
import ControlButtons from '@/components/ControlButtons';
import webSocketService from '@/services/webSocketService';
import { FaChevronUp, FaChevronDown } from 'react-icons/fa';

export default function TournamentPage({ params: paramsPromise }) {
  // Unwrap the params promise with React.use()
  const params = React.use(paramsPromise);
  if (!params) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8 bg-white rounded-lg shadow-md">
          <h2 className="text-2xl font-bold text-red-600 mb-2">Error</h2>
          <p className="text-gray-700">Missing parameters. Please try again.</p>
        </div>
      </div>
    );
  }

  const { tournamentId } = params;
  if (!tournamentId) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8 bg-white rounded-lg shadow-md">
          <h2 className="text-2xl font-bold text-red-600 mb-2">Error</h2>
          <p className="text-gray-700">Tournament ID is missing. Please try again.</p>
        </div>
      </div>
    );
  }

  // Get token and ensure it exists
  const token = typeof window !== 'undefined' ? getCookie('token') : null;
  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8 bg-white rounded-lg shadow-md">
          <h2 className="text-2xl font-bold text-red-600 mb-2">Access Denied</h2>
          <p className="text-gray-700 mb-4">You must be logged in to access this page.</p>
          <a href="/login" className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors">
            Go to Login
          </a>
        </div>
      </div>
    );
  }

  const dispatch = useDispatch();
  const [customInput, setCustomInput] = useState('');
  const [output, setOutput] = useState('');
  const [submitFlag, setSubmitFlag] = useState(true);
  const [showQuestion, setShowQuestion] = useState(true);
  const questionSectionRef = useRef(null);
  const editorSectionRef = useRef(null);
  
  const username = useSelector((state) => state.auth.user) || 'anonymous';
  const index = useSelector((state) => state.index) ?? 0;
  const testcase = useSelector((state) => state.testcase.data) || '';
  const tournamentType = useSelector(
    (state) => state.tournament?.tournamentData?.tournament?.tournamentType
  ) || 'CLASSIC';

  // Initialize input with testcase if available
  useEffect(() => {
    if (testcase && testcase.input) {
      setCustomInput(testcase.input);
    }
  }, [testcase]);

  // Establish WebSocket connection conditionally based on token
  useEffect(() => {
    if (!token) return;
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}/ws`, token);
    return () => {
      webSocketService.disconnect();
    };
  }, [token]);

  // Handle scroll behavior
  useEffect(() => {
    const container = document.getElementById('left-scroll-container');
    if (!container || !editorSectionRef.current) return;

    const handleScroll = () => {
      const editorRect = editorSectionRef.current.getBoundingClientRect();
      const containerTop = container.getBoundingClientRect().top;
      
      // If editor is about to go off-screen (top edge reaching container top)
      if (editorRect.top <= containerTop + 10) {
        container.scrollTop = container.scrollTop - (containerTop - editorRect.top + 10);
      }
    };

    container.addEventListener('scroll', handleScroll);
    return () => container.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollToSection = (ref) => {
    if (ref.current) {
      ref.current.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-200">
      {/* Header */}
      <div className="sticky top-0 z-50 bg-white dark:bg-gray-800 shadow-sm">
        <TournamentControlBox />
      </div>
      
      <main className="flex flex-1 overflow-hidden">
        {/* Left Section: Question and CodeEditor */}
        <section className="relative w-[65%] h-full border-r border-gray-200 dark:border-gray-700 flex flex-col">
          {/* Navigation tabs */}
          <div className="flex border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
            <button 
              onClick={() => {
                setShowQuestion(true);
                scrollToSection(questionSectionRef);
              }}
              className={`px-4 py-2 font-medium text-sm ${showQuestion 
                ? 'text-indigo-600 border-b-2 border-indigo-600 dark:text-indigo-400 dark:border-indigo-400' 
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200'}`}
            >
              Problem
            </button>
            <button 
              onClick={() => {
                setShowQuestion(false);
                scrollToSection(editorSectionRef);
              }}
              className={`px-4 py-2 font-medium text-sm ${!showQuestion 
                ? 'text-indigo-600 border-b-2 border-indigo-600 dark:text-indigo-400 dark:border-indigo-400' 
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200'}`}
            >
              Code
            </button>
          </div>

          {/* Scrollable content area */}
          <div 
            id="left-scroll-container"
            className="flex-1 overflow-y-auto"
          >
            {/* Question Section */}
            <div 
              ref={questionSectionRef}
              className="p-4 md:p-6"
            >
              {tournamentId && index !== undefined && token ? (
                <Question tournamentId={tournamentId} index={index} token={token} />
              ) : (
                <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
                  <p className="text-red-500">Error loading question. Required data is missing.</p>
                </div>
              )}
            </div>

            {/* Navigation hint */}
            <div className="flex justify-center my-4">
              <button 
                onClick={() => {
                  setShowQuestion(false);
                  scrollToSection(editorSectionRef);
                }}
                className="flex items-center text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300"
              >
                <span>Go to Code Editor</span>
                <FaChevronDown className="ml-1" />
              </button>
            </div>
            
            {/* Code Editor Section */}
            <div 
              ref={editorSectionRef}
              id="code-editor-section"
              className="p-4 md:p-6 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700"
            >
              <div className="flex justify-between items-center">
                {/* <h2 className="text-xl font-bold">Code Editor</h2> */}
                <button 
                  onClick={() => {
                    setShowQuestion(true);
                    scrollToSection(questionSectionRef);
                  }}
                  className="flex items-center text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300"
                >
                  <FaChevronUp className="mr-1" />
                  <span>Back to Problem</span>
                </button>
              </div>
              <CodeEditor />
            </div>
          </div>
        </section>

        {/* Right Section: IOBox and ControlButtons */}
        <aside className="w-[35%] h-full flex flex-col bg-gray-100 dark:bg-gray-850">
          <div className="flex-1 overflow-y-auto">
            <IOBox 
              customInput={customInput}
              setCustomInput={setCustomInput}
              output={output}
              setOutput={setOutput}
            />
          </div>
          <div className="border-t border-gray-200 dark:border-gray-700 p-4 bg-white dark:bg-gray-800">
            <ControlButtons 
              tournamentId={tournamentId}
              token={token}
              setOutput={setOutput}
              customInput={customInput}
              submitFlag={submitFlag}
              setSubmitFlag={setSubmitFlag}
            />
          </div>
        </aside>
      </main>
    </div>
  );
}