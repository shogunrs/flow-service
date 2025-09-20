<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900"
  >
    <!-- Header -->
    <header
      class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 p-4"
    >
      <div class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"></div>

      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-6">
          <!-- Enhanced icon with glow -->
          <div class="relative group">
            <div
              class="absolute inset-0 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl blur-lg opacity-50 group-hover:opacity-75 transition-opacity duration-300"
            ></div>
            <div>
              <i class="fa-solid fa-robot text-white text-lg"></i>
            </div>
          </div>

          <div class="space-y-1">
            <p class="app-header-subtitle">
              Configure e monitore seus modelos de inteligência artificial
            </p>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="p-6 relative z-10">
      <div class="max-w-7xl mx-auto">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- Providers Configuration -->
          <div class="lg:col-span-2 space-y-6">
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center gap-3 mb-6">
                <i class="fa-solid fa-plug text-xl text-indigo-400"></i>
                <h2 class="text-sm font-medium text-white">
                  Configuração de Providers
                </h2>
              </div>

              <div class="grid grid-cols-5 gap-4">
                <!-- OpenAI -->
                <button
                  @click="toggleProvider('openai')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.openai
                      ? 'border-green-500 bg-green-500/10 shadow-lg shadow-green-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-green-500/50',
                  ]"
                  :title="providers.openai ? 'OpenAI Ativo' : 'Ativar OpenAI'"
                >
                  <img
                    src="./../../assets/icons/png/11865338.png"
                    class="w-6 h-6 mx-auto"
                    alt="OpenAI"
                  />
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    OpenAI
                  </div>
                  <div
                    v-if="providers.openai"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-green-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Gemini -->
                <button
                  @click="toggleProvider('gemini')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.gemini
                      ? 'border-blue-500 bg-blue-500/10 shadow-lg shadow-blue-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-blue-500/50',
                  ]"
                  :title="providers.gemini ? 'Gemini Ativo' : 'Ativar Gemini'"
                >
                  <img
                    src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/google/google-original.svg"
                    class="w-6 h-6 mx-auto"
                    alt="Google"
                  />
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Gemini
                  </div>
                  <div
                    v-if="providers.gemini"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-blue-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Claude -->
                <button
                  @click="toggleProvider('claude')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.claude
                      ? 'border-orange-500 bg-orange-500/10 shadow-lg shadow-orange-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-orange-500/50',
                  ]"
                  :title="providers.claude ? 'Claude Ativo' : 'Ativar Claude'"
                >
                  <div>
                    <img
                      src="../../assets/icons/png/claude-ai-icon.webp"
                      class="w-6 h-6 mx-auto"
                      alt="Google"
                    />
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Claude
                  </div>
                  <div
                    v-if="providers.claude"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-orange-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Groq -->
                <button
                  @click="toggleProvider('groq')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.groq
                      ? 'border-orange-500 bg-orange-500/10 shadow-lg shadow-orange-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-orange-500/50',
                  ]"
                  :title="providers.groq ? 'Groq Ativo' : 'Ativar Groq'"
                >
                  <div>
                    <img
                      src="../../assets/icons/svg/groq.svg"
                      class="w-6 h-6 mx-auto"
                      alt="Google"
                    />
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Groq
                  </div>
                  <div
                    v-if="providers.groq"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-orange-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Ollama -->
                <button
                  @click="toggleProvider('ollama')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.ollama
                      ? 'border-slate-500 bg-slate-500/10 shadow-lg shadow-slate-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-slate-500/50',
                  ]"
                  :title="providers.ollama ? 'Ollama Ativo' : 'Ativar Ollama'"
                >
                  <div>
                    <img
                      src="../../assets/icons/png/olhama.png"
                      class="w-6 h-6 mx-auto"
                      alt="Google"
                    />
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Ollama
                  </div>
                  <div
                    v-if="providers.ollama"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-slate-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>
              </div>

              <div class="flex gap-3 mt-6">
                <button
                  class="bg-indigo-600 hover:bg-indigo-700 px-4 py-2 rounded-lg text-white text-sm font-medium transition-colors"
                >
                  <i class="fa-solid fa-save mr-2"></i>
                  Salvar Configurações
                </button>
                <button
                  class="bg-slate-600 hover:bg-slate-700 px-4 py-2 rounded-lg text-white text-sm font-medium transition-colors"
                >
                  <i class="fa-solid fa-network-wired mr-2"></i>
                  Testar Conexões
                </button>
              </div>
            </div>

            <!-- MCP Database Connections -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center justify-between mb-6">
                <div class="flex items-center gap-3">
                  <i class="fa-solid fa-database text-xl text-cyan-400"></i>
                  <h2 class="text-sm font-medium text-white">Conexões MCP</h2>
                </div>
                <button
                  @click="openMcpModal"
                  class="bg-cyan-600 hover:bg-cyan-700 px-3 py-1 rounded-lg text-white text-xs font-medium transition-colors"
                >
                  <i class="fa-solid fa-plus mr-1"></i>
                  Adicionar MCP
                </button>
              </div>

              <!-- MCP List -->
              <div class="space-y-3">
                <div
                  v-if="mcpConnections.length === 0"
                  class="text-center py-8 text-slate-500"
                >
                  <i class="fa-solid fa-plug text-3xl mb-3"></i>
                  <p class="text-sm">Nenhuma conexão MCP configurada</p>
                  <p class="text-xs text-slate-400 mt-1">
                    Adicione conexões para PostgreSQL, MongoDB ou ClickHouse
                  </p>
                </div>

                <div
                  v-for="(mcp, index) in mcpConnections"
                  :key="mcp.id"
                  class="border border-slate-600/30 bg-slate-800/40 rounded-lg p-4"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                      <div
                        class="w-8 h-8 rounded-lg flex items-center justify-center"
                        :class="getMcpTypeIcon(mcp.type).bg"
                      >
                        <i
                          :class="getMcpTypeIcon(mcp.type).icon"
                          class="text-sm"
                        ></i>
                      </div>
                      <div>
                        <h3 class="font-medium text-white text-sm">
                          {{ mcp.name }}
                        </h3>
                        <p class="text-xs text-slate-400">
                          {{ mcp.type }} • {{ mcp.host }}:{{ mcp.port }}
                        </p>
                      </div>
                    </div>
                    <div class="flex items-center gap-2">
                      <div
                        :class="[
                          'w-2 h-2 rounded-full',
                          mcp.status === 'connected'
                            ? 'bg-green-500'
                            : 'bg-red-500',
                        ]"
                        :title="
                          mcp.status === 'connected'
                            ? 'Conectado'
                            : 'Desconectado'
                        "
                      ></div>
                      <button
                        @click="editMcp(mcp)"
                        class="p-1 text-slate-400 hover:text-white transition-colors"
                        title="Editar"
                      >
                        <i class="fa-solid fa-edit text-xs"></i>
                      </button>
                      <button
                        @click="deleteMcp(mcp.id)"
                        class="p-1 text-red-400 hover:text-red-300 transition-colors"
                        title="Remover"
                      >
                        <i class="fa-solid fa-trash text-xs"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Dataset Management -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center gap-3 mb-6">
                <i class="fa-solid fa-database text-xl text-purple-400"></i>
                <h2 class="text-sm font-medium text-white">
                  Gestão de Datasets
                </h2>
              </div>

              <div class="space-y-4">
                <!-- Upload Dataset -->
                <div
                  class="border-2 border-dashed border-slate-600/50 rounded-lg p-6 text-center hover:border-purple-400/50 transition-colors"
                >
                  <i
                    class="fa-solid fa-cloud-upload-alt text-3xl text-slate-400 mb-3"
                  ></i>
                  <h3 class="text-sm font-medium text-white mb-2">
                    Upload de Dataset
                  </h3>
                  <p class="text-slate-400 mb-4">
                    Faça upload de arquivos JSON, CSV ou TXT para treinar a IA
                  </p>
                  <button
                    class="bg-purple-600 hover:bg-purple-700 px-4 py-2 rounded-lg text-white font-medium transition-colors"
                  >
                    <i class="fa-solid fa-plus mr-2"></i>
                    Selecionar Arquivos
                  </button>
                </div>

                <div class="text-center py-8 text-slate-500">
                  <i class="fa-solid fa-inbox text-3xl mb-2"></i>
                  <p>Nenhum dataset encontrado</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Monitoring & Training -->
          <div class="space-y-6">
            <!-- Training Status -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center gap-3 mb-6">
                <i class="fa-solid fa-chart-line text-xl text-green-400"></i>
                <h2 class="text-sm font-medium text-white">
                  Status do Treinamento
                </h2>
              </div>

              <div class="text-center py-6 text-slate-500">
                <i class="fa-solid fa-pause-circle text-3xl mb-2"></i>
                <p>Nenhum treinamento ativo</p>
              </div>

              <button
                class="w-full bg-indigo-600 hover:bg-indigo-700 px-4 py-2 rounded-lg text-white font-medium transition-colors"
              >
                <i class="fa-solid fa-play mr-2"></i>
                Iniciar Treinamento
              </button>
            </div>

            <!-- Recent Activity -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center gap-3 mb-6">
                <i class="fa-solid fa-history text-xl text-orange-400"></i>
                <h2 class="text-sm font-medium text-white">
                  Atividade Recente
                </h2>
              </div>

              <div class="text-center py-6 text-slate-500">
                <i class="fa-solid fa-clock text-2xl mb-2"></i>
                <p>Nenhuma atividade recente</p>
              </div>
            </div>

            <!-- Quick Actions -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center gap-3 mb-6">
                <i class="fa-solid fa-bolt text-xl text-yellow-400"></i>
                <h2 class="text-sm font-medium text-white">Ações Rápidas</h2>
              </div>

              <div class="space-y-3">
                <button
                  class="w-full flex items-center gap-3 px-3 py-2 text-left text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-lg transition-colors"
                >
                  <i class="fa-solid fa-download"></i>
                  Exportar Configuração
                </button>
                <button
                  class="w-full flex items-center gap-3 px-3 py-2 text-left text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-lg transition-colors"
                >
                  <i class="fa-solid fa-upload"></i>
                  Importar Configuração
                </button>
                <button
                  class="w-full flex items-center gap-3 px-3 py-2 text-left text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-lg transition-colors"
                >
                  <i class="fa-solid fa-file-lines"></i>
                  Ver Logs
                </button>
                <button
                  class="w-full flex items-center gap-3 px-3 py-2 text-left text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-lg transition-colors"
                >
                  <i class="fa-solid fa-trash-can"></i>
                  Limpar Cache
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- MCP Modal -->
    <div
      v-if="showMcpModal"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      @click.self="closeMcpModal"
    >
      <div
        class="bg-gradient-to-br from-slate-800/95 via-slate-700/95 to-slate-800/95 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto"
      >
        <!-- Header -->
        <div
          class="flex items-center justify-between p-6 border-b border-slate-600/30"
        >
          <div class="flex items-center gap-3">
            <div
              class="w-8 h-8 bg-cyan-600 rounded-lg flex items-center justify-center"
            >
              <i class="fa-solid fa-database text-white text-sm"></i>
            </div>
            <h3 class="text-lg font-semibold text-white">
              {{ editingMcp ? "Editar Conexão MCP" : "Nova Conexão MCP" }}
            </h3>
          </div>
          <button
            @click="closeMcpModal"
            class="text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-lg p-2 transition-all"
          >
            <i class="fa-solid fa-times"></i>
          </button>
        </div>

        <!-- Form -->
        <div class="p-6 space-y-5">
          <!-- Nome e Tipo lado a lado -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-tag mr-1 text-slate-400"></i>
                Nome da Conexão
              </label>
              <input
                v-model="mcpForm.name"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
                placeholder="Ex: Banco Principal"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-server mr-1 text-slate-400"></i>
                Tipo de Banco
              </label>
              <select
                v-model="mcpForm.type"
                @change="updatePortPlaceholder"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
              >
                <option value="" class="bg-slate-800">Selecione o tipo</option>
                <option value="postgresql" class="bg-slate-800">
                  PostgreSQL
                </option>
                <option value="mongodb" class="bg-slate-800">MongoDB</option>
                <option value="clickhouse" class="bg-slate-800">
                  ClickHouse
                </option>
              </select>
            </div>
          </div>

          <!-- Host e Porta lado a lado -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-globe mr-1 text-slate-400"></i>
                Host
              </label>
              <input
                v-model="mcpForm.host"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
                placeholder="localhost ou IP do servidor"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-plug mr-1 text-slate-400"></i>
                Porta
              </label>
              <input
                v-model="mcpForm.port"
                type="number"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
                :placeholder="getDefaultPort(mcpForm.type)"
              />
            </div>
          </div>

          <!-- Database -->
          <div>
            <label class="block text-sm font-medium text-slate-200 mb-2">
              <i class="fa-solid fa-database mr-1 text-slate-400"></i>
              Database
            </label>
            <input
              v-model="mcpForm.database"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
              placeholder="Nome do banco de dados"
            />
          </div>

          <!-- Credenciais lado a lado -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-user mr-1 text-slate-400"></i>
                Usuário
              </label>
              <input
                v-model="mcpForm.username"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
                placeholder="Usuário do banco"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-lock mr-1 text-slate-400"></i>
                Senha
              </label>
              <input
                v-model="mcpForm.password"
                type="password"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
                placeholder="Senha do banco"
              />
            </div>
          </div>

          <!-- Conexão de teste -->
          <div
            class="bg-slate-800/30 border border-slate-600/30 rounded-lg p-4"
          >
            <div class="flex items-center gap-3 mb-3">
              <i class="fa-solid fa-wifi text-cyan-400"></i>
              <span class="text-sm font-medium text-white"
                >Teste de Conexão</span
              >
            </div>
            <button
              @click="testConnection"
              class="w-full bg-slate-700/50 hover:bg-slate-600/50 border border-slate-600/30 text-slate-300 hover:text-white rounded-lg px-4 py-2 text-sm font-medium transition-all duration-300"
            >
              <i class="fa-solid fa-flask mr-2"></i>
              Testar Conexão
            </button>
          </div>
        </div>

        <!-- Footer -->
        <div
          class="flex items-center justify-end gap-3 p-6 border-t border-slate-600/30 bg-slate-800/20"
        >
          <button
            @click="closeMcpModal"
            class="px-5 py-2.5 bg-slate-700/50 hover:bg-slate-600/50 text-slate-300 hover:text-white rounded-xl text-sm font-medium transition-all duration-300 backdrop-blur-sm"
          >
            Cancelar
          </button>
          <button
            @click="saveMcp"
            class="bg-gradient-to-r from-cyan-600 to-cyan-700 hover:from-cyan-700 hover:to-cyan-800 text-white px-6 py-2.5 rounded-xl text-sm font-semibold transition-all duration-300 shadow-lg shadow-cyan-500/25"
          >
            <i class="fa-solid fa-save mr-2"></i>
            {{ editingMcp ? "Atualizar Conexão" : "Criar Conexão" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

definePageMeta({
  layout: "sidebar",
});

// Provider states
const providers = ref({
  openai: false,
  gemini: false,
  claude: false,
  groq: false,
  ollama: false,
});

// MCP states
const mcpConnections = ref([]);
const showMcpModal = ref(false);
const editingMcp = ref(false);
const mcpForm = ref({
  id: null,
  name: "",
  type: "",
  host: "",
  port: "",
  database: "",
  username: "",
  password: "",
  status: "disconnected",
});

// Toggle provider function
function toggleProvider(providerName) {
  providers.value[providerName] = !providers.value[providerName];
}

// MCP functions
function getMcpTypeIcon(type) {
  const icons = {
    postgresql: {
      icon: "fa-brands fa-postgresql text-blue-600",
      bg: "bg-blue-100",
    },
    mongodb: {
      icon: "fa-solid fa-leaf text-green-600",
      bg: "bg-green-100",
    },
    clickhouse: {
      icon: "fa-solid fa-chart-bar text-orange-600",
      bg: "bg-orange-100",
    },
  };
  return (
    icons[type] || {
      icon: "fa-solid fa-database text-slate-600",
      bg: "bg-slate-100",
    }
  );
}

function getDefaultPort(type) {
  const ports = {
    postgresql: "5432",
    mongodb: "27017",
    clickhouse: "8123",
  };
  return ports[type] || "";
}

function openMcpModal() {
  resetMcpForm();
  editingMcp.value = false;
  showMcpModal.value = true;
}

function closeMcpModal() {
  showMcpModal.value = false;
  resetMcpForm();
  editingMcp.value = false;
}

function resetMcpForm() {
  mcpForm.value = {
    id: null,
    name: "",
    type: "",
    host: "",
    port: "",
    database: "",
    username: "",
    password: "",
    status: "disconnected",
  };
}

function editMcp(mcp) {
  mcpForm.value = { ...mcp };
  editingMcp.value = true;
  showMcpModal.value = true;
}

function saveMcp() {
  if (!mcpForm.value.name || !mcpForm.value.type || !mcpForm.value.host) {
    alert("Preencha todos os campos obrigatórios");
    return;
  }

  // Set default port if not provided
  if (!mcpForm.value.port) {
    mcpForm.value.port = getDefaultPort(mcpForm.value.type);
  }

  if (editingMcp.value) {
    // Update existing MCP
    const index = mcpConnections.value.findIndex(
      (m) => m.id === mcpForm.value.id
    );
    if (index !== -1) {
      mcpConnections.value[index] = { ...mcpForm.value };
    }
  } else {
    // Create new MCP
    const newMcp = {
      ...mcpForm.value,
      id: Date.now().toString(),
      status: "disconnected",
    };
    mcpConnections.value.push(newMcp);
  }

  closeMcpModal();
}

function deleteMcp(mcpId) {
  if (confirm("Tem certeza que deseja remover esta conexão MCP?")) {
    mcpConnections.value = mcpConnections.value.filter((m) => m.id !== mcpId);
  }
}

// Add some sample data for demonstration
mcpConnections.value = [
  {
    id: "1",
    name: "Banco Principal",
    type: "postgresql",
    host: "localhost",
    port: "5432",
    database: "docheck_db",
    username: "postgres",
    password: "***",
    status: "connected",
  },
  {
    id: "2",
    name: "Analytics DB",
    type: "clickhouse",
    host: "analytics.docheck.com",
    port: "8123",
    database: "analytics",
    username: "analyst",
    password: "***",
    status: "disconnected",
  },
];
</script>
