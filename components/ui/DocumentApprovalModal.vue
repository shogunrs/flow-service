<template>
  <BaseModal
    :model-value="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
    :title="title"
    size="xl"
    :z-index="zIndex"
    :close-on-backdrop="!loading"
    body-class="max-h-[70vh]"
  >
    <div v-if="loading" class="flex items-center justify-center py-16 text-slate-300">
      <i class="fa-solid fa-spinner fa-spin text-2xl"></i>
      <span class="ml-3 text-sm">Carregando documentos...</span>
    </div>

    <div v-else class="grid gap-4 md:grid-cols-[220px_minmax(0,1fr)]">
      <aside class="space-y-2">
        <p class="text-[11px] uppercase tracking-wide text-slate-400">
          Documentos para {{ contextLabel || 'revisão' }}
        </p>
        <div
          v-for="doc in documents"
          :key="doc.fileType"
          class="rounded-xl border border-slate-700/60 bg-slate-900/60 hover:bg-slate-900/80 transition cursor-pointer"
          :class="selected?.fileType === doc.fileType ? 'ring-2 ring-indigo-500/70 border-indigo-500/60' : ''"
          @click="selectDocument(doc)"
        >
          <div class="px-3 py-3 space-y-2">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-slate-200">{{ resolveLabel(doc.fileType) }}</span>
              <span
                class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-semibold"
                :class="statusClass(doc.status)"
              >
                <i
                  class="fa-solid"
                  :class="{
                    'fa-clock': doc.status === 'PENDING',
                    'fa-check': doc.status === 'APPROVED',
                    'fa-xmark': doc.status === 'REJECTED'
                  }"
                ></i>
                {{ statusLabel(doc.status) }}
              </span>
            </div>
            <p class="text-[11px] text-slate-400 truncate">{{ doc.filename || 'Sem nome' }}</p>
          </div>
        </div>

        <div v-if="!documents.length" class="rounded-xl border border-dashed border-slate-600/60 bg-slate-900/40 px-3 py-10 text-center text-xs text-slate-500">
          Nenhum documento disponível para revisão.
        </div>
      </aside>

      <section class="space-y-4" v-if="selected">
        <header class="flex items-center justify-between gap-3">
          <div>
            <h4 class="text-sm font-semibold text-slate-100">{{ resolveLabel(selected.fileType) }}</h4>
            <p class="text-xs text-slate-400">{{ selected.filename || 'Sem nome' }}</p>
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md border border-slate-600/60 px-3 py-2 text-xs font-semibold text-slate-200 hover:bg-slate-700/40"
            @click="openInNewTab"
          >
            <i class="fa-solid fa-up-right-from-square"></i>
            Abrir documento
          </button>
        </header>

        <div class="min-h-[220px] rounded-xl border border-slate-700/60 bg-slate-950/60 flex items-center justify-center overflow-hidden relative">
          <div v-if="previewLoading" class="absolute inset-0 flex items-center justify-center bg-slate-950/80">
            <i class="fa-solid fa-spinner fa-spin text-indigo-400 text-xl"></i>
          </div>

          <template v-if="previewUrl && isImage(selected.contentType)">
            <img :src="previewUrl" :alt="selected.filename" class="max-h-[320px] object-contain" />
          </template>
          <template v-else>
            <div class="text-center px-6 py-10 text-xs text-slate-400">
              <i class="fa-solid fa-file-lines text-lg text-slate-500 mb-2"></i>
              <p>Pré-visualização indisponível para este tipo de arquivo.</p>
              <p class="mt-1 text-[11px]">Clique em “Abrir documento” para visualizar.</p>
            </div>
          </template>
        </div>

        <div class="grid gap-3 md:grid-cols-2">
          <div class="rounded-lg border border-slate-700/60 bg-slate-900/70 p-3 text-xs space-y-1 text-slate-300">
            <p><span class="text-slate-400">Enviado:</span> {{ formatDate(selected.uploadedAt) }}</p>
            <p><span class="text-slate-400">Tamanho:</span> {{ formatSize(selected.fileSize) }}</p>
            <p><span class="text-slate-400">Tipo:</span> {{ selected.contentType || 'desconhecido' }}</p>
          </div>
          <div class="rounded-lg border border-slate-700/60 bg-slate-900/70 p-3 text-xs space-y-1 text-slate-300">
            <p><span class="text-slate-400">Status:</span> {{ statusLabel(selected.status) }}</p>
            <p v-if="selected.reviewerName"><span class="text-slate-400">Revisor:</span> {{ selected.reviewerName }}</p>
            <p v-if="selected.reviewedAt"><span class="text-slate-400">Revisado em:</span> {{ formatDate(selected.reviewedAt) }}</p>
            <p v-if="selected.reviewNotes"><span class="text-slate-400">Notas:</span> {{ selected.reviewNotes }}</p>
            <p v-else class="text-slate-500 italic">Sem notas anteriores.</p>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-[11px] uppercase tracking-wide text-slate-400">Observações adicionais</label>
          <textarea
            v-model="notes"
            class="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
            rows="3"
            placeholder="Adicione observações para o histórico de revisão"
          ></textarea>
        </div>

        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md bg-emerald-500/15 px-4 py-2 text-sm font-semibold text-emerald-200 hover:bg-emerald-500/25 transition disabled:opacity-60"
            :disabled="actionLoading"
            @click="submitDecision('APPROVED')"
          >
            <i class="fa-solid fa-check"></i>
            Aprovar
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md bg-rose-500/15 px-4 py-2 text-sm font-semibold text-rose-200 hover:bg-rose-500/25 transition disabled:opacity-60"
            :disabled="actionLoading"
            @click="submitDecision('REJECTED')"
          >
            <i class="fa-solid fa-xmark"></i>
            Reprovar
          </button>
          <button
            type="button"
            class="ml-auto inline-flex items-center gap-2 rounded-md border border-slate-600/60 px-4 py-2 text-sm font-semibold text-slate-200 hover:bg-slate-700/40"
            @click="emit('update:modelValue', false)"
          >
            <i class="fa-solid fa-circle-xmark"></i>
            Fechar
          </button>
        </div>
      </section>

      <section v-else class="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-700 bg-slate-900/40 p-6 text-center text-sm text-slate-400">
        <i class="fa-solid fa-circle-info text-lg mb-3 text-slate-500"></i>
        <p>Selecione um documento na lista ao lado para visualizar detalhes e aprovar/reprovar.</p>
      </section>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import { useDocumentApproval, type DocumentApprovalFile } from '~/composables/useDocumentApproval';
import { useCurrentUser } from '~/composables/useCurrentUser';
import { useToast } from '~/composables/useToast';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  entityType: { type: String, default: 'USER' },
  entityId: { type: String, required: true },
  contextLabel: { type: String, default: '' },
  title: { type: String, default: 'Revisão de documentos' },
  zIndex: { type: Number, default: 2100 },
});

const emit = defineEmits(['update:modelValue', 'decision']);

const { listDocumentFiles, decideDocument, getDownloadUrl, loading } = useDocumentApproval();
const { user: currentUser } = useCurrentUser();
const { error: toastError } = useToast();

const documents = ref<DocumentApprovalFile[]>([]);
const selected = ref<DocumentApprovalFile | null>(null);
const previewUrl = ref<string | null>(null);
const previewLoading = ref(false);
const actionLoading = ref(false);
const notes = ref('');

const documentLabels: Record<string, string> = {
  DOCUMENT: 'Documento de identidade',
  IDENTITY: 'Documento de identidade',
  ADDRESS_PROOF: 'Comprovante de endereço',
  CARTAO_CNPJ: 'Cartão CNPJ',
  CONTRATO_SOCIAL: 'Contrato social',
  QUALIFICACAO_SOCIOS: 'Qualificação dos sócios',
  PROFILE_PHOTO: 'Foto do usuário',
};

const isImage = (type?: string) => {
  if (!type) return false;
  return type.startsWith('image/');
};

const statusLabel = (status?: string) => {
  switch (status) {
    case 'APPROVED':
      return 'Aprovado';
    case 'REJECTED':
      return 'Rejeitado';
    default:
      return 'Pendente';
  }
};

const statusClass = (status?: string) => {
  switch (status) {
    case 'APPROVED':
      return 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30';
    case 'REJECTED':
      return 'bg-rose-500/15 text-rose-300 border border-rose-500/30';
    default:
      return 'bg-amber-500/15 text-amber-300 border border-amber-500/30';
  }
};

const resolveLabel = (fileType: string) => {
  const key = fileType?.toUpperCase() || '';
  return documentLabels[key] || key;
};

const fetchDocuments = async () => {
  if (!props.entityId || !props.modelValue) return;
  const files = await listDocumentFiles(props.entityType, props.entityId);
  documents.value = files;
  if (files.length) {
    selected.value = files[0];
    notes.value = files[0]?.reviewNotes || '';
    loadPreview(files[0]);
  } else {
    selected.value = null;
    notes.value = '';
    previewUrl.value = null;
  }
};

const loadPreview = async (doc: DocumentApprovalFile | null) => {
  if (!doc) {
    previewUrl.value = null;
    return;
  }
  if (!isImage(doc.contentType)) {
    previewUrl.value = null;
    return;
  }
  try {
    previewLoading.value = true;
    const url = await getDownloadUrl(props.entityType, props.entityId, doc.fileType);
    previewUrl.value = url;
  } finally {
    previewLoading.value = false;
  }
};

const selectDocument = (doc: DocumentApprovalFile) => {
  selected.value = doc;
  notes.value = doc.reviewNotes || '';
  previewUrl.value = null;
  loadPreview(doc);
};

const openInNewTab = async () => {
  if (!selected.value) return;
  const url = await getDownloadUrl(props.entityType, props.entityId, selected.value.fileType);
  if (url) {
    window.open(url, '_blank', 'noopener');
  }
};

const submitDecision = async (status: 'APPROVED' | 'REJECTED') => {
  if (!selected.value) return;
  if (actionLoading.value) return;

  if (status === 'REJECTED' && !notes.value.trim()) {
    notes.value = '';
    toastError('Informe o motivo da reprovação para registrar o histórico.');
    return;
  }

  try {
    actionLoading.value = true;
    await decideDocument(props.entityType, props.entityId, selected.value.fileType, status, {
      reviewerId: currentUser.value?.id,
      reviewerName: currentUser.value?.name,
      reviewNotes: notes.value?.trim() || undefined,
    });
    emit('decision', { fileType: selected.value.fileType, status });
    await fetchDocuments();
  } finally {
    actionLoading.value = false;
  }
};

const formatDate = (value?: string) => {
  if (!value) return '—';
  try {
    return new Date(value).toLocaleString();
  } catch {
    return value;
  }
};

const formatSize = (bytes?: number) => {
  if (!bytes) return '—';
  const units = ['B', 'KB', 'MB', 'GB'];
  let idx = 0;
  let value = bytes;
  while (value >= 1024 && idx < units.length - 1) {
    value /= 1024;
    idx += 1;
  }
  return `${value.toFixed(1)} ${units[idx]}`;
};

watch(
  () => props.modelValue,
  async (visible) => {
    if (visible) {
      await fetchDocuments();
    }
  }
);

watch(
  () => props.entityId,
  async () => {
    if (props.modelValue) {
      await fetchDocuments();
    }
  }
);

onMounted(() => {
  if (props.modelValue) {
    fetchDocuments();
  }
});
</script>

<style scoped>
</style>
