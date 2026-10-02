import React from 'react';
import { Package, ShoppingBag, Truck, FileText, CheckCircle2 } from 'lucide-react';
import type { CitationEvidence } from '../../types';

export const CitationBadge: React.FC<{
  citation: CitationEvidence;
  onInspect?: (evidence: CitationEvidence) => void;
}> = ({ citation, onInspect }) => {
  const getIcon = () => {
    switch (citation.entityType.toUpperCase()) {
      case 'ORDER':
        return <ShoppingBag className="w-3.5 h-3.5 text-blue-500" />;
      case 'INVENTORY':
        return <Package className="w-3.5 h-3.5 text-emerald-500" />;
      case 'SUPPLIER':
      case 'PURCHASE_ORDER':
        return <Truck className="w-3.5 h-3.5 text-amber-500" />;
      case 'INVOICE':
      case 'DOCUMENT':
        return <FileText className="w-3.5 h-3.5 text-purple-500" />;
      default:
        return <CheckCircle2 className="w-3.5 h-3.5 text-primary" />;
    }
  };

  const relevancePct = Math.round((citation.confidence || 0.95) * 100);

  return (
    <div
      onClick={() => onInspect && onInspect(citation)}
      className="inline-flex items-center gap-2 px-2.5 py-1.5 rounded-lg bg-surface border border-border hover:border-primary hover:bg-surface-hover cursor-pointer transition-all text-xs text-foreground shadow-subtle group font-sans"
      title={`${citation.summary} (Click to inspect evidence)`}
    >
      {getIcon()}
      <span className="font-medium text-foreground group-hover:text-primary transition-colors">
        {citation.label}
      </span>
      <span className="text-[10px] text-muted-foreground bg-surface-subtle px-1.5 py-0.5 rounded font-mono border border-border">
        {relevancePct}% match
      </span>
    </div>
  );
};
