import React from 'react';
import clsx from 'clsx';

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
}

/**
 * Primitive Skeleton component with pulsing animation matching Tailwind/Shadcn standards.
 */
export const Skeleton: React.FC<SkeletonProps> = ({ className, ...props }) => {
  return (
    <div
      className={clsx(
        'animate-pulse rounded-md bg-muted-foreground/15 dark:bg-white/[0.07]',
        className
      )}
      {...props}
    />
  );
};

/**
 * Metric Tile Skeleton for dashboard and analytics KPI cards.
 */
export const MetricTileSkeleton: React.FC<{ count?: number; className?: string }> = ({
  count = 4,
  className,
}) => {
  return (
    <div
      className={clsx(
        'card-digest p-2 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2',
        className
      )}
    >
      {Array.from({ length: count }).map((_, idx) => (
        <div key={idx} className="p-5 rounded-xl space-y-4">
          <div className="flex items-center justify-between">
            <Skeleton className="h-3.5 w-24" />
            <Skeleton className="h-5 w-16 rounded-full" />
          </div>
          <div className="space-y-2">
            <Skeleton className="h-8 w-28" />
            <Skeleton className="h-3 w-36" />
          </div>
          <div className="pt-2 flex items-center justify-between">
            <Skeleton className="h-3 w-20" />
            <Skeleton className="h-3 w-4" />
          </div>
        </div>
      ))}
    </div>
  );
};

/**
 * Data Table Skeleton with pulsing headers, simulated cells, status pills, and action buttons.
 */
export const TableSkeleton: React.FC<{
  rows?: number;
  columns?: number;
  className?: string;
}> = ({ rows = 6, columns = 6, className }) => {
  return (
    <div
      className={clsx(
        'rounded-xl border border-border bg-surface overflow-hidden theme-surface',
        className
      )}
    >
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-border bg-surface-subtle">
              {Array.from({ length: columns }).map((_, colIdx) => (
                <th key={colIdx} className="py-3 px-4">
                  <Skeleton className="h-3.5 w-20" />
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {Array.from({ length: rows }).map((_, rowIdx) => (
              <tr key={rowIdx} className="hover:bg-surface-hover/30 transition-colors">
                {Array.from({ length: columns }).map((_, colIdx) => {
                  if (colIdx === 0) {
                    return (
                      <td key={colIdx} className="py-4 px-4 space-y-1.5">
                        <Skeleton className="h-3.5 w-32" />
                        <Skeleton className="h-2.5 w-20" />
                      </td>
                    );
                  }
                  if (colIdx === columns - 2) {
                    // Simulated badge
                    return (
                      <td key={colIdx} className="py-4 px-4 text-center">
                        <Skeleton className="h-5 w-16 mx-auto rounded-full" />
                      </td>
                    );
                  }
                  if (colIdx === columns - 1) {
                    // Simulated action button
                    return (
                      <td key={colIdx} className="py-4 px-4 text-right">
                        <Skeleton className="h-6 w-16 ml-auto rounded-md" />
                      </td>
                    );
                  }
                  return (
                    <td key={colIdx} className="py-4 px-4">
                      <Skeleton className="h-3.5 w-24" />
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

/**
 * AI Agent Chat Box Skeleton simulating real-time multi-hop response loading.
 */
export const AgentChatSkeleton: React.FC<{ className?: string }> = ({ className }) => {
  return (
    <div className={clsx('space-y-4 p-4', className)}>
      {/* Bot response skeleton */}
      <div className="flex flex-col items-start space-y-1.5 max-w-[85%]">
        <div className="flex items-center gap-2">
          <Skeleton className="w-4 h-4 rounded-full" />
          <Skeleton className="h-3 w-32" />
        </div>
        <div className="p-4 rounded-xl rounded-bl-none border border-border bg-surface w-full space-y-3 shadow-subtle">
          {/* Reasoning banner skeleton */}
          <div className="p-2.5 rounded-lg bg-surface-subtle border border-border flex items-center gap-2">
            <Skeleton className="w-3.5 h-3.5 rounded-full" />
            <Skeleton className="h-3 w-48" />
          </div>

          {/* Staggered text lines */}
          <div className="space-y-2 pt-1">
            <Skeleton className="h-3.5 w-[92%]" />
            <Skeleton className="h-3.5 w-[80%]" />
            <Skeleton className="h-3.5 w-[88%]" />
            <Skeleton className="h-3.5 w-[65%]" />
          </div>

          {/* Tool execution pills skeleton */}
          <div className="pt-2 border-t border-border flex gap-2">
            <Skeleton className="h-6 w-28 rounded-full" />
            <Skeleton className="h-6 w-32 rounded-full" />
          </div>
        </div>
      </div>
    </div>
  );
};
