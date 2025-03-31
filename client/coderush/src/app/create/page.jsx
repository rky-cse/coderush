'use client'
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { getCookie } from 'cookies-next';
import { FaChevronLeft, FaSave, FaInfoCircle, FaLock, FaGlobe, FaClock, FaTrophy, FaCog, FaUserTag, FaChartLine } from 'react-icons/fa';
import { Toaster, toast } from 'react-hot-toast';


export default function CreateTournament() {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startTime: '',
    durationInSeconds: 7200,
    visibility: 'PUBLIC',
    password: '',
    tournamentType: 'FREE_STYLE',
    penaltyFactor: 300,
    minRatingReq: 0,
    maxRatingReq: 0,
    rated: false,
    teamStyle: false,
  });

  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    const newValue = type === 'checkbox' ? checked : value;

    setFormData((prev) => ({
      ...prev,
      [name]: newValue,
    }));

    // Clear the error when the field is edited
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  // Handle duration change in hours and minutes
  const handleDurationChange = (hours, minutes) => {
    // Remove leading zeros from strings (if any)
    const sanitizedHours = hours.toString().replace(/^0+(?!$)/, "");
    const sanitizedMinutes = minutes.toString().replace(/^0+(?!$)/, "");
    // Parse the numbers safely
    const hoursInt = parseInt(sanitizedHours, 10);
    const minutesInt = parseInt(sanitizedMinutes, 10);
    const validHours = isNaN(hoursInt) ? 0 : hoursInt;
    const validMinutes = isNaN(minutesInt) ? 0 : minutesInt;

    setFormData(prev => ({
      ...prev,
      durationInSeconds: (validHours * 3600) + (validMinutes * 60)
    }));
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Tournament name is required';
    }

    if (!formData.startTime) {
      newErrors.startTime = 'Start time is required';
    }

    if (formData.visibility === 'PRIVATE' && !formData.password.trim()) {
      newErrors.password = 'Password is required for private tournaments';
    }

    if (formData.durationInSeconds <=0) {
      newErrors.durationInSeconds = 'Duration must be at least 30 minutes';
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const formErrors = validateForm();
    if (Object.keys(formErrors).length > 0) {
      setErrors(formErrors);
      return;
    }

    setIsSubmitting(true);
    const startTimeMs = new Date(formData.startTime).getTime();

    const payload = {
      ...formData,
      startTime: startTimeMs
    };

    try {
      const toastId = toast.loading('Creating tournament...');

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
        toast.success('Tournament created successfully!', { id: toastId });
        router.push('/tournaments');
      } else {
        toast.error(result.message || 'Failed to create tournament', { id: toastId });
        setErrors({ submit: result.message || 'Failed to create tournament' });
      }
    } catch (error) {
      console.error('Error creating tournament:', error);
      toast.error('An error occurred. Please try again.');
      setErrors({ submit: 'Network error. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Calculate duration hours and minutes for display
  const durationHours = Math.floor(formData.durationInSeconds / 3600);
  const durationMinutes = Math.floor((formData.durationInSeconds % 3600) / 60);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100">
      <Toaster position="top-right" />

      <header className="bg-white dark:bg-gray-800 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <button
            onClick={() => router.back()}
            className="flex items-center text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors"
          >
            <FaChevronLeft className="mr-2" />
            <span>Back</span>
          </button>

          <h1 className="text-2xl font-bold flex items-center">
            <FaTrophy className="mr-3 text-indigo-500" />
            Create New Tournament
          </h1>

          <div className="w-20"></div> {/* Spacer for centering */}
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {errors.submit && (
          <div className="mb-8 bg-red-50 border-l-4 border-red-500 p-4 dark:bg-red-900/20 dark:border-red-700 rounded">
            <p className="text-red-700 dark:text-red-400">{errors.submit}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column: Basic Information */}
          <div className="lg:col-span-2 space-y-8">
            <section className="bg-white dark:bg-gray-800 shadow-sm rounded-lg p-6">
              <h2 className="text-xl font-medium mb-6 flex items-center">
                <FaInfoCircle className="mr-2 text-indigo-500" />
                Basic Information
              </h2>

              <div className="space-y-6">
                <div>
                  <label htmlFor="name" className="block text-sm font-medium mb-1">
                    Tournament Name*
                  </label>
                  <input
                    id="name"
                    name="name"
                    type="text"
                    value={formData.name}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border ${errors.name ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'} rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors`}
                    placeholder="Enter a unique tournament name"
                  />
                  {errors.name && <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.name}</p>}
                </div>

                <div>
                  <label htmlFor="description" className="block text-sm font-medium mb-1">
                    Description
                  </label>
                  <textarea
                    id="description"
                    name="description"
                    rows="5"
                    value={formData.description}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors"
                    placeholder="Describe your tournament rules, prizes, and any additional information participants should know"
                  />
                </div>
              </div>
            </section>

            <section className="bg-white dark:bg-gray-800 shadow-sm rounded-lg p-6">
              <h2 className="text-xl font-medium mb-6 flex items-center">
                <FaClock className="mr-2 text-indigo-500" />
                Schedule
              </h2>

              <div className="space-y-6">
                <div>
                  <label htmlFor="startTime" className="block text-sm font-medium mb-1">
                    Start Time*
                  </label>
                  <input
                    id="startTime"
                    name="startTime"
                    type="datetime-local"
                    value={formData.startTime}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border ${errors.startTime ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'} rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors`}
                  />
                  {errors.startTime && <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.startTime}</p>}
                  <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">Your local timezone will be used</p>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">
                    Duration*
                  </label>
                  <div className="flex space-x-4">
                    <div className="w-1/2">
                      <label htmlFor="durationHours" className="block text-xs text-gray-500 mb-1">Hours</label>
                      <input
                        id="durationHours"
                        type="number"
                        min="0"
                        max="24"
                        value={durationHours}
                        onChange={(e) => handleDurationChange(e.target.value, durationMinutes)}
                        className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors"
                      />
                    </div>
                    <div className="w-1/2">
                      <label htmlFor="durationMinutes" className="block text-xs text-gray-500 mb-1">Minutes</label>
                      <input
                        id="durationMinutes"
                        type="number"
                        min="0"
                        max="59"
                        step="1"
                        value={durationMinutes}
                        onChange={(e) => handleDurationChange(durationHours, e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors"
                      />
                    </div>
                  </div>
                  <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                    Total time: {formData.durationInSeconds} seconds
                  </p>
                </div>
              </div>
            </section>

            <section className="bg-white dark:bg-gray-800 shadow-sm rounded-lg p-6">
              <h2 className="text-xl font-medium mb-6 flex items-center">
                <FaCog className="mr-2 text-indigo-500" />
                Tournament Settings
              </h2>

              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Tournament Type
                    </label>
                    <div className="space-y-3">
                      <div className="flex items-center p-3 border border-gray-200 dark:border-gray-700 rounded-md cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
                        <input
                          id="type-freestyle"
                          name="tournamentType"
                          type="radio"
                          value="FREE_STYLE"
                          checked={formData.tournamentType === 'FREE_STYLE'}
                          onChange={handleChange}
                          className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                        />
                        <label htmlFor="type-freestyle" className="ml-3 flex-1 text-sm">
                          <span className="font-medium block">FreeStyle</span>
                          <span className="text-gray-500 dark:text-gray-400 text-xs">Solve problems in any order</span>
                        </label>
                      </div>

                      <div className="flex items-center p-3 border border-gray-200 dark:border-gray-700 rounded-md cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
                        <input
                          id="type-classic"
                          name="tournamentType"
                          type="radio"
                          value="CLASSIC"
                          checked={formData.tournamentType === 'CLASSIC'}
                          onChange={handleChange}
                          className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                        />
                        <label htmlFor="type-classic" className="ml-3 flex-1 text-sm">
                          <span className="font-medium block">Classic</span>
                          <span className="text-gray-500 dark:text-gray-400 text-xs">Problems must be solved in order</span>
                        </label>
                      </div>
                    </div>
                  </div>

                  <div>
                    <label htmlFor="penaltyFactor" className="block text-sm font-medium mb-1">
                      Penalty Per Wrong Submission (seconds)
                    </label>
                    <input
                      id="penaltyFactor"
                      name="penaltyFactor"
                      type="number"
                      min="0"
                      max="1800"
                      value={formData.penaltyFactor}
                      onChange={handleChange}
                      className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors"
                    />
                    <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                      Recommended: 300 seconds (5 minutes)
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Additional Options
                    </label>
                    <div className="space-y-3">
                      <div className="flex items-center p-3 border border-gray-200 dark:border-gray-700 rounded-md hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
                        <input
                          id="rated"
                          name="rated"
                          type="checkbox"
                          checked={formData.rated}
                          onChange={handleChange}
                          className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                        />
                        <label htmlFor="rated" className="ml-3 flex-1 text-sm">
                          <span className="font-medium block">Rated Tournament</span>
                          <span className="text-gray-500 dark:text-gray-400 text-xs">Results will affect participant ratings</span>
                        </label>
                      </div>

                      <div className="flex items-center p-3 border border-gray-200 dark:border-gray-700 rounded-md hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
                        <input
                          id="teamStyle"
                          name="teamStyle"
                          type="checkbox"
                          checked={formData.teamStyle}
                          onChange={handleChange}
                          className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                        />
                        <label htmlFor="teamStyle" className="ml-3 flex-1 text-sm">
                          <span className="font-medium block">Team Competition</span>
                          <span className="text-gray-500 dark:text-gray-400 text-xs">Allow participants to compete in teams</span>
                        </label>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </div>

          {/* Right Column: Access Settings */}
          <div className="space-y-8">
            <section className="bg-white dark:bg-gray-800 shadow-sm rounded-lg p-6">
              <h2 className="text-xl font-medium mb-6 flex items-center">
                <FaGlobe className="mr-2 text-indigo-500" />
                Access Control
              </h2>

              <div className="space-y-6">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Visibility
                  </label>
                  <div className="space-y-3">
                    <div className="flex items-center p-3 border border-gray-200 dark:border-gray-700 rounded-md cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
                      <input
                        id="visibility-public"
                        name="visibility"
                        type="radio"
                        value="PUBLIC"
                        checked={formData.visibility === 'PUBLIC'}
                        onChange={handleChange}
                        className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                      />
                      <label htmlFor="visibility-public" className="ml-3 flex-1 text-sm">
                        <span className="font-medium block">Public</span>
                        <span className="text-gray-500 dark:text-gray-400 text-xs">Visible to everyone in tournament listings</span>
                      </label>
                      <FaGlobe className="text-gray-400" />
                    </div>

                    <div className="flex items-center p-3 border border-gray-200 dark:border-gray-700 rounded-md cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
                      <input
                        id="visibility-private"
                        name="visibility"
                        type="radio"
                        value="PRIVATE"
                        checked={formData.visibility === 'PRIVATE'}
                        onChange={handleChange}
                        className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                      />
                      <label htmlFor="visibility-private" className="ml-3 flex-1 text-sm">
                        <span className="font-medium block">Private</span>
                        <span className="text-gray-500 dark:text-gray-400 text-xs">Requires password to join</span>
                      </label>
                      <FaLock className="text-gray-400" />
                    </div>
                  </div>
                </div>

                {formData.visibility === 'PRIVATE' && (
                  <div className="transition-all duration-200 ease-in-out">
                    <label htmlFor="password" className="block text-sm font-medium mb-1">
                      Password*
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <FaLock className="text-gray-400" />
                      </div>
                      <input
                        id="password"
                        name="password"
                        type="password"
                        value={formData.password}
                        onChange={handleChange}
                        className={`block w-full pl-10 pr-3 py-2 border ${errors.password ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'} rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors`}
                        placeholder="Enter password"
                      />
                    </div>
                    {errors.password && <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.password}</p>}
                  </div>
                )}
              </div>
            </section>

            <section className="bg-white dark:bg-gray-800 shadow-sm rounded-lg p-6">
              <h2 className="text-xl font-medium mb-6 flex items-center">
                <FaUserTag className="mr-2 text-indigo-500" />
                Participant Requirements
              </h2>

              <div className="space-y-6">
                <div>
                  <label htmlFor="minRatingReq" className="block text-sm font-medium mb-1">
                    Minimum Rating (0 = no minimum)
                  </label>
                  <input
                    id="minRatingReq"
                    name="minRatingReq"
                    type="number"
                    min="0"
                    value={formData.minRatingReq}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors"
                    placeholder="0"
                  />
                </div>

                <div>
                  <label htmlFor="maxRatingReq" className="block text-sm font-medium mb-1">
                    Maximum Rating (0 = no maximum)
                  </label>
                  <input
                    id="maxRatingReq"
                    name="maxRatingReq"
                    type="number"
                    min="0"
                    value={formData.maxRatingReq}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 transition-colors"
                    placeholder="0"
                  />
                </div>
              </div>
            </section>

            <section className="bg-white dark:bg-gray-800 shadow-sm rounded-lg p-6">
              <h2 className="text-xl font-medium mb-6 flex items-center">
                <FaChartLine className="mr-2 text-indigo-500" />
                Create Tournament
              </h2>

              <div className="text-sm text-gray-500 dark:text-gray-400 mb-6">
                Review your tournament details before submitting. Once created, some settings cannot be changed.
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Creating Tournament...
                  </>
                ) : (
                  <>
                    <FaSave className="mr-2" />
                    Create Tournament
                  </>
                )}
              </button>
            </section>
          </div>
        </form>
      </main>
    </div>
  );
}