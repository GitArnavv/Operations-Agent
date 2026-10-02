import React, { createContext, useContext, useState } from 'react';
import type { AgentMessage, AgentActionProposal } from '../types';
import { agentApi, approvalsApi } from '../api/services';

interface AgentContextType {
  messages: AgentMessage[];
  isLoading: boolean;
  isDrawerOpen: boolean;
  setIsDrawerOpen: (open: boolean) => void;
  sendMessage: (prompt: string) => Promise<AgentMessage>;
  approveAction: (approvalId: string, notes?: string) => Promise<void>;
  rejectAction: (approvalId: string, reason?: string) => Promise<void>;
  clearConversation: () => void;
  language: string;
  setLanguage: (lang: string) => void;
}

const AgentContext = createContext<AgentContextType | undefined>(undefined);

const INITIAL_MESSAGE: AgentMessage = {
  id: 'msg_welcome',
  role: 'AGENT',
  content:
    'Namaste! I am your **AI Operations Agent** for Sharma Electricals.\n\nI monitor your ERP, inventory, supplier reliability, and orders 24/7.\n\nYou can speak or type queries such as:\n- *"What needs my attention today?"*\n- *"Why is Sharma Electronics\' order delayed?"*\n- *"Which products are likely to run out this week?"*\n- *"Show me overdue invoices above ₹1 lakh"*',
  citations: [],
  toolExecutions: [],
  timestamp: new Date().toISOString(),
};

export const AgentProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [messages, setMessages] = useState<AgentMessage[]>([INITIAL_MESSAGE]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
  const [language, setLanguage] = useState<string>('en-IN');

  const sendMessage = async (prompt: string): Promise<AgentMessage> => {
    const userMsg: AgentMessage = {
      id: 'msg_' + Date.now(),
      role: 'USER',
      content: prompt,
      citations: [],
      toolExecutions: [],
      timestamp: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setIsLoading(true);

    try {
      const response = await agentApi.chat(prompt);
      setMessages((prev) => [...prev, response]);
      return response;
    } catch (err: any) {
      const errorMsg: AgentMessage = {
        id: 'msg_err_' + Date.now(),
        role: 'AGENT',
        content: `Operational agent temporarily encountered an error: ${err.message || 'Service unavailable'}. Please verify backend connection.`,
        citations: [],
        toolExecutions: [],
        timestamp: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, errorMsg]);
      return errorMsg;
    } finally {
      setIsLoading(false);
    }
  };

  const approveAction = async (approvalId: string, notes?: string) => {
    try {
      await approvalsApi.approve(approvalId, notes);
      // Update message in state to mark executed
      setMessages((prev) =>
        prev.map((msg) => {
          if (msg.proposedAction && msg.proposedAction.id === approvalId) {
            return {
              ...msg,
              proposedAction: {
                ...msg.proposedAction,
                requiresApproval: false,
                title: '✅ EXECUTED: ' + msg.proposedAction.title,
              },
            };
          }
          return msg;
        })
      );
    } catch (err: any) {
      console.error('Failed to approve action', err);
    }
  };

  const rejectAction = async (approvalId: string, reason?: string) => {
    try {
      await approvalsApi.reject(approvalId, reason);
      setMessages((prev) =>
        prev.map((msg) => {
          if (msg.proposedAction && msg.proposedAction.id === approvalId) {
            return {
              ...msg,
              proposedAction: {
                ...msg.proposedAction,
                requiresApproval: false,
                title: '❌ REJECTED: ' + msg.proposedAction.title,
              },
            };
          }
          return msg;
        })
      );
    } catch (err: any) {
      console.error('Failed to reject action', err);
    }
  };

  const clearConversation = () => {
    setMessages([INITIAL_MESSAGE]);
  };

  return (
    <AgentContext.Provider
      value={{
        messages,
        isLoading,
        isDrawerOpen,
        setIsDrawerOpen,
        sendMessage,
        approveAction,
        rejectAction,
        clearConversation,
        language,
        setLanguage,
      }}
    >
      {children}
    </AgentContext.Provider>
  );
};

export const useAgent = () => {
  const context = useContext(AgentContext);
  if (!context) {
    throw new Error('useAgent must be used within an AgentProvider');
  }
  return context;
};
