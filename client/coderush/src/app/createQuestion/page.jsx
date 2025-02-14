'use client'
import { useState } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';

export default function CreateQuestion() {
  const [formData, setFormData] = useState({
    name: '',
    legend: '',
    inputFormat: '',
    outputFormat: '',
    notes: '',
    tutorial: '',
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = getCookie('token'); // Retrieve token from localStorage or any other method
      const response = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/api/question/createQuestion`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      alert('Question created successfully!');
      console.log(response.data);
    } catch (error) {
      console.error('Error creating question:', error);
      alert('Failed to create question.');
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
        />
        <textarea
          name="legend"
          placeholder="Legend"
          value={formData.legend}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        ></textarea>
        <textarea
          name="inputFormat"
          placeholder="Input Format"
          value={formData.inputFormat}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        ></textarea>
        <textarea
          name="outputFormat"
          placeholder="Output Format"
          value={formData.outputFormat}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        ></textarea>
        <textarea
          name="notes"
          placeholder="Notes"
          value={formData.notes}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
        ></textarea>
        <textarea
          name="tutorial"
          placeholder="Tutorial"
          value={formData.tutorial}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
        ></textarea>
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
