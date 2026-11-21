const BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    ...options,
    headers: { ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }), ...options.headers },
  });
  const raw = await response.text();
  let data = null;
  try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
  if (!response.ok) throw new Error(data?.message || data?.error || `Request failed (${response.status})`);
  return data;
}
const query = (values) => new URLSearchParams(values).toString();

export const api = {
  register: (body) => request('/api/v1/users/register', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) => request('/api/v1/users/login', { method: 'POST', body: JSON.stringify(body) }),
  me: () => request('/api/v1/users/me'),
  logout: () => request('/api/v1/users/logout', { method: 'POST' }),
  languages: () => request('/api/v1/languages'),
  chat: (message, mode) => request('/api/v1/chat', { method: 'POST', body: JSON.stringify({ message, mode }) }),
  folderContents: (id) => request(`/api/v1/folders/${id}/contents`),
  createFolder: (parentId, folderName) => request(`/api/v1/folders/create?${query({ parentId, folderName })}`, { method: 'POST' }),
  updateFolder: (id, folderName) => request(`/api/v1/folders/update/${id}?${query({ folderName })}`, { method: 'PUT' }),
  deleteFolder: (id) => request(`/api/v1/folders/delete/${id}`, { method: 'DELETE' }),
  createFile: (folderId, fileName, fileExtension) => request(`/api/v1/files/create?${query({ folderId, fileName, fileExtension })}`, { method: 'POST' }),
  getFile: (id) => request(`/api/v1/files/${id}`),
  updateFile: (id, fileName, fileContent) => request(`/api/v1/files/update/${id}?${query({ fileName })}`, { method: 'PUT', body: fileContent, headers: { 'Content-Type': 'text/plain;charset=UTF-8' } }),
  deleteFile: (id) => request(`/api/v1/files/delete/${id}`, { method: 'DELETE' }),
};

export const API_BASE_URL = BASE_URL;
