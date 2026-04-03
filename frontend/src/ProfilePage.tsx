import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { clearChatHistory, deleteAccount, getCurrentUserEmail, logout } from './api';
import Alert from './components/ui/Alert';
import Button from './components/ui/Button';
import Card from './components/ui/Card';

export default function ProfilePage() {
  const navigate = useNavigate();
  const currentUserEmail = getCurrentUserEmail();
  const [busyAction, setBusyAction] = useState<'clear' | 'delete' | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const resetFeedback = () => {
    setError(null);
    setSuccess(null);
  };

  const runBusyAction = async (action: 'clear' | 'delete', work: () => Promise<void>) => {
    resetFeedback();
    setBusyAction(action);
    try {
      await work();
    } finally {
      setBusyAction(null);
    }
  };

  const handleBackToChat = () => {
    navigate('/chat');
  };

  const handleClearHistory = async () => {
    await runBusyAction('clear', async () => {
      const ok = await clearChatHistory();
      if (ok) {
        setSuccess('Chat history deleted successfully.');
      } else {
        setError('Could not delete chat history.');
      }
    });
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

    await runBusyAction('delete', async () => {
      try {
        await deleteAccount();
        navigate('/auth', { replace: true });
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Could not delete account.');
      }
    });
  };

  return (
    <div className="min-h-screen bg-primary-dark px-4 py-10 text-gray-100">
      <Card className="mx-auto w-full max-w-2xl p-6">
        <div className="mb-6 flex items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-semibold">Profile Management</h1>
            <p className="mt-1 text-sm text-gray-400">Manage your session and account actions.</p>
          </div>
          <Button onClick={handleBackToChat} variant="secondary" className="text-sm font-medium">
            Back to chat
          </Button>
        </div>

        <Card className="mb-6 rounded-xl bg-[#1f1f1f] p-4 shadow-none">
          <p className="text-xs uppercase tracking-wide text-gray-400">Current user</p>
          <p className="mt-2 break-all text-base font-medium text-gray-100">
            {currentUserEmail ?? 'Unknown user'}
          </p>
        </Card>

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

        <div className="space-y-3">
          <Button
            onClick={handleClearHistory}
            disabled={busyAction !== null}
            variant="primary"
            fullWidth
            align="left"
            className="py-3"
          >
            {busyAction === 'clear' ? 'Deleting chat history...' : 'Delete Data'}
          </Button>

          <Button
            onClick={handleLogout}
            disabled={busyAction !== null}
            variant="secondary"
            fullWidth
            align="left"
            className="py-3"
          >
            Logout
          </Button>

          <Button
            onClick={handleDeleteAccount}
            disabled={busyAction !== null}
            variant="danger"
            fullWidth
            align="left"
            className="py-3"
          >
            {busyAction === 'delete' ? 'Deleting account...' : 'Delete Account'}
          </Button>
        </div>

        <p className="mt-4 text-xs text-gray-500">
          Deleting your account is permanent and cannot be undone.
        </p>
      </Card>
    </div>
  );
}
