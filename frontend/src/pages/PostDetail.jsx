import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { format } from 'date-fns'
import toast from 'react-hot-toast'
import { posts as postsApi } from '../services/api'
import { ArrowLeftIcon, TrashIcon } from '@heroicons/react/24/outline'

const STATUS_STYLES = {
  draft:     'bg-blue-100 text-blue-700 border-blue-200',
  pending:   'bg-orange-100 text-orange-700 border-orange-200',
  scheduled: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  published: 'bg-green-100 text-green-700 border-green-200',
  failed:    'bg-red-100 text-red-700 border-red-200',
}

const STATUS_DESC = {
  draft:     'Draft — not yet submitted to the scheduler',
  pending:   'Pending — waiting for the outbox processor to call Late API',
  scheduled: 'Scheduled — Late API accepted the post and will publish at the scheduled time',
  published: 'Published — post has been published on the social networks',
  failed:    'Failed — an error occurred during scheduling or publication',
}

export default function PostDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [post, setPost] = useState(null)
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    postsApi.get(id)
      .then(({ data }) => setPost(data))
      .catch(() => { toast.error('Post not found'); navigate('/') })
      .finally(() => setLoading(false))
  }, [id])

  // Poll every 5s while status is pending/processing
  useEffect(() => {
    if (!post || ['published', 'failed', 'draft'].includes(post.status)) return
    const timer = setInterval(() => {
      postsApi.get(id).then(({ data }) => setPost(data)).catch(() => {})
    }, 5000)
    return () => clearInterval(timer)
  }, [post?.status])

  const handleDelete = async () => {
    if (!confirm('Delete this post?')) return
    setDeleting(true)
    try {
      await postsApi.remove(id)
      toast.success('Post deleted')
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cannot delete this post')
      setDeleting(false)
    }
  }

  if (loading) return (
    <div className="flex justify-center py-20">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-500" />
    </div>
  )

  if (!post) return null

  const status = post.status || 'draft'

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <button onClick={() => navigate('/')} className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6">
        <ArrowLeftIcon className="w-4 h-4" /> Back
      </button>

      <div className="space-y-4">
        {/* Header */}
        <div className="card">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-xl font-bold text-gray-900">{post.title || 'Untitled post'}</h1>
              <p className="text-sm text-gray-500 mt-0.5">
                Scheduled for {format(new Date(post.scheduledAt), 'PPP p')} UTC
              </p>
            </div>
            <span className={`shrink-0 text-sm font-semibold px-3 py-1 rounded-full border ${STATUS_STYLES[status]}`}>
              {status}
            </span>
          </div>

          <p className="text-sm text-gray-500 mt-3">{STATUS_DESC[status]}</p>

          {post.latePostId && (
            <p className="text-xs text-gray-400 mt-2">Late post ID: {post.latePostId}</p>
          )}
          {post.errorMessage && (
            <div className="mt-3 p-3 rounded-lg bg-red-50 text-sm text-red-700 border border-red-200">
              {post.errorMessage}
            </div>
          )}
        </div>

        {/* Caption */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-2">Caption</h2>
          <p className="text-gray-700 whitespace-pre-wrap">{post.caption || '—'}</p>
          {post.hashtags && (
            <p className="text-brand-600 text-sm mt-2">{post.hashtags}</p>
          )}
        </div>

        {/* Video */}
        {post.videoUrl && (
          <div className="card">
            <h2 className="font-semibold text-gray-800 mb-2">Video</h2>
            <video src={post.videoUrl} controls className="w-full rounded-lg max-h-64 object-contain bg-black" />
            <p className="text-xs text-gray-400 mt-1 truncate">{post.videoFilename || post.videoUrl}</p>
          </div>
        )}

        {/* Platforms */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-2">Platforms</h2>
          <div className="flex flex-wrap gap-2">
            {(post.platforms || []).map(p => (
              <span key={p} className="px-3 py-1 bg-gray-100 rounded-full text-sm text-gray-700">{p}</span>
            ))}
          </div>
        </div>

        {/* Meta */}
        <div className="card text-sm text-gray-500 space-y-1">
          <p>Created: {format(new Date(post.createdAt), 'PPP p')}</p>
          <p>Updated: {format(new Date(post.updatedAt), 'PPP p')}</p>
          <p>Timezone: {post.timezone}</p>
        </div>

        {/* Actions */}
        {!['scheduled', 'published'].includes(status) && (
          <div className="flex justify-end">
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="flex items-center gap-1.5 text-sm text-red-600 hover:text-red-800 disabled:opacity-50"
            >
              <TrashIcon className="w-4 h-4" />
              {deleting ? 'Deleting…' : 'Delete post'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
