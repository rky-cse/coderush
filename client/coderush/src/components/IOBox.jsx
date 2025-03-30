import React, { useState } from 'react';
import { FaRegClipboard, FaCheck, FaUndo, FaCode, FaTerminal } from 'react-icons/fa';

export default function IOBox({ 
  // Support both direct props and Split layout props
  mode, 
  value,
  onChange,
  label,
  readOnly = false,
  // Original props (for backward compatibility)
  customInput, 
  setCustomInput, 
  output, 
  setOutput 
}) {
  const [inputCopied, setInputCopied] = useState(false);
  const [outputCopied, setOutputCopied] = useState(false);

  // Determine if this is input or output mode
  const isInput = mode === 'input' || (mode === undefined && customInput !== undefined);
  
  // Set the actual values and handlers based on the provided props
  const currentValue = isInput 
    ? (value !== undefined ? value : customInput)
    : (value !== undefined ? value : output);
    
  const handleChange = (e) => {
    const newValue = e.target.value;
    if (isInput) {
      // Use either the onChange prop or setCustomInput
      if (onChange) {
        onChange(newValue);
      } else if (setCustomInput) {
        setCustomInput(newValue);
      }
    } else {
      // Use either the onChange prop or setOutput
      if (onChange) {
        onChange(newValue);
      } else if (setOutput) {
        setOutput(newValue);
      }
    }
  };

  const handleClear = () => {
    if (isInput) {
      if (onChange) {
        onChange('');
      } else if (setCustomInput) {
        setCustomInput('');
      }
    } else {
      if (onChange) {
        onChange('');
      } else if (setOutput) {
        setOutput('');
      }
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(currentValue || '');
    if (isInput) {
      setInputCopied(true);
      setTimeout(() => setInputCopied(false), 2000);
    } else {
      setOutputCopied(true);
      setTimeout(() => setOutputCopied(false), 2000);
    }
  };

  // Determine which "copied" state to use
  const isCopied = isInput ? inputCopied : outputCopied;

  return (
    <div className="flex flex-col h-full">
      <div className="px-3 py-2 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <h3 className="text-xs font-medium text-gray-600 dark:text-gray-400 uppercase tracking-wide flex items-center">
          {isInput ? (
            <FaCode className="mr-2 text-indigo-500 dark:text-indigo-400" />
          ) : (
            <FaTerminal className="mr-2 text-indigo-500 dark:text-indigo-400" />
          )}
          {label || (isInput ? 'Input' : 'Output')}
        </h3>
        
        <div className="flex items-center space-x-2">
          <button 
            onClick={handleClear}
            disabled={!currentValue}
            className={`p-1 text-xs rounded-md transition-colors ${
              currentValue
                ? 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700' 
                : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
            }`}
            title="Clear"
          >
            <FaUndo className="w-3 h-3" />
          </button>
          <button 
            onClick={copyToClipboard}
            disabled={!currentValue}
            className={`p-1 text-xs rounded-md transition-colors ${
              currentValue 
                ? 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700' 
                : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
            }`}
            title={isCopied ? 'Copied!' : 'Copy to clipboard'}
          >
            {isCopied ? <FaCheck className="w-3 h-3 text-green-500" /> : <FaRegClipboard className="w-3 h-3" />}
          </button>
        </div>
      </div>
      
      <div className="flex-1 p-0 relative">
        <textarea
          className="w-full h-full p-3 text-sm font-mono resize-none focus:outline-none focus:ring-1 focus:ring-indigo-400 focus:border-transparent
            bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 border-0"
          placeholder={isInput ? "Enter input here..." : "Output will appear here..."}
          value={currentValue || ''}
          onChange={handleChange}
          spellCheck="false"
          readOnly={isInput ? false : (readOnly !== undefined ? readOnly : false)}
          aria-label={isInput ? "Input for code execution" : "Output from code execution"}
        />
        {!currentValue && (
          <div className="absolute bottom-2 right-2 text-xs text-gray-400 dark:text-gray-600 pointer-events-none px-2 py-1 bg-white dark:bg-gray-800 rounded-md">
            {isInput ? 'Enter program input here' : 'Output will appear here'}
          </div>
        )}
      </div>
    </div>
  );
}