import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

export type ToastType = 'error' | 'warning' | 'success' | 'info';

export interface ToastItem {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
}

interface ToastContextType {
  toasts: ToastItem[];
  addToast: (toast: Omit<ToastItem, 'id'>) => string;
  removeToast: (id: string) => void;
  showError: (title: string, message?: string) => string;
  showSuccess: (title: string, message?: string) => string;
  showWarning: (title: string, message?: string) => string;
  showInfo: (title: string, message?: string) => string;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (toast: Omit<ToastItem, 'id'>) => {
      const id = `toast-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
      const newToast: ToastItem = { ...toast, id };
      const duration = toast.duration ?? (toast.type === 'error' ? 6000 : 4000);

      setToasts((prev) => [...prev, newToast]);

      if (duration > 0) {
        setTimeout(() => {
          removeToast(id);
        }, duration);
      }

      return id;
    },
    [removeToast]
  );

  const showError = useCallback(
    (title: string, message?: string) => addToast({ type: 'error', title, message }),
    [addToast]
  );

  const showSuccess = useCallback(
    (title: string, message?: string) => addToast({ type: 'success', title, message }),
    [addToast]
  );

  const showWarning = useCallback(
    (title: string, message?: string) => addToast({ type: 'warning', title, message }),
    [addToast]
  );

  const showInfo = useCallback(
    (title: string, message?: string) => addToast({ type: 'info', title, message }),
    [addToast]
  );

  // Monitor network online / offline transitions
  useEffect(() => {
    const handleOnline = () => {
      showSuccess(
        'Connection Restored',
        'Back online. Telemetry and real-time ERP sync are active.'
      );
    };

    const handleOffline = () => {
      showWarning(
        'Offline Mode Detected',
        'Network disconnected. Operating with cached data. Sync will resume upon reconnection.'
      );
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Initial check
    if (!navigator.onLine) {
      handleOffline();
    }

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [showSuccess, showWarning]);

  // Listen for global network error events dispatched from api/client
  useEffect(() => {
    const handleNetworkError = (event: Event) => {
      const customEvent = event as CustomEvent<{
        message: string;
        status?: number;
        endpoint?: string;
      }>;
      const detail = customEvent.detail;
      const statusText = detail.status ? ` (HTTP ${detail.status})` : '';
      showError(
        `API Request Failed${statusText}`,
        detail.message || 'Unable to communicate with the operations backend.'
      );
    };

    window.addEventListener('aiops:network-error', handleNetworkError);
    return () => {
      window.removeEventListener('aiops:network-error', handleNetworkError);
    };
  }, [showError]);

  return (
    <ToastContext.Provider
      value={{
        toasts,
        addToast,
        removeToast,
        showError,
        showSuccess,
        showWarning,
        showInfo,
      }}
    >
      {children}
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};
