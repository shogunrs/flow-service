<template>
  <aside
    :class="[
      'fixed left-0 top-0 h-screen w-14 z-40 border-r border-slate-800/80 bg-slate-900/95 backdrop-blur-sm',
      'flex flex-col items-stretch py-2 overflow-visible'
    ]"
  >
    <!-- Top brand/logo slot -->
    <div class="flex items-center justify-center h-12 mb-2">
      <slot name="brand">
        <i class="fa-solid fa-gem text-orange-400"></i>
      </slot>
    </div>

    <!-- Main items -->
    <nav class="flex-1 overflow-visible px-1">
      <ul class="space-y-2">
        <SidebarItem
          v-for="it in items"
          :key="it.key || it.label"
          :icon="it.icon"
          :label="it.label"
          :to="it.to"
          :href="it.href"
          :active="isActive(it)"
          :has-submenu="Array.isArray(it.children) && it.children.length > 0"
        >
          <template #icon>
            <i :class="[it.icon, 'text-[18px]']"></i>
          </template>
          <template v-if="it.children?.length" #submenu>
            <SidebarSubmenu :items="it.children" @select="onSelect" />
          </template>
        </SidebarItem>
      </ul>
    </nav>

    <!-- Bottom actions -->
    <div class="mt-2 px-1">
      <ul class="space-y-2">
        <slot name="footer">
          <SidebarItem icon="fa-solid fa-gear" label="Configurações" :has-submenu="true">
            <template #submenu>
              <SidebarSubmenu :items="defaultFooter" @select="onSelect" />
            </template>
          </SidebarItem>
        </slot>
      </ul>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from '#imports'
import SidebarItem from './SidebarItem.vue'
import SidebarSubmenu from './SidebarSubmenu.vue'

const props = defineProps({
  items: { type: Array, default: () => [] }
})

const emit = defineEmits(['select'])
const route = useRoute()

const defaultFooter = [
  { key: 'profile', label: 'Perfil', icon: 'fa-regular fa-user', to: '/perfil' },
  { key: 'logout', label: 'Sair', icon: 'fa-solid fa-right-from-bracket', to: '/logout' }
]

function isActive(it) {
  if (it?.active) return true
  const to = it?.to
  if (!to) return false
  const path = typeof to === 'string' ? to : to?.path || ''
  if (!path) return false
  return route.path.startsWith(path)
}

function onSelect(item) {
  emit('select', item)
}
</script>

<style scoped>
</style>
