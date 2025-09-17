<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Page header -->
    <header class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-6 py-4">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Configurações da Esteira</h1>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Gerencie processos e configurações da esteira
          </p>
        </div>
      </div>
    </header>

    <main class="p-6">
      <div class="bg-gray-800 rounded-lg p-4 space-y-3">
        <div class="space-y-3">
          <div class="flex items-end gap-2 flex-wrap">
            <div class="min-w-[16rem]">
              <label class="text-[12px] text-slate-300">Novo processo (nome)</label>
              <input
                v-model="newProcName"
                class="mt-1 bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="ex.: ConsorEquity"
              />
            </div>
            <button
              class="bg-indigo-600 hover:bg-indigo-700 text-white p-2 rounded-md text-sm disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center w-9 h-9 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              :disabled="creatingProcess"
              @click="createProcess"
              title="Adicionar processo"
              aria-label="Adicionar processo"
            >
              <i
                v-if="creatingProcess"
                class="fa-solid fa-spinner fa-spin"
              ></i>
              <i v-else class="fa-solid fa-plus"></i>
            </button>
            <button
              class="bg-slate-700 hover:bg-slate-600 text-white p-2 rounded-md text-sm w-9 h-9 flex items-center justify-center"
              @click="openResetModal"
              title="Zerar dados locais"
              aria-label="Zerar dados locais"
            >
              <i class="fa-solid fa-rotate-left"></i>
            </button>
          </div>
          <div>
            <label class="text-[12px] text-slate-300">Processos</label>
            <div class="mt-1 space-y-2">
              <div
                v-for="p in processes"
                :key="p.key"
                class="flex items-center justify-between bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2"
              >
                <label
                  class="inline-flex items-center gap-2 cursor-pointer"
                  @click="currentProcessKey = p.key"
                >
                  <input
                    type="radio"
                    class="accent-indigo-500"
                    :checked="currentProcessKey === p.key"
                  />
                  <span class="text-sm font-medium">{{ p.name }}</span>
                  <span class="text-[11px] text-slate-400">({{ p.key }})</span>
                </label>
                <div class="flex items-center gap-2">
                  <span
                    class="text-[11px]"
                    :class="
                      p.active !== false ? 'text-green-300' : 'text-slate-400'
                    "
                  >{{ p.active !== false ? "Ativo" : "Inativo" }}</span>
                  <button
                    class="p-2 text-slate-300 hover:text-white transition-colors"
                    @click.stop="toggleActive(p)"
                    :title="p.active !== false ? 'Desativar' : 'Ativar'"
                    :aria-label="p.active !== false ? 'Desativar' : 'Ativar'"
                  >
                    <i
                      :class="
                        p.active !== false
                          ? 'fa-solid fa-power-off text-sky-400'
                          : 'fa-solid fa-play text-sky-400'
                      "
                    />
                  </button>
                  <button
                    class="p-2 text-slate-300 hover:text-sky-400 transition-colors"
                    @click.stop="editProcess(p)"
                    title="Editar"
                    aria-label="Editar"
                  >
                    <i class="fa-regular fa-pen-to-square"></i>
                  </button>
                  <button
                    class="p-2 text-red-400 hover:text-red-300 transition-colors"
                    @click.stop="openDeleteModal(p)"
                    title="Excluir"
                    aria-label="Excluir"
                  >
                    <i class="fa-regular fa-trash-can"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pipeline Manager Component -->
        <div v-if="currentProcessKey" class="mt-6">
          <PipelineManager
            ref="pipelineManagerRef"
            v-model:stages="pipelineStages"
            :pipeline-key="currentProcessKey"
          />
        </div>
      </div>
    </main>

    <!-- Pipeline Edit Modal -->
    <BaseModal
      v-model="showPipelineModal"
      :title="`Editar Pipeline: ${processName}`"
      size="xl"
    >
      <div class="space-y-4">
        <div>
          <label class="text-[12px] text-slate-300">Nome do processo</label>
          <input
            v-model="processName"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="ex.: ConsorEquity"
          />
        </div>
        <PipelineManager
          ref="pipelineManagerModalRef"
          v-model:stages="pipelineStages"
          :pipeline-key="currentProcessKey"
        />
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="closePipelineModal"
          >
            Fechar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-2 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            @click="savePipeline"
          >
            Salvar
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Delete Confirmation Modal -->
    <BaseModal
      v-model="showDeleteModal"
      title="Confirmar Exclusão"
      size="sm"
    >
      <div class="space-y-4">
        <p class="text-slate-300">
          Tem certeza que deseja excluir o processo "{{ processToDelete?.name }}"?
        </p>
        <p class="text-sm text-slate-400">
          Esta ação não pode ser desfeita.
        </p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="showDeleteModal = false"
          >
            Cancelar
          </button>
          <button
            class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm"
            @click="confirmDelete"
          >
            Excluir
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Reset Confirmation Modal -->
    <BaseModal
      v-model="showResetModal"
      title="Zerar Dados Locais"
      size="sm"
    >
      <div class="space-y-4">
        <p class="text-slate-300">
          Tem certeza que deseja zerar todos os dados locais?
        </p>
        <p class="text-sm text-slate-400">
          Esta ação removerá todos os processos e configurações locais.
        </p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="showResetModal = false"
          >
            Cancelar
          </button>
          <button
            class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm"
            @click="confirmReset"
          >
            Zerar Dados
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import BaseModal from "~/components/ui/BaseModal.vue"
import PipelineManager from "~/components/admin/PipelineManager.vue"
import {
  listProcesses,
  addProcess,
  setProcessActive,
  removeProcess,
  setProcessName,
  sanitizeProcessKey,
  renameProcessKey,
} from "~/composables/usePipeline"
import {
  fetchStagesApi,
  saveStagesPreservingIdsApi,
} from "~/composables/useStages"
import {
  fetchStageFieldsApi,
  saveStageFieldsApi,
} from "~/composables/useStageFields"
import { isApiEnabled } from "~/utils/api/index"
import { useProcessSubmenu } from "~/composables/useProcessMenu"
import { useToast } from "~/composables/useToast"

definePageMeta({
  layout: 'sidebar',
  title: 'Pipeline - Admin'
})

// Composables
const { setLastKey } = useProcessSubmenu()
const { success: toastSuccess, error: toastError, info: toastInfo } = useToast()

// State
const processes = ref(listProcesses())
const currentProcessKey = ref(processes.value[0]?.key || "")
const newProcName = ref("")
const creatingProcess = ref(false)
const processName = ref("")
const pipelineStages = ref([])

// Component refs
const pipelineManagerRef = ref(null)
const pipelineManagerModalRef = ref(null)

// Modals
const showPipelineModal = ref(false)
const showDeleteModal = ref(false)
const showResetModal = ref(false)
const processToDelete = ref(null)

// Functions
async function createProcess() {
  const name = newProcName.value.trim()
  if (!name) return
  // Gera UUID opaco para o processo (mais seguro e estável)
  const key =
    globalThis.crypto?.randomUUID?.() ||
    Math.random().toString(36).slice(2) + Date.now().toString(36)
  if (creatingProcess.value) return
  creatingProcess.value = true
  // Otimista: atualiza DOM primeiro
  processes.value = [...processes.value, { key, name, active: true }]
  const ok = await addProcess(key, name || key)
  if (ok) {
    currentProcessKey.value = key
    setLastKey(key)
    newProcName.value = ""
    toastSuccess("Processo criado")
  } else {
    // rollback visual
    processes.value = processes.value.filter((p) => p.key !== key)
    toastError("Falha ao criar processo. Tente novamente.")
  }
  creatingProcess.value = false
}

function toggleActive(p) {
  setProcessActive(p.key, !(p.active !== false))
  processes.value = listProcesses()
}

async function editProcess(p) {
  currentProcessKey.value = p.key
  const loaded = await fetchStagesApi(p.key)
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : []
  setLastKey(p.key)
  const p_found = processes.value.find((x) => x.key === p.key)
  processName.value = p_found?.name || p.key
  await prefetchStageFields()
  showPipelineModal.value = true
}

function openDeleteModal(p) {
  processToDelete.value = p
  showDeleteModal.value = true
}

async function confirmDelete() {
  const p = processToDelete.value
  if (!p) return

  // Remoção otimista no DOM
  const key = p.key
  processes.value = processes.value.filter((x) => x.key !== key)
  if (currentProcessKey.value === key) {
    currentProcessKey.value = processes.value[0]?.key || ""
    pipelineStages.value = []
    if (currentProcessKey.value) setLastKey(currentProcessKey.value)
  }

  // Efetiva no backend e re-sincroniza silenciosamente
  const ok = await removeProcess(key)
  if (ok) toastSuccess("Processo excluído")
  else {
    // rollback
    processes.value = [...processes.value, p]
    toastError("Falha ao excluir. Tente novamente.")
  }

  showDeleteModal.value = false
  processToDelete.value = null
}

function openResetModal() {
  showResetModal.value = true
}

function confirmReset() {
  // Remove todos os processos e dados locais relacionados
  try {
    const keys = processes.value.map((p) => p.key)
    keys.forEach((k) => removeProcess(k))
    // também limpa o registro explicitamente
    localStorage.removeItem("pipeline_processes")
  } catch (_) {}
  processes.value = []
  currentProcessKey.value = ""
  pipelineStages.value = []
  setLastKey("")
  showResetModal.value = false
}

function closePipelineModal() {
  showPipelineModal.value = false
  processName.value = ""
  // Não zerar pipelineStages aqui - deixar para ser gerenciado pelo watch
}

async function savePipeline() {
  if (currentProcessKey.value) {
    // Quando backend ativo, não renomeamos a key (a API ainda não suporta). Apenas atualiza nome.
    if (!isApiEnabled()) {
      const base = sanitizeProcessKey(
        processName.value || currentProcessKey.value
      )
      const ensureUnique = (k) => {
        const existing = new Set(listProcesses().map((p) => p.key))
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
      processes.value = processes.value.map((p) =>
        p.key === currentProcessKey.value ? { ...p, name: newName } : p
      )
    }
    try {
      // snapshot antes de salvar para migrar IDs de campos
      const prevStages = (pipelineStages.value || []).map((s) => ({
        id: s.id,
        title: s.title,
      }))
      const saved = await saveStagesPreservingIdsApi(
        currentProcessKey.value,
        prevStages,
        pipelineStages.value
      )
      if (Array.isArray(saved) && saved.length) {
        // Atualiza local com IDs reais do backend
        const asClient = saved.map((s) => ({
          id: s.id,
          title: s.title,
          slaDays: s.slaDays,
          color: s.color,
        }))
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
                const ordered = (nextMap[newId] || []).map((f, idx) => ({
                  ...f,
                  order: idx,
                }))
                try {
                  await saveStageFieldsApi(newId, ordered)
                } catch (e) {}
              }
            }
          }
          localStorage.setItem(storageKey, JSON.stringify(nextMap))
        } catch (_) {}
      }
      if (okName) toastSuccess("Esteira salva")
      else toastInfo("Etapas salvas; falha ao renomear o processo")
      showPipelineModal.value = false
    } catch (e) {
      toastError("Falha ao salvar etapas. Verifique a conexão.")
      // mantém o modal aberto para o usuário tentar novamente
      return
    }
  } else {
    showPipelineModal.value = false
  }
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
          nextMap[sid] = arr.map((r) => ({
            id: r.id || String(Date.now()),
            label: r.label,
            type: r.type,
            required: !!r.required,
            placeholder: r.placeholder || "",
            options: Array.isArray(r.options) ? r.options : [],
          }))
        }
      } catch (_) {}
    }
    localStorage.setItem(storageKey, JSON.stringify(nextMap))
  } catch (_) {}
}

// Watch for process key changes
watch(currentProcessKey, async (k) => {
  if (!k) {
    pipelineStages.value = []
    return
  }
  const loaded = await fetchStagesApi(k)
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : []
  setLastKey(k)
  const p = processes.value.find((x) => x.key === k)
  processName.value = p?.name || k
  await prefetchStageFields()
})

// Load/save pipeline config locally
onMounted(async () => {
  if (!currentProcessKey.value) return
  const loaded = await fetchStagesApi(currentProcessKey.value)
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : []
  await prefetchStageFields()
})

// Initialize
if (currentProcessKey.value) {
  setLastKey(currentProcessKey.value)
}
</script>