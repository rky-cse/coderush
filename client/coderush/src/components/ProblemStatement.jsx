import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';
export default function ProblemStatement({ questionId }) {
  const [problemData, setProblemData] = useState({
    name: '',
    legend: '',
    inputFormat: '',
    outputFormat: '',
    notes: '',
    tutorial: ''
    // statement field removed as requested
  });
  const [loading, setLoading] = useState(true);
  const token = getCookie('token');

  const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  useEffect(() => {
    async function fetchProblemStatement() {
      try {
        const response = await axios.get(
          `${baseUrl}/api/question/${questionId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (response.data) {
          setProblemData({
            name: response.data.name || '',
            legend: response.data.legend || '',
            inputFormat: response.data.inputFormat || '',
            outputFormat: response.data.outputFormat || '',
            notes: response.data.notes || '',
            tutorial: response.data.tutorial || ''
          });
        }
      } catch (error) {
        console.error('Error fetching problem statement:', error);
      }
      setLoading(false);
    }

    fetchProblemStatement();
  }, [questionId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProblemData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = async () => {
    try {
      await axios.put(
        `${baseUrl}/api/question/${questionId}`,
        problemData,
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

  if (loading) {
    return (
      <div className="h-40 flex items-center justify-center">
        <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

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
      
      <div className="space-y-4">
        {/* Problem Name */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Problem Name
          </label>
          <input
            type="text"
            name="name"
            className="w-full p-2 border rounded-md"
            value={problemData.name}
            onChange={handleChange}
            placeholder="Enter problem name"
          />
        </div>
        
        {/* legend */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            legend
          </label>
          <textarea 
            name="legend"
            className="w-full p-3 border rounded-md min-h-[100px]" 
            value={problemData.legend}
            onChange={handleChange}
            placeholder="Enter any variable definitions or legend"
          />
        </div>
        
        {/* Input Format */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Input Format
          </label>
          <textarea 
            name="inputFormat"
            className="w-full p-3 border rounded-md min-h-[100px]" 
            value={problemData.inputFormat}
            onChange={handleChange}
            placeholder="Describe the input format"
          />
        </div>
        
        {/* Output Format */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Output Format
          </label>
          <textarea 
            name="outputFormat"
            className="w-full p-3 border rounded-md min-h-[100px]" 
            value={problemData.outputFormat}
            onChange={handleChange}
            placeholder="Describe the expected output format"
          />
        </div>
        
        {/* Notes */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Notes
          </label>
          <textarea 
            name="notes"
            className="w-full p-3 border rounded-md min-h-[100px]" 
            value={problemData.notes}
            onChange={handleChange}
            placeholder="Enter any additional notes, constraints or examples"
          />
        </div>
        
        {/* Tutorial */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Tutorial
          </label>
          <textarea 
            name="tutorial"
            className="w-full p-3 border rounded-md min-h-[150px]" 
            value={problemData.tutorial}
            onChange={handleChange}
            placeholder="Provide a tutorial or solution explanation (optional)"
          />
        </div>
      </div>
    </div>
  );
}