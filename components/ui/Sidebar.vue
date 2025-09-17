<template>
  <div class="sidebar-container">
    <!-- Mobile overlay -->
    <div
      v-if="mobileMenuOpen"
      class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
      @click="closeMobileMenu"
    ></div>

    <!-- Sidebar -->
    <nav
      :class="[
        'fixed top-0 left-0 h-screen bg-slate-900 border-r border-slate-700 transform transition-all duration-300 ease-in-out z-[100]',
        'lg:translate-x-0',
        collapsed ? 'w-16' : 'w-64',
        mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0',
      ]"
    >
      <!-- Header/Brand -->
      <div
        class="flex items-center justify-between p-4 border-b border-slate-700"
      >
        <div class="flex items-center gap-3">
          <BrandMark size="sm" />
          <h1
            v-show="!collapsed"
            class="text-lg font-bold text-white transition-opacity duration-300"
          >
            ForCon
          </h1>
        </div>
        <div class="flex items-center gap-2">
          <button
            @click="toggleCollapse"
            class="hidden lg:block p-1 rounded-md text-gray-400 hover:text-white hover:bg-gray-800 transition-colors"
            :title="collapsed ? 'Expandir sidebar' : 'Recolher sidebar'"
          >
            <i
              :class="
                collapsed
                  ? 'fa-solid fa-chevron-right'
                  : 'fa-solid fa-chevron-left'
              "
              class="text-sm"
            ></i>
          </button>
          <button
            v-if="mobileMenuOpen"
            @click="closeMobileMenu"
            class="lg:hidden p-1 rounded-md text-gray-400 hover:text-white hover:bg-gray-800"
          >
            <i class="fa-solid fa-times text-lg"></i>
          </button>
        </div>
      </div>

      <!-- User info -->
      <div class="p-4 border-b border-slate-700">
        <div class="flex items-center gap-3">
          <div
            class="w-8 h-8 bg-indigo-600 rounded-full flex items-center justify-center cursor-pointer hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
            :title="collapsed ? currentUser?.name : ''"
          >
            <i class="fa-solid fa-user text-white text-sm"></i>
          </div>
          <div
            v-show="!collapsed"
            class="flex-1 min-w-0 transition-opacity duration-300"
          >
            <p class="text-sm font-medium text-white truncate">
              {{ currentUser?.name || "Usuário" }}
            </p>
            <p class="text-xs text-gray-400 truncate">
              {{ currentUser?.email || "user@example.com" }}
            </p>
          </div>
        </div>
      </div>

      <!-- Navigation Menu -->
      <div class="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
        <SidebarItem
          v-for="item in menuItems"
          :key="item.path"
          :item="item"
          :is-active="isActive(item.path)"
          :collapsed="collapsed"
          @click="navigateTo"
          @fetch-esteira="handleEsteiraFetch"
        />
      </div>

      <!-- Footer/Settings -->
      <div class="p-4 border-t border-slate-700">
        <SidebarItem
          :item="settingsItem"
          :is-active="false"
          :collapsed="collapsed"
          @click="openSettings"
        />
        <button
          @click="logout"
          :class="[
            'w-full mt-2 flex items-center gap-3 px-3 py-2 text-gray-300 hover:text-white hover:bg-gray-800 rounded-md transition-colors text-sm',
            collapsed ? 'justify-center' : '',
          ]"
          :title="collapsed ? 'Sair' : ''"
        >
          <i class="fa-solid fa-sign-out-alt w-5"></i>
          <span v-show="!collapsed" class="transition-opacity duration-300"
            >Sair</span
          >
        </button>
      </div>
    </nav>

    <!-- Mobile menu button -->
    <button
      v-if="!mobileMenuOpen"
      @click="openMobileMenu"
      class="fixed top-4 left-4 z-[110] lg:hidden p-2 rounded-md bg-gray-900 text-white hover:bg-gray-800 transition-colors"
    >
      <i class="fa-solid fa-bars text-lg"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "#imports";
import BrandMark from "./BrandMark.vue";
import SidebarItem from "./SidebarItem.vue";
import { useProcesses } from "~/composables/useProcesses";

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
});

// Emits
const emit = defineEmits(["update:modelValue", "collapsed"]);

// Composables
const route = useRoute();
const router = useRouter();

// State
const currentUser = ref({
  name: "Admin User",
  email: "admin@docheck.com",
});

const collapsed = ref(false);

// Composable para processos
const {
  processMenuItems,
  fetchProcesses,
  loading: loadingProcesses,
} = useProcesses();

// Load collapsed state from localStorage on mount
onMounted(() => {
  // console.log('SIDEBAR MOUNTED - Component is loading!')
  const savedState = localStorage.getItem("sidebar-collapsed");
  if (savedState !== null) {
    collapsed.value = JSON.parse(savedState);
    emit("collapsed", collapsed.value);
  }

  // Não carregar processos automaticamente mais
  // Eles serão carregados quando clicar na Esteira
});

// Mobile menu state
const mobileMenuOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

// Menu items configuration - computed para reagir aos processos
const menuItems = computed(() => [
  {
    label: "Dashboard",
    path: "/",
    icon: "fa-solid fa-home",
    description: "Visão geral do sistema",
  },
  {
    label: "Esteira",
    path: "/esteira",
    icon: "fa-solid fa-sitemap",
    description: "Gerenciar fluxos de trabalho",
    children: loadingProcesses.value
      ? [
          {
            label: "Carregando...",
            path: "/esteira/loading",
            icon: "fa-solid fa-spinner fa-spin",
            description: "Carregando processos",
          },
        ]
      : processMenuItems.value.length > 0
        ? processMenuItems.value
        : [
            {
              label: "Nenhum processo ativo",
              path: "/esteira/empty",
              icon: "fa-solid fa-info-circle",
              description: "Nenhum processo ativo encontrado",
            },
          ],
  },
  {
    label: "Processos",
    path: "/processos",
    icon: "fa-solid fa-clipboard-list",
    description: "Gerenciar processos",
  },
  {
    label: "Administração",
    path: "/admin",
    icon: "fa-solid fa-cog",
    description: "Configurações do sistema",
    roles: ["admin"], // Apenas para admins
    children: [
      {
        label: "Usuários",
        path: "/admin/users",
        icon: "fa-solid fa-users",
        description: "Gerenciar usuários",
      },
      {
        label: "Esteira",
        path: "/admin/pipeline",
        icon: "fa-solid fa-sitemap",
        description: "Gerenciar pipeline",
      },
      {
        label: "Status",
        path: "/admin/status",
        icon: "fa-solid fa-circle-info",
        description: "Gerenciar status",
      },
      {
        label: "Notificações",
        path: "/admin/notifications",
        icon: "fa-solid fa-bell",
        description: "Gerenciar notificações",
      },
    ],
  },
]);

const settingsItem = {
  label: "Configurações",
  path: "/settings",
  icon: "fa-solid fa-gear",
  description: "Configurações da conta",
};

// Methods
function isActive(path) {
  if (path === "/") {
    return route.path === "/";
  }
  return route.path.startsWith(path);
}

async function navigateTo(item) {
  // Se o item tem children e a sidebar está colapsada, expandir automaticamente
  if (item.children && item.children.length > 0 && collapsed.value) {
    collapsed.value = false;
    localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed.value));
    emit("collapsed", collapsed.value);
  }

  // Sempre navegar para a página, exceto para Esteira principal que só expande os submenus
  if (item?.path && item.path !== "/esteira") {
    router.push(item.path);
  }

  if (mobileMenuOpen.value) {
    closeMobileMenu();
  }
}

// Função específica para fetch da Esteira
async function handleEsteiraFetch() {
  console.log("🎯 Clicou no ícone da Esteira - buscando processos...");
  await fetchProcesses();
  console.log("✅ Processos atualizados:", processMenuItems.value);
}

function openMobileMenu() {
  mobileMenuOpen.value = true;
}

function closeMobileMenu() {
  mobileMenuOpen.value = false;
}

function openSettings() {
  navigateTo(settingsItem);
}

function logout() {
  // Implementar logout
  console.log("Logout clicked");
  router.push("/login");
}

function toggleCollapse() {
  collapsed.value = !collapsed.value;
  localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed.value));
  emit("collapsed", collapsed.value);
}

// Close mobile menu when route changes
watch(
  () => route.path,
  () => {
    if (mobileMenuOpen.value) {
      closeMobileMenu();
    }
  }
);
</script>

<style scoped>
.sidebar-container {
  /* Ensure proper stacking context */
}

/* Scroll styling for webkit browsers */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #1f2937;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #4b5563;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}

/* Smooth transitions for collapse/expand */
nav {
  transition: width 0.3s ease-in-out;
}

/* Ensure text animations work properly */
.transition-opacity {
  transition: opacity 0.3s ease-in-out;
}

/* Custom animations for mobile overlay */
@media (max-width: 1023px) {
  nav {
    width: 16rem !important; /* 64 = w-64 */
    transform: translateX(-100%) !important;
  }

  nav.mobile-open {
    transform: translateX(0) !important;
  }
}

/* Ensure proper mobile behavior */
@media (min-width: 1024px) {
  nav {
    transform: translateX(0) !important;
  }

  /* Hide mobile menu button on desktop */
  .mobile-menu-btn {
    display: none;
  }
}

/* Improve touch targets on mobile */
@media (max-width: 1023px) {
  button {
    min-height: 44px;
  }

  .sidebar-item {
    padding: 12px;
  }
}

/* Add subtle shadows and depth */
nav {
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  height: 100vh !important;
  z-index: 100 !important;
}
</style>
