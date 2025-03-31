'use client';
import React, { useState, useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getCookie } from 'cookies-next';
import Split from 'react-split';
import TournamentControlBox from '@/components/TournamentControlBox';
import Question from '@/components/Question';
import CodeEditor from '@/components/CodeEditor';
import IOBox from '@/components/IOBox';
import ControlButtons from '@/components/ControlButtons';
import webSocketService from '@/services/webSocketService';
import { FaChevronUp, FaChevronDown, FaFileAlt, FaCode } from 'react-icons/fa';
import { setTournamentData } from '@/redux/slices/tournamentSlice';
import axios from 'axios';

export default function TournamentPage({ params }) {
  // Existing state and refs
  const { tournamentId } = React.use(params) || {};
  console.log(tournamentId);
  
  const [customInput, setCustomInput] = useState('');
  const [output, setOutput] = useState('');
  const [submitFlag, setSubmitFlag] = useState(true);
  const [mainSplitSizes, setMainSplitSizes] = useState([60, 40]);
  const [ioSplitSizes, setIoSplitSizes] = useState([50, 50]);
  const [activeSection, setActiveSection] = useState('problem');
  const [registeredTournaments, setRegisteredTournaments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Refs for scrolling to sections
  const questionSectionRef = useRef(null);
  const editorSectionRef = useRef(null);
  const problemContainerRef = useRef(null);
  const editorHeaderRef = useRef(null);

  const dispatch = useDispatch();
  const username = useSelector((state) => state.auth?.user) || 'anonymous';
  const index = useSelector((state) => state.index) ?? 0;
  const testcase = useSelector((state) => state.testcase?.data) || '';
  const token = getCookie("token");
  const [tournament,setTournament] =useState(useSelector((state) => state.tournament?.tournamentData) || null);

  // Initialize input with testcase if available
  useEffect(() => {
    if (testcase && testcase.input) {
      setCustomInput(testcase.input);
    }
  }, [testcase]);

  // Establish WebSocket connection
  useEffect(() => {
    if (!token) return;
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/ws`, token);
    return () => {
      webSocketService.disconnect();
    };
  }, [token]);

  // Scroll to top when question index changes
  useEffect(() => {
    if (problemContainerRef.current) {
      // Smooth scroll to top with animation
      problemContainerRef.current.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      
      // Also set active section to problem when changing questions
      setActiveSection('problem');
      
      // If we have the question section ref, scroll to it
      if (questionSectionRef.current) {
        questionSectionRef.current.scrollIntoView({ 
          behavior: 'smooth',
          block: 'start'
        });
      }
    }
  }, [index]); // Listen for index changes

  useEffect(() => {
    const fetchTournamentData = async () => {
      if (!tournamentId) return;
      
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      
      try {
        // Fetch tournament details
        const { data: tournamentData } = await axios.get(
          `${baseUrl}/api/tournament/mtm/getTournamentById/${tournamentId}`,
          { headers }
        );
        
        if (!tournamentData) {
          setError('Tournament not found');
          setLoading(false);
          return;
        }
        
        setTournament(tournamentData);
        dispatch(setTournamentData(tournamentData));

        // Fetch registered tournaments
        if (token) {
          const { data: userTournaments } = await axios.get(
            `${baseUrl}/api/tournament/mtm/registeredTournamentsByUser`,
            { headers }
          );
          
          if (userTournaments) {
            const registeredIds = userTournaments;
            setRegisteredTournaments(registeredIds);
          }
        }
      } catch (err) {
        console.error("Error fetching tournament data:", err);
        setError(err.response?.data?.message || 'Failed to load tournament details');
      } finally {
        setLoading(false);
      }
    };
    
    fetchTournamentData();
  }, [tournamentId, token]);

  // Enhanced function to scroll to a section
  const scrollToSection = (ref, section) => {
    setActiveSection(section);

    if (section === 'code') {
      // For code section, scroll to show the full editor
      setTimeout(() => {
        if (problemContainerRef.current && editorSectionRef.current) {
          // Get the scroll position that shows the complete editor
          const containerHeight = problemContainerRef.current.clientHeight;
          const editorTop = editorSectionRef.current.offsetTop;
          
          // Scroll to position that shows the complete editor
          problemContainerRef.current.scrollTo({
            top: editorTop - 40, // Subtract header height to account for sticky header
            behavior: 'smooth'
          });
        }
      }, 10);
    } else if (ref.current) {
      // For problem section, use standard scrollIntoView
      ref.current.scrollIntoView({ 
        behavior: 'smooth',
        block: 'start'
      });
    }
  };

  // Gutter style functions for Split panels
  const getHorizontalGutterStyle = () => ({
    height: '4px',
    cursor: 'row-resize',
    backgroundColor: 'var(--split-gutter-bg)',
    position: 'relative',
  });

  const getVerticalGutterStyle = () => ({
    width: '4px',
    cursor: 'col-resize',
    backgroundColor: 'var(--split-gutter-bg)',
    position: 'relative',
  });

  // Error state handling kept the same
  if (!tournamentId || !token) {
    // Error displays kept the same
    return null;
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-200">
      {/* Header */}
      <div className="sticky top-0 z-50 bg-white dark:bg-gray-800 shadow-sm">
        <TournamentControlBox />
      </div>
      
      {/* Main Content with Resizable Split */}
      <div className="flex-1 overflow-hidden">
        <Split 
          sizes={mainSplitSizes}
          minSize={300}
          gutterSize={4}
          gutterStyle={getVerticalGutterStyle}
          onDragEnd={sizes => setMainSplitSizes(sizes)}
          className="flex h-full"
          direction="horizontal"
        >
          {/* Left Panel */}
          <div className="h-full flex flex-col overflow-hidden">
            {/* Fixed Navigation tabs */}
            <div className="flex h-10 min-h-[40px] bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 z-10 sticky top-0">
              <button 
                onClick={() => scrollToSection(questionSectionRef, 'problem')}
                className={`flex items-center px-4 py-2 text-sm font-medium ${activeSection === 'problem' 
                  ? 'text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-600 dark:border-indigo-400' 
                  : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-300'}`}
              >
                <FaFileAlt className={`mr-1.5 ${activeSection === 'problem' ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-500 dark:text-gray-500'}`} />
                Problem
              </button>
              <button 
                onClick={() => scrollToSection(editorSectionRef, 'code')}
                className={`flex items-center px-4 py-2 text-sm font-medium ${activeSection === 'code' 
                  ? 'text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-600 dark:border-indigo-400' 
                  : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-300'}`}
              >
                <FaCode className={`mr-1.5 ${activeSection === 'code' ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-500 dark:text-gray-500'}`} />
                Code
              </button>
            </div>
            
            {/* Scrollable content area */}
            <div 
              ref={problemContainerRef} 
              className="flex-1 overflow-auto bg-white dark:bg-gray-800 scroll-smooth" 
            >
              {/* Problem Section */}
              <div 
                ref={questionSectionRef} 
                className="p-4"
                id="problem-section"
              >
                <div className="overflow-visible">
                  {tournamentId && index !== undefined && token ? (
                    <Question tournamentId={tournamentId} index={index} token={token} />
                  ) : (
                    <div className="bg-white dark:bg-gray-800 p-3 rounded-lg shadow-sm">
                      <p className="text-red-500">Error loading question. Required data is missing.</p>
                    </div>
                  )}
                </div>
              
              </div>
              
              {/* Code Editor Section */}
              <div 
                ref={editorSectionRef}
                id="editor-section"
                className="bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700"
              >
                
                <div className="h-[calc(100vh-280px)]">
                  <CodeEditor />
                </div>
              </div>
            </div>
          </div>
          
          {/* Right Panel remains unchanged */}
          <div className="flex flex-col h-full bg-white dark:bg-gray-800 border-l border-gray-200 dark:border-gray-700">
            {/* IO Section - Resizable Input/Output */}
            <div className="flex-1 overflow-hidden">
              <Split
                direction="vertical"
                sizes={ioSplitSizes}
                minSize={100}
                gutterSize={4}
                gutterStyle={getHorizontalGutterStyle}
                onDragEnd={sizes => setIoSplitSizes(sizes)}
                className="h-full flex flex-col"
              >
                {/* Input */}
                <div className="h-full">
                  <IOBox 
                    mode="input"
                    value={customInput}
                    onChange={setCustomInput}
                    label="Input"
                  />
                </div>
                
                {/* Output */}
                <div className="h-full">
                  <IOBox 
                    mode="output"
                    value={output}
                    onChange={setOutput}
                    label="Output"
                  />
                </div>
              </Split>
            </div>
            
            {/* Control Buttons */}
            <div className="p-3 border-t border-gray-200 dark:border-gray-700">
              <ControlButtons 
                tournamentId={tournamentId}
                token={token}
                output={output}
                customInput={customInput}
                submitFlag={submitFlag}
                setSubmitFlag={setSubmitFlag}
              />
            </div>
          </div>
        </Split>
      </div>
    </div>
  );
}