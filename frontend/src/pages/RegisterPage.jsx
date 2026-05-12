import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/auth";
import { useAuth } from "../contexts/AuthContext";

/**
 * Registration page — creates a new account via {@code POST /api/auth/register}
 * and logs the user in immediately on success.
 */
export default function RegisterPage() {
  const [username, setUsername]         = useState("");
  const [email, setEmail]               = useState("");
  const [password, setPassword]         = useState("");
  const [confirmPassword, setConfirmPw] = useState("");
  const [error, setError]               = useState(null);
  const [loading, setLoading]           = useState(false);

  const { login: storeAuth } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await register({ username, email, password });
      storeAuth(res.data);
      navigate("/");
    } catch (err) {
      const detail = err.response?.data?.detail;
      const fields = err.response?.data?.fields;
      setError(fields ? Object.values(fields).join(", ") : (detail ?? "Registration failed"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Create account</h1>
        <p className="subtitle">Start tracking market data</p>
        {error && <div className="error-msg">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              minLength={3}
              maxLength={50}
              autoComplete="username"
              required
            />
          </div>
          <div className="field">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              required
            />
          </div>
          <div className="field">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              autoComplete="new-password"
              required
            />
          </div>
          <div className="field">
            <label>Confirm password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPw(e.target.value)}
              autoComplete="new-password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? "Creating account…" : "Create account"}
          </button>
        </form>
        <p style={{ marginTop: 20, textAlign: "center", fontSize: 13, color: "var(--text-secondary)" }}>
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
