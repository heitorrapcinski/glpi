import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { apiClient } from '../../api/client';
import { IDENTITY } from '../../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface ProfileItem {
  id: string;
  name: string;
  interface: 'central' | 'helpdesk';
}

interface EntityNode {
  id: string;
  name: string;
  completename: string;
  level: number;
  children?: EntityNode[];
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapperStyle: React.CSSProperties = {
  position: 'relative',
};

const triggerStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  padding: '0.375rem 0.75rem',
  border: 'none',
  background: 'transparent',
  cursor: 'pointer',
  borderRadius: '0.375rem',
  color: 'var(--tblr-body-color, #1e293b)',
  fontSize: '0.875rem',
  lineHeight: 1.4,
};

const avatarStyle: React.CSSProperties = {
  width: 32,
  height: 32,
  borderRadius: '50%',
  background: 'var(--tblr-primary)',
  color: 'var(--tblr-primary-fg)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontWeight: 600,
  fontSize: '0.75rem',
  flexShrink: 0,
};

const userInfoStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-end',
  lineHeight: 1.3,
};

const usernameStyle: React.CSSProperties = {
  fontWeight: 600,
  fontSize: '0.8125rem',
};

const profileLabelStyle: React.CSSProperties = {
  fontSize: '0.6875rem',
  color: 'var(--tblr-secondary)',
  maxWidth: '10rem',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

const entityLabelStyle: React.CSSProperties = {
  fontSize: '0.75rem',
  color: 'var(--tblr-secondary)',
  maxWidth: '10rem',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

const dropdownStyle: React.CSSProperties = {
  position: 'absolute',
  top: '100%',
  right: 0,
  marginTop: '0.25rem',
  minWidth: '16rem',
  maxHeight: '28rem',
  overflowY: 'auto',
  background: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  borderRadius: '0.375rem',
  boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
  zIndex: 1050,
  padding: '0.25rem 0',
};

const sectionLabelStyle: React.CSSProperties = {
  padding: '0.5rem 0.75rem 0.25rem',
  fontSize: '0.6875rem',
  fontWeight: 600,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  color: 'var(--tblr-secondary)',
};

const menuItemStyle: React.CSSProperties = {
  display: 'block',
  width: '100%',
  padding: '0.375rem 0.75rem',
  border: 'none',
  background: 'transparent',
  textAlign: 'left',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
};

const activeItemStyle: React.CSSProperties = {
  ...menuItemStyle,
  fontWeight: 600,
  color: 'var(--tblr-primary)',
};

const dividerStyle: React.CSSProperties = {
  height: 1,
  background: 'var(--tblr-border-color, #e6e7e9)',
  margin: '0.25rem 0',
};

const loadingStyle: React.CSSProperties = {
  padding: '0.5rem 0.75rem',
  fontSize: '0.75rem',
  color: 'var(--tblr-secondary)',
  fontStyle: 'italic',
};

// ---------------------------------------------------------------------------
// Entity tree rendering helper
// ---------------------------------------------------------------------------

function EntityTreeItem({
  node,
  currentEntityId,
  onSelect,
}: {
  node: EntityNode;
  currentEntityId: string | undefined;
  onSelect: (id: string) => void;
}) {
  const isActive = node.id === currentEntityId;
  const indent = node.level * 1;

  return (
    <>
      <button
        type="button"
        style={{
          ...(isActive ? activeItemStyle : menuItemStyle),
          paddingLeft: `${0.75 + indent}rem`,
        }}
        role="menuitem"
        aria-current={isActive ? 'true' : undefined}
        onClick={() => onSelect(node.id)}
        onMouseEnter={(e) => {
          if (!isActive) {
            (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg, #f1f5f9)';
          }
        }}
        onMouseLeave={(e) => {
          (e.currentTarget as HTMLElement).style.background = 'transparent';
        }}
      >
        {node.name}
      </button>
      {node.children?.map((child) => (
        <EntityTreeItem
          key={child.id}
          node={child}
          currentEntityId={currentEntityId}
          onSelect={onSelect}
        />
      ))}
    </>
  );
}

// ---------------------------------------------------------------------------
// Flatten entity list into tree structure
// ---------------------------------------------------------------------------

function buildEntityTree(entities: EntityNode[]): EntityNode[] {
  if (!entities || entities.length === 0) return [];
  // Entities are already returned as a flat list with level info.
  // Build a tree by using the level to determine parent-child relationships.
  const roots: EntityNode[] = [];
  const stack: EntityNode[] = [];

  for (const entity of entities) {
    const node: EntityNode = { ...entity, children: [] };

    // Pop stack until we find the parent (level - 1)
    while (stack.length > 0 && stack[stack.length - 1].level >= node.level) {
      stack.pop();
    }

    if (stack.length === 0) {
      roots.push(node);
    } else {
      const parent = stack[stack.length - 1];
      if (!parent.children) parent.children = [];
      parent.children.push(node);
    }

    stack.push(node);
  }

  return roots;
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function UserMenu() {
  const navigate = useNavigate();
  const {
    user,
    logout,
    switchProfile,
    switchProfileStatus,
    switchEntity,
    switchEntityStatus,
  } = useAuth();

  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  const initials = user
    ? user.username.slice(0, 2).toUpperCase()
    : '??';

  const isSwitching = switchProfileStatus.isPending || switchEntityStatus.isPending;

  // -----------------------------------------------------------------------
  // Fetch profiles when dropdown is open
  // -----------------------------------------------------------------------
  const {
    data: profilesData,
    isLoading: profilesLoading,
  } = useQuery<ProfileItem[]>({
    queryKey: ['user-profiles'],
    queryFn: async () => {
      const res = await apiClient.get<ProfileItem[]>(IDENTITY.PROFILES);
      return res.data;
    },
    enabled: open,
    staleTime: 60_000,
  });

  const profiles = profilesData ?? [];

  // -----------------------------------------------------------------------
  // Fetch entities when dropdown is open
  // -----------------------------------------------------------------------
  const {
    data: entitiesData,
    isLoading: entitiesLoading,
  } = useQuery<EntityNode[]>({
    queryKey: ['user-entities'],
    queryFn: async () => {
      const res = await apiClient.get<EntityNode[]>(IDENTITY.ENTITIES);
      return res.data;
    },
    enabled: open,
    staleTime: 60_000,
  });

  const entityTree = useMemo(
    () => buildEntityTree(entitiesData ?? []),
    [entitiesData],
  );

  // -----------------------------------------------------------------------
  // Close on outside click
  // -----------------------------------------------------------------------
  useEffect(() => {
    if (!open) return;
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [open]);

  // Close on Escape
  useEffect(() => {
    if (!open) return;
    function handleKey(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpen(false);
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [open]);

  // -----------------------------------------------------------------------
  // Handlers
  // -----------------------------------------------------------------------
  const handleLogout = useCallback(async () => {
    setOpen(false);
    await logout();
    navigate('/login');
  }, [logout, navigate]);

  const handlePreferences = useCallback(() => {
    setOpen(false);
    navigate('/preferences');
  }, [navigate]);

  const handleSwitchProfile = useCallback(
    async (profileId: string) => {
      if (profileId === user?.profileId || isSwitching) return;
      try {
        await switchProfile({ profileId });
        // Reload current view to reflect new permissions
        navigate(0);
      } catch {
        // Error is handled by the mutation's error state
      }
    },
    [user?.profileId, isSwitching, switchProfile, navigate],
  );

  const handleSwitchEntity = useCallback(
    async (entityId: string) => {
      if (entityId === user?.entityId || isSwitching) return;
      try {
        await switchEntity({ entityId });
        // Reload current view to reflect new entity scope
        navigate(0);
      } catch {
        // Error is handled by the mutation's error state
      }
    },
    [user?.entityId, isSwitching, switchEntity, navigate],
  );

  return (
    <div ref={ref} style={wrapperStyle}>
      <button
        type="button"
        style={triggerStyle}
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="true"
        aria-expanded={open}
        aria-label="User menu"
      >
        <div style={userInfoStyle}>
          <span style={usernameStyle}>{user?.username ?? 'User'}</span>
          <span style={profileLabelStyle} title={user?.profileName}>
            {user?.profileName ?? ''}
          </span>
          <span style={entityLabelStyle} title={user?.entityName}>
            {user?.entityName ?? ''}
          </span>
        </div>
        <div style={avatarStyle} aria-hidden="true">
          {initials}
        </div>
      </button>

      {open && (
        <div style={dropdownStyle} role="menu" aria-label="User menu options">
          {/* ---- Profile selector ---- */}
          <div style={sectionLabelStyle}>Profile</div>
          {profilesLoading ? (
            <div style={loadingStyle}>Loading profiles…</div>
          ) : (
            profiles.map((p) => (
              <button
                key={p.id}
                type="button"
                style={p.id === user?.profileId ? activeItemStyle : menuItemStyle}
                role="menuitem"
                aria-current={p.id === user?.profileId ? 'true' : undefined}
                disabled={isSwitching}
                onClick={() => handleSwitchProfile(p.id)}
                onMouseEnter={(e) => {
                  if (p.id !== user?.profileId) {
                    (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg, #f1f5f9)';
                  }
                }}
                onMouseLeave={(e) => {
                  (e.currentTarget as HTMLElement).style.background = 'transparent';
                }}
              >
                {p.name}
              </button>
            ))
          )}

          <div style={dividerStyle} />

          {/* ---- Entity selector with tree hierarchy ---- */}
          <div style={sectionLabelStyle}>Entity</div>
          {entitiesLoading ? (
            <div style={loadingStyle}>Loading entities…</div>
          ) : (
            entityTree.map((node) => (
              <EntityTreeItem
                key={node.id}
                node={node}
                currentEntityId={user?.entityId}
                onSelect={handleSwitchEntity}
              />
            ))
          )}

          <div style={dividerStyle} />

          {/* ---- Actions ---- */}
          <button
            type="button"
            style={menuItemStyle}
            role="menuitem"
            onClick={handlePreferences}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg, #f1f5f9)';
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
            }}
          >
            Preferences
          </button>
          <button
            type="button"
            style={{
              ...menuItemStyle,
              color: '#ef4444',
            }}
            role="menuitem"
            onClick={handleLogout}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg, #f1f5f9)';
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
            }}
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}
