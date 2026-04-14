import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import SearchEngine, { type ColumnDef, type FilterDef, type BulkActionDef } from './SearchEngine';

// ---------------------------------------------------------------------------
// Mock the api client
// ---------------------------------------------------------------------------
vi.mock('../../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import api from '../../api/client';
const mockGet = vi.mocked(api.get);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

interface TestRow {
  id: string;
  name: string;
  status: string;
}

const columns: ColumnDef<TestRow>[] = [
  { key: 'id', label: 'ID', sortable: true, width: '60px' },
  { key: 'name', label: 'Name', sortable: true },
  { key: 'status', label: 'Status', render: (v) => <span data-testid="status">{String(v)}</span> },
];

function makePaginatedResponse(rows: TestRow[], total = rows.length, page = 1, size = 50) {
  return {
    data: {
      data: rows,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      currentPage: page,
      pageSize: size,
    },
  };
}

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

const sampleRows: TestRow[] = [
  { id: '1', name: 'Ticket A', status: 'open' },
  { id: '2', name: 'Ticket B', status: 'closed' },
  { id: '3', name: 'Ticket C', status: 'pending' },
];

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('SearchEngine', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders column headers', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse(sampleRows));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
  });

  it('renders rows from API response', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse(sampleRows));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });
    expect(screen.getByText('Ticket B')).toBeInTheDocument();
    expect(screen.getByText('Ticket C')).toBeInTheDocument();
  });

  it('uses custom render function for columns', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse(sampleRows));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      const badges = screen.getAllByTestId('status');
      expect(badges).toHaveLength(3);
      expect(badges[0]).toHaveTextContent('open');
    });
  });

  it('shows loading state', () => {
    mockGet.mockReturnValue(new Promise(() => {})); // never resolves

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    expect(screen.getByText('Loading…')).toBeInTheDocument();
  });

  it('shows empty state when no rows', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse([], 0));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('No results found')).toBeInTheDocument();
    });
  });

  it('shows error state on fetch failure', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });
  });

  it('calls onRowClick when a row is clicked', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse(sampleRows));
    const onClick = vi.fn();

    render(
      <SearchEngine endpoint="/tickets" columns={columns} onRowClick={onClick} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Ticket A'));
    expect(onClick).toHaveBeenCalledWith(sampleRows[0]);
  });

  it('toggles sort order on column header click', async () => {
    mockGet.mockResolvedValue(makePaginatedResponse(sampleRows));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });

    // Click Name header to sort ascending
    fireEvent.click(screen.getByText('Name'));

    await waitFor(() => {
      expect(mockGet).toHaveBeenCalledWith(
        '/tickets',
        expect.objectContaining({ sort: 'name', order: 'asc' }),
      );
    });

    // Click again to sort descending
    fireEvent.click(screen.getByText('Name'));

    await waitFor(() => {
      expect(mockGet).toHaveBeenCalledWith(
        '/tickets',
        expect.objectContaining({ sort: 'name', order: 'desc' }),
      );
    });
  });

  it('renders filter controls and sends filter params', async () => {
    mockGet.mockResolvedValue(makePaginatedResponse(sampleRows));

    const testFilters: FilterDef[] = [
      {
        key: 'status',
        label: 'Status',
        type: 'select',
        options: [
          { value: 'open', label: 'Open' },
          { value: 'closed', label: 'Closed' },
        ],
      },
    ];

    render(
      <SearchEngine endpoint="/tickets" columns={columns} filters={testFilters} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });

    const select = screen.getByLabelText('Status');
    fireEvent.change(select, { target: { value: 'open' } });

    await waitFor(() => {
      expect(mockGet).toHaveBeenCalledWith(
        '/tickets',
        expect.objectContaining({ status: 'open', page: 1 }),
      );
    });
  });

  it('renders checkboxes and bulk action bar when bulkActions provided', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse(sampleRows));
    const onAction = vi.fn();

    const actions: BulkActionDef[] = [
      { key: 'delete', label: 'Delete', onAction, variant: 'danger' },
    ];

    render(
      <SearchEngine endpoint="/tickets" columns={columns} bulkActions={actions} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });

    // Checkboxes should be present
    const checkboxes = screen.getAllByRole('checkbox');
    expect(checkboxes.length).toBe(4); // 1 select-all + 3 rows

    // Select first row
    fireEvent.click(checkboxes[1]);
    expect(screen.getByText('1 selected')).toBeInTheDocument();
    expect(screen.getByText('Delete')).toBeInTheDocument();

    // Execute bulk action
    fireEvent.click(screen.getByText('Delete'));
    expect(onAction).toHaveBeenCalledWith(['1']);
  });

  it('select-all checkbox toggles all rows', async () => {
    mockGet.mockResolvedValueOnce(makePaginatedResponse(sampleRows));
    const onAction = vi.fn();

    const actions: BulkActionDef[] = [
      { key: 'assign', label: 'Assign', onAction },
    ];

    render(
      <SearchEngine endpoint="/tickets" columns={columns} bulkActions={actions} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });

    const selectAll = screen.getByLabelText('Select all rows');
    fireEvent.click(selectAll);

    expect(screen.getByText('3 selected')).toBeInTheDocument();

    // Deselect all
    fireEvent.click(selectAll);
    expect(screen.queryByText('3 selected')).not.toBeInTheDocument();
  });

  it('renders pagination footer', async () => {
    const manyRows = Array.from({ length: 15 }, (_, i) => ({
      id: String(i + 1),
      name: `Item ${i + 1}`,
      status: 'open',
    }));
    mockGet.mockResolvedValueOnce(makePaginatedResponse(manyRows, 30, 1, 15));

    render(
      <SearchEngine endpoint="/tickets" columns={columns} pageSize={15} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('1–15 of 30')).toBeInTheDocument();
    });

    expect(screen.getByLabelText('Next page')).toBeInTheDocument();
  });

  it('renders text filter', async () => {
    mockGet.mockResolvedValue(makePaginatedResponse(sampleRows));

    const textFilters: FilterDef[] = [
      { key: 'search', label: 'Search', type: 'text' },
    ];

    render(
      <SearchEngine endpoint="/tickets" columns={columns} filters={textFilters} />,
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(screen.getByText('Ticket A')).toBeInTheDocument();
    });

    const input = screen.getByPlaceholderText('Search');
    fireEvent.change(input, { target: { value: 'test' } });

    await waitFor(() => {
      expect(mockGet).toHaveBeenCalledWith(
        '/tickets',
        expect.objectContaining({ search: 'test' }),
      );
    });
  });
});
