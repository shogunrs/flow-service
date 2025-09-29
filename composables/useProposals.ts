import { ref } from 'vue'
import { sanitizeProcessKey } from '~/utils/strings/sanitize'
import { isApiEnabled, apiFetch } from '~/utils/api/index'

export type ExternalNotification = {
  name: string
  email: string
  phone?: string
  emailEnabled: boolean
  whatsappEnabled: boolean
}

export type Proposal = {
  id: number | string
  name: string
  amount: number
  stageId: string
  status: string
  isArchived: boolean
  stageEnteredAt: string
  details?: any
  // Controle de acesso
  createdBy?: string
  organizationId?: string
  isPublic?: boolean
  visibleToUsers?: string[]
  externalNotifications?: ExternalNotification[]
}

const PROPOSALS_KEY = (k: string) => `pipeline_proposals__${k}`

function safeParse<T>(raw: string | null, fallback: T): T {
  try { return raw ? (JSON.parse(raw) as T) : fallback } catch { return fallback }
}

export function loadProposals(processKey: string): Proposal[] {
  if (isApiEnabled()) {
    try {
      return [] // chamadas devem ser feitas de forma assíncrona; usar fetchProposalsApi no fluxo
    } catch { return [] }
  }
  if (typeof localStorage === 'undefined') return []
  const k = sanitizeProcessKey(processKey)
  return safeParse<Proposal[]>(localStorage.getItem(PROPOSALS_KEY(k)), [])
}

export function saveProposals(processKey: string, items: Proposal[]): void {
  if (isApiEnabled()) {
    // POST/PUT per item would be used when API is available; no-op for now
    return
  }
  if (typeof localStorage === 'undefined') return
  const k = sanitizeProcessKey(processKey)
  localStorage.setItem(PROPOSALS_KEY(k), JSON.stringify(items || []))
}

export function useProposals(processKey: string) {
  const k = sanitizeProcessKey(processKey)
  const proposals = ref<Proposal[]>(loadProposals(k))
  const setProposals = (arr: Proposal[]) => { proposals.value = arr; saveProposals(k, arr) }
  const addProposal = (p: Proposal) => { const next = [p, ...proposals.value]; setProposals(next) }
  return { proposals, setProposals, addProposal }
}

// API helpers
export async function fetchProposalsApi(processKey: string): Promise<Proposal[]> {
  if (!isApiEnabled()) return []
  try {
    const arr = await apiFetch<any[]>(`/api/v1/processes/${encodeURIComponent(processKey)}/proposals`)
    return (arr || []).map(x => ({
      id: x.id,
      name: x.name,
      amount: Number(x.amount || 0),
      stageId: x.stageId,
      status: x.status || 'Pendente',
      isArchived: !!x.archived,
      stageEnteredAt: x.stageEnteredAt,
      details: x.details || {},
      // Campos de controle de acesso
      createdBy: x.createdBy,
      organizationId: x.organizationId,
      isPublic: x.isPublic,
      visibleToUsers: x.visibleToUsers || [],
      externalNotifications: x.externalNotifications || []
    }))
  } catch { return [] }
}

export async function createProposalApi(processKey: string, payload: Partial<Proposal>): Promise<Proposal | null> {
  if (!isApiEnabled()) return null
  try {
    const body: any = {
      name: payload.name,
      amount: payload.amount,
      stageId: payload.stageId,
      status: payload.status
    }
    if (payload.details && Object.keys(payload.details).length > 0) {
      body.details = payload.details
    }
    // Campos de controle de acesso
    if (typeof payload.isPublic === 'boolean') body.isPublic = payload.isPublic
    if (payload.visibleToUsers) body.visibleToUsers = payload.visibleToUsers
    if (payload.externalNotifications) body.externalNotifications = payload.externalNotifications

    // Headers para identificação do usuário
    const headers: Record<string, string> = {}
    if (payload.createdBy) headers['X-User-ID'] = payload.createdBy
    if (payload.organizationId) headers['X-Organization-ID'] = payload.organizationId

    const x = await apiFetch<any>(`/api/v1/processes/${encodeURIComponent(processKey)}/proposals`, {
      method: 'POST',
      body,
      headers
    })
    return {
      id: x.id,
      name: x.name,
      amount: Number(x.amount || 0),
      stageId: x.stageId,
      status: x.status || 'Pendente',
      isArchived: !!x.archived,
      stageEnteredAt: x.stageEnteredAt,
      details: x.details || {},
      createdBy: x.createdBy,
      organizationId: x.organizationId,
      isPublic: x.isPublic,
      visibleToUsers: x.visibleToUsers || [],
      externalNotifications: x.externalNotifications || []
    }
  } catch { return null }
}

export async function fetchProposalFormsApi(processKey: string, proposalId: string): Promise<Record<string, any>> {
  if (!isApiEnabled()) return {}
  try {
    return await apiFetch<Record<string, any>>(`/api/v1/processes/${encodeURIComponent(processKey)}/proposals/${encodeURIComponent(proposalId)}/forms`)
  } catch { return {} }
}

export async function saveProposalStageFormApi(processKey: string, proposalId: string, stageId: string, values: Record<string, any>): Promise<Record<string, any>> {
  if (!isApiEnabled()) return values || {}
  try {
    return await apiFetch<Record<string, any>>(`/api/v1/processes/${encodeURIComponent(processKey)}/proposals/${encodeURIComponent(proposalId)}/forms/${encodeURIComponent(stageId)}`, {
      method: 'PUT',
      body: { values: values || {} },
      silent: true
    })
  } catch { return values || {} }
}

export async function updateProposalApi(processKey: string, proposalId: string, patch: Partial<Proposal>): Promise<Proposal | null> {
  if (!isApiEnabled()) return null
  try {
    const body: any = {}
    if (patch.stageId) body.stageId = patch.stageId
    if (typeof patch.amount === 'number') body.amount = patch.amount
    if (patch.status) body.status = patch.status
    if (patch.name) body.name = patch.name
    const x = await apiFetch<any>(`/api/v1/processes/${encodeURIComponent(processKey)}/proposals/${encodeURIComponent(String(proposalId))}`, { method: 'PUT', body, silent: true })
    return {
      id: x.id,
      name: x.name,
      amount: Number(x.amount || 0),
      stageId: x.stageId,
      status: x.status || 'Pendente',
      isArchived: !!x.archived,
      stageEnteredAt: x.stageEnteredAt,
      details: x.details || {}
    }
  } catch { return null }
}

export async function deleteProposalApi(processKey: string, proposalId: string): Promise<boolean> {
  if (!isApiEnabled()) return false
  try {
    await apiFetch<void>(`/api/v1/processes/${encodeURIComponent(processKey)}/proposals/${encodeURIComponent(String(proposalId))}`, { method: 'DELETE' })
    return true
  } catch { return false }
}

// Novo composable reativo usando useFetch com refs mutáveis
export function useProposalsReactive(processKey: string) {
  const { public: { FLOW_API_BASE } } = useRuntimeConfig()
  const { user: currentUser } = useCurrentUser()
  const apiBase = FLOW_API_BASE

  // Ref mutável para manipulações locais
  const proposals = ref<Proposal[]>([])
  const loading = ref(false)

  // Função para carregar dados do servidor
  const loadFromServer = async () => {
    if (!apiBase || !processKey) {
      proposals.value = []
      return
    }

    loading.value = true
    try {
      // Adicionar headers de usuário para filtragem de acesso
      const headers: Record<string, string> = {}
      if (currentUser.value?.id) {
        headers['X-User-ID'] = currentUser.value.id
      }
      if (currentUser.value?.organizationId) {
        headers['X-Organization-ID'] = currentUser.value.organizationId
      }

      const arr = await $fetch<any[]>(`${apiBase}/api/v1/processes/${encodeURIComponent(processKey)}/proposals`, {
        headers
      })
      proposals.value = (arr || []).map(x => ({
        id: x.id,
        name: x.name,
        amount: Number(x.amount || 0),
        stageId: x.stageId,
        status: x.status || 'Pendente',
        isArchived: !!x.archived,
        stageEnteredAt: x.stageEnteredAt,
        details: x.details || {},
        // Campos de controle de acesso
        createdBy: x.createdBy,
        organizationId: x.organizationId,
        isPublic: x.isPublic,
        visibleToUsers: x.visibleToUsers || [],
        externalNotifications: x.externalNotifications || []
      }))
    } catch (error) {
      console.error('Erro ao carregar propostas:', error)
      proposals.value = []
    } finally {
      loading.value = false
    }
  }

  // Carregar dados inicialmente
  loadFromServer()

  return {
    proposals,
    loading: readonly(loading),
    refresh: loadFromServer
  }
}
