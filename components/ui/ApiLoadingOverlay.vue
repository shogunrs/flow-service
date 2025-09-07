<template>
  <ClientOnly>
    <LoadingSpinner :show="visible" fullscreen />
  </ClientOnly>
  
</template>

<script setup>
import LoadingSpinner from '~/components/ui/LoadingSpinner.vue'
import { useApiLoading } from '~/composables/useApiLoading'
import { ref, watch } from 'vue'

const { isLoading } = useApiLoading()
const visible = ref(false)
let timer = null

// Pequeno atraso para evitar piscar em requisições muito rápidas
watch(isLoading, (v) => {
  if (v) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => { visible.value = true }, 150)
  } else {
    if (timer) { clearTimeout(timer); timer = null }
    visible.value = false
  }
}, { immediate: true })
</script>
