import { ref } from 'vue'
import type { Proposal } from '~/composables/useProposals'
import { useProposalAccess } from '~/composables/useProposalAccess'
import { useToast } from '~/composables/useToast'

export function useEditAccess() {
  const isEditAccessModalOpen = ref(false)
  const editingProposal = ref<Proposal | null>(null)

  /**
   * Abre o modal de edição de acesso para uma proposta específica
   */
  function openEditAccessModal(proposal: Proposal) {
    editingProposal.value = proposal
    isEditAccessModalOpen.value = true
  }

  /**
   * Fecha o modal de edição de acesso
   */
  function closeEditAccessModal() {
    isEditAccessModalOpen.value = false
    editingProposal.value = null
  }

  /**
   * Salva as alterações de acesso da proposta
   */
  async function saveAccessChanges(processKey: string, data: { proposalId: string, accessData: any }) {
    const { success, error } = useToast()
    const { updateProposalAccess } = useProposalAccess()

    try {
      // Atualizar via nova API de controle de acesso
      await updateProposalAccess(processKey, data.proposalId, data.accessData)

      success('Permissões de acesso atualizadas com sucesso!')

      // Retornar dados para que o componente pai possa atualizar a lista
      return {
        proposalId: data.proposalId,
        updates: data.accessData
      }
    } catch (err) {
      console.error('Erro ao salvar alterações de acesso:', err)
      error('Erro ao atualizar permissões. Tente novamente.')
      throw err
    }
  }

  /**
   * Atualiza uma proposta local com os novos dados de acesso
   */
  function updateLocalProposal(proposals: Proposal[], proposalId: string, accessData: any): Proposal[] {
    return proposals.map(proposal => {
      if (proposal.id === proposalId) {
        return {
          ...proposal,
          isPublic: accessData.isPublic,
          visibleToUsers: accessData.visibleToUsers,
          externalNotifications: accessData.externalNotifications,
        }
      }
      return proposal
    })
  }

  /**
   * Obtém ícone e cor baseado no tipo de acesso da proposta
   */
  function getAccessIcon(proposal: Proposal) {
    if (proposal.isPublic) {
      return {
        icon: 'fa-solid fa-globe',
        color: 'text-green-400',
        tooltip: 'Público - Todos podem ver'
      }
    }

    const visibleCount = proposal.visibleToUsers?.length || 0
    const externalCount = proposal.externalNotifications?.length || 0

    return {
      icon: 'fa-solid fa-lock',
      color: 'text-amber-400',
      tooltip: `Restrito - ${visibleCount} usuário(s)${externalCount > 0 ? ` + ${externalCount} externo(s)` : ''}`
    }
  }

  /**
   * Verifica se o usuário pode editar as permissões de uma proposta
   */
  function canEditAccess(proposal: Proposal, currentUser: any): boolean {
    // Criador pode sempre editar
    if (proposal.createdBy === currentUser?.id) {
      return true
    }

    // Admin da mesma organização pode sempre editar
    if (currentUser?.superUser || currentUser?.roles?.includes('admin')) {
      if (proposal.organizationId === currentUser?.organizationId) {
        return true
      }
    }

    return false
  }

  return {
    isEditAccessModalOpen,
    editingProposal,
    openEditAccessModal,
    closeEditAccessModal,
    saveAccessChanges,
    updateLocalProposal,
    getAccessIcon,
    canEditAccess
  }
}