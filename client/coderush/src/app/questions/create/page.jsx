'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { getCookie } from 'cookies-next';

export default function CreateQuestion() {
  const [questionName, setQuestionName] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e) => {
  e.preventDefault();
  setLoading(true);

  try {
    const token = getCookie('token'); // or use your existing token variable
    const baseUrl = process.env.NEXT_PUBLIC_API_URL;

    const response = await fetch(`${baseUrl}/api/question/createQuestion`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ name:questionName }),
    });

    if (!response.ok) {
      throw new Error('Failed to create question');
    }

    const data = await response.json();
    router.push(`/questions/${data.questionId}`);
  } catch (error) {
    console.error('Error creating question:', error);
    setLoading(false);
  }
};


  return (
    <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-bold mb-6">Create New Question</h1>
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label htmlFor="questionName" className="block text-sm font-medium text-gray-700">
            Question Name
          </label>
          <input
            type="text"
            id="questionName"
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            value={questionName}
            onChange={(e) => setQuestionName(e.target.value)}
            placeholder="Enter question name"
            required
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 disabled:opacity-50"
        >
          {loading ? 'Creating...' : 'Create Question'}
        </button>
      </form>
    </div>
  );
}