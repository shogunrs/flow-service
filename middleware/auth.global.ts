import { navigateTo, useRuntimeConfig } from '#imports'

export default defineNuxtRouteMiddleware((to) => {
  if (process.server) {
    return
  }

  const token = localStorage.getItem('flow-auth-token')
  const isAuthRoute = to.path === '/' || to.path === '/login'
  const config = useRuntimeConfig()
  const dashboard = config.public?.FLOW_DASHBOARD_URL || '/admin/dashboard'

  if (!token && !isAuthRoute) {
    return navigateTo('/', { replace: true })
  }

  if (token && isAuthRoute) {
    return navigateTo(dashboard, { replace: true })
  }
})
