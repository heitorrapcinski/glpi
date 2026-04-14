import type { CSSProperties } from 'react';
import { useNavigate } from 'react-router-dom';
import SearchEngine, {
  type ColumnDef,
  type FilterDef,
} from '@/components/common/SearchEngine';
import { ASSETS } from '@/api/endpoints';
import type { Software } from '@/hooks/useAssets';

// ---------------------------------------------------------------------------
// Column definitions
// ---------------------------------------------------------------------------

const columns: ColumnDef<Software>[] = [
  {
    key: 'name',
    label: 'Name',
    sortable: true,
    render: (value) => (
      <span style={{ fontWeight: 500 }}>{value as string}</span>
    ),
  },
  {
    key: 'manufacturerId',
    label: 'Manufacturer',
    sortable: true,
    width: '160px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'categoryId',
    label: 'Category',
    sortable: true,
    width: '150px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'installationsCount',
    label: 'Installations',
    sortable: true,
    width: '120px',
    render: (value) => String(value ?? 0),
  },
  {
    key: 'licensesCount',
    label: 'Licenses',
    sortable: true,
    width: '100px',
    render: (value) => String(value ?? 0),
  },
];

// ---------------------------------------------------------------------------
// Filter definitions
// ---------------------------------------------------------------------------

const filters: FilterDef[] = [
  { key: 'manufacturerId', label: 'Manufacturer', type: 'text' },
  { key: 'categoryId', label: 'Category', type: 'text' },
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
// SoftwareListPage
// ---------------------------------------------------------------------------

export default function SoftwareListPage() {
  const navigate = useNavigate();

  return (
    <main>
      <div style={pageHeader}>
        <h1 style={pageTitle}>Software</h1>
      </div>

      <SearchEngine<Software>
        endpoint={ASSETS.SOFTWARE}
        columns={columns}
        defaultSort={{ field: 'name', order: 'asc' }}
        filters={filters}
        onRowClick={(sw) => navigate(`/assets/Software/${sw.id}`)}
        pageSize={50}
      />
    </main>
  );
}
