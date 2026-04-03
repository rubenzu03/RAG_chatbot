import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import PageLoader from './components/ui/PageLoader';
import { isAuthenticated } from './api';

const AuthPage = lazy(() => import('./AuthPage'));
const ChatbotMain = lazy(() => import('./ChatbotMain'));
const ProfilePage = lazy(() => import('./ProfilePage'));

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoader message="Loading page..." />}>
        <Routes>
          <Route path="/auth" element={<AuthPage />} />
          <Route path="/" element={<Navigate to={isAuthenticated() ? '/chat' : '/auth'} replace />} />
          <Route
            path="/chat"
            element={
              <ProtectedRoute>
                <ChatbotMain />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default App;
