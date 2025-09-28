<script setup lang="ts">
import { onMounted } from 'vue'
import { navigateTo } from '#imports'

definePageMeta({ layout: false })

onMounted(() => {
  navigateTo('/', { replace: true })
})
</script>

<template></template>
