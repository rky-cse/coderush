'use client';
import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getCookie } from 'cookies-next';
import TournamentControlBox from '@/components/TournamentControlBox';
import Question from '@/components/Question';
import CodeEditor from '@/components/CodeEditor';
import IOBox from '@/components/IOBox';
import ControlButtons from '@/components/ControlButtons';
import RankListComponent from '@/components/RankListComponent';
import webSocketService from '@/services/webSocketService';

export default function TournamentPage({ params: paramsPromise }) {
  // Unwrap the params promise with React.use()
  const params = React.use(paramsPromise);
  if (!params) {
    console.error("TournamentPage: Params not provided.");
    return <div>Error: Missing parameters.</div>;
  }

  const { tournamentId } = params;
  if (!tournamentId) {
    console.error("TournamentPage: Tournament ID is missing in params.");
    return <div>Error: Tournament ID is missing.</div>;
  }

  // Get token and ensure it exists
  const token = typeof window !== 'undefined' ? getCookie('token') : null;
  if (!token) {
    console.error("TournamentPage: User token not found. User must be logged in.");
    return <div>Error: You must be logged in to access this page.</div>;
  }

  const dispatch = useDispatch();
  const [customInput, setCustomInput] = useState('');
  const [output, setOutput] = useState('');
  const [submitFlag, setSubmitFlag] = useState(true);
  const username = useSelector((state) => state.auth.user) || 'anonymous';
  const index = useSelector((state) => state.index) ?? 0;
  const testcase = useSelector((state) => state.testcase.data) || '';
  const tournamentType = useSelector(
    (state) =>
      state.tournament?.tournamentData?.tournament?.tournamentType
  ) || 'CLASSIC';

  // Initialize input with testcase if available
  useEffect(() => {
    if (testcase && testcase.input) {
      setCustomInput(testcase.input);
    }
  }, [testcase]);

  // Establish WebSocket connection conditionally based on token.
  useEffect(() => {
    if (!token) return;
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}/ws`, token);
    return () => {
      webSocketService.disconnect();
    };
  }, [token]);

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <div className="h-[6vh]">
        <TournamentControlBox />
      </div>
      
      <main className="flex h-screen w-full">
        {/* Left Section: Question (scrollable) and CodeEditor (sticky) */}
        <section
          id="left-scroll-container"
          className="relative w-[70%] h-full p-4 border-r flex flex-col overflow-y-auto snap-y snap-mandatory"
          style={{ WebkitOverflowScrolling: 'touch' }}
        >
          {/* Question Section */}
          <div className="flex-1 snap-start">
            {tournamentId && index !== undefined && token ? (
              <Question tournamentId={tournamentId} index={index} token={token} />
            ) : (
              <p>Error loading question. Required data is missing.</p>
            )}
          </div>
          {/* Code Editor Section */}
          <div className="sticky z-20 bg-white border-t snap-start" style={{ top: '10vh' }}>
            <h2 className="text-xl font-bold mb-2">Code Editor</h2>
            <CodeEditor />
          </div>
        </section>

        {/* Right Section: IOBox and ControlButtons */}
        <aside className="w-[30%] h-full bg-gray-100 flex flex-col">
          <IOBox 
            customInput={customInput}
            setCustomInput={setCustomInput}
            output={output}
            setOutput={setOutput}
          />
          <ControlButtons 
            tournamentId={tournamentId}
            token={token}
            setOutput={setOutput}
            customInput={customInput}
            submitFlag={submitFlag}
            setSubmitFlag={setSubmitFlag}
          />
        </aside>
      </main>

      {/* Second Page - RankList */}
      {/* <section className="min-h-screen pt-8 bg-white">
        <p className="font-bold text-center text-gray-800 mb-4">RankList</p>
        {tournamentId && token ? (
          <RankListComponent tournamentId={tournamentId} token={token} />
        ) : (
          <p>Error loading Rank List. Required data is missing.</p>
        )}
      </section> */}
    </div>
  );
}
