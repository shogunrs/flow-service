import type { Proposal } from '~/composables/useProposals'
import type { CurrentUser } from '~/composables/useCurrentUser'
import { useCurrentUser } from '~/composables/useCurrentUser'
import { apiFetch } from '~/utils/api/index'

export function useProposalAccess() {
  const { user: currentUser } = useCurrentUser()

  /**
   * Atualiza o controle de acesso de uma proposta via API
   */
  async function updateProposalAccess(processKey: string, proposalId: string, accessData: {
    isPublic: boolean
    visibleToUsers?: string[]
    externalNotifications?: Array<{
      name: string
      email: string
      phone: string
      emailEnabled: boolean
      whatsappEnabled: boolean
    }>
  }) {
    const headers: Record<string, string> = {}
    if (currentUser.value?.id) {
      headers['X-User-ID'] = currentUser.value.id
    }
    if (currentUser.value?.organizationId) {
      headers['X-Organization-ID'] = currentUser.value.organizationId
    }

    return await apiFetch(`/api/v1/processes/${processKey}/proposals/${proposalId}/access`, {
      method: 'PUT',
      body: accessData,
      headers
    })
  }

  /**
   * Obtém informações de acesso de uma proposta via API
   */
  async function getProposalAccess(processKey: string, proposalId: string) {
    const headers: Record<string, string> = {}
    if (currentUser.value?.id) {
      headers['X-User-ID'] = currentUser.value.id
    }
    if (currentUser.value?.organizationId) {
      headers['X-Organization-ID'] = currentUser.value.organizationId
    }

    return await apiFetch(`/api/v1/processes/${processKey}/proposals/${proposalId}/access`, {
      headers
    })
  }

  /**
   * Verifica se o usuário atual pode visualizar uma proposta
   * Regras:
   * 1. Criador pode sempre ver
   * 2. Admin da mesma organização pode sempre ver
   * 3. Proposta pública (isPublic = true) pode ser vista por todos da esteira
   * 4. Proposta restrita só pode ser vista por usuários específicos na lista
   */
  function canViewProposal(proposal: Proposal, currentUser: CurrentUser): boolean {
    // Se não há dados de controle de acesso, assume público (backward compatibility)
    if (!proposal.createdBy && proposal.isPublic === undefined) {
      return true
    }

    // 1. Criador pode sempre ver
    if (proposal.createdBy === currentUser.id) {
      return true
    }

    // 2. Admin da mesma organização pode sempre ver
    if (currentUser.superUser || currentUser.roles?.includes('admin')) {
      if (proposal.organizationId === currentUser.organizationId) {
        return true
      }
    }

    // 3. Proposta pública pode ser vista por todos
    if (proposal.isPublic) {
      return true
    }

    // 4. Proposta restrita - verificar se usuário está na lista
    if (!proposal.isPublic && proposal.visibleToUsers) {
      return proposal.visibleToUsers.includes(currentUser.id)
    }

    // Se chegou até aqui, não tem permissão
    return false
  }

  /**
   * Verifica se o usuário pode editar uma proposta
   * Regras mais restritivas:
   * 1. Criador pode sempre editar
   * 2. Admin da mesma organização pode sempre editar
   */
  function canEditProposal(proposal: Proposal, currentUser: CurrentUser): boolean {
    // 1. Criador pode sempre editar
    if (proposal.createdBy === currentUser.id) {
      return true
    }

    // 2. Admin da mesma organização pode sempre editar
    if (currentUser.superUser || currentUser.roles?.includes('admin')) {
      if (proposal.organizationId === currentUser.organizationId) {
        return true
      }
    }

    return false
  }

  /**
   * Filtra uma lista de propostas baseado nas permissões do usuário
   */
  function filterVisibleProposals(proposals: Proposal[], currentUser: CurrentUser): Proposal[] {
    return proposals.filter(proposal => canViewProposal(proposal, currentUser))
  }

  /**
   * Obtém informações sobre o acesso da proposta para exibição na UI
   */
  function getAccessInfo(proposal: Proposal) {
    if (proposal.isPublic) {
      return {
        type: 'public' as const,
        label: 'Público',
        description: 'Todos da esteira podem ver',
        icon: 'fa-solid fa-globe',
        color: 'text-green-400'
      }
    }

    const visibleCount = proposal.visibleToUsers?.length || 0
    const externalCount = proposal.externalNotifications?.length || 0

    return {
      type: 'restricted' as const,
      label: 'Restrito',
      description: `${visibleCount} usuário(s)${externalCount > 0 ? ` + ${externalCount} externo(s)` : ''}`,
      icon: 'fa-solid fa-lock',
      color: 'text-amber-400'
    }
  }

  return {
    canViewProposal,
    canEditProposal,
    filterVisibleProposals,
    getAccessInfo,
    updateProposalAccess,
    getProposalAccess
  }
}