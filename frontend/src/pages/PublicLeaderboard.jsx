import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  TextField,
  MenuItem,
  CircularProgress,
} from "@mui/material";

const PublicLeaderboard = () => {
  const [employeesLeaderboard, setEmployeesLeaderboard] = useState([]);
  const [teamsLeaderboard, setTeamsLeaderboard] = useState([]);
  const [department, setDepartment] = useState("");
  const [loading, setLoading] = useState(true);
  const departments = ["Sales", "HR", "IT", "Marketing", "Finance"]; // customize

  const fetchLeaderboard = async () => {
    setLoading(true);
    try {
      const empRes = await fetch(
        `http://localhost:8080/api/kudos/leaderboard/employees${
          department ? `?department=${department}` : ""
        }`
      );
      const teamRes = await fetch(
        `http://localhost:8080/api/kudos/leaderboard/teams${
          department ? `?department=${department}` : ""
        }`
      );

      if (empRes.ok && teamRes.ok) {
        setEmployeesLeaderboard(await empRes.json());
        setTeamsLeaderboard(await teamRes.json());
      }
    } catch (err) {
      console.error("Leaderboard fetch failed:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLeaderboard();
  }, [department]);

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="80vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box p={4} maxWidth="1000px" mx="auto">
      <Typography variant="h4" fontWeight="bold" gutterBottom align="center">
        🏆 Kudos Leaderboard
      </Typography>
      <Typography color="textSecondary" align="center" mb={4}>
        Top employees and teams this month
      </Typography>

      {/* Department Filter */}
      <Box display="flex" justifyContent="center" mb={4}>
        <TextField
          select
          label="Filter by Department"
          value={department}
          onChange={(e) => setDepartment(e.target.value)}
          sx={{ width: 300 }}
        >
          <MenuItem value="">All Departments</MenuItem>
          {departments.map((d) => (
            <MenuItem key={d} value={d}>
              {d}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <Box display="flex" flexDirection={{ xs: "column", md: "row" }} gap={4}>
        {/* Employees */}
        <Box flex={1}>
          <Typography variant="h6" gutterBottom>
            🧑‍💼 Top 5 Employees
          </Typography>
          {employeesLeaderboard.length === 0 ? (
            <Typography color="textSecondary">No data available.</Typography>
          ) : (
            employeesLeaderboard.map((emp, index) => (
              <Box
                key={index}
                display="flex"
                justifyContent="space-between"
                py={1}
                borderBottom="1px solid #eee"
              >
                <Typography>
                  {index + 1}. {emp.employee} ({emp.department})
                </Typography>
                <Typography fontWeight="bold">{emp.kudosCount} ⭐</Typography>
              </Box>
            ))
          )}
        </Box>

        {/* Teams */}
        <Box flex={1}>
          <Typography variant="h6" gutterBottom>
            👥 Top 5 Teams
          </Typography>
          {teamsLeaderboard.length === 0 ? (
            <Typography color="textSecondary">No data available.</Typography>
          ) : (
            teamsLeaderboard.map((team, index) => (
              <Box
                key={index}
                display="flex"
                justifyContent="space-between"
                py={1}
                borderBottom="1px solid #eee"
              >
                <Typography>
                  {index + 1}. {team.team}
                </Typography>
                <Typography fontWeight="bold">{team.kudosCount} ⭐</Typography>
              </Box>
            ))
          )}
        </Box>
      </Box>
    </Box>
  );
};

export default PublicLeaderboard;
