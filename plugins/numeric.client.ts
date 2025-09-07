import { type NumericOptions, attachNumericInput } from '~/utils/inputs/numeric'

export default defineNuxtPlugin((nuxtApp) => {
  // Provide helper for programmatic usage: const { $numeric } = useNuxtApp()
  nuxtApp.provide('numeric', (el: HTMLInputElement, opts?: NumericOptions) => attachNumericInput(el, opts))

  // Global directive: v-numeric="{ allowDecimal: false }"
  nuxtApp.vueApp.directive('numeric', {
    mounted(el, binding) {
      const input = (el instanceof HTMLInputElement ? el : (el.querySelector('input') as HTMLInputElement))
      if (!input) return
      const cleanup = attachNumericInput(input, binding?.value || {})
      ;(el as any).__numericCleanup = cleanup
    },
    updated(el, binding) {
      // Re-attach if options object identity changed
      const cleanup = (el as any).__numericCleanup as (() => void) | undefined
      if (cleanup) cleanup()
      const input = (el instanceof HTMLInputElement ? el : (el.querySelector('input') as HTMLInputElement))
      if (!input) return
      const newCleanup = attachNumericInput(input, binding?.value || {})
      ;(el as any).__numericCleanup = newCleanup
    },
    unmounted(el) {
      const cleanup = (el as any).__numericCleanup as (() => void) | undefined
      if (cleanup) cleanup()
      delete (el as any).__numericCleanup
    }
  })
})

declare module '#app' {
  interface NuxtApp {
    $numeric: (el: HTMLInputElement, opts?: NumericOptions) => () => void
  }
}

declare module 'vue' {
  interface ComponentCustomProperties {
    $numeric: (el: HTMLInputElement, opts?: NumericOptions) => () => void
  }
}

