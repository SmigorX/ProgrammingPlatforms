import client from "./client";

/**
 * Dashboard and widget CRUD API.
 */

export const listDashboards = () => client.get("/dashboards");

export const getDashboard = (id) => client.get(`/dashboards/${id}`);

export const createDashboard = (data) => client.post("/dashboards", data);

export const updateDashboard = (id, data) => client.put(`/dashboards/${id}`, data);

export const deleteDashboard = (id) => client.delete(`/dashboards/${id}`);

export const addWidget = (dashboardId, data) =>
  client.post(`/dashboards/${dashboardId}/widgets`, data);

export const updateWidget = (dashboardId, widgetId, data) =>
  client.put(`/dashboards/${dashboardId}/widgets/${widgetId}`, data);

export const deleteWidget = (dashboardId, widgetId) =>
  client.delete(`/dashboards/${dashboardId}/widgets/${widgetId}`);
