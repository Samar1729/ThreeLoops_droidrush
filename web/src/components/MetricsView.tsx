import type { MetricsSnapshot } from '../types'
import { Activity, ShieldCheck, HardDrive, Cpu, Zap, Lock } from 'lucide-react'

interface MetricsViewProps {
  metrics: MetricsSnapshot
}

export const MetricsView = ({ metrics }: MetricsViewProps) => {
  const stageBudgets = [
    { name: 'Decode (512px Thumbnail)', ms: metrics.perStageMs.decode, budget: '15-40 ms', owner: 'Rishav' },
    { name: 'ML Kit Labels (Vision)', ms: metrics.perStageMs.label, budget: '30-100 ms', owner: 'Aditya' },
    { name: 'ML Kit Faces (FAST mode)', ms: metrics.perStageMs.face, budget: '30-80 ms', owner: 'Aditya' },
    { name: 'OCR (Latin, text-like only)', ms: metrics.perStageMs.ocr, budget: '150-400 ms', owner: 'Aditya' },
    { name: 'Perceptual Hash + Blur (DCT)', ms: metrics.perStageMs.hash, budget: '5-20 ms', owner: 'Samar' },
    { name: 'Room SQLite Batch Commit', ms: metrics.perStageMs.db, budget: '50-150 ms/batch', owner: 'Rishav' },
  ]

  const maxMs = Math.max(...stageBudgets.map((s) => s.ms))

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Header */}
      <div>
        <div className="flex items-center space-x-2 text-xs font-semibold tracking-wider uppercase text-neutral-400 dark:text-neutral-500 mb-1">
          <Activity className="w-3.5 h-3.5" />
          <span>Module 3.19 • Benchmark Telemetry & Privacy Audit</span>
        </div>
        <h1 className="text-3xl font-semibold tracking-tight text-neutral-900 dark:text-neutral-100">
          On-Device Performance & Proof
        </h1>
        <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-1 max-w-2xl">
          Real numbers measured on physical device hardware. Confirms zero internet permission, low memory footprint, and pigeonhole bucketing speedup.
        </p>
      </div>

      {/* Headline Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-4 rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800">
          <div className="flex items-center space-x-1.5 text-neutral-500 text-[11px] font-semibold uppercase tracking-wider mb-1">
            <Zap className="w-3.5 h-3.5" />
            <span>Throughput</span>
          </div>
          <p className="text-2xl font-bold text-neutral-900 dark:text-neutral-100">
            {metrics.throughputImgPerSec} <span className="text-xs font-normal text-neutral-500">img/sec</span>
          </p>
          <p className="text-[10px] text-neutral-400 mt-1">Parallel worker pipeline</p>
        </div>

        <div className="p-4 rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800">
          <div className="flex items-center space-x-1.5 text-neutral-500 text-[11px] font-semibold uppercase tracking-wider mb-1">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
            <span>Network</span>
          </div>
          <p className="text-xl font-bold text-emerald-600 dark:text-emerald-400">
            0 Permission
          </p>
          <p className="text-[10px] text-neutral-400 mt-1">INTERNET stripped in Manifest</p>
        </div>

        <div className="p-4 rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800">
          <div className="flex items-center space-x-1.5 text-neutral-500 text-[11px] font-semibold uppercase tracking-wider mb-1">
            <HardDrive className="w-3.5 h-3.5" />
            <span>RAM Probe</span>
          </div>
          <p className="text-2xl font-bold text-neutral-900 dark:text-neutral-100">
            {metrics.peakHeapMb} <span className="text-xs font-normal text-neutral-500">MB Peak</span>
          </p>
          <p className="text-[10px] text-neutral-400 mt-1">Semaphore(3) bounds memory</p>
        </div>

        <div className="p-4 rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800">
          <div className="flex items-center space-x-1.5 text-neutral-500 text-[11px] font-semibold uppercase tracking-wider mb-1">
            <Lock className="w-3.5 h-3.5" />
            <span>Encryption</span>
          </div>
          <p className="text-base font-bold text-neutral-900 dark:text-neutral-100 truncate">
            SQLCipher
          </p>
          <p className="text-[10px] text-neutral-400 mt-1">AES-GCM Keystore wrapped</p>
        </div>
      </div>

      {/* Pigeonhole Bucketing Speedup (Headline from Section 3.12) */}
      <div className="p-5 rounded-2xl bg-neutral-900 text-white dark:bg-neutral-950 border border-neutral-800 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Cpu className="w-4 h-4 text-emerald-400" />
            <h2 className="text-sm font-semibold tracking-wide">
              Perceptual Hashing Bucketing Speedup (Samar S-2 & R-9)
            </h2>
          </div>
          <span className="text-xs font-mono text-emerald-400 font-semibold px-2 py-0.5 rounded bg-emerald-950 border border-emerald-800">
            99.99% Pair Reduction
          </span>
        </div>

        <p className="text-xs text-neutral-400 font-mono leading-relaxed">
          Splitting 64-bit hashes into 8 chunks of 8 bits guarantees that pairs with Hamming distance ≤ 7 share at least one identical byte (pigeonhole principle). Only bucket collisions are evaluated.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
          <div className="p-3 rounded-xl bg-neutral-800/80 border border-neutral-700/60 font-mono text-xs">
            <span className="text-neutral-400 text-[10px] uppercase">Naive All-Pairs Comparison:</span>
            <p className="text-lg font-bold text-neutral-200 mt-0.5">
              {metrics.comparisonsNaive.toLocaleString()} pairs
            </p>
            <span className="text-[10px] text-neutral-500">Formula: n × (n - 1) / 2</span>
          </div>
          <div className="p-3 rounded-xl bg-emerald-950/40 border border-emerald-800/60 font-mono text-xs">
            <span className="text-emerald-400 text-[10px] uppercase">Pigeonhole Bucketed Pairs:</span>
            <p className="text-lg font-bold text-emerald-300 mt-0.5">
              {metrics.comparisonsBucketed.toLocaleString()} pairs
            </p>
            <span className="text-[10px] text-emerald-500">Only candidate bucket matches checked</span>
          </div>
        </div>
      </div>

      {/* One Photo's Journey: Stage Budgets (Section 7) */}
      <div className="p-5 rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 space-y-4">
        <div className="flex items-center justify-between border-b border-neutral-100 dark:border-neutral-800 pb-3">
          <div>
            <h2 className="text-sm font-semibold text-neutral-900 dark:text-neutral-100">
              One Photo's Journey: Execution Budgets (Section 7)
            </h2>
            <p className="text-xs text-neutral-400 mt-0.5">
              Breakdown of milliseconds elapsed per pipeline stage
            </p>
          </div>
          <span className="text-xs font-mono font-medium text-neutral-500">
            Total Avg: ~{metrics.avgMsPerImage} ms/img
          </span>
        </div>

        <div className="space-y-3 font-mono text-xs">
          {stageBudgets.map((stage) => {
            const widthPct = Math.min(100, Math.max(8, (stage.ms / maxMs) * 100))
            return (
              <div key={stage.name} className="space-y-1">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-sans font-medium text-neutral-800 dark:text-neutral-200">
                    {stage.name}
                  </span>
                  <div className="flex items-center space-x-3 text-neutral-500">
                    <span className="text-[10px] px-1.5 py-0.5 rounded bg-neutral-100 dark:bg-neutral-800 text-neutral-600 dark:text-neutral-400">
                      Owner: {stage.owner}
                    </span>
                    <span className="font-semibold text-neutral-900 dark:text-neutral-100">
                      {stage.ms} ms
                    </span>
                    <span className="text-[10px] text-neutral-400">
                      (spec: {stage.budget})
                    </span>
                  </div>
                </div>
                
                {/* Visual Bar */}
                <div className="w-full h-2 rounded-full bg-neutral-100 dark:bg-neutral-800 overflow-hidden">
                  <div
                    style={{ width: `${widthPct}%` }}
                    className="h-full rounded-full bg-neutral-900 dark:bg-neutral-100 transition-all duration-500"
                  />
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
