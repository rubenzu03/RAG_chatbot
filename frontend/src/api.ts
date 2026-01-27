import axios from 'axios';
import * as dotenv from 'dotenv';

const API_BASE_URL = 'http://localhost:8080/api';

const SESSION_KEY = 'chatbot_session_id';

//TODO: Setup env
// dotenv.config

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export function getOrCreateSessionId(): string {
  let sessionId = localStorage.getItem(SESSION_KEY);
  if (!sessionId) {
    sessionId = crypto.randomUUID();
    localStorage.setItem(SESSION_KEY, sessionId);
  }
  return sessionId;
}

export function setSessionId(sessionId: string): void {
  localStorage.setItem(SESSION_KEY, sessionId);
}

export function clearSessionId(): void {
  localStorage.removeItem(SESSION_KEY);
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatResponse {
  message: string;
  conversationId: string;
}

export async function sendMessage(message: string, sessionId?: string): Promise<ChatResponse> {
  const sid = sessionId || getOrCreateSessionId();
  const params = new URLSearchParams({
    query: message,
    sessionId: sid,
  });

  const response = await fetch(`${API_BASE_URL}/ai/test/query?${params}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`API request failed with status ${response.status}`);
  }

  const data = await response.json();
  return data;
}

export async function streamRagQuery(
  message: string,
  sessionId: string,
  onToken: (token: string) => void,
  onSessionId: (sessionId: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): Promise<void> {
  const params = new URLSearchParams({
    query: message,
    sessionId: sessionId,
  });

  try {
    const response = await fetch(`${API_BASE_URL}/ai/test/ragquery?${params}`, {
      method: 'POST',
      headers: {
        Accept: 'text/event-stream',
      },
    });

    if (!response.ok || !response.body) {
      throw new Error(`API request failed with status ${response.status}`);
    }

    const reader = response.body?.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let sessionIdExtracted = false;

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5);

          if (!sessionIdExtracted && data.trim().startsWith('[SESSION:')) {
            const match = data.match(/\[SESSION:([^\]]+)\]/);
            if (match) {
              const newSessionId = match[1];
              const existingSessionId = localStorage.getItem(SESSION_KEY);
              if (!existingSessionId) {
                setSessionId(newSessionId);
                onSessionId(newSessionId);
              }
              sessionIdExtracted = true;
              const remaining = data.replace(/\[SESSION:[^\]]+\]/, '').trimStart();
              if (remaining) {
                onToken(remaining);
              }
              continue;
            }
          }

          if (data.trim() && data.trim() !== '[DONE]') {
            onToken(data);
          }
        }
      }
    }

    onComplete();
  } catch (error) {
    onError(error instanceof Error ? error : new Error(String(error)));
  }
}
