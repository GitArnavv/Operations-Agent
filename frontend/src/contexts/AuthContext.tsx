import React, { createContext, useContext, useState, useEffect } from 'react';
import type { User, Organization } from '../types';
import { authApi } from '../api/services';

interface AuthContextType {
  user: User | null;
  organization: Organization | null;
  token: string | null;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  signup: (payload: { email: string; password: string; fullName: string; orgName?: string; gstin?: string }) => Promise<void>;
  logout: () => void;
  loginAsDemo: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('aiops_token'));
  const [user, setUser] = useState<User | null>(null);
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('aiops_token');
      if (storedToken) {
        try {
          const u = await authApi.me();
          setUser(u);
          // Set default Sharma Electricals org metadata
          setOrganization({
            id: u.tenantId,
            name: 'Sharma Electricals Pvt. Ltd.',
            gstin: '27AABCS1429B1Z2',
            pan: 'AABCS1429B',
            state: 'Maharashtra',
            city: 'Mumbai',
            industry: 'Electrical Distribution',
            tier: 'BUSINESS',
            currency: 'INR',
            address: 'Bhiwandi Central Logistics Hub, Thane, Maharashtra',
          });
        } catch {
          localStorage.removeItem('aiops_token');
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, pass: string) => {
    const res = await authApi.login(email, pass);
    localStorage.setItem('aiops_token', res.token);
    setToken(res.token);
    setUser(res.user);
    setOrganization(res.organization);
  };

  const signup = async (payload: { email: string; password: string; fullName: string; orgName?: string; gstin?: string }) => {
    const res = await authApi.signup(payload);
    localStorage.setItem('aiops_token', res.token);
    setToken(res.token);
    setUser(res.user);
    setOrganization(res.organization);
  };

  const loginAsDemo = async () => {
    await login('amit.patel@sharmaelectricals.in', 'demo123');
  };

  const logout = () => {
    localStorage.removeItem('aiops_token');
    setToken(null);
    setUser(null);
    setOrganization(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        organization,
        token,
        isLoading,
        login,
        signup,
        logout,
        loginAsDemo,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
