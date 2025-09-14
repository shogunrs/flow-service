import { ref, computed, readonly } from 'vue'
import { useState } from '#imports'

export interface LoadingState {
  show: boolean
  title: string
  subtitle: string
  progress: number
  showProgress: boolean
}

export function useModernLoading() {
  const state = useState<LoadingState>('modern-loading', () => ({
    show: false,
    title: 'Carregando...',
    subtitle: 'Aguarde enquanto processamos sua solicitação',
    progress: 0,
    showProgress: false
  }))

  const isLoading = computed(() => state.value.show)

  const showLoading = (options?: Partial<Omit<LoadingState, 'show'>>) => {
    state.value = {
      ...state.value,
      show: true,
      ...options
    }
  }

  const hideLoading = () => {
    state.value.show = false
  }

  const updateProgress = (progress: number) => {
    state.value.progress = Math.min(100, Math.max(0, progress))
    state.value.showProgress = true
  }

  const updateText = (title: string, subtitle?: string) => {
    state.value.title = title
    if (subtitle !== undefined) {
      state.value.subtitle = subtitle
    }
  }

  // Simulação de progresso automático
  const simulateProgress = async (duration = 3000) => {
    state.value.showProgress = true
    state.value.progress = 0
    
    const steps = 100
    const stepDuration = duration / steps
    
    for (let i = 0; i <= steps; i++) {
      if (!state.value.show) break
      
      state.value.progress = i
      await new Promise(resolve => setTimeout(resolve, stepDuration))
    }
  }

  return {
    state: readonly(state),
    isLoading,
    showLoading,
    hideLoading,
    updateProgress,
    updateText,
    simulateProgress
  }
}

// Função utilitária para loading com promise
export async function withModernLoading<T>(
  promise: Promise<T>,
  options?: {
    title?: string
    subtitle?: string
    showProgress?: boolean
    simulateProgress?: boolean
  }
): Promise<T> {
  const { showLoading, hideLoading, simulateProgress } = useModernLoading()
  
  try {
    showLoading({
      title: options?.title || 'Carregando...',
      subtitle: options?.subtitle || 'Aguarde enquanto processamos sua solicitação',
      showProgress: options?.showProgress || false
    })
    
    if (options?.simulateProgress) {
      simulateProgress()
    }
    
    const result = await promise
    return result
  } finally {
    hideLoading()
  }
}