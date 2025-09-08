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
      <div>
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
          v-model="recordAmount"
          type="text"
          mask="currency"
          locale="pt-BR"
          label="Valor do Processo (R$)"
          size="md"
          placeholder="0,00"
        />
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
          class="px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white text-sm font-medium rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
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

// Cores das etapas (mesmo mapeamento usado na esteira)
const colorMap = {
  sky: "bg-sky-500",
  indigo: "bg-indigo-500",
  amber: "bg-amber-500",
  rose: "bg-rose-500",
  green: "bg-green-500",
  purple: "bg-purple-500",
  blue: "bg-blue-500",
  orange: "bg-orange-500",
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
  }
});

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
    amount: recordAmount.value || 0,
    stageId: firstStage.value?.id,
    status: firstStage.value?.status || "Pendente",
    fieldValues: {},
    fieldFiles: {},
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
