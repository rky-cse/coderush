'use client'
import React, { useState, useCallback } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Timer } from 'lucide-react';
import { getCookie } from 'cookies-next';
const DuelPage = () => {
  const timeControls = [
    5, 15, 25, 45, 60, 75, 90, 100, 120
  ];

  const [selectedTime, setSelectedTime] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [timeoutId, setTimeoutId] = useState(null);

  const handleBackendCall = useCallback((time) => {
    console.log(`Making backend call to find match for ${time} minute game`);
  }, []);

  const handleTimeControlClick = (time) => {
    if (isLoading && selectedTime === time) {
      if (timeoutId) {
        clearTimeout(timeoutId);
        setTimeoutId(null);
      }
      setIsLoading(false);
      setSelectedTime(null);
    } 
    else if (isLoading && selectedTime !== time) {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
      setSelectedTime(time);
      const newTimeoutId = setTimeout(() => {
        handleBackendCall(time);
        setIsLoading(false);
        setSelectedTime(null);
        setTimeoutId(null);
      }, 4000);
      setTimeoutId(newTimeoutId);
    }
    else if (!isLoading) {
      setSelectedTime(time);
      setIsLoading(true);
      const newTimeoutId = setTimeout(() => {
        handleBackendCall(time);
        setIsLoading(false);
        setSelectedTime(null);
        setTimeoutId(null);
      }, 4000);
      setTimeoutId(newTimeoutId);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600 mb-4">
            Duel
          </h1>
          <p className="text-gray-600 text-lg">Select your preferred time control to start the game</p>
        </div>
        
        <Card className="bg-white/80 backdrop-blur-sm shadow-2xl border-0">
          <CardContent className="p-8">
            <div className="grid grid-cols-3 gap-6">
              {timeControls.map((time) => (
                <button
                  key={time}
                  onClick={() => handleTimeControlClick(time)}
                  className={`
                    relative h-36 rounded-xl 
                    border-2 ${selectedTime === time ? 'border-blue-400' : 'border-gray-100'}
                    hover:border-blue-300 transition-all duration-300
                    flex flex-col items-center justify-center
                    bg-white hover:bg-blue-50/50
                    shadow-lg hover:shadow-xl
                    group
                  `}
                >
                  {/* Smooth border animation */}
                  {isLoading && selectedTime === time && (
                    <div className="absolute inset-0">
                      <svg className="absolute inset-0 w-full h-full" viewBox="0 0 100 100">
                        <rect
                          x="2"
                          y="2"
                          width="96"
                          height="96"
                          fill="none"
                          stroke="white"
                          strokeWidth="2"
                          rx="12"
                          className="base-path"
                        />
                        <rect
                          x="2"
                          y="2"
                          width="96"
                          height="96"
                          fill="none"
                          stroke="#3B82F6"
                          strokeWidth="2"
                          rx="12"
                          className="animated-path"
                        />
                      </svg>
                    </div>
                  )}
                  
                  <Timer className="w-8 h-8 mb-3 text-blue-500 group-hover:text-blue-600 transition-colors" />
                  <span className="text-2xl font-semibold text-gray-700 group-hover:text-gray-900">
                    {time} mins
                  </span>
                  <span className="text-sm text-gray-400 mt-1">
                    {time === 5 ? 'Bullet' : time <= 15 ? 'Blitz' : 'Rapid'}
                  </span>
                </button>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
      
      <style jsx global>{`
        .base-path {
          stroke-dasharray: 388;
          stroke-dashoffset: 0;
        }
        
        .animated-path {
          stroke-dasharray: 388;
          stroke-dashoffset: 388;
          animation: draw 4s linear forwards;
        }

        @keyframes draw {
          to {
            stroke-dashoffset: 0;
          }
        }
      `}</style>
    </div>
  );
};

export default DuelPage;