import axios from 'axios';

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

function authHeaders(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function login(email: string, password: string): Promise<string> {
  const response = await api.post('/auth/signin', { email, password });
  const token = response.data;
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

  const response = await fetch(`${API_BASE_URL}/ai/test/query?${params}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`API request failed with status ${response.status}`);
  }

  const data = await response.json();
  return data;
}

export async function getChatHistory(): Promise<ChatMessage[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/ai/chat/history`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...authHeaders(),
      },
    });

    if (!response.ok) {
      throw new Error(`API request failed with status ${response.status}`);
    }

    const data = await response.json();
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
  const response = await fetch(`${API_BASE_URL}/question-mode/generate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
  });
  if (!response.ok) {
    throw new Error(`Generate question failed with status ${response.status}`);
  }
  return response.json();
}

export async function evaluateAnswer(req: EvaluationRequest): Promise<EvaluationResponse> {
  const response = await fetch(`${API_BASE_URL}/question-mode/evaluate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(req),
  });
  if (!response.ok) {
    throw new Error(`Evaluate answer failed with status ${response.status}`);
  }
  return response.json();
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
    const response = await fetch(`${API_BASE_URL}/ai/test/ragquery?${params}`, {
      method: 'POST',
      headers: {
        Accept: 'text/event-stream',
        ...authHeaders(),
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
