import { useEffect, useState } from 'react';

export type TelemetryEvent = {
  eventType: string;
  timestamp: string;
  [key: string]: any;
};

export const useTelemetryStream = () => {
  const [events, setEvents] = useState<TelemetryEvent[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    let eventSource: EventSource | null = null;
    let retryTimeout: ReturnType<typeof setTimeout>;

    const connect = () => {
      const token = localStorage.getItem('aiops_token');
      const url = `/api/v1/telemetry/stream${token ? `?token=${token}` : ''}`;
      
      eventSource = new EventSource(url);

      eventSource.onopen = () => {
        setConnected(true);
      };

      eventSource.addEventListener('CONNECT', (event: MessageEvent) => {
        console.log(event.data);
      });

      eventSource.addEventListener('STOCK_BREACH', (event: MessageEvent) => {
        const data = JSON.parse(event.data);
        setEvents((prev) => [data, ...prev].slice(0, 50));
        // We could also dispatch to a global store here if needed
      });

      eventSource.addEventListener('JOB_STATUS_UPDATED', (event: MessageEvent) => {
        const data = JSON.parse(event.data);
        setEvents((prev) => [data, ...prev].slice(0, 50));
      });

      eventSource.addEventListener('NEW_ATTENTION_ITEM', (event: MessageEvent) => {
        const data = JSON.parse(event.data);
        setEvents((prev) => [data, ...prev].slice(0, 50));
      });

      eventSource.onerror = () => {
        setConnected(false);
        if (eventSource) {
          eventSource.close();
        }
        // Attempt to reconnect after 5 seconds
        retryTimeout = setTimeout(() => {
          connect();
        }, 5000);
      };
    };

    connect();

    return () => {
      clearTimeout(retryTimeout);
      if (eventSource) {
        eventSource.close();
      }
    };
  }, []);

  return { events, connected };
};
