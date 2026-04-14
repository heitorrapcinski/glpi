import { useState, useMemo } from 'react';
import type { CSSProperties, ReactNode } from 'react';
import StatusBadge from '@/components/common/StatusBadge';
import PriorityBadge from '@/components/common/PriorityBadge';
import ActorBadge from '@/components/common/ActorBadge';
import { getSlaIndicatorColor } from '@/utils/status';
import type { Actor, SlaContext } from '@/hooks/useTickets';

// ---------------------------------------------------------------------------
// FieldsPanel — Collapsible right-side metadata panel for ITIL objects
// ---------------------------------------------------------------------------

export interface FieldsPanelProps {
  status: number;
  priority: number;
  urgency: number;
  impact: number;
  categoryId: string | null;
  actors: Actor[];
  sla: SlaContext | null;
  dates: {
    createdAt: string;
    updatedAt: string;
    solvedAt: string | null;
    closedAt: string | null;
  };
  linkedItems?: { type: string; id: string; title: string }[];
  onUpdate: (field: string, value: unknown) => void;
  collapsed?: boolean;
  onToggleCollapse?: () => void;
  extraSections?: { key: string; label: string; icon: string; content: ReactNode }[];
}

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const STATUS_OPTIONS = [
  { value: 1, label: 'New' },
  { value: 2, label: 'Assigned' },
  { value: 3, label: 'Planned' },
  { value: 4, label: 'Pending' },
  { value: 5, label: 'Solved' },
  { value: 6, label: 'Closed' },
];

const URGENCY_OPTIONS = [
  { value: 1, label: 'Very Low' },
  { value: 2, label: 'Low' },
  { value: 3, label: 'Medium' },
  { value: 4, label: 'High' },
  { value: 5, label: 'Very High' },
];

const IMPACT_OPTIONS = [
  { value: 1, label: 'Low' },
  { value: 2, label: 'Medium' },
  { value: 3, label: 'High' },
];

const SECTION_ICONS: Record<string, string> = {
  status: '●',
  dates: '📅',
  actors: '👤',
  priority: '⚡',
  category: '📁',
  sla: '⏱',
  linked: '🔗',
};

const COLLAPSED_WIDTH = '90px';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function formatTimestamp(iso: string | null): string {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('en-US', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

function groupActors(actors: Actor[]) {
  const requesters = actors.filter((a) => a.actorType === 1);
  const assigned = actors.filter((a) => a.actorType === 2);
  const observers = actors.filter((a) => a.actorType === 3);
  return { requesters, assigned, observers };
}

function getSlaColor(sla: SlaContext | null): ReturnType<typeof getSlaIndicatorColor> | null {
  if (!sla?.deadline) return null;
  const deadline = new Date(sla.deadline).getTime();
  const now = Date.now();
  return getSlaIndicatorColor(deadline, now, sla.totalDuration);
}

const SLA_COLOR_MAP: Record<string, string> = {
  green: '#22c55e',
  orange: '#f97316',
  red: '#ef4444',
};

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const panelExpanded: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  borderLeft: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  height: '100%',
  overflow: 'auto',
};

const panelCollapsed: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  width: COLLAPSED_WIDTH,
  borderLeft: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  height: '100%',
  overflow: 'auto',
  paddingTop: '0.5rem',
};

const toggleBtn: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: '28px',
  height: '28px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  cursor: 'pointer',
  fontSize: '0.875rem',
  color: 'var(--tblr-body-color, #1e293b)',
  margin: '0.5rem',
  flexShrink: 0,
};

const sectionHeader: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0.6rem 0.75rem',
  cursor: 'pointer',
  fontWeight: 600,
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  userSelect: 'none',
};

const sectionBody: CSSProperties = {
  padding: '0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.8125rem',
};

const fieldRow: CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '0.5rem',
};

const fieldLabel: CSSProperties = {
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.75rem',
  fontWeight: 500,
};

const selectStyle: CSSProperties = {
  padding: '0.25rem 0.4rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.8125rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  cursor: 'pointer',
};

const collapsedIcon: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: '44px',
  height: '44px',
  cursor: 'pointer',
  fontSize: '1.1rem',
  borderRadius: '6px',
  marginBottom: '0.25rem',
};

const actorGroup: CSSProperties = {
  marginBottom: '0.5rem',
};

const actorGroupLabel: CSSProperties = {
  fontSize: '0.7rem',
  fontWeight: 600,
  textTransform: 'uppercase' as const,
  color: 'var(--tblr-secondary, #606f91)',
  marginBottom: '0.25rem',
  letterSpacing: '0.04em',
};

const actorList: CSSProperties = {
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.35rem',
};

const linkedItem: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.35rem',
  padding: '0.3rem 0',
  fontSize: '0.8125rem',
  color: 'var(--tblr-link-color, #3a5693)',
  cursor: 'pointer',
};

// ---------------------------------------------------------------------------
// AccordionSection sub-component
// ---------------------------------------------------------------------------

interface AccordionSectionProps {
  id: string;
  label: string;
  icon: string;
  defaultOpen?: boolean;
  children: ReactNode;
}

function AccordionSection({ id, label, icon, defaultOpen = false, children }: AccordionSectionProps) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div role="region" aria-labelledby={`section-${id}`}>
      <div
        id={`section-${id}`}
        role="button"
        tabIndex={0}
        aria-expanded={open}
        style={sectionHeader}
        onClick={() => setOpen((v) => !v)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            setOpen((v) => !v);
          }
        }}
      >
        <span>
          <span aria-hidden="true" style={{ marginRight: '0.4rem' }}>{icon}</span>
          {label}
        </span>
        <span aria-hidden="true" style={{ fontSize: '0.7rem' }}>{open ? '▲' : '▼'}</span>
      </div>
      {open && <div style={sectionBody}>{children}</div>}
    </div>
  );
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function FieldsPanel({
  status,
  priority,
  urgency,
  impact,
  categoryId,
  actors,
  sla,
  dates,
  linkedItems = [],
  onUpdate,
  collapsed = false,
  onToggleCollapse,
  extraSections = [],
}: FieldsPanelProps) {
  const { requesters, assigned, observers } = useMemo(() => groupActors(actors), [actors]);
  const slaColor = useMemo(() => getSlaColor(sla), [sla]);

  // ---- Collapsed mode: icon strip ----
  if (collapsed) {
    const icons = [
      { key: 'status', icon: SECTION_ICONS.status, label: 'Status' },
      { key: 'dates', icon: SECTION_ICONS.dates, label: 'Dates' },
      { key: 'actors', icon: SECTION_ICONS.actors, label: 'Actors' },
      { key: 'priority', icon: SECTION_ICONS.priority, label: 'Priority' },
      { key: 'category', icon: SECTION_ICONS.category, label: 'Category' },
      { key: 'sla', icon: SECTION_ICONS.sla, label: 'SLA' },
      { key: 'linked', icon: SECTION_ICONS.linked, label: 'Linked Items' },
      ...extraSections.map((s) => ({ key: s.key, icon: s.icon, label: s.label })),
    ];

    return (
      <aside style={panelCollapsed} aria-label="Fields panel (collapsed)" data-testid="fields-panel-collapsed">
        <button
          type="button"
          style={toggleBtn}
          aria-label="Expand fields panel"
          onClick={onToggleCollapse}
        >
          ◀
        </button>
        {icons.map((item) => (
          <div
            key={item.key}
            style={collapsedIcon}
            title={item.label}
            aria-label={item.label}
            role="img"
          >
            {item.icon}
          </div>
        ))}
      </aside>
    );
  }

  // ---- Expanded mode ----
  return (
    <aside style={panelExpanded} aria-label="Fields panel" data-testid="fields-panel">
      <button
        type="button"
        style={toggleBtn}
        aria-label="Collapse fields panel"
        onClick={onToggleCollapse}
      >
        ▶
      </button>

      {/* Status */}
      <AccordionSection id="status" label="Status" icon={SECTION_ICONS.status} defaultOpen>
        <div style={fieldRow}>
          <StatusBadge code={status} />
          <select
            aria-label="Change status"
            style={selectStyle}
            value={status}
            onChange={(e) => onUpdate('status', Number(e.target.value))}
          >
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>
      </AccordionSection>

      {/* Dates */}
      <AccordionSection id="dates" label="Dates" icon={SECTION_ICONS.dates}>
        <div style={fieldRow}>
          <span style={fieldLabel}>Created</span>
          <span>{formatTimestamp(dates.createdAt)}</span>
        </div>
        <div style={fieldRow}>
          <span style={fieldLabel}>Updated</span>
          <span>{formatTimestamp(dates.updatedAt)}</span>
        </div>
        <div style={fieldRow}>
          <span style={fieldLabel}>Solved</span>
          <span>{formatTimestamp(dates.solvedAt)}</span>
        </div>
        <div style={fieldRow}>
          <span style={fieldLabel}>Closed</span>
          <span>{formatTimestamp(dates.closedAt)}</span>
        </div>
      </AccordionSection>

      {/* Actors */}
      <AccordionSection id="actors" label="Actors" icon={SECTION_ICONS.actors} defaultOpen>
        {requesters.length > 0 && (
          <div style={actorGroup}>
            <div style={actorGroupLabel}>Requester</div>
            <div style={actorList}>
              {requesters.map((a) => (
                <ActorBadge key={a.actorId} displayName={a.displayName ?? a.actorId} size={28} />
              ))}
            </div>
          </div>
        )}
        {assigned.length > 0 && (
          <div style={actorGroup}>
            <div style={actorGroupLabel}>Assigned</div>
            <div style={actorList}>
              {assigned.map((a) => (
                <ActorBadge key={a.actorId} displayName={a.displayName ?? a.actorId} size={28} />
              ))}
            </div>
          </div>
        )}
        {observers.length > 0 && (
          <div style={actorGroup}>
            <div style={actorGroupLabel}>Observer</div>
            <div style={actorList}>
              {observers.map((a) => (
                <ActorBadge key={a.actorId} displayName={a.displayName ?? a.actorId} size={28} />
              ))}
            </div>
          </div>
        )}
        {actors.length === 0 && (
          <span style={{ color: 'var(--tblr-secondary, #606f91)' }}>No actors assigned</span>
        )}
      </AccordionSection>

      {/* Priority / Urgency / Impact */}
      <AccordionSection id="priority" label="Priority / Urgency / Impact" icon={SECTION_ICONS.priority}>
        <div style={fieldRow}>
          <span style={fieldLabel}>Priority</span>
          <PriorityBadge code={priority} size="sm" />
        </div>
        <div style={fieldRow}>
          <span style={fieldLabel}>Urgency</span>
          <select
            aria-label="Change urgency"
            style={selectStyle}
            value={urgency}
            onChange={(e) => onUpdate('urgency', Number(e.target.value))}
          >
            {URGENCY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>
        <div style={fieldRow}>
          <span style={fieldLabel}>Impact</span>
          <select
            aria-label="Change impact"
            style={selectStyle}
            value={impact}
            onChange={(e) => onUpdate('impact', Number(e.target.value))}
          >
            {IMPACT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>
      </AccordionSection>

      {/* Category */}
      <AccordionSection id="category" label="Category" icon={SECTION_ICONS.category}>
        <div style={fieldRow}>
          <span style={fieldLabel}>Category</span>
          <span>{categoryId ?? '—'}</span>
        </div>
      </AccordionSection>

      {/* SLA */}
      <AccordionSection id="sla" label="SLA" icon={SECTION_ICONS.sla}>
        {sla?.deadline ? (
          <div style={fieldRow}>
            <span style={fieldLabel}>Deadline</span>
            <span
              style={{
                fontWeight: 600,
                color: slaColor ? SLA_COLOR_MAP[slaColor] : 'inherit',
              }}
              aria-label={`SLA deadline: ${slaColor ?? 'unknown'}`}
            >
              {formatTimestamp(sla.deadline)}
            </span>
          </div>
        ) : (
          <span style={{ color: 'var(--tblr-secondary, #606f91)' }}>No SLA configured</span>
        )}
      </AccordionSection>

      {/* Linked Items */}
      <AccordionSection id="linked" label="Linked Items" icon={SECTION_ICONS.linked}>
        {linkedItems.length > 0 ? (
          linkedItems.map((item) => (
            <div key={`${item.type}-${item.id}`} style={linkedItem}>
              <span style={{ fontWeight: 600, textTransform: 'capitalize' }}>{item.type}</span>
              <span>#{item.id}</span>
              <span>{item.title}</span>
            </div>
          ))
        ) : (
          <span style={{ color: 'var(--tblr-secondary, #606f91)' }}>No linked items</span>
        )}
      </AccordionSection>

      {/* Extra sections (for problems/changes) */}
      {extraSections.map((section) => (
        <AccordionSection key={section.key} id={section.key} label={section.label} icon={section.icon}>
          {section.content}
        </AccordionSection>
      ))}
    </aside>
  );
}
