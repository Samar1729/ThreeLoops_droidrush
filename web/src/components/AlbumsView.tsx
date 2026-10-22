import type { AlbumSummary, AlbumType } from '../types'
import { Folder, FileText, Smartphone, Utensils, Users, ChevronRight } from 'lucide-react'

interface AlbumsViewProps {
  albums: AlbumSummary[]
  onSelectAlbum: (type: AlbumType) => void
}

export const AlbumsView = ({ albums, onSelectAlbum }: AlbumsViewProps) => {
  const getAlbumIcon = (type: AlbumType) => {
    switch (type) {
      case 'RECEIPTS':
        return FileText
      case 'SCREENSHOTS':
        return Smartphone
      case 'DOCUMENTS':
        return Folder
      case 'FOOD':
        return Utensils
      case 'PEOPLE':
        return Users
      default:
        return Folder
    }
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Title */}
      <div className="border-b border-neutral-200 dark:border-neutral-800 pb-4">
        <h1 className="text-3xl sm:text-4xl font-semibold tracking-tight text-neutral-900 dark:text-neutral-100">
          Albums
        </h1>
        <p className="mt-1 text-sm text-neutral-500 dark:text-neutral-400">
          {albums.length} Albums
        </p>
      </div>

      {/* Album Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {albums.map((album) => {
          const Icon = getAlbumIcon(album.type)
          return (
            <div
              key={album.type}
              onClick={() => onSelectAlbum(album.type)}
              className="group relative cursor-pointer rounded-2xl bg-white dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 hover:border-neutral-400 dark:hover:border-neutral-600 transition-all duration-200 overflow-hidden shadow-sm hover:shadow-md flex flex-col justify-between"
            >
              {/* Cover Image */}
              <div className="relative aspect-4/3 w-full overflow-hidden bg-neutral-100 dark:bg-neutral-800">
                <img
                  src={album.coverUri}
                  alt={album.title}
                  className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                  loading="lazy"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent" />
                
                {/* Count Badge on Image */}
                <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between text-white">
                  <div className="flex items-center space-x-1.5 px-2.5 py-1 rounded-full backdrop-blur-md bg-black/40 text-xs font-medium border border-white/20">
                    <Icon className="w-3.5 h-3.5" />
                    <span>{album.title}</span>
                  </div>
                  <span className="text-xs font-mono font-medium px-2 py-0.5 rounded-full bg-white/20 backdrop-blur-md">
                    {album.count}
                  </span>
                </div>
              </div>

              {/* Info */}
              <div className="p-4 flex items-center justify-between">
                <div>
                  <h2 className="text-base font-semibold text-neutral-900 dark:text-neutral-100">
                    {album.title}
                  </h2>
                  <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-0.5">
                    {album.description}
                  </p>
                </div>
                <ChevronRight className="w-4 h-4 text-neutral-400 group-hover:translate-x-0.5 transition-transform" />
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
