'use client';
import toast from 'react-hot-toast';
import { CheckCircle, XCircle, Info, AlertTriangle } from 'lucide-react';

/**
 * Custom toasts with a draining progress bar that visualises the remaining time.
 * Use as:
 *   import notify from '@/services/notify';
 *   notify.success('Saved!');
 *   notify.error(err.message);
 */

const DURATIONS = {
  success: 4000,
  error: 5000,
  warning: 5000,
  info: 4000,
};

const PALETTE = {
  success: { bg: '#064e3b', text: '#ecfdf5', accent: '#10b981' },
  error:   { bg: '#7f1d1d', text: '#fef2f2', accent: '#ef4444' },
  warning: { bg: '#78350f', text: '#fffbeb', accent: '#f59e0b' },
  info:    { bg: '#1f2937', text: '#f9fafb', accent: '#3b82f6' },
};

const ICONS = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
};

function ProgressToast({ t, message, type }) {
  const duration = DURATIONS[type];
  const colors = PALETTE[type];
  const Icon = ICONS[type];

  return (
    <div
      style={{
        background: colors.bg,
        color: colors.text,
        borderLeft: `4px solid ${colors.accent}`,
        borderRadius: '10px',
        padding: '12px 16px',
        boxShadow:
          '0 10px 25px -5px rgba(0,0,0,0.25), 0 8px 10px -6px rgba(0,0,0,0.1)',
        maxWidth: '420px',
        minWidth: '280px',
        position: 'relative',
        overflow: 'hidden',
        opacity: t.visible ? 1 : 0,
        transform: t.visible ? 'translateY(0)' : 'translateY(-10px)',
        transition: 'opacity 0.25s ease, transform 0.25s ease',
        display: 'flex',
        alignItems: 'flex-start',
        gap: '12px',
        fontSize: '14px',
        fontWeight: 500,
      }}
    >
      <Icon size={20} style={{ color: colors.accent, flexShrink: 0, marginTop: 1 }} />
      <span style={{ flex: 1, lineHeight: 1.45 }}>{message}</span>
      <button
        onClick={() => toast.dismiss(t.id)}
        style={{
          color: colors.text,
          opacity: 0.6,
          cursor: 'pointer',
          background: 'none',
          border: 'none',
          padding: 0,
          fontSize: '16px',
          lineHeight: 1,
          marginTop: 2,
        }}
        aria-label="Dismiss"
      >
        ✕
      </button>

      {/* Draining progress bar */}
      <div
        style={{
          position: 'absolute',
          left: 0,
          bottom: 0,
          height: '3px',
          width: '100%',
          background: colors.accent,
          transformOrigin: 'left',
          animation: `toastDrain ${duration}ms linear forwards`,
        }}
      />
    </div>
  );
}

const notify = {
  success: (message) =>
    toast.custom((t) => <ProgressToast t={t} message={message} type="success" />, {
      duration: DURATIONS.success,
    }),
  error: (message) =>
    toast.custom((t) => <ProgressToast t={t} message={message} type="error" />, {
      duration: DURATIONS.error,
    }),
  warning: (message) =>
    toast.custom((t) => <ProgressToast t={t} message={message} type="warning" />, {
      duration: DURATIONS.warning,
    }),
  info: (message) =>
    toast.custom((t) => <ProgressToast t={t} message={message} type="info" />, {
      duration: DURATIONS.info,
    }),
  dismiss: (id) => toast.dismiss(id),
};

export default notify;
