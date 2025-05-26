'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import axios from 'axios';
import QuestionNav from '@/components/QuestionNav';
import { getCookie } from 'cookies-next';

const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export default function QuestionDetails() {
  const { questionId } = useParams();
  const [questionData, setQuestionData] = useState(null);
  const [activeTab, setActiveTab] = useState('general');
  const [loading, setLoading] = useState(true);
  const token = getCookie('token');
  console.log(token);
  
useEffect(() => {
  async function fetchQuestion() {
    try {
      const response = await axios.get(
        `${baseUrl}/api/question/${questionId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`, // Include your token here
          },
        }
      );

      setQuestionData(response.data);
    } catch (error) {
      console.error('Error fetching question:', error);
    } finally {
      setLoading(false);
    }
  }

  fetchQuestion();
}, [questionId]); // include token if it's reactive


  const renderTabContent = () => {
    switch (activeTab) {
      case 'general':
        return <GeneralInfo questionData={questionData} />;
      case 'problem':
        return <ProblemStatement questionId={questionId} />;
      case 'checker':
        return <Checker questionId={questionId} />;
      case 'validator':
        return <Validator questionId={questionId} />;
      case 'tests':
        return <Tests questionId={questionId} />;
      case 'invocation':
        return <Invocation questionId={questionId} />;
      case 'access':
        return <AccessControl questionId={questionId} />;
      default:
        return <div>Select a tab</div>;
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">{questionData?.questionName || 'Question Details'}</h1>
      <QuestionNav 
        questionId={questionId} 
        activeTab={activeTab} 
        onTabChange={setActiveTab} 
      />
      <div className="mt-6">
        {renderTabContent()}
      </div>
    </div>
  );
}

// Tab content components
function GeneralInfo({ questionData }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">General Information</h2>
      <div className="grid grid-cols-1 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700">Question ID</label>
          <p className="mt-1 text-sm text-gray-900">{questionData?.questionId}</p>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Question Name</label>
          <p className="mt-1 text-sm text-gray-900">{questionData?.questionName}</p>
        </div>
        {/* Add other general fields as needed */}
      </div>
    </div>
  );
}

function ProblemStatement({ questionId }) {
  const [statement, setStatement] = useState('');
  const [loading, setLoading] = useState(true);
  const token = getCookie('token');
  console.log(token);

    useEffect(() => {
    async function fetchProblemStatement() {
        try {
        const response = await axios.get(
            `${baseUrl}/api/questions/${questionId}/problem-statement`,
            {
            headers: {
                Authorization: `Bearer ${token}`, // using your existing token
            },
            }
        );

        setStatement(response.data.statement || '');
        } catch (error) {
        console.error('Error fetching problem statement:', error);
        }
        setLoading(false);
    }

    fetchProblemStatement();
    }, [questionId]); // include token in dependency if it's dynamic


  const handleSave = async () => {
    try {
      await axios.put(`${baseUrl}/api/questions/${questionId}/problem-statement`, {
        statement
      }
      ,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
      // Show success message
      alert('Problem statement saved successfully');
    } catch (error) {
      console.error('Error saving problem statement:', error);
      alert('Error saving problem statement');
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">Problem Statement</h2>
        <button 
          onClick={handleSave}
          className="px-3 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700"
        >
          Save
        </button>
      </div>
      {loading ? (
        <div className="h-40 flex items-center justify-center">
          <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-500"></div>
        </div>
      ) : (
        <textarea 
          className="w-full p-3 border rounded-md min-h-[300px]" 
          value={statement}
          onChange={(e) => setStatement(e.target.value)}
          placeholder="Enter the problem statement here"
        />
      )}
    </div>
  );
}

function Checker({ questionId }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Checker</h2>
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">Checker Type</label>
        <select className="w-full p-2 border rounded-md">
          <option value="standard">Standard</option>
          <option value="custom">Custom</option>
        </select>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Custom Checker Code</label>
        <textarea 
          className="w-full p-3 border rounded-md min-h-[200px]"
          placeholder="Enter custom checker code here..."
        ></textarea>
      </div>
    </div>
  );
}

function Validator({ questionId }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Validator</h2>
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">Input Format</label>
        <textarea 
          className="w-full p-3 border rounded-md min-h-[100px]"
          placeholder="Describe the input format..."
        ></textarea>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Validation Code</label>
        <textarea 
          className="w-full p-3 border rounded-md min-h-[200px]"
          placeholder="Enter validation code here..."
        ></textarea>
      </div>
    </div>
  );
}

function Tests({ questionId }) {
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


function Invocation({ questionId }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Invocation</h2>
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Time Limit (seconds)</label>
          <input 
            type="number" 
            className="w-full p-2 border rounded-md"
            min="0.1"
            step="0.1"
            defaultValue="1.0"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Memory Limit (MB)</label>
          <input 
            type="number" 
            className="w-full p-2 border rounded-md"
            min="16"
            step="16"
            defaultValue="256"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Allowed Languages</label>
          <div className="grid grid-cols-3 gap-2">
            <label className="flex items-center">
              <input type="checkbox" className="mr-2" defaultChecked />
              Java
            </label>
            <label className="flex items-center">
              <input type="checkbox" className="mr-2" defaultChecked />
              C++
            </label>
            <label className="flex items-center">
              <input type="checkbox" className="mr-2" defaultChecked />
              Python
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}

function AccessControl({ questionId }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Access Control</h2>
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Visibility</label>
          <select className="w-full p-2 border rounded-md">
            <option value="private">Private</option>
            <option value="public">Public</option>
            <option value="limited">Limited Access</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Authorized Users</label>
          <textarea 
            className="w-full p-3 border rounded-md min-h-[100px]"
            placeholder="Enter usernames or email addresses, one per line"
          ></textarea>
        </div>
      </div>
    </div>
  );
}