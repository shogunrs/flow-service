<template>
  <ClientOnly>
    <Teleport to="body">
      <ModernLoading
        :show="state.show"
        :title="state.title"
        :subtitle="state.subtitle"
        :progress="state.progress"
        :show-progress="state.showProgress"
        fullscreen
        overlay
      />
    </Teleport>
  </ClientOnly>
</template>

<script setup>
import ModernLoading from '~/components/ui/ModernLoading.vue'
import { useModernLoading } from '~/composables/useModernLoading'

const { state } = useModernLoading()
</script>