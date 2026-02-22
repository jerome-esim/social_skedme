import { useRef, useState } from 'react'
import { CloudArrowUpIcon, VideoCameraIcon } from '@heroicons/react/24/outline'
import { media } from '../services/api'
import toast from 'react-hot-toast'

export default function VideoUploader({ onUploaded }) {
  const [dragging, setDragging] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [preview, setPreview] = useState(null)
  const inputRef = useRef()

  const handleFile = async (file) => {
    if (!file || !file.type.startsWith('video/')) {
      toast.error('Please select a video file')
      return
    }

    setPreview(URL.createObjectURL(file))
    setUploading(true)

    try {
      const { data } = await media.upload(file)
      onUploaded(data.videoUrl, data.filename)
      toast.success('Video uploaded!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed')
      setPreview(null)
    } finally {
      setUploading(false)
    }
  }

  const onDrop = (e) => {
    e.preventDefault()
    setDragging(false)
    const file = e.dataTransfer.files[0]
    if (file) handleFile(file)
  }

  return (
    <div>
      <div
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
        className={`relative flex flex-col items-center justify-center rounded-xl border-2 border-dashed cursor-pointer transition-colors min-h-[200px]
          ${dragging ? 'border-brand-500 bg-brand-50' : 'border-gray-300 hover:border-gray-400 bg-gray-50'}`}
      >
        {preview ? (
          <video
            src={preview}
            className="max-h-48 rounded-lg object-contain"
            controls
          />
        ) : (
          <div className="flex flex-col items-center gap-2 p-8 text-gray-400">
            {uploading
              ? <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-500" />
              : <CloudArrowUpIcon className="w-10 h-10" />
            }
            <p className="text-sm font-medium">
              {uploading ? 'Uploading…' : 'Drag & drop your video or click to browse'}
            </p>
            <p className="text-xs">MP4, MOV, AVI — up to 500 MB</p>
          </div>
        )}

        {preview && !uploading && (
          <button
            type="button"
            onClick={(e) => { e.stopPropagation(); setPreview(null); onUploaded('', '') }}
            className="absolute top-2 right-2 bg-white rounded-full shadow p-1 text-gray-500 hover:text-red-600"
          >
            ✕
          </button>
        )}
      </div>

      <input
        ref={inputRef}
        type="file"
        accept="video/*"
        className="hidden"
        onChange={(e) => handleFile(e.target.files[0])}
      />
    </div>
  )
}
