import { ref } from 'vue'

// Estado global do modal
const isModalOpen = ref(false)
const modalData = ref({
  stages: [],
  stageFields: [],
  pipelineKey: '',
  isFinancial: false,
  onSave: null,
})

export function useNewRecordModal() {
  function openModal({ stages, stageFields, pipelineKey, isFinancial, onSave }) {
    modalData.value = {
      stages: stages || [],
      stageFields: stageFields || [],
      pipelineKey: pipelineKey || '',
      isFinancial: Boolean(isFinancial),
      onSave: onSave || (() => {}),
    }
    isModalOpen.value = true
  }

  function closeModal() {
    isModalOpen.value = false
    modalData.value = {
      stages: [],
      stageFields: [],
      pipelineKey: '',
      isFinancial: false,
      onSave: null,
    }
  }

  function handleSave(recordData) {
    if (modalData.value.onSave) {
      modalData.value.onSave(recordData)
    }
    closeModal()
  }

  return {
    isModalOpen,
    modalData,
    openModal,
    closeModal,
    handleSave
  }
}
