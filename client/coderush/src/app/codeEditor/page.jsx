'use client'
import React, { useRef, useState, useEffect } from 'react';
import { Editor } from '@monaco-editor/react';
import { useSelector, useDispatch } from 'react-redux';
import { setCode, setLanguage } from '@/redux/slices/codeSlice';
import { BiCopy, BiCheck } from 'react-icons/bi';
import { FaSun, FaMoon, FaJs, FaPython, FaJava } from 'react-icons/fa';
import { SiTypescript, SiCplusplus, SiC, SiDotnet } from 'react-icons/si';
import { FiPlus, FiMinus } from 'react-icons/fi';

const CodeEditor = () => {
  const dispatch = useDispatch();
  const code = useSelector((state) => state.editor.code || '');
  const language = useSelector((state) => state.editor.language || 'c++');

  const [theme, setTheme] = useState('vs-dark');
  const [fontSize, setFontSize] = useState(14);
  const [copySuccess, setCopySuccess] = useState(false);
  const [isEditorReady, setIsEditorReady] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const editorRef = useRef(null);
  const dropdownRef = useRef(null);
  const containerRef = useRef(null);

  // Language options with icons
  const languageOptions = [
    { value: 'javascript', label: 'JavaScript', icon: <FaJs className="text-yellow-400" /> },
    { value: 'typescript', label: 'TypeScript', icon: <SiTypescript className="text-blue-500" /> },
    { value: 'python', label: 'Python', icon: <FaPython className="text-green-500" /> },
    { value: 'java', label: 'Java', icon: <FaJava className="text-red-500" /> },
    { value: 'csharp', label: 'C#', icon: <SiDotnet className="text-purple-500" /> },
    { value: 'cpp', label: 'C++', icon: <SiCplusplus className="text-blue-600" /> },
    { value: 'c', label: 'C', icon: <SiC className="text-blue-400" /> },
  ];

  // Find current language object
  const currentLanguage = languageOptions.find(lang => lang.value === language) || languageOptions[0];

  // Match theme with system preference
  useEffect(() => {
    const isDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    setTheme(isDarkMode ? 'vs-dark' : 'vs');
    
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e) => setTheme(e.matches ? 'vs-dark' : 'vs');
    
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    }
  }, []);

  // Handle clicks outside dropdown
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;
    setIsEditorReady(true);
    
    // Focus editor after it's mounted
    setTimeout(() => editor.focus(), 100);
    
    // Apply editor customizations
    monaco.editor.defineTheme('customDark', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#1e1e1e',
        'editor.lineHighlightBackground': '#2d2d2d',
        'editorLineNumber.foreground': '#6e6e6e',
        'editorLineNumber.activeForeground': '#bdbdbd',
      }
    });
    
    if (theme.includes('dark')) {
      monaco.editor.setTheme('customDark');
    }
  };

  const handleEditorChange = (value) => {
    if (value !== undefined) {
      dispatch(setCode(value));
    }
  };

  const handleLanguageChange = (value) => {
    dispatch(setLanguage(value));
    setIsDropdownOpen(false);
  };

  const handleCopyCode = () => {
    if (code) {
      navigator.clipboard.writeText(code);
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    }
  };

  const handleThemeToggle = () => {
    setTheme(theme === 'vs' ? 'vs-dark' : 'vs');
  };

  const handleFontSizeChange = (increment) => {
    const newSize = Math.max(10, Math.min(24, fontSize + increment));
    setFontSize(newSize);
    
    // Manually update editor options when font size changes
    if (editorRef.current) {
      editorRef.current.updateOptions({ fontSize: newSize });
    }
  };

  return (
    <div 
      ref={containerRef}
      className="flex flex-col border border-gray-200 dark:border-gray-700 rounded-lg shadow-md overflow-hidden"
      style={{ height: '80vh', maxHeight: '800px' }} // Explicit height is crucial for Monaco
    >
      {/* Editor Toolbar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
        {/* Language Selector */}
        <div className="relative mb-2 sm:mb-0" ref={dropdownRef}>
          <button 
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="flex items-center text-sm font-medium px-3 py-1.5 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            aria-label="Select programming language"
          >
            <span className="flex items-center">
              {currentLanguage.icon}
              <span className="ml-2">{currentLanguage.label}</span>
            </span>
            <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          {isDropdownOpen && (
            <div className="absolute left-0 top-full mt-1 w-60 z-10 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-md shadow-lg">
              <ul className="py-1 max-h-60 overflow-y-auto">
                {languageOptions.map((lang) => (
                  <li key={lang.value}>
                    <button
                      className={`w-full text-left px-4 py-2 flex items-center text-sm ${
                        language === lang.value 
                          ? 'bg-indigo-50 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300' 
                          : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600'
                      }`}
                      onClick={() => handleLanguageChange(lang.value)}
                    >
                      <span className="w-6">{lang.icon}</span>
                      <span className="ml-2">{lang.label}</span>
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        {/* Control Buttons */}
        <div className="flex items-center space-x-2">
          {/* Font Size Controls */}
          <div className="flex items-center border border-gray-200 dark:border-gray-700 rounded-md overflow-hidden">
            <button 
              onClick={() => handleFontSizeChange(-1)} 
              className="p-1.5 bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50"
              title="Decrease font size"
              disabled={fontSize <= 10}
              aria-label="Decrease font size"
            >
              <FiMinus className="w-4 h-4" />
            </button>
            <span className="px-2 py-1 min-w-[2rem] text-center text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700">
              {fontSize}
            </span>
            <button 
              onClick={() => handleFontSizeChange(1)} 
              className="p-1.5 bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50"
              title="Increase font size"
              disabled={fontSize >= 24}
              aria-label="Increase font size"
            >
              <FiPlus className="w-4 h-4" />
            </button>
          </div>

          {/* Theme Toggle */}
          <button 
            onClick={handleThemeToggle} 
            className="p-1.5 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-700 rounded-md text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600"
            title={theme === 'vs' ? 'Switch to dark theme' : 'Switch to light theme'}
            aria-label={theme === 'vs' ? 'Switch to dark theme' : 'Switch to light theme'}
          >
            {theme === 'vs' ? <FaMoon className="w-4 h-4" /> : <FaSun className="w-4 h-4" />}
          </button>

          {/* Copy Button */}
          <button 
            onClick={handleCopyCode} 
            className="p-1.5 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-700 rounded-md text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600"
            title="Copy code"
            aria-label="Copy code to clipboard"
          >
            {copySuccess ? (
              <BiCheck className="w-4 h-4 text-green-500" />
            ) : (
              <BiCopy className="w-4 h-4" />
            )}
          </button>
        </div>
      </div>

      {/* Editor Container - CRITICAL: Must have explicit height */}
      <div className="flex-1 relative" style={{ minHeight: '200px' }}>
        {/* Loading State */}
        {!isEditorReady && (
          <div className="absolute inset-0 flex items-center justify-center bg-white dark:bg-gray-800 z-10">
            <div className="flex flex-col items-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 dark:border-indigo-400"></div>
              <span className="mt-2 text-sm text-gray-600 dark:text-gray-400">Loading editor...</span>
            </div>
          </div>
        )}

        {/* The Monaco Editor */}
        <Editor
          height="100%" 
          width="100%"
          defaultLanguage={language}
          defaultValue={code}
          language={language}
          theme={theme}
          value={code}
          onChange={handleEditorChange}
          onMount={handleEditorDidMount}
          loading={<div>Loading editor...</div>}
          options={{
            fontSize: fontSize,
            automaticLayout: true,
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            renderLineHighlight: 'all',
            quickSuggestions: true,
            snippetSuggestions: 'inline',
            tabSize: 2,
            scrollbar: {
              vertical: 'visible',
              horizontal: 'visible',
              verticalScrollbarSize: 12,
              horizontalScrollbarSize: 12,
            },
            wordWrap: 'on',
            lineNumbers: 'on',
            folding: true,
            bracketPairColorization: { enabled: true }
          }}
        />
      </div>

      {/* Notification Toast */}
      <div 
        className={`fixed top-4 right-4 px-4 py-2 bg-green-100 text-green-800 rounded-md shadow-lg transition-opacity duration-300 ${
          copySuccess ? 'opacity-100' : 'opacity-0 pointer-events-none'
        } z-50`}
        aria-live="polite"
      >
        <div className="flex items-center">
          <BiCheck className="mr-1 text-lg" />
          <span>Code copied to clipboard!</span>
        </div>
      </div>
    </div>
  );
};

export default CodeEditor;