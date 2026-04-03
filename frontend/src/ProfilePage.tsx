import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { clearChatHistory, deleteAccount, getCurrentUserEmail, logout } from './api';

export default function ProfilePage() {
  const navigate = useNavigate();
  const currentUserEmail = useMemo(() => getCurrentUserEmail(), []);
  const [busyAction, setBusyAction] = useState<'clear' | 'delete' | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleBackToChat = () => {
    navigate('/chat');
  };

  const handleClearHistory = async () => {
    setError(null);
    setSuccess(null);
    setBusyAction('clear');

    const ok = await clearChatHistory();
    if (ok) {
      setSuccess('Chat history deleted successfully.');
    } else {
      setError('Could not delete chat history.');
    }

    setBusyAction(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/auth', { replace: true });
  };

  const handleDeleteAccount = async () => {
    const confirmed = window.confirm(
      'This action will permanently delete your account. Do you want to continue?'
    );

    if (!confirmed) return;

    setError(null);
    setSuccess(null);
    setBusyAction('delete');

    try {
      await deleteAccount();
      navigate('/auth', { replace: true });
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not delete account.');
    } finally {
      setBusyAction(null);
    }
  };

  return (
    <div className="min-h-screen bg-primary-dark px-4 py-10 text-gray-100">
      <div className="mx-auto w-full max-w-2xl rounded-2xl border border-gray-700 bg-[#2f2f2f] p-6 shadow-lg">
        <div className="mb-6 flex items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-semibold">Profile Management</h1>
            <p className="mt-1 text-sm text-gray-400">Manage your session and account actions.</p>
          </div>
          <button
            onClick={handleBackToChat}
            className="rounded-lg bg-gray-700 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-600"
          >
            Back to chat
          </button>
        </div>

        <div className="mb-6 rounded-xl border border-gray-700 bg-[#1f1f1f] p-4">
          <p className="text-xs uppercase tracking-wide text-gray-400">Current user</p>
          <p className="mt-2 break-all text-base font-medium text-gray-100">
            {currentUserEmail ?? 'Unknown user'}
          </p>
        </div>

        {error && (
          <div className="mb-4 rounded-lg bg-red-500/20 p-3 text-sm text-red-300">{error}</div>
        )}
        {success && (
          <div className="mb-4 rounded-lg bg-green-500/20 p-3 text-sm text-green-300">
            {success}
          </div>
        )}

        <div className="space-y-3">
          <button
            onClick={handleClearHistory}
            disabled={busyAction !== null}
            className="w-full rounded-lg bg-blue-600 px-4 py-3 text-left font-semibold text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busyAction === 'clear' ? 'Deleting chat history...' : 'Delete Data'}
          </button>

          <button
            onClick={handleLogout}
            disabled={busyAction !== null}
            className="w-full rounded-lg bg-gray-600 px-4 py-3 text-left font-semibold text-white transition-colors hover:bg-gray-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Logout
          </button>

          <button
            onClick={handleDeleteAccount}
            disabled={busyAction !== null}
            className="w-full rounded-lg bg-red-600 px-4 py-3 text-left font-semibold text-white transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busyAction === 'delete' ? 'Deleting account...' : 'Delete Account'}
          </button>
        </div>

        <p className="mt-4 text-xs text-gray-500">
          Deleting your account is permanent and cannot be undone.
        </p>
      </div>
    </div>
  );
}
