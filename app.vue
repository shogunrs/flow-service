<template>
  <NuxtLayout name="sidebar">
    <NuxtPage />
  </NuxtLayout>
  <ToastContainer position="bottom-center" />

  <!-- Modal global de novo registro -->
  <NewRecordModal
    v-model="isModalOpen"
    :stages="modalData.stages"
    :stage-fields="modalData.stageFields"
    :pipeline-key="modalData.pipelineKey"
    :is-financial-process="modalData.isFinancial"
    :lead-options="modalData.leadOptions"
    @save="handleSave"
    @close="closeModal"
  />
</template>

<script setup>
import ToastContainer from "~/components/ui/ToastContainer.vue";
import NewRecordModal from "~/components/ui/NewRecordModal.vue";
import { useNewRecordModal } from "~/composables/useNewRecordModal";

// Modal global de novo registro
const { isModalOpen, modalData, closeModal, handleSave } = useNewRecordModal();

// Mantém apenas Font Awesome via CDN para ícones
useHead({
  link: [
    {
      rel: "stylesheet",
      href: "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css",
    },
    // Commercial font: Inter (clean, readable)
    {
      rel: "preconnect",
      href: "https://fonts.googleapis.com",
    },
    {
      rel: "preconnect",
      href: "https://fonts.gstatic.com",
      crossorigin: "",
    },
    {
      rel: "stylesheet",
      href: "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Press+Start+2P&display=swap",
    },
  ],
});
</script>
