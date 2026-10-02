import React from 'react';
import clsx from 'clsx';
import { Skeleton, MetricTileSkeleton, TableSkeleton, AgentChatSkeleton } from './Skeleton';

export interface PageSkeletonLoaderProps {
  variant?: 'dashboard' | 'table' | 'agent' | 'attention' | 'generic';
  className?: string;
}

/**
 * Standard Header Area Skeleton matching PageHeader component structure.
 */
export const PageHeaderSkeleton: React.FC = () => {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2">
      <div className="space-y-2">
        {/* Breadcrumbs placeholder */}
        <div className="flex items-center gap-2">
          <Skeleton className="h-3 w-16" />
          <span className="text-muted-foreground/30 text-xs">/</span>
          <Skeleton className="h-3 w-28" />
        </div>
        {/* Title and Subtitle */}
        <div className="flex items-center gap-3">
          <Skeleton className="h-7 w-52 sm:w-64 rounded-lg" />
          <Skeleton className="h-5 w-20 rounded-full" />
        </div>
        <Skeleton className="h-3.5 w-60 sm:w-80" />
      </div>

      {/* Action controls placeholder */}
      <div className="flex items-center gap-2.5 shrink-0 pt-1 sm:pt-0">
        <Skeleton className="h-8.5 w-28 rounded-lg" />
        <Skeleton className="h-8.5 w-32 rounded-lg" />
      </div>
    </div>
  );
};

/**
 * High-Fidelity Bento 4-Card Metric Grid Skeleton.
 */
export const MetricGridSkeleton: React.FC<{ count?: number }> = ({ count = 4 }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
      {Array.from({ length: count }).map((_, idx) => (
        <div
          key={idx}
          className="p-4 sm:p-5 rounded-xl border border-border bg-surface flex flex-col justify-between h-32 relative overflow-hidden"
        >
          {/* Top Row: Label + Icon */}
          <div className="flex items-center justify-between">
            <Skeleton className="h-3.5 w-24" />
            <Skeleton className="h-7 w-7 rounded-lg" />
          </div>

          {/* Middle Row: Primary KPI Metric + Trend Pill */}
          <div className="flex items-baseline justify-between mt-2">
            <Skeleton className="h-8 w-32" />
            <Skeleton className="h-5 w-14 rounded-full" />
          </div>

          {/* Bottom Row: Context line */}
          <div className="pt-2 border-t border-border/40 flex items-center justify-between">
            <Skeleton className="h-2.5 w-28" />
            <Skeleton className="h-2.5 w-12" />
          </div>
        </div>
      ))}
    </div>
  );
};

/**
 * High-Fidelity Attention Triage Skeleton.
 */
export const AttentionTriageSkeleton: React.FC = () => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div className="lg:col-span-2 space-y-3">
        {/* Filter bar */}
        <div className="flex items-center justify-between gap-3 p-3 rounded-xl border border-border bg-surface">
          <div className="flex gap-2">
            <Skeleton className="h-7 w-20 rounded-lg" />
            <Skeleton className="h-7 w-24 rounded-lg" />
            <Skeleton className="h-7 w-20 rounded-lg" />
          </div>
          <Skeleton className="h-7 w-28 rounded-lg" />
        </div>

        {/* Attention item cards */}
        {Array.from({ length: 4 }).map((_, idx) => (
          <div
            key={idx}
            className="p-4 rounded-xl border border-border bg-surface space-y-3"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-2.5">
                <Skeleton className="w-8 h-8 rounded-lg" />
                <div className="space-y-1.5">
                  <Skeleton className="h-4 w-48" />
                  <Skeleton className="h-3 w-32" />
                </div>
              </div>
              <Skeleton className="h-5 w-20 rounded-full" />
            </div>
            <Skeleton className="h-3.5 w-full" />
            <div className="flex items-center justify-between pt-2 border-t border-border/50">
              <Skeleton className="h-3 w-36" />
              <div className="flex gap-2">
                <Skeleton className="h-7 w-20 rounded-md" />
                <Skeleton className="h-7 w-24 rounded-md" />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Right Drawer / Detail View Skeleton */}
      <div className="p-5 rounded-xl border border-border bg-surface space-y-4 h-[480px]">
        <div className="flex items-center justify-between pb-3 border-b border-border">
          <Skeleton className="h-5 w-32" />
          <Skeleton className="h-6 w-16 rounded-full" />
        </div>
        <div className="space-y-3">
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-20 w-full rounded-lg" />
          <Skeleton className="h-4 w-1/2" />
          <Skeleton className="h-24 w-full rounded-lg" />
        </div>
        <div className="pt-4 border-t border-border flex gap-2">
          <Skeleton className="h-9 w-full rounded-lg" />
        </div>
      </div>
    </div>
  );
};

/**
 * High-Fidelity Agent Page Skeleton.
 */
export const AgentPageSkeleton: React.FC = () => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 h-[calc(100vh-140px)]">
      {/* Sessions / Presets Sidebar */}
      <div className="hidden lg:flex flex-col p-4 rounded-xl border border-border bg-surface space-y-4">
        <Skeleton className="h-8 w-full rounded-lg" />
        <div className="space-y-2 flex-1">
          {Array.from({ length: 6 }).map((_, idx) => (
            <Skeleton key={idx} className="h-10 w-full rounded-lg" />
          ))}
        </div>
      </div>

      {/* Main Conversation Stream */}
      <div className="lg:col-span-3 flex flex-col rounded-xl border border-border bg-surface overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Skeleton className="w-8 h-8 rounded-lg" />
            <div className="space-y-1">
              <Skeleton className="h-4 w-36" />
              <Skeleton className="h-3 w-24" />
            </div>
          </div>
          <Skeleton className="h-7 w-20 rounded-md" />
        </div>

        {/* Message Stream */}
        <div className="flex-1 p-4 space-y-4 overflow-y-auto">
          <AgentChatSkeleton />
          <div className="flex justify-end">
            <div className="p-3.5 rounded-xl rounded-br-none bg-primary/10 border border-primary/20 max-w-[70%] space-y-2">
              <Skeleton className="h-3.5 w-48" />
              <Skeleton className="h-3.5 w-32" />
            </div>
          </div>
        </div>

        {/* Bottom Input Dock */}
        <div className="p-3.5 border-t border-border bg-surface-subtle flex items-center gap-3">
          <Skeleton className="h-10 flex-1 rounded-xl" />
          <Skeleton className="h-10 w-10 rounded-xl" />
        </div>
      </div>
    </div>
  );
};

/**
 * Universal High-Fidelity Page Skeleton Loader.
 * Designed to guarantee zero blank frames, zero layout shift, and instant visual continuity.
 */
export const PageSkeletonLoader: React.FC<PageSkeletonLoaderProps> = ({
  variant = 'generic',
  className = '',
}) => {
  return (
    <div
      className={clsx(
        'w-full space-y-6 animate-fade-in transition-opacity duration-150',
        className
      )}
      aria-label="Loading content..."
      role="status"
    >
      {/* Top Header Placeholder */}
      <PageHeaderSkeleton />

      {/* Route-Specific Content Skeletons */}
      {variant === 'agent' ? (
        <AgentPageSkeleton />
      ) : variant === 'attention' ? (
        <>
          <MetricGridSkeleton count={4} />
          <AttentionTriageSkeleton />
        </>
      ) : variant === 'dashboard' ? (
        <>
          <MetricGridSkeleton count={4} />
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 p-5 rounded-xl border border-border bg-surface space-y-4">
              <div className="flex items-center justify-between pb-3 border-b border-border">
                <Skeleton className="h-5 w-40" />
                <Skeleton className="h-7 w-24 rounded-md" />
              </div>
              <Skeleton className="h-64 w-full rounded-lg" />
            </div>
            <div className="p-5 rounded-xl border border-border bg-surface space-y-4">
              <div className="flex items-center justify-between pb-3 border-b border-border">
                <Skeleton className="h-5 w-32" />
                <Skeleton className="h-5 w-16 rounded-full" />
              </div>
              <div className="space-y-3">
                {Array.from({ length: 5 }).map((_, idx) => (
                  <div key={idx} className="p-3 rounded-lg bg-surface-subtle border border-border/50 space-y-1.5">
                    <Skeleton className="h-3.5 w-3/4" />
                    <Skeleton className="h-2.5 w-1/2" />
                  </div>
                ))}
              </div>
            </div>
          </div>
          <TableSkeleton rows={5} columns={6} />
        </>
      ) : variant === 'table' ? (
        <>
          <MetricGridSkeleton count={4} />
          {/* Search & Filter Bar Skeleton */}
          <div className="flex items-center justify-between gap-4 p-3 rounded-xl border border-border bg-surface">
            <Skeleton className="h-8.5 w-72 rounded-lg" />
            <div className="flex gap-2">
              <Skeleton className="h-8.5 w-24 rounded-lg" />
              <Skeleton className="h-8.5 w-24 rounded-lg" />
            </div>
          </div>
          <TableSkeleton rows={7} columns={6} />
        </>
      ) : (
        <>
          <MetricGridSkeleton count={4} />
          <TableSkeleton rows={6} columns={5} />
        </>
      )}
    </div>
  );
};
