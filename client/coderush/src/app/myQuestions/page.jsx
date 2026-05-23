'use client';
import React, { useState, useEffect } from 'react';
import { Clock, Eye, MessageCircle, Search, Filter, Plus, Code, FileText, Star, Image, Sparkles } from 'lucide-react';
import api from '@/services/api';
import notify from '@/services/notify';

const MyQuestionsPage = () => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRated, setFilterRated] = useState('all');

  useEffect(() => {
    fetchQuestions();
  }, []);

  const fetchQuestions = async () => {
    try {
      setLoading(true);
      const { data } = await api.get('/api/question/user');
      setQuestions(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch questions');
      notify.error(err.message || 'Failed to fetch questions');
    } finally {
      setLoading(false);
    }
  };

  const handleQuestionClick = (questionId) => {
    // Navigate to questions/{questionId}
    window.location.href = `/questions/${questionId}`;
    // Or if using Next.js router:
    // router.push(`/questions/${questionId}`);
  };

  const filteredQuestions = questions.filter(question => {
    const matchesSearch = question.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         question.legend?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         question.notes?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesFilter = filterRated === 'all' || 
                         (filterRated === 'rated' && question.rated) ||
                         (filterRated === 'unrated' && !question.rated);
    
    return matchesSearch && matchesFilter;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading your questions...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            <p className="font-medium">Error loading questions</p>
            <p className="text-sm">{error}</p>
          </div>
          <button
            onClick={fetchQuestions}
            className="mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">My Coding Questions</h1>
          <p className="text-gray-600">Manage and solve your programming challenges</p>
        </div>

        {/* Controls */}
        <div className="mb-6 flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
          <div className="flex flex-col sm:flex-row gap-4 flex-1">
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search questions..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent w-full sm:w-64"
              />
            </div>

            {/* Filter */}
            <div className="relative">
              <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <select
                value={filterRated}
                onChange={(e) => setFilterRated(e.target.value)}
                className="pl-10 pr-8 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none bg-white"
              >
                <option value="all">All Questions</option>
                <option value="rated">Rated Only</option>
                <option value="unrated">Unrated Only</option>
              </select>
            </div>
          </div>

          {/* AI Generate Button */}
          <a href="/generateQuestion"
            className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition font-medium text-sm">
            <Sparkles className="h-4 w-4" />
            AI Generate
          </a>
        </div>

        {/* Questions Grid */}
        {filteredQuestions.length === 0 ? (
          <div className="text-center py-12">
            <Code className="h-16 w-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-xl font-medium text-gray-900 mb-2">No questions found</h3>
            <p className="text-gray-600">
              {searchTerm || filterRated !== 'all' 
                ? "Try adjusting your search or filter criteria" 
                : "You haven't created any questions yet"}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredQuestions.map((question) => (
              <div 
                key={question.questionId} 
                onClick={() => handleQuestionClick(question.questionId)}
                className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md hover:border-blue-300 transition-all cursor-pointer group"
              >
                {/* Header */}
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-2">
                    <Code className="h-5 w-5 text-blue-600" />
                    <span className="text-sm font-medium text-gray-500">#{question.questionId}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    {question.rated && (
                      <Star className="h-4 w-4 text-yellow-500 fill-current" />
                    )}
                    {question.imageUrls && (
                      <Image className="h-4 w-4 text-gray-400" />
                    )}
                    {question.freeStyle && (
                      <span className="px-2 py-1 bg-purple-100 text-purple-800 text-xs rounded-full">
                        Free Style
                      </span>
                    )}
                  </div>
                </div>

                {/* Question Title */}
                <h3 className="text-lg font-semibold text-gray-900 mb-2 group-hover:text-blue-600 transition-colors">
                  {question.name}
                </h3>

                {/* Legend/Description */}
                <div className="mb-4">
                  <p className="text-gray-600 font-mono text-sm bg-gray-50 p-3 rounded border">
                    {question.legend}
                  </p>
                </div>

                {/* Input/Output Format */}
                <div className="space-y-2 mb-4">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-medium text-gray-500 w-16">INPUT:</span>
                    <code className="text-xs bg-blue-50 text-blue-800 px-2 py-1 rounded font-mono">
                      {question.inputFormat}
                    </code>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-medium text-gray-500 w-16">OUTPUT:</span>
                    <code className="text-xs bg-green-50 text-green-800 px-2 py-1 rounded font-mono">
                      {question.outputFormat}
                    </code>
                  </div>
                </div>

                {/* Notes */}
                {question.notes && (
                  <div className="mb-4">
                    <div className="flex items-start gap-2">
                      <FileText className="h-4 w-4 text-gray-400 mt-0.5" />
                      <p className="text-sm text-gray-600 line-clamp-2">{question.notes}</p>
                    </div>
                  </div>
                )}

                {/* Tutorial indicator */}
                {question.tutorial && (
                  <div className="flex items-center gap-2 text-sm text-blue-600">
                    <MessageCircle className="h-4 w-4" />
                    <span>Tutorial available</span>
                  </div>
                )}

                {/* Click indicator */}
                <div className="mt-4 pt-4 border-t border-gray-100">
                  <div className="flex justify-between items-center">
                    <span className="text-xs text-gray-500">Click to edit</span>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity">
                      <div className="w-2 h-2 bg-blue-600 rounded-full"></div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Stats */}
        <div className="mt-8 bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Stats</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{questions.length}</div>
              <div className="text-sm text-gray-600">Total Questions</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-yellow-600">
                {questions.filter(q => q.rated).length}
              </div>
              <div className="text-sm text-gray-600">Rated</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">
                {questions.filter(q => q.freeStyle).length}
              </div>
              <div className="text-sm text-gray-600">Free Style</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {questions.filter(q => q.tutorial).length}
              </div>
              <div className="text-sm text-gray-600">With Tutorial</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyQuestionsPage;