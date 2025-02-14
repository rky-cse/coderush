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
export default function TournamentPage({ params }) {
  const { tournamentId } = React.use(params);
  const [output, setOutput] = useState('');
  const dispatch = useDispatch();
  const { language, code } = useSelector((state) => state.editor);
  
  const token = typeof window !== 'undefined' ? getCookie('token') : null;
  const testcase = useSelector((state) => state.testcase.data) || '';
  const [customInput, setCustomInput] = useState('');
  const [submissionResult, setSubmissionResult] = useState(null);
  const index=useSelector((state)=>state.index);

  useEffect(() => {
    if (testcase && testcase.input) {
      setCustomInput(testcase.input);
    }
  }, [testcase]);

  const handleInputChange = (e) => {
    setCustomInput(e.target.value);
  };

  const handleOutputChange = (e) => {
    setOutput(e.target.value);
  };

  const handleRunCode = async () => {
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
    } catch (error) {
      console.error('Error running code:', error);
      setOutput('Error running code');
    }
  };

  const handleSubmit = () => {
    if (!token) {
      alert('You must be logged in to submit.');
      return;
    }
  
    const payload = {
      index,
      tournamentId,
      submissionTime: Date.now(),
      userOutput: output,
    };
  
    console.log('Sending payload:', payload);
  
    try {
      webSocketService.send('/app/tournament/submit', payload);
  
      console.log('Message sent successfully');
  
      webSocketService.subscribe(
        `/topic/tournament/submit/${tournamentId}/${index}`,
        (response) => {
          console.log('Received response:', response);
          setSubmissionResult(response);
  
          // Handle response toast notifications
          if (response === true) {
            toast.success('Correct!');
          } else if (response === false) {
            toast.error('Incorrect!');
          } else {
            toast.error('Unexpected response. Please contact support.');
          }
        }
      );
    } catch (error) {
      console.error('Error sending message:', error);
      toast.error('An error occurred while submitting.');
    }
  };

  const handleNext = () => {
    dispatch(increment()); // Dispatch the increment action
  };
  
  const handlePrev = () => {
    dispatch(decrement()); // Dispatch the decrement action
  };

  return (
    <div>
      <div className="flex flex-col h-screen">
        <div className="h-[5vh] w-full bg-blue-200 flex items-center justify-center">
          <p className="font-bold">Tournament Control Box</p>
        </div>

        <div className="flex h-[95vh] w-full">
          <div className="relative w-[70%] h-full p-4 border-r">
            <div className="questionAndEditorBox absolute inset-0 overflow-auto bg-white p-4">
              <Question tournamentId={tournamentId} index={index} token={token} />

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
        <p className="font-bold">RankList box</p>
        <RankListComponent tournamentId={tournamentId} token={token}></RankListComponent>
      </div>
    </div>
  );
}
