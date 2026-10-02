import React, { useEffect, useState } from 'react';
import {
  FileText,
  Upload,
  Bot,
  FileCheck,
  ShieldCheck,
} from 'lucide-react';
import { documentsApi } from '../api/services';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { useAgent } from '../contexts/AgentContext';
import type { DocumentRecord } from '../types';

export const DocumentsPage: React.FC = () => {
  const [documents, setDocuments] = useState<DocumentRecord[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [activeDocument, setActiveDocument] = useState<DocumentRecord | null>(null);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    documentsApi.getDocuments().then((docs) => {
      setDocuments(docs);
      if (docs.length > 0) setActiveDocument(docs[0]);
    });
  }, []);

  const handleSimulatedUpload = async (title: string, fileName: string) => {
    setIsUploading(true);
    try {
      const doc = await documentsApi.upload(title, fileName, 'PDF', 380000);
      setDocuments((prev) => [doc, ...prev]);
      setActiveDocument(doc);
    } finally {
      setIsUploading(false);
    }
  };

  const handleAskAIToAuditDoc = (doc: DocumentRecord) => {
    sendMessage(`Audit extracted OCR metadata for document "${doc.title}". Verify vendor GSTIN against Maharashtra state registry and cross-check matching purchase order.`);
    setIsDrawerOpen(true);
  };

  return (
    <PageTransition className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Ingestion & Extraction"
        title="Document Intelligence & OCR Pipeline"
        description="Automated OCR extraction, GSTIN validation, and PO reconciliation for tax invoices and lorry receipts (LR)."
        actions={
          <StatusBadge
            variant="success"
            label="Anti-Prompt-Injection Scanner Active"
          />
        }
      />

      {/* Upload Dropzone */}
      <div className="p-8 rounded-xl border border-dashed border-border bg-surface text-center shadow-subtle">
        <div className="w-12 h-12 rounded-xl bg-surface-subtle text-primary flex items-center justify-center mx-auto mb-3 border border-border">
          <Upload className="w-6 h-6" />
        </div>
        <h2 className="text-sm font-bold text-foreground mb-1">
          Upload Invoices, POs, or Delivery Challans
        </h2>
        <p className="text-xs text-muted-foreground max-w-md mx-auto mb-5 leading-relaxed">
          Upload PDF, scanned LR receipts, Excel, or CSV invoices. The Document AI agent automatically extracts GST line items, HSN codes, and matching purchase orders.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-2.5">
          <button
            onClick={() =>
              handleSimulatedUpload(
                'Polycab Inbound Wire Tax Invoice #INV-8821.pdf',
                'invoice_polycab_halol_8821.pdf'
              )
            }
            disabled={isUploading}
            className="px-4 py-2.5 rounded-xl bg-primary hover:bg-primary-hover disabled:opacity-50 text-primary-foreground text-xs font-bold shadow-subtle transition-all flex items-center gap-2"
          >
            {isUploading ? (
              <>
                <span className="w-3.5 h-3.5 rounded-full border-2 border-primary-foreground border-t-transparent animate-spin"></span>
                <span>Running OCR & GST Validation...</span>
              </>
            ) : (
              <>
                <Upload className="w-3.5 h-3.5" />
                <span>Upload Sample Vendor Invoice (Polycab Halol)</span>
              </>
            )}
          </button>

          <button
            onClick={() =>
              handleSimulatedUpload(
                'Bhiwandi Freight Lorry Receipt #LR-8921.pdf',
                'lorry_receipt_bhiwandi_8921.pdf'
              )
            }
            disabled={isUploading}
            className="px-4 py-2.5 rounded-xl border border-border bg-surface hover:bg-surface-hover text-foreground text-xs font-semibold shadow-subtle transition-colors"
          >
            Upload Lorry Receipt (LR)
          </button>
        </div>
      </div>

      {/* OCR Pipeline Steps Banner */}
      <div className="p-4 rounded-xl border border-border bg-surface-subtle">
        <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider mb-2">
          Automated Ingestion Pipeline Stages
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
          {[
            { step: '1. Antivirus & Magic Byte Check', status: 'Passed' },
            { step: '2. Tesseract/Vision OCR Extractor', status: 'Active' },
            { step: '3. GSTIN & HSN Code Validator', status: 'Verified' },
            { step: '4. ERP Purchase Order Reconciler', status: 'Auto-Matched' },
          ].map((s, idx) => (
            <div key={idx} className="p-2.5 rounded-xl bg-surface border border-border space-y-0.5">
              <div className="text-[11px] font-semibold text-foreground">{s.step}</div>
              <div className="text-[10px] text-emerald-600 dark:text-emerald-400 font-mono font-medium">{s.status}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Documents Grid and Selected OCR Inspector */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Document List */}
        <div className="lg:col-span-2 rounded-xl border border-border bg-surface shadow-subtle overflow-hidden">
          <div className="p-4 border-b border-border text-xs font-bold uppercase tracking-wider text-muted-foreground bg-surface-subtle">
            Processed Document Repository ({documents.length})
          </div>
          <div className="divide-y divide-border">
            {documents.map((doc) => {
              const isSelected = activeDocument?.id === doc.id;
              return (
                <div
                  key={doc.id}
                  onClick={() => setActiveDocument(doc)}
                  className={`p-4 flex items-center justify-between gap-3 hover:bg-surface-hover cursor-pointer transition-colors ${
                    isSelected ? 'bg-surface-hover border-l-2 border-primary' : ''
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2.5 rounded-xl bg-surface-subtle border border-border text-primary">
                      <FileCheck className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-foreground">{doc.title}</div>
                      <div className="text-[11px] text-muted-foreground font-mono mt-0.5">
                        {doc.fileName} &bull; {(((doc.fileSizeBytes || doc.fileSize || 0)) / 1024).toFixed(0)} KB &bull;{' '}
                        {new Date(doc.createdAt).toLocaleDateString()}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <StatusBadge variant="success" label={doc.status} />
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleAskAIToAuditDoc(doc);
                      }}
                      className="p-1.5 rounded-lg text-muted-foreground hover:text-primary transition-colors"
                      title="Ask AI to audit"
                    >
                      <Bot className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Selected Document OCR Inspector Panel */}
        {activeDocument && (
          <div className="p-5 rounded-xl border border-border bg-surface shadow-subtle space-y-4">
            <div className="flex items-center justify-between border-b border-border pb-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-foreground">
                OCR Extracted Metadata
              </h3>
              <StatusBadge variant="success" label="98.4% Confidence" />
            </div>

            <div className="space-y-3 text-xs">
              <div className="p-3 rounded-xl bg-surface-subtle border border-border space-y-1">
                <div className="text-[10px] text-muted-foreground uppercase font-semibold">Vendor / Issuer</div>
                <div className="font-bold text-foreground">Polycab Wires Ltd (Halol Plant)</div>
                <div className="text-[10px] font-mono text-muted-foreground">GSTIN: 27AAACP0123M1ZQ</div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div className="p-2.5 rounded-xl border border-border bg-surface">
                  <div className="text-[10px] text-muted-foreground font-semibold">Invoice Number</div>
                  <div className="font-mono font-bold text-foreground mt-0.5">POL-98124</div>
                </div>
                <div className="p-2.5 rounded-xl border border-border bg-surface">
                  <div className="text-[10px] text-muted-foreground font-semibold">Invoice Date</div>
                  <div className="font-mono text-foreground mt-0.5">24 Sep 2026</div>
                </div>
              </div>

              <div className="p-3 rounded-xl border border-border bg-surface space-y-1 font-mono text-[11px]">
                <div className="flex justify-between text-muted-foreground">
                  <span>Taxable Subtotal:</span>
                  <span className="text-foreground">₹1,22,881.36</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>CGST (9%):</span>
                  <span className="text-foreground">₹11,059.32</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>SGST (9%):</span>
                  <span className="text-foreground">₹11,059.32</span>
                </div>
                <div className="flex justify-between font-bold text-foreground pt-1 border-t border-border">
                  <span>Total Amount:</span>
                  <span className="text-primary">₹1,45,000.00</span>
                </div>
              </div>

              <button
                onClick={() => handleAskAIToAuditDoc(activeDocument)}
                className="w-full py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-bold shadow-subtle flex items-center justify-center gap-2 transition-all"
              >
                <Bot className="w-4 h-4" />
                <span>Verify Against PO #PO-2381</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </PageTransition>
  );
};
