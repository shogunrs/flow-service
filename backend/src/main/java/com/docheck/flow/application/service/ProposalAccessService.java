package com.docheck.flow.application.service;

import com.docheck.flow.domain.model.Proposal;
import com.docheck.flow.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProposalAccessService {

    /**
     * Verifica se um usuário pode visualizar uma proposta
     */
    public boolean canViewProposal(Proposal proposal, User currentUser) {
        if (proposal == null || currentUser == null) {
            return false;
        }

        // 1. Criador pode sempre ver
        if (proposal.getCreatedBy() != null && proposal.getCreatedBy().equals(currentUser.getId())) {
            return true;
        }

        // 2. Super usuário pode sempre ver
        if (currentUser.isSuperUser()) {
            return true;
        }

        // 3. Admin da mesma organização pode sempre ver
        if (currentUser.getRoles().contains("admin") &&
            proposal.getOrganizationId() != null &&
            proposal.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return true;
        }

        // 4. Se não há dados de controle de acesso, assume público (backward compatibility)
        if (proposal.getCreatedBy() == null && proposal.getIsPublic() == null) {
            return true;
        }

        // 5. Proposta pública pode ser vista por todos da organização
        if (proposal.isPublic()) {
            return proposal.getOrganizationId() == null ||
                   proposal.getOrganizationId().equals(currentUser.getOrganizationId());
        }

        // 6. Proposta restrita - verificar se usuário está na lista
        Set<String> visibleToUsers = proposal.getVisibleToUsers();
        return visibleToUsers != null && visibleToUsers.contains(currentUser.getId());
    }

    /**
     * Verifica se um usuário pode editar o controle de acesso de uma proposta
     */
    public boolean canEditAccessControl(Proposal proposal, User currentUser) {
        if (proposal == null || currentUser == null) {
            return false;
        }

        // 1. Criador pode sempre editar
        if (proposal.getCreatedBy() != null && proposal.getCreatedBy().equals(currentUser.getId())) {
            return true;
        }

        // 2. Super usuário pode sempre editar
        if (currentUser.isSuperUser()) {
            return true;
        }

        // 3. Admin da mesma organização pode sempre editar
        if (currentUser.getRoles().contains("admin") &&
            proposal.getOrganizationId() != null &&
            proposal.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return true;
        }

        return false;
    }

    /**
     * Valida se um usuário pode ser adicionado à lista de acesso de uma proposta
     */
    public boolean canAddUserToAccess(String userId, User currentUser, Proposal proposal) {
        if (userId == null || currentUser == null || proposal == null) {
            return false;
        }

        // Só pode adicionar se pode editar o controle de acesso
        if (!canEditAccessControl(proposal, currentUser)) {
            return false;
        }

        // Não pode remover o criador da lista (mas pode adicionar outros)
        return true;
    }

    /**
     * Valida se um usuário pode ser removido da lista de acesso de uma proposta
     */
    public boolean canRemoveUserFromAccess(String userId, User currentUser, Proposal proposal) {
        if (userId == null || currentUser == null || proposal == null) {
            return false;
        }

        // Só pode remover se pode editar o controle de acesso
        if (!canEditAccessControl(proposal, currentUser)) {
            return false;
        }

        // Não pode remover o criador da lista
        if (proposal.getCreatedBy() != null && proposal.getCreatedBy().equals(userId)) {
            return false;
        }

        return true;
    }

    /**
     * Ensura que o criador sempre esteja na lista de usuários visíveis para propostas restritas
     */
    public void ensureCreatorInAccessList(Proposal proposal) {
        if (proposal == null || proposal.isPublic() || proposal.getCreatedBy() == null) {
            return;
        }

        Set<String> visibleToUsers = proposal.getVisibleToUsers();
        if (visibleToUsers != null && !visibleToUsers.contains(proposal.getCreatedBy())) {
            visibleToUsers.add(proposal.getCreatedBy());
        }
    }
}