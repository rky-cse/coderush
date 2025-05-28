'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';

function Solution({ questionId }) {
  const [file, setFile] = useState(null);
  const [text, setText] = useState("");
  const [fileName, setFileName] = useState("");
  const [loading, setLoading] = useState(false);
  const [solutionExists, setSolutionExists] = useState(false);
  const [solutionFileName, setSolutionFileName] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  
  // Get these from your environment or auth context
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || '';
  const token = getCookie('token'); // You'll need to implement this function
  
  // Check if solution already exists on component mount
  useEffect(() => {
    checkExistingSolution();
  }, [questionId]);
  
  const checkExistingSolution = async () => {
    try {
      const response = await axios.get(
        `${baseUrl}/api/questions/solution/${questionId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // If we get here, solution exists
      setSolutionExists(true);
      
      // Extract filename from Content-Disposition header
      const contentDisposition = response.headers['content-disposition'];
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match) {
          setSolutionFileName(match[1]);
        }
      }
    } catch (error) {
      // If 404 or 204, solution doesn't exist
      if (error.response && (error.response.status === 404 || error.response.status === 204)) {
        setSolutionExists(false);
      } else {
        console.error('Error checking existing solution:', error);
      }
    }
  };
  
  const handleUploadSolution = async () => {
    // Validate input
    if (!file && (!text || !fileName)) {
      setError("Please provide either a file or text content with a filename");
      return;
    }
    
    setLoading(true);
    setError("");
    setSuccess("");
    
    const formData = new FormData();
    
    if (file) {
      formData.append("file", file);
    } else if (text && fileName) {
      // Convert text to file
      const blob = new Blob([text], { type: "text/plain" });
      const fakeFile = new File([blob], fileName);
      formData.append("file", fakeFile);
    }
    
    try {
      // Use POST for new upload or PUT for update
      const method = solutionExists ? 'put' : 'post';
      const response = await axios[method](
        `${baseUrl}/api/questions/solution/${questionId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      setSuccess(solutionExists ? "Solution updated successfully" : "Solution uploaded successfully");
      setSolutionExists(true);
      setSolutionFileName(file ? file.name : fileName);
      
      // Clear form
      setFile(null);
      setText("");
      setFileName("");
      
      // Clear file input
      const fileInput = document.getElementById('solution-file-input');
      if (fileInput) fileInput.value = '';
      
    } catch (error) {
      console.error('Error uploading solution:', error);
      setError(error.response?.data || "Failed to upload solution");
    } finally {
      setLoading(false);
    }
  };
  
  const handleDownloadSolution = async () => {
    if (!solutionExists) return;
    
    setLoading(true);
    setError("");
    
    try {
      const response = await axios.get(
        `${baseUrl}/api/questions/solution/${questionId}`,
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
      link.setAttribute('download', solutionFileName || 'solution-file');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
    } catch (error) {
      console.error('Error downloading solution:', error);
      setError("Failed to download solution");
    } finally {
      setLoading(false);
    }
  };
  
  const handleViewSolution = async () => {
    if (!solutionExists) return;
    
    setLoading(true);
    setError("");
    
    try {
      const response = await axios.get(
        `${baseUrl}/api/questions/solution/${questionId}`,
        {
          responseType: 'text',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // Display content in the text area for viewing/editing
      setText(response.data);
      setFileName(solutionFileName);
      
    } catch (error) {
      console.error('Error viewing solution:', error);
      setError("Failed to load solution content");
    } finally {
      setLoading(false);
    }
  };
  
  const clearForm = () => {
    setFile(null);
    setText("");
    setFileName("");
    setError("");
    setSuccess("");
    
    const fileInput = document.getElementById('solution-file-input');
    if (fileInput) fileInput.value = '';
  };
  
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Solution</h2>
      
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
      
      {/* Existing Solution Display */}
      {solutionExists && (
        <div className="border p-4 rounded-md mb-4 bg-gray-50">
          <h3 className="font-medium mb-2">Current Solution File</h3>
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-600">
              📄 {solutionFileName || 'Solution file'}
            </span>
            <div className="space-x-2">
              <button
                onClick={handleViewSolution}
                disabled={loading}
                className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
              >
                View/Edit
              </button>
              <button
                onClick={handleDownloadSolution}
                disabled={loading}
                className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50"
              >
                Download
              </button>
            </div>
          </div>
        </div>
      )}
      
      {/* Upload Section */}
      <div className="space-y-4">
        <h3 className="font-medium">
          {solutionExists ? 'Update Solution' : 'Upload Solution'}
        </h3>
        
        {/* File Upload Option */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Upload File
          </label>
          <input
            id="solution-file-input"
            type="file"
            className="w-full p-2 border rounded-md"
            onChange={(e) => {
              setFile(e.target.files?.[0] || null);
              // Clear text input when file is selected
              if (e.target.files?.[0]) {
                setText("");
                setFileName("");
              }
            }}
          />
        </div>
        
        <div className="text-center text-gray-500">OR</div>
        
        {/* Text Content Option */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Paste Content
          </label>
          <textarea
            className="w-full p-3 border rounded-md min-h-[200px] font-mono text-sm"
            value={text}
            onChange={(e) => {
              setText(e.target.value);
              // Clear file input when text is entered
              if (e.target.value) {
                setFile(null);
                const fileInput = document.getElementById('solution-file-input');
                if (fileInput) fileInput.value = '';
              }
            }}
            placeholder="Paste solution code here..."
          />
          
          <input
            type="text"
            className="w-full p-2 border rounded-md mt-2"
            placeholder="File name (e.g., solution.cpp, solution.py)"
            value={fileName}
            onChange={(e) => setFileName(e.target.value)}
          />
        </div>
        
        {/* Action Buttons */}
        <div className="flex space-x-2 justify-end">
          <button
            onClick={clearForm}
            className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
          >
            Clear
          </button>
          <button
            onClick={handleUploadSolution}
            disabled={loading || (!file && (!text || !fileName))}
            className={`px-4 py-2 rounded ${
              loading || (!file && (!text || !fileName))
                ? 'bg-gray-300 cursor-not-allowed'
                : 'bg-indigo-600 text-white hover:bg-indigo-700'
            }`}
          >
            {loading 
              ? 'Processing...' 
              : (solutionExists ? 'Update Solution' : 'Upload Solution')
            }
          </button>
        </div>
      </div>
    </div>
  );
}

// Cookie utility function - you can move this to a separate utils file
function getCookie(name) {
  if (typeof document === 'undefined') return null;
  
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  
  return null;
}

export default Solution;