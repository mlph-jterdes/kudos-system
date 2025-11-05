import { Navigate } from 'react-router-dom';

export default function ProtectedRoute({ children, allowedRole }) {
  const token = localStorage.getItem('jwtToken');
  const role = localStorage.getItem('role');

  if (!token || role !== allowedRole) {
    return <Navigate to="/" replace />;
  }

  return children;
}
