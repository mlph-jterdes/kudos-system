import { useState } from 'react';
import API from '../../services/api';

export default function CreateAdmin() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const create = async () => {
    try {
      const res = await API.post('/admin/create-admin', null, { params: { email, password } });
      alert(`Admin created: ${res.data.email}`);
    } catch (err) {
      alert(err.response?.data?.error || 'Creation failed');
    }
  };

  return (
    <div>
      <h2>Create Admin</h2>
      <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
      <input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} />
      <button onClick={create}>Create Admin</button>
    </div>
  );
}
