import { ref } from 'vue'

export default defineNuxtPlugin(async (nuxtApp) => {
  const cfg = useRuntimeConfig()
  const base = (cfg.public as any)?.FLOW_API_BASE as string | undefined
  const health = ref<{ ok: boolean | null; message?: string; checkedAt?: number }>({ ok: null })

  async function check() {
    if (!base) {
      health.value = { ok: null, message: 'FLOW_API_BASE not set', checkedAt: Date.now() }
      return
    }
    const url = (p: string) => `${base!.replace(/\/$/, '')}${p}`
    try {
      await $fetch(url('/api/v1/processes'), { timeout: 2500 })
      health.value = { ok: true, message: 'API reachable', checkedAt: Date.now() }
      console.info('[Flow] API health: reachable')
      window.dispatchEvent(new CustomEvent('flow:api-health', { detail: { ok: true } }))
    } catch (e: any) {
      const msg = e?.message || 'API unreachable'
      health.value = { ok: false, message: msg, checkedAt: Date.now() }
      console.warn('[Flow] API health: DOWN -', msg)
      window.dispatchEvent(new CustomEvent('flow:api-health', { detail: { ok: false, error: msg } }))
    }
  }

  // initial check
  check()
  // expose helper
  nuxtApp.provide('apiHealth', health)
  nuxtApp.provide('checkApiHealth', check)
})

declare module '#app' {
  interface NuxtApp {
    $apiHealth: Ref<{ ok: boolean | null; message?: string; checkedAt?: number }>
    $checkApiHealth: () => Promise<void>
  }
}
