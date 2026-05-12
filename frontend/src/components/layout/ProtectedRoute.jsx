import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

/**
 * Renders its child routes only when the user is authenticated.
 * Unauthenticated visitors are redirected to `/login`, with the
 * current path saved in state so the login page can redirect back.
 */
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}
