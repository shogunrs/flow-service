<template>
  <div class="relative">
    <button
      @click="handleClick"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
      :class="[
        'w-full flex items-center gap-3 px-3 py-1.5 text-sm rounded-2xl transition-all duration-200 relative overflow-hidden border border-transparent',
        isActive
          ? 'text-slate-100'
          : 'text-slate-300 hover:text-white hover:bg-white/5 hover:border-white/10',
        collapsed ? 'justify-center' : ''
      ]"
      :title="collapsed ? item.label : item.description"
    >
      <i :class="[item.icon, 'w-5 flex-shrink-0', isActive ? 'text-slate-100' : 'text-slate-300']"></i>
      <span v-show="!collapsed" class="flex-1 text-left truncate transition-opacity duration-300">{{ item.label }}</span>

      <!-- Active indicator -->
      <i
        v-if="hasChildren && !collapsed"
        :class="[
          'fa-solid text-xs transition-transform',
          isExpanded ? 'fa-chevron-down' : 'fa-chevron-right'
        ]"
      ></i>

      <span
        v-if="isChild && isActive"
        class="pointer-events-none absolute right-3 top-2.5 w-1.5 h-1.5 rounded-full bg-white/80 shadow-[0_0_12px_rgba(255,255,255,0.55)]"
      ></span>

      <!-- Badge/Count (if needed) -->
      <span
        v-if="item.badge && !collapsed"
        class="bg-red-500 text-white text-xs px-1.5 py-0.5 rounded-full"
      >
        {{ item.badge }}
      </span>

      <!-- Submenu arrow (if has children) -->
    </button>

    <!-- Tooltip for collapsed mode -->
    <div
      v-if="collapsed && showTooltip"
      class="absolute left-full top-1/2 transform -translate-y-1/2 ml-2 z-50"
    >
      <div class="bg-slate-800 text-white text-sm px-2 py-1 rounded shadow-lg whitespace-nowrap">
        {{ item.label }}
        <div class="absolute right-full top-1/2 transform -translate-y-1/2 w-0 h-0 border-t-4 border-b-4 border-r-4 border-transparent border-r-slate-800"></div>
      </div>
    </div>

    <!-- Submenu items (inline when expanded) -->
    <div
      v-if="hasChildren && !collapsed"
      :class="[
        'mt-1 ml-8 space-y-1 overflow-hidden transition-all duration-300',
        isExpanded ? 'max-h-96 opacity-100' : 'max-h-0 opacity-0'
      ]"
    >
      <SidebarItem
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :is-active="childIsActive(child.path)"
        :collapsed="false"
        :is-child="true"
        @click="handleChildClick(child)"
      />
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from '#imports'

// Props
const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  },
  collapsed: {
    type: Boolean,
    default: false
  },
  isChild: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['click', 'fetch-esteira'])

// Composables
const route = useRoute()


// State
const showTooltip = ref(false)
const isExpanded = ref(false)
let tooltipTimeout = null

// Computed
const hasChildren = computed(() => {
  return props.item.children && props.item.children.length > 0
})


// Methods
function handleClick() {
  // Se for a Esteira, fazer fetch dos processos primeiro
  if (props.item.path === '/esteira') {
    emit('fetch-esteira')
  }

  // Se tem onClick personalizado, executar
  if (props.item.onClick && typeof props.item.onClick === 'function') {
    props.item.onClick()
    return
  }

  if (hasChildren.value && !props.collapsed) {
    isExpanded.value = !isExpanded.value
  } else {
    emit('click', props.item)
  }
}

function handleChildClick(child) {
  emit('click', child)
}

function childIsActive(path) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

function handleMouseEnter() {
  if (props.collapsed) {
    tooltipTimeout = setTimeout(() => {
      showTooltip.value = true
    }, 500)
  }
}

function handleMouseLeave() {
  if (tooltipTimeout) {
    clearTimeout(tooltipTimeout)
    tooltipTimeout = null
  }
  showTooltip.value = false
}

// Auto-expand if child is active
watch(() => props.isActive, (newVal) => {
  if (newVal && hasChildren.value) {
    isExpanded.value = true
  }
})

// Auto-expand if any child is active
watch(() => route.path, (newPath) => {
  if (hasChildren.value) {
    const hasActiveChild = props.item.children.some(child => {
      if (child.path === '/') {
        return newPath === '/'
      }
      return newPath.startsWith(child.path)
    })

    if (hasActiveChild) {
      isExpanded.value = true
    }
  }
}, { immediate: true })


</script>

<style scoped>
/* Additional hover effects */
.group:hover .group-hover\\:translate-x-1 {
  transform: translateX(0.25rem);
}

/* Custom scrollbar for submenu */
.overflow-hidden {
  scrollbar-width: thin;
  scrollbar-color: #4b5563 #1f2937;
}

/* Tooltip and dropdown animations */
.tooltip-arrow::before {
  content: '';
  position: absolute;
  right: 100%;
  top: 50%;
  transform: translateY(-50%);
  border: 4px solid transparent;
  border-right-color: #1f2937;
}


/* Ensure proper z-index stacking */
.z-50 {
  z-index: 50;
}

/* Custom animations */
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-fade-in {
  animation: fade-in 0.2s ease-in-out;
}

/* Micro interactions */
.hover\\:scale-105:hover {
  transform: scale(1.05);
}

/* Better tooltip positioning */
.tooltip-container {
  position: relative;
}

.tooltip-container::after {
  content: '';
  position: absolute;
  right: 100%;
  top: 50%;
  transform: translateY(-50%);
  border: 6px solid transparent;
  border-right-color: #1f2937;
}
</style>
