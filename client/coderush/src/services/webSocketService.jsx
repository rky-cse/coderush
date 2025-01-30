import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.subscriptions = new Map();
    this.isConnected = false;
    this.connectionCallbacks = [];
    this.pendingSubscriptions = [];
    this.pendingMessages = [];
  }

  connect(url, token) {
    if (this.stompClient) return;

    const socket = new SockJS(url);
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        console.log('WebSocket connected');
        this.isConnected = true;

        // Process pending subscriptions
        this.pendingSubscriptions.forEach(({ destination, callback }) => {
          this.subscribe(destination, callback);
        });
        this.pendingSubscriptions = [];

        // Process pending messages
        this.pendingMessages.forEach(({ destination, body }) => {
          this.send(destination, body);
        });
        this.pendingMessages = [];

        // Trigger connection callbacks
        this.connectionCallbacks.forEach((callback) => callback());
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers.message);
        this.isConnected = false;
      },
      onWebSocketClose: () => {
        console.log('WebSocket disconnected');
        this.isConnected = false;
      },
    });

    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
      this.isConnected = false;
    }
  }

  subscribe(destination, callback) {
    if (this.stompClient && this.stompClient.connected) {
      console.log(`Subscribing to ${destination}`);
      const subscription = this.stompClient.subscribe(destination, (message) => {
        callback(JSON.parse(message.body));
      });
      this.subscriptions.set(destination, subscription);
    } else {
      console.log('WebSocket is not connected. Queueing subscription...');
      this.pendingSubscriptions.push({ destination, callback });
    }
  }

  unsubscribe(destination) {
    if (this.subscriptions.has(destination)) {
      console.log(`Unsubscribing from ${destination}`);
      this.subscriptions.get(destination).unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  send(destination, body) {
    if (this.stompClient && this.stompClient.connected) {
      console.log(`Sending message to ${destination}:`, body);
      this.stompClient.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.log('WebSocket is not connected. Queueing message...');
      this.pendingMessages.push({ destination, body });
    }
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;