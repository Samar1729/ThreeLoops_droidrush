import { useState } from 'react'
import type { ImageItem } from '../types'
import { X, Copy, Check } from 'lucide-react'

interface ImageDetailModalProps {
  image: ImageItem | null
  onClose: () => void
}

export const ImageDetailModal = ({ image, onClose }: ImageDetailModalProps) => {
  if (!image) return null

  const [copied, setCopied] = useState(false)

  const handleCopyOcr = () => {
    if (image.ocrText) {
      navigator.clipboard.writeText(image.ocrText)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-md flex items-center justify-center p-2 sm:p-4 lg:p-6 animate-in fade-in duration-200">
      <div className="relative w-full max-w-4xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col lg:flex-row max-h-[92vh]">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-3 right-3 z-20 p-2 rounded-full bg-black/50 hover:bg-black/80 text-white backdrop-blur-md border border-white/20 transition-colors"
          aria-label="Close"
        >
          <X className="w-4 h-4" />
        </button>

        {/* Left: Image Canvas */}
        <div className="relative lg:w-3/5 bg-neutral-950 flex flex-col justify-center items-center min-h-[320px] p-4">
          <img
            src={image.uri}
            alt={image.displayName}
            className="max-h-[70vh] w-auto object-contain rounded-lg shadow-lg"
          />
        </div>

        {/* Right: Info Panel */}
        <div className="lg:w-2/5 p-5 sm:p-6 overflow-y-auto space-y-5 border-t lg:border-t-0 lg:border-l border-neutral-200 dark:border-neutral-800">
          
          {/* Header */}
          <div className="border-b border-neutral-200 dark:border-neutral-800 pb-3">
            <h2 className="text-base font-semibold text-neutral-900 dark:text-neutral-100 truncate">
              {image.displayName}
            </h2>
            <p className="text-xs text-neutral-400 mt-0.5">
              {image.width} × {image.height} px • {(image.sizeBytes / (1024 * 1024)).toFixed(1)} MB
            </p>
          </div>

          {/* Albums */}
          {image.albums.length > 0 && (
            <div className="space-y-2">
              <span className="text-xs font-semibold text-neutral-500">
                Albums
              </span>
              <div className="flex flex-wrap gap-1.5">
                {image.albums.map((alb) => (
                  <span
                    key={alb}
                    className="px-2.5 py-1 rounded-lg bg-neutral-100 dark:bg-neutral-800 text-neutral-800 dark:text-neutral-200 text-xs font-medium"
                  >
                    {alb.charAt(0).toUpperCase() + alb.slice(1).toLowerCase()}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Labels */}
          {image.labels.length > 0 && (
            <div className="space-y-2">
              <span className="text-xs font-semibold text-neutral-500">
                Labels
              </span>
              <div className="flex flex-wrap gap-1.5">
                {image.labels.map((lbl) => (
                  <span
                    key={lbl.text}
                    className="px-2.5 py-1 rounded-full bg-neutral-100 dark:bg-neutral-800 text-neutral-800 dark:text-neutral-200 border border-neutral-200 dark:border-neutral-700 text-xs font-medium"
                  >
                    {lbl.text}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* OCR Extracted Text */}
          {image.ocrText && (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-neutral-500">
                  Text Found
                </span>
                <button
                  onClick={handleCopyOcr}
                  className="inline-flex items-center space-x-1 text-xs text-neutral-500 hover:text-black dark:hover:text-white"
                >
                  {copied ? <Check className="w-3 h-3 text-emerald-500" /> : <Copy className="w-3 h-3" />}
                  <span>{copied ? 'Copied' : 'Copy'}</span>
                </button>
              </div>

              <pre className="p-3 rounded-xl bg-neutral-50 dark:bg-neutral-950 border border-neutral-200 dark:border-neutral-800 text-xs font-sans text-neutral-700 dark:text-neutral-300 leading-relaxed max-h-36 overflow-y-auto whitespace-pre-wrap">
                {image.ocrText}
              </pre>
            </div>
          )}

        </div>
      </div>
    </div>
  )
}
