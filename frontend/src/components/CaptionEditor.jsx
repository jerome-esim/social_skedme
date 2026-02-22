export default function CaptionEditor({ caption, hashtags, onCaptionChange, onHashtagsChange }) {
  const captionLength = caption?.length || 0

  return (
    <div className="space-y-3">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Caption
          <span className="ml-2 font-normal text-gray-400">({captionLength}/2200)</span>
        </label>
        <textarea
          value={caption}
          onChange={e => onCaptionChange(e.target.value)}
          rows={4}
          maxLength={2200}
          placeholder="Write your caption here…"
          className="input resize-none"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Hashtags</label>
        <input
          type="text"
          value={hashtags}
          onChange={e => onHashtagsChange(e.target.value)}
          placeholder="#house #techno #dj"
          className="input"
        />
        <p className="text-xs text-gray-400 mt-1">Space-separated hashtags — they'll be appended to the caption</p>
      </div>
    </div>
  )
}
