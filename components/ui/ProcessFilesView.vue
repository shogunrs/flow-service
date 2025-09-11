<template>
  <BaseModal 
    :model-value="show" 
    title="Arquivos do Processo"
    container-class="w-[90vw] max-w-4xl rounded-xl"
    @close="$emit('close')"
  >
    <div class="space-y-4">
      <!-- Header com informações -->
      <div class="flex items-center justify-between bg-slate-900/50 rounded-lg p-3">
        <div>
          <h3 class="text-sm font-medium text-slate-200">Processo: {{ processKey }}</h3>
          <p class="text-xs text-slate-400 mt-1">
            {{ files.length }} arquivo{{ files.length !== 1 ? 's' : '' }} encontrado{{ files.length !== 1 ? 's' : '' }}
          </p>
        </div>
        <button
          @click="refresh"
          :disabled="loading"
          class="px-3 py-1 text-xs bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white rounded-md transition-colors flex items-center gap-2"
        >
          <i :class="['fa-solid fa-refresh', { 'animate-spin': loading }]"></i>
          Atualizar
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="loading && files.length === 0" class="flex items-center justify-center py-8">
        <div class="flex items-center gap-2 text-slate-400">
          <i class="fa-solid fa-spinner animate-spin"></i>
          <span class="text-sm">Carregando arquivos...</span>
        </div>
      </div>

      <!-- Error state -->
      <div v-if="error" class="bg-red-900/20 border border-red-700/50 rounded-lg p-3">
        <div class="flex items-center gap-2 text-red-400">
          <i class="fa-solid fa-circle-exclamation"></i>
          <span class="text-sm">{{ error }}</span>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!loading && files.length === 0 && !error" class="text-center py-8">
        <div class="text-slate-500 mb-2">
          <i class="fa-solid fa-folder-open text-4xl"></i>
        </div>
        <p class="text-slate-400 text-sm">Nenhum arquivo encontrado neste processo</p>
      </div>

      <!-- Files list -->
      <div v-if="files.length > 0" class="space-y-2 max-h-96 overflow-y-auto">
        <div
          v-for="file in files"
          :key="file.id"
          class="bg-slate-800/50 rounded-lg p-3 hover:bg-slate-800/70 transition-colors"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <!-- File icon -->
              <div class="flex-shrink-0">
                <i :class="getFileIcon(file.contentType)"></i>
              </div>

              <!-- File info -->
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <h4 class="text-sm font-medium text-slate-200 truncate">
                    {{ file.originalName }}
                  </h4>
                  <span class="text-xs text-slate-500 px-2 py-0.5 bg-slate-700 rounded">
                    {{ formatFileSize(file.fileSize) }}
                  </span>
                </div>
                
                <div class="flex items-center gap-4 mt-1 text-xs text-slate-400">
                  <span>
                    <i class="fa-solid fa-clock w-3"></i>
                    {{ formatDate(file.uploadedAt) }}
                  </span>
                  
                  <span v-if="file.stageId" class="flex items-center gap-1">
                    <i class="fa-solid fa-layer-group w-3"></i>
                    Etapa
                  </span>
                  
                  <span v-if="file.proposalId" class="flex items-center gap-1">
                    <i class="fa-solid fa-file-contract w-3"></i>
                    Proposta
                  </span>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-2 flex-shrink-0">
              <button
                @click="downloadFile(file)"
                :disabled="loading"
                class="px-3 py-1 text-xs bg-green-600 hover:bg-green-700 disabled:bg-slate-600 text-white rounded-md transition-colors flex items-center gap-1"
                :title="`Baixar ${file.originalName}`"
              >
                <i class="fa-solid fa-download"></i>
                <span class="hidden sm:inline">Baixar</span>
              </button>
              
              <button
                @click="viewFileDetails(file)"
                class="px-3 py-1 text-xs bg-slate-600 hover:bg-slate-700 text-white rounded-md transition-colors flex items-center gap-1"
                :title="`Ver detalhes de ${file.originalName}`"
              >
                <i class="fa-solid fa-info"></i>
                <span class="hidden sm:inline">Detalhes</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- File details modal -->
    <BaseModal 
      :model-value="showDetails" 
      title="Detalhes do Arquivo"
      container-class="w-[90vw] max-w-lg rounded-xl"
      @close="closeDetails"
    >
      <div v-if="selectedFile" class="space-y-4">
        <div class="bg-slate-900/50 rounded-lg p-3">
          <div class="flex items-center gap-3 mb-3">
            <i :class="getFileIcon(selectedFile.contentType)" class="text-2xl"></i>
            <h3 class="text-sm font-medium text-slate-200">{{ selectedFile.originalName }}</h3>
          </div>
          
          <div class="grid grid-cols-1 gap-3 text-xs">
            <div class="flex justify-between">
              <span class="text-slate-400">Tamanho:</span>
              <span class="text-slate-200">{{ formatFileSize(selectedFile.fileSize) }}</span>
            </div>
            
            <div class="flex justify-between">
              <span class="text-slate-400">Tipo:</span>
              <span class="text-slate-200">{{ selectedFile.contentType }}</span>
            </div>
            
            <div class="flex justify-between">
              <span class="text-slate-400">Upload:</span>
              <span class="text-slate-200">{{ formatDate(selectedFile.uploadedAt) }}</span>
            </div>
            
            <div v-if="selectedFile.stageId" class="flex justify-between">
              <span class="text-slate-400">Etapa:</span>
              <span class="text-slate-200">{{ selectedFile.stageId }}</span>
            </div>
            
            <div v-if="selectedFile.proposalId" class="flex justify-between">
              <span class="text-slate-400">Proposta:</span>
              <span class="text-slate-200">{{ selectedFile.proposalId }}</span>
            </div>
            
            <div class="flex justify-between">
              <span class="text-slate-400">Chave do objeto:</span>
              <span class="text-slate-200 font-mono text-xs break-all">{{ selectedFile.objectKey }}</span>
            </div>
          </div>
        </div>
        
        <div class="flex gap-2">
          <button
            @click="downloadFile(selectedFile)"
            :disabled="loading"
            class="flex-1 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-slate-600 text-white rounded-lg transition-colors flex items-center justify-center gap-2"
          >
            <i class="fa-solid fa-download"></i>
            Baixar Arquivo
          </button>
        </div>
      </div>
    </BaseModal>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import { useProcessFiles } from '~/composables/useProcessFiles'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  processKey: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close'])

const {
  files,
  loading,
  error,
  loadProcessFiles,
  downloadFile,
  formatFileSize,
  getFileIcon
} = useProcessFiles()

const showDetails = ref(false)
const selectedFile = ref(null)

// Load files when modal opens
watch(() => props.show, (isOpen) => {
  if (isOpen && props.processKey) {
    loadProcessFiles(props.processKey)
  }
})

const refresh = () => {
  if (props.processKey) {
    loadProcessFiles(props.processKey)
  }
}

const viewFileDetails = (file) => {
  selectedFile.value = file
  showDetails.value = true
}

const closeDetails = () => {
  showDetails.value = false
  selectedFile.value = null
}

const formatDate = (dateString) => {
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'Data inválida'
  }
}
</script>