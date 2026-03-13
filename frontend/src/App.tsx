import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ChatbotMain from './ChatbotMain';
import AuthPage from './AuthPage';
import ProtectedRoute from './ProtectedRoute';
import { isAuthenticated } from './api';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/auth"
          element={isAuthenticated() ? <Navigate to="/chat" replace /> : <AuthPage />}
        />
        <Route
          path="/chat"
          element={
            <ProtectedRoute>
              <ChatbotMain />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to={isAuthenticated() ? '/chat' : '/auth'} replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
