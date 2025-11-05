import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Snackbar,
  Alert,
  Divider,
  Grid,
} from "@mui/material";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import RefreshIcon from "@mui/icons-material/Refresh";
import RestartAltIcon from "@mui/icons-material/RestartAlt";

const API_BASE = "http://localhost:8080/api/admin";

const AdminDashboard = () => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "info" });

  const token = JSON.parse(localStorage.getItem("token"))?.token;

  // ---------- Fetch All Employees ----------
  const fetchEmployees = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE}/employees`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      
      if (response.ok) {
        const data = await response.json();
        setEmployees(data);
      } else {
        throw new Error("Failed to fetch employees");
      }
    } catch (err) {
      setSnackbar({ open: true, message: err.message, severity: "error" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, []);

  // ---------- Handle CSV Upload ----------
  const handleFileChange = (e) => setFile(e.target.files[0]);

  const handleUpload = async () => {
    if (!file) {
      setSnackbar({ open: true, message: "Please select a CSV file first.", severity: "warning" });
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setUploading(true);
    try {
      const response = await fetch(`${API_BASE}/upload`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      const data = await response.json();
      if (response.ok) {
        setSnackbar({ open: true, message: "✅ Upload successful!", severity: "success" });
        fetchEmployees(); // Refresh list
      } else {
        setSnackbar({ open: true, message: data.error || "Upload failed", severity: "error" });
      }
    } catch (error) {
      setSnackbar({ open: true, message: error.message, severity: "error" });
    } finally {
      setUploading(false);
    }
  };

  // ---------- Reset Kudos (Global) ----------
  const handleResetAllKudos = async () => {
    if (!window.confirm("Are you sure you want to reset ALL kudos counts?")) return;

    try {
      const response = await fetch(`${API_BASE}/reset-kudos`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.ok) {
        setSnackbar({ open: true, message: "All kudos counts have been reset.", severity: "success" });
        fetchEmployees();
      } else {
        setSnackbar({ open: true, message: "Failed to reset kudos.", severity: "error" });
      }
    } catch (error) {
      setSnackbar({ open: true, message: error.message, severity: "error" });
    }
  };

  // ---------- Reset Employee Password ----------
  const handleResetEmployeePassword = async (employeeId) => {
    try{
      const response = await fetch(`${API_BASE}/employees/${employeeId}/reset-password`, {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        setSnackbar({ open: true, message: `Password reset for ${employeeId}`, severity: "success" });
        fetchEmployees();
      } else {
        setSnackbar({ open: true, message: "Failed to reset password.", severity: "error" });
      }
    }catch (error) {
      setSnackbar({ open: true, message: error.message, severity: "error" });
    }
  }

  // ---------- UI ----------
  return (
    <Box sx={{ p: 5, bgcolor: "#f7f9fc", minHeight: "100vh" }}>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        🛠️ Admin Dashboard
      </Typography>

      {/* CSV Upload + Global Reset Controls */}
      <Grid container spacing={3} sx={{ mb: 5 }}>
        <Grid item xs={12} md={6}>
          <Paper elevation={3} sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" gutterBottom>
              📤 Upload Employee List (CSV)
            </Typography>

            <input type="file" accept=".csv" onChange={handleFileChange} />
            <Button
              variant="contained"
              startIcon={<CloudUploadIcon />}
              onClick={handleUpload}
              disabled={uploading}
              sx={{ mt: 2 }}
            >
              {uploading ? "Uploading..." : "Upload CSV"}
            </Button>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper elevation={3} sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" gutterBottom>
              🔁 Reset Kudos Counts
            </Typography>
            <Button
              variant="outlined"
              color="error"
              startIcon={<RestartAltIcon />}
              onClick={handleResetAllKudos}
            >
              Reset All Kudos
            </Button>
          </Paper>
        </Grid>
      </Grid>

      {/* Employee List */}
      <Paper elevation={4} sx={{ p: 3, borderRadius: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h6">👥 Employee List</Typography>
          <Button
            variant="text"
            startIcon={<RefreshIcon />}
            onClick={fetchEmployees}
            disabled={loading}
          >
            Refresh
          </Button>
        </Box>

        <Divider sx={{ mb: 2 }} />

        {loading ? (
          <Box display="flex" justifyContent="center" p={3}>
            <CircularProgress />
          </Box>
        ) : (
          <TableContainer component={Paper}>
            <Table>
              <TableHead sx={{ backgroundColor: "#1976d2" }}>
                <TableRow>
                  <TableCell sx={{ color: "white", fontWeight: "bold" }}>ID</TableCell>
                  <TableCell sx={{ color: "white", fontWeight: "bold" }}>Name</TableCell>
                  <TableCell sx={{ color: "white", fontWeight: "bold" }}>Email</TableCell>
                  <TableCell sx={{ color: "white", fontWeight: "bold" }}>Department</TableCell>
                  <TableCell sx={{ color: "white", fontWeight: "bold" }}>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {employees.map((emp) => (
                  <TableRow key={emp.employeeId} hover>
                    <TableCell>{emp.employeeId}</TableCell>
                    <TableCell>{emp.name}</TableCell>
                    <TableCell>{emp.email}</TableCell>
                    <TableCell sx={{ textTransform: "capitalize" }}>{emp.department}</TableCell>
                    <TableCell>
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={() => handleResetEmployeePassword(emp.employeeId)}
                      >
                        Reset Password?
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      {/* Snackbar Notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity} sx={{ width: "100%" }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default AdminDashboard;
