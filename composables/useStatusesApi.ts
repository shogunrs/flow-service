import { isApiEnabled, apiFetch } from '~/utils/api/index'

export interface StatusDTO {
  id: string
  name: string
  color: string
  createdAt: string
  updatedAt: string
}

export interface CreateStatusRequest {
  name: string
  color: string
}

export interface UpdateStatusRequest {
  name: string
  color: string
}

export async function fetchStatusesApi(): Promise<StatusDTO[]> {
  if (!isApiEnabled()) {
    return []
  }

  try {
    return await apiFetch<StatusDTO[]>('/api/v1/statuses/detailed')
  } catch (error) {
    console.error('Error fetching statuses:', error)
    return []
  }
}

export async function createStatusApi(request: CreateStatusRequest): Promise<StatusDTO | null> {
  if (!isApiEnabled()) {
    return null
  }

  try {
    return await apiFetch<StatusDTO>('/api/v1/statuses', {
      method: 'POST',
      body: request
    })
  } catch (error) {
    console.error('Error creating status:', error)
    throw error
  }
}

export async function updateStatusApi(id: string, request: UpdateStatusRequest): Promise<StatusDTO | null> {
  if (!isApiEnabled()) {
    return null
  }

  try {
    return await apiFetch<StatusDTO>(`/api/v1/statuses/${id}`, {
      method: 'PUT',
      body: request
    })
  } catch (error) {
    console.error('Error updating status:', error)
    throw error
  }
}

export async function deleteStatusApi(id: string): Promise<boolean> {
  if (!isApiEnabled()) {
    return false
  }

  try {
    await apiFetch(`/api/v1/statuses/${id}`, {
      method: 'DELETE'
    })
    return true
  } catch (error) {
    console.error('Error deleting status:', error)
    return false
  }
}