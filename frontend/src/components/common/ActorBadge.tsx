import type { CSSProperties } from 'react';

// ---------------------------------------------------------------------------
// ActorBadge — User avatar / initials badge
// ---------------------------------------------------------------------------

export interface ActorBadgeProps {
  /** Full display name of the actor. */
  displayName: string;
  /** Optional avatar image URL. */
  avatarUrl?: string;
  /** Optional size in pixels (default 32). */
  size?: number;
}

function getInitials(name: string): string {
  const parts = name.trim().split(/\s+/);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

/** Simple hash to pick a deterministic hue from a name. */
function nameToHue(name: string): number {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return Math.abs(hash) % 360;
}

export default function ActorBadge({
  displayName,
  avatarUrl,
  size = 32,
}: ActorBadgeProps) {
  const hue = nameToHue(displayName);

  const container: CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: size,
    height: size,
    borderRadius: '50%',
    overflow: 'hidden',
    flexShrink: 0,
    backgroundColor: `hsl(${hue}, 55%, 65%)`,
    color: '#fff',
    fontSize: size * 0.4,
    fontWeight: 600,
    lineHeight: 1,
    userSelect: 'none',
  };

  if (avatarUrl) {
    return (
      <span
        role="img"
        aria-label={displayName}
        style={container}
        title={displayName}
      >
        <img
          src={avatarUrl}
          alt={displayName}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      </span>
    );
  }

  return (
    <span
      role="img"
      aria-label={displayName}
      style={container}
      title={displayName}
    >
      {getInitials(displayName)}
    </span>
  );
}
