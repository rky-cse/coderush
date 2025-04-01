'use client';

import React, { useState, useEffect, useRef } from 'react';

const DualRangeSlider = ({
  axis,
  min,
  max,
  step,
  defaultMinValue,
  defaultMaxValue,
  onChange,
  label
}) => {
  const [minValue, setMinValue] = useState(defaultMinValue);
  const [maxValue, setMaxValue] = useState(defaultMaxValue);
  const [isDraggingMin, setIsDraggingMin] = useState(false);
  const [isDraggingMax, setIsDraggingMax] = useState(false);
  const sliderMinRef = useRef(null);
  const sliderMaxRef = useRef(null);
  const trackRef = useRef(null);

  // Update when props change
  useEffect(() => {
    setMinValue(defaultMinValue);
  }, [defaultMinValue]);

  useEffect(() => {
    setMaxValue(defaultMaxValue);
  }, [defaultMaxValue]);

  // Notify parent component when values change
  useEffect(() => {
    if (onChange) {
      onChange(minValue, maxValue);
    }
  }, [minValue, maxValue, onChange]);

  const handleMinChange = (e) => {
    const newMin = Number(e.target.value);
    if (newMin < maxValue) {
      setMinValue(newMin);
    }
  };

  const handleMaxChange = (e) => {
    const newMax = Number(e.target.value);
    if (newMax > minValue) {
      setMaxValue(newMax);
    }
  };

  // Calculate positions for visual elements
  const minPosition = ((minValue - min) / (max - min)) * 100;
  const maxPosition = ((maxValue - min) / (max - min)) * 100;

  // Handle direct track click
  const handleTrackClick = (e) => {
    // Skip if we're already dragging a handle
    if (isDraggingMin || isDraggingMax) return;

    if (!trackRef.current) return;

    const rect = trackRef.current.getBoundingClientRect();
    const clickPosition = e.clientX - rect.left;
    const clickPercent = (clickPosition / rect.width) * 100;

    // Calculate where the click is relative to min and max handles
    const distToMin = Math.abs(clickPercent - minPosition);
    const distToMax = Math.abs(clickPercent - maxPosition);

    // Move the closest handle to the click position
    if (distToMin <= distToMax) {
      const newMinValue = min + ((max - min) * clickPercent / 100);
      if (newMinValue < maxValue) {
        setMinValue(Math.round(newMinValue / step) * step);
      }
    } else {
      const newMaxValue = min + ((max - min) * clickPercent / 100);
      if (newMaxValue > minValue) {
        setMaxValue(Math.round(newMaxValue / step) * step);
      }
    }
  };

  return (
    <div className="w-full mb-4">
      <div className="flex justify-between items-center mb-1.5">
        <label className="block text-xs text-gray-700 dark:text-gray-300">
          {label}
        </label>
        <div className="flex space-x-2 items-center text-xs">
          <input
            type="number"
            value={minValue}
            onChange={handleMinChange}
            className="w-16 px-1.5 py-0.5 text-xs border border-gray-300 dark:border-gray-600 rounded focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 dark:text-gray-300"
            min={min}
            max={maxValue - step}
            step={step}
          />
          <span className="text-gray-500 dark:text-gray-400">to</span>
          <input
            type="number"
            value={maxValue}
            onChange={handleMaxChange}
            className="w-16 px-1.5 py-0.5 text-xs border border-gray-300 dark:border-gray-600 rounded focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-gray-700 dark:text-gray-300"
            min={minValue + step}
            max={max}
            step={step}
          />
        </div>
      </div>

      <div
        className="relative h-7 flex items-center px-1"
        ref={trackRef}
        onClick={handleTrackClick}
      >
        {/* Background track */}
        <div className="absolute w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full" />

        {/* Filled track */}
        <div
          className="absolute h-1.5 bg-indigo-500 rounded-full"
          style={{
            left: `${minPosition}%`,
            width: `${maxPosition - minPosition}%`
          }}
        />

        {/* Min handle - Split into two sections for better control */}
        <div className="absolute h-7 flex items-center" style={{ left: `calc(${minPosition}% - 8px)`, width: '16px' }}>
          <input
            ref={sliderMinRef}
            type="range"
            className="absolute w-full h-7 opacity-0 cursor-pointer"
            min={min}
            max={max}
            step={step}
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

        {/* Max handle - Split into two sections for better control */}
        <div className="absolute h-7 flex items-center" style={{ left: `calc(${maxPosition}% - 8px)`, width: '16px' }}>
          <input
            ref={sliderMaxRef}
            type="range"
            className="absolute w-full h-7 opacity-0 cursor-pointer"
            min={min}
            max={max}
            step={step}
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

        {/* Tick marks for key values */}


        {/* Tick marks for key values */}
        {axis === "y" && (
          <div className="absolute w-full top-1/2 -translate-y-1/2 pointer-events-none">
            {RATING_LEVELS?.map((level, index) => (
              <div
                key={index}
                className="absolute h-3 w-0.5 bg-gray-300 dark:bg-gray-600"
                style={{
                  left: `${((level.min - min) / (max - min)) * 100}%`,
                  display: level.min >= min && level.min <= max ? 'block' : 'none'
                }}
              />
            ))}
          </div>
        )}

        {/* Day markers for X-axis */}
        {axis === "x" && (
          <div className="absolute w-full top-1/2 -translate-y-1/2 pointer-events-none">
            {/* Generate markers for 10%, 25%, 50%, 75%, 90% */}
            {[10, 25, 50, 75, 90].map((percent) => (
              <div
                key={percent}
                className="absolute h-2 w-0.5 bg-gray-300 dark:bg-gray-600"
                style={{
                  left: `${percent}%`,
                }}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

// Rating levels for tick marks on Y axis
const RATING_LEVELS = [
  { name: 'Newbie', min: 0 },
  { name: 'Pupil', min: 1200 },
  { name: 'Specialist', min: 1400 },
  { name: 'Expert', min: 1600 },
  { name: 'Candidate Master', min: 1900 },
  { name: 'Master', min: 2100 },
  { name: 'International Master', min: 2300 },
  { name: 'Grandmaster', min: 2400 },
  { name: 'International Grandmaster', min: 2600 },
  { name: 'Legendary Grandmaster', min: 3000 }
];

export default DualRangeSlider;