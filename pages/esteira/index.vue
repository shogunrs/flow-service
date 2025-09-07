<template>
  <div class="min-h-screen">
    <Sidebar :items="menu" @select="onSelect" />
    <!-- Header -->
    <header class="p-3 border-b border-slate-800 ml-14">
      <div class="w-full grid grid-cols-1 sm:grid-cols-3 sm:items-center gap-2">
        <!-- Left: (reserved for menu breadcrumb/title later) -->
        <div class="hidden sm:block"></div>

        <!-- Center: global search, centered and proportional width -->
        <div
          class="px-0 sm:px-4 justify-self-center w-full max-w-md sm:max-w-lg md:max-w-xl"
        >
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
            <span class="hidden sm:inline">Novo Registro</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="p-3 sm:p-6 ml-14">
      <!-- Kanban View -->
      <div v-if="viewMode === 'kanban'" class="overflow-x-auto kanban-scroll">
        <transition-group name="kanban-col" tag="div" class="flex gap-2 min-h-[10rem] pb-2">
          <div
            v-for="(stage, i) in stages"
            :key="stage.id"
            class="w-56 sm:w-60 md:w-64 flex-shrink-0 bg-slate-800 rounded-md p-1.5 transition-colors"
            :class="[
              dragOverStage === stage.id
                ? 'ring-2 ring-blue-400 ring-offset-0'
                : '',
              isDraggingStage && columnDragOverIndex === i
                ? 'ring-2 ring-orange-400'
                : '',
              isDraggingStage && draggingStageId === stage.id ? 'opacity-90' : ''
            ]"
            @dragover.prevent="onColumnDragOver($event, i)"
            @dragenter.prevent="onDragEnter(stage.id)"
            @dragleave="onDragLeave(stage.id)"
            @drop.capture="onColumnDrop($event, i)"
            @drop="onDrop(stage.id)"
          >
            <div
              :class="['h-1 rounded-md mb-2', stageStripClass(stage.id)]"
            ></div>
            <!-- Column header -->
            <div class="flex items-center justify-between mb-1.5">
              <div class="flex items-center gap-2">
                <button
                  class="text-slate-300/80 hover:text-white px-1 py-0.5 rounded"
                  title="Reordenar coluna"
                  :draggable="!isTouch"
                  @dragstart="onStageDragStart($event, stage, i)"
                  @dragend="onStageDragEnd"
                >
                  <i class="fa-solid fa-grip-vertical"></i>
                </button>
                <h3 class="font-semibold text-xs">{{ stage.title }}</h3>
                <span
                  v-if="stage.auto"
                  class="text-[10px] px-1.5 py-0.5 rounded-full bg-slate-700 text-slate-300 border border-slate-600/60"
                  >auto</span
                >
                <span class="text-[11px] text-slate-400"
                  >SLA {{ stage.slaDays }}d</span
                >
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
                :draggable="!isTouch"
                @dragstart="onDragStart($event, p)"
                @click="openCardForm(p)"
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
        </transition-group>
      </div>

      <!-- List View -->
      <div v-else class="bg-slate-800 rounded-lg">
        <!-- Mobile: cards -->
        <div class="sm:hidden">
          <ul class="divide-y divide-slate-700">
            <li v-for="p in filteredProposals" :key="p.id" class="p-3">
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0">
                  <div class="font-medium text-sm truncate">{{ p.name }}</div>
                  <div class="mt-0.5 text-[12px] text-slate-300">
                    Valor: R$ {{ p.amount.toLocaleString("pt-BR") }}
                  </div>
                  <div class="text-[12px] text-slate-300">
                    Etapa: {{ stageTitle(p.stageId) }}
                  </div>
                </div>
                <span
                  :class="statusPillClass(p.status)"
                  class="flex-shrink-0 text-[10px] font-semibold px-2 py-0.5 rounded-full"
                >
                  {{ p.status }}
                </span>
              </div>
            </li>
          </ul>
          <div
            v-if="filteredProposals.length === 0"
            class="p-3 text-slate-400 text-sm"
          >
            Sem registros
          </div>
        </div>

        <!-- Desktop: table -->
        <div class="hidden sm:block overflow-x-auto">
          <table class="w-full text-sm text-left">
            <thead class="bg-slate-700/60 text-xs text-slate-300 uppercase">
              <tr>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Cliente</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Valor</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Etapa</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Status</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="p in filteredProposals"
                :key="p.id"
                class="border-b border-slate-700 hover:bg-slate-700/50"
              >
                <td class="px-3 sm:px-6 py-2 sm:py-3">{{ p.name }}</td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  R$ {{ p.amount.toLocaleString("pt-BR") }}
                </td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  {{ stageTitle(p.stageId) }}
                </td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  <span
                    :class="statusPillClass(p.status)"
                    class="text-xs font-semibold px-2 py-1 rounded-full"
                  >
                    {{ p.status }}
                  </span>
                </td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  <div class="flex items-center gap-2 justify-end text-slate-300">
                    <button class="hover:text-white" title="Ver/Editar" @click="openCardForm(p)">
                      <i class="fa-regular fa-pen-to-square"></i>
                    </button>
                    <button class="hover:text-white" title="Mover" @click="openMoveModal(p)">
                      <i class="fa-solid fa-arrow-right-arrow-left"></i>
                    </button>
                    <button class="text-red-400 hover:text-red-300" title="Excluir" @click="openDeleteProposal(p)">
                      <i class="fa-regular fa-trash-can"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <!-- New Proposal Modal using BaseModal -->
    <BaseModal
      v-model="showNewModal"
      title="Novo Registro"
      size="md"
      :body-class="'new-proposal-modal'"
    >
      <div class="space-y-2">
        <!-- Genérico: Etapa + Campos dinâmicos do processo -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <BaseSelect
            v-model="newProposal.stageId"
            label="Etapa"
            size="xs"
            :options="stages.map((s) => ({ label: s.title, value: s.id }))"
          />
        </div>
        <div class="space-y-2">
          <h4 class="text-xs font-semibold text-slate-200">Campos do processo</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <template v-for="f in stageDynamicFields" :key="f.id">
              <div>
                <BaseInput
                  v-if="f.type === 'text' || f.type === 'number'"
                  v-model="extraFieldValues[f.id]"
                  :type="f.type === 'number' ? 'text' : 'text'"
                  :numeric="f.type === 'number'"
                  :label="f.label"
                  size="xs"
                  :placeholder="f.placeholder || ''"
                />
                <BaseInput
                  v-else-if="f.type === 'date'"
                  v-model="extraFieldValues[f.id]"
                  type="date"
                  :label="f.label"
                  size="xs"
                />
                <BaseSelect
                  v-else-if="f.type === 'select'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  size="xs"
                  :options="(f.options || []).map(o => ({ label: o, value: o }))"
                />
                <div v-else-if="f.type === 'file'">
                  <FormField :label="f.label" :dense="true">
                    <FileDrop @files="(files) => onExtraFile(f.id, files)" />
                  </FormField>
                </div>
                <div v-if="extraFieldErrors[f.id]" class="text-[11px] text-red-400 mt-1">{{ extraFieldErrors[f.id] }}</div>
              </div>
            </template>
          </div>
        </div>
        <!-- Linha 1: Nome, CPF, CEP (abaixo do Nome), Administradora (abaixo do CPF) -->
        <div v-if="false" class="grid grid-cols-12 gap-2">
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
              :options="adminOptions.map((opt) => ({ label: opt, value: opt }))"
            />
          </div>
        </div>

        <!-- Linha 2: Cidade, Estado, Banco de Cotas, Fundo -->
        <div v-if="false" class="grid grid-cols-1 md:grid-cols-2 gap-2">
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
            :options="bankOptions.map((opt) => ({ label: opt, value: opt }))"
          />
          <BaseSelect
            v-model="newProposal.fund"
            label="Fundo"
            size="xs"
            :options="fundOptions.map((opt) => ({ label: opt, value: opt }))"
          />
        </div>

        <!-- Toggle de detalhes -->
        <div v-if="false" class="flex items-center justify-end">
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

        <div v-if="false && showAdvanced" class="space-y-2">
          <!-- Campos da Etapa (dinâmicos) -->
          <div v-if="stageDynamicFields.length" class="space-y-2">
            <h4 class="text-xs font-semibold text-slate-200">
              Campos do processo
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
              <template v-for="f in stageDynamicFields" :key="f.id">
                <div>
                  <BaseInput
                    v-if="f.type === 'text' || f.type === 'number'"
                    v-model="extraFieldValues[f.id]"
                    :type="'text'"
                    :numeric="f.type === 'number'"
                    :label="f.label"
                    size="xs"
                    :placeholder="f.placeholder || ''"
                  />
                  <BaseInput
                    v-else-if="f.type === 'date'"
                    v-model="extraFieldValues[f.id]"
                    type="date"
                    :label="f.label"
                    size="xs"
                  />
                  <BaseSelect
                    v-else-if="f.type === 'select'"
                    v-model="extraFieldValues[f.id]"
                    :label="f.label"
                    size="xs"
                    :options="
                      (f.options || []).map((o) => ({ label: o, value: o }))
                    "
                  />
                  <div v-else-if="f.type === 'file'">
                    <FormField :label="f.label" :dense="true">
                      <FileDrop @files="(files) => onExtraFile(f.id, files)" />
                    </FormField>
                  </div>
                  <div
                    v-if="extraFieldErrors[f.id]"
                    class="text-[10px] text-red-400 mt-0.5"
                  >
                    {{ extraFieldErrors[f.id] }}
                  </div>
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
              :options="productTypes.map((opt) => ({ label: opt, value: opt }))"
            />
          </div>

          <!-- Resumo -->
          <div>
            <label class="form-label">Resumo do Objetivo do Crédito</label>
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
              :options="guaranteeTypes.map((o) => ({ label: o, value: o }))"
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
              :options="representatives.map((o) => ({ label: o, value: o }))"
            />
            <BaseSelect
              v-model="newProposal.manager"
              label="Gerente"
              size="xs"
              :options="managerOptions.map((o) => ({ label: o, value: o }))"
            />
          </div>
        </div>
        <div v-if="false" class="grid grid-cols-1 md:grid-cols-2 gap-2"></div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
            @click="closeNewProposalModal"
          >
            Cancelar
          </button>
          <button
            class="bg-orange-500 hover:bg-orange-600 text-white px-2 py-1 rounded-md text-xs"
            @click="saveNewProposal"
          >
            <i class="fa-solid fa-check mr-1"></i>Salvar
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Delete Proposal Modal -->
    <BaseModal v-model="showDeleteProposalModal" title="Excluir Registro" size="sm" :z-index="80">
      <p class="text-sm text-slate-200">Tem certeza que deseja excluir este registro?</p>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs" @click="cancelDeleteProposal">Cancelar</button>
          <button class="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded-md text-xs" @click="confirmDeleteProposal">Excluir</button>
        </div>
      </template>
    </BaseModal>

    <!-- Stage Form Modal (per card) -->
    <BaseModal v-model="showStageFormModal" title="Formulário da Etapa" size="md" :z-index="70">
      <div class="space-y-2">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <div class="text-[12px] text-slate-300">Etapa atual</div>
          <div class="text-[12px] text-slate-100">{{ stageTitle(selectedProposal?.stageId) }}</div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <template v-for="f in stageFormFields" :key="f.id">
            <div>
              <BaseInput v-if="f.type === 'text' || f.type === 'number'" v-model="stageFormValues[f.id]" :type="f.type === 'number' ? 'text' : 'text'" :numeric="f.type === 'number'" :label="f.label" size="xs" :placeholder="f.placeholder || ''" />
              <BaseInput v-else-if="f.type === 'date'" v-model="stageFormValues[f.id]" type="date" :label="f.label" size="xs" />
              <BaseSelect v-else-if="f.type === 'select'" v-model="stageFormValues[f.id]" :label="f.label" size="xs" :options="(f.options || []).map(o => ({ label: o, value: o }))" />
              <div v-else-if="f.type === 'file'">
                <FormField :label="f.label" :dense="true"><FileDrop @files="(files) => onStageFormFile(f.id, files)" /></FormField>
              </div>
            </div>
          </template>
        </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs" @click="showStageFormModal = false">Fechar</button>
          <button class="bg-orange-500 hover:bg-orange-600 text-white px-2 py-1 rounded-md text-xs" @click="saveStageForm">Salvar</button>
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
      <div
        v-if="toast.show"
        class="fixed bottom-4 right-4 z-[70] bg-slate-800 text-slate-100 px-3 py-2 rounded-md border border-slate-700 shadow"
      >
        {{ toast.text }}
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, onActivated } from "vue";
import { loadProposals, saveProposals, fetchProposalsApi, createProposalApi, fetchProposalFormsApi, saveProposalStageFormApi, updateProposalApi } from "~/composables/useProposals";
import Sidebar from "~/components/ui/Sidebar.vue";
import { useProcessSubmenu } from "~/composables/useProcessMenu";
import ChatModal from "~/components/ui/ChatModal.vue";
import BaseInput from "~/components/ui/BaseInput.vue";
import BaseSelect from "~/components/ui/BaseSelect.vue";
import BaseInputGroup from "~/components/ui/BaseInputGroup.vue";
import FileDrop from "~/components/ui/FileDrop.vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import FormField from "~/components/ui/FormField.vue";
import { isApiEnabled } from '~/utils/api/index'
import { saveStagesApi } from '~/composables/useStages'
import { useToast } from '~/composables/useToast'
import { fetchStageFieldsApi } from '~/composables/useStageFields'
import { uploadFileViaPresign } from '~/composables/useFiles'

useHead({ title: "Esteira" });
definePageMeta({ layout: "default" });

// Pipeline key (padrão: quotaequity). Pode ser passado como prop quando usado dentro de [process].vue
const props = defineProps({
  pipelineKey: { type: String, default: "quotaequity" },
});
const pipelineKey = computed(() => props.pipelineKey || "quotaequity");

// Detecta dispositivos touch para desabilitar drag & drop (melhor usabilidade no mobile)
const isTouch = ref(false);
onMounted(() => {
  try {
    isTouch.value =
      "ontouchstart" in window ||
      (navigator && (navigator.maxTouchPoints || 0) > 0);
  } catch (_) {
    isTouch.value = false;
  }
});

// Sidebar menu (mesmo usado no dashboard)
const { processes, getLastKey } = useProcessSubmenu();
const menu = computed(() => [
  { key: "home", label: "Home", icon: "fa-solid fa-house", to: "/" },
  {
    key: "processos",
    label: "Processos",
    icon: "fa-solid fa-list-check",
    to: (() => {
      const last = getLastKey();
      return last ? `/esteira/${last}` : "/processos";
    })(),
    children: [
      { key: "ver-processos", label: "Ver Processos", to: "/processos" },
      ...processes.value.map((p) => ({
        key: p.key,
        label: p.name,
        to: `/esteira/${p.key}`,
      })),
    ],
  },
  {
    key: "admin",
    label: "Admin",
    icon: "fa-solid fa-user-shield",
    to: "/admin",
    children: [
      { key: "admin-users", label: "Usuários", href: "/admin?tab=users" },
      {
        key: "admin-pipeline",
        label: "Gestão de Esteira",
        href: "/admin?tab=pipeline",
      },
      {
        key: "admin-notifications",
        label: "Notificações",
        href: "/admin?tab=notifications",
      },
    ],
  },
]);

function onSelect(_item) {
  /* hook para ações do submenu */
}

// Colunas da esteira (carregadas por processo). Mantemos vazio por padrão
// e populamos via configuração salva ou a partir dos estágios presentes nas propostas.
const stages = ref([]);
const lastStagesSaveAt = ref(0)

// Load pipeline config: API-first, fallback local
async function loadPipelineConfig() {
  try {
    const { fetchStagesApi } = await import('~/composables/useStages')
    const { isApiEnabled } = await import('~/utils/api/index')
    if (isApiEnabled()) {
      const apiStages = await fetchStagesApi(pipelineKey.value)
      if (Array.isArray(apiStages)) {
        stages.value = apiStages.map((s) => ({
          id: s.id || s.key || s._id || (s.title || '').toLowerCase().replace(/\s+/g, '_'),
          title: s.title || s.name || 'Etapa',
          slaDays: Number(s.slaDays ?? 0),
          color: s.color || 'sky',
          status: 'Pendente'
        }))
        return
      }
    }
  } catch (_) {}
  // Fallback local (somente se API off)
  try {
    const raw = localStorage.getItem(`pipeline_config__${pipelineKey.value}`);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        const statusById = Object.fromEntries(stages.value.map((s) => [s.id, s.status]));
        stages.value = parsed.map((s) => ({
          id: s.id,
          title: s.title,
          slaDays: s.slaDays ?? 0,
          color: s.color || 'sky',
          status: statusById[s.id] || 'Pendente',
        }));
      }
    }
  } catch (_) {}
  ensureStagesCoverProposals();
}

async function syncProposalsFromApi() {
  if (!isApiEnabled()) return
  try {
    const arr = await fetchProposalsApi(pipelineKey.value)
    if (Array.isArray(arr)) proposals.value = arr
  } catch {}
}

onMounted(() => {
  loadPipelineConfig()
  syncProposalsFromApi()
  try { window.addEventListener('focus', syncProposalsFromApi) } catch {}
});
onBeforeUnmount(() => { try { window.removeEventListener('focus', syncProposalsFromApi) } catch {} })
onActivated(() => { syncProposalsFromApi() })
watch(
  () => pipelineKey.value,
  () => {
    loadPipelineConfig();
    loadStageForms();
    initStageFields();
    if (isApiEnabled()) {
      fetchProposalsApi(pipelineKey.value).then(arr => { if (Array.isArray(arr)) proposals.value = arr }).catch(() => {})
    }
  }
);

// Propostas por processo (persistência local por enquanto)
const proposals = ref(loadProposals(pipelineKey.value) || []);
watch(
  () => pipelineKey.value,
  (k) => {
    const loaded = loadProposals(k);
    proposals.value = Array.isArray(loaded) ? loaded : [];
    if (isApiEnabled()) {
      fetchProposalsApi(k).then(arr => { if (Array.isArray(arr)) proposals.value = arr }).catch(() => {})
    }
  }
);
const persistProposals = () => {
  try { saveProposals(pipelineKey.value, proposals.value); } catch (_) {}
};

// Ensure Kanban has columns for all proposal stageIds
function titleFromId(id) {
  try {
    const pretty = String(id || "").replace(/_/g, " ");
    return pretty.replace(/\b\w/g, (m) => m.toUpperCase());
  } catch {
    return id;
  }
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
      status: "Pendente",
      color: "sky",
      auto: true,
    }));
    stages.value = stages.value.concat(now);
  }
}

// Recalcula colunas apenas quando o conjunto de stageIds muda
watch(
  () => proposals.value.map((p) => p.stageId).join("|"),
  () => ensureStagesCoverProposals()
);

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

async function openCardForm(p) {
  selectedProposal.value = p
  // load fields for current stage
  try { stageFormFields.value = await fetchStageFieldsApi(String(p.stageId)) } catch { stageFormFields.value = [] }
  // load saved values from backend
  stageFormValues.value = {}
  if (isApiEnabled()) {
    try {
      const all = await fetchProposalFormsApi(pipelineKey.value, String(p.id))
      stageFormValues.value = (all && all[String(p.stageId)]) ? { ...all[String(p.stageId)] } : {}
    } catch {}
  }
  showStageFormModal.value = true
}

async function saveStageForm() {
  const p = selectedProposal.value
  if (!p) { showStageFormModal.value = false; return }
  try {
    if (isApiEnabled()) {
      await saveProposalStageFormApi(pipelineKey.value, String(p.id), String(p.stageId), stageFormValues.value)
    }
  } catch {}
  showStageFormModal.value = false
}

function openDeleteProposal(p) {
  deleteProposalTarget.value = p
  showDeleteProposalModal.value = true
}
function cancelDeleteProposal() {
  showDeleteProposalModal.value = false
  deleteProposalTarget.value = null
}
async function confirmDeleteProposal() {
  const p = deleteProposalTarget.value
  if (!p) return cancelDeleteProposal()
  // optimistic UI update
  const prev = proposals.value.slice()
  proposals.value = proposals.value.filter(x => x.id !== p.id)
  try {
    if (isApiEnabled()) {
      const ok = await deleteProposalApi(pipelineKey.value, String(p.id))
      if (!ok) proposals.value = prev
    } else {
      persistProposals()
    }
  } catch { proposals.value = prev }
  cancelDeleteProposal()
}

function openMoveModal(p) {
  // For now open the stage form as edit; future: implement a quick move popup
  openCardForm(p)
}

// Stage form modal state
const showStageFormModal = ref(false)
const selectedProposal = ref(null)
const stageFormFields = ref([])
const stageFormValues = ref({})
const showDeleteProposalModal = ref(false)
const deleteProposalTarget = ref(null)

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
  sky: {
    border: "border-sky-500",
    strip: "bg-sky-500",
    badge: "bg-sky-900/50 text-sky-300 ring-1 ring-inset ring-sky-500/20",
  },
  indigo: {
    border: "border-indigo-500",
    strip: "bg-indigo-500",
    badge:
      "bg-indigo-900/50 text-indigo-300 ring-1 ring-inset ring-indigo-500/20",
  },
  amber: {
    border: "border-amber-500",
    strip: "bg-amber-500",
    badge: "bg-amber-900/50 text-amber-300 ring-1 ring-inset ring-amber-500/20",
  },
  rose: {
    border: "border-rose-500",
    strip: "bg-rose-500",
    badge: "bg-rose-900/50 text-rose-300 ring-1 ring-inset ring-rose-500/20",
  },
  green: {
    border: "border-green-500",
    strip: "bg-green-500",
    badge: "bg-green-900/50 text-green-300 ring-1 ring-inset ring-green-500/20",
  },
  purple: {
    border: "border-purple-500",
    strip: "bg-purple-500",
    badge:
      "bg-purple-900/50 text-purple-300 ring-1 ring-inset ring-purple-500/20",
  },
  blue: {
    border: "border-blue-500",
    strip: "bg-blue-500",
    badge: "bg-blue-900/50 text-blue-300 ring-1 ring-inset ring-blue-500/20",
  },
  orange: {
    border: "border-orange-500",
    strip: "bg-orange-500",
    badge:
      "bg-orange-900/50 text-orange-300 ring-1 ring-inset ring-orange-500/20",
  },
  teal: {
    border: "border-teal-500",
    strip: "bg-teal-500",
    badge: "bg-teal-900/50 text-teal-300 ring-1 ring-inset ring-teal-500/20",
  },
  cyan: {
    border: "border-cyan-500",
    strip: "bg-cyan-500",
    badge: "bg-cyan-900/50 text-cyan-300 ring-1 ring-inset ring-cyan-500/20",
  },
  lime: {
    border: "border-lime-500",
    strip: "bg-lime-500",
    badge: "bg-lime-900/50 text-lime-300 ring-1 ring-inset ring-lime-500/20",
  },
  emerald: {
    border: "border-emerald-500",
    strip: "bg-emerald-500",
    badge:
      "bg-emerald-900/50 text-emerald-300 ring-1 ring-inset ring-emerald-500/20",
  },
  fuchsia: {
    border: "border-fuchsia-500",
    strip: "bg-fuchsia-500",
    badge:
      "bg-fuchsia-900/50 text-fuchsia-300 ring-1 ring-inset ring-fuchsia-500/20",
  },
  violet: {
    border: "border-violet-500",
    strip: "bg-violet-500",
    badge:
      "bg-violet-900/50 text-violet-300 ring-1 ring-inset ring-violet-500/20",
  },
  pink: {
    border: "border-pink-500",
    strip: "bg-pink-500",
    badge: "bg-pink-900/50 text-pink-300 ring-1 ring-inset ring-pink-500/20",
  },
  red: {
    border: "border-red-500",
    strip: "bg-red-500",
    badge: "bg-red-900/50 text-red-300 ring-1 ring-inset ring-red-500/20",
  },
  yellow: {
    border: "border-yellow-500",
    strip: "bg-yellow-500",
    badge:
      "bg-yellow-900/50 text-yellow-300 ring-1 ring-inset ring-yellow-500/20",
  },
  slate: {
    border: "border-slate-500",
    strip: "bg-slate-500",
    badge: "bg-slate-900/50 text-slate-300 ring-1 ring-inset ring-slate-500/20",
  },
  stone: {
    border: "border-stone-500",
    strip: "bg-stone-500",
    badge: "bg-stone-900/50 text-stone-300 ring-1 ring-inset ring-stone-500/20",
  },
  zinc: {
    border: "border-zinc-500",
    strip: "bg-zinc-500",
    badge: "bg-zinc-900/50 text-zinc-300 ring-1 ring-inset ring-zinc-500/20",
  },
};

const stageBorderClass = (stageId) => {
  const st = stages.value.find((s) => s.id === stageId);
  return (st && colorStyles[st.color]?.border) || "border-slate-500";
};
const stageStripClass = (stageId) => {
  const st = stages.value.find((s) => s.id === stageId);
  return (st && colorStyles[st.color]?.strip) || "bg-slate-500";
};
const stageBadgeClass = (stageId) => {
  const st = stages.value.find((s) => s.id === stageId);
  return (
    (st && colorStyles[st.color]?.badge) ||
    "bg-slate-700 text-slate-300 ring-1 ring-inset ring-slate-500/20"
  );
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
const draggingStageId = ref("");
const columnDragOverIndex = ref(-1);
const isDraggingStage = computed(() => !!draggingStageId.value);

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
  if (isDraggingStage.value) return;
  dragOverStage.value = stageId;
};

const onDragLeave = (stageId) => {
  if (isDraggingStage.value) return;
  if (dragOverStage.value === stageId) dragOverStage.value = "";
};

const onDrop = (stageId) => {
  // If dragging a column, ignore card drop handler
  // (column drop is handled in capture phase)
  if (isDraggingStage.value) return;
  if (!draggedId.value) return;
  const idx = proposals.value.findIndex((p) => p.id === draggedId.value);
  if (idx === -1) return onDragEnd();

  const stage = stages.value.find((s) => s.id === stageId);
  if (!stage) return onDragEnd();

  // Update stage and default status when moved
  const updated = {
    ...proposals.value[idx],
    stageId,
    status: stage.status,
    stageEnteredAt: new Date().toISOString(),
  };
  const next = proposals.value.slice();
  next[idx] = updated;
  proposals.value = next;
  if (isApiEnabled()) {
    const pid = String(updated.id)
    const prev = proposals.value[idx]
    updateProposalApi(pipelineKey.value, pid, { stageId: stageId, status: stage.status })
      .catch(() => {
        // rollback on failure
        const arr = proposals.value.slice();
        arr[idx] = { ...prev }
        proposals.value = arr
      })
  } else {
    persistProposals();
  }

  onDragEnd();
};

// Column reorder (DnD)
const STAGE_MIME = "application/x-stage-id";
function onStageDragStart(event, stage, index) {
  draggingStageId.value = stage.id;
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    try {
      event.dataTransfer.setData(STAGE_MIME, stage.id);
    } catch (_) {}
  }
}
function onStageDragEnd() {
  draggingStageId.value = "";
  columnDragOverIndex.value = -1;
}
function onColumnDragOver(event, overIndex) {
  if (!draggingStageId.value) return;
  const types = Array.from(event?.dataTransfer?.types || []);
  if (!types.includes(STAGE_MIME)) return;
  event.preventDefault();
  columnDragOverIndex.value = overIndex;
}
function persistStages() {
  if (isApiEnabled()) {
    const { success, error } = useToast()
    saveStagesApi(pipelineKey.value, stages.value)
      .then((saved) => {
        if (Array.isArray(saved) && saved.length) {
          // preserva status por título quando possível
          const statusByTitle = {}
          try { stages.value.forEach((s) => { statusByTitle[s.title] = s.status || 'Pendente' }) } catch {}
          stages.value = saved.map((s) => ({
            id: s.id,
            title: s.title,
            slaDays: s.slaDays,
            color: s.color,
            status: statusByTitle[s.title] || 'Pendente'
          }))
        }
      const now = Date.now()
        if (now - lastStagesSaveAt.value > 1500) {
          success('Colunas atualizadas')
          lastStagesSaveAt.value = now
        }
      })
      .catch(() => error('Falha ao salvar colunas'))
    return
  }
  try {
    localStorage.setItem(
      `pipeline_config__${pipelineKey.value}`,
      JSON.stringify(stages.value)
    );
  } catch (_) {}
}
function onColumnDrop(event, dropIndex) {
  const types = Array.from(event?.dataTransfer?.types || []);
  if (!types.includes(STAGE_MIME)) return;
  event.preventDefault();
  event.stopPropagation();
  const dragged =
    event.dataTransfer.getData(STAGE_MIME) || draggingStageId.value;
  const fromIndex = stages.value.findIndex((s) => s.id === dragged);
  if (fromIndex < 0 || dropIndex < 0 || fromIndex === dropIndex) {
    onStageDragEnd();
    return;
  }
  const arr = stages.value.slice();
  const [moved] = arr.splice(fromIndex, 1);
  arr.splice(dropIndex, 0, moved);
  stages.value = arr;
  persistStages();
  onStageDragEnd();
}

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
  if (stageDynamicFields.value.length > 0) notify("Campos da etapa carregados");
  showNewModal.value = true;
};

const closeNewProposalModal = () => {
  showNewModal.value = false;
};

const validateNewProposal = () => {
  // Validação agora é totalmente guiada pelos campos dinâmicos
  formErrors.value = { name: "", cpf: "", amountDesired: "" };
  return true;
};

const saveNewProposal = () => {
  if (!validateNewProposal()) return;
  // validate dynamic required fields
  extraFieldErrors.value = {};
  let okExtra = true;
  for (const f of stageDynamicFields.value) {
    if (!f?.required) continue;
    if (f.type === "file") {
      const hasFile = !!extraFieldFiles.value?.[f.id]?.length;
      if (!hasFile) {
        extraFieldErrors.value[f.id] = "Obrigatório";
        okExtra = false;
      }
    } else {
      const val = (extraFieldValues.value?.[f.id] ?? "").toString().trim();
      if (!val) {
        extraFieldErrors.value[f.id] = "Obrigatório";
        okExtra = false;
      }
    }
  }
  if (!okExtra) {
    showAdvanced.value = true;
    notify("Preencha os campos obrigatórios da etapa");
    return;
  }
  const stage = stages.value.find((s) => s.id === newProposal.value.stageId);
  const parseCurrency = (s) => {
    const str = String(s || "").trim();
    if (!str) return 0;
    const normalized = str.replace(/\./g, "").replace(/,/g, ".");
    const n = Number(normalized);
    return isNaN(n) ? 0 : n;
  };
  // Deriva 'name' e 'amount' a partir dos campos dinâmicos
  const deriveName = () => {
    for (const f of stageDynamicFields.value) {
      const lbl = (f.label || '').toLowerCase()
      if (lbl.includes('nome')) {
        const v = (extraFieldValues.value?.[f.id] || '').toString().trim()
        if (v) return v
      }
    }
    const firstText = stageDynamicFields.value.find(f => f.type === 'text')
    return (firstText ? (extraFieldValues.value?.[firstText.id] || '').toString().trim() : '') || 'Registro'
  }
  const deriveAmount = () => {
    for (const f of stageDynamicFields.value) {
      if (f.type === 'number') {
        const lbl = (f.label || '').toLowerCase()
        if (lbl.includes('valor') || lbl.includes('amount') || lbl.includes('preço') || lbl.includes('preco')) {
          return parseCurrency(extraFieldValues.value?.[f.id])
        }
      }
    }
    return 0
  }
  const payload = {
    name: deriveName(),
    amount: deriveAmount(),
    stageId: newProposal.value.stageId,
    status: stage?.status || 'Pendente'
  }
  const stageValues = (() => {
    const out = {}
    try { for (const f of stageDynamicFields.value) { out[f.id] = (extraFieldValues.value?.[f.id] ?? '') } } catch {}
    return out
  })()
  if (isApiEnabled()) {
    createProposalApi(pipelineKey.value, payload)
      .then(async (created) => {
        if (created) {
          try { await saveProposalStageFormApi(pipelineKey.value, String(created.id), String(created.stageId), stageValues) } catch {}
          proposals.value = [created, ...proposals.value]
        }
      })
      .catch(() => {})
  } else {
    const id = Date.now();
    proposals.value.unshift({
      id,
      name: payload.name,
      amount: payload.amount,
      stageId: payload.stageId,
      status: payload.status,
      isArchived: false,
      stageEnteredAt: new Date().toISOString(),
      details: undefined,
    })
    persistProposals();
  }
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
const toast = ref({ show: false, text: "" });
function notify(msg) {
  toast.value = { show: true, text: msg };
  setTimeout(() => (toast.value.show = false), 2200);
}

// Dynamic fields per stage (from Admin builder)
const stageFormsMap = ref({});
const stageDynamicFields = ref([]);
const extraFieldValues = ref({});
const extraFieldFiles = ref({});
const extraFieldErrors = ref({});

function loadStageForms() {
  try {
    const raw = localStorage.getItem(
      `pipeline_stage_forms__${pipelineKey.value}`
    );
    if (raw) stageFormsMap.value = JSON.parse(raw) || {};
  } catch (_) {
    stageFormsMap.value = {};
  }
}

function initStageFields() {
  const sid = newProposal.value.stageId;
  const fallback = () => {
    const arr = Array.isArray(stageFormsMap.value[sid]) ? stageFormsMap.value[sid] : []
    stageDynamicFields.value = arr
    const vals = {}
    arr.forEach((f) => { if (f.type !== 'file') vals[f.id] = '' })
    extraFieldValues.value = vals
    extraFieldFiles.value = {}
    extraFieldErrors.value = {}
  }
  // If API is enabled and stage id looks like Mongo ObjectId, load fields from backend
  const isMongoId = typeof sid === 'string' && /^[a-fA-F0-9]{24}$/.test(sid)
  if (isApiEnabled() && isMongoId) {
    fetchStageFieldsApi(sid)
      .then(remote => {
        const arr = Array.isArray(remote)
          ? remote.map(r => ({ id: r.id || r.label, label: r.label, type: r.type, required: !!r.required, placeholder: r.placeholder || '', options: Array.isArray(r.options) ? r.options : [] }))
          : []
        stageDynamicFields.value = arr
        const vals = {}
        arr.forEach((f) => { if (f.type !== 'file') vals[f.id] = '' })
        extraFieldValues.value = vals
        extraFieldFiles.value = {}
        extraFieldErrors.value = {}
      })
      .catch(() => fallback())
  } else {
    fallback()
  }
}

function onExtraFile(id, files) {
  extraFieldFiles.value[id] = files;
  // Upload immediately if API is enabled and store object keys as values
  try {
    if (isApiEnabled()) {
      const arr = Array.from(files || [])
      Promise.all(arr.map((f) => uploadFileViaPresign(f, `proposals/tmp`)))
        .then(results => {
          // store serialized list of keys or first key depending on expected shape
          const keys = results.map(r => r.objectKey)
          extraFieldValues.value[id] = arr.length <= 1 ? keys[0] : keys
        })
        .catch(() => {})
    }
  } catch {}
}

function onStageFormFile(id, files) {
  try {
    if (isApiEnabled()) {
      const arr = Array.from(files || [])
      Promise.all(arr.map((f) => uploadFileViaPresign(f, `proposals/${selectedProposal.value?.id || 'unknown'}`)))
        .then(results => {
          const keys = results.map(r => r.objectKey)
          stageFormValues.value[id] = arr.length <= 1 ? keys[0] : keys
        })
        .catch(() => {})
    }
  } catch {}
}

watch(
  () => newProposal.value.stageId,
  () => initStageFields()
);
onMounted(() => {
  loadStageForms();
  initStageFields();
});

// IA Chat modal (reutilizável via componente)
const showAiModal = ref(false);
const CHAT_STORE_KEY = "consoria_chat_history";
const aiMessages = ref([
  {
    role: "assistant",
    content: "Olá! Eu sou a ConsorIA. Como posso ajudar hoje?",
  },
]);
const aiTyping = ref(false);

const openAiChat = () => {
  try {
    const raw = localStorage.getItem(CHAT_STORE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (
        Array.isArray(parsed) &&
        parsed.every((m) => m && typeof m.role === "string")
      ) {
        aiMessages.value = parsed;
      }
    }
  } catch (_) {}
  showAiModal.value = true;
};
const closeAiChat = () => {
  showAiModal.value = false;
};
const onAiSend = async ({ text, attachments }) => {
  aiMessages.value.push({ role: "user", content: text, attachments });
  aiTyping.value = true;
  try {
    const history = aiMessages.value
      .slice(-8)
      .map((m) => ({ role: m.role, content: m.content }));
    const res = await $fetch("/api/ai", {
      method: "POST",
      body: { text, attachments, history },
    });
    aiMessages.value.push({
      role: "assistant",
      content: res?.["text"] || res?.["message"] || "Sem resposta.",
    });
  } catch (e) {
    aiMessages.value.push({
      role: "assistant",
      content: "Falha ao obter resposta da IA. Verifique sua configuração.",
    });
  } finally {
    aiTyping.value = false;
  }
};

// Persist chat history locally
// Persiste histórico apenas quando o tamanho da lista muda
watch(
  () => aiMessages.value.length,
  () => {
    try {
      localStorage.setItem(
        CHAT_STORE_KEY,
        JSON.stringify(aiMessages.value.slice(-50))
      );
    } catch (_) {}
  }
);
</script>

<style scoped>
/* Smooth column reordering */
.kanban-col-move { transition: transform 180ms ease, opacity 180ms ease; }
.kanban-col-enter-active, .kanban-col-leave-active { transition: all 180ms ease; }
.kanban-col-enter-from, .kanban-col-leave-to { opacity: 0; transform: translateY(6px); }

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
