<template>
  <div v-if="shouldShowStats" class="flex items-center gap-3">
    <!-- Indicador de processo financeiro -->
    <div class="flex items-center gap-1.5 px-2 py-1 bg-emerald-600/20 border border-emerald-500/30 rounded-md">
      <i class="fa-solid fa-dollar-sign text-emerald-400 text-xs animate-pulse"
         style="filter: drop-shadow(0 0 4px rgba(34, 197, 94, 0.6));"></i>
      <span class="text-emerald-300 text-xs font-medium">Financeiro</span>
    </div>

    <!-- Estatísticas monetárias -->
    <div v-if="displayStats && !loading" class="flex items-center gap-4">
      <!-- Total em pipeline -->
      <div class="flex items-center gap-1.5">
        <div class="w-2 h-2 rounded-full bg-blue-400 animate-pulse"></div>
        <div class="text-xs">
          <span class="text-slate-400">Pipeline:</span>
          <span class="text-white font-semibold ml-1">{{ formatCompactCurrency(displayStats?.totalAmount || 0) }}</span>
        </div>
      </div>

      <!-- Número de propostas -->
      <div class="flex items-center gap-1.5">
        <div class="w-2 h-2 rounded-full bg-indigo-400"></div>
        <div class="text-xs">
          <span class="text-slate-400">Propostas:</span>
          <span class="text-white font-semibold ml-1">{{ displayStats?.totalProposals || 0 }}</span>
        </div>
      </div>

      <!-- Valor médio -->
      <div v-if="(displayStats?.totalProposals || 0) > 0" class="flex items-center gap-1.5">
        <div class="w-2 h-2 rounded-full bg-amber-400"></div>
        <div class="text-xs">
          <span class="text-slate-400">Média:</span>
          <span class="text-white font-semibold ml-1">{{ formatCompactCurrency(displayStats?.averageAmount || 0) }}</span>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-else-if="loading" class="flex items-center gap-2">
      <div class="w-4 h-4 border-2 border-slate-600 border-t-emerald-400 rounded-full animate-spin"></div>
      <span class="text-slate-400 text-xs">Carregando...</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useProposalStats } from '~/composables/useProposalStats'

const props = defineProps({
  pipelineKey: { type: String, required: true },
  isFinancialProcess: { type: Boolean, default: false },
  proposals: { type: Array, default: () => [] }
})

const { stats, loading, refresh, formatCompactCurrency } = useProposalStats(props.pipelineKey)

// Recalcular estatísticas localmente quando as propostas mudarem
const localStats = computed(() => {
  if (!props.proposals?.length) return null

  const activeProposals = props.proposals.filter(p => !p.isArchived)
  const totalProposals = activeProposals.length
  const totalAmount = activeProposals.reduce((sum, p) => sum + (p.amount || 0), 0)
  const averageAmount = totalProposals > 0 ? totalAmount / totalProposals : 0

  const proposalsByStage = {}
  activeProposals.forEach(p => {
    if (p.stageId) {
      proposalsByStage[p.stageId] = (proposalsByStage[p.stageId] || 0) + 1
    }
  })

  return {
    totalProposals,
    totalAmount,
    averageAmount,
    proposalsByStage,
    processKey: props.pipelineKey
  }
})

// Usar estatísticas locais se API stats estiver vazio ou não disponível
const displayStats = computed(() => {
  // Se temos dados da API e eles têm conteúdo, usar API
  if (stats.value && (stats.value.totalProposals > 0 || stats.value.totalAmount > 0)) {
    return stats.value
  }
  // Senão, usar estatísticas calculadas localmente
  return localStats.value
})

const shouldShowStats = computed(() => props.isFinancialProcess && props.pipelineKey)

// Carregar estatísticas quando componente monta ou pipeline muda
onMounted(() => {
  if (shouldShowStats.value) {
    refresh()
  }
})

watch(() => props.pipelineKey, () => {
  if (shouldShowStats.value) {
    refresh()
  }
})

watch(() => props.isFinancialProcess, (isFinancial) => {
  if (isFinancial && props.pipelineKey) {
    refresh()
  }
})

// Expor função para refresh manual (para quando propostas são criadas/atualizadas)
defineExpose({ refresh })
</script>

<style scoped>
/* Animação suave para o indicador financeiro */
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>