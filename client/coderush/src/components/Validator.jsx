import { useState, useEffect } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import React from 'react';

const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export default function Validator({ questionId }) {
  const [validator, setValidator] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // File upload state
  const [selectedFile, setSelectedFile] = useState(null);
  
  // Copy-paste state
  const [codeContent, setCodeContent] = useState('');
  const [fileName, setFileName] = useState('');
  const [showPasteArea, setShowPasteArea] = useState(false);

  // Fetch existing validator on component mount
  useEffect(() => {
    fetchValidator();
  }, [questionId]);

  const fetchValidator = async () => {
    try {
      setLoading(true);
      const token = getCookie('token');
      const response = await axios.get(`${baseUrl}/api/questions/validator/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setValidator(response.data);
    } catch (err) {
      if (err.response?.status !== 404) {
        setError('Failed to fetch validator');
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
        `${baseUrl}/api/questions/validator/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Validator uploaded successfully!');
      setValidator(response.data);
      setSelectedFile(null);
      
      // Reset file input
      const fileInput = document.getElementById('validator-file-upload');
      if (fileInput) fileInput.value = '';
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload validator');
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
        `${baseUrl}/api/questions/validator/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Validator saved successfully!');
      setValidator(response.data);
      setCodeContent('');
      setFileName('');
      setShowPasteArea(false);
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save validator');
    } finally {
      setLoading(false);
    }
  };

  const togglePasteArea = () => {
    setShowPasteArea(!showPasteArea);
    if (!showPasteArea) {
      setSelectedFile(null);
      // Reset file input
      const fileInput = document.getElementById('validator-file-upload');
      if (fileInput) fileInput.value = '';
    }
    setError('');
  };

  const handleDeleteValidator = async () => {
    if (!confirm('Are you sure you want to delete this validator?')) {
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      const token = getCookie('token');
      await axios.delete(`${baseUrl}/api/questions/validator/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      
      setSuccess('Validator deleted successfully!');
      setValidator(null);
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete validator');
    } finally {
      setLoading(false);
    }
  };

  const handleRunValidator = async () => {
    if (!validator) {
      setError('No validator available to run');
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      const token = getCookie('token');
      const response = await axios.post(
        `${baseUrl}/api/questions/validator/${questionId}/run`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess(`Validator executed successfully! Result: ${response.data.result || 'Passed'}`);
      
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to run validator');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="validator-container p-6 bg-white rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-6">Validator Management</h2>
      
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

      {/* Existing Validator Display */}
      {validator && (
        <div className="mb-6 p-4 bg-gray-50 border rounded">
          <div className="flex justify-between items-start">
            <div className="flex-grow">
              <h3 className="text-lg font-semibold mb-2">Current Validator</h3>
              <p className="text-sm text-gray-600">
                File: {validator.fileName || 'validator.txt'}
              </p>
              <p className="text-sm text-gray-600">
                Uploaded: {new Date(validator.createdAt).toLocaleString()}
              </p>
              {validator.description && (
                <p className="text-sm text-gray-600 mt-1">
                  Description: {validator.description}
                </p>
              )}
              {validator.lastRun && (
                <p className="text-sm text-gray-600 mt-1">
                  Last Run: {new Date(validator.lastRun).toLocaleString()}
                </p>
              )}
            </div>
            <div className="flex space-x-2">
              <button
                onClick={handleRunValidator}
                disabled={loading}
                className="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Run
              </button>
              <button
                onClick={handleDeleteValidator}
                disabled={loading}
                className="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Upload Options */}
      <div className="space-y-6">
        
        {/* File Upload Section */}
        <div className="border-2 border-dashed border-gray-300 rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-4">Upload Validator File</h3>
          <div className="mb-4 p-3 bg-orange-50 border border-orange-200 rounded text-sm text-orange-700">
            <strong>Validator Guidelines:</strong>
            <ul className="mt-2 space-y-1">
              <li>• Should validate input constraints and problem requirements</li>
              <li>• Must check data format, ranges, and logical consistency</li>
              <li>• Should return detailed error messages for invalid inputs</li>
              <li>• Common patterns: input validation, constraint checking, format verification</li>
              <li>• Should handle edge cases and boundary conditions</li>
              <li>• Must be efficient for large test cases</li>
            </ul>
          </div>
          
          <div className="space-y-4">
            <input
              id="validator-file-upload"
              type="file"
              onChange={handleFileUpload}
              className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-orange-50 file:text-orange-700 hover:file:bg-orange-100"
              accept=".txt,.js,.py,.java,.cpp,.c,.sh,.rb,.go,.rs,.php"
            />
            
            {selectedFile && (
              <div className="mt-2">
                <p className="text-sm text-gray-600">
                  Selected: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(2)} KB)
                </p>
                <button
                  onClick={handleSubmitFile}
                  disabled={loading}
                  className="mt-2 px-4 py-2 bg-orange-600 text-white rounded hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Uploading...' : 'Upload Validator'}
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
            <h3 className="text-lg font-semibold">Write Validator Code</h3>
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
                <label htmlFor="validator-filename" className="block text-sm font-medium text-gray-700 mb-1">
                  File Name *
                </label>
                <input
                  id="validator-filename"
                  type="text"
                  value={fileName}
                  onChange={(e) => setFileName(e.target.value)}
                  placeholder="e.g., validator.py, validate_input.cpp, check_constraints.js"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                />
              </div>
              
              <div>
                <label htmlFor="validator-code-content" className="block text-sm font-medium text-gray-700 mb-1">
                  Validator Code *
                </label>
                <textarea
                  id="validator-code-content"
                  value={codeContent}
                  onChange={(e) => setCodeContent(e.target.value)}
                  rows={22}
                  placeholder="Write your validator code here...

Example Python validator:
import sys
import re

def validate_input(input_string):
    lines = input_string.strip().split('\n')
    
    # Check number of lines
    if len(lines) < 1:
        return False, 'Input must have at least one line'
    
    # Validate first line (e.g., number of test cases)
    try:
        n = int(lines[0])
        if n < 1 or n > 1000:
            return False, 'Number of test cases must be between 1 and 1000'
    except ValueError:
        return False, 'First line must be a valid integer'
    
    # Validate remaining lines
    for i in range(1, n + 1):
        if i >= len(lines):
            return False, f'Missing input for test case {i}'
        
        # Add your specific validation logic here
        line = lines[i].strip()
        if not line:
            return False, f'Test case {i} cannot be empty'
    
    return True, 'Input is valid'

if __name__ == '__main__':
    input_data = sys.stdin.read()
    valid, message = validate_input(input_data)
    
    if valid:
        print('VALID')
        sys.exit(0)
    else:
        print(f'INVALID: {message}')
        sys.exit(1)"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent font-mono text-sm"
                />
              </div>
              
              <button
                onClick={handleSubmitPastedCode}
                disabled={loading || !codeContent.trim() || !fileName.trim()}
                className="px-4 py-2 bg-orange-600 text-white rounded hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Saving...' : 'Save Validator'}
              </button>
            </div>
          )}
        </div>

        {/* Validator Testing and Management Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          
          {/* Test Validator Section */}
          <div className="border border-gray-300 rounded-lg p-4 bg-gray-50">
            <h3 className="text-lg font-semibold mb-3">Test Validator</h3>
            <p className="text-sm text-gray-600 mb-4">
              Test your validator against sample inputs to ensure it properly validates constraints and formats.
            </p>
            <button
              className="w-full px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
              onClick={() => {
                alert('Validator testing interface would be implemented here');
              }}
            >
              Open Test Interface
            </button>
          </div>

          {/* Validation History Section */}
          <div className="border border-gray-300 rounded-lg p-4 bg-gray-50">
            <h3 className="text-lg font-semibold mb-3">Validation History</h3>
            <p className="text-sm text-gray-600 mb-4">
              View logs and results from previous validator runs to debug and improve validation logic.
            </p>
            <button
              className="w-full px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
              onClick={() => {
                alert('Validation history would be shown here');
              }}
            >
              View History
            </button>
          </div>
        </div>

        {/* Validator Configuration Section */}
        {validator && (
          <div className="border border-orange-300 rounded-lg p-6 bg-orange-50">
            <h3 className="text-lg font-semibold mb-4">Validator Configuration</h3>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-white p-3 rounded border">
                <h4 className="font-medium text-sm text-gray-700 mb-2">Timeout Settings</h4>
                <p className="text-xs text-gray-600">Configure execution timeout for validator runs</p>
                <button className="mt-2 text-xs text-orange-600 hover:text-orange-800">Configure</button>
              </div>
              
              <div className="bg-white p-3 rounded border">
                <h4 className="font-medium text-sm text-gray-700 mb-2">Memory Limits</h4>
                <p className="text-xs text-gray-600">Set memory usage limits for validation</p>
                <button className="mt-2 text-xs text-orange-600 hover:text-orange-800">Configure</button>
              </div>
              
              <div className="bg-white p-3 rounded border">
                <h4 className="font-medium text-sm text-gray-700 mb-2">Error Reporting</h4>
                <p className="text-xs text-gray-600">Customize error message formatting</p>
                <button className="mt-2 text-xs text-orange-600 hover:text-orange-800">Configure</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}