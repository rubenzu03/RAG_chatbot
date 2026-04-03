import axios from 'axios';

//TODO: Move to Docker API URL
const API_BASE_URL = 'http://localhost:8080/api';

const TOKEN_KEY = 'auth_token';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function isAuthenticated(): boolean {
  return !!getToken();
}

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const decoded = atob(padded);
    return JSON.parse(decoded) as Record<string, unknown>;
  } catch {
    return null;
  }
}

export function getCurrentUserEmail(): string | null {
  const token = getToken();
  if (!token) return null;

  const payload = decodeJwtPayload(token);
  if (!payload) return null;

  const candidates = [payload.email, payload.preferred_username, payload.username, payload.sub];

  for (const candidate of candidates) {
    if (typeof candidate === 'string' && candidate.trim().length > 0) {
      return candidate;
    }
  }

  return null;
}

function authHeaders(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function buildJsonHeaders(extraHeaders?: Record<string, string>): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...authHeaders(),
    ...extraHeaders,
  };
}

async function fetchJson<T>(
  endpointWithQuery: string,
  options: RequestInit,
  errorPrefix: string
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${endpointWithQuery}`, options);
  if (!response.ok) {
    throw new Error(`${errorPrefix} with status ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export async function login(email: string, password: string): Promise<string> {
  const response = await api.post('/auth/signin', { email, password });
  const token =
    typeof response.data === 'string'
      ? response.data
      : (response.data?.token as string | undefined);

  if (!token) {
    throw new Error('Login response did not include a token');
  }

  setToken(token);
  return token;
}

export async function register(email: string, password: string): Promise<string> {
  const response = await api.post('/auth/signup', { email, password });
  return response.data;
}

export function logout(): void {
  removeToken();
}

async function tryDeleteAccount(endpoint: string): Promise<boolean> {
  try {
    await api.delete(endpoint, { headers: authHeaders() });
    return true;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      if (status === 404 || status === 405) {
        return false;
      }
    }
    throw error;
  }
}

export async function deleteAccount(): Promise<void> {
  const candidateEndpoints = ['/auth/account', '/auth/delete', '/auth/me'];

  for (const endpoint of candidateEndpoints) {
    const deleted = await tryDeleteAccount(endpoint);
    if (deleted) {
      removeToken();
      return;
    }
  }

  throw new Error('No delete-account endpoint was found in the API.');
}

export async function clearChatHistory(): Promise<boolean> {
  try {
    await api.delete('/ai/chat/history', { headers: authHeaders() });
    return true;
  } catch (error) {
    console.error('Error clearing chat history:', error);
    return false;
  }
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatResponse {
  message: string;
  conversationId: string;
}

export async function sendMessage(message: string): Promise<ChatResponse> {
  const params = new URLSearchParams({
    query: message,
  });

  return fetchJson<ChatResponse>(
    `/ai/ragquery?${params}`,
    {
      method: 'POST',
      headers: buildJsonHeaders(),
    },
    'API request failed'
  );
}

export async function getChatHistory(): Promise<ChatMessage[]> {
  try {
    const data = await fetchJson<{ history: ChatMessage[] }>(
      '/ai/chat/history',
      {
        method: 'GET',
        headers: buildJsonHeaders(),
      },
      'API request failed'
    );
    return data.history as ChatMessage[];
  } catch (error) {
    console.error('Error fetching chat history:', error);
    return [];
  }
}

// ── Question Mode API ──

export interface QuestionResponse {
  questionId: string;
  question: string;
}

export interface EvaluationRequest {
  questionId: string;
  answer: string;
}

export interface EvaluationResponse {
  result: string; // "CORRECTA" | "INCORRECTA" | "PARCIAL"
  explanation: string;
}

export async function generateQuestion(): Promise<QuestionResponse> {
  return fetchJson<QuestionResponse>(
    '/question-mode/generate',
    {
      method: 'POST',
      headers: buildJsonHeaders(),
    },
    'Generate question failed'
  );
}

export async function evaluateAnswer(req: EvaluationRequest): Promise<EvaluationResponse> {
  return fetchJson<EvaluationResponse>(
    '/question-mode/evaluate',
    {
      method: 'POST',
      headers: buildJsonHeaders(),
      body: JSON.stringify(req),
    },
    'Evaluate answer failed'
  );
}

// ── Chat / RAG API ──

export async function streamRagQuery(
  message: string,
  onToken: (token: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): Promise<void> {
  const params = new URLSearchParams({
    query: message,
  });

  try {
    const response = await fetch(`${API_BASE_URL}/ai/ragquery?${params}`, {
      method: 'POST',
      headers: {
        ...buildJsonHeaders({ Accept: 'text/event-stream' }),
      },
    });

    if (!response.ok || !response.body) {
      throw new Error(`API request failed with status ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let dataLines: string[] = [];

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() ?? '';

      for (const line of lines) {
        if (line.startsWith('data:')) {
          dataLines.push(line.slice(5));
        } else if (line.trim() === '') {
          if (dataLines.length > 0) {
            const data = dataLines.join('\n');
            dataLines = [];

            if (data !== '[DONE]') onToken(data);
          }
        }
      }
    }

    if (dataLines.length > 0) {
      const data = dataLines.join('\n');
      if (data !== '[DONE]') onToken(data);
    }

    onComplete();
  } catch (error) {
    onError(error instanceof Error ? error : new Error(String(error)));
  }
}
