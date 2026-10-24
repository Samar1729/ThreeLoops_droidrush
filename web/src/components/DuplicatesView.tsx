import type { ImageItem, DuplicateGroup } from '../types'
import { Check, Trash2 } from 'lucide-react'

interface DuplicatesViewProps {
  groups: DuplicateGroup[]
  images: ImageItem[]
  onSelectImage: (image: ImageItem) => void
}

export const DuplicatesView = ({
  groups,
  images,
  onSelectImage,
}: DuplicatesViewProps) => {
  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-semibold tracking-tight text-neutral-900 dark:text-neutral-100">
          Duplicates
        </h1>
        <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-1">
          Review similar and burst shots to free up storage
        </p>
      </div>

      {/* Groups List */}
      <div className="space-y-6">
        {groups.map((group) => {
          const groupImages = group.imageIds
            .map((id) => images.find((img) => img.id === id))
            .filter((img): img is ImageItem => Boolean(img))

          const reclaimableBytes = groupImages
            .filter((img) => img.id !== group.bestId)
            .reduce((sum, img) => sum + img.sizeBytes, 0)
          const reclaimableMb = (reclaimableBytes / (1024 * 1024)).toFixed(1)

          return (
            <div
              key={group.id}
              className="p-5 rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 shadow-sm space-y-4"
            >
              {/* Group Header */}
              <div className="flex items-center justify-between border-b border-neutral-100 dark:border-neutral-800 pb-3">
                <span className="text-sm font-semibold text-neutral-900 dark:text-neutral-100">
                  {groupImages.length} Similar Photos
                </span>
                <span className="text-xs text-neutral-500">
                  Reclaimable: <strong className="text-neutral-900 dark:text-neutral-100">{reclaimableMb} MB</strong>
                </span>
              </div>

              {/* Side-by-Side Photos */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {groupImages.map((image) => {
                  const isBest = image.id === group.bestId

                  return (
                    <div
                      key={image.id}
                      onClick={() => onSelectImage(image)}
                      className={`relative cursor-pointer rounded-xl overflow-hidden border transition-all ${
                        isBest
                          ? 'border-neutral-900 dark:border-white ring-2 ring-neutral-400/20'
                          : 'border-neutral-200 dark:border-neutral-800'
                      }`}
                    >
                      <div className="relative aspect-3/4 w-full overflow-hidden">
                        <img
                          src={image.uri}
                          alt={image.displayName}
                          className="w-full h-full object-cover"
                          loading="lazy"
                        />

                        {isBest && (
                          <div className="absolute top-2.5 left-2.5 px-2 py-0.5 rounded-full bg-black/80 dark:bg-white/90 text-white dark:text-black text-[10px] font-semibold flex items-center space-x-1 shadow-md">
                            <Check className="w-3 h-3" />
                            <span>BEST SHOT</span>
                          </div>
                        )}
                      </div>

                      <div className="p-2.5 text-xs">
                        <p className="text-[11px] font-medium text-neutral-900 dark:text-neutral-100 truncate">
                          {image.displayName}
                        </p>
                      </div>
                    </div>
                  )
                })}
              </div>

              {/* Action Button */}
              <div className="pt-1 flex items-center justify-end">
                <button
                  onClick={() => alert("Keep Best Shot: Keeps the sharpest photo and purges duplicates.")}
                  className="px-4 py-2 rounded-xl bg-neutral-900 hover:bg-black text-white dark:bg-neutral-100 dark:hover:bg-white dark:text-black text-xs font-medium transition-colors flex items-center space-x-1.5 shadow-sm"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Keep Best Shot ({reclaimableMb} MB)</span>
                </button>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
