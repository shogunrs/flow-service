export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = ref(false)
  const user = ref<any>(null)

  const login = async (_credentials: unknown) => {
    // Mock login logic
    isAuthenticated.value = true
    user.value = { name: 'User', email: 'user@example.com' }
  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
  }

  return {
    isAuthenticated,
    user,
    login,
    logout
  }
})