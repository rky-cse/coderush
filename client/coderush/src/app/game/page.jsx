'use client';
import { useEffect, useState } from 'react';
import webSocketService from '@/services/webSocketService';

const GamePage = () => {
  const [response, setResponse] = useState(null);
  const [token, setToken] = useState('');
  const[userName, setUserName] = useState('');

  useEffect(() => {
    // Fetch the token only on the client
    const storedToken = localStorage.getItem('token');
    setToken(storedToken);
    const storedUserName = localStorage.getItem('username');
    setUserName(storedUserName);
  }, []);

  useEffect(() => {
    if (!token) return;

    // Connect to WebSocket when the token is available
    webSocketService.connect(`${process.env.NEXT_PUBLIC_API_URL}/ws`, token);

    webSocketService.subscribe(`/topic/oneToOneGame/requestPairing/${userName}`, (message) => {
      setResponse(message);
      console.log('Received message:', message);
    });

    return () => {
      webSocketService.disconnect();
    };
  }, [token]);

  const handleTimeClick = (time) => {
    webSocketService.send('/app/oneToOneGame/requestPairing', {timeControl: time });
  };

  const times = [1, 3, 5, 10, 15, 20, 30, 45, 60];

  return (
    <div>
      <h1>Select Time</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 100px)', gap: '10px' }}>
        {times.map((time) => (
          <button
            key={time}
            style={{ padding: '20px', fontSize: '16px' }}
            onClick={() => handleTimeClick(time)}
          >
            {time} mins
          </button>
        ))}
      </div>

      {response && (
        <div style={{ marginTop: '20px' }}>
          <h2>Response:</h2>
          <pre>{JSON.stringify(response, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

export default GamePage;
