import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { increment, decrement } from '@/redux/slices/indexSlice';
import { toast } from 'react-hot-toast';
import { runCode } from '@/services/codeRunner';
import { submitTournament } from '@/services/tournamentService';
import { FaChevronLeft, FaChevronRight, FaPlay, FaCheck, FaSpinner, FaKeyboard } from 'react-icons/fa';

export default function ControlButtons({ tournamentId, token, setOutput,output, customInput, submitFlag, setSubmitFlag }) {
  const dispatch = useDispatch();
  const { language, code } = useSelector((state) => state.editor) || {};
  const index = useSelector((state) => state.index) ?? 0;
  const question = useSelector((state) => state.question.data) || {};
  const tournamentType = useSelector((state) => state.tournament?.tournamentData?.tournamentType) || 'CLASSIC';
  console.log(tournamentType);
  const username = useSelector((state) => state.auth.user) || 'anonymous';
  const [tempOutput,setTempOutput] = useState('');
  
  
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showShortcuts, setShowShortcuts] = useState(false);

  // Scroll when changing questions
  const scrollLeftContainerToTop = () => {
    const leftContainer = document.getElementById('left-scroll-container');
    if (leftContainer) {
      leftContainer.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handlePrev = () => {
    if (index === 0) {
      toast.error('This is the first question!', { id: 'first-question' });
      return;
    }
    dispatch(decrement());
    scrollLeftContainerToTop();
  };

  const handleNext = () => {
    if (index === 4) {
      toast.error('This is the last question!', { id: 'last-question' });
      return;
    }
    dispatch(increment());
    scrollLeftContainerToTop();
  };

  const handleRunCode = async () => {
    if (isRunning || !code) return;
    
    setIsRunning(true);
    toast.loading('Running code...', { id: 'run-code' });
    
    try {
      const result = await runCode({ language, code, customInput });
      setOutput(result.output);
      setTempOutput(result.output);

      if (result.error) {
        toast.error(result.errorMessage, { id: 'run-code' });
      } else {
        toast.success(result.successMessage, { id: 'run-code' });
      }
    } catch (error) {
      toast.error('An error occurred while running the code.', { id: 'run-code' });
      console.error(error);
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmit = () => {
    if (isSubmitting || !submitFlag) return;
    
    setIsSubmitting(true);
    toast.loading('Submitting...', { id: 'submit' });
    setSubmitFlag(false);
    
    if (!tournamentType) {
      console.error("handleSubmit: Tournament type is missing.");
      toast.error("Tournament type is missing, cannot submit.", { id: 'submit' });
      setIsSubmitting(false);
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
      userOutput: output,
      toast,
      onComplete: () => {
        setSubmitFlag(true);
        setIsSubmitting(false);
      },
    });
  };

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Run code: Ctrl+Enter
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        e.preventDefault();
        handleRunCode();
      }
      // Submit: Ctrl+S
      else if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        handleSubmit();
      }
      // Previous: Ctrl+Left
      else if ((e.ctrlKey || e.metaKey) && e.key === 'ArrowLeft') {
        e.preventDefault();
        handlePrev();
      }
      // Next: Ctrl+Right
      else if ((e.ctrlKey || e.metaKey) && e.key === 'ArrowRight') {
        e.preventDefault();
        handleNext();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [index, code, isRunning, isSubmitting, submitFlag]);

  return (
    <div className="flex items-center justify-between px-4 py-2 bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700">
      {/* Left side - Question indicator */}
      <div className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400">
        <span className="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-md">Q{index + 1}/5</span>
      </div>
      
      {/* Center - Navigation and action buttons */}
      <div className="flex items-center space-x-2">
        <button
          className="p-1.5 rounded text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-40"
          onClick={handlePrev}
          disabled={index === 0}
          title="Previous question (Ctrl+←)"
        >
          <FaChevronLeft className="w-4 h-4" />
        </button>
        
        <button
          className="flex items-center px-3 py-1 rounded-md bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium disabled:opacity-60"
          onClick={handleRunCode}
          disabled={isRunning || !code}
          title="Run code (Ctrl+Enter)"
        >
          {isRunning ? <FaSpinner className="animate-spin w-3.5 h-3.5" /> : <FaPlay className="w-3.5 h-3.5 mr-1" />}
          <span>Run</span>
        </button>
        
        <button
          className={`flex items-center px-3 py-1 rounded-md text-sm font-medium ${
            !submitFlag || isSubmitting
              ? 'bg-gray-300 dark:bg-gray-600 text-gray-600 dark:text-gray-400 opacity-60'
              : 'bg-indigo-600 hover:bg-indigo-700 text-white'
          }`}
          onClick={handleSubmit}
          disabled={!submitFlag || isSubmitting}
          title="Submit solution (Ctrl+S)"
        >
          {isSubmitting ? <FaSpinner className="animate-spin w-3.5 h-3.5" /> : <FaCheck className="w-3.5 h-3.5 mr-1" />}
          <span>Submit</span>
        </button>
        
        <button
          className="p-1.5 rounded text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-40"
          onClick={handleNext}
          disabled={index === 4}
          title="Next question (Ctrl+→)"
        >
          <FaChevronRight className="w-4 h-4" />
        </button>
      </div>
      
      {/* Right side - Shortcuts toggle */}
      <div className="relative">
        <button
          className="p-1.5 text-xs text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md"
          onClick={() => setShowShortcuts(!showShortcuts)}
          title="Keyboard shortcuts"
        >
          <FaKeyboard className="w-4 h-4" />
        </button>
        
        {showShortcuts && (
          <div className="absolute bottom-full right-0 mb-2 w-48 p-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-md shadow-lg text-xs z-10">
            <div className="mb-1 font-medium text-gray-700 dark:text-gray-300">Keyboard Shortcuts:</div>
            <div className="grid grid-cols-2 gap-1">
              <span className="text-gray-600 dark:text-gray-400">Ctrl+Enter:</span>
              <span className="text-gray-800 dark:text-gray-200">Run Code</span>
              
              <span className="text-gray-600 dark:text-gray-400">Ctrl+S:</span>
              <span className="text-gray-800 dark:text-gray-200">Submit</span>
              
              <span className="text-gray-600 dark:text-gray-400">Ctrl+←:</span>
              <span className="text-gray-800 dark:text-gray-200">Prev Question</span>
              
              <span className="text-gray-600 dark:text-gray-400">Ctrl+→:</span>
              <span className="text-gray-800 dark:text-gray-200">Next Question</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}