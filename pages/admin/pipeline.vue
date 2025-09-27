<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900"
  >
    <!-- Header -->
    <header
      class="app-header bg-slate-900 border-b border-slate-800/70 px-6 py-4"
    >
      <div class="flex items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-lg bg-slate-800 text-slate-200"
          >
            <i class="fa-solid fa-sitemap"></i>
          </div>
          <div>
            <h1 class="app-header-title">Configuração da Esteira</h1>
            <p class="app-header-subtitle">
              Cadastre processos e mantenha o fluxo operacional organizado
            </p>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <button
            class="inline-flex items-center gap-2 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 transition hover:border-indigo-500 hover:text-white"
            @click="openCreateProcessModal"
          >
            <i class="fa-solid fa-plus text-xs"></i>
            Novo processo
          </button>

          <button
            class="flex h-11 w-11 items-center justify-center rounded-lg border border-slate-700 bg-slate-900 text-slate-200 transition hover:border-indigo-500 hover:text-white"
            @click="openCreateProcessModal"
            :aria-label="'Criar processo'"
          >
            <i class="fa-solid fa-sitemap"></i>
          </button>
        </div>
      </div>
    </header>

    <main class="px-6 py-5 space-y-6">
      <section
        class="rounded-xl border border-slate-800 bg-slate-900/80 px-5 py-4"
      >
        <p class="text-sm text-slate-400">
          Utilize o botão <span class="text-slate-200">Novo processo</span> para
          registrar fluxos adicionais. Os processos existentes estão listados
          abaixo.
        </p>
      </section>

      <!-- Card: Lista de Processos -->
      <section
        class="rounded-xl border border-slate-800 bg-slate-900/80 px-5 py-5"
        aria-labelledby="pipeline-list-header"
      >
        <div class="flex items-center justify-between gap-3 flex-wrap mb-4">
          <div>
            <h2
              id="pipeline-list-header"
              class="text-base font-semibold text-slate-100"
            >
              Processos existentes
            </h2>
            <p class="text-sm text-slate-400">
              Selecione um processo para editar etapas e configurações
            </p>
          </div>
        </div>

        <div class="p-2">
          <div
            class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3"
          >
            <div
              v-for="(p, index) in processes"
              :key="p.key"
              class="group relative cursor-pointer overflow-hidden rounded-xl border border-slate-800/70 bg-slate-900/80 p-3 transition-all duration-300 hover:border-slate-700 hover:shadow-lg"
              :class="
                p.key === currentProcessKey
                  ? 'border-emerald-500/60 shadow-[0_0_25px_rgba(16,185,129,0.25)]'
                  : ''
              "
              :style="{ animationDelay: `${index * 25}ms` }"
              @click="editProcess(p)"
            >
              <button
                class="absolute top-1.5 left-1.5 text-[11px] px-2 py-0.5 rounded-full border border-slate-700 text-slate-400 uppercase tracking-wide"
                @click.stop="currentProcessKey = p.key"
              >
                {{ p.key }}
              </button>

              <div class="mt-5 space-y-3">
                <div class="flex items-center justify-between">
                  <h3 class="text-sm font-semibold text-white truncate">
                    {{ p.name || "Processo" }}
                  </h3>
                  <button
                    class="rounded-md border border-slate-700 bg-slate-900 p-2 text-slate-200 hover:bg-slate-800"
                    @click.stop="toggleActive(p)"
                    :title="p.active !== false ? 'Desativar' : 'Ativar'"
                  >
                    <i
                      :class="
                        p.active !== false
                          ? 'fa-solid fa-power-off text-emerald-300'
                          : 'fa-solid fa-play text-slate-400'
                      "
                    />
                  </button>
                </div>

                <div class="flex items-center justify-between text-xs">
                  <div class="flex items-center gap-2">
                    <span
                      class="inline-flex items-center gap-2 rounded-full px-2 py-0.5 text-xs"
                      :class="processTypeBadgeClass(p.type, p.isFinanceiro)"
                    >
                      <i :class="['fa-solid', processTypeIcon(p.type, p.isFinanceiro)]"></i>
                      {{ resolveProcessTypeLabel(p.type, p.isFinanceiro) }}
                    </span>
                    <span
                      class="inline-flex items-center gap-1"
                      :class="p.active !== false ? 'text-emerald-300' : 'text-slate-400'"
                    >
                      <span
                        :class="[
                          'inline-block w-2 h-2 rounded-full',
                          p.active !== false ? 'bg-emerald-400' : 'bg-slate-500',
                        ]"
                      ></span>
                      {{ p.active !== false ? 'Ativo' : 'Inativo' }}
                    </span>
                  </div>

                  <div class="flex items-center gap-2">
                    <button
                      class="rounded-md border border-slate-700 bg-slate-900 p-2 text-red-300 hover:bg-slate-800"
                      @click.stop="openDeleteModal(p)"
                      title="Excluir"
                    >
                      <i class="fa-regular fa-trash-can"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div
          class="mt-4 rounded-lg border border-slate-800 bg-slate-900 px-4 py-3"
        >
          <p class="text-sm text-slate-400">
            <i class="fa-solid fa-info-circle text-slate-500 mr-2"></i>
            Selecione um processo para ajustar etapas, campos e automações.
          </p>
        </div>
      </section>
    </main>

    <!-- Modal: Novo Processo -->
    <BaseModal v-model="showCreateProcessModal" title="Novo processo" size="sm">
      <div class="space-y-4">
        <div>
          <label
            class="text-xs font-semibold text-slate-300 uppercase tracking-wide"
            for="modal-pipeline-name"
          >
            Nome do processo
          </label>
          <input
            id="modal-pipeline-name"
            v-model="newProcName"
            class="mt-2 w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
            placeholder="ex.: ConsorEquity"
          />
        </div>

        <div>
          <label class="text-xs font-semibold text-slate-300 uppercase tracking-wide">
            Tipo do processo
          </label>
          <div class="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-3">
            <button
              v-for="option in processTypeOptions"
              :key="option.value"
              type="button"
              @click="newProcessType = option.value"
              :class="[
                'flex items-start gap-3 rounded-xl border px-3 py-3 text-left transition-all',
                newProcessType === option.value
                  ? 'border-indigo-500 bg-indigo-500/10 shadow-lg shadow-indigo-500/20'
                  : 'border-slate-700/60 bg-slate-900/40 hover:border-indigo-500/40'
              ]"
            >
              <div
                class="w-9 h-9 rounded-lg bg-slate-900/70 flex items-center justify-center text-base"
              >
                <i :class="option.icon"></i>
              </div>
              <div class="flex-1">
                <p class="text-sm font-semibold text-slate-100">
                  {{ option.label }}
                </p>
                <p class="text-[11px] text-slate-400">
                  {{ option.description }}
                </p>
              </div>
              <i
                v-if="newProcessType === option.value"
                class="fa-solid fa-circle-check text-indigo-400"
              ></i>
            </button>
          </div>
        </div>

        <div>
          <label class="text-xs font-semibold text-slate-300 uppercase tracking-wide">
            Usuários autorizados
          </label>
          <div class="mt-2 flex flex-wrap gap-2">
            <span
              v-for="user in newProcessUserDetails"
              :key="user.id"
              class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
            >
              <span class="truncate max-w-[160px]">{{ user.label }}</span>
              <button
                class="text-indigo-200/80 hover:text-white"
                type="button"
                @click="removeNewProcessUser(user.id)"
              >
                <i class="fa-solid fa-xmark text-[10px]"></i>
              </button>
            </span>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-[12px] text-slate-200 hover:border-indigo-500/50 hover:text-white"
              @click="openUserSelector('create')"
            >
              <i class="fa-solid fa-user-plus text-xs"></i>
              Gerenciar acesso
            </button>
          </div>
          <p class="text-[11px] text-slate-400 mt-1">
            Deixe vazio para liberar o acesso a todos.
          </p>
        </div>
      </div>

      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-300 hover:bg-slate-800"
            @click="closeCreateProcessModal"
          >
            Cancelar
          </button>
          <button
            class="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-500 disabled:opacity-60"
            :disabled="creatingProcess || !newProcName.trim()"
            @click="createProcess"
          >
            <i
              v-if="creatingProcess"
              class="fa-solid fa-spinner fa-spin text-xs"
            ></i>
            <template v-else>
              <i class="fa-solid fa-plus text-xs"></i>
              Criar processo
            </template>
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Gestão da Esteira (BaseModal) -->
    <BaseModal
      v-model="showPipelineModal"
      title="Gestão da Esteira"
      size="lg"
      :z-index="55"
    >
      <div class="space-y-3 max-h-[70vh] overflow-y-auto pr-1">
        <div>
          <label class="text-[12px] text-slate-300">Nome do Processo</label>
          <input
            v-model="processName"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="ex.: ConsorEquity"
          />
        </div>
        <div>
          <label class="text-[12px] text-slate-300">Usuários com acesso à esteira</label>
          <div class="mt-2 flex flex-wrap gap-2">
            <span
              v-for="user in selectedProcessUserDetails"
              :key="user.id"
              class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
            >
              <span class="truncate max-w-[160px]">{{ user.label }}</span>
              <button
                class="text-indigo-200/80 hover:text-white"
                type="button"
                @click="removeSelectedProcessUser(user.id)"
              >
                <i class="fa-solid fa-xmark text-[10px]"></i>
              </button>
            </span>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-[12px] text-slate-200 hover:border-indigo-500/50 hover:text-white"
              @click="openUserSelector('edit')"
            >
              <i class="fa-solid fa-user-plus text-xs"></i>
              Gerenciar acesso
            </button>
          </div>
          <p class="text-[11px] text-slate-400 mt-1">
            Sem seleção, todos os usuários com acesso ao sistema enxergam esta esteira.
          </p>
        </div>
        <PipelineManager
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
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-3 py-2 rounded-md text-sm"
            @click="savePipelineModal"
          >
            Salvar
          </button>
        </div>
      </template>
    </BaseModal>

    <BaseModal
      v-model="showUserSelectorModal"
      title="Selecionar usuários"
      size="md"
    >
      <div class="space-y-3">
        <input
          v-model="userSearch"
          type="search"
          class="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
          placeholder="Buscar por nome ou e-mail"
        />
        <div class="max-h-64 overflow-y-auto rounded-lg border border-slate-800 bg-slate-900/60">
          <div
            v-if="!filteredModalUsers.length"
            class="px-3 py-4 text-center text-sm text-slate-500"
          >
            Nenhum usuário encontrado.
          </div>
          <button
            v-for="user in filteredModalUsers"
            :key="user.id"
            type="button"
            :class="[
              'w-full flex items-center justify-between px-3 py-2 text-sm transition-colors border-b border-slate-800 last:border-b-0',
              modalSelectionSet.has(user.id)
                ? 'bg-indigo-500/10 text-indigo-200 border-indigo-500/30'
                : 'text-slate-200 hover:bg-slate-800/60'
            ]"
            @click="toggleUserInSelection(user.id)"
          >
            <span class="truncate max-w-[220px] text-left">{{ user.label }}</span>
            <i
              :class="modalSelectionSet.has(user.id)
                ? 'fa-solid fa-check text-indigo-300'
                : 'fa-regular fa-circle text-slate-500'"
            ></i>
          </button>
        </div>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="id in userSelectorSelection"
            :key="id"
            class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
          >
            <span class="truncate max-w-[160px]">
              {{ userMap.get(id)?.label || id }}
            </span>
            <button
              class="text-indigo-200/80 hover:text-white"
              type="button"
              @click="toggleUserInSelection(id)"
            >
              <i class="fa-solid fa-xmark text-[10px]"></i>
            </button>
          </span>
        </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-300 hover:bg-slate-800"
            @click="closeUserSelectorModal"
          >
            Cancelar
          </button>
          <button
            class="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-500"
            @click="confirmUserSelection"
          >
            Confirmar
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Confirmar Exclusão de Processo -->
    <BaseModal
      v-model="showDeleteModal"
      title="Confirmar exclusão"
      size="sm"
      :z-index="60"
    >
      <div class="space-y-2">
        <p class="text-sm text-slate-200">
          Para confirmar, digite exatamente o nome do processo abaixo.
        </p>
        <div
          class="rounded-md border border-slate-700/60 bg-slate-900/40 px-3 py-2"
        >
          <div class="text-white text-sm font-semibold">
            {{ deleteTarget?.name || deleteTarget?.key }}
          </div>
          <div class="text-[11px] text-slate-400">{{ deleteTarget?.key }}</div>
        </div>
        <div>
          <label class="text-[12px] text-slate-300"
            >Digite o nome do processo</label
          >
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
          <div
            v-if="deleteConfirm && !canConfirmDelete"
            class="text-[11px] text-red-400 mt-1"
          >
            O nome digitado não confere.
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="cancelDelete"
          >
            Cancelar
          </button>
          <button
            class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm"
            :disabled="!canConfirmDelete"
            @click="confirmDelete"
          >
            Excluir Processo
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Confirmar Reset -->
    <BaseModal
      v-model="showResetModal"
      title="Confirmar reset"
      size="sm"
      :z-index="60"
    >
      <div class="space-y-2">
        <p class="text-sm text-slate-200">
          Tem certeza que deseja zerar todos os dados locais?
        </p>
        <p class="text-[12px] text-slate-400">
          Esta ação removerá todos os processos e configurações locais.
        </p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="cancelReset"
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
import { ref, computed, watch, onMounted } from "vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import PipelineManager from "~/components/admin/PipelineManager.vue";
import {
  listProcesses,
  addProcess,
  setProcessActive,
  removeProcess,
  setProcessName,
  sanitizeProcessKey,
  renameProcessKey,
  applyProcessBlueprint,
} from "~/composables/usePipeline";
import {
  fetchStagesApi,
  saveStagesPreservingIdsApi,
} from "~/composables/useStages";
import {
  fetchStageFieldsApi,
  saveStageFieldsApi,
} from "~/composables/useStageFields";
import { isApiEnabled } from "~/utils/api/index";
import { useProcessSubmenu } from "~/composables/useProcessMenu";
import { useToast } from "~/composables/useToast";
import { fetchUsersApi } from "~/composables/useUsersApi";

definePageMeta({
  layout: "sidebar",
  title: "Pipeline - Admin",
});

// Composables
const { setLastKey } = useProcessSubmenu();
const {
  success: toastSuccess,
  error: toastError,
  info: toastInfo,
} = useToast();

// Process registry
const processes = ref(listProcesses());
const currentProcessKey = ref(processes.value[0]?.key || "");
const newProcName = ref("");
const newProcessType = ref('GENERIC');
const creatingProcess = ref(false);
const showCreateProcessModal = ref(false);
const usersList = ref([]);
const newProcessUsers = ref([]);
const selectedProcessUsers = ref([]);
const showUserSelectorModal = ref(false);
const userSelectorMode = ref('create');
const userSelectorSelection = ref([]);
const userSearch = ref('');

const processTypeOptions = [
  {
    value: 'GENERIC',
    label: 'Pipeline Livre',
    description: 'Fluxo flexível para diferentes contextos.',
    icon: 'fa-solid fa-diagram-project text-slate-300',
  },
  {
    value: 'FINANCIAL',
    label: 'Financeiro',
    description: 'Controle de etapas com indicadores financeiros.',
    icon: 'fa-solid fa-dollar-sign text-emerald-400',
  },
  {
    value: 'TODO_LIST',
    label: 'To-do List',
    description: 'Gestão de tarefas e checklists colaborativos.',
    icon: 'fa-solid fa-list-check text-sky-400',
  },
  {
    value: 'LEAD_QUALIFICATION',
    label: 'Qualificação de Leads',
    description: 'Integração direta com o cadastro de leads.',
    icon: 'fa-solid fa-user-check text-indigo-400',
  },
];

const processTypeLabels = {
  GENERIC: 'Pipeline Livre',
  FINANCIAL: 'Financeiro',
  TODO_LIST: 'To-do List',
  LEAD_QUALIFICATION: 'Qualificação de Leads',
};

const userSelectionOptions = computed(() =>
  (usersList.value || [])
    .filter((user) => typeof user?.id === "string" && user.id)
    .map((user) => ({
      id: String(user.id),
      label: [user.name, user.email]
        .filter((value) => typeof value === "string" && value.trim())
        .join(" — ") || String(user.id),
    }))
);

const userMap = computed(() => {
  const map = new Map();
  for (const user of usersList.value || []) {
    if (!user?.id) continue;
    map.set(String(user.id), {
      id: String(user.id),
      name: user.name,
      email: user.email,
      label:
        [user.name, user.email]
          .filter((value) => typeof value === "string" && value.trim())
          .join(" — ") || String(user.id),
    });
  }
  return map;
});

const selectedProcessUserDetails = computed(() =>
  sanitizeUserSelection(selectedProcessUsers.value).map((id) =>
    userMap.value.get(id) || { id, label: id }
  )
);

const newProcessUserDetails = computed(() =>
  sanitizeUserSelection(newProcessUsers.value).map((id) =>
    userMap.value.get(id) || { id, label: id }
  )
);

function sanitizeUserSelection(ids) {
  return Array.from(
    new Set(
      (ids || [])
        .filter((id) => typeof id === "string")
        .map((id) => id.trim())
        .filter(Boolean)
    )
  );
}

function resolveProcessTypeLabel(type, isFinanceiro) {
  const normalized = typeof type === 'string' ? type.trim().toUpperCase() : undefined;
  if (!normalized) {
    return isFinanceiro ? processTypeLabels.FINANCIAL : processTypeLabels.GENERIC;
  }
  return processTypeLabels[normalized] || processTypeLabels.GENERIC;
}

function processTypeIcon(type, isFinanceiro) {
  const normalized = typeof type === 'string' ? type.trim().toUpperCase() : undefined;
  if (normalized === 'FINANCIAL' || (normalized == null && isFinanceiro)) return 'fa-coins text-emerald-300';
  if (normalized === 'TODO_LIST') return 'fa-list-check text-sky-300';
  if (normalized === 'LEAD_QUALIFICATION') return 'fa-user-check text-indigo-300';
  return 'fa-diagram-project text-slate-300';
}

function processTypeBadgeClass(type, isFinanceiro) {
  const normalized = typeof type === 'string' ? type.trim().toUpperCase() : undefined;
  if (normalized === 'FINANCIAL' || (normalized == null && isFinanceiro)) {
    return 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/40';
  }
  if (normalized === 'TODO_LIST') {
    return 'bg-sky-500/10 text-sky-300 border border-sky-500/30';
  }
  if (normalized === 'LEAD_QUALIFICATION') {
    return 'bg-indigo-500/10 text-indigo-300 border border-indigo-500/30';
  }
  return 'bg-slate-800 text-slate-400 border border-slate-700';
}

function syncCurrentProcessUsers() {
  const current = processes.value.find((p) => p.key === currentProcessKey.value);
  const allowed = sanitizeUserSelection(current?.allowedUserIds || []);
  const validIds = new Set(userSelectionOptions.value.map((user) => user.id));
  selectedProcessUsers.value = allowed.filter((id) => (validIds.size ? validIds.has(id) : true));
}

watch(userSelectionOptions, () => {
  const validIds = new Set(userSelectionOptions.value.map((user) => user.id));
  newProcessUsers.value = sanitizeUserSelection(
    newProcessUsers.value.filter((id) => (validIds.size ? validIds.has(id) : true))
  );
  selectedProcessUsers.value = sanitizeUserSelection(
    selectedProcessUsers.value.filter((id) => (validIds.size ? validIds.has(id) : true))
  );
});

watch(currentProcessKey, () => {
  syncCurrentProcessUsers();
});

watch(processes, () => {
  syncCurrentProcessUsers();
});

async function openUserSelector(mode) {
  if (!usersList.value.length) {
    try {
      await loadUsersList();
    } catch (_) {}
  }
  userSelectorMode.value = mode;
  userSelectorSelection.value = mode === 'create'
    ? [...sanitizeUserSelection(newProcessUsers.value)]
    : [...sanitizeUserSelection(selectedProcessUsers.value)];
  userSearch.value = '';
  showUserSelectorModal.value = true;
}

function closeUserSelectorModal() {
  showUserSelectorModal.value = false;
  userSelectorSelection.value = [];
  userSearch.value = '';
}

function toggleUserInSelection(id) {
  const normalized = String(id);
  const list = new Set(userSelectorSelection.value.map((value) => String(value)));
  if (list.has(normalized)) {
    list.delete(normalized);
  } else {
    list.add(normalized);
  }
  userSelectorSelection.value = Array.from(list);
}

function confirmUserSelection() {
  const sanitized = sanitizeUserSelection(userSelectorSelection.value);
  if (userSelectorMode.value === 'create') {
    newProcessUsers.value = sanitized;
  } else {
    selectedProcessUsers.value = sanitized;
  }
  closeUserSelectorModal();
}

function removeNewProcessUser(id) {
  newProcessUsers.value = sanitizeUserSelection(
    newProcessUsers.value.filter((value) => value !== id)
  );
}

function removeSelectedProcessUser(id) {
  selectedProcessUsers.value = sanitizeUserSelection(
    selectedProcessUsers.value.filter((value) => value !== id)
  );
}

const filteredModalUsers = computed(() => {
  const term = userSearch.value.trim().toLowerCase();
  if (!term) return userSelectionOptions.value;
  return userSelectionOptions.value.filter((user) =>
    user.label.toLowerCase().includes(term)
  );
});

const modalSelectionSet = computed(() => new Set(userSelectorSelection.value));

async function createProcess() {
  const name = newProcName.value.trim();
  if (!name) return;
  // Gera UUID opaco para o processo (mais seguro e estável)
  const key =
    globalThis.crypto?.randomUUID?.() ||
    Math.random().toString(36).slice(2) + Date.now().toString(36);
  if (creatingProcess.value) return;
  creatingProcess.value = true;
  const selectedType = newProcessType.value;
  const blueprint = DEFAULT_PIPELINES[selectedType]
    ? [...DEFAULT_PIPELINES[selectedType]]
    : [];
  const allowedIds = sanitizeUserSelection(newProcessUsers.value);
  // Otimista: atualiza DOM primeiro
  processes.value = [
    ...processes.value,
    {
      key,
      name,
      active: true,
      type: selectedType,
      isFinanceiro: selectedType === 'FINANCIAL',
      allowedUserIds: allowedIds,
    },
  ];
  const ok = await addProcess(key, name || key, selectedType, allowedIds);
  if (ok) {
    if (blueprint.length) {
      try {
        await applyProcessBlueprint(key, blueprint);
        pipelineStages.value = [...blueprint];
      } catch (error) {
        console.error('Falha ao registrar blueprint do processo:', error);
      }
    }
    currentProcessKey.value = key;
    setLastKey(key);
    newProcName.value = "";
    newProcessType.value = 'GENERIC';
    newProcessUsers.value = [];
    selectedProcessUsers.value = sanitizeUserSelection(allowedIds);
    toastSuccess("Processo criado");
    showCreateProcessModal.value = false;
  } else {
    // rollback visual
    processes.value = processes.value.filter((p) => p.key !== key);
    toastError("Falha ao criar processo. Tente novamente.");
  }
  creatingProcess.value = false;
}

function openCreateProcessModal() {
  newProcName.value = "";
  newProcessType.value = 'GENERIC';
  newProcessUsers.value = [];
  showCreateProcessModal.value = true;
}

function closeCreateProcessModal() {
  showCreateProcessModal.value = false;
}

const DEFAULT_PIPELINES = {
  GENERIC: [
    { id: 'briefing', title: 'Briefing Inicial', slaDays: 2, color: 'sky' },
    { id: 'execucao', title: 'Execução', slaDays: 5, color: 'violet' },
    { id: 'revisao', title: 'Revisão', slaDays: 3, color: 'amber' },
  ],
  FINANCIAL: [
    { id: 'entrada', title: 'Entrada de Dados', slaDays: 2, color: 'emerald' },
    { id: 'analise_credito', title: 'Análise de Crédito', slaDays: 3, color: 'cyan' },
    { id: 'formalizacao', title: 'Formalização', slaDays: 4, color: 'indigo' },
  ],
  TODO_LIST: [
    { id: 'planejamento', title: 'Planejamento', slaDays: 1, color: 'pink' },
    { id: 'execucao_tarefas', title: 'Execução de Tarefas', slaDays: 5, color: 'purple' },
    { id: 'validacao', title: 'Validação', slaDays: 2, color: 'lime' },
  ],
  LEAD_QUALIFICATION: [
    { id: 'captacao', title: 'Captação', slaDays: 1, color: 'blue', defaultStatus: 'Novo Lead' },
    { id: 'contato', title: 'Contato Inicial', slaDays: 2, color: 'emerald', defaultStatus: 'Contato Realizado' },
    { id: 'qualificacao', title: 'Qualificação', slaDays: 3, color: 'violet', defaultStatus: 'Qualificado' },
    { id: 'negociacao', title: 'Negociação', slaDays: 2, color: 'amber', defaultStatus: 'Em Negociação' },
  ],
};

const pipelineStages = ref([...DEFAULT_PIPELINES.GENERIC]);
const processName = ref("");

// Modal Gestão da Esteira
const showPipelineModal = ref(false);
function openPipelineModal() {
  showPipelineModal.value = true;
}
function closePipelineModal() {
  showPipelineModal.value = false;
}
async function savePipelineModal() {
  if (currentProcessKey.value) {
    // Quando backend ativo, não renomeamos a key (a API ainda não suporta). Apenas atualiza nome.
    if (!isApiEnabled()) {
      const base = sanitizeProcessKey(
        processName.value || currentProcessKey.value
      );
      const ensureUnique = (k) => {
        const existing = new Set(listProcesses().map((p) => p.key));
        if (!existing.has(k) || k === currentProcessKey.value) return k;
        let i = 2;
        while (existing.has(`${k}-${i}`)) i++;
        return `${k}-${i}`;
      };
      const desiredKey = ensureUnique(base);
      if (desiredKey !== currentProcessKey.value) {
        const res = renameProcessKey(currentProcessKey.value, desiredKey);
        if (!res.ok) return;
        currentProcessKey.value = desiredKey;
        setLastKey(desiredKey);
      }
    }
    const newName = processName.value || currentProcessKey.value;
    const sanitizedAllowed = sanitizeUserSelection(selectedProcessUsers.value);
    const okSettings = await setProcessName(currentProcessKey.value, {
      name: newName,
      allowedUserIds: sanitizedAllowed,
    });
    if (okSettings) {
      // Atualiza lista local sem novo GET
      processes.value = processes.value.map((p) =>
        p.key === currentProcessKey.value
          ? { ...p, name: newName, allowedUserIds: sanitizedAllowed }
          : p
      );
    }
    try {
      // snapshot antes de salvar para migrar IDs de campos
      const prevStages = (pipelineStages.value || []).map((s) => ({
        id: s.id,
        title: s.title,
      }));
      const saved = await saveStagesPreservingIdsApi(
        currentProcessKey.value,
        prevStages,
        pipelineStages.value
      );
      if (Array.isArray(saved) && saved.length) {
        // Atualiza local com IDs reais do backend
        const asClient = saved.map((s) => ({
          id: s.id,
          title: s.title,
          slaDays: s.slaDays,
          color: s.color,
        }));
        pipelineStages.value = asClient;

        // Migra mapa de forms local dos IDs antigos para os novos e persiste no backend
        try {
          const storageKey = `pipeline_stage_forms__${currentProcessKey.value}`;
          const raw = localStorage.getItem(storageKey);
          const map = raw ? JSON.parse(raw) || {} : {};
          const nextMap = { ...map };
          for (let i = 0; i < asClient.length; i++) {
            const oldId = prevStages[i]?.id;
            const newId = asClient[i]?.id;
            if (!oldId || !newId || oldId === newId) continue;
            if (map[oldId]) {
              nextMap[newId] = map[oldId];
              delete nextMap[oldId];
              // também envia para o backend, preservando ordem
              if (isApiEnabled() && /^[a-fA-F0-9]{24}$/.test(newId)) {
                const ordered = (nextMap[newId] || []).map((f, idx) => ({
                  ...f,
                  order: idx,
                }));
                try {
                  await saveStageFieldsApi(newId, ordered);
                } catch (e) {}
              }
            }
          }
          localStorage.setItem(storageKey, JSON.stringify(nextMap));
        } catch (_) {}
      }
      if (okSettings) toastSuccess("Esteira salva");
      else toastInfo("Etapas salvas; falha ao atualizar dados do processo");
      showPipelineModal.value = false;
    } catch (e) {
      toastError("Falha ao salvar etapas. Verifique a conexão.");
      // mantém o modal aberto para o usuário tentar novamente
      return;
    }
  } else {
    showPipelineModal.value = false;
  }
}

// Load/save pipeline config locally
onMounted(async () => {
  await loadUsersList();
  syncCurrentProcessUsers();
  if (!currentProcessKey.value) return;
  const loaded = await fetchStagesApi(currentProcessKey.value);
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : [];
  await prefetchStageFields();
});

watch(currentProcessKey, async (k) => {
  if (!k) {
    pipelineStages.value = [];
    return;
  }
  const loaded = await fetchStagesApi(k);
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : [];
  setLastKey(k);
  const p = processes.value.find((x) => x.key === k);
  processName.value = p?.name || k;
  await prefetchStageFields();
});

// Persistiremos as etapas somente ao salvar no modal

function toggleActive(p) {
  setProcessActive(p.key, !(p.active !== false));
  processes.value = listProcesses();
}

async function editProcess(p) {
  currentProcessKey.value = p.key;
  const loaded = await fetchStagesApi(p.key);
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : [];
  if (!pipelineStages.value.length && p.type && DEFAULT_PIPELINES[p.type]) {
    pipelineStages.value = [...DEFAULT_PIPELINES[p.type]];
  }
  setLastKey(p.key);
  processName.value = p.name || p.key;
  await prefetchStageFields();
  openPipelineModal();
}

// Busca os fields do backend por etapa (para mostrar contagem e já aquecer o builder)
async function prefetchStageFields() {
  if (!isApiEnabled()) return;
  try {
    const storageKey = `pipeline_stage_forms__${currentProcessKey.value}`;
    const raw = localStorage.getItem(storageKey);
    const map = raw ? JSON.parse(raw) || {} : {};
    const nextMap = { ...map };
    for (const st of pipelineStages.value || []) {
      const sid = st?.id;
      if (!sid || !/^[a-fA-F0-9]{24}$/.test(sid)) continue;
      try {
        const arr = await fetchStageFieldsApi(sid);
        if (Array.isArray(arr)) {
          nextMap[sid] = arr.map((r) => ({
            id: r.id || String(Date.now()),
            label: r.label,
            type: r.type,
            required: !!r.required,
            placeholder: r.placeholder || "",
            options: Array.isArray(r.options) ? r.options : [],
          }));
        }
      } catch (_) {}
    }
    localStorage.setItem(storageKey, JSON.stringify(nextMap));
  } catch (_) {}
}

async function loadUsersList() {
  try {
    usersList.value = await fetchUsersApi();
  } catch (error) {
    console.error("Failed to load users list:", error);
    toastError("Erro ao carregar usuários");
    return;
  }
  syncCurrentProcessUsers();
}

// Delete modal state/handlers
const showDeleteModal = ref(false);
const deleteTarget = ref(null);
const deleteConfirm = ref("");
const canConfirmDelete = computed(() => {
  const target = (deleteTarget.value?.name || "").trim().toLowerCase();
  const typed = deleteConfirm.value.trim().toLowerCase();
  return !!target && typed === target;
});
const showResetModal = ref(false);
function openDeleteModal(p) {
  deleteTarget.value = p;
  deleteConfirm.value = "";
  showDeleteModal.value = true;
}
function cancelDelete() {
  showDeleteModal.value = false;
  deleteTarget.value = null;
  deleteConfirm.value = "";
}
async function confirmDelete() {
  const p = deleteTarget.value;
  if (!p || !canConfirmDelete.value) return;

  // Remoção otimista no DOM
  const key = p.key;
  processes.value = processes.value.filter((x) => x.key !== key);
  if (currentProcessKey.value === key) {
    currentProcessKey.value = processes.value[0]?.key || "";
    pipelineStages.value = [];
    if (currentProcessKey.value) setLastKey(currentProcessKey.value);
  }

  // Efetiva no backend e re-sincroniza silenciosamente
  const ok = await removeProcess(key);
  if (ok) toastSuccess("Processo excluído");
  else {
    // rollback
    processes.value = [...processes.value, p];
    toastError("Falha ao excluir. Tente novamente.");
  }
  // Não recarrega toda a lista para evitar GET extra; o evento já atualizará quem escuta

  cancelDelete();
}

function onDeleteConfirmKeydown(e) {
  // Block paste shortcuts (Cmd/Ctrl+V) and select-all paste combos
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "v") {
    e.preventDefault();
    return;
  }
}

function openResetModal() {
  showResetModal.value = true;
}
function cancelReset() {
  showResetModal.value = false;
}
function confirmReset() {
  // Remove todos os processos e dados locais relacionados
  try {
    const keys = processes.value.map((p) => p.key);
    keys.forEach((k) => removeProcess(k));
    // também limpa o registro explicitamente
    localStorage.removeItem("pipeline_processes");
  } catch (_) {}
  processes.value = [];
  currentProcessKey.value = "";
  pipelineStages.value = [];
  setLastKey("");
  showResetModal.value = false;
}

// Initialize
if (currentProcessKey.value) {
  setLastKey(currentProcessKey.value);
}
</script>
