import React, { useState } from 'react';
import { FaRegClipboard, FaCheck, FaUndo, FaCode, FaTerminal } from 'react-icons/fa';

export default function IOBox({ customInput, setCustomInput, output, setOutput }) {
  const [inputCopied, setInputCopied] = useState(false);
  const [outputCopied, setOutputCopied] = useState(false);

  const handleInputChange = (e) => {
    setCustomInput(e.target.value);
  };

  const handleOutputChange = (e) => {
    setOutput(e.target.value);
  };

  const handleClearInput = () => {
    setCustomInput('');
  };

  const handleClearOutput = () => {
    setOutput('');
  };

  const copyToClipboard = (text, type) => {
    navigator.clipboard.writeText(text);
    if (type === 'input') {
      setInputCopied(true);
      setTimeout(() => setInputCopied(false), 2000);
    } else {
      setOutputCopied(true);
      setTimeout(() => setOutputCopied(false), 2000);
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex flex-col h-full overflow-hidden">
        {/* Input Box - Top Section */}
        <div className="flex flex-col flex-1 mb-4 border-b border-gray-200 dark:border-gray-700 pb-4">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 flex items-center">
              <FaCode className="mr-2 text-indigo-500 dark:text-indigo-400" />
              Input
            </h3>
            <div className="flex items-center space-x-2">
              <button 
                onClick={handleClearInput}
                disabled={!customInput}
                className={`p-1 text-xs rounded-md transition-colors ${
                  customInput 
                    ? 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700' 
                    : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
                }`}
                title="Clear input"
              >
                <FaUndo className="w-3 h-3" />
              </button>
              <button 
                onClick={() => copyToClipboard(customInput, 'input')}
                disabled={!customInput}
                className={`p-1 text-xs rounded-md transition-colors ${
                  customInput 
                    ? 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700' 
                    : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
                }`}
                title={inputCopied ? 'Copied!' : 'Copy to clipboard'}
              >
                {inputCopied ? <FaCheck className="w-3 h-3 text-green-500" /> : <FaRegClipboard className="w-3 h-3" />}
              </button>
            </div>
          </div>
          <div className="flex-grow relative">
            <textarea
              className="w-full h-full p-3 text-sm font-mono bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-md resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent dark:text-gray-200"
              placeholder="Enter input here..."
              value={customInput}
              onChange={handleInputChange}
              spellCheck="false"
              aria-label="Input for code execution"
            />
            {!customInput && (
              <div className="absolute bottom-2 right-2 text-xs text-gray-400 dark:text-gray-600 pointer-events-none px-2 py-1 bg-white dark:bg-gray-800 rounded-md">
                Enter program input here
              </div>
            )}
          </div>
        </div>

        {/* Output Box - Bottom Section */}
        <div className="flex flex-col flex-1">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 flex items-center">
              <FaTerminal className="mr-2 text-indigo-500 dark:text-indigo-400" />
              Output
            </h3>
            <div className="flex items-center space-x-2">
              <button 
                onClick={handleClearOutput}
                disabled={!output}
                className={`p-1 text-xs rounded-md transition-colors ${
                  output 
                    ? 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700' 
                    : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
                }`}
                title="Clear output"
              >
                <FaUndo className="w-3 h-3" />
              </button>
              <button 
                onClick={() => copyToClipboard(output, 'output')}
                disabled={!output}
                className={`p-1 text-xs rounded-md transition-colors ${
                  output 
                    ? 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700' 
                    : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
                }`}
                title={outputCopied ? 'Copied!' : 'Copy to clipboard'}
              >
                {outputCopied ? <FaCheck className="w-3 h-3 text-green-500" /> : <FaRegClipboard className="w-3 h-3" />}
              </button>
            </div>
          </div>
          <div className="flex-grow relative">
            <textarea
              className="w-full h-full p-3 text-sm font-mono bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-md resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent dark:text-gray-200"
              placeholder="Output will appear here..."
              value={output}
              onChange={handleOutputChange}
              spellCheck="false"
              readOnly={false}
              aria-label="Output from code execution"
            />
            {!output && (
              <div className="absolute bottom-2 right-2 text-xs text-gray-400 dark:text-gray-600 pointer-events-none px-2 py-1 bg-gray-50 dark:bg-gray-900 rounded-md">
                Output will appear here
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}