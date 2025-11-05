import { useState } from 'react';
import API from '../../services/api';

export default function SendKudos() {
  const [message, setMessage] = useState('');
  const [recipientId, setRecipientId] = useState('');
  const [feedback, setFeedback] = useState('');

  const send = async () => {
    try {
      const res = await API.post('/kudos/send', null, { params: { message, recipientEmployeeId: recipientId } });
      setFeedback(res.data.message);
    } catch (err) {
      setFeedback(err.response?.data?.error || 'Error sending kudos');
    }
  };

  return (
    <div>
      <h2>Send Kudos</h2>
      <input placeholder="Recipient ID" value={recipientId} onChange={e => setRecipientId(e.target.value)} />
      <textarea placeholder="Message" value={message} onChange={e => setMessage(e.target.value)} />
      <button onClick={send}>Send</button>
      {feedback && <p>{feedback}</p>}
    </div>
  );
}
