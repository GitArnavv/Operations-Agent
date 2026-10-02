import React, { useState } from 'react';
import { Terminal, Check, ChevronDown, ChevronUp, Clock, AlertCircle } from 'lucide-react';
import type { ToolExecutionResult } from '../../types';

export const ToolExecutionPill: React.FC<{ execution: ToolExecutionResult }> = ({ execution }) => {
  const [expanded, setExpanded] = useState(false);

  const getHumanDescription = (name: string): string => {
    switch (name) {
      case 'order.investigateDelay':
        return 'Investigating order fulfillment bottleneck';
      case 'order.getOrderDetails':
        return 'Retrieving order & line item ledger';
      case 'inventory.checkStock':
        return 'Auditing warehouse inventory balances';
      case 'inventory.getLowStockForecast':
        return 'Projecting days-of-supply & stockout risk';
      case 'supplier.benchmarkReliability':
        return 'Benchmarking supplier on-time delivery rates';
      case 'supplier.getOpenPurchaseOrders':
        return 'Querying open vendor purchase orders';
      case 'invoice.getOverdueInvoices':
        return 'Filtering overdue receivables above threshold';
      case 'analytics.getAttentionSummary':
        return 'Synthesizing operational exceptions';
      case 'action.proposePurchaseOrder':
        return 'Generating replenishment PO proposal';
      default:
        return `Executing ${name}`;
    }
  };

  return (
    <div className="inline-block my-1 font-sans text-xs">
      <div
        onClick={() => setExpanded(!expanded)}
        className="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-surface border border-border hover:border-primary cursor-pointer transition-all shadow-subtle group"
      >
        <div className="flex items-center gap-1.5 font-mono text-[11px] text-primary">
          <Terminal className="w-3 h-3" />
          <span>{execution.toolName}</span>
        </div>
        <span className="text-muted-foreground hidden sm:inline">&bull;</span>
        <span className="text-foreground font-medium">
          {getHumanDescription(execution.toolName)}
        </span>
        <div className="flex items-center gap-1 text-[10px] text-muted-foreground font-mono">
          <Clock className="w-2.5 h-2.5" />
          <span>{execution.executionTimeMs}ms</span>
        </div>
        {execution.success ? (
          <Check className="w-3.5 h-3.5 text-emerald-500" />
        ) : (
          <AlertCircle className="w-3.5 h-3.5 text-danger" />
        )}
        {expanded ? (
          <ChevronUp className="w-3 h-3 text-muted-foreground" />
        ) : (
          <ChevronDown className="w-3 h-3 text-muted-foreground" />
        )}
      </div>

      {expanded && (
        <div className="mt-1.5 p-3 rounded-xl border border-border bg-surface-elevated shadow-subtle text-xs space-y-2 animate-scale-in max-w-lg">
          <div className="flex items-center justify-between text-[11px] text-muted-foreground font-mono">
            <span>Result Summary</span>
            <span className={execution.success ? 'text-emerald-500' : 'text-danger'}>
              {execution.success ? 'Completed' : 'Failed'}
            </span>
          </div>
          <div className="p-2 rounded-lg bg-surface-subtle text-foreground font-mono text-[11px] overflow-x-auto whitespace-pre-wrap max-h-36 border border-border">
            {typeof execution.result === 'string'
              ? execution.result
              : JSON.stringify(execution.result, null, 2)}
          </div>
        </div>
      )}
    </div>
  );
};
