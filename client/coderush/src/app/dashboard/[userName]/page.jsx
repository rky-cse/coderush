'use client';
import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import Image from 'next/image';
import RatingGraph from '@/components/RatingGraph';
import RecentActivity from '@/components/RecentActivity';
import { FaUser, FaCalendarAlt, FaCode, FaTrophy, FaMedal, FaChartLine, FaSpinner } from 'react-icons/fa';
import Link from 'next/link';

// Helper function to get rating color based on rating value
const getRatingColor = (rating) => {
  if (rating >= 3000) return 'text-red-800';
  if (rating >= 2600) return 'text-red-600';
  if (rating >= 2400) return 'text-red-600';
  if (rating >= 2300) return 'text-orange-500';
  if (rating >= 2100) return 'text-orange-500';
  if (rating >= 1900) return 'text-purple-600';
  if (rating >= 1600) return 'text-blue-600';
  if (rating >= 1400) return 'text-teal-500';
  if (rating >= 1200) return 'text-green-600';
  return 'text-gray-500';
};

// Helper function to get rating level based on rating value
const getRatingLevel = (rating) => {
  if (rating >= 3000) return 'Legendary Grandmaster';
  if (rating >= 2600) return 'International Grandmaster';
  if (rating >= 2400) return 'Grandmaster';
  if (rating >= 2300) return 'International Master';
  if (rating >= 2100) return 'Master';
  if (rating >= 1900) return 'Candidate Master';
  if (rating >= 1600) return 'Expert';
  if (rating >= 1400) return 'Specialist';
  if (rating >= 1200) return 'Pupil';
  return 'Newbie';
};

export default function Profile() {
  const { userName } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userName) return;

    const fetchUser = async () => {
      try {
        setLoading(true);
        const token = getCookie('token');

        if (!token) {
          setError('Authentication required');
          return;
        }

        const response = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/user/${userName}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        setUser(response.data);
      } catch (error) {
        console.error('Error fetching user:', error);
        setError(error.response?.data?.message || 'Failed to load user profile');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [userName]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <FaSpinner className="animate-spin h-12 w-12 mx-auto text-indigo-600 dark:text-indigo-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Loading profile</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">Please wait while we fetch the user data</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center max-w-md mx-auto p-6 bg-white dark:bg-gray-800 rounded-lg shadow-md">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/20 text-red-500 mb-4">
            <svg className="h-8 w-8" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Error Loading Profile</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{error}</p>
          <Link href="/dashboard" 
                className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Back to Dashboard
          </Link>
        </div>
      </div>
    );
  }

  if (!user) return null;

  const ratingColor = getRatingColor(user.rating);
  const ratingLevel = getRatingLevel(user.rating);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 pb-10">
      {/* Header - Profile Overview */}
      <div className="bg-white dark:bg-gray-800 shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex flex-col md:flex-row items-start md:items-center space-y-4 md:space-y-0 md:space-x-6">
            {/* Avatar */}
            <div className="relative flex-shrink-0">
              <div className="h-24 w-24 rounded-full overflow-hidden bg-gray-200 dark:bg-gray-700 border-4 border-white dark:border-gray-700 shadow-lg">
                {user.profilePicture ? (
                  <Image
                    src={user.profilePicture}
                    alt={`${user.userName}'s profile`}
                    width={96}
                    height={96}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="h-full w-full flex items-center justify-center bg-indigo-100 dark:bg-indigo-900/20 text-indigo-500 dark:text-indigo-400">
                    <FaUser className="h-12 w-12" />
                  </div>
                )}
              </div>
              <div className={`absolute -bottom-1 -right-1 h-8 w-8 rounded-full flex items-center justify-center ${ratingColor} bg-white dark:bg-gray-800 border-2 border-white dark:border-gray-800 shadow`}>
                <FaTrophy className="h-4 w-4" />
              </div>
            </div>
            
            {/* User info */}
            <div className="flex-1">
              <div className="flex items-center space-x-2">
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{user.firstName} {user.lastName}</h1>
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300">
                  @{user.userName}
                </span>
              </div>
              
              <div className="mt-1 flex flex-col sm:flex-row sm:flex-wrap space-y-1 sm:space-y-0 sm:space-x-4">
                {/* <div className="text-sm text-gray-500 dark:text-gray-400 flex items-center">
                  <FaCalendarAlt className="mr-1.5 h-4 w-4 text-gray-400 dark:text-gray-500" />
                  Joined {new Date(user.createdAt).toLocaleDateString()}
                </div> */}
                {/* <div className="text-sm text-gray-500 dark:text-gray-400 flex items-center">
                  <FaCode className="mr-1.5 h-4 w-4 text-gray-400 dark:text-gray-500" />
                  {user.solvedProblems || 0} problems solved
                </div>
                <div className="text-sm text-gray-500 dark:text-gray-400 flex items-center">
                  <FaMedal className="mr-1.5 h-4 w-4 text-gray-400 dark:text-gray-500" />
                  {user.contestsParticipated || 0} contests
                </div> */}
              </div>
              
              <div className="mt-3 flex items-center">
                <div className={`mr-2 font-semibold text-lg ${ratingColor}`}>
                  {user.rating || 0}
                </div>
                <div className={`px-2 py-0.5 rounded text-xs font-medium ${ratingColor} bg-opacity-10 dark:bg-opacity-20`}>
                  {ratingLevel}
                </div>
              </div>
            </div>
            
            {/* Action buttons */}
            <div className="flex space-x-3 self-start">
              {userName === getCookie('userName') && (
                <button className="inline-flex items-center px-3 py-1.5 border border-gray-300 dark:border-gray-600 shadow-sm text-sm font-medium rounded text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-650 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:focus:ring-offset-gray-900">
                  Edit Profile
                </button>
              )}
              <Link 
              href={`/dashboard/${userName}/tournaments`}
              className="inline-flex items-center px-3 py-1.5 border border-gray-300 dark:border-gray-600 shadow-sm text-sm font-medium rounded text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-650 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:focus:ring-offset-gray-900">
              <FaTrophy className="mr-1.5 h-4 w-4" />
              Tournaments
            </Link>
            </div>
          </div>
        </div>
      </div>
      
      {/* Main content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left column - Stats & Info */}
          <div className="lg:col-span-1 space-y-6">
            
            {/* User details */}
            <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
              <div className="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Profile Details</h3>
              </div>
              <div className="px-6 py-5 space-y-4">
                {user.email && (
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Email</h4>
                    <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">{user.email}</p>
                  </div>
                )}
                
                {user.institution && (
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Institution</h4>
                    <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">{user.institution}</p>
                  </div>
                )}
                
                {user.location && (
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Location</h4>
                    <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">{user.location}</p>
                  </div>
                )}
                
                {user.socialLinks && (
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Social Links</h4>
                    <div className="mt-2 flex space-x-3">
                      {/* Social media links would go here */}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
          
          {/* Right column - Rating Graph & Activity */}
          <div className="lg:col-span-2 space-y-6">
            {/* Rating Graph */}
            {/* <RatingGraph className="h-full" /> */}
            <RatingGraph/>
            
            {/* Recent Activity */}
            <RecentActivity userName={userName}/>
            {/* <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
              <div className="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Recent Activity</h3>
              </div>
              <div className="divide-y divide-gray-200 dark:divide-gray-700">
                {(user.recentActivity || []).length > 0 ? (
                  user.recentActivity.map((activity, index) => (
                    <div key={index} className="px-6 py-4">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <div className="h-8 w-8 rounded-full bg-indigo-100 dark:bg-indigo-900/20 flex items-center justify-center">
                            <FaChartLine className="h-4 w-4 text-indigo-500" />
                          </div>
                        </div>
                        <div className="ml-4 flex-1">
                          <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{activity.title}</p>
                          <p className="text-sm text-gray-500 dark:text-gray-400">{activity.description}</p>
                        </div>
                        <div className="ml-4 flex-shrink-0">
                          <span className="text-xs text-gray-500 dark:text-gray-400">{activity.timestamp}</span>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="px-6 py-8 text-center">
                    <p className="text-sm text-gray-500 dark:text-gray-400">No recent activity to display</p>
                  </div>
                )}
              </div>
            </div> */}
          </div>
        </div>
      </div>
    </div>
  );
}