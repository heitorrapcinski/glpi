import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { applyTheme, DARK_PALETTES } from '../theme/themeEngine';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface PreferencesState {
  theme: string;
  isDarkMode: boolean;
  locale: string;
  layoutMode: 'vertical' | 'horizontal';
  timelineOrder: 'newest' | 'oldest';
  itemsPerPage: number;
  sidebarCollapsed: boolean;

  setTheme: (theme: string) => void;
  setLocale: (locale: string) => void;
  setLayoutMode: (mode: 'vertical' | 'horizontal') => void;
  setTimelineOrder: (order: 'newest' | 'oldest') => void;
  setItemsPerPage: (count: number) => void;
  toggleSidebar: () => void;
}

// ---------------------------------------------------------------------------
// localStorage key
// ---------------------------------------------------------------------------
const STORAGE_KEY = 'glpi_preferences';

// ---------------------------------------------------------------------------
// Store
// ---------------------------------------------------------------------------

export const usePreferencesStore = create<PreferencesState>()(
  persist(
    (set) => ({
      theme: 'default',
      isDarkMode: false,
      locale: 'en-US',
      layoutMode: 'vertical',
      timelineOrder: 'newest',
      itemsPerPage: 50,
      sidebarCollapsed: false,

      setTheme(theme: string) {
        applyTheme(theme);
        set({ theme, isDarkMode: DARK_PALETTES.includes(theme) });
      },

      setLocale(locale: string) {
        set({ locale });
      },

      setLayoutMode(mode: 'vertical' | 'horizontal') {
        set({ layoutMode: mode });
      },

      setTimelineOrder(order: 'newest' | 'oldest') {
        set({ timelineOrder: order });
      },

      setItemsPerPage(count: number) {
        set({ itemsPerPage: count });
      },

      toggleSidebar() {
        set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed }));
      },
    }),
    {
      name: STORAGE_KEY,
      partialize: (state) => ({
        theme: state.theme,
        isDarkMode: state.isDarkMode,
        locale: state.locale,
        layoutMode: state.layoutMode,
        timelineOrder: state.timelineOrder,
        itemsPerPage: state.itemsPerPage,
        sidebarCollapsed: state.sidebarCollapsed,
      }),
    },
  ),
);
