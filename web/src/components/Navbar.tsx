import { Moon, Sun } from 'lucide-react'

interface NavbarProps {
  darkMode: boolean
  setDarkMode: (val: boolean) => void
}

export const Navbar = ({ darkMode, setDarkMode }: NavbarProps) => {
  return (
    <header className="sticky top-0 z-40 backdrop-blur-xl bg-white/80 dark:bg-black/80 border-b border-neutral-200 dark:border-neutral-800 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Left: App Identity */}
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 rounded-lg bg-black dark:bg-white flex items-center justify-center text-white dark:text-black font-semibold tracking-tighter text-sm shadow-sm">
            MA
          </div>
          <div>
            <span className="font-semibold text-base tracking-tight text-neutral-900 dark:text-neutral-100">
              Mirror Archive
            </span>
          </div>
        </div>

        {/* Right: Theme Toggle */}
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setDarkMode(!darkMode)}
            aria-label="Toggle theme"
            className="p-2 rounded-lg text-neutral-600 dark:text-neutral-400 hover:bg-neutral-100 dark:hover:bg-neutral-900 border border-transparent hover:border-neutral-200 dark:hover:border-neutral-800 transition-colors"
          >
            {darkMode ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>
        </div>

      </div>
    </header>
  )
}
