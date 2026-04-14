import { useNavigate } from 'react-router-dom';
import SearchEngine, {
  type ColumnDef,
  type FilterDef,
  type BulkActionDef,
} from '@/components/common/SearchEngine';
import StatusBadge from '@/components/common/StatusBadge';
import PriorityBadge from '@/components/common/PriorityBadge';
import { TICKETS } from '@/api/endpoints';
import { formatDate } from '@/utils/formatters';
import type { Ticket, Actor } from '@/hooks/useTickets';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Returns the display name of the first actor matching the given actorType. */
function getActorName(actors: Actor[], actorType: number): string {
  const actor = actors.find((a) => a.actorType === actorType);
  return actor?.displayName ?? actor?.actorId ?? '—';
}

// ---------------------------------------------------------------------------
// Column definitions
// ---------------------------------------------------------------------------

const columns: ColumnDef<Ticket>[] = [
  {
    key: 'id',
    label: 'ID',
    sortable: true,
    width: '70px',
  },
  {
    key: 'status',
    label: 'Status',
    sortable: true,
    width: '120px',
    render: (value) => <StatusBadge code={value as number} size="sm" />,
  },
  {
    key: 'title',
    label: 'Title',
    sortable: true,
    render: (value) => (
      <span style={{ fontWeight: 500 }}>{value as string}</span>
    ),
  },
  {
    key: 'actors',
    label: 'Requester',
    sortable: false,
    width: '140px',
    render: (_value, item) => getActorName(item.actors, 1),
  },
  {
    key: 'actors',
    label: 'Assigned',
    sortable: false,
    width: '140px',
    render: (_value, item) => getActorName(item.actors, 2),
  },
  {
    key: 'priority',
    label: 'Priority',
    sortable: true,
    width: '120px',
    render: (value) => <PriorityBadge code={value as number} size="sm" />,
  },
  {
    key: 'updatedAt',
    label: 'Last Update',
    sortable: true,
    width: '160px',
    render: (value) =>
      value ? formatDate(value as string) : '—',
  },
  {
    key: 'entityId',
    label: 'Entity',
    sortable: true,
    width: '120px',
    render: (value) => (value as string) || '—',
  },
];

// ---------------------------------------------------------------------------
// Filter definitions
// ---------------------------------------------------------------------------

const filters: FilterDef[] = [
  {
    key: 'status',
    label: 'Status',
    type: 'select',
    options: [
      { value: '1', label: 'New' },
      { value: '2', label: 'Assigned' },
      { value: '3', label: 'Planned' },
      { value: '4', label: 'Pending' },
      { value: '5', label: 'Solved' },
      { value: '6', label: 'Closed' },
    ],
  },
  {
    key: 'priority',
    label: 'Priority',
    type: 'select',
    options: [
      { value: '1', label: 'Very Low' },
      { value: '2', label: 'Low' },
      { value: '3', label: 'Medium' },
      { value: '4', label: 'High' },
      { value: '5', label: 'Very High' },
      { value: '6', label: 'Major' },
    ],
  },
  {
    key: 'entityId',
    label: 'Entity',
    type: 'text',
  },
  {
    key: 'assignedUserId',
    label: 'Assigned User',
    type: 'text',
  },
  {
    key: 'categoryId',
    label: 'Category',
    type: 'text',
  },
];

// ---------------------------------------------------------------------------
// TicketListPage
// ---------------------------------------------------------------------------

export default function TicketListPage() {
  const navigate = useNavigate();

  const bulkActions: BulkActionDef[] = [
    {
      key: 'change-status',
      label: 'Change Status',
      onAction: (ids) => {
        // TODO: open a status change dialog for the selected ticket IDs
        console.info('Change status for tickets:', ids);
      },
    },
    {
      key: 'assign',
      label: 'Assign',
      onAction: (ids) => {
        // TODO: open an assign dialog for the selected ticket IDs
        console.info('Assign tickets:', ids);
      },
    },
    {
      key: 'delete',
      label: 'Delete',
      variant: 'danger',
      onAction: (ids) => {
        // TODO: confirm and send DELETE requests for the selected ticket IDs
        console.info('Delete tickets:', ids);
      },
    },
  ];

  return (
    <main>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '1rem',
        }}
      >
        <h1
          style={{
            fontSize: '1.25rem',
            fontWeight: 700,
            margin: 0,
            color: 'var(--tblr-body-color, #1e293b)',
          }}
        >
          Tickets
        </h1>
        <button
          type="button"
          onClick={() => navigate('/tickets/new')}
          style={{
            padding: '0.4rem 1rem',
            background: 'var(--tblr-primary, rgb(254,201,92))',
            color: 'var(--tblr-primary-fg, #1e293b)',
            border: 'none',
            borderRadius: '4px',
            fontWeight: 600,
            fontSize: '0.8125rem',
            cursor: 'pointer',
            minHeight: '36px',
          }}
        >
          + New Ticket
        </button>
      </div>

      <SearchEngine<Ticket>
        endpoint={TICKETS.LIST}
        columns={columns}
        defaultSort={{ field: 'updatedAt', order: 'desc' }}
        filters={filters}
        bulkActions={bulkActions}
        onRowClick={(ticket) => navigate(`/tickets/${ticket.id}`)}
        pageSize={50}
      />
    </main>
  );
}
