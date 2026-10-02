import React from 'react';
import {
  Clock,
  Package,
  IndianRupee,
  Truck,
  Bot,
  ShieldCheck,
} from 'lucide-react';
import { InteractiveChart } from '../components/ui/InteractiveChart';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { MetricCard } from '../components/ui/MetricCard';
import { useAgent } from '../contexts/AgentContext';

export const AnalyticsPage: React.FC = () => {
  const { sendMessage, setIsDrawerOpen } = useAgent();

  const handleAskAI = (prompt: string) => {
    sendMessage(prompt);
    setIsDrawerOpen(true);
  };

  const revenueData = [
    { label: 'May', value: 88, secondaryValue: 80 },
    { label: 'Jun', value: 95, secondaryValue: 85 },
    { label: 'Jul', value: 102, secondaryValue: 92 },
    { label: 'Aug', value: 110, secondaryValue: 98 },
    { label: 'Sep', value: 118, secondaryValue: 104 },
    { label: 'Oct (Proj)', value: 124, secondaryValue: 110 },
  ];

  const slaAdherenceData = [
    { label: 'Wk 1', value: 92, secondaryValue: 95 },
    { label: 'Wk 2', value: 94, secondaryValue: 95 },
    { label: 'Wk 3', value: 89, secondaryValue: 95 },
    { label: 'Wk 4', value: 84, secondaryValue: 95 },
    { label: 'Current', value: 91, secondaryValue: 95 },
  ];

  const receivablesAgingData = [
    { label: 'Current (0-30d)', value: 42.5 },
    { label: '31-60 Days', value: 14.8 },
    { label: '61-90 Days', value: 4.8 },
    { label: '>90 Days Overdue', value: 1.2 },
  ];

  return (
    <PageTransition className="space-y-6 max-w-7xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Business Intelligence & Telemetry"
        title="Operational Analytics & Predictive Intelligence"
        description="Order cycle times, supplier lead-time distributions, and working capital inventory turns."
        actions={
          <button
            onClick={() =>
              handleAskAI(
                'Provide an executive summary of current operational trends, revenue trajectory, and key supply chain bottlenecks.'
              )
            }
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-subtle transition-all"
          >
            <Bot className="w-4 h-4" />
            <span>Executive AI Briefing</span>
          </button>
        }
      />

      {/* Top Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          label="Fulfillment SLA Rate"
          value="91.4%"
          trend={{ text: '+2.1% vs last month', isPositive: true }}
          context="95% SLA target commitment"
          icon={<Clock className="w-4 h-4" />}
        />
        <MetricCard
          label="Inventory Turns / Year"
          value="8.4x"
          context="Fastest: Cables & 32A MCBs"
          icon={<Package className="w-4 h-4" />}
        />
        <MetricCard
          label="Average Payment Cycle (DSO)"
          value="32 Days"
          trend={{ text: '-9 days', isPositive: true }}
          context="Reduced via autonomous reminders"
          icon={<IndianRupee className="w-4 h-4" />}
        />
        <MetricCard
          label="Supplier On-Time Index"
          value="84.2%"
          trend={{ text: 'Transit delay impact', isNegative: true }}
          context="Polycab Halol corridor delayed"
          icon={<Truck className="w-4 h-4" />}
        />
      </div>

      {/* Interactive Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <InteractiveChart
          title="Billed Monthly Revenue Run-Rate"
          subtitle="Realized B2B turnover vs budget target (₹ Lakhs)"
          data={revenueData}
          type="area"
          valuePrefix="₹"
          valueSuffix="L"
          secondaryLabel="Budget Target"
        />

        <InteractiveChart
          title="Delivery SLA Adherence Trend"
          subtitle="On-time deliveries percentage vs 95% target SLA"
          data={slaAdherenceData}
          type="line"
          valueSuffix="%"
          colorVar="var(--chart-2)"
          secondaryLabel="95% Target SLA"
        />
      </div>

      {/* Receivables Aging Bar Chart & Warehouse Capacity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <InteractiveChart
            title="Accounts Receivable Aging Distribution"
            subtitle="Outstanding dealer exposure bucketed by aging (₹ Lakhs)"
            data={receivablesAgingData}
            type="bar"
            valuePrefix="₹"
            valueSuffix="L"
            colorVar="var(--chart-4)"
          />
        </div>

        {/* Warehouse Capacity Card */}
        <div className="p-6 rounded-xl border border-border bg-surface shadow-subtle space-y-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                Warehouse Capacity Utilization
              </h3>
              <ShieldCheck className="w-4 h-4 text-primary" />
            </div>

            <div className="mt-5 space-y-4">
              <div>
                <div className="flex items-center justify-between text-xs text-foreground mb-1.5">
                  <span className="font-semibold">Bhiwandi Central Logistics Hub</span>
                  <span className="font-mono font-bold text-primary">78%</span>
                </div>
                <div className="w-full h-2 rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full bg-primary rounded-full transition-all duration-500" style={{ width: '78%' }}></div>
                </div>
                <div className="text-[10px] text-muted-foreground mt-1">78,000 / 100,000 units capacity</div>
              </div>

              <div>
                <div className="flex items-center justify-between text-xs text-foreground mb-1.5">
                  <span className="font-semibold">Pune Chakan Fulfillment Center</span>
                  <span className="font-mono font-bold text-emerald-500">54%</span>
                </div>
                <div className="w-full h-2 rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full bg-emerald-500 rounded-full transition-all duration-500" style={{ width: '54%' }}></div>
                </div>
                <div className="text-[10px] text-muted-foreground mt-1">32,400 / 60,000 units capacity</div>
              </div>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-surface-subtle text-xs text-muted-foreground">
            Autonomous stock rebalancing recommended from Pune to Bhiwandi for Fast-Moving MCBs.
          </div>
        </div>
      </div>
    </PageTransition>
  );
};
