import React, { useState, useEffect } from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Button,
  Snackbar,
  Alert,
  CircularProgress,
  TextField,
  FormControlLabel,
  Checkbox,
  Switch,
  Stack, 
  MenuItem
} from "@mui/material";
import { EmojiEvents, History, Group, ExitToApp, Send } from "@mui/icons-material";

const drawerWidth = 240;
const API_BASE = "http://localhost:8080/api/employee";

const EmployeeDashboard = () => {
  const [selectedMenu, setSelectedMenu] = useState("dashboard");
  const [loading] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "info" });

  // TOKEN
  const token = JSON.parse(localStorage.getItem("token"))?.token;
  const currentEmployeeEmail = JSON.parse(atob(token.split(".")[1]))?.sub || "";

  // DASHBOARD
  const [totalKudos, setTotalKudos] = useState(0);
  const [recentKudos, setRecentKudos] = useState([]);

  // SEND KUDOS/COMMENT
  const [recipientType, setRecipientType] = useState("");
  const [recipientEmployeeId, setRecipientEmployeeId] = useState("");
  const [recipientTeamId, setRecipientTeamId] = useState("");
  const [message, setMessage] = useState("");
  const [anonymous, setAnonymous] = useState(false);
  const [isComment, setIsComment] = useState(false);
  const [sending, setSending] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [teams, setTeams] = useState([]);

  // HISTORY
  const [employee, setEmployee] = useState(null);
  const [employeeHistory, setEmployeeHistory] = useState([]); // kudos/comments received by this employee
  const [employeeTeams, setEmployeeTeams] = useState([]); // teams the employee belongs to
  const [teamHistories, setTeamHistories] = useState({}); // team histories of this employee
  const [period, setPeriod] = useState("all");
  const [historyLoading, setHistoryLoading] = useState(false);
  const [selectedTeamId, setSelectedTeamId] = useState("all"); // "all" or a specific team id

  // LEADERBOARD
  const [employeesLeaderboard, setEmployeesLeaderboard] = useState([]);
  const [teamsLeaderboard, setTeamsLeaderboard] = useState([]);
  const [department, setDepartment] = useState("");
  const departments = ["Sales", "HR", "IT", "Marketing", "Finance"]; // FOR NOW

  const handleLogout = () => {
    localStorage.removeItem("token");
    window.location.href = "/";
  };

  const handleMenuClick = (menu) => {
    setSelectedMenu(menu);
  };

  const handleSend = async () => {
    if (!message || (!recipientEmployeeId && !recipientTeamId)) {
      setSnackbar({ open: true, message: "Please complete all fields.", severity: "warning" });
      return;
    }

    const endpoint = isComment ? "/comment" : "/send";

    const payload = {
      recipientEmployeeId: recipientEmployeeId ? parseInt(recipientEmployeeId) : null,
      recipientTeamId: recipientTeamId ? parseInt(recipientTeamId) : null,
      message,
      anonymous, // use your actual anonymous state
    };

    setSending(true);
    try {
      const res = await fetch(`http://localhost:8080/api/kudos${endpoint}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      // prevent error on empty response
      const data = res.status !== 204 ? await res.json().catch(() => ({})) : {};

      if (res.ok) {
        setSnackbar({
          open: true,
          message: data.message || "Sent successfully!",
          severity: "success",
        });
        setMessage("");
        setRecipientEmployeeId("");
        setRecipientTeamId("");
      } else {
        setSnackbar({
          open: true,
          message: data.error || "Failed to send.",
          severity: "error",
        });
      }
    } catch (err) {
      console.error("Error sending:", err);
      setSnackbar({
        open: true,
        message: err.message,
        severity: "error",
      });
    } finally {
      setSending(false);
    }
  };

  const renderContent = () => {
    
    switch (selectedMenu) {
      case "dashboard":
        return (
          <Box p={3}>
            <Typography variant="h5" fontWeight="bold">
              Welcome Back!
            </Typography>
            <Typography color="textSecondary">
              Here’s your kudos summary.
            </Typography>

            <Box mt={3}>
              <Typography variant="h6" sx={{ color: "#ff4081" }}>
                Total Kudos Received: {totalKudos} ⭐
              </Typography>

              <Box mt={2}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Recent Kudos & Comments:
                </Typography>
                {recentKudos.length > 0 ? (
                  recentKudos.map((kudo) => (
                    <Box
                      key={kudo.id}
                      mt={1}
                      p={2}
                      border="1px solid #ddd"
                      borderRadius={2}
                      bgcolor="#fff0f6"
                    >
                      <Typography>
                        <strong>{kudo.sender}</strong>: “{kudo.message}”
                      </Typography>
                      <Typography variant="caption" color="textSecondary">
                        {new Date(kudo.createdAt).toLocaleString()}
                      </Typography>
                    </Box>
                  ))
                ) : (
                  <Typography color="textSecondary">
                    No recent kudos yet.
                  </Typography>
                )}
              </Box>
            </Box>
          </Box>
        );
      case "sendKudos":
        return (
          <Box p={3}>
            <Typography variant="h5" fontWeight="bold" gutterBottom>
              🙌 Send Kudos / Leave a Comment
            </Typography>

            <Typography color="textSecondary" mb={3}>
              Choose whether to send a kudos (with count) or a comment (no count).
            </Typography>

            <Box
              component="form"
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              sx={{
                display: "flex",
                flexDirection: "column",
                gap: 3,
                maxWidth: 600,
              }}
            >
              {/* Recipient Type */}
              <Stack spacing={2}>
                <TextField
                  select
                  label="Recipient Type"
                  value={recipientType}
                  onChange={(e) => {
                    setRecipientType(e.target.value);
                    setRecipientEmployeeId("");
                    setRecipientTeamId("");
                  }}
                  fullWidth
                >
                  <MenuItem value="">-- Select Type --</MenuItem>
                  <MenuItem value="employee">Employee</MenuItem>
                  <MenuItem value="team">Team</MenuItem>
                </TextField>

                {/* Employee dropdown */}
                {recipientType === "employee" && (
                  <TextField
                    select
                    label="Select Employee"
                    value={recipientEmployeeId}
                    onChange={(e) => setRecipientEmployeeId(e.target.value)}
                    fullWidth
                  >
                    <MenuItem value="">-- Select Employee --</MenuItem>
                    {employees.map((emp) => (
                      <MenuItem
                      key={emp.id}
                      value={emp.id}
                      disabled={emp.email === currentEmployeeEmail} // 👈 prevent self-selection
                    >
                      {emp.name} ({emp.department})
                      {/* {emp.email === currentEmployeeEmail ? " (You)" : ""} */}
                    </MenuItem>
                    ))}
                  </TextField>
                )}

                {/* Team dropdown */}
                {recipientType === "team" && (
                  <TextField
                    select
                    label="Select Team"
                    value={recipientTeamId}
                    onChange={(e) => setRecipientTeamId(e.target.value)}
                    fullWidth
                  >
                    <MenuItem value="">-- Select Team --</MenuItem>
                    {teams.map((team) => (
                      <MenuItem key={team.id} value={team.id}>
                        {team.name}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              </Stack>

              {/* Message field */}
              <TextField
                label="Your Message"
                multiline
                rows={3}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                fullWidth
              />

              {/* Options */}
              <FormControlLabel
                control={
                  <Checkbox
                    checked={anonymous}
                    onChange={(e) => setAnonymous(e.target.checked)}
                  />
                }
                label="Send Anonymously"
              />

              <FormControlLabel
                control={
                  <Switch
                    checked={isComment}
                    onChange={(e) => setIsComment(e.target.checked)}
                  />
                }
                label={isComment ? "Send as Comment" : "Send as Kudos"}
              />

              {/* Submit */}
              <Button
                variant="contained"
                color="primary"
                disabled={sending}
                type="submit"
              >
                {sending ? "Sending..." : isComment ? "Submit Comment" : "Send Kudos"}
              </Button>
            </Box>
          </Box>
        );

      case "myHistory":
        return (
          <Box p={3}>
            <Typography variant="h5" fontWeight="bold">My Kudos History</Typography>
            <Typography color="textSecondary" mb={2}>
              View kudos and comments received personally and those received by your team(s).
            </Typography>

            <Box display="flex" gap={2} mb={2} flexWrap="wrap">
              <TextField
                select
                label="Filter by Period"
                value={period}
                onChange={(e) => setPeriod(e.target.value)}
                sx={{ minWidth: 160 }}
              >
                <MenuItem value="week">Past Week</MenuItem>
                <MenuItem value="month">Past Month</MenuItem>
                <MenuItem value="all">All Time</MenuItem>
              </TextField>

              <TextField
                select
                label="Team View"
                value={selectedTeamId}
                onChange={(e) => setSelectedTeamId(e.target.value)}
                sx={{ minWidth: 200 }}
              >
                <MenuItem value="all">All Teams</MenuItem>
                {employeeTeams.map((t) => (
                  <MenuItem key={t.id} value={String(t.id)}>{t.name}</MenuItem>
                ))}
              </TextField>
            </Box>

            {historyLoading ? (
              <Box display="flex" justifyContent="center" alignItems="center" minHeight="30vh">
                <CircularProgress />
              </Box>
            ) : (
              <>
                {/* Personal history */}
                <Box mb={3}>
                  <Typography variant="h6">Personal (received)</Typography>
                  {!Array.isArray(employeeHistory) || employeeHistory.length === 0 ? (
                    <Typography color="textSecondary">No personal kudos/comments found for the selected period.</Typography>
                  ) : (
                    employeeHistory.map((msg) => (
                      <Box key={msg.id} p={2} mb={1} border="1px solid #eee" borderRadius={1} bgcolor="#fff">
                        <Typography variant="subtitle2">{msg.type === "Comment" ? "💬 Comment" : "⭐ Kudos"} — {msg.sender}</Typography>
                        <Typography mt={1}>{msg.message}</Typography>
                        <Typography color="textSecondary" variant="caption" display="block" mt={1}>
                          {new Date(msg.createdAt || msg.createdAt || msg.createdAt).toLocaleString()}
                        </Typography>
                      </Box>
                    ))
                  )}
                </Box>

                {/* Team history */}
                <Box mb={3}>
                  <Typography variant="h6">Team(s)</Typography>

                  {employeeTeams.length === 0 ? (
                    <Typography color="textSecondary">You are not part of any team or team data is unavailable.</Typography>
                  ) : (
                    employeeTeams
                      .filter(t => selectedTeamId === "all" || String(t.id) === String(selectedTeamId))
                      .map((t) => {
                        const messages = teamHistories[t.id] || [];
                        return (
                          <Box key={t.id} mb={2}>
                            <Typography variant="subtitle1">{t.name}</Typography>
                            {messages.length === 0 ? (
                              <Typography color="textSecondary">No kudos/comments for this team in the selected period.</Typography>
                            ) : (
                              messages.map((msg) => (
                                <Box key={msg.id} p={2} mb={1} border="1px solid #eee" borderRadius={1} bgcolor="#fff">
                                  <Typography variant="subtitle2">{msg.type === "Comment" ? "💬 Comment" : "⭐ Kudos"} — {msg.sender}</Typography>
                                  <Typography mt={1}>{msg.message}</Typography>
                                  <Typography color="textSecondary" variant="caption" display="block" mt={1}>
                                    {new Date(msg.createdAt).toLocaleString()}
                                  </Typography>
                                </Box>
                              ))
                            )}
                          </Box>
                        );
                      })
                  )}
                </Box>
              </>
            )}
          </Box>
        );
      case "leaderboard":
        return (
          <Box p={3}>
            <Typography variant="h5" fontWeight="bold" gutterBottom>
              🏆 Kudos Leaderboard
            </Typography>
            <Typography color="textSecondary" mb={3}>
              Top employees and teams this month.
            </Typography>

            {/* Department Filter */}
            <TextField
              select
              label="Filter by Department"
              value={department}
              onChange={(e) => setDepartment(e.target.value)}
              sx={{ mb: 3, width: 300 }}
            >
              <MenuItem value="">All Departments</MenuItem>
              {departments.map((d) => (
                <MenuItem key={d} value={d}>
                  {d}
                </MenuItem>
              ))}
            </TextField>

            <Box display="flex" flexDirection={{ xs: "column", md: "row" }} gap={3}>
              {/* Top Employees */}
              <Box flex={1}>
                <Typography variant="h6" gutterBottom>
                  🧑‍💼 Top 5 Employees
                </Typography>
                {employeesLeaderboard.length === 0 ? (
                  <Typography>No data available</Typography>
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

              {/* Top Teams */}
              <Box flex={1}>
                <Typography variant="h6" gutterBottom>
                  👥 Top 5 Teams
                </Typography>
                {teamsLeaderboard.length === 0 ? (
                  <Typography>No data available</Typography>
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


      default:
        return null;
    }
  };

  const fetchEmployees = async () => {
    try {
      const response = await fetch(`${API_BASE}/all`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        setEmployees(await response.json());
      } else {
        console.error("Failed to load employees");
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchTeams = async () => {
    try {
      const response = await fetch(`${API_BASE}/teams`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        setTeams(await response.json());
      } else {
        console.error("Failed to load teams");
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchLeaderboard = async () => {
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
      } else {
        console.error("Failed to load leaderboard");
      }
    } catch (err) {
      console.error("Leaderboard fetch error:", err);
    }
  };

  // Fetch employee details (to get teams).
  const fetchEmployeeDetails = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/employee/me", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        const data = await res.json();
        setEmployee(data.employee);
        setEmployeeTeams(data.teams || []);
      } else {
        console.error("Failed to load employee profile");
      }
    } catch (err) {
      console.error("Error fetching profile:", err);
    }
  };

  // Fetch personal kudos/comments received by employee
  const fetchEmployeeHistory = async () => {
    if (!employee?.id) return;
    setHistoryLoading(true);
    try {
      const res = await fetch(
        `http://localhost:8080/api/kudos/employee/${employee.id}/history?period=${period}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (res.ok) {
        const data = await res.json();
        const messages = Array.isArray(data.messages) ? data.messages : [];

        setEmployeeHistory(messages);
      }
    } catch (err) {
      console.error("Error fetching employee history:", err);
    } finally {
      setHistoryLoading(false);
    }
  };

  // Fetch team history for a given teamId
  const fetchTeamHistory = async () => {
    if (!employeeTeams.length) return;

    setHistoryLoading(true);
    try {
      const results = await Promise.all(
        employeeTeams.map(async (team) => {
          const res = await fetch(
            `http://localhost:8080/api/kudos/team/${team.id}/history?period=${period}`,
            { headers: { Authorization: `Bearer ${token}` } }
          );
          if (res.ok) {
            console.log("TEAM HISTORY");
            const data = await res.json();
            const messages = Array.isArray(data.messages) ? data.messages : [];
            return [team.id, messages];
          }
          return [team.id, []];
        })
      );

      setTeamHistories(Object.fromEntries(results));
    } catch (err) {
      console.error("Error fetching team history:", err);
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    fetchEmployees();
    fetchEmployeeDetails();
    fetchTeams();
  }, []);

  // When period changes while on the page, re-fetch
  useEffect(() => {
    if (selectedMenu === "myHistory" && employee) {
      fetchEmployeeHistory();
      fetchTeamHistory();
    }
  }, [employee, period, selectedMenu]);

  // DASHBOARD functions
  useEffect(() => {
    const fetchData = async () => {
      try {
        const resProfile = await fetch("http://localhost:8080/api/employee/me", {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!resProfile.ok) throw new Error("Failed to load profile");
        const profile = await resProfile.json();

        const employeeId = profile.employee?.id;
        if (!employeeId) {
          console.warn("Employee ID missing in profile response");
          return;
        }

        // Get kudos count directly from employee profile
        const resCount = await fetch(
          `http://localhost:8080/api/kudos/employee/${employeeId}/kudos-count`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        const data = await resCount.json();
        setTotalKudos(data.totalKudos || 0);

        // Fetch recent system-wide kudos/comments
        const res2 = await fetch("http://localhost:8080/api/kudos/public/recent");
        const recentData = res2.ok ? await res2.json() : [];
        setRecentKudos(recentData.slice(0, 3));
      } catch (err) {
        console.error("Error loading dashboard data:", err);
      }
    };

    fetchData();
  }, [token]);

  // LEADERBOARD functions
  useEffect(() => {
    if (selectedMenu === "leaderboard") {
      fetchLeaderboard();
    }
  }, [selectedMenu, department]);

  return (
    <Box sx={{ display: "flex" }}>
      {/* Sidebar */}
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: "border-box" },
        }}
      >
        <Toolbar>
          <Typography variant="h6" fontWeight="bold">Employee Panel</Typography>
        </Toolbar>
        <List>
          <ListItem button selected={selectedMenu === "dashboard"} onClick={() => handleMenuClick("dashboard")}>
            <ListItemIcon><EmojiEvents /></ListItemIcon>
            <ListItemText primary="Dashboard" />
          </ListItem>
          <ListItem button selected={selectedMenu === "sendKudos"} onClick={() => handleMenuClick("sendKudos")}>
            <ListItemIcon><Send /></ListItemIcon>
            <ListItemText primary="Send Kudos / Comment" />
          </ListItem>
          <ListItem button selected={selectedMenu === "myHistory"} onClick={() => handleMenuClick("myHistory")}>
            <ListItemIcon><History /></ListItemIcon>
            <ListItemText primary="My History" />
          </ListItem>
          <ListItem button selected={selectedMenu === "leaderboard"} onClick={() => handleMenuClick("leaderboard")}>
            <ListItemIcon><Group /></ListItemIcon>
            <ListItemText primary="Leaderboard" />
          </ListItem>
        </List>
        <Box sx={{ position: "absolute", bottom: 0, width: "100%" }}>
          <Button
            fullWidth
            color="error"
            startIcon={<ExitToApp />}
            onClick={handleLogout}
          >
            Logout
          </Button>
        </Box>
      </Drawer>

      {/* Main content */}
      <Box
        component="main"
        sx={{ flexGrow: 1, bgcolor: "#f8f9fa", p: 3, minHeight: "100vh" }}
      >
        <AppBar position="static" color="default" elevation={1}>
          <Toolbar>
            <Typography variant="h6" color="primary" sx={{ flexGrow: 1 }}>
              {selectedMenu === "dashboard" && "Dashboard"}
              {selectedMenu === "sendKudos" && "Send Kudos / Comment"}
              {selectedMenu === "myHistory" && "My Kudos History"}
              {selectedMenu === "leaderboard" && "Leaderboard"}
            </Typography>
          </Toolbar>
        </AppBar>

        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
            <CircularProgress />
          </Box>
        ) : (
          renderContent()
        )}

        <Snackbar
          open={snackbar.open}
          autoHideDuration={3000}
          onClose={() => setSnackbar({ ...snackbar, open: false })}
        >
          <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
        </Snackbar>
      </Box>
    </Box>
  );
};

export default EmployeeDashboard;
