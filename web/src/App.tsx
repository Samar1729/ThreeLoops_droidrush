import { useState, useEffect } from 'react'
import type { AlbumType, ImageItem } from './types'
import { INITIAL_IMAGES, ALBUM_SUMMARIES, MOCK_DUPLICATE_GROUPS } from './data/mockData'
import { Navbar } from './components/Navbar'
import { BottomTabBar, type TabKey } from './components/BottomTabBar'
import { AlbumsView } from './components/AlbumsView'
import { AlbumDetailView } from './components/AlbumDetailView'
import { SearchView } from './components/SearchView'
import { PeopleView } from './components/PeopleView'
import { DuplicatesView } from './components/DuplicatesView'
import { ImageDetailModal } from './components/ImageDetailModal'

export function App() {
  const [darkMode, setDarkMode] = useState(true)
  const [activeTab, setActiveTab] = useState<TabKey>('albums')
  const [selectedAlbum, setSelectedAlbum] = useState<AlbumType | null>(null)
  const [selectedImage, setSelectedImage] = useState<ImageItem | null>(null)
  const [images] = useState<ImageItem[]>(INITIAL_IMAGES)
  const [albums] = useState(ALBUM_SUMMARIES)

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }, [darkMode])

  const handleSelectAlbum = (type: AlbumType) => {
    setSelectedAlbum(type)
  }

  const handleBackToAlbums = () => {
    setSelectedAlbum(null)
  }

  const handleTabChange = (tab: TabKey) => {
    setActiveTab(tab)
    if (tab !== 'albums') {
      setSelectedAlbum(null)
    }
  }

  return (
    <div className="min-h-screen bg-white dark:bg-black text-neutral-900 dark:text-neutral-100 transition-colors font-sans antialiased selection:bg-neutral-200 dark:selection:bg-neutral-800">
      
      {/* Top Navigation */}
      <Navbar
        darkMode={darkMode}
        setDarkMode={setDarkMode}
      />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8 pb-32">
        {activeTab === 'albums' && (
          selectedAlbum ? (
            <AlbumDetailView
              albumType={selectedAlbum}
              images={images}
              onBack={handleBackToAlbums}
              onSelectImage={(img) => setSelectedImage(img)}
            />
          ) : (
            <AlbumsView
              albums={albums}
              onSelectAlbum={handleSelectAlbum}
            />
          )
        )}

        {activeTab === 'search' && (
          <SearchView
            images={images}
            onSelectImage={(img) => setSelectedImage(img)}
          />
        )}

        {activeTab === 'people' && (
          <PeopleView
            images={images}
            onSelectImage={(img) => setSelectedImage(img)}
          />
        )}

        {activeTab === 'duplicates' && (
          <DuplicatesView
            groups={MOCK_DUPLICATE_GROUPS}
            images={images}
            onSelectImage={(img) => setSelectedImage(img)}
          />
        )}
      </main>

      {/* Floating Bottom Bar */}
      <BottomTabBar
        activeTab={activeTab}
        onSelectTab={handleTabChange}
      />

      {/* Photo Inspector Modal */}
      <ImageDetailModal
        image={selectedImage}
        onClose={() => setSelectedImage(null)}
      />

    </div>
  )
}

export default App
