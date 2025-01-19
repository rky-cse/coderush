'use client'
import { useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function TournamentConnectForm() {
  const [tournamentId, setTournamentId] = useState('');
  const [connected, setConnected] = useState(false);
  const [client, setClient] = useState(null);
  const [tournamentData, setTournamentData] = useState(null);

  const handleConnect = async () => {
    if (!tournamentId) {
      alert('Please enter a valid Tournament ID');
      return;
    }

    const token = localStorage.getItem('token'); // Retrieve token from local storage

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'), // SockJS endpoint
      connectHeaders: {
        Authorization: `Bearer ${token}`, // Send token for authorization
      },
      debug: (str) => console.log(str),
      onConnect: () => {
        setConnected(true);
        stompClient.subscribe(`/topic/${tournamentId}`, (message) => {
          const data = JSON.parse(message.body);
          setTournamentData(data);
          console.log('Received tournament data:', data);
        });
        console.log(`Subscribed to /topic/${tournamentId}`);
      },
      onStompError: (frame) => {
        console.error('Broker reported error:', frame.headers['message']);
        console.error('Additional details:', frame.body);
      },
    });

    stompClient.activate();
    setClient(stompClient);
  };

  const handleDisconnect = () => {
    if (client) {
      client.deactivate();
      setConnected(false);
      setClient(null);
      setTournamentData(null);
      console.log('Disconnected from STOMP broker');
    }
  };

  return (
    <div className="p-4 max-w-md mx-auto border rounded shadow">
      <h1 className="text-xl font-bold mb-4">Tournament Connect</h1>
      <div className="mb-4">
        <label htmlFor="tournamentId" className="block text-sm font-medium mb-2">
          Tournament ID:
        </label>
        <input
          type="text"
          id="tournamentId"
          className="w-full border px-3 py-2 rounded"
          value={tournamentId}
          onChange={(e) => setTournamentId(e.target.value)}
          placeholder="Enter Tournament ID"
        />
      </div>
      <button
        onClick={connected ? handleDisconnect : handleConnect}
        className={`w-full py-2 px-4 rounded text-white font-semibold ${
          connected ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-500 hover:bg-blue-600'
        }`}
      >
        {connected ? 'Disconnect' : 'Connect'}
      </button>

      {tournamentData && (
        <div className="mt-6 p-4 border rounded bg-gray-100">
          <h2 className="text-lg font-semibold mb-2">Tournament Details</h2>
          <p><strong>ID:</strong> {tournamentData.tournamentId}</p>
          <p><strong>Name:</strong> {tournamentData.name}</p>
          <p><strong>Description:</strong> {tournamentData.description}</p>
          <p><strong>Creator:</strong> {tournamentData.creatorUserName}</p>
          <p><strong>Start Time:</strong> {tournamentData.startTime}</p>
          <p><strong>Duration:</strong> {tournamentData.durationInSeconds} seconds</p>
          <p><strong>Min Rating Required:</strong> {tournamentData.minRatingReq}</p>
          <p><strong>Max Rating Allowed:</strong> {tournamentData.maxRatingReq}</p>
          <p><strong>Rated:</strong> {tournamentData.rated ? 'Yes' : 'No'}</p>
          
        </div>
      )}
    </div>
  );
}
