<template>
  <div class="input-wrapper" :class="wrapperClasses">
    <label v-if="label" :for="inputId" class="input-label">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="input-container" :class="containerClasses">
      <div v-if="$slots.prefix || prefixIcon" class="input-prefix">
        <slot name="prefix">
          <Icon v-if="prefixIcon" :name="prefixIcon" class="input-icon" />
        </slot>
      </div>
      
      <input
        :id="inputId"
        ref="inputRef"
        v-model="inputValue"
        :type="type"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :autocomplete="autocomplete"
        :maxlength="maxlength"
        :minlength="minlength"
        :min="min"
        :max="max"
        :step="step"
        :pattern="pattern"
        class="input-field"
        :class="inputClasses"
        @input="handleInput"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
      />
      
      <div v-if="$slots.suffix || suffixIcon || showClearButton" class="input-suffix">
        <button
          v-if="showClearButton"
          type="button"
          class="input-clear"
          @click="clearInput"
        >
          <Icon name="x" class="input-icon" />
        </button>
        <slot name="suffix">
          <Icon v-if="suffixIcon" :name="suffixIcon" class="input-icon" />
        </slot>
      </div>
    </div>
    
    <div v-if="helpText || errorMessage" class="input-help">
      <p v-if="errorMessage" class="input-error">
        {{ errorMessage }}
      </p>
      <p v-else-if="helpText" class="input-help-text">
        {{ helpText }}
      </p>
    </div>
    
    <div v-if="showCharCount && maxlength" class="input-char-count">
      {{ String(inputValue || '').length }}/{{ maxlength }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, useId } from 'vue'

interface Props {
  modelValue?: string | number
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url' | 'search' | 'date'
  label?: string
  placeholder?: string
  helpText?: string
  errorMessage?: string
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  size?: 'xs' | 'sm' | 'md' | 'lg'
  variant?: 'default' | 'filled' | 'outlined'
  state?: 'default' | 'error' | 'success' | 'warning'
  prefixIcon?: string
  suffixIcon?: string
  clearable?: boolean
  showCharCount?: boolean
  autocomplete?: string
  maxlength?: number
  minlength?: number
  min?: number | string
  max?: number | string
  step?: number | string
  pattern?: string
  mask?: 'none' | 'currency'
  locale?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  size: 'md',
  variant: 'default',
  state: 'default',
  clearable: false,
  showCharCount: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  input: [event: Event]
  change: [event: Event]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
  keydown: [event: KeyboardEvent]
  clear: []
}>()

const inputRef = ref<HTMLInputElement>()
const inputId = useId()
const isFocused = ref(false)

const inputValue = computed({
  get: () => props.modelValue ?? '',
  set: (value: string | number) => emit('update:modelValue', value)
})

const showClearButton = computed(() => {
  return props.clearable && inputValue.value && !props.disabled && !props.readonly
})

const wrapperClasses = computed(() => {
  const classes: string[] = []
  
  if (props.disabled) classes.push('input-wrapper--disabled')
  if (props.readonly) classes.push('input-wrapper--readonly')
  if (props.state !== 'default') classes.push(`input-wrapper--${props.state}`)
  
  return classes
})

const containerClasses = computed(() => {
  const classes: string[] = []
  
  classes.push(`input-container--${props.size}`)
  classes.push(`input-container--${props.variant}`)
  
  if (isFocused.value) classes.push('input-container--focused')
  if (props.disabled) classes.push('input-container--disabled')
  if (props.readonly) classes.push('input-container--readonly')
  if (props.state !== 'default') classes.push(`input-container--${props.state}`)
  
  return classes
})

const inputClasses = computed(() => {
  const classes: string[] = []
  
  classes.push(`input-field--${props.size}`)
  
  if (props.state !== 'default') classes.push(`input-field--${props.state}`)
  
  return classes
})

// Methods
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value: string | number = target.value

  if (props.mask === 'currency') {
    // Keep only digits
    const digits = String(value).replace(/\D/g, '')
    const cents = digits ? Number(digits) : 0
    const num = cents / 100
    const loc = props.locale || 'pt-BR'
    // Format like 1.234,56 (no currency symbol)
    value = num.toLocaleString(loc, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    target.value = String(value)
  } else if (props.type === 'number') {
    value = Number(value)
  }

  inputValue.value = value
  emit('input', event)
}

const handleChange = (event: Event) => {
  emit('change', event)
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  emit('blur', event)
}

const handleKeydown = (event: KeyboardEvent) => {
  emit('keydown', event)
}

const clearInput = () => {
  inputValue.value = ''
  emit('clear')
  inputRef.value?.focus()
}

// Expose methods
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  select: () => inputRef.value?.select()
})
</script>

<style scoped>
.input-wrapper {
  @apply w-full;
}

.input-label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.input-container {
  @apply relative flex items-center;
}

.input-container--sm {
  @apply h-8;
}
.input-container--xs {
  @apply h-7;
}

.input-container--md {
  @apply h-10;
}

.input-container--lg {
  @apply h-12;
}

.input-container--default {
  @apply border border-gray-300 rounded-md bg-white;
}

.input-container--filled {
  @apply bg-gray-50 border-0 rounded-md;
}

.input-container--outlined {
  @apply border-2 border-gray-300 rounded-md bg-transparent;
}

.input-container--focused {
  @apply ring-2 ring-blue-500 ring-opacity-50 border-blue-500;
}

.input-container--disabled {
  @apply bg-gray-100 border-gray-200 cursor-not-allowed;
}

.input-container--readonly {
  @apply bg-gray-50 border-gray-200;
}

.input-container--error {
  @apply border-red-500 ring-red-500;
}

.input-container--success {
  @apply border-green-500 ring-green-500;
}

.input-container--warning {
  @apply border-yellow-500 ring-yellow-500;
}

.input-field {
  @apply flex-1 border-0 bg-transparent outline-none placeholder-gray-400;
}

.input-field--sm {
  @apply text-sm px-3;
}
.input-field--xs {
  @apply text-[12px] px-2 leading-tight;
}

.input-field--md {
  @apply text-base px-3;
}

.input-field--lg {
  @apply text-lg px-4;
}

.input-field:disabled {
  @apply cursor-not-allowed text-gray-500;
}

.input-prefix,
.input-suffix {
  @apply flex items-center px-3 text-gray-500;
}

.input-icon {
  @apply w-4 h-4;
}

.input-clear {
  @apply p-1 text-gray-400 hover:text-gray-600 rounded transition-colors;
}

.input-help {
  @apply mt-1;
}

.input-error {
  @apply text-sm text-red-600;
}

.input-help-text {
  @apply text-sm text-gray-600;
}

.input-char-count {
  @apply text-xs text-gray-500 text-right mt-1;
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .input-label {
    @apply text-gray-300;
  }
  
  .input-container--default {
    @apply border-slate-600/60 bg-slate-700/70 rounded-sm;
  }
  
  .input-container--filled {
    @apply bg-gray-700;
  }
  
  .input-container--outlined {
    @apply border-gray-600;
  }
  
  .input-container--disabled {
    @apply bg-gray-700 border-gray-600;
  }
  
  .input-container--readonly {
    @apply bg-gray-700 border-gray-600;
  }
  
  .input-field {
    @apply text-white placeholder-gray-400;
  }
  
  .input-field:disabled {
    @apply text-gray-400;
  }
  
  .input-prefix,
  .input-suffix {
    @apply text-gray-400;
  }
  
  .input-help-text {
    @apply text-gray-400;
  }
  
  .input-char-count {
    @apply text-gray-400;
  }
}
</style>
