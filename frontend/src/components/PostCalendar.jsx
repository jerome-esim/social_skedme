import { useState } from 'react'
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameDay, addMonths, subMonths } from 'date-fns'
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/outline'
import { Link } from 'react-router-dom'

const STATUS_DOT = {
  draft:     'bg-blue-400',
  pending:   'bg-orange-400',
  scheduled: 'bg-yellow-400',
  published: 'bg-green-400',
  failed:    'bg-red-400',
}

export default function PostCalendar({ posts }) {
  const [current, setCurrent] = useState(new Date())

  const days = eachDayOfInterval({
    start: startOfMonth(current),
    end:   endOfMonth(current),
  })

  const startDow = startOfMonth(current).getDay() // 0=Sun

  const postsForDay = (day) =>
    posts.filter(p => isSameDay(new Date(p.scheduledAt), day))

  return (
    <div className="card">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold">{format(current, 'MMMM yyyy')}</h2>
        <div className="flex gap-1">
          <button onClick={() => setCurrent(subMonths(current, 1))} className="p-1.5 rounded hover:bg-gray-100">
            <ChevronLeftIcon className="w-4 h-4" />
          </button>
          <button onClick={() => setCurrent(addMonths(current, 1))} className="p-1.5 rounded hover:bg-gray-100">
            <ChevronRightIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Day names */}
      <div className="grid grid-cols-7 mb-1">
        {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => (
          <div key={d} className="text-center text-xs font-medium text-gray-400 py-1">{d}</div>
        ))}
      </div>

      {/* Days grid */}
      <div className="grid grid-cols-7 gap-px bg-gray-100 rounded-lg overflow-hidden">
        {/* Empty cells before month start */}
        {Array.from({ length: startDow }).map((_, i) => (
          <div key={`empty-${i}`} className="bg-white min-h-[80px]" />
        ))}

        {days.map(day => {
          const dayPosts = postsForDay(day)
          const isToday = isSameDay(day, new Date())

          return (
            <div
              key={day.toISOString()}
              className="bg-white min-h-[80px] p-1.5"
            >
              <span className={`text-xs font-medium inline-flex items-center justify-center w-6 h-6 rounded-full
                ${isToday ? 'bg-brand-500 text-white' : 'text-gray-700'}`}>
                {format(day, 'd')}
              </span>

              <div className="mt-1 space-y-0.5">
                {dayPosts.slice(0, 3).map(post => (
                  <Link
                    key={post.id}
                    to={`/posts/${post.id}`}
                    className="flex items-center gap-1 text-xs truncate hover:underline"
                    title={post.title || post.caption}
                  >
                    <span className={`shrink-0 w-1.5 h-1.5 rounded-full ${STATUS_DOT[post.status] || STATUS_DOT.draft}`} />
                    <span className="truncate text-gray-700">{post.title || 'Post'}</span>
                  </Link>
                ))}
                {dayPosts.length > 3 && (
                  <p className="text-xs text-gray-400">+{dayPosts.length - 3} more</p>
                )}
              </div>
            </div>
          )
        })}
      </div>

      {/* Legend */}
      <div className="mt-4 flex flex-wrap gap-3 text-xs text-gray-500">
        {Object.entries(STATUS_DOT).map(([status, cls]) => (
          <span key={status} className="flex items-center gap-1">
            <span className={`w-2 h-2 rounded-full ${cls}`} />
            {status}
          </span>
        ))}
      </div>
    </div>
  )
}
