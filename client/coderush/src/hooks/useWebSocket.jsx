import { useEffect, useState } from 'react';
import websocketService from '../services/websocketService';

/**
 * useWebSocket Hook - Ensures a single WebSocket connection is reused across components.
 */
const useWebSocket = () => {
  const [isConnected, setIsConnected] = useState(websocketService.isConnected());

  useEffect(() => {
    if (!isConnected) {
      websocketService.connect(
        () => setIsConnected(true),
        () => setIsConnected(false)
      );
    }

    return () => {
      // DO NOT disconnect here, to keep connection alive across components.
    };
  }, [isConnected]);

  /**
   * Subscribes to a WebSocket topic and triggers a callback function.
   */
  const subscribe = (destination, callback) => {
    websocketService.subscribe(destination, callback);
  };

  /**
   * Sends a message to a WebSocket endpoint.
   */
  const send = (destination, body) => {
    websocketService.send(destination, body);
  };

  return { isConnected, subscribe, send };
};

export default useWebSocket;
