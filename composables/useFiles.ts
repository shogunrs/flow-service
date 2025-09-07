import { apiFetch, isApiEnabled } from '~/utils/api/index'

export type UploadedObject = { objectKey: string; url?: string; name?: string; type?: string; size?: number }

export async function presignUpload(filename: string, contentType: string, prefix?: string): Promise<{ url: string; objectKey: string }> {
  if (!isApiEnabled()) throw new Error('API disabled')
  return await apiFetch('/api/v1/files/presign-upload', {
    method: 'POST',
    body: { filename, contentType, prefix }
  })
}

export async function uploadFileViaPresign(file: File, prefix?: string): Promise<UploadedObject> {
  if (!isApiEnabled()) throw new Error('API disabled')
  const { url, objectKey } = await presignUpload(file.name, file.type || 'application/octet-stream', prefix)
  await fetch(url, { method: 'PUT', body: file, headers: { 'Content-Type': file.type || 'application/octet-stream' } })
  return { objectKey, name: file.name, type: file.type, size: file.size }
}

