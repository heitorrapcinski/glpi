import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { useTicketList, type Ticket } from '../hooks/useTickets';
import StatusBadge from '../components/common/StatusBadge';
import { formatDate } from '../utils/formatters';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

/** Open statuses: New (1), Assigned (2), Planned (3), Pending (4) */
const OPEN_STATUSES = [1, 2, 3, 4];
/** Solved status: 5 */
const SOLVED_STATUS = 5;

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const containerStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  minHeight: '100%',
  margin: 'calc(var(--glpi-content-margin, 24px) * -1)',
};

const searchBannerStyle: React.CSSProperties = {
  backgroundColor: 'var(--glpi-helpdesk-header)',
  padding: '5rem 1.5rem 3rem',
  textAlign: 'center',
};

const bannerTitleStyle: React.CSSProperties = {
  fontSize: '3.7rem',
  fontWeight: 700,
  color: 'var(--glpi-mainmenu-bg, #2f3f64)',
  marginBottom: '1.5rem',
  lineHeight: 1.1,
};

const searchInputStyle: React.CSSProperties = {
  maxWidth: '600px',
  margin: '0 auto',
  padding: '0.75rem 1rem 0.75rem 2.75rem',
  fontSize: '1rem',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  borderRadius: '0.375rem',
  width: '100%',
  backgroundColor: '#fff',
  lineHeight: '26px',
  outline: 'none',
};

const searchWrapperStyle: React.CSSProperties = {
  position: 'relative',
  maxWidth: '600px',
  margin: '0 auto',
};

const searchIconStyle: React.CSSProperties = {
  position: 'absolute',
  left: '0.875rem',
  top: '50%',
  transform: 'translateY(-50%)',
  color: 'var(--glpi-mainmenu-bg, #2f3f64)',
  fontSize: '1.1rem',
  pointerEvents: 'none',
};

const tilesBannerStyle: React.CSSProperties = {
  backgroundColor: 'var(--glpi-helpdesk-tiles-section-bg, #f6f8fb)',
  padding: '4.375rem 1.5rem',
};

const tilesGridStyle: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
  gap: '1rem',
  maxWidth: '1140px',
  margin: '0 auto',
};

const tileCardStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '1rem',
  padding: '1.25rem 1.5rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  borderRadius: '0.375rem',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  textDecoration: 'none',
  color: 'inherit',
  transition: 'box-shadow 0.15s ease',
};

const tileIconStyle: React.CSSProperties = {
  fontSize: '2rem',
  flexShrink: 0,
  width: '70px',
  height: '70px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  borderRadius: '0.375rem',
  backgroundColor: 'var(--glpi-helpdesk-tiles-section-bg, #f6f8fb)',
};

const tileTitleStyle: React.CSSProperties = {
  fontSize: '1.125rem',
  fontWeight: 600,
  marginBottom: '0.25rem',
  color: 'var(--tblr-body-color, #1e293b)',
};

const tileDescStyle: React.CSSProperties = {
  fontSize: '0.875rem',
  color: 'var(--tblr-secondary, #606f91)',
  margin: 0,
};

const ticketsBannerStyle: React.CSSProperties = {
  backgroundColor: 'var(--glpi-helpdesk-tabs-section-bg, #fff)',
  padding: '4.375rem 1.5rem',
  flexGrow: 1,
};

const ticketsContainerStyle: React.CSSProperties = {
  maxWidth: '1140px',
  margin: '0 auto',
};

const tabBarStyle: React.CSSProperties = {
  display: 'flex',
  borderBottom: '1px solid var(--tblr-border-color, #e6e7e9)',
  marginBottom: 0,
};

const tabButtonBase: React.CSSProperties = {
  padding: '0.75rem 1.25rem',
  fontSize: '0.875rem',
  fontWeight: 500,
  border: '1px solid transparent',
  borderBottom: 'none',
  background: 'none',
  cursor: 'pointer',
  color: 'var(--tblr-body-color, #1e293b)',
  borderTopLeftRadius: '0.375rem',
  borderTopRightRadius: '0.375rem',
  marginBottom: '-1px',
};

const tabButtonActive: React.CSSProperties = {
  ...tabButtonBase,
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  borderColor: 'var(--tblr-border-color, #e6e7e9)',
  borderBottomColor: 'var(--tblr-bg-surface, #fff)',
  fontWeight: 600,
};

const tableStyle: React.CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  borderTop: 'none',
  borderTopLeftRadius: 0,
  borderTopRightRadius: 0,
};

const thStyle: React.CSSProperties = {
  padding: '0.75rem 1rem',
  fontSize: '0.75rem',
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  borderBottom: '1px solid var(--tblr-border-color, #e6e7e9)',
  textAlign: 'left',
};

const tdStyle: React.CSSProperties = {
  padding: '0.75rem 1rem',
  fontSize: '0.875rem',
  borderBottom: '1px solid var(--tblr-border-color, #e6e7e9)',
  color: 'var(--tblr-body-color, #1e293b)',
};

const emptyStyle: React.CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.875rem',
};

const linkRowStyle: React.CSSProperties = {
  cursor: 'pointer',
};

// ---------------------------------------------------------------------------
// Tile definitions
// ---------------------------------------------------------------------------

const TILES = [
  {
    to: '/tickets/new',
    icon: '🎫',
    title: 'Create a Ticket',
    description: 'Report an incident or submit a service request.',
  },
  {
    to: '/tickets',
    icon: '📋',
    title: 'My Tickets',
    description: 'View and track your open and past tickets.',
  },
  {
    to: '/knowledge',
    icon: '❓',
    title: 'FAQ',
    description: 'Browse frequently asked questions and knowledge articles.',
  },
] as const;

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

interface TicketTableProps {
  tickets: Ticket[];
  isLoading: boolean;
  emptyMessage: string;
  onRowClick: (id: string) => void;
}

function TicketTable({ tickets, isLoading, emptyMessage, onRowClick }: TicketTableProps) {
  if (isLoading) {
    return <div style={emptyStyle}>Loading tickets…</div>;
  }

  if (tickets.length === 0) {
    return <div style={emptyStyle}>{emptyMessage}</div>;
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={tableStyle} role="grid" aria-label="Tickets">
        <thead>
          <tr>
            <th style={thStyle}>ID</th>
            <th style={thStyle}>Status</th>
            <th style={thStyle}>Title</th>
            <th style={thStyle}>Last Update</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map((ticket) => (
            <tr
              key={ticket.id}
              style={linkRowStyle}
              onClick={() => onRowClick(ticket.id)}
              tabIndex={0}
              role="row"
              aria-label={`Ticket ${ticket.id}: ${ticket.title}`}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  onRowClick(ticket.id);
                }
              }}
            >
              <td style={tdStyle}>#{ticket.id}</td>
              <td style={tdStyle}><StatusBadge code={ticket.status} size="sm" /></td>
              <td style={tdStyle}>{ticket.title}</td>
              <td style={tdStyle}>{formatDate(ticket.updatedAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ---------------------------------------------------------------------------
// HelpdeskHomePage
// ---------------------------------------------------------------------------

type TabKey = 'open' | 'solved';

export default function HelpdeskHomePage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [activeTab, setActiveTab] = useState<TabKey>('open');
  const [searchValue, setSearchValue] = useState('');

  // Fetch open tickets for the current user
  const openTickets = useTicketList({
    status: OPEN_STATUSES,
    pageSize: 10,
    sort: 'updatedAt',
    order: 'desc',
  });

  // Fetch recently solved tickets for the current user
  const solvedTickets = useTicketList({
    status: [SOLVED_STATUS],
    pageSize: 10,
    sort: 'updatedAt',
    order: 'desc',
  });

  const handleRowClick = (id: string) => {
    navigate(`/tickets/${id}`);
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchValue.trim()) {
      navigate(`/knowledge?search=${encodeURIComponent(searchValue.trim())}`);
    }
  };

  const greeting = user?.username
    ? `How can we help you, ${user.username}?`
    : 'How can we help you?';

  return (
    <div style={containerStyle}>
      {/* Search Banner — Req 4.2 */}
      <section style={searchBannerStyle} aria-label="Search">
        <h1 style={bannerTitleStyle} data-testid="home-title">
          {greeting}
        </h1>
        <form onSubmit={handleSearchSubmit} role="search">
          <div style={searchWrapperStyle}>
            <span style={searchIconStyle} aria-hidden="true">🔍</span>
            <input
              type="text"
              style={searchInputStyle}
              placeholder="Search for knowledge base entries or forms"
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              aria-label="Search knowledge base"
              data-testid="home-search"
            />
          </div>
        </form>
      </section>

      {/* Tile Cards — Req 4.3 */}
      <section style={tilesBannerStyle} aria-label="Quick Access" data-testid="quick-access">
        <div style={tilesGridStyle}>
          {TILES.map((tile) => (
            <Link
              key={tile.to}
              to={tile.to}
              style={tileCardStyle}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLElement).style.boxShadow =
                  'var(--tblr-box-shadow-input, 0 1px 3px rgba(0,0,0,0.08))';
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.boxShadow = 'none';
              }}
            >
              <div style={tileIconStyle} aria-hidden="true">
                {tile.icon}
              </div>
              <div>
                <h2 style={tileTitleStyle}>{tile.title}</h2>
                <p style={tileDescStyle}>{tile.description}</p>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Tabbed Tickets Section — Req 4.4 */}
      <section style={ticketsBannerStyle} aria-label="Your Tickets">
        <div style={ticketsContainerStyle}>
          <div style={tabBarStyle} role="tablist" aria-label="Ticket tabs">
            <button
              role="tab"
              aria-selected={activeTab === 'open'}
              aria-controls="open-tickets-pane"
              id="tab-open"
              style={activeTab === 'open' ? tabButtonActive : tabButtonBase}
              onClick={() => setActiveTab('open')}
            >
              Open Tickets
            </button>
            <button
              role="tab"
              aria-selected={activeTab === 'solved'}
              aria-controls="solved-tickets-pane"
              id="tab-solved"
              style={activeTab === 'solved' ? tabButtonActive : tabButtonBase}
              onClick={() => setActiveTab('solved')}
            >
              Recently Solved
            </button>
          </div>

          <div
            id="open-tickets-pane"
            role="tabpanel"
            aria-labelledby="tab-open"
            hidden={activeTab !== 'open'}
          >
            <TicketTable
              tickets={(openTickets.data?.data as Ticket[]) ?? []}
              isLoading={openTickets.isLoading}
              emptyMessage="No open tickets."
              onRowClick={handleRowClick}
            />
          </div>

          <div
            id="solved-tickets-pane"
            role="tabpanel"
            aria-labelledby="tab-solved"
            hidden={activeTab !== 'solved'}
          >
            <TicketTable
              tickets={(solvedTickets.data?.data as Ticket[]) ?? []}
              isLoading={solvedTickets.isLoading}
              emptyMessage="No recently solved tickets."
              onRowClick={handleRowClick}
            />
          </div>
        </div>
      </section>
    </div>
  );
}
