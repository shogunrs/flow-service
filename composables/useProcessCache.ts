import { useRoute } from '#imports'
import { watch, onUnmounted } from 'vue'
import { useProcesses } from './useProcesses'

export const useProcessCache = () => {
  const route = useRoute()
  const { clearCache } = useProcesses()

  // Verificar se estamos em uma página de processo
  const isProcessPage = (path: string) => {
    return path.startsWith('/esteira/') && path !== '/esteira'
  }

  // Limpar cache quando sair de páginas de processo
  watch(() => route.path, (newPath, oldPath) => {
    // Se estávamos em uma página de processo e saímos dela
    if (oldPath && isProcessPage(oldPath) && !isProcessPage(newPath)) {
      console.log(`🗑️ Saindo da página de processo ${oldPath} - limpando cache`)
      clearCache()
    }
  })

  // Limpar cache quando o componente for desmontado
  onUnmounted(() => {
    if (route.path && isProcessPage(route.path)) {
      console.log('🗑️ Componente desmontado - limpando cache de processos')
      clearCache()
    }
  })

  return {
    clearCache,
    isProcessPage
  }
}