import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

export default function OAuthCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [status, setStatus] = useState('processing') // processing | success | error

  useEffect(() => {
    const error = searchParams.get('error')
    if (error) {
      setStatus('error')
      setTimeout(() => navigate('/accounts'), 3000)
      return
    }
    // Success — Late.dev has connected the account
    setStatus('success')
    setTimeout(() => navigate('/accounts'), 2000)
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 max-w-sm w-full text-center">
        {status === 'processing' && (
          <>
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-brand-500 mx-auto mb-4" />
            <p className="text-gray-600">Finalizing connection…</p>
          </>
        )}
        {status === 'success' && (
          <>
            <div className="text-5xl mb-4">✓</div>
            <h2 className="text-lg font-semibold text-gray-900 mb-1">Account connected!</h2>
            <p className="text-sm text-gray-500">Redirecting you back…</p>
          </>
        )}
        {status === 'error' && (
          <>
            <div className="text-5xl mb-4">✕</div>
            <h2 className="text-lg font-semibold text-gray-900 mb-1">Authorization failed</h2>
            <p className="text-sm text-gray-500">
              {searchParams.get('error_description') || 'Something went wrong. Please try again.'}
            </p>
            <p className="text-xs text-gray-400 mt-2">Redirecting you back…</p>
          </>
        )}
      </div>
    </div>
  )
}
