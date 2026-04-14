import type { CSSProperties } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { PRIORITY_CONFIG } from '@/utils/priority';
import type { PriorityChartEntry } from '@/hooks/useDashboard';

// ---------------------------------------------------------------------------
// PieChartWidget — Tickets by priority pie chart
// ---------------------------------------------------------------------------

export interface PieChartWidgetProps {
  /** Chart data: one entry per priority. */
  data: PriorityChartEntry[];
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

export default function PieChartWidget({
  data,
  title = 'Tickets by Priority',
}: PieChartWidgetProps) {
  return (
    <div style={card} role="img" aria-label={title}>
      <h3 style={heading}>{title}</h3>
      <ResponsiveContainer width="100%" height={260}>
        <PieChart>
          <Pie
            data={data}
            dataKey="count"
            nameKey="label"
            cx="50%"
            cy="50%"
            outerRadius={90}
            innerRadius={45}
            paddingAngle={2}
          >
            {data.map((entry) => (
              <Cell
                key={entry.priority}
                fill={PRIORITY_CONFIG[entry.priority]?.color ?? '#9ca3af'}
              />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{
              borderRadius: '6px',
              border: '1px solid #dee2e6',
              fontSize: '0.8125rem',
            }}
          />
          <Legend
            iconType="circle"
            iconSize={10}
            wrapperStyle={{ fontSize: '0.8125rem', paddingTop: '0.5rem' }}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
