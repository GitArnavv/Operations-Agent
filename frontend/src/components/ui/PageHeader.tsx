import React from 'react';

interface PageHeaderProps {
  eyebrow?: string;
  title: string;
  description?: string;
  icon?: React.ReactNode;
  actions?: React.ReactNode;
  statusIndicator?: React.ReactNode;
  className?: string;
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  eyebrow,
  title,
  description,
  icon,
  actions,
  statusIndicator,
  className = '',
}) => {
  return (
    <div className={`flex flex-col sm:flex-row sm:items-end justify-between gap-4 border-b border-border pb-5 theme-surface ${className}`}>
      <div className="space-y-1">
        {(eyebrow || statusIndicator) && (
          <div className="flex items-center gap-2 mb-1">
            {statusIndicator}
            {eyebrow && (
              <span className="text-[11px] font-mono font-semibold tracking-wider text-primary uppercase">
                {eyebrow}
              </span>
            )}
          </div>
        )}
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground flex items-center gap-2.5">
          {icon && <span className="text-primary shrink-0">{icon}</span>}
          <span>{title}</span>
        </h1>
        {description && (
          <p className="text-xs sm:text-sm text-muted-foreground max-w-3xl leading-relaxed mt-1">
            {description}
          </p>
        )}
      </div>

      {actions && <div className="flex items-center gap-2.5 shrink-0">{actions}</div>}
    </div>
  );
};
