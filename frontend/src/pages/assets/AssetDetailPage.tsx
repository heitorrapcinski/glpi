import { useState } from 'react';
import type { CSSProperties } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAssetDetail } from '@/hooks/useAssets';
import type { Asset, NetworkPort, Infocom } from '@/hooks/useAssets';
import { formatDate } from '@/utils/formatters';

// ---------------------------------------------------------------------------
// Tab definitions
// ---------------------------------------------------------------------------

type TabKey =
  | 'general'
  | 'components'
  | 'software'
  | 'network-ports'
  | 'financial'
  | 'contracts'
  | 'linked-tickets'
  | 'history';

interface TabDef {
  key: TabKey;
  label: string;
  /** Only show for these asset types. undefined = show for all. */
  onlyFor?: string[];
}

const ALL_TABS: TabDef[] = [
  { key: 'general', label: 'General Information' },
  { key: 'components', label: 'Components', onlyFor: ['Computer'] },
  { key: 'software', label: 'Software', onlyFor: ['Computer'] },
  { key: 'network-ports', label: 'Network Ports' },
  { key: 'financial', label: 'Financial Information' },
  { key: 'contracts', label: 'Contracts' },
  { key: 'linked-tickets', label: 'Linked Tickets' },
  { key: 'history', label: 'History' },
];

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const pageContainer: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  height: '100%',
  minHeight: 0,
};

const pageHeader: CSSProperties = {
  padding: '1rem 1.25rem 0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  flexShrink: 0,
};

const headerMeta: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  marginBottom: '0.25rem',
};

const assetIdBadge: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.15rem 0.5rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.75rem',
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
  fontFamily: 'monospace',
};

const assetTypeBadge: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.15rem 0.5rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  fontSize: '0.75rem',
  fontWeight: 600,
};

const assetTitle: CSSProperties = {
  margin: 0,
  fontSize: '1.125rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  lineHeight: 1.3,
};

const backBtn: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.3rem',
  padding: '0.3rem 0.6rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  fontSize: '0.8125rem',
  cursor: 'pointer',
  fontWeight: 500,
  marginBottom: '0.5rem',
};

const tabBar: CSSProperties = {
  display: 'flex',
  gap: '0.25rem',
  borderBottom: '2px solid var(--tblr-border-color, #d9dbde)',
  overflowX: 'auto',
  flexWrap: 'nowrap',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  paddingInline: '1.25rem',
  flexShrink: 0,
};

function tabBtnStyle(active: boolean): CSSProperties {
  return {
    padding: '0.5rem 0.9rem',
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
    fontSize: '0.8125rem',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    transition: 'color 0.15s, border-color 0.15s',
  };
}

const tabContent: CSSProperties = {
  flex: 1,
  overflowY: 'auto',
  padding: '1.25rem',
};

const loadingState: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '3rem',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.9375rem',
};

const errorState: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '3rem',
  gap: '1rem',
  color: 'var(--tblr-danger, #ef4444)',
};

const retryBtn: CSSProperties = {
  padding: '0.4rem 0.9rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-danger, #ef4444)',
  backgroundColor: 'transparent',
  color: 'var(--tblr-danger, #ef4444)',
  cursor: 'pointer',
  fontWeight: 600,
  fontSize: '0.875rem',
};

const card: CSSProperties = {
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
  padding: '1rem',
  marginBottom: '1rem',
};

const fieldRow: CSSProperties = {
  display: 'flex',
  padding: '0.4rem 0',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.875rem',
};

const fieldLabel: CSSProperties = {
  flex: '0 0 180px',
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
};

const fieldValue: CSSProperties = {
  flex: 1,
  color: 'var(--tblr-body-color, #1e293b)',
};

const tableStyle: CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: '0.875rem',
};

const thStyle: CSSProperties = {
  textAlign: 'left',
  padding: '0.5rem 0.75rem',
  borderBottom: '2px solid var(--tblr-border-color, #d9dbde)',
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.8125rem',
};

const tdStyle: CSSProperties = {
  padding: '0.5rem 0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  color: 'var(--tblr-body-color, #1e293b)',
};

/* linkedItemLink kept for future use when linked ITIL objects are fetched */

const emptyState: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.875rem',
};

// ---------------------------------------------------------------------------
// Tab content components
// ---------------------------------------------------------------------------

function GeneralInfoTab({ asset }: { asset: Asset }) {
  const fields: { label: string; value: string }[] = [
    { label: 'Name', value: asset.name },
    { label: 'Type', value: asset.assetType },
    { label: 'Status', value: asset.stateId || '—' },
    { label: 'Location', value: asset.locationId || '—' },
    { label: 'Assigned User', value: asset.userId || '—' },
    { label: 'Assigned Group', value: asset.groupId || '—' },
    { label: 'Manufacturer', value: asset.manufacturerId || '—' },
    { label: 'Model', value: asset.modelId || '—' },
    { label: 'Serial Number', value: asset.serial || '—' },
    { label: 'Inventory Number', value: asset.otherSerial || '—' },
    { label: 'Entity', value: asset.entityId || '—' },
    { label: 'Created', value: asset.createdAt ? formatDate(asset.createdAt) : '—' },
    { label: 'Last Update', value: asset.updatedAt ? formatDate(asset.updatedAt) : '—' },
  ];

  return (
    <div style={card}>
      {fields.map((f) => (
        <div key={f.label} style={fieldRow}>
          <span style={fieldLabel}>{f.label}</span>
          <span style={fieldValue}>{f.value}</span>
        </div>
      ))}
    </div>
  );
}

function ComponentsTab({ asset }: { asset: Asset }) {
  const details = asset.computerDetails;
  if (!details) {
    return <div style={emptyState}>No component data available.</div>;
  }

  const sections: { label: string; items: string[] }[] = [
    { label: 'CPU', items: details.cpus },
    { label: 'RAM', items: details.ram },
    { label: 'HDD', items: details.hdds },
    { label: 'GPU', items: details.gpus },
  ];

  return (
    <>
      {sections.map((section) => (
        <div key={section.label} style={card}>
          <h3 style={{ margin: '0 0 0.5rem', fontSize: '0.9375rem', fontWeight: 700, color: 'var(--tblr-body-color, #1e293b)' }}>
            {section.label}
          </h3>
          {section.items.length === 0 ? (
            <div style={{ color: 'var(--tblr-secondary, #606f91)', fontSize: '0.875rem' }}>None</div>
          ) : (
            <table style={tableStyle}>
              <thead>
                <tr>
                  <th style={thStyle}>Component</th>
                </tr>
              </thead>
              <tbody>
                {section.items.map((item, idx) => (
                  <tr key={idx}>
                    <td style={tdStyle}>{item}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ))}
    </>
  );
}

function SoftwareTab() {
  // Software installations for this computer — requires a dedicated endpoint
  return (
    <div style={emptyState}>
      Software installations will be loaded from the asset software endpoint.
    </div>
  );
}

function NetworkPortsTab({ ports }: { ports: NetworkPort[] }) {
  if (ports.length === 0) {
    return <div style={emptyState}>No network ports configured.</div>;
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>Port Name</th>
            <th style={thStyle}>MAC Address</th>
            <th style={thStyle}>IP Address</th>
            <th style={thStyle}>VLAN</th>
            <th style={thStyle}>Connection Type</th>
          </tr>
        </thead>
        <tbody>
          {ports.map((port) => (
            <tr key={port.id}>
              <td style={tdStyle}>{port.name || '—'}</td>
              <td style={{ ...tdStyle, fontFamily: 'monospace' }}>{port.macAddress || '—'}</td>
              <td style={{ ...tdStyle, fontFamily: 'monospace' }}>{port.ipAddress || '—'}</td>
              <td style={tdStyle}>{port.vlan || '—'}</td>
              <td style={tdStyle}>{port.connectionType || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function FinancialInfoTab({ infocom }: { infocom: Infocom | null }) {
  if (!infocom) {
    return <div style={emptyState}>No financial information available.</div>;
  }

  const fields: { label: string; value: string }[] = [
    { label: 'Purchase Date', value: infocom.purchaseDate ? formatDate(infocom.purchaseDate) : '—' },
    { label: 'Purchase Price', value: infocom.purchasePrice != null ? `$${infocom.purchasePrice.toFixed(2)}` : '—' },
    { label: 'Warranty Expiry', value: infocom.warrantyExpiry ? formatDate(infocom.warrantyExpiry) : '—' },
    { label: 'Order Number', value: infocom.orderNumber || '—' },
    { label: 'Delivery Date', value: infocom.deliveryDate ? formatDate(infocom.deliveryDate) : '—' },
  ];

  return (
    <div style={card}>
      {fields.map((f) => (
        <div key={f.label} style={fieldRow}>
          <span style={fieldLabel}>{f.label}</span>
          <span style={fieldValue}>{f.value}</span>
        </div>
      ))}
    </div>
  );
}

function ContractsTab({ contractIds }: { contractIds: string[] }) {
  if (contractIds.length === 0) {
    return <div style={emptyState}>No contracts linked to this asset.</div>;
  }

  return (
    <div style={card}>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>Contract ID</th>
          </tr>
        </thead>
        <tbody>
          {contractIds.map((cid) => (
            <tr key={cid}>
              <td style={tdStyle}>{cid}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function LinkedTicketsTab() {
  // Linked tickets/problems/changes — navigable links
  // In a full implementation this would fetch linked ITIL objects from the API
  return (
    <div style={emptyState}>
      <p style={{ marginBottom: '0.5rem' }}>
        Linked ITIL objects will be loaded from the asset relationships endpoint.
      </p>
      <p style={{ fontSize: '0.8125rem', color: 'var(--tblr-secondary, #606f91)' }}>
        Navigate to linked{' '}
        <Link to="/tickets" style={{ color: 'var(--tblr-link-color, #3a5693)' }}>Tickets</Link>,{' '}
        <Link to="/problems" style={{ color: 'var(--tblr-link-color, #3a5693)' }}>Problems</Link>, or{' '}
        <Link to="/changes" style={{ color: 'var(--tblr-link-color, #3a5693)' }}>Changes</Link>.
      </p>
    </div>
  );
}

function HistoryTab() {
  return (
    <div style={emptyState}>
      Asset history will be loaded from the asset history endpoint.
    </div>
  );
}

// ---------------------------------------------------------------------------
// Responsive style injection
// ---------------------------------------------------------------------------

const RESPONSIVE_STYLE_ID = 'asset-detail-responsive';

function ensureResponsiveStyles() {
  if (typeof document === 'undefined') return;
  if (document.getElementById(RESPONSIVE_STYLE_ID)) return;
  const style = document.createElement('style');
  style.id = RESPONSIVE_STYLE_ID;
  style.textContent = `
    @media (max-width: 767px) {
      .asset-detail-tab-bar {
        gap: 0 !important;
      }
    }
  `;
  document.head.appendChild(style);
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function AssetDetailPage() {
  ensureResponsiveStyles();

  const { type, id } = useParams<{ type: string; id: string }>();
  const navigate = useNavigate();

  const assetType = type ?? '';
  const assetId = id ?? '';

  const [activeTab, setActiveTab] = useState<TabKey>('general');

  const { data: assetResponse, isLoading, isError, refetch } = useAssetDetail(assetType, assetId);
  const asset = assetResponse?.data;

  // Filter tabs based on asset type
  const visibleTabs = ALL_TABS.filter(
    (tab) => !tab.onlyFor || tab.onlyFor.includes(assetType),
  );

  // Reset to general if current tab is not visible for this type
  const currentTab = visibleTabs.find((t) => t.key === activeTab)
    ? activeTab
    : 'general';

  // ---- Loading state ----
  if (isLoading) {
    return (
      <div style={pageContainer} role="main" aria-label="Asset detail">
        <div style={loadingState} aria-live="polite" aria-busy="true">
          Loading asset…
        </div>
      </div>
    );
  }

  // ---- Error state ----
  if (isError || !asset) {
    return (
      <div style={pageContainer} role="main" aria-label="Asset detail">
        <div style={errorState} role="alert">
          <span>Failed to load asset.</span>
          <button type="button" style={retryBtn} onClick={() => refetch()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  // ---- Tab content renderer ----
  function renderTabContent() {
    switch (currentTab) {
      case 'general':
        return <GeneralInfoTab asset={asset!} />;
      case 'components':
        return <ComponentsTab asset={asset!} />;
      case 'software':
        return <SoftwareTab />;
      case 'network-ports':
        return <NetworkPortsTab ports={asset!.networkPorts} />;
      case 'financial':
        return <FinancialInfoTab infocom={asset!.infocom} />;
      case 'contracts':
        return <ContractsTab contractIds={asset!.contractIds} />;
      case 'linked-tickets':
        return <LinkedTicketsTab />;
      case 'history':
        return <HistoryTab />;
      default:
        return null;
    }
  }

  return (
    <div style={pageContainer} role="main" aria-label={`Asset #${asset.id}: ${asset.name}`}>
      {/* Header */}
      <header style={pageHeader}>
        <button
          type="button"
          style={backBtn}
          aria-label="Back to asset list"
          onClick={() => navigate('/assets')}
        >
          ← Back
        </button>
        <div style={headerMeta}>
          <span style={assetIdBadge} aria-label={`Asset ID ${asset.id}`}>
            #{asset.id}
          </span>
          <span style={assetTypeBadge}>{asset.assetType}</span>
        </div>
        <h1 style={assetTitle}>{asset.name}</h1>
      </header>

      {/* Tab bar */}
      <nav style={tabBar} className="asset-detail-tab-bar" aria-label="Asset detail tabs">
        {visibleTabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            style={tabBtnStyle(currentTab === tab.key)}
            aria-selected={currentTab === tab.key}
            role="tab"
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {/* Tab content */}
      <div style={tabContent} role="tabpanel" aria-label={`${visibleTabs.find((t) => t.key === currentTab)?.label ?? ''} tab content`}>
        {renderTabContent()}
      </div>
    </div>
  );
}
