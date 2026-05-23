'use client';
import { useState, useEffect, useCallback } from 'react';
import { FaChartLine, FaCalendarAlt, FaChessBoard, FaChess } from 'react-icons/fa';
import api from '@/services/api';

const RecentActivity = ({ userName }) => {
  const [activityData, setActivityData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchActivity = useCallback(async () => {
    if (!userName) return;
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/api/user/getRecentActivity/${userName}`);
      setActivityData(response.data.activity);
    } catch (err) {
      setError(err.message || 'Failed to load activity data');
    } finally {
      setLoading(false);
    }
  }, [userName]);

  useEffect(() => {
    fetchActivity();
  }, [fetchActivity]);

  // Sort dates in descending order (most recent first)
  const sortedDates = activityData 
    ? Object.keys(activityData).sort((a, b) => new Date(b) - new Date(a))
    : [];

  const getTournamentIcon = (type) => {
    switch (type.toLowerCase()) {
      case 'classic':
        return <FaChessBoard className="h-4 w-4 text-blue-500" />;
      case 'freestyle':
        return <FaChess className="h-4 w-4 text-purple-500" />;
      default:
        return <FaChartLine className="h-4 w-4 text-indigo-500" />;
    }
  };

  const getActivityColor = (type) => {
    switch (type.toLowerCase()) {
      case 'classic':
        return 'bg-blue-100 dark:bg-blue-900/20';
      case 'freestyle':
        return 'bg-purple-100 dark:bg-purple-900/20';
      default:
        return 'bg-indigo-100 dark:bg-indigo-900/20';
    }
  };

  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
        <div className="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Recent Activity</h3>
        </div>
        <div className="px-6 py-8 text-center">
          <div className="inline-flex items-center">
            <svg className="animate-spin h-5 w-5 mr-2 text-indigo-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span className="text-sm text-gray-500 dark:text-gray-400">Loading activity data...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
        <div className="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Recent Activity</h3>
        </div>
        <div className="px-6 py-8 text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-3">
            Couldn't load activity. {error}
          </p>
          <button
            onClick={fetchActivity}
            className="px-3 py-1 text-xs bg-indigo-600 text-white rounded hover:bg-indigo-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
      <div className="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
        <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">Recent Activity</h3>
      </div>
      <div className="divide-y divide-gray-200 dark:divide-gray-700">
        {sortedDates.length > 0 ? (
          sortedDates.map((date) => (
            <div key={date} className="px-6 py-4">
              <div className="flex items-center mb-2">
                <FaCalendarAlt className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                <span className="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">{formatDate(date)}</span>
              </div>
              <div className="ml-6 space-y-3">
                {Object.entries(activityData[date]).map(([tournamentType, count]) => (
                  <div key={`${date}-${tournamentType}`} className="flex items-center">
                    <div className={`flex-shrink-0 h-8 w-8 rounded-full ${getActivityColor(tournamentType)} flex items-center justify-center`}>
                      {getTournamentIcon(tournamentType)}
                    </div>
                    <div className="ml-4 flex-1">
                      <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        Participated in {count} {tournamentType} tournament{count > 1 ? 's' : ''}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))
        ) : (
          <div className="px-6 py-8 text-center">
            <p className="text-sm text-gray-500 dark:text-gray-400">No recent activity to display</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RecentActivity;