import { useState, useEffect } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import React from 'react';

const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export default function Solution({ questionId }) {
  const [solution, setSolution] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // File upload state
  const [selectedFile, setSelectedFile] = useState(null);
  
  // Copy-paste state
  const [codeContent, setCodeContent] = useState('');
  const [fileName, setFileName] = useState('');
  const [showPasteArea, setShowPasteArea] = useState(false);

  // Fetch existing solution on component mount
  useEffect(() => {
    fetchSolution();
  }, [questionId]);

  const fetchSolution = async () => {
    try {
      setLoading(true);
      const token = getCookie('token');
      const response = await axios.get(`${baseUrl}/api/questions/solution/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setSolution(response.data);
    } catch (err) {
      if (err.response?.status !== 404) {
        setError('Failed to fetch solution');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      setShowPasteArea(false);
      setCodeContent('');
      setFileName('');
      setError('');
    }
  };

  const handleSubmitFile = async () => {
    if (!selectedFile) {
      setError('Please select a file');
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      const formData = new FormData();
      formData.append('file', selectedFile);
      
      const token = getCookie('token');
      const response = await axios.post(
        `${baseUrl}/api/questions/solution/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Solution uploaded successfully!');
      setSolution(response.data);
      setSelectedFile(null);
      
      // Reset file input
      const fileInput = document.getElementById('file-upload');
      if (fileInput) fileInput.value = '';
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload solution');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitPastedCode = async () => {
    if (!codeContent.trim()) {
      setError('Please enter code content');
      return;
    }
    
    if (!fileName.trim()) {
      setError('Please enter a file name');
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      // Create a blob from the code content and convert to file
      const blob = new Blob([codeContent], { type: 'text/plain' });
      const file = new File([blob], fileName, { type: 'text/plain' });
      
      const formData = new FormData();
      formData.append('file', file);
      
      const token = getCookie('token');
      const response = await axios.post(
        `${baseUrl}/api/questions/solution/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Solution saved successfully!');
      setSolution(response.data);
      setCodeContent('');
      setFileName('');
      setShowPasteArea(false);
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save solution');
    } finally {
      setLoading(false);
    }
  };

  const togglePasteArea = () => {
    setShowPasteArea(!showPasteArea);
    if (!showPasteArea) {
      setSelectedFile(null);
      // Reset file input
      const fileInput = document.getElementById('file-upload');
      if (fileInput) fileInput.value = '';
    }
    setError('');
  };

  return (
    <div className="solution-container p-6 bg-white rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-6">Solution Submission</h2>
      
      {/* Error and Success Messages */}
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}
      
      {success && (
        <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded">
          {success}
        </div>
      )}

      {/* Existing Solution Display */}
      {solution && (
        <div className="mb-6 p-4 bg-gray-50 border rounded">
          <h3 className="text-lg font-semibold mb-2">Current Solution</h3>
          <p className="text-sm text-gray-600">
            File: {solution.fileName || 'solution.txt'}
          </p>
          <p className="text-sm text-gray-600">
            Uploaded: {new Date(solution.createdAt).toLocaleString()}
          </p>
        </div>
      )}

      {/* Upload Options */}
      <div className="space-y-6">
        
        {/* File Upload Section */}
        <div className="border-2 border-dashed border-gray-300 rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-4">Upload File</h3>
          <div className="space-y-4">
            <input
              id="file-upload"
              type="file"
              onChange={handleFileUpload}
              className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
              accept=".txt,.js,.py,.java,.cpp,.c,.html,.css,.json,.xml,.md"
            />
            
            {selectedFile && (
              <div className="mt-2">
                <p className="text-sm text-gray-600">
                  Selected: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(2)} KB)
                </p>
                <button
                  onClick={handleSubmitFile}
                  disabled={loading}
                  className="mt-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Uploading...' : 'Upload Solution'}
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Divider */}
        <div className="flex items-center">
          <div className="flex-grow border-t border-gray-300"></div>
          <span className="px-4 text-gray-500 bg-white">OR</span>
          <div className="flex-grow border-t border-gray-300"></div>
        </div>

        {/* Copy-Paste Section */}
        <div className="border-2 border-dashed border-gray-300 rounded-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold">Paste Code</h3>
            <button
              onClick={togglePasteArea}
              className="px-3 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
            >
              {showPasteArea ? 'Hide' : 'Show'} Code Area
            </button>
          </div>
          
          {showPasteArea && (
            <div className="space-y-4">
              <div>
                <label htmlFor="filename" className="block text-sm font-medium text-gray-700 mb-1">
                  File Name *
                </label>
                <input
                  id="filename"
                  type="text"
                  value={fileName}
                  onChange={(e) => setFileName(e.target.value)}
                  placeholder="e.g., solution.py, main.js, index.html"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              
              <div>
                <label htmlFor="code-content" className="block text-sm font-medium text-gray-700 mb-1">
                  Code Content *
                </label>
                <textarea
                  id="code-content"
                  value={codeContent}
                  onChange={(e) => setCodeContent(e.target.value)}
                  rows={15}
                  placeholder="Paste your code here..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
                />
              </div>
              
              <button
                onClick={handleSubmitPastedCode}
                disabled={loading || !codeContent.trim() || !fileName.trim()}
                className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Saving...' : 'Save Solution'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}