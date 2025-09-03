<template>
  <div class="card" :class="cardClasses" @click="handleClick">
    <div v-if="$slots.header || title" class="card-header" :class="headerClasses">
      <slot name="header">
        <div class="card-title-wrapper">
          <h3 v-if="title" class="card-title">
            {{ title }}
          </h3>
          <p v-if="subtitle" class="card-subtitle">
            {{ subtitle }}
          </p>
        </div>
        <div v-if="$slots.actions" class="card-actions">
          <slot name="actions" />
        </div>
      </slot>
    </div>
    
    <div v-if="$slots.media" class="card-media">
      <slot name="media" />
    </div>
    
    <div class="card-body" :class="bodyClasses">
      <slot />
    </div>
    
    <div v-if="$slots.footer" class="card-footer" :class="footerClasses">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title?: string
  subtitle?: string
  variant?: 'default' | 'outlined' | 'elevated' | 'filled'
  size?: 'sm' | 'md' | 'lg'
  padding?: 'none' | 'sm' | 'md' | 'lg'
  rounded?: 'none' | 'sm' | 'md' | 'lg' | 'xl' | 'full'
  shadow?: 'none' | 'sm' | 'md' | 'lg' | 'xl'
  hover?: boolean
  clickable?: boolean
  loading?: boolean
  disabled?: boolean
  borderColor?: string
  backgroundColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'md',
  padding: 'md',
  rounded: 'md',
  shadow: 'sm',
  hover: false,
  clickable: false,
  loading: false,
  disabled: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const cardClasses = computed(() => {
  const classes: string[] = ['card-base']
  
  // Variant
  classes.push(`card--${props.variant}`)
  
  // Size
  classes.push(`card--${props.size}`)
  
  // Padding
  if (props.padding !== 'none') {
    classes.push(`card--padding-${props.padding}`)
  }
  
  // Rounded
  if (props.rounded !== 'none') {
    classes.push(`card--rounded-${props.rounded}`)
  }
  
  // Shadow
  if (props.shadow !== 'none') {
    classes.push(`card--shadow-${props.shadow}`)
  }
  
  // States
  if (props.hover) classes.push('card--hover')
  if (props.clickable) classes.push('card--clickable')
  if (props.loading) classes.push('card--loading')
  if (props.disabled) classes.push('card--disabled')
  
  return classes
})

const headerClasses = computed(() => {
  const classes: string[] = []
  
  if (props.padding !== 'none') {
    classes.push(`card-header--padding-${props.padding}`)
  }
  
  return classes
})

const bodyClasses = computed(() => {
  const classes: string[] = []
  
  if (props.padding !== 'none') {
    classes.push(`card-body--padding-${props.padding}`)
  }
  
  return classes
})

const footerClasses = computed(() => {
  const classes: string[] = []
  
  if (props.padding !== 'none') {
    classes.push(`card-footer--padding-${props.padding}`)
  }
  
  return classes
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.card-base {
  @apply bg-white border border-gray-200 transition-all duration-200;
}

/* Variants */
.card--default {
  @apply bg-white border-gray-200;
}

.card--outlined {
  @apply bg-white border-2 border-gray-300;
}

.card--elevated {
  @apply bg-white border-0;
}

.card--filled {
  @apply bg-gray-50 border-gray-200;
}

/* Sizes */
.card--sm {
  @apply max-w-sm;
}

.card--md {
  @apply max-w-md;
}

.card--lg {
  @apply max-w-lg;
}

/* Padding */
.card--padding-sm {
  @apply p-3;
}

.card--padding-md {
  @apply p-4;
}

.card--padding-lg {
  @apply p-6;
}

/* Rounded */
.card--rounded-sm {
  @apply rounded-sm;
}

.card--rounded-md {
  @apply rounded-md;
}

.card--rounded-lg {
  @apply rounded-lg;
}

.card--rounded-xl {
  @apply rounded-xl;
}

.card--rounded-full {
  @apply rounded-full;
}

/* Shadow */
.card--shadow-sm {
  @apply shadow-sm;
}

.card--shadow-md {
  @apply shadow-md;
}

.card--shadow-lg {
  @apply shadow-lg;
}

.card--shadow-xl {
  @apply shadow-xl;
}

/* States */
.card--hover:hover {
  @apply shadow-lg transform -translate-y-1;
}

.card--clickable {
  @apply cursor-pointer;
}

.card--clickable:hover {
  @apply shadow-md;
}

.card--loading {
  @apply opacity-60 pointer-events-none;
}

.card--disabled {
  @apply opacity-50 pointer-events-none cursor-not-allowed;
}

/* Header */
.card-header {
  @apply border-b border-gray-200 flex items-start justify-between;
}

.card-header--padding-sm {
  @apply px-3 py-2;
}

.card-header--padding-md {
  @apply px-4 py-3;
}

.card-header--padding-lg {
  @apply px-6 py-4;
}

.card-title-wrapper {
  @apply flex-1;
}

.card-title {
  @apply text-lg font-semibold text-gray-900 mb-1;
}

.card-subtitle {
  @apply text-sm text-gray-600;
}

.card-actions {
  @apply flex items-center space-x-2 ml-4;
}

/* Media */
.card-media {
  @apply overflow-hidden;
}

/* Body */
.card-body {
  @apply flex-1;
}

.card-body--padding-sm {
  @apply px-3 py-2;
}

.card-body--padding-md {
  @apply px-4 py-3;
}

.card-body--padding-lg {
  @apply px-6 py-4;
}

/* Footer */
.card-footer {
  @apply border-t border-gray-200;
}

.card-footer--padding-sm {
  @apply px-3 py-2;
}

.card-footer--padding-md {
  @apply px-4 py-3;
}

.card-footer--padding-lg {
  @apply px-6 py-4;
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .card--default {
    @apply bg-gray-800 border-gray-700;
  }
  
  .card--outlined {
    @apply bg-gray-800 border-gray-600;
  }
  
  .card--elevated {
    @apply bg-gray-800;
  }
  
  .card--filled {
    @apply bg-gray-700 border-gray-600;
  }
  
  .card-header {
    @apply border-gray-700;
  }
  
  .card-title {
    @apply text-white;
  }
  
  .card-subtitle {
    @apply text-gray-400;
  }
  
  .card-footer {
    @apply border-gray-700;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .card-base {
    @apply border-2 border-black;
  }
  
  .card-title {
    @apply text-black font-bold;
  }
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .card-base {
    @apply transition-none;
  }
  
  .card--hover:hover {
    @apply transform-none;
  }
}
</style>