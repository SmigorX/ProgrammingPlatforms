import axios from "axios";

/**
 * Pre-configured axios instance for the MarketViz backend.
 *
 * The request interceptor injects the JWT from localStorage on every request
 * so individual API modules don't need to handle auth headers themselves.
 * A 401 response clears the stored token and reloads to force a login redirect.
 */
const client = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

client.interceptors.request.use((config) => {
  try {
    const auth = JSON.parse(localStorage.getItem("marketviz_auth"));
    if (auth?.token) {
      config.headers.Authorization = `Bearer ${auth.token}`;
    }
  } catch {
    // storage unavailable or malformed — proceed without token
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("marketviz_auth");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default client;
