/* eslint-disable react/prop-types */
/* Shared UI primitives — small, cosmetic recreations of the shadcn components
   used in the codebase. NOT production code; visual fidelity only. */

const cx = (...xs) => xs.filter(Boolean).join(' ');

function Button({ variant = 'default', size = 'default', className = '', asChild, ...props }) {
  const base = 'nh-btn';
  return <button className={cx(base, `nh-btn-${variant}`, `nh-btn-${size}`, className)} {...props} />;
}

function IconButton({ className = '', ...props }) {
  return <button className={cx('nh-icon-btn', className)} {...props} />;
}

function Input({ className = '', invalid, ...props }) {
  return <input className={cx('nh-input', invalid && 'nh-input-invalid', className)} {...props} />;
}

function Label({ className = '', ...props }) {
  return <label className={cx('nh-label', className)} {...props} />;
}

function Card({ className = '', interactive, children, ...props }) {
  return (
    <div className={cx('nh-card', interactive && 'nh-card-interactive', className)} {...props}>
      {children}
    </div>
  );
}

function Badge({ tone = 'neutral', className = '', children, ...props }) {
  return (
    <span className={cx('nh-badge', `nh-badge-${tone}`, className)} {...props}>
      {children}
    </span>
  );
}

function JlptBadge({ level, className = '' }) {
  return <Badge tone={level?.toLowerCase()} className={className}>{level}</Badge>;
}

function Separator({ className = '' }) {
  return <hr className={cx('nh-sep', className)} />;
}

function Avatar({ initials, src, size = 36 }) {
  if (src) return <img className="nh-avatar" style={{ width: size, height: size }} src={src} alt="" />;
  return (
    <div className="nh-avatar nh-avatar-fallback" style={{ width: size, height: size }}>
      {initials}
    </div>
  );
}

/* Lucide-ish icons — we ship just the handful used across the kit so the bundle
   doesn't depend on the lucide CDN. Stroke 2, viewBox 24, follow Lucide grid. */
const Ic = ({ d, c, children, size = 16, stroke = 2, fill = 'none', ...rest }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill}
       stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round" {...rest}>
    {c ? <circle cx="12" cy="12" r="10" /> : null}
    {d ? <path d={d} /> : null}
    {children}
  </svg>
);

const Icon = {
  Bell:        (p) => <Ic {...p}><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></Ic>,
  Search:      (p) => <Ic {...p}><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></Ic>,
  Bookmark:    (p) => <Ic {...p}><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></Ic>,
  BookmarkFill:(p) => <Ic fill="currentColor" {...p}><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></Ic>,
  Message:     (p) => <Ic {...p}><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></Ic>,
  RotateCw:    (p) => <Ic {...p}><path d="M21 12a9 9 0 1 1-3-6.7L21 8"/><path d="M21 3v5h-5"/></Ic>,
  Volume:      (p) => <Ic {...p}><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"/></Ic>,
  Mic:         (p) => <Ic {...p}><rect x="9" y="2" width="6" height="12" rx="3"/><path d="M5 10a7 7 0 0 0 14 0"/><path d="M12 17v4"/></Ic>,
  ArrowLeft:   (p) => <Ic {...p}><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></Ic>,
  ChevronRight:(p) => <Ic {...p}><path d="m9 18 6-6-6-6"/></Ic>,
  ChevronLeft: (p) => <Ic {...p}><path d="m15 18-6-6 6-6"/></Ic>,
  Check:       (p) => <Ic {...p}><path d="M20 6 9 17l-5-5"/></Ic>,
  X:           (p) => <Ic {...p}><path d="M18 6 6 18"/><path d="m6 6 12 12"/></Ic>,
  Sparkles:    (p) => <Ic {...p}><path d="M12 3l1.5 4.5L18 9l-4.5 1.5L12 15l-1.5-4.5L6 9l4.5-1.5z"/><path d="M19 14l.75 2.25L22 17l-2.25.75L19 20l-.75-2.25L16 17l2.25-.75z"/></Ic>,
  Layers:      (p) => <Ic {...p}><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></Ic>,
  Flame:       (p) => <Ic {...p}><path d="M8.5 14.5A2.5 2.5 0 0 0 11 17c2 0 3-2 3-3 0-3 1-4 2-5 0 4 4 3 4 7a8 8 0 0 1-16 0c0-3.5 2-7 5-9-1 4 2 5 2 5z"/></Ic>,
  Brain:       (p) => <Ic {...p}><path d="M12 5a3 3 0 0 0-3-3 3 3 0 0 0-3 3 4 4 0 0 0-2 7 4 4 0 0 0 2 7 3 3 0 0 0 3 3 3 3 0 0 0 3-3z"/><path d="M12 5a3 3 0 0 1 3-3 3 3 0 0 1 3 3 4 4 0 0 1 2 7 4 4 0 0 1-2 7 3 3 0 0 1-3 3 3 3 0 0 1-3-3z"/></Ic>,
  Calendar:    (p) => <Ic {...p}><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></Ic>,
  User:        (p) => <Ic {...p}><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></Ic>,
  Settings:    (p) => <Ic {...p}><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/></Ic>,
  Lock:        (p) => <Ic {...p}><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></Ic>,
  LogOut:      (p) => <Ic {...p}><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></Ic>,
  Send:        (p) => <Ic {...p}><path d="m22 2-7 20-4-9-9-4z"/><path d="M22 2 11 13"/></Ic>,
  TrendUp:     (p) => <Ic {...p}><path d="m3 17 6-6 4 4 8-8"/><path d="M14 7h7v7"/></Ic>,
};

Object.assign(window, { cx, Button, IconButton, Input, Label, Card, Badge, JlptBadge, Separator, Avatar, Icon });
