import { useEffect, useState } from 'react'
import { format } from 'date-fns'
import toast from 'react-hot-toast'
import { accounts as accountsApi } from '../services/api'
import { PlusIcon, TrashIcon } from '@heroicons/react/24/outline'

const PLATFORM_ICONS = {
  instagram: '📷',
  tiktok:    '🎵',
  linkedin:  '💼',
  twitter:   '🐦',
  youtube:   '▶️',
}

export default function Accounts() {
  const [list, setList] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ platform: 'instagram', lateAccountId: '', accountName: '' })
  const [saving, setSaving] = useState(false)

  const load = () => {
    accountsApi.list()
      .then(({ data }) => setList(data))
      .catch(() => toast.error('Failed to load accounts'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const connect = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      await accountsApi.connect(form)
      toast.success('Account connected!')
      setShowForm(false)
      setForm({ platform: 'instagram', lateAccountId: '', accountName: '' })
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to connect account')
    } finally {
      setSaving(false)
    }
  }

  const remove = async (id) => {
    if (!confirm('Remove this account?')) return
    try {
      await accountsApi.remove(id)
      setList(l => l.filter(a => a.id !== id))
      toast.success('Account removed')
    } catch (err) {
      toast.error('Failed to remove account')
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Social Accounts</h1>
          <p className="text-sm text-gray-500 mt-0.5">Connect your Late.dev accounts</p>
        </div>
        <button onClick={() => setShowForm(v => !v)} className="btn-primary flex items-center gap-1.5">
          <PlusIcon className="w-4 h-4" />
          Connect account
        </button>
      </div>

      {/* Connect form */}
      {showForm && (
        <div className="card mb-6">
          <h2 className="font-semibold text-gray-800 mb-4">Connect a new account</h2>
          <form onSubmit={connect} className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Platform</label>
              <select
                value={form.platform}
                onChange={e => setForm(f => ({ ...f, platform: e.target.value }))}
                className="input"
              >
                {Object.keys(PLATFORM_ICONS).map(p => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Late Account ID</label>
              <input
                type="text"
                required
                value={form.lateAccountId}
                onChange={e => setForm(f => ({ ...f, lateAccountId: e.target.value }))}
                placeholder="late_acc_xxxxxxxxx"
                className="input"
              />
              <p className="text-xs text-gray-400 mt-1">
                Find this in your getlate.dev dashboard under connected accounts.
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Display name (optional)</label>
              <input
                type="text"
                value={form.accountName}
                onChange={e => setForm(f => ({ ...f, accountName: e.target.value }))}
                placeholder="@myhandle"
                className="input"
              />
            </div>
            <div className="flex gap-2 justify-end pt-2">
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
              <button type="submit" disabled={saving} className="btn-primary">
                {saving ? 'Connecting…' : 'Connect'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Accounts list */}
      {loading ? (
        <div className="flex justify-center py-10">
          <div className="animate-spin rounded-full h-7 w-7 border-b-2 border-brand-500" />
        </div>
      ) : list.length === 0 ? (
        <div className="card text-center py-12 text-gray-400">
          <p className="text-lg font-medium">No accounts connected</p>
          <p className="text-sm mt-1">Connect your Late.dev accounts to start scheduling</p>
        </div>
      ) : (
        <div className="space-y-3">
          {list.map(acc => (
            <div key={acc.id} className="card flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="text-2xl">{PLATFORM_ICONS[acc.platform] || '📱'}</span>
                <div>
                  <p className="font-medium text-gray-900">
                    {acc.accountName || acc.platform}
                  </p>
                  <p className="text-xs text-gray-400">{acc.lateAccountId}</p>
                  <p className="text-xs text-gray-400 mt-0.5">
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
          ))}
        </div>
      )}
    </div>
  )
}
