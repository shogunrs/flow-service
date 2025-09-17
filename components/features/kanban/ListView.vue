<template>
  <div class="list-view">
    <!-- List Header -->
    <div class="list-header">
      <div class="list-stats">
        <div class="stat-item">
          <span class="stat-label">Total</span>
          <span class="stat-value">{{ totalItems }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Selecionados</span>
          <span class="stat-value">{{ selectedItems.length }}</span>
        </div>
      </div>
      
      <div class="list-actions">
        <BaseButton
          variant="secondary"
          size="sm"
          @click="toggleSelectAll"
        >
          {{ allSelected ? 'Desmarcar Todos' : 'Selecionar Todos' }}
        </BaseButton>
        <BaseButton
          variant="primary"
          size="sm"
          @click="addNewItem"
        >
          + Nova Proposta
        </BaseButton>
      </div>
    </div>
    
    <!-- List Content -->
    <div class="list-content">
      <BaseTable
        :data="items"
        :columns="columns"
        :loading="loading"
        :searchable="true"
        :sortable="true"
        :selectable="true"
        :pagination="true"
        :page-size="pageSize"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        @sort="handleSort"
      >
        <!-- Custom column templates -->
        <template #status="{ item }">
          <span class="status-badge" :class="getStatusClass(item.status)">
            {{ getStatusLabel(item.status) }}
          </span>
        </template>
        
        <template #priority="{ item }">
          <span class="priority-badge" :class="getPriorityClass(item.priority)">
            {{ getPriorityLabel(item.priority) }}
          </span>
        </template>
        
        <template #assignee="{ item }">
          <div v-if="(item as ListItem).assignee" class="assignee-info">
            <img
              v-if="(item as ListItem).assignee?.avatar"
              :src="(item as ListItem).assignee?.avatar"
              :alt="(item as ListItem).assignee?.name"
              class="assignee-avatar"
            />
            <div v-else class="assignee-avatar-placeholder">
              {{ (item as ListItem).assignee?.name?.charAt(0).toUpperCase() }}
            </div>
            <span class="assignee-name">{{ (item as ListItem).assignee?.name }}</span>
          </div>
          <span v-else class="text-gray-400">Não atribuído</span>
        </template>
        
        <template #dueDate="{ item }">
          <span v-if="(item as ListItem).dueDate" :class="getDueDateClass((item as ListItem).dueDate!)">
            {{ formatDate((item as ListItem).dueDate!) }}
          </span>
          <span v-else class="text-gray-400">-</span>
        </template>
        
        <template #actions="{ item }">
          <div class="action-buttons">
            <button
              class="action-btn action-btn--edit"
              @click.stop="editItem(item as ListItem)"
            >
              <Icon name="edit" />
            </button>
            <button
              class="action-btn action-btn--delete"
              @click.stop="deleteItem(item as ListItem)"
            >
              <Icon name="trash" />
            </button>
          </div>
        </template>
      </BaseTable>
    </div>
    
    <!-- Loading State -->
    <div v-if="loading" class="list-loading">
      <div class="loading-spinner"></div>
      <p>Carregando propostas...</p>
    </div>
    
    <!-- Empty State -->
    <div v-if="!loading && totalItems === 0" class="list-empty">
      <Icon name="inbox" class="empty-icon" />
      <h3>Nenhuma proposta encontrada</h3>
      <p>Comece criando sua primeira proposta</p>
      <BaseButton variant="primary" @click="addNewItem">
        + Nova Proposta
      </BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import BaseButton from '../../ui/BaseButton.vue'
import BaseTable from '../../ui/BaseTable.vue'

interface ListItem extends Record<string, unknown> {
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
  createdAt: string
  updatedAt: string
}

interface Props {
  items?: ListItem[]
  loading?: boolean
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  loading: false,
  pageSize: 20
})

const emit = defineEmits<{
  'item-click': [item: ListItem]
  'item-edit': [item: ListItem]
  'item-delete': [item: ListItem]
  'selection-change': [items: ListItem[]]
  'sort': [column: string, direction: 'asc' | 'desc']
  'new-item': []
}>()

// State
const selectedItems = ref<ListItem[]>([])

// Table columns configuration
const columns = [
  {
    key: 'title',
    label: 'Título',
    sortable: true,
    width: '25%'
  },
  {
    key: 'status',
    label: 'Status',
    sortable: true,
    width: '15%'
  },
  {
    key: 'priority',
    label: 'Prioridade',
    sortable: true,
    width: '15%'
  },
  {
    key: 'assignee',
    label: 'Responsável',
    sortable: false,
    width: '20%'
  },
  {
    key: 'dueDate',
    label: 'Prazo',
    sortable: true,
    width: '15%'
  },
  {
    key: 'actions',
    label: 'Ações',
    sortable: false,
    width: '10%'
  }
]

// Computed
const totalItems = computed(() => props.items.length)

const allSelected = computed(() => {
  return props.items.length > 0 && selectedItems.value.length === props.items.length
})

// Methods
const handleSelectionChange = (items: Record<string, unknown>[]) => {
  selectedItems.value = items.filter(item => 
    typeof item === 'object' && item !== null && 'id' in item
  ) as ListItem[]
  emit('selection-change', selectedItems.value)
}

const handleRowClick = (item: Record<string, unknown>) => {
  if (typeof item === 'object' && item !== null && 'id' in item) {
    emit('item-click', item as ListItem)
  }
}

const handleSort = (column: string, direction: 'asc' | 'desc') => {
  emit('sort', column, direction)
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedItems.value = []
  } else {
    selectedItems.value = [...props.items]
  }
  emit('selection-change', selectedItems.value)
}

const addNewItem = () => {
  emit('new-item')
}

const editItem = (item: ListItem) => {
  emit('item-edit', item)
}

const deleteItem = (item: ListItem) => {
  emit('item-delete', item)
}

// Utility methods
const getStatusClass = (status: string) => {
  const statusClasses: Record<string, string> = {
    'backlog': 'status-badge--gray',
    'todo': 'status-badge--blue',
    'in-progress': 'status-badge--yellow',
    'review': 'status-badge--purple',
    'done': 'status-badge--green'
  }
  return statusClasses[status] || 'status-badge--gray'
}

const getStatusLabel = (status: string) => {
  const statusLabels: Record<string, string> = {
    'backlog': 'Backlog',
    'todo': 'A Fazer',
    'in-progress': 'Em Andamento',
    'review': 'Em Revisão',
    'done': 'Concluído'
  }
  return statusLabels[status] || status
}

const getPriorityClass = (priority: string) => {
  const priorityClasses: Record<string, string> = {
    'low': 'priority-badge--green',
    'medium': 'priority-badge--yellow',
    'high': 'priority-badge--orange',
    'urgent': 'priority-badge--red'
  }
  return priorityClasses[priority] || 'priority-badge--gray'
}

const getPriorityLabel = (priority: string) => {
  const priorityLabels: Record<string, string> = {
    'low': 'Baixa',
    'medium': 'Média',
    'high': 'Alta',
    'urgent': 'Urgente'
  }
  return priorityLabels[priority] || priority
}

const getDueDateClass = (dueDate: string) => {
  const today = new Date()
  const due = new Date(dueDate)
  const diffDays = Math.ceil((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return 'text-red-600 font-semibold' // Overdue
  if (diffDays <= 3) return 'text-indigo-600 font-semibold' // Due soon
  return 'text-gray-600' // Normal
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}
</script>

<style scoped>
.list-view {
  @apply h-full flex flex-col;
}

.list-header {
  @apply flex items-center justify-between p-4 bg-white border-b border-gray-200;
}

.list-stats {
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

.list-actions {
  @apply flex items-center space-x-2;
}

.list-content {
  @apply flex-1 overflow-hidden;
}

.status-badge {
  @apply inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium;
}

.status-badge--gray {
  @apply bg-gray-100 text-gray-800;
}

.status-badge--blue {
  @apply bg-blue-100 text-blue-800;
}

.status-badge--yellow {
  @apply bg-yellow-100 text-yellow-800;
}

.status-badge--purple {
  @apply bg-purple-100 text-purple-800;
}

.status-badge--green {
  @apply bg-green-100 text-green-800;
}

.priority-badge {
  @apply inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium;
}

.priority-badge--green {
  @apply bg-green-100 text-green-800;
}

.priority-badge--yellow {
  @apply bg-yellow-100 text-yellow-800;
}

.priority-badge--orange {
  @apply bg-orange-100 text-orange-800;
}

.priority-badge--red {
  @apply bg-red-100 text-red-800;
}

.priority-badge--gray {
  @apply bg-gray-100 text-gray-800;
}

.assignee-info {
  @apply flex items-center space-x-2;
}

.assignee-avatar {
  @apply w-6 h-6 rounded-full object-cover;
}

.assignee-avatar-placeholder {
  @apply w-6 h-6 rounded-full bg-gray-300 flex items-center justify-center text-xs font-semibold text-gray-600;
}

.assignee-name {
  @apply text-sm text-gray-900 truncate;
}

.action-buttons {
  @apply flex items-center space-x-1;
}

.action-btn {
  @apply p-1 rounded hover:bg-gray-100 transition-colors;
}

.action-btn--edit {
  @apply text-blue-600 hover:text-blue-700;
}

.action-btn--delete {
  @apply text-red-600 hover:text-red-700;
}

.list-loading {
  @apply flex-1 flex flex-col items-center justify-center space-y-4;
}

.loading-spinner {
  @apply w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin;
}

.list-empty {
  @apply flex-1 flex flex-col items-center justify-center space-y-4 text-center;
}

.empty-icon {
  @apply w-16 h-16 text-gray-400;
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .list-header {
    @apply bg-gray-800 border-gray-700;
  }
  
  .stat-label {
    @apply text-gray-400;
  }
  
  .stat-value {
    @apply text-white;
  }
  
  .assignee-name {
    @apply text-gray-100;
  }
  
  .action-btn {
    @apply hover:bg-gray-700;
  }
}
</style>