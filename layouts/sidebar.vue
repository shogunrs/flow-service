<template>
  <div class="min-h-screen bg-slate-950">
    <!-- Sidebar -->
    <Sidebar v-model="mobileMenuOpen" @collapsed="sidebarCollapsed = $event" />

    <!-- Main content area -->
    <div :class="['min-h-screen transition-all duration-300 lg:pl-0', sidebarCollapsed ? 'lg:ml-16' : 'lg:ml-64']">
      <!-- Top header (optional) -->
      <header class="bg-slate-900 border-b border-slate-700 lg:hidden">
        <div class="flex items-center justify-between px-4 py-3">
          <h1 class="text-lg font-semibold text-white">
            {{ pageTitle }}
          </h1>
          <button
            @click="mobileMenuOpen = true"
            class="p-2 rounded-md text-slate-300 hover:bg-slate-800"
          >
            <i class="fa-solid fa-bars text-lg"></i>
          </button>
        </div>
      </header>

      <!-- Page content -->
      <main class="flex-1">
        <slot />
      </main>
    </div>

    <!-- Global components -->
    <ApiLoadingOverlay />
    <ToastContainer position="bottom-center" />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from '#imports'
import Sidebar from '~/components/ui/Sidebar.vue'
import ApiLoadingOverlay from '~/components/ui/ApiLoadingOverlay.vue'
import ToastContainer from '~/components/ui/ToastContainer.vue'
import { useProcessCache } from '~/composables/useProcessCache'

// Composables
const route = useRoute()

// Ativar monitoramento de cache de processos
useProcessCache()

// State
const mobileMenuOpen = ref(false)
const sidebarCollapsed = ref(false)

// Computed
const pageTitle = computed(() => {
  const path = route.path

  if (path === '/') return 'Dashboard'
  if (path.startsWith('/admin')) return 'Administração'
  if (path.startsWith('/esteira')) return 'Esteira'
  if (path.startsWith('/processos')) return 'Processos'

  return 'DoCheck'
})

// Auto-close mobile menu on route change
watch(() => route.path, () => {
  mobileMenuOpen.value = false
})
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: "Inter", sans-serif;
}

/* Ensure sidebar doesn't interfere with other fixed elements */
.lg\:ml-64 {
  transition: margin-left 0.3s ease-in-out;
}

/* Custom scrollbar for main content */
main::-webkit-scrollbar {
  width: 8px;
}

main::-webkit-scrollbar-track {
  background: #f1f5f9;
}

main::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

main::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Dark mode scrollbar */
.dark main::-webkit-scrollbar-track {
  background: #1e293b;
}

.dark main::-webkit-scrollbar-thumb {
  background: #475569;
}

.dark main::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}
</style>