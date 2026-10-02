import React from 'react';

export type StatusVariant =
  | 'success'
  | 'warning'
  | 'danger'
  | 'info'
  | 'neutral'
  | 'primary';

interface StatusBadgeProps {
  variant?: StatusVariant;
  label: string;
  size?: 'sm' | 'md';
  icon?: React.ReactNode;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  variant = 'neutral',
  label,
  size = 'sm',
  icon,
  className = '',
}) => {
  const variantStyles: Record<StatusVariant, string> = {
    success: 'bg-[var(--status-success-bg)] text-[var(--status-success-text)]',
    warning: 'bg-[var(--status-warning-bg)] text-[var(--status-warning-text)]',
    danger: 'bg-[var(--status-danger-bg)] text-[var(--status-danger-text)]',
    info: 'bg-[var(--status-info-bg)] text-[var(--status-info-text)]',
    neutral: 'bg-surface-subtle text-muted-foreground',
    primary: 'bg-primary/10 text-primary',
  };

  const sizeStyles = {
    sm: 'text-[11px] px-2 py-0.5 gap-1',
    md: 'text-xs px-2.5 py-1 gap-1.5',
  };

  return (
    <span
      className={`inline-flex items-center font-medium font-mono uppercase tracking-wide rounded select-none ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
    >
      {icon && <span className="shrink-0">{icon}</span>}
      <span>{label}</span>
    </span>
  );
};
