import React, { useEffect, useState } from 'react';
import { Search } from 'lucide-react';
import { customersApi } from '../api/services';
import { useAgent } from '../contexts/AgentContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { TableSkeleton } from '../components/ui/Skeleton';
import { formatInr, formatPercent } from '../utils/formatters';
import type { Customer } from '../types';

export const CustomersPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    customersApi
      .getCustomers()
      .then((data) => {
        setCustomers(data);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleAuditCustomer = (cust: Customer) => {
    sendMessage(`Audit credit limit, payment history, and active order fulfillment for customer ${cust.name}`);
    setIsDrawerOpen(true);
  };

  const filtered = customers.filter(
    (c) =>
      c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.city.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <PageTransition className="space-y-6 max-w-7xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Commercial Counterparties"
        title="Customer Accounts & Credit Exposure"
        description="200+ registered SME contractors, industrial builders, and wholesale retail stores across Maharashtra."
        actions={
          <div className="relative">
            <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search customer by name or city..."
              className="bg-surface border border-border rounded-xl pl-9 pr-3 py-2 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary shadow-subtle"
            />
          </div>
        }
      />

      {/* Table or Skeleton */}
      {loading ? (
        <TableSkeleton rows={6} columns={8} />
      ) : (
        <div className="rounded-xl border border-border bg-surface shadow-subtle overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-surface-subtle text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
                  <th className="py-3 px-4">Customer Name</th>
                  <th className="py-3 px-4">GSTIN & State</th>
                  <th className="py-3 px-4">City</th>
                  <th className="py-3 px-4">Contact Person</th>
                  <th className="py-3 px-4 text-right">Credit Limit (₹)</th>
                  <th className="py-3 px-4 text-center">Limit Utilized</th>
                  <th className="py-3 px-4 text-right">Outstanding (₹)</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {filtered.slice(0, 100).map((c) => {
                  const creditLimit = Number(c.creditLimit ?? 0);
                  const outstanding = Number(c.outstandingAmount ?? 0);
                  const utilizationPct = Math.min(
                    100,
                    Math.round((outstanding / (creditLimit || 1)) * 100)
                  );
                  const isOverLimit = utilizationPct >= 80;

                  return (
                    <tr
                      key={c.id}
                      onClick={() => handleAuditCustomer(c)}
                      className="hover:bg-surface-hover cursor-pointer transition-colors group"
                    >
                      <td className="py-3.5 px-4 font-bold text-foreground">{c.name}</td>
                      <td className="py-3.5 px-4">
                        <div className="font-mono text-muted-foreground">{c.gstin}</div>
                        <div className="text-[10px] text-muted-foreground">{c.state}</div>
                      </td>
                      <td className="py-3.5 px-4 text-foreground">{c.city}</td>
                      <td className="py-3.5 px-4 text-muted-foreground">
                        <div>{c.name.split(' ')[0]} Manager</div>
                        <div className="text-[10px] font-mono text-muted-foreground">{c.phone}</div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-foreground font-semibold text-right">
                        {formatInr(c.creditLimit)}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <StatusBadge
                          variant={isOverLimit ? 'danger' : 'success'}
                          label={formatPercent(utilizationPct, 0)}
                        />
                      </td>
                      <td className="py-3.5 px-4 text-right font-mono font-bold">
                        <span className={outstanding > 100000 ? 'text-danger' : 'text-foreground'}>
                          {formatInr(c.outstandingAmount)}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleAuditCustomer(c);
                          }}
                          className="text-primary hover:underline text-[11px] font-medium"
                        >
                          Audit &rarr;
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
