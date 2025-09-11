import { ref } from 'vue'

export interface ProposalFile {
  id: string
  objectKey: string
  fileName: string
  originalName: string
  fileSize: number
  contentType: string
  uploadedAt: string
  proposalId: string
  stageId: string
  fieldId: string
  fieldLabel?: string
  approvalStatus?: 'pending' | 'approved' | 'rejected'
  approvalBy?: string
  approvedAt?: string
  approvalNotes?: string
}

export function useProposalFiles() {
  const files = ref<ProposalFile[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const extractFilesFromProposal = (proposal: any, stageFields: any[] = []) => {
    const extractedFiles: ProposalFile[] = []
    
    if (!proposal.forms) return extractedFiles

    // Percorrer todas as stages no forms
    Object.keys(proposal.forms).forEach(stageId => {
      const stageForm = proposal.forms[stageId]
      if (!stageForm || typeof stageForm !== 'object') return

      // Percorrer todos os campos da stage
      Object.keys(stageForm).forEach(fieldId => {
        const fieldValue = stageForm[fieldId]
        
        // Verificar se o valor é uma string que parece ser um objectKey
        if (typeof fieldValue === 'string' && fieldValue.includes('/')) {
          // Extrair o nome original do arquivo do objectKey
          const parts = fieldValue.split('/')
          const fileNameWithTimestamp = parts[parts.length - 1] || fieldValue
          
          // Tentar extrair o nome original removendo o timestamp
          const originalName = extractOriginalFileName(fileNameWithTimestamp)
          
          // Buscar informações do campo
          const fieldInfo = stageFields.find(f => f.id === fieldId)
          
          // Criar objeto de arquivo
          const file: ProposalFile = {
            id: `${proposal._id}_${stageId}_${fieldId}`,
            objectKey: fieldValue,
            fileName: fileNameWithTimestamp,
            originalName: originalName,
            fileSize: 0, // Não temos essa informação na proposta
            contentType: inferContentType(originalName),
            uploadedAt: proposal.updatedAt || proposal.createdAt || new Date().toISOString(),
            proposalId: proposal._id,
            stageId: stageId,
            fieldId: fieldId,
            fieldLabel: fieldInfo?.label || `Campo ${fieldId}`,
            approvalStatus: 'pending'
          }
          
          extractedFiles.push(file)
        }
      })
    })

    return extractedFiles
  }

  const extractOriginalFileName = (fileNameWithTimestamp: string): string => {
    // Remove timestamp no formato: "1757345494592_NOME_ARQUIVO.ext"
    const timestampRegex = /^\d+_(.+)$/
    const match = fileNameWithTimestamp.match(timestampRegex)
    return match ? match[1] : fileNameWithTimestamp
  }

  const inferContentType = (fileName: string): string => {
    const extension = fileName.split('.').pop()?.toLowerCase()
    
    switch (extension) {
      case 'pdf': return 'application/pdf'
      case 'csv': return 'text/csv'
      case 'xlsx': case 'xls': return 'application/vnd.ms-excel'
      case 'doc': case 'docx': return 'application/msword'
      case 'png': return 'image/png'
      case 'jpg': case 'jpeg': return 'image/jpeg'
      case 'gif': return 'image/gif'
      case 'txt': return 'text/plain'
      default: return 'application/octet-stream'
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
    if (contentType.includes('excel') || contentType.includes('spreadsheet') || contentType.includes('csv')) return 'fa-solid fa-file-excel text-green-600'
    if (contentType.includes('powerpoint')) return 'fa-solid fa-file-powerpoint text-orange-500'
    if (contentType.includes('zip') || contentType.includes('rar')) return 'fa-solid fa-file-zipper text-yellow-600'
    return 'fa-solid fa-file text-slate-500'
  }

  return {
    files,
    loading,
    error,
    extractFilesFromProposal,
    formatFileSize,
    getFileIcon
  }
}