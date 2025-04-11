'use client';
import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import axios from 'axios';
import { getCookie } from 'cookies-next';
import Link from 'next/link';
import { FaSpinner, FaTrophy, FaCalendarAlt, FaUsers, FaClock, FaArrowLeft, FaChevronLeft, FaChevronRight } from 'react-icons/fa';

export default function UserTournaments() {
  const { userName } = useParams();
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortBy, setSortBy] = useState("startDate");
  const [sortDirection, setSortDirection] = useState("desc");

  const fetchTournaments = async (page = currentPage) => {
    try {
      setLoading(true);
      const token = getCookie('token');

      if (!token) {
        setError('Authentication required');
        return;
      }

      const response = await axios.get(
        `${process.env.NEXT_PUBLIC_API_URL}/api/user/${userName}/tournaments`, 
        {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            page,
            size: pageSize,
            sortBy,
            direction: sortDirection
          }
        }
      );

      // Assuming the API returns a Spring Page object
      const pageData = response.data;
      setTournaments(pageData.content);
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.totalElements);
      setCurrentPage(pageData.number);
    } catch (error) {
      console.error('Error fetching tournaments:', error);
      // Handle 401 specifically
    if (error.response?.status === 401) {
      setError('Your session has expired. Please log in again.');
      // Redirect to login page or refresh token
    } else {
      setError(error.response?.data?.message || 'Failed to load tournaments');
    }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTournaments(0); // Start at page 0 when component mounts
  }, [userName, pageSize, sortBy, sortDirection]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchTournaments(newPage);
    }
  };

  // Loading and error states remain the same...
  if (loading && currentPage === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <FaSpinner className="animate-spin h-12 w-12 mx-auto text-indigo-600 dark:text-indigo-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Loading tournaments</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">Please wait while we fetch the tournament data</p>
        </div>
      </div>
    );
  }

  if (error) {
    // Error state remains the same...
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center max-w-md mx-auto p-6 bg-white dark:bg-gray-800 rounded-lg shadow-md">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/20 text-red-500 mb-4">
            <svg className="h-8 w-8" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Error Loading Tournaments</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{error}</p>
          <Link href={`/dashboard/${userName}`} 
                className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Back to Profile
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 pb-10">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <Link href={`/dashboard/${userName}`} className="text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 mr-4">
                <FaArrowLeft className="h-5 w-5" />
              </Link>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
                <FaTrophy className="mr-3 h-6 w-6 text-indigo-600 dark:text-indigo-400" />
                {userName}'s Tournaments
              </h1>
            </div>
            
            {/* Add sorting options */}
            <div className="flex items-center">
              <select 
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="mr-2 text-sm rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
              >
                <option value="startDate">Start Date</option>
                <option value="name">Tournament Name</option>
                <option value="endDate">End Date</option>
              </select>
              <select 
                value={sortDirection}
                onChange={(e) => setSortDirection(e.target.value)}
                className="text-sm rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
              >
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {tournaments.length > 0 ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {tournaments.map((tournament) => (
                <Link href={`/tournaments/${tournament._id}`} key={tournament._id}>
                  <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden hover:shadow-lg transition-shadow duration-300">
                    <div className="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
                      <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 truncate">{tournament.name}</h3>
                      <div className="mt-1 flex items-center">
                        <span className={`px-2 py-1 text-xs font-medium rounded ${
                          !tournament.status
                            ? 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300'
                            : tournament.status === 'upcoming' 
                              ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300' 
                              : tournament.status === 'ongoing' 
                                ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300'
                                : 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300'
                        }`}>
                          {tournament.status 
                            ? tournament.status.charAt(0).toUpperCase() + tournament.status.slice(1)
                            : 'Unknown'}
                        </span>
                      </div>
                    </div>
                    <div className="px-6 py-4 space-y-3">
                      <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                        <FaCalendarAlt className="mr-2 h-4 w-4" />
                        <span>
                          {new Date(tournament.startDate).toLocaleDateString()} - {new Date(tournament.endDate).toLocaleDateString()}
                        </span>
                      </div>
                      <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                        <FaUsers className="mr-2 h-4 w-4" />
                        <span>{tournament.participantCount || 0} participants</span>
                      </div>
                      <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                        <FaClock className="mr-2 h-4 w-4" />
                        <span>Duration: {tournament.duration || 'Not specified'}</span>
                      </div>
                    </div>
                    {tournament.userRole && (
                      <div className="px-6 py-3 bg-gray-50 dark:bg-gray-750">
                        <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                          Your role: {tournament.userRole}
                        </span>
                      </div>
                    )}
                  </div>
                </Link>
              ))}
            </div>

            {/* Pagination controls */}
            <div className="mt-8 flex items-center justify-between">
              <div className="text-sm text-gray-700 dark:text-gray-300">
                Showing <span className="font-medium">{tournaments.length}</span> of <span className="font-medium">{totalElements}</span> tournaments
              </div>
              <div className="flex items-center space-x-2">
                <button 
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                  className={`inline-flex items-center px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium 
                          ${currentPage === 0 
                            ? 'text-gray-400 dark:text-gray-500 cursor-not-allowed' 
                            : 'text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700'}`}
                >
                  <FaChevronLeft className="h-4 w-4 mr-1.5" />
                  Previous
                </button>
                <div className="text-sm text-gray-700 dark:text-gray-300">
                  Page <span className="font-medium">{currentPage + 1}</span> of <span className="font-medium">{totalPages}</span>
                </div>
                <button 
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage >= totalPages - 1}
                  className={`inline-flex items-center px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium 
                          ${currentPage >= totalPages - 1 
                            ? 'text-gray-400 dark:text-gray-500 cursor-not-allowed' 
                            : 'text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700'}`}
                >
                  Next
                  <FaChevronRight className="h-4 w-4 ml-1.5" />
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="bg-white dark:bg-gray-800 shadow rounded-lg p-8 text-center">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 mb-4">
              <FaTrophy className="h-8 w-8" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">No tournaments found</h3>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              {userName === getCookie('userName') 
                ? "You haven't participated in any tournaments yet." 
                : `${userName} hasn't participated in any tournaments yet.`}
            </p>
            <div className="mt-6">
              <Link href="/tournaments" 
                    className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                Browse Tournaments
              </Link>
            </div>
          </div>
        )}
        
        {/* Loading indicator for pagination */}
        {loading && currentPage > 0 && (
          <div className="mt-4 text-center">
            <FaSpinner className="animate-spin h-5 w-5 mx-auto text-indigo-600 dark:text-indigo-400" />
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">Loading more tournaments...</p>
          </div>
        )}
      </div>
    </div>
  );
}