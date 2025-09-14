import { ref, computed } from 'vue'
import type { CurrentUser } from '~/composables/useCurrentUser'

export type NotificationScope = 'global' | 'role' | 'user'
export type NotificationSeverity = 'info' | 'success' | 'warning' | 'error'

export interface NotificationItem {
  id: string
  text: string
  scope: NotificationScope
  roles?: string[]
  users?: string[]
  severity?: NotificationSeverity
  expiresAt?: number // epoch ms
  createdAt: number
}

const store = ref<NotificationItem[]>([])

function cleanExpired(){
  const now = Date.now()
  store.value = store.value.filter(n => !n.expiresAt || n.expiresAt > now)
}

export function useNotificationCenter(){
  function publish(text: string, opts: Partial<NotificationItem> & { scope: NotificationScope }): NotificationItem{
    const n: NotificationItem = {
      id: opts.id || `${Date.now()}_${Math.random().toString(36).slice(2,7)}`,
      text,
      scope: opts.scope,
      roles: opts.roles || [],
      users: opts.users || [],
      severity: opts.severity || 'info',
      expiresAt: opts.expiresAt,
      createdAt: Date.now()
    }
    store.value.push(n)
    return n
  }
  function remove(id: string){ store.value = store.value.filter(n => n.id !== id) }
  function clear(){ store.value = [] }

  function visibleFor(user: CurrentUser){
    cleanExpired()
    return store.value.filter(n => {
      if (n.scope === 'global') return true
      if (n.scope === 'role') return (n.roles || []).some(r => user.roles.includes(r))
      if (n.scope === 'user') return (n.users || []).includes(user.id)
      return false
    })
  }

  const all = computed(() => { cleanExpired(); return store.value.slice() })

  return { publish, remove, clear, visibleFor, all }
}

