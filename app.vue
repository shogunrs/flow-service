<template>
  <NuxtPage />
  <ToastContainer position="bottom-center" />
  
  <!-- Modal global de novo registro -->
  <NewRecordModal
    v-model="isModalOpen"
    :stages="modalData.stages"
    :stage-fields="modalData.stageFields"
    :pipeline-key="modalData.pipelineKey"
    @save="handleSave"
    @close="closeModal"
  />
</template>

<script setup>
import ToastContainer from '~/components/ui/ToastContainer.vue'
import NewRecordModal from '~/components/ui/NewRecordModal.vue'
import { useNewRecordModal } from '~/composables/useNewRecordModal'

// Modal global de novo registro
const { isModalOpen, modalData, closeModal, handleSave } = useNewRecordModal()

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
