import { apiFetch, isApiEnabled } from '~/utils/api/index'

export type StageField = {
  id?: string
  stageId?: string
  label: string
  type: string
  required?: boolean
  placeholder?: string
  options?: string[]
  order?: number
}

export async function fetchStageFieldsApi(stageId: string): Promise<StageField[]> {
  if (!isApiEnabled() || !stageId) return []
  try {
    return await apiFetch<StageField[]>(`/api/v1/stages/${encodeURIComponent(stageId)}/fields`)
  } catch { return [] }
}

export async function saveStageFieldsApi(stageId: string, fields: StageField[]): Promise<void> {
  if (!isApiEnabled() || !stageId) return
  const body = (fields || []).map((f, i) => ({
    id: f.id,
    stageId,
    label: f.label,
    type: f.type,
    required: !!f.required,
    placeholder: f.placeholder || '',
    options: Array.isArray(f.options) ? f.options : [],
    order: typeof f.order === 'number' ? f.order : i
  }))
  try {
    await apiFetch<void>(`/api/v1/stages/${encodeURIComponent(stageId)}/fields`, { method: 'PUT', body })
  } catch {}
}

