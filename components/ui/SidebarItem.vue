<template>
  <li
    class="relative group"
    @mouseenter="scheduleOpen"
    @mouseleave="scheduleClose"
    @focusin="scheduleOpen"
    @focusout="scheduleClose"
  >
    <component
      :is="linkTag"
      :to="to"
      :href="href"
      class="flex items-center justify-center w-11 h-11 mx-auto rounded-xl text-slate-300 hover:text-white hover:bg-slate-700/50 transition-colors"
      :class="[{ 'bg-slate-700/60 text-white': active }, itemClass]"
      :aria-label="label"
      :aria-expanded="hasSubmenu ? open : undefined"
      :title="label"
      tabindex="0"
      @focus="scheduleOpen"
      @blur="scheduleClose"
      @click="onClick"
    >
      <slot name="icon">
        <i :class="icon"></i>
      </slot>
    </component>

    <!-- Floating submenu slot -->
    <div
      v-if="hasSubmenu && open"
      class="absolute left-[52px] top-1/2 -translate-y-1/2 ml-1 z-50"
      @mouseenter="scheduleOpen"
      @mouseleave="scheduleClose"
    >
      <slot name="submenu" />
    </div>
  </li>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  icon: { type: String, default: 'fa-solid fa-circle' },
  label: { type: String, required: true },
  to: { type: [String, Object], default: null },
  href: { type: String, default: null },
  active: { type: Boolean, default: false },
  hasSubmenu: { type: Boolean, default: false },
  itemClass: { type: String, default: '' },
  openDelay: { type: Number, default: 120 },
  closeDelay: { type: Number, default: 250 }
})

const open = ref(false)
const linkTag = computed(() => (props.to ? 'NuxtLink' : props.href ? 'a' : 'button'))

let enterTimer = null
let leaveTimer = null

function clearTimers() {
  if (enterTimer) { clearTimeout(enterTimer); enterTimer = null }
  if (leaveTimer) { clearTimeout(leaveTimer); leaveTimer = null }
}

function scheduleOpen() {
  if (!props.hasSubmenu) return
  if (leaveTimer) { clearTimeout(leaveTimer); leaveTimer = null }
  if (open.value) return
  enterTimer = setTimeout(() => { open.value = true }, Math.max(0, props.openDelay))
}

function scheduleClose() {
  if (!props.hasSubmenu) return
  if (enterTimer) { clearTimeout(enterTimer); enterTimer = null }
  leaveTimer = setTimeout(() => { open.value = false }, Math.max(0, props.closeDelay))
}

function onClick(e) {
  if (!props.hasSubmenu) return
  e?.preventDefault?.()
  clearTimers()
  open.value = !open.value
}
</script>

<style scoped>
</style>
