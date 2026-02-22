import { Link } from 'react-router-dom'
import { format } from 'date-fns'

const STATUS_STYLES = {
  draft:     'bg-blue-100 text-blue-700',
  pending:   'bg-orange-100 text-orange-700',
  scheduled: 'bg-yellow-100 text-yellow-700',
  published: 'bg-green-100 text-green-700',
  failed:    'bg-red-100 text-red-700',
}

const PLATFORM_ICONS = {
  instagram: '📷',
  tiktok:    '🎵',
  linkedin:  '💼',
  twitter:   '🐦',
  youtube:   '▶️',
}

export default function PostCard({ post }) {
  const status = post.status || 'draft'
  const scheduled = new Date(post.scheduledAt)

  return (
    <Link to={`/posts/${post.id}`} className="block card hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <h3 className="font-semibold text-gray-900 truncate">
            {post.title || 'Untitled post'}
          </h3>
          <p className="text-sm text-gray-500 mt-0.5 line-clamp-2">
            {post.caption || '—'}
          </p>
        </div>
        <span className={`shrink-0 text-xs font-medium px-2.5 py-1 rounded-full ${STATUS_STYLES[status] || STATUS_STYLES.draft}`}>
          {status}
        </span>
      </div>

      <div className="mt-4 flex items-center justify-between text-sm text-gray-500">
        <div className="flex gap-1">
          {(post.platforms || []).map(p => (
            <span key={p} title={p}>{PLATFORM_ICONS[p] || p}</span>
          ))}
        </div>
        <span>{format(scheduled, 'MMM d, yyyy HH:mm')}</span>
      </div>
    </Link>
  )
}
