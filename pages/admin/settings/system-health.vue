<template>
  <div class="min-h-screen bg-slate-950 text-slate-100">
    <div class="border-b border-slate-800 bg-slate-900/80 px-6 py-4 backdrop-blur">
      <div class="max-w-6xl mx-auto flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-lg font-semibold">Central de Monitoramento</h1>
          <p class="text-sm text-slate-400">
            Acompanhe a saúde dos serviços e configure alertas vinculados aos status do sistema.
          </p>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-md bg-indigo-500/15 px-4 py-2 text-sm font-semibold text-indigo-300 hover:bg-indigo-500/25"
          @click="refreshAll"
        >
          <i class="fa-solid fa-rotate"></i>
          Atualizar
        </button>
      </div>
    </div>

    <main class="max-w-6xl mx-auto p-6 space-y-6">
      <section class="grid gap-4 sm:grid-cols-3">
        <div class="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
          <p class="text-xs text-slate-400">Pendências</p>
          <p class="mt-2 text-2xl font-semibold text-amber-300">{{ summary.unacknowledgedCount }}</p>
          <p class="text-[11px] text-slate-500 mt-1">Eventos sem reconhecimento</p>
        </div>
        <div
          v-for="(count, severity) in summary.bySeverity"
          :key="severity"
          class="rounded-xl border border-slate-800 bg-slate-900/70 p-4"
        >
          <p class="text-xs text-slate-400">{{ severityLabel(severity) }}</p>
          <p class="mt-2 text-2xl font-semibold">{{ count }}</p>
          <p class="text-[11px] text-slate-500 mt-1">Eventos registrados</p>
        </div>
      </section>

      <section class="rounded-xl border border-slate-800 bg-slate-900/70 p-6 space-y-4">
        <header class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 class="text-sm font-semibold text-slate-100">Serviços monitorados</h2>
            <p class="text-xs text-slate-400">
              Cadastre os componentes que enviarão webhooks de monitoramento.
            </p>
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md bg-slate-800 px-3 py-2 text-xs font-semibold text-slate-200 hover:bg-slate-700"
            @click="openCreateComponent"
          >
            <i class="fa-solid fa-plus"></i>
            Novo serviço
          </button>
        </header>

        <div class="overflow-hidden rounded-lg border border-slate-800/60">
          <table class="min-w-full text-sm">
            <thead class="bg-slate-900/80 text-slate-400 text-xs uppercase tracking-wide">
              <tr>
                <th class="px-3 py-2 text-left">Serviço</th>
                <th class="px-3 py-2 text-left">Status tag</th>
                <th class="px-3 py-2 text-left">Severidade</th>
                <th class="px-3 py-2 text-left">Webhook</th>
                <th class="px-3 py-2 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="component in components"
                :key="component.id"
                class="border-t border-slate-800/60 hover:bg-slate-800/40"
              >
                <td class="px-3 py-2 align-top">
                  <div class="font-medium text-slate-100">{{ component.name }}</div>
                  <p class="text-[11px] text-slate-500">{{ component.description || component.componentKey }}</p>
                </td>
                <td class="px-3 py-2 align-top">
                  <span
                    class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold border"
                    :class="statusBadgeClass(component.statusTag)"
                    :style="statusBadgeStyle(component.statusTag)">
                    <i class="fa-solid fa-tag"></i>
                    {{ component.statusTag || '—' }}
                  </span>
                </td>
                <td class="px-3 py-2 align-top text-xs text-slate-300">
                  {{ component.defaultSeverity || 'WARN' }}
                </td>
                <td class="px-3 py-2 align-top text-xs text-slate-400">
                  <code class="rounded bg-slate-900/80 px-2 py-1 text-[11px]">
                    component: {{ component.componentKey }}
                  </code>
                </td>
                <td class="px-3 py-2 align-top text-right text-xs">
                  <button
                    class="inline-flex items-center gap-1 rounded-md border border-slate-700 px-2 py-1 text-slate-200 hover:bg-slate-800"
                    @click="openEditComponent(component)"
                  >
                    <i class="fa-solid fa-pen"></i>
                    Editar
                  </button>
                  <button
                    class="ml-2 inline-flex items-center gap-1 rounded-md border border-rose-500/70 px-2 py-1 text-rose-300 hover:bg-rose-500/20"
                    @click="removeComponent(component)"
                  >
                    <i class="fa-solid fa-trash"></i>
                    Remover
                  </button>
                </td>
              </tr>
              <tr v-if="!components.length" class="border-t border-slate-800/60">
                <td colspan="5" class="px-3 py-6 text-center text-slate-500 text-sm">
                  Nenhum serviço registrado. Clique em "Novo serviço" para começar.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="rounded-lg border border-slate-800/80 bg-slate-900/60 p-4 text-xs text-slate-400">
          <p class="font-semibold text-slate-200 mb-2">Como integrar</p>
          <ol class="space-y-1 list-decimal list-inside">
            <li>Cadastre o serviço e copie o <code>component</code> sugerido.</li>
            <li>Configure seu serviço para fazer <code>POST /api/v1/system-health/events</code>.</li>
            <li>Envie JSON contendo <code>component</code> e os campos opcionais (<code>statusTag</code>, <code>severity</code>, <code>message</code>, etc.).</li>
            <li>Os eventos aparecerão nesta central e poderão ser reconhecidos pela equipe.</li>
          </ol>
        </div>
      </section>

      <section class="space-y-3">
        <header class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 class="text-sm font-semibold">Eventos recentes</h2>
            <p class="text-xs text-slate-400">
              Visualize alertas recebidos via webhook em tempo real.
            </p>
          </div>
          <div class="flex flex-wrap gap-2 text-xs">
            <select
              v-model="filters.severity"
              class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-xs text-slate-100"
            >
              <option value="">Todas as severidades</option>
              <option value="INFO">Informação</option>
              <option value="WARN">Atenção</option>
              <option value="ERROR">Crítico</option>
            </select>
            <label class="inline-flex items-center gap-2 text-slate-300">
              <input type="checkbox" v-model="filters.onlyUnacknowledged" class="rounded border-slate-600 bg-slate-900" />
              Apenas pendentes
            </label>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-md border border-slate-700 px-3 py-2 text-slate-300 hover:bg-slate-800"
              @click="loadEvents"
            >
              <i class="fa-solid fa-filter"></i>
              Filtrar
            </button>
          </div>
        </header>

        <div class="overflow-hidden rounded-xl border border-slate-800 bg-slate-900/70">
          <table class="min-w-full text-sm">
            <thead class="bg-slate-900/80 text-slate-400 text-xs uppercase tracking-wide">
              <tr>
                <th class="px-3 py-2 text-left">Componente</th>
                <th class="px-3 py-2 text-left">Status</th>
                <th class="px-3 py-2 text-left">Mensagem</th>
                <th class="px-3 py-2 text-left">Ocorrido</th>
                <th class="px-3 py-2 text-left">Reconhecimento</th>
                <th class="px-3 py-2 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="event in events"
                :key="event.id"
                class="border-t border-slate-800/70 hover:bg-slate-800/40"
              >
                <td class="px-3 py-2 align-top">
                  <div class="font-medium text-slate-100">
                    {{ componentMap[event.component || '']?.name || event.component || 'Sem identificador' }}
                  </div>
                  <div class="text-[11px] text-slate-500">
                    {{ componentMap[event.component || '']?.description || event.component }} · {{ event.severity || 'INFO' }}
                  </div>
                </td>
                <td class="px-3 py-2 align-top">
                  <span
                    class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold border"
                    :class="statusBadgeClass(event.statusTag)"
                    :style="statusBadgeStyle(event.statusTag)"
                  >
                    <i class="fa-solid fa-tag"></i>
                    {{ event.statusTag || 'Sem tag' }}
                  </span>
                </td>
                <td class="px-3 py-2 align-top">
                  <div class="text-slate-100">{{ event.message || '—' }}</div>
                  <p v-if="event.details" class="mt-1 text-[11px] text-slate-500">
                    {{ event.details }}
                  </p>
                </td>
                <td class="px-3 py-2 align-top text-slate-300">
                  {{ formatDate(event.occurredAt || event.createdAt) }}
                </td>
                <td class="px-3 py-2 align-top text-xs text-slate-400">
                  <div v-if="event.acknowledged">
                    <p class="text-slate-200">{{ event.acknowledgedBy || 'Equipe' }}</p>
                    <p>{{ formatDate(event.acknowledgedAt) }}</p>
                    <p v-if="event.acknowledgementNotes">{{ event.acknowledgementNotes }}</p>
                  </div>
                  <span v-else class="text-amber-300">Pendente</span>
                </td>
                <td class="px-3 py-2 align-top text-right">
                  <button
                    v-if="!event.acknowledged"
                    type="button"
                    class="inline-flex items-center gap-1 rounded-md bg-emerald-500/15 px-3 py-1 text-xs font-semibold text-emerald-300 hover:bg-emerald-500/25"
                    @click="acknowledge(event)"
                  >
                    <i class="fa-solid fa-check"></i>
                    Reconhecer
                  </button>
                </td>
              </tr>
              <tr v-if="!events.length" class="border-t border-slate-800/70">
                <td class="px-3 py-8 text-center text-slate-500" colspan="6">
                  Nenhum evento encontrado para os filtros selecionados.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="rounded-xl border border-slate-800 bg-slate-900/70 p-6 space-y-4">
        <header>
          <h2 class="text-sm font-semibold text-slate-100">Configuração de alertas</h2>
          <p class="text-xs text-slate-400">
            Defina quais status do catálogo devem disparar alertas e notificações.
          </p>
        </header>

        <div class="space-y-3">
          <label class="text-xs text-slate-300">Status monitorados (categoria SISTEMA)</label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="status in availableStatuses"
              :key="status.id"
              type="button"
              @click="toggleStatusTag(status.name)"
              :class="[
                'inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs transition',
                configForm.monitoredStatusTags.includes(status.name)
                  ? 'border-indigo-500 text-indigo-200 bg-indigo-500/15'
                  : 'border-slate-700 text-slate-300 hover:bg-slate-800/60'
              ]"
              :style="
                configForm.monitoredStatusTags.includes(status.name)
                  ? { borderColor: status.color, backgroundColor: `${status.color}22`, color: status.color }
                  : { borderColor: status.color || '#475569', color: status.color || undefined }
              "
            >
              <span
                class="inline-block h-2 w-2 rounded-full"
                :style="{ backgroundColor: status.color || '#64748b' }"
              ></span>
              {{ status.name }}
            </button>
          </div>
          <p class="text-[11px] text-slate-500">
            Esses status serão destacados quando eventos críticos forem recebidos.
          </p>
        </div>

        <div class="flex justify-end">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md bg-indigo-500/15 px-4 py-2 text-sm font-semibold text-indigo-300 hover:bg-indigo-500/25"
            @click="saveConfig"
          >
            <i class="fa-solid fa-floppy-disk"></i>
            Salvar configuração
          </button>
        </div>
      </section>
    </main>

    <BaseModal
      v-model="componentModalVisible"
      title="Configurar serviço monitorado"
      size="md"
    >
      <div class="space-y-3">
        <div>
          <label class="text-[12px] text-slate-300">Nome do serviço</label>
          <input
            v-model="componentForm.name"
            type="text"
            class="mt-1 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
            placeholder="Ex: backend-api"
          />
        </div>

        <div>
          <label class="text-[12px] text-slate-300">Identificador (component)</label>
          <input
            v-model="componentForm.componentKey"
            type="text"
            class="mt-1 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
            placeholder="ex: backend-api"
          />
          <p class="mt-1 text-[11px] text-slate-500">Será usado no campo <code>component</code> do webhook.</p>
        </div>

        <div>
          <label class="text-[12px] text-slate-300">Status tag padrão</label>
          <select
            v-model="componentForm.statusTag"
            class="mt-1 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
          >
            <option value="">Sem status automático</option>
            <option
              v-for="status in availableStatuses"
              :key="status.id"
              :value="status.name"
            >
              {{ status.name }}
            </option>
          </select>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="text-[12px] text-slate-300">Severidade padrão</label>
            <select
              v-model="componentForm.defaultSeverity"
              class="mt-1 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
            >
              <option v-for="opt in severityOptions" :key="opt" :value="opt">{{ opt }}</option>
            </select>
          </div>
          <label class="text-[12px] text-slate-300 inline-flex items-center gap-2 mt-5 sm:mt-[26px]">
            <input type="checkbox" v-model="componentForm.enabled" class="rounded border-slate-700 bg-slate-900" />
            Ativo
          </label>
        </div>

        <div>
          <label class="text-[12px] text-slate-300">Descrição (opcional)</label>
          <textarea
            v-model="componentForm.description"
            rows="2"
            class="mt-1 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
            placeholder="Link, servidor ou contexto adicional"
          ></textarea>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <button
            type="button"
            class="rounded-md border border-slate-700 px-3 py-2 text-sm text-slate-200 hover:bg-slate-800"
            @click="componentModalVisible = false"
          >
            Cancelar
          </button>
          <button
            type="button"
            class="rounded-md bg-indigo-500/20 px-3 py-2 text-sm font-semibold text-indigo-200 hover:bg-indigo-500/30"
            @click="persistComponent"
          >
            Salvar
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useSystemHealth, type MonitoredComponent } from '~/composables/useSystemHealth'
import { fetchDetailedStatuses, type StatusDTO } from '~/composables/useStatuses'
import { useToast } from '~/composables/useToast'
import BaseModal from '~/components/ui/BaseModal.vue'

const systemHealth = useSystemHealth()
const listEvents = systemHealth.listEvents
const fetchSummary = systemHealth.fetchSummary
const acknowledgeEvent = systemHealth.acknowledgeEvent
const fetchConfig = systemHealth.fetchConfig
const updateConfig = systemHealth.updateConfig
const fetchComponents = systemHealth.fetchComponents
const createComponent = systemHealth.createComponent
const updateComponent = systemHealth.updateComponent
const deleteComponent = systemHealth.deleteComponent
const components = computed(() => systemHealth.components.value)
const componentMap = computed(() => {
  return components.value.reduce<Record<string, MonitoredComponent>>((acc, component) => {
    acc[component.componentKey] = component
    return acc
  }, {})
})
const summary = computed(() =>
  systemHealth.summary.value ?? { bySeverity: {}, unacknowledgedCount: 0, latestPerComponent: {} }
)
const { success: toastSuccess, error: toastError } = useToast()

const events = ref([])
const availableStatuses = ref<StatusDTO[]>([])
const statusColorMap = computed(() => {
  return availableStatuses.value.reduce<Record<string, string>>((acc, status) => {
    acc[status.name] = status.color
    return acc
  }, {})
})
const configForm = reactive({ monitoredStatusTags: [] as string[] })
const componentForm = reactive({
  id: null as string | null,
  name: '',
  componentKey: '',
  statusTag: '',
  defaultSeverity: 'WARN',
  description: '',
  enabled: true,
})
const componentModalVisible = ref(false)
const severityOptions = ['INFO', 'WARN', 'ERROR']

const filters = reactive({
  severity: '',
  onlyUnacknowledged: false,
})

const severityLabel = (value: string) => {
  switch (value?.toUpperCase()) {
    case 'ERROR':
      return 'Crítico'
    case 'WARN':
      return 'Atenção'
    case 'INFO':
    default:
      return 'Informação'
  }
}

const statusBadgeClass = (status?: string) => {
  if (!status) return 'bg-slate-800/60 text-slate-200 border border-slate-700/60'
  return configForm.monitoredStatusTags.includes(status)
    ? 'text-slate-900 border border-transparent'
    : 'text-slate-200'
}

const statusBadgeStyle = (status?: string) => {
  if (!status) return {}
  const baseColor = statusColorMap.value[status]
  if (!baseColor) {
    return configForm.monitoredStatusTags.includes(status)
      ? { backgroundColor: '#6366f1', color: '#0f172a' }
      : {}
  }
  return configForm.monitoredStatusTags.includes(status)
    ? { backgroundColor: baseColor, color: '#0f172a' }
    : { borderColor: baseColor, color: baseColor }
}

const formatDate = (value?: string) => {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

const loadEvents = async () => {
  const query: { acknowledged?: boolean; severity?: string } = {}
  if (filters.severity) query.severity = filters.severity
  if (filters.onlyUnacknowledged) query.acknowledged = false
  events.value = await listEvents(query)
}

const acknowledge = async (event: any) => {
  try {
    await acknowledgeEvent(event.id, { reviewerName: 'Admin Console' })
    toastSuccess('Evento reconhecido com sucesso.')
    await loadEvents()
    await fetchSummary()
  } catch (error) {
    console.error('ack error', error)
    toastError('Não foi possível reconhecer o evento.')
  }
}

const toggleStatusTag = (statusName: string) => {
  const idx = configForm.monitoredStatusTags.indexOf(statusName)
  if (idx >= 0) {
    configForm.monitoredStatusTags.splice(idx, 1)
  } else {
    configForm.monitoredStatusTags.push(statusName)
  }
}

const saveConfig = async () => {
  try {
    await updateConfig({ monitoredStatusTags: [...configForm.monitoredStatusTags] })
    toastSuccess('Configuração de monitoramento atualizada.')
  } catch (error) {
    console.error('config error', error)
    toastError('Não foi possível salvar a configuração.')
  }
}

const refreshAll = async () => {
  await Promise.all([loadEvents(), fetchSummary()])
}

const formatComponentKey = (value: string) => value.trim().toLowerCase().replace(/\s+/g, '-')

const resetComponentForm = () => {
  componentForm.id = null
  componentForm.name = ''
  componentForm.componentKey = ''
  componentForm.statusTag = ''
  componentForm.defaultSeverity = 'WARN'
  componentForm.description = ''
  componentForm.enabled = true
}

const openCreateComponent = () => {
  resetComponentForm()
  componentModalVisible.value = true
}

const openEditComponent = (component: MonitoredComponent) => {
  componentForm.id = component.id
  componentForm.name = component.name
  componentForm.componentKey = component.componentKey
  componentForm.statusTag = component.statusTag || ''
  componentForm.defaultSeverity = component.defaultSeverity || 'WARN'
  componentForm.description = component.description || ''
  componentForm.enabled = component.enabled
  componentModalVisible.value = true
}

const persistComponent = async () => {
  try {
    const payload = {
      name: componentForm.name.trim(),
      componentKey: formatComponentKey(componentForm.componentKey || componentForm.name),
      statusTag: componentForm.statusTag || undefined,
      defaultSeverity: componentForm.defaultSeverity,
      description: componentForm.description || undefined,
      enabled: componentForm.enabled,
    }

    if (!payload.name) {
      toastError('Informe o nome do serviço.');
      return;
    }

    if (componentForm.id) {
      await updateComponent(componentForm.id, payload)
      toastSuccess('Serviço atualizado.')
    } else {
      await createComponent(payload)
      toastSuccess('Serviço cadastrado.')
    }

    await fetchComponents()
    componentModalVisible.value = false
  } catch (error) {
    console.error('component save error', error)
    toastError('Não foi possível salvar o serviço.')
  }
}

const removeComponent = async (component: MonitoredComponent) => {
  if (!confirm(`Remover monitoramento de ${component.name}?`)) return
  try {
    await deleteComponent(component.id)
    toastSuccess('Serviço removido.')
  } catch (error) {
    console.error('component delete error', error)
    toastError('Não foi possível remover o serviço.')
  }
}

onMounted(async () => {
  availableStatuses.value = await fetchDetailedStatuses('SISTEMA')
  const cfg = await fetchConfig()
  configForm.monitoredStatusTags.splice(0, configForm.monitoredStatusTags.length, ...(cfg?.monitoredStatusTags || []))
  await fetchComponents()
  await refreshAll()
})

watch(
  () => componentForm.name,
  (value) => {
    if (!componentForm.id) {
      componentForm.componentKey = formatComponentKey(value || '')
    }
  }
)

definePageMeta({
  layout: 'sidebar',
  title: 'Monitoramento do sistema',
})
</script>
