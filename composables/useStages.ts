import { apiFetch, isApiEnabled } from '~/utils/api/index'

export async function fetchStagesApi(processKey: string): Promise<any[]> {
  if (!isApiEnabled()) return []
  try {
    return await apiFetch<any[]>(`/api/v1/processes/${encodeURIComponent(processKey)}/stages`)
  } catch { return [] }
}

export async function saveStagesApi(processKey: string, stages: any[]): Promise<any[]> {
  if (!isApiEnabled()) return []
  try {
    return await apiFetch<any[]>(`/api/v1/processes/${encodeURIComponent(processKey)}/stages`, {
      method: 'PUT',
      body: stages || []
    })
  } catch { return [] }
}

// Função para atualizar apenas a ordem dos estágios sem recriar
export async function updateStagesOrderApi(processKey: string, stageOrders: Array<{id: string, order: number}>): Promise<boolean> {
  if (!isApiEnabled()) return false
  try {
    await apiFetch(`/api/v1/processes/${encodeURIComponent(processKey)}/stages/reorder`, {
      method: 'PATCH',
      body: { orders: stageOrders }
    })
    return true
  } catch { return false }
}
