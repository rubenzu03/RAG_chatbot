import { useState, useRef, useEffect } from 'react';
import {
  generateQuestion,
  evaluateAnswer,
  type QuestionResponse,
  type EvaluationResponse,
} from './api';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

type Phase = 'idle' | 'loading-question' | 'answering' | 'evaluating' | 'result';

interface HistoryEntry {
  question: string;
  answer: string;
  result: string;
  explanation: string;
}

export default function QuestionMode() {
  const [phase, setPhase] = useState<Phase>('idle');
  const [currentQuestion, setCurrentQuestion] = useState<QuestionResponse | null>(null);
  const [answer, setAnswer] = useState('');
  const [evaluation, setEvaluation] = useState<EvaluationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [score, setScore] = useState({ correct: 0, partial: 0, incorrect: 0 });
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [history, phase]);

  const handleGenerate = async () => {
    setPhase('loading-question');
    setError(null);
    setEvaluation(null);
    setAnswer('');
    try {
      const res = await generateQuestion();
      setCurrentQuestion(res);
      setPhase('answering');
      setTimeout(() => inputRef.current?.focus(), 100);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error generating question');
      setPhase('idle');
    }
  };

  const handleEvaluate = async () => {
    if (!currentQuestion || !answer.trim()) return;
    setPhase('evaluating');
    setError(null);
    try {
      const res = await evaluateAnswer({
        questionId: currentQuestion.questionId,
        answer: answer.trim(),
      });
      setEvaluation(res);

      // Update score
      const r = res.result.toUpperCase();
      setScore((prev) => ({
        correct: prev.correct + (r === 'CORRECTA' ? 1 : 0),
        partial: prev.partial + (r === 'PARCIAL' ? 1 : 0),
        incorrect: prev.incorrect + (r === 'INCORRECTA' ? 1 : 0),
      }));

      setHistory((prev) => [
        ...prev,
        {
          question: currentQuestion.question,
          answer: answer.trim(),
          result: res.result,
          explanation: res.explanation,
        },
      ]);
      setPhase('result');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error evaluating answer');
      setPhase('answering');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleEvaluate();
    }
  };

  const resultColor = (result: string) => {
    const r = result.toUpperCase();
    if (r === 'CORRECTA') return 'text-green-400';
    if (r === 'PARCIAL') return 'text-yellow-400';
    return 'text-red-400';
  };

  const resultBg = (result: string) => {
    const r = result.toUpperCase();
    if (r === 'CORRECTA') return 'border-green-500/40 bg-green-900/20';
    if (r === 'PARCIAL') return 'border-yellow-500/40 bg-yellow-900/20';
    return 'border-red-500/40 bg-red-900/20';
  };

  const resultIcon = (result: string) => {
    const r = result.toUpperCase();
    if (r === 'CORRECTA')
      return (
        <svg
          className="w-6 h-6 text-green-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
        </svg>
      );
    if (r === 'PARCIAL')
      return (
        <svg
          className="w-6 h-6 text-yellow-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01" />
        </svg>
      );
    return (
      <svg className="w-6 h-6 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M6 18L18 6M6 6l12 12"
        />
      </svg>
    );
  };

  // const total = score.correct + score.partial + score.incorrect;

  return (
    <div className="flex flex-col h-full">
      {/* Score bar */}
      {/* {total > 0 && (
        <div className="flex items-center gap-4 px-6 py-3 bg-gray-800/50 border-b border-gray-700 text-sm">
          <span className="text-gray-400 font-medium">Score:</span>
          <span className="text-green-400 font-semibold">{score.correct} Correct</span>
          <span className="text-yellow-400 font-semibold">{score.partial} Partial</span>
          <span className="text-red-400 font-semibold">{score.incorrect} Incorrect</span>
          <span className="text-gray-500 ml-auto">{total} total</span>
        </div>
      )} */}

      {/* Main scrollable area */}
      <div className="flex-1 overflow-y-auto px-6 py-6 space-y-6">
        {/* History */}
        {history.map((entry, i) => (
          <div key={i} className={`rounded-xl border p-4 space-y-3 ${resultBg(entry.result)}`}>
            <div className="flex items-start gap-2">
              {resultIcon(entry.result)}
              <div className="flex-1">
                <div className="text-gray-300 text-sm font-medium mb-1">Question {i + 1}</div>
                <div className="text-white">
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>{entry.question}</ReactMarkdown>
                </div>
              </div>
              <span
                className={`text-xs font-bold px-2 py-1 rounded ${resultColor(entry.result)} bg-gray-800/50`}
              >
                {entry.result}
              </span>
            </div>
            <div className="pl-8">
              <div className="text-gray-400 text-xs font-medium mb-1">Your answer:</div>
              <div className="text-gray-200 text-sm bg-gray-800/40 rounded-lg px-3 py-2">
                {entry.answer}
              </div>
            </div>
            <div className="pl-8">
              <div className="text-gray-400 text-xs font-medium mb-1">Explanation:</div>
              <div className="text-gray-300 text-sm">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>{entry.explanation}</ReactMarkdown>
              </div>
            </div>
          </div>
        ))}

        {phase === 'idle' && (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <svg
              className="w-16 h-16 mb-4 text-gray-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
            <h2 className="text-xl font-semibold text-gray-300 mb-2">Questions Mode</h2>
            <p className="text-center max-w-md mb-6">
              Test your knowledge! Generate questions based on your knowledge base and get instant
              feedback on your answers.
            </p>
            <button
              onClick={handleGenerate}
              className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors duration-200 flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                />
              </svg>
              Generate Question
            </button>
          </div>
        )}

        {phase === 'loading-question' && (
          <div className="flex flex-col items-center justify-center py-12 text-gray-400">
            <svg
              className="animate-spin h-10 w-10 mb-4 text-blue-500"
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
            <p className="text-gray-300 font-medium">Generating question...</p>
            <p className="text-sm text-gray-500 mt-1">Analyzing your knowledge base</p>
          </div>
        )}

        {(phase === 'answering' || phase === 'evaluating') && currentQuestion && (
          <div className="max-w-3xl mx-auto space-y-4">
            <div className="bg-gray-800/60 rounded-xl border border-gray-700 p-5">
              <div className="flex items-center gap-2 mb-3">
                <svg
                  className="w-5 h-5 text-blue-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span className="text-blue-400 text-sm font-semibold">
                  Question {history.length + 1}
                </span>
              </div>
              <div className="text-white text-lg leading-relaxed">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                  {currentQuestion.question}
                </ReactMarkdown>
              </div>
            </div>
          </div>
        )}

        {phase === 'result' && evaluation && (
          <div className="max-w-3xl mx-auto">
            <div className={`rounded-xl border p-5 space-y-4 ${resultBg(evaluation.result)}`}>
              <div className="flex items-center gap-3">
                {resultIcon(evaluation.result)}
                <span className={`text-2xl font-bold ${resultColor(evaluation.result)}`}>
                  {evaluation.result}
                </span>
              </div>
              <div className="text-gray-300">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>{evaluation.explanation}</ReactMarkdown>
              </div>
            </div>
            <div className="flex justify-center mt-6">
              <button
                onClick={handleGenerate}
                className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors duration-200 flex items-center gap-2"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  />
                </svg>
                Next Question
              </button>
            </div>
          </div>
        )}

        {error && (
          <div className="max-w-3xl mx-auto bg-red-900/30 border border-red-500/40 rounded-xl p-4 text-red-300 text-sm">
            {error}
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      {/* Input area (only visible when answering) */}
      {(phase === 'answering' || phase === 'evaluating') && (
        <div className="bg-primary-dark px-5 py-4">
          <div className="relative max-w-4xl mx-auto flex items-center">
            <textarea
              ref={inputRef}
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Write your answer... (Enter to submit, Shift+Enter for new line)"
              disabled={phase === 'evaluating'}
              rows={2}
              className="w-full bg-message-bot-dark text-white placeholder-gray-400 rounded-lg pl-4 pr-14 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed resize-none min-h-20 max-h-48"
              style={{ scrollbarWidth: 'thin' }}
            />
            <button
              onClick={handleEvaluate}
              disabled={!answer.trim() || phase === 'evaluating'}
              className="absolute right-3 p-2 bg-green-600 hover:bg-green-700 disabled:bg-button-disabled-dark disabled:cursor-not-allowed text-white rounded-full transition-colors duration-200 flex items-center justify-center"
              title="Submit answer"
            >
              {phase === 'evaluating' ? (
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
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
