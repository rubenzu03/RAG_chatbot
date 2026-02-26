import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login, register } from './api';

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

  const handleAction = async () => {
    setError('');
    setSuccess('');

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
      <div className="w-full max-w-md bg-[#2f2f2f] rounded-2xl shadow-lg p-8">
        {/* Tabs */}
        <div className="flex mb-6 border-b border-[#3a3a3a]">
          <button
            onClick={() => {
              setMode('login');
              setError('');
              setSuccess('');
            }}
            className={`flex-1 pb-3 text-center font-semibold transition-colors cursor-pointer ${
              mode === 'login'
                ? 'text-message-user-dark border-b-2 border-message-user-dark'
                : 'text-gray-400 hover:text-gray-300'
            }`}
          >
            Login
          </button>
          <button
            onClick={() => {
              setMode('register');
              setError('');
              setSuccess('');
            }}
            className={`flex-1 pb-3 text-center font-semibold transition-colors cursor-pointer ${
              mode === 'register'
                ? 'text-message-user-dark border-b-2 border-message-user-dark'
                : 'text-gray-400 hover:text-gray-300'
            }`}
          >
            Register
          </button>
        </div>

        {}
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-500/20 text-red-300 text-sm">
            Login failed
          </div>
        )}
        {success && (
          <div className="mb-4 p-3 rounded-lg bg-green-500/20 text-green-300 text-sm">
            {success}
          </div>
        )}

        <form action={handleAction} className="flex flex-col gap-4">
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
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
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

          <button
            type="submit"
            disabled={loading}
            className="mt-2 w-full py-2.5 rounded-lg bg-message-user-dark text-white font-semibold hover:opacity-90 transition-opacity disabled:opacity-50 cursor-pointer disabled:cursor-not-allowed"
          >
            {loading ? 'Loading...' : mode === 'login' ? 'Log in' : 'Sign up'}
          </button>
        </form>
      </div>
    </div>
  );
}
