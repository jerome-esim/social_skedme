import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

// Attach JWT on every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Redirect to /login on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const auth = {
  register: (data) => api.post('/api/auth/register', data),
  login:    (data) => api.post('/api/auth/login', data),
}

export const posts = {
  list:   (page = 0, size = 50) => api.get(`/api/posts?page=${page}&size=${size}`),
  get:    (id)     => api.get(`/api/posts/${id}`),
  create: (data)   => api.post('/api/posts', data),
  update: (id, data) => api.put(`/api/posts/${id}`, data),
  remove: (id)     => api.delete(`/api/posts/${id}`),
}

export const media = {
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return api.post('/api/media/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

export const accounts = {
  list:    ()        => api.get('/api/accounts'),
  connect: (data)    => api.post('/api/accounts/connect', data),
  remove:  (id)      => api.delete(`/api/accounts/${id}`),
}

export default api
