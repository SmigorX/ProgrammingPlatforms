/**
 * Displays the most recent closing price and day-over-day change for an asset.
 *
 * @param {{ data: Array<{date: string, close: number}>, color: string }} props
 */
export default function NumberWidget({ data, color }) {
  if (!data?.length) return <div className="empty-state">No price data available</div>;

  const latest = data[data.length - 1];
  const prev   = data.length > 1 ? data[data.length - 2] : null;

  const close  = parseFloat(latest.close);
  const change = prev ? close - parseFloat(prev.close) : null;
  const pct    = prev ? (change / parseFloat(prev.close)) * 100 : null;
  const positive = change >= 0;

  return (
    <div className="number-widget">
      <div className="current-price" style={{ color }}>
        {close.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
      </div>
      {change != null && (
        <div className={`change ${positive ? "positive" : "negative"}`}>
          {positive ? "▲" : "▼"}{" "}
          {Math.abs(change).toLocaleString(undefined, { minimumFractionDigits: 2 })}
          {" "}({positive ? "+" : ""}{pct.toFixed(2)}%)
        </div>
      )}
      <div style={{ fontSize: 12, color: "var(--text-secondary)", marginTop: 8 }}>
        as of {latest.date}
      </div>
    </div>
  );
}
