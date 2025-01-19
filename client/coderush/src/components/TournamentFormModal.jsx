import React, { useState } from 'react';

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
    
    // Form validation
    if (
      !formData.name ||
      !formData.startTime ||
      !formData.durationInSeconds
    ) {
      alert('Please fill in all required fields.');
      return;
    }
  
    // Handle form submission logic here (send data to backend)
    try {
      const response = await fetch('http://localhost:8080/api/tournament/createTournament', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(formData),
      });
  
      const result = await response.json();
  
      if (response.ok) {
        console.log('Tournament Created:', result);
        // Pass the result to the homepage or display it in the modal as feedback
        alert('Tournament created successfully!');
        closeModal(); // Close the modal after submission
  
        // Optionally, handle the response here (e.g., pass it to a parent component)
        // Example: setTournamentInfo(result);
      } else {
        console.error('Error creating tournament:', result);
        alert('Failed to create tournament.');
      }
    } catch (error) {
      console.error('Error submitting tournament data:', error);
      alert('An error occurred while creating the tournament.');
    }
  };
  

  return (
    <div style={overlayStyles}>
      <div style={modalStyles}>
        <h2>Create New Tournament</h2>
        <form onSubmit={handleSubmit}>

          <div style={fieldGroupStyles}>
            <label>Description:</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Enter tournament description"
            />
          </div>

          <div style={fieldGroupStyles}>
            <label>Name:</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>

          <div style={fieldGroupStyles}>
            <label>Start Time:</label>
            <input
              type="datetime-local"
              name="startTime"
              value={formData.startTime}
              onChange={handleChange}
              required
            />
          </div>

          <div style={fieldGroupStyles}>
            <label>Rated:</label>
            <input
              type="checkbox"
              name="rated"
              checked={formData.rated}
              onChange={handleChange}
            />
          </div>

          <div style={fieldGroupStyles}>
            <label>Min Rating Requirement:</label>
            <input
              type="number"
              name="minRatingReq"
              value={formData.minRatingReq}
              onChange={handleChange}
            />
          </div>

          <div style={fieldGroupStyles}>
            <label>Max Rating Requirement:</label>
            <input
              type="number"
              name="maxRatingReq"
              value={formData.maxRatingReq}
              onChange={handleChange}
            />
          </div>

          <div style={fieldGroupStyles}>
            <label>Duration (in seconds):</label>
            <input
              type="number"
              name="durationInSeconds"
              value={formData.durationInSeconds}
              onChange={handleChange}
              required
            />
          </div>

          <button type="submit">Create Tournament</button>
        </form>
        <button onClick={closeModal}>Close</button>
      </div>
    </div>
  );
};

const overlayStyles = {
  position: 'fixed',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.5)',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
};

const modalStyles = {
  backgroundColor: 'white',
  padding: '20px',
  borderRadius: '8px',
  width: '80%',
  maxWidth: '600px',
  display: 'flex',
  flexDirection: 'column',
  gap: '10px',
};

const fieldGroupStyles = {
  display: 'flex',
  flexDirection: 'column',
};

export default TournamentFormModal;
