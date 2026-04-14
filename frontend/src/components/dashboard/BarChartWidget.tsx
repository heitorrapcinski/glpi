import type { CSSProperties } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import { STATUS_CONFIG } from '@/utils/status';
import type { StatusChartEntry } from '@/hooks/useDashboard';

// ---------------------------------------------------------------------------
// BarChartWidget — Tickets by status bar chart
// ---------------------------------------------------------------------------

export interface BarChartWidgetProps {
  /** Chart data: one entry per status. */
  data: StatusChartEntry[];
  /** Optional card title. */
  title?: string;
}

const card: CSSProperties = {
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  borderRadius: '8px',
  boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
  padding: '1.25rem 1.5rem',
};

const heading: CSSProperties = {
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: '0 0 1rem 0',
};

export default function BarChartWidget({
  data,
  title = 'Tickets by Status',
}: BarChartWidgetProps) {
  return (
    <div style={card} role="img" aria-label={title}>
      <h3 style={heading}>{title}</h3>
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e9ecef" />
          <XAxis
            dataKey="label"
            tick={{ fontSize: 12, fill: '#606f91' }}
            axisLine={{ stroke: '#dee2e6' }}
            tickLine={false}
          />
          <YAxis
            allowDecimals={false}
            tick={{ fontSize: 12, fill: '#606f91' }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            contentStyle={{
              borderRadius: '6px',
              border: '1px solid #dee2e6',
              fontSize: '0.8125rem',
            }}
          />
          <Bar dataKey="count" radius={[4, 4, 0, 0]} maxBarSize={48}>
            {data.map((entry) => (
              <Cell
                key={entry.status}
                fill={STATUS_CONFIG[entry.status]?.color ?? '#9ca3af'}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
