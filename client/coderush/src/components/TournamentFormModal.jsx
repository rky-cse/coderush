import { useState } from 'react';
import { getCookie } from 'cookies-next';
import { FaTrophy, FaClock, FaLock, FaGlobe, FaUserFriends, FaChartLine, FaInfoCircle, FaUsers, FaUserTag, FaExclamationTriangle, FaChevronDown } from 'react-icons/fa';

const TournamentFormModal = ({ closeModal }) => {
  const [formData, setFormData] = useState({
    description: '',
    name: '',
    startTime: '',
    rated: false,
    minRatingReq: 0,
    maxRatingReq: 0,
    durationInSeconds: 7200, // Default 2 hours
    penaltyFactor: 300,      // Default 5 minutes
    visibility: 'PUBLIC',
    password: '',
    tournamentType: 'FREE_STYLE',
    teamStyle: false,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [expandedSections, setExpandedSections] = useState({
    basic: true,
    schedule: true,
    settings: true,
    access: true
  });

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const validateField = (name, value) => {
    switch (name) {
      case 'name':
        return value.trim() ? '' : 'Tournament name is required';
      case 'startTime':
        return value ? '' : 'Start time is required';
      case 'durationInSeconds':
        return value > 0 ? '' : 'Duration must be greater than 0';
      case 'password':
        return formData.visibility === 'PRIVATE' && !value.trim() 
          ? 'Password is required for private tournaments' 
          : '';
      default:
        return '';
    }
  };

  // Fixed handleChange function to properly handle text input
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    // For text inputs, use the raw value (including spaces)
    const newValue = type === 'checkbox' ? checked : value;
    
    setFormData(prev => ({
      ...prev,
      [name]: newValue,
    }));

    // Clear error when field is edited
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    
    // Special case: if visibility changes to PUBLIC, clear password error
    if (name === 'visibility' && value === 'PUBLIC') {
      setErrors(prev => ({ ...prev, password: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Validate all fields
    const newErrors = {};
    Object.entries(formData).forEach(([name, value]) => {
      const error = validateField(name, value);
      if (error) newErrors[name] = error;
    });

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      setIsSubmitting(false);
      
      // Find the section with errors and expand it
      if (newErrors.name || newErrors.description) {
        setExpandedSections(prev => ({ ...prev, basic: true }));
      }
      if (newErrors.startTime || newErrors.durationInSeconds) {
        setExpandedSections(prev => ({ ...prev, schedule: true }));
      }
      if (newErrors.visibility || newErrors.password) {
        setExpandedSections(prev => ({ ...prev, access: true }));
      }
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
        // Success notification
        closeModal();
      } else {
        setErrors({ submit: result.message || 'Failed to create tournament.' });
      }
    } catch (error) {
      console.error('Error submitting tournament data:', error);
      setErrors({ submit: 'Network error while creating the tournament.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Helper function to render tooltip
  const renderTooltip = (text) => (
    <div className="group relative flex items-center">
      <FaInfoCircle className="ml-1.5 text-gray-400 hover:text-gray-600 cursor-help text-sm" />
      <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-opacity z-50">
        {text}
        <div className="absolute top-full left-1/2 transform -translate-x-1/2 border-4 border-transparent border-t-gray-800"></div>
      </div>
    </div>
  );

  // Helper to convert seconds to hours for display
  const secondsToHours = (seconds) => {
    return (seconds / 3600).toFixed(1);
  };

  // Helper to convert seconds to minutes for display
  const secondsToMinutes = (seconds) => {
    return (seconds / 60).toFixed(0);
  };

  // Section component for consistent styling
  const Section = ({ title, id, children, hasError }) => (
    <div className="border border-gray-200 dark:border-gray-700 rounded-lg mb-6 overflow-hidden">
      <div 
        className={`flex justify-between items-center p-4 cursor-pointer 
                   ${hasError ? 'bg-red-50 dark:bg-red-900/10' : 'bg-gray-50 dark:bg-gray-800'}`}
        onClick={() => toggleSection(id)}
      >
        <h3 className={`text-lg font-medium flex items-center 
                       ${hasError ? 'text-red-700 dark:text-red-400' : 'text-gray-800 dark:text-gray-200'}`}>
          {title}
          {hasError && (
            <span className="ml-2 w-2 h-2 rounded-full bg-red-500"></span>
          )}
        </h3>
        <FaChevronDown className={`transform transition-transform ${expandedSections[id] ? 'rotate-180' : ''}`} />
      </div>
      
      {expandedSections[id] && (
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 animate-fadeIn">
          {children}
        </div>
      )}
    </div>
  );

  const hasBasicErrors = errors.name || errors.description;
  const hasScheduleErrors = errors.startTime || errors.durationInSeconds; 
  const hasSettingsErrors = errors.penaltyFactor || errors.minRatingReq || errors.maxRatingReq;
  const hasAccessErrors = errors.visibility || errors.password;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn">
      <div onClick={e => e.stopPropagation()} className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
        <div className="bg-gradient-to-r from-indigo-600 to-purple-600 p-4 text-white">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <FaTrophy className="text-2xl mr-3" />
              <h2 className="text-xl font-bold">Create New Tournament</h2>
            </div>
            <button 
              onClick={closeModal}
              className="text-white/80 hover:text-white transition rounded-full p-1 hover:bg-white/10"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
        
        {/* Scrollable form content */}
        <div className="overflow-y-auto p-5 flex-grow">
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Global error message */}
            {errors.submit && (
              <div className="bg-red-50 border-l-4 border-red-500 p-4 dark:bg-red-900/20 dark:border-red-700 mb-6">
                <div className="flex items-center">
                  <FaExclamationTriangle className="text-red-500 dark:text-red-400" />
                  <p className="ml-2 text-red-700 dark:text-red-400">{errors.submit}</p>
                </div>
              </div>
            )}
            
            {/* Basic Info Section */}
            <Section 
              title="Basic Information" 
              id="basic" 
              hasError={hasBasicErrors}
            >
              <div className="space-y-4">
                <div>
                  <div className="flex items-center mb-1">
                    <label htmlFor="tournament-name" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Tournament Name</label>
                    {errors.name && <p className="ml-2 text-xs text-red-600 dark:text-red-400">{errors.name}</p>}
                  </div>
                  <input
                    id="tournament-name"
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-offset-1 focus:outline-none ${
                      errors.name 
                        ? 'border-red-500 focus:ring-red-200 dark:focus:ring-red-900' 
                        : 'border-gray-200 focus:ring-indigo-200 dark:border-gray-700 dark:bg-gray-800/50 dark:text-white'
                    }`}
                    placeholder="Enter tournament name"
                    autoComplete="off"
                  />
                </div>

                <div>
                  <div className="flex items-center mb-1">
                    <label htmlFor="tournament-description" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Description</label>
                    {errors.description && <p className="ml-2 text-xs text-red-600 dark:text-red-400">{errors.description}</p>}
                  </div>
                  <textarea
                    id="tournament-description"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows="4"
                    className="w-full px-4 py-2 border border-gray-200 rounded-md focus:ring-2 focus:ring-indigo-200 focus:ring-offset-1 focus:outline-none dark:border-gray-700 dark:bg-gray-800/50 dark:text-white"
                    placeholder="Describe the tournament rules, prize details, eligibility, etc."
                  />
                </div>
                
                <div className="flex items-center p-3 bg-indigo-50 border border-indigo-100 rounded-md dark:bg-indigo-900/20 dark:border-indigo-900">
                  <div className="flex-shrink-0 mr-3">
                    <FaInfoCircle className="text-indigo-600 dark:text-indigo-400" />
                  </div>
                  <p className="text-sm text-indigo-700 dark:text-indigo-300">
                    A clear and detailed description helps participants understand what to expect from your tournament.
                  </p>
                </div>
              </div>
            </Section>
            
            {/* Schedule Section */}
            <Section 
              title="Schedule" 
              id="schedule" 
              hasError={hasScheduleErrors}
            >
              <div className="space-y-4">
                <div>
                  <div className="flex items-center mb-1">
                    <label htmlFor="start-time" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Start Time (Local)</label>
                    {errors.startTime && <p className="ml-2 text-xs text-red-600 dark:text-red-400">{errors.startTime}</p>}
                  </div>
                  <div className="flex items-center">
                    <FaClock className="text-gray-400 mr-2" />
                    <input
                      id="start-time"
                      type="datetime-local"
                      name="startTime"
                      value={formData.startTime}
                      onChange={handleChange}
                      className={`w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-offset-1 focus:outline-none ${
                        errors.startTime 
                          ? 'border-red-500 focus:ring-red-200 dark:focus:ring-red-900' 
                          : 'border-gray-200 focus:ring-indigo-200 dark:border-gray-700 dark:bg-gray-800/50 dark:text-white'
                      }`}
                    />
                  </div>
                  <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                    Tournament start time in your local timezone
                  </p>
                </div>

                <div>
                  <div className="flex items-center mb-1">
                    <label htmlFor="duration-slider" className="block text-sm font-medium text-gray-700 dark:text-gray-300">
                      Duration
                    </label>
                    {errors.durationInSeconds && (
                      <p className="ml-2 text-xs text-red-600 dark:text-red-400">{errors.durationInSeconds}</p>
                    )}
                    {renderTooltip('Total time participants will have to complete all problems')}
                  </div>
                  <div className="flex items-center">
                    <input
                      id="duration-slider"
                      type="range"
                      name="durationInSeconds"
                      min="1800"
                      max="14400"
                      step="900"
                      value={formData.durationInSeconds}
                      onChange={handleChange}
                      className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-indigo-600 dark:bg-gray-700"
                    />
                  </div>
                  <div className="flex justify-between mt-1 text-xs text-gray-500 dark:text-gray-400">
                    <span>30 min</span>
                    <span className="text-indigo-600 font-medium dark:text-indigo-400">
                      {secondsToHours(formData.durationInSeconds)} hours ({formData.durationInSeconds} seconds)
                    </span>
                    <span>4 hours</span>
                  </div>
                </div>
              </div>
            </Section>
            
            {/* Access Section */}
            <Section 
              title="Access Control" 
              id="access" 
              hasError={hasAccessErrors}
            >
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Visibility</label>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div 
                      className={`flex items-center p-3 border rounded-lg cursor-pointer transition ${
                        formData.visibility === 'PUBLIC'
                          ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20 dark:border-indigo-600'
                          : 'border-gray-200 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-800/50'
                      }`}
                      onClick={() => setFormData(prev => ({...prev, visibility: 'PUBLIC'}))}
                    >
                      <div className="flex-shrink-0">
                        <FaGlobe className={formData.visibility === 'PUBLIC' ? "text-indigo-600 dark:text-indigo-400" : "text-gray-400"} />
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium">Public</p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Visible to everyone, anyone can join
                        </p>
                      </div>
                      <input 
                        type="radio" 
                        name="visibility" 
                        value="PUBLIC"
                        checked={formData.visibility === 'PUBLIC'} 
                        onChange={handleChange}
                        className="ml-auto h-4 w-4 text-indigo-600 focus:ring-indigo-500"
                      />
                    </div>
                    <div 
                      className={`flex items-center p-3 border rounded-lg cursor-pointer transition ${
                        formData.visibility === 'PRIVATE'
                          ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20 dark:border-indigo-600'
                          : 'border-gray-200 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-800/50'
                      }`}
                      onClick={() => setFormData(prev => ({...prev, visibility: 'PRIVATE'}))}
                    >
                      <div className="flex-shrink-0">
                        <FaLock className={formData.visibility === 'PRIVATE' ? "text-indigo-600 dark:text-indigo-400" : "text-gray-400"} />
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium">Private</p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Password protected, invitation only
                        </p>
                      </div>
                      <input 
                        type="radio" 
                        name="visibility" 
                        value="PRIVATE"
                        checked={formData.visibility === 'PRIVATE'} 
                        onChange={handleChange}
                        className="ml-auto h-4 w-4 text-indigo-600 focus:ring-indigo-500"
                      />
                    </div>
                  </div>
                </div>

                {formData.visibility === 'PRIVATE' && (
                  <div className="pt-2 animate-fadeIn">
                    <div className="flex items-center mb-1">
                      <label htmlFor="tournament-password" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Password</label>
                      {errors.password && <p className="ml-2 text-xs text-red-600 dark:text-red-400">{errors.password}</p>}
                    </div>
                    <div className="flex items-center">
                      <FaLock className="text-gray-400 mr-2" />
                      <input
                        id="tournament-password"
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        className={`w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-offset-1 focus:outline-none ${
                          errors.password 
                            ? 'border-red-500 focus:ring-red-200 dark:focus:ring-red-900' 
                            : 'border-gray-200 focus:ring-indigo-200 dark:border-gray-700 dark:bg-gray-800/50 dark:text-white'
                        }`}
                        placeholder="Enter tournament password"
                        autoComplete="new-password"
                      />
                    </div>
                    <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                      Password will be required for participants to join
                    </p>
                  </div>
                )}
              </div>
            </Section>
          </form>
        </div>
        
        {/* Form actions */}
        <div className="border-t border-gray-200 dark:border-gray-700 p-4 bg-gray-50 dark:bg-gray-800/50 flex justify-end items-center">
          <div className="flex space-x-3">
            <button
              type="button"
              onClick={closeModal}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-700"
            >
              Cancel
            </button>
            
            <button
              type="button"
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-70 disabled:cursor-not-allowed dark:bg-indigo-700 dark:hover:bg-indigo-800 flex items-center"
            >
              {isSubmitting ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Creating...
                </>
              ) : (
                'Create Tournament'
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TournamentFormModal;