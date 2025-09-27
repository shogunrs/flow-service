<template>
  <div class="sidebar-container">
    <!-- Mobile overlay -->
    <div
      v-if="mobileMenuOpen"
      class="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 lg:hidden"
      @click="closeMobileMenu"
    ></div>

    <!-- Sidebar -->
    <nav
      :class="[
        'fixed top-0 left-0 h-screen bg-gradient-to-b from-slate-950 via-slate-900/95 to-slate-950/90 border-r border-slate-900/60 backdrop-blur-xl transform transition-all duration-300 ease-in-out z-[100]',
        'lg:translate-x-0',
        collapsed ? 'w-16' : 'w-64',
        mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0',
      ]"
    >
      <div class="flex flex-col h-full">
        <!-- User Capsule -->
        <div class="px-4 pt-6 pb-5 border-b border-slate-900/70">
          <div class="flex items-center gap-3">
            <div
              class="relative flex-shrink-0 overflow-hidden rounded-2xl ring-2 ring-indigo-500/40 shadow-lg shadow-indigo-500/10 bg-slate-900/70"
              :class="collapsed ? 'w-10 h-10 rounded-full cursor-pointer' : 'w-12 h-12'"
              @click="collapsed && toggleCollapse()"
            >
              <img
                v-if="userAvatar"
                :src="userAvatar"
                alt="Avatar"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center text-sm font-semibold text-indigo-200"
              >
                {{ userInitials }}
              </div>
            </div>
            <div v-show="!collapsed" class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-slate-100 truncate">
                {{ currentUserDisplay.name }}
              </p>
              <p class="text-xs text-slate-400 truncate">
                {{ currentUserDisplay.email }}
              </p>
            </div>
            <button
              v-if="!collapsed"
              @click="toggleCollapse"
              class="hidden lg:inline-flex items-center justify-center w-9 h-9 rounded-xl bg-slate-900/60 text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              :title="collapsed ? 'Expandir sidebar' : 'Recolher sidebar'"
            >
              <i
                :class="
                  collapsed
                    ? 'fa-solid fa-chevron-right'
                    : 'fa-solid fa-chevron-left'
                "
              ></i>
            </button>
            <button
              v-if="mobileMenuOpen"
              @click="closeMobileMenu"
              class="lg:hidden inline-flex items-center justify-center w-9 h-9 rounded-xl bg-slate-900/60 text-slate-300 hover:text-white"
            >
              <i class="fa-solid fa-times"></i>
            </button>
          </div>
          <div v-show="!collapsed" class="mt-4 flex items-center gap-2 text-[11px] text-slate-400">
            <span class="inline-flex items-center gap-1 px-2 py-1 rounded-lg bg-slate-900/70 border border-slate-800/70 uppercase tracking-wide">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
              Online
            </span>
            <span v-if="currentUserRoles.length" class="truncate">
              {{ currentUserRoles.join(' • ') }}
            </span>
          </div>
        </div>

        <!-- Navigation -->
        <div class="flex-1 px-3 py-6 space-y-2 overflow-y-auto">
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

        <!-- Footer -->
        <div class="px-4 pb-5 pt-4 border-t border-slate-900/70 backdrop-blur-sm">
          <SidebarItem
            :item="settingsItem"
            :is-active="false"
            :collapsed="collapsed"
            @click="openSettings"
          />

          <button
            @click="logout"
            :class="[
              'w-full mt-2 flex items-center gap-3 px-3 py-2 text-slate-300 hover:text-white hover:bg-slate-800/70 rounded-xl transition-colors text-sm',
              collapsed ? 'justify-center' : 'justify-start',
            ]"
            :title="collapsed ? 'Sair' : ''"
          >
            <i class="fa-solid fa-arrow-right-from-bracket w-5"></i>
            <span v-show="!collapsed" class="transition-opacity duration-300"
              >Sair</span
            >
          </button>
        </div>
      </div>
    </nav>

    <!-- Mobile toggle -->
    <button
      v-if="!mobileMenuOpen"
      @click="openMobileMenu"
      class="fixed top-4 left-4 z-[110] lg:hidden p-2 rounded-xl bg-slate-900/90 text-white hover:bg-slate-800 transition-colors shadow-lg shadow-slate-900/40"
    >
      <i class="fa-solid fa-bars text-lg"></i>
    </button>

    <!-- Chat Modal -->
    <ChatModal
      v-model:show="showChatModal"
      :messages="chatMessages"
      :typing="chatTyping"
      title="ConsorIA"
      placeholder="Pergunte sobre seus consórcios, processos ou dados..."
      size="md"
      @send="handleChatSend"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "#imports";
import SidebarItem from "./SidebarItem.vue";
import ChatModal from "./ChatModal.vue";
import { useProcesses } from "~/composables/useProcesses";
import { useCurrentUser } from "~/composables/useCurrentUser";

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
const { user: userState, load: loadCurrentUser } = useCurrentUser();
const currentUser = computed(() => userState.value || {});

const collapsed = ref(false);

// Chat IA state
const showChatModal = ref(false);
const chatMessages = ref([]);
const chatTyping = ref(false);

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
  loadCurrentUser();
});

// Mobile menu state
const mobileMenuOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const currentUserDisplay = computed(() => ({
  name: currentUser.value?.name || "Usuário",
  email: currentUser.value?.email || "sem-email",
}));

const currentUserRoles = computed(() => {
  const roles = Array.isArray(currentUser.value?.roles)
    ? currentUser.value.roles
    : [];
  return roles.slice(0, 3).map((role) => role.toString());
});

const userAvatar = computed(() => {
  if (currentUser.value?.fotoPerfilUrl) return currentUser.value.fotoPerfilUrl;
  if (currentUser.value?.profileImage) return currentUser.value.profileImage;
  return null;
});

const userInitials = computed(() => {
  const name = currentUser.value?.name || "Usuário";
  const parts = name.trim().split(/\s+/);
  if (!parts.length) return "U";
  const initials = parts.slice(0, 2).map((p) => p[0]?.toUpperCase()).join("");
  return initials || "U";
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
    label: "Cadastro de Leads",
    path: "/leads",
    icon: "fa-solid fa-address-card",
    description: "Gerencie leads e qualificações",
  },
  {
    label: "Configurações",
    path: "/admin",
    icon: "fa-solid fa-cog",
    description: "Configurações do sistema",
    roles: ["admin"],
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
        label: "Monitoramento",
        path: "/admin/settings/system-health",
        icon: "fa-solid fa-heart-pulse",
        description: "Central de monitoramento e alertas",
      },
      {
        label: "Status",
        path: "/admin/settings/status",
        icon: "fa-solid fa-tags",
        description: "Gerenciar status do sistema",
      },
      {
        label: "Notificações",
        path: "/admin/notifications",
        icon: "fa-solid fa-bell",
        description: "Gerenciar notificações",
      },
      {
        label: "Centro IA",
        path: "/admin/settings/ia",
        icon: "fa-solid fa-robot",
        description: "Centro de treinamento de IA",
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

// Chat IA functions
function openChatModal() {
  showChatModal.value = true;
  // Adicionar mensagem de boas-vindas se for primeira vez
  if (chatMessages.value.length === 0) {
    chatMessages.value.push({
      role: "assistant",
      content:
        "Olá! Sou a ConsorIA, sua assistente especializada em consórcios e gestão de processos. Posso ajudar com:\n\n• Análise de propostas e estágios\n• Informações sobre processos financeiros\n• Dúvidas sobre fluxo de trabalho\n• Interpretação de dados e estatísticas\n\nComo posso ajudá-lo hoje?",
    });
  }
}

async function handleChatSend(data) {
  // Adicionar mensagem do usuário
  chatMessages.value.push({
    role: "user",
    content: data.text,
    attachments: data.attachments,
  });

  // Chamar API real de IA
  chatTyping.value = true;
  try {
    // Criar contexto rico com informações do sistema
    const currentPage = route.path;
    let contextInfo = "";

    if (currentPage.includes("/esteira")) {
      const processKey = currentPage.split("/esteira/")[1];
      contextInfo = processKey
        ? `Usuário está na esteira do processo: ${processKey}. `
        : "Usuário está visualizando uma esteira de processos. ";
    } else if (currentPage.includes("/admin")) {
      contextInfo = "Usuário está na área administrativa. ";
    }

    // Construir prompt contextualizado
    const contextualPrompt = `${contextInfo}${data.text}`;

    const response = await $fetch("/api/ai", {
      method: "POST",
      body: {
        text: contextualPrompt,
        attachments: data.attachments,
        history: chatMessages.value.slice(-10).map((msg) => ({
          role: msg.role,
          content: msg.content,
        })), // Últimas 10 mensagens para contexto
      },
    });

    chatMessages.value.push({
      role: "assistant",
      content:
        response.text ||
        "Desculpe, não consegui processar sua pergunta no momento.",
    });
  } catch (error) {
    console.error("Erro ao chamar API de IA:", error);
    chatMessages.value.push({
      role: "assistant",
      content:
        "Ops! Estou com dificuldades técnicas no momento. Tente novamente em instantes.",
    });
  } finally {
    chatTyping.value = false;
  }
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
