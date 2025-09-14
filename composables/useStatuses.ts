import { apiFetch, isApiEnabled } from '~/utils/api/index'

export interface StatusDTO {
  id: string
  name: string
  color: string
  category: string
  createdAt: string
  updatedAt: string
}

export interface CreateStatusRequest {
  name: string
  color: string
  category: string
}

export async function fetchStatuses(): Promise<string[]> {
  if (!isApiEnabled()) {
    return ['Pendente', 'Em análise', 'Aprovado', 'Reprovado', 'Arquivado']
  }
  try {
    const arr = await apiFetch<string[]>(`/api/v1/statuses`)
    if (Array.isArray(arr) && arr.length) return arr
    return ['Pendente', 'Em análise', 'Aprovado', 'Reprovado', 'Arquivado']
  } catch {
    return ['Pendente', 'Em análise', 'Aprovado', 'Reprovado', 'Arquivado']
  }
}

export async function fetchDetailedStatuses(category?: string): Promise<StatusDTO[]> {
  if (!isApiEnabled()) {
    return []
  }
  try {
    const url = category ? `/api/v1/statuses/detailed?category=${category}` : '/api/v1/statuses/detailed'
    const statuses = await apiFetch<StatusDTO[]>(url)
    return Array.isArray(statuses) ? statuses : []
  } catch {
    return []
  }
}

export async function createStatus(request: CreateStatusRequest): Promise<StatusDTO | null> {
  if (!isApiEnabled()) {
    return null
  }
  try {
    return await apiFetch<StatusDTO>('/api/v1/statuses', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
  } catch {
    return null
  }
}

export async function updateStatus(id: string, request: CreateStatusRequest): Promise<StatusDTO | null> {
  if (!isApiEnabled()) {
    return null
  }
  try {
    return await apiFetch<StatusDTO>(`/api/v1/statuses/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
  } catch {
    return null
  }
}

export async function deleteStatus(id: string): Promise<boolean> {
  if (!isApiEnabled()) {
    return false
  }
  try {
    await apiFetch(`/api/v1/statuses/${id}`, { method: 'DELETE' })
    return true
  } catch {
    return false
  }
}

export const STATUS_CATEGORIES = [
  { value: 'ESTEIRA', label: 'Esteira' },
  { value: 'USUARIOS', label: 'Usuários' },
  { value: 'SISTEMA', label: 'Sistema' }
] as const

