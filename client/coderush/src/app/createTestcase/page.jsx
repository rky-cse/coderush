'use client'
import { useState } from 'react';
import axios from 'axios';

export default function CreateTestcase() {
  const [formData, setFormData] = useState({
    questionId: '',
    input: '',
    output: '',
    rating: 0,
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
      const token = localStorage.getItem('token'); // Retrieve token from localStorage or any other method
      const response = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/api/testcase/createTestcase`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      alert('Testcase created successfully!');
      console.log(response.data);
    } catch (error) {
      console.error('Error creating testcase:', error);
      alert('Failed to create testcase.');
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
        <input
          type="number"
          name="rating"
          placeholder="Rating"
          value={formData.rating}
          onChange={handleInputChange}
          className="block w-full border px-3 py-2"
          required
        />
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
