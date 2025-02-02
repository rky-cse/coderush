'use client';
import React, { useEffect, useState } from 'react';
import webSocketService from '@/services/webSocketService';

const RankListComponent = ({ tournamentId, token }) => {
  const [rankList, setRankList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Initialize WebSocket connection
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}`, token);

    // Subscribe to the rank list topic
    const destination = `/topic/rankList${tournamentId}`;
    webSocketService.subscribe(destination, (message) => {
      console.log('Received rank list update:', message);
      setRankList(message.rankList || []);
      setLoading(false);
    });

    // Cleanup on component unmount
    return () => {
      webSocketService.unsubscribe(destination);
      webSocketService.disconnect();
    };
  }, [tournamentId, token]);

  if (loading) {
    return <div>Loading rank list...</div>;
  }

  return (
    <div>
      <h2>Rank List for Tournament {tournamentId}</h2>
      <table className="min-w-full border-collapse border border-gray-400">
        <thead>
          <tr>
            <th className="border border-gray-300 px-4 py-2">User</th>
            <th className="border border-gray-300 px-4 py-2">Score</th>
            <th className="border border-gray-300 px-4 py-2">Testcase Details</th>
          </tr>
        </thead>
        <tbody>
          {rankList.map((rank, index) => (
            <tr key={index}>
              <td className="border border-gray-300 px-4 py-2">{rank.userName}</td>
              <td className="border border-gray-300 px-4 py-2">{rank.score}</td>
              <td className="border border-gray-300 px-4 py-2">
                <div className="flex space-x-4">
                  {rank.userTestcases.map((testcase, idx) => (
                    <div key={idx} className="bg-gray-100 p-2 rounded">
                      {/* <div><strong>ID:</strong> {testcase.testcaseId}</div> */}
                      <div><strong>Solved:</strong> {testcase.isSolved ? 'Yes' : 'No'}</div>
                      <div><strong>Attempts:</strong> {testcase.numberOfAttempts}</div>
                    </div>
                  ))}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default RankListComponent;
