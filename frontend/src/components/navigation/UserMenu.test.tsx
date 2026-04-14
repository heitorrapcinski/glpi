import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import UserMenu from './UserMenu';
import { useAuthStore } from '../../stores/authStore';

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

vi.mock('../../api/client', () => ({
  apiClient: {
    get: vi.fn(),
  },
  api: {
    get: vi.fn(),
    post: vi.fn().mockResolvedValue({ data: {} }),
    patch: vi.fn(),
    delete: vi.fn(),
  },
  registerAuthStore: vi.fn(),
}));

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function createQueryClient() {
  return new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
}

function renderWithProviders(ui: React.ReactElement) {
  const qc = createQueryClient();
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter>{ui}</MemoryRouter>
    </QueryClientProvider>,
  );
}

function seedAuthStore() {
  useAuthStore.setState({
    accessToken: 'test-token',
    refreshToken: 'test-refresh',
    user: {
      userId: '1',
      username: 'jdoe',
      entityId: 'e1',
      entityName: 'Root Entity',
      profileId: 'p1',
      profileName: 'Super-Admin',
      profileInterface: 'central',
      rights: {},
      language: 'en-US',
    },
    isAuthenticated: true,
    isLoading: false,
    rememberMe: false,
  });
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('UserMenu', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    seedAuthStore();
  });

  it('renders username, profile name, and entity name in the trigger', () => {
    renderWithProviders(<UserMenu />);

    expect(screen.getByText('jdoe')).toBeInTheDocument();
    expect(screen.getByText('Super-Admin')).toBeInTheDocument();
    expect(screen.getByText('Root Entity')).toBeInTheDocument();
  });

  it('renders user initials in the avatar', () => {
    renderWithProviders(<UserMenu />);

    expect(screen.getByText('JD')).toBeInTheDocument();
  });

  it('opens dropdown on trigger click', () => {
    renderWithProviders(<UserMenu />);

    const trigger = screen.getByRole('button', { name: 'User menu' });
    fireEvent.click(trigger);

    expect(screen.getByRole('menu', { name: 'User menu options' })).toBeInTheDocument();
    expect(screen.getByText('Profile')).toBeInTheDocument();
    expect(screen.getByText('Entity')).toBeInTheDocument();
  });

  it('closes dropdown on Escape key', () => {
    renderWithProviders(<UserMenu />);

    fireEvent.click(screen.getByRole('button', { name: 'User menu' }));
    expect(screen.getByRole('menu')).toBeInTheDocument();

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('menu')).not.toBeInTheDocument();
  });

  it('navigates to /preferences when Preferences is clicked', () => {
    renderWithProviders(<UserMenu />);

    fireEvent.click(screen.getByRole('button', { name: 'User menu' }));
    fireEvent.click(screen.getByText('Preferences'));

    expect(mockNavigate).toHaveBeenCalledWith('/preferences');
  });

  it('calls logout and navigates to /login when Logout is clicked', async () => {
    renderWithProviders(<UserMenu />);

    fireEvent.click(screen.getByRole('button', { name: 'User menu' }));
    fireEvent.click(screen.getByText('Logout'));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('shows loading state for profiles and entities when dropdown opens', () => {
    renderWithProviders(<UserMenu />);

    fireEvent.click(screen.getByRole('button', { name: 'User menu' }));

    expect(screen.getByText('Loading profiles…')).toBeInTheDocument();
    expect(screen.getByText('Loading entities…')).toBeInTheDocument();
  });

  it('displays profile name and entity name with title attributes for truncation', () => {
    renderWithProviders(<UserMenu />);

    const profileLabel = screen.getByTitle('Super-Admin');
    expect(profileLabel).toBeInTheDocument();

    const entityLabel = screen.getByTitle('Root Entity');
    expect(entityLabel).toBeInTheDocument();
  });
});
