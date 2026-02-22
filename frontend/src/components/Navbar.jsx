import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { CalendarDaysIcon, PlusCircleIcon, LinkIcon, ArrowRightStartOnRectangleIcon } from '@heroicons/react/24/outline'

const NAV = [
  { to: '/',         label: 'Dashboard',  Icon: CalendarDaysIcon },
  { to: '/posts/new',label: 'New Post',   Icon: PlusCircleIcon },
  { to: '/accounts', label: 'Accounts',   Icon: LinkIcon },
]

export default function Navbar() {
  const { user, logout } = useAuth()
  const { pathname } = useLocation()

  return (
    <nav className="fixed top-0 inset-x-0 h-16 bg-white border-b border-gray-200 flex items-center px-6 z-50">
      <span className="text-lg font-bold text-brand-600 mr-8">SkedMe</span>

      <div className="flex items-center gap-1 flex-1">
        {NAV.map(({ to, label, Icon }) => (
          <Link
            key={to}
            to={to}
            className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors
              ${pathname === to
                ? 'bg-brand-50 text-brand-600'
                : 'text-gray-600 hover:bg-gray-100'}`}
          >
            <Icon className="w-4 h-4" />
            {label}
          </Link>
        ))}
      </div>

      <div className="flex items-center gap-3">
        <span className="text-sm text-gray-500">{user?.email}</span>
        <button
          onClick={logout}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-red-600 transition-colors"
        >
          <ArrowRightStartOnRectangleIcon className="w-4 h-4" />
          Logout
        </button>
      </div>
    </nav>
  )
}
