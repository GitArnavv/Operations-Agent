import React from 'react';
import {
  LayoutDashboard,
  AlertTriangle,
  Bot,
  ShoppingBag,
  Package,
  Truck,
  Users,
  FileSpreadsheet,
  FileText,
  Activity,
  CheckSquare,
  Network,
  ShieldCheck,
  CreditCard,
  Settings,
  History,
  Building2,
  BarChart3,
  UserCheck,
  X,
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { PrefetchNavLink } from '../../contexts/RouteTransitionContext';

interface NavItem {
  name: string;
  path: string;
  icon: React.ElementType;
  badge?: string;
  badgeVariant?: 'primary' | 'warning' | 'neutral';
}

export const Sidebar: React.FC<{ isOpen?: boolean; onClose?: () => void }> = ({
  isOpen = false,
  onClose,
}) => {
  const { organization } = useAuth();

  const groups: { title: string; items: NavItem[] }[] = [
    {
      title: 'COMMAND',
      items: [
        { name: 'Command Center', path: '/dashboard', icon: LayoutDashboard },
        { name: 'Attention Center', path: '/attention', icon: AlertTriangle, badge: '3', badgeVariant: 'warning' },
        { name: 'AI Assistant', path: '/agent', icon: Bot },
      ],
    },
    {
      title: 'OPERATIONS',
      items: [
        { name: 'Orders & Deliveries', path: '/orders', icon: ShoppingBag, badge: '2 Due', badgeVariant: 'warning' },
        { name: 'Inventory & Stock', path: '/inventory', icon: Package },
        { name: 'Suppliers & Vendors', path: '/suppliers', icon: Truck },
        { name: 'Customer Accounts', path: '/customers', icon: Users },
      ],
    },
    {
      title: 'FINANCE',
      items: [
        { name: 'Invoices & GST', path: '/invoices', icon: FileSpreadsheet },
        { name: 'Analytics & Trends', path: '/analytics', icon: BarChart3 },
        { name: 'SaaS Subscription', path: '/billing', icon: CreditCard },
      ],
    },
    {
      title: 'AUTOMATION',
      items: [
        { name: 'Monitoring Rules', path: '/monitoring', icon: Activity },
        { name: 'Pending Approvals', path: '/approvals', icon: CheckSquare, badge: '1', badgeVariant: 'primary' },
        { name: 'Agent Observability', path: '/agent-activity', icon: History },
      ],
    },
    {
      title: 'DATA',
      items: [
        { name: 'Document Vault (OCR)', path: '/documents', icon: FileText },
      ],
    },
    {
      title: 'ADMIN',
      items: [
        { name: 'Settings & Policy', path: '/settings', icon: Settings },
        { name: 'ERP Integrations', path: '/integrations', icon: Network },
        { name: 'Team & RBAC', path: '/team', icon: UserCheck },
        { name: 'Audit Trail', path: '/audit-logs', icon: ShieldCheck },
      ],
    },
  ];

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-overlay lg:hidden animate-fade-in"
          onClick={onClose}
        />
      )}

      <aside
        className={`fixed lg:sticky top-0 inset-y-0 left-0 z-sidebar w-60 bg-surface border-r border-border flex flex-col h-screen select-none shrink-0 overflow-hidden transition-transform duration-200 lg:translate-x-0 ${
          isOpen ? 'translate-x-0 shadow-elevated' : '-translate-x-full'
        } font-sans`}
      >
        {/* Brand Header */}
        <div className="h-14 sm:h-16 px-4 border-b border-border flex items-center justify-between shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#FF6500] to-[#FF4D00] flex items-center justify-center text-white font-bold text-xs shadow-[0_0_12px_rgba(255,90,0,0.3)]">
              AI
            </div>
            <div>
              <span className="text-xs font-bold tracking-tight text-foreground block leading-tight">
                AIOps Enterprise
              </span>
              <span className="text-[10px] text-muted-foreground font-mono leading-none">
                India Supply Chain
              </span>
            </div>
          </div>
          {onClose && (
            <button
              onClick={onClose}
              className="lg:hidden p-1 rounded-md text-muted-foreground hover:text-foreground hover:bg-surface-hover"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Tenant Organization Capsule */}
        <div className="mx-3 mt-3 mb-2 p-2.5 rounded-xl bg-white/[0.02] text-xs shrink-0">
          <div className="flex items-center gap-1.5 text-foreground font-semibold truncate text-[11px]">
            <Building2 className="w-3.5 h-3.5 text-primary shrink-0" />
            <span className="truncate">{organization?.name || 'Sharma Electricals Pvt. Ltd.'}</span>
          </div>
          <div className="flex items-center justify-between mt-1.5 pt-1 text-[10px] font-mono text-muted-foreground">
            <span>GSTIN: 27AABCS...</span>
            <span className="text-[var(--status-success-text)] font-semibold flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-[var(--status-success-text)]"></span>
              Active
            </span>
          </div>
        </div>

        {/* Navigation Sections with internal isolated scrolling */}
        <nav className="flex-1 overflow-y-auto overflow-x-hidden min-h-0 px-2 py-2 space-y-4 scrollbar-thin">
          {groups.map((group) => (
            <div key={group.title}>
              <div className="px-3 mb-1 text-[10px] font-mono font-semibold tracking-wider text-muted-foreground uppercase opacity-75">
                {group.title}
              </div>
              <div className="space-y-0.5">
                {group.items.map((item) => {
                  const Icon = item.icon;
                  return (
                    <PrefetchNavLink
                      key={item.path}
                      to={item.path}
                      onClick={() => onClose && onClose()}
                      className={({ isActive }) =>
                        `relative flex items-center justify-between px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                          isActive
                            ? 'text-white font-semibold bg-white/[0.04]'
                            : 'text-muted-foreground hover:text-white hover:bg-white/[0.03]'
                        }`
                      }
                    >
                      {({ isActive }) => (
                        <>
                          {/* Active vertical orange line indicator (Engineering Digest Style: │ Command Center) */}
                          {isActive && (
                            <span className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-4 bg-primary rounded-r shadow-[0_0_8px_rgba(255,90,0,0.8)]" />
                          )}
                          <div className="flex items-center gap-2.5 truncate">
                            <Icon className={`w-4 h-4 shrink-0 transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground group-hover:text-white'}`} />
                            <span className="truncate">{item.name}</span>
                          </div>
                          {item.badge && (
                            <span
                              className={`px-1.5 py-0.2 rounded text-[10px] font-mono font-semibold ${
                                item.badgeVariant === 'warning'
                                  ? 'bg-[var(--status-warning-bg)] text-[var(--status-warning-text)]'
                                  : item.badgeVariant === 'primary'
                                  ? 'bg-primary/10 text-primary'
                                  : 'bg-surface-subtle text-muted-foreground'
                              }`}
                            >
                              {item.badge}
                            </span>
                          )}
                        </>
                      )}
                    </PrefetchNavLink>
                  );
                })}
              </div>
            </div>
          ))}
        </nav>

        {/* Footer Info */}
        <div className="p-3 text-[10px] text-muted-foreground flex items-center justify-between shrink-0 font-mono">
          <span>Bhiwandi & Pune Hubs</span>
          <span className="text-foreground font-semibold">v1.2</span>
        </div>
      </aside>
    </>
  );
};
