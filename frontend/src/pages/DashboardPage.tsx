import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  ArrowRight,
  Sparkles,
  ChevronRight,
  Building2,
  Calendar,
  Layers,
  ShoppingBag,
  Package,
  Truck,
  CreditCard,
  AlertTriangle,
} from 'lucide-react';
import { dashboardApi } from '../api/services';
import type { DashboardData } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useAgent } from '../contexts/AgentContext';
import { useRouteTransition } from '../contexts/RouteTransitionContext';
import { InteractiveChart } from '../components/ui/InteractiveChart';
import { StatusBadge } from '../components/ui/StatusBadge';
import { Skeleton } from '../components/ui/Skeleton';
import { SpotlightCard } from '../components/ui/SpotlightCard';
import { AnimatedCounter } from '../components/ui/AnimatedCounter';
import { MotionButton } from '../components/ui/MotionButton';
import { StaggerContainer, StaggerItem } from '../components/ui/StaggerContainer';
import {
  formatInrCompact,
  formatPercent,
  formatNumber,
  formatDays,
} from '../utils/formatters';

export const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [aiInput, setAiInput] = useState('');
  const { organization } = useAuth();
  const { sendMessage, setIsDrawerOpen } = useAgent();
  const { navigateWithTransition, prefetch } = useRouteTransition();
  const navigate = useNavigate();

  useEffect(() => {
    let isSubscribed = true;
    dashboardApi
      .getDashboard()
      .then((res) => {
        if (isSubscribed) {
          setData(res);
        }
      })
      .catch((err) => {
        console.error('[DashboardPage] Failed to fetch dashboard data:', err);
      })
      .finally(() => {
        if (isSubscribed) {
          setLoading(false);
        }
      });

    return () => {
      isSubscribed = false;
    };
  }, []);

  const handleAskAI = (promptText: string) => {
    sendMessage(promptText);
    setIsDrawerOpen(true);
  };

  const handlePromptSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!aiInput.trim()) return;
    handleAskAI(aiInput);
    setAiInput('');
  };

  const kpis = data?.kpis;
  const activeOrders = kpis?.activeOrdersCount ?? 42;
  const catalogSkus = kpis?.totalCatalogSkus ?? 500;
  const delayedCount = kpis?.delayedOrdersCount ?? 2;
  const lowStockCount = kpis?.lowStockItemsCount ?? 3;
  const overdueAmount = kpis?.overdueInvoicesAmountInr ?? 480000;
  const overdueFormatted = formatInrCompact(overdueAmount, { shortSuffix: true });

  const supplierOnTime = kpis?.averageSupplierOnTimeRate ?? 84.2;
  const ordersHealth: 'Warning' | 'Healthy' = delayedCount > 0 ? 'Warning' : 'Healthy';
  const inventoryHealth: 'Warning' | 'Healthy' = lowStockCount > 2 ? 'Warning' : 'Healthy';
  const suppliersHealth: 'Warning' | 'Healthy' = supplierOnTime >= 95 ? 'Healthy' : 'Warning';
  const receivablesHealth: 'Warning' | 'Healthy' = overdueAmount > 300000 ? 'Warning' : 'Healthy';

  // Sample historical trend data for interactive charts
  const fulfillmentData = [
    { label: 'Sep 1', value: 92, secondaryValue: 95 },
    { label: 'Sep 5', value: 94, secondaryValue: 95 },
    { label: 'Sep 10', value: 91, secondaryValue: 95 },
    { label: 'Sep 15', value: 86, secondaryValue: 95 },
    { label: 'Sep 20', value: 88, secondaryValue: 95 },
    { label: 'Sep 25', value: 85, secondaryValue: 95 },
    { label: 'Oct 1', value: 84, secondaryValue: 95 },
  ];

  const inventoryCoverData = [
    { label: 'Power Cables', value: 3.4 },
    { label: 'MCB Switchgear', value: 6.8 },
    { label: 'Transformers', value: 14.2 },
    { label: 'Control Panels', value: 21.0 },
    { label: 'PVC Conduits', value: 28.5 },
  ];

  return (
    <StaggerContainer className="space-y-10 max-w-7xl mx-auto pb-20 font-sans">
      {/* ========================================================
          1. HERO SECTION: CINEMATIC EDITORIAL COMMAND CENTER
          ======================================================== */}
      <StaggerItem className="relative pt-4 sm:pt-6">
        {/* Ambient Top Glow Layer */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-3xl h-64 ambient-glow-hero pointer-events-none opacity-80" />

        {/* Eyebrow Status */}
        <div className="inline-flex items-center gap-2 py-1 text-xs font-mono mb-4 text-muted-foreground">
          <span className="w-2 h-2 rounded-full bg-[var(--status-success-text)] shadow-[0_0_8px_rgba(52,211,153,0.8)] animate-pulse" />
          <span className="text-[11px] font-semibold uppercase tracking-wider text-[var(--status-success-text)]">
            Autonomous Operations Active
          </span>
          <span className="opacity-40">&bull;</span>
          <span className="text-[11px] text-muted-foreground">
            {organization?.name || 'Sharma Electricals Pvt. Ltd.'}
          </span>
        </div>

        {/* Display Heading with Selective Warm Orange Accent */}
        <div className="space-y-3 max-w-4xl">
          <h1 className="display-hero text-foreground tracking-tight">
            Operations <br className="hidden sm:inline" />
            <span className="text-primary text-orange-glow">
              Command Center
            </span>
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground max-w-2xl leading-relaxed">
            Your autonomous intelligence layer for Indian supply chain operations. Continuously synthesizing dispatch schedules, GST e-way bills, stockout horizons, and vendor OTIF.
          </p>
        </div>

        {/* Context metadata */}
        <div className="flex flex-wrap items-center gap-4 mt-6 text-xs text-muted-foreground font-mono text-[11px]">
          <div className="flex items-center gap-1.5 text-foreground/80">
            <Calendar className="w-3.5 h-3.5 text-primary" />
            <span>Friday, 2 Oct 2026</span>
          </div>
          <span className="opacity-30">&bull;</span>
          <div className="flex items-center gap-1.5 text-foreground/80">
            <Building2 className="w-3.5 h-3.5 text-primary" />
            <span>GSTIN: 27AABCS1429B1Z2</span>
          </div>
          <span className="opacity-30">&bull;</span>
          <div className="flex items-center gap-1.5 text-foreground/80">
            <Layers className="w-3.5 h-3.5 text-primary" />
            <span>Hubs: Bhiwandi & Pune</span>
          </div>
        </div>

        {/* AI Agent Command Dock with Spotlight Glow */}
        <SpotlightCard className="mt-8 p-5 sm:p-6 focus-within:border-primary/60 focus-within:shadow-[0_0_0_1px_rgba(255,90,0,0.35),0_0_35px_rgba(255,90,0,0.10)]">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2 text-xs font-semibold text-primary font-mono uppercase tracking-wide">
              <Sparkles className="w-4 h-4" />
              <span>Ask Operations Agent</span>
            </div>
            <span className="text-[10px] font-mono text-muted-foreground hidden sm:inline">
              Natural Language &bull; Real-time ERP Synthesis
            </span>
          </div>

          <form onSubmit={handlePromptSubmit} className="relative">
            <input
              type="text"
              value={aiInput}
              onChange={(e) => setAiInput(e.target.value)}
              placeholder="Ask your operations agent anything... (e.g. 'What needs my attention today?')"
              className="w-full bg-surface-subtle border border-border rounded-xl px-4 py-3.5 pr-36 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50 transition-colors"
            />
            <div className="absolute right-2 top-1/2 -translate-y-1/2">
              <MotionButton type="submit" variant="primary" size="sm">
                <span>Investigate</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </MotionButton>
            </div>
          </form>

          {/* Quick Query Suggestions with Magnetic Pull */}
          <div className="flex flex-wrap items-center gap-2 pt-3 text-xs">
            <span className="text-[10px] font-mono text-muted-foreground uppercase tracking-wider mr-1">
              Suggested:
            </span>
            {[
              'What needs my attention today?',
              "Why is Sharma Electronics' order delayed?",
              'Which SKUs will stock out this week?',
              'Show overdue receivables above ₹1 lakh',
            ].map((prompt) => (
              <motion.button
                key={prompt}
                whileHover={{ y: -1, scale: 1.02 }}
                whileTap={{ scale: 0.96 }}
                onClick={() => handleAskAI(prompt)}
                className="px-2.5 py-1 rounded-md text-muted-foreground hover:text-white hover:bg-white/[0.05] border border-border/40 hover:border-orange-500/30 text-xs transition-colors flex items-center gap-1 group cursor-pointer"
              >
                <span>&ldquo;{prompt}&rdquo;</span>
                <ArrowRight className="w-3 h-3 text-primary opacity-0 group-hover:opacity-100 transition-opacity" />
              </motion.button>
            ))}
          </div>
        </SpotlightCard>
      </StaggerItem>

      {/* ========================================================
          2. UNIFIED METRIC STRIP (BENTO SPOTLIGHT CARDS)
          ======================================================== */}
      <StaggerItem className="space-y-3">
        <div className="flex items-center justify-between px-1">
          <h2 className="text-xs font-bold uppercase tracking-wider text-muted-foreground font-mono">
            Operational Health Overview
          </h2>
          <span className="text-[11px] font-mono text-muted-foreground">
            Telemetry from ERP &bull; Bhiwandi / Pune Hubs
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
          {/* 1. Orders & Deliveries */}
          <SpotlightCard
            onClick={() => navigateWithTransition('/orders')}
            onMouseEnter={() => prefetch('/orders')}
            className="p-5 cursor-pointer flex flex-col justify-between h-36"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground group-hover:text-foreground transition-colors">
                  Active Orders
                </span>
                <StatusBadge
                  variant={ordersHealth === 'Healthy' ? 'success' : 'warning'}
                  label={ordersHealth}
                />
              </div>
              <div className="flex items-baseline gap-2 mt-1">
                {loading ? (
                  <Skeleton className="h-9 w-20" />
                ) : (
                  <span className="text-3xl font-bold font-mono text-foreground tracking-tight">
                    <AnimatedCounter value={activeOrders} />
                  </span>
                )}
                <span className="text-xs text-muted-foreground font-medium">orders</span>
              </div>
            </div>
            <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground border-t border-border/40 pt-2">
              <span className="text-[11px] text-[var(--status-warning-text)] font-mono">
                {delayedCount} delayed shipments
              </span>
              <ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all text-primary" />
            </div>
          </SpotlightCard>

          {/* 2. Inventory & Stock Cover */}
          <SpotlightCard
            onClick={() => navigateWithTransition('/inventory')}
            onMouseEnter={() => prefetch('/inventory')}
            className="p-5 cursor-pointer flex flex-col justify-between h-36"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground group-hover:text-foreground transition-colors">
                  SKUs Monitored
                </span>
                <StatusBadge
                  variant={inventoryHealth === 'Healthy' ? 'success' : 'warning'}
                  label={inventoryHealth}
                />
              </div>
              <div className="flex items-baseline gap-2 mt-1">
                {loading ? (
                  <Skeleton className="h-9 w-24" />
                ) : (
                  <span className="text-3xl font-bold font-mono text-foreground tracking-tight">
                    <AnimatedCounter value={catalogSkus} />
                  </span>
                )}
                <span className="text-xs text-muted-foreground font-medium">SKUs</span>
              </div>
            </div>
            <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground border-t border-border/40 pt-2">
              <span className="text-[11px] text-[var(--status-danger-text)] font-mono">
                {lowStockCount} below safety stock
              </span>
              <ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all text-primary" />
            </div>
          </SpotlightCard>

          {/* 3. Supplier Reliability & OTIF */}
          <SpotlightCard
            onClick={() => navigateWithTransition('/suppliers')}
            onMouseEnter={() => prefetch('/suppliers')}
            className="p-5 cursor-pointer flex flex-col justify-between h-36"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground group-hover:text-foreground transition-colors">
                  Supplier OTIF
                </span>
                <StatusBadge
                  variant={suppliersHealth === 'Healthy' ? 'success' : 'warning'}
                  label={suppliersHealth}
                />
              </div>
              <div className="flex items-baseline gap-2 mt-1">
                {loading ? (
                  <Skeleton className="h-9 w-24" />
                ) : (
                  <span className="text-3xl font-bold font-mono text-foreground tracking-tight">
                    <AnimatedCounter value={supplierOnTime} decimals={1} suffix="%" />
                  </span>
                )}
                <span className="text-xs text-muted-foreground font-medium">on-time</span>
              </div>
            </div>
            <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground border-t border-border/40 pt-2">
              <span className="text-[11px] text-muted-foreground font-mono">
                Polycab SLA breach
              </span>
              <ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all text-primary" />
            </div>
          </SpotlightCard>

          {/* 4. Overdue Receivables */}
          <SpotlightCard
            onClick={() => navigateWithTransition('/invoices')}
            onMouseEnter={() => prefetch('/invoices')}
            className="p-5 cursor-pointer flex flex-col justify-between h-36"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground group-hover:text-foreground transition-colors">
                  Receivables
                </span>
                <StatusBadge
                  variant={receivablesHealth === 'Healthy' ? 'success' : 'warning'}
                  label={receivablesHealth}
                />
              </div>
              <div className="flex items-baseline gap-2 mt-1">
                {loading ? (
                  <Skeleton className="h-9 w-24" />
                ) : (
                  <span className="text-3xl font-bold font-mono text-foreground tracking-tight">
                    {overdueFormatted}
                  </span>
                )}
                <span className="text-xs text-muted-foreground font-medium">overdue</span>
              </div>
            </div>
            <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground border-t border-border/40 pt-2">
              <span className="text-[11px] text-[var(--status-warning-text)] font-mono">
                {kpis?.overdueInvoicesCount || 2} overdue accounts
              </span>
              <ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all text-primary" />
            </div>
          </SpotlightCard>
        </div>
      </StaggerItem>

      {/* ========================================================
          3. ATTENTION CENTER: EDITORIAL ANOMALIES SECTION
          ======================================================== */}
      <StaggerItem className="space-y-6 pt-2">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-3">
          <div>
            <h2 className="display-section text-foreground tracking-tight">
              What needs <span className="text-primary text-orange-glow">your attention?</span>
            </h2>
            <p className="text-xs sm:text-sm text-muted-foreground mt-1 max-w-xl">
              High-priority exceptions synthesized by your autonomous agent, ranked by financial impact and business disruption.
            </p>
          </div>
          <motion.button
            whileHover={{ x: 2 }}
            onClick={() => navigateWithTransition('/attention')}
            onMouseEnter={() => prefetch('/attention')}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary hover:text-primary-hover transition-colors cursor-pointer"
          >
            <span>View All Attention Items</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </motion.button>
        </div>

        {/* Attention Cards Grid with Spotlight Glow */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {loading ? (
            Array.from({ length: 3 }).map((_, idx) => (
              <div key={idx} className="p-6 space-y-4 rounded-xl border border-border bg-surface">
                <div className="flex justify-between items-center">
                  <Skeleton className="h-5 w-28 rounded-full" />
                  <Skeleton className="h-4 w-16 rounded" />
                </div>
                <Skeleton className="h-5 w-4/5 rounded" />
                <Skeleton className="h-14 w-full rounded-lg" />
                <div className="pt-2 flex justify-between items-center border-t border-border/40">
                  <Skeleton className="h-3 w-16 rounded" />
                  <Skeleton className="h-4 w-20 rounded" />
                </div>
              </div>
            ))
          ) : (
            data?.attentionItems?.map((item) => (
              <SpotlightCard
                key={item.id}
                className="p-6 flex flex-col justify-between space-y-4"
              >
                <div>
                  <div className="flex items-center justify-between gap-2 mb-3">
                    <StatusBadge
                      variant={item.severity === 'CRITICAL' ? 'danger' : 'warning'}
                      label={`${item.severity} • ${item.type}`}
                    />
                    {item.financialImpactInr != null && item.financialImpactInr > 0 && (
                      <span className="text-xs font-bold font-mono text-primary">
                        {formatInrCompact(item.financialImpactInr, { shortSuffix: true })} Risk
                      </span>
                    )}
                  </div>

                  <h3 className="text-sm font-semibold text-foreground group-hover:text-primary transition-colors leading-snug">
                    {item.title}
                  </h3>
                  <p className="text-xs text-muted-foreground mt-2 line-clamp-3 leading-relaxed">
                    {item.description}
                  </p>
                </div>

                <div className="pt-2 flex items-center justify-between text-xs border-t border-border/40">
                  <span className="text-[10px] font-mono text-muted-foreground">
                    {item.entityId}
                  </span>
                  <motion.button
                    whileHover={{ x: 2 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => handleAskAI(item.promptToResolve || `Investigate ${item.title}`)}
                    className="text-primary hover:text-primary-hover font-semibold flex items-center gap-1 transition-all cursor-pointer"
                  >
                    <span>Investigate</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </motion.button>
                </div>
              </SpotlightCard>
            ))
          )}
        </div>
      </StaggerItem>

      {/* ========================================================
          4. ANALYTICS & FULFILLMENT TRENDS
          ======================================================== */}
      <StaggerItem className="space-y-6 pt-2">
        <div>
          <h2 className="display-section text-foreground tracking-tight">
            Operational <span className="text-primary text-orange-glow">Intelligence Trends</span>
          </h2>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            Fulfillment cycle times, safety stock coverage, and supplier OTIF benchmarks.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <SpotlightCard className="p-0 overflow-hidden">
            <InteractiveChart
              title="Order Fulfillment SLA & Cycle Time"
              subtitle="On-time delivery percentage vs target benchmark"
              data={fulfillmentData}
              type="area"
              valueSuffix="%"
              secondaryLabel="Target Benchmark"
            />
          </SpotlightCard>

          <SpotlightCard className="p-0 overflow-hidden">
            <InteractiveChart
              title="Days of Inventory Cover (DoC) by Category"
              subtitle="Projected days remaining before stockout"
              data={inventoryCoverData}
              type="bar"
              valueSuffix=" Days"
              colorVar="var(--chart-1)"
            />
          </SpotlightCard>
        </div>
      </StaggerItem>

      {/* ========================================================
          5. DELAYED ORDERS & LOW STOCK SPOTLIGHT TABLES
          ======================================================== */}
      <StaggerItem className="grid grid-cols-1 lg:grid-cols-2 gap-6 pt-2">
        {/* Critical Delayed Orders */}
        <SpotlightCard className="p-6 space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-border">
            <div>
              <h3 className="text-sm font-bold text-foreground">
                Critical Delayed Orders
              </h3>
              <p className="text-[11px] text-muted-foreground font-mono mt-0.5">
                Dispatch commitments at risk
              </p>
            </div>
            <motion.button
              whileHover={{ x: 2 }}
              onClick={() => navigateWithTransition('/orders')}
              onMouseEnter={() => prefetch('/orders')}
              className="text-xs text-primary font-semibold hover:underline flex items-center gap-1 cursor-pointer"
            >
              <span>All Orders</span>
              <ArrowRight className="w-3 h-3" />
            </motion.button>
          </div>

          <div className="divide-y divide-border">
            {loading ? (
              Array.from({ length: 3 }).map((_, idx) => (
                <div key={idx} className="py-3 flex items-center justify-between gap-3">
                  <div className="space-y-1.5 flex-1">
                    <Skeleton className="h-4 w-32 rounded" />
                    <Skeleton className="h-3 w-44 rounded" />
                  </div>
                  <div className="space-y-1.5 text-right">
                    <Skeleton className="h-4 w-16 ml-auto rounded" />
                    <Skeleton className="h-3 w-12 ml-auto rounded" />
                  </div>
                </div>
              ))
            ) : (
              data?.delayedOrders?.slice(0, 3).map((order) => (
                <div
                  key={order.id}
                  onClick={() => navigateWithTransition('/orders')}
                  className="py-3 flex items-center justify-between gap-3 text-xs hover:bg-white/[0.03] px-2 rounded-lg transition-colors cursor-pointer"
                >
                  <div>
                    <div className="font-semibold text-foreground">{order.orderNumber}</div>
                    <div className="text-[11px] text-muted-foreground">{order.customerName}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold font-mono text-foreground">
                      {formatInrCompact(order.totalAmountInr || order.totalAmount || 0, { shortSuffix: true })}
                    </div>
                    <div className="text-[11px] text-[var(--status-danger-text)] font-mono font-medium">
                      Delay: {order.delayDays}d
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </SpotlightCard>

        {/* Impending Stockout Risk */}
        <SpotlightCard className="p-6 space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-border">
            <div>
              <h3 className="text-sm font-bold text-foreground">
                Impending Stockout Risk
              </h3>
              <p className="text-[11px] text-muted-foreground font-mono mt-0.5">
                Categories with &lt; 7 days of stock cover
              </p>
            </div>
            <motion.button
              whileHover={{ x: 2 }}
              onClick={() => navigateWithTransition('/inventory')}
              onMouseEnter={() => prefetch('/inventory')}
              className="text-xs text-primary font-semibold hover:underline flex items-center gap-1 cursor-pointer"
            >
              <span>All Inventory</span>
              <ArrowRight className="w-3 h-3" />
            </motion.button>
          </div>

          <div className="divide-y divide-border">
            {loading ? (
              Array.from({ length: 3 }).map((_, idx) => (
                <div key={idx} className="py-3 flex items-center justify-between gap-3">
                  <div className="space-y-1.5 flex-1">
                    <Skeleton className="h-4 w-36 rounded" />
                    <Skeleton className="h-3 w-24 rounded" />
                  </div>
                  <div className="space-y-1.5 text-right">
                    <Skeleton className="h-4 w-16 ml-auto rounded" />
                    <Skeleton className="h-3 w-16 ml-auto rounded" />
                  </div>
                </div>
              ))
            ) : (
              data?.lowStockItems?.slice(0, 3).map((item) => (
                <div
                  key={item.id}
                  onClick={() => navigateWithTransition('/inventory')}
                  className="py-3 flex items-center justify-between gap-3 text-xs hover:bg-white/[0.03] px-2 rounded-lg transition-colors cursor-pointer"
                >
                  <div>
                    <div className="font-semibold text-foreground">{item.productName}</div>
                    <div className="text-[11px] text-muted-foreground font-mono">{item.sku}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold font-mono text-foreground">
                      {formatNumber(item.availableStock ?? item.availableQuantity ?? 0)} units
                    </div>
                    <div className="text-[11px] text-[var(--status-danger-text)] font-mono font-medium">
                      DoC: {formatDays(item.daysOfStockRemaining ?? item.daysOfInventoryRemaining ?? 0, 1)}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </SpotlightCard>
      </StaggerItem>
    </StaggerContainer>
  );
};
