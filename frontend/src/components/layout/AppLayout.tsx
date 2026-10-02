import React, { useState, Suspense } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { AgentDrawer } from '../agent/AgentDrawer';
import { CommandPalette } from '../ui/CommandPalette';
import { PageSkeletonLoader } from '../ui/PageSkeletonLoader';
import { getSkeletonVariantForPath } from '../../utils/routePrefetch';
import { useAgent } from '../../contexts/AgentContext';
import { useMediaQuery } from '../../hooks/useMediaQuery';

export const AppLayout: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { isDrawerOpen } = useAgent();
  const isDesktop = useMediaQuery('(min-width: 1100px)');
  const location = useLocation();

  const skeletonVariant = getSkeletonVariantForPath(location.pathname);

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-background text-foreground font-sans theme-surface select-none">
      {/* 100% Pinned Left Navigation Sidebar */}
      <Sidebar isOpen={mobileMenuOpen} onClose={() => setMobileMenuOpen(false)} />

      {/* Main Application Area */}
      <div className="flex-1 flex flex-col h-screen min-w-0 min-h-0 overflow-hidden">
        {/* Sticky Top Header */}
        <Header onMenuToggle={() => setMobileMenuOpen(!mobileMenuOpen)} />

        {/* Workspace: Isolated scrollable main content and optional agent drawer */}
        <div className={`flex-1 flex min-w-0 min-h-0 overflow-hidden ${isDrawerOpen && isDesktop ? 'panel-open' : ''}`}>
          <main className="flex-1 h-full min-w-0 min-h-0 overflow-y-auto overflow-x-hidden bg-background border-l border-border/40 p-4 sm:p-6 scrollbar-thin">
            <Suspense fallback={<PageSkeletonLoader variant={skeletonVariant} />}>
              <Outlet />
            </Suspense>
          </main>

          {/* Persistent Desktop AI Orchestrator Pane */}
          {isDrawerOpen && isDesktop && (
            <AgentDrawer isMobileDrawer={false} />
          )}
        </div>
      </div>

      {/* Mobile/Tablet Overlay Drawer Mode */}
      {isDrawerOpen && !isDesktop && (
        <AgentDrawer isMobileDrawer={true} />
      )}

      {/* Global Command Bar (Ctrl + K) */}
      <CommandPalette />
    </div>
  );
};

export default AppLayout;

