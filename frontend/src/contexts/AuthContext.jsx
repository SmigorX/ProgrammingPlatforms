import { createContext, useContext, useState, useCallback } from "react";

/**
 * @typedef {Object} AuthState
 * @property {string|null} token - JWT access token
 * @property {string|null} username
 * @property {string|null} role
 */

/**
 * @typedef {Object} AuthContextValue
 * @property {AuthState} auth
 * @property {(tokenResponse: Object) => void} login - persist auth state from a token response
 * @property {() => void} logout
 * @property {boolean} isAuthenticated
 */

const AuthContext = createContext(/** @type {AuthContextValue} */ (null));

const STORAGE_KEY = "marketviz_auth";

function loadFromStorage() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "null");
  } catch {
    return null;
  }
}

/**
 * Provides authentication state to the component tree.
 * The JWT and user info are persisted in localStorage so the session
 * survives page refreshes.
 *
 * @param {{ children: React.ReactNode }} props
 */
export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => loadFromStorage() ?? { token: null, username: null, role: null });

  const login = useCallback((tokenResponse) => {
    const state = {
      token:    tokenResponse.accessToken,
      username: tokenResponse.username,
      role:     tokenResponse.role,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    setAuth(state);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setAuth({ token: null, username: null, role: null });
  }, []);

  return (
    <AuthContext.Provider value={{ auth, login, logout, isAuthenticated: !!auth.token }}>
      {children}
    </AuthContext.Provider>
  );
}

/** @returns {AuthContextValue} */
export function useAuth() {
  return useContext(AuthContext);
}
