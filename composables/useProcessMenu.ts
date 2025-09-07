import { ref, onMounted, onBeforeUnmount } from 'vue'
import { listActiveProcesses, type ProcessInfo } from '~/composables/usePipeline'
import { isApiEnabled } from '~/utils/api/index'

export function useProcessSubmenu() {
  const processes = ref<ProcessInfo[]>([])
  const LAST_PROCESS_KEY = 'pipeline_last_process'
  const getLastKey = (): string => {
    try { return localStorage.getItem(LAST_PROCESS_KEY) || '' } catch { return '' }
  }
  const setLastKey = (k: string) => {
    try { if (k) localStorage.setItem(LAST_PROCESS_KEY, k) } catch {}
  }

  const refresh = () => {
    try { processes.value = listActiveProcesses() } catch { processes.value = [] }
  }

  const onChange = () => refresh()

  onMounted(() => {
    refresh()
    try {
      window.addEventListener('storage', onChange)
      window.addEventListener('processes:changed', onChange as any)
    } catch (_) {}
  })
  onBeforeUnmount(() => {
    try {
      window.removeEventListener('storage', onChange)
      window.removeEventListener('processes:changed', onChange as any)
    } catch (_) {}
  })

  return { processes, refresh, getLastKey, setLastKey, LAST_PROCESS_KEY }
}
