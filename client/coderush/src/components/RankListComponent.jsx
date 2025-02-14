// 'use client';
// import React, { useEffect, useState } from 'react';
// import webSocketService from '@/services/webSocketService';
// import { setTournamentEndTime } from '@/redux/slices/tournamentEndTimeSlice';
// import { useDispatch } from 'react-redux';


// const RankListComponent = ({ tournamentId, token }) => {
//   const [rankList, setRankList] = useState([]);
//   const [loading, setLoading] = useState(true);
//   const dispatch = useDispatch();


//   useEffect(() => {
//     // Initialize WebSocket connection
//     webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}`, token);

//     // Subscribe to the rank list topic
//     const destination = `/topic/rankList${tournamentId}`;
//     webSocketService.subscribe(destination, (message) => {
//       console.log('Received rank list update:', message);
//       setRankList(message.rankList || []);
//       dispatch(setTournamentEndTime(message.endTime));
//       setLoading(false);
//     });

//     // Cleanup on component unmount
//     return () => {
//       webSocketService.unsubscribe(destination);
//       webSocketService.disconnect();
//     };
//   }, [tournamentId, token]);

//   if (loading) {
//     return <div>Loading rank list...</div>;
//   }

//   return (
//     <div>
//       <h2>Rank List for Tournament {tournamentId}</h2>
//       <table className="min-w-full border-collapse border border-gray-400">
//         <thead>
//           <tr>
//             <th className="border border-gray-300 px-4 py-2">User</th>
//             <th className="border border-gray-300 px-4 py-2">Score</th>
//             <th className="border border-gray-300 px-4 py-2">Testcase Details</th>
//           </tr>
//         </thead>
//         <tbody>
//           {rankList.map((rank, index) => (
//             <tr key={index}>
//               <td className="border border-gray-300 px-4 py-2">{rank.userName}</td>
//               <td className="border border-gray-300 px-4 py-2">{rank.score}</td>
//               <td className="border border-gray-300 px-4 py-2">
//                 <div className="flex space-x-4">
//                   {rank.userTestcases.map((testcase, idx) => (
//                     <div key={idx} className="bg-gray-100 p-2 rounded">
//                       {/* <div><strong>ID:</strong> {testcase.testcaseId}</div> */}
//                       <div><strong>Solved:</strong> {testcase.solved ? 'Yes' : 'No'}</div>
//                       <div><strong>Attempts:</strong> {testcase.numberOfAttempts}</div>
//                     </div>
//                   ))}
//                 </div>
//               </td>
//             </tr>
//           ))}
//         </tbody>
//       </table>
//     </div>
//   );
// };

// export default RankListComponent;


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
      setRankList(message.rankList || []);
      dispatch(setTournamentEndTime(message.endTime));
      setLoading(false);
    });

    // Cleanup on component unmount
    return () => {
      webSocketService.unsubscribe(destination);
      webSocketService.disconnect();
    };
  }, [tournamentId, token]);

  if (loading) {
    return (
      <div className="p-4">
        <Skeleton active paragraph={{ rows: 10 }} />
      </div>
    );
  }

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Rank List for Tournament {tournamentId}</h2>
      <div className="bg-white shadow-md rounded-lg overflow-hidden">
        <table className="min-w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Rank</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">User</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Score</th>
              <th className="text-left py-3 px-4 font-semibold text-sm text-gray-600">Testcase Details</th>
            </tr>
          </thead>
          <tbody>
            {rankList.map((rank, index) => (
              <tr key={index} className="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td className="py-3 px-4 text-gray-700">{index + 1}</td>
                <td className="py-3 px-4">
                  <Link href={`/profile/${rank.userName}`} passHref>
                    <div className="flex items-center cursor-pointer">
                      <Avatar src={rank.avatarUrl} icon={<UserOutlined />} className="mr-3" />
                      <span className="text-blue-500 hover:text-blue-600 transition-colors">{rank.userName}</span>
                    </div>
                  </Link>
                </td>
                <td className="py-3 px-4 text-gray-700">{rank.score}</td>
                <td className="py-3 px-4">
                  <div className="flex space-x-2">
                    {rank.userTestcases.map((testcase, idx) => (
                      <div key={idx} className="bg-gray-100 p-2 rounded-lg text-sm text-gray-600">
                        <div><strong>Solved:</strong> {testcase.solved ? '✅' : '❌'}</div>
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
    </div>
  );
};

export default RankListComponent;