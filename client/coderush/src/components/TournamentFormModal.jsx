import { useState } from 'react';

const TournamentFormModal = ({ closeModal }) => {
  const [formData, setFormData] = useState({
    description: '',
    name: '',
    startTime: '',
    rated: false,
    minRatingReq: 0,
    maxRatingReq: 0,
    durationInSeconds: 0,
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Basic validation
    if (!formData.name || !formData.startTime || !formData.durationInSeconds) {
      alert('Please fill in all required fields.');
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/tournament/createTournament`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(formData),
      });

      const result = await response.json();

      if (response.ok) {
        alert('Tournament created successfully!');
        closeModal();
      } else {
        alert(result.message || 'Failed to create tournament.');
      }
    } catch (error) {
      console.error('Error submitting tournament data:', error);
      alert('An error occurred while creating the tournament.');
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-8 rounded-xl shadow-lg w-full max-w-lg">
        <h2 className="text-xl font-bold mb-4 text-gray-800">Create New Tournament</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-600">Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Enter tournament description"
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600">Name</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600">Start Time</label>
            <input
              type="datetime-local"
              name="startTime"
              value={formData.startTime}
              onChange={handleChange}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-600">Rated:</label>
            <input
              type="checkbox"
              name="rated"
              checked={formData.rated}
              onChange={handleChange}
              className="form-checkbox h-5 w-5 text-indigo-600"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600">Min Rating Requirement</label>
            <input
              type="number"
              name="minRatingReq"
              value={formData.minRatingReq}
              onChange={handleChange}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600">Max Rating Requirement</label>
            <input
              type="number"
              name="maxRatingReq"
              value={formData.maxRatingReq}
              onChange={handleChange}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600">Duration (in seconds)</label>
            <input
              type="number"
              name="durationInSeconds"
              value={formData.durationInSeconds}
              onChange={handleChange}
              className="w-full px-4 py-2 mt-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div className="flex justify-end space-x-4">
            <button
              type="button"
              onClick={closeModal}
              className="px-4 py-2 bg-gray-300 hover:bg-gray-400 rounded-md"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
            >
              Create Tournament
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TournamentFormModal;
