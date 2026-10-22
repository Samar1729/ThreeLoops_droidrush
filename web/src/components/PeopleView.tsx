import type { ImageItem } from '../types'

interface PeopleViewProps {
  images: ImageItem[]
  onSelectImage: (image: ImageItem) => void
}

export const PeopleView = ({ images, onSelectImage }: PeopleViewProps) => {
  const peoplePhotos = images.filter((img) => img.faceCount > 0)

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Title */}
      <div>
        <h1 className="text-3xl font-semibold tracking-tight text-neutral-900 dark:text-neutral-100">
          People
        </h1>
        <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-1">
          {peoplePhotos.length} Photos
        </p>
      </div>

      {/* Photo Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
        {peoplePhotos.map((image) => (
          <div
            key={image.id}
            onClick={() => onSelectImage(image)}
            className="group cursor-pointer aspect-square rounded-2xl overflow-hidden bg-neutral-100 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-800 relative shadow-sm"
          >
            <img
              src={image.uri}
              alt={image.displayName}
              className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
              loading="lazy"
            />
            <div className="absolute bottom-2 left-2 px-2 py-0.5 rounded-full backdrop-blur-md bg-black/60 text-white text-[10px] font-medium">
              {image.faceCount} face{image.faceCount > 1 ? 's' : ''}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
