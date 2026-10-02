import React, { useState } from 'react';
import { CreditCard, Check, Download, Receipt, Zap, ShieldCheck } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';

export const BillingPage: React.FC = () => {
  const { organization } = useAuth();
  const [selectedTier, setSelectedTier] = useState('BUSINESS');

  const tiers = [
    {
      id: 'GROWTH',
      name: 'GROWTH',
      price: '₹14,999',
      period: '/ month',
      description: 'For growing Indian SME distributors managing up to 500 SKUs.',
      features: ['Up to 5 Users', '1,000 AI Agent Runs / mo', 'Tally Prime Sync', 'Basic Stockout Alerts', 'Standard Email Support'],
      current: false,
    },
    {
      id: 'BUSINESS',
      name: 'BUSINESS',
      price: '₹34,999',
      period: '/ month',
      description: 'Ideal for mid-market supply chain operations with multiple logistics hubs.',
      features: [
        'Unlimited Users',
        '10,000 AI Agent Runs / mo',
        'Human-in-the-Loop Approval System',
        'Tally, Zoho & WhatsApp APIs',
        'GST & OCR Document Vault',
        'Multi-Warehouse Logistics Routing',
      ],
      current: true,
    },
    {
      id: 'ENTERPRISE',
      name: 'ENTERPRISE',
      price: 'Custom',
      period: '',
      description: 'Custom SLA, dedicated on-premise connectors, and private LLM deployments.',
      features: [
        'Dedicated Cloud / On-Premise VPC',
        'Custom ERP Adaptors (SAP / Oracle)',
        'Unlimited AI Runs & OCR',
        'Dedicated Operations Engineer',
        'Custom Indian Voice Fine-Tuning',
      ],
      current: false,
    },
  ];

  const invoices = [
    { id: 'INV-2026-09', date: '01 Sep 2026', amount: '₹41,299', gst: '₹6,300 (18% IGST)', status: 'Paid', receipt: 'REC-9012' },
    { id: 'INV-2026-08', date: '01 Aug 2026', amount: '₹41,299', gst: '₹6,300 (18% IGST)', status: 'Paid', receipt: 'REC-8841' },
    { id: 'INV-2026-07', date: '01 Jul 2026', amount: '₹41,299', gst: '₹6,300 (18% IGST)', status: 'Paid', receipt: 'REC-7492' },
  ];

  const handleDownloadInvoice = (invId: string) => {
    const blob = new Blob([`Tax Invoice ${invId}\nBilled to: Sharma Electricals Pvt. Ltd.\nGSTIN: 27AABCS1429B1Z2\nAmount: ₹41,299 (incl. 18% GST)\nStatus: PAID`], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Tax-Invoice-${invId}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <PageTransition>
      <div className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
        <PageHeader
          eyebrow="SaaS Subscription & Quotas"
          title="Enterprise Plan & Usage Telemetry"
          description="Manage your company tier, monitor real-time AI autonomous run quota, and access GST-compliant tax invoices."
          actions={
            <span className="text-xs font-mono px-3 py-1.5 rounded-lg bg-surface border border-border text-foreground">
              Billing Cycle: 1st of every month
            </span>
          }
        />

        {/* Quota Telemetry Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="p-5 rounded-xl bg-surface border border-border shadow-subtle space-y-2">
            <div className="flex items-center justify-between text-xs text-muted-foreground">
              <span className="font-semibold text-foreground flex items-center gap-1.5">
                <Zap className="w-3.5 h-3.5 text-primary" />
                AI Agent Runs
              </span>
              <span className="font-mono">64%</span>
            </div>
            <div className="text-xl font-bold text-foreground font-mono">6,420 / 10,000</div>
            <div className="w-full bg-surface-subtle h-2 rounded-full overflow-hidden border border-border">
              <div className="bg-primary h-full rounded-full transition-all" style={{ width: '64%' }} />
            </div>
            <p className="text-[11px] text-muted-foreground">Resets in 28 days</p>
          </div>

          <div className="p-5 rounded-xl bg-surface border border-border shadow-subtle space-y-2">
            <div className="flex items-center justify-between text-xs text-muted-foreground">
              <span className="font-semibold text-foreground flex items-center gap-1.5">
                <Receipt className="w-3.5 h-3.5 text-emerald-500" />
                Document OCR Pages
              </span>
              <span className="font-mono">38%</span>
            </div>
            <div className="text-xl font-bold text-foreground font-mono">384 / 1,000</div>
            <div className="w-full bg-surface-subtle h-2 rounded-full overflow-hidden border border-border">
              <div className="bg-emerald-500 h-full rounded-full transition-all" style={{ width: '38%' }} />
            </div>
            <p className="text-[11px] text-muted-foreground">Includes PDF & physical invoice scans</p>
          </div>

          <div className="p-5 rounded-xl bg-surface border border-border shadow-subtle space-y-2">
            <div className="flex items-center justify-between text-xs text-muted-foreground">
              <span className="font-semibold text-foreground flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5 text-amber-500" />
                WhatsApp Alert Quota
              </span>
              <span className="font-mono">36%</span>
            </div>
            <div className="text-xl font-bold text-foreground font-mono">1,840 / 5,000</div>
            <div className="w-full bg-surface-subtle h-2 rounded-full overflow-hidden border border-border">
              <div className="bg-amber-500 h-full rounded-full transition-all" style={{ width: '36%' }} />
            </div>
            <p className="text-[11px] text-muted-foreground">Automated customer & supplier dispatch notices</p>
          </div>
        </div>

        {/* Pricing Tiers */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {tiers.map((tier) => (
            <div
              key={tier.name}
              className={`p-6 rounded-xl flex flex-col justify-between transition-all ${
                tier.id === selectedTier
                  ? 'bg-surface border-2 border-primary shadow-subtle relative'
                  : 'bg-surface border border-border shadow-subtle'
              }`}
            >
              {tier.id === selectedTier && (
                <div className="absolute -top-3 right-6">
                  <StatusBadge variant="primary" label="Active Tier" />
                </div>
              )}

              <div>
                <div className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-1 font-mono">
                  {tier.name}
                </div>
                <div className="flex items-baseline gap-1 mb-2">
                  <span className="text-3xl font-extrabold text-foreground">{tier.price}</span>
                  <span className="text-xs text-muted-foreground">{tier.period}</span>
                </div>
                <p className="text-xs text-muted-foreground mb-6 leading-relaxed">{tier.description}</p>

                <div className="space-y-2.5 text-xs text-foreground">
                  {tier.features.map((f, idx) => (
                    <div key={idx} className="flex items-center gap-2">
                      <Check className="w-4 h-4 text-primary shrink-0" />
                      <span>{f}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="mt-8 pt-4 border-t border-border">
                <button
                  onClick={() => setSelectedTier(tier.id)}
                  disabled={tier.id === selectedTier}
                  className={`w-full py-2.5 rounded-xl text-xs font-bold transition-all ${
                    tier.id === selectedTier
                      ? 'bg-surface-subtle text-muted-foreground border border-border cursor-default'
                      : 'bg-primary hover:bg-primary-hover text-primary-foreground shadow-sm'
                  }`}
                >
                  {tier.id === selectedTier ? 'Current Enterprise Plan' : 'Select Plan'}
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Invoices Table */}
        <div className="rounded-xl bg-surface border border-border shadow-subtle overflow-hidden">
          <div className="p-4 border-b border-border bg-surface-subtle font-bold text-xs text-foreground flex items-center justify-between">
            <span className="flex items-center gap-2">
              <Receipt className="w-4 h-4 text-primary" />
              GST Tax Invoices & Payment Receipts
            </span>
            <span className="text-[11px] font-mono text-muted-foreground">GSTIN: 27AABCS1429B1Z2</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-foreground">
              <thead className="bg-surface-subtle text-[11px] uppercase tracking-wider text-muted-foreground border-b border-border font-semibold">
                <tr>
                  <th className="py-3 px-4">Invoice #</th>
                  <th className="py-3 px-4">Date</th>
                  <th className="py-3 px-4">Gross Amount</th>
                  <th className="py-3 px-4">GST Component</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Download</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border font-medium">
                {invoices.map((inv) => (
                  <tr key={inv.id} className="hover:bg-surface-hover transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-foreground">{inv.id}</td>
                    <td className="py-3 px-4 text-muted-foreground">{inv.date}</td>
                    <td className="py-3 px-4 font-bold text-foreground font-mono">{inv.amount}</td>
                    <td className="py-3 px-4 text-muted-foreground text-[11px]">{inv.gst}</td>
                    <td className="py-3 px-4">
                      <StatusBadge variant="success" label={inv.status} />
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => handleDownloadInvoice(inv.id)}
                        className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-surface hover:bg-surface-hover border border-border text-foreground text-xs font-medium transition-colors"
                      >
                        <Download className="w-3.5 h-3.5 text-muted-foreground" />
                        <span>PDF</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </PageTransition>
  );
};
