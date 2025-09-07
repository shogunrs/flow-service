import { ref } from 'vue'

export type ToastType = 'info' | 'success' | 'warning' | 'error'
export interface ToastItem {
  id: string
  type: ToastType
  message: string
  title?: string
  timeout?: number
}

// Global toast state
const globalToasts = ref<ToastItem[]>([])

export function useToast() {
  const toasts = globalToasts

  function push(t: Omit<ToastItem, 'id'>) {
    const id = `${Date.now()}_${Math.random().toString(36).slice(2, 7)}`
    const item: ToastItem = { id, timeout: 3000, ...t }
    toasts.value.unshift(item)
    if (item.timeout && item.timeout > 0) {
      setTimeout(() => remove(id), item.timeout)
    }
    return id
  }

  const info = (message: string, timeout = 3000, title?: string) => push({ type: 'info', message, timeout, title })
  const success = (message: string, timeout = 3000, title?: string) => push({ type: 'success', message, timeout, title })
  const warning = (message: string, timeout = 3500, title?: string) => push({ type: 'warning', message, timeout, title })
  const error = (message: string, timeout = 4000, title?: string) => push({ type: 'error', message, timeout, title })

  function remove(id: string) {
    const i = toasts.value.findIndex(t => t.id === id)
    if (i >= 0) toasts.value.splice(i, 1)
  }

  function clear() { toasts.value = [] }

  return { toasts, push, info, success, warning, error, remove, clear }
}

