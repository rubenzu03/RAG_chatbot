import { useState, useRef, useEffect } from 'react';
import { streamRagQuery, getOrCreateSessionId, clearSessionId, type ChatMessage } from './api';
import ReactMarkdown from 'react-markdown';

import { sendMessage } from './api';

export default function ChatbotMain() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string>(() => getOrCreateSessionId());
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

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

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
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
    clearMessages();
    setSessionId(getOrCreateSessionId());
    inputRef.current?.focus();
  };

  const clearMessages = () => {
    setMessages([]);
  };

  return (
    <div className="flex flex-col h-screen">
      <div className="flex items-center justify-start px-6 py-4 border-b border-gray-700 bg-gray-800">
        <img src="/src/assets/react.svg" alt="PLACEHOLDER" className="w-8 h-8 mr-3" />
        <h1 className="text-2xl font-bold text-white">Chatbot</h1>
        <button
          className="text-white bg-red-500 px-4 py-2 rounded-lg ml-auto hover:bg-red-800"
          onClick={handleDataDeletion}
        >
          Delete Data
        </button>
      </div>
      <div className="flex-1 overflow-y-auto px-15 py-6 space-y-4 bg-primary-dark">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <h2 className="text-xl font-semibold text-gray-300 mb-2">Welcome to RAG Chatbot</h2>
            <p className="text-center max-w-md">
              Start a conversation by typing a message below. I can help answer questions based on
              your knowledge base.
            </p>
          </div>
        ) : (
          // Messages List
          <>
            {messages.map((message, index) => (
              <div
                key={index}
                className={`flex ${
                  message.role === 'user' ? 'justify-end' : 'justify-start'
                } animate-fade-in`}
              >
                <div
                  className={`max-w-[65%] rounded-2xl px-4 py-3 ${
                    message.role === 'user'
                      ? 'bg-message-user-dark text-white'
                      : 'bg-message-bot-dark text-gray-100'
                  }`}
                >
                  {/* <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-semibold opacity-80">
                      {message.role === 'user' ? 'You' : 'Assistant'}
                    </span>
                  </div> */}

                  <div className="whitespace-pre-wrap wrapbreak-words">
                    {message.content ? (<ReactMarkdown>{message.content}</ReactMarkdown>) : (
                      <span className="inline-flex gap-1">
                        <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                        <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                        <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></span>
                      </span>
                    )}
                  </div>

                  {/* Timestamp */}
                  <div className="text-xs opacity-60 mt-1">
                    {new Date().toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </div>
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Input Area */}
      <div className="bg-primary-dark px-5 py-4">
        <div className="relative max-w-4xl mx-auto flex items-center">
          <textarea
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type your message... (Press Enter to send, Shift+Enter for new line)"
            disabled={isLoading}
            rows={2}
            className="w-full bg-message-bot-dark text-white placeholder-gray-400 rounded-lg pl-4 pr-14 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed resize-none min-h-20 max-h-48"
            style={{
              scrollbarWidth: 'thin',
            }}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || isLoading}
            className="absolute right-3 p-2 bg-message-user-dark hover:bg-blue-600 disabled:bg-button-disabled-dark disabled:cursor-not-allowed text-white rounded-full transition-colors duration-200 flex items-center justify-center"
          >
            {isLoading ? (
              <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
            ) : (
              <img src="/src/assets/send-ins-line.svg" alt="Send" className="w-5 h-5" />
            )}
          </button>
        </div>
      </div>
      {/* AI Content warning */}
      <div className="bg-message-bot-dark px-4 py-4 text-center text-base text-gray-200">
        <p>
          Content generated by AI may not be accurate or reliable. Please verify information from
          trusted sources.
        </p>
      </div>
    </div>
  );
}
