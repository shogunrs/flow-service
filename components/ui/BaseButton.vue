<template>
  <component
    :is="tag"
    :type="tag === 'button' ? type : undefined"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="handleClick"
  >
    <Icon
      v-if="loading"
      name="mdi:loading"
      class="animate-spin"
      :size="iconSize"
    />
    <Icon
      v-else-if="icon && iconPosition === 'left'"
      :name="icon"
      :size="iconSize"
    />
    
    <span v-if="$slots.default" :class="textClasses">
      <slot />
    </span>
    
    <Icon
      v-if="!loading && icon && iconPosition === 'right'"
      :name="icon"
      :size="iconSize"
    />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'ghost' | 'link'
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  type?: 'button' | 'submit' | 'reset'
  tag?: 'button' | 'a' | 'nuxt-link' | 'router-link'
  disabled?: boolean
  loading?: boolean
  icon?: string
  iconPosition?: 'left' | 'right'
  iconOnly?: boolean
  rounded?: boolean
  block?: boolean
  outline?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  tag: 'button',
  disabled: false,
  loading: false,
  iconPosition: 'left',
  iconOnly: false,
  rounded: false,
  block: false,
  outline: false
})

const emit = defineEmits<{
  click: [event: Event]
}>()

const handleClick = (event: Event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}

const buttonClasses = computed(() => {
  const classes = [
    'inline-flex items-center justify-center font-medium transition-all duration-200',
    'focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-900',
    'disabled:opacity-50 disabled:cursor-not-allowed'
  ]

  // Size classes
  const sizeClasses = {
    xs: props.iconOnly ? 'p-1' : 'px-2 py-1 text-xs',
    sm: props.iconOnly ? 'p-1.5' : 'px-3 py-1.5 text-sm',
    md: props.iconOnly ? 'p-2' : 'px-4 py-2 text-sm',
    lg: props.iconOnly ? 'p-2.5' : 'px-5 py-2.5 text-base',
    xl: props.iconOnly ? 'p-3' : 'px-6 py-3 text-lg'
  }
  classes.push(sizeClasses[props.size])

  // Rounded classes
  if (props.rounded) {
    classes.push('rounded-full')
  } else {
    const roundedClasses = {
      xs: 'rounded',
      sm: 'rounded-md',
      md: 'rounded-lg',
      lg: 'rounded-lg',
      xl: 'rounded-xl'
    }
    classes.push(roundedClasses[props.size])
  }

  // Block class
  if (props.block) {
    classes.push('w-full')
  }

  // Variant classes
  if (props.outline) {
    const outlineVariantClasses = {
      primary: 'border-2 border-orange-500 text-orange-500 hover:bg-orange-500 hover:text-white focus:ring-orange-500',
      secondary: 'border-2 border-gray-500 text-gray-500 hover:bg-gray-500 hover:text-white focus:ring-gray-500',
      success: 'border-2 border-green-500 text-green-500 hover:bg-green-500 hover:text-white focus:ring-green-500',
      danger: 'border-2 border-red-500 text-red-500 hover:bg-red-500 hover:text-white focus:ring-red-500',
      warning: 'border-2 border-yellow-500 text-yellow-500 hover:bg-yellow-500 hover:text-white focus:ring-yellow-500',
      info: 'border-2 border-blue-500 text-blue-500 hover:bg-blue-500 hover:text-white focus:ring-blue-500',
      ghost: 'border-2 border-transparent text-gray-400 hover:text-white hover:bg-gray-700 focus:ring-gray-500',
      link: 'border-2 border-transparent text-orange-500 hover:text-orange-400 focus:ring-orange-500'
    }
    classes.push(outlineVariantClasses[props.variant])
  } else {
    const variantClasses = {
      primary: 'bg-orange-500 text-white hover:bg-orange-600 focus:ring-orange-500',
      secondary: 'bg-gray-600 text-white hover:bg-gray-700 focus:ring-gray-500',
      success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
      danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
      warning: 'bg-yellow-600 text-white hover:bg-yellow-700 focus:ring-yellow-500',
      info: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
      ghost: 'bg-transparent text-gray-400 hover:text-white hover:bg-gray-700 focus:ring-gray-500',
      link: 'bg-transparent text-orange-500 hover:text-orange-400 focus:ring-orange-500'
    }
    classes.push(variantClasses[props.variant])
  }

  return classes.join(' ')
})

const textClasses = computed(() => {
  const classes: string[] = []
  
  if (props.icon && !props.iconOnly) {
    if (props.iconPosition === 'left') {
      classes.push('ml-2')
    } else {
      classes.push('mr-2')
    }
  }
  
  return classes.join(' ')
})

const iconSize = computed(() => {
  const sizes = {
    xs: '14',
    sm: '16',
    md: '18',
    lg: '20',
    xl: '24'
  }
  return sizes[props.size]
})
</script>