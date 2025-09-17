<template>
  <div class="group-wrapper">
    <label v-if="label" :for="id" class="group-label">{{ label }}</label>
    <div class="group-row" :class="rowClasses">
      <input
        :id="id"
        class="group-input"
        :class="inputClasses"
        :type="type"
        :placeholder="placeholder"
        :disabled="disabled"
        v-model="model"
      />
      <button
        type="button"
        class="group-btn"
        :class="btnClasses"
        :disabled="disabled || loading"
        @click="$emit('append-click')"
      >
        <i v-if="loading" class="fa-solid fa-spinner fa-spin"></i>
        <span v-else>{{ appendText }}</span>
      </button>
    </div>
    <p v-if="errorMessage" class="group-error">{{ errorMessage }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed, useId } from 'vue'

interface Props {
  modelValue?: string | number
  label?: string
  placeholder?: string
  type?: 'text' | 'number' | 'search'
  size?: 'xs' | 'sm'
  appendText?: string
  loading?: boolean
  disabled?: boolean
  errorMessage?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  type: 'text',
  size: 'xs',
  appendText: 'OK',
  loading: false,
  disabled: false,
  errorMessage: ''
})

defineEmits<{ 'update:modelValue': [val: string | number]; 'append-click': [] }>()

const id = useId()
const model = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const rowClasses = computed(() => [props.size === 'xs' ? 'h-7' : 'h-8'])
const inputClasses = computed(() => [props.size === 'xs' ? 'h-7 text-[12px] px-2 leading-tight' : 'h-8 text-sm px-2'])
const btnClasses = computed(() => [props.size === 'xs' ? 'h-7 text-[12px] px-2' : 'h-8 text-sm px-3'])
</script>

<style scoped>
.group-label { @apply text-[10px] text-slate-300; }
.group-row { @apply flex items-center gap-2 mt-1; }
.group-input { @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm outline-none text-slate-200 placeholder-gray-400 focus:ring-[0.5px] focus:ring-indigo-500/60 focus:border-indigo-400/50 transition; }
.group-btn { @apply rounded-sm bg-slate-700 hover:bg-slate-600 text-slate-200 transition disabled:opacity-60 disabled:cursor-not-allowed; }
.group-error { @apply text-[11px] text-red-400 mt-0.5; }
</style>
