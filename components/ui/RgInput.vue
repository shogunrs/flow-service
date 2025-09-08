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
        :placeholder="placeholder || '00.000.000-0'"
        :required="required"
        :disabled="disabled"
        class="input-field input-field--xs"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        maxlength="12"
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
          <div :class="validationChecks.length ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.length"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Comprimento adequado
          </div>
          <div :class="validationChecks.pattern ? 'text-green-400' : 'text-red-400'">
            <i class="fa-solid fa-check mr-1" v-if="validationChecks.pattern"></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Padrão válido
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

const inputId = computed(() => `rg-${Math.random().toString(36).slice(2, 8)}`)
const maskedValue = ref('')
const isValid = ref(null)
const isValidating = ref(false)
const errorMessage = ref('')
const validationChecks = ref({
  format: false,
  length: false,
  pattern: false
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

// Aplicar máscara ao RG (formato SP: 00.000.000-0)
function applyRgMask(value) {
  // Remove tudo que não é número ou letra (para o dígito verificador)
  const clean = value.replace(/[^\dA-Za-z]/g, '').toUpperCase()
  
  if (clean.length <= 2) return clean
  if (clean.length <= 5) return `${clean.slice(0, 2)}.${clean.slice(2)}`
  if (clean.length <= 8) return `${clean.slice(0, 2)}.${clean.slice(2, 5)}.${clean.slice(5)}`
  return `${clean.slice(0, 2)}.${clean.slice(2, 5)}.${clean.slice(5, 8)}-${clean.slice(8, 9)}`
}

// Remover máscara para obter apenas números e letras
function removeRgMask(value) {
  return value.replace(/[^\dA-Za-z]/g, '').toUpperCase()
}

// Validação do RG
function validateRg(value) {
  const clean = removeRgMask(value)
  
  // Verifica formato básico (8 ou 9 caracteres, último pode ser letra)
  const formatValid = /^[0-9]{8}[0-9A-Z]?$/.test(clean)
  
  // Verifica comprimento (8-9 caracteres)
  const lengthValid = clean.length >= 7 && clean.length <= 9
  
  // Verifica se não são todos números iguais
  const patternValid = !/^(\d)\1+$/.test(clean) && clean !== '000000000'
  
  return {
    format: formatValid,
    length: lengthValid,
    pattern: patternValid
  }
}

// Validação completa
async function validateRgComplete(value) {
  if (!value.trim()) {
    isValid.value = null
    errorMessage.value = ''
    validationChecks.value = { format: false, length: false, pattern: false }
    return null
  }

  isValidating.value = true
  
  await new Promise(resolve => setTimeout(resolve, 200))
  
  const clean = removeRgMask(value)
  const checks = validateRg(value)
  validationChecks.value = checks
  
  const allValid = checks.format && checks.length && checks.pattern
  
  if (!checks.length) {
    isValid.value = false
    errorMessage.value = 'RG deve ter entre 7 e 9 caracteres'
  } else if (!checks.format) {
    isValid.value = false
    errorMessage.value = 'Formato de RG inválido'
  } else if (!checks.pattern) {
    isValid.value = false
    errorMessage.value = 'RG não pode ter todos os dígitos iguais'
  } else {
    isValid.value = true
    errorMessage.value = ''
  }
  
  isValidating.value = false
  emit('validation', { valid: allValid, rg: clean, checks })
  return allValid
}

function handleInput(event) {
  const value = event.target.value
  const masked = applyRgMask(value)
  maskedValue.value = masked
  
  const cleanValue = removeRgMask(masked)
  emit('update:modelValue', cleanValue)
  
  if (isValid.value !== null) {
    isValid.value = null
    errorMessage.value = ''
  }
}

function handleBlur() {
  validateRgComplete(maskedValue.value)
}

function handleFocus() {
  errorMessage.value = ''
}

// Watch para mudanças externas no modelValue
watch(() => props.modelValue, (newValue) => {
  if (newValue !== removeRgMask(maskedValue.value)) {
    maskedValue.value = applyRgMask(newValue || '')
  }
}, { immediate: true })
</script>