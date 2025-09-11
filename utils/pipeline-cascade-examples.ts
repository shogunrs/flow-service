/**
 * Exemplos de uso da funcionalidade de deleção em cascade do Pipeline
 * 
 * Este arquivo demonstra como usar as novas funcionalidades de cascade deletion
 * implementadas no usePipeline.ts
 */

import { 
  removeProcess, 
  auditAndCleanOrphanedData, 
  getStorageUsageByProcess, 
  backupProcessData 
} from '~/composables/usePipeline'

// ==========================================
// EXEMPLO 1: Deleção básica com cascade
// ==========================================

export async function deleteProcessExample() {
  const processKey = 'minha-esteira'
  
  console.log('🗑️ Deletando processo com cascade...')
  
  const result = await removeProcess(processKey)
  
  if (result.success) {
    console.log('✅ Processo deletado com sucesso!')
    console.log(`📦 Backup criado com ${Object.keys(result.backup || {}).length} chaves`)
    console.log(`🧹 ${result.deletedKeys?.length || 0} chaves removidas:`, result.deletedKeys)
  } else {
    console.error('❌ Falha ao deletar processo')
  }
}

// ==========================================
// EXEMPLO 2: Deleção com backup manual
// ==========================================

export async function deleteWithManualBackup() {
  const processKey = 'esteira-importante'
  
  // 1. Criar backup manual antes da deleção
  console.log('💾 Criando backup manual...')
  const backup = backupProcessData(processKey)
  
  if (backup) {
    // Salvar backup em arquivo ou enviar para servidor
    const backupBlob = new Blob([JSON.stringify(backup, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(backupBlob)
    
    const link = document.createElement('a')
    link.href = url
    link.download = `backup-${processKey}-${Date.now()}.json`
    link.click()
    
    console.log('💾 Backup salvo em arquivo')
  }
  
  // 2. Deletar sem criar backup automático (já temos o manual)
  const result = await removeProcess(processKey, { backup: false })
  
  console.log(result.success ? '✅ Deletado com sucesso' : '❌ Falha na deleção')
}

// ==========================================
// EXEMPLO 3: Auditoria e limpeza de órfãos
// ==========================================

export function cleanupOrphanedDataExample() {
  console.log('🔍 Auditando dados órfãos...')
  
  const audit = auditAndCleanOrphanedData()
  
  console.log(`📊 Resultado da auditoria:
    🗑️ Órfãos encontrados: ${audit.orphaned.length}
    ✅ Limpos com sucesso: ${audit.cleaned.length}  
    ❌ Erros na limpeza: ${audit.errors.length}
  `)
  
  if (audit.cleaned.length > 0) {
    console.log('🧹 Chaves limpas:', audit.cleaned)
  }
  
  if (audit.errors.length > 0) {
    console.error('⚠️ Erros:', audit.errors)
  }
}

// ==========================================
// EXEMPLO 4: Análise de uso de storage
// ==========================================

export function analyzeStorageUsage() {
  console.log('📊 Analisando uso de storage por processo...')
  
  const usage = getStorageUsageByProcess()
  
  Object.entries(usage).forEach(([processKey, data]) => {
    const sizeKB = Math.round(data.sizeEstimate / 1024)
    console.log(`
📁 Processo: ${processKey}
   📝 ${data.keys.length} chaves de dados
   💾 ~${sizeKB} KB estimados
   🔑 Chaves: ${data.keys.join(', ')}
    `)
  })
  
  const totalSize = Object.values(usage).reduce((acc, data) => acc + data.sizeEstimate, 0)
  const totalKeys = Object.values(usage).reduce((acc, data) => acc + data.keys.length, 0)
  
  console.log(`
📊 RESUMO TOTAL:
   📁 ${Object.keys(usage).length} processos
   🔑 ${totalKeys} chaves total
   💾 ~${Math.round(totalSize / 1024)} KB total
  `)
}

// ==========================================
// EXEMPLO 5: Fluxo completo de manutenção
// ==========================================

export async function fullMaintenanceFlow() {
  console.log('🔧 Iniciando manutenção completa do pipeline...')
  
  // 1. Análise inicial
  console.log('\n📊 1. Analisando uso atual...')
  analyzeStorageUsage()
  
  // 2. Limpeza de órfãos
  console.log('\n🧹 2. Limpando dados órfãos...')
  cleanupOrphanedDataExample()
  
  // 3. Análise pós-limpeza
  console.log('\n📊 3. Análise pós-limpeza...')
  analyzeStorageUsage()
  
  console.log('\n✅ Manutenção completa finalizada!')
}

// ==========================================
// EXEMPLO 6: Interface de confirmação de deleção
// ==========================================

export async function deleteWithConfirmation(processKey: string, processName: string) {
  // Verificar dados que serão perdidos
  const usage = getStorageUsageByProcess()[processKey]
  
  if (!usage) {
    console.log('ℹ️ Processo não possui dados armazenados')
    return
  }
  
  const message = `
⚠️ ATENÇÃO: Deleção de Esteira

Processo: ${processName} (${processKey})
Dados que serão perdidos:
  📝 ${usage.keys.length} conjuntos de dados
  💾 ~${Math.round(usage.sizeEstimate / 1024)} KB de informações
  
Dados incluem:
  • Configurações de estágios
  • Formulários personalizados  
  • Propostas e registros
  • Histórico de atividades
  • Arquivos anexados
  • Relatórios gerados

Esta ação NÃO PODE ser desfeita!
Um backup será criado automaticamente.

Tem certeza que deseja continuar?
  `
  
  if (confirm(message)) {
    console.log('🗑️ Usuário confirmou deleção. Executando...')
    
    const result = await removeProcess(processKey)
    
    if (result.success) {
      alert(`✅ Processo "${processName}" deletado com sucesso!\n\n📦 Backup criado com ${Object.keys(result.backup || {}).length} chaves\n🧹 ${result.deletedKeys?.length || 0} chaves removidas`)
    } else {
      alert('❌ Falha ao deletar processo. Verifique o console para mais detalhes.')
    }
  } else {
    console.log('🚫 Deleção cancelada pelo usuário')
  }
}