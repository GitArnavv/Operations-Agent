import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  AlertTriangle,
  CheckCircle2,
  Bot,
  CheckSquare,
  Info,
  X,
  ExternalLink,
  CheckCheck,
} from 'lucide-react';

export interface NotificationItem {
  id: string;
  category: 'Critical' | 'AI' | 'Approvals' | 'System';
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  title: string;
  reason: string;
  entityId?: string;
  entityType?: 'ORDER' | 'INVENTORY' | 'SUPPLIER' | 'INVOICE' | 'APPROVAL';
  timestamp: string;
  isRead: boolean;
  link?: string;
}

const INITIAL_NOTIFICATIONS: NotificationItem[] = [
  {
    id: 'notif-1',
    category: 'Critical',
    severity: 'CRITICAL',
    title: 'Order #ORD-1042 Fulfillment Blocked',
    reason: 'Zero inventory available for Polycab 2.5mm² wire; supplier delivery delayed to Oct 4.',
    entityId: 'ORD-1042',
    entityType: 'ORDER',
    timestamp: '10m ago',
    isRead: false,
    link: '/orders',
  },
  {
    id: 'notif-2',
    category: 'Approvals',
    severity: 'WARNING',
    title: 'High-Risk Action Awaiting Approval',
    reason: 'PO proposal for Havells India Ltd (₹99,120) requires Operations Manager sign-off.',
    entityId: 'APR-7721',
    entityType: 'APPROVAL',
    timestamp: '25m ago',
    isRead: false,
    link: '/approvals',
  },
  {
    id: 'notif-3',
    category: 'AI',
    severity: 'WARNING',
    title: 'Impending Stockout Forecast',
    reason: 'Havells 32A MCB has only 4.2 days of supply remaining at current daily consumption rate.',
    entityId: 'PRD-102',
    entityType: 'INVENTORY',
    timestamp: '1h ago',
    isRead: false,
    link: '/inventory',
  },
  {
    id: 'notif-4',
    category: 'System',
    severity: 'INFO',
    title: 'Tally Prime Voucher Sync Complete',
    reason: 'Successfully reconciled 48 new sales invoices and 12 purchase vouchers from Bhiwandi hub.',
    timestamp: '2h ago',
    isRead: true,
    link: '/integrations',
  },
];

export const NotificationCenter: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>(INITIAL_NOTIFICATIONS);
  const [isOpen, setIsOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<'All' | 'Critical' | 'AI' | 'Approvals' | 'System'>('All');
  const dropdownRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const markAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  const markAsRead = (id: string) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
  };

  const dismiss = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const filtered = notifications.filter((n) => {
    if (activeTab === 'All') return true;
    return n.category === activeTab;
  });

  const getSeverityIcon = (n: NotificationItem) => {
    if (n.category === 'AI') return <Bot className="w-4 h-4 text-primary" />;
    if (n.category === 'Approvals') return <CheckSquare className="w-4 h-4 text-amber-500" />;
    if (n.severity === 'CRITICAL') return <AlertTriangle className="w-4 h-4 text-danger" />;
    if (n.severity === 'WARNING') return <AlertTriangle className="w-4 h-4 text-warning" />;
    return <Info className="w-4 h-4 text-info" />;
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Open notifications"
        className="relative p-2 rounded-lg text-muted-foreground hover:text-foreground hover:bg-white/[0.04] transition-colors focus:outline-none"
      >
        <Bell className="w-4 h-4" />
        {unreadCount > 0 && (
          <span className="absolute top-1.5 right-1.5 flex h-2 w-2 rounded-full bg-primary ring-2 ring-surface" />
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-96 max-w-[calc(100vw-2rem)] rounded-xl border border-border bg-[#141318] shadow-elevated z-popover overflow-hidden animate-scale-in font-sans">
          {/* Header */}
          <div className="p-4 border-b border-border bg-surface flex items-center justify-between">
            <div className="flex items-center gap-2">
              <h3 className="text-xs font-bold text-foreground">Operational Notifications</h3>
              {unreadCount > 0 && (
                <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-[10px] font-semibold">
                  {unreadCount} unread
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="text-[11px] text-muted-foreground hover:text-foreground flex items-center gap-1 transition-colors"
              >
                <CheckCheck className="w-3.5 h-3.5" />
                <span>Mark all read</span>
              </button>
            )}
          </div>

          {/* Category Tabs */}
          <div className="flex border-b border-border bg-surface-subtle px-2 py-1.5 gap-1 text-[11px] overflow-x-auto">
            {(['All', 'Critical', 'AI', 'Approvals', 'System'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-2.5 py-1 rounded-md font-medium transition-all ${
                  activeTab === tab
                    ? 'bg-surface text-foreground font-semibold'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* Notifications List */}
          <div className="max-h-80 overflow-y-auto divide-y divide-border">
            {filtered.length === 0 ? (
              <div className="p-8 text-center text-muted-foreground text-xs">
                <CheckCircle2 className="w-6 h-6 text-success mx-auto mb-2 opacity-60" />
                <p>No notifications in this category</p>
              </div>
            ) : (
              filtered.map((item) => (
                <div
                  key={item.id}
                  onClick={() => {
                    markAsRead(item.id);
                    if (item.link) {
                      navigate(item.link);
                      setIsOpen(false);
                    }
                  }}
                  className={`p-3.5 flex gap-3 hover:bg-surface-hover cursor-pointer transition-colors relative group ${
                    !item.isRead ? 'bg-surface-raised' : ''
                  }`}
                >
                  <div className="mt-0.5 shrink-0">{getSeverityIcon(item)}</div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-1 mb-0.5">
                      <h4
                        className={`text-xs truncate ${
                          !item.isRead ? 'font-bold text-foreground' : 'font-medium text-foreground'
                        }`}
                      >
                        {item.title}
                      </h4>
                      <span className="text-[10px] text-muted-foreground shrink-0">{item.timestamp}</span>
                    </div>
                    <p className="text-[11px] text-muted-foreground line-clamp-2 leading-relaxed">
                      {item.reason}
                    </p>
                    {item.entityId && (
                      <div className="mt-1.5 flex items-center gap-2">
                        <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-surface-subtle border border-border text-foreground">
                          {item.entityId}
                        </span>
                        <span className="text-[10px] text-primary flex items-center gap-0.5 font-medium group-hover:underline">
                          Inspect <ExternalLink className="w-2.5 h-2.5" />
                        </span>
                      </div>
                    )}
                  </div>
                  <button
                    onClick={(e) => dismiss(item.id, e)}
                    className="opacity-0 group-hover:opacity-100 p-1 text-muted-foreground hover:text-foreground transition-opacity"
                    title="Dismiss"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
