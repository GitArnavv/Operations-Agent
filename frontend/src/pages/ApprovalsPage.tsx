import React, { useEffect, useState } from 'react';
import {
  CheckSquare,
  ShieldAlert,
  CheckCircle2,
  XCircle,
  ArrowRight,
  Info,
} from 'lucide-react';
import { approvalsApi } from '../api/services';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { formatInr } from '../utils/formatters';
import type { ApprovalRequest } from '../types';

export const ApprovalsPage: React.FC = () => {
  const [approvals, setApprovals] = useState<ApprovalRequest[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = () => {
    approvalsApi.getApprovals().then((data) => {
      setApprovals(data);
      setLoading(false);
    });
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleApprove = async (id: string) => {
    await approvalsApi.approve(id, 'Approved via Operations Portal.');
    loadData();
  };

  const handleReject = async (id: string) => {
    await approvalsApi.reject(id, 'Rejected by Operations Manager.');
    loadData();
  };

  const pendingCount = approvals.filter((a) => a.status === 'PENDING').length;

  return (
    <PageTransition className="space-y-6 max-w-5xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Governance & Authority"
        title="Human-in-the-Loop Approval Center"
        description="Autonomous agent proposals classified by risk level. High-risk procurement requires explicit human authorization."
        actions={
          <StatusBadge
            variant={pendingCount > 0 ? 'warning' : 'neutral'}
            label={`${pendingCount} Pending Decision`}
          />
        }
      />

      {/* Approvals List */}
      <div className="space-y-4">
        {approvals.map((req) => (
          <div
            key={req.id}
            className="p-6 rounded-xl border border-border bg-surface transition-all shadow-subtle space-y-4"
          >
            {/* Proposal Card Header */}
            <div className="flex items-center justify-between pb-1">
              <div className="flex items-center gap-2.5">
                <ShieldAlert className="w-5 h-5 text-primary" />
                <div>
                  <h2 className="text-sm font-bold text-foreground">{req.title}</h2>
                  <span className="text-[11px] font-mono text-muted-foreground">Proposal ID: {req.id}</span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <StatusBadge
                  variant={req.riskLevel === 'HIGH_RISK' ? 'danger' : 'warning'}
                  label={req.riskLevel}
                />
                <StatusBadge
                  variant={
                    req.status === 'EXECUTED' || req.status === 'APPROVED'
                      ? 'success'
                      : req.status === 'REJECTED'
                      ? 'danger'
                      : 'warning'
                  }
                  label={req.status}
                />
              </div>
            </div>

            {/* Proposal Content */}
            <div className="space-y-3 text-xs">
              <div>
                <span className="text-muted-foreground font-semibold">Operational Rationale: </span>
                <span className="text-foreground font-medium">{req.reason}</span>
              </div>

              {/* Financial & Entity Box */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 p-4 rounded-xl bg-surface-subtle">
                <div>
                  <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                    Estimated Financial Value
                  </div>
                  <div className="text-base font-bold font-mono text-foreground mt-0.5">
                    {formatInr(req.estimatedCostInr)}
                  </div>
                </div>
                <div>
                  <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                    Affected Entities / Supplier
                  </div>
                  <div className="text-xs font-semibold text-foreground mt-1 truncate">
                    {req.affectedEntities}
                  </div>
                </div>
              </div>

              <div>
                <span className="text-muted-foreground font-semibold">Expected Operational Impact: </span>
                <span className="text-foreground">{req.expectedImpact}</span>
              </div>

              {/* Action Disclosure Notice */}
              <div className="p-3 rounded-xl bg-surface-subtle text-[11px] text-foreground flex items-start gap-2">
                <Info className="w-4 h-4 text-primary shrink-0 mt-0.5" />
                <span>
                  <strong>Action Disclosure:</strong> Approving will generate a formal purchase order to the verified OEM supplier. No automated financial debit will be executed without secondary payment approval.
                </span>
              </div>
            </div>

            {/* Actions */}
            {req.status === 'PENDING' ? (
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  onClick={() => handleReject(req.id)}
                  className="px-4 py-2 rounded-xl bg-surface-subtle hover:bg-surface-hover text-muted-foreground hover:text-foreground text-xs font-medium transition-colors"
                >
                  Reject Proposal
                </button>
                <button
                  onClick={() => handleApprove(req.id)}
                  className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-bold shadow-subtle transition-all"
                >
                  <span>Authorize & Execute</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            ) : req.status === 'EXECUTED' || req.status === 'APPROVED' ? (
              <div className="flex items-center gap-2 pt-2 text-emerald-600 dark:text-emerald-400 text-xs font-medium">
                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                <span>Action authorized and executed through controlled action tools. Immutable audit trail recorded.</span>
              </div>
            ) : (
              <div className="flex items-center gap-2 pt-2 text-danger text-xs font-medium">
                <XCircle className="w-4 h-4 text-danger" />
                <span>Action rejected by user. No database mutations or side effects performed.</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </PageTransition>
  );
};
