import { Link } from 'react-router-dom';

export default function Navbar({ role }) {
  return (
    <nav className="bg-gray-800 text-white p-4 flex space-x-4">
      <Link to="/">Home</Link>
      {role === 'ADMIN' && <Link to="/admin">Admin Dashboard</Link>}
      {role === 'EMPLOYEE' && <Link to="/employee">Employee Dashboard</Link>}
      <button onClick={() => { localStorage.removeItem('jwtToken'); window.location.reload(); }}>
        Logout
      </button>
    </nav>
  );
}
