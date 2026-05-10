import { Navigate } from 'react-router-dom';
import { isAuthenticated, isTokenExpired, removeToken } from './api';

interface Props {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: Props) {
  if (!isAuthenticated()) {
    if (isTokenExpired()) {
      removeToken();
    }
    return <Navigate to="/auth" replace />;
  }
  
  return <>{children}</>;
}

