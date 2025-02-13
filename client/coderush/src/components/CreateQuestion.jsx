'use client';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';

export default function CreateQuestion({ questionData }) {
  const [formData, setFormData] = useState({
    name: '',
    legend: '',
    inputFormat: '',
    outputFormat: '',
    notes: '',
    tutorial: '',
  });

  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // Load existing question data (for editing)
  useEffect(() => {
    if (questionData) {
      setFormData({
        name: questionData.name || '',
        legend: questionData.legend || '',
        inputFormat: questionData.inputFormat || '',
        outputFormat: questionData.outputFormat || '',
        notes: questionData.notes || '',
        tutorial: questionData.tutorial || '',
      });
    }
  }, [questionData]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccessMessage('');
    setErrorMessage('');

    try {
      const token = getCookie('token');
      if (!token) throw new Error('Authentication token not found.');

      let response;
      if (questionData?.questionId) {
        // Update existing question
        response = await axios.put(
          `${process.env.NEXT_PUBLIC_API_URL}/api/question/${questionData.questionId}`,
          formData,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setSuccessMessage('Question updated successfully!');
      } else {
        // Create new question
        response = await axios.post(
          `${process.env.NEXT_PUBLIC_API_URL}/api/question/createQuestion`,
          formData,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setSuccessMessage('Question created successfully!');
        // ✅ Removed form reset to keep data visible
      }

      console.log(response.data);
    } catch (error) {
      console.error('Error saving question:', error);
      setErrorMessage(error.response?.data?.message || 'Failed to save question.');
    } finally {
      setLoading(false);
      // ✅ Automatically hide success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    }
  };

  return (
    <div className="container mx-auto p-6 max-w-2xl">
      <h1 className="text-3xl font-bold mb-6 text-center">
        {questionData ? 'Edit Question' : 'Create Question'}
      </h1>

      {/* Success and Error Messages */}
      {successMessage && <p className="text-green-600 bg-green-100 p-3 rounded">{successMessage}</p>}
      {errorMessage && <p className="text-red-600 bg-red-100 p-3 rounded">{errorMessage}</p>}

      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          name="name"
          placeholder="Question Name"
          value={formData.name}
          onChange={handleInputChange}
          className="block w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500"
          required
        />
        <textarea
          name="legend"
          placeholder="Problem Statement / Legend"
          value={formData.legend}
          onChange={handleInputChange}
          className="block w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500"
          required
        />
        <textarea
          name="inputFormat"
          placeholder="Input Format"
          value={formData.inputFormat}
          onChange={handleInputChange}
          className="block w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500"
          required
        />
        <textarea
          name="outputFormat"
          placeholder="Output Format"
          value={formData.outputFormat}
          onChange={handleInputChange}
          className="block w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500"
          required
        />
        <textarea
          name="notes"
          placeholder="Additional Notes (Optional)"
          value={formData.notes}
          onChange={handleInputChange}
          className="block w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500"
        />
        <textarea
          name="tutorial"
          placeholder="Tutorial / Explanation (Optional)"
          value={formData.tutorial}
          onChange={handleInputChange}
          className="block w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500"
        />

        <button
          type="submit"
          className={`w-full text-white px-4 py-2 rounded-lg transition ${
            loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-500 hover:bg-green-600'
          }`}
          disabled={loading}
        >
          {loading ? 'Saving...' : questionData ? 'Update Question' : 'Create Question'}
        </button>
      </form>
    </div>
  );
}
