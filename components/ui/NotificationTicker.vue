<template>
  <div class="relative overflow-hidden" :class="containerClass">
    <CanvasNotify v-if="items.length" :items="items" :speed="speed" :font-size="fontSize" :font-family="fontFamily" :glow="glow" :align="align" :offset="offset" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CanvasNotify from '~/components/ui/CanvasNotify.vue'
import { useNotificationCenter } from '~/composables/useNotificationCenter'
import { useCurrentUser } from '~/composables/useCurrentUser'

const props = withDefaults(defineProps<{
  speed?: number,
  fontSize?: number,
  fontFamily?: string,
  glow?: number,
  align?: 'top'|'center'|'bottom',
  offset?: number,
  containerClass?: string
}>(), {
  speed: 0.9,
  fontSize: 13,
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
  glow: 5,
  align: 'bottom',
  offset: 6,
  containerClass: 'h-10'
})

const { visibleFor } = useNotificationCenter()
const { user } = useCurrentUser()

const items = computed(() => {
  const msgs = visibleFor(user.value)
  const colorBy = (sev?: string) => sev === 'success' ? '#22c55e' : sev === 'warning' ? '#f59e0b' : sev === 'error' ? '#ef4444' : '#00E5FF'
  return msgs.map(m => ({ text: m.text, color: colorBy(m.severity) }))
})
</script>

