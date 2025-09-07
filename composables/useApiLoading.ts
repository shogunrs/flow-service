import { computed } from 'vue'
import { useState } from '#imports'

export function useApiLoading() {
  const pending = useState<number>('api-pending', () => 0)
  const isLoading = computed(() => pending.value > 0)
  const inc = () => { pending.value++ }
  const dec = () => { pending.value = Math.max(0, pending.value - 1) }
  return { pending, isLoading, inc, dec }
}
