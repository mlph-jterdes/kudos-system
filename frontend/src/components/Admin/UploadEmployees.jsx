import { useState } from 'react';
import API from '../../services/api';

export default function UploadEmployees() {
  const [file, setFile] = useState(null);
  const [messages, setMessages] = useState([]);

  const upload = async () => {
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await API.post('/admin/upload-employees', formData);
      setMessages(res.data);
    } catch (err) {
      setMessages([err.response?.data?.error || 'Upload failed']);
    }
  };

  return (
    <div>
      <h2>Upload Employees CSV</h2>
      <input type="file" onChange={e => setFile(e.target.files[0])} />
      <button onClick={upload}>Upload</button>
      {messages.map((msg, i) => <p key={i}>{msg}</p>)}
    </div>
  );
}
