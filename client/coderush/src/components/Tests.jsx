'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';

function Tests({ questionId }) {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isAddingTest, setIsAddingTest] = useState(false);
  const [uploadLoading, setUploadLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // For new test upload
  const [file, setFile] = useState(null);
  const [text, setText] = useState('');
  const [fileName, setFileName] = useState('');
  
  // For updating existing test
  const [editingTest, setEditingTest] = useState(null);
  
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || '';
  const token = getCookie('token');
  
  // Fetch all tests on component mount
  useEffect(() => {
    fetchTests();
  }, [questionId]);
  
  const fetchTests = async () => {
    try {
      setLoading(true);
      const response = await axios.get(
        `${baseUrl}/api/questions/${questionId}/tests`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setTests(response.data.tests || []);
    } catch (error) {
      console.error('Error fetching tests:', error);
      setError('Failed to load test cases');
    } finally {
      setLoading(false);
    }
  };
  
  const handleAddTest = () => {
    setIsAddingTest(true);
    setEditingTest(null);
    clearForm();
  };
  
  const handleCancelAdd = () => {
    setIsAddingTest(false);
    setEditingTest(null);
    clearForm();
  };
  
  const clearForm = () => {
    setFile(null);
    setText('');
    setFileName('');
    setError('');
    setSuccess('');
    
    // Clear file input
    const fileInput = document.getElementById('test-file-input');
    if (fileInput) fileInput.value = '';
  };
  
  const handleSubmitTest = async () => {
    // Validate input
    if (!file && (!text || !fileName)) {
      setError('Please provide either a file or text content with a filename');
      return;
    }
    
    setUploadLoading(true);
    setError('');
    setSuccess('');
    
    const formData = new FormData();
    
    if (file) {
      formData.append('file', file);
    } else if (text && fileName) {
      // Convert text to file
      const blob = new Blob([text], { type: 'text/plain' });
      const fakeFile = new File([blob], fileName);
      formData.append('file', fakeFile);
    }
    
    try {
      let response;
      if (editingTest) {
        // Update existing test
        response = await axios.put(
          `${baseUrl}/api/questions/${questionId}/tests/${editingTest.id}`,
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data',
              Authorization: `Bearer ${token}`,
            },
          }
        );
      } else {
        // Create new test
        response = await axios.post(
          `${baseUrl}/api/questions/${questionId}/tests`,
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data',
              Authorization: `Bearer ${token}`,
            },
          }
        );
      }
      
      setSuccess(editingTest ? 'Test case updated successfully' : 'Test case uploaded successfully');
      
      // Refresh the tests list
      await fetchTests();
      
      // Reset form
      setIsAddingTest(false);
      setEditingTest(null);
      clearForm();
      
    } catch (error) {
      console.error('Error uploading test:', error);
      setError(error.response?.data || 'Failed to upload test case');
    } finally {
      setUploadLoading(false);
    }
  };
  
  const handleDownloadTest = async (testId, fileName) => {
    try {
      const response = await axios.get(
        `${baseUrl}/api/questions/${questionId}/tests/${testId}`,
        {
          responseType: 'blob',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // Create blob URL and trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName || `test-${testId}.txt`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
    } catch (error) {
      console.error('Error downloading test:', error);
      setError('Failed to download test case');
    }
  };
  
  const handleEditTest = async (test) => {
    try {
      // Fetch the test content for editing
      const response = await axios.get(
        `${baseUrl}/api/questions/${questionId}/tests/${test.id}`,
        {
          responseType: 'text',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setEditingTest(test);
      setText(response.data);
      setFileName(test.fileName);
      setIsAddingTest(true);
      
    } catch (error) {
      console.error('Error loading test for editing:', error);
      setError('Failed to load test case for editing');
    }
  };
  
  const handleDeleteTest = async (testId) => {
    if (!confirm('Are you sure you want to delete this test case?')) {
      return;
    }
    
    try {
      await axios.delete(
        `${baseUrl}/api/questions/${questionId}/tests/${testId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess('Test case deleted successfully');
      
      // Refresh the tests list
      await fetchTests();
      
    } catch (error) {
      console.error('Error deleting test:', error);
      setError('Failed to delete test case');
    }
  };
  
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };
  
  if (loading) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-sm">
        <div className="flex justify-center items-center h-32">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-500"></div>
        </div>
      </div>
    );
  }
  
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">Test Cases</h2>
        {!isAddingTest && (
          <button
            onClick={handleAddTest}
            className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700"
          >
            Add Test
          </button>
        )}
      </div>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      
      {success && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {success}
        </div>
      )}
      
      {/* Add/Edit Test Form */}
      {isAddingTest && (
        <div className="border p-4 rounded-md mb-4">
          <h3 className="font-medium mb-3">
            {editingTest ? `Edit Test Case #${editingTest.id}` : 'Add New Test Case'}
          </h3>
          
          {/* File Upload Option */}
          <div className="mb-3">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Upload File
            </label>
            <input
              id="test-file-input"
              type="file"
              className="w-full p-2 border rounded-md"
              onChange={(e) => {
                setFile(e.target.files?.[0] || null);
                // Clear text input when file is selected
                if (e.target.files?.[0]) {
                  setText('');
                  setFileName('');
                }
              }}
            />
          </div>
          
          <div className="text-center text-gray-500 mb-3">OR</div>
          
          {/* Text Content Option */}
          <div className="mb-3">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Paste Content
            </label>
            <textarea
              className="w-full p-3 border rounded-md min-h-[150px] font-mono text-sm"
              rows="6"
              placeholder="Paste test case content here..."
              value={text}
              onChange={(e) => {
                setText(e.target.value);
                // Clear file input when text is entered
                if (e.target.value) {
                  setFile(null);
                  const fileInput = document.getElementById('test-file-input');
                  if (fileInput) fileInput.value = '';
                }
              }}
            />
            
            <input
              type="text"
              className="w-full p-2 border rounded-md mt-2"
              placeholder="File name (e.g., input1.txt, test1.in)"
              value={fileName}
              onChange={(e) => setFileName(e.target.value)}
            />
          </div>
          
          {/* Action Buttons */}
          <div className="flex space-x-2 justify-end">
            <button
              onClick={handleCancelAdd}
              className="px-3 py-1 bg-gray-500 text-white rounded hover:bg-gray-600"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmitTest}
              disabled={uploadLoading || (!file && (!text || !fileName))}
              className={`px-3 py-1 rounded ${
                uploadLoading || (!file && (!text || !fileName))
                  ? 'bg-gray-300 cursor-not-allowed'
                  : 'bg-indigo-600 text-white hover:bg-indigo-700'
              }`}
            >
              {uploadLoading 
                ? 'Processing...' 
                : (editingTest ? 'Update Test' : 'Upload Test')
              }
            </button>
          </div>
        </div>
      )}
      
      {/* Tests List */}
      {tests.length === 0 && !isAddingTest ? (
        <div className="text-center p-8 text-gray-500 border border-dashed rounded-md">
          No test cases added yet. Click 'Add Test' to create one.
        </div>
      ) : (
        <div className="space-y-3">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Test ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Filename
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Size
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {tests.map((test) => (
                <tr key={test.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    #{test.id}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {test.fileName || 'Unknown'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatFileSize(test.size)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                    <button
                      onClick={() => handleEditTest(test)}
                      className="text-blue-600 hover:text-blue-900"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDownloadTest(test.id, test.fileName)}
                      className="text-indigo-600 hover:text-indigo-900"
                    >
                      Download
                    </button>
                    <button
                      onClick={() => handleDeleteTest(test.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// Cookie utility function
function getCookie(name) {
  if (typeof document === 'undefined') return null;
  
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  
  return null;
}

export default Tests;