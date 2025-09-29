<template>
  <BaseModal
    v-model="isOpen"
    title="Controle de Acesso"
    size="md"
    @update:modelValue="handleClose"
  >
    <div class="space-y-4">
      <!-- Info do card -->
      <div class="text-center border-b border-slate-700/60 pb-4">
        <div class="text-sm font-medium text-slate-200">{{ proposal?.name }}</div>
        <div class="text-xs text-slate-400 mt-1">
          Editando permissões de acesso
        </div>
      </div>

      <!-- Controle de Acesso -->
      <div class="space-y-3">
        <div class="text-left">
          <label class="block text-xs text-slate-300 uppercase tracking-wide mb-2">
            Tipo de Acesso
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
                  v-for="user in availableUsersForSelection"
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
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full bg-slate-700 flex items-center justify-center text-xs text-slate-300">
                    {{ getUserInitials(user.name) }}
                  </div>
                  <span class="text-sm text-slate-200">{{ user.name }}</span>
                  <span v-if="user.id === proposal?.createdBy" class="text-xs text-indigo-400 bg-indigo-600/20 px-1.5 py-0.5 rounded">
                    Criador
                  </span>
                </div>
                <button
                  v-if="user.id !== proposal?.createdBy"
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
      <div class="flex items-center justify-end gap-3 pt-2 border-t border-slate-700/60">
        <button
          type="button"
          class="px-4 py-2 text-sm text-slate-300 hover:text-white transition-colors"
          @click="handleClose"
        >
          Cancelar
        </button>
        <button
          type="button"
          class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white text-sm font-medium rounded-md transition-colors"
          @click="handleSave"
          :disabled="saving"
        >
          <i v-if="saving" class="fa-solid fa-spinner fa-spin mr-2"></i>
          {{ saving ? 'Salvando...' : 'Salvar Alterações' }}
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
  proposal: { type: Object, default: null },
  availableUsers: { type: Array, default: () => [] },
});

const emit = defineEmits(["update:modelValue", "save", "close"]);

// Estado do modal
const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const saving = ref(false);

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

// Computeds
const selectedUsersDisplay = computed(() => {
  return selectedUsers.value.map(userId => {
    const user = props.availableUsers.find(u => u.id === userId);
    return user || { id: userId, name: 'Usuário não encontrado', email: '' };
  });
});

const availableUsersForSelection = computed(() => {
  return props.availableUsers.filter(user =>
    !selectedUsers.value.includes(user.id)
  );
});

// Utilitários
function getUserInitials(name) {
  if (!name) return "U";
  const parts = name.trim().split(/\s+/);
  const initials = parts
    .slice(0, 2)
    .map(p => p[0]?.toUpperCase())
    .join("");
  return initials || "U";
}

// Watchers para popular dados quando o modal abre
watch([isOpen, () => props.proposal], ([modalOpen, proposal]) => {
  if (modalOpen && proposal) {
    // Carregar dados existentes da proposta
    accessType.value = proposal.isPublic ? "public" : "restricted";
    selectedUsers.value = [...(proposal.visibleToUsers || [])];

    // Sempre incluir o criador na lista se for restrito
    if (!proposal.isPublic && proposal.createdBy && !selectedUsers.value.includes(proposal.createdBy)) {
      selectedUsers.value.unshift(proposal.createdBy);
    }

    externalContacts.value = [...(proposal.externalNotifications || [])];

    // Limpar formulário de contato externo
    externalContact.value = {
      name: "",
      email: "",
      phone: "",
      emailEnabled: true,
      whatsappEnabled: false,
    };
    selectedUser.value = "";
  }
}, { immediate: true });

// Funções para gerenciar usuários
function addUser() {
  if (!selectedUser.value || selectedUsers.value.includes(selectedUser.value)) return;
  selectedUsers.value.push(selectedUser.value);
  selectedUser.value = "";
}

function removeUser(userId) {
  // Não permitir remover o criador
  if (userId === props.proposal?.createdBy) return;

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

async function handleSave() {
  if (!props.proposal) return;

  saving.value = true;

  try {
    // Preparar dados de acesso atualizados
    const accessData = {
      isPublic: accessType.value === "public",
      visibleToUsers: accessType.value === "restricted" ? [...selectedUsers.value] : [],
      externalNotifications: [...externalContacts.value],
    };

    emit("save", {
      proposalId: props.proposal.id,
      accessData
    });

    handleClose();
  } catch (error) {
    console.error("Erro ao salvar alterações de acesso:", error);
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
/* Melhorar aparência dos inputs de radio */
input[type="radio"] {
  appearance: none;
  border: 2px solid #4b5563;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  background-color: #1f2937;
  cursor: pointer;
  transition: all 0.2s;
}

input[type="radio"]:checked {
  background-color: #4f46e5;
  border-color: #4f46e5;
  box-shadow: inset 0 0 0 3px #1f2937;
}

input[type="radio"]:focus {
  outline: none;
  ring: 2px solid #4f46e5;
}
</style>