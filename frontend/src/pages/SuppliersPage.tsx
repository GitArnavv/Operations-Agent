import React, { useEffect, useState } from 'react';
import {
  Truck,
  Star,
  Bot,
  Search,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Building2,
  ArrowRight,
  TrendingDown,
  TrendingUp,
} from 'lucide-react';
import { suppliersApi } from '../api/services';
import { useAgent } from '../contexts/AgentContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { TableSkeleton } from '../components/ui/Skeleton';
import { formatPercent, formatDays } from '../utils/formatters';
import type { Supplier } from '../types';

export const SuppliersPage: React.FC = () => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    suppliersApi
      .getSuppliers()
      .then((data) => {
        setSuppliers(data);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleBenchmark = () => {
    sendMessage('Which supplier has the worst on-time delivery rate? Compare Polycab, Havells, and Schneider Electric.');
    setIsDrawerOpen(true);
  };

  const handleInspectSupplier = (sup: Supplier) => {
    sendMessage(`Show me the vendor scorecard and open purchase orders for ${sup.name}`);
    setIsDrawerOpen(true);
  };

  const filtered = suppliers.filter(
    (s) =>
      s.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.city.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <PageTransition className="space-y-6 max-w-7xl mx-auto pb-16 font-sans">
      {/* Top Header */}
      <PageHeader
        eyebrow="Procurement & Lead Time Optimization"
        title="Supplier Intelligence & Vendor SLAs"
        description="50+ verified OEM manufacturers, distributors, and delivery lead-time scorecards across Indian industrial hubs."
        icon={<Truck className="w-6 h-6" />}
        actions={
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search vendor or city..."
                className="bg-surface-subtle border border-border rounded-md pl-9 pr-3 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
              />
            </div>

            <button
              onClick={handleBenchmark}
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-md bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-sm transition-colors"
            >
              <Bot className="w-3.5 h-3.5" />
              <span>Benchmark Vendors</span>
            </button>
          </div>
        }
      />

      {/* Comparative Vendor Intelligence Card */}
      <div className="p-5 rounded-xl border border-border bg-surface theme-surface space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Bot className="w-4 h-4 text-primary" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-muted-foreground font-mono">
              Autonomous Vendor Comparison (Top 3 OEMs)
            </h3>
          </div>
          <span className="text-[11px] text-muted-foreground font-mono">Trailing 90 Days PO Audit</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
          <div className="p-3.5 rounded-lg bg-surface-subtle space-y-1.5">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-foreground">Polycab Wires Ltd</span>
              <StatusBadge variant="danger" label="84% OTIF" />
            </div>
            <div className="text-[11px] text-muted-foreground font-mono">
              Avg Lead: 7.0 days &bull; 6 late dispatches
            </div>
            <p className="text-[11px] text-foreground leading-relaxed pt-1">
              &ldquo;Halol factory logistics bottlenecks. High dispatch variance during peak construction quarters.&rdquo;
            </p>
          </div>

          <div className="p-3.5 rounded-lg bg-surface-subtle space-y-1.5">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-foreground">Havells India Ltd</span>
              <StatusBadge variant="success" label="94% OTIF" />
            </div>
            <div className="text-[11px] text-muted-foreground font-mono">
              Avg Lead: 4.5 days &bull; 1 late dispatch
            </div>
            <p className="text-[11px] text-foreground leading-relaxed pt-1">
              &ldquo;Excellent delivery SLA compliance via Pune Hadapsar depot. 4% higher unit premium.&rdquo;
            </p>
          </div>

          <div className="p-3.5 rounded-lg bg-surface-subtle space-y-1.5">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-foreground">Schneider Electric</span>
              <StatusBadge variant="info" label="91% OTIF" />
            </div>
            <div className="text-[11px] text-muted-foreground font-mono">
              Avg Lead: 5.2 days &bull; 2 late dispatches
            </div>
            <p className="text-[11px] text-foreground leading-relaxed pt-1">
              &ldquo;Consistent heavy switchgear delivery. Extended 60-day credit terms provided.&rdquo;
            </p>
          </div>
        </div>
      </div>

      {/* Suppliers Table or Skeleton */}
      {loading ? (
        <TableSkeleton rows={6} columns={8} />
      ) : (
        <div className="rounded-xl border border-border bg-surface overflow-hidden theme-surface">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-surface-subtle text-[11px] font-semibold text-muted-foreground uppercase tracking-wider font-mono">
                  <th className="py-3 px-4">Supplier / Vendor</th>
                  <th className="py-3 px-4">Primary Category</th>
                  <th className="py-3 px-4">Location & State</th>
                  <th className="py-3 px-4 text-right">Avg Lead Time</th>
                  <th className="py-3 px-4 text-right">On-Time Delivery %</th>
                  <th className="py-3 px-4 text-right">Defect Rate</th>
                  <th className="py-3 px-4 text-center">Reliability</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {filtered.map((s) => {
                  const onTimeRate = Number(s.onTimeDeliveryRate ?? 0);
                  const isUnderperforming = onTimeRate < 90;
                  return (
                    <tr
                      key={s.id}
                      onClick={() => handleInspectSupplier(s)}
                      className="hover:bg-surface-hover cursor-pointer transition-colors"
                    >
                      <td className="py-3.5 px-4">
                        <div className="font-semibold text-foreground">{s.name}</div>
                        <div className="text-[10px] text-muted-foreground font-mono">
                          GSTIN: {s.gstin}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-muted-foreground">
                        {s.category || 'Electrical Equipment'}
                      </td>
                      <td className="py-3.5 px-4 text-muted-foreground">
                        <div className="flex items-center gap-1.5">
                          <Building2 className="w-3.5 h-3.5 text-muted-foreground" />
                          <span>
                            {s.city}, {s.state}
                          </span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-right font-mono font-medium text-foreground">
                        {formatDays(s.leadTimeDays || s.averageLeadTimeDays || 5, 1)}
                      </td>
                      <td className="py-3.5 px-4 text-right font-mono font-bold">
                        <span
                          className={
                            isUnderperforming
                              ? 'text-[var(--status-danger-text)]'
                              : 'text-[var(--status-success-text)]'
                          }
                        >
                          {formatPercent(s.onTimeDeliveryRate, 2)}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-right font-mono text-muted-foreground">
                        {formatPercent(s.defectRate, 2)}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <StatusBadge
                          variant={
                            (s.reliabilityScore ?? 0) >= 90
                              ? 'success'
                              : (s.reliabilityScore ?? 0) >= 80
                              ? 'warning'
                              : 'danger'
                          }
                          label={`${formatPercent(s.reliabilityScore, 0, { suffix: '' })}/100`}
                        />
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleInspectSupplier(s);
                          }}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-surface-subtle hover:bg-surface-hover text-foreground text-xs font-medium transition-colors"
                        >
                          <Bot className="w-3.5 h-3.5 text-primary" />
                          <span>Audit &rarr;</span>
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </PageTransition>
  );
};
