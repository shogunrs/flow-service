<template>
  <BaseModal
    v-model="isOpen"
    title="Novo Registro"
    size="md"
    @update:modelValue="handleClose"
  >
    <div class="space-y-4">
      <!-- Info da primeira etapa -->
      <div class="text-center">
        <div class="text-xs text-slate-400 mb-2">
          <span class="inline-flex items-center gap-2">
            <span
              class="w-2 h-4 rounded-full"
              :class="getStageColor(firstStage?.id)"
            ></span>
            Será criado na etapa: {{ firstStage?.title || "Primeira Etapa" }}
          </span>
        </div>
        <p class="text-sm text-slate-300">
          Digite apenas o nome para criar o registro. Os demais campos serão
          preenchidos clicando no card da etapa.
        </p>
      </div>

      <!-- Campo Nome -->
      <div class="space-y-3">
        <div v-if="hasLeadOptions" class="text-left">
          <label class="block text-xs text-slate-300 uppercase tracking-wide mb-1">
            Selecionar lead cadastrado (opcional)
          </label>
          <select
            v-model="selectedLeadId"
            class="w-full rounded-xl bg-slate-900/80 border border-slate-700/60 text-slate-100 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          >
            <option value="">Digitar manualmente</option>
            <option
              v-for="lead in selectableLeadOptions"
              :key="lead.id"
              :value="lead.id"
            >
              {{ lead.label }}
            </option>
          </select>
        </div>

        <BaseInput
          v-model="recordName"
          label="Nome do Registro"
          placeholder="Digite o nome..."
          :required="true"
          size="md"
          :error-message="nameError"
          @keyup.enter="handleSave"
        />

        <BaseInput
          v-if="isFinancialProcess"
          v-model="recordAmount"
          type="text"
          mask="currency"
          locale="pt-BR"
          label="Valor do Processo (R$)"
          size="md"
          placeholder="0,00"
        />
      </div>

      <!-- Controle de Acesso -->
      <div v-if="showAccessControl" class="space-y-3 border-t border-slate-700/60 pt-4">
        <div class="text-left">
          <label class="block text-xs text-slate-300 uppercase tracking-wide mb-2">
            Controle de Acesso
          </label>

          <!-- Opção Público -->
          <div class="space-y-2">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                v-model="accessType"
                type="radio"
                value="public"
                class="w-4 h-4 text-indigo-600 bg-slate-900 border-slate-600 focus:ring-indigo-500"
              />
              <span class="text-sm text-slate-200">Público (todos da esteira podem ver)</span>
            </label>

            <label class="flex items-center gap-2 cursor-pointer">
              <input
                v-model="accessType"
                type="radio"
                value="restricted"
                class="w-4 h-4 text-indigo-600 bg-slate-900 border-slate-600 focus:ring-indigo-500"
              />
              <span class="text-sm text-slate-200">Restrito (apenas usuários selecionados)</span>
            </label>
          </div>

          <!-- Seleção de Usuários -->
          <div v-if="accessType === 'restricted'" class="mt-3 space-y-3">
            <div>
              <label class="block text-xs text-slate-400 mb-1">Usuários do Sistema</label>
              <select
                v-model="selectedUser"
                class="w-full rounded-lg bg-slate-900/80 border border-slate-700/60 text-slate-100 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
                @change="addUser"
              >
                <option value="">Selecionar usuário...</option>
                <option
                  v-for="user in availableUsers"
                  :key="user.id"
                  :value="user.id"
                >
                  {{ user.name }} ({{ user.email }})
                </option>
              </select>
            </div>

            <!-- Lista de usuários selecionados -->
            <div v-if="selectedUsers.length > 0" class="space-y-1">
              <label class="block text-xs text-slate-400">Usuários com acesso:</label>
              <div
                v-for="user in selectedUsersDisplay"
                :key="user.id"
                class="flex items-center justify-between bg-slate-800/50 rounded-lg px-3 py-2"
              >
                <span class="text-sm text-slate-200">{{ user.name }}</span>
                <button
                  @click="removeUser(user.id)"
                  class="text-slate-400 hover:text-red-400 transition-colors"
                >
                  <i class="fa-solid fa-times text-xs"></i>
                </button>
              </div>
            </div>

            <!-- Notificações Externas -->
            <div class="border-t border-slate-700/40 pt-3">
              <label class="block text-xs text-slate-400 mb-2">Notificações Externas</label>
              <div class="grid grid-cols-1 gap-2">
                <BaseInput
                  v-model="externalContact.name"
                  label="Nome"
                  size="sm"
                  placeholder="Nome completo"
                />
                <BaseInput
                  v-model="externalContact.email"
                  label="Email"
                  type="email"
                  size="sm"
                  placeholder="email@exemplo.com"
                />
                <BaseInput
                  v-model="externalContact.phone"
                  label="Telefone (opcional)"
                  size="sm"
                  placeholder="(11) 99999-9999"
                />
                <div class="flex items-center gap-4">
                  <label class="flex items-center gap-1 cursor-pointer">
                    <input
                      v-model="externalContact.emailEnabled"
                      type="checkbox"
                      class="w-3 h-3 text-indigo-600 bg-slate-900 border-slate-600 rounded focus:ring-indigo-500"
                    />
                    <span class="text-xs text-slate-300">Email</span>
                  </label>
                  <label class="flex items-center gap-1 cursor-pointer">
                    <input
                      v-model="externalContact.whatsappEnabled"
                      type="checkbox"
                      class="w-3 h-3 text-indigo-600 bg-slate-900 border-slate-600 rounded focus:ring-indigo-500"
                    />
                    <span class="text-xs text-slate-300">WhatsApp</span>
                  </label>
                  <button
                    @click="addExternalContact"
                    :disabled="!externalContact.name || !externalContact.email"
                    class="ml-auto px-2 py-1 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white text-xs rounded transition-colors"
                  >
                    Adicionar
                  </button>
                </div>
              </div>

              <!-- Lista de contatos externos -->
              <div v-if="externalContacts.length > 0" class="mt-3 space-y-1">
                <label class="block text-xs text-slate-400">Contatos externos:</label>
                <div
                  v-for="(contact, index) in externalContacts"
                  :key="index"
                  class="flex items-center justify-between bg-slate-800/50 rounded-lg px-3 py-2"
                >
                  <div class="flex-1">
                    <div class="text-sm text-slate-200">{{ contact.name }}</div>
                    <div class="text-xs text-slate-400">{{ contact.email }}</div>
                    <div class="flex items-center gap-2 mt-1">
                      <span v-if="contact.emailEnabled" class="inline-flex items-center gap-1 px-1.5 py-0.5 bg-blue-600/20 text-blue-300 rounded text-xs">
                        <i class="fa-solid fa-envelope text-[8px]"></i>
                        Email
                      </span>
                      <span v-if="contact.whatsappEnabled" class="inline-flex items-center gap-1 px-1.5 py-0.5 bg-green-600/20 text-green-300 rounded text-xs">
                        <i class="fa-brands fa-whatsapp text-[8px]"></i>
                        WhatsApp
                      </span>
                    </div>
                  </div>
                  <button
                    @click="removeExternalContact(index)"
                    class="text-slate-400 hover:text-red-400 transition-colors"
                  >
                    <i class="fa-solid fa-times text-xs"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Botões de ação -->
      <div class="flex items-center justify-end gap-3 pt-2">
        <button
          type="button"
          class="px-4 py-2 text-sm text-slate-300 hover:text-white transition-colors"
          @click="handleClose"
        >
          Cancelar
        </button>
        <button
          type="button"
          class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white text-sm font-medium rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="!recordName.trim()"
          @click="handleSave"
        >
          Criar Registro
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import BaseInput from "~/components/ui/BaseInput.vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  stages: { type: Array, default: () => [] },
  pipelineKey: { type: String, default: "" },
  isFinancialProcess: { type: Boolean, default: false },
  leadOptions: { type: Array, default: () => [] },
  showAccessControl: { type: Boolean, default: false },
  availableUsers: { type: Array, default: () => [] },
});

const emit = defineEmits(["update:modelValue", "save", "close"]);

// Estado do modal
const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

// Primeira etapa
const firstStage = computed(() => props.stages[0]);

// Campo nome e validação
const recordName = ref("");
const recordAmount = ref(0);
const nameError = ref("");
const amountError = ref("");
const selectedLeadId = ref("");

// Controle de acesso
const accessType = ref("public");
const selectedUsers = ref([]);
const selectedUser = ref("");
const externalContacts = ref([]);
const externalContact = ref({
  name: "",
  email: "",
  phone: "",
  emailEnabled: true,
  whatsappEnabled: false,
});

// Cores das etapas (mesmo mapeamento usado na esteira)
const colorMap = {
  sky: "bg-sky-500",
  indigo: "bg-indigo-500",
  amber: "bg-amber-500",
  rose: "bg-rose-500",
  green: "bg-green-500",
  purple: "bg-purple-500",
  blue: "bg-blue-500",
  orange: "bg-indigo-600",
  teal: "bg-teal-500",
  cyan: "bg-cyan-500",
  lime: "bg-lime-500",
  emerald: "bg-emerald-500",
  fuchsia: "bg-fuchsia-500",
  violet: "bg-violet-500",
  pink: "bg-pink-500",
  red: "bg-red-500",
  yellow: "bg-yellow-500",
  slate: "bg-slate-500",
  stone: "bg-stone-500",
  zinc: "bg-zinc-500",
};

function getStageColor(stageId) {
  const stage = props.stages.find((s) => s.id === stageId);
  return colorMap[stage?.color] || "bg-sky-500";
}

// Limpar campos quando o modal abre
watch(isOpen, (newValue) => {
  if (newValue) {
    recordName.value = "";
    nameError.value = "";
    recordAmount.value = 0;
    amountError.value = "";
    selectedLeadId.value = "";

    // Limpar controle de acesso
    accessType.value = "public";
    selectedUsers.value = [];
    selectedUser.value = "";
    externalContacts.value = [];
    externalContact.value = {
      name: "",
      email: "",
      phone: "",
      emailEnabled: true,
      whatsappEnabled: false,
    };
  }
});

const availableLeads = computed(() => props.leadOptions || []);
const selectableLeadOptions = computed(() =>
  availableLeads.value.map((lead) => ({
    ...lead,
    label: [lead.name, lead.email || lead.phone]
      .filter(Boolean)
      .join(" — ") || lead.name,
  }))
);
const hasLeadOptions = computed(() => selectableLeadOptions.value.length > 0);

watch(selectedLeadId, (leadId) => {
  if (!leadId) return;
  const lead = availableLeads.value.find((item) => item.id === leadId);
  if (lead?.name) {
    recordName.value = lead.name;
  }
});

// Computeds para controle de acesso
const selectedUsersDisplay = computed(() => {
  return selectedUsers.value.map(userId => {
    const user = props.availableUsers.find(u => u.id === userId);
    return user || { id: userId, name: 'Usuário não encontrado' };
  });
});

// Funções para gerenciar usuários
function addUser() {
  if (!selectedUser.value || selectedUsers.value.includes(selectedUser.value)) return;
  selectedUsers.value.push(selectedUser.value);
  selectedUser.value = "";
}

function removeUser(userId) {
  const index = selectedUsers.value.indexOf(userId);
  if (index > -1) {
    selectedUsers.value.splice(index, 1);
  }
}

// Funções para gerenciar contatos externos
function addExternalContact() {
  if (!externalContact.value.name || !externalContact.value.email) return;

  externalContacts.value.push({ ...externalContact.value });
  externalContact.value = {
    name: "",
    email: "",
    phone: "",
    emailEnabled: true,
    whatsappEnabled: false,
  };
}

function removeExternalContact(index) {
  externalContacts.value.splice(index, 1);
}

// Ações do modal
function handleClose() {
  emit("close");
  emit("update:modelValue", false);
}

function handleSave() {
  // Validar nome
  if (!recordName.value.trim()) {
    nameError.value = "Nome é obrigatório";
    return;
  }

  nameError.value = "";

  // Preparar dados simples para salvar
  const recordData = {
    name: recordName.value.trim(),
    amount: props.isFinancialProcess ? recordAmount.value || 0 : 0,
    stageId: firstStage.value?.id,
    status:
      firstStage.value?.defaultStatus || firstStage.value?.status || "Pendente",
    leadId: selectedLeadId.value || null,
    fieldValues: {},
    fieldFiles: {},
    // Dados de controle de acesso
    isPublic: accessType.value === "public",
    visibleToUsers: accessType.value === "restricted" ? [...selectedUsers.value] : [],
    externalNotifications: [...externalContacts.value],
  };

  emit("save", recordData);
  handleClose();
}
</script>

<style scoped>
.new-record-modal {
  @apply max-h-[80vh] overflow-y-auto;
}
</style>
