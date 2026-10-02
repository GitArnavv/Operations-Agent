import React, { useEffect, useState } from 'react';
import { Clock, User, Download, Search, Lock } from 'lucide-react';
import { auditLogsApi } from '../api/services';
import type { AuditLog } from '../types';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedEntity, setSelectedEntity] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    auditLogsApi.getAuditLogs().then((data) => {
      setLogs(data);
      setLoading(false);
    });
  }, []);

  const filteredLogs = logs.filter((log) => {
    const matchesEntity = selectedEntity === 'ALL' || log.entityType === selectedEntity;
    const matchesSearch =
      !searchQuery ||
      log.action.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.actorName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.entityId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (log.reason && log.reason.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesEntity && matchesSearch;
  });

  const handleExport = () => {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(logs, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `audit-trail-${new Date().toISOString().slice(0, 10)}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  return (
    <PageTransition>
      <div className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
        <PageHeader
          eyebrow="Compliance & Security"
          title="Immutable Operational Audit Trail"
          description="Cryptographically timestamped record of human approvals, autonomous agent interventions, and financial side-effects. Records are tamper-evident and tenant-isolated."
          actions={
            <button
              onClick={handleExport}
              className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-surface border border-border hover:bg-surface-hover text-foreground text-xs font-semibold shadow-sm transition-all"
            >
              <Download className="w-4 h-4 text-muted-foreground" />
              <span>Export Ledger</span>
            </button>
          }
        />

        {/* Filters & Search */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-1.5 overflow-x-auto w-full sm:w-auto pb-1 sm:pb-0">
            {['ALL', 'ORDERS', 'INVENTORY', 'APPROVALS', 'INVOICES'].map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedEntity(cat)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors ${
                  selectedEntity === cat
                    ? 'bg-primary text-primary-foreground shadow-sm'
                    : 'bg-surface border border-border text-muted-foreground hover:text-foreground hover:bg-surface-hover'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search actions, actors, or IDs..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-surface border border-border rounded-xl pl-9 pr-3 py-1.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
            />
          </div>
        </div>

        {/* Audit Log Entries */}
        <div className="rounded-xl bg-surface border border-border shadow-subtle overflow-hidden">
          <div className="p-4 border-b border-border bg-surface-subtle flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Lock className="w-3.5 h-3.5 text-muted-foreground" />
              <span className="text-xs font-bold text-foreground">
                Showing {filteredLogs.length} verified events
              </span>
            </div>
            <span className="text-[11px] font-mono text-muted-foreground">
              Tenant: Sharma Electricals Pvt. Ltd. (ORG-1029)
            </span>
          </div>

          <div className="divide-y divide-border text-xs">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground text-xs">
                Verifying audit ledger signatures...
              </div>
            ) : filteredLogs.length === 0 ? (
              <div className="p-8 text-center text-muted-foreground text-xs">
                No audit entries match the selected filter.
              </div>
            ) : (
              filteredLogs.map((log) => (
                <div key={log.id} className="p-4 hover:bg-surface-hover transition-colors space-y-2.5">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-mono font-bold text-xs px-2.5 py-0.5 rounded-lg bg-surface-subtle text-primary border border-border">
                        {log.action}
                      </span>
                      <span className="text-foreground font-semibold">
                        {log.entityType} <span className="font-mono text-muted-foreground">({log.entityId})</span>
                      </span>
                    </div>

                    <div className="flex items-center gap-3 text-[11px] text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <User className="w-3.5 h-3.5 text-muted-foreground" />
                        <span className="font-medium text-foreground">{log.actorName}</span>
                      </div>
                      <div className="flex items-center gap-1 font-mono">
                        <Clock className="w-3.5 h-3.5 text-muted-foreground" />
                        <span>{new Date(log.createdAt).toLocaleString()}</span>
                      </div>
                    </div>
                  </div>

                  {log.reason && (
                    <p className="text-foreground italic text-[11px] pl-3 border-l-2 border-primary">
                      "{log.reason}"
                    </p>
                  )}

                  {(log.beforeState || log.afterState) && (
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1 font-mono text-[10px]">
                      {log.beforeState && (
                        <div className="p-2.5 rounded-xl bg-surface-subtle border border-border text-foreground truncate">
                          <span className="text-rose-500 font-bold block mb-0.5">PREVIOUS STATE:</span>
                          <span className="text-muted-foreground">{log.beforeState}</span>
                        </div>
                      )}
                      {log.afterState && (
                        <div className="p-2.5 rounded-xl bg-surface-subtle border border-border text-foreground truncate">
                          <span className="text-emerald-500 font-bold block mb-0.5">APPLIED STATE:</span>
                          <span>{log.afterState}</span>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </PageTransition>
  );
};
