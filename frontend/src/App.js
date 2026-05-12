import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import ProtectedRoute from "./components/layout/ProtectedRoute";
import NavBar from "./components/layout/NavBar";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import "./App.css";

function AppShell() {
  return (
    <div className="app-layout">
      <NavBar />
      <main className="app-content">
        <DashboardPage />
      </main>
    </div>
  );
}

/**
 * Application root — sets up routing and the auth context.
 *
 * Route structure:
 * - `/login`    public login page
 * - `/register` public registration page
 * - `/`         protected shell (NavBar + DashboardPage); redirects to /login if unauthenticated
 * - `*`         catch-all redirect to /
 */
function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<AppShell />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
