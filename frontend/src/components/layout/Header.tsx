import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Search,
  Mic,
  CheckSquare,
  LogOut,
  Menu,
  Command,
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useAgent } from '../../contexts/AgentContext';
import { useRouteTransition } from '../../contexts/RouteTransitionContext';
import { ThemeToggle } from '../ui/ThemeToggle';
import { NotificationCenter } from '../ui/NotificationCenter';

export const Header: React.FC<{ onMenuToggle?: () => void }> = ({ onMenuToggle }) => {
  const { user, logout } = useAuth();
  const { setIsDrawerOpen, sendMessage } = useAgent();
  const { navigateWithTransition, prefetch } = useRouteTransition();
  const [searchQuery, setSearchQuery] = useState('');
  const location = useLocation();

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    sendMessage(searchQuery);
    setIsDrawerOpen(true);
    setSearchQuery('');
  };

  const openCommandPalette = () => {
    window.dispatchEvent(new KeyboardEvent('keydown', { key: 'k', ctrlKey: true, metaKey: true }));
  };

  const navLinks = [
    { label: 'Command Center', path: '/dashboard' },
    { label: 'Operations', path: '/orders' },
    { label: 'Finance', path: '/invoices' },
    { label: 'Automation', path: '/monitoring' },
    { label: 'Data', path: '/documents' },
  ];

  return (
    <header className="h-14 sm:h-16 bg-surface/90 backdrop-blur-md border-b border-border px-4 sm:px-6 flex items-center justify-between z-header sticky top-0 shrink-0 font-sans theme-surface">
      {/* Left: Brand & Mobile Menu */}
      <div className="flex items-center gap-3 sm:gap-6 shrink-0">
        {onMenuToggle && (
          <button
            onClick={onMenuToggle}
            className="lg:hidden p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-surface-hover transition-colors"
            aria-label="Open menu"
          >
            <Menu className="w-5 h-5" />
          </button>
        )}

        <div
          className="flex items-center gap-2.5 cursor-pointer"
          onClick={() => navigateWithTransition('/dashboard')}
          onMouseEnter={() => prefetch('/dashboard')}
        >
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#FF6500] to-[#FF4D00] flex items-center justify-center text-white font-bold text-xs shadow-[0_0_12px_rgba(255,90,0,0.3)]">
            AI
          </div>
          <div>
            <div className="text-xs font-bold tracking-tight text-foreground flex items-center gap-1.5">
              <span>AI Ops India</span>
              <span className="w-1.5 h-1.5 rounded-full bg-[var(--status-success-text)] shadow-[0_0_8px_rgba(52,211,153,0.6)]"></span>
            </div>
            <div className="text-[10px] text-muted-foreground hidden sm:block">
              Autonomous Operations
            </div>
          </div>
        </div>

        {/* Center-Left Navigation Links (Engineering Digest Minimal Style) */}
        <nav className="hidden xl:flex items-center gap-1 ml-4 pl-4 border-l border-border">
          {navLinks.map((link) => {
            const isActive = location.pathname.startsWith(link.path);
            return (
              <button
                key={link.path}
                onClick={() => navigateWithTransition(link.path)}
                onMouseEnter={() => prefetch(link.path)}
                onFocus={() => prefetch(link.path)}
                className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors ${
                  isActive
                    ? 'text-white font-semibold'
                    : 'text-muted-foreground hover:text-white hover:bg-surface-hover'
                }`}
              >
                {link.label}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Center Search / Command Input */}
      <div className="hidden md:flex items-center flex-1 max-w-sm mx-4">
        <form onSubmit={handleSearchSubmit} className="relative w-full">
          <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Ask AI or jump to SKU, GSTIN... (Ctrl+K)"
            className="w-full bg-surface-subtle border border-border rounded-lg pl-8 pr-12 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors"
          />
          <button
            type="button"
            onClick={openCommandPalette}
            className="absolute right-2 top-1/2 -translate-y-1/2 px-1.5 py-0.5 text-[10px] font-mono text-muted-foreground bg-surface rounded border border-border hover:border-primary transition-colors flex items-center gap-0.5"
            title="Open Command Palette (Ctrl+K)"
          >
            <Command className="w-2.5 h-2.5" />
            <span>K</span>
          </button>
        </form>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-2 sm:gap-3">
        {/* Voice Trigger Button - Warm Orange Accent */}
        <button
          onClick={() => setIsDrawerOpen(true)}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-raised hover:bg-surface-hover text-foreground text-xs font-semibold hover:shadow-[0_0_16px_rgba(255,90,0,0.15)] transition-all group"
        >
          <Mic className="w-3.5 h-3.5 text-primary group-hover:scale-110 transition-transform" />
          <span className="hidden md:inline">Voice Agent</span>
          <span className="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
        </button>

        {/* Pending Approvals Quick Link */}
        <button
          onClick={() => navigateWithTransition('/approvals')}
          onMouseEnter={() => prefetch('/approvals')}
          className="relative p-2 rounded-lg text-muted-foreground hover:text-foreground hover:bg-white/[0.04] transition-colors"
          title="Pending Approvals"
        >
          <CheckSquare className="w-4 h-4 text-warning" />
          <span className="absolute 1 top-1.5 right-1.5 flex h-2 w-2 rounded-full bg-primary ring-2 ring-surface" />
        </button>

        {/* Notifications Center */}
        <NotificationCenter />

        {/* Theme Toggle (Dark / Light / System) */}
        <ThemeToggle />

        {/* User Profile */}
        <div className="flex items-center gap-2 sm:gap-2.5 ml-1">
          <div className="text-right hidden lg:block">
            <div className="text-xs font-bold text-foreground">{user?.fullName || 'Amit Patel'}</div>
            <div className="text-[10px] text-muted-foreground font-mono">Operations Manager</div>
          </div>

          <div className="w-8 h-8 rounded-full bg-surface-raised flex items-center justify-center text-primary text-xs font-bold font-mono">
            {user?.fullName ? user.fullName[0] : 'A'}
          </div>

          <button
            onClick={logout}
            className="p-1.5 text-muted-foreground hover:text-danger transition-colors rounded-md hover:bg-white/[0.04]"
            title="Log Out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
