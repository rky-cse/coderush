'use client';

import React, { useState, useEffect, useRef, useMemo } from 'react';
import { format, parseISO, isValid, addDays, addMonths, startOfMonth } from 'date-fns';
import { FaCalendarAlt, FaChevronLeft, FaChevronRight } from 'react-icons/fa';
import ReactDOM from 'react-dom';

/**
 * Simple Custom DatePicker to avoid dependency issues
 */
const CustomDatePicker = ({ value, onChange, minDate, maxDate, label }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [viewDate, setViewDate] = useState(() => value ? new Date(value) : new Date());
    const [selectedDate, setSelectedDate] = useState(value ? new Date(value) : null);
    const calendarRef = useRef(null);
    const inputRef = useRef(null);
    const [calendarStyles, setCalendarStyles] = useState({});

    const minDateObj = minDate ? new Date(minDate) : null;
    const maxDateObj = maxDate ? new Date(maxDate) : null;

    // Ensure we have a valid date object
    const dateValue = useMemo(() => {
        if (!value) return null;
        const date = value instanceof Date ? value : new Date(value);
        return isValid(date) ? date : null;
    }, [value]);

    const formattedDate = dateValue ? format(dateValue, 'dd/MM/yyyy') : '';

    // Update calendar position when opening
    useEffect(() => {
        if (isOpen && inputRef.current) {
            const rect = inputRef.current.getBoundingClientRect();
            setCalendarStyles({
                position: 'absolute',
                top: rect.bottom + window.scrollY + 4, // 4px margin below input
                left: rect.left + window.scrollX,
                width: '280px',
                zIndex: 9999
            });
        }
    }, [isOpen]);

    // Handle click outside to close calendar
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (calendarRef.current && !calendarRef.current.contains(e.target) && 
                inputRef.current && !inputRef.current.contains(e.target)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    useEffect(() => {
        if (value) {
            setSelectedDate(new Date(value));
            setViewDate(new Date(value));
        }
    }, [value]);

    const prevMonth = () => {
        setViewDate(prevDate => addMonths(prevDate, -1));
    };

    const nextMonth = () => {
        setViewDate(prevDate => addMonths(prevDate, 1));
    };

    const handleDateSelect = (date) => {
        setSelectedDate(date);
        onChange(date);
        setIsOpen(false);
    };

    // Generate days for the current month
    const days = useMemo(() => {
        const daysArray = [];
        const monthStart = startOfMonth(viewDate);
        const startDate = new Date(monthStart);
        startDate.setDate(1 - startDate.getDay()); // Start from the first day of the week

        for (let i = 0; i < 42; i++) {
            const currentDate = addDays(startDate, i);
            const isCurrentMonth = currentDate.getMonth() === viewDate.getMonth();
            const isDisabled =
                (minDateObj && currentDate < minDateObj) ||
                (maxDateObj && currentDate > maxDateObj);

            daysArray.push({
                date: currentDate,
                isCurrentMonth,
                isToday: currentDate.toDateString() === new Date().toDateString(),
                isSelected: selectedDate && currentDate.toDateString() === selectedDate.toDateString(),
                isDisabled
            });
        }

        return daysArray;
    }, [viewDate, selectedDate, minDateObj, maxDateObj]);

    return (
        <div className="relative" ref={inputRef}>
            {/* Improved the input field with larger sizing */}
            <div
                className="flex items-center text-sm space-x-2 border border-gray-300 dark:border-gray-600 rounded px-3 py-2 cursor-pointer focus-within:ring-1 focus-within:ring-indigo-500 dark:bg-gray-700 hover:border-indigo-400 dark:hover:border-indigo-400 transition-colors"
                onClick={() => setIsOpen(!isOpen)}
            >
                <FaCalendarAlt className="text-indigo-500 dark:text-indigo-400 h-4 w-4" />
                <span className="text-gray-700 dark:text-gray-300">{formattedDate || label}</span>
            </div>

            {isOpen &&
                ReactDOM.createPortal(
                    <div
                        ref={calendarRef}
                        className="bg-white dark:bg-gray-800 shadow-lg border border-gray-200 dark:border-gray-700 rounded-md p-4"
                        style={calendarStyles}
                    >
                        {/* Calendar header */}
                        <div className="flex justify-between items-center mb-3">
                            <button onClick={prevMonth} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded">
                                <FaChevronLeft className="h-4 w-4 text-gray-500 dark:text-gray-400" />
                            </button>
                            <div className="text-sm font-medium">{format(viewDate, 'MMMM yyyy')}</div>
                            <button onClick={nextMonth} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded">
                                <FaChevronRight className="h-4 w-4 text-gray-500 dark:text-gray-400" />
                            </button>
                        </div>

                        {/* Day names */}
                        <div className="grid grid-cols-7 gap-2 mb-2 text-center">
                            {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(day => (
                                <div key={day} className="text-xs font-medium text-gray-500 dark:text-gray-400">
                                    {day}
                                </div>
                            ))}
                        </div>

                        {/* Calendar days */}
                        <div className="grid grid-cols-7 gap-2">
                            {days.map((day, i) => (
                                <button
                                    key={i}
                                    onClick={() => !day.isDisabled && handleDateSelect(day.date)}
                                    disabled={day.isDisabled}
                                    className={`
                                        w-8 h-8 text-sm rounded flex items-center justify-center
                                        ${day.isCurrentMonth ? 'text-gray-700 dark:text-gray-300' : 'text-gray-400 dark:text-gray-600'}
                                        ${day.isSelected ? 'bg-indigo-500 text-white' : 'hover:bg-indigo-100 dark:hover:bg-indigo-900'}
                                        ${day.isToday && !day.isSelected ? 'border border-indigo-400' : ''}
                                        ${day.isDisabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
                                    `}
                                >
                                    {day.date.getDate()}
                                </button>
                            ))}
                        </div>

                        {/* Today button */}
                        <div className="mt-3 flex justify-end">
                            <button
                                onClick={() => handleDateSelect(new Date())}
                                className="text-xs px-3 py-1 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 rounded text-gray-700 dark:text-gray-300"
                            >
                                Today
                            </button>
                        </div>
                    </div>,
                    document.body
                )}
        </div>
    );
};

/**
 * Improved TimeDualRangeSlider - Time range slider with custom calendar pickers
 * @param {number} minTimestamp - Earliest possible timestamp (milliseconds)
 * @param {number} maxTimestamp - Latest possible timestamp (milliseconds, usually current time)
 * @param {Function} onChange - Callback when range changes: (startDate, endDate) => void
 * @param {string} className - Additional CSS classes
 * @param {number} initialStartTimestamp - Optional initial start time
 * @param {number} initialEndTimestamp - Optional initial end time
 */
const TimeDualRangeSlider = ({
    minTimestamp,
    maxTimestamp,
    onChange,
    className = '',
    initialStartTimestamp,
    initialEndTimestamp
}) => {
    // Calculate time range with proper defaults
    const timeRange = useMemo(() => {
        const min = minTimestamp || maxTimestamp - (90 * 24 * 60 * 60 * 1000);
        const max = maxTimestamp; // added extra time if needed
        return {
            min,
            max,
            range: max-min, // placeholder range
            dayMs: 24 * 60 * 60 * 1000,
            totalDays: Math.ceil((max - min) / (24 * 60 * 60 * 1000))
        };
    }, [minTimestamp, maxTimestamp]);

    // Internal state for current selection with proper initialization
    const [currentStartDate, setCurrentStartDate] = useState(() => {
        const initialStart = initialStartTimestamp || timeRange.min;
        return new Date(initialStart);
    });

    const [currentEndDate, setCurrentEndDate] = useState(() => {
        const initialEnd = initialEndTimestamp || timeRange.max;
        return new Date(initialEnd);
    });

    const [sliderMin, setSliderMin] = useState(() => {
        if (initialStartTimestamp) {
            return ((initialStartTimestamp - timeRange.min) / timeRange.range) * 100;
        }
        return 0;
    });

    const [sliderMax, setSliderMax] = useState(() => {
        if (initialEndTimestamp) {
            return ((initialEndTimestamp - timeRange.min) / timeRange.range) * 100;
        }
        return 100;
    });

    const [isDraggingMin, setIsDraggingMin] = useState(false);
    const [isDraggingMax, setIsDraggingMax] = useState(false);
    const [showTooltips, setShowTooltips] = useState(false);

    // Refs for DOM elements
    const sliderMinRef = useRef(null);
    const sliderMaxRef = useRef(null);
    const trackRef = useRef(null);

    // Initialize dates when component mounts or time range changes
    useEffect(() => {
        if (!initialStartTimestamp && !initialEndTimestamp) {
            setCurrentStartDate(new Date(timeRange.min));
            setCurrentEndDate(new Date(timeRange.max));
            setSliderMin(0);
            setSliderMax(100);
        }
    }, [timeRange.min, timeRange.max, initialStartTimestamp, initialEndTimestamp]);

    // Notify parent when selection changes, with debounce
    useEffect(() => {
        if (onChange && isValid(currentStartDate) && isValid(currentEndDate)) {
            const timeoutId = setTimeout(() => {
                onChange(currentStartDate, currentEndDate);
            }, 100);
            return () => clearTimeout(timeoutId);
        }
    }, [currentStartDate, currentEndDate, onChange]);

    // Show tooltips when dragging
    useEffect(() => {
        setShowTooltips(isDraggingMin || isDraggingMax);
    }, [isDraggingMin, isDraggingMax]);

    // Update slider positions when dates change directly (from calendar)
    const updateSliderPositionsFromDates = (startDate, endDate) => {
        if (!timeRange.range || !isValid(startDate) || !isValid(endDate)) return;
        const startTime = startDate.getTime();
        const endTime = endDate.getTime();
        const newSliderMin = ((startTime - timeRange.min) / timeRange.range) * 100;
        const newSliderMax = ((endTime - timeRange.min) / timeRange.range) * 100;
        setSliderMin(Math.max(0, Math.min(100, newSliderMin)));
        setSliderMax(Math.max(0, Math.min(100, newSliderMax)));
    };

    // Handle date picker changes with validation
    const handleStartDateChange = (date) => {
        if (isValid(date)) {
            const startTime = Math.max(timeRange.min, Math.min(currentEndDate.getTime() - 86400000, date.getTime()));
            const newStart = new Date(startTime);
            setCurrentStartDate(newStart);
            updateSliderPositionsFromDates(newStart, currentEndDate);
        }
    };

    const handleEndDateChange = (date) => {
        if (isValid(date)) {
            const endTime = Math.min(timeRange.max, Math.max(currentStartDate.getTime() + 86400000, date.getTime()));
            const newEnd = new Date(endTime);
            setCurrentEndDate(newEnd);
            updateSliderPositionsFromDates(currentStartDate, newEnd);
        }
    };

    // Handle slider changes with improved validation
    const handleMinChange = (e) => {
        const newMin = parseFloat(e.target.value);
        if (!isNaN(newMin) && newMin < sliderMax - 1) {
            setSliderMin(newMin);
            updateDatesFromSliders(newMin, sliderMax);
        }
    };

    const handleMaxChange = (e) => {
        const newMax = parseFloat(e.target.value);
        if (!isNaN(newMax) && newMax > sliderMin + 1) {
            setSliderMax(newMax);
            updateDatesFromSliders(sliderMin, newMax);
        }
    };

    // Handle direct track click
    const handleTrackClick = (e) => {
        e.preventDefault();
        if (isDraggingMin || isDraggingMax || !trackRef.current) return;
        const rect = trackRef.current.getBoundingClientRect();
        const clickPosition = (e.clientX || (e.touches && e.touches[0].clientX)) - rect.left;
        const clickPercent = Math.max(0, Math.min(100, (clickPosition / rect.width) * 100));
        const distToMin = Math.abs(clickPercent - sliderMin);
        const distToMax = Math.abs(clickPercent - sliderMax);
        if (distToMin <= distToMax) {
            if (clickPercent < sliderMax - 1) {
                setSliderMin(clickPercent);
                updateDatesFromSliders(clickPercent, sliderMax);
            }
        } else {
            if (clickPercent > sliderMin + 1) {
                setSliderMax(clickPercent);
                updateDatesFromSliders(sliderMin, clickPercent);
            }
        }
    };

    // Convert slider percentages to actual dates
    const updateDatesFromSliders = (min, max) => {
        if (!timeRange.range) return;
        const startTimestamp = timeRange.min + (timeRange.range * min / 100);
        const endTimestamp = timeRange.min + (timeRange.range * max / 100);
        const newStartDate = new Date(Math.max(timeRange.min, Math.min(endTimestamp - 86400000, startTimestamp)));
        const newEndDate = new Date(Math.min(timeRange.max, Math.max(startTimestamp + 86400000, endTimestamp)));
        newStartDate.setHours(0, 0, 0, 0);
        newEndDate.setHours(23, 59, 59, 999);
        setCurrentStartDate(newStartDate);
        setCurrentEndDate(newEndDate);
    };

    // Generate day markers for slider
    const dayMarkers = useMemo(() => {
        if (!timeRange.totalDays) return [];
        const markers = [];
        let interval = 1;
        if (timeRange.totalDays <= 14) {
            interval = 1;
        } else if (timeRange.totalDays <= 60) {
            interval = 7;
        } else if (timeRange.totalDays <= 365) {
            interval = 14;
        } else {
            interval = 30;
        }
        for (let i = 0; i <= timeRange.totalDays; i += interval) {
            const percent = (i / timeRange.totalDays) * 100;
            if (percent <= 100) {
                markers.push({
                    percent,
                    date: new Date(timeRange.min + (i * timeRange.dayMs))
                });
            }
        }
        return markers;
    }, [timeRange]);

    return (
        <div className={`w-full mb-4 ${className}`}>
            <div className="flex items-center justify-start space-x-4 mb-2">
                <label className="text-xs font-medium text-gray-700 dark:text-gray-300">
                    Time Range
                </label>
                <div className="flex space-x-2 items-center">
                    <CustomDatePicker
                        value={currentStartDate}
                        onChange={handleStartDateChange}
                        maxDate={currentEndDate}
                        minDate={new Date(timeRange.min)}
                        label="Start Date"
                    />
                    <span className="text-xs text-gray-500 dark:text-gray-400">to</span>
                    <CustomDatePicker
                        value={currentEndDate}
                        onChange={handleEndDateChange}
                        minDate={currentStartDate}
                        maxDate={new Date(timeRange.max)}
                        label="End Date"
                    />
                </div>
            </div>

            <div className="relative h-8 mt-3">
                <div
                    className="relative h-8 flex items-center px-1 cursor-pointer"
                    ref={trackRef}
                    onClick={handleTrackClick}
                    onTouchStart={handleTrackClick}
                >
                    {/* Background track */}
                    <div className="absolute w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full" />

                    {/* Time markers */}
                    <div className="absolute w-full top-full mt-1 pointer-events-none">
                        {dayMarkers.map((marker, index) => (
                            <div key={index} className="absolute flex flex-col items-center" style={{ left: `${marker.percent}%` }}>
                                <div className="h-2 w-0.5 bg-gray-300 dark:bg-gray-600 mb-0.5" />
                                {index % 2 === 0 && index > 0 && index < dayMarkers.length - 1 && (
                                    <span className="text-[9px] text-gray-400 dark:text-gray-500 mt-0.5 transform -translate-x-1/2">
                                        {format(marker.date, 'dd/MM')}
                                    </span>
                                )}
                            </div>
                        ))}
                    </div>

                    {/* Filled track */}
                    <div
                        className="absolute h-1.5 bg-indigo-500 rounded-full"
                        style={{
                            left: `${sliderMin}%`,
                            width: `${sliderMax - sliderMin}%`
                        }}
                    />

                    {/* Min handle with tooltip */}
                    <div className="absolute h-8 flex items-center" style={{ left: `calc(${sliderMin}% - 8px)`, width: '16px' }}>
                        <input
                            ref={sliderMinRef}
                            type="range"
                            className="absolute w-full h-8 opacity-0 cursor-pointer z-10"
                            min={0}
                            max={100}
                            step={0.1}
                            value={sliderMin}
                            onChange={handleMinChange}
                            onMouseDown={() => setIsDraggingMin(true)}
                            onMouseUp={() => setIsDraggingMin(false)}
                            onMouseLeave={() => setIsDraggingMin(false)}
                            onTouchStart={() => setIsDraggingMin(true)}
                            onTouchEnd={() => setIsDraggingMin(false)}
                        />

                        <div
                            className={`absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-4 h-4 bg-white dark:bg-gray-200 border-2 ${isDraggingMin ? 'border-indigo-700 scale-110' : 'border-indigo-500'} rounded-full shadow-sm hover:scale-110 transition-all duration-100 pointer-events-none`}
                        />

                        {(isDraggingMin || showTooltips) && (
                            <div className="absolute bottom-full mb-1.5 -translate-x-1/2 left-1/2 bg-indigo-600 text-white text-xs px-1.5 py-0.5 rounded shadow-sm pointer-events-none">
                                {format(currentStartDate, 'dd/MM/yyyy')}
                            </div>
                        )}
                    </div>

                    {/* Max handle with tooltip */}
                    <div className="absolute h-8 flex items-center" style={{ left: `calc(${sliderMax}% - 8px)`, width: '16px' }}>
                        <input
                            ref={sliderMaxRef}
                            type="range"
                            className="absolute w-full h-8 opacity-0 cursor-pointer z-20"
                            min={0}
                            max={100}
                            step={0.1}
                            value={sliderMax}
                            onChange={handleMaxChange}
                            onMouseDown={() => setIsDraggingMax(true)}
                            onMouseUp={() => setIsDraggingMax(false)}
                            onMouseLeave={() => setIsDraggingMax(false)}
                            onTouchStart={() => setIsDraggingMax(true)}
                            onTouchEnd={() => setIsDraggingMax(false)}
                        />

                        <div
                            className={`absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-4 h-4 bg-white dark:bg-gray-200 border-2 ${isDraggingMax ? 'border-indigo-700 scale-110' : 'border-indigo-500'} rounded-full shadow-sm hover:scale-110 transition-all duration-100 pointer-events-none`}
                        />

                        {(isDraggingMax || showTooltips) && (
                            <div className="absolute bottom-full mb-1.5 -translate-x-1/2 left-1/2 bg-indigo-600 text-white text-xs px-1.5 py-0.5 rounded shadow-sm pointer-events-none">
                                {format(currentEndDate, 'dd/MM/yyyy')}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <div className="flex justify-between text-[10px] text-gray-500 dark:text-gray-400 mt-6">
                <span>{format(new Date(timeRange.min), 'dd MMM yyyy')}</span>
                <span>{format(new Date(timeRange.max), 'dd MMM yyyy')}</span>
            </div>
        </div>
    );
};

export default TimeDualRangeSlider;
