<template>
  <div class="min-h-screen">
    <Sidebar :items="menu" @select="onSelect" />
    <header class="px-6 py-4 flex items-center justify-between ml-14">
      <h1 class="text-2xl font-bold">Painel Administrativo</h1>
      <div class="text-sm text-slate-300">
        <span class="opacity-80">Admin</span>
        <span class="mx-2 text-slate-500">/</span>
        <span :class="activeTab === 'pipeline' ? 'text-white' : 'opacity-80'">Esteira</span>
        <template v-if="activeTab === 'pipeline' && currentProcessName">
          <span class="mx-2 text-slate-500">/</span>
          <span class="px-2 py-0.5 rounded-md bg-slate-700/60 text-slate-100">{{ currentProcessName }}</span>
        </template>
      </div>
    </header>

    <nav class="px-6 border-b border-gray-700 mb-4 ml-14">
      <div class="flex gap-6">
        <button class="py-3 px-1 border-b-2" :class="activeTab === 'users' ? 'border-orange-500 text-orange-500' : 'border-transparent text-slate-400'" @click="activeTab = 'users'">Usuários</button>
        <button class="py-3 px-1 border-b-2" :class="activeTab === 'pipeline' ? 'border-orange-500 text-orange-500' : 'border-transparent text-slate-400'" @click="activeTab = 'pipeline'">Esteira</button>
        <button class="py-3 px-1 border-b-2" :class="activeTab === 'notifications' ? 'border-orange-500 text-orange-500' : 'border-transparent text-slate-400'" @click="activeTab = 'notifications'">Notificações</button>
      </div>
    </nav>

    <main class="p-6 ml-14">
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
        <h2 class="text-xl font-semibold mb-2">Configurações da Esteira</h2>
        <div class="bg-gray-800 rounded-lg p-4 space-y-3">
          <div class="space-y-3">
            <div class="flex items-end gap-2 flex-wrap">
              <div class="min-w-[16rem]">
                <label class="text-[12px] text-slate-300">Novo processo (nome)</label>
                <input v-model="newProcName" class="mt-1 bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm" placeholder="ex.: ConsorEquity" />
              </div>
              <button class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-2 rounded-md text-sm disabled:opacity-60 disabled:cursor-not-allowed flex items-center gap-2" :disabled="creatingProcess" @click="createProcess">
                <i v-if="creatingProcess" class="fa-solid fa-spinner fa-spin"></i>
                <span v-if="creatingProcess">Adicionando...</span>
                <span v-else>Adicionar</span>
              </button>
              <button class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm" @click="openResetModal">Zerar (local)</button>
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Processos</label>
              <div class="mt-1 space-y-2">
                <div v-for="p in processes" :key="p.key" class="flex items-center justify-between bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2">
                  <label class="inline-flex items-center gap-2 cursor-pointer" @click="currentProcessKey = p.key">
                    <input type="radio" class="accent-orange-500" :checked="currentProcessKey === p.key" />
                    <span class="text-sm font-medium">{{ p.name }}</span>
                    <span class="text-[11px] text-slate-400">({{ p.key }})</span>
                  </label>
                  <div class="flex items-center gap-2">
                    <span class="text-[11px]" :class="p.active !== false ? 'text-green-300' : 'text-slate-400'">{{ p.active !== false ? 'Ativo' : 'Inativo' }}</span>
                    <button class="text-xs bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md" @click.stop="toggleActive(p)">{{ p.active !== false ? 'Desativar' : 'Ativar' }}</button>
                    <button class="text-xs bg-orange-500 hover:bg-orange-600 text-white px-2 py-1 rounded-md" @click.stop="editProcess(p)">Editar</button>
                    <button class="text-xs bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded-md" @click.stop="openDeleteModal(p)">Excluir</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="text-[12px] text-slate-300">
            Selecione um processo acima e clique em <span class="text-orange-400 font-medium">Editar</span> para configurar colunas e inputs no modal.
          </div>
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
        <div>
          <label class="text-[12px] text-slate-300">Nome do Processo</label>
          <input v-model="processName" class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm" placeholder="ex.: ConsorEquity" />
        </div>
        <PipelineManager v-model:stages="pipelineStages" :pipeline-key="currentProcessKey" />
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm" @click="closePipelineModal">Fechar</button>
          <button class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-2 rounded-md text-sm" @click="savePipelineModal">Salvar</button>
        </div>
      </template>
    </BaseModal>
    
    <!-- Modal: Confirmar Exclusão de Processo -->
    <BaseModal v-model="showDeleteModal" title="Confirmar exclusão" size="sm" :z-index="60">
      <div class="space-y-2">
        <p class="text-sm text-slate-200">Para confirmar, digite exatamente o nome do processo abaixo.</p>
        <div class="rounded-md border border-slate-700/60 bg-slate-900/40 px-3 py-2">
          <div class="text-white text-sm font-semibold">{{ deleteTarget?.name || deleteTarget?.key }}</div>
          <div class="text-[11px] text-slate-400">{{ deleteTarget?.key }}</div>
        </div>
        <div>
          <label class="text-[12px] text-slate-300">Digite o nome do processo</label>
          <input
            v-model="deleteConfirm"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            :placeholder="deleteTarget?.name || ''"
            autocomplete="off"
            autocapitalize="off"
            spellcheck="false"
            @paste.prevent
            @drop.prevent
            @copy.prevent
            @cut.prevent
            @contextmenu.prevent
            @keydown="onDeleteConfirmKeydown"
          />
          <div v-if="deleteConfirm && !canConfirmDelete" class="text-[11px] text-red-400 mt-1">O nome não confere.</div>
        </div>
        <p class="text-[12px] text-slate-300">Esta ação remove as colunas, inputs e cards salvos localmente para este processo.</p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm" @click="cancelDelete">Cancelar</button>
          <button class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed" :disabled="!canConfirmDelete" @click="confirmDelete">Excluir</button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Resetar dados locais -->
    <BaseModal v-model="showResetModal" title="Zerar dados locais" size="sm" :z-index="60">
      <div class="space-y-2">
        <p class="text-sm text-slate-200">Isto apagará todos os processos, colunas, inputs e cards salvos no navegador.</p>
        <p class="text-[12px] text-slate-300">Use esta opção apenas para testes locais. Não há como desfazer.</p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm" @click="cancelReset">Cancelar</button>
          <button class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm" @click="confirmReset">Zerar</button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import Sidebar from '~/components/ui/Sidebar.vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import { useRoute } from '#imports'
import PipelineManager from '~/components/admin/PipelineManager.vue'
import { listProcesses, addProcess, setProcessActive, sanitizeProcessKey, removeProcess, setProcessName, renameProcessKey } from '~/composables/usePipeline'
import { fetchStagesApi, saveStagesApi } from '~/composables/useStages'
import { isApiEnabled } from '~/utils/api/index'
import { useProcessSubmenu } from '~/composables/useProcessMenu'
import { useToast } from '~/composables/useToast'
import { fetchStageFieldsApi, saveStageFieldsApi } from '~/composables/useStageFields'

useHead({ title: 'Admin' })
definePageMeta({ layout: 'default' })

const route = useRoute()
const { setLastKey, processes: menuProcesses, getLastKey } = useProcessSubmenu()
const activeTab = ref('users')
const users = ref([
  { id: 1, name: 'Maria Silva', companyName: 'ACME', accessLevel: 'admin', isActive: true },
  { id: 2, name: 'João Souza', companyName: 'Beta Ltda', accessLevel: 'user', isActive: false }
])

const { success: toastSuccess, error: toastError, info: toastInfo } = useToast()

// Sidebar menu items
const menu = computed(() => [
  { key: 'home', label: 'Home', icon: 'fa-solid fa-house', to: '/' },
  {
    key: 'processos',
    label: 'Processos',
    icon: 'fa-solid fa-list-check',
    to: (() => { const last = getLastKey(); return last ? `/esteira/${last}` : '/processos' })(),
    children: [
      { key: 'ver-processos', label: 'Ver Processos', to: '/processos' },
      ...menuProcesses.value.map(p => ({ key: p.key, label: p.name, to: `/esteira/${p.key}` }))
    ]
  },
  {
    key: 'admin',
    label: 'Admin',
    icon: 'fa-solid fa-user-shield',
    to: '/admin',
    children: [
      { key: 'admin-users', label: 'Usuários', href: '/admin?tab=users' },
      { key: 'admin-pipeline', label: 'Gestão de Esteira', href: '/admin?tab=pipeline' },
      { key: 'admin-notifications', label: 'Notificações', href: '/admin?tab=notifications' }
    ]
  }
])

function onSelect(_item) { /* hook de clique no submenu */ }

// Header breadcrumb current process name
const currentProcessName = computed(() => {
  const p = processes.value.find(x => x.key === currentProcessKey.value)
  return p?.name || ''
})

// Process registry
const processes = ref(listProcesses())
const currentProcessKey = ref(processes.value[0]?.key || '')
const newProcName = ref('')
const creatingProcess = ref(false)

async function createProcess() {
  const name = newProcName.value.trim()
  if (!name) return
  // Gera UUID opaco para o processo (mais seguro e estável)
  const key = (globalThis.crypto?.randomUUID?.() || Math.random().toString(36).slice(2) + Date.now().toString(36))
  if (creatingProcess.value) return
  creatingProcess.value = true
  // Otimista: atualiza DOM primeiro
  processes.value = [...processes.value, { key, name, active: true }]
  const ok = await addProcess(key, name || key)
  if (ok) {
    currentProcessKey.value = key
    setLastKey(key)
    newProcName.value = ''
    toastSuccess('Processo criado')
  } else {
    // rollback visual
    processes.value = processes.value.filter(p => p.key !== key)
    toastError('Falha ao criar processo. Tente novamente.')
  }
  creatingProcess.value = false
}

const pipelineStages = ref([
  { id: 'dados_basicos', title: 'Dados Básicos', slaDays: 2, color: 'sky' },
  { id: 'documentacao', title: 'Documentação', slaDays: 5, color: 'indigo' }
])
const processName = ref('')

// Modal Gestão da Esteira
const showPipelineModal = ref(false)
function openPipelineModal() { showPipelineModal.value = true }
function closePipelineModal() { showPipelineModal.value = false }
async function savePipelineModal() {
  if (currentProcessKey.value) {
    // Quando backend ativo, não renomeamos a key (a API ainda não suporta). Apenas atualiza nome.
    if (!isApiEnabled()) {
      const base = sanitizeProcessKey(processName.value || currentProcessKey.value)
      const ensureUnique = (k) => {
        const existing = new Set(listProcesses().map(p => p.key))
        if (!existing.has(k) || k === currentProcessKey.value) return k
        let i = 2
        while (existing.has(`${k}-${i}`)) i++
        return `${k}-${i}`
      }
      const desiredKey = ensureUnique(base)
      if (desiredKey !== currentProcessKey.value) {
        const res = renameProcessKey(currentProcessKey.value, desiredKey)
        if (!res.ok) return
        currentProcessKey.value = desiredKey
        setLastKey(desiredKey)
      }
    }
    const newName = processName.value || currentProcessKey.value
    const okName = await setProcessName(currentProcessKey.value, newName)
    if (okName) {
      // Atualiza lista local sem novo GET
      processes.value = processes.value.map(p => p.key === currentProcessKey.value ? { ...p, name: newName } : p)
    }
    try {
      // snapshot antes de salvar para migrar IDs de campos
      const prevStages = (pipelineStages.value || []).map(s => ({ id: s.id, title: s.title }))
      const saved = await saveStagesApi(currentProcessKey.value, pipelineStages.value)
      if (Array.isArray(saved) && saved.length) {
        // Atualiza local com IDs reais do backend
        const asClient = saved.map((s) => ({ id: s.id, title: s.title, slaDays: s.slaDays, color: s.color }))
        pipelineStages.value = asClient

        // Migra mapa de forms local dos IDs antigos para os novos e persiste no backend
        try {
          const storageKey = `pipeline_stage_forms__${currentProcessKey.value}`
          const raw = localStorage.getItem(storageKey)
          const map = raw ? JSON.parse(raw) || {} : {}
          const nextMap = { ...map }
          for (let i = 0; i < asClient.length; i++) {
            const oldId = prevStages[i]?.id
            const newId = asClient[i]?.id
            if (!oldId || !newId || oldId === newId) continue
            if (map[oldId]) {
              nextMap[newId] = map[oldId]
              delete nextMap[oldId]
              // também envia para o backend, preservando ordem
              if (isApiEnabled() && /^[a-fA-F0-9]{24}$/.test(newId)) {
                const ordered = (nextMap[newId] || []).map((f, idx) => ({ ...f, order: idx }))
                try { await saveStageFieldsApi(newId, ordered) } catch (e) {}
              }
            }
          }
          localStorage.setItem(storageKey, JSON.stringify(nextMap))
        } catch (_) {}
      }
      if (okName) toastSuccess('Esteira salva')
      else toastInfo('Etapas salvas; falha ao renomear o processo')
      showPipelineModal.value = false
    } catch (e) {
      toastError('Falha ao salvar etapas. Verifique a conexão.')
      // mantém o modal aberto para o usuário tentar novamente
      return
    }
  } else {
    showPipelineModal.value = false
  }
}

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
onMounted(async () => {
  if (!currentProcessKey.value) return
  const loaded = await fetchStagesApi(currentProcessKey.value)
  pipelineStages.value = Array.isArray(loaded) ? loaded.map((s) => ({ id: s.id, title: s.title, slaDays: s.slaDays, color: s.color })) : []
  await prefetchStageFields()
})

watch(currentProcessKey, async (k) => {
  if (!k) { pipelineStages.value = []; return }
  const loaded = await fetchStagesApi(k)
  pipelineStages.value = Array.isArray(loaded) ? loaded.map((s) => ({ id: s.id, title: s.title, slaDays: s.slaDays, color: s.color })) : []
  setLastKey(k)
  const p = processes.value.find(x => x.key === k)
  processName.value = p?.name || k
  await prefetchStageFields()
})

// Persistiremos as etapas somente ao salvar no modal

function toggleActive(p) {
  setProcessActive(p.key, !(p.active !== false))
  processes.value = listProcesses()
}

async function editProcess(p) {
  currentProcessKey.value = p.key
  const loaded = await fetchStagesApi(p.key)
  pipelineStages.value = Array.isArray(loaded) ? loaded.map((s) => ({ id: s.id, title: s.title, slaDays: s.slaDays, color: s.color })) : []
  setLastKey(p.key)
  processName.value = p.name || p.key
  await prefetchStageFields()
  openPipelineModal()
}

// Busca os fields do backend por etapa (para mostrar contagem e já aquecer o builder)
async function prefetchStageFields() {
  if (!isApiEnabled()) return
  try {
    const storageKey = `pipeline_stage_forms__${currentProcessKey.value}`
    const raw = localStorage.getItem(storageKey)
    const map = raw ? JSON.parse(raw) || {} : {}
    const nextMap = { ...map }
    for (const st of pipelineStages.value || []) {
      const sid = st?.id
      if (!sid || !/^[a-fA-F0-9]{24}$/.test(sid)) continue
      try {
        const arr = await fetchStageFieldsApi(sid)
        if (Array.isArray(arr)) {
          nextMap[sid] = arr.map(r => ({
            id: r.id || String(Date.now()),
            label: r.label,
            type: r.type,
            required: !!r.required,
            placeholder: r.placeholder || '',
            options: Array.isArray(r.options) ? r.options : [],
          }))
        }
      } catch (_) {}
    }
    localStorage.setItem(storageKey, JSON.stringify(nextMap))
  } catch (_) {}
}

// Delete modal state/handlers
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
const deleteConfirm = ref('')
const canConfirmDelete = computed(() => {
  const target = (deleteTarget.value?.name || '').trim().toLowerCase()
  const typed = deleteConfirm.value.trim().toLowerCase()
  return !!target && typed === target
})
const showResetModal = ref(false)
function openDeleteModal(p) { deleteTarget.value = p; deleteConfirm.value = ''; showDeleteModal.value = true }
function cancelDelete() { showDeleteModal.value = false; deleteTarget.value = null; deleteConfirm.value = '' }
async function confirmDelete() {
  const p = deleteTarget.value
  if (!p || !canConfirmDelete.value) return

  // Remoção otimista no DOM
  const key = p.key
  processes.value = processes.value.filter(x => x.key !== key)
  if (currentProcessKey.value === key) {
    currentProcessKey.value = processes.value[0]?.key || ''
    pipelineStages.value = []
    if (currentProcessKey.value) setLastKey(currentProcessKey.value)
  }

  // Efetiva no backend e re-sincroniza silenciosamente
  const ok = await removeProcess(key)
  if (ok) toastSuccess('Processo excluído')
  else {
    // rollback
    processes.value = [...processes.value, p]
    toastError('Falha ao excluir. Tente novamente.')
  }
  // Não recarrega toda a lista para evitar GET extra; o evento já atualizará quem escuta

  cancelDelete()
}

function onDeleteConfirmKeydown(e) {
  // Block paste shortcuts (Cmd/Ctrl+V) and select-all paste combos
  if ((e.ctrlKey || e.metaKey) && (e.key.toLowerCase() === 'v')) {
    e.preventDefault()
    return
  }
}

function openResetModal() { showResetModal.value = true }
function cancelReset() { showResetModal.value = false }
function confirmReset() {
  // Remove todos os processos e dados locais relacionados
  try {
    const keys = processes.value.map(p => p.key)
    keys.forEach(k => removeProcess(k))
    // também limpa o registro explicitamente
    localStorage.removeItem('pipeline_processes')
  } catch (_) {}
  processes.value = []
  currentProcessKey.value = ''
  pipelineStages.value = []
  setLastKey('')
  cancelReset()
}

</script>

<style scoped>
body { font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
