
import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';

export default function Tests({ questionId }) {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // For the new test case
  const [file, setFile] = useState(null);
  const [text, setText] = useState("");
  const [fileName, setFileName] = useState("");
  const [isAddingTest, setIsAddingTest] = useState(false);
  
  // Get these from your auth context or environment variables
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || '';

  const token = getCookie('token');
  console.log(token);
  
  useEffect(() => {
    async function fetchTests() {
      try {
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
      } finally {
        setLoading(false);
      }
    }

    fetchTests();
  }, [questionId]);
  
  const handleAddTest = () => {
    setIsAddingTest(true);
    setFile(null);
    setText("");
    setFileName("");
  };
  
  const handleCancelAdd = () => {
    setIsAddingTest(false);
  };
  
  const handleSubmitTest = async () => {
    // Validate that either file or (text and fileName) is provided
    if (!file && (!text || !fileName)) {
      alert("Please provide either a file or text content with a file name");
      return;
    }
    
    const formData = new FormData();
    formData.append("questionId", questionId);
    
    if (file) {
      formData.append("file", file);
    } else if (text && fileName) {
      const blob = new Blob([text], { type: "text/plain" });
      const fakeFile = new File([blob], fileName);
      formData.append("file", fakeFile);
    }
    
    try {
      setLoading(true);
      
      const response = await axios.post(
        `${baseUrl}/api/questions/tests/upload`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // Add the new test to the list using data from the response
      // The server returns testcaseId in the response
      const newTest = {
        id: response.data.testcaseId, // Use the testcaseId from the response
        fileName: file ? file.name : fileName,
        size: file ? file.size : text.length
      };
      
      setTests([...tests, newTest]);
      setIsAddingTest(false);
      alert("Test uploaded successfully");
    } catch (error) {
      console.error("Error uploading test:", error);
      alert("Error uploading test: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };
  
  const handleDeleteTest = async (testId) => {
    if (!confirm("Are you sure you want to delete this test?")) {
      return;
    }
    
    try {
      setLoading(true);
      
      await axios.delete(
        `${baseUrl}/api/questions/${questionId}/tests/${testId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // Remove the test from the list
      setTests(tests.filter(test => test.id !== testId));
      alert("Test deleted successfully");
    } catch (error) {
      console.error("Error deleting test:", error);
      alert("Error deleting test: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
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
      
      // Create blob link to download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Error downloading test:", error);
      alert("Error downloading test: " + (error.response?.data?.message || error.message));
    }
  };
  
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">Tests</h2>
        {!isAddingTest && (
          <button 
            className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700"
            onClick={handleAddTest}
          >
            Add Test
          </button>
        )}
      </div>
      
      {loading ? (
        <div className="flex justify-center items-center h-32">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-500"></div>
        </div>
      ) : (
        <>
          {isAddingTest ? (
            <div className="border p-4 rounded-md mb-4">
              <h3 className="font-medium mb-3">Add New Test</h3>
              
              <div className="mb-3">
                <label className="block text-sm font-medium text-gray-700 mb-1">Upload File</label>
                <input 
                  type="file" 
                  className="w-full p-2 border rounded-md"
                  onChange={e => {
                    setFile(e.target.files?.[0] || null);
                    // Clear the text input when a file is selected
                    if (e.target.files?.[0]) {
                      setText("");
                      setFileName("");
                    }
                  }}
                />
              </div>
              
              <div className="mb-3">
                <label className="block text-sm font-medium text-gray-700 mb-1">Or Paste Content</label>
                <textarea 
                  className="w-full p-2 border rounded-md mb-2"
                  rows="4"
                  placeholder="Paste test content here"
                  value={text}
                  onChange={e => {
                    setText(e.target.value);
                    // Clear the file input when text is entered
                    if (e.target.value) setFile(null);
                  }}
                ></textarea>
                
                <input 
                  type="text" 
                  className="w-full p-2 border rounded-md"
                  placeholder="File name (e.g., input.txt)"
                  value={fileName}
                  onChange={e => setFileName(e.target.value)}
                />
              </div>
              
              <div className="flex space-x-2 justify-end">
                <button 
                  className="px-3 py-1 bg-gray-500 text-white rounded hover:bg-gray-600"
                  onClick={handleCancelAdd}
                >
                  Cancel
                </button>
                <button 
                  className="px-3 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                  onClick={handleSubmitTest}
                >
                  Upload Test
                </button>
              </div>
            </div>
          ) : null}
          
          {tests.length === 0 && !isAddingTest ? (
            <div className="text-center p-8 text-gray-500 border border-dashed rounded-md">
              No tests added yet. Click 'Add Test' to create one.
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
                        {test.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {test.fileName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {typeof test.size === 'number' ? 
                          `${(test.size / 1024).toFixed(2)} KB` : 
                          'Unknown'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button 
                          className="text-red-600 hover:text-red-900 mr-2"
                          onClick={() => handleDeleteTest(test.id)}
                        >
                          Delete
                        </button>
                        <button 
                          className="text-indigo-600 hover:text-indigo-900"
                          onClick={() => handleDownloadTest(test.id, test.fileName)}
                        >
                          Download
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}