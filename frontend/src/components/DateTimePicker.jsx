const TIMEZONES = [
  'Europe/Paris',
  'Europe/London',
  'America/New_York',
  'America/Los_Angeles',
  'America/Chicago',
  'Asia/Tokyo',
  'Asia/Singapore',
  'Australia/Sydney',
  'UTC',
]

export default function DateTimePicker({ scheduledAt, timezone, onScheduledAtChange, onTimezoneChange }) {
  // scheduledAt is a "datetime-local" string: "YYYY-MM-DDTHH:mm"
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Date & time</label>
        <input
          type="datetime-local"
          value={scheduledAt}
          onChange={e => onScheduledAtChange(e.target.value)}
          min={new Date().toISOString().slice(0, 16)}
          className="input"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Timezone</label>
        <select
          value={timezone}
          onChange={e => onTimezoneChange(e.target.value)}
          className="input"
        >
          {TIMEZONES.map(tz => (
            <option key={tz} value={tz}>{tz}</option>
          ))}
        </select>
      </div>
    </div>
  )
}
