const PLATFORMS = [
  { id: 'instagram', label: 'Instagram', icon: '📷' },
  { id: 'tiktok',    label: 'TikTok',    icon: '🎵' },
  { id: 'linkedin',  label: 'LinkedIn',  icon: '💼' },
  { id: 'twitter',   label: 'Twitter/X', icon: '🐦' },
  { id: 'youtube',   label: 'YouTube',   icon: '▶️' },
]

export default function PlatformSelector({ selected, onChange }) {
  const toggle = (id) => {
    if (selected.includes(id)) {
      onChange(selected.filter(p => p !== id))
    } else {
      onChange([...selected, id])
    }
  }

  return (
    <div className="flex flex-wrap gap-2">
      {PLATFORMS.map(({ id, label, icon }) => {
        const active = selected.includes(id)
        return (
          <button
            key={id}
            type="button"
            onClick={() => toggle(id)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-sm font-medium transition-colors
              ${active
                ? 'border-brand-500 bg-brand-50 text-brand-700'
                : 'border-gray-300 bg-white text-gray-600 hover:border-gray-400'}`}
          >
            <span>{icon}</span>
            {label}
          </button>
        )
      })}
    </div>
  )
}
