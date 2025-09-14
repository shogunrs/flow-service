import { apiFetch, isApiEnabled } from '~/utils/api/index'

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

