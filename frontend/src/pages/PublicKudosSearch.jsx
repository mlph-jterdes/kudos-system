import React, { useState, useEffect } from "react";
import {
  Container,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Typography,
  Box,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  Switch,
  FormControlLabel,
  CircularProgress,
} from "@mui/material";

const PublicKudosSearch = () => {
  const [employees, setEmployees] = useState([]);
  const [teams, setTeams] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState("");
  const [selectedTeam, setSelectedTeam] = useState("");
  const [period, setPeriod] = useState("all");
  const [showAll, setShowAll] = useState(false);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // Fetch employees and teams once
  useEffect(() => {
    const fetchOptions = async () => {
      try {
        const empRes = await fetch("http://localhost:8080/api/employee/all");
        const teamRes = await fetch("http://localhost:8080/api/employee/teams");
        setEmployees(await empRes.json());
        setTeams(await teamRes.json());
      } catch (err) {
        console.error(err);
        setError("Failed to load employees or teams");
      }
    };
    fetchOptions();
  }, []);

  // Fetch history whenever selection/filters change
  useEffect(() => {
    const fetchHistory = async () => {
      if (!selectedEmployee && !selectedTeam) {
        setResults(null);
        return;
      }

      setLoading(true);
      setError("");

      try {
        const queryParam = selectedEmployee
          ? `employeeId=${selectedEmployee}`
          : `teamId=${selectedTeam}`;

        const res = await fetch(
          `http://localhost:8080/api/kudos/public/history?${queryParam}&period=${period}&showAllMessages=${showAll}`
        );

        if (!res.ok) throw new Error("Failed to fetch results");

        const data = await res.json();
        console.log("SEARCHED", data);
        setResults(data || null);
      } catch (err) {
        console.error(err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [selectedEmployee, selectedTeam, period, showAll]);

  return (
    <Container maxWidth="md" sx={{ mt: 4, display: "flex", gap: 4 }}>
      {/* Side panel */}
      <Box sx={{ flex: 1, display: "flex", flexDirection: "column", gap: 2 }}>
        <Typography variant="h5">Select Employee or Team</Typography>

        <FormControl fullWidth>
          <InputLabel>Employee</InputLabel>
          <Select
            value={selectedEmployee}
            label="Employee"
            onChange={(e) => {
              setSelectedEmployee(e.target.value);
              setSelectedTeam("");
            }}
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>
            {employees.map((emp) => (
              <MenuItem key={emp.id} value={emp.id}>
                {emp.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth>
          <InputLabel>Team</InputLabel>
          <Select
            value={selectedTeam}
            label="Team"
            onChange={(e) => {
              setSelectedTeam(e.target.value);
              setSelectedEmployee("");
            }}
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>
            {teams.map((team) => (
              <MenuItem key={team.id} value={team.id}>
                {team.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth>
          <InputLabel>Period</InputLabel>
          <Select
            value={period}
            label="Period"
            onChange={(e) => setPeriod(e.target.value)}
          >
            <MenuItem value="week">Past Week</MenuItem>
            <MenuItem value="month">Past Month</MenuItem>
            <MenuItem value="all">All</MenuItem>
          </Select>
        </FormControl>

        <FormControlLabel
          control={
            <Switch
              checked={showAll}
              onChange={(e) => setShowAll(e.target.checked)}
            />
          }
          label="Show All Messages"
        />
      </Box>

      {/* Result card */}
      <Box sx={{ flex: 2 }}>
        {loading && <CircularProgress />}
        {error && <Typography color="error">{error}</Typography>}

        {results && (
          <Card sx={{ maxWidth: 600 }}>
            <CardContent>
              <Typography variant="h6">
                {results.type === "employee" ? "Employee" : "Team"}: {results.name}
              </Typography>
              <Typography variant="subtitle1">
                Total Kudos: {results.kudosCount}
              </Typography>

              <List dense>
                {results.messages.map((msg, i) => (
                  <ListItem key={i}>
                    <ListItemText
                      primary={`${msg.sender} ${
                        msg.isComment ? "(Comment)" : "(Kudos)"
                      }`}
                      secondary={`${msg.message} — ${new Date(
                        msg.createdAt
                      ).toLocaleString()}`}
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        )}

        {!loading && !results && (
          <Typography>Select an employee or team to view history.</Typography>
        )}
      </Box>
    </Container>
  );
};

export default PublicKudosSearch;
