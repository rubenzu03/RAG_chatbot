import { useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  streamRagQuery,
  getOrCreateSessionId,
  clearSessionId,
  logout,
  type ChatMessage,
} from './api';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneDark } from 'react-syntax-highlighter/dist/esm/styles/prism';

function CopyButton({ code }: { code: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }, [code]);

  return (
    <button
      onClick={handleCopy}
      className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-gray-200 transition-colors"
    >
      {copied ? (
        <>
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          Copied!
        </>
      ) : (
        <>
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
            />
          </svg>
          Copy code
        </>
      )}
    </button>
  );
}

// Overrides markdown for proper code block render
const markdownComponents: React.ComponentProps<typeof ReactMarkdown>['components'] = {
  code({ className, children, ...props }) {
    const match = /language-(\w+)/.exec(className || '');
    const codeString = String(children).replace(/\n$/, '');
    const isBlock = match || codeString.includes('\n');

    if (isBlock) {
      const lang = match?.[1] ?? 'text';
      return (
        <div className="code-block-wrapper rounded-lg overflow-hidden my-3 border border-[#3a3a3a]">
          <div className="flex items-center justify-between bg-[#2f2f2f] px-4 py-2">
            <span className="text-xs text-gray-400 select-none">{lang}</span>
            <CopyButton code={codeString} />
          </div>
          <SyntaxHighlighter
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            style={oneDark as any}
            language={lang}
            PreTag="div"
            wrapLongLines
            customStyle={{
              margin: 0,
              padding: '1rem 1.25rem',
              background: '#1e1e1e',
              fontSize: '0.85rem',
              lineHeight: '1.65',
              borderRadius: 0,
            }}
            codeTagProps={{
              style: {
                fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
              },
            }}
          >
            {codeString}
          </SyntaxHighlighter>
        </div>
      );
    }

    return (
      <code className="inline-code" {...props}>
        {children}
      </code>
    );
  },
  pre({ children }) {
    return <>{children}</>;
  },
};

// Fix for markdown rendering
function normalizeMarkdown(text: string): string {
  return text
    .replace(/([^\n])(\d+\.\s)/g, '$1\n\n$2')
    .replace(/([^\n])([-*+]\s)/g, '$1\n\n$2')
    .replace(/\n{3,}/g, '\n\n');
}

export default function ChatbotMain() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string>(() => getOrCreateSessionId());
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const autoScrollRef = useRef<boolean>(true);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/auth', { replace: true });
  };

  useEffect(() => {
    document.title = 'Chatbot';
  }, [sessionId]);

  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container) return;

    const onScroll = () => {
      const threshold = 150;
      const distanceFromBottom =
        container.scrollHeight - container.scrollTop - container.clientHeight;
      autoScrollRef.current = distanceFromBottom < threshold;
    };

    container.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
    return () => container.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container) return;

    if (!autoScrollRef.current) return;

    if (isLoading) {
      container.scrollTop = container.scrollHeight;
    } else {
      try {
        container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
      } catch (e) {
        console.warn(e);
        container.scrollTop = container.scrollHeight;
      }
    }
  }, [messages, isLoading]);

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
      (newSessionId) => {
        setSessionId(newSessionId);
      },
      () => {
        setIsLoading(false);
      },
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
        <button
          className="text-white bg-gray-600 px-4 py-2 rounded-lg ml-2 hover:bg-gray-700"
          onClick={handleLogout}
        >
          Logout
        </button>
      </div>
      <div
        ref={messagesContainerRef}
        className="flex-1 overflow-y-auto px-15 py-6 space-y-4 bg-primary-dark"
      >
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

                  <div className="markdown-content break-words">
                    {message.content ? (
                      <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
                        {normalizeMarkdown(message.content)}
                      </ReactMarkdown>
                    ) : (
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
