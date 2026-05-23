import React, { useState, useEffect } from 'react';
import { Trash2, Edit3, Plus, Save, X } from 'lucide-react';
import api from '@/services/api';
import notify from '@/services/notify';

const FreeStyleTests = ({ questionId }) => {
  const [testCases, setTestCases] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Fetch existing test cases
  useEffect(() => {
    if (questionId) {
      fetchTestCases();
    }
  }, [questionId]);

  const fetchTestCases = async () => {
    try {
      const response = await api.get(
        `/api/testcase/getTestcasesByQuestionId/${questionId}`
      );
      setTestCases(response.data);
    } catch (err) {
      setError(err.message || 'Failed to fetch test cases');
      notify.error(err.message || 'Failed to fetch test cases');
    }
  };

  const addTestCase = () => {
    const newTestCase = {
      id: Date.now(), // Temporary ID for new cases
      questionId: questionId,
      input: '',
      output: '',
      rating: 1,
      isNew: true
    };
    setTestCases([...testCases, newTestCase]);
    setEditingId(newTestCase.id);
  };

  const saveTestCase = async (testCase) => {
    setLoading(true);
    setError('');

    try {
      const payload = {
        questionId: questionId,
        input: testCase.input,
        output: testCase.output,
        rating: parseInt(testCase.rating),
      };

      let response;
      if (testCase.isNew) {
        response = await api.post('/api/testcase/createTestcase', payload);
      } else {
        response = await api.put(`/api/testcase/${testCase.testcaseId}`, {
          ...payload,
          testcaseId: testCase.testcaseId,
        });
      }

      const updatedTestCase = response.data;
      setTestCases(prev =>
        prev.map(tc =>
          tc.id === testCase.id || tc.testcaseId === testCase.testcaseId
            ? { ...updatedTestCase, id: updatedTestCase.testcaseId }
            : tc
        )
      );
      setEditingId(null);
      notify.success(testCase.isNew ? 'Test case created.' : 'Test case updated.');
    } catch (err) {
      setError(err.message || 'Failed to save test case');
      notify.error(err.message || 'Failed to save test case');
    } finally {
      setLoading(false);
    }
  };

  const deleteTestCase = async (testCase) => {
    if (!testCase.testcaseId) {
      // If it's a new unsaved test case, just remove it from state
      setTestCases(prev => prev.filter(tc => tc.id !== testCase.id));
      return;
    }

    setLoading(true);
    try {
      await api.delete(`/api/testcase/${testCase.testcaseId}`);

      setTestCases(prev => prev.filter(tc => tc.testcaseId !== testCase.testcaseId));
      notify.success('Test case deleted.');
    } catch (err) {
      setError(err.message || 'Failed to delete test case');
      notify.error(err.message || 'Failed to delete test case');
    } finally {
      setLoading(false);
    }
  };

  const updateTestCase = (id, field, value) => {
    setTestCases(prev => 
      prev.map(tc => 
        (tc.id === id || tc.testcaseId === id)
          ? { ...tc, [field]: value }
          : tc
      )
    );
  };

  const cancelEdit = (testCase) => {
    if (testCase.isNew) {
      setTestCases(prev => prev.filter(tc => tc.id !== testCase.id));
    }
    setEditingId(null);
  };

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-lg">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Test Cases</h2>
        <button
          onClick={addTestCase}
          disabled={loading}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white px-4 py-2 rounded-lg transition-colors"
        >
          <Plus size={16} />
          Add Test
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      <div className="space-y-4">
        {testCases.map((testCase, index) => {
          const isEditing = editingId === testCase.id || editingId === testCase.testcaseId;
          const tcId = testCase.id || testCase.testcaseId;
          
          return (
            <div
              key={tcId}
              className="border border-gray-200 rounded-lg p-4 bg-gray-50"
            >
              <div className="flex justify-between items-center mb-3">
                <h3 className="text-lg font-semibold text-gray-700">
                  Test Case {index + 1}
                </h3>
                <div className="flex gap-2">
                  {isEditing ? (
                    <>
                      <button
                        onClick={() => saveTestCase(testCase)}
                        disabled={loading}
                        className="flex items-center gap-1 bg-green-600 hover:bg-green-700 disabled:bg-green-400 text-white px-3 py-1 rounded text-sm transition-colors"
                      >
                        <Save size={14} />
                        Save
                      </button>
                      <button
                        onClick={() => cancelEdit(testCase)}
                        disabled={loading}
                        className="flex items-center gap-1 bg-gray-600 hover:bg-gray-700 disabled:bg-gray-400 text-white px-3 py-1 rounded text-sm transition-colors"
                      >
                        <X size={14} />
                        Cancel
                      </button>
                    </>
                  ) : (
                    <>
                      <button
                        onClick={() => setEditingId(tcId)}
                        disabled={loading}
                        className="flex items-center gap-1 bg-yellow-600 hover:bg-yellow-700 disabled:bg-yellow-400 text-white px-3 py-1 rounded text-sm transition-colors"
                      >
                        <Edit3 size={14} />
                        Edit
                      </button>
                      <button
                        onClick={() => deleteTestCase(testCase)}
                        disabled={loading}
                        className="flex items-center gap-1 bg-red-600 hover:bg-red-700 disabled:bg-red-400 text-white px-3 py-1 rounded text-sm transition-colors"
                      >
                        <Trash2 size={14} />
                        Delete
                      </button>
                    </>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Input
                  </label>
                  {isEditing ? (
                    <textarea
                      value={testCase.input || ''}
                      onChange={(e) => updateTestCase(tcId, 'input', e.target.value)}
                      className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      rows="3"
                      placeholder="Enter input..."
                    />
                  ) : (
                    <div className="p-2 bg-white border border-gray-300 rounded-md min-h-[76px] whitespace-pre-wrap">
                      {testCase.input || 'No input specified'}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Expected Output
                  </label>
                  {isEditing ? (
                    <textarea
                      value={testCase.output || ''}
                      onChange={(e) => updateTestCase(tcId, 'output', e.target.value)}
                      className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      rows="3"
                      placeholder="Enter expected output..."
                    />
                  ) : (
                    <div className="p-2 bg-white border border-gray-300 rounded-md min-h-[76px] whitespace-pre-wrap">
                      {testCase.output || 'No output specified'}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Rating
                  </label>
                  {isEditing ? (
                    <input
                      type="number"
                      min="1"
                      max="1000"
                      value={testCase.rating || 1}
                      onChange={(e) => updateTestCase(tcId, 'rating', parseInt(e.target.value) || 1)}
                      className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Enter rating (1-1000)"
                    />
                  ) : (
                    <div className="p-2 bg-white border border-gray-300 rounded-md">
                      <span className="font-medium">{testCase.rating || 1}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {testCases.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <p className="text-lg">No test cases yet</p>
          <p className="text-sm">Click "Add Test" to create your first test case</p>
        </div>
      )}

      {loading && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-4 rounded-lg">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-2 text-gray-600">Processing...</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default FreeStyleTests;