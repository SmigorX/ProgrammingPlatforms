import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/auth";
import { useAuth } from "../contexts/AuthContext";

/**
 * Login page — authenticates against {@code POST /api/auth/login}
 * and redirects to the dashboard on success.
 */
export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError]       = useState(null);
  const [loading, setLoading]   = useState(false);

  const { login: storeAuth } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await login({ username, password });
      storeAuth(res.data);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.detail ?? "Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Sign in</h1>
        <p className="subtitle">MarketViz — market data visualization</p>
        {error && <div className="error-msg">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </div>
          <div className="field">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>
        <p style={{ marginTop: 20, textAlign: "center", fontSize: 13, color: "var(--text-secondary)" }}>
          No account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
}
