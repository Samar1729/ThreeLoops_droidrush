import { X, Cpu, RefreshCw, AlertTriangle } from 'lucide-react'

interface ArchitectureModalProps {
  onClose: () => void
}

export const ArchitectureModal = ({ onClose }: ArchitectureModalProps) => {
  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-md flex items-center justify-center p-3 sm:p-6 animate-in fade-in duration-200">
      <div className="relative w-full max-w-4xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Modal Header */}
        <div className="p-5 border-b border-neutral-200 dark:border-neutral-800 flex items-center justify-between">
          <div>
            <span className="text-[10px] font-semibold tracking-wider uppercase text-neutral-400">
              Droidrush 2026 • Problem Statement 06
            </span>
            <h2 className="text-lg font-bold text-neutral-900 dark:text-neutral-100">
              Mirror Archive Backend & Frontend Specification
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-neutral-400 hover:text-neutral-900 dark:hover:text-white hover:bg-neutral-100 dark:hover:bg-neutral-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-6 text-xs text-neutral-600 dark:text-neutral-400 font-mono">
          
          {/* Team Work Split */}
          <div className="space-y-3">
            <h3 className="text-xs font-bold uppercase tracking-wider text-neutral-900 dark:text-neutral-100 flex items-center space-x-1.5">
              <Cpu className="w-4 h-4" />
              <span>Three-Person Feature Ownership</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="p-3.5 rounded-xl border-2 border-neutral-900 dark:border-neutral-100 bg-neutral-50 dark:bg-neutral-950">
                <span className="text-[10px] font-bold uppercase tracking-wider text-black dark:text-white">
                  Aditya (Machine Learning)
                </span>
                <p className="font-semibold text-neutral-900 dark:text-neutral-100 mt-1">
                  ML Kit & Vision Engine
                </p>
                <ul className="mt-2 space-y-1 text-[11px] list-disc list-inside text-neutral-500">
                  <li>3.7 Image Analyzer (labels, faces, ocr)</li>
                  <li>3.8 Album Classifier (rules, synonyms)</li>
                  <li>3.9 Search (tag + runic OCR)</li>
                  <li>3.10 Faces & People (quality scoring)</li>
                  <li>3.19 Search latency timer</li>
                </ul>
              </div>

              <div className="p-3.5 rounded-xl border border-neutral-200 dark:border-neutral-800 bg-neutral-50/50 dark:bg-neutral-950/50">
                <span className="text-[10px] font-bold uppercase tracking-wider text-neutral-500">
                  Rishav (Engine & Storage)
                </span>
                <p className="font-semibold text-neutral-900 dark:text-neutral-100 mt-1">
                  Android Plumbing & Pipeline
                </p>
                <ul className="mt-2 space-y-1 text-[11px] list-disc list-inside text-neutral-500">
                  <li>3.1 Permissions & Onboarding</li>
                  <li>3.2 MediaStore Data Source</li>
                  <li>3.4 Room Database & DAOs</li>
                  <li>3.6 Indexing Worker (WorkManager)</li>
                  <li>3.18 SQLCipher Encryption</li>
                </ul>
              </div>

              <div className="p-3.5 rounded-xl border border-neutral-200 dark:border-neutral-800 bg-neutral-50/50 dark:bg-neutral-950/50">
                <span className="text-[10px] font-bold uppercase tracking-wider text-neutral-500">
                  Samar (Hashing & Dupes)
                </span>
                <p className="font-semibold text-neutral-900 dark:text-neutral-100 mt-1">
                  Algorithms & Clean-Up
                </p>
                <ul className="mt-2 space-y-1 text-[11px] list-disc list-inside text-neutral-500">
                  <li>3.11 Perceptual Hasher (dHash/pHash)</li>
                  <li>3.12 Duplicate Finder (Pigeonhole)</li>
                  <li>3.13 Purge Flow (DeleteRequest)</li>
                  <li>3.14 Burst & Best-Shot Selector</li>
                  <li>3.15 Storage Saver (Blur)</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Section 4 Indexing State Machine */}
          <div className="space-y-3 pt-3 border-t border-neutral-200 dark:border-neutral-800">
            <h3 className="text-xs font-bold uppercase tracking-wider text-neutral-900 dark:text-neutral-100 flex items-center space-x-1.5">
              <RefreshCw className="w-4 h-4" />
              <span>Section 4: Indexing State Machine</span>
            </h3>
            <p className="text-[11px] text-neutral-500">
              PAUSED is not a failure. Because each batch of 50 images and its checkpoint commit together in one Room transaction, resuming from PAUSED repeats no work and loses none.
            </p>
            <div className="p-3 rounded-xl bg-neutral-50 dark:bg-neutral-950 border border-neutral-200 dark:border-neutral-800 text-[11px] space-y-1.5">
              <div><strong className="text-neutral-900 dark:text-neutral-100">IDLE:</strong> App installed, permissions granted, waiting for trigger.</div>
              <div><strong className="text-neutral-900 dark:text-neutral-100">QUEUED:</strong> Work enqueued; waiting for batteryNotLow & storageNotLow constraints.</div>
              <div><strong className="text-neutral-900 dark:text-neutral-100">SCANNING:</strong> Paging MediaStore 50 at a time with Semaphore(3).</div>
              <div><strong className="text-neutral-900 dark:text-neutral-100">PAUSED:</strong> Worker interrupted (OS kill, 10-min limit); checkpoint preserved.</div>
              <div><strong className="text-neutral-900 dark:text-neutral-100">COMPLETE:</strong> MediaStore exhausted; ContentObserver armed for incremental sync.</div>
            </div>
          </div>

          {/* Section 9 Failure Modes */}
          <div className="space-y-3 pt-3 border-t border-neutral-200 dark:border-neutral-800">
            <h3 className="text-xs font-bold uppercase tracking-wider text-neutral-900 dark:text-neutral-100 flex items-center space-x-1.5">
              <AlertTriangle className="w-4 h-4" />
              <span>Section 9: Failure Modes & Recovery</span>
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-[11px]">
              <div className="p-3 rounded-xl bg-neutral-50 dark:bg-neutral-950 border border-neutral-200 dark:border-neutral-800">
                <span className="font-bold text-neutral-900 dark:text-neutral-100">OS Kills Worker Mid-Scan:</span>
                <p className="mt-1 text-neutral-500">
                  Checkpoint is saved in the same transaction as each batch. WorkManager auto-retries from lastProcessedId.
                </p>
              </div>
              <div className="p-3 rounded-xl bg-neutral-50 dark:bg-neutral-950 border border-neutral-200 dark:border-neutral-800">
                <span className="font-bold text-neutral-900 dark:text-neutral-100">Unreadable / Corrupt Image:</span>
                <p className="mt-1 text-neutral-500">
                  try/catch around processOne logs to SkippedImageEntity and advances checkpointId to prevent infinite retry loops.
                </p>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}
