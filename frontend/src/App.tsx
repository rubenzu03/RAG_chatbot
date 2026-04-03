import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ChatbotMain from './ChatbotMain';
import AuthPage from './AuthPage';
import ProtectedRoute from './ProtectedRoute';
import ProfilePage from './ProfilePage';
import { isAuthenticated } from './api';

function App() {
  return (
    <BrowserRouter>
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
    </BrowserRouter>
  );
}

export default App;
