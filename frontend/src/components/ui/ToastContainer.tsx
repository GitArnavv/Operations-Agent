import React from 'react';
import { AlertCircle, AlertTriangle, CheckCircle2, Info, X } from 'lucide-react';
import { useToast, ToastItem } from '../../contexts/ToastContext';

export const ToastContainer: React.FC = () => {
  const { toasts, removeToast } = useToast();

  if (toasts.length === 0) return null;

  return (
    <div
      role="region"
      aria-live="polite"
      aria-label="Notifications"
      className="fixed bottom-5 right-5 z-[600] flex flex-col gap-2.5 max-w-sm w-full pointer-events-none px-4 sm:px-0 font-sans"
    >
      {toasts.map((toast) => (
        <ToastCard key={toast.id} toast={toast} onDismiss={() => removeToast(toast.id)} />
      ))}
    </div>
  );
};

const ToastCard: React.FC<{ toast: ToastItem; onDismiss: () => void }> = ({ toast, onDismiss }) => {
  const iconMap = {
    error: <AlertCircle className="w-4 h-4 text-[var(--status-danger-text)] shrink-0" />,
    warning: <AlertTriangle className="w-4 h-4 text-[var(--status-warning-text)] shrink-0" />,
    success: <CheckCircle2 className="w-4 h-4 text-[var(--status-success-text)] shrink-0" />,
    info: <Info className="w-4 h-4 text-[var(--status-info-text)] shrink-0" />,
  };

  const borderClass = {
    error: 'border-[var(--status-danger-border)] bg-[var(--status-danger-bg)]/90',
    warning: 'border-[var(--status-warning-border)] bg-[var(--status-warning-bg)]/90',
    success: 'border-[var(--status-success-border)] bg-[var(--status-success-bg)]/90',
    info: 'border-[var(--status-info-border)] bg-[var(--status-info-bg)]/90',
  }[toast.type];

  return (
    <div
      className={`pointer-events-auto flex items-start gap-3 p-4 rounded-xl border shadow-elevated backdrop-blur-md transition-all animate-slide-up ${borderClass}`}
    >
      <div className="pt-0.5">{iconMap[toast.type]}</div>
      <div className="flex-1 min-w-0">
        <h4 className="text-xs font-bold text-foreground leading-snug tracking-tight">
          {toast.title}
        </h4>
        {toast.message && (
          <p className="text-[11px] text-muted-foreground mt-0.5 leading-relaxed line-clamp-3">
            {toast.message}
          </p>
        )}
        {toast.action && (
          <button
            onClick={toast.action.onClick}
            className="mt-2 text-[11px] font-semibold text-primary hover:underline block"
          >
            {toast.action.label}
          </button>
        )}
      </div>
      <button
        onClick={onDismiss}
        className="p-1 rounded-md text-muted-foreground hover:text-foreground hover:bg-surface-hover/50 transition-colors shrink-0"
        aria-label="Dismiss notification"
      >
        <X className="w-3.5 h-3.5" />
      </button>
    </div>
  );
};
