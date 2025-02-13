'use client';
import { useEffect, useState } from 'react';
import CreateQuestion from '@/components/CreateQuestion';
import CreateTestcase from '@/components/CreateTestcase';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import axios from 'axios';
import { getCookie } from 'cookies-next';

export default function QuestionCreationArea() {
  const [activeTab, setActiveTab] = useState('question');
  const searchParams = useSearchParams();
  const questionId = searchParams.get('questionId'); // Get question ID from URL
  const [questionData, setQuestionData] = useState(null);

  useEffect(() => {
    const fetchQuestionDetails = async () => {
      if (!questionId) return;

      try {
        const token = getCookie('token');
        const response = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/question/${questionId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        setQuestionData(response.data);
      } catch (error) {
        console.error('Error fetching question details:', error);
      }
    };

    fetchQuestionDetails();
  }, [questionId]);

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">Question Creation Area</h1>

      {/* Back to My Questions Page */}
      <Link href="/myQuestions">
        <button className="bg-gray-500 text-white px-4 py-2 rounded-lg mb-4 hover:bg-gray-600">
          Back to My Questions
        </button>
      </Link>

      {/* Toggle Buttons */}
      <div className="flex space-x-4 mb-4">
        <button
          onClick={() => setActiveTab('question')}
          className={`px-4 py-2 rounded ${activeTab === 'question' ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}
        >
          Create/Edit Question
        </button>
        <button
          onClick={() => setActiveTab('testcase')}
          className={`px-4 py-2 rounded ${activeTab === 'testcase' ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}
        >
          Create Testcase
        </button>
      </div>

      {/* Render Components Based on Active Tab */}
      {activeTab === 'question' ? <CreateQuestion questionData={questionData} /> : <CreateTestcase />}
    </div>
  );
}
