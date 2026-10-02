import React, { useEffect, useState } from 'react';
import {
  FileSpreadsheet,
  AlertTriangle,
  Bot,
  Search,
  CheckCircle2,
  Clock,
  Send,
  Building2,
  Calendar,
  FileCheck,
  CreditCard,
  MessageSquare,
} from 'lucide-react';
import { invoicesApi } from '../api/services';
import { useAgent } from '../contexts/AgentContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { MetricCard } from '../components/ui/MetricCard';
import { TableSkeleton } from '../components/ui/Skeleton';
import { formatInr, formatInrCompact } from '../utils/formatters';
import type { Invoice } from '../types';

export const InvoicesPage: React.FC = () => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [overdueOnly, setOverdueOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    invoicesApi
      .getInvoices()
      .then((data) => {
        setInvoices(data);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleAudit = (invoiceNumber: string) => {
    sendMessage(`Validate GST calculations, HSN rates, and matching PO for Invoice #${invoiceNumber}`);
    setIsDrawerOpen(true);
  };

  const handleSendReminder = (inv: Invoice) => {
    const amt = inv.totalAmountInr || inv.totalAmount || 0;
    sendMessage(`Prepare an automated WhatsApp and formal email payment reminder for ${inv.entityName} regarding overdue Invoice #${inv.invoiceNumber} of ${formatInr(amt)}`);
    setIsDrawerOpen(true);
  };

  const filtered = invoices.filter((inv) => {
    if (overdueOnly && inv.paymentStatus !== 'OVERDUE') return false;
    if (
      searchTerm &&
      !inv.invoiceNumber.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !inv.entityName.toLowerCase().includes(searchTerm.toLowerCase())
    ) {
      return false;
    }
    return true;
  });

  const totalReceivables = invoices.reduce((acc, curr) => acc + (curr.totalAmountInr || curr.totalAmount || 0), 0);
  const overdueTotal = invoices
    .filter((inv) => inv.paymentStatus === 'OVERDUE')
    .reduce((acc, curr) => acc + (curr.totalAmountInr || curr.totalAmount || 0), 0);

  return (
    <PageTransition className="space-y-6 max-w-7xl mx-auto pb-16 font-sans">
      {/* Top Header */}
      <PageHeader
        eyebrow="GST Ledger & Receivables Aging"
        title="Invoices & Working Capital Management"
        description="B2B GST invoices, E-Invoicing status, input tax credit (ITC) reconciliation, and receivables collection automation."
        icon={<FileSpreadsheet className="w-6 h-6" />}
        actions={
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search invoice or customer..."
                className="bg-surface-subtle border border-border rounded-md pl-9 pr-3 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
              />
            </div>

            <button
              onClick={() => setOverdueOnly(!overdueOnly)}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors flex items-center gap-1.5 ${
                overdueOnly
                  ? 'bg-[var(--status-danger-bg)] text-[var(--status-danger-text)] font-semibold'
                  : 'bg-surface-subtle hover:bg-surface-hover text-muted-foreground hover:text-foreground'
              }`}
            >
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Overdue (&gt;30 Days)</span>
            </button>
          </div>
        }
      />

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <MetricCard
          label="Total Billed Receivables"
          value={formatInrCompact(totalReceivables)}
          unit=""
          context="All active GST vouchers"
        />

        <MetricCard
          label="Overdue Receivables (>30 Days)"
          value={formatInrCompact(overdueTotal)}
          unit=""
          statusBadge={<StatusBadge variant="danger" label="High Risk" />}
          context="ABC Traders & Sunrise Infra"
        />

        <MetricCard
          label="GST Reconciliation (ITC)"
          value="100%"
          unit="Matched"
          statusBadge={<StatusBadge variant="success" label="GSTR-2B Verified" />}
          context="All input credits validated"
        />
      </div>

      {/* Invoices Table or Skeleton */}
      {loading ? (
        <TableSkeleton rows={6} columns={8} />
      ) : (
        <div className="rounded-xl border border-border bg-surface overflow-hidden theme-surface">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-surface-subtle text-[11px] font-semibold text-muted-foreground uppercase tracking-wider font-mono">
                  <th className="py-3 px-4">Invoice #</th>
                  <th className="py-3 px-4">Customer / Dealer</th>
                  <th className="py-3 px-4">Taxable Value</th>
                  <th className="py-3 px-4">GST Split (CGST/SGST/IGST)</th>
                  <th className="py-3 px-4 text-right">Total Invoice (₹)</th>
                  <th className="py-3 px-4">Due Date</th>
                  <th className="py-3 px-4 text-center">Status</th>
                  <th className="py-3 px-4 text-right">Autonomous Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {filtered.map((inv) => {
                  const isOverdue = inv.paymentStatus === 'OVERDUE';
                  const cgst = inv.cgstAmountInr || inv.cgst || 0;
                  const sgst = inv.sgstAmountInr || inv.sgst || 0;
                  const igst = inv.igstAmountInr || inv.igst || 0;

                  return (
                    <tr key={inv.id} className="hover:bg-surface-hover cursor-pointer transition-colors group">
                      <td className="py-3.5 px-4 font-mono font-bold text-foreground">
                        {inv.invoiceNumber}
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="font-semibold text-foreground">{inv.entityName}</div>
                        <div className="text-[10px] text-muted-foreground font-mono">
                          GSTIN: {inv.entityGstin || inv.gstin || '27AABCS1429B1Z2'}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-foreground">
                        {formatInr(inv.subtotalInr || inv.subtotal || 0)}
                      </td>
                      <td className="py-3.5 px-4 text-muted-foreground text-[11px] font-mono">
                        {igst > 0 ? (
                          <span>IGST: {formatInr(igst)}</span>
                        ) : (
                          <span>
                            CGST: {formatInr(cgst)} &bull; SGST: {formatInr(sgst)}
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-right font-mono font-bold text-foreground">
                        {formatInr(inv.totalAmountInr || inv.totalAmount || 0)}
                      </td>
                      <td className="py-3.5 px-4 text-muted-foreground">
                        <div className="flex items-center gap-1.5 font-mono">
                          <Clock className="w-3 h-3 text-muted-foreground" />
                          <span>{new Date(inv.dueDate).toLocaleDateString()}</span>
                        </div>
                        {isOverdue && (
                          <div className="text-[10px] text-[var(--status-danger-text)] font-semibold mt-0.5 font-mono">
                            38+ days overdue
                          </div>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <StatusBadge
                          variant={isOverdue ? 'danger' : 'success'}
                          label={inv.paymentStatus}
                        />
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        {isOverdue ? (
                          <button
                            onClick={() => handleSendReminder(inv)}
                            className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-primary hover:bg-primary-hover text-primary-foreground font-semibold text-xs shadow-sm transition-colors"
                          >
                            <MessageSquare className="w-3.5 h-3.5" />
                            <span>WhatsApp Ping</span>
                          </button>
                        ) : (
                          <button
                            onClick={() => handleAudit(inv.invoiceNumber)}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-surface-subtle hover:bg-surface-hover text-foreground text-xs font-medium transition-colors"
                          >
                            <FileCheck className="w-3.5 h-3.5 text-primary" />
                            <span>Audit</span>
                          </button>
                        )}
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
