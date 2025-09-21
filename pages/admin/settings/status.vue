<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900"
  >
    <header
      class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 p-4"
    >
      <div class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"></div>

      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-4">
          <!-- Enhanced icon with glow -->
          <div class="relative group">
            <div
              class="absolute inset-0 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl blur-lg opacity-50 group-hover:opacity-75 transition-opacity duration-300"
            ></div>
            <div>
              <i class="fa-solid fa-tags text-white text-lg"></i>
            </div>
          </div>

          <div>
            <p class="app-header-subtitle">
              Configure status e categorias do sistema com total controle
            </p>
          </div>
        </div>

        <!-- Enhanced CTA button -->
        <button class="p-2 rounded-xl" @click="openNewStatusModal">
          <i class="fa-solid fa-tags text-purple-300"></i>
        </button>
      </div>
    </header>

    <main class="p-6 relative z-10">
      <!-- Enhanced Category Filter -->
      <div class="mb-8">
        <div class="app-tabs overflow-x-auto">
          <button
            type="button"
            class="app-tab"
            :class="{ 'app-tab--active': selectedCategory === null }"
            @click="filterByCategory(null)"
          >
            Todos os Status
          </button>
          <button
            v-for="cat in statusCategories"
            :key="cat.value"
            type="button"
            class="app-tab"
            :class="{ 'app-tab--active': selectedCategory === cat.value }"
            @click="filterByCategory(cat.value)"
          >
            {{ cat.label }}
          </button>
        </div>
      </div>

      <!-- Enhanced Status List -->
      <div class="relative">
        <!-- Glass morphism container -->
        <div
          class="bg-slate-900/40 backdrop-blur-2xl border border-white/10 rounded-2xl overflow-hidden shadow-2xl"
        >
          <!-- Empty state -->
          <div v-if="filteredStatusList.length === 0" class="p-12 text-center">
            <div class="relative inline-block mb-6">
              <div
                class="relative w-16 h-16 bg-gradient-to-br from-slate-800 to-slate-900 rounded-full flex items-center justify-center border border-slate-700/50"
              >
                <i class="fa-solid fa-circle-info text-2xl text-slate-400"></i>
              </div>
            </div>

            <h3
              class="text-xl font-semibold text-slate-200 mb-2"
              v-if="statusList.length === 0"
            >
              Nenhum status cadastrado
            </h3>
            <h3 class="text-xl font-semibold text-slate-200 mb-2" v-else>
              Nenhum status encontrado
            </h3>

            <p class="text-slate-400 mb-4" v-if="statusList.length === 0">
              Comece criando seu primeiro status para organizar os processos
            </p>
            <p class="text-slate-400 mb-4" v-else>
              Não há status para a categoria selecionada
            </p>

            <button
              v-if="statusList.length === 0"
              @click="openNewStatusModal"
              class="inline-flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-6 py-3 rounded-xl font-medium transition-all duration-300"
            >
              <i class="fa-solid fa-plus"></i>
              Criar Primeiro Status
            </button>
          </div>

          <!-- Status grid -->
          <div v-else class="p-3">
            <div
              class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8 gap-2"
            >
              <div
                v-for="(status, index) in filteredStatusList"
                :key="status.id"
                class="group relative overflow-hidden bg-gradient-to-br from-slate-800/50 to-slate-900/50 backdrop-blur-sm border border-slate-700/30 rounded-lg p-3 hover:border-slate-600/50 transition-all duration-300 transform hover:scale-105 hover:shadow-lg animate-fade-in-up"
                :style="{ animationDelay: `${index * 30}ms` }"
              >
                <!-- Card glow effect -->
                <div
                  class="absolute inset-0 bg-gradient-to-br opacity-0 group-hover:opacity-5 transition-opacity duration-300 rounded-lg"
                  :style="{
                    background: `linear-gradient(135deg, ${status.color}20, transparent)`,
                  }"
                ></div>

                <!-- Action buttons -->
                <div
                  class="absolute top-0 right-1 flex items-center gap-0.5 opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 transition-opacity duration-300"
                >
                  <button
                    class="p-1 text-slate-400 hover:text-white hover:bg-slate-700/70 rounded transition-all duration-200 transform hover:scale-110 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-400"
                    @click="editStatus(status)"
                    title="Editar status"
                  >
                    <i class="fa-solid fa-pen text-[8px]"></i>
                  </button>
                  <button
                    class="p-1 text-red-400 hover:text-red-300 hover:bg-red-900/30 rounded transition-all duration-200 transform hover:scale-110 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-400"
                    @click="deleteStatus(status.id)"
                    title="Excluir status"
                  >
                    <i class="fa-solid fa-trash text-[8px]"></i>
                  </button>
                </div>

                <!-- Status content -->
                <div class="space-y-1.5">
                  <!-- Status badge -->
                  <div
                    class="inline-flex items-center gap-1 px-2 py-1 rounded font-medium text-xs w-full transition-all duration-300 group-hover:scale-105"
                    :style="getEnhancedStatusStyle(status)"
                  >
                    <div
                      class="w-1.5 h-1.5 rounded-full flex-shrink-0"
                      :style="{ backgroundColor: status.color }"
                    ></div>
                    <span class="truncate text-xs">{{ status.name }}</span>
                  </div>

                  <!-- Category badge -->
                  <div class="flex justify-center">
                    <span
                      class="text-[9px] px-1.5 py-0.5 bg-slate-700/50 text-slate-400 rounded font-medium truncate"
                    >
                      {{ getCategoryLabel(status.category) }}
                    </span>
                  </div>
                </div>

                <!-- Hover effect overlay -->
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Enhanced Modal: Status Management -->
    <BaseModal
      v-model="showStatusModal"
      :title="editingStatus ? 'Editar Status' : 'Novo Status'"
      size="md"
      :z-index="70"
    >
      <div class="space-y-6">
        <!-- Form fields with enhanced styling -->
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-200 mb-2"
              >Nome do Status</label
            >
            <input
              v-model="statusForm.name"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-slate-100 rounded-xl px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
              placeholder="Ex.: Em análise, Aprovado, Rejeitado..."
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-200 mb-2"
              >Categoria</label
            >
            <select
              v-model="statusForm.category"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-slate-100 rounded-xl px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
            >
              <option
                v-for="cat in statusCategories"
                :key="cat.value"
                :value="cat.value"
                class="bg-slate-800 text-slate-100"
              >
                {{ cat.label }}
              </option>
            </select>
            <p class="text-xs text-slate-400 mt-2 flex items-center gap-1">
              <i class="fa-solid fa-info-circle"></i>
              Define onde este status será usado no sistema
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-200 mb-3"
              >Cor do Status</label
            >
            <div class="grid grid-cols-5 gap-3">
              <button
                v-for="color in statusColors"
                :key="color.value"
                class="group relative w-12 h-12 rounded-xl border-2 transition-all duration-300 transform hover:scale-110 hover:shadow-lg"
                :class="
                  statusForm.color === color.value
                    ? 'border-white shadow-lg scale-105'
                    : 'border-slate-600/50 hover:border-slate-500'
                "
                :style="{ backgroundColor: color.value }"
                :title="color.name"
                @click="statusForm.color = color.value"
              >
                <!-- Selection indicator -->
                <div
                  v-if="statusForm.color === color.value"
                  class="absolute inset-0 flex items-center justify-center"
                >
                  <i
                    class="fa-solid fa-check text-white text-sm font-bold drop-shadow-md"
                  ></i>
                </div>
                <!-- Hover glow -->
                <div
                  class="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-20 transition-opacity duration-300"
                  :style="{ backgroundColor: color.value, filter: 'blur(8px)' }"
                ></div>
              </button>
            </div>
          </div>
        </div>

        <!-- Enhanced Preview Section -->
        <div
          class="p-6 bg-gradient-to-br from-slate-800/50 to-slate-900/50 rounded-xl border border-slate-700/30 backdrop-blur-sm"
        >
          <div class="flex items-center gap-2 mb-4">
            <i class="fa-solid fa-eye text-purple-400"></i>
            <span class="text-sm font-medium text-slate-200"
              >Pré-visualização</span
            >
          </div>

          <div class="flex items-center gap-4">
            <!-- Status badge preview -->
            <div
              class="inline-flex items-center gap-2 px-4 py-2 rounded-lg font-medium text-sm transition-all duration-300"
              :style="
                getEnhancedStatusStyle({
                  color: statusForm.color,
                  name: statusForm.name,
                })
              "
            >
              <div
                class="w-2 h-2 rounded-full"
                :style="{ backgroundColor: statusForm.color }"
              ></div>
              {{ statusForm.name || "Nome do Status" }}
            </div>

            <!-- Category badge preview -->
            <span
              class="text-xs px-3 py-1 bg-slate-700/50 text-slate-300 rounded-lg font-medium border border-slate-600/30"
            >
              {{ getCategoryLabel(statusForm.category) }}
            </span>
          </div>

          <!-- Color info -->
          <div class="mt-4 flex items-center gap-2 text-xs text-slate-400">
            <i class="fa-solid fa-palette"></i>
            <span class="font-mono">{{ statusForm.color }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div
          class="flex items-center justify-end gap-3 pt-4 border-t border-slate-700/30"
        >
          <button
            class="px-5 py-2.5 bg-slate-700/50 hover:bg-slate-600/50 text-slate-300 hover:text-white rounded-xl text-sm font-medium transition-all duration-300 backdrop-blur-sm"
            @click="closeStatusModal"
          >
            Cancelar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-6 py-2.5 rounded-xl text-sm font-semibold transition-all duration-300"
            @click="saveStatus"
          >
            {{ editingStatus ? "Atualizar Status" : "Criar Status" }}
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import {
  fetchStatusesApi,
  createStatusApi,
  updateStatusApi,
  deleteStatusApi,
} from "~/composables/useStatusesApi";
import { STATUS_CATEGORIES } from "~/composables/useStatuses";
import { useToast } from "~/composables/useToast";

definePageMeta({
  layout: "sidebar",
  title: "Status - Admin",
});

// Composables
const { success: toastSuccess, error: toastError } = useToast();

// Status management
const statusList = ref([]);
const statusCategories = STATUS_CATEGORIES;
const selectedCategory = ref(null);
const showStatusModal = ref(false);
const statusForm = ref({
  id: null,
  name: "",
  color: "#3b82f6",
  category: "ESTEIRA",
});
const editingStatus = ref(false);

const statusColors = [
  { name: "Azul", value: "#3b82f6" },
  { name: "Verde", value: "#10b981" },
  { name: "Vermelho", value: "#ef4444" },
  { name: "Amarelo", value: "#f59e0b" },
  { name: "Roxo", value: "#8b5cf6" },
  { name: "Rosa", value: "#ec4899" },
  { name: "Laranja", value: "#f97316" },
  { name: "Ciano", value: "#06b6d4" },
  { name: "Lima", value: "#65a30d" },
  { name: "Índigo", value: "#6366f1" },
];

// Computed properties
const filteredStatusList = computed(() => {
  if (selectedCategory.value === null) {
    return statusList.value;
  }
  return statusList.value.filter(
    (status) => status.category === selectedCategory.value
  );
});

// Status management functions
async function loadStatusList() {
  try {
    console.log("Loading status list...");
    statusList.value = await fetchStatusesApi();
    console.log("Status list loaded:", statusList.value.length, "items");
  } catch (error) {
    console.error("Failed to load status list:", error);
    toastError("Erro ao carregar status");
  }
}

function filterByCategory(category) {
  selectedCategory.value = category;
}

function getCategoryLabel(category) {
  const cat = statusCategories.find((c) => c.value === category);
  return cat ? cat.label : "Desconhecido";
}

function openNewStatusModal() {
  statusForm.value = {
    id: null,
    name: "",
    color: "#3b82f6",
    category: "ESTEIRA",
  };
  editingStatus.value = false;
  showStatusModal.value = true;
}

function editStatus(status) {
  statusForm.value = {
    id: status.id,
    name: status.name,
    color: status.color,
    category: status.category,
  };
  editingStatus.value = true;
  showStatusModal.value = true;
}

async function saveStatus() {
  try {
    const { id, name, color, category } = statusForm.value;

    if (!name.trim()) {
      toastError("Nome é obrigatório");
      return;
    }

    if (editingStatus.value && id) {
      // Update existing status
      const updated = await updateStatusApi(id, {
        name: name.trim(),
        color,
        category,
      });
      if (updated) {
        toastSuccess("Status atualizado com sucesso");
        await loadStatusList();
        showStatusModal.value = false;
      }
    } else {
      // Create new status
      const created = await createStatusApi({
        name: name.trim(),
        color,
        category,
      });
      if (created) {
        toastSuccess("Status criado com sucesso");
        await loadStatusList();
        showStatusModal.value = false;
      }
    }
  } catch (error) {
    console.error("Error saving status:", error);
    toastError("Erro ao salvar status");
  }
}

async function deleteStatus(statusId) {
  if (!confirm("Tem certeza que deseja excluir este status?")) {
    return;
  }

  try {
    const success = await deleteStatusApi(statusId);
    if (success) {
      toastSuccess("Status excluído com sucesso");
      await loadStatusList();
    } else {
      toastError("Erro ao excluir status");
    }
  } catch (error) {
    console.error("Error deleting status:", error);
    toastError("Erro ao excluir status");
  }
}

function closeStatusModal() {
  showStatusModal.value = false;
  statusForm.value = {
    id: null,
    name: "",
    color: "#3b82f6",
    category: "ESTEIRA",
  };
  editingStatus.value = false;
}

// Helper function to convert hex to rgba
function hexToRgba(hex, alpha = 0.2) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Enhanced function to get dynamic status style with modern effects
function getEnhancedStatusStyle(status) {
  if (!status?.color) return "";
  const baseColor = status.color;
  return `
    background: linear-gradient(135deg, ${hexToRgba(baseColor, 0.15)}, ${hexToRgba(baseColor, 0.05)});
    color: ${baseColor};
    border: 1px solid ${hexToRgba(baseColor, 0.3)};
    box-shadow: 0 4px 12px ${hexToRgba(baseColor, 0.15)};
  `
    .replace(/\s+/g, " ")
    .trim();
}

// Load status on mount
onMounted(() => {
  loadStatusList();
});
</script>

<style scoped>
/* Modern Animations and Effects */

/* Shimmer animation for header */
@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.animate-shimmer {
  animation: shimmer 3s ease-in-out infinite;
}

/* Fade in up animation for cards */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out both;
}

/* Pulse animation for background particles */
@keyframes pulse {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.05);
  }
}

/* Enhanced scroll behavior */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(51, 65, 85, 0.2);
  border-radius: 6px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(
    135deg,
    rgba(139, 92, 246, 0.4),
    rgba(99, 102, 241, 0.4)
  );
  border-radius: 6px;
  transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(
    135deg,
    rgba(139, 92, 246, 0.6),
    rgba(99, 102, 241, 0.6)
  );
}

/* Glassmorphism effects */
.backdrop-blur-2xl {
  backdrop-filter: blur(24px);
}

.backdrop-blur-3xl {
  backdrop-filter: blur(32px);
}

/* Enhanced button hover effects */
.group:hover .group-hover\:scale-110 {
  transform: scale(1.1);
}

.group:hover .group-hover\:scale-105 {
  transform: scale(1.05);
}

/* Focus ring enhancement */
.focus\:ring-purple-500\/50:focus {
  --tw-ring-color: rgba(139, 92, 246, 0.5);
}

/* Card glow effects */
.hover\:shadow-xl:hover {
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04),
    0 0 30px rgba(139, 92, 246, 0.1);
}

.hover\:shadow-purple-500\/25:hover {
  box-shadow:
    0 10px 15px -3px rgba(139, 92, 246, 0.25),
    0 4px 6px -2px rgba(139, 92, 246, 0.05);
}

/* Smooth transitions for all interactive elements */
* {
  transition-property:
    color, background-color, border-color, text-decoration-color, fill, stroke,
    opacity, box-shadow, transform, filter, backdrop-filter;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

/* Enhanced color picker glow */
.group:hover .opacity-0 {
  transition: opacity 0.3s ease;
}

/* Responsive animations - reduce motion for accessibility */
@media (prefers-reduced-motion: reduce) {
  .animate-shimmer,
  .animate-fade-in-up,
  .animate-pulse {
    animation: none;
  }

  * {
    transition-duration: 0.01ms !important;
  }
}

/* Dark mode optimizations */
@media (prefers-color-scheme: dark) {
  ::-webkit-scrollbar-track {
    background: rgba(30, 41, 59, 0.3);
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .border-white\/10 {
    border-color: rgba(255, 255, 255, 0.3);
  }

  .border-slate-700\/50 {
    border-color: rgba(51, 65, 85, 0.8);
  }
}
</style>
