<template>
  <div class="p-8">
    <h1 class="text-3xl font-bold text-indigo-600 mb-4">Centro de Treinamento IA - Teste</h1>
    <p class="text-gray-600">Esta é uma página de teste para verificar se consegue acessar o centro de IA.</p>

    <div class="mt-8 grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-lg shadow">
        <h2 class="text-xl font-semibold mb-4">Configuração de Providers</h2>
        <p class="text-gray-600">Configure OpenAI, Gemini, Claude, Groq e Ollama</p>
      </div>

      <div class="bg-white p-6 rounded-lg shadow">
        <h2 class="text-xl font-semibold mb-4">Gestão de Datasets</h2>
        <p class="text-gray-600">Upload e gerenciamento de datasets para treinamento</p>
      </div>
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  layout: 'sidebar'
})
</script>