import { ref } from 'vue'
import { sanitizeProcessKey } from '~/utils/strings/sanitize'
import { isApiEnabled, apiFetch } from '~/utils/api/index'
import type { StageField } from '~/composables/useStageFields'
import { saveStagesPreservingIdsApi } from '~/composables/useStages'

export type Stage = { id: string; title: string; slaDays: number; color: string; status?: string }

const PROCESS_REGISTRY_KEY = 'pipeline_processes'
const PIPELINE_CONFIG_KEY = (k: string) => `pipeline_config__${k}`
const STAGE_FORMS_KEY = (k: string) => `pipeline_stage_forms__${k}`

function safeParse<T>(raw: string | null, fallback: T): T {
  try { return raw ? (JSON.parse(raw) as T) : fallback } catch { return fallback }
}

function normalizeProcessInfo(raw: any): ProcessInfo {
  if (!raw) {
    return {
      key: '',
      name: '',
      active: true,
      type: 'GENERIC',
      isFinanceiro: false,
    }
  }

  const rawType = typeof raw.type === 'string' ? raw.type.trim().toUpperCase() : undefined
  const allowed: ProcessType[] = ['GENERIC', 'FINANCIAL', 'TODO_LIST', 'LEAD_QUALIFICATION']
  const type: ProcessType = rawType && allowed.includes(rawType as ProcessType)
    ? (rawType as ProcessType)
    : raw.isFinanceiro
      ? 'FINANCIAL'
      : 'GENERIC'

  const info: ProcessInfo = {
    key: raw.key || raw.externalId || '',
    name: raw.name || raw.key || '',
    active: raw.active !== false,
    type,
    isFinanceiro: type === 'FINANCIAL',
  }

  return info
}

export type ProcessType = 'GENERIC' | 'FINANCIAL' | 'TODO_LIST' | 'LEAD_QUALIFICATION'

export type ProcessInfo = {
  key: string
  name: string
  active?: boolean
  type?: ProcessType
  isFinanceiro?: boolean
}

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
      apiProcessCache = (arr || []).map((p) => normalizeProcessInfo(p))
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
      const cached = apiProcessCache.length
        ? apiProcessCache
        : (typeof localStorage !== 'undefined'
            ? safeParse<Array<ProcessInfo>>(localStorage.getItem(PROCESS_REGISTRY_KEY), [])
            : [])
      // Fire-and-forget refresh (without rebroadcast to avoid loops)
      refreshProcessesCacheOnce()
      return (cached || []).map((p) => normalizeProcessInfo(p))
    } catch { return [] }
  }
  if (typeof localStorage === 'undefined') return []
  const arr = safeParse<Array<ProcessInfo>>(localStorage.getItem(PROCESS_REGISTRY_KEY), [])
  return arr.map((p) => normalizeProcessInfo(p))
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

export async function getProcessInfo(key: string): Promise<ProcessInfo | null> {
  if (!key) return null

  if (isApiEnabled()) {
    try {
      const process = await apiFetch<any>(`/api/v1/processes/${encodeURIComponent(key)}`)
      return normalizeProcessInfo(process)
    } catch {
      return null
    }
  }

  // Fallback para localStorage
  const processes = listProcesses()
  return processes.find(p => p.key === key) || null
}

export async function addProcess(key: string, name: string, type: ProcessType = 'GENERIC'): Promise<boolean> {
  if (!key) return
  if (isApiEnabled()) {
    // attempt API create first; if falhar, não espelha local para evitar inconsistência
    try {
      await apiFetch('/api/v1/processes', { method: 'POST', body: { key, name, type } })
      // update in-memory cache otimista
      apiProcessCache = [
        ...apiProcessCache.filter((p) => p.key !== key),
        normalizeProcessInfo({ key, name, active: true, type }),
      ]
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
    arr.push(normalizeProcessInfo({ key: k, name: name || k, active: true, type }))
    localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(arr))
    try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
  }
  return true
}

export async function applyProcessBlueprint(key: string, stages: Array<{ id: string; title: string; slaDays: number; color: string; defaultStatus?: string }>): Promise<void> {
  const k = sanitizeProcessKey(key)
  if (!k || !Array.isArray(stages) || !stages.length) return

  const normalizedStages = stages.map((stage, index) => ({
    id: stage.id || `stage_${index}_${Date.now()}`,
    title: stage.title,
    slaDays: stage.slaDays ?? 0,
    color: stage.color || 'sky',
    defaultStatus: stage.defaultStatus || '',
  }))

  if (isApiEnabled()) {
    try {
      await saveStagesPreservingIdsApi(k, [], normalizedStages)
    } catch (error) {
      console.error('Falha ao aplicar blueprint via API:', error)
    }
    return
  }

  if (typeof localStorage === 'undefined') return
  try {
    const storageKey = PIPELINE_CONFIG_KEY(k)
    localStorage.setItem(storageKey, JSON.stringify(normalizedStages))
  } catch (error) {
    console.warn('Não foi possível persistir blueprint local:', error)
  }
}

export async function removeProcess(key: string, options: { backup?: boolean, skipConfirmation?: boolean } = {}): Promise<{ success: boolean, backup?: Record<string, any>, deletedKeys?: string[] }> {
  const k = sanitizeProcessKey(key)
  
  // Create backup before deletion if requested
  let backup: Record<string, any> | null = null
  if (options.backup !== false) { // Default to true
    backup = backupProcessData(k)
  }
  
  const result = {
    success: false,
    backup: backup || undefined,
    deletedKeys: [] as string[]
  }
  
  if (isApiEnabled()) {
    try {
      // API should handle cascade deletion on backend
      await apiFetch(`/api/v1/processes/${encodeURIComponent(k)}`, { method: 'DELETE' })
      apiProcessCache = apiProcessCache.filter(p => p.key !== k)
      try { localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(apiProcessCache)) } catch {}
      
      // Clean local cache in case of API/local hybrid mode
      result.deletedKeys = cascadeDeleteLocalData(k)
      
      try { window.dispatchEvent(new Event('processes:changed')) } catch {}
      result.success = true
      return result
    } catch { 
      return result 
    }
  }
  
  if (typeof localStorage === 'undefined') return result
  
  // Remove process from registry
  const arr = listProcesses().filter(p => p.key !== k)
  localStorage.setItem(PROCESS_REGISTRY_KEY, JSON.stringify(arr))
  
  // Cascade delete all related data
  result.deletedKeys = cascadeDeleteLocalData(k)
  
  try { window.dispatchEvent(new Event('processes:changed')) } catch (_) {}
  result.success = true
  return result
}

// Function to handle cascade deletion of all related data
function cascadeDeleteLocalData(processKey: string): string[] {
  const k = sanitizeProcessKey(processKey)
  const deletedKeys: string[] = []
  
  if (typeof localStorage === 'undefined') return deletedKeys
  
  // Core pipeline data
  const keysToDelete = [
    PIPELINE_CONFIG_KEY(k),           // Stage configurations
    STAGE_FORMS_KEY(k),              // Stage form definitions
    `pipeline_proposals__${k}`,       // Process proposals/records
  ]
  
  // Additional data patterns that might exist
  const additionalPatterns = [
    `pipeline_files__${k}`,          // File attachments
    `pipeline_history__${k}`,        // Process history
    `pipeline_analytics__${k}`,      // Analytics data
    `pipeline_cache__${k}`,          // Cached data
    `pipeline_settings__${k}`,       // Process-specific settings
    `pipeline_templates__${k}`,      // Form templates
    `pipeline_notifications__${k}`,  // Notifications
    `pipeline_reports__${k}`,        // Generated reports
    `pipeline_exports__${k}`,        // Export data
    `pipeline_backups__${k}`,        // Backup data
    `pipeline_workflow__${k}`,       // Workflow data
    `pipeline_permissions__${k}`,    // Permission settings
    `pipeline_logs__${k}`,           // Activity logs
    `pipeline_drafts__${k}`,         // Draft data
  ]
  
  // Delete core keys
  keysToDelete.forEach(storageKey => {
    try {
      if (localStorage.getItem(storageKey) !== null) {
        localStorage.removeItem(storageKey)
        deletedKeys.push(storageKey)
        console.info(`Deleted: ${storageKey}`)
      }
    } catch (err) {
      console.warn(`Failed to delete ${storageKey}:`, err)
    }
  })
  
  // Delete additional pattern keys
  additionalPatterns.forEach(storageKey => {
    try {
      if (localStorage.getItem(storageKey) !== null) {
        localStorage.removeItem(storageKey)
        deletedKeys.push(storageKey)
      }
    } catch (err) {
      // Silent fail for optional keys
    }
  })
  
  // Handle special case: clear last_process if it matches deleted process
  try {
    const lastProcess = localStorage.getItem('pipeline_last_process')
    if (lastProcess === k) {
      localStorage.removeItem('pipeline_last_process')
      deletedKeys.push('pipeline_last_process')
      console.info(`Cleared last_process reference: ${k}`)
    }
  } catch {}
  
  // Scan and clean any orphaned keys (safety net)
  try {
    const allKeys = Object.keys(localStorage)
    const orphanedKeys = allKeys.filter(storageKey => 
      (storageKey.includes(`__${k}`) || storageKey.includes(`_${k}_`)) &&
      !deletedKeys.includes(storageKey)
    )
    
    orphanedKeys.forEach(storageKey => {
      try {
        localStorage.removeItem(storageKey)
        deletedKeys.push(storageKey)
        console.info(`Cleaned orphaned key: ${storageKey}`)
      } catch {}
    })
    
    if (deletedKeys.length > 0) {
      console.info(`Cascade deletion complete: removed ${deletedKeys.length} keys for process "${k}"`, deletedKeys)
    }
  } catch (err) {
    console.warn('Error during orphaned key cleanup:', err)
  }
  
  return deletedKeys
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

// Utility function to find and clean orphaned data
export function auditAndCleanOrphanedData(): { orphaned: string[], cleaned: string[], errors: string[] } {
  if (typeof localStorage === 'undefined') return { orphaned: [], cleaned: [], errors: [] }
  
  const activeProcesses = listProcesses().map(p => p.key)
  const allKeys = Object.keys(localStorage)
  const pipelineKeys = allKeys.filter(key => key.startsWith('pipeline_'))
  
  const orphaned: string[] = []
  const cleaned: string[] = []
  const errors: string[] = []
  
  pipelineKeys.forEach(key => {
    // Skip registry and global keys
    if (key === PROCESS_REGISTRY_KEY || key === 'pipeline_last_process') return
    
    // Extract process key from storage key
    const match = key.match(/pipeline_.*?__(.+)$/)
    if (match) {
      const processKey = match[1]
      
      // Check if process still exists
      if (!activeProcesses.includes(processKey)) {
        orphaned.push(key)
        
        try {
          localStorage.removeItem(key)
          cleaned.push(key)
        } catch (err) {
          errors.push(`${key}: ${err}`)
        }
      }
    }
  })
  
  // Check for invalid last_process reference
  try {
    const lastProcess = localStorage.getItem('pipeline_last_process')
    if (lastProcess && !activeProcesses.includes(lastProcess)) {
      orphaned.push('pipeline_last_process')
      localStorage.removeItem('pipeline_last_process')
      cleaned.push('pipeline_last_process')
    }
  } catch (err) {
    errors.push(`pipeline_last_process: ${err}`)
  }
  
  if (cleaned.length > 0) {
    console.info(`Orphaned data cleanup: removed ${cleaned.length} keys`, cleaned)
  }
  
  return { orphaned, cleaned, errors }
}

// Utility to get storage usage by process
export function getStorageUsageByProcess(): Record<string, { keys: string[], sizeEstimate: number }> {
  if (typeof localStorage === 'undefined') return {}
  
  const processes = listProcesses()
  const usage: Record<string, { keys: string[], sizeEstimate: number }> = {}
  
  processes.forEach(process => {
    const processKeys = Object.keys(localStorage).filter(key => 
      key.includes(`__${process.key}`) || key.includes(`_${process.key}_`)
    )
    
    let sizeEstimate = 0
    processKeys.forEach(key => {
      try {
        const value = localStorage.getItem(key)
        if (value) sizeEstimate += value.length
      } catch {}
    })
    
    usage[process.key] = {
      keys: processKeys,
      sizeEstimate
    }
  })
  
  return usage
}

// Utility to backup process data before deletion
export function backupProcessData(processKey: string): Record<string, any> | null {
  if (typeof localStorage === 'undefined') return null
  
  const k = sanitizeProcessKey(processKey)
  const backup: Record<string, any> = {}
  const allKeys = Object.keys(localStorage)
  
  // Find all keys related to this process
  const processKeys = allKeys.filter(key => 
    key.includes(`__${k}`) || key.includes(`_${k}_`)
  )
  
  processKeys.forEach(key => {
    try {
      const value = localStorage.getItem(key)
      if (value) {
        backup[key] = JSON.parse(value)
      }
    } catch {
      // If parsing fails, store as string
      backup[key] = localStorage.getItem(key)
    }
  })
  
  if (Object.keys(backup).length > 0) {
    backup._metadata = {
      processKey: k,
      backupDate: new Date().toISOString(),
      keyCount: Object.keys(backup).length - 1 // -1 for metadata
    }
    
    console.info(`Backed up ${Object.keys(backup).length - 1} keys for process "${k}"`)
  }
  
  return Object.keys(backup).length > 0 ? backup : null
}

export const pipelineKeys = {
  PROCESS_REGISTRY_KEY,
  PIPELINE_CONFIG_KEY,
  STAGE_FORMS_KEY,
}

export { sanitizeProcessKey }
