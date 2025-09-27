<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
    <header class="app-header relative backdrop-blur-2xl border-b border-slate-800/70 p-4">
      <div class="absolute inset-0 bg-white/5 blur-3xl opacity-10"></div>
      <div class="relative flex items-center justify-between">
        <div class="space-y-1">
          <h1 class="app-header-title">Cadastro de Leads</h1>
          <p class="app-header-subtitle">
            Registre, qualifique e acompanhe leads para alimentar seus processos internos.
          </p>
        </div>
        <button
          class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium shadow-lg shadow-indigo-500/30 transition-colors"
          @click="openLeadModal()"
        >
          <i class="fa-solid fa-user-plus"></i>
          Novo Lead
        </button>
      </div>
    </header>

    <main class="p-6">
      <div class="bg-slate-900/60 border border-slate-800/60 rounded-2xl shadow-2xl backdrop-blur-xl overflow-hidden">
        <div v-if="leads.length === 0" class="p-10 text-center text-slate-300">
          <i class="fa-solid fa-address-card text-4xl text-indigo-400 mb-4"></i>
          <p class="text-lg font-semibold">Nenhum lead cadastrado ainda</p>
          <p class="text-sm text-slate-400">
            Clique em “Novo Lead” para iniciar o relacionamento com seus clientes e analistas.
          </p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead class="bg-slate-900/70 text-slate-400 uppercase text-xs tracking-wide">
              <tr>
                <th class="px-4 py-3 text-left">Nome</th>
                <th class="px-4 py-3 text-left">Email</th>
                <th class="px-4 py-3 text-left">Telefone</th>
                <th class="px-4 py-3 text-left">Origem</th>
                <th class="px-4 py-3 text-left">Qualificação</th>
                <th class="px-4 py-3 text-left">Criado em</th>
                <th class="px-4 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/70 text-slate-200">
              <tr
                v-for="lead in leads"
                :key="lead.id"
                class="hover:bg-slate-900/40 transition-colors"
              >
                <td class="px-4 py-3 font-medium">{{ lead.name }}</td>
                <td class="px-4 py-3">{{ lead.email }}</td>
                <td class="px-4 py-3 text-slate-300">{{ lead.phone || '-' }}</td>
                <td class="px-4 py-3 text-slate-300">{{ originLabel(lead.origin) }}</td>
                <td class="px-4 py-3">
                  <span
                    :class="lead.type === 'ANALYST_CANDIDATE' ? 'bg-amber-500/20 text-amber-300' : 'bg-emerald-500/20 text-emerald-300'"
                    class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
                  >
                    {{ typeLabel(lead.type) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-xs text-slate-400">
                  {{ formatDate(lead.createdAt) }}
                </td>
                <td class="px-4 py-3">
                  <div class="flex items-center justify-end gap-2">
                    <button
                      class="p-2 rounded-lg bg-slate-800/80 text-slate-300 hover:text-white hover:bg-slate-700"
                      @click="openLeadModal(lead)"
                      title="Editar"
                    >
                      <i class="fa-solid fa-pen"></i>
                    </button>
                    <button
                      class="p-2 rounded-lg bg-red-900/40 text-red-300 hover:text-white hover:bg-red-600/60"
                      @click="confirmDelete(lead)"
                      title="Excluir"
                    >
                      <i class="fa-solid fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <BaseModal
      v-model="showLeadModal"
      :title="editingLead ? 'Editar Lead' : 'Novo Lead'"
      size="md"
      :z-index="90"
    >
      <div class="space-y-4">
        <div>
          <label class="block text-xs text-slate-300 uppercase tracking-wide">Nome *</label>
          <input
            v-model="leadForm.name"
            type="text"
            class="mt-1 w-full rounded-xl bg-slate-900/80 border border-slate-700/60 text-slate-100 px-3 py-2 focus:border-indigo-500 focus:outline-none"
            placeholder="Nome do lead"
          />
        </div>
        <div>
          <label class="block text-xs text-slate-300 uppercase tracking-wide">Email *</label>
          <input
            v-model="leadForm.email"
            type="email"
            class="mt-1 w-full rounded-xl bg-slate-900/80 border border-slate-700/60 text-slate-100 px-3 py-2 focus:border-indigo-500 focus:outline-none"
            placeholder="email@exemplo.com"
          />
        </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-xs text-slate-300 uppercase tracking-wide">Telefone</label>
          <input
            v-model="leadForm.phone"
            type="text"
            class="mt-1 w-full rounded-xl bg-slate-900/80 border border-slate-700/60 text-slate-100 px-3 py-2 focus:border-indigo-500 focus:outline-none"
            placeholder="(00) 00000-0000"
          />
        </div>
        <div>
          <label class="block text-xs text-slate-300 uppercase tracking-wide">Origem *</label>
          <select
            v-model="leadForm.origin"
            class="mt-1 w-full rounded-xl bg-slate-900/80 border border-slate-700/60 text-slate-100 px-3 py-2 focus:border-indigo-500 focus:outline-none"
          >
            <option
              v-for="origin in originOptions"
              :key="origin.value"
              :value="origin.value"
            >
              {{ origin.label }}
            </option>
          </select>
        </div>
      </div>
        <div>
          <label class="block text-xs text-slate-300 uppercase tracking-wide">Qualificação *</label>
          <div class="mt-2 grid grid-cols-1 md:grid-cols-2 gap-3">
            <button
              v-for="option in typeOptions"
              :key="option.value"
              type="button"
              @click="leadForm.type = option.value"
              :class="[
                'flex items-center gap-2 px-3 py-2 rounded-xl border transition-all',
                leadForm.type === option.value
                  ? 'border-emerald-500 bg-emerald-500/10 text-emerald-200'
                  : 'border-slate-700/60 bg-slate-900/60 text-slate-300 hover:border-emerald-500/60'
              ]"
            >
              <i :class="option.icon"></i>
              <div class="text-left">
                <p class="text-sm font-medium">{{ option.label }}</p>
                <p class="text-[11px] text-slate-400">{{ option.description }}</p>
              </div>
            </button>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-2">
          <button
            class="px-4 py-2 rounded-xl bg-slate-800/80 text-slate-200 hover:bg-slate-700 text-sm"
            @click="closeModal"
          >
            Cancelar
          </button>
          <button
            class="px-4 py-2 rounded-xl bg-indigo-600 text-white hover:bg-indigo-500 text-sm shadow-lg shadow-indigo-500/30"
            @click="saveLead"
          >
            {{ editingLead ? 'Atualizar Lead' : 'Salvar Lead' }}
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import { useToast } from '~/composables/useToast';
import {
  fetchLeadsApi,
  createLeadApi,
  updateLeadApi,
  deleteLeadApi,
} from '~/composables/useLeadsApi';

definePageMeta({ layout: 'sidebar' });

const leads = ref([]);
const showLeadModal = ref(false);
const editingLead = ref(false);
const leadForm = ref({
  id: null,
  name: '',
  email: '',
  phone: '',
  origin: 'INDICACAO',
  type: 'CLIENT',
});

const { success: toastSuccess, error: toastError } = useToast();

const typeOptions = [
  {
    value: 'CLIENT',
    label: 'Cliente',
    description: 'Leads interessados nos serviços da FourCon.',
    icon: 'fa-solid fa-user-check text-emerald-400',
  },
  {
    value: 'ANALYST_CANDIDATE',
    label: 'Possível Analista',
    description: 'Profissionais para composição dos times de gerentes.',
    icon: 'fa-solid fa-people-arrows text-amber-400',
  },
];

const originOptions = [
  { value: 'INDICACAO', label: 'Indicação' },
  { value: 'EVENTO', label: 'Evento' },
  { value: 'SITE', label: 'Site / Landing Page' },
  { value: 'ANUNCIO', label: 'Anúncio Pago' },
  { value: 'SOCIAL_MEDIA', label: 'Redes Sociais' },
  { value: 'PARCERIA', label: 'Parceria' },
  { value: 'COLD_CALL', label: 'Cold Call' },
  { value: 'EMAIL_MARKETING', label: 'Email Marketing' },
  { value: 'RETORNO_CLIENTE', label: 'Retorno de Cliente' },
  { value: 'OUTROS', label: 'Outros' },
];

async function loadLeads() {
  try {
    leads.value = await fetchLeadsApi();
  } catch (error) {
    console.error('Erro ao carregar leads:', error);
    toastError('Não foi possível carregar os leads');
  }
}

function openLeadModal(lead = null) {
  if (lead) {
    leadForm.value = {
      id: lead.id,
      name: lead.name,
      email: lead.email,
      phone: lead.phone || '',
      origin: lead.origin || 'INDICACAO',
      type: lead.type || 'CLIENT',
    };
    editingLead.value = true;
  } else {
    leadForm.value = {
      id: null,
      name: '',
      email: '',
      phone: '',
      origin: 'INDICACAO',
      type: 'CLIENT',
    };
    editingLead.value = false;
  }
  showLeadModal.value = true;
}

function closeModal() {
  showLeadModal.value = false;
}

async function saveLead() {
  try {
    if (!leadForm.value.name.trim()) {
      toastError('Nome é obrigatório');
      return;
    }
    if (!leadForm.value.email.trim()) {
      toastError('Email é obrigatório');
      return;
    }

    const payload = {
      name: leadForm.value.name.trim(),
      email: leadForm.value.email.trim(),
      phone: leadForm.value.phone?.trim() || undefined,
      origin: leadForm.value.origin,
      type: leadForm.value.type,
    };

    if (editingLead.value && leadForm.value.id) {
      await updateLeadApi(leadForm.value.id, payload);
      toastSuccess('Lead atualizado com sucesso');
    } else {
      await createLeadApi(payload);
      toastSuccess('Lead cadastrado com sucesso');
    }

    showLeadModal.value = false;
    await loadLeads();
  } catch (error) {
    console.error('Erro ao salvar lead:', error);
    toastError(error?.data?.error || 'Não foi possível salvar o lead');
  }
}

async function confirmDelete(lead) {
  if (!confirm(`Deseja excluir o lead ${lead.name}?`)) {
    return;
  }
  try {
    await deleteLeadApi(lead.id);
    toastSuccess('Lead removido');
    await loadLeads();
  } catch (error) {
    console.error('Erro ao excluir lead:', error);
    toastError('Não foi possível excluir o lead');
  }
}

function formatDate(value) {
  if (!value) return '-';
  try {
    const date = new Date(value);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  } catch {
    return value;
  }
}

function typeLabel(type) {
  return type === 'ANALYST_CANDIDATE' ? 'Possível Analista' : 'Cliente';
}

function originLabel(origin) {
  if (!origin) return '-';
  const found = originOptions.find((opt) => opt.value === origin);
  return found ? found.label : origin;
}

onMounted(() => {
  loadLeads();
});
</script>
