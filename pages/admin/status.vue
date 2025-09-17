<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Page header -->
    <header class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-6 py-4">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Gerenciamento de Status</h1>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Configure status dos processos e fluxos
          </p>
        </div>
      </div>
    </header>

    <main class="p-6">
      <div class="bg-gray-800 rounded-lg p-6">
        <div class="text-center py-12">
          <i class="fa-solid fa-circle-info text-4xl text-slate-400 mb-4"></i>
          <h3 class="text-xl font-semibold text-slate-300 mb-2">Configurações de Status</h3>
          <p class="text-slate-400">
            Configure os status disponíveis para os processos da esteira
          </p>

          <!-- Placeholder for status management content -->
          <div class="mt-8 space-y-4">
            <div class="bg-slate-700/50 rounded-lg p-4 text-left">
              <h4 class="text-sm font-medium text-slate-300 mb-2">Status Disponíveis</h4>
              <div class="space-y-2">
                <div class="flex items-center justify-between bg-slate-800/50 rounded px-3 py-2">
                  <div class="flex items-center gap-3">
                    <div class="w-3 h-3 bg-yellow-400 rounded-full"></div>
                    <span class="text-sm text-slate-300">Pendente</span>
                  </div>
                  <button class="text-slate-400 hover:text-white">
                    <i class="fa-solid fa-pen text-xs"></i>
                  </button>
                </div>
                <div class="flex items-center justify-between bg-slate-800/50 rounded px-3 py-2">
                  <div class="flex items-center gap-3">
                    <div class="w-3 h-3 bg-blue-400 rounded-full"></div>
                    <span class="text-sm text-slate-300">Em Análise</span>
                  </div>
                  <button class="text-slate-400 hover:text-white">
                    <i class="fa-solid fa-pen text-xs"></i>
                  </button>
                </div>
                <div class="flex items-center justify-between bg-slate-800/50 rounded px-3 py-2">
                  <div class="flex items-center gap-3">
                    <div class="w-3 h-3 bg-green-400 rounded-full"></div>
                    <span class="text-sm text-slate-300">Aprovado</span>
                  </div>
                  <button class="text-slate-400 hover:text-white">
                    <i class="fa-solid fa-pen text-xs"></i>
                  </button>
                </div>
                <div class="flex items-center justify-between bg-slate-800/50 rounded px-3 py-2">
                  <div class="flex items-center gap-3">
                    <div class="w-3 h-3 bg-red-400 rounded-full"></div>
                    <span class="text-sm text-slate-300">Rejeitado</span>
                  </div>
                  <button class="text-slate-400 hover:text-white">
                    <i class="fa-solid fa-pen text-xs"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
definePageMeta({
  layout: 'sidebar',
  title: 'Status - Admin'
})
</script>