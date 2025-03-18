'use client';
import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import axios from 'axios';
import CodeEditor from '@/components/CodeEditor';
import Question from '@/components/Question';
import { setLanguage } from '@/redux/slices/codeSlice';
import webSocketService from '@/services/webSocketService';
import { increment, decrement } from '@/redux/slices/indexSlice';
import RankListComponent from '@/components/RankListComponent';
import { toast } from 'react-hot-toast';
import { getCookie } from 'cookies-next';
import TournamentControlBox from '@/components/TournamentControlBox';

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
  const { language, code } = useSelector((state) => state.editor) || {};
  const testcase = useSelector((state) => state.testcase.data) || '';
  const [customInput, setCustomInput] = useState('');
  const [output, setOutput] = useState('');
  const [submissionResult, setSubmissionResult] = useState(null);
  const index = useSelector((state) => state.index) ?? 0;
  const username = useSelector((state) => state.auth.user) || 'anonymous';
  const question= useSelector((state) => state.question.data) || {};
  const tournamentType =
    useSelector((state) => state.tournament?.tournamentData?.tournament?.tournamentType) || 'CLASSIC';

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

  const handleInputChange = (e) => {
    setCustomInput(e.target.value);
  };

  const handleOutputChange = (e) => {
    setOutput(e.target.value);
  };

  const handleRunCode = async () => {
    toast.dismiss();
    toast.loading('Running code...');
    const languageVersionMap = {
      javascript: '18.15.0',
      typescript: '5.0.3',
      python: '3.10.0',
      java: '15.0.2',
      csharp: '6.12.0',
      c: '10.2.0',
      cpp: '10.2.0',
    };
    const version = languageVersionMap[language];

    try {
      const response = await axios.post('https://emkc.org/api/v2/piston/execute', {
        language,
        version,
        files: [{ content: code }],
        stdin: customInput,
      });
      setOutput(response.data.run.output);

      if (response.data.run.signal === "SIGKILL") {
        toast.dismiss();
        toast.error('Time Limit exceeded!');
      } else if (response.data.compile && response.data.compile.code === 1) {
        toast.dismiss();
        toast.error('Compilation Error!');
      } else if (response.data.run.code === 1) {
        toast.dismiss();
        toast.error('Runtime Error!');
      } else if (response.data.run.code === 0) {
        toast.dismiss();
        toast.success('Code executed successfully!');
      }
    } catch (error) {
      console.error('Error running code:', error);
      setOutput('Error running code');
      toast.dismiss();
      toast.error('Error running code.');
    }
  };

  const handleSubmit = () => {
    toast.loading('Submitting...');
    if (!tournamentType) {
      console.error("handleSubmit: Tournament type is missing.");
      toast.dismiss();
      toast.error("Tournament type is missing, cannot submit.");
      return;
    }

    try {
      if (tournamentType === 'FREE_STYLE') {
        const payload = {
          index,
          questionId: question.questionId,
          tournamentId,
          userOutput: output,
        };

        console.log('Submitting payload (FREE_STYLE):', payload);
        webSocketService.send('/app/tournament/freeStyleSubmit', payload);

        webSocketService.subscribe(
          `/topic/tournament/freeStyleSubmit/${username}/${index}`,
          (response) => {
            console.log('Received FREE_STYLE response:', response);
            setSubmissionResult(response);
            toast.dismiss();
            if (response === true) {
              toast.success('Correct!');
            } else if (response === false) {
              toast.error('Incorrect!');
            } else {
              toast.error('Unexpected response. Please contact support.');
            }
          }
        );
      } else if (tournamentType === 'CLASSIC') {
        const payload = {
          index,
          tournamentId,
          language,
          code,
        };

        console.log('Submitting payload (CLASSIC):', payload);
        webSocketService.send('/app/tournament/classicSubmit', payload);

        webSocketService.subscribe(
          `/topic/tournament/classicSubmit/${username}/${index}`,
          (response) => {
            console.log('Received CLASSIC response:', response);
            setSubmissionResult(response);
            toast.dismiss();
            if (response === true) {
              toast.success('Correct!');
            } else if (response === false) {
              toast.error('Incorrect!');
            } else {
              toast.error('Unexpected response. Please contact support.');
            }
          }
        );
      } else {
        console.error("handleSubmit: Unknown tournament type:", tournamentType);
        toast.dismiss();
        toast.error("Unknown tournament type. Cannot submit.");
      }
    } catch (error) {
      console.error('Error sending message:', error);
      toast.dismiss();
      toast.error('An error occurred while submitting.');
    }
  };

  const handleNext = () => {
    if (index === 4) {
      toast.dismiss();
      toast.error('This is the last question!');
      return;
    }
    dispatch(increment());
  };

  const handlePrev = () => {
    if (index === 0) {
      toast.dismiss();
      toast.error('This is the first question!');
      return;
    }
    dispatch(decrement());
  };

  return (
    <div>
      <div className="flex flex-col h-screen">
        <div className="h-[6vh] w-full bg-blue-500 text-white flex items-center justify-between px-4 rounded-md shadow-md">
          <TournamentControlBox />
        </div>

        <div className="flex h-[95vh] w-full">
          <div className="relative w-[70%] h-full p-4 border-r">
            <div className="questionAndEditorBox absolute inset-0 overflow-auto bg-white p-4">
              {tournamentId && index !== undefined && token ? (
                <Question tournamentId={tournamentId} index={index} token={token} />
              ) : (
                <p>Error loading question. Required data is missing.</p>
              )}

              <div className="mb-8">
                <h2 className="text-xl font-bold mb-2">Code Editor</h2>
                <CodeEditor />
              </div>
            </div>
          </div>

          <div className="w-[30%] h-full bg-gray-100 flex flex-col">
            <div className="h-[40%] p-4 border-b">
              <h3 className="font-semibold mb-2">Input Box</h3>
              <textarea
                className="w-full h-full p-2 border rounded resize-none"
                placeholder="Enter input here..."
                value={customInput}
                onChange={handleInputChange}
              />
            </div>

            <div className="h-[40%] p-4 border-b">
              <h3 className="font-semibold mb-2">Output Box</h3>
              <textarea
                className="w-full h-full p-2 border rounded resize-none"
                placeholder="Output will appear here..."
                value={output}
                onChange={handleOutputChange}
              />
            </div>

            <div className="h-[20%] flex items-center justify-evenly p-4">
              <button
                className="bg-gray-400 text-white px-4 py-2 rounded"
                onClick={handlePrev}
              >
                Prev
              </button>
              <button
                className="bg-green-600 text-white px-4 py-2 rounded"
                onClick={handleRunCode}
              >
                Run Code
              </button>
              <button
                className="bg-blue-600 text-white px-4 py-2 rounded"
                onClick={handleSubmit}
              >
                Submit
              </button>
              <button
                className="bg-gray-400 text-white px-4 py-2 rounded"
                onClick={handleNext}
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>

      {submissionResult !== null && (
        <div className="p-4 bg-white rounded shadow mt-4">
          <p className="font-bold">Submission Result:</p>
          <p>{submissionResult ? 'Correct!' : 'Incorrect!'}</p>
        </div>
      )}

      <div className="mb-8">
        <p className="font-bold">RankList Box</p>
        {tournamentId && token ? (
          <RankListComponent tournamentId={tournamentId} token={token} />
        ) : (
          <p>Error loading Rank List. Required data is missing.</p>
        )}
      </div>
    </div>
  );
}
