import { create } from 'zustand';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface UiState {
  /** Whether the mobile sidebar overlay is open. */
  mobileSidebarOpen: boolean;
  /** Currently visible modal identifier, or null if none. */
  activeModal: string | null;
  /** Whether the global search dropdown is open. */
  searchOpen: boolean;

  openMobileSidebar: () => void;
  closeMobileSidebar: () => void;
  toggleMobileSidebar: () => void;
  openModal: (modalId: string) => void;
  closeModal: () => void;
  setSearchOpen: (open: boolean) => void;
}

// ---------------------------------------------------------------------------
// Store — transient UI state (not persisted)
// ---------------------------------------------------------------------------

export const useUiStore = create<UiState>()((set) => ({
  mobileSidebarOpen: false,
  activeModal: null,
  searchOpen: false,

  openMobileSidebar() {
    set({ mobileSidebarOpen: true });
  },

  closeMobileSidebar() {
    set({ mobileSidebarOpen: false });
  },

  toggleMobileSidebar() {
    set((state) => ({ mobileSidebarOpen: !state.mobileSidebarOpen }));
  },

  openModal(modalId: string) {
    set({ activeModal: modalId });
  },

  closeModal() {
    set({ activeModal: null });
  },

  setSearchOpen(open: boolean) {
    set({ searchOpen: open });
  },
}));
