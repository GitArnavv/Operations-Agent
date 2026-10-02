import React, { useEffect, useState } from 'react';
import {
  Package,
  AlertTriangle,
  Bot,
  Search,
  CheckCircle2,
  TrendingDown,
  Clock,
  Layers,
  X,
  Truck,
  ArrowRight,
  ShieldAlert,
} from 'lucide-react';
import { inventoryApi } from '../api/services';
import { useAgent } from '../contexts/AgentContext';
import { InteractiveChart } from '../components/ui/InteractiveChart';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import type { InventoryItem } from '../types';

export const InventoryPage: React.FC = () => {
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [criticalOnly, setCriticalOnly] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<InventoryItem | null>(null);
  const [loading, setLoading] = useState(true);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    inventoryApi.getInventory().then((data) => {
      setItems(data);
      setLoading(false);
    });
  }, []);

  const handleAskAI = (prodName: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    sendMessage(`How much inventory do we have for ${prodName}? Show days of supply, daily burn rate, and replenishment supplier options.`);
    setIsDrawerOpen(true);
  };

  const filtered = items.filter((i) => {
    if (criticalOnly && i.stockoutRisk !== 'HIGH' && i.stockoutRisk !== 'CRITICAL') return false;
    if (
      searchTerm &&
      !i.productName.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !i.sku.toLowerCase().includes(searchTerm.toLowerCase())
    ) {
      return false;
    }
    return true;
  });

  const demandForecastData = [
    { label: 'Wk -3', value: 28, secondaryValue: 25 },
    { label: 'Wk -2', value: 34, secondaryValue: 30 },
    { label: 'Wk -1', value: 38, secondaryValue: 35 },
    { label: 'Current', value: 42, secondaryValue: 40 },
    { label: 'Wk +1 (Proj)', value: 45, secondaryValue: 44 },
    { label: 'Wk +2 (Proj)', value: 49, secondaryValue: 48 },
  ];

  return (
    <PageTransition className="space-y-6 max-w-7xl mx-auto pb-16 font-sans">
      {/* Top Header */}
      <PageHeader
        eyebrow="Stockout Risk & Reorder Optimization"
        title="Inventory Intelligence & Stock Forecasting"
        description="Real-time SKU balances, burn rates, and Days of Cover (DoC) across Bhiwandi and Pune distribution centers."
        icon={<Package className="w-6 h-6" />}
        actions={
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search product or SKU..."
                className="bg-surface-subtle border border-border rounded-md pl-9 pr-3 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
              />
            </div>

            <button
              onClick={() => setCriticalOnly(!criticalOnly)}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors flex items-center gap-1.5 ${
                criticalOnly
                  ? 'bg-[var(--status-danger-bg)] text-[var(--status-danger-text)] font-semibold'
                  : 'bg-surface-subtle hover:bg-surface-hover text-muted-foreground hover:text-foreground'
              }`}
            >
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Low Stock (&lt; 7 Days)</span>
            </button>
          </div>
        }
      />

      {/* Inventory Table */}
      <div className="rounded-xl border border-border bg-surface overflow-hidden theme-surface">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border bg-surface-subtle text-[11px] font-semibold text-muted-foreground uppercase tracking-wider font-mono">
                <th className="py-3 px-4">SKU / Code</th>
                <th className="py-3 px-4">Product Name & Category</th>
                <th className="py-3 px-4">Warehouse Location</th>
                <th className="py-3 px-4 text-right">Physical / Available</th>
                <th className="py-3 px-4 text-right">Daily Demand</th>
                <th className="py-3 px-4 text-right">Days of Cover (DoC)</th>
                <th className="py-3 px-4 text-center">Stockout Risk</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {filtered.map((item) => {
                const isCritical =
                  (item.availableQuantity ?? item.availableStock ?? 0) <= 15 ||
                  (item.daysOfInventoryRemaining ?? item.daysOfStockRemaining ?? 0) < 7 ||
                  (item.availableQuantity ?? item.availableStock ?? 0) === 0;

                return (
                  <tr
                    key={item.id}
                    onClick={() => setSelectedProduct(item)}
                    className="hover:bg-surface-hover cursor-pointer transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-bold text-foreground">
                      {item.sku}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-foreground">{item.productName}</div>
                      <div className="text-[11px] text-muted-foreground">Industrial Electricals</div>
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground">
                      <span className="px-2 py-0.5 rounded bg-surface-subtle font-mono text-[10px] text-foreground">
                        {item.warehouseName}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono">
                      <span className="font-bold text-foreground">
                        {item.availableQuantity ?? item.availableStock ?? 0} {item.unitOfMeasure || 'coils'}
                      </span>
                      <span className="text-muted-foreground block text-[10px]">
                        (Res: {item.reservedQuantity ?? item.reservedStock ?? 0})
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono text-muted-foreground">
                      {item.dailyDemandRate || 3.5} / day
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono font-bold">
                      <span
                        className={
                          isCritical
                            ? 'text-[var(--status-danger-text)]'
                            : 'text-foreground'
                        }
                      >
                        {(item.daysOfInventoryRemaining ?? item.daysOfStockRemaining ?? 0)} days
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <StatusBadge
                        variant={isCritical ? 'danger' : 'success'}
                        label={item.stockoutRisk || (isCritical ? 'CRITICAL' : 'HEALTHY')}
                      />
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedProduct(item);
                        }}
                        className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-surface-subtle hover:bg-surface-hover text-foreground text-xs font-medium transition-colors"
                      >
                        <span>Forecast &rarr;</span>
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Slide-over Detailed Product Inventory & Demand Projection Drawer */}
      {selectedProduct && (
        <div className="fixed inset-0 z-modal flex justify-end bg-overlay animate-fade-in font-sans">
          <div
            className="w-full max-w-xl bg-surface border-l border-border h-full flex flex-col shadow-elevated animate-slide-up theme-surface"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div className="p-4 border-b border-border bg-surface-subtle flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-base font-bold text-foreground">
                    {selectedProduct.productName}
                  </h2>
                  <span className="text-xs font-mono text-muted-foreground bg-surface-subtle px-2 py-0.5 rounded">
                    {selectedProduct.sku}
                  </span>
                </div>
                <div className="text-xs text-muted-foreground mt-0.5">
                  Stored at {selectedProduct.warehouseName}
                </div>
              </div>
              <button
                onClick={() => setSelectedProduct(null)}
                className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-surface-hover"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-y-auto p-5 space-y-5">
              {/* Core Stock Inventory Metrics */}
              <div className="grid grid-cols-3 gap-2.5 text-xs">
                <div className="p-3 rounded-lg bg-surface-subtle">
                  <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">Available</div>
                  <div className="text-lg font-bold font-mono text-foreground mt-0.5">
                    {selectedProduct.availableQuantity ?? selectedProduct.availableStock ?? 0}
                  </div>
                  <div className="text-[10px] text-muted-foreground">Immediate Dispatch</div>
                </div>
                <div className="p-3 rounded-lg bg-surface-subtle">
                  <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">Reserved</div>
                  <div className="text-lg font-bold font-mono text-foreground mt-0.5">
                    {selectedProduct.reservedQuantity ?? selectedProduct.reservedStock ?? 0}
                  </div>
                  <div className="text-[10px] text-muted-foreground">Allocated to Orders</div>
                </div>
                <div className="p-3 rounded-lg bg-surface-subtle">
                  <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">Days of Cover</div>
                  <div className="text-lg font-bold font-mono text-[var(--status-danger-text)] mt-0.5">
                    {(selectedProduct.daysOfInventoryRemaining ?? selectedProduct.daysOfStockRemaining ?? 3.4)}d
                  </div>
                  <div className="text-[10px] text-muted-foreground">Burn Rate: 3.5/day</div>
                </div>
              </div>

              {/* Operational Thresholds & Lead Times */}
              <div className="p-3.5 rounded-lg bg-surface-subtle space-y-2 text-xs">
                <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground font-mono">
                  Inventory Buffer Parameters
                </h4>
                <div className="grid grid-cols-2 gap-3 text-muted-foreground pt-1">
                  <div>
                    <span>Reorder Point: </span>
                    <strong className="text-foreground font-mono">
                      {selectedProduct.reorderPoint || 30} units
                    </strong>
                  </div>
                  <div>
                    <span>Safety Buffer: </span>
                    <strong className="text-foreground font-mono">15 units</strong>
                  </div>
                  <div>
                    <span>Supplier Lead Time: </span>
                    <strong className="text-foreground font-mono">7 days (Halol Plant)</strong>
                  </div>
                  <div>
                    <span>Stockout Probability: </span>
                    <strong className="text-[var(--status-danger-text)] font-mono">
                      {(selectedProduct.availableQuantity ?? selectedProduct.availableStock ?? 0) === 0 ? '100% (Stockout)' : '87% Risk'}
                    </strong>
                  </div>
                </div>
              </div>

              {/* Interactive Demand Projection Chart */}
              <InteractiveChart
                title="Historical Demand vs Projected 14-Day Trajectory"
                subtitle="Historical weekly run-rate and calibrated forecast"
                data={demandForecastData}
                type="line"
                valueSuffix=" Coils"
                secondaryLabel="Safety Stock Threshold"
              />

              {/* Autonomous AI Reorder Trigger Button */}
              <div className="p-4 rounded-xl bg-surface-subtle space-y-2">
                <div className="flex items-center gap-2 text-primary font-semibold text-xs font-mono uppercase tracking-wide">
                  <Bot className="w-4 h-4" />
                  <span>Autonomous Replenishment Recommendation</span>
                </div>
                <p className="text-xs text-foreground leading-relaxed">
                  Generate Requisition PO for 50 coils from Polycab Wires Ltd to replenish Bhiwandi inventory buffer before safety stock exhaustion.
                </p>
                <button
                  onClick={() => handleAskAI(selectedProduct.productName)}
                  className="w-full py-2.5 rounded-lg bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-sm transition-colors flex items-center justify-center gap-2"
                >
                  <Bot className="w-4 h-4" />
                  <span>Draft Replenishment Action with AI</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </PageTransition>
  );
};
