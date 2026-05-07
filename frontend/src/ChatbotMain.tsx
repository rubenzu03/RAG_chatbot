import { useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { streamRagQuery, type ChatMessage } from './api';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import QuestionMode from './QuestionMode';
import sendIcon from './assets/send-ins-line.svg';

type AppMode = 'chat' | 'questions';

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
          <svg
            aria-hidden="true"
            focusable="false"
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          Copied!
        </>
      ) : (
        <>
          <svg
            aria-hidden="true"
            focusable="false"
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
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

// We removed normalizeMarkdown as ReactMarkdown with remark-gfm handles standard markdown correctly.
// The real issue was in api.ts where SSE chunks were prepended with spaces, breaking markdown rendering.

export default function ChatbotMain() {
  const [mode, setMode] = useState<AppMode>('chat');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const autoScrollRef = useRef<boolean>(true);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    document.title = 'Chatbot Answer Mode';
  }, []);

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

  const updateLastAssistantMessage = useCallback((transform: (content: string) => string) => {
    setMessages((prev) => {
      const updated = [...prev];
      const lastIndex = updated.length - 1;

      if (updated[lastIndex]?.role === 'assistant') {
        updated[lastIndex] = {
          ...updated[lastIndex],
          content: transform(updated[lastIndex].content),
        };
      }

      return updated;
    });
  }, []);

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
      (token) => {
        updateLastAssistantMessage((content) => content + token);
      },
      () => {
        setIsLoading(false);
      },
      (error) => {
        console.error('Stream error:', error);
        updateLastAssistantMessage(() => 'Sorry, an error occurred. Please try again.');
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

  return (
    <div className="flex flex-col h-screen">
      <div className="flex items-center justify-start px-6 py-4 border-b border-gray-700 bg-gray-800">
        <h1 className="text-2xl font-bold text-white">Chatbot</h1>

        {/* Mode switcher tabs */}
        <div
          className="flex ml-8 bg-gray-900/50 rounded-lg p-1"
          role="tablist"
          aria-label="View mode"
        >
          <button
            id="tab-chat"
            role="tab"
            aria-selected={mode === 'chat'}
            aria-controls="chat-panel"
            onClick={() => setMode('chat')}
            className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors duration-200 flex items-center gap-1.5 ${
              mode === 'chat' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            <svg
              aria-hidden="true"
              focusable="false"
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
              />
            </svg>
            Chat
          </button>
          <button
            id="tab-questions"
            role="tab"
            aria-selected={mode === 'questions'}
            aria-controls="questions-panel"
            onClick={() => setMode('questions')}
            className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors duration-200 flex items-center gap-1.5 ${
              mode === 'questions' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            <svg
              aria-hidden="true"
              focusable="false"
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
            Questions
          </button>
        </div>

        <button
          className="text-white bg-gray-600 px-4 py-2 rounded-lg ml-auto hover:bg-gray-700"
          onClick={() => navigate('/profile')}
        >
          Profile
        </button>
      </div>
      {mode === 'questions' ? (
        <div
          id="questions-panel"
          role="region"
          aria-labelledby="tab-questions"
          aria-hidden={mode !== 'questions'}
          className="flex-1 overflow-hidden bg-primary-dark"
        >
          <QuestionMode />
        </div>
      ) : (
        <div
          id="chat-panel"
          role="region"
          aria-labelledby="tab-chat"
          aria-hidden={mode !== 'chat'}
          className="flex-1 flex flex-col"
        >
          <div
            ref={messagesContainerRef}
            role="log"
            aria-live="polite"
            aria-atomic={false}
            aria-label="Conversation log"
            className="flex-1 overflow-y-auto px-15 py-6 space-y-4 bg-primary-dark"
          >
            {messages.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-400">
                <h2 className="text-xl font-semibold text-gray-300 mb-2">Welcome to RAG Chatbot</h2>
                <p className="text-center max-w-md">
                  Start a conversation by typing a message below. I can help answer questions based
                  on your knowledge base.
                </p>
              </div>
            ) : (
              // Messages List
              <>
                {messages.map((message, index) => (
                  <div
                    key={index}
                    role="article"
                    aria-label={`${message.role === 'user' ? 'User' : 'Assistant'} message`}
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
                      <div className="markdown-content wrap-break-words">
                        {message.content ? (
                          <ReactMarkdown
                            remarkPlugins={[remarkGfm]}
                            components={markdownComponents}
                          >
                            {message.content}
                          </ReactMarkdown>
                        ) : (
                          <span className="inline-flex gap-1" aria-hidden="true">
                            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></span>
                          </span>
                        )}
                      </div>

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
                aria-label="Type your message"
                disabled={isLoading}
                rows={2}
                className="w-full bg-message-bot-dark text-white placeholder-gray-400 rounded-lg pl-4 pr-14 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed resize-none min-h-20 max-h-48"
                style={{
                  scrollbarWidth: 'thin',
                }}
              />
              <button
                onClick={handleSend}
                aria-label="Send message"
                disabled={!input.trim() || isLoading}
                className="absolute right-3 p-2 bg-message-user-dark hover:bg-blue-600 disabled:bg-button-disabled-dark disabled:cursor-not-allowed text-white rounded-full transition-colors duration-200 flex items-center justify-center"
              >
                {isLoading ? (
                  <svg
                    aria-hidden="true"
                    focusable="false"
                    className="animate-spin h-5 w-5"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
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
                  <img src={sendIcon} alt="Send" className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>
          {/* AI Content warning */}
          <div
            role="note"
            className="bg-message-bot-dark px-4 py-4 text-center text-base text-gray-200"
          >
            <p>
              Content generated by AI may not be accurate or reliable. Please verify information
              from trusted sources. Do not share sensitive personal information. Use responsibly.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
