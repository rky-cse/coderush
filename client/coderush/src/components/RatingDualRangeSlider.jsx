'use client';

import React, { useState, useEffect, useRef } from 'react';

// Define rating levels exactly like CodeForces
const RATING_LEVELS = [
  { name: 'Newbie', min: 0, max: 1199, color: '#808080' },
  { name: 'Pupil', min: 1200, max: 1399, color: '#008000' },
  { name: 'Specialist', min: 1400, max: 1599, color: '#03A89E' },
  { name: 'Expert', min: 1600, max: 1899, color: '#0000FF' },
  { name: 'Candidate Master', min: 1900, max: 2099, color: '#AA00AA' },
  { name: 'Master', min: 2100, max: 2299, color: '#FF8C00' },
  { name: 'International Master', min: 2300, max: 2399, color: '#FF8C00' },
  { name: 'Grandmaster', min: 2400, max: 2599, color: '#FF0000' },
  { name: 'International Grandmaster', min: 2600, max: 2999, color: '#FF0000' },
  { name: 'Legendary Grandmaster', min: 3000, max: 4000, color: '#800000' }
];

const RatingDualRangeSlider = ({
  minRating = 0,
  maxRating = 4000,
  currentMinRating = 0,
  currentMaxRating = 4000,
  onChange,
  className = ''
}) => {
  const [minValue, setMinValue] = useState(currentMinRating);
  const [maxValue, setMaxValue] = useState(currentMaxRating);
  const [isDraggingMin, setIsDraggingMin] = useState(false);
  const [isDraggingMax, setIsDraggingMax] = useState(false);
  const sliderMinRef = useRef(null);
  const sliderMaxRef = useRef(null);
  const trackRef = useRef(null);

  // Update values when props change
  useEffect(() => {
    setMinValue(currentMinRating);
  }, [currentMinRating]);

  useEffect(() => {
    setMaxValue(currentMaxRating);
  }, [currentMaxRating]);

  const handleMinChange = (e) => {
    const newMin = Number(e.target.value);
    if (newMin < maxValue) {
      setMinValue(newMin);
      if (onChange) {
        onChange(newMin, maxValue);
      }
    }
  };

  const handleMaxChange = (e) => {
    const newMax = Number(e.target.value);
    if (newMax > minValue) {
      setMaxValue(newMax);
      if (onChange) {
        onChange(minValue, newMax);
      }
    }
  };

  // Handle direct track click
  const handleTrackClick = (e) => {
    if (isDraggingMin || isDraggingMax) return;
    
    if (!trackRef.current) return;
    
    const rect = trackRef.current.getBoundingClientRect();
    const clickPosition = e.clientX - rect.left;
    const clickPercent = (clickPosition / rect.width) * 100;
    
    // Map percentage to rating value
    const ratingRange = maxRating - minRating;
    const clickRating = minRating + (ratingRange * clickPercent / 100);
    
    // Calculate where the click is relative to min and max handles
    const distToMin = Math.abs(clickRating - minValue);
    const distToMax = Math.abs(clickRating - maxValue);
    
    // Move the closest handle to the click position
    if (distToMin <= distToMax) {
      if (clickRating < maxValue) {
        const newMin = Math.max(minRating, Math.min(maxRating, Math.round(clickRating / 50) * 50));
        setMinValue(newMin);
        if (onChange) {
          onChange(newMin, maxValue);
        }
      }
    } else {
      if (clickRating > minValue) {
        const newMax = Math.max(minRating, Math.min(maxRating, Math.round(clickRating / 50) * 50));
        setMaxValue(newMax);
        if (onChange) {
          onChange(minValue, newMax);
        }
      }
    }
  };

  // Calculate positions for visual elements (0-100%)
  const minPosition = ((minValue - minRating) / (maxRating - minRating)) * 100;
  const maxPosition = ((maxValue - minRating) / (maxRating - minRating)) * 100;

  return (
    <div className={`w-full mb-4 ${className}`}>
      <div className="flex justify-between items-center mb-1.5">
        <label className="block text-xs text-gray-700 dark:text-gray-300">
          Rating Range
        </label>
        <div className="flex space-x-2 items-center text-xs">
          <input
            type="number"
            value={minValue}
            onChange={handleMinChange}
            className="w-16 px-1.5 py-0.5 text-xs border border-gray-300 dark:border-gray-600 rounded focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 dark:text-gray-300"
            min={minRating}
            max={maxValue - 50}
            step={50}
          />
          <span className="text-gray-500 dark:text-gray-400">to</span>
          <input
            type="number"
            value={maxValue}
            onChange={handleMaxChange}
            className="w-16 px-1.5 py-0.5 text-xs border border-gray-300 dark:border-gray-600 rounded focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 dark:text-gray-300"
            min={minValue + 50}
            max={maxRating}
            step={50}
          />
        </div>
      </div>

      <div 
        className="relative h-7 flex items-center px-1" 
        ref={trackRef}
        onClick={handleTrackClick}
      >
        {/* Background track */}
        <div className="absolute w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full">
          {/* Rating level background segments */}
          {RATING_LEVELS.map((level) => {
            const levelMin = Math.max(minRating, level.min);
            const levelMax = Math.min(maxRating, level.max);
            if (levelMin >= levelMax) return null;
            
            const startPos = ((levelMin - minRating) / (maxRating - minRating)) * 100;
            const endPos = ((levelMax - minRating) / (maxRating - minRating)) * 100;
            const width = endPos - startPos;
            
            return (
              <div 
                key={level.name}
                className="absolute h-1.5"
                style={{
                  left: `${startPos}%`,
                  width: `${width}%`,
                  backgroundColor: level.color,
                  opacity: 0.2
                }}
              />
            );
          })}
        </div>
        
        {/* Filled track */}
        <div
          className="absolute h-1.5 bg-indigo-500 rounded-full"
          style={{
            left: `${minPosition}%`,
            width: `${maxPosition - minPosition}%`
          }}
        />
        
        {/* Min handle */}
        <div className="absolute h-7 flex items-center" style={{ left: `calc(${minPosition}% - 8px)`, width: '16px' }}>
          <input
            ref={sliderMinRef}
            type="range"
            className="absolute w-full h-7 opacity-0 cursor-pointer z-10"
            min={minRating}
            max={maxRating}
            step={50}
            value={minValue}
            onChange={handleMinChange}
            onMouseDown={() => setIsDraggingMin(true)}
            onMouseUp={() => setIsDraggingMin(false)}
            onTouchStart={() => setIsDraggingMin(true)}
            onTouchEnd={() => setIsDraggingMin(false)}
          />
          
          {/* Visual Min handle */}
          <div
            className={`absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-3 h-3 bg-white dark:bg-gray-200 border-2 ${isDraggingMin ? 'border-indigo-700' : 'border-indigo-500'} rounded-full shadow-sm hover:scale-110 transition-transform duration-100 pointer-events-none`}
          />
        </div>
        
        {/* Max handle */}
        <div className="absolute h-7 flex items-center" style={{ left: `calc(${maxPosition}% - 8px)`, width: '16px' }}>
          <input
            ref={sliderMaxRef}
            type="range"
            className="absolute w-full h-7 opacity-0 cursor-pointer z-20"
            min={minRating}
            max={maxRating}
            step={50}
            value={maxValue}
            onChange={handleMaxChange}
            onMouseDown={() => setIsDraggingMax(true)}
            onMouseUp={() => setIsDraggingMax(false)}
            onTouchStart={() => setIsDraggingMax(true)}
            onTouchEnd={() => setIsDraggingMax(false)}
          />
          
          {/* Visual Max handle */}
          <div
            className={`absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-3 h-3 bg-white dark:bg-gray-200 border-2 ${isDraggingMax ? 'border-indigo-700' : 'border-indigo-500'} rounded-full shadow-sm hover:scale-110 transition-transform duration-100 pointer-events-none`}
          />
        </div>
        
        {/* Rating level tick marks */}
        <div className="absolute w-full top-1/2 -translate-y-1/2 pointer-events-none">
          {RATING_LEVELS.map((level, index) => (
            <div 
              key={index}
              className="absolute h-3 w-0.5"
              style={{ 
                left: `${((level.min - minRating) / (maxRating - minRating)) * 100}%`,
                backgroundColor: level.color,
                display: level.min >= minRating && level.min <= maxRating ? 'block' : 'none'
              }}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default RatingDualRangeSlider;