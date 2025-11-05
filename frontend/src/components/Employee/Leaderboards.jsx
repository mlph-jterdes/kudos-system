import { useEffect, useState } from 'react';
import API from '../../services/api';

export default function Leaderboards() {
  const [topEmployees, setTopEmployees] = useState([]);
  const [topTeams, setTopTeams] = useState([]);

  useEffect(() => {
    API.get('/kudos/leaderboard/employees').then(res => setTopEmployees(res.data));
    API.get('/kudos/leaderboard/teams').then(res => setTopTeams(res.data));
  }, []);

  return (
    <div>
      <h2>Top Employees</h2>
      <ul>{topEmployees.map(e => <li key={e.employee}>{e.employee} ({e.kudosCount})</li>)}</ul>

      <h2>Top Teams</h2>
      <ul>{topTeams.map(t => <li key={t.team}>{t.team} ({t.kudosCount})</li>)}</ul>
    </div>
  );
}
