import { ref } from 'vue'

export interface SystemHealthEvent {
  id: string
  component: string
  statusTag?: string
  severity?: string
  message?: string
  details?: string
  context?: Record<string, any>
  occurredAt?: string
  createdAt?: string
  acknowledged: boolean
  acknowledgedBy?: string
  acknowledgedAt?: string
  acknowledgementNotes?: string
}

export interface SystemHealthSummary {
  bySeverity: Record<string, number>
  unacknowledgedCount: number
  latestPerComponent: Record<string, SystemHealthEvent>
}

export interface SystemHealthConfig {
  monitoredStatusTags: string[]
  updatedAt?: string
}

export interface MonitoredComponent {
  id: string
  name: string
  componentKey: string
  description?: string
  statusTag?: string
  defaultSeverity?: string
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export function useSystemHealth() {
  const loading = ref(false)
  const summary = ref<SystemHealthSummary | null>(null)
  const config = ref<SystemHealthConfig>({ monitoredStatusTags: [] })
  const components = ref<MonitoredComponent[]>([])

  const listEvents = async (filters: { acknowledged?: boolean; severity?: string; since?: number } = {}): Promise<SystemHealthEvent[]> => {
    const params = new URLSearchParams()
    if (filters.acknowledged !== undefined) params.append('acknowledged', String(filters.acknowledged))
    if (filters.severity) params.append('severity', filters.severity)
    if (filters.since) params.append('since', String(filters.since))

    const { apiFetch } = await import('~/utils/api/index')
    loading.value = true
    try {
      const response = await apiFetch<{ success: boolean; data: SystemHealthEvent[] }>(
        `/api/v1/system-health/events${params.toString() ? `?${params.toString()}` : ''}`,
        { silent: true }
      )
      return response?.data || []
    } finally {
      loading.value = false
    }
  }

  const fetchSummary = async () => {
    const { apiFetch } = await import('~/utils/api/index')
    const response = await apiFetch<{ success: boolean; data: SystemHealthSummary }>(
      '/api/v1/system-health/summary',
      { silent: true }
    )
    summary.value = response?.data || null
    return summary.value
  }

  const acknowledgeEvent = async (eventId: string, payload: { reviewerId?: string; reviewerName?: string; notes?: string } = {}) => {
    const { apiFetch } = await import('~/utils/api/index')
    await apiFetch(`/api/v1/system-health/events/${eventId}/ack`, {
      method: 'PATCH',
      body: payload,
    })
  }

  const fetchConfig = async () => {
    const { apiFetch } = await import('~/utils/api/index')
    const response = await apiFetch<{ success: boolean; data: SystemHealthConfig }>('/api/v1/system-health/config', {
      method: 'GET',
      silent: true,
    })
    config.value = response?.data || { monitoredStatusTags: [] }
    return config.value
  }

  const updateConfig = async (payload: SystemHealthConfig) => {
    const { apiFetch } = await import('~/utils/api/index')
    const response = await apiFetch<{ success: boolean; data: SystemHealthConfig }>('/api/v1/system-health/config', {
      method: 'PUT',
      body: payload,
    })
    config.value = response?.data || payload
    return config.value
  }

  const fetchComponents = async () => {
    const { apiFetch } = await import('~/utils/api/index')
    const response = await apiFetch<{ success: boolean; data: MonitoredComponent[] }>(
      '/api/v1/system-health/components',
      { method: 'GET', silent: true }
    )
    components.value = response?.data || []
    return components.value
  }

  const createComponent = async (payload: Partial<MonitoredComponent>) => {
    const { apiFetch } = await import('~/utils/api/index')
    const response = await apiFetch<{ success: boolean; data: MonitoredComponent }>(
      '/api/v1/system-health/components',
      {
        method: 'POST',
        body: payload,
      }
    )
    if (response?.data) {
      components.value.push(response.data)
    }
    return response?.data
  }

  const updateComponent = async (id: string, payload: Partial<MonitoredComponent>) => {
    const { apiFetch } = await import('~/utils/api/index')
    const response = await apiFetch<{ success: boolean; data: MonitoredComponent }>(
      `/api/v1/system-health/components/${id}`,
      {
        method: 'PUT',
        body: payload,
      }
    )
    if (response?.data) {
      const idx = components.value.findIndex((c) => c.id === id)
      if (idx >= 0) {
        components.value[idx] = response.data
      }
    }
    return response?.data
  }

  const deleteComponent = async (id: string) => {
    const { apiFetch } = await import('~/utils/api/index')
    await apiFetch(`/api/v1/system-health/components/${id}`, {
      method: 'DELETE',
    })
    components.value = components.value.filter((c) => c.id !== id)
  }

  return {
    loading,
    summary,
    config,
    components,
    listEvents,
    fetchSummary,
    acknowledgeEvent,
    fetchConfig,
    updateConfig,
    fetchComponents,
    createComponent,
    updateComponent,
    deleteComponent,
  }
}
