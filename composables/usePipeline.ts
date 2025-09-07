import { ref } from 'vue'
import { sanitizeProcessKey } from '~/utils/strings/sanitize'
import { isApiEnabled, apiFetch } from '~/utils/api/index'

export type Stage = { id: string; title: string; slaDays: number; color: string; status?: string }
export type StageField = { id: string; label: string; type: string; required?: boolean; placeholder?: string; options?: string[] }

const PROCESS_REGISTRY_KEY = 'pipeline_processes'
const PIPELINE_CONFIG_KEY = (k: string) => `pipeline_config__${k}`
const STAGE_FORMS_KEY = (k: string) => `pipeline_stage_forms__${k}`

function safeParse<T>(raw: string | null, fallback: T): T {
  try { return raw ? (JSON.parse(raw) as T) : fallback } catch { return fallback }
}

export type ProcessInfo = { key: string; name: string; active?: boolean }

let apiProcessCache: Array<ProcessInfo> = []
let processesRefreshInFlight: Promise<void> | null = null
let processesLastRefresh = 0

async function refreshProcessesCacheOnce(): Promise<void> {
  if (!isApiEnabled()) return
  const now = Date.now()
  if (processesRefreshInFlight) return processesRefreshInFlight
  // throttle: no more than one refresh per 800ms
  if (now - processesLastRefresh < 800 && apiProcessCache.length) return
  processesRefreshInFlight = apiFetch<Array<ProcessInfo>>('/api/v1/processes')
    .then(arr => {
      apiProcessCache = (arr || []).map(p => ({ ...p, active: p.active !== false }))
      processesLastRefresh = Date.now()
      try { localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(apiProcessCache)) } catch {}
    })
    .catch(() => {})
    .finally(() => { processesRefreshInFlight = null })
  return processesRefreshInFlight
}

export function listProcesses(): Array<ProcessInfo> {
  if (isApiEnabled()) {
    // Best-effort sync from API; also mirror to local for menu fallback
    // Note: callers expect sync; here we return cached local immediately if present.
    try {
      // Return in-memory cache first
      const cached = apiProcessCache.length ? apiProcessCache : (typeof localStorage !== 'undefined' ? safeParse<Array<ProcessInfo>>(localStorage.getItem(PROCESS_REGISTRY_KEY), []) : [])
      // Fire-and-forget refresh (without rebroadcast to avoid loops)
      refreshProcessesCacheOnce()
      return (cached || []).map(p => ({ ...p, active: p.active !== false }))
    } catch { return [] }
  }
  if (typeof localStorage === 'undefined') return []
  const arr = safeParse<Array<ProcessInfo>>(localStorage.getItem(PROCESS_REGISTRY_KEY), [])
  return arr.map(p => ({ ...p, active: p.active !== false }))
}

export function ensureDefaultProcess(defaultKey = 'quotaequity', defaultName = 'QuotaEquity'): void {
  // Quando API está ativa, não criamos nada no localStorage
  if (isApiEnabled()) return
  if (typeof localStorage === 'undefined') return
  const arr = listProcesses()
  const key = sanitizeProcessKey(defaultKey)
  if (!arr.find(p => p.key === key)) {
    const next = [...arr, { key, name: defaultName, active: true }]
    localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(next))
  }
}

export async function addProcess(key: string, name: string): Promise<boolean> {
  if (!key) return
  if (isApiEnabled()) {
    // attempt API create first; if falhar, não espelha local para evitar inconsistência
    try {
      await apiFetch('/api/v1/processes', { method: 'POST', body: { key, name } })
      // update in-memory cache otimista
      apiProcessCache = [...apiProcessCache.filter(p => p.key !== key), { key, name, active: true }]
      try { localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(apiProcessCache)) } catch {}
      try { window.dispatchEvent(new Event('processes:changed')) } catch {}
      return true
    } catch { return false }
  }
  if (typeof localStorage === 'undefined') return
  const arr = listProcesses()
  const k = sanitizeProcessKey(key)
  if (!k) return
  if (!arr.find(p => p.key === k)) {
    arr.push({ key: k, name: name || k, active: true })
    localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(arr))
    try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
  }
  return true
}

export async function removeProcess(key: string): Promise<boolean> {
  if (isApiEnabled()) {
    try {
      await apiFetch(`/api/v1/processes/${encodeURIComponent(sanitizeProcessKey(key))}`, { method: 'DELETE' })
      apiProcessCache = apiProcessCache.filter(p => p.key !== sanitizeProcessKey(key))
      try { localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(apiProcessCache)) } catch {}
      try { window.dispatchEvent(new Event('processes:changed')) } catch {}
      return true
    } catch { return false }
  }
  if (typeof localStorage === 'undefined') return
  const k = sanitizeProcessKey(key)
  const arr = listProcesses().filter(p => p.key !== k)
  localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(arr))
  // Clean related stored configs
  try { localStorage.removeItem(PIPELINE_CONFIG_KEY(k)) } catch {}
  try { localStorage.removeItem(STAGE_FORMS_KEY(k)) } catch {}
  try { localStorage.removeItem(`pipeline_proposals__${k}`) } catch {}
  try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
  return true
}

export function setProcessActive(key: string, active: boolean): void {
  if (typeof localStorage === 'undefined') return
  const k = sanitizeProcessKey(key)
  const arr = listProcesses().map(p => (p.key === k ? { ...p, active } : p))
  localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(arr))
  try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
}

export function listActiveProcesses(): Array<ProcessInfo> {
  return listProcesses().filter(p => p.active !== false)
}

export async function setProcessName(key: string, name: string): Promise<boolean> {
  if (isApiEnabled()) {
    try {
      await apiFetch(`/api/v1/processes/${encodeURIComponent(sanitizeProcessKey(key))}`, { method: 'PUT', body: { name } })
      apiProcessCache = apiProcessCache.map(p => p.key === sanitizeProcessKey(key) ? { ...p, name } : p)
      try { localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(apiProcessCache)) } catch {}
      try { window.dispatchEvent(new Event('processes:changed')) } catch {}
      return true
    } catch { return false }
  }
  if (typeof localStorage === 'undefined') return
  const k = sanitizeProcessKey(key)
  const arr = listProcesses().map(p => (p.key === k ? { ...p, name: name || k } : p))
  localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(arr))
  try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
  return true
}

export function renameProcessKey(oldKey: string, newKey: string): { ok: boolean; message?: string } {
  if (isApiEnabled()) {
    // Not supported via API yet; caller should avoid renomear chave quando API ativa
    return { ok: false, message: 'not-supported' }
  }
  if (typeof localStorage === 'undefined') return { ok: false, message: 'no-storage' }
  const from = sanitizeProcessKey(oldKey)
  const to = sanitizeProcessKey(newKey)
  if (!to) return { ok: false, message: 'invalid-target' }
  if (from === to) return { ok: true }
  const arr = listProcesses()
  const idx = arr.findIndex(p => p.key === from)
  if (idx < 0) return { ok: false, message: 'source-not-found' }
  if (arr.some(p => p.key === to)) return { ok: false, message: 'target-exists' }

  // migrate registry
  const updated = arr.slice()
  updated[idx] = { ...updated[idx], key: to }
  localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(updated))

  // migrate stored configs
  const cfgOld = PIPELINE_CONFIG_KEY(from)
  const cfgNew = PIPELINE_CONFIG_KEY(to)
  const formsOld = STAGE_FORMS_KEY(from)
  const formsNew = STAGE_FORMS_KEY(to)
  const propsOld = `pipeline_proposals__${from}`
  const propsNew = `pipeline_proposals__${to}`
  try {
    const cfg = localStorage.getItem(cfgOld); if (cfg) { localStorage.setItem(cfgNew, cfg); localStorage.removeItem(cfgOld) }
    const forms = localStorage.getItem(formsOld); if (forms) { localStorage.setItem(formsNew, forms); localStorage.removeItem(formsOld) }
    const props = localStorage.getItem(propsOld); if (props) { localStorage.setItem(propsNew, props); localStorage.removeItem(propsOld) }
  } catch (_) {}

  // update last used key
  try { localStorage.setItem('pipeline_last_process', to) } catch {}
  try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
  return { ok: true }
}

export function loadStages(key: string): Stage[] {
  if (typeof localStorage === 'undefined') return []
  const k = sanitizeProcessKey(key)
  return safeParse<Stage[]>(localStorage.getItem(PIPELINE_CONFIG_KEY(k)), [])
}

export function saveStages(key: string, stages: Stage[]): void {
  if (typeof localStorage === 'undefined') return
  const k = sanitizeProcessKey(key)
  localStorage.setItem(PIPELINE_CONFIG_KEY(k), JSON.stringify(stages || []))
}

export function loadStageFormsMap(key: string): Record<string, StageField[]> {
  if (typeof localStorage === 'undefined') return {}
  const k = sanitizeProcessKey(key)
  return safeParse<Record<string, StageField[]>>(localStorage.getItem(STAGE_FORMS_KEY(k)), {})
}

export function saveStageFormsMap(key: string, map: Record<string, StageField[]>): void {
  if (typeof localStorage === 'undefined') return
  const k = sanitizeProcessKey(key)
  localStorage.setItem(STAGE_FORMS_KEY(k), JSON.stringify(map || {}))
}

// Helper to manage in components
export function usePipeline(key: string) {
  const k = sanitizeProcessKey(key)
  const stages = ref<Stage[]>(loadStages(k))
  const formsMap = ref<Record<string, StageField[]>>(loadStageFormsMap(k))
  const setStages = (s: Stage[]) => { stages.value = s; saveStages(k, s) }
  const setFormsMap = (m: Record<string, StageField[]>) => { formsMap.value = m; saveStageFormsMap(k, m) }
  return { stages, formsMap, setStages, setFormsMap }
}

export const pipelineKeys = {
  PROCESS_REGISTRY_KEY,
  PIPELINE_CONFIG_KEY,
  STAGE_FORMS_KEY,
}

export { sanitizeProcessKey }
