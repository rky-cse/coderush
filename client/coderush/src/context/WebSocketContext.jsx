'use client';
import React, { createContext, useContext, useRef, useCallback, useState } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
  const [isConnected, setIsConnected] = useState(false);
  const stompClientRef = useRef(null);

  // Connect with token
  const connect = useCallback((token) => {
    if (stompClientRef.current) return; // Already connected or connecting

    const socket = new SockJS(`${process.env.API_URL}/ws`); // Replace with your server endpoint
    const stompClient = Stomp.over(socket);

    stompClient.connect(
      { Authorization: `Bearer ${token}` }, // Headers for token
      () => {
        stompClientRef.current = stompClient;
        setIsConnected(true);
        console.log('WebSocket connected.');
      },
      (error) => {
        console.error('WebSocket error:', error);
        setIsConnected(false);
      }
    );
  }, []);

  // Disconnect
  const disconnect = useCallback(() => {
    if (!stompClientRef.current) return;
    stompClientRef.current.disconnect(() => {
      console.log('WebSocket disconnected.');
    });
    stompClientRef.current = null;
    setIsConnected(false);
  }, []);

  // Subscribe to a topic
  const subscribe = useCallback((destination, callback, headers = {}) => {
    if (!stompClientRef.current) return null;
    return stompClientRef.current.subscribe(destination, (message) => {
      const data = JSON.parse(message.body);
      callback(data);
    }, headers);
  }, []);

  // Send a message
  const sendMessage = useCallback((destination, body, headers = {}) => {
    if (!stompClientRef.current) return;
    stompClientRef.current.send(destination, headers, JSON.stringify(body));
  }, []);

  const value = {
    connect,
    disconnect,
    subscribe,
    sendMessage,
    isConnected,
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};