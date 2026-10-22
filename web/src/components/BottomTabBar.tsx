import { FolderKanban, Search, Users, Copy } from 'lucide-react'

export type TabKey = 'albums' | 'search' | 'people' | 'duplicates'

interface BottomTabBarProps {
  activeTab: TabKey
  onSelectTab: (tab: TabKey) => void
}

export const BottomTabBar = ({ activeTab, onSelectTab }: BottomTabBarProps) => {
  const tabs = [
    { key: 'albums' as TabKey, label: 'Albums', icon: FolderKanban },
    { key: 'search' as TabKey, label: 'Search', icon: Search },
    { key: 'people' as TabKey, label: 'People', icon: Users },
    { key: 'duplicates' as TabKey, label: 'Duplicates', icon: Copy },
  ]

  return (
    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-40 w-auto max-w-[92vw]">
      <nav className="flex items-center space-x-1 px-2 py-1.5 rounded-full backdrop-blur-2xl bg-white/85 dark:bg-neutral-900/85 border border-neutral-200/80 dark:border-neutral-800/80 shadow-lg shadow-black/5 dark:shadow-black/40">
        {tabs.map((tab) => {
          const Icon = tab.icon
          const isActive = activeTab === tab.key
          return (
            <button
              key={tab.key}
              onClick={() => onSelectTab(tab.key)}
              className={`relative flex items-center space-x-2 px-4 py-2 rounded-full text-xs font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-black text-white dark:bg-white dark:text-black shadow-sm'
                  : 'text-neutral-600 dark:text-neutral-400 hover:text-black dark:hover:text-white hover:bg-neutral-100 dark:hover:bg-neutral-800'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          )
        })}
      </nav>
    </div>
  )
}
