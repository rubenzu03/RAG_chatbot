import { useState, useRef, useEffect } from 'react';
import { streamRagQuery, getOrCreateSessionId, clearSessionId, type ChatMessage } from './api';

import { sendMessage } from './api';

export default function ChatbotMain() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string>(() => getOrCreateSessionId());
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleSend = async () => {
    const trimmedInput = input.trim();
    if (!trimmedInput || isLoading) return;

    const userMessage: ChatMessage = { role: 'user', content: trimmedInput };
    setMessages((prevMessages) => [...prevMessages, userMessage]);
    setInput('');
    setIsLoading(true);

    setMessages((prev) => [...prev, { role: 'assistant', content: '' }]);

    await streamRagQuery(
      trimmedInput,
      sessionId,
      (token) => {
        setMessages((prev) => {
          const updated = [...prev];
          const lastIndex = updated.length - 1;
          if (updated[lastIndex]?.role === 'assistant') {
            updated[lastIndex] = {
              ...updated[lastIndex],
              content: updated[lastIndex].content + token,
            };
          }
          return updated;
        });
      },
      // onSessionId - update session if backend returns a new one
      (newSessionId) => {
        setSessionId(newSessionId);
      },
      // onComplete
      () => {
        setIsLoading(false);
      },
      // onError
      (error) => {
        console.error('Stream error:', error);
        setMessages((prev) => {
          const updated = [...prev];
          const lastIndex = updated.length - 1;
          if (updated[lastIndex]?.role === 'assistant') {
            updated[lastIndex] = {
              ...updated[lastIndex],
              content: 'Sorry, an error occurred. Please try again.',
            };
          }
          return updated;
        });
        setIsLoading(false);
      }
    );
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  /* const handleNewChat = () => {
    clearSession();
    setSessionId(getOrCreateSessionId());
    setMessages([]);
    inputRef.current?.focus();
  }; */

  const handleDataDeletion = () => {
    clearSessionId();
  };

  return (
        <div className = "flex flex-col h-full">
          <div className = "flex items-start justify-items-start px-6 py-4 border-b border-gray-700 bg-gray-800">
            <div className = "flex items-center mr-5">
              <img src = "src/assets/react.svg" alt = "PLACEHOLDER" className = "h-8 w-8 mr-2"/>
              <h1 className = "text-3xl font-bold text-white">Chatbot</h1>
            </div>
            <button onClick={handleDataDeletion} className="ml-auto px-4 py-1 bg-red-600 text-white rounded hover:bg-red-700">
              Delete Chat History
            </button>
          </div>
        </div>
    );
}
