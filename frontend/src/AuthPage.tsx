import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { isAuthenticated, login, register } from './api';
import Alert from './components/ui/Alert';
import Button from './components/ui/Button';
import Card from './components/ui/Card';

type AuthMode = 'login' | 'register';

export default function AuthPage() {
  const [mode, setMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const isLogin = mode === 'login';

  useEffect(() => {
    document.title = 'AI-Powered Study Assistant Login / Register';
  }, []);

  useEffect(() => {
    if (isAuthenticated()) {
      navigate('/chat', { replace: true });
    }
  }, [navigate]);

  const resetFeedback = () => {
    setError('');
    setSuccess('');
  };

  const switchMode = (nextMode: AuthMode) => {
    setMode(nextMode);
    resetFeedback();
  };

  const handleAction = async () => {
    resetFeedback();

    if (!email || !password) {
      setError('Please fill in all required fields.');
      return;
    }

    if (mode === 'register' && password !== confirmPassword) {
      setError("Passwords don't match.");
      return;
    }

    setLoading(true);
    try {
      if (mode === 'login') {
        await login(email, password);
        navigate('/chat');
      } else {
        await register(email, password);
        setSuccess('Registration successful! You can now log in.');
        setMode('login');
        setPassword('');
        setConfirmPassword('');
      }
    } catch (err: unknown) {
      const e = err as { response?: { data?: unknown }; message?: string };
      const msg = e?.response?.data ?? e?.message ?? 'An error occurred.';
      setError(typeof msg === 'string' ? msg : JSON.stringify(msg));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-primary-dark">
      <Card className="w-full max-w-md p-8">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-semibold text-gray-200">Login or signup to AI-Powered Study Assistant</h1>
        </div>
        <div className="flex mb-6 border-b border-[#898989]">
          <button
            onClick={() => switchMode('login')}
            className={`flex-1 pb-3 text-center font-semibold transition-colors cursor-pointer ${
              isLogin
                ? 'text-message-user-dark border-b-2 border-message-user-dark'
                : 'text-gray-400 hover:text-gray-300'
            }`}
          >
            Login
          </button>
          <button
            onClick={() => switchMode('register')}
            className={`flex-1 pb-3 text-center font-semibold transition-colors cursor-pointer ${
              !isLogin
                ? 'text-message-user-dark border-b-2 border-message-user-dark'
                : 'text-gray-400 hover:text-gray-300'
            }`}
          >
            Register
          </button>
        </div>

        {error && (
          <Alert variant="error" className="mb-4">
            {error}
          </Alert>
        )}
        {success && (
          <Alert variant="success" className="mb-4">
            {success}
          </Alert>
        )}

        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleAction();
          }}
          className="flex flex-col gap-4"
        >
          <div>
            <label className="block text-sm text-gray-300 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2.5 rounded-lg bg-[#1e1e1e] text-gray-200 border border-[#3a3a3a] focus:outline-none focus:border-message-user-dark transition-colors"
              placeholder="yourmail@email.com"
              autoComplete="email"
            />
          </div>

          <div>
            <label className="block text-sm text-gray-300 mb-1">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2.5 rounded-lg bg-[#1e1e1e] text-gray-200 border border-[#3a3a3a] focus:outline-none focus:border-message-user-dark transition-colors"
              placeholder="••••••••"
              autoComplete={isLogin ? 'current-password' : 'new-password'}
            />
          </div>

          {mode === 'register' && (
            <div>
              <label className="block text-sm text-gray-300 mb-1">Confirm Password</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full px-4 py-2.5 rounded-lg bg-[#1e1e1e] text-gray-200 border border-[#3a3a3a] focus:outline-none focus:border-message-user-dark transition-colors"
                placeholder="••••••••"
                autoComplete="new-password"
              />
            </div>
          )}

          <Button
            type="submit"
            disabled={loading}
            variant="brand"
            fullWidth
            className="mt-2"
          >
            {loading ? 'Loading...' : isLogin ? 'Log in' : 'Sign up'}
          </Button>
        </form>
      </Card>
    </div>
  );
}
