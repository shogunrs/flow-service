import { getApiBase, isApiEnabled } from '~/utils/api/index'
import { loadProposals } from './useProposals'
import type { Proposal } from './useProposals'

export interface ProposalStats {
  totalProposals: number
  totalAmount: number
  averageAmount: number
  proposalsByStage: Record<string, number>
  processKey: string
}

function calculateLocalStats(proposals: Proposal[], processKey: string): ProposalStats {
  const activeProposals = proposals.filter(p => !p.isArchived)
  const totalProposals = activeProposals.length
  const totalAmount = activeProposals.reduce((sum, p) => sum + (p.amount || 0), 0)
  const averageAmount = totalProposals > 0 ? totalAmount / totalProposals : 0

  const proposalsByStage: Record<string, number> = {}
  activeProposals.forEach(p => {
    if (p.stageId) {
      proposalsByStage[p.stageId] = (proposalsByStage[p.stageId] || 0) + 1
    }
  })

  return {
    totalProposals,
    totalAmount,
    averageAmount,
    proposalsByStage,
    processKey
  }
}

export function useProposalStats(processKey: string) {
  const apiBase = getApiBase()
  const stats = ref<ProposalStats | null>(null)
  const loading = ref(false)

  const loadStats = async () => {
    if (!processKey) {
      stats.value = null
      return
    }

    loading.value = true
    try {
      if (isApiEnabled() && apiBase) {
        // Tentar carregar da API primeiro
        const apiStats = await $fetch<ProposalStats>(`${apiBase}/api/v1/processes/${encodeURIComponent(processKey)}/proposals/stats`)
        stats.value = apiStats
      } else {
        // Fallback para cálculo local
        const localProposals = loadProposals(processKey)
        stats.value = calculateLocalStats(localProposals, processKey)
      }
    } catch (error) {
      console.warn('Erro ao carregar estatísticas da API, usando cálculo local:', error)
      // Fallback para cálculo local em caso de erro
      const localProposals = loadProposals(processKey)
      stats.value = calculateLocalStats(localProposals, processKey)
    } finally {
      loading.value = false
    }
  }

  const refresh = loadStats

  const formatCurrency = (value: number): string => {
    if (value == null || isNaN(value)) {
      return 'R$ 0,00'
    }
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value)
  }

  const formatCompactCurrency = (value: number): string => {
    if (value == null || isNaN(value)) {
      return 'R$ 0,00'
    }
    if (value >= 1000000) {
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
        notation: 'compact',
        maximumFractionDigits: 1
      }).format(value)
    }
    return formatCurrency(value)
  }

  return {
    stats,
    loading,
    refresh,
    formatCurrency,
    formatCompactCurrency
  }
}