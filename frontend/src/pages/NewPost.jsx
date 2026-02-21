import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { posts as postsApi } from '../services/api'
import VideoUploader from '../components/VideoUploader'
import PlatformSelector from '../components/PlatformSelector'
import CaptionEditor from '../components/CaptionEditor'
import DateTimePicker from '../components/DateTimePicker'

const defaultScheduledAt = () => {
  const d = new Date(Date.now() + 2 * 60 * 60 * 1000) // +2h
  return d.toISOString().slice(0, 16) // "YYYY-MM-DDTHH:mm"
}

export default function NewPost() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  const [form, setForm] = useState({
    title:       '',
    caption:     '',
    hashtags:    '',
    videoUrl:    '',
    videoFilename: '',
    platforms:   [],
    scheduledAt: defaultScheduledAt(),
    timezone:    'Europe/Paris',
  })

  const set = (key) => (value) => setForm(f => ({ ...f, [key]: value }))

  const onVideoUploaded = (videoUrl, videoFilename) => {
    setForm(f => ({ ...f, videoUrl, videoFilename }))
  }

  const submit = async (e) => {
    e.preventDefault()

    if (!form.videoUrl) {
      toast.error('Please upload a video first')
      return
    }
    if (form.platforms.length === 0) {
      toast.error('Select at least one platform')
      return
    }

    setLoading(true)
    try {
      const payload = {
        ...form,
        // backend expects ISO datetime without timezone offset in body
        scheduledAt: form.scheduledAt + ':00',
      }
      const { data } = await postsApi.create(payload)
      toast.success('Post scheduled!')
      navigate(`/posts/${data.id}`)
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Failed to create post'
      toast.error(String(msg))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Schedule a new post</h1>

      <form onSubmit={submit} className="space-y-6">
        {/* Title */}
        <div className="card">
          <label className="block text-sm font-medium text-gray-700 mb-1">Title (internal)</label>
          <input
            type="text"
            value={form.title}
            onChange={e => set('title')(e.target.value)}
            placeholder="e.g. Set @ Club X"
            className="input"
          />
        </div>

        {/* Video */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-3">Video</h2>
          <VideoUploader onUploaded={onVideoUploaded} />
          {form.videoUrl && (
            <p className="text-xs text-gray-400 mt-2 truncate">{form.videoUrl}</p>
          )}
        </div>

        {/* Caption */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-3">Caption & Hashtags</h2>
          <CaptionEditor
            caption={form.caption}
            hashtags={form.hashtags}
            onCaptionChange={set('caption')}
            onHashtagsChange={set('hashtags')}
          />
        </div>

        {/* Platforms */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-3">Platforms</h2>
          <PlatformSelector selected={form.platforms} onChange={set('platforms')} />
        </div>

        {/* Schedule */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-3">Schedule</h2>
          <DateTimePicker
            scheduledAt={form.scheduledAt}
            timezone={form.timezone}
            onScheduledAtChange={set('scheduledAt')}
            onTimezoneChange={set('timezone')}
          />
        </div>

        {/* Actions */}
        <div className="flex gap-3 justify-end">
          <button type="button" onClick={() => navigate('/')} className="btn-secondary">
            Cancel
          </button>
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Scheduling…' : 'Schedule post'}
          </button>
        </div>
      </form>
    </div>
  )
}
