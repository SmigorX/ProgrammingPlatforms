import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from "recharts";

const tooltipStyle = {
  background: "var(--bg-elevated)",
  border: "1px solid var(--border)",
  borderRadius: 6,
  color: "var(--text-primary)",
  fontSize: 12,
};

/**
 * Responsive bar chart for a single asset's closing prices.
 *
 * @param {{ data: Array<{date: string, close: number}>, color: string }} props
 */
export default function PriceBarChart({ data, color }) {
  if (!data?.length) return <div className="empty-state">No price data available</div>;

  const formatted = data.map((p) => ({
    date:  p.date,
    close: parseFloat(p.close),
  }));

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={formatted} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid stroke="var(--border)" strokeDasharray="3 3" />
        <XAxis
          dataKey="date"
          tick={{ fill: "var(--text-secondary)", fontSize: 11 }}
          tickFormatter={(v) => v.slice(0, 7)}
          interval="preserveStartEnd"
          minTickGap={60}
        />
        <YAxis
          tick={{ fill: "var(--text-secondary)", fontSize: 11 }}
          width={64}
          tickFormatter={(v) => v.toLocaleString()}
        />
        <Tooltip
          contentStyle={tooltipStyle}
          formatter={(v) => [v.toLocaleString(undefined, { minimumFractionDigits: 2 }), "Close"]}
        />
        <Bar dataKey="close" fill={color} radius={[3, 3, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}
