import { apiFetch, isApiEnabled } from '~/utils/api/index'

export async function fetchStagesApi(processKey: string): Promise<any[]> {
  if (!isApiEnabled()) return []
  try {
    return await apiFetch<any[]>(`/api/v1/processes/${encodeURIComponent(processKey)}/stages`)
  } catch { return [] }
}

export async function saveStagesApi(processKey: string, stages: any[]): Promise<any[]> {
  if (!isApiEnabled()) return []
  try {
    return await apiFetch<any[]>(`/api/v1/processes/${encodeURIComponent(processKey)}/stages`, {
      method: 'PUT',
      body: stages || []
    })
  } catch { return [] }
}

// NOTA: Backend apenas suporta operações em lote via PUT
// As funções individuais não são suportadas pela API atual

// Função inteligente que preserva IDs existentes ao salvar usando PUT em lote
export async function saveStagesPreservingIdsApi(processKey: string, currentStages: any[], newStages: any[]): Promise<any[]> {
  if (!isApiEnabled()) return newStages
  
  try {
    // Busca estágios atuais do backend
    const existingStages = await fetchStagesApi(processKey)
    
    // Mapeia novos estágios preservando IDs existentes quando possível
    const stagesToSave = newStages.map((newStage, index) => {
      // Verifica se é um ID temporário
      const isTemporaryId = newStage.id && (
        newStage.id.startsWith('temp_') ||
        newStage.id.match(/^\d+$/) // timestamp simples
      )
      
      // Tenta encontrar estágio existente para preservar ID
      let existing = null
      if (!isTemporaryId && newStage.id) {
        // Busca por ID exato primeiro
        existing = existingStages.find(e => e.id === newStage.id)
      }
      if (!existing) {
        // Busca por título como fallback
        existing = existingStages.find(e => e.title === newStage.title)
      }
      if (!existing && index < existingStages.length) {
        // Busca por posição como último recurso
        existing = existingStages[index]
      }
      
      return {
        id: existing?.id, // Preserva ID existente se encontrado
        title: newStage.title,
        slaDays: newStage.slaDays,
        color: newStage.color,
        defaultStatus: newStage.defaultStatus || existing?.defaultStatus || '',
        order: index
      }
    })
    
    // Usa PUT para substituir todos os estágios
    return await saveStagesApi(processKey, stagesToSave)
  } catch (error) {
    console.error('Error preserving stage IDs:', error)
    // Fallback para método original em caso de erro
    return await saveStagesApi(processKey, newStages)
  }
}

// Função para atualizar apenas a ordem dos estágios usando PUT com dados atuais
export async function updateStagesOrderApi(processKey: string, stageOrders: Array<{id: string, order: number}>): Promise<boolean> {
  if (!isApiEnabled()) return false
  try {
    // Busca estágios atuais para preservar outros dados
    const existingStages = await fetchStagesApi(processKey)
    
    // Cria mapa de ordens por ID
    const orderMap = new Map(stageOrders.map(o => [o.id, o.order]))
    
    // Ordena estágios pela nova ordem e mantém dados existentes
    const reorderedStages = existingStages
      .map(stage => ({
        id: stage.id,
        title: stage.title,
        slaDays: stage.slaDays,
        color: stage.color,
        defaultStatus: stage.defaultStatus,
        order: orderMap.get(stage.id) ?? 0
      }))
      .sort((a, b) => a.order - b.order)
      .map((stage, index) => ({ ...stage, order: index }))
    
    // Usa PUT para salvar com nova ordem
    await saveStagesApi(processKey, reorderedStages)
    return true
  } catch { return false }
}
