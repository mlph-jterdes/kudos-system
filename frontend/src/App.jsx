import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Shared/Navbar';
import AdminDashboard from './pages/AdminDashboard';
import EmployeeDashboard from './pages/EmployeeDashboard';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';
import PublicLeaderboard from "./pages/PublicLeaderboard";

function App() {
  const role = localStorage.getItem('role'); // "ADMIN" or "EMPLOYEE"

  return (
    <Router>
      <Navbar role={role} />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employee"
          element={
            <ProtectedRoute allowedRole="EMPLOYEE">
              <EmployeeDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/leaderboard" element={<PublicLeaderboard />} />
        <Route path="*" element={<div>Page Not Found</div>} />
      </Routes>
    </Router>
  );
}

export default App;
