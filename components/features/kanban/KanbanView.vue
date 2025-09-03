<template>
  <div class="kanban-view">
    <!-- Kanban Header -->
    <div class="kanban-header">
      <div class="kanban-stats">
        <div class="stat-item">
          <span class="stat-label">Total</span>
          <span class="stat-value">{{ totalCards }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Em Andamento</span>
          <span class="stat-value">{{ inProgressCards }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Concluídos</span>
          <span class="stat-value">{{ completedCards }}</span>
        </div>
      </div>
      
      <div class="kanban-actions">
        <BaseButton
          variant="secondary"
          size="sm"
          @click="toggleCompactMode"
        >
          {{ compactMode ? 'Expandir' : 'Compactar' }}
        </BaseButton>
        <BaseButton
          variant="primary"
          size="sm"
          @click="addNewCard"
        >
          + Nova Proposta
        </BaseButton>
      </div>
    </div>
    
    <!-- Kanban Board -->
    <div class="kanban-board" :class="{ 'kanban-board--compact': compactMode }">
      <div
        v-for="column in columns"
        :key="column.id"
        class="kanban-column"
        :class="getColumnClasses(column)"
        @drop="handleDrop($event, column.id)"
        @dragover.prevent
        @dragenter.prevent
      >
        <!-- Column Header -->
        <div class="kanban-column-header">
          <div class="column-title-wrapper">
            <h3 class="column-title">
              {{ column.title }}
            </h3>
            <span class="column-count">
              {{ getColumnCards(column.id).length }}
            </span>
            <span v-if="column.limit" class="column-limit">
              / {{ column.limit }}
            </span>
          </div>
          
          <div class="column-actions">
            <button
              class="column-action-btn"
              @click="toggleColumnCollapse(column.id)"
            >
              <Icon :name="column.collapsed ? 'chevron-right' : 'chevron-down'" />
            </button>
            <button
              class="column-action-btn"
              @click="openColumnSettings(column.id)"
            >
              <Icon name="settings" />
            </button>
          </div>
        </div>
        
        <!-- Column Content -->
        <div v-if="!column.collapsed" class="kanban-column-content">
          <!-- Cards -->
          <div class="kanban-cards">
            <!-- TODO: Implement KanbanCard component -->
            <div
              v-for="card in getColumnCards(column.id)"
              :key="card.id"
              class="kanban-card-placeholder"
              @click="openCard(card)"
            >
              {{ card.title }}
            </div>
          </div>
          
          <!-- Add Card Button -->
          <button
            class="add-card-btn"
            @click="addCardToColumn(column.id)"
          >
            <Icon name="plus" />
            <span>Adicionar cartão</span>
          </button>
          
          <!-- Drop Zone -->
          <div
            v-if="isDragging"
            class="drop-zone"
            :class="{ 'drop-zone--active': dragOverColumn === column.id }"
          >
            Solte aqui
          </div>
        </div>
      </div>
    </div>
    
    <!-- Loading State -->
    <div v-if="loading" class="kanban-loading">
      <div class="loading-spinner"></div>
      <p>Carregando propostas...</p>
    </div>
    
    <!-- Empty State -->
    <div v-if="!loading && totalCards === 0" class="kanban-empty">
      <Icon name="inbox" class="empty-icon" />
      <h3>Nenhuma proposta encontrada</h3>
      <p>Comece criando sua primeira proposta</p>
      <BaseButton variant="primary" @click="addNewCard">
        + Nova Proposta
      </BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import BaseButton from '../../ui/BaseButton.vue'

interface KanbanCard {
  id: string
  title: string
  description?: string
  status: string
  priority: 'low' | 'medium' | 'high' | 'urgent'
  assignee?: {
    id: string
    name: string
    avatar?: string
  }
  dueDate?: string
  tags?: string[]
  attachments?: number
  comments?: number
  checklist?: {
    total: number
    completed: number
  }
  createdAt: string
  updatedAt: string
}

interface KanbanColumn {
  id: string
  title: string
  color?: string
  limit?: number
  collapsed?: boolean
  order: number
}

interface Props {
  columns?: KanbanColumn[]
  cards?: KanbanCard[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  columns: () => [],
  cards: () => [],
  loading: false
})

const emit = defineEmits<{
  'card-click': [card: KanbanCard]
  'card-edit': [card: KanbanCard]
  'card-delete': [card: KanbanCard]
  'card-move': [cardId: string, fromColumn: string, toColumn: string]
  'card-add': [columnId: string]
  'column-settings': [columnId: string]
  'new-card': []
}>()

// State
const compactMode = ref(false)
const isDragging = ref(false)
const dragOverColumn = ref<string | null>(null)
const draggedCard = ref<KanbanCard | null>(null)

// Default columns if none provided
const defaultColumns: KanbanColumn[] = [
  { id: 'backlog', title: 'Backlog', order: 1 },
  { id: 'todo', title: 'A Fazer', order: 2 },
  { id: 'in-progress', title: 'Em Andamento', limit: 3, order: 3 },
  { id: 'review', title: 'Em Revisão', order: 4 },
  { id: 'done', title: 'Concluído', order: 5 }
]

const columns = computed(() => {
  return props.columns.length > 0 ? props.columns : defaultColumns
})

// Computed
const totalCards = computed(() => props.cards.length)

const inProgressCards = computed(() => {
  return props.cards.filter(card => 
    ['todo', 'in-progress', 'review'].includes(card.status)
  ).length
})

const completedCards = computed(() => {
  return props.cards.filter(card => card.status === 'done').length
})

// Methods
const getColumnCards = (columnId: string) => {
  return props.cards.filter(card => card.status === columnId)
}

const getColumnClasses = (column: KanbanColumn) => {
  const classes: string[] = []
  
  if (column.collapsed) classes.push('kanban-column--collapsed')
  if (column.color) classes.push(`kanban-column--${column.color}`)
  
  const cards = getColumnCards(column.id)
  if (column.limit && cards.length >= column.limit) {
    classes.push('kanban-column--limit-reached')
  }
  
  if (dragOverColumn.value === column.id) {
    classes.push('kanban-column--drag-over')
  }
  
  return classes
}

const toggleCompactMode = () => {
  compactMode.value = !compactMode.value
}

const toggleColumnCollapse = (columnId: string) => {
  const column = columns.value.find(col => col.id === columnId)
  if (column) {
    column.collapsed = !column.collapsed
  }
}

const openColumnSettings = (columnId: string) => {
  emit('column-settings', columnId)
}

const addNewCard = () => {
  emit('new-card')
}

const addCardToColumn = (columnId: string) => {
  emit('card-add', columnId)
}

const openCard = (card: KanbanCard) => {
  emit('card-click', card)
}

const _editCard = (card: KanbanCard) => {
  emit('card-edit', card)
}

const _deleteCard = (card: KanbanCard) => {
  emit('card-delete', card)
}

// Drag and Drop
const _handleDragStart = (event: DragEvent, card: KanbanCard) => {
  isDragging.value = true
  draggedCard.value = card
  
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', card.id)
  }
}

const handleDrop = (event: DragEvent, columnId: string) => {
  event.preventDefault()
  
  if (draggedCard.value && draggedCard.value.status !== columnId) {
    emit('card-move', draggedCard.value.id, draggedCard.value.status, columnId)
  }
  
  isDragging.value = false
  dragOverColumn.value = null
  draggedCard.value = null
}

onMounted(() => {
  // Initialize any required data
})
</script>

<style scoped>
.kanban-view {
  @apply h-full flex flex-col;
}

.kanban-header {
  @apply flex items-center justify-between p-4 bg-white border-b border-gray-200;
}

.kanban-stats {
  @apply flex items-center space-x-6;
}

.stat-item {
  @apply flex flex-col items-center;
}

.stat-label {
  @apply text-xs text-gray-500 uppercase tracking-wide;
}

.stat-value {
  @apply text-2xl font-bold text-gray-900;
}

.kanban-actions {
  @apply flex items-center space-x-2;
}

.kanban-board {
  @apply flex-1 flex overflow-x-auto p-4 space-x-4;
}

.kanban-board--compact {
  @apply space-x-2;
}

.kanban-column {
  @apply flex-shrink-0 w-80 bg-gray-50 rounded-lg;
}

.kanban-board--compact .kanban-column {
  @apply w-64;
}

.kanban-column--collapsed {
  @apply w-12;
}

.kanban-column--limit-reached {
  @apply bg-red-50 border border-red-200;
}

.kanban-column--drag-over {
  @apply bg-blue-50 border-2 border-blue-300 border-dashed;
}

.kanban-column-header {
  @apply flex items-center justify-between p-3 border-b border-gray-200;
}

.column-title-wrapper {
  @apply flex items-center space-x-2;
}

.column-title {
  @apply font-semibold text-gray-900;
}

.column-count {
  @apply bg-gray-200 text-gray-700 text-xs px-2 py-1 rounded-full;
}

.column-limit {
  @apply text-xs text-gray-500;
}

.column-actions {
  @apply flex items-center space-x-1;
}

.column-action-btn {
  @apply p-1 text-gray-400 hover:text-gray-600 rounded transition-colors;
}

.kanban-column-content {
  @apply p-3 space-y-3 min-h-32;
}

.kanban-cards {
  @apply space-y-3;
}

.add-card-btn {
  @apply w-full flex items-center justify-center space-x-2 p-3 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg border-2 border-dashed border-gray-300 hover:border-gray-400 transition-all;
}

.drop-zone {
  @apply w-full h-16 border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center text-gray-500;
}

.drop-zone--active {
  @apply border-blue-500 bg-blue-50 text-blue-600;
}

.kanban-loading {
  @apply flex-1 flex flex-col items-center justify-center space-y-4;
}

.loading-spinner {
  @apply w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin;
}

.kanban-empty {
  @apply flex-1 flex flex-col items-center justify-center space-y-4 text-center;
}

.empty-icon {
  @apply w-16 h-16 text-gray-400;
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .kanban-header {
    @apply bg-gray-800 border-gray-700;
  }
  
  .stat-label {
    @apply text-gray-400;
  }
  
  .stat-value {
    @apply text-white;
  }
  
  .kanban-column {
    @apply bg-gray-700;
  }
  
  .kanban-column-header {
    @apply border-gray-600;
  }
  
  .column-title {
    @apply text-white;
  }
  
  .column-count {
    @apply bg-gray-600 text-gray-300;
  }
  
  .column-limit {
    @apply text-gray-400;
  }
  
  .add-card-btn {
    @apply text-gray-400 hover:text-gray-200 hover:bg-gray-600 border-gray-500 hover:border-gray-400;
  }
  
  .drop-zone {
    @apply border-gray-500 text-gray-400;
  }
}
</style>