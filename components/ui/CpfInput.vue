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
        :placeholder="placeholder || '000.000.000-00'"
        :required="required"
        :disabled="disabled"
        class="input-field input-field--xs"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        maxlength="14"
      />
      
      <div class="input-suffix">
        <i v-if="isValid === true" class="fa-solid fa-check text-green-500 input-icon"></i>
        <i v-else-if="isValid === false" class="fa-solid fa-times text-red-500 input-icon"></i>
        <i v-else-if="isValidating" class="fa-solid fa-spinner fa-spin text-slate-400 input-icon"></i>
      </div>
    </div>
    
    <div v-if="errorMessage || (showValidationInfo && maskedValue && isValid !== null)" class="input-help">
      <p v-if="errorMessage" class="input-error">
        {{ errorMessage }}
      </p>
      <div v-if="showValidationInfo && maskedValue && isValid !== null" class="text-[10px] text-slate-400 mt-1">
        <div class="space-y-0.5">
          <div :class="validationChecks.format ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.format"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Formato válido
          </div>
          <div :class="validationChecks.math ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.math"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Dígitos verificadores
          </div>
          <div :class="validationChecks.antifraude ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.antifraude"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Validação antifraude
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

const inputId = computed(() => `cpf-${Math.random().toString(36).slice(2, 8)}`)
const maskedValue = ref('')
const isValid = ref(null)
const isValidating = ref(false)
const errorMessage = ref('')
const validationChecks = ref({
  format: false,
  math: false,
  antifraude: false
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

// Aplicar máscara ao CPF
function applyCpfMask(value) {
  const numbers = value.replace(/\D/g, '')
  if (numbers.length <= 3) return numbers
  if (numbers.length <= 6) return `${numbers.slice(0, 3)}.${numbers.slice(3)}`
  if (numbers.length <= 9) return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6)}`
  return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6, 9)}-${numbers.slice(9, 11)}`
}

// Remover máscara para obter apenas números
function removeCpfMask(value) {
  return value.replace(/\D/g, '')
}

// Validação matemática do CPF
function validateCpfMath(cpf) {
  const numbers = cpf.replace(/\D/g, '')
  if (numbers.length !== 11) return false

  // Verifica se todos os dígitos são iguais (caso comum de fraude)
  if (/^(\d)\1{10}$/.test(numbers)) return false

  // Cálculo do primeiro dígito verificador
  let sum = 0
  for (let i = 0; i < 9; i++) {
    sum += parseInt(numbers[i]) * (10 - i)
  }
  let remainder = sum % 11
  const firstDigit = remainder < 2 ? 0 : 11 - remainder

  if (parseInt(numbers[9]) !== firstDigit) return false

  // Cálculo do segundo dígito verificador
  sum = 0
  for (let i = 0; i < 10; i++) {
    sum += parseInt(numbers[i]) * (11 - i)
  }
  remainder = sum % 11
  const secondDigit = remainder < 2 ? 0 : 11 - remainder

  return parseInt(numbers[10]) === secondDigit
}

// Validação antifraude
function validateAntiFraude(cpf) {
  const numbers = cpf.replace(/\D/g, '')
  if (numbers.length !== 11) return false

  // Lista de CPFs conhecidos como inválidos/teste
  const blacklist = [
    '00000000000', '11111111111', '22222222222', '33333333333',
    '44444444444', '55555555555', '66666666666', '77777777777',
    '88888888888', '99999999999'
  ]
  
  if (blacklist.includes(numbers)) return false

  // Padrões sequenciais
  if (/^(\d{3})\1{2}\d{2}$/.test(numbers)) return false // 123123123XX
  if (/^123456789\d{2}$/.test(numbers)) return false // 123456789XX
  if (/^987654321\d{2}$/.test(numbers)) return false // 987654321XX

  // Verifica se não é uma sequência numérica crescente ou decrescente
  let isSequential = true
  for (let i = 1; i < 11; i++) {
    if (parseInt(numbers[i]) !== parseInt(numbers[i-1]) + 1) {
      isSequential = false
      break
    }
  }
  if (isSequential) return false

  isSequential = true
  for (let i = 1; i < 11; i++) {
    if (parseInt(numbers[i]) !== parseInt(numbers[i-1]) - 1) {
      isSequential = false
      break
    }
  }
  if (isSequential) return false

  return true
}

// Validação completa
async function validateCpf(value) {
  if (!value.trim()) {
    isValid.value = null
    errorMessage.value = ''
    validationChecks.value = { format: false, math: false, antifraude: false }
    return null
  }

  isValidating.value = true
  
  // Simula delay de validação para melhor UX
  await new Promise(resolve => setTimeout(resolve, 300))
  
  const numbers = removeCpfMask(value)
  
  // Validação de formato
  const formatValid = /^\d{11}$/.test(numbers)
  validationChecks.value.format = formatValid
  
  if (!formatValid) {
    isValid.value = false
    errorMessage.value = 'CPF deve ter 11 dígitos'
    isValidating.value = false
    emit('validation', { valid: false, cpf: numbers, checks: validationChecks.value })
    return false
  }

  // Validação matemática
  const mathValid = validateCpfMath(numbers)
  validationChecks.value.math = mathValid
  
  if (!mathValid) {
    isValid.value = false
    errorMessage.value = 'CPF com dígitos verificadores inválidos'
    isValidating.value = false
    emit('validation', { valid: false, cpf: numbers, checks: validationChecks.value })
    return false
  }

  // Validação antifraude
  const antiFraudeValid = validateAntiFraude(numbers)
  validationChecks.value.antifraude = antiFraudeValid
  
  if (!antiFraudeValid) {
    isValid.value = false
    errorMessage.value = 'CPF identificado como inválido ou teste'
    isValidating.value = false
    emit('validation', { valid: false, cpf: numbers, checks: validationChecks.value })
    return false
  }

  // Tudo válido
  isValid.value = true
  errorMessage.value = ''
  isValidating.value = false
  emit('validation', { valid: true, cpf: numbers, checks: validationChecks.value })
  return true
}

function handleInput(event) {
  const value = event.target.value
  const masked = applyCpfMask(value)
  maskedValue.value = masked
  
  const cleanValue = removeCpfMask(masked)
  emit('update:modelValue', cleanValue)
  
  // Reset validation state durante digitação
  if (isValid.value !== null) {
    isValid.value = null
    errorMessage.value = ''
  }
}

function handleBlur() {
  validateCpf(maskedValue.value)
}

function handleFocus() {
  errorMessage.value = ''
}

// Watch para mudanças externas no modelValue
watch(() => props.modelValue, (newValue) => {
  if (newValue !== removeCpfMask(maskedValue.value)) {
    maskedValue.value = applyCpfMask(newValue || '')
  }
}, { immediate: true })
</script>

