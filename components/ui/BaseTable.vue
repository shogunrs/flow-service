<template>
  <div class="table-container">
    <!-- Table Toolbar -->
    <div v-if="showToolbar" class="table-toolbar">
      <div class="table-toolbar-left">
        <slot name="toolbar-left">
          <!-- Search -->
          <div v-if="searchable" class="table-search">
            <div class="relative">
              <Icon
                name="mdi:magnify"
                size="16"
                class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
              />
              <input
                v-model="searchQuery"
                type="text"
                :placeholder="searchPlaceholder"
                class="table-search-input pl-10"
              >
            </div>
          </div>
          
          <!-- Filters -->
          <div v-if="filters && filters.length > 0" class="table-filters">
            <div
              v-for="filter in filters"
              :key="filter.key"
              class="table-filter"
            >
              <label class="table-filter-label">{{ filter.label }}</label>
              <select
                v-model="filterValues[filter.key]"
                class="table-filter-select"
                @change="handleFilterChange"
              >
                <option value="">Todos</option>
                <option
                  v-for="option in filter.options"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
          </div>
        </slot>
      </div>
      
      <div class="table-toolbar-right">
        <slot name="toolbar-right">
          <BaseButton
            v-if="showAddButton"
            variant="primary"
            icon="mdi:plus"
            @click="$emit('add')"
          >
            {{ addButtonText }}
          </BaseButton>
        </slot>
      </div>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <table class="table" :class="tableClasses">
        <!-- Header -->
        <thead class="table-header">
          <tr>
            <th
              v-if="selectable"
              class="w-12"
            >
              <input
                v-model="selectAll"
                type="checkbox"
                class="table-checkbox"
                @change="handleSelectAll"
              >
            </th>
            <th
              v-for="column in columns"
              :key="column.key"
              :class="getHeaderClasses(column)"
              @click="handleSort(column)"
            >
              <div class="flex items-center space-x-2">
                <span>{{ column.label }}</span>
                <Icon
                  v-if="column.sortable && sortBy === column.key"
                  :name="sortOrder === 'asc' ? 'mdi:arrow-up' : 'mdi:arrow-down'"
                  size="14"
                  class="sort-icon"
                  :class="sortOrder"
                />
                <Icon
                  v-else-if="column.sortable"
                  name="mdi:unfold-more-horizontal"
                  size="14"
                  class="sort-icon"
                />
              </div>
            </th>
            <th v-if="hasActions" class="w-24">
              Ações
            </th>
          </tr>
        </thead>
        
        <!-- Body -->
        <tbody class="table-body">
          <!-- Loading State -->
          <tr v-if="loading">
            <td :colspan="totalColumns" class="text-center py-8">
              <Icon name="mdi:loading" size="24" class="animate-spin text-orange-500" />
              <span class="ml-2 text-gray-300">{{ loadingText }}</span>
            </td>
          </tr>
          
          <!-- Empty State -->
          <tr v-else-if="filteredData.length === 0">
            <td :colspan="totalColumns" class="table-empty">
              <Icon :name="emptyIcon" size="48" class="table-empty-icon" />
              <h3 class="table-empty-title">{{ emptyTitle }}</h3>
              <p class="table-empty-message">{{ emptyMessage }}</p>
              <BaseButton
                v-if="showEmptyAction"
                variant="primary"
                class="table-empty-action"
                @click="$emit('empty-action')"
              >
                {{ emptyActionText }}
              </BaseButton>
            </td>
          </tr>
          
          <!-- Data Rows -->
          <tr
            v-for="(item, index) in paginatedData"
            :key="String(getRowKey(item, index))"
            class="table-row"
            :class="getRowClasses(item, index)"
            @click="handleRowClick(item, index)"
          >
            <!-- Selection -->
            <td v-if="selectable">
              <input
                v-model="selectedItems"
                type="checkbox"
                :value="getRowKey(item, index)"
                class="table-checkbox"
                @click.stop
              >
            </td>
            
            <!-- Data Columns -->
            <td
              v-for="column in columns"
              :key="column.key"
              :class="getCellClasses(column)"
            >
              <slot
                :name="`cell-${column.key}`"
                :item="item"
                :value="getColumnValue(item, column.key)"
                :column="column"
                :index="index"
              >
                <component
                  :is="getCellComponent(column)"
                  :value="getColumnValue(item, column.key)"
                  :item="item"
                  :column="column"
                />
              </slot>
            </td>
            
            <!-- Actions -->
            <td v-if="hasActions" class="table-cell actions">
              <div class="table-actions">
                <slot
                  name="actions"
                  :item="item"
                  :index="index"
                >
                  <button
                    v-if="showEditAction"
                    type="button"
                    class="table-action edit"
                    @click.stop="$emit('edit', item, index)"
                  >
                    <Icon name="mdi:pencil" size="16" />
                  </button>
                  <button
                    v-if="showDeleteAction"
                    type="button"
                    class="table-action delete"
                    @click.stop="$emit('delete', item, index)"
                  >
                    <Icon name="mdi:delete" size="16" />
                  </button>
                </slot>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="paginated && !loading" class="table-pagination">
      <div class="table-pagination-info">
        Mostrando {{ startItem }} a {{ endItem }} de {{ totalItems }} registros
      </div>
      
      <div class="table-pagination-controls">
        <button
          type="button"
          class="table-pagination-button"
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          <Icon name="mdi:chevron-left" size="16" />
          Anterior
        </button>
        
        <button
          v-for="page in visiblePages"
          :key="page"
          type="button"
          class="table-pagination-button"
          :class="{ active: page === currentPage }"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
        
        <button
          type="button"
          class="table-pagination-button"
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          Próximo
          <Icon name="mdi:chevron-right" size="16" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Column {
  key: string
  label: string
  sortable?: boolean
  type?: 'text' | 'number' | 'date' | 'currency' | 'status' | 'custom'
  width?: string
  align?: 'left' | 'center' | 'right'
  format?: (value: unknown) => string
}

interface Filter {
  key: string
  label: string
  options: { label: string; value: string }[]
}

interface Props {
  data: Record<string, unknown>[]
  columns: Column[]
  loading?: boolean
  loadingText?: string
  selectable?: boolean
  searchable?: boolean
  searchPlaceholder?: string
  sortable?: boolean
  paginated?: boolean
  pageSize?: number
  striped?: boolean
  bordered?: boolean
  hover?: boolean
  showToolbar?: boolean
  showAddButton?: boolean
  addButtonText?: string
  showEditAction?: boolean
  showDeleteAction?: boolean
  emptyTitle?: string
  emptyMessage?: string
  emptyIcon?: string
  showEmptyAction?: boolean
  emptyActionText?: string
  filters?: Filter[]
  rowKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [] as Record<string, unknown>[],
  columns: () => [],
  loading: false,
  loadingText: 'Carregando...',
  selectable: false,
  searchable: true,
  searchPlaceholder: 'Pesquisar...',
  sortable: true,
  paginated: true,
  pageSize: 10,
  striped: true,
  bordered: false,
  hover: true,
  showToolbar: true,
  showAddButton: true,
  addButtonText: 'Adicionar',
  showEditAction: true,
  showDeleteAction: true,
  emptyTitle: 'Nenhum registro encontrado',
  emptyMessage: 'Não há dados para exibir no momento.',
  emptyIcon: 'mdi:database-off',
  showEmptyAction: false,
  emptyActionText: 'Adicionar primeiro registro',
  filters: () => [],
  rowKey: 'id'
})

const emit = defineEmits<{
  add: []
  edit: [item: Record<string, unknown>, index: number]
  delete: [item: Record<string, unknown>, index: number]
  'row-click': [item: Record<string, unknown>, index: number]
  'selection-change': [items: Record<string, unknown>[]]
  'empty-action': []
}>()

// Reactive data
const searchQuery = ref('')
const sortBy = ref('')
const sortOrder = ref<'asc' | 'desc'>('asc')
const currentPage = ref(1)
const selectedItems = ref<string[]>([])
const selectAll = ref(false)
const filterValues = ref<Record<string, string>>({})

// Initialize filters
watch(() => props.filters, (newFilters) => {
  const newFilterValues: Record<string, string> = {}
  newFilters?.forEach(filter => {
    newFilterValues[filter.key] = ''
  })
  filterValues.value = newFilterValues
}, { immediate: true })

// Computed properties
const hasActions = computed(() => {
  return props.showEditAction || props.showDeleteAction
})

const totalColumns = computed(() => {
  let count = props.columns.length
  if (props.selectable) count++
  if (hasActions.value) count++
  return count
})

const tableClasses = computed(() => {
  const classes: string[] = []
  if (props.striped) classes.push('striped')
  if (props.bordered) classes.push('bordered')
  if (props.hover) classes.push('hover')
  return classes
})

// Data filtering and sorting
const filteredData = computed(() => {
  let result = [...props.data]
  
  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item => {
      return props.columns.some(column => {
        const value = getColumnValue(item, column.key)
        return String(value).toLowerCase().includes(query)
      })
    })
  }
  
  // Apply column filters
  Object.entries(filterValues.value).forEach(([key, value]) => {
    if (value) {
      result = result.filter(item => {
        const itemValue = getColumnValue(item, key)
        return String(itemValue) === value
      })
    }
  })
  
  // Apply sorting
  if (sortBy.value) {
    result.sort((a, b) => {
      const aValue = getColumnValue(a, sortBy.value)
      const bValue = getColumnValue(b, sortBy.value)
      const aStr = String(aValue ?? '')
      const bStr = String(bValue ?? '')
      
      let comparison = 0
      if (aStr < bStr) comparison = -1
      if (aStr > bStr) comparison = 1
      
      return sortOrder.value === 'desc' ? -comparison : comparison
    })
  }
  
  return result
})

const totalItems = computed(() => filteredData.value.length)
const totalPages = computed(() => Math.ceil(totalItems.value / props.pageSize))

const paginatedData = computed(() => {
  if (!props.paginated) return filteredData.value
  
  const start = (currentPage.value - 1) * props.pageSize
  const end = Math.min(start + props.pageSize, filteredData.value.length)
  return filteredData.value.slice(start, end)
})

const startItem = computed(() => {
  if (totalItems.value === 0) return 0
  return (currentPage.value - 1) * props.pageSize + 1
})

const endItem = computed(() => {
  const end = currentPage.value * props.pageSize
  return Math.min(end, totalItems.value)
})

const visiblePages = computed(() => {
  const pages: number[] = []
  const maxVisible = 5
  const half = Math.floor(maxVisible / 2)
  
  let start = Math.max(1, currentPage.value - half)
  const end = Math.min(totalPages.value, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// Methods
const getColumnValue = (item: Record<string, unknown>, key: string): string | number | boolean | null => {
  const value = key.split('.').reduce((obj: Record<string, unknown> | undefined, k: string): Record<string, unknown> | undefined => {
    return obj && typeof obj === 'object' ? obj[k] as Record<string, unknown> : undefined
  }, item)
  
  if (value === null || value === undefined) {
    return null
  }
  
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return value
  }
  
  return String(value)
}

const getRowKey = (item: Record<string, unknown>, index: number) => {
  return getColumnValue(item, props.rowKey) || index
}

const getHeaderClasses = (column: Column) => {
  const classes: string[] = []
  
  if (column.sortable) {
    classes.push('sortable')
  }
  
  if (sortBy.value === column.key) {
    classes.push('sorted')
  }
  
  if (column.width) {
    classes.push(`w-${column.width}`)
  }
  
  return classes
}

const getCellClasses = (column: Column) => {
  const classes: string[] = ['table-cell']
  
  if (column.type) {
    classes.push(column.type)
  }
  
  if (column.align) {
    const alignClasses = {
      left: 'text-left',
      center: 'text-center',
      right: 'text-right'
    }
    classes.push(alignClasses[column.align])
  }
  
  return classes
}

const getRowClasses = (item: Record<string, unknown>, index: number) => {
  const classes: string[] = []
  
  const key = getRowKey(item, index)
  if (selectedItems.value.includes(String(key))) {
    classes.push('selected')
  }
  
  return classes
}

const getCellComponent = (column: Column) => {
  // Return appropriate cell component based on column type
  switch (column.type) {
    case 'status':
      return 'TableCellStatus'
    case 'currency':
      return 'TableCellCurrency'
    case 'date':
      return 'TableCellDate'
    default:
      return 'TableCellText'
  }
}

const handleSort = (column: Column) => {
  if (!column.sortable) return
  
  if (sortBy.value === column.key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = column.key
    sortOrder.value = 'asc'
  }
}

const handleSelectAll = () => {
  if (selectAll.value) {
    selectedItems.value = paginatedData.value.map((item, index) => 
      String(getRowKey(item, index))
    )
  } else {
    selectedItems.value = []
  }
}

const handleRowClick = (item: Record<string, unknown>, index: number) => {
  emit('row-click', item, index)
}

const handleFilterChange = () => {
  currentPage.value = 1
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// Watch for selection changes
watch(selectedItems, (newSelection) => {
  const items = newSelection.map(key => {
    return filteredData.value.find((item, index) => 
      String(getRowKey(item, index)) === key
    )
  }).filter(Boolean) as Record<string, unknown>[]
  
  emit('selection-change', items)
}, { deep: true })

// Watch for select all state
watch(selectedItems, (newSelection) => {
  const currentPageKeys = paginatedData.value.map((item, index) => 
    String(getRowKey(item, index))
  )
  
  selectAll.value = currentPageKeys.length > 0 && 
    currentPageKeys.every(key => newSelection.includes(key))
}, { deep: true })

// Reset page when data changes
watch(() => props.data, () => {
  currentPage.value = 1
})

// Reset page when search changes
watch(searchQuery, () => {
  currentPage.value = 1
})
</script>