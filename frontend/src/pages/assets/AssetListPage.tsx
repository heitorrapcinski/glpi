import { useState } from 'react';
import type { CSSProperties } from 'react';
import { useNavigate } from 'react-router-dom';
import SearchEngine, {
  type ColumnDef,
  type FilterDef,
  type BulkActionDef,
} from '@/components/common/SearchEngine';
import { ASSETS } from '@/api/endpoints';
import { formatDate } from '@/utils/formatters';
import type { Asset, AssetType } from '@/hooks/useAssets';

// ---------------------------------------------------------------------------
// Asset type tab definitions
// ---------------------------------------------------------------------------

interface AssetTab {
  key: 'all' | AssetType;
  label: string;
}

const ASSET_TABS: AssetTab[] = [
  { key: 'all', label: 'All' },
  { key: 'Computer', label: 'Computers' },
  { key: 'NetworkEquipment', label: 'Network Equipment' },
  { key: 'Monitor', label: 'Monitors' },
  { key: 'Printer', label: 'Printers' },
  { key: 'Phone', label: 'Phones' },
  { key: 'Peripheral', label: 'Peripherals' },
  { key: 'Software', label: 'Software' },
];

// ---------------------------------------------------------------------------
// Column definitions
// ---------------------------------------------------------------------------

const columns: ColumnDef<Asset>[] = [
  {
    key: 'id',
    label: 'ID',
    sortable: true,
    width: '70px',
  },
  {
    key: 'name',
    label: 'Name',
    sortable: true,
    render: (value) => (
      <span style={{ fontWeight: 500 }}>{value as string}</span>
    ),
  },
  {
    key: 'assetType',
    label: 'Type',
    sortable: true,
    width: '140px',
    render: (value) => {
      const type = value as AssetType;
      const labels: Record<AssetType, string> = {
        Computer: 'Computer',
        NetworkEquipment: 'Network Equipment',
        Monitor: 'Monitor',
        Printer: 'Printer',
        Phone: 'Phone',
        Peripheral: 'Peripheral',
        Software: 'Software',
      };
      return labels[type] ?? type;
    },
  },
  {
    key: 'stateId',
    label: 'Status',
    sortable: true,
    width: '120px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'locationId',
    label: 'Location',
    sortable: true,
    width: '130px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'userId',
    label: 'Assigned User',
    sortable: true,
    width: '140px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'entityId',
    label: 'Entity',
    sortable: true,
    width: '120px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'serial',
    label: 'Serial',
    sortable: true,
    width: '130px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'updatedAt',
    label: 'Last Update',
    sortable: true,
    width: '160px',
    render: (value) => (value ? formatDate(value as string) : '—'),
  },
];

// ---------------------------------------------------------------------------
// Filter definitions
// ---------------------------------------------------------------------------

const filters: FilterDef[] = [
  {
    key: 'stateId',
    label: 'Status',
    type: 'text',
  },
  {
    key: 'locationId',
    label: 'Location',
    type: 'text',
  },
  {
    key: 'userId',
    label: 'Assigned User',
    type: 'text',
  },
  {
    key: 'entityId',
    label: 'Entity',
    type: 'text',
  },
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

const tabBar: CSSProperties = {
  display: 'flex',
  gap: '0.25rem',
  marginBottom: '1rem',
  borderBottom: '2px solid var(--tblr-border-color, #d9dbde)',
  overflowX: 'auto',
  flexWrap: 'nowrap',
};

function tabStyle(active: boolean): CSSProperties {
  return {
    padding: '0.45rem 0.9rem',
    border: 'none',
    borderBottom: active
      ? '2px solid var(--tblr-primary, rgb(254,201,92))'
      : '2px solid transparent',
    marginBottom: '-2px',
    backgroundColor: 'transparent',
    color: active
      ? 'var(--tblr-body-color, #1e293b)'
      : 'var(--tblr-secondary, #606f91)',
    fontWeight: active ? 700 : 400,
    fontSize: '0.875rem',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    transition: 'color 0.15s, border-color 0.15s',
  };
}

// ---------------------------------------------------------------------------
// AssetListPage
// ---------------------------------------------------------------------------

export default function AssetListPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'all' | AssetType>('all');

  // Derive endpoint based on active tab
  const endpoint =
    activeTab === 'all' ? ASSETS.LIST : ASSETS.BY_TYPE(activeTab);

  const bulkActions: BulkActionDef[] = [
    {
      key: 'delete',
      label: 'Delete',
      variant: 'danger',
      onAction: (ids) => {
        // TODO: confirm and send DELETE requests for the selected asset IDs
        console.info('Delete assets:', ids);
      },
    },
  ];

  return (
    <main>
      <div style={pageHeader}>
        <h1 style={pageTitle}>Assets</h1>
      </div>

      {/* Type filter tab bar */}
      <nav style={tabBar} aria-label="Asset type filter">
        {ASSET_TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            style={tabStyle(activeTab === tab.key)}
            aria-pressed={activeTab === tab.key}
            aria-label={`Filter by ${tab.label}`}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <SearchEngine<Asset>
        key={activeTab}
        endpoint={endpoint}
        columns={columns}
        defaultSort={{ field: 'updatedAt', order: 'desc' }}
        filters={filters}
        bulkActions={bulkActions}
        onRowClick={(asset) => navigate(`/assets/${asset.assetType}/${asset.id}`)}
        pageSize={50}
      />
    </main>
  );
}
