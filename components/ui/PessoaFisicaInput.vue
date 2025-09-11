<template>
  <div class="relative">
    <BaseInput
      :model-value="displayValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      readonly
      @click="openModal"
      class="cursor-pointer bg-slate-800 hover:bg-slate-700 transition-colors"
    >
      <template #suffix>
        <button
          type="button"
          @click="openModal"
          class="text-slate-400 hover:text-white transition-colors p-1"
          :disabled="disabled"
        >
          <i class="fa-solid fa-user text-sm"></i>
        </button>
      </template>
    </BaseInput>
    
    <!-- Modal para edição -->
    <PessoaFisica
      :show="showModal"
      :initial-data="modelValue || {}"
      @close="closeModal"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import PessoaFisica from '~/components/ui/PessoaFisica.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  placeholder: {
    type: String,
    default: 'Clique para inserir dados da pessoa física'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const showModal = ref(false)

// Computed para mostrar um resumo dos dados no input
const displayValue = computed(() => {
  if (!props.modelValue || Object.keys(props.modelValue).length === 0) {
    return ''
  }
  
  const { nomeCompleto, cpf } = props.modelValue
  if (nomeCompleto && cpf) {
    return `${nomeCompleto} - CPF: ${cpf}`
  } else if (nomeCompleto) {
    return nomeCompleto
  } else if (cpf) {
    return `CPF: ${cpf}`
  }
  
  return 'Dados preenchidos'
})

const openModal = () => {
  if (!props.disabled) {
    showModal.value = true
  }
}

const closeModal = () => {
  showModal.value = false
}

const handleSubmit = (data) => {
  emit('update:modelValue', data)
  closeModal()
}
</script>