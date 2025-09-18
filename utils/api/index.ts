import { useRuntimeConfig, useState } from '#imports'
import { useToast } from '~/composables/useToast'
import { useApiLoading } from '~/composables/useApiLoading'

export function getApiBase(): string {
  const cfg = useRuntimeConfig()
  const base = (cfg?.public as any)?.FLOW_API_BASE || ''
  return String(base || '')
}

export function isApiEnabled(): boolean {
  return !!getApiBase()
}

export async function apiFetch<T>(path: string, opts: any = {}): Promise<T> {
  const base = getApiBase()
  if (!base) throw new Error('FLOW_API_BASE not set')
  const url = path.startsWith('http') ? path : `${base.replace(/\/$/, '')}${path.startsWith('/') ? '' : '/'}${path}`

  // Check if this should be a silent operation (no global loading)
  const isSilent = opts.silent === true
  delete opts.silent // Remove before sending to $fetch

  const { inc, dec } = useApiLoading()
  if (!isSilent) inc()

  try {
    return await $fetch<T>(url, opts)
  } catch (e: any) {
    // Toast genérico para operações de escrita, com leve proteção contra spam
    try {
      const method = (opts?.method || 'GET').toString().toUpperCase()
      const isWrite = method !== 'GET'
      const lastKey = useState<number>('api-last-error-toast', () => 0)
      const now = Date.now()
      if (isWrite && now - lastKey.value > 2000) {
        lastKey.value = now
        const { error } = useToast()
        console.warn('[Flow] API error for', method, url, e?.message || e)
        error('Falha de conexão com a API. Tente novamente.')
      }
    } catch {}
    throw e
  } finally {
    if (!isSilent) dec()
  }
}
