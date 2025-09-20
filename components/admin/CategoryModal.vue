<template>
  <BaseModal v-model="show" title="Categoria de Status" size="md">
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- Name -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          Nome da Categoria
        </label>
        <input
          v-model="form.name"
          type="text"
          required
          placeholder="Ex: Processos Financeiros"
          class="w-full px-3 py-2 bg-slate-800/50 border border-slate-700/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500/50 transition-all duration-300"
        />
      </div>

      <!-- Description -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          Descrição
        </label>
        <textarea
          v-model="form.description"
          rows="3"
          placeholder="Descreva para que serve esta categoria..."
          class="w-full px-3 py-2 bg-slate-800/50 border border-slate-700/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500/50 transition-all duration-300 resize-none"
        ></textarea>
      </div>

      <!-- Icon Selection -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-3">
          Ícone da Categoria
        </label>
        <div class="grid grid-cols-4 gap-3">
          <button
            v-for="iconOption in iconOptions"
            :key="iconOption.icon"
            type="button"
            @click="form.icon = iconOption.icon"
            :class="[
              'p-3 rounded-lg border-2 transition-all duration-300 flex flex-col items-center gap-2',
              form.icon === iconOption.icon
                ? 'border-purple-500 bg-purple-500/20'
                : 'border-slate-700/50 bg-slate-800/30 hover:border-slate-600/50'
            ]"
          >
            <i :class="iconOption.icon" class="text-lg text-white"></i>
            <span class="text-xs text-slate-400">{{ iconOption.label }}</span>
          </button>
        </div>
      </div>

      <!-- Usage Examples -->
      <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/30">
        <h4 class="text-sm font-medium text-slate-300 mb-2 flex items-center gap-2">
          <i class="fa-solid fa-lightbulb text-yellow-400"></i>
          Sugestões de Uso
        </h4>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-slate-400">
          <div>• Processos Financeiros</div>
          <div>• Projetos de Software</div>
          <div>• Gestão de Equipes</div>
          <div>• Atendimento ao Cliente</div>
          <div>• Produtos/Serviços</div>
          <div>• Vendas e Marketing</div>
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
          :disabled="!form.name.trim()"
          class="bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-2 rounded-lg font-medium transition-all duration-300 shadow-lg hover:shadow-purple-500/25"
        >
          {{ category ? 'Atualizar' : 'Criar' }} Categoria
        </button>
      </div>
    </form>
  </BaseModal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  show: Boolean,
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
  icon: 'fa-solid fa-clipboard-list'
})

const iconOptions = [
  { icon: 'fa-solid fa-clipboard-list', label: 'Processos' },
  { icon: 'fa-solid fa-project-diagram', label: 'Projetos' },
  { icon: 'fa-solid fa-users', label: 'Equipes' },
  { icon: 'fa-solid fa-dollar-sign', label: 'Financeiro' },
  { icon: 'fa-solid fa-shipping-fast', label: 'Logística' },
  { icon: 'fa-solid fa-calendar', label: 'Agenda' },
  { icon: 'fa-solid fa-bell', label: 'Notificações' },
  { icon: 'fa-solid fa-cog', label: 'Sistema' },
  { icon: 'fa-solid fa-chart-line', label: 'Analytics' },
  { icon: 'fa-solid fa-handshake', label: 'Vendas' },
  { icon: 'fa-solid fa-headset', label: 'Suporte' },
  { icon: 'fa-solid fa-shield-alt', label: 'Segurança' }
]

watch(() => props.category, (newCategory) => {
  if (newCategory) {
    form.value = {
      name: newCategory.name || '',
      description: newCategory.description || '',
      icon: newCategory.icon || 'fa-solid fa-clipboard-list'
    }
  } else {
    form.value = {
      name: '',
      description: '',
      icon: 'fa-solid fa-clipboard-list'
    }
  }
}, { immediate: true })

const handleSubmit = () => {
  if (!form.value.name.trim()) return

  emit('save', {
    name: form.value.name.trim(),
    description: form.value.description.trim(),
    icon: form.value.icon
  })
}

const handleClose = () => {
  show.value = false
}
</script>