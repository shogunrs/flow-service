import { ref } from 'vue'
import { apiFetch, isApiEnabled } from '~/utils/api/index'

export interface ProcessFile {
  id: string
  objectKey: string
  fileName: string
  originalName: string
  fileSize: number
  contentType: string
  uploadedAt: string
  proposalId?: string
  stageId?: string
  fieldId?: string
}

export function useProcessFiles() {
  const files = ref<ProcessFile[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loadProcessFiles = async (processKey: string) => {
    if (!isApiEnabled()) {
      console.warn('API disabled, cannot load process files')
      return
    }

    loading.value = true
    error.value = null

    try {
      const response = await apiFetch(`/api/v1/processes/${processKey}/files`)
      files.value = response || []
    } catch (err) {
      error.value = 'Erro ao carregar arquivos do processo'
      console.error('Error loading process files:', err)
    } finally {
      loading.value = false
    }
  }

  const getFileDownloadUrl = async (objectKey: string): Promise<string> => {
    if (!isApiEnabled()) {
      throw new Error('API disabled')
    }

    try {
      const response = await apiFetch('/api/v1/files/download-url', {
        method: 'POST',
        body: { objectKey }
      })
      return response.url
    } catch (err) {
      throw new Error('Erro ao gerar URL de download')
    }
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes'
    
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const getFileIcon = (contentType: string): string => {
    if (contentType.includes('image/')) return 'fa-solid fa-image text-blue-500'
    if (contentType.includes('pdf')) return 'fa-solid fa-file-pdf text-red-500'
    if (contentType.includes('word')) return 'fa-solid fa-file-word text-blue-600'
    if (contentType.includes('excel') || contentType.includes('spreadsheet')) return 'fa-solid fa-file-excel text-green-600'
    if (contentType.includes('powerpoint')) return 'fa-solid fa-file-powerpoint text-orange-500'
    if (contentType.includes('zip') || contentType.includes('rar')) return 'fa-solid fa-file-zipper text-yellow-600'
    return 'fa-solid fa-file text-slate-500'
  }

  const downloadFile = async (file: ProcessFile) => {
    try {
      loading.value = true
      const downloadUrl = await getFileDownloadUrl(file.objectKey)
      
      // Create temporary link to download
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = file.originalName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } catch (err) {
      error.value = 'Erro ao fazer download do arquivo'
      console.error('Download error:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    files,
    loading,
    error,
    loadProcessFiles,
    downloadFile,
    formatFileSize,
    getFileIcon
  }
}