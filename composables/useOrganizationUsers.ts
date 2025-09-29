import { ref } from 'vue'
import { apiFetch, isApiEnabled } from '~/utils/api/index'

export type OrganizationUser = {
  id: string
  name: string
  email: string
  roles: string[]
  organizationId?: string
}

export function useOrganizationUsers() {
  const users = ref<OrganizationUser[]>([])
  const loading = ref(false)

  async function fetchUsers() {
    if (!isApiEnabled()) {
      users.value = []
      return
    }

    loading.value = true
    try {
      const data = await apiFetch<any[]>('/api/v1/users', { silent: true })
      users.value = (data || []).map(user => ({
        id: user.id,
        name: user.name || 'Sem nome',
        email: user.email || '',
        roles: Array.isArray(user.roles) ? user.roles : [],
        organizationId: user.organizationId
      }))
    } catch (error) {
      console.warn('Erro ao buscar usuários da organização:', error)
      users.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    users,
    loading,
    fetchUsers
  }
}