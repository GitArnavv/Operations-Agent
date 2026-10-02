import React from 'react';

interface MetricCardProps {
  label: string;
  value: string | number;
  unit?: string;
  trend?: {
    text: string;
    isPositive?: boolean;
    isNegative?: boolean;
  };
  context?: string;
  statusBadge?: React.ReactNode;
  icon?: React.ReactNode;
  onClick?: () => void;
  className?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  label,
  value,
  unit,
  trend,
  context,
  statusBadge,
  icon,
  onClick,
  className = '',
}) => {
  return (
    <div
      onClick={onClick}
      className={`p-5 card-digest theme-surface flex flex-col justify-between ${
        onClick ? 'cursor-pointer' : ''
      } ${className}`}
    >
      <div>
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground font-mono">
            {label}
          </span>
          {statusBadge || (icon && <div className="text-muted-foreground">{icon}</div>)}
        </div>

        <div className="flex items-baseline gap-1.5 mt-1">
          <span className="text-2xl sm:text-3xl font-bold font-mono text-foreground tracking-tight">
            {value}
          </span>
          {unit && <span className="text-xs text-muted-foreground font-medium">{unit}</span>}
        </div>
      </div>

      {(trend || context) && (
        <div className="mt-3 pt-1 flex items-center justify-between text-xs text-muted-foreground">
          {trend && (
            <span
              className={`font-mono text-[11px] font-medium ${
                trend.isPositive
                  ? 'text-[var(--status-success-text)]'
                  : trend.isNegative
                  ? 'text-[var(--status-danger-text)]'
                  : 'text-muted-foreground'
              }`}
            >
              {trend.text}
            </span>
          )}
          {context && <span className="text-[11px] text-muted-foreground truncate">{context}</span>}
        </div>
      )}
    </div>
  );
};
