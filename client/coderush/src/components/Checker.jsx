import { useState, useEffect } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import React from 'react';

const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export default function Checker({ questionId }) {
  const [checker, setChecker] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // File upload state
  const [selectedFile, setSelectedFile] = useState(null);
  
  // Copy-paste state
  const [codeContent, setCodeContent] = useState('');
  const [fileName, setFileName] = useState('');
  const [showPasteArea, setShowPasteArea] = useState(false);

  // Fetch existing checker on component mount
  useEffect(() => {
    fetchChecker();
  }, [questionId]);

  const fetchChecker = async () => {
    try {
      setLoading(true);
      const token = getCookie('token');
      const response = await axios.get(`${baseUrl}/api/questions/checker/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setChecker(response.data);
    } catch (err) {
      if (err.response?.status !== 404) {
        setError('Failed to fetch checker');
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
        `${baseUrl}/api/questions/checker/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Checker uploaded successfully!');
      setChecker(response.data);
      setSelectedFile(null);
      
      // Reset file input
      const fileInput = document.getElementById('checker-file-upload');
      if (fileInput) fileInput.value = '';
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload checker');
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
        `${baseUrl}/api/questions/checker/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Checker saved successfully!');
      setChecker(response.data);
      setCodeContent('');
      setFileName('');
      setShowPasteArea(false);
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save checker');
    } finally {
      setLoading(false);
    }
  };

  const togglePasteArea = () => {
    setShowPasteArea(!showPasteArea);
    if (!showPasteArea) {
      setSelectedFile(null);
      // Reset file input
      const fileInput = document.getElementById('checker-file-upload');
      if (fileInput) fileInput.value = '';
    }
    setError('');
  };

  const handleDeleteChecker = async () => {
    if (!confirm('Are you sure you want to delete this checker?')) {
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      const token = getCookie('token');
      await axios.delete(`${baseUrl}/api/questions/checker/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      
      setSuccess('Checker deleted successfully!');
      setChecker(null);
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete checker');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checker-container p-6 bg-white rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-6">Checker Management</h2>
      
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

      {/* Existing Checker Display */}
      {checker && (
        <div className="mb-6 p-4 bg-gray-50 border rounded">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-lg font-semibold mb-2">Current Checker</h3>
              <p className="text-sm text-gray-600">
                File: {checker.fileName || 'checker.txt'}
              </p>
              <p className="text-sm text-gray-600">
                Uploaded: {new Date(checker.createdAt).toLocaleString()}
              </p>
              {checker.description && (
                <p className="text-sm text-gray-600 mt-1">
                  Description: {checker.description}
                </p>
              )}
            </div>
            <button
              onClick={handleDeleteChecker}
              disabled={loading}
              className="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Delete
            </button>
          </div>
        </div>
      )}

      {/* Upload Options */}
      <div className="space-y-6">
        
        {/* File Upload Section */}
        <div className="border-2 border-dashed border-gray-300 rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-4">Upload Checker File</h3>
          <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded text-sm text-blue-700">
            <strong>Checker Guidelines:</strong>
            <ul className="mt-2 space-y-1">
              <li>• Should validate solution output against expected results</li>
              <li>• Must return exit code 0 for correct solutions, non-zero for incorrect</li>
              <li>• Common formats: Python (.py), C++ (.cpp), Java (.java), Shell (.sh)</li>
              <li>• Should handle edge cases and provide meaningful error messages</li>
            </ul>
          </div>
          
          <div className="space-y-4">
            <input
              id="checker-file-upload"
              type="file"
              onChange={handleFileUpload}
              className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-purple-50 file:text-purple-700 hover:file:bg-purple-100"
              accept=".txt,.js,.py,.java,.cpp,.c,.sh,.rb,.go,.rs"
            />
            
            {selectedFile && (
              <div className="mt-2">
                <p className="text-sm text-gray-600">
                  Selected: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(2)} KB)
                </p>
                <button
                  onClick={handleSubmitFile}
                  disabled={loading}
                  className="mt-2 px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Uploading...' : 'Upload Checker'}
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
            <h3 className="text-lg font-semibold">Write Checker Code</h3>
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
                <label htmlFor="checker-filename" className="block text-sm font-medium text-gray-700 mb-1">
                  File Name *
                </label>
                <input
                  id="checker-filename"
                  type="text"
                  value={fileName}
                  onChange={(e) => setFileName(e.target.value)}
                  placeholder="e.g., checker.py, validate.cpp, check.sh"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                />
              </div>
              
              <div>
                <label htmlFor="checker-code-content" className="block text-sm font-medium text-gray-700 mb-1">
                  Checker Code *
                </label>
                <textarea
                  id="checker-code-content"
                  value={codeContent}
                  onChange={(e) => setCodeContent(e.target.value)}
                  rows={20}
                  placeholder="Write your checker code here...

Example Python checker:
import sys

def check_solution(solution_output, expected_output):
    if solution_output.strip() == expected_output.strip():
        return True
    return False

# Your checker logic here
if __name__ == '__main__':
    # Read inputs and validate
    pass"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent font-mono text-sm"
                />
              </div>
              
              <button
                onClick={handleSubmitPastedCode}
                disabled={loading || !codeContent.trim() || !fileName.trim()}
                className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Saving...' : 'Save Checker'}
              </button>
            </div>
          )}
        </div>

        {/* Checker Testing Section */}
        {checker && (
          <div className="border border-gray-300 rounded-lg p-6 bg-gray-50">
            <h3 className="text-lg font-semibold mb-4">Test Checker</h3>
            <p className="text-sm text-gray-600 mb-4">
              Once you have uploaded a checker, you can test it against sample solutions to ensure it works correctly.
            </p>
            <button
              className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
              onClick={() => {
                // This could navigate to a test page or open a modal
                alert('Checker testing functionality would be implemented here');
              }}
            >
              Test Checker
            </button>
          </div>
        )}
      </div>
    </div>
  );
}