import { ref } from 'vue'
import { sanitizeProcessKey } from '~/utils/strings/sanitize'
import { isApiEnabled, apiFetch } from '~/utils/api/index'

export type Proposal = {
  id: number | string
  name: string
  amount: number
  stageId: string
  status: string
  isArchived: boolean
  stageEnteredAt: string
  details?: any
}

const PROPOSALS_KEY = (k: string) => `pipeline_proposals__${k}`

function safeParse<T>(raw: string | null, fallback: T): T {
  try { return raw ? (JSON.parse(raw) as T) : fallback } catch { return fallback }
}

export function loadProposals(processKey: string): Proposal[] {
  if (isApiEnabled()) {
    // API-first
    try {
      // GET /api/v1/proposals?processKey=...
      // Placeholder until backend endpoint exists
      // @ts-ignore
      return []
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
