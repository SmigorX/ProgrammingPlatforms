/**
 * Tabular view of recent daily OHLCV data.
 * Shows the 20 most recent rows in reverse-chronological order.
 *
 * @param {{ data: Array<{date: string, open: number, high: number, low: number, close: number, volume: number}> }} props
 */
export default function PriceTable({ data }) {
  if (!data?.length) return <div className="empty-state">No price data available</div>;

  const rows = [...data].reverse().slice(0, 20);
  const fmt = (v) =>
    v != null ? parseFloat(v).toLocaleString(undefined, { minimumFractionDigits: 2 }) : "—";

  return (
    <div style={{ overflowX: "auto" }}>
      <table className="price-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Open</th>
            <th>High</th>
            <th>Low</th>
            <th>Close</th>
            <th>Volume</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.date}>
              <td>{row.date}</td>
              <td>{fmt(row.open)}</td>
              <td>{fmt(row.high)}</td>
              <td>{fmt(row.low)}</td>
              <td style={{ fontWeight: 600 }}>{fmt(row.close)}</td>
              <td style={{ color: "var(--text-secondary)" }}>
                {row.volume != null ? Number(row.volume).toLocaleString() : "—"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
