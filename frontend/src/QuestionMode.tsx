import { useState, useRef, useEffect } from 'react';
import {
  generateQuestion,
  evaluateAnswer,
  type QuestionResponse,
  type EvaluationResponse,
  type EvaluationResultToken,
} from './api';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import Alert from './components/ui/Alert';

type Phase = 'idle' | 'loading-question' | 'answering' | 'evaluating' | 'result';

interface HistoryEntry {
  question: string;
  answer: string;
  result: string;
  explanation: string;
}

type ResultKind = 'correct' | 'partial' | 'incorrect';

type UiLanguage = 'es' | 'en';

const uiLanguage: UiLanguage =
  typeof navigator !== 'undefined' && navigator.language.toLowerCase().startsWith('es')
    ? 'es'
    : 'en';

const uiText: Record<UiLanguage, Record<string, string>> = {
  es: {
    pageTitle: 'Chatbot Modo Preguntas',
    errorGenerate: 'Error al generar la pregunta',
    errorEvaluate: 'Error al evaluar la respuesta',
    questionLabel: 'Pregunta',
    yourAnswer: 'Tu respuesta:',
    explanation: 'Explicación:',
    questionsMode: 'Modo Preguntas',
    questionsIntro:
      'Genera preguntas basadas en tus documentos y recibe feedback inmediato sobre tus respuestas.',
    generateQuestion: 'Generar pregunta',
    generatingQuestion: 'Generando pregunta...',
    analyzingDocuments: 'Analizando tus documentos...',
    nextQuestion: 'Siguiente pregunta',
    answerPlaceholder: 'Escribe tu respuesta... (Enter para enviar, Shift+Enter para nueva línea)',
    submitAnswer: 'Enviar respuesta',
    resultCorrect: 'Correcto',
    resultPartial: 'Parcial',
    resultIncorrect: 'Incorrecto',
  },
  en: {
    pageTitle: 'Chatbot Question Mode',
    errorGenerate: 'Error generating question',
    errorEvaluate: 'Error evaluating answer',
    questionLabel: 'Question',
    yourAnswer: 'Your answer:',
    explanation: 'Explanation:',
    questionsMode: 'Questions Mode',
    questionsIntro:
      'Generate questions based on your documents and get instant feedback on your answers.',
    generateQuestion: 'Generate Question',
    generatingQuestion: 'Generating question...',
    analyzingDocuments: 'Analyzing your documents...',
    nextQuestion: 'Next Question',
    answerPlaceholder: 'Write your answer... (Enter to submit, Shift+Enter for new line)',
    submitAnswer: 'Submit answer',
    resultCorrect: 'Correct',
    resultPartial: 'Partial',
    resultIncorrect: 'Incorrect',
  },
};

function normalizeResultToken(result: string): EvaluationResultToken {
  const normalized = result
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toUpperCase();

  if (normalized.includes('INCORRECT')) return 'INCORRECT';
  if (normalized.includes('PARTIAL') || normalized.includes('PARCIAL')) return 'PARTIAL';
  if (normalized.includes('CORRECT')) return 'CORRECT';
  return 'INCORRECT';
}

function classifyResult(result: string): ResultKind {
  const normalized = normalizeResultToken(result);

  if (normalized === 'CORRECT') return 'correct';
  if (normalized === 'PARTIAL') return 'partial';
  return 'incorrect';
}

function resultLabel(result: string): string {
  const token = normalizeResultToken(result);
  if (token === 'CORRECT') return uiText[uiLanguage].resultCorrect;
  if (token === 'PARTIAL') return uiText[uiLanguage].resultPartial;
  return uiText[uiLanguage].resultIncorrect;
}

function extractExplanationText(raw: string): string {
  if (!raw) return '';
  const s = raw.trim();

  if ((s.startsWith('{') && s.endsWith('}')) || (s.startsWith('[') && s.endsWith(']'))) {
    try {
      const parsed = JSON.parse(s);
      if (typeof parsed === 'string') return parsed;
      if (Array.isArray(parsed)) {
        return parsed.filter((x) => typeof x === 'string').join(' ');
      }
      const keys = [
        'explanation',
        'explain',
        'message',
        'detail',
        'description',
        'texto',
        'explicacion',
        'explicación',
      ];
      for (const k of keys) {
        if (typeof parsed[k] === 'string' && parsed[k].trim().length > 0) return parsed[k].trim();
      }
      const parts: string[] = [];
      for (const v of Object.values(parsed)) {
        if (typeof v === 'string' && v.trim().length > 0) parts.push(v.trim());
        else if (Array.isArray(v)) parts.push(v.filter((x) => typeof x === 'string').join(' '));
      }
      if (parts.length > 0) return parts.join(' ');
      return JSON.stringify(parsed);
    } catch (e) {}
  }

  const jsonStart = s.indexOf('{');
  const jsonEnd = s.lastIndexOf('}');
  if (jsonStart >= 0 && jsonEnd > jsonStart) {
    const fragment = s.substring(jsonStart, jsonEnd + 1);
    try {
      const parsed = JSON.parse(fragment);
      return extractExplanationText(JSON.stringify(parsed));
    } catch (e) {}
  }

  return s;
}

export default function QuestionMode() {
  const [phase, setPhase] = useState<Phase>('idle');
  const [currentQuestion, setCurrentQuestion] = useState<QuestionResponse | null>(null);
  const [answer, setAnswer] = useState('');
  const [evaluation, setEvaluation] = useState<EvaluationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    document.title = uiText[uiLanguage].pageTitle;
  }, []);

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
      setError(e instanceof Error ? e.message : uiText[uiLanguage].errorGenerate);
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
      setError(e instanceof Error ? e.message : uiText[uiLanguage].errorEvaluate);
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
    const kind = classifyResult(result);
    if (kind === 'correct') return 'text-green-400';
    if (kind === 'partial') return 'text-yellow-400';
    return 'text-red-400';
  };

  const resultBg = (result: string) => {
    const kind = classifyResult(result);
    if (kind === 'correct') return 'border-green-500/40 bg-green-900/20';
    if (kind === 'partial') return 'border-yellow-500/40 bg-yellow-900/20';
    return 'border-red-500/40 bg-red-900/20';
  };

  const resultIcon = (result: string) => {
    const kind = classifyResult(result);
    if (kind === 'correct')
      return (
        <svg
          aria-hidden="true"
          focusable="false"
          className="w-6 h-6 text-green-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
        </svg>
      );
    if (kind === 'partial')
      return (
        <svg
          aria-hidden="true"
          focusable="false"
          className="w-6 h-6 text-yellow-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01" />
        </svg>
      );
    return (
      <svg
        aria-hidden="true"
        focusable="false"
        className="w-6 h-6 text-red-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M6 18L18 6M6 6l12 12"
        />
      </svg>
    );
  };

  return (
    <div className="flex flex-col h-full">
      <h1 id="questions-page-title" className="sr-only">
        {uiText[uiLanguage].questionsMode}
      </h1>
      {/* Main scrollable area */}
      <div
        id="questions-panel"
        role="region"
        aria-labelledby="questions-page-title"
        className="flex-1 overflow-y-auto px-6 py-6 space-y-6"
      >
        {/* History */}
        {history.map((entry, i) => (
          <div
            key={i}
            role="article"
            aria-labelledby={`question-${i + 1}`}
            className={`rounded-xl border p-4 space-y-3 ${resultBg(entry.result)}`}
          >
            <div className="flex items-start gap-2">
              {resultIcon(entry.result)}
              <div className="flex-1">
                <div id={`question-${i + 1}`} className="text-gray-300 text-sm font-medium mb-1">
                  {uiText[uiLanguage].questionLabel} {i + 1}
                </div>
                <div className="text-white">
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>{entry.question}</ReactMarkdown>
                </div>
              </div>
              <span
                className={`text-xs font-bold px-2 py-1 rounded ${resultColor(entry.result)} bg-gray-800/50`}
              >
                {resultLabel(entry.result)}
              </span>
            </div>
            <div className="pl-8">
              <div className="text-gray-400 text-xs font-medium mb-1">
                {uiText[uiLanguage].yourAnswer}
              </div>
              <div className="text-gray-200 text-sm bg-gray-800/40 rounded-lg px-3 py-2">
                {entry.answer}
              </div>
            </div>
            <div className="pl-8">
              <div className="text-gray-400 text-xs font-medium mb-1">
                {uiText[uiLanguage].explanation}
              </div>
              <div className="text-gray-300 text-sm">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                  {extractExplanationText(entry.explanation)}
                </ReactMarkdown>
              </div>
            </div>
          </div>
        ))}

        {phase === 'idle' && (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <svg
              aria-hidden="true"
              focusable="false"
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
            <h2 className="text-xl font-semibold text-gray-300 mb-2">
              {uiText[uiLanguage].questionsMode}
            </h2>
            <p className="text-center max-w-md mb-6">{uiText[uiLanguage].questionsIntro}</p>
            <button
              onClick={handleGenerate}
              aria-controls="current-question"
              className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors duration-200 flex items-center gap-2"
            >
              <svg
                aria-hidden="true"
                focusable="false"
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                />
              </svg>
              {uiText[uiLanguage].generateQuestion}
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
            <p className="text-gray-300 font-medium">{uiText[uiLanguage].generatingQuestion}</p>
            <p className="text-sm text-gray-500 mt-1">{uiText[uiLanguage].analyzingDocuments}</p>
          </div>
        )}

        {(phase === 'answering' || phase === 'evaluating') && currentQuestion && (
          <div className="max-w-3xl mx-auto space-y-4">
            <div
              id="current-question"
              aria-live="polite"
              className="bg-gray-800/60 rounded-xl border border-gray-700 p-5"
            >
              <div className="flex items-center gap-2 mb-3">
                <svg
                  aria-hidden="true"
                  focusable="false"
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
                  {uiText[uiLanguage].questionLabel} {history.length + 1}
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
            <div
              id="result-panel"
              role="status"
              aria-live="polite"
              aria-atomic="true"
              className={`rounded-xl border p-5 space-y-4 ${resultBg(evaluation.result)}`}
            >
              <div className="flex items-center gap-3">
                {resultIcon(evaluation.result)}
                <span className={`text-2xl font-bold ${resultColor(evaluation.result)}`}>
                  {resultLabel(evaluation.result)}
                </span>
              </div>
              <div className="text-gray-300">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                  {extractExplanationText(evaluation.explanation)}
                </ReactMarkdown>
              </div>
            </div>
            <div className="flex justify-center mt-6">
              <button
                onClick={handleGenerate}
                aria-controls="current-question"
                className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors duration-200 flex items-center gap-2"
              >
                <svg
                  aria-hidden="true"
                  focusable="false"
                  className="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  />
                </svg>
                {uiText[uiLanguage].nextQuestion}
              </button>
            </div>
          </div>
        )}

        {error && (
          <Alert variant="error" id="questions-error" className="max-w-3xl mx-auto mb-4">
            {error}
          </Alert>
        )}

        <div ref={bottomRef} />
      </div>

      {(phase === 'answering' || phase === 'evaluating') && (
        <div className="bg-primary-dark px-5 py-4">
          <div className="relative max-w-4xl mx-auto flex items-center">
            <textarea
              ref={inputRef}
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={uiText[uiLanguage].answerPlaceholder}
              aria-label={uiText[uiLanguage].yourAnswer}
              disabled={phase === 'evaluating'}
              rows={2}
              className="w-full bg-message-bot-dark text-white placeholder-gray-400 rounded-lg pl-4 pr-14 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed resize-none min-h-20 max-h-48"
              style={{ scrollbarWidth: 'thin' }}
            />
            <button
              onClick={handleEvaluate}
              aria-label={uiText[uiLanguage].submitAnswer}
              disabled={!answer.trim() || phase === 'evaluating'}
              className="absolute right-3 p-2 bg-green-600 hover:bg-green-700 disabled:bg-button-disabled-dark disabled:cursor-not-allowed text-white rounded-full transition-colors duration-200 flex items-center justify-center"
              title={uiText[uiLanguage].submitAnswer}
            >
              {phase === 'evaluating' ? (
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
                <svg
                  aria-hidden="true"
                  focusable="false"
                  className="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
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
