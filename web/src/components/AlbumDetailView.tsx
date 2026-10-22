import type { AlbumType, ImageItem } from '../types'
import { ArrowLeft, SlidersHorizontal, Eye } from 'lucide-react'

interface AlbumDetailViewProps {
  albumType: AlbumType
  images: ImageItem[]
  onBack: () => void
  onSelectImage: (image: ImageItem) => void
}

export const AlbumDetailView = ({
  albumType,
  images,
  onBack,
  onSelectImage,
}: AlbumDetailViewProps) => {
  const filteredImages = images.filter((img) => img.albums.includes(albumType))

  const getAlbumTitle = (type: AlbumType) => {
    switch (type) {
      case 'RECEIPTS':
        return 'Receipts'
      case 'SCREENSHOTS':
        return 'Screenshots'
      case 'DOCUMENTS':
        return 'Documents'
      case 'FOOD':
        return 'Food'
      case 'PEOPLE':
        return 'People'
      default:
        return type
    }
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Navigation & Header */}
      <div>
        <button
          onClick={onBack}
          className="inline-flex items-center space-x-1.5 text-xs font-medium text-neutral-500 dark:text-neutral-400 hover:text-black dark:hover:text-white mb-3 transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Albums</span>
        </button>

        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-semibold tracking-tight text-neutral-900 dark:text-neutral-100">
              {getAlbumTitle(albumType)}
            </h1>
            <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-1">
              {filteredImages.length} Photos
            </p>
          </div>
        </div>
      </div>

      {/* Photos Grid */}
      {filteredImages.length === 0 ? (
        <div className="text-center py-20 border border-dashed border-neutral-200 dark:border-neutral-800 rounded-2xl">
          <SlidersHorizontal className="w-8 h-8 text-neutral-400 mx-auto mb-2" />
          <p className="text-sm font-medium text-neutral-900 dark:text-neutral-100">
            No photos in this album
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {filteredImages.map((image) => (
            <div
              key={image.id}
              onClick={() => onSelectImage(image)}
              className="group relative cursor-pointer aspect-square rounded-xl overflow-hidden bg-neutral-100 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-800 shadow-sm hover:shadow-md transition-all duration-200"
            >
              <img
                src={image.uri}
                alt={image.displayName}
                className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                loading="lazy"
              />

              <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-end justify-between p-3 text-white">
                <span className="text-xs truncate">{image.displayName}</span>
                <Eye className="w-3.5 h-3.5 shrink-0" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
