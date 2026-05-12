import { useState, useEffect, useCallback } from "react";
import {
  listDashboards,
  createDashboard,
  deleteDashboard,
  addWidget,
  updateWidget,
  deleteWidget,
} from "../api/dashboards";
import WidgetCard from "../components/WidgetCard";
import AddWidgetModal from "../components/AddWidgetModal";

/**
 * Main application view.
 *
 * Renders the list of user dashboards as tabs, displays widgets for the
 * active dashboard, and provides controls to add/edit/delete dashboards
 * and widgets. Dashboard selection is persisted to localStorage under
 * {@code marketviz_active_dashboard} so the same dashboard opens on
 * the next visit.
 */
export default function DashboardPage() {
  const [dashboards, setDashboards]       = useState([]);
  const [activeDashId, setActiveDashId]   = useState(
    () => localStorage.getItem("marketviz_active_dashboard") ?? null
  );
  const [showAddWidget, setShowAddWidget] = useState(false);
  const [editingWidget, setEditingWidget] = useState(null);
  const [loading, setLoading]             = useState(true);
  const [error, setError]                 = useState(null);

  const loadDashboards = useCallback(async () => {
    try {
      const res = await listDashboards();
      setDashboards(res.data);
      if (!activeDashId && res.data.length) {
        const defaultDash = res.data.find((d) => d.isDefault) ?? res.data[0];
        setActiveDashId(String(defaultDash.id));
      }
    } catch {
      setError("Failed to load dashboards");
    } finally {
      setLoading(false);
    }
  }, [activeDashId]);

  useEffect(() => { loadDashboards(); }, [loadDashboards]);

  const handleSelectDash = (id) => {
    setActiveDashId(String(id));
    localStorage.setItem("marketviz_active_dashboard", String(id));
  };

  const handleCreateDashboard = async () => {
    const name = window.prompt("Dashboard name:");
    if (!name?.trim()) return;
    await createDashboard({ name: name.trim(), isDefault: !dashboards.length });
    loadDashboards();
  };

  const handleDeleteDashboard = async (id) => {
    if (!window.confirm("Delete this dashboard and all its widgets?")) return;
    await deleteDashboard(id);
    if (String(id) === activeDashId) {
      setActiveDashId(null);
      localStorage.removeItem("marketviz_active_dashboard");
    }
    loadDashboards();
  };

  const handleAddWidget = async (data) => {
    await addWidget(Number(activeDashId), data);
    setShowAddWidget(false);
    loadDashboards();
  };

  const handleEditWidget = async (data) => {
    await updateWidget(Number(activeDashId), editingWidget.id, data);
    setEditingWidget(null);
    loadDashboards();
  };

  const handleDeleteWidget = async (widgetId) => {
    if (!window.confirm("Remove this widget?")) return;
    await deleteWidget(Number(activeDashId), widgetId);
    loadDashboards();
  };

  if (loading) return <div className="loading"><div className="spinner" /><p>Loading dashboards…</p></div>;
  if (error) return <div className="error-msg" style={{ margin: 24 }}>{error}</div>;

  const activeDash = dashboards.find((d) => String(d.id) === activeDashId);

  return (
    <div>
      <div className="dashboard-header">
        <div className="dashboard-tabs">
          {dashboards.map((d) => (
            <button
              key={d.id}
              className={`tab ${String(d.id) === activeDashId ? "active" : ""}`}
              onClick={() => handleSelectDash(d.id)}
            >
              {d.name}
            </button>
          ))}
          <button className="tab" onClick={handleCreateDashboard}>+ New dashboard</button>
        </div>

        <div style={{ display: "flex", gap: 8 }}>
          {activeDash && (
            <>
              <button className="btn btn-secondary" onClick={() => setShowAddWidget(true)}>
                + Add widget
              </button>
              <button
                className="btn btn-danger"
                onClick={() => handleDeleteDashboard(activeDash.id)}
              >
                Delete dashboard
              </button>
            </>
          )}
        </div>
      </div>

      {!activeDash ? (
        <div className="empty-state">
          <p>No dashboards yet. Create one to get started.</p>
        </div>
      ) : activeDash.widgets.length === 0 ? (
        <div className="empty-state">
          <p>This dashboard has no widgets. Add one to start visualizing data.</p>
        </div>
      ) : (
        <div className="widgets-grid">
          {activeDash.widgets.map((widget) => (
            <WidgetCard
              key={widget.id}
              widget={widget}
              onEdit={() => setEditingWidget(widget)}
              onDelete={() => handleDeleteWidget(widget.id)}
            />
          ))}
        </div>
      )}

      {showAddWidget && (
        <AddWidgetModal onSubmit={handleAddWidget} onClose={() => setShowAddWidget(false)} />
      )}
      {editingWidget && (
        <AddWidgetModal
          initial={editingWidget}
          onSubmit={handleEditWidget}
          onClose={() => setEditingWidget(null)}
        />
      )}
    </div>
  );
}
