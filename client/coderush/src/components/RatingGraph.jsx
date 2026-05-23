'use client';
import React, { useEffect, useState, useMemo, useRef, useCallback } from 'react';
import api from '@/services/api';
import { Line } from 'react-chartjs-2';
import { format } from 'date-fns';
import TimeDualRangeSlider from './TimeDualRangeSlider';
import RatingDualRangeSlider from './RatingDualRangeSlider';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip as ChartTooltip,
  Legend,
  TimeScale,
  Filler,
  defaults
} from 'chart.js';
import zoomPlugin from 'chartjs-plugin-zoom';
import annotationPlugin from 'chartjs-plugin-annotation';
import 'chartjs-adapter-date-fns';
import { FaUndo, FaTrophy, FaSlidersH } from 'react-icons/fa';

// Register Chart.js components and plugins
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  TimeScale,
  ChartTooltip,
  Legend,
  Filler,
  zoomPlugin,
  annotationPlugin
);

// Set Chart.js defaults for consistent styling
defaults.font.family = "'Inter', 'system-ui', sans-serif";
defaults.color = '#6B7280';

// Define rating levels exactly like CodeForces
const RATING_LEVELS = [
  { name: 'Newbie', min: 0, max: 1199, color: '#808080', textColor: 'text-gray-500', bgColor: 'rgba(128, 128, 128, 0.1)' },
  { name: 'Pupil', min: 1200, max: 1399, color: '#008000', textColor: 'text-green-600', bgColor: 'rgba(0, 128, 0, 0.1)' },
  { name: 'Specialist', min: 1400, max: 1599, color: '#03A89E', textColor: 'text-teal-500', bgColor: 'rgba(3, 168, 158, 0.1)' },
  { name: 'Expert', min: 1600, max: 1899, color: '#0000FF', textColor: 'text-blue-600', bgColor: 'rgba(0, 0, 255, 0.1)' },
  { name: 'Candidate Master', min: 1900, max: 2099, color: '#AA00AA', textColor: 'text-purple-600', bgColor: 'rgba(170, 0, 170, 0.1)' },
  { name: 'Master', min: 2100, max: 2299, color: '#FF8C00', textColor: 'text-orange-500', bgColor: 'rgba(255, 140, 0, 0.1)' },
  { name: 'International Master', min: 2300, max: 2399, color: '#FF8C00', textColor: 'text-orange-500', bgColor: 'rgba(255, 140, 0, 0.1)' },
  { name: 'Grandmaster', min: 2400, max: 2599, color: '#FF0000', textColor: 'text-red-600', bgColor: 'rgba(255, 0, 0, 0.1)' },
  { name: 'International Grandmaster', min: 2600, max: 2999, color: '#FF0000', textColor: 'text-red-600', bgColor: 'rgba(255, 0, 0, 0.1)' },
  { name: 'Legendary Grandmaster', min: 3000, max: Infinity, color: '#800000', textColor: 'text-red-800', bgColor: 'rgba(128, 0, 0, 0.1)' }
];

// Helper to get rating level based on rating value
const getRatingLevel = (rating) => {
  return RATING_LEVELS.find(level => rating >= level.min && rating <= level.max) || RATING_LEVELS[0];
};

const RatingGraph = ({ className }) => {
  const [ratingHistory, setRatingHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeFilter, setTimeFilter] = useState('all');
  const [showRangeSliders, setShowRangeSliders] = useState(false);

  // Chart axis control states
  const [yMin, setYMin] = useState(0);
  const [yMax, setYMax] = useState(3500);
  const [xMin, setXMin] = useState(null);
  const [xMax, setXMax] = useState(null);
  const [earliestTimestamp, setEarliestTimestamp] = useState(null);
  
  const chartContainer = useRef(null);
  const chartRef = useRef(null);

  // Fetch rating history data (extracted as a callback so the Retry button can call it)
  const fetchRatingHistory = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/api/user/getRatingHistory');

      // Process and sort the data by timestamp
      const data = response.data
        .map((dto) => ({
          ...dto,
          newRating: Number(dto.newRating),
          oldRating: Number(dto.oldRating || 0),
          ratingUpdateTimestamp: Number(dto.ratingUpdateTimestamp),
          tournamentName: dto.tournamentName || 'Unknown Tournament',
          ratingChange: Number(dto.newRating) - Number(dto.oldRating || 0)
        }))
        .sort((a, b) => a.ratingUpdateTimestamp - b.ratingUpdateTimestamp);

      setRatingHistory(data);

      // Set initial date range if we have data
      if (data.length > 0) {
        const timestamps = data.map(item => item.ratingUpdateTimestamp);
        const minTimestamp = Math.min(...timestamps);
        const maxTimestamp = Date.now();

        setEarliestTimestamp(minTimestamp);

        setXMin(new Date(minTimestamp));
        setXMax(new Date(maxTimestamp));

        const ratings = data.map(item => item.newRating);
        setYMin(0);
        setYMax(Math.max(...ratings) + 500);
      }
    } catch (err) {
      setError(err.message || 'Failed to load rating history');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRatingHistory();
  }, [fetchRatingHistory]);

  // Filter data based on timeFilter
  const filteredData = useMemo(() => {
    if (timeFilter === 'all' || ratingHistory.length === 0) {
      return ratingHistory;
    }

    const now = new Date();
    let cutoffDate;

    switch (timeFilter) {
      case '1m':
        cutoffDate = new Date(now.setMonth(now.getMonth() - 1));
        break;
      case '3m':
        cutoffDate = new Date(now.setMonth(now.getMonth() - 3));
        break;
      case '6m':
        cutoffDate = new Date(now.setMonth(now.getMonth() - 6));
        break;
      case '1y':
        cutoffDate = new Date(now.setFullYear(now.getFullYear() - 1));
        break;
      default:
        return ratingHistory;
    }

    return ratingHistory.filter(
      (item) => new Date(item.ratingUpdateTimestamp) >= cutoffDate
    );
  }, [ratingHistory, timeFilter]);

  // Calculate key statistics
  const stats = useMemo(() => {
    if (filteredData.length === 0) {
      return { 
        highestRating: 0, 
        lowestRating: 0, 
        currentRating: 0, 
        totalChange: 0,
        currentLevel: RATING_LEVELS[0]
      };
    }

    const ratings = filteredData.map(item => item.newRating);
    const currentRating = ratings[ratings.length - 1];
    const highestRating = Math.max(...ratings);
    const lowestRating = Math.min(...ratings);
    const initialRating = filteredData[0].oldRating || filteredData[0].newRating;
    const totalChange = currentRating - initialRating;
    const currentLevel = getRatingLevel(currentRating);

    return {
      highestRating,
      lowestRating,
      currentRating,
      totalChange,
      currentLevel
    };
  }, [filteredData]);

  // Reset zoom function
  const resetZoom = () => {
    if (chartRef.current) {
      chartRef.current.resetZoom();

      // Reset ranges to initial values
      if (filteredData.length > 0) {
        const timestamps = filteredData.map(item => item.ratingUpdateTimestamp);
        const minTimestamp = Math.min(...timestamps);
        const maxTimestamp = Date.now(); // Current time
        
        const minDate = new Date(minTimestamp);
        const maxDate = new Date(maxTimestamp);
        
        setXMin(minDate);
        setXMax(maxDate);
        
        // Reset Y range based on rating values
        const minRating = 0;
        const maxRating = Math.max(...filteredData.map(item => item.newRating)) + 500;
        setYMin(minRating);
        setYMax(maxRating);
      }
    }
  };

  // Handle time range changes from TimeDualRangeSlider
  const handleTimeRangeChange = (startDate, endDate) => {
    setXMin(startDate);
    setXMax(endDate);
    applyAxisLimits();
  };

  // Handle rating range changes from RatingDualRangeSlider
  const handleRatingRangeChange = (min, max) => {
    setYMin(min);
    setYMax(max);
    applyAxisLimits();
  };

  // Function to apply the axis limits to the chart
  const applyAxisLimits = () => {
    if (!chartRef.current) return;
    const chart = chartRef.current;

    if (xMin && xMax) {
      chart.options.scales.x.min = xMin;
      chart.options.scales.x.max = xMax;
    }
    
    chart.options.scales.y.min = yMin;
    chart.options.scales.y.max = yMax;
    
    chart.update();
  };

  // Update chart when axes change
  useEffect(() => {
    applyAxisLimits();
  }, [xMin, xMax, yMin, yMax]);

  // Prepare the chart data
  const chartData = useMemo(() => {
    if (filteredData.length === 0) {
      return { datasets: [] };
    }

    return {
      labels: filteredData.map(item => new Date(item.ratingUpdateTimestamp)),
      datasets: [
        {
          label: 'Rating',
          data: filteredData.map(item => item.newRating),
          borderColor: 'rgb(79, 70, 229)', // indigo-600
          backgroundColor: 'rgba(79, 70, 229, 0.1)',
          borderWidth: 2,
          pointBackgroundColor: filteredData.map(item => {
            const level = getRatingLevel(item.newRating);
            return level.color;
          }),
          pointBorderColor: filteredData.map(item => {
            const level = getRatingLevel(item.newRating);
            return level.color;
          }),
          pointRadius: filteredData.map(item => {
            const change = Math.abs(item.ratingChange || 0);
            if (change > 100) return 5;
            if (change > 50) return 4;
            if (change > 25) return 3;
            return 2;
          }),
          pointHoverRadius: 6,
          tension: 0.3,
          fill: true
        }
      ]
    };
  }, [filteredData]);

  // Prepare chart options
  const chartOptions = useMemo(() => {
    // Create colored background areas for each rating level
    const ratingLevelBackgrounds = RATING_LEVELS.map(level => ({
      type: 'box',
      xScaleID: 'x',
      yScaleID: 'y',
      yMin: level.min,
      yMax: level.max === Infinity ? stats.highestRating + 100 : level.max,
      backgroundColor: level.bgColor,
      borderColor: level.color,
      borderWidth: 1,
      borderDash: [5, 5],
      drawTime: 'beforeDatasetsDraw',
      label: {
        display: false, // Hide labels for minimalism
        content: level.name,
        position: 'start',
        font: {
          size: 10
        },
        color: level.color
      }
    }));

    return {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false,
      },
      scales: {
        x: {
          type: 'time',
          time: {
            unit: timeFilter === '1m' ? 'day' : timeFilter === '3m' ? 'week' : 'month',
            tooltipFormat: 'MMM d, yyyy',
            displayFormats: {
              day: 'MMM d',
              week: 'MMM d',
              month: 'MMM yyyy'
            }
          },
          grid: {
            display: false
          },
          ticks: {
            maxRotation: 0,
            autoSkip: true,
            autoSkipPadding: 20,
            color: 'rgb(107, 114, 128)',
            font: {
              size: 10
            }
          }
        },
        y: {
          grid: {
            color: 'rgba(229, 231, 235, 0.3)',
            drawBorder: false
          },
          ticks: {
            color: 'rgb(107, 114, 128)',
            font: {
              size: 10
            },
            callback: function(value) {
              // Show only key rating thresholds
              for (const level of RATING_LEVELS) {
                if (value === level.min) {
                  return value;
                }
              }
              return value;
            }
          },
          suggestedMin: Math.max(0, stats.lowestRating - 100),
          suggestedMax: stats.highestRating + 100
        }
      },
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          titleColor: '#1F2937',
          bodyColor: '#4B5563',
          borderColor: 'rgba(229, 231, 235, 0.8)',
          borderWidth: 1,
          padding: 8,
          cornerRadius: 4,
          usePointStyle: true,
          boxWidth: 6,
          boxHeight: 6,
          titleFont: {
            size: 12
          },
          bodyFont: {
            size: 11
          },
          callbacks: {
            title: (tooltipItems) => {
              const date = new Date(tooltipItems[0].parsed.x);
              return format(date, 'MMMM d, yyyy');
            },
            label: (context) => {
              const index = context.dataIndex;
              const item = filteredData[index];
              const change = item.ratingChange;
              const sign = change > 0 ? '+' : change < 0 ? '-' : '';
              const level = getRatingLevel(item.newRating);
              return [
                `Rating: ${item.newRating} (${level.name})`,
                `Change: ${sign}${Math.abs(change)}`,
                `Tournament: ${item.tournamentName || 'Unknown'}`
              ];
            }
          }
        },
        zoom: {
          zoom: {
            wheel: { enabled: true },
            pinch: { enabled: true },
            mode: 'xy',
            onZoomComplete: ({ chart }) => {
              chart.update('none');
              if (chart.scales.x.min && chart.scales.x.max) {
                setXMin(new Date(chart.scales.x.min));
                setXMax(new Date(chart.scales.x.max));
              }
              if (chart.scales.y.min !== undefined && chart.scales.y.max !== undefined) {
                setYMin(Math.round(chart.scales.y.min));
                setYMax(Math.round(chart.scales.y.max));
              }
            }
          },
          pan: { enabled: true, mode: 'xy' },
          limits: { y: { min: 0, max: 4000 } }
        },
        annotation: {
          annotations: {
            ...ratingLevelBackgrounds.reduce((obj, item, index) => {
              obj[`levelBg${index}`] = item;
              return obj;
            }, {})
          }
        }
      }
    };
  }, [filteredData, timeFilter, stats]);

  // Loading state
  if (loading) {
    return (
      <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-md ${className}`}>
        <div className="flex items-center justify-center h-64">
          <div className="animate-pulse flex flex-col items-center">
            <div className="h-6 w-6 rounded-full bg-gray-300 dark:bg-gray-600"></div>
            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded w-24 mt-3"></div>
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-md p-3 ${className}`}>
        <div className="flex flex-col items-center justify-center h-64 text-center">
          <div className="text-red-500 dark:text-red-400 mb-2">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 className="text-sm text-gray-900 dark:text-gray-100 mb-1">Failed to load rating data</h3>
          {error && (
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-2">{error}</p>
          )}
          <button
            onClick={fetchRatingHistory}
            className="mt-2 px-2 py-1 bg-indigo-600 text-white text-xs rounded hover:bg-indigo-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  // Empty data state
  if (ratingHistory.length === 0) {
    return (
      <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-md p-3 ${className}`}>
        <div className="flex flex-col items-center justify-center h-64 text-center">
          <svg className="h-6 w-6 text-blue-500 dark:text-blue-400 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <h3 className="text-sm text-gray-900 dark:text-gray-100 mb-1">No rating history yet</h3>
          <p className="text-xs text-gray-500 dark:text-gray-400 max-w-xs">
            Participate in tournaments to build your rating history
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-md overflow-hidden ${className}`} ref={chartContainer}>
      {/* Header with rating and controls */}
      <div className="flex items-center justify-between px-3 py-1.5 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-750">
        <div className="flex items-center">
          <FaTrophy className="mr-1.5 h-3.5 w-3.5" style={{ color: stats.currentLevel.color }} />
          <span className={`font-bold text-xs ${stats.currentLevel.textColor}`}>
            {stats.currentRating}
          </span>
          <span className="text-xs text-gray-500 ml-1 hidden sm:inline">
            ({stats.currentLevel.name})
          </span>
          <div className="mx-2 h-3 border-l border-gray-300 dark:border-gray-600"></div>
          <div className="flex items-center space-x-1">
            <span className="text-[10px] text-gray-500">∆:</span>
            <span className={`text-xs font-medium ${
              stats.totalChange > 0 
                ? 'text-green-500' 
                : stats.totalChange < 0 
                  ? 'text-red-500' 
                  : 'text-gray-700 dark:text-gray-300'
            }`}>
              {stats.totalChange > 0 ? '+' : ''}{stats.totalChange}
            </span>
          </div>
        </div>
        
        {/* Time filters and controls */}
        <div className="flex items-center space-x-1 text-[10px]">
          {['1m', '3m', '6m', '1y', 'all'].map((filter) => (
            <button
              key={filter}
              className={`px-1.5 py-0.5 rounded ${
                timeFilter === filter
                  ? 'bg-indigo-600 text-white'
                  : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
              }`}
              onClick={() => setTimeFilter(filter)}
            >
              {filter === 'all' ? 'All' : filter}
            </button>
          ))}
          <button
            onClick={resetZoom}
            className="px-1 py-0.5 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
            title="Reset zoom"
          >
            <FaUndo className="h-2.5 w-2.5" />
          </button>
          <button
            onClick={() => setShowRangeSliders(!showRangeSliders)}
            className={`px-1 py-0.5 rounded ${
              showRangeSliders 
                ? 'bg-indigo-600 text-white' 
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
            }`}
            title="Toggle range controls"
          >
            <FaSlidersH className="h-2.5 w-2.5" />
          </button>
        </div>
      </div>
      
      {/* Chart area */}
      <div className="relative">
        <div className="h-96">
          <Line 
            data={chartData} 
            options={chartOptions} 
            ref={chartRef}
          />
        </div>
      </div>
      
      {/* Range sliders - conditionally shown */}
      {showRangeSliders && (
        <div className="px-4 pt-1 pb-2 bg-gray-50 dark:bg-gray-750 border-t border-gray-200 dark:border-gray-700">
          {/* Time range slider - using simplified API */}
          <TimeDualRangeSlider
            minTimestamp={earliestTimestamp}
            maxTimestamp={Date.now()}
            onChange={handleTimeRangeChange}
            className="mb-3"
          />
          
          {/* Rating range slider */}
          <RatingDualRangeSlider
            minRating={0}
            maxRating={4000}
            currentMinRating={yMin}
            currentMaxRating={yMax}
            onChange={handleRatingRangeChange}
          />
        </div>
      )}
      
      {/* Footer with help text */}
      <div className="py-1 text-center border-t border-gray-200 dark:border-gray-700 text-[10px] text-gray-500 dark:text-gray-400">
        Drag to pan • Scroll to zoom • <FaSlidersH className="inline h-2 w-2" /> for range controls
      </div>
    </div>
  );
};

export default RatingGraph;