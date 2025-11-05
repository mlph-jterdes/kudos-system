import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Shared/Navbar';
import AdminDashboard from './pages/AdminDashboard';
import EmployeeDashboard from './pages/EmployeeDashboard';
import PublicLeaderboard from "./pages/PublicLeaderboard";

function App() {
  const role = localStorage.getItem('role'); // "ADMIN" or "EMPLOYEE"

  return (
    <Router>
      <Navbar role={role} />
      <Routes>
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/employee" element={<EmployeeDashboard />} />
        <Route path="/leaderboard" element={<PublicLeaderboard />} />
        <Route path="*" element={<div>Home Page</div>} />
      </Routes>
    </Router>
  );
}

export default App;
