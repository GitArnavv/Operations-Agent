import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Building2, ArrowRight, Mail, Lock, User, Hash } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { ThemeToggle } from '../components/ui/ThemeToggle';
import { PageTransition } from '../components/ui/PageTransition';

export const SignupPage: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [orgName, setOrgName] = useState('');
  const [gstin, setGstin] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError('');
    try {
      await signup({ fullName, email, password, orgName, gstin });
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Onboarding failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <PageTransition>
      <div className="min-h-screen w-full bg-background flex flex-col justify-between p-4 sm:p-6 font-sans relative">
        {/* Top Navbar */}
        <div className="w-full max-w-5xl mx-auto flex items-center justify-between py-2">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center text-primary-foreground font-black text-sm shadow-sm">
              AI
            </div>
            <div>
              <span className="font-bold text-foreground text-sm tracking-tight block">
                OpsAgent India
              </span>
              <span className="text-[10px] text-muted-foreground font-mono">
                B2B Autonomous Operations
              </span>
            </div>
          </div>
          <ThemeToggle />
        </div>

        {/* Center Card */}
        <div className="w-full max-w-lg mx-auto my-auto p-7 sm:p-8 rounded-xl bg-surface border border-border shadow-elevated relative z-10 space-y-6">
          <div className="text-center space-y-2">
            <div className="w-12 h-12 rounded-xl bg-surface-subtle flex items-center justify-center mx-auto text-primary">
              <Building2 className="w-6 h-6" />
            </div>
            <h1 className="text-2xl font-extrabold text-foreground tracking-tight">
              Register Business Organization
            </h1>
            <p className="text-xs text-muted-foreground max-w-sm mx-auto leading-relaxed">
              Set up a secure, multi-tenant operations environment with automated GSTIN verification.
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-status-danger-bg text-rose-600 dark:text-rose-400 text-xs font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="text-foreground font-medium block mb-1">Company Legal Name</label>
              <div className="relative">
                <input
                  type="text"
                  required
                  value={orgName}
                  onChange={(e) => setOrgName(e.target.value)}
                  placeholder="e.g. Patel Engineering Works Pvt. Ltd."
                  className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
                />
                <Building2 className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="text-foreground font-medium block mb-1">GSTIN Number</label>
                <div className="relative">
                  <input
                    type="text"
                    required
                    value={gstin}
                    onChange={(e) => setGstin(e.target.value.toUpperCase())}
                    placeholder="27AABCP1234F1Z5"
                    maxLength={15}
                    className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground font-mono uppercase focus:outline-none focus:border-primary"
                  />
                  <Hash className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
                </div>
              </div>
              <div>
                <label className="text-foreground font-medium block mb-1">Your Full Name</label>
                <div className="relative">
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Rajesh Kumar"
                    className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
                  />
                  <User className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
                </div>
              </div>
            </div>

            <div>
              <label className="text-foreground font-medium block mb-1">Work Email</label>
              <div className="relative">
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="rajesh@patelengineering.in"
                  className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
                />
                <Mail className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
              </div>
            </div>

            <div>
              <label className="text-foreground font-medium block mb-1">Create Password</label>
              <div className="relative">
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
                />
                <Lock className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground font-bold text-xs shadow-sm transition-all flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <span>{isSubmitting ? 'Provisioning Tenant...' : 'Initialize Organization Environment'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="pt-2 text-center text-xs text-muted-foreground">
            <span>Already registered? </span>
            <Link to="/login" className="text-primary hover:underline font-bold">
              Sign In Here →
            </Link>
          </div>
        </div>

        {/* Footer */}
        <div className="w-full max-w-5xl mx-auto text-center py-2 text-[11px] text-muted-foreground font-mono">
          Strict Data Sovereignty • Hosted in AWS Mumbai (ap-south-1) • Zero External Data Sharing
        </div>
      </div>
    </PageTransition>
  );
};
