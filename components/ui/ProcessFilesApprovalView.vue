<template>
  <BaseModal 
    :model-value="show" 
    title="Arquivos do Processo"
    container-class="w-[95vw] max-w-6xl rounded-xl"
    :z-index="80"
    @update:model-value="$emit('close')"
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

      <!-- Filters -->
      <div class="flex items-center gap-2 text-xs">
        <button
          @click="filterStatus = 'all'"
          :class="filterStatus === 'all' ? 'bg-indigo-600 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'"
          class="px-2 py-1 rounded-md transition-colors"
        >
          Todos ({{ files.length }})
        </button>
        <button
          @click="filterStatus = 'approved'"
          :class="filterStatus === 'approved' ? 'bg-green-500 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'"
          class="px-2 py-1 rounded-md transition-colors flex items-center gap-1"
        >
          <i class="fa-solid fa-check"></i>
          Aprovados ({{ approvedCount }})
        </button>
        <button
          @click="filterStatus = 'rejected'"
          :class="filterStatus === 'rejected' ? 'bg-red-500 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'"
          class="px-2 py-1 rounded-md transition-colors flex items-center gap-1"
        >
          <i class="fa-solid fa-times"></i>
          Reprovados ({{ rejectedCount }})
        </button>
        <button
          @click="filterStatus = 'pending'"
          :class="filterStatus === 'pending' ? 'bg-yellow-500 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'"
          class="px-2 py-1 rounded-md transition-colors flex items-center gap-1"
        >
          <i class="fa-solid fa-clock"></i>
          Pendentes ({{ pendingCount }})
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
      <div v-if="!loading && filteredFiles.length === 0 && !error" class="text-center py-8">
        <div class="text-slate-500 mb-2">
          <i class="fa-solid fa-folder-open text-4xl"></i>
        </div>
        <p class="text-slate-400 text-sm">
          {{ files.length === 0 ? 'Nenhum arquivo encontrado neste processo' : 'Nenhum arquivo corresponde ao filtro selecionado' }}
        </p>
      </div>

      <!-- Files list -->
      <div v-if="filteredFiles.length > 0" class="space-y-3 max-h-96 overflow-y-auto">
        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="bg-slate-800/50 rounded-lg p-4 border-l-4"
          :class="getStatusBorderClass(file.approvalStatus)"
        >
          <div class="flex items-start justify-between">
            <!-- File info -->
            <div class="flex items-start gap-4 min-w-0 flex-1">
              <!-- File icon -->
              <div class="flex-shrink-0 mt-1">
                <i :class="getFileIcon(file.contentType)" class="text-lg"></i>
              </div>

              <!-- File details -->
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <h4 class="text-sm font-medium text-slate-200 truncate">
                    {{ file.originalName }}
                  </h4>
                  <span class="text-xs text-slate-500 px-2 py-0.5 bg-slate-700 rounded">
                    {{ formatFileSize(file.fileSize) }}
                  </span>
                  <span 
                    class="text-xs px-2 py-0.5 rounded-full font-medium"
                    :class="getStatusClass(file.approvalStatus)"
                  >
                    {{ getStatusText(file.approvalStatus) }}
                  </span>
                </div>
                
                <div class="flex items-center gap-4 text-xs text-slate-400 mb-2">
                  <span class="flex items-center gap-1">
                    <i class="fa-solid fa-clock w-3"></i>
                    {{ formatDate(file.uploadedAt) }}
                  </span>
                  
                  <span v-if="file.stageId" class="flex items-center gap-1">
                    <i class="fa-solid fa-layer-group w-3"></i>
                    Etapa: {{ file.stageId }}
                  </span>
                  
                  <span v-if="file.proposalId" class="flex items-center gap-1">
                    <i class="fa-solid fa-file-contract w-3"></i>
                    Proposta: {{ file.proposalId }}
                  </span>
                </div>

                <!-- Approval info -->
                <div v-if="file.approvalStatus && file.approvalStatus !== 'pending'" class="text-xs">
                  <div class="flex items-center gap-2 text-slate-400">
                    <i :class="file.approvalBy === 'AI' ? 'fa-solid fa-robot' : 'fa-solid fa-user'"></i>
                    <span>
                      {{ file.approvalBy === 'AI' ? 'Aprovado por IA' : `Aprovado por ${file.approvalBy || 'Usuário'}` }}
                      <span v-if="file.approvedAt"> em {{ formatDate(file.approvedAt) }}</span>
                    </span>
                  </div>
                  <p v-if="file.approvalNotes" class="text-slate-300 mt-1 bg-slate-900/30 rounded p-2">
                    {{ file.approvalNotes }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-2 flex-shrink-0 ml-4">
              <!-- Approval buttons -->
              <div class="flex items-center gap-1">
                <button
                  @click="approveFile(file, 'AI')"
                  :disabled="loading || file.approvalStatus === 'approved'"
                  class="px-2 py-1 text-xs bg-green-600 hover:bg-green-700 disabled:bg-slate-600 text-white rounded-md transition-colors flex items-center gap-1"
                  title="Aprovar por IA"
                >
                  <i class="fa-solid fa-robot text-[10px]"></i>
                  IA
                </button>
                <button
                  @click="approveFile(file, 'HUMAN')"
                  :disabled="loading || file.approvalStatus === 'approved'"
                  class="px-2 py-1 text-xs bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white rounded-md transition-colors flex items-center gap-1"
                  title="Aprovar por Humano"
                >
                  <i class="fa-solid fa-user text-[10px]"></i>
                  Humano
                </button>
                <button
                  @click="rejectFile(file)"
                  :disabled="loading || file.approvalStatus === 'rejected'"
                  class="px-2 py-1 text-xs bg-red-600 hover:bg-red-700 disabled:bg-slate-600 text-white rounded-md transition-colors flex items-center gap-1"
                  title="Reprovar arquivo"
                >
                  <i class="fa-solid fa-times text-[10px]"></i>
                </button>
              </div>

              <div class="w-px h-6 bg-slate-600"></div>

              <!-- File actions -->
              <button
                @click="downloadFile(file)"
                :disabled="loading"
                class="px-2 py-1 text-xs bg-orange-600 hover:bg-orange-700 disabled:bg-slate-600 text-white rounded-md transition-colors flex items-center gap-1"
                :title="`Baixar ${file.originalName}`"
              >
                <i class="fa-solid fa-download text-[10px]"></i>
              </button>
              
              <button
                @click="viewFileDetails(file)"
                class="px-2 py-1 text-xs bg-slate-600 hover:bg-slate-700 text-white rounded-md transition-colors flex items-center gap-1"
                :title="`Ver detalhes de ${file.originalName}`"
              >
                <i class="fa-solid fa-info text-[10px]"></i>
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
      :z-index="90"
      @update:model-value="closeDetails"
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
            
            <div class="flex justify-between">
              <span class="text-slate-400">Status:</span>
              <span :class="getStatusClass(selectedFile.approvalStatus)">
                {{ getStatusText(selectedFile.approvalStatus) }}
              </span>
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
            class="flex-1 px-4 py-2 bg-orange-600 hover:bg-orange-700 disabled:bg-slate-600 text-white rounded-lg transition-colors flex items-center justify-center gap-2"
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
import { ref, computed, watch } from 'vue'
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
  },
  files: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'refresh', 'approveFile', 'rejectFile', 'downloadFile'])

const {
  formatFileSize,
  getFileIcon
} = useProcessFiles()

const showDetails = ref(false)
const selectedFile = ref(null)
const filterStatus = ref('all')
const loading = ref(false)
const error = ref(null)

// Use files from props with approval status
const filesWithApproval = computed(() => {
  return props.files.map(file => ({
    ...file,
    approvalStatus: file.approvalStatus || 'pending',
    approvalBy: file.approvalBy || null,
    approvedAt: file.approvedAt || null,
    approvalNotes: file.approvalNotes || null
  }))
})

// Filtered files based on approval status
const filteredFiles = computed(() => {
  if (filterStatus.value === 'all') return filesWithApproval.value
  return filesWithApproval.value.filter(file => file.approvalStatus === filterStatus.value)
})

// Counts
const approvedCount = computed(() => filesWithApproval.value.filter(f => f.approvalStatus === 'approved').length)
const rejectedCount = computed(() => filesWithApproval.value.filter(f => f.approvalStatus === 'rejected').length)
const pendingCount = computed(() => filesWithApproval.value.filter(f => f.approvalStatus === 'pending').length)

const refresh = () => {
  // Emit event to parent to reload files
  emit('refresh')
}

const viewFileDetails = (file) => {
  selectedFile.value = file
  showDetails.value = true
}

const closeDetails = () => {
  showDetails.value = false
  selectedFile.value = null
}

const downloadFile = async (file) => {
  // Emit event to parent to handle download
  emit('downloadFile', { file })
}

const approveFile = async (file, approvalBy) => {
  // TODO: Implementar chamada para API de aprovação
  console.log(`Aprovando arquivo ${file.id} por ${approvalBy}`)
  
  // For now, just emit an event to the parent
  emit('approveFile', { file, approvalBy })
}

const rejectFile = async (file) => {
  // TODO: Implementar chamada para API de reprovação
  console.log(`Reprovando arquivo ${file.id}`)
  
  // For now, just emit an event to the parent
  emit('rejectFile', { file })
}

const getStatusClass = (status) => {
  switch (status) {
    case 'approved': return 'text-green-400 bg-green-900/30'
    case 'rejected': return 'text-red-400 bg-red-900/30'
    case 'pending': return 'text-yellow-400 bg-yellow-900/30'
    default: return 'text-slate-400 bg-slate-900/30'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'approved': return 'Aprovado'
    case 'rejected': return 'Reprovado'
    case 'pending': return 'Pendente'
    default: return 'Desconhecido'
  }
}

const getStatusBorderClass = (status) => {
  switch (status) {
    case 'approved': return 'border-green-500'
    case 'rejected': return 'border-red-500'
    case 'pending': return 'border-yellow-500'
    default: return 'border-slate-600'
  }
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