<template>
  <div class="bg-gray-800 rounded-lg p-6 border border-gray-700">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-gray-400 text-sm font-medium">{{ title }}</p>
        <p class="text-2xl font-bold text-white mt-1">{{ value }}</p>
        <div v-if="trend" class="flex items-center mt-2">
          <i 
            :class="[
              'fas text-sm mr-1',
              trend > 0 ? 'fa-arrow-up text-green-400' : 'fa-arrow-down text-red-400'
            ]"
          ></i>
          <span 
            :class="[
              'text-sm font-medium',
              trend > 0 ? 'text-green-400' : 'text-red-400'
            ]"
          >
            {{ Math.abs(trend) }}%
          </span>
        </div>
      </div>
      <div 
        :class="[
          'w-12 h-12 rounded-lg flex items-center justify-center',
          colorClasses
        ]"
      >
        <i :class="['fas text-xl', icon]"></i>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  value: string | number
  icon: string
  color: 'orange' | 'green' | 'blue' | 'purple' | 'red'
  trend?: number
}

const props = defineProps<Props>()

const colorClasses = computed(() => {
  const colors = {
    orange: 'bg-indigo-600/20 text-indigo-400',
    green: 'bg-green-500/20 text-green-400',
    blue: 'bg-blue-500/20 text-blue-400',
    purple: 'bg-purple-500/20 text-purple-400',
    red: 'bg-red-500/20 text-red-400'
  }
  return colors[props.color] || colors.orange
})
</script>