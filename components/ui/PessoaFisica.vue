<template>
  <BaseModal 
    :model-value="show" 
    title="Dados da Pessoa Física"
    container-class="w-[90vw] max-w-2xl rounded-xl"
    @close="$emit('close')"
  >
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Nome Completo -->
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-slate-300 mb-2">
            Nome Completo *
          </label>
          <BaseInput
            v-model="formData.nomeCompleto"
            placeholder="Digite o nome completo"
            required
          />
        </div>

        <!-- CPF -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            CPF *
          </label>
          <CpfInput
            v-model="formData.cpf"
            required
          />
        </div>

        <!-- RG -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            RG
          </label>
          <RgInput
            v-model="formData.rg"
          />
        </div>

        <!-- Data de Nascimento -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            Data de Nascimento
          </label>
          <BaseInput
            v-model="formData.dataNascimento"
            type="date"
            placeholder="dd/mm/aaaa"
          />
        </div>

        <!-- Estado Civil -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            Estado Civil
          </label>
          <BaseSelect
            v-model="formData.estadoCivil"
            :options="estadoCivilOptions"
            placeholder="Selecione o estado civil"
          />
        </div>

        <!-- Telefone -->
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-slate-300 mb-2">
            Telefone
          </label>
          <TelefoneInput
            v-model="formData.telefone"
          />
        </div>

        <!-- Email -->
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-slate-300 mb-2">
            Email
          </label>
          <BaseInput
            v-model="formData.email"
            type="email"
            placeholder="email@exemplo.com"
          />
        </div>

        <!-- Endereço -->
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-slate-300 mb-2">
            Endereço
          </label>
          <EnderecoInput
            v-model="formData.endereco"
          />
        </div>
      </div>
    </form>

    <template #footer>
      <div class="flex justify-end space-x-2">
        <button
          type="button"
          @click="$emit('close')"
          class="px-4 py-2 text-sm font-medium text-slate-300 bg-slate-700 hover:bg-slate-600 rounded-lg transition-colors"
        >
          Cancelar
        </button>
        <button
          type="button"
          @click="handleSubmit"
          class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
        >
          Salvar
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseSelect from '~/components/ui/BaseSelect.vue'
import CpfInput from '~/components/ui/CpfInput.vue'
import RgInput from '~/components/ui/RgInput.vue'
import TelefoneInput from '~/components/ui/TelefoneInput.vue'
import EnderecoInput from '~/components/ui/EnderecoInput.vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'submit'])

const formData = ref({
  nomeCompleto: '',
  cpf: '',
  rg: '',
  dataNascimento: '',
  estadoCivil: '',
  telefone: '',
  email: '',
  endereco: {}
})

const estadoCivilOptions = [
  { value: 'solteiro', label: 'Solteiro(a)' },
  { value: 'casado', label: 'Casado(a)' },
  { value: 'divorciado', label: 'Divorciado(a)' },
  { value: 'viuvo', label: 'Viúvo(a)' },
  { value: 'separado', label: 'Separado(a)' },
  { value: 'uniao_estavel', label: 'União Estável' }
]

// Watch for initialData changes and update form
watch(() => props.initialData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    formData.value = { ...formData.value, ...newData }
  }
}, { immediate: true, deep: true })

// Reset form when modal opens
watch(() => props.show, (isVisible) => {
  if (isVisible && props.initialData && Object.keys(props.initialData).length > 0) {
    formData.value = { ...formData.value, ...props.initialData }
  }
})

const handleSubmit = () => {
  // Basic validation
  if (!formData.value.nomeCompleto.trim()) {
    alert('Nome completo é obrigatório')
    return
  }
  
  if (!formData.value.cpf.trim()) {
    alert('CPF é obrigatório')
    return
  }

  emit('submit', { ...formData.value })
}

// Initialize form when component mounts
onMounted(() => {
  if (props.initialData && Object.keys(props.initialData).length > 0) {
    formData.value = { ...formData.value, ...props.initialData }
  }
})
</script>