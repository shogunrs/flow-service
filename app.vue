<template>
  <NuxtPage />
  <ToastContainer position="top-right" />
</template>

<script setup>
import ToastContainer from '~/components/ui/ToastContainer.vue'

// Mantém apenas Font Awesome via CDN para ícones
useHead({
  link: [
    {
      rel: 'stylesheet',
      href: 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css'
    }
  ]
})
</script>
