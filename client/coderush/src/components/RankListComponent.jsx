'use client';
import React, { useEffect, useState } from 'react';
import webSocketService from '@/services/webSocketService';
import { setTournamentEndTime } from '@/redux/slices/tournamentEndTimeSlice';
import { useDispatch } from 'react-redux';
import { Avatar, Skeleton } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import Link from 'next/link';

const RankListComponent = ({ tournamentId, token }) => {
  const [rankList, setRankList] = useState([]);
  const [loading, setLoading] = useState(true);
  const dispatch = useDispatch();

  useEffect(() => {
    // Initialize WebSocket connection
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}`, token);

    // Subscribe to the rank list topic
    const destination = `/topic/rankList${tournamentId}`;
    webSocketService.subscribe(destination, (message) => {
      console.log('Received rank list update:', message);
      
      let data = message;
      // If message is a string, parse it as JSON
      if (typeof message === 'string') {
        try {
          data = JSON.parse(message);
        } catch (err) {
          console.error('Error parsing message', err);
          data = {};
        }
      }
      
      // Ensure rankList is always an array
      setRankList(Array.isArray(data.rankList) ? data.rankList : []);
      dispatch(setTournamentEndTime(data.endTime));
      setLoading(false);
    });

    // Cleanup on component unmount
    return () => {
      webSocketService.unsubscribe(destination);
      webSocketService.disconnect();
    };
  }, [tournamentId, token, dispatch]);

  if (loading) {
    return (
      <div className="p-4">
        <Skeleton active paragraph={{ rows: 10 }} />
      </div>
    );
  }

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">
        Rank List for Tournament {tournamentId}
      </h2>
      <div className="bg-white shadow-md rounded-lg overflow-hidden">
        <table className="min-w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Rank</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">User</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Score</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Penalty</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Testcase Details</th>
            </tr>
          </thead>
          <tbody>
            {rankList.map((rank, index) => (
              <tr key={index} className="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td className="py-3 px-4 text-gray-700">{index + 1}</td>
                <td className="py-3 px-4">
                  <Link href={`/profile/${rank.userName}`}>
                    <div className="flex items-center cursor-pointer">
                      <Avatar src={rank.avatarUrl} icon={<UserOutlined />} className="mr-3" />
                      <span className="text-blue-500 hover:text-blue-600 transition-colors">{rank.userName}</span>
                    </div>
                  </Link>
                </td>
                <td className="py-3 px-4 text-gray-700">{rank.score}</td>
                <td className="py-3 px-4 text-gray-700">{rank.penalty}</td>
                <td className="py-3 px-4">
                  <div className="flex space-x-2">
                    {rank.submissionDTOS && rank.submissionDTOS.length > 0 ? (
                      rank.submissionDTOS.map((testcase, idx) => (
                        <div key={idx} className="bg-gray-100 p-2 rounded-lg text-sm text-gray-600">
                          <div>
                            <strong>Solved:</strong> {testcase.solved ? '✅' : '❌'}
                          </div>
                          <div>
                            <strong>Attempts:</strong> {testcase.numberOfAttempts}
                          </div>
                        </div>
                      ))
                    ) : (
                      <span>No testcase details available.</span>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RankListComponent;
