<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
    <!-- Modern Header with Glass Effect -->
    <header class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 p-4">
      <div class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"></div>
      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="p-2 bg-gradient-to-br from-purple-600 to-indigo-600 rounded-xl shadow-lg">
            <i class="fa-solid fa-bell text-white text-sm"></i>
          </div>
          <div>
            <h1 class="app-header-title">Notificações</h1>
            <p class="app-header-subtitle">
              Configure notificações e alertas do sistema
            </p>
          </div>
        </div>
        <button class="app-header-action" @click="openNotificationModal">
          <i class="fa-solid fa-plus"></i>
          Nova Notificação
        </button>
      </div>
    </header>

    <main class="p-4 space-y-4">
      <!-- Card: Configurações Gerais -->
      <div class="app-card">
        <div class="app-card-content">
          <div class="flex items-center gap-3 mb-4">
            <div class="app-card-icon">
              <i class="fa-solid fa-cog"></i>
            </div>
            <h3 class="app-section-title">Configurações</h3>
          </div>

          <div class="space-y-3">
            <div class="app-card-item flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="rounded-md bg-blue-500/20 p-2 text-blue-300">
                  <i class="fa-solid fa-envelope text-xs"></i>
                </div>
                <label class="app-section-label">Email</label>
              </div>
              <button
                @click="toggleSetting('email')"
                :class="[
                  'relative inline-flex h-5 w-9 items-center rounded-full transition-all',
                  settings.email ? 'bg-purple-600' : 'bg-slate-600'
                ]"
              >
                <span :class="[
                  'inline-block h-3 w-3 transform rounded-full bg-white transition-transform',
                  settings.email ? 'translate-x-4' : 'translate-x-1'
                ]"></span>
              </button>
            </div>

            <div class="app-card-item flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="rounded-md bg-green-500/20 p-2 text-green-300">
                  <i class="fa-solid fa-bell text-xs"></i>
                </div>
                <label class="app-section-label">Push</label>
              </div>
              <button
                @click="toggleSetting('push')"
                :class="[
                  'relative inline-flex h-5 w-9 items-center rounded-full transition-all',
                  settings.push ? 'bg-purple-600' : 'bg-slate-600'
                ]"
              >
                <span :class="[
                  'inline-block h-3 w-3 transform rounded-full bg-white transition-transform',
                  settings.push ? 'translate-x-4' : 'translate-x-1'
                ]"></span>
              </button>
            </div>

            <div class="app-card-item flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="rounded-md bg-yellow-500/20 p-2 text-yellow-200">
                  <i class="fa-solid fa-mobile text-xs"></i>
                </div>
                <label class="app-section-label">SMS</label>
              </div>
              <button
                @click="toggleSetting('sms')"
                :class="[
                  'relative inline-flex h-5 w-9 items-center rounded-full transition-all',
                  settings.sms ? 'bg-purple-600' : 'bg-slate-600'
                ]"
              >
                <span :class="[
                  'inline-block h-3 w-3 transform rounded-full bg-white transition-transform',
                  settings.sms ? 'translate-x-4' : 'translate-x-1'
                ]"></span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Card: Tipos de Notificação -->
      <div class="app-card">
        <div class="app-card-content">
          <div class="flex items-center gap-3 mb-4">
            <div class="app-card-icon">
              <i class="fa-solid fa-tags"></i>
            </div>
            <h3 class="app-section-title">Tipos de Notificação</h3>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            <div
              v-for="type in notificationTypes"
              :key="type.id"
              @click="openTypeModal(type)"
              class="app-card-item cursor-pointer flex flex-col gap-2"
            >
              <div class="flex items-center gap-3 w-full">
                <div
                  :class="[
                    'rounded-md p-2 text-xs',
                    `bg-${type.color}-500/20`,
                    `text-${type.color}-300`
                  ]"
                >
                  <i :class="[type.icon, 'text-xs']"></i>
                </div>
                <div class="flex-1 min-w-0">
                  <h4 class="app-section-label truncate">{{ type.name }}</h4>
                  <p class="app-section-hint truncate">{{ type.description }}</p>
                </div>
              </div>
              <div>
                <span :class="[
                  'app-chip',
                  type.active ? 'app-chip--success' : 'app-chip--muted'
                ]">
                  {{ type.active ? 'Ativo' : 'Inativo' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card: Histórico Recente -->
      <div class="app-card">
        <div class="app-card-content">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="app-card-icon">
                <i class="fa-solid fa-history"></i>
              </div>
              <h3 class="app-section-title">Histórico recente</h3>
            </div>
            <button class="app-link">
              Ver tudo
            </button>
          </div>

          <div class="space-y-2">
            <div
              v-for="notification in recentNotifications.slice(0, 3)"
              :key="notification.id"
              class="app-card-item flex items-center justify-between gap-3"
            >
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <div :class="[
                  'w-2 h-2 rounded-full',
                  `bg-${notification.color}-400`
                ]"></div>
                <div class="flex-1 min-w-0">
                  <h4 class="app-section-label truncate">{{ notification.title }}</h4>
                  <p class="app-section-hint truncate">{{ notification.message }}</p>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <span class="app-section-hint">
                  {{ notification.time.split(' ')[1] }}
                </span>
                <button
                  @click="removeNotification(notification.id)"
                  class="text-slate-400 hover:text-red-400 transition-colors"
                >
                  <i class="fa-solid fa-times text-xs"></i>
                </button>
              </div>
            </div>
          </div>

          <div v-if="recentNotifications.length === 0" class="text-center py-4">
            <i class="fa-solid fa-bell-slash text-slate-500 text-base mb-2"></i>
            <p class="app-section-hint">Nenhuma notificação recente</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

definePageMeta({
  layout: 'sidebar',
  title: 'Notificações - Admin'
})

// Estado das configurações
const settings = reactive({
  email: true,
  push: false,
  sms: false
})

// Tipos de notificação
const notificationTypes = ref([
  {
    id: 1,
    name: 'Novos Processos',
    description: 'Quando um novo processo é criado',
    icon: 'fa-solid fa-file-plus',
    color: 'blue',
    active: true
  },
  {
    id: 2,
    name: 'Aprovações',
    description: 'Processos aprovados ou rejeitados',
    icon: 'fa-solid fa-check-circle',
    color: 'green',
    active: true
  },
  {
    id: 3,
    name: 'Sistema',
    description: 'Manutenções e atualizações',
    icon: 'fa-solid fa-cog',
    color: 'yellow',
    active: false
  },
  {
    id: 4,
    name: 'Usuários',
    description: 'Novos usuários e alterações',
    icon: 'fa-solid fa-users',
    color: 'purple',
    active: true
  },
  {
    id: 5,
    name: 'Financeiro',
    description: 'Transações e pagamentos',
    icon: 'fa-solid fa-dollar-sign',
    color: 'emerald',
    active: true
  },
  {
    id: 6,
    name: 'Segurança',
    description: 'Alertas de segurança',
    icon: 'fa-solid fa-shield',
    color: 'red',
    active: false
  }
])

// Histórico de notificações
const recentNotifications = ref([
  {
    id: 1,
    title: 'Novo processo criado',
    message: 'ConsorEquity foi adicionado ao sistema',
    time: 'Há 2 horas',
    type: 'Processo',
    color: 'blue'
  },
  {
    id: 2,
    title: 'Usuário aprovado',
    message: 'João Silva passou na verificação',
    time: 'Há 5 horas',
    type: 'Usuário',
    color: 'green'
  },
  {
    id: 3,
    title: 'Pagamento recebido',
    message: 'Transação de R$ 2.500 processada',
    time: 'Há 8 horas',
    type: 'Financeiro',
    color: 'emerald'
  },
  {
    id: 4,
    title: 'Sistema atualizado',
    message: 'Nova versão 2.1.0 implantada',
    time: 'Há 1 dia',
    type: 'Sistema',
    color: 'yellow'
  }
])

// Funções
function toggleSetting(type) {
  settings[type] = !settings[type]
  console.log(`${type} notifications:`, settings[type] ? 'enabled' : 'disabled')
}

function openNotificationModal() {
  console.log('Opening notification creation modal')
}

function openTypeModal(type) {
  console.log('Configuring notification type:', type.name)
}

function removeNotification(id) {
  const index = recentNotifications.value.findIndex(n => n.id === id)
  if (index > -1) {
    recentNotifications.value.splice(index, 1)
  }
}
</script>
