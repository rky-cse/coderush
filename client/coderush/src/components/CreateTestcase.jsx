'use client'
import { useState } from 'react';
import api from '@/services/api';
import notify from '@/services/notify';

export default function CreateTestcase() {
  const [formData, setFormData] = useState({
    questionId: '',
    input: '',
    output: '',
    rating: 0, // Only applicable for FreeStyle Testcases
    testcaseType: 'freeStyle', // default option set to FreeStyle
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'rating' ? parseInt(value, 10) : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const endpoint =
        formData.testcaseType === 'classic'
          ? '/api/testcase/createClassicTestcase'
          : '/api/testcase/createTestcase';

      const { testcaseType, ...rest } = formData;
      const payload = { ...rest };
      if (testcaseType === 'classic') {
        delete payload.rating;
      }

      await api.post(endpoint, payload);
      notify.success('Testcase created.');
    } catch (error) {
      notify.error(error.message || 'Failed to create testcase.');
    }
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Create Testcase</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="number"
          name="questionId"
          placeholder="Question ID"
          value={formData.questionId}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        />
        <textarea
          name="input"
          placeholder="Input"
          value={formData.input}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        ></textarea>
        <textarea
          name="output"
          placeholder="Output"
          value={formData.output}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        ></textarea>

        <div>
          <label htmlFor="testcaseType" className="block mb-2">
            Testcase Type
          </label>
          <select
            name="testcaseType"
            value={formData.testcaseType}
            onChange={handleInputChange}
            className="block w-full border px-3 py-2"
          >
            <option value="freeStyle">FreeStyle Testcase</option>
            <option value="classic">Classic Testcase</option>
          </select>
        </div>

        {formData.testcaseType === 'freeStyle' && (
          <input
            type="number"
            name="rating"
            placeholder="Rating"
            value={formData.rating}
            onChange={handleInputChange}
            className="block w-full border px-3 py-2"
            required
          />
        )}

        <button
          type="submit"
          className="bg-green-500 text-white px-4 py-2 rounded"
        >
          Submit
        </button>
      </form>
    </div>
  );
}
