<template>
  <div class="input-wrapper" :class="wrapperClasses">
    <label v-if="label" :for="inputId" class="input-label">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="input-container" :class="containerClasses">
      <input
        :id="inputId"
        v-model="maskedValue"
        type="text"
        :placeholder="placeholder || '(00) 00000-0000'"
        :required="required"
        :disabled="disabled"
        class="input-field input-field--xs"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        maxlength="15"
      />
      
      <div class="input-suffix">
        <i v-if="isValid === true" class="fa-solid fa-check text-green-500 input-icon"></i>
        <i v-else-if="isValid === false" class="fa-solid fa-times text-red-500 input-icon"></i>
        <i v-else-if="isValidating" class="fa-solid fa-spinner fa-spin text-slate-400 input-icon"></i>
      </div>
    </div>
    
    <div v-if="errorMessage || phoneInfo || (showValidationInfo && maskedValue && isValid !== null)" class="input-help">
      <p v-if="errorMessage" class="input-error">
        {{ errorMessage }}
      </p>
      <div v-if="phoneInfo && isValid === true" class="text-[10px] text-slate-400 mt-1">
        <div class="bg-slate-900/50 p-2 rounded border border-slate-700/40">
          <div class="flex items-center gap-2 text-slate-300">
            <i :class="phoneTypeIcon" class="text-sm"></i>
            <span class="font-medium">{{ phoneInfo.type }} - {{ phoneInfo.region }}</span>
          </div>
        </div>
      </div>
      <div v-if="showValidationInfo && maskedValue && isValid !== null" class="text-[10px] text-slate-400 mt-1">
        <div class="space-y-0.5">
          <div :class="validationChecks.format ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.format"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Formato válido
          </div>
          <div :class="validationChecks.ddd ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.ddd"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            DDD válido
          </div>
          <div :class="validationChecks.type ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.type"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Tipo identificado
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  showValidationInfo: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'validation'])

const inputId = computed(() => `telefone-${Math.random().toString(36).slice(2, 8)}`)
const maskedValue = ref('')
const isValid = ref(null)
const isValidating = ref(false)
const errorMessage = ref('')
const phoneInfo = ref(null)
const validationChecks = ref({
  format: false,
  ddd: false,
  type: false
})

// Classes do BaseInput
const wrapperClasses = computed(() => {
  const classes = []
  if (props.disabled) classes.push('input-wrapper--disabled')
  if (isValid.value === false) classes.push('input-wrapper--error')
  if (isValid.value === true) classes.push('input-wrapper--success')
  return classes
})

const containerClasses = computed(() => {
  const classes = []
  classes.push('input-container--xs')
  classes.push('input-container--default')
  if (props.disabled) classes.push('input-container--disabled')
  if (isValid.value === false) classes.push('input-container--error')
  if (isValid.value === true) classes.push('input-container--success')
  return classes
})

// DDDs válidos por região
const dddMap = {
  11: 'São Paulo - SP', 12: 'São José dos Campos - SP', 13: 'Santos - SP', 14: 'Bauru - SP', 15: 'Sorocaba - SP',
  16: 'Ribeirão Preto - SP', 17: 'São José do Rio Preto - SP', 18: 'Presidente Prudente - SP', 19: 'Campinas - SP',
  21: 'Rio de Janeiro - RJ', 22: 'Campos dos Goytacazes - RJ', 24: 'Volta Redonda - RJ',
  27: 'Vitória - ES', 28: 'Cachoeiro de Itapemirim - ES',
  31: 'Belo Horizonte - MG', 32: 'Juiz de Fora - MG', 33: 'Governador Valadares - MG', 34: 'Uberlândia - MG', 35: 'Poços de Caldas - MG', 37: 'Divinópolis - MG', 38: 'Montes Claros - MG',
  41: 'Curitiba - PR', 42: 'Ponta Grossa - PR', 43: 'Londrina - PR', 44: 'Maringá - PR', 45: 'Cascavel - PR', 46: 'Francisco Beltrão - PR',
  47: 'Joinville - SC', 48: 'Florianópolis - SC', 49: 'Chapecó - SC',
  51: 'Porto Alegre - RS', 53: 'Pelotas - RS', 54: 'Caxias do Sul - RS', 55: 'Santa Maria - RS',
  61: 'Brasília - DF', 62: 'Goiânia - GO', 63: 'Palmas - TO', 64: 'Rio Verde - GO', 65: 'Cuiabá - MT', 66: 'Rondonópolis - MT', 67: 'Campo Grande - MS',
  68: 'Rio Branco - AC', 69: 'Porto Velho - RO',
  71: 'Salvador - BA', 73: 'Ilhéus - BA', 74: 'Juazeiro - BA', 75: 'Feira de Santana - BA', 77: 'Vitória da Conquista - BA',
  79: 'Aracaju - SE', 81: 'Recife - PE', 82: 'Maceió - AL', 83: 'João Pessoa - PB', 84: 'Natal - RN', 85: 'Fortaleza - CE',
  86: 'Teresina - PI', 87: 'Petrolina - PE', 88: 'Juazeiro do Norte - CE', 89: 'Picos - PI',
  91: 'Belém - PA', 92: 'Manaus - AM', 93: 'Santarém - PA', 94: 'Marabá - PA', 95: 'Boa Vista - RR', 96: 'Macapá - AP', 97: 'Coari - AM', 98: 'São Luís - MA', 99: 'Imperatriz - MA'
}

const phoneTypeIcon = computed(() => {
  if (!phoneInfo.value) return 'fa-solid fa-phone'
  return phoneInfo.value.type === 'Celular' ? 'fa-solid fa-mobile-screen' : 'fa-solid fa-phone'
})

// Aplicar máscara ao telefone
function applyPhoneMask(value) {
  const numbers = value.replace(/\D/g, '')
  
  if (numbers.length <= 2) return numbers
  if (numbers.length <= 6) return `(${numbers.slice(0, 2)}) ${numbers.slice(2)}`
  if (numbers.length <= 10) return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 6)}-${numbers.slice(6)}`
  return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 7)}-${numbers.slice(7, 11)}`
}

// Remover máscara para obter apenas números
function removePhoneMask(value) {
  return value.replace(/\D/g, '')
}

// Identificar tipo de telefone
function identifyPhoneType(phone) {
  const numbers = removePhoneMask(phone)
  
  if (numbers.length < 10) return null
  
  const ddd = parseInt(numbers.slice(0, 2))
  const firstDigit = parseInt(numbers[2])
  
  // Verificar se DDD é válido
  if (!dddMap[ddd]) return null
  
  let type = 'Fixo'
  
  // Celular: 9xxxx-xxxx (11 dígitos) ou alguns casos especiais
  if (numbers.length === 11 && firstDigit === 9) {
    type = 'Celular'
  } else if (numbers.length === 10 && [6, 7, 8, 9].includes(firstDigit)) {
    // Alguns celulares antigos podem ter 10 dígitos
    type = 'Celular'
  }
  
  return {
    type,
    ddd: ddd.toString().padStart(2, '0'),
    region: dddMap[ddd],
    number: numbers,
    formatted: applyPhoneMask(numbers)
  }
}

// Validação completa do telefone
async function validatePhone(value) {
  if (!value.trim()) {
    isValid.value = null
    errorMessage.value = ''
    phoneInfo.value = null
    validationChecks.value = { format: false, ddd: false, type: false }
    return null
  }

  isValidating.value = true
  
  await new Promise(resolve => setTimeout(resolve, 200))
  
  const numbers = removePhoneMask(value)
  
  // Validação de formato (10 ou 11 dígitos)
  const formatValid = /^\d{10,11}$/.test(numbers)
  validationChecks.value.format = formatValid
  
  if (!formatValid) {
    isValid.value = false
    errorMessage.value = 'Telefone deve ter 10 ou 11 dígitos'
    phoneInfo.value = null
    validationChecks.value.ddd = false
    validationChecks.value.type = false
    isValidating.value = false
    emit('validation', { valid: false, phone: numbers, checks: validationChecks.value })
    return false
  }

  // Identificar tipo e validar DDD
  const info = identifyPhoneType(numbers)
  
  if (!info) {
    isValid.value = false
    errorMessage.value = 'DDD inválido'
    phoneInfo.value = null
    validationChecks.value.ddd = false
    validationChecks.value.type = false
  } else {
    isValid.value = true
    errorMessage.value = ''
    phoneInfo.value = info
    validationChecks.value.ddd = true
    validationChecks.value.type = true
  }
  
  isValidating.value = false
  emit('validation', { 
    valid: isValid.value, 
    phone: numbers, 
    info: phoneInfo.value,
    checks: validationChecks.value 
  })
  
  return isValid.value
}

function handleInput(event) {
  const value = event.target.value
  const masked = applyPhoneMask(value)
  maskedValue.value = masked
  
  const cleanValue = removePhoneMask(masked)
  emit('update:modelValue', cleanValue)
  
  if (isValid.value !== null) {
    isValid.value = null
    errorMessage.value = ''
    phoneInfo.value = null
  }
}

function handleBlur() {
  validatePhone(maskedValue.value)
}

function handleFocus() {
  errorMessage.value = ''
}

// Watch para mudanças externas no modelValue
watch(() => props.modelValue, (newValue) => {
  if (newValue !== removePhoneMask(maskedValue.value)) {
    maskedValue.value = applyPhoneMask(newValue || '')
  }
}, { immediate: true })
</script>