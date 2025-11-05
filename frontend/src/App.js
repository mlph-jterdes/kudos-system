import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import AdminPage from './pages/AdminDashboard';
import EmployeePage from './pages/EmployeeDashboard';
import PublicLeaderboard from "./pages/PublicLeaderboard";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/employee" element={<EmployeePage />} />
        <Route path="/leaderboard" element={<PublicLeaderboard />} />
      </Routes>
    </Router>
  );
}

export default App;
