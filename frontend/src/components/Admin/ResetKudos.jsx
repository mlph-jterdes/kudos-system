import API from '../../services/api';

export default function ResetKudos() {
  const reset = async () => {
    try {
      await API.post('/admin/reset-kudos');
      alert('All kudos counts reset!');
    } catch (err) {
      alert(err.response?.data?.error || 'Reset failed');
    }
  };

  return (
    <div>
      <h2>Reset Kudos</h2>
      <button onClick={reset}>Reset All</button>
    </div>
  );
}
