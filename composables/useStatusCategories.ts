import { ref, computed, readonly } from 'vue'
import { isApiEnabled, apiFetch } from '~/utils/api/index'

export type StatusItem = {
  id: string
  name: string
  description?: string
  color: string
  categoryId: string
  createdAt: string
  updatedAt: string
}

export type StatusCategory = {
  id: string
  name: string
  description: string
  icon: string
  statuses: StatusItem[]
  createdAt: string
  updatedAt: string
}

const CATEGORIES_STORAGE_KEY = 'status-categories'

// Local Storage helpers
function saveToLocalStorage(categories: StatusCategory[]) {
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(CATEGORIES_STORAGE_KEY, JSON.stringify(categories))
  }
}

function loadFromLocalStorage(): StatusCategory[] {
  if (typeof localStorage === 'undefined') return []

  try {
    const saved = localStorage.getItem(CATEGORIES_STORAGE_KEY)
    return saved ? JSON.parse(saved) : []
  } catch (error) {
    console.error('Error loading categories from localStorage:', error)
    return []
  }
}

// API helpers
async function fetchCategoriesApi(): Promise<StatusCategory[]> {
  if (!isApiEnabled()) return []
  try {
    return await apiFetch<StatusCategory[]>('/api/v1/status-categories')
  } catch (error) {
    console.error('Error fetching categories:', error)
    return []
  }
}

async function createCategoryApi(data: { name: string; description: string; icon: string }): Promise<StatusCategory | null> {
  if (!isApiEnabled()) return null
  try {
    return await apiFetch<StatusCategory>('/api/v1/status-categories', {
      method: 'POST',
      body: data
    })
  } catch (error) {
    console.error('Error creating category:', error)
    return null
  }
}

async function updateCategoryApi(id: string, data: { name: string; description: string; icon: string }): Promise<StatusCategory | null> {
  if (!isApiEnabled()) return null
  try {
    return await apiFetch<StatusCategory>(`/api/v1/status-categories/${id}`, {
      method: 'PUT',
      body: data
    })
  } catch (error) {
    console.error('Error updating category:', error)
    return null
  }
}

async function deleteCategoryApi(id: string): Promise<boolean> {
  if (!isApiEnabled()) return false
  try {
    await apiFetch<void>(`/api/v1/status-categories/${id}`, {
      method: 'DELETE'
    })
    return true
  } catch (error) {
    console.error('Error deleting category:', error)
    return false
  }
}

async function createStatusApi(categoryId: string, data: { name: string; description?: string; color: string }): Promise<StatusCategory | null> {
  if (!isApiEnabled()) return null
  try {
    return await apiFetch<StatusCategory>(`/api/v1/status-categories/${categoryId}/statuses`, {
      method: 'POST',
      body: data
    })
  } catch (error) {
    console.error('Error creating status:', error)
    return null
  }
}

async function updateStatusApi(categoryId: string, statusId: string, data: { name: string; description?: string; color: string }): Promise<StatusCategory | null> {
  if (!isApiEnabled()) return null
  try {
    return await apiFetch<StatusCategory>(`/api/v1/status-categories/${categoryId}/statuses/${statusId}`, {
      method: 'PUT',
      body: data
    })
  } catch (error) {
    console.error('Error updating status:', error)
    return null
  }
}

async function deleteStatusApi(categoryId: string, statusId: string): Promise<StatusCategory | null> {
  if (!isApiEnabled()) return null
  try {
    return await apiFetch<StatusCategory>(`/api/v1/status-categories/${categoryId}/statuses/${statusId}`, {
      method: 'DELETE'
    })
  } catch (error) {
    console.error('Error deleting status:', error)
    return null
  }
}

export function useStatusCategories() {
  const categories = ref<StatusCategory[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Load categories
  const loadCategories = async () => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const apiCategories = await fetchCategoriesApi()
        categories.value = apiCategories
      } else {
        categories.value = loadFromLocalStorage()
      }
    } catch (err) {
      error.value = 'Erro ao carregar categorias'
      console.error('Error loading categories:', err)
    } finally {
      loading.value = false
    }
  }

  // Create category
  const createCategory = async (data: { name: string; description: string; icon: string }) => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const newCategory = await createCategoryApi(data)
        if (newCategory) {
          categories.value.push(newCategory)
        }
      } else {
        // Local storage fallback
        const newCategory: StatusCategory = {
          id: Date.now().toString(),
          ...data,
          statuses: [],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
        categories.value.push(newCategory)
        saveToLocalStorage(categories.value)
      }
    } catch (err) {
      error.value = 'Erro ao criar categoria'
      console.error('Error creating category:', err)
    } finally {
      loading.value = false
    }
  }

  // Update category
  const updateCategory = async (id: string, data: { name: string; description: string; icon: string }) => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const updatedCategory = await updateCategoryApi(id, data)
        if (updatedCategory) {
          const index = categories.value.findIndex(c => c.id === id)
          if (index !== -1) {
            categories.value[index] = updatedCategory
          }
        }
      } else {
        // Local storage fallback
        const index = categories.value.findIndex(c => c.id === id)
        if (index !== -1) {
          categories.value[index] = {
            ...categories.value[index],
            ...data,
            updatedAt: new Date().toISOString()
          }
          saveToLocalStorage(categories.value)
        }
      }
    } catch (err) {
      error.value = 'Erro ao atualizar categoria'
      console.error('Error updating category:', err)
    } finally {
      loading.value = false
    }
  }

  // Delete category
  const deleteCategory = async (id: string) => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const success = await deleteCategoryApi(id)
        if (success) {
          categories.value = categories.value.filter(c => c.id !== id)
        }
      } else {
        // Local storage fallback
        categories.value = categories.value.filter(c => c.id !== id)
        saveToLocalStorage(categories.value)
      }
    } catch (err) {
      error.value = 'Erro ao excluir categoria'
      console.error('Error deleting category:', err)
    } finally {
      loading.value = false
    }
  }

  // Create status
  const createStatus = async (categoryId: string, data: { name: string; description?: string; color: string }) => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const updatedCategory = await createStatusApi(categoryId, data)
        if (updatedCategory) {
          const index = categories.value.findIndex(c => c.id === categoryId)
          if (index !== -1) {
            categories.value[index] = updatedCategory
          }
        }
      } else {
        // Local storage fallback
        const categoryIndex = categories.value.findIndex(c => c.id === categoryId)
        if (categoryIndex !== -1) {
          const newStatus: StatusItem = {
            id: Date.now().toString(),
            ...data,
            categoryId,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          }
          categories.value[categoryIndex].statuses.push(newStatus)
          categories.value[categoryIndex].updatedAt = new Date().toISOString()
          saveToLocalStorage(categories.value)
        }
      }
    } catch (err) {
      error.value = 'Erro ao criar status'
      console.error('Error creating status:', err)
    } finally {
      loading.value = false
    }
  }

  // Update status
  const updateStatus = async (categoryId: string, statusId: string, data: { name: string; description?: string; color: string }) => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const updatedCategory = await updateStatusApi(categoryId, statusId, data)
        if (updatedCategory) {
          const index = categories.value.findIndex(c => c.id === categoryId)
          if (index !== -1) {
            categories.value[index] = updatedCategory
          }
        }
      } else {
        // Local storage fallback
        const categoryIndex = categories.value.findIndex(c => c.id === categoryId)
        if (categoryIndex !== -1) {
          const statusIndex = categories.value[categoryIndex].statuses.findIndex(s => s.id === statusId)
          if (statusIndex !== -1) {
            categories.value[categoryIndex].statuses[statusIndex] = {
              ...categories.value[categoryIndex].statuses[statusIndex],
              ...data,
              updatedAt: new Date().toISOString()
            }
            categories.value[categoryIndex].updatedAt = new Date().toISOString()
            saveToLocalStorage(categories.value)
          }
        }
      }
    } catch (err) {
      error.value = 'Erro ao atualizar status'
      console.error('Error updating status:', err)
    } finally {
      loading.value = false
    }
  }

  // Delete status
  const deleteStatus = async (categoryId: string, statusId: string) => {
    loading.value = true
    error.value = null

    try {
      if (isApiEnabled()) {
        const updatedCategory = await deleteStatusApi(categoryId, statusId)
        if (updatedCategory) {
          const index = categories.value.findIndex(c => c.id === categoryId)
          if (index !== -1) {
            categories.value[index] = updatedCategory
          }
        }
      } else {
        // Local storage fallback
        const categoryIndex = categories.value.findIndex(c => c.id === categoryId)
        if (categoryIndex !== -1) {
          categories.value[categoryIndex].statuses = categories.value[categoryIndex].statuses.filter(s => s.id !== statusId)
          categories.value[categoryIndex].updatedAt = new Date().toISOString()
          saveToLocalStorage(categories.value)
        }
      }
    } catch (err) {
      error.value = 'Erro ao excluir status'
      console.error('Error deleting status:', err)
    } finally {
      loading.value = false
    }
  }

  // Computed values
  const totalStatusCount = computed(() => {
    return categories.value.reduce((total, category) => total + category.statuses.length, 0)
  })

  return {
    categories: readonly(categories),
    loading: readonly(loading),
    error: readonly(error),
    totalStatusCount,
    loadCategories,
    createCategory,
    updateCategory,
    deleteCategory,
    createStatus,
    updateStatus,
    deleteStatus
  }
}