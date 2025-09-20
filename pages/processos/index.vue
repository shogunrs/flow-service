<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
    <!-- Modern Header with Glass Effect -->
    <header class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 p-4">
      <div class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"></div>
      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="p-2 bg-gradient-to-br from-purple-600 to-indigo-600 rounded-xl shadow-lg">
            <i class="fa-solid fa-clipboard-list text-white text-sm"></i>
          </div>
          <div>
            <h1 class="app-header-title">Processos Ativos</h1>
            <p class="app-header-subtitle">
              Visualize e gerencie todos os processos em andamento
            </p>
          </div>
        </div>
        <NuxtLink
          to="/admin?tab=pipeline"
          class="bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all duration-300 flex items-center gap-2 shadow-lg hover:shadow-purple-500/25 hover:scale-105"
        >
          <i class="fa-solid fa-cog"></i>
          Gerenciar Processos
        </NuxtLink>
      </div>
    </header>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 px-6 py-6">
      <div
        v-for="p in processes"
        :key="p.key"
        class="rounded-lg border border-slate-700/60 bg-slate-900/40 p-4"
      >
        <div class="flex items-center justify-between mb-2">
          <div>
            <div class="text-white text-sm font-semibold">{{ p.name }}</div>
            <div class="text-[11px] text-slate-400">{{ p.key }}</div>
          </div>
          <NuxtLink
            :to="`/esteira/${p.key}`"
            class="text-xs bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md"
            >Abrir</NuxtLink
          >
        </div>
        <div class="flex items-center gap-2">
          <button
            class="text-xs bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md"
            @click="openConfig(p.key)"
          >
            Configurar Colunas
          </button>
        </div>
      </div>
      <div v-if="processes.length === 0" class="text-slate-300 text-sm">
        Nenhum processo ativo. Crie um em Admin &gt; Esteira.
      </div>
    </div>

    <BaseModal
      v-model="showModal"
      title="Configurar Esteira"
      size="lg"
      :z-index="55"
    >
      <div class="space-y-3 max-h-[70vh] overflow-y-auto pr-1">
        <PipelineManager v-model:stages="stages" :pipeline-key="selectedKey" />
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="showModal = false"
          >
            Fechar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-3 py-2 rounded-md text-sm"
            @click="showModal = false"
          >
            Salvar
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import PipelineManager from "~/components/admin/PipelineManager.vue";
import {
  listActiveProcesses,
  loadStages,
  saveStages,
  sanitizeProcessKey,
} from "~/composables/usePipeline";

definePageMeta({
  layout: 'sidebar'
});

useHead({ title: "Processos" });

const processes = ref(listActiveProcesses());

const showModal = ref(false);
const selectedKey = ref("");
const stages = ref<Stage[]>([]);

function openConfig(key: string) {
  const k = sanitizeProcessKey(key);
  selectedKey.value = k;
  stages.value = loadStages(k) || [];
  showModal.value = true;
}

watch(
  stages,
  (v) => {
    if (!selectedKey.value) return;
    saveStages(selectedKey.value, v);
  },
  { deep: true }
);
</script>
