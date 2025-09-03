<template>
  <div class="min-h-screen flex items-center justify-center relative overflow-hidden">
    <div
      class="absolute inset-0 bg-cover bg-center bg-no-repeat"
      style="background-image: url('https://images.unsplash.com/photo-1677442135703-1787eea5ce01?q=80&w=2574&auto=format&fit=crop');"
    >
      <div class="absolute inset-0 bg-black/80"></div>
    </div>

    <div class="relative z-10 w-full max-w-md px-6">
      <div class="bg-white/10 backdrop-blur-md rounded-2xl shadow-2xl p-8 border border-white/20 text-white">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold">Bem-vindo de volta</h1>
          <p class="text-slate-300 mt-2">Faça login para acessar sua conta.</p>
        </div>

        <form class="space-y-6" @submit.prevent>
          <div>
            <label for="email" class="block text-sm font-medium text-slate-300">E-mail</label>
            <div class="mt-1 relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                <i class="fa-solid fa-envelope text-slate-400"></i>
              </span>
              <input id="email" type="email" class="w-full bg-white/10 border border-white/20 rounded-lg py-3 pl-10 pr-3 text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition" placeholder="voce@exemplo.com" />
            </div>
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-slate-300">Senha</label>
            <div class="mt-1 relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                <i class="fa-solid fa-lock text-slate-400"></i>
              </span>
              <input id="password" type="password" class="w-full bg-white/10 border border-white/20 rounded-lg py-3 pl-10 pr-3 text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition" placeholder="Sua senha" />
            </div>
          </div>

          <div class="flex items-center justify-between">
            <label class="flex items-center">
              <input type="checkbox" class="h-4 w-4 bg-white/20 border-white/30 text-orange-500 focus:ring-orange-500 rounded" />
              <span class="ml-2 block text-sm text-slate-300">Lembrar-me</span>
            </label>
            <NuxtLink to="#" class="text-sm font-medium text-orange-400 hover:text-orange-300">Esqueceu sua senha?</NuxtLink>
          </div>

          <button type="submit" class="w-full flex justify-center py-3 px-4 rounded-lg text-sm font-medium text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-slate-800 focus:ring-orange-500 transition-transform transform hover:scale-105">
            Entrar
          </button>
        </form>

        <p class="mt-8 text-center text-sm text-slate-400">
          Não tem uma conta?
          <NuxtLink to="#" class="font-medium text-orange-400 hover:text-orange-300">Cadastre-se</NuxtLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
useHead({ title: 'Login' })
definePageMeta({ layout: 'default' })
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css');
</style>
