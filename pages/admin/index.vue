<template>
  <div class="min-h-screen">
    <header class="px-6 py-4 flex items-center justify-between">
      <h1 class="text-2xl font-bold">Painel Administrativo</h1>
      <div class="flex items-center gap-2">
        <button
          class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-2 rounded-lg text-sm flex items-center gap-2"
          @click="openPipelineModal"
        >
          <i class="fa-solid fa-diagram-project"></i>
          Gestão da Esteira
        </button>
      </div>
    </header>

    <nav class="px-6 border-b border-gray-700 mb-4">
      <div class="flex gap-6">
        <button class="py-3 px-1 border-b-2" :class="activeTab === 'users' ? 'border-orange-500 text-orange-500' : 'border-transparent text-slate-400'" @click="activeTab = 'users'">Usuários</button>
        <button class="py-3 px-1 border-b-2" :class="activeTab === 'pipeline' ? 'border-orange-500 text-orange-500' : 'border-transparent text-slate-400'" @click="activeTab = 'pipeline'">Esteira</button>
        <button class="py-3 px-1 border-b-2" :class="activeTab === 'notifications' ? 'border-orange-500 text-orange-500' : 'border-transparent text-slate-400'" @click="activeTab = 'notifications'">Notificações</button>
      </div>
    </nav>

    <main class="p-6">
      <section v-if="activeTab === 'users'">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold">Usuários</h2>
          <button class="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-lg">Novo Usuário</button>
        </div>
        <div class="bg-gray-800 rounded-lg overflow-x-auto">
          <table class="w-full text-sm text-left">
            <thead class="bg-gray-700/60 text-xs text-slate-300 uppercase">
              <tr>
                <th class="px-6 py-3">Nome</th>
                <th class="px-6 py-3">Empresa</th>
                <th class="px-6 py-3">Acesso</th>
                <th class="px-6 py-3">Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id" class="border-b border-gray-700">
                <td class="px-6 py-3">{{ u.name }}</td>
                <td class="px-6 py-3">{{ u.companyName }}</td>
                <td class="px-6 py-3">{{ u.accessLevel }}</td>
                <td class="px-6 py-3">{{ u.isActive ? 'Ativo' : 'Inativo' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-else-if="activeTab === 'pipeline'">
        <h2 class="text-xl font-semibold mb-4">Configurações da Esteira</h2>
        <div class="bg-gray-800 rounded-lg p-4 space-y-3">
          <PipelineManager v-model:stages="pipelineStages" pipeline-key="quotaequity" />
        </div>
      </section>

      <section v-else>
        <h2 class="text-xl font-semibold mb-4">Notificações</h2>
        <div class="bg-gray-800 rounded-lg p-4">
          <p class="text-slate-300">Configurações simples de exemplo.</p>
        </div>
      </section>
    </main>

    <!-- Modal: Gestão da Esteira (BaseModal) -->
    <BaseModal v-model="showPipelineModal" title="Gestão da Esteira" size="lg" :z-index="55">
      <div class="space-y-3 max-h-[70vh] overflow-y-auto pr-1">
        <PipelineManager v-model:stages="pipelineStages" pipeline-key="quotaequity" />
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm" @click="closePipelineModal">Fechar</button>
          <button class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-2 rounded-md text-sm" @click="closePipelineModal">Salvar</button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import { useRoute, useRouter } from '#imports'
import PipelineManager from '~/components/admin/PipelineManager.vue'

useHead({ title: 'Admin' })
definePageMeta({ layout: 'default' })

const route = useRoute()
const router = useRouter()
const activeTab = ref('users')
const users = ref([
  { id: 1, name: 'Maria Silva', companyName: 'ACME', accessLevel: 'admin', isActive: true },
  { id: 2, name: 'João Souza', companyName: 'Beta Ltda', accessLevel: 'user', isActive: false }
])

const pipelineStages = ref([
  { id: 'dados_basicos', title: 'Dados Básicos', slaDays: 2, color: 'sky' },
  { id: 'documentacao', title: 'Documentação', slaDays: 5, color: 'indigo' }
])

// Modal Gestão da Esteira
const showPipelineModal = ref(false)
function openPipelineModal() { showPipelineModal.value = true }
function closePipelineModal() { showPipelineModal.value = false }

// Sync tab with query (?tab=users|pipeline|notifications)
watch(
  () => route.query.tab,
  (t) => {
    const tab = String(t || 'users')
    if (['users', 'pipeline', 'notifications'].includes(tab)) activeTab.value = tab
  },
  { immediate: true }
)

// Load/save pipeline config locally
onMounted(() => {
  try {
    const raw = localStorage.getItem('pipeline_config__quotaequity')
    if (raw) {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) pipelineStages.value = parsed
    }
  } catch (_) {}
})

watch(pipelineStages, (v) => {
  try { localStorage.setItem('pipeline_config__quotaequity', JSON.stringify(v)) } catch (_) {}
}, { deep: true })

</script>

<style scoped>
body { font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
