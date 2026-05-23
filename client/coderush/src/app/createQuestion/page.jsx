'use client'
import { useState } from 'react';
import api from '@/services/api';
import notify from '@/services/notify';

export default function CreateQuestion() {
  const [formData, setFormData] = useState({
    name: '',
    legend: '',
    inputFormat: '',
    outputFormat: '',
    notes: '',
    tutorial: '',
  });
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);
    try {
      await api.post('/api/question/createQuestion', formData);
      notify.success('Question created.');
      setFormData({
        name: '',
        legend: '',
        inputFormat: '',
        outputFormat: '',
        notes: '',
        tutorial: '',
      });
    } catch (err) {
      notify.error(err.message || 'Failed to create question.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Create Question</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          name="name"
          placeholder="Name"
          value={formData.name}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
          disabled={loading}
        />
        <textarea
          name="legend"
          placeholder="Legend"
          value={formData.legend}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
          disabled={loading}
        ></textarea>
        <textarea
          name="inputFormat"
          placeholder="Input Format"
          value={formData.inputFormat}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
          disabled={loading}
        ></textarea>
        <textarea
          name="outputFormat"
          placeholder="Output Format"
          value={formData.outputFormat}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
          disabled={loading}
        ></textarea>
        <textarea
          name="notes"
          placeholder="Notes"
          value={formData.notes}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          disabled={loading}
        ></textarea>
        <textarea
          name="tutorial"
          placeholder="Tutorial"
          value={formData.tutorial}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          disabled={loading}
        ></textarea>
        <button
          type="submit"
          disabled={loading}
          className={`bg-green-500 text-white px-4 py-2 rounded ${
            loading ? 'opacity-70 cursor-not-allowed' : 'hover:bg-green-600'
          }`}
        >
          {loading ? 'Creating…' : 'Submit'}
        </button>
      </form>
    </div>
  );
}
