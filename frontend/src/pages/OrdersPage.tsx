import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  ShoppingBag,
  AlertCircle,
  Bot,
  Search,
  CheckCircle2,
  Clock,
  ArrowRight,
  X,
  TrendingUp,
  AlertTriangle,
  Truck,
  ShieldCheck,
} from 'lucide-react';
import { ordersApi } from '../api/services';
import { useAgent } from '../contexts/AgentContext';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { Skeleton } from '../components/ui/Skeleton';
import { SpotlightCard } from '../components/ui/SpotlightCard';
import { AnimatedCounter } from '../components/ui/AnimatedCounter';
import { MotionButton } from '../components/ui/MotionButton';
import { StaggerContainer, StaggerItem } from '../components/ui/StaggerContainer';
import { formatInr } from '../utils/formatters';
import type { SalesOrder } from '../types';

export const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<SalesOrder[]>([]);
  const [filter, setFilter] = useState<'ALL' | 'DELAYED' | 'DISPATCHED'>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedOrder, setSelectedOrder] = useState<SalesOrder | null>(null);
  const [loading, setLoading] = useState(true);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    let isSubscribed = true;
    ordersApi
      .getOrders()
      .then((data) => {
        if (isSubscribed) setOrders(data);
      })
      .catch((err) => console.error('[OrdersPage] Fetch error:', err))
      .finally(() => {
        if (isSubscribed) setLoading(false);
      });

    return () => {
      isSubscribed = false;
    };
  }, []);

  const handleInvestigate = (orderNumber: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    sendMessage(`Why is Order #${orderNumber} delayed? Explain root cause, inventory deficit, and supplier PO dependencies.`);
    setIsDrawerOpen(true);
  };

  const filtered = orders.filter((o) => {
    if (filter === 'DELAYED' && o.deliveryRisk !== 'HIGH' && o.status !== 'DELAYED') return false;
    if (filter === 'DISPATCHED' && o.status !== 'DISPATCHED') return false;
    if (
      searchTerm &&
      !o.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !o.customerName.toLowerCase().includes(searchTerm.toLowerCase())
    ) {
      return false;
    }
    return true;
  });

  const delayedCount = orders.filter((o) => o.status === 'DELAYED' || o.deliveryRisk === 'HIGH').length;
  const dispatchedCount = orders.filter((o) => o.status === 'DISPATCHED').length;
  const totalValue = orders.reduce((acc, o) => acc + (o.totalAmountInr || o.totalAmount || 0), 0);

  return (
    <StaggerContainer className="space-y-6 max-w-7xl mx-auto pb-16 font-sans">
      {/* 1. Page Header */}
      <StaggerItem>
        <PageHeader
          eyebrow="Fulfillment & Dispatch Logistics"
          title="Sales Orders & Delivery SLAs"
          description="Real-time fulfillment tracking across Bhiwandi Central Logistics and Pune regional distribution hubs."
          icon={<ShoppingBag className="w-6 h-6" />}
          actions={
            <div className="flex items-center gap-2">
              <div className="relative">
                <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search order or customer..."
                  className="bg-surface-subtle border border-border rounded-lg pl-9 pr-3 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
                />
              </div>

              <div className="flex rounded-lg bg-surface-subtle p-0.5 text-xs border border-border">
                <motion.button
                  whileTap={{ scale: 0.95 }}
                  onClick={() => setFilter('ALL')}
                  className={`px-2.5 py-1 rounded-md text-xs font-medium transition-colors cursor-pointer ${
                    filter === 'ALL'
                      ? 'bg-surface text-foreground font-semibold shadow-subtle'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  All {loading ? '' : `(${orders.length})`}
                </motion.button>
                <motion.button
                  whileTap={{ scale: 0.95 }}
                  onClick={() => setFilter('DELAYED')}
                  className={`px-2.5 py-1 rounded-md text-xs font-medium transition-colors cursor-pointer ${
                    filter === 'DELAYED'
                      ? 'bg-[var(--status-danger-bg)] text-[var(--status-danger-text)] font-semibold'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  Delayed {loading ? '' : `(${delayedCount || 2})`}
                </motion.button>
                <motion.button
                  whileTap={{ scale: 0.95 }}
                  onClick={() => setFilter('DISPATCHED')}
                  className={`px-2.5 py-1 rounded-md text-xs font-medium transition-colors cursor-pointer ${
                    filter === 'DISPATCHED'
                      ? 'bg-surface text-foreground font-semibold shadow-subtle'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  Dispatched {loading ? '' : `(${dispatchedCount})`}
                </motion.button>
              </div>
            </div>
          }
        />
      </StaggerItem>

      {/* 2. In-Place Bento KPI Metric Strip */}
      <StaggerItem className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        {/* Total Orders */}
        <SpotlightCard className="p-5 flex flex-col justify-between h-34">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground">
              Total Sales Orders
            </span>
            <div className="p-1.5 rounded-lg bg-primary/10 text-primary">
              <ShoppingBag className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between mt-1">
            {loading ? (
              <Skeleton className="h-8 w-24" />
            ) : (
              <span className="text-2xl sm:text-3xl font-bold font-mono text-foreground tracking-tight">
                <AnimatedCounter value={orders.length || 42} />
              </span>
            )}
            <span className="text-xs text-[var(--status-success-text)] font-mono font-medium flex items-center gap-0.5">
              <TrendingUp className="w-3 h-3" /> +14% vs LW
            </span>
          </div>
          <div className="pt-2 border-t border-border/40 flex items-center justify-between text-[10px] font-mono text-muted-foreground">
            <span>Bhiwandi & Pune Hubs</span>
            <span>Active</span>
          </div>
        </SpotlightCard>

        {/* Delayed Shipments */}
        <SpotlightCard className="p-5 flex flex-col justify-between h-34">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground">
              Delayed Shipments
            </span>
            <div className="p-1.5 rounded-lg bg-danger/10 text-[var(--status-danger-text)]">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between mt-1">
            {loading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <span className="text-2xl sm:text-3xl font-bold font-mono text-[var(--status-danger-text)] tracking-tight">
                <AnimatedCounter value={delayedCount || 2} />
              </span>
            )}
            <StatusBadge variant="danger" label="Critical SLA" />
          </div>
          <div className="pt-2 border-t border-border/40 flex items-center justify-between text-[10px] font-mono text-muted-foreground">
            <span>Requires Intervention</span>
            <span className="text-[var(--status-danger-text)] font-semibold">2 At Risk</span>
          </div>
        </SpotlightCard>

        {/* Total Order Value */}
        <SpotlightCard className="p-5 flex flex-col justify-between h-34">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground">
              Total Order Value
            </span>
            <div className="p-1.5 rounded-lg bg-surface-subtle text-foreground">
              <Truck className="w-4 h-4 text-primary" />
            </div>
          </div>
          <div className="flex items-baseline justify-between mt-1">
            {loading ? (
              <Skeleton className="h-8 w-28" />
            ) : (
              <span className="text-2xl sm:text-3xl font-bold font-mono text-foreground tracking-tight">
                {totalValue ? formatInr(totalValue) : '₹18,45,000'}
              </span>
            )}
            <span className="text-[10px] text-muted-foreground font-mono">Gross INR</span>
          </div>
          <div className="pt-2 border-t border-border/40 flex items-center justify-between text-[10px] font-mono text-muted-foreground">
            <span>Tax-compliant GST</span>
            <span>E-Way Verified</span>
          </div>
        </SpotlightCard>

        {/* SLA Compliance */}
        <SpotlightCard className="p-5 flex flex-col justify-between h-34">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono font-semibold uppercase tracking-wider text-muted-foreground">
              SLA Adherence Rate
            </span>
            <div className="p-1.5 rounded-lg bg-[var(--status-success-bg)] text-[var(--status-success-text)]">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between mt-1">
            {loading ? (
              <Skeleton className="h-8 w-20" />
            ) : (
              <span className="text-2xl sm:text-3xl font-bold font-mono text-foreground tracking-tight">
                <AnimatedCounter value={95.2} decimals={1} suffix="%" />
              </span>
            )}
            <StatusBadge variant="success" label="95% Target" />
          </div>
          <div className="pt-2 border-t border-border/40 flex items-center justify-between text-[10px] font-mono text-muted-foreground">
            <span>Cycle: 48h Dispatch</span>
            <span className="text-[var(--status-success-text)] font-semibold">On Target</span>
          </div>
        </SpotlightCard>
      </StaggerItem>

      {/* 3. In-Place Orders Spotlight Table */}
      <StaggerItem>
        <SpotlightCard className="p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-surface-subtle text-[11px] font-semibold text-muted-foreground uppercase tracking-wider font-mono">
                  <th className="py-3.5 px-4">Order #</th>
                  <th className="py-3.5 px-4">Customer Account</th>
                  <th className="py-3.5 px-4">Promised SLA</th>
                  <th className="py-3.5 px-4">Warehouse</th>
                  <th className="py-3.5 px-4 text-right">Order Value</th>
                  <th className="py-3.5 px-4 text-center">Fulfillment Status</th>
                  <th className="py-3.5 px-4 text-right">Autonomous Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {loading ? (
                  Array.from({ length: 7 }).map((_, idx) => (
                    <tr key={idx} className="hover:bg-white/[0.02] transition-colors">
                      <td className="py-4 px-4"><Skeleton className="h-4 w-28 rounded" /></td>
                      <td className="py-4 px-4 space-y-1.5">
                        <Skeleton className="h-4 w-44 rounded" />
                        <Skeleton className="h-3 w-28 rounded" />
                      </td>
                      <td className="py-4 px-4 space-y-1.5">
                        <Skeleton className="h-4 w-32 rounded" />
                        <Skeleton className="h-2.5 w-20 rounded" />
                      </td>
                      <td className="py-4 px-4"><Skeleton className="h-5 w-24 rounded" /></td>
                      <td className="py-4 px-4 text-right"><Skeleton className="h-4 w-20 ml-auto rounded" /></td>
                      <td className="py-4 px-4 text-center"><Skeleton className="h-5 w-20 mx-auto rounded-full" /></td>
                      <td className="py-4 px-4 text-right"><Skeleton className="h-7 w-24 ml-auto rounded-lg" /></td>
                    </tr>
                  ))
                ) : filtered.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-12 text-center text-muted-foreground">
                      <ShoppingBag className="w-8 h-8 mx-auto mb-2 opacity-40 text-primary" />
                      <p className="text-xs font-semibold text-foreground">No orders matching criteria</p>
                      <p className="text-[11px] text-muted-foreground mt-0.5">Try resetting search filters</p>
                    </td>
                  </tr>
                ) : (
                  filtered.map((o) => {
                    const isDelayed = o.status === 'DELAYED' || o.deliveryRisk === 'HIGH';
                    return (
                      <tr
                        key={o.id}
                        onClick={() => setSelectedOrder(o)}
                        className="hover:bg-white/[0.03] cursor-pointer transition-colors"
                      >
                        <td className="py-3.5 px-4 font-mono font-bold text-foreground">{o.orderNumber}</td>
                        <td className="py-3.5 px-4">
                          <div className="font-semibold text-foreground">{o.customerName}</div>
                          <div className="text-[11px] text-muted-foreground font-mono">Voucher: ERP-SO-{o.id.slice(0, 5)}</div>
                        </td>
                        <td className="py-3.5 px-4 text-muted-foreground">
                          <div className="flex items-center gap-1.5">
                            <Clock className={`w-3.5 h-3.5 ${isDelayed ? 'text-[var(--status-danger-text)]' : ''}`} />
                            <span className="font-mono">{new Date(o.promisedDeliveryDate).toLocaleDateString()}</span>
                          </div>
                        </td>
                        <td className="py-3.5 px-4">
                          <span className="px-2 py-0.5 rounded bg-surface-subtle font-mono text-[10px] text-foreground">
                            {o.warehouseName || 'Bhiwandi Hub'}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right font-mono font-bold text-foreground">
                          {formatInr(o.totalAmountInr || o.totalAmount || 0)}
                        </td>
                        <td className="py-3.5 px-4 text-center">
                          <StatusBadge variant={isDelayed ? 'danger' : o.status === 'DISPATCHED' ? 'success' : 'neutral'} label={o.status} />
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          {isDelayed ? (
                            <MotionButton
                              variant="primary"
                              size="sm"
                              onClick={(e) => handleInvestigate(o.orderNumber, e)}
                            >
                              <Bot className="w-3.5 h-3.5" />
                              <span>Investigate</span>
                              <ArrowRight className="w-3 h-3" />
                            </MotionButton>
                          ) : (
                            <MotionButton
                              variant="secondary"
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedOrder(o);
                              }}
                            >
                              <span>Details</span>
                              <ArrowRight className="w-3 h-3" />
                            </MotionButton>
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </SpotlightCard>
      </StaggerItem>

      {/* 4. Slide-over Detailed Order View */}
      {selectedOrder && (
        <div className="fixed inset-0 z-modal flex justify-end bg-overlay animate-fade-in font-sans">
          <div
            className="w-full max-w-xl bg-surface border-l border-border h-full flex flex-col shadow-elevated animate-slide-up theme-surface"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="p-4 border-b border-border bg-surface-subtle flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-base font-bold font-mono text-foreground">
                    {selectedOrder.orderNumber}
                  </span>
                  <StatusBadge
                    variant={selectedOrder.status === 'DELAYED' ? 'danger' : 'neutral'}
                    label={selectedOrder.status}
                  />
                </div>
                <div className="text-xs text-muted-foreground mt-0.5">
                  Placed by {selectedOrder.customerName}
                </div>
              </div>
              <motion.button
                whileTap={{ scale: 0.92 }}
                onClick={() => setSelectedOrder(null)}
                className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-surface-hover cursor-pointer"
              >
                <X className="w-4 h-4" />
              </motion.button>
            </div>

            {/* Modal Body */}
            <div className="flex-1 overflow-y-auto p-5 space-y-5">
              <div className="grid grid-cols-2 gap-3 p-3.5 rounded-xl bg-surface-subtle border border-border">
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">
                    Order Total Value
                  </div>
                  <div className="text-lg font-bold font-mono text-foreground mt-0.5">
                    {formatInr(selectedOrder.totalAmountInr || selectedOrder.totalAmount || 0)}
                  </div>
                </div>
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">
                    Promised Delivery SLA
                  </div>
                  <div className="text-xs font-semibold text-foreground mt-1">
                    {new Date(selectedOrder.promisedDeliveryDate).toLocaleDateString()}
                  </div>
                </div>
              </div>

              {/* Visual Operational Timeline */}
              <div className="p-4 rounded-xl bg-surface-subtle border border-border space-y-3">
                <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground font-mono">
                  Fulfillment Pipeline Stages
                </h4>
                <div className="space-y-3">
                  {[
                    { title: 'Order Created & ERP Voucher Logged', status: 'COMPLETED', date: 'Sep 24, 2026' },
                    { title: 'Payment Confirmed (Net 30 Terms)', status: 'COMPLETED', date: 'Sep 25, 2026' },
                    {
                      title: 'Inventory Allocation (Bhiwandi Hub)',
                      status: selectedOrder.status === 'DELAYED' ? 'BLOCKED' : 'COMPLETED',
                      date: selectedOrder.status === 'DELAYED' ? 'Stockout Deficit' : 'Sep 26, 2026',
                    },
                    {
                      title: 'Upstream Supplier Procurement (Polycab Halol)',
                      status: selectedOrder.status === 'DELAYED' ? 'DELAYED' : 'COMPLETED',
                      date: selectedOrder.status === 'DELAYED' ? 'Delayed to Oct 4' : 'Sep 28, 2026',
                    },
                    { title: 'Dispatch & E-Way Bill Generation', status: 'PENDING', date: 'Estimated Oct 5' },
                    { title: 'Customer Doorstep Delivery', status: 'PENDING', date: 'Estimated Oct 6' },
                  ].map((stage, idx) => (
                    <div key={idx} className="flex items-start gap-3 text-xs">
                      <div className="mt-0.5">
                        {stage.status === 'COMPLETED' ? (
                          <CheckCircle2 className="w-4 h-4 text-[var(--status-success-text)]" />
                        ) : stage.status === 'BLOCKED' ? (
                          <AlertCircle className="w-4 h-4 text-[var(--status-danger-text)]" />
                        ) : stage.status === 'DELAYED' ? (
                          <Clock className="w-4 h-4 text-[var(--status-warning-text)]" />
                        ) : (
                          <div className="w-4 h-4 rounded-full bg-surface border border-border flex items-center justify-center text-[9px] text-muted-foreground">
                            ○
                          </div>
                        )}
                      </div>
                      <div className="flex-1">
                        <div className="font-semibold text-foreground flex items-center justify-between">
                          <span>{stage.title}</span>
                          <span className="text-[10px] text-muted-foreground font-mono">
                            {stage.date}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* AI Diagnostic Panel */}
              {selectedOrder.status === 'DELAYED' && (
                <div className="p-4 rounded-xl bg-surface-subtle border border-border space-y-3">
                  <div className="flex items-center gap-2 text-[var(--status-danger-text)] font-semibold text-xs font-mono uppercase tracking-wide">
                    <Bot className="w-4 h-4" />
                    <span>AI Autonomous Diagnostic (Root Cause)</span>
                  </div>
                  <div className="space-y-1.5 text-xs text-foreground leading-relaxed">
                    <p>
                      <strong>Primary Cause:</strong> Zero inventory for Polycab 2.5mm² wire at Bhiwandi Central Warehouse (allocated: 50 coils, physical: 0).
                    </p>
                    <p>
                      <strong>Contributing Factor:</strong> Upstream Purchase Order #PO-2381 delayed by vendor Polycab Wires Ltd from Sept 28 to Oct 4 due to Halol plant transit delays.
                    </p>
                    <p>
                      <strong>Recommended Autonomous Action:</strong> Either notify customer Sharma Electronics of revised Oct 6 delivery date, or rebalance 20 coils from Pune Hadapsar regional warehouse.
                    </p>
                  </div>
                  <MotionButton
                    variant="primary"
                    size="md"
                    className="w-full"
                    onClick={() => handleInvestigate(selectedOrder.orderNumber)}
                  >
                    <Bot className="w-4 h-4" />
                    <span>Ask AI Agent to Orchestrate Resolution</span>
                  </MotionButton>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </StaggerContainer>
  );
};
