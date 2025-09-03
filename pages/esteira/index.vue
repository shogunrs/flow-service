<template>
  <div class="min-h-screen">
    <!-- Header -->
    <header class="p-3 border-b border-slate-800">
      <div class="w-full sm:grid sm:grid-cols-3 sm:items-center gap-2">
        <!-- Left: (reserved for menu breadcrumb/title later) -->
        <div class="hidden sm:block"></div>

        <!-- Center: global search, centered and proportional width -->
        <div class="px-0 sm:px-4 justify-self-center w-full max-w-xl">
          <AiSearch
            v-model="filterText"
            aiLabel="IA"
            size="md"
            @ai="openAiChat"
          />
        </div>

        <!-- Right: controls -->
        <div class="flex items-center gap-2 mt-3 sm:mt-0 justify-self-end">
        <!-- View Mode Toggle -->
        <div class="bg-slate-800/80 p-0.5 rounded-md flex items-center">
          <button
            @click="viewMode = 'kanban'"
            :class="
              viewMode === 'kanban'
                ? 'bg-orange-500 text-white'
                : 'text-slate-300 hover:bg-slate-700/80'
            "
            class="px-2 py-0.5 rounded-md text-[11px] font-medium transition-colors flex items-center gap-1"
          >
            <i class="fa-solid fa-grip-vertical"></i> Kanban
          </button>
          <button
            @click="viewMode = 'list'"
            :class="
              viewMode === 'list'
                ? 'bg-orange-500 text-white'
                : 'text-slate-300 hover:bg-slate-700/80'
            "
            class="px-2 py-0.5 rounded-md text-[11px] font-medium transition-colors flex items-center gap-1"
          >
            <i class="fa-solid fa-list"></i> Lista
          </button>
        </div>
        <!-- New proposal -->
        <button
          @click="openNewProposalModal"
          class="bg-orange-500 hover:bg-orange-600 text-white font-medium px-2.5 py-1 rounded-md transition-colors flex items-center gap-1 text-xs"
        >
          <i class="fa-solid fa-plus text-[10px]"></i>
          <span class="hidden sm:inline">Nova Proposta</span>
        </button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="p-6">
      <!-- Kanban View -->
      <div v-if="viewMode === 'kanban'" class="overflow-x-auto kanban-scroll">
        <div class="flex gap-2 min-h-[10rem] pb-2">
          <div
            v-for="stage in stages"
            :key="stage.id"
            class="w-64 flex-shrink-0 bg-slate-800 rounded-md p-1.5 transition-colors"
            :class="
              dragOverStage === stage.id
                ? 'ring-2 ring-blue-400 ring-offset-0'
                : ''
            "
            @dragover.prevent
            @dragenter.prevent="onDragEnter(stage.id)"
            @dragleave="onDragLeave(stage.id)"
            @drop="onDrop(stage.id)"
          >
            <div
              :class="['h-1 rounded-md mb-2', stageStripClass(stage.id)]"
            ></div>
            <!-- Column header -->
            <div class="flex items-center justify-between mb-1.5">
              <div class="flex items-center gap-2">
                <h3 class="font-semibold text-xs">{{ stage.title }}</h3>
                <span v-if="stage.auto" class="text-[10px] px-1.5 py-0.5 rounded-full bg-slate-700 text-slate-300 border border-slate-600/60">auto</span>
                <span class="text-[11px] text-slate-400">SLA {{ stage.slaDays }}d</span>
              </div>
              <span
                :class="[
                  'text-[10px] font-semibold px-2 py-0.5 rounded-full',
                  stageBadgeClass(stage.id),
                ]"
                >{{ filteredByStage(stage.id).length }}</span
              >
            </div>
            <!-- Cards -->
            <div class="space-y-1.5 min-h-10">
              <div
                v-for="p in filteredByStage(stage.id)"
                :key="p.id"
                class="bg-slate-700 rounded-md p-1.5 shadow-sm cursor-move border-l-4"
                :class="[
                  draggedId === p.id ? 'opacity-60' : '',
                  stageBorderClass(p.stageId),
                ]"
                draggable="true"
                @dragstart="onDragStart($event, p)"
                @dragend="onDragEnd"
              >
                <div class="flex items-start justify-between">
                  <div>
                    <div class="font-medium leading-tight text-xs">
                      {{ p.name }}
                    </div>
                    <div class="text-[11px] text-slate-300">
                      R$ {{ p.amount.toLocaleString("pt-BR") }}
                    </div>
                  </div>
                  <span
                    :class="statusPillClass(p.status)"
                    class="text-[10px] font-semibold px-1.5 py-0.5 rounded-full"
                  >
                    {{ p.status }}
                  </span>
                </div>
                <!-- SLA row -->
                <div class="mt-1 flex items-center gap-1.5">
                  <span
                    :class="[
                      'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[10px] font-semibold',
                      slaBadgeClass(p),
                    ]"
                  >
                    <i class="fa-regular fa-calendar"></i>
                    {{ slaLabel(p) }}
                  </span>
                  <span
                    v-if="getSlaInfo(p).status === 'overdue'"
                    class="inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[10px] font-semibold bg-red-900/60 text-red-300"
                  >
                    <i class="fa-solid fa-flag"></i>
                    Atraso
                  </span>
                </div>
              </div>
              <div
                v-if="filteredByStage(stage.id).length === 0"
                class="text-sm text-slate-400"
              >
                Sem propostas
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- List View -->
      <div v-else class="bg-slate-800 rounded-lg overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead class="bg-slate-700/60 text-xs text-slate-300 uppercase">
            <tr>
              <th class="px-6 py-3">Cliente</th>
              <th class="px-6 py-3">Valor</th>
              <th class="px-6 py-3">Etapa</th>
              <th class="px-6 py-3">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="p in filteredProposals"
              :key="p.id"
              class="border-b border-slate-700 hover:bg-slate-700/50"
            >
              <td class="px-6 py-3">{{ p.name }}</td>
              <td class="px-6 py-3">
                R$ {{ p.amount.toLocaleString("pt-BR") }}
              </td>
              <td class="px-6 py-3">{{ stageTitle(p.stageId) }}</td>
              <td class="px-6 py-3">
                <span
                  :class="statusPillClass(p.status)"
                  class="text-xs font-semibold px-2 py-1 rounded-full"
                >
                  {{ p.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <!-- New Proposal Modal using BaseModal -->
    <BaseModal v-model="showNewModal" title="Nova Proposta" size="md" :body-class="'new-proposal-modal'">
      <div class="space-y-2">
              <!-- Linha 1: Nome, CPF, CEP (abaixo do Nome), Administradora (abaixo do CPF) -->
              <div class="grid grid-cols-12 gap-2">
                <div class="col-span-12 md:col-span-6">
                  <BaseInput
                    v-model="newProposal.name"
                    label="Nome"
                    size="xs"
                    placeholder="Digite aqui..."
                    :error-message="formErrors.name"
                  />
                </div>
                <div class="col-span-12 md:col-span-6">
                  <BaseInput
                    v-model="newProposal.cpf"
                    label="CPF"
                    size="xs"
                    placeholder="000.000.000-00"
                    :error-message="formErrors.cpf"
                  />
                </div>
                <div class="col-span-12 md:col-span-6">
                  <BaseInputGroup
                    v-model="newProposal.cep"
                    label="CEP"
                    size="xs"
                    placeholder="00000-000"
                    append-text="Buscar"
                    :loading="cepLoading"
                    :error-message="cepError"
                    @append-click="fetchCep"
                  />
                </div>
                <div class="col-span-12 md:col-span-6">
                  <BaseSelect
                    v-model="newProposal.administrator"
                    label="Administradora"
                    size="xs"
                    :options="
                      adminOptions.map((opt) => ({ label: opt, value: opt }))
                    "
                  />
                </div>
              </div>

              <!-- Linha 2: Cidade, Estado, Banco de Cotas, Fundo -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                <BaseInput
                  v-model="newProposal.city"
                  label="Cidade"
                  size="xs"
                  placeholder="Cidade"
                />
                <BaseSelect
                  v-model="newProposal.state"
                  label="Estado"
                  size="xs"
                  :options="states.map((uf) => ({ label: uf, value: uf }))"
                />
                <BaseSelect
                  v-model="newProposal.bank"
                  label="Banco de Cotas"
                  size="xs"
                  :options="
                    bankOptions.map((opt) => ({ label: opt, value: opt }))
                  "
                />
                <BaseSelect
                  v-model="newProposal.fund"
                  label="Fundo"
                  size="xs"
                  :options="
                    fundOptions.map((opt) => ({ label: opt, value: opt }))
                  "
                />
              </div>

              <!-- Toggle de detalhes -->
              <div class="flex items-center justify-end">
                <button
                  type="button"
                  class="text-[11px] text-slate-300 hover:text-white inline-flex items-center gap-1"
                  @click="showAdvanced = !showAdvanced"
                >
                  <i
                    :class="
                      showAdvanced
                        ? 'fa-solid fa-chevron-up'
                        : 'fa-solid fa-chevron-down'
                    "
                  ></i>
                  <span>{{
                    showAdvanced ? "Ocultar detalhes" : "Mais detalhes"
                  }}</span>
                </button>
              </div>

              <div v-if="showAdvanced" class="space-y-2">
                <!-- Campos da Etapa (dinâmicos) -->
                <div v-if="stageDynamicFields.length" class="space-y-2">
                  <h4 class="text-xs font-semibold text-slate-200">Campos da etapa</h4>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                    <template v-for="f in stageDynamicFields" :key="f.id">
                      <div>
                        <BaseInput v-if="f.type === 'text' || f.type === 'number'" v-model="extraFieldValues[f.id]" :type="f.type === 'number' ? 'number' : 'text'" :label="f.label" size="xs" :placeholder="f.placeholder || ''" />
                        <BaseInput v-else-if="f.type === 'date'" v-model="extraFieldValues[f.id]" type="date" :label="f.label" size="xs" />
                        <BaseSelect v-else-if="f.type === 'select'" v-model="extraFieldValues[f.id]" :label="f.label" size="xs" :options="(f.options || []).map(o => ({ label: o, value: o }))" />
                        <div v-else-if="f.type === 'file'">
                          <FormField :label="f.label" :dense="true">
                            <FileDrop @files="(files) => onExtraFile(f.id, files)" />
                          </FormField>
                        </div>
                        <div v-if="extraFieldErrors[f.id]" class="text-[10px] text-red-400 mt-0.5">{{ extraFieldErrors[f.id] }}</div>
                      </div>
                    </template>
                  </div>
                </div>
                <!-- Linha 3: Tipo de Produto -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <BaseSelect
                    v-model="newProposal.productType"
                    label="Tipo de Produto"
                    size="xs"
                    :options="
                      productTypes.map((opt) => ({ label: opt, value: opt }))
                    "
                  />
                </div>

                <!-- Resumo -->
                <div>
                  <label class="form-label"
                    >Resumo do Objetivo do Crédito</label
                  >
                  <textarea
                    v-model="newProposal.summary"
                    rows="2"
                    class="form-textarea mt-1"
                    placeholder="Digite o resumo aqui..."
                  ></textarea>
                </div>

                <hr class="border-slate-700" />

                <!-- Dados da Operação -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <BaseInput
                    v-model="newProposal.startDate"
                    type="date"
                    label="Data de Início"
                    size="xs"
                  />
                  <BaseInput
                    v-model="newProposal.amountDesired"
                    type="text"
                    mask="currency"
                    locale="pt-BR"
                    label="Valor Desejado (R$)"
                    size="xs"
                    placeholder="0,00"
                  />
                  <BaseInput
                    v-model="newProposal.guaranteeValue"
                    type="text"
                    mask="currency"
                    locale="pt-BR"
                    label="Valor da Garantia (R$)"
                    size="xs"
                    placeholder="0,00"
                  />
                </div>

                <!-- Documentos -->
                <div>
                  <FormField label="Documentos (arraste e solte)" :dense="true">
                    <FileDrop
                      accept="image/*,application/pdf"
                      multiple
                      @files="(f) => (uploadedDocs.value = f)"
                    />
                  </FormField>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <BaseSelect
                    v-model="newProposal.guaranteeType"
                    label="Tipo de Garantia"
                    size="xs"
                    :options="
                      guaranteeTypes.map((o) => ({ label: o, value: o }))
                    "
                  />
                  <BaseSelect
                    v-model="newProposal.objective"
                    label="Objetivo"
                    size="xs"
                    :options="objectives.map((o) => ({ label: o, value: o }))"
                  />
                </div>

                <hr class="border-slate-700" />

                <!-- Responsáveis -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <BaseSelect
                    v-model="newProposal.representative"
                    label="Representante"
                    size="xs"
                    :options="
                      representatives.map((o) => ({ label: o, value: o }))
                    "
                  />
                  <BaseSelect
                    v-model="newProposal.manager"
                    label="Gerente"
                    size="xs"
                    :options="
                      managerOptions.map((o) => ({ label: o, value: o }))
                    "
                  />
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                <BaseSelect
                  v-model="newProposal.stageId"
                  label="Etapa inicial"
                  size="xs"
                  :options="
                    stages.map((s) => ({ label: s.title, value: s.id }))
                  "
                />
              </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs" @click="closeNewProposalModal">Cancelar</button>
          <button class="bg-orange-500 hover:bg-orange-600 text-white px-2 py-1 rounded-md text-xs" @click="saveNewProposal">
            <i class="fa-solid fa-check mr-1"></i>Salvar
          </button>
        </div>
      </template>
    </BaseModal>

    <ChatModal
      v-model:show="showAiModal"
      :messages="aiMessages"
      :typing="aiTyping"
      title="ConsorIA"
      size="sm"
      @send="onAiSend"
    />

    <!-- Toast -->
    <Teleport to="body">
      <div v-if="toast.show" class="fixed bottom-4 right-4 z-[70] bg-slate-800 text-slate-100 px-3 py-2 rounded-md border border-slate-700 shadow">
        {{ toast.text }}
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import ChatModal from "~/components/ui/ChatModal.vue";
import BaseInput from "~/components/ui/BaseInput.vue";
import BaseSelect from "~/components/ui/BaseSelect.vue";
import BaseInputGroup from "~/components/ui/BaseInputGroup.vue";
import FileDrop from "~/components/ui/FileDrop.vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import FormField from "~/components/ui/FormField.vue";

useHead({ title: "Esteira" });
definePageMeta({ layout: "default" });

const stages = ref([
  {
    id: "dados_basicos",
    title: "Dados Básicos",
    slaDays: 2,
    status: "Pendente",
    color: 'sky'
  },
  { id: "documentacao", title: "Documentação", slaDays: 5, status: "Pendente", color: 'indigo' },
  { id: "credito", title: "Crédito", slaDays: 7, status: "Em Análise", color: 'amber' },
  {
    id: "solicitacao_vistoria",
    title: "Solicitação Vistoria",
    slaDays: 3,
    status: "Aguardando",
    color: 'rose'
  },
  {
    id: "avaliacao_final",
    title: "Avaliação Final",
    slaDays: 5,
    status: "Em Análise",
    color: 'green'
  },
  {
    id: "avaliacao_diretoria",
    title: "Avaliação Diretoria",
    slaDays: 4,
    status: "Em Análise",
    color: 'purple'
  },
  { id: "fundo", title: "Fundo", slaDays: 2, status: "Aguardando", color: 'blue' },
  {
    id: "faturamento",
    title: "Faturamento",
    slaDays: 3,
    status: "Em Processamento",
    color: 'orange'
  },
  { id: "finalizado", title: "Finalizado", slaDays: 0, status: "Finalizado", color: 'green' },
]);

// Load pipeline config from Admin (localStorage)
onMounted(() => {
  try {
    const raw = localStorage.getItem(`pipeline_config__${pipelineKey}`);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        const statusById = Object.fromEntries(stages.value.map(s => [s.id, s.status]));
        stages.value = parsed.map((s) => ({
          id: s.id,
          title: s.title,
          slaDays: s.slaDays ?? 0,
          color: s.color || 'sky',
          status: statusById[s.id] || 'Pendente'
        }));
      }
    }
  } catch (_) {}
  ensureStagesCoverProposals();
});

const daysAgo = (n) =>
  new Date(Date.now() - n * 24 * 60 * 60 * 1000).toISOString();

const proposals = ref([
  {
    id: 1,
    name: "Ana Clara Souza",
    amount: 75000,
    stageId: "credito",
    status: "Em Análise",
    isArchived: false,
    stageEnteredAt: daysAgo(1),
  },
  {
    id: 2,
    name: "Bruno Martins",
    amount: 120000,
    stageId: "documentacao",
    status: "Pendente",
    isArchived: false,
    stageEnteredAt: daysAgo(4),
  },
  {
    id: 3,
    name: "Clara Nunes",
    amount: 50000,
    stageId: "dados_basicos",
    status: "Pendente",
    isArchived: false,
    stageEnteredAt: daysAgo(0),
  },
  {
    id: 4,
    name: "Diego Ramos",
    amount: 90000,
    stageId: "solicitacao_vistoria",
    status: "Aguardando",
    isArchived: false,
    stageEnteredAt: daysAgo(6),
  },
  {
    id: 5,
    name: "Eduarda Lima",
    amount: 150000,
    stageId: "avaliacao_final",
    status: "Em Análise",
    isArchived: false,
    stageEnteredAt: daysAgo(3),
  },
  {
    id: 6,
    name: "Felipe Rocha",
    amount: 200000,
    stageId: "avaliacao_diretoria",
    status: "Em Análise",
    isArchived: false,
    stageEnteredAt: daysAgo(8),
  },
  {
    id: 7,
    name: "Gabriela Alves",
    amount: 110000,
    stageId: "fundo",
    status: "Aguardando",
    isArchived: false,
    stageEnteredAt: daysAgo(2),
  },
  {
    id: 8,
    name: "Helena Prado",
    amount: 80000,
    stageId: "faturamento",
    status: "Em Processamento",
    isArchived: false,
    stageEnteredAt: daysAgo(5),
  },
  {
    id: 9,
    name: "Igor Teixeira",
    amount: 60000,
    stageId: "finalizado",
    status: "Finalizado",
    isArchived: false,
    stageEnteredAt: daysAgo(10),
  },
]);

// Ensure Kanban has columns for all proposal stageIds
function titleFromId(id) {
  try {
    const pretty = String(id || '').replace(/_/g, ' ');
    return pretty.replace(/\b\w/g, (m) => m.toUpperCase());
  } catch { return id; }
}
function ensureStagesCoverProposals() {
  const existing = new Set(stages.value.map((s) => s.id));
  const toAdd = new Set();
  for (const p of proposals.value) {
    if (p?.stageId && !existing.has(p.stageId)) toAdd.add(p.stageId);
  }
  if (toAdd.size) {
    const now = Array.from(toAdd).map((id) => ({
      id,
      title: titleFromId(id),
      slaDays: 0,
      status: 'Pendente',
      color: 'sky',
      auto: true
    }));
    stages.value = stages.value.concat(now);
  }
}

watch(proposals, () => ensureStagesCoverProposals(), { deep: true });

const filterText = ref("");
const filterStatus = ref("");
  const showArchived = ref(false);
const viewMode = ref("kanban");

const statusList = [
  "Pendente",
  "Em Análise",
  "Aguardando",
  "Em Processamento",
  "Aprovado",
  "Reprovado",
  "Finalizado",
];

const filteredProposals = computed(() => {
  return proposals.value.filter((p) => {
    const nameMatch = p.name
      .toLowerCase()
      .includes(filterText.value.toLowerCase());
    const statusMatch = !filterStatus.value || p.status === filterStatus.value;
    const archivedMatch = showArchived.value ? true : !p.isArchived;
    return nameMatch && statusMatch && archivedMatch;
  });
});

const filteredByStage = (stageId) =>
  filteredProposals.value.filter((p) => p.stageId === stageId);
const stageTitle = (id) => stages.value.find((s) => s.id === id)?.title || id;

const statusPillClass = (status) => {
  switch (status) {
    case "Pendente":
      return "bg-amber-900/50 text-amber-300 ring-1 ring-inset ring-amber-500/20";
    case "Em Análise":
      return "bg-blue-900/50 text-blue-300 ring-1 ring-inset ring-blue-500/20";
    case "Aguardando":
      return "bg-slate-700 text-slate-300 ring-1 ring-inset ring-slate-500/20";
    case "Em Processamento":
      return "bg-purple-900/50 text-purple-300 ring-1 ring-inset ring-purple-500/20";
    case "Aprovado":
      return "bg-green-900/50 text-green-300 ring-1 ring-inset ring-green-500/20";
    case "Reprovado":
      return "bg-red-900/50 text-red-300 ring-1 ring-inset ring-red-500/20";
    case "Finalizado":
      return "bg-orange-900/50 text-orange-300 ring-1 ring-inset ring-orange-500/20";
    default:
      return "bg-slate-700 text-slate-300 ring-1 ring-inset ring-slate-500/20";
  }
};

// Stage color styles derived from color field
const colorStyles = {
  sky: { border: 'border-sky-500', strip: 'bg-sky-500', badge: 'bg-sky-900/50 text-sky-300 ring-1 ring-inset ring-sky-500/20' },
  indigo: { border: 'border-indigo-500', strip: 'bg-indigo-500', badge: 'bg-indigo-900/50 text-indigo-300 ring-1 ring-inset ring-indigo-500/20' },
  amber: { border: 'border-amber-500', strip: 'bg-amber-500', badge: 'bg-amber-900/50 text-amber-300 ring-1 ring-inset ring-amber-500/20' },
  rose: { border: 'border-rose-500', strip: 'bg-rose-500', badge: 'bg-rose-900/50 text-rose-300 ring-1 ring-inset ring-rose-500/20' },
  green: { border: 'border-green-500', strip: 'bg-green-500', badge: 'bg-green-900/50 text-green-300 ring-1 ring-inset ring-green-500/20' },
  purple: { border: 'border-purple-500', strip: 'bg-purple-500', badge: 'bg-purple-900/50 text-purple-300 ring-1 ring-inset ring-purple-500/20' },
  blue: { border: 'border-blue-500', strip: 'bg-blue-500', badge: 'bg-blue-900/50 text-blue-300 ring-1 ring-inset ring-blue-500/20' },
  orange: { border: 'border-orange-500', strip: 'bg-orange-500', badge: 'bg-orange-900/50 text-orange-300 ring-1 ring-inset ring-orange-500/20' },
  teal: { border: 'border-teal-500', strip: 'bg-teal-500', badge: 'bg-teal-900/50 text-teal-300 ring-1 ring-inset ring-teal-500/20' },
  cyan: { border: 'border-cyan-500', strip: 'bg-cyan-500', badge: 'bg-cyan-900/50 text-cyan-300 ring-1 ring-inset ring-cyan-500/20' },
  lime: { border: 'border-lime-500', strip: 'bg-lime-500', badge: 'bg-lime-900/50 text-lime-300 ring-1 ring-inset ring-lime-500/20' },
  emerald: { border: 'border-emerald-500', strip: 'bg-emerald-500', badge: 'bg-emerald-900/50 text-emerald-300 ring-1 ring-inset ring-emerald-500/20' },
  fuchsia: { border: 'border-fuchsia-500', strip: 'bg-fuchsia-500', badge: 'bg-fuchsia-900/50 text-fuchsia-300 ring-1 ring-inset ring-fuchsia-500/20' },
  violet: { border: 'border-violet-500', strip: 'bg-violet-500', badge: 'bg-violet-900/50 text-violet-300 ring-1 ring-inset ring-violet-500/20' },
  pink: { border: 'border-pink-500', strip: 'bg-pink-500', badge: 'bg-pink-900/50 text-pink-300 ring-1 ring-inset ring-pink-500/20' },
  red: { border: 'border-red-500', strip: 'bg-red-500', badge: 'bg-red-900/50 text-red-300 ring-1 ring-inset ring-red-500/20' },
  yellow: { border: 'border-yellow-500', strip: 'bg-yellow-500', badge: 'bg-yellow-900/50 text-yellow-300 ring-1 ring-inset ring-yellow-500/20' },
  slate: { border: 'border-slate-500', strip: 'bg-slate-500', badge: 'bg-slate-900/50 text-slate-300 ring-1 ring-inset ring-slate-500/20' },
  stone: { border: 'border-stone-500', strip: 'bg-stone-500', badge: 'bg-stone-900/50 text-stone-300 ring-1 ring-inset ring-stone-500/20' },
  zinc: { border: 'border-zinc-500', strip: 'bg-zinc-500', badge: 'bg-zinc-900/50 text-zinc-300 ring-1 ring-inset ring-zinc-500/20' }
};

const stageBorderClass = (stageId) => {
  const st = stages.value.find(s => s.id === stageId);
  return (st && colorStyles[st.color]?.border) || 'border-slate-500';
};
const stageStripClass = (stageId) => {
  const st = stages.value.find(s => s.id === stageId);
  return (st && colorStyles[st.color]?.strip) || 'bg-slate-500';
};
const stageBadgeClass = (stageId) => {
  const st = stages.value.find(s => s.id === stageId);
  return (st && colorStyles[st.color]?.badge) || 'bg-slate-700 text-slate-300 ring-1 ring-inset ring-slate-500/20';
};

// SLA helpers
const getSlaInfo = (p) => {
  const stage = stages.value.find((s) => s.id === p.stageId);
  const sla = stage?.slaDays ?? 0;
  if (!sla || !p.stageEnteredAt)
    return { days: 0, sla, remaining: 0, status: "ok" };
  const entered = new Date(p.stageEnteredAt);
  const now = new Date();
  const msPerDay = 24 * 60 * 60 * 1000;
  const days = Math.max(
    0,
    Math.ceil((now.getTime() - entered.getTime()) / msPerDay)
  );
  const remaining = sla - days;
  let status = "ok";
  if (days > sla) status = "overdue";
  else if (days >= Math.max(1, Math.floor(sla * 0.8))) status = "warning";
  return { days, sla, remaining, status };
};

const slaBadgeClass = (p) => {
  const { status } = getSlaInfo(p);
  if (status === "overdue") return "bg-red-900/60 text-red-300";
  if (status === "warning") return "bg-amber-900/60 text-amber-300";
  return "bg-slate-700 text-slate-300";
};

const slaLabel = (p) => {
  const { days, sla } = getSlaInfo(p);
  if (!sla) return "SLA N/A";
  return `${days}/${sla}d`;
};

// Drag and Drop state/handlers
const draggedId = ref(null);
const dragOverStage = ref("");

const onDragStart = (event, proposal) => {
  draggedId.value = proposal.id;
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/plain", String(proposal.id));
  }
};

const onDragEnd = () => {
  draggedId.value = null;
  dragOverStage.value = "";
};

const onDragEnter = (stageId) => {
  dragOverStage.value = stageId;
};

const onDragLeave = (stageId) => {
  if (dragOverStage.value === stageId) dragOverStage.value = "";
};

const onDrop = (stageId) => {
  if (!draggedId.value) return;
  const idx = proposals.value.findIndex((p) => p.id === draggedId.value);
  if (idx === -1) return onDragEnd();

  const stage = stages.value.find((s) => s.id === stageId);
  if (!stage) return onDragEnd();

  // Update stage and default status when moved
  proposals.value[idx] = {
    ...proposals.value[idx],
    stageId,
    status: stage.status,
    stageEnteredAt: new Date().toISOString(),
  };

  onDragEnd();
};

// New Proposal Modal (mocked options; replace with API later)
const showNewModal = ref(false);
const states = [
  "AC",
  "AL",
  "AP",
  "AM",
  "BA",
  "CE",
  "DF",
  "ES",
  "GO",
  "MA",
  "MT",
  "MS",
  "MG",
  "PA",
  "PB",
  "PR",
  "PE",
  "PI",
  "RJ",
  "RN",
  "RS",
  "RO",
  "RR",
  "SC",
  "SP",
  "SE",
  "TO",
];
const bankOptions = [
  "TOCO",
  "LANCE",
  "AMAURI",
  "Tramonta",
  "Fraga & Bitello",
  "SI",
  "Larban",
  "DW Consórcios",
];
const fundOptions = ["CREMONA", "SAVEL", "FOUR CAPITAL", "VALOR", "LARBAN"];
const productTypes = ["ConsorEquity", "Fundo Inter", "Flex", "Outro"];
const adminOptions = ["Itaú", "HS", "Bradesco"];
const guaranteeTypes = ["Imóvel", "Veículo", "Aplicação", "Outro"];
const objectives = ["Compra de imóvel", "Reforma", "Capital de giro", "Outros"];
const representatives = ["Sem representante", "Ana", "Bruno", "Carlos"];
const managerOptions = [
  "Sem gerente",
  "Juliana Rezende",
  "Rafael Santos",
  "Carla Lima",
];

const newProposal = ref({
  name: "",
  cpf: "",
  cep: "",
  city: "",
  state: "",
  administrator: "",
  bank: "",
  fund: "",
  productType: "",
  summary: "",
  startDate: "",
  amountDesired: 0,
  guaranteeValue: 0,
  guaranteeType: "",
  objective: "",
  representative: representatives[0],
  manager: managerOptions[0],
  stageId: stages.value[0]?.id || "dados_basicos",
});
const formErrors = ref({ name: "", cpf: "", amountDesired: "" });

const openNewProposalModal = () => {
  newProposal.value = {
    name: "",
    cpf: "",
    cep: "",
    city: "",
    state: "",
    administrator: "",
    bank: "",
    fund: "",
    productType: "",
    summary: "",
    startDate: "",
    amountDesired: 0,
    guaranteeValue: 0,
    guaranteeType: "",
    objective: "",
    representative: representatives[0],
    manager: managerOptions[0],
    stageId: stages.value[0]?.id || "dados_basicos",
  };
  formErrors.value = { name: "", cpf: "", amountDesired: "" };
  // init dynamic fields for initial stage
  initStageFields();
  // auto expand details if there are dynamic fields
  showAdvanced.value = stageDynamicFields.value.length > 0;
  if (stageDynamicFields.value.length > 0) notify('Campos da etapa carregados');
  showNewModal.value = true;
};

const closeNewProposalModal = () => {
  showNewModal.value = false;
};

const validateNewProposal = () => {
  formErrors.value = { name: "", cpf: "", amountDesired: "" };
  let ok = true;
  if (!newProposal.value.name.trim()) {
    formErrors.value.name = "Obrigatório";
    ok = false;
  }
  if (!newProposal.value.cpf.trim()) {
    formErrors.value.cpf = "Obrigatório";
    ok = false;
  }
  if (
    !newProposal.value.amountDesired ||
    Number(newProposal.value.amountDesired) <= 0
  ) {
    formErrors.value.amountDesired = "Valor inválido";
    ok = false;
  }
  return ok;
};

const saveNewProposal = () => {
  if (!validateNewProposal()) return;
  // validate dynamic required fields
  extraFieldErrors.value = {};
  let okExtra = true;
  for (const f of stageDynamicFields.value) {
    if (!f?.required) continue;
    if (f.type === 'file') {
      const hasFile = !!(extraFieldFiles.value?.[f.id]?.length);
      if (!hasFile) { extraFieldErrors.value[f.id] = 'Obrigatório'; okExtra = false; }
    } else {
      const val = (extraFieldValues.value?.[f.id] ?? '').toString().trim();
      if (!val) { extraFieldErrors.value[f.id] = 'Obrigatório'; okExtra = false; }
    }
  }
  if (!okExtra) {
    showAdvanced.value = true;
    notify('Preencha os campos obrigatórios da etapa');
    return;
  }
  const stage = stages.value.find((s) => s.id === newProposal.value.stageId);
  const parseCurrency = (s) => {
    const str = String(s || "").trim();
    if (!str) return 0;
    // Remove thousand separators and convert decimal comma to dot
    const normalized = str.replace(/\./g, "").replace(/,/g, ".");
    const n = Number(normalized);
    return isNaN(n) ? 0 : n;
  };
  const id = Date.now();
  proposals.value.unshift({
    id,
    name: newProposal.value.name,
    amount: parseCurrency(newProposal.value.amountDesired),
    stageId: newProposal.value.stageId,
    status: stage?.status || "Pendente",
    isArchived: false,
    stageEnteredAt: new Date().toISOString(),
    details: { ...newProposal.value, extra: { ...extraFieldValues.value } },
  });
  closeNewProposalModal();
};

// CEP lookup via SSR API (server route)
const cepLoading = ref(false);
const cepError = ref("");
const showAdvanced = ref(false);

// Dispara busca de CEP automaticamente ao digitar o 8º dígito
const lastCepQueried = ref("");
watch(
  () => newProposal.value.cep,
  (val) => {
    const digits = String(val || "").replace(/\D/g, "");
    if (
      digits.length === 8 &&
      digits !== lastCepQueried.value &&
      !cepLoading.value
    ) {
      lastCepQueried.value = digits;
      fetchCep();
    }
    if (digits.length < 8) {
      cepError.value = "";
    }
  }
);
const fetchCep = async () => {
  cepError.value = "";
  const digits = (newProposal.value.cep || "").replace(/\D/g, "");
  if (digits.length !== 8) {
    cepError.value = "Informe 8 dígitos";
    return;
  }
  cepLoading.value = true;
  try {
    const res = await $fetch("/api/cep?cep=" + digits);
    if (res) {
      // res is plain object from our SSR route
      newProposal.value.city = res.city || "";
      newProposal.value.state = res.state || "";
    }
  } catch (e) {
    cepError.value = e?.statusMessage || "Erro ao buscar CEP";
  } finally {
    cepLoading.value = false;
  }
};

// Upload de documentos para anexos no modal (exemplo, sem enviar ao backend ainda)
const uploadedDocs = ref([]);

// Simple toast
const toast = ref({ show: false, text: '' });
function notify(msg) { toast.value = { show: true, text: msg }; setTimeout(() => (toast.value.show = false), 2200); }

// Dynamic fields per stage (from Admin builder)
const stageFormsMap = ref({});
const stageDynamicFields = ref([]);
const extraFieldValues = ref({});
const extraFieldFiles = ref({});
const extraFieldErrors = ref({});

function loadStageForms() {
  try {
    const raw = localStorage.getItem(`pipeline_stage_forms__${pipelineKey}`);
    if (raw) stageFormsMap.value = JSON.parse(raw) || {};
  } catch (_) { stageFormsMap.value = {}; }
}

function initStageFields() {
  const sid = newProposal.value.stageId;
  const arr = Array.isArray(stageFormsMap.value[sid]) ? stageFormsMap.value[sid] : [];
  stageDynamicFields.value = arr;
  const vals = {};
  arr.forEach(f => { if (f.type !== 'file') vals[f.id] = ''; });
  extraFieldValues.value = vals;
  extraFieldFiles.value = {};
  extraFieldErrors.value = {};
}

function onExtraFile(id, files) {
  extraFieldFiles.value[id] = files;
}

watch(() => newProposal.value.stageId, () => initStageFields());
onMounted(() => { loadStageForms(); initStageFields(); });

// IA Chat modal (reutilizável via componente)
const showAiModal = ref(false);
const CHAT_STORE_KEY = 'consoria_chat_history';
const aiMessages = ref([
  { role: 'assistant', content: 'Olá! Eu sou a ConsorIA. Como posso ajudar hoje?' }
]);
const aiTyping = ref(false);

const openAiChat = () => {
  try {
    const raw = localStorage.getItem(CHAT_STORE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.every(m => m && typeof m.role === 'string')) {
        aiMessages.value = parsed;
      }
    }
  } catch (_) {}
  showAiModal.value = true;
};
const closeAiChat = () => { showAiModal.value = false; };
const onAiSend = async ({ text, attachments }) => {
  aiMessages.value.push({ role: 'user', content: text, attachments });
  aiTyping.value = true;
  try {
    const history = aiMessages.value.slice(-8).map(m => ({ role: m.role, content: m.content }));
    const res = await $fetch('/api/ai', {
      method: 'POST',
      body: { text, attachments, history }
    });
    aiMessages.value.push({ role: 'assistant', content: (res?.['text'] || res?.['message'] || 'Sem resposta.') });
  } catch (e) {
    aiMessages.value.push({ role: 'assistant', content: 'Falha ao obter resposta da IA. Verifique sua configuração.' });
  } finally {
    aiTyping.value = false;
  }
};

// Persist chat history locally
watch(aiMessages, (list) => {
  try {
    localStorage.setItem(CHAT_STORE_KEY, JSON.stringify(list.slice(-50)));
  } catch (_) {}
}, { deep: true });
</script>

<style scoped>
/* Mantém a aparência próxima ao HTML original */

/* Horizontal scrollbar styling for Kanban */
.kanban-scroll {
  scrollbar-width: thin;
  scrollbar-color: #475569 #0f172a; /* thumb slate-600, track slate-900 */
}
.kanban-scroll::-webkit-scrollbar {
  height: 8px;
}
.kanban-scroll::-webkit-scrollbar-track {
  background: #febf00; /* slate-800 */
  border-radius: 8px;
}
.kanban-scroll::-webkit-scrollbar-thumb {
  background: #475569; /* slate-600 */
  border-radius: 8px;
}
.kanban-scroll::-webkit-scrollbar-thumb:hover {
  background: #64748b; /* slate-500 */
}

/* Minimal, harmonious form controls for modal */
.new-proposal-modal .form-label {
  @apply text-[11px] text-slate-300;
}
.new-proposal-modal .form-label {
  @apply text-[10px];
}
.new-proposal-modal .form-input {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm h-5 px-1 text-[10px] leading-none outline-none focus:ring-[0.5px] focus:ring-orange-500/60 focus:border-orange-400/50 transition;
}
.new-proposal-modal .form-select {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm h-5 px-1 text-[10px] leading-none outline-none focus:ring-[0.5px] focus:ring-orange-500/60 focus:border-orange-400/50 transition;
}
.new-proposal-modal .form-textarea {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm px-1.5 py-1 text-[10px] outline-none focus:ring-[0.5px] focus:ring-orange-500/60 focus:border-orange-400/50 transition;
}
.new-proposal-modal .cep-group {
  @apply flex items-center gap-2;
}
.new-proposal-modal .btn {
  @apply px-1.5 py-[2px] rounded text-[10px] transition-colors;
}
.new-proposal-modal .cep-group .btn {
  @apply h-5;
}
.new-proposal-modal .btn-neutral {
  @apply bg-slate-700 hover:bg-slate-600 text-slate-200;
}
.new-proposal-modal .btn-primary {
  @apply bg-orange-500 hover:bg-orange-600 text-white;
}
/* tighten grid gaps globally in modal */
.new-proposal-modal .grid {
  gap: 0.5rem;
}

/* Harmonize BaseInput (xs) with modal dark compact style */
.new-proposal-modal .input-label {
  @apply text-[10px] text-slate-300;
}
.new-proposal-modal .input-container--xs {
  @apply h-7;
}
.new-proposal-modal .input-container--default {
  @apply bg-slate-700/70 border border-slate-600/60 rounded-sm;
}
.new-proposal-modal .input-container--focused {
  @apply ring-[0.5px] ring-orange-500/60 border-orange-400/50;
}
.new-proposal-modal .input-field {
  @apply text-slate-200 placeholder-gray-400;
}
.new-proposal-modal .input-field--xs {
  @apply text-[12px] leading-tight px-2;
}
/* Ensure selects inside modal always show value */
.new-proposal-modal select {
  color: #e5e7eb !important;
  -webkit-text-fill-color: #e5e7eb !important;
}
.new-proposal-modal option {
  color: #e5e7eb !important;
  background-color: #1f2937 !important;
}
</style>
const pipelineKey = 'quotaequity';
