import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { increment, decrement } from '@/redux/slices/indexSlice';
import { toast } from 'react-hot-toast';
import webSocketService from '@/services/webSocketService';
import { runCode } from '@/services/codeRunner';
import { submitTournament } from '@/services/tournamentService';

export default function ControlButtons({ tournamentId, token, setOutput, customInput, submitFlag, setSubmitFlag }) {
  const dispatch = useDispatch();
  const { language, code } = useSelector((state) => state.editor) || {};
  const index = useSelector((state) => state.index) ?? 0;
  const question = useSelector((state) => state.question.data) || {};
  const tournamentType = useSelector((state) => state.tournament?.tournamentData?.tournament?.tournamentType) || 'CLASSIC';
  const username = useSelector((state) => state.auth.user) || 'anonymous';

  // Function to scroll the left container to top
  const scrollLeftContainerToTop = () => {
    const leftContainer = document.getElementById('left-scroll-container');
    if (leftContainer) {
      leftContainer.scrollTo({ top: 0 });
    }
  };

  const handlePrev = () => {
    if (index === 0) {
      toast.dismiss();
      toast.error('This is the first question!');
      return;
    }
    dispatch(decrement());
    scrollLeftContainerToTop();
  };

  const handleNext = () => {
    if (index === 4) {
      toast.dismiss();
      toast.error('This is the last question!');
      return;
    }
    dispatch(increment());
    scrollLeftContainerToTop();
  };

  const handleRunCode = async () => {
    toast.dismiss();
    toast.loading('Running code...');
    const result = await runCode({ language, code, customInput });
    setOutput(result.output);

    if (result.error) {
      toast.dismiss();
      toast.error(result.errorMessage);
    } else {
      toast.dismiss();
      toast.success(result.successMessage);
    }
  };

  const handleSubmit = () => {
    toast.loading('Submitting...');
    setSubmitFlag(false);
    if (!tournamentType) {
      console.error("handleSubmit: Tournament type is missing.");
      toast.dismiss();
      toast.error("Tournament type is missing, cannot submit.");
      return;
    }

    submitTournament({
      tournamentType,
      index,
      tournamentId,
      username,
      token,
      question,
      language,
      code,
      output: setOutput, // callback if needed
      toast,
      onComplete: () => setSubmitFlag(true),
    });
  };

  return (
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
        disabled={!submitFlag}
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
  );
}
