import { ref } from 'vue'
import { apiFetch, isApiEnabled } from '~/utils/api/index'

export type CurrentUser = {
  id: string
  roles: string[]
  name?: string
  email?: string
  superUser?: boolean
  organizationId?: string
  fotoPerfilUrl?: string | null
  profileImage?: string | null
  avatarUrl?: string | null
}

const defaultUser: CurrentUser = {
  id: 'u-guest',
  roles: ['guest'],
  name: 'Guest',
  email: '',
  superUser: false,
  organizationId: undefined,
  fotoPerfilUrl: null,
  profileImage: null,
  avatarUrl: null,
}

const state = ref<CurrentUser>({ ...defaultUser })

function normalizeRoles(roles: unknown): string[] {
  if (!Array.isArray(roles)) return ['guest']
  const normalized = roles
    .map((role) => (typeof role === 'string' ? role.trim() : ''))
    .filter(Boolean)
  return normalized.length ? normalized : ['guest']
}

function mergeUser(partial: Partial<CurrentUser>): CurrentUser {
  const next: CurrentUser = {
    id: partial.id || state.value.id || defaultUser.id,
    name: partial.name ?? state.value.name ?? defaultUser.name,
    email: partial.email ?? state.value.email ?? defaultUser.email,
    superUser: partial.superUser ?? state.value.superUser ?? defaultUser.superUser,
    organizationId: partial.organizationId ?? state.value.organizationId ?? defaultUser.organizationId,
    roles: normalizeRoles(partial.roles ?? state.value.roles ?? defaultUser.roles),
    fotoPerfilUrl:
      partial.fotoPerfilUrl ?? state.value.fotoPerfilUrl ?? defaultUser.fotoPerfilUrl,
    profileImage:
      partial.profileImage ?? state.value.profileImage ?? defaultUser.profileImage,
    avatarUrl:
      partial.avatarUrl ??
        partial.fotoPerfilUrl ??
        partial.profileImage ??
        state.value.avatarUrl ??
        state.value.fotoPerfilUrl ??
        state.value.profileImage ??
        defaultUser.avatarUrl,
  }

  return next
}

async function fetchRemoteUser(): Promise<void> {
  if (!isApiEnabled()) return
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('flow-auth-token') : null
  const email = typeof localStorage !== 'undefined' ? localStorage.getItem('flow-auth-email') : null
  if (!token || !email) return

  try {
    const users: any[] = await apiFetch('/api/v1/users', {
      silent: true,
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (!Array.isArray(users) || !users.length) return
    const match = users.find((u) =>
      typeof u?.email === 'string' && u.email.toLowerCase() === email.toLowerCase()
    )
    if (!match) return

    const next = mergeUser({
      id: match.id,
      name: match.name,
      email: match.email,
      roles: Array.isArray(match.roles) ? match.roles : [],
      superUser: match.superUser === true,
      organizationId: match.organizationId,
      fotoPerfilUrl: match.fotoPerfilUrl ?? match.profileImage ?? null,
      profileImage: match.profileImage ?? match.fotoPerfilUrl ?? null,
      avatarUrl: match.fotoPerfilUrl ?? match.profileImage ?? null,
    })
    state.value = next
    try { localStorage.setItem('current-user', JSON.stringify(next)) } catch {}
  } catch (error) {
    console.warn('[Flow] Falha ao atualizar usuário atual via API', error)
  }
}

export function useCurrentUser() {
  function setUser(partial: Partial<CurrentUser>) {
    const next = mergeUser(partial)
    state.value = next
    try { localStorage.setItem('current-user', JSON.stringify(next)) } catch {}
  }

  async function load() {
    try {
      const raw = localStorage.getItem('current-user')
      if (raw) {
        const parsed = JSON.parse(raw)
        state.value = mergeUser(parsed)
      } else {
        state.value = { ...defaultUser }
      }
    } catch {
      state.value = { ...defaultUser }
    }

    await fetchRemoteUser()
  }

  async function refresh() {
    await fetchRemoteUser()
  }

  return { user: state, setUser, load, refresh }
}
