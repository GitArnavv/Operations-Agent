/**
 * Centralized Route Prefetching Engine for AIOps India Frontend.
 * Allows instant, optimistic chunk preloading on hover/focus to achieve 0ms perception delay.
 */

type RouteLoader = () => Promise<unknown>;

const prefetchRegistry: Record<string, RouteLoader> = {
  '/dashboard': () => import('../pages/DashboardPage'),
  '/attention': () => import('../pages/AttentionPage'),
  '/agent': () => import('../pages/AgentPage'),
  '/analytics': () => import('../pages/AnalyticsPage'),
  '/orders': () => import('../pages/OrdersPage'),
  '/inventory': () => import('../pages/InventoryPage'),
  '/suppliers': () => import('../pages/SuppliersPage'),
  '/customers': () => import('../pages/CustomersPage'),
  '/invoices': () => import('../pages/InvoicesPage'),
  '/documents': () => import('../pages/DocumentsPage'),
  '/billing': () => import('../pages/BillingPage'),
  '/monitoring': () => import('../pages/MonitoringPage'),
  '/approvals': () => import('../pages/ApprovalsPage'),
  '/agent-activity': () => import('../pages/AgentActivityPage'),
  '/integrations': () => import('../pages/IntegrationsPage'),
  '/team': () => import('../pages/TeamPage'),
  '/audit-logs': () => import('../pages/AuditLogsPage'),
  '/settings': () => import('../pages/SettingsPage'),
};

const prefetchedCache = new Set<string>();

/**
 * Prefetches the dynamic module for a given path if registered and not already loaded.
 */
export function prefetchRoute(path: string): void {
  // Normalize path (strip query params and trailing slash)
  const normalizedPath = path.split('?')[0].replace(/\/$/, '') || '/dashboard';
  
  if (prefetchedCache.has(normalizedPath)) {
    return;
  }

  const loader = prefetchRegistry[normalizedPath];
  if (loader) {
    prefetchedCache.add(normalizedPath);
    loader().catch((err) => {
      // Allow retry on future interaction if network temporarily failed
      prefetchedCache.delete(normalizedPath);
      console.warn(`[Prefetch] Failed to preload route chunk for ${normalizedPath}:`, err);
    });
  }
}

/**
 * Determines the matching skeleton variant for a given pathname.
 */
export function getSkeletonVariantForPath(pathname: string): 'dashboard' | 'table' | 'agent' | 'attention' | 'generic' {
  if (pathname.includes('/agent')) return 'agent';
  if (pathname.includes('/attention')) return 'attention';
  if (pathname.includes('/dashboard') || pathname === '/') return 'dashboard';
  if (
    pathname.includes('/orders') ||
    pathname.includes('/inventory') ||
    pathname.includes('/invoices') ||
    pathname.includes('/suppliers') ||
    pathname.includes('/customers') ||
    pathname.includes('/documents') ||
    pathname.includes('/audit-logs')
  ) {
    return 'table';
  }
  return 'generic';
}
