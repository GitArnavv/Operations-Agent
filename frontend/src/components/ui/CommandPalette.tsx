import React, { useState, useEffect, useRef } from 'react';
import {
  Search,
  Bot,
  LayoutDashboard,
  ShoppingBag,
  Package,
  Truck,
  Users,
  FileSpreadsheet,
  CheckSquare,
  Activity,
  Settings,
  Sun,
  Moon,
  ArrowRight,
  Sparkles,
  Command,
} from 'lucide-react';
import { useAgent } from '../../contexts/AgentContext';
import { useTheme } from '../../contexts/ThemeContext';
import { useRouteTransition } from '../../contexts/RouteTransitionContext';

interface CommandItem {
  id: string;
  category: 'Navigation' | 'Actions' | 'Entities' | 'System';
  title: string;
  subtitle?: string;
  icon: React.ElementType;
  action: () => void;
  keywords?: string[];
}

export const CommandPalette: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const { navigateWithTransition } = useRouteTransition();
  const { sendMessage, setIsDrawerOpen } = useAgent();
  const { effectiveTheme, setTheme } = useTheme();

  // Listen for Cmd+K / Ctrl+K
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setIsOpen((prev) => !prev);
      } else if (e.key === 'Escape' && isOpen) {
        setIsOpen(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  // Focus input when opened
  useEffect(() => {
    if (isOpen) {
      setQuery('');
      setSelectedIndex(0);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [isOpen]);

  const closeAndExecute = (action: () => void) => {
    setIsOpen(false);
    action();
  };

  const commands: CommandItem[] = [
    {
      id: 'ai-prompt',
      category: 'Actions',
      title: query ? `Ask Operations AI: "${query}"` : 'Ask Operations AI...',
      subtitle: 'Analyze cross-system operational data with the autonomous agent',
      icon: Bot,
      action: () => {
        if (query.trim()) {
          sendMessage(query);
          setIsDrawerOpen(true);
        } else {
          setIsDrawerOpen(true);
        }
      },
    },
    {
      id: 'nav-dashboard',
      category: 'Navigation',
      title: 'Command Center',
      subtitle: 'Overview of operational KPIs, exceptions, and live alerts',
      icon: LayoutDashboard,
      action: () => navigateWithTransition('/dashboard'),
      keywords: ['home', 'overview', 'kpi'],
    },
    {
      id: 'nav-orders',
      category: 'Navigation',
      title: 'Orders & Deliveries',
      subtitle: 'Manage sales orders, track shipments, and diagnose delays',
      icon: ShoppingBag,
      action: () => navigateWithTransition('/orders'),
      keywords: ['delivery', 'fulfillment', 'dispatch', 'ord-1042'],
    },
    {
      id: 'nav-inventory',
      category: 'Navigation',
      title: 'Inventory & Stock',
      subtitle: 'Monitor warehouse SKU balances and stockout projections',
      icon: Package,
      action: () => navigateWithTransition('/inventory'),
      keywords: ['products', 'stock', 'warehouse', 'bhiwandi', 'pune', 'mcb'],
    },
    {
      id: 'nav-suppliers',
      category: 'Navigation',
      title: 'Suppliers & Vendors',
      subtitle: 'View vendor scorecards, on-time delivery rates, and POs',
      icon: Truck,
      action: () => navigateWithTransition('/suppliers'),
      keywords: ['polycab', 'havells', 'vendor', 'reliability', 'sla'],
    },
    {
      id: 'nav-customers',
      category: 'Navigation',
      title: 'Customer Accounts',
      subtitle: 'B2B dealer profiles, credit limits, and outstanding balances',
      icon: Users,
      action: () => navigateWithTransition('/customers'),
      keywords: ['dealers', 'sharma electronics', 'accounts'],
    },
    {
      id: 'nav-invoices',
      category: 'Navigation',
      title: 'Invoices & GST Compliance',
      subtitle: 'Track receivables, overdue aging, and tax breakdowns',
      icon: FileSpreadsheet,
      action: () => navigateWithTransition('/invoices'),
      keywords: ['gst', 'cgst', 'sgst', 'igst', 'overdue', 'payment'],
    },
    {
      id: 'nav-approvals',
      category: 'Navigation',
      title: 'Pending Approvals',
      subtitle: 'Review and approve high-risk AI-recommended actions',
      icon: CheckSquare,
      action: () => navigateWithTransition('/approvals'),
      keywords: ['human in the loop', 'po', 'transfer'],
    },
    {
      id: 'nav-monitoring',
      category: 'Navigation',
      title: 'Autonomous Monitoring Rules',
      subtitle: 'Configure real-time automated threshold rules and alerts',
      icon: Activity,
      action: () => navigateWithTransition('/monitoring'),
      keywords: ['rules', 'triggers', 'thresholds'],
    },
    {
      id: 'nav-settings',
      category: 'Navigation',
      title: 'Platform Settings',
      subtitle: 'Organization details, GSTIN configuration, and AI autonomy level',
      icon: Settings,
      action: () => navigateWithTransition('/settings'),
      keywords: ['config', 'organization', 'company'],
    },
    {
      id: 'theme-toggle',
      category: 'System',
      title: effectiveTheme === 'dark' ? 'Switch to Light Theme' : 'Switch to Dark Theme',
      subtitle: 'Toggle application visual appearance',
      icon: effectiveTheme === 'dark' ? Sun : Moon,
      action: () => setTheme(effectiveTheme === 'dark' ? 'light' : 'dark'),
      keywords: ['color', 'mode', 'theme', 'appearance'],
    },
  ];

  const filteredCommands = commands.filter((cmd) => {
    if (!query.trim()) return true;
    const q = query.toLowerCase();
    const titleMatch = cmd.title.toLowerCase().includes(q);
    const subtitleMatch = cmd.subtitle?.toLowerCase().includes(q);
    const keywordMatch = cmd.keywords?.some((k) => k.toLowerCase().includes(q));
    return titleMatch || subtitleMatch || keywordMatch;
  });

  // Handle keyboard navigation within list
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < filteredCommands.length - 1 ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : filteredCommands.length - 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const selected = filteredCommands[selectedIndex];
      if (selected) {
        closeAndExecute(selected.action);
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-command flex items-start justify-center pt-24 px-4 bg-overlay animate-fade-in font-sans">
      <div
        className="w-full max-w-2xl bg-surface-elevated border border-border rounded-xl shadow-elevated overflow-hidden animate-scale-in"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Search Input Bar */}
        <div className="flex items-center px-4 py-3.5 border-b border-border bg-surface">
          <Search className="w-5 h-5 text-muted-foreground mr-3 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setSelectedIndex(0);
            }}
            onKeyDown={handleKeyDown}
            placeholder="Type a command, search orders, or ask AI... (Press Esc to close)"
            className="w-full bg-transparent text-foreground placeholder:text-muted-foreground text-sm focus:outline-none"
          />
          <div className="flex items-center gap-1.5 ml-2">
            <span className="px-1.5 py-0.5 rounded bg-surface-subtle text-[10px] font-mono text-muted-foreground">
              ESC
            </span>
          </div>
        </div>

        {/* Command Items List */}
        <div className="max-h-96 overflow-y-auto p-2 space-y-1">
          {filteredCommands.length === 0 ? (
            <div className="py-10 text-center text-muted-foreground text-xs">
              <Bot className="w-8 h-8 text-primary mx-auto mb-2 opacity-60" />
              <p>No direct matching command found.</p>
              <button
                onClick={() =>
                  closeAndExecute(() => {
                    sendMessage(query);
                    setIsDrawerOpen(true);
                  })
                }
                className="mt-3 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground text-xs font-medium hover:bg-primary-hover transition-colors"
              >
                Ask Operations AI about &ldquo;{query}&rdquo;
              </button>
            </div>
          ) : (
            filteredCommands.map((cmd, idx) => {
              const Icon = cmd.icon;
              const isSelected = idx === selectedIndex;
              return (
                <div
                  key={cmd.id}
                  onClick={() => closeAndExecute(cmd.action)}
                  onMouseEnter={() => setSelectedIndex(idx)}
                  className={`flex items-center justify-between px-3.5 py-2.5 rounded-lg cursor-pointer transition-all ${
                    isSelected ? 'bg-surface-hover text-foreground' : 'text-foreground hover:bg-surface-subtle'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`p-2 rounded-lg border ${
                        isSelected
                          ? 'border-border bg-primary-subtle text-primary'
                          : 'border-border bg-surface text-muted-foreground'
                      }`}
                    >
                      <Icon className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-xs font-semibold flex items-center gap-2">
                        {cmd.title}
                        <span className="text-[9px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-surface-subtle text-muted-foreground border border-border">
                          {cmd.category}
                        </span>
                      </div>
                      {cmd.subtitle && (
                        <div className="text-[11px] text-muted-foreground line-clamp-1 mt-0.5">
                          {cmd.subtitle}
                        </div>
                      )}
                    </div>
                  </div>
                  {isSelected && <ArrowRight className="w-4 h-4 text-primary shrink-0" />}
                </div>
              );
            })
          )}
        </div>

        {/* Footer shortcuts */}
        <div className="px-4 py-2 border-t border-border bg-surface-subtle flex items-center justify-between text-[11px] text-muted-foreground">
          <div className="flex items-center gap-3">
            <span>
              <kbd className="font-mono text-[10px] px-1 rounded bg-surface border border-border">↑</kbd>{' '}
              <kbd className="font-mono text-[10px] px-1 rounded bg-surface border border-border">↓</kbd> Navigate
            </span>
            <span>
              <kbd className="font-mono text-[10px] px-1 rounded bg-surface border border-border">↵</kbd> Select
            </span>
          </div>
          <div className="flex items-center gap-1">
            <Sparkles className="w-3 h-3 text-primary" />
            <span className="text-[10px] font-medium">AIOps Autonomous Shell</span>
          </div>
        </div>
      </div>
    </div>
  );
};
