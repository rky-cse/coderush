import { useState } from 'react';
import { getCookie } from 'cookies-next';

const TournamentFormModal = ({ closeModal }) => {
  const [formData, setFormData] = useState({
    description: '',
    name: '',
    startTime: '',
    rated: false,
    minRatingReq: 0,
    maxRatingReq: 0,
    durationInSeconds: 0,
    penaltyFactor: 0,
    visibility: 'PUBLIC',
    password: '',
    tournamentType: 'FREE_STYLE',
    teamStyle: false,
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

    if (!formData.name || !formData.startTime || !formData.durationInSeconds) {
      alert('Please fill in all required fields.');
      return;
    }

    const utcStartTime = Math.floor(new Date(formData.startTime).getTime());

    const payload = {
      ...formData,
      startTime: utcStartTime,
    };

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/tournament/mtm/createMTMTournament`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getCookie('token')}`,
        },
        body: JSON.stringify(payload),
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
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white p-6 rounded-xl shadow-lg max-w-4xl w-full max-h-screen overflow-y-auto">
        <h2 className="text-2xl font-bold text-indigo-600 mb-4">Create New Tournament</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Tournament Name</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="Enter tournament description"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Start Time (Local)</label>
            <input
              type="datetime-local"
              name="startTime"
              value={formData.startTime}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Min Rating</label>
              <input
                type="number"
                name="minRatingReq"
                value={formData.minRatingReq}
                onChange={handleChange}
                className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Max Rating</label>
              <input
                type="number"
                name="maxRatingReq"
                value={formData.maxRatingReq}
                onChange={handleChange}
                className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Duration (seconds)</label>
            <input
              type="number"
              name="durationInSeconds"
              value={formData.durationInSeconds}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Penalty Per Wrong Submission (seconds)</label>
            <input
              type="number"
              name="penaltyFactor"
              value={formData.penaltyFactor}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Visibility</label>
              <select
                name="visibility"
                value={formData.visibility}
                onChange={handleChange}
                className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              >
                <option value="PUBLIC">Public</option>
                <option value="PRIVATE">Private</option>
              </select>
            </div>

            {formData.visibility === 'PRIVATE' && (
              <div>
                <label className="block text-sm font-medium text-gray-700">Password</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Tournament Type</label>
            <select
              name="tournamentType"
              value={formData.tournamentType}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            >
              <option value="FREE_STYLE">FreeStyle</option>
              <option value="CLASSIC">Classic</option>
              
            
            </select>
          </div>

          <div className="flex items-center space-x-3">
            <input
              type="checkbox"
              name="rated"
              checked={formData.rated}
              onChange={handleChange}
              className="h-5 w-5 text-indigo-600"
            />
            <label className="text-sm font-medium text-gray-700">Rated Tournament</label>
          </div>

          <div className="flex items-center space-x-3">
            <input
              type="checkbox"
              name="teamStyle"
              checked={formData.teamStyle}
              onChange={handleChange}
              className="h-5 w-5 text-indigo-600"
            />
            <label className="text-sm font-medium text-gray-700">Team Style Tournament</label>
          </div>

          <div className="flex justify-end space-x-4">
            <button
              type="button"
              onClick={closeModal}
              className="px-4 py-2 bg-gray-400 text-white rounded-md hover:bg-gray-500"
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
