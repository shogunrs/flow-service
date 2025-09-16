<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <main class="p-6 space-y-6">
      <div class="relative overflow-hidden rounded-lg h-14">
        <CanvasFinance class="opacity-40" :speed="0.6" :font-size="12" />
        <div class="absolute inset-0 flex items-center px-2">
          <h1 class="text-xl font-semibold text-white">Dashboard</h1>
        </div>
      </div>
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
