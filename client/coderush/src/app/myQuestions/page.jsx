'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';

export default function MyQuestions() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const router = useRouter();

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const token = getCookie('token');
        if (!token) throw new Error("Authentication token not found.");

        const response = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/question/user`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        setQuestions(response.data);
      } catch (err) {
        console.error('Error fetching questions:', err);
        setError('Failed to load questions.');
      } finally {
        setLoading(false);
      }
    };

    fetchQuestions();
  }, []);

  const handleEditQuestion = (questionId) => {
    router.push(`/questionCreationArea?questionId=${questionId}`);
  };

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">My Questions</h1>

      {loading ? (
        <p className="text-gray-500">Loading...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <>
          {/* Button to navigate to Question Creation Area */}
          <Link href="/questionCreationArea">
            <button className="bg-blue-500 text-white px-4 py-2 rounded-lg mb-4 hover:bg-blue-600">
              Create New Question
            </button>
          </Link>

          {questions.length > 0 ? (
            <ul className="list-disc pl-5 space-y-2">
              {questions.map((q) => (
                <li
                  key={q.questionId}
                  className="bg-gray-100 p-4 rounded-lg shadow-md cursor-pointer"
                  onClick={() => handleEditQuestion(q.questionId)}
                >
                  <span className="text-blue-600 hover:underline">{q.name}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-500">No questions found.</p>
          )}
        </>
      )}
    </div>
  );
}
