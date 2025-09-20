<template>
  <BaseModal v-model="show" title="Configurar Status" size="md">
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- Category Info -->
      <div v-if="category" class="bg-slate-800/30 rounded-lg p-3 border border-slate-700/30">
        <div class="flex items-center gap-3">
          <div class="p-2 rounded-lg bg-gradient-to-br from-purple-600 to-indigo-600">
            <i :class="category.icon" class="text-white text-sm"></i>
          </div>
          <div>
            <h4 class="text-sm font-medium text-white">{{ category.name }}</h4>
            <p class="text-xs text-slate-400">{{ category.description }}</p>
          </div>
        </div>
      </div>

      <!-- Status Name -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          Nome do Status
        </label>
        <input
          v-model="form.name"
          type="text"
          required
          placeholder="Ex: Em Análise"
          class="w-full px-3 py-2 bg-slate-800/50 border border-slate-700/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500/50 transition-all duration-300"
        />
      </div>

      <!-- Status Description -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          Descrição (Opcional)
        </label>
        <input
          v-model="form.description"
          type="text"
          placeholder="Descrição do que significa este status..."
          class="w-full px-3 py-2 bg-slate-800/50 border border-slate-700/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500/50 transition-all duration-300"
        />
      </div>

      <!-- Color Selection -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-3">
          Cor do Status
        </label>
        <div class="grid grid-cols-8 gap-3 mb-4">
          <button
            v-for="colorOption in colorOptions"
            :key="colorOption.value"
            type="button"
            @click="form.color = colorOption.value"
            :class="[
              'w-8 h-8 rounded-full border-2 transition-all duration-300 relative',
              form.color === colorOption.value
                ? 'border-white scale-110 shadow-lg'
                : 'border-slate-600 hover:border-slate-500 hover:scale-105'
            ]"
            :style="{ backgroundColor: colorOption.value }"
            :title="colorOption.name"
          >
            <i
              v-if="form.color === colorOption.value"
              class="fa-solid fa-check text-white text-xs absolute inset-0 flex items-center justify-center"
            ></i>
          </button>
        </div>

        <!-- Custom Color Input -->
        <div class="flex items-center gap-3">
          <label class="text-xs text-slate-400">Cor personalizada:</label>
          <input
            v-model="form.color"
            type="color"
            class="w-12 h-8 rounded border border-slate-700/50 bg-slate-800 cursor-pointer"
          />
          <span class="text-xs text-slate-400 font-mono">{{ form.color }}</span>
        </div>
      </div>

      <!-- Preview -->
      <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/30">
        <h4 class="text-sm font-medium text-slate-300 mb-3 flex items-center gap-2">
          <i class="fa-solid fa-eye text-purple-400"></i>
          Preview
        </h4>
        <div class="flex items-center gap-3 bg-slate-900/50 rounded-lg p-3">
          <div
            class="w-3 h-3 rounded-full"
            :style="{ backgroundColor: form.color }"
          ></div>
          <div>
            <div class="text-sm text-white font-medium">
              {{ form.name || 'Nome do Status' }}
            </div>
            <div class="text-xs text-slate-400">
              {{ form.description || 'Descrição do status' }}
            </div>
          </div>
        </div>
      </div>

      <!-- Suggested Status -->
      <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/30">
        <h4 class="text-sm font-medium text-slate-300 mb-2 flex items-center gap-2">
          <i class="fa-solid fa-lightbulb text-yellow-400"></i>
          Sugestões Rápidas
        </h4>
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="suggestion in statusSuggestions"
            :key="suggestion.name"
            type="button"
            @click="applySuggestion(suggestion)"
            class="flex items-center gap-2 p-2 rounded bg-slate-700/30 hover:bg-slate-700/50 transition-all duration-300 text-left"
          >
            <div
              class="w-2 h-2 rounded-full"
              :style="{ backgroundColor: suggestion.color }"
            ></div>
            <span class="text-xs text-slate-300">{{ suggestion.name }}</span>
          </button>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex justify-end gap-3 pt-4 border-t border-slate-700/50">
        <button
          type="button"
          @click="handleClose"
          class="px-4 py-2 text-slate-300 hover:text-white transition-colors"
        >
          Cancelar
        </button>
        <button
          type="submit"
          :disabled="!form.name.trim() || !form.color"
          class="bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-2 rounded-lg font-medium transition-all duration-300 shadow-lg hover:shadow-purple-500/25"
        >
          {{ status ? 'Atualizar' : 'Criar' }} Status
        </button>
      </div>
    </form>
  </BaseModal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  show: Boolean,
  status: Object,
  category: Object
})

const emit = defineEmits(['update:show', 'save'])

const show = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

const form = ref({
  name: '',
  description: '',
  color: '#3b82f6'
})

const colorOptions = [
  { name: 'Vermelho', value: '#ef4444' },
  { name: 'Laranja', value: '#f97316' },
  { name: 'Amarelo', value: '#eab308' },
  { name: 'Verde', value: '#22c55e' },
  { name: 'Esmeralda', value: '#10b981' },
  { name: 'Azul', value: '#3b82f6' },
  { name: 'Índigo', value: '#6366f1' },
  { name: 'Roxo', value: '#8b5cf6' },
  { name: 'Rosa', value: '#ec4899' },
  { name: 'Cinza', value: '#6b7280' },
  { name: 'Ciano', value: '#06b6d4' },
  { name: 'Lima', value: '#84cc16' },
  { name: 'Âmbar', value: '#f59e0b' },
  { name: 'Teal', value: '#14b8a6' },
  { name: 'Violeta', value: '#7c3aed' },
  { name: 'Slate', value: '#64748b' }
]

const statusSuggestions = [
  { name: 'Pendente', color: '#f59e0b', description: 'Aguardando ação' },
  { name: 'Em Análise', color: '#3b82f6', description: 'Sendo analisado' },
  { name: 'Aprovado', color: '#22c55e', description: 'Processo aprovado' },
  { name: 'Rejeitado', color: '#ef4444', description: 'Processo rejeitado' },
  { name: 'Em Progresso', color: '#06b6d4', description: 'Em desenvolvimento' },
  { name: 'Concluído', color: '#10b981', description: 'Finalizado' },
  { name: 'Pausado', color: '#f97316', description: 'Temporariamente pausado' },
  { name: 'Cancelado', color: '#6b7280', description: 'Processo cancelado' }
]

watch(() => props.status, (newStatus) => {
  if (newStatus) {
    form.value = {
      name: newStatus.name || '',
      description: newStatus.description || '',
      color: newStatus.color || '#3b82f6'
    }
  } else {
    form.value = {
      name: '',
      description: '',
      color: '#3b82f6'
    }
  }
}, { immediate: true })

const applySuggestion = (suggestion) => {
  form.value.name = suggestion.name
  form.value.description = suggestion.description
  form.value.color = suggestion.color
}

const handleSubmit = () => {
  if (!form.value.name.trim() || !form.value.color) return

  emit('save', {
    name: form.value.name.trim(),
    description: form.value.description.trim(),
    color: form.value.color
  })
}

const handleClose = () => {
  show.value = false
}
</script>