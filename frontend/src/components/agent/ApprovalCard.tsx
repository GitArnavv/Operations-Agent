import React, { useState } from 'react';
import {
  ShieldAlert,
  CheckCircle,
  XCircle,
  ArrowRight,
  Info,
  HelpCircle,
  Edit3,
  Bot,
} from 'lucide-react';
import type { AgentActionProposal } from '../../types';
import { useAgent } from '../../contexts/AgentContext';
import { formatInr } from '../../utils/formatters';

interface ApprovalCardProps {
  proposal: AgentActionProposal;
  onApprove: (id: string, notes?: string) => Promise<void>;
  onReject: (id: string, reason?: string) => Promise<void>;
}

export const ApprovalCard: React.FC<ApprovalCardProps> = ({ proposal, onApprove, onReject }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isApproved, setIsApproved] = useState(false);
  const [isRejected, setIsRejected] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editedNotes, setEditedNotes] = useState('');
  const { sendMessage, setIsDrawerOpen } = useAgent();

  const handleApprove = async () => {
    setIsSubmitting(true);
    try {
      await onApprove(proposal.id, editedNotes || undefined);
      setIsApproved(true);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async () => {
    setIsSubmitting(true);
    try {
      await onReject(proposal.id, editedNotes || undefined);
      setIsRejected(true);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleAskAIToRevise = () => {
    sendMessage(`Revise action proposal "${proposal.title}". Please adjust quantities or explore alternate supplier options.`);
    setIsDrawerOpen(true);
  };

  const getRiskBadge = (risk: string) => {
    switch (risk.toUpperCase()) {
      case 'HIGH_RISK':
        return 'bg-[var(--status-danger-bg)] text-[var(--status-danger-text)] border-[var(--status-danger-border)]';
      case 'MEDIUM_RISK':
        return 'bg-[var(--status-warning-bg)] text-[var(--status-warning-text)] border-[var(--status-warning-border)]';
      default:
        return 'bg-[var(--status-info-bg)] text-[var(--status-info-text)] border-[var(--status-info-border)]';
    }
  };

  return (
    <div className="my-3 p-4 rounded-xl border border-border bg-surface text-sm font-sans theme-surface">
      {/* Header */}
      <div className="flex items-center justify-between gap-2 pb-3 mb-3 border-b border-border">
        <div className="flex items-center gap-2">
          <ShieldAlert className="w-4 h-4 text-primary shrink-0" />
          <span className="font-semibold text-foreground tracking-tight text-xs sm:text-sm">
            {proposal.title}
          </span>
        </div>
        <span
          className={`px-2 py-0.5 rounded text-[10px] font-mono font-semibold tracking-wider uppercase border ${getRiskBadge(
            proposal.riskLevel
          )}`}
        >
          {proposal.riskLevel}
        </span>
      </div>

      {/* Proposal Details */}
      <div className="space-y-2.5 text-xs mb-3.5">
        <div>
          <span className="text-muted-foreground">Business Rationale: </span>
          <span className="text-foreground font-medium">{proposal.reason}</span>
        </div>

        {/* Financial & Entity Grid */}
        <div className="grid grid-cols-2 gap-2 p-2.5 rounded-lg bg-surface-subtle border border-border">
          <div>
            <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">
              Estimated Value
            </div>
            <div className="text-base font-bold text-foreground font-mono mt-0.5">
              {formatInr(proposal.estimatedCostInr)}
            </div>
          </div>
          <div>
            <div className="text-[10px] text-muted-foreground uppercase font-mono font-semibold">
              Affected Entity
            </div>
            <div className="text-xs font-medium text-foreground mt-0.5 truncate">
              {proposal.affectedEntities}
            </div>
          </div>
        </div>

        <div>
          <span className="text-muted-foreground">Expected Impact: </span>
          <span className="text-foreground">{proposal.expectedImpact}</span>
        </div>

        {/* Explicit Action Disclosure */}
        <div className="p-2.5 rounded-lg bg-surface-subtle border border-border text-[11px] text-muted-foreground flex items-center gap-2">
          <Info className="w-4 h-4 text-primary shrink-0" />
          <span>
            {proposal.actionType === 'PURCHASE_ORDER_EXPEDITE'
              ? 'Approving will issue PO Requisition #PO-2026-442 to Polycab India. No payment will be debited.'
              : 'Approving will update the order promised date in ERP and send an automated WhatsApp delivery update.'}
          </span>
        </div>
      </div>

      {/* Decision Buttons */}
      {isApproved ? (
        <div className="p-2.5 rounded-lg bg-[var(--status-success-bg)] border border-[var(--status-success-border)] text-[var(--status-success-text)] font-semibold text-xs flex items-center gap-2">
          <CheckCircle className="w-4 h-4" />
          <span>Action Approved & Recorded in Audit Ledger</span>
        </div>
      ) : isRejected ? (
        <div className="p-2.5 rounded-lg bg-[var(--status-danger-bg)] border border-[var(--status-danger-border)] text-[var(--status-danger-text)] font-semibold text-xs flex items-center gap-2">
          <XCircle className="w-4 h-4" />
          <span>Action Rejected by Manager</span>
        </div>
      ) : (
        <div className="space-y-2">
          {isEditing && (
            <div className="mb-2">
              <input
                type="text"
                value={editedNotes}
                onChange={(e) => setEditedNotes(e.target.value)}
                placeholder="Enter modification notes or manager authorization remarks..."
                className="w-full bg-surface-subtle border border-border rounded-lg px-3 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
              />
            </div>
          )}

          <div className="flex flex-wrap items-center gap-2">
            <button
              onClick={handleApprove}
              disabled={isSubmitting}
              className="px-3.5 py-1.5 rounded-lg bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-sm transition-colors disabled:opacity-50"
            >
              Approve Action
            </button>
            <button
              onClick={() => setIsEditing(!isEditing)}
              className="px-3 py-1.5 rounded-lg bg-surface border border-border hover:bg-surface-hover text-foreground text-xs font-medium transition-colors"
            >
              {isEditing ? 'Cancel Note' : 'Add Note'}
            </button>
            <button
              onClick={handleReject}
              disabled={isSubmitting}
              className="px-3 py-1.5 rounded-lg bg-surface border border-border hover:bg-[var(--status-danger-bg)] text-muted-foreground hover:text-[var(--status-danger-text)] text-xs font-medium transition-colors disabled:opacity-50"
            >
              Reject
            </button>
            <button
              onClick={handleAskAIToRevise}
              className="px-2.5 py-1.5 rounded-lg text-primary hover:bg-surface-subtle text-xs font-medium transition-colors flex items-center gap-1 ml-auto"
            >
              <Bot className="w-3.5 h-3.5" />
              <span>Ask AI to Revise</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
