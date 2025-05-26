'use client';

import { useState } from 'react';

export default function QuestionNav({ questionId, activeTab, onTabChange }) {
  const tabs = [
    { id: 'general', label: 'General Info' },
    { id: 'problem', label: 'Problem Statement' },
    { id: 'checker', label: 'Checker' },
    { id: 'validator', label: 'Validator' },
    { id: 'tests', label: 'Tests' },
    { id: 'invocation', label: 'Invocation' },
    { id: 'access', label: 'Access Control' },
  ];

  return (
    <nav className="flex flex-wrap mb-6 border-b overflow-x-auto">
      {tabs.map(tab => (
        <button
          key={tab.id}
          onClick={() => onTabChange(tab.id)}
          className={`py-2 px-4 mr-1 ${
            activeTab === tab.id
              ? 'border-b-2 border-indigo-500 font-medium text-indigo-600'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          {tab.label}
        </button>
      ))}
    </nav>
  );
}