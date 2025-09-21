import { ref } from 'vue'

export type CurrentUser = {
  id: string
  roles: string[]
  name?: string
}

const state = ref<CurrentUser>({ id: 'u-guest', roles: ['guest'], name: 'Guest' })

export function useCurrentUser(){
  function setUser(u: CurrentUser){
    state.value = u
    try { localStorage.setItem('current-user', JSON.stringify(u)) } catch {}
  }
  function load(){
    try { const raw = localStorage.getItem('current-user'); if (raw) state.value = JSON.parse(raw) } catch {}
  }
  return { user: state, setUser, load }
}
