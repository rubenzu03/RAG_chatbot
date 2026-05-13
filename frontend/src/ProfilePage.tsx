import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { clearChatHistory, deleteAccount, getCurrentUserEmail, logout } from './api';
import Alert from './components/ui/Alert';
import Button from './components/ui/Button';
import Card from './components/ui/Card';

const CHAT_STATE_STORAGE_PREFIX = 'rag_chatbot_active_state';

function getChatStateStorageKey(): string {
  const currentUserEmail = getCurrentUserEmail();
  return currentUserEmail
    ? `${CHAT_STATE_STORAGE_PREFIX}:${currentUserEmail}`
    : CHAT_STATE_STORAGE_PREFIX;
}

function clearLocalChatState(): void {
  localStorage.removeItem(getChatStateStorageKey());
}

export default function ProfilePage() {
  const navigate = useNavigate();
  const currentUserEmail = getCurrentUserEmail();
  const [busyAction, setBusyAction] = useState<'clear' | 'delete' | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    document.title = 'Profile Management' + (currentUserEmail ? ` - ${currentUserEmail}` : '');
  }, [currentUserEmail]);

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
        clearLocalChatState();
        setSuccess('Chat history deleted successfully.');
      } else {
        setError('Could not delete chat history.');
      }
    });
  };

  const handleLogout = () => {
    clearLocalChatState();
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
        clearLocalChatState();
        navigate('/auth', { replace: true });
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Could not delete account.');
      }
    });
  };

  return (
    <main
      aria-labelledby="profile-heading"
      className="min-h-screen flex items-center justify-center bg-primary-dark px-4 py-10 text-gray-100"
    >
      <Card
        role="region"
        aria-labelledby="profile-heading"
        aria-busy={busyAction !== null}
        className="mx-auto w-full max-w-2xl p-6"
      >
        <div className="mb-6 flex items-start justify-between gap-4">
          <div>
            <h1 id="profile-heading" className="text-2xl font-semibold">
              Profile Management
            </h1>
            <p className="mt-1 text-sm text-gray-400">Manage your session and account actions.</p>
          </div>
          <Button
            onClick={handleBackToChat}
            variant="secondary"
            className="text-sm font-medium px-3 py-1.5"
          >
            Back to chat
          </Button>
        </div>
        <Card
          role="region"
          aria-labelledby="current-user-label"
          className="mb-6 rounded-xl bg-[#1f1f1f] p-4 shadow-none flex flex-col items-center text-center"
        >
          <p id="current-user-label" className="text-xs uppercase tracking-wide text-gray-400">
            Current user
          </p>
          <p
            aria-label="current user email"
            className="mt-2 text-sm font-medium text-gray-100 text-center max-w-xs truncate"
          >
            {currentUserEmail ?? 'Unknown user'}
          </p>
        </Card>

        {error && (
          <Alert variant="error" id="profile-error" className="mb-4">
            {error}
          </Alert>
        )}
        {success && (
          <Alert variant="success" id="profile-success" className="mb-4">
            {success}
          </Alert>
        )}

        <div
          role="group"
          aria-label="Account actions"
          className="flex flex-col items-center sm:flex-row sm:justify-center sm:items-center gap-2"
        >
          <Button
            onClick={handleClearHistory}
            disabled={busyAction !== null}
            variant="primary"
            className="px-3 py-1.5 text-sm"
          >
            {busyAction === 'clear' ? 'Deleting chat history...' : 'Delete Data'}
          </Button>

          <Button
            onClick={handleLogout}
            disabled={busyAction !== null}
            variant="secondary"
            className="px-3 py-1.5 text-sm"
          >
            Logout
          </Button>

          <Button
            onClick={handleDeleteAccount}
            disabled={busyAction !== null}
            variant="danger"
            className="px-3 py-1.5 text-sm"
            aria-describedby="delete-warning"
          >
            {busyAction === 'delete' ? 'Deleting account...' : 'Delete Account'}
          </Button>
        </div>

        <p id="delete-warning" className="mt-4 text-xs text-gray-500">
          Deleting your account is permanent and cannot be undone.
        </p>
      </Card>
    </main>
  );
}
