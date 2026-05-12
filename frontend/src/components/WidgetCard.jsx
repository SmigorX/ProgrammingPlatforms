import { useState, useEffect } from "react";
import { getPrices } from "../api/assets";
import PriceLineChart from "./charts/PriceLineChart";
import PriceBarChart from "./charts/PriceBarChart";
import PriceTable from "./charts/PriceTable";
import NumberWidget from "./charts/NumberWidget";

const CHART_COMPONENTS = {
  LINE:   PriceLineChart,
  BAR:    PriceBarChart,
  TABLE:  PriceTable,
  NUMBER: NumberWidget,
};

const RANGE_LABELS = {
  ONE_MONTH:    "1M",
  THREE_MONTHS: "3M",
  SIX_MONTHS:   "6M",
  ONE_YEAR:     "1Y",
  THREE_YEARS:  "3Y",
  FIVE_YEARS:   "5Y",
};

/**
 * Renders a single dashboard widget — fetches price data and delegates
 * rendering to the appropriate chart component based on {@code chartType}.
 *
 * @param {{
 *   widget: import("../api/dashboards").WidgetResponse,
 *   onEdit: () => void,
 *   onDelete: () => void
 * }} props
 */
export default function WidgetCard({ widget, onEdit, onDelete }) {
  const [prices, setPrices]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    getPrices(widget.asset.id, widget.timeRange)
      .then((res) => setPrices(res.data))
      .catch(() => setError("Failed to load price data"))
      .finally(() => setLoading(false));
  }, [widget.asset.id, widget.timeRange]);

  const ChartComponent = CHART_COMPONENTS[widget.chartType] ?? PriceLineChart;

  return (
    <div className="widget-card">
      <div className="widget-card-header">
        <div>
          <div className="widget-title">{widget.title}</div>
          <div className="widget-meta">
            {widget.asset.symbol} · {widget.asset.category} · {RANGE_LABELS[widget.timeRange]}
          </div>
        </div>
        <div className="widget-actions">
          <button className="icon-btn" onClick={onEdit} title="Edit widget">✎</button>
          <button className="icon-btn danger" onClick={onDelete} title="Remove widget">✕</button>
        </div>
      </div>

      {loading && (
        <div className="loading">
          <div className="spinner" />
        </div>
      )}
      {error && <div className="error-msg">{error}</div>}
      {!loading && !error && (
        <ChartComponent data={prices} color={widget.color} />
      )}
    </div>
  );
}
