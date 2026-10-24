import { useState, useMemo } from 'react'
import type { ImageItem, SearchMode } from '../types'
import { Search as SearchIcon, X, ArrowRight } from 'lucide-react'
import { SearchEngine } from '../services/searchEngine'

interface SearchViewProps {
  images: ImageItem[]
  onSelectImage: (image: ImageItem) => void
}

export const SearchView = ({ images, onSelectImage }: SearchViewProps) => {
  const [query, setQuery] = useState('')
  const [mode, setMode] = useState<SearchMode>('TAGS')

  const suggestions = [
    'receipt',
    'coffee',
    'pet',
    'document',
    'screenshot',
    'person',
    'food',
    'invoice',
    'burger',
    'apple'
  ]

  const results = useMemo(() => {
    if (!query.trim()) return []
    if (mode === 'TAGS') {
      return SearchEngine.searchByTag(images, query)
    } else {
      return SearchEngine.searchByText(images, query)
    }
  }, [images, query, mode])

  const clearQuery = () => {
    setQuery('')
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Title */}
      <div>
        <h1 className="text-3xl font-semibold tracking-tight text-neutral-900 dark:text-neutral-100">
          Search
        </h1>
      </div>

      {/* Segmented Picker */}
      <div className="inline-flex p-1 rounded-xl bg-neutral-100 dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800">
        <button
          onClick={() => setMode('TAGS')}
          className={`px-4 py-1.5 rounded-lg text-xs font-medium transition-all ${
            mode === 'TAGS'
              ? 'bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white shadow-sm'
              : 'text-neutral-600 dark:text-neutral-400 hover:text-black dark:hover:text-white'
          }`}
        >
          Tags
        </button>
        <button
          onClick={() => setMode('TEXT')}
          className={`px-4 py-1.5 rounded-lg text-xs font-medium transition-all ${
            mode === 'TEXT'
              ? 'bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white shadow-sm'
              : 'text-neutral-600 dark:text-neutral-400 hover:text-black dark:hover:text-white'
          }`}
        >
          Text in Photos
        </button>
      </div>

      {/* Apple Search Input */}
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-neutral-400">
          <SearchIcon className="w-4 h-4" />
        </div>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={
            mode === 'TAGS'
              ? "Search by tag ('food', 'coffee', 'pet')..."
              : "Search text in photos ('total', 'invoice')..."
          }
          className="w-full pl-10 pr-10 py-3 rounded-xl bg-neutral-100 dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 placeholder-neutral-400 dark:placeholder-neutral-500 text-sm focus:outline-none focus:ring-2 focus:ring-neutral-400 dark:focus:ring-neutral-600 transition-all"
        />
        {query && (
          <button
            onClick={clearQuery}
            className="absolute inset-y-0 right-0 pr-3 flex items-center text-neutral-400 hover:text-neutral-600 dark:hover:text-neutral-200"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Suggestion Chips */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-1 text-xs">
        {suggestions.map((item) => (
          <button
            key={item}
            onClick={() => setQuery(item)}
            className="px-3 py-1 rounded-full bg-neutral-100 dark:bg-neutral-900 hover:bg-neutral-200 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300 border border-neutral-200 dark:border-neutral-800 shrink-0 transition-colors"
          >
            {item}
          </button>
        ))}
      </div>

      {/* Results View */}
      {query.trim() === '' ? (
        <div className="py-20 text-center border border-dashed border-neutral-200 dark:border-neutral-800 rounded-2xl">
          <SearchIcon className="w-8 h-8 text-neutral-400 mx-auto mb-2" />
          <p className="text-sm text-neutral-500 dark:text-neutral-400">
            Search photos by objects, scenes, or text
          </p>
        </div>
      ) : results.length === 0 ? (
        <div className="py-20 text-center border border-dashed border-neutral-200 dark:border-neutral-800 rounded-2xl">
          <p className="text-sm font-medium text-neutral-900 dark:text-neutral-100">
            No results for "{query}"
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {results.map((image: ImageItem) => (
            <div
              key={image.id}
              onClick={() => onSelectImage(image)}
              className="group cursor-pointer aspect-square rounded-xl overflow-hidden bg-neutral-100 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-800 relative"
            >
              <img
                src={image.uri}
                alt={image.displayName}
                className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                loading="lazy"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-between p-3 text-white">
                <span className="text-xs truncate">{image.displayName}</span>
                <ArrowRight className="w-3.5 h-3.5 shrink-0" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
