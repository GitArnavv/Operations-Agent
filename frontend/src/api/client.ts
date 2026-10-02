const BASE_URL = '/api/v1';

export class ApiError extends Error {
  code?: string;
  requestId?: string;
  status: number;

  constructor(message: string, status: number, code?: string, requestId?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.requestId = requestId;
  }
}

function dispatchNetworkError(message: string, status?: number, endpoint?: string) {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(
      new CustomEvent('aiops:network-error', {
        detail: { message, status, endpoint },
      })
    );
  }
}

export async function apiClient<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('aiops_token');

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${endpoint}`;

  let response: Response;
  try {
    response = await fetch(url, {
      ...options,
      headers,
    });
  } catch (err: any) {
    const errorMsg = !navigator.onLine
      ? 'You are currently offline. Please check your internet connection.'
      : err?.message || 'Network connection failed. Unable to reach operations server.';
    dispatchNetworkError(errorMsg, 0, endpoint);
    throw new ApiError(errorMsg, 0, 'NETWORK_DISCONNECTED');
  }

  if (!response.ok) {
    let errorData: any = {};
    try {
      errorData = await response.json();
    } catch {
      // not json
    }

    const message = errorData.message || response.statusText || 'An error occurred during request';
    dispatchNetworkError(message, response.status, endpoint);

    throw new ApiError(
      message,
      response.status,
      errorData.code,
      errorData.requestId
    );
  }

  return response.json();
}
