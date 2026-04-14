import type { CSSProperties } from 'react';
import SearchEngine, {
  type ColumnDef,
  type FilterDef,
} from '@/components/common/SearchEngine';
import { ASSETS } from '@/api/endpoints';
import { getLicenseComplianceColor } from '@/utils/status';
import { formatDate } from '@/utils/formatters';
import type { SoftwareLicense } from '@/hooks/useAssets';

// ---------------------------------------------------------------------------
// Compliance color map → CSS values
// ---------------------------------------------------------------------------

const COMPLIANCE_CSS: Record<string, { bg: string; fg: string }> = {
  green:  { bg: '#d1fae5', fg: '#065f46' },
  orange: { bg: '#ffedd5', fg: '#9a3412' },
  red:    { bg: '#fee2e2', fg: '#991b1b' },
};

function complianceBadge(total: number, used: number): React.ReactNode {
  const color = getLicenseComplianceColor(total, used);
  const css = COMPLIANCE_CSS[color];
  const style: CSSProperties = {
    display: 'inline-block',
    padding: '2px 8px',
    borderRadius: '4px',
    fontSize: '0.8rem',
    fontWeight: 600,
    backgroundColor: css.bg,
    color: css.fg,
  };

  const remaining = total - used;
  return <span style={style}>{remaining}</span>;
}

// ---------------------------------------------------------------------------
// Column definitions
// ---------------------------------------------------------------------------

const columns: ColumnDef<SoftwareLicense>[] = [
  {
    key: 'name',
    label: 'Name',
    sortable: true,
    render: (value) => (
      <span style={{ fontWeight: 500 }}>{value as string}</span>
    ),
  },
  {
    key: 'softwareName',
    label: 'Software',
    sortable: true,
    width: '160px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'licenseType',
    label: 'License Type',
    sortable: true,
    width: '130px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'serial',
    label: 'Serial',
    sortable: false,
    width: '140px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'totalSeats',
    label: 'Total Seats',
    sortable: true,
    width: '100px',
    render: (value) => String(value ?? 0),
  },
  {
    key: 'usedSeats',
    label: 'Used Seats',
    sortable: true,
    width: '100px',
    render: (value) => String(value ?? 0),
  },
  {
    key: 'remainingSeats',
    label: 'Remaining',
    sortable: true,
    width: '110px',
    render: (_value, item) => complianceBadge(item.totalSeats, item.usedSeats),
  },
  {
    key: 'expiryDate',
    label: 'Expiry Date',
    sortable: true,
    width: '150px',
    render: (value) => {
      if (!value) return '—';
      const d = new Date(value as string);
      const expired = d.getTime() < Date.now();
      return (
        <span style={{ color: expired ? '#991b1b' : 'inherit', fontWeight: expired ? 600 : 400 }}>
          {formatDate(value as string)}
        </span>
      );
    },
  },
];

// ---------------------------------------------------------------------------
// Filter definitions
// ---------------------------------------------------------------------------

const filters: FilterDef[] = [
  { key: 'softwareId', label: 'Software', type: 'text' },
  { key: 'licenseType', label: 'License Type', type: 'text' },
];

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const pageHeader: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  marginBottom: '0.75rem',
};

const pageTitle: CSSProperties = {
  fontSize: '1.25rem',
  fontWeight: 700,
  margin: 0,
  color: 'var(--tblr-body-color, #1e293b)',
};

// ---------------------------------------------------------------------------
// LicenseListPage
// ---------------------------------------------------------------------------

export default function LicenseListPage() {
  return (
    <main>
      <div style={pageHeader}>
        <h1 style={pageTitle}>Software Licenses</h1>
      </div>

      <SearchEngine<SoftwareLicense>
        endpoint={ASSETS.LICENSES}
        columns={columns}
        defaultSort={{ field: 'name', order: 'asc' }}
        filters={filters}
        pageSize={50}
      />
    </main>
  );
}
