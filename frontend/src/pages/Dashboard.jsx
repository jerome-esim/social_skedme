import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PlusIcon } from '@heroicons/react/24/solid'
import toast from 'react-hot-toast'
import { posts as postsApi } from '../services/api'
import PostCalendar from '../components/PostCalendar'
import PostCard from '../components/PostCard'

const STATUS_COUNTS = ['pending', 'scheduled', 'published', 'failed']

export default function Dashboard() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [view, setView] = useState('calendar') // 'calendar' | 'list'

  useEffect(() => {
    postsApi.list().then(({ data }) => {
      setPosts(data.content || [])
    }).catch(() => {
      toast.error('Failed to load posts')
    }).finally(() => setLoading(false))
  }, [])

  const counts = STATUS_COUNTS.reduce((acc, s) => {
    acc[s] = posts.filter(p => p.status === s).length
    return acc
  }, {})

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* Top bar */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-500 text-sm mt-0.5">{posts.length} posts total</p>
        </div>
        <Link to="/posts/new" className="btn-primary flex items-center gap-1.5">
          <PlusIcon className="w-4 h-4" />
          New post
        </Link>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
        {[
          { label: 'Pending',   value: counts.pending,   color: 'text-orange-600 bg-orange-50' },
          { label: 'Scheduled', value: counts.scheduled, color: 'text-yellow-600 bg-yellow-50' },
          { label: 'Published', value: counts.published, color: 'text-green-600 bg-green-50' },
          { label: 'Failed',    value: counts.failed,    color: 'text-red-600 bg-red-50' },
        ].map(({ label, value, color }) => (
          <div key={label} className={`rounded-xl p-4 ${color}`}>
            <p className="text-2xl font-bold">{value}</p>
            <p className="text-sm font-medium opacity-80">{label}</p>
          </div>
        ))}
      </div>

      {/* View toggle */}
      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setView('calendar')}
          className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors
            ${view === 'calendar' ? 'bg-brand-500 text-white' : 'bg-white border border-gray-300 text-gray-600 hover:bg-gray-50'}`}
        >
          Calendar
        </button>
        <button
          onClick={() => setView('list')}
          className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors
            ${view === 'list' ? 'bg-brand-500 text-white' : 'bg-white border border-gray-300 text-gray-600 hover:bg-gray-50'}`}
        >
          List
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-500" />
        </div>
      ) : view === 'calendar' ? (
        <PostCalendar posts={posts} />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {posts.length === 0 && (
            <div className="col-span-3 text-center py-16 text-gray-400">
              <p className="text-lg font-medium">No posts yet</p>
              <p className="text-sm mt-1">Create your first scheduled post to get started</p>
              <Link to="/posts/new" className="btn-primary inline-flex items-center gap-1.5 mt-4">
                <PlusIcon className="w-4 h-4" /> New post
              </Link>
            </div>
          )}
          {posts.map(post => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      )}
    </div>
  )
}
