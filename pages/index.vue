<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
    <!-- Modern Header with Glass Effect -->
    <header class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 p-4">
      <div class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"></div>
      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="p-2 bg-gradient-to-br from-purple-600 to-indigo-600 rounded-xl shadow-lg">
            <i class="fa-solid fa-home text-white text-sm"></i>
          </div>
          <div>
            <h1 class="app-header-title">Dashboard</h1>
            <p class="app-header-subtitle">
              Visão geral do sistema e métricas principais
            </p>
          </div>
        </div>
      </div>
    </header>

    <main class="p-3 space-y-3">
      <KPIGrid :items="kpis" />

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-3">
        <div class="lg:col-span-2">
          <MiniBarChart
            :label="'Propostas por dia'"
            :period="'Últimos 12 dias'"
            :data="chartData"
            color="orange"
          />
        </div>
        <QuickActions :actions="quickActions" @action="onAction" />
      </div>

      <ActivityFeed :items="activities" />
    </main>
  </div>
</template>

<script setup>
import CanvasFinance from "~/components/ui/CanvasFinance.vue";
import KPIGrid from "~/components/dashboard/KPIGrid.vue";
import MiniBarChart from "~/components/dashboard/MiniBarChart.vue";
import ActivityFeed from "~/components/dashboard/ActivityFeed.vue";
import QuickActions from "~/components/dashboard/QuickActions.vue";
import { useProcessSubmenu } from "~/composables/useProcessMenu";

definePageMeta({
  layout: 'sidebar'
});

// Dashboard data (mocked)
const kpis = [
  {
    key: "volume",
    label: "Volume (R$)",
    value: "1,25 mi",
    delta: 6.8,
    hint: "vs. mês anterior",
    icon: "fa-solid fa-sack-dollar",
    color: "orange",
  },
  {
    key: "propostas",
    label: "Propostas",
    value: 128,
    delta: 3.2,
    hint: "30 dias",
    icon: "fa-solid fa-file-lines",
    color: "blue",
  },
  {
    key: "aprovadas",
    label: "Aprovadas",
    value: 48,
    delta: 1.1,
    hint: "30 dias",
    icon: "fa-solid fa-circle-check",
    color: "green",
  },
  {
    key: "sla",
    label: "SLA Médio (dias)",
    value: 4.2,
    delta: -0.4,
    hint: "últ. 10 mov.",
    icon: "fa-solid fa-stopwatch",
    color: "purple",
  },
];

const chartData = [4, 6, 5, 7, 10, 8, 6, 9, 11, 10, 12, 13];

const quickActions = [
  { key: "nova", label: "Nova Proposta", icon: "fa-solid fa-plus" },
  { key: "importar", label: "Importar CSV", icon: "fa-solid fa-file-import" },
  { key: "simular", label: "Simular Carta", icon: "fa-solid fa-calculator" },
  { key: "filtros", label: "Abrir Filtros", icon: "fa-solid fa-filter" },
];

const activities = [
  {
    id: 1,
    title: "Proposta de Ana atualizada para Crédito",
    time: "há 2h",
    icon: "fa-solid fa-pen-to-square",
  },
  {
    id: 2,
    title: "Documento recebido: RG - Bruno",
    time: "há 3h",
    icon: "fa-regular fa-file",
  },
  {
    id: 3,
    title: "Proposta de Carla aprovada",
    time: "há 5h",
    icon: "fa-solid fa-circle-check",
  },
];
</script>
