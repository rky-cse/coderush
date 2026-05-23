'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import api from '@/services/api';
import notify from '@/services/notify';
import QuestionNav from '@/components/QuestionNav';
import ProblemStatement from '@/components/ProblemStatement';
import Tests from '@/components/Tests';
import Solution from '@/components/Solution';
import Checker from '@/components/Checker';
import Validator from '@/components/Validator';
import Invocation from '@/components/Invocation';
import FreeStyleTests from '@/components/FreeStyleTests';

export default function QuestionDetails() {
  const { questionId } = useParams();
  const [questionData, setQuestionData] = useState(null);
  const [activeTab, setActiveTab] = useState('general');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchQuestion() {
      try {
        const response = await api.get(`/api/question/${questionId}`);
        setQuestionData(response.data);
      } catch (err) {
        notify.error(err.message || 'Failed to load question');
      } finally {
        setLoading(false);
      }
    }

    fetchQuestion();
  }, [questionId]);


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
      case 'freeStyleTests':
        return <FreeStyleTests questionId={questionId} />;
      case 'tests':
        return <Tests questionId={questionId} />;
      case 'solution':
        return <Solution questionId={questionId} />;
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
          <p className="mt-1 text-sm text-gray-900">{questionData?.name}</p>
        </div>
        {/* Add other general fields as needed */}
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