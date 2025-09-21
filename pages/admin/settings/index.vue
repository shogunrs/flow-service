<template>
  <div class="min-h-screen bg-slate-950 text-slate-100">
    <header class="border-b border-slate-800 bg-slate-900/80 px-6 py-4 backdrop-blur">
      <div class="max-w-5xl mx-auto">
        <h1 class="text-lg font-semibold">Configurações do sistema</h1>
        <p class="text-sm text-slate-400">Selecione um módulo para ajustar integrações e monitoramento.</p>
      </div>
    </header>
    <main class="max-w-5xl mx-auto p-6 grid gap-6 sm:grid-cols-2">
      <NuxtLink
        to="/admin/settings/system-health"
        class="group rounded-xl border border-slate-800 bg-slate-900/70 p-5 hover:border-indigo-500/60 hover:bg-indigo-500/10 transition"
      >
        <div class="flex items-center gap-3">
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-500/20 text-indigo-300">
            <i class="fa-solid fa-heart-pulse"></i>
          </span>
          <div>
            <h2 class="text-sm font-semibold">Monitoramento do sistema</h2>
            <p class="text-xs text-slate-400">Visualize alertas, reconheça incidentes e configure status monitorados.</p>
          </div>
        </div>
      </NuxtLink>
      <NuxtLink
        to="/admin/settings/status"
        class="group rounded-xl border border-slate-800 bg-slate-900/70 p-5 hover:border-sky-500/60 hover:bg-sky-500/10 transition"
      >
        <div class="flex items-center gap-3">
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-sky-500/20 text-sky-300">
            <i class="fa-solid fa-tags"></i>
          </span>
          <div>
            <h2 class="text-sm font-semibold">Catálogo de status</h2>
            <p class="text-xs text-slate-400">Gerencie as tags utilizadas em pipelines, usuários e alertas.</p>
          </div>
        </div>
      </NuxtLink>
      <NuxtLink
        to="/admin/settings/ia"
        class="group rounded-xl border border-slate-800 bg-slate-900/70 p-5 hover:border-purple-500/60 hover:bg-purple-500/10 transition"
      >
        <div class="flex items-center gap-3">
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-purple-500/20 text-purple-300">
            <i class="fa-solid fa-robot"></i>
          </span>
          <div>
            <h2 class="text-sm font-semibold">Centro IA</h2>
            <p class="text-xs text-slate-400">Configure providers e monitore treinos dos modelos de IA.</p>
          </div>
        </div>
      </NuxtLink>
      <NuxtLink
        to="/admin/notifications"
        class="group rounded-xl border border-slate-800 bg-slate-900/70 p-5 hover:border-amber-500/60 hover:bg-amber-500/10 transition"
      >
        <div class="flex items-center gap-3">
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-amber-500/20 text-amber-300">
            <i class="fa-solid fa-bell"></i>
          </span>
          <div>
            <h2 class="text-sm font-semibold">Notificações</h2>
            <p class="text-xs text-slate-400">Configure canais e templates de comunicação.</p>
          </div>
        </div>
      </NuxtLink>
    </main>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'sidebar',
  title: 'Configurações do sistema',
})
</script>
