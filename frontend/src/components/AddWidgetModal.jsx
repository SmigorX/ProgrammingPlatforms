import { useState, useEffect } from "react";
import { listAssets } from "../api/assets";

const CHART_TYPES = ["LINE", "BAR", "TABLE", "NUMBER"];
const TIME_RANGES = ["ONE_MONTH", "THREE_MONTHS", "SIX_MONTHS", "ONE_YEAR", "THREE_YEARS", "FIVE_YEARS"];
const RANGE_LABELS = {
  ONE_MONTH: "1 Month", THREE_MONTHS: "3 Months", SIX_MONTHS: "6 Months",
  ONE_YEAR: "1 Year", THREE_YEARS: "3 Years", FIVE_YEARS: "5 Years",
};

/**
 * Modal dialog for adding or editing a dashboard widget.
 *
 * @param {{
 *   initial?: Object,
 *   onSubmit: (data: Object) => Promise<void>,
 *   onClose: () => void
 * }} props
 */
export default function AddWidgetModal({ initial, onSubmit, onClose }) {
  const [assets, setAssets]       = useState([]);
  const [assetId, setAssetId]     = useState(initial?.asset?.id ?? "");
  const [chartType, setChartType] = useState(initial?.chartType ?? "LINE");
  const [timeRange, setTimeRange] = useState(initial?.timeRange ?? "ONE_YEAR");
  const [color, setColor]         = useState(initial?.color ?? "#3b82f6");
  const [title, setTitle]         = useState(initial?.title ?? "");
  const [saving, setSaving]       = useState(false);
  const [error, setError]         = useState(null);

  useEffect(() => {
    listAssets().then((res) => {
      setAssets(res.data);
      if (!assetId && res.data.length) setAssetId(res.data[0].id);
    });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await onSubmit({ assetId: Number(assetId), chartType, timeRange, color, title, displayOrder: initial?.displayOrder ?? 0 });
    } catch (err) {
      setError(err.response?.data?.detail ?? "Failed to save widget");
      setSaving(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <h3>{initial ? "Edit Widget" : "Add Widget"}</h3>
        {error && <div className="error-msg">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Asset</label>
            <select value={assetId} onChange={(e) => setAssetId(e.target.value)} required>
              {assets.map((a) => (
                <option key={a.id} value={a.id}>{a.name} ({a.symbol})</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Chart type</label>
            <select value={chartType} onChange={(e) => setChartType(e.target.value)}>
              {CHART_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Time range</label>
            <select value={timeRange} onChange={(e) => setTimeRange(e.target.value)}>
              {TIME_RANGES.map((r) => <option key={r} value={r}>{RANGE_LABELS[r]}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Color</label>
            <input type="color" value={color} onChange={(e) => setColor(e.target.value)} />
          </div>
          <div className="field">
            <label>Title (optional)</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Leave blank to use asset name"
              maxLength={100}
            />
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" style={{ width: "auto" }} disabled={saving}>
              {saving ? "Saving…" : (initial ? "Save changes" : "Add widget")}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
