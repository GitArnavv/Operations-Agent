import React, { useState } from 'react';
import { UserCheck, Key, Plus, UserPlus, X } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';

export const TeamPage: React.FC = () => {
  const { user } = useAuth();
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [newMember, setNewMember] = useState({
    name: '',
    email: '',
    role: 'OPERATIONS_MANAGER',
    phone: '',
  });

  const [members, setMembers] = useState([
    { id: '1', name: 'Rajesh Sharma', email: 'rajesh@sharmaelectricals.in', role: 'OWNER', phone: '+91 98201 12345', status: 'Active' },
    { id: '2', name: 'Amit Patel', email: 'amit.patel@sharmaelectricals.in', role: 'OPERATIONS_MANAGER', phone: '+91 98202 23456', status: 'Active' },
    { id: '3', name: 'Priya Deshmukh', email: 'priya.deshmukh@sharmaelectricals.in', role: 'FINANCE_MANAGER', phone: '+91 98203 34567', status: 'Active' },
    { id: '4', name: 'Vikas Gupta', email: 'vikas.g@sharmaelectricals.in', role: 'INVENTORY_MANAGER', phone: '+91 98204 45678', status: 'Active' },
  ]);

  const permissions = [
    { code: 'inventory.read', label: 'View Inventory & Stockout Probability', roles: ['OWNER', 'OPERATIONS_MANAGER', 'INVENTORY_MANAGER', 'ANALYST'] },
    { code: 'orders.write', label: 'Update Orders & Delivery Expedites', roles: ['OWNER', 'OPERATIONS_MANAGER'] },
    { code: 'purchase_orders.approve', label: 'Authorize Procurement & PO Requisitions', roles: ['OWNER', 'OPERATIONS_MANAGER'] },
    { code: 'invoices.write', label: 'Reconcile GST Invoices & Payment Offsets', roles: ['OWNER', 'FINANCE_MANAGER'] },
    { code: 'agent.execute', label: 'Authorize High-Risk Autonomous Agent Actions', roles: ['OWNER', 'OPERATIONS_MANAGER'] },
  ];

  const handleInviteSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMember.name || !newMember.email) return;

    setMembers([
      ...members,
      {
        id: String(Date.now()),
        name: newMember.name,
        email: newMember.email,
        role: newMember.role,
        phone: newMember.phone || '+91 98200 00000',
        status: 'Invited',
      },
    ]);
    setShowInviteModal(false);
    setNewMember({ name: '', email: '', role: 'OPERATIONS_MANAGER', phone: '' });
  };

  return (
    <PageTransition>
      <div className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
        <PageHeader
          eyebrow="Access & Governance"
          title="Team Members & Granular RBAC"
          description="Role-based access control protecting tenant isolation, financial authority thresholds, and AI autonomous execution scopes."
          actions={
            <button
              onClick={() => setShowInviteModal(true)}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-sm transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Invite Member</span>
            </button>
          }
        />

        {/* Members Table */}
        <div className="rounded-xl bg-surface border border-border shadow-subtle overflow-hidden">
          <div className="p-4 border-b border-border bg-surface-subtle font-bold text-xs text-foreground flex items-center justify-between">
            <span>Active Team Members ({members.length})</span>
            <span className="text-[11px] font-mono text-muted-foreground">Tenant: Sharma Electricals</span>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-foreground">
              <thead className="bg-surface-subtle text-[11px] uppercase tracking-wider text-muted-foreground border-b border-border font-semibold">
                <tr>
                  <th className="py-3 px-4">Full Name</th>
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Role</th>
                  <th className="py-3 px-4">Phone</th>
                  <th className="py-3 px-4 text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border font-medium">
                {members.map((m) => (
                  <tr key={m.id} className="hover:bg-surface-hover transition-colors">
                    <td className="py-3 px-4 font-bold text-foreground">{m.name}</td>
                    <td className="py-3 px-4 text-muted-foreground font-mono">{m.email}</td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-primary-subtle text-primary">
                        {m.role}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-muted-foreground font-mono">{m.phone}</td>
                    <td className="py-3 px-4 text-right">
                      <StatusBadge
                        variant={m.status === 'Active' ? 'success' : 'neutral'}
                        label={m.status}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Permissions Matrix */}
        <div className="p-6 rounded-xl bg-surface border border-border shadow-subtle space-y-4">
          <div className="flex items-center gap-2">
            <Key className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-foreground">Granular Action Permissions Matrix</h2>
          </div>
          <p className="text-xs text-muted-foreground">
            Defines which enterprise actions require human review versus automatic authorization.
          </p>
          <div className="space-y-2 text-xs">
            {permissions.map((p) => (
              <div
                key={p.code}
                className="p-3.5 rounded-xl bg-surface-subtle flex flex-col sm:flex-row sm:items-center justify-between gap-2"
              >
                <div>
                  <span className="font-mono text-primary font-semibold text-xs">{p.code}</span>
                  <p className="text-muted-foreground mt-0.5">{p.label}</p>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {p.roles.map((r) => (
                    <span
                      key={r}
                      className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-surface text-foreground"
                    >
                      {r}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Invite Member Modal */}
        {showInviteModal && (
          <div className="fixed inset-0 z-modal bg-overlay flex items-center justify-center p-4">
            <div className="w-full max-w-md bg-surface border border-border rounded-xl p-6 shadow-elevated space-y-4 animate-scale-in">
              <div className="flex items-center justify-between border-b border-border pb-3">
                <h3 className="text-base font-bold text-foreground flex items-center gap-2">
                  <UserPlus className="w-4 h-4 text-primary" />
                  Invite Team Member
                </h3>
                <button
                  onClick={() => setShowInviteModal(false)}
                  className="text-muted-foreground hover:text-foreground"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <form onSubmit={handleInviteSubmit} className="space-y-4 text-xs">
                <div>
                  <label className="block text-foreground font-medium mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Sunil Mehta"
                    value={newMember.name}
                    onChange={(e) => setNewMember({ ...newMember, name: e.target.value })}
                    className="w-full bg-surface-subtle border border-border rounded-xl px-3 py-2 text-foreground focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-foreground font-medium mb-1">Email Address</label>
                  <input
                    type="email"
                    required
                    placeholder="sunil.mehta@sharmaelectricals.in"
                    value={newMember.email}
                    onChange={(e) => setNewMember({ ...newMember, email: e.target.value })}
                    className="w-full bg-surface-subtle border border-border rounded-xl px-3 py-2 text-foreground focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-foreground font-medium mb-1">Role & Authority</label>
                  <select
                    value={newMember.role}
                    onChange={(e) => setNewMember({ ...newMember, role: e.target.value })}
                    className="w-full bg-surface-subtle border border-border rounded-xl px-3 py-2 text-foreground focus:outline-none focus:border-primary"
                  >
                    <option value="OPERATIONS_MANAGER">OPERATIONS_MANAGER (Approval & Action Authority)</option>
                    <option value="FINANCE_MANAGER">FINANCE_MANAGER (Invoices & Ledger)</option>
                    <option value="INVENTORY_MANAGER">INVENTORY_MANAGER (Stock & Warehouses)</option>
                    <option value="ANALYST">ANALYST (Read-Only Telemetry)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-foreground font-medium mb-1">Phone Number (WhatsApp Alerts)</label>
                  <input
                    type="tel"
                    placeholder="+91 98205 12345"
                    value={newMember.phone}
                    onChange={(e) => setNewMember({ ...newMember, phone: e.target.value })}
                    className="w-full bg-surface-subtle border border-border rounded-xl px-3 py-2 text-foreground font-mono focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="flex items-center justify-end gap-2 pt-2 border-t border-border">
                  <button
                    type="button"
                    onClick={() => setShowInviteModal(false)}
                    className="px-4 py-2 rounded-xl text-muted-foreground hover:text-foreground text-xs font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground font-semibold text-xs shadow-sm transition-colors"
                  >
                    Send Invitation
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </PageTransition>
  );
};
