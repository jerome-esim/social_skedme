import { useEffect, useState } from 'react'
import { format } from 'date-fns'
import toast from 'react-hot-toast'
import { accounts as accountsApi } from '../services/api'
import { TrashIcon } from '@heroicons/react/24/outline'

const PLATFORMS = [
  { id: 'instagram', label: 'Instagram',  icon: '📷' },
  { id: 'tiktok',   label: 'TikTok',     icon: '🎵' },
  { id: 'linkedin', label: 'LinkedIn',   icon: '💼' },
  { id: 'twitter',  label: 'Twitter / X', icon: '🐦' },
  { id: 'youtube',  label: 'YouTube',    icon: '▶️' },
]

export default function Accounts() {
  const [list, setList]           = useState([])
  const [loading, setLoading]     = useState(true)
  // pending = { connectUrl, profileId, platform, accountName }
  const [pending, setPending]     = useState(null)
  const [confirming, setConfirming] = useState(false)

  const load = () => {
    accountsApi.list()
      .then(({ data }) => setList(data))
      .catch(() => toast.error('Failed to load accounts'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const startConnect = async (platform) => {
    try {
      const { data } = await accountsApi.connectUrl({ platform })
      setPending(data)
      window.open(data.connectUrl, '_blank', 'noopener,noreferrer')
    } catch {
      toast.error('Could not start connection. Try again.')
    }
  }

  const confirmConnect = async () => {
    if (!pending) return
    setConfirming(true)
    try {
      await accountsApi.connect({
        platform:      pending.platform,
        lateAccountId: pending.profileId,
        accountName:   pending.accountName || '',
      })
      toast.success('Account connected!')
      setPending(null)
      load()
    } catch {
      toast.error('Failed to save account. Please try again.')
    } finally {
      setConfirming(false)
    }
  }

  const remove = async (id) => {
    if (!confirm('Remove this account?')) return
    try {
      await accountsApi.remove(id)
      setList(l => l.filter(a => a.id !== id))
      toast.success('Account removed')
    } catch {
      toast.error('Failed to remove account')
    }
  }

  const connectedPlatforms = new Set(list.map(a => a.platform))

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Social Accounts</h1>
        <p className="text-sm text-gray-500 mt-0.5">Connect your social media accounts to start scheduling</p>
      </div>

      {/* Platform buttons */}
      <div className="card mb-6">
        <h2 className="font-semibold text-gray-800 mb-4">Connect a new account</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {PLATFORMS.map(p => (
            <button
              key={p.id}
              onClick={() => startConnect(p.id)}
              disabled={!!pending}
              className="flex items-center gap-2 px-4 py-3 rounded-lg border border-gray-200 hover:border-brand-400 hover:bg-brand-50 transition-colors text-sm font-medium text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span className="text-xl">{p.icon}</span>
              {p.label}
              {connectedPlatforms.has(p.id) && (
                <span className="ml-auto w-2 h-2 rounded-full bg-green-400" title="Connected" />
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Authorization confirmation modal */}
      {pending && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-sm w-full p-6">
            <div className="text-center mb-4">
              <span className="text-4xl">
                {PLATFORMS.find(p => p.id === pending.platform)?.icon}
              </span>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 text-center mb-2">
              Authorize {PLATFORMS.find(p => p.id === pending.platform)?.label}
            </h3>
            <p className="text-sm text-gray-500 text-center mb-6">
              A new tab has opened for you to authorize access.
              Once done, click <strong>Confirm</strong> to complete the connection.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setPending(null)}
                className="flex-1 btn-secondary"
              >
                Cancel
              </button>
              <button
                onClick={confirmConnect}
                disabled={confirming}
                className="flex-1 btn-primary"
              >
                {confirming ? 'Saving…' : 'Confirm'}
              </button>
            </div>
            <button
              onClick={() => window.open(pending.connectUrl, '_blank', 'noopener,noreferrer')}
              className="mt-3 w-full text-xs text-center text-brand-500 hover:underline"
            >
              Re-open authorization page
            </button>
          </div>
        </div>
      )}

      {/* Accounts list */}
      {loading ? (
        <div className="flex justify-center py-10">
          <div className="animate-spin rounded-full h-7 w-7 border-b-2 border-brand-500" />
        </div>
      ) : list.length === 0 ? (
        <div className="card text-center py-12 text-gray-400">
          <p className="text-lg font-medium">No accounts connected yet</p>
          <p className="text-sm mt-1">Choose a platform above to get started</p>
        </div>
      ) : (
        <div className="space-y-3">
          {list.map(acc => {
            const platform = PLATFORMS.find(p => p.id === acc.platform)
            return (
              <div key={acc.id} className="card flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <span className="text-2xl">{platform?.icon || '📱'}</span>
                  <div>
                    <p className="font-medium text-gray-900">
                      {acc.accountName || platform?.label || acc.platform}
                    </p>
                    <p className="text-xs text-gray-400">
                      Connected {format(new Date(acc.connectedAt), 'MMM d, yyyy')}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => remove(acc.id)}
                  className="text-gray-400 hover:text-red-600 transition-colors"
                  title="Remove account"
                >
                  <TrashIcon className="w-4 h-4" />
                </button>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
