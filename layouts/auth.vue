<template>
  <div class="min-h-screen">
    <slot />
    <ApiLoadingOverlay />
    <ToastContainer position="bottom-center" />
  </div>
</template>

<script setup>
import ApiLoadingOverlay from '~/components/ui/ApiLoadingOverlay.vue'
import ToastContainer from '~/components/ui/ToastContainer.vue'
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: "Inter", sans-serif;
}
</style>