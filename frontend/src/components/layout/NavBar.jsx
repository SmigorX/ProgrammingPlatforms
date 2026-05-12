import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

const styles = {
  nav: {
    background: "var(--bg-surface)",
    borderBottom: "1px solid var(--border)",
    padding: "0 24px",
    height: 56,
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    position: "sticky",
    top: 0,
    zIndex: 100,
  },
  brand: {
    fontWeight: 700,
    fontSize: 16,
    color: "var(--text-primary)",
    letterSpacing: "-0.02em",
  },
  right: { display: "flex", alignItems: "center", gap: 16 },
  user: { fontSize: 13, color: "var(--text-secondary)" },
  logout: {
    background: "transparent",
    border: "1px solid var(--border)",
    borderRadius: "var(--radius)",
    color: "var(--text-secondary)",
    padding: "5px 12px",
    fontSize: 13,
    cursor: "pointer",
    transition: "all 0.15s",
  },
};

/**
 * Top navigation bar shown to authenticated users.
 * Displays the app brand, current username, and a logout button.
 */
export default function NavBar() {
  const { auth, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav style={styles.nav}>
      <Link to="/" style={styles.brand}>
        MarketViz
      </Link>
      <div style={styles.right}>
        <span style={styles.user}>{auth.username}</span>
        <button style={styles.logout} onClick={handleLogout}>
          Sign out
        </button>
      </div>
    </nav>
  );
}
