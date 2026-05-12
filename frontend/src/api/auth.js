import client from "./client";

/**
 * Authentication API — registration and login.
 * Both endpoints return a {@code TokenResponse} on success.
 */

/**
 * @param {{ username: string, email: string, password: string }} data
 * @returns {Promise<import("axios").AxiosResponse>}
 */
export const register = (data) => client.post("/auth/register", data);

/**
 * @param {{ username: string, password: string }} data
 * @returns {Promise<import("axios").AxiosResponse>}
 */
export const login = (data) => client.post("/auth/login", data);
