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
                  @click="openProviderModal('openai')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.openai.active
                      ? 'border-green-500 bg-green-500/10 shadow-lg shadow-green-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-green-500/50',
                  ]"
                  title="Configurar OpenAI"
                >
                  <div
                    v-if="providers.openai.status === 'valid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-green-500 rounded-full border-2 border-slate-800"
                    title="Conexão Válida"
                  ></div>
                  <div
                    v-if="providers.openai.status === 'invalid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-red-500 rounded-full border-2 border-slate-800"
                    title="Conexão Inválida"
                  ></div>
                  <img
                    src="../../../assets/icons/png/11865338.png"
                    class="w-6 h-6 mx-auto"
                    alt="OpenAI"
                  />
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    OpenAI
                  </div>
                  <div
                    v-if="providers.openai.active"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-green-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Gemini -->
                <button
                  @click="openProviderModal('gemini')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.gemini.active
                      ? 'border-blue-500 bg-blue-500/10 shadow-lg shadow-blue-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-blue-500/50',
                  ]"
                  title="Configurar Gemini"
                >
                  <div
                    v-if="providers.gemini.status === 'valid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-green-500 rounded-full border-2 border-slate-800"
                    title="Conexão Válida"
                  ></div>
                  <div
                    v-if="providers.gemini.status === 'invalid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-red-500 rounded-full border-2 border-slate-800"
                    title="Conexão Inválida"
                  ></div>
                  <img
                    src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/google/google-original.svg"
                    class="w-6 h-6 mx-auto"
                    alt="Google"
                  />
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Gemini
                  </div>
                  <div
                    v-if="providers.gemini.active"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-blue-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Claude -->
                <button
                  @click="openProviderModal('claude')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.claude.active
                      ? 'border-orange-500 bg-orange-500/10 shadow-lg shadow-orange-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-orange-500/50',
                  ]"
                  title="Configurar Claude"
                >
                  <div
                    v-if="providers.claude.status === 'valid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-green-500 rounded-full border-2 border-slate-800"
                    title="Conexão Válida"
                  ></div>
                  <div
                    v-if="providers.claude.status === 'invalid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-red-500 rounded-full border-2 border-slate-800"
                    title="Conexão Inválida"
                  ></div>
                  <div>
                    <img
                      src="../../../assets/icons/png/claude-ai-icon.webp"
                      class="w-6 h-6 mx-auto"
                      alt="Claude"
                    />
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Claude
                  </div>
                  <div
                    v-if="providers.claude.active"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-orange-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Groq -->
                <button
                  @click="openProviderModal('groq')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.groq.active
                      ? 'border-yellow-500 bg-yellow-500/10 shadow-lg shadow-yellow-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-yellow-500/50',
                  ]"
                  title="Configurar Groq"
                >
                  <div
                    v-if="providers.groq.status === 'valid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-green-500 rounded-full border-2 border-slate-800"
                    title="Conexão Válida"
                  ></div>
                  <div
                    v-if="providers.groq.status === 'invalid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-red-500 rounded-full border-2 border-slate-800"
                    title="Conexão Inválida"
                  ></div>
                  <div>
                    <img
                      src="../../../assets/icons/svg/groq.svg"
                      class="w-6 h-6 mx-auto"
                      alt="Groq"
                    />
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Groq
                  </div>
                  <div
                    v-if="providers.groq.active"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-yellow-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>

                <!-- Ollama -->
                <button
                  @click="openProviderModal('ollama')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.ollama.active
                      ? 'border-slate-500 bg-slate-500/10 shadow-lg shadow-slate-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-slate-500/50',
                  ]"
                  title="Configurar Ollama"
                >
                  <div
                    v-if="providers.ollama.status === 'valid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-green-500 rounded-full border-2 border-slate-800"
                    title="Conexão Válida"
                  ></div>
                  <div
                    v-if="providers.ollama.status === 'invalid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-red-500 rounded-full border-2 border-slate-800"
                    title="Conexão Inválida"
                  ></div>
                  <div>
                    <img
                      src="../../../assets/icons/png/olhama.png"
                      class="w-6 h-6 mx-auto"
                      alt="Ollama"
                    />
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    Ollama
                  </div>
                  <div
                    v-if="providers.ollama.active"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-slate-500 rounded-full flex items-center justify-center"
                  >
                    <i class="fa-solid fa-check text-white text-[6px]"></i>
                  </div>
                </button>
              </div>
            </div>

            <!-- Chat Playground -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-3">
                  <i class="fa-solid fa-comments text-xl text-teal-400"></i>
                  <h2 class="text-sm font-medium text-white">
                    Playground de Chat
                  </h2>
                </div>
              </div>

              <!-- Model Selection -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                <select
                  v-model="selectedTestProvider"
                  class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500/50"
                >
                  <option value="">Selecione um Provider</option>
                  <option
                    v-for="provider in availableTestProviders"
                    :key="provider.id"
                    :value="provider.id"
                  >
                    {{ provider.name }}
                  </option>
                </select>
                <select
                  v-model="selectedTestModel"
                  :disabled="!selectedTestProvider"
                  class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500/50 disabled:opacity-50"
                >
                  <option value="">Selecione um Modelo</option>
                  <option
                    v-for="model in availableTestModels"
                    :key="model"
                    :value="model"
                  >
                    {{ model }}
                  </option>
                </select>
              </div>

              <!-- Chat History -->
              <div
                ref="chatHistoryRef"
                class="bg-slate-900/50 border border-slate-700/30 rounded-lg p-4 h-64 overflow-y-auto mb-4 space-y-4"
              >
                <div
                  v-if="chatHistory.length === 0"
                  class="text-center text-slate-500 pt-20"
                >
                  <i class="fa-solid fa-robot text-3xl"></i>
                  <p class="mt-2 text-sm">Nenhuma mensagem ainda.</p>
                </div>
                <div
                  v-for="(message, index) in chatHistory"
                  :key="index"
                  class="flex"
                  :class="
                    message.role === 'user' ? 'justify-end' : 'justify-start'
                  "
                >
                  <div
                    class="max-w-xs lg:max-w-md rounded-lg px-4 py-2"
                    :class="
                      message.role === 'user'
                        ? 'bg-indigo-600 text-white'
                        : 'bg-slate-700 text-slate-200'
                    "
                  >
                    <p class="text-sm">{{ message.content }}</p>
                  </div>
                </div>
                <div v-if="isModelResponding" class="flex justify-start">
                  <div
                    class="max-w-xs lg:max-w-md rounded-lg px-4 py-2 bg-slate-700 text-slate-200"
                  >
                    <i class="fa-solid fa-spinner fa-spin"></i>
                  </div>
                </div>
              </div>

              <!-- Chat Input -->
              <div class="flex gap-3">
                <input
                  v-model="userMessage"
                  @keyup.enter="sendChatMessage"
                  :disabled="!selectedTestModel"
                  class="flex-grow w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-2 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300 disabled:opacity-50"
                  placeholder="Digite sua mensagem..."
                />
                <button
                  @click="sendChatMessage"
                  :disabled="
                    !userMessage || !selectedTestModel || isModelResponding
                  "
                  class="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <i class="fa-solid fa-paper-plane"></i>
                </button>
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

    <!-- Provider Settings Modal -->
    <div
      v-if="showProviderModal && currentProvider"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      @click.self="closeProviderModal"
    >
      <div
        class="bg-gradient-to-br from-slate-800/95 via-slate-700/95 to-slate-800/95 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
      >
        <!-- Header -->
        <div
          class="flex items-center justify-between p-6 border-b border-slate-600/30"
        >
          <div class="flex items-center gap-3">
            <h3 class="text-lg font-semibold text-white">
              Configurar {{ currentProvider.name }}
            </h3>
          </div>
          <button
            @click="closeProviderModal"
            class="text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-lg p-2 transition-all"
          >
            <i class="fa-solid fa-times"></i>
          </button>
        </div>

        <!-- Form -->
        <div class="p-6 space-y-6">
          <!-- Ollama Specific Controls -->
          <div
            v-if="currentProviderKey === 'ollama'"
            class="bg-slate-800/30 border border-slate-600/30 rounded-lg p-4 space-y-3"
          >
            <h4 class="text-sm font-medium text-slate-200">
              <i class="fa-solid fa-cogs mr-1 text-slate-400"></i>
              Controles Locais do Ollama
            </h4>
            <div class="flex flex-col sm:flex-row gap-3">
              <button
                @click="startOllamaServer"
                class="flex-1 w-full bg-slate-700/50 hover:bg-slate-600/50 border border-slate-600/30 text-slate-300 hover:text-white rounded-lg px-4 py-2 text-sm font-medium transition-all duration-300"
              >
                <i class="fa-solid fa-power-off mr-2"></i>
                Verificar/Iniciar Servidor
              </button>
              <button
                @click="syncOllamaModels"
                :disabled="isSyncingModels"
                class="flex-1 w-full bg-slate-700/50 hover:bg-slate-600/50 border border-slate-600/30 text-slate-300 hover:text-white rounded-lg px-4 py-2 text-sm font-medium transition-all duration-300 disabled:opacity-50"
              >
                <i
                  :class="[
                    isSyncingModels ? 'fa-spinner fa-spin' : 'fa-sync',
                    'fa-solid mr-2',
                  ]"
                ></i>
                Sincronizar Modelos
              </button>
            </div>
            <!-- In-Modal Console -->
            <div
              v-if="showConsole"
              class="w-full bg-black/50 rounded-lg border border-slate-700 mt-4"
            >
              <div
                class="flex items-center justify-between px-3 py-1 bg-slate-700/50 rounded-t-lg"
              >
                <h4
                  class="font-['VT323',_monospace] text-base text-slate-300 tracking-wider"
                >
                  [XT-83 Terminal]
                </h4>
                <button
                  @click="consoleOutput = []"
                  class="text-slate-400 hover:text-white text-xs transition-all"
                >
                  <i class="fa-solid fa-ban mr-1"></i>
                  Clear
                </button>
              </div>
              <div
                class="p-3 h-32 overflow-y-auto font-['VT323',_monospace] text-base text-green-400"
              >
                <div v-for="(line, index) in consoleOutput" :key="index">
                  <span class="text-slate-500 mr-2">&gt;</span>
                  <span :class="line.type === 'error' ? 'text-red-400' : ''">{{
                    line.text
                  }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- API Key -->
          <div v-if="currentProviderKey !== 'ollama'">
            <label class="block text-sm font-medium text-slate-200 mb-2">
              <i class="fa-solid fa-key mr-1 text-slate-400"></i>
              API Key
            </label>
            <input
              v-model="providerForm.apiKey"
              type="password"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
              placeholder="Chave da API (será criptografada)"
            />
          </div>

          <!-- Base URL -->
          <div>
            <label class="block text-sm font-medium text-slate-200 mb-2">
              <i class="fa-solid fa-link mr-1 text-slate-400"></i>
              Base URL (cURL)
            </label>
            <input
              v-model="providerForm.baseUrl"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
              placeholder="https://api.example.com/v1"
            />
          </div>

          <!-- Model Management -->
          <div
            class="bg-slate-800/30 border border-slate-600/30 rounded-lg p-4 space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-robot mr-1 text-slate-400"></i>
                Modelo Padrão
              </label>
              <select
                v-model="providerForm.selectedModel"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
              >
                <option
                  v-if="providerForm.models.length === 0"
                  value=""
                  disabled
                  class="bg-slate-800"
                >
                  Sincronize os modelos para selecionar
                </option>
                <option
                  v-for="model in providerForm.models"
                  :key="model"
                  :value="model"
                  class="bg-slate-800"
                >
                  {{ model }}
                </option>
              </select>
            </div>

            <div>
              <h4 class="text-sm font-medium text-slate-200 mb-2">
                <i class="fa-solid fa-list-ul mr-1 text-slate-400"></i>
                Gerenciar Modelos
              </h4>
              <div class="space-y-2 max-h-40 overflow-y-auto pr-2">
                <div
                  v-for="(model, index) in providerForm.models"
                  :key="index"
                  class="flex items-center justify-between bg-slate-700/30 rounded-lg p-2"
                >
                  <span class="text-sm text-slate-300">{{ model }}</span>
                  <button
                    @click="removeModel(index)"
                    class="text-red-400 hover:text-red-300 p-1 rounded-md hover:bg-red-500/10 transition-colors"
                    title="Remover Modelo"
                  >
                    <i class="fa-solid fa-trash-alt text-xs"></i>
                  </button>
                </div>
              </div>
              <div class="flex gap-2 mt-3">
                <input
                  v-model="newModelName"
                  @keyup.enter="addModel"
                  class="flex-grow w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-2 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
                  placeholder="Adicionar novo modelo"
                />
                <button
                  @click="addModel"
                  class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium transition-colors"
                >
                  <i class="fa-solid fa-plus"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Connection Test -->
          <div
            class="bg-slate-800/30 border border-slate-600/30 rounded-lg p-4"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <i class="fa-solid fa-wifi text-indigo-400"></i>
                <span class="text-sm font-medium text-white"
                  >Teste de Conexão</span
                >
              </div>
              <div
                v-if="currentProvider.status !== 'unknown'"
                class="flex items-center gap-2 text-xs"
              >
                <span
                  v-if="currentProvider.status === 'testing'"
                  class="text-slate-400"
                  ><i class="fa-solid fa-spinner fa-spin mr-1"></i
                  >Testando...</span
                >
                <span
                  v-if="currentProvider.status === 'valid'"
                  class="text-green-400"
                  ><i class="fa-solid fa-check-circle mr-1"></i>Conexão
                  Válida</span
                >
                <span
                  v-if="currentProvider.status === 'invalid'"
                  class="text-red-400"
                  ><i class="fa-solid fa-times-circle mr-1"></i>Conexão
                  Inválida</span
                >
              </div>
            </div>
            <button
              @click="testProviderConnection"
              :disabled="currentProvider.status === 'testing'"
              class="w-full mt-3 bg-slate-700/50 hover:bg-slate-600/50 border border-slate-600/30 text-slate-300 hover:text-white rounded-lg px-4 py-2 text-sm font-medium transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
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
            @click="closeProviderModal"
            class="px-5 py-2.5 bg-slate-700/50 hover:bg-slate-600/50 text-slate-300 hover:text-white rounded-xl text-sm font-medium transition-all duration-300 backdrop-blur-sm"
          >
            Cancelar
          </button>
          <button
            @click="saveProviderSettings"
            class="bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-700 hover:to-indigo-800 text-white px-6 py-2.5 rounded-xl text-sm font-semibold transition-all duration-300 shadow-lg shadow-indigo-500/25"
          >
            <i class="fa-solid fa-save mr-2"></i>
            Salvar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useToast } from "../../../composables/useToast";

const toast = useToast();

definePageMeta({
  layout: "sidebar",
});

// --- API Configuration ---
const apiBaseUrl = "http://localhost:8080/api/v1";

// --- Component State ---
const providers = ref({
  openai: {
    active: false,
    apiKey: "",
    baseUrl: "https://api.openai.com/v1",
    models: [],
    selectedModel: "",
    status: "unknown",
    name: "OpenAI",
  },
  gemini: {
    active: false,
    apiKey: "",
    baseUrl: "https://generativelanguage.googleapis.com/v1beta",
    models: [],
    selectedModel: "",
    status: "unknown",
    name: "Gemini",
  },
  claude: {
    active: false,
    apiKey: "",
    baseUrl: "https://api.anthropic.com/v1",
    models: [],
    selectedModel: "",
    status: "unknown",
    name: "Claude",
  },
  groq: {
    active: false,
    apiKey: "",
    baseUrl: "https://api.groq.com/openai/v1",
    models: [],
    selectedModel: "",
    status: "unknown",
    name: "Groq",
  },
  ollama: {
    active: false,
    apiKey: "",
    baseUrl: "http://localhost:11434",
    models: [],
    selectedModel: "",
    status: "unknown",
    name: "Ollama",
  },
});

const showProviderModal = ref(false);
const currentProviderKey = ref(null);
const newModelName = ref("");
const isSyncingModels = ref(false);
const showConsole = ref(false);
const consoleOutput = ref([]);

const providerForm = ref({
  apiKey: "",
  baseUrl: "",
  selectedModel: "",
  models: [],
});

const currentProvider = computed(() => {
  return currentProviderKey.value
    ? providers.value[currentProviderKey.value]
    : null;
});

// --- Lifecycle Hooks ---
onMounted(async () => {
  console.log("Fetching provider configurations from Java backend...");
  try {
    const fetchedProviders = await $fetch(`${apiBaseUrl}/ai-providers`);
    if (fetchedProviders && fetchedProviders.length > 0) {
      const newProvidersState = { ...providers.value };
      fetchedProviders.forEach((p) => {
        if (newProvidersState[p.id]) {
          newProvidersState[p.id] = {
            ...newProvidersState[p.id],
            ...p,
            apiKey: p.apiKey ? "********" : "",
          };
        }
      });
      providers.value = newProvidersState;
      console.log("Successfully loaded provider configurations from backend.");
    }
  } catch (error) {
    console.error("Could not fetch provider configurations:", error);
    toast.warning("perda de conexão: Não foi possível conectar ao servidor");
  }
});

// --- Modal & Form Logic ---
function openProviderModal(providerKey) {
  currentProviderKey.value = providerKey;
  const providerData = providers.value[providerKey];
  providerForm.value = {
    apiKey: "", // Always clear API key field for security
    baseUrl: providerData.baseUrl,
    selectedModel: providerData.selectedModel,
    models: [...providerData.models],
  };
  showConsole.value = false; // Hide console when opening a new modal
  consoleOutput.value = [];
  showProviderModal.value = true;
}

function closeProviderModal() {
  showProviderModal.value = false;
  currentProviderKey.value = null;
  newModelName.value = "";
}

function addModel() {
  if (
    newModelName.value &&
    !providerForm.value.models.includes(newModelName.value)
  ) {
    providerForm.value.models.push(newModelName.value);
    newModelName.value = "";
  }
}

function removeModel(index) {
  providerForm.value.models.splice(index, 1);
}

// --- API & Shell Commands ---
async function saveProviderSettings() {
  if (!currentProviderKey.value) return;
  const providerKey = currentProviderKey.value;

  // For Ollama, the API key is not needed and should not be sent.
  const apiKeyToSend =
    providerKey === "ollama" ? "" : providerForm.value.apiKey;

  const payload = {
    apiKey: apiKeyToSend,
    baseUrl: providerForm.value.baseUrl,
    selectedModel: providerForm.value.selectedModel,
    models: providerForm.value.models,
    active: providerKey === "ollama" ? true : !!providerForm.value.apiKey,
  };

  try {
    console.log(`Calling API: PUT ${apiBaseUrl}/ai-providers/${providerKey}`);
    const updatedProvider = await $fetch(
      `${apiBaseUrl}/ai-providers/${providerKey}`,
      {
        method: "PUT",
        body: payload,
      }
    );

    providers.value[providerKey] = {
      ...providers.value[providerKey],
      ...updatedProvider,
    };

    console.log("Successfully saved provider settings.");
    closeProviderModal();
  } catch (error) {
    console.error("Failed to save provider settings:", error);
    alert("Error: Could not save settings to the Java server.");
  }
}

async function startOllamaServer() {
  showConsole.value = true;
  consoleOutput.value = [
    { text: "Attempting to start Ollama server via backend..." },
  ];
  try {
    const result = await $fetch(`${apiBaseUrl}/ollama/start`, {
      method: "POST",
    });
    result.stdout.forEach((line) => consoleOutput.value.push({ text: line }));
    result.stderr.forEach((line) =>
      consoleOutput.value.push({ text: line, type: "error" })
    );
  } catch (error) {
    console.error("Failed to start Ollama server:", error);
    consoleOutput.value.push({
      text: `Error: ${error.data?.message || error.message}`,
      type: "error",
    });
  }
}

async function syncOllamaModels() {
  isSyncingModels.value = true;
  showConsole.value = true;
  consoleOutput.value = [{ text: "$ ollama list" }];

  try {
    const result = await $fetch(`${apiBaseUrl}/ollama/list`);
    result.stdout.forEach((line) => consoleOutput.value.push({ text: line }));

    if (result.stderr && result.stderr.length > 0) {
      throw new Error(result.stderr.join("\n"));
    }

    const lines = result.stdout;
    if (lines.length > 1) {
      // Assuming the first line is the header
      const models = lines.slice(1).map((line) => line.split(/\s+/)[0]);
      providerForm.value.models = models;
      if (!providerForm.value.selectedModel && models.length > 0) {
        providerForm.value.selectedModel = models[0];
      }
      console.log("Ollama models synced:", models);
      consoleOutput.value.push({ text: "\nSync successful!" });
    }
  } catch (error) {
    console.error("Failed to sync Ollama models:", error);
    consoleOutput.value.push({
      text: `Error syncing Ollama models: ${error.message}`,
      type: "error",
    });
  } finally {
    isSyncingModels.value = false;
  }
}

async function testProviderConnection() {
  if (!currentProviderKey.value) return;
  const providerKey = currentProviderKey.value;
  const provider = providers.value[providerKey];
  provider.status = "testing";

  console.log(`API CALL: POST ${apiBaseUrl}/ai-providers/${providerKey}/test`, {
    apiKey: providerForm.value.apiKey,
    baseUrl: providerForm.value.baseUrl,
  });

  await new Promise((resolve) => setTimeout(resolve, 1500));

  if (
    providerKey === "ollama" ||
    (providerForm.value.apiKey && providerForm.value.apiKey.length > 5)
  ) {
    provider.status = "valid";
  } else {
    provider.status = "invalid";
  }
}

// --- Chat Playground ---
const selectedTestProvider = ref("");
const selectedTestModel = ref("");
const chatHistory = ref([]);
const userMessage = ref("");
const isModelResponding = ref(false);
const chatHistoryRef = ref(null);

const availableTestProviders = computed(() => {
  return Object.entries(providers.value)
    .filter(([key, provider]) => provider.active)
    .map(([id, provider]) => ({ id, name: provider.name }));
});

const availableTestModels = computed(() => {
  if (
    selectedTestProvider.value &&
    providers.value[selectedTestProvider.value]
  ) {
    return providers.value[selectedTestProvider.value].models;
  }
  return [];
});

watch(selectedTestProvider, (newProvider) => {
  selectedTestModel.value = ""; // Reset model selection when provider changes
  const provider = providers.value[newProvider];
  if (provider && provider.selectedModel) {
    selectedTestModel.value = provider.selectedModel;
  }
});

watch(
  chatHistory,
  () => {
    // Scroll to bottom of chat history
    nextTick(() => {
      if (chatHistoryRef.value) {
        chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
      }
    });
  },
  { deep: true }
);

async function sendChatMessage() {
  if (
    !userMessage.value ||
    !selectedTestProvider.value ||
    !selectedTestModel.value ||
    isModelResponding.value
  ) {
    return;
  }

  const messageContent = userMessage.value;
  chatHistory.value.push({ role: "user", content: messageContent });
  userMessage.value = "";
  isModelResponding.value = true;

  try {
    // NOTE: A new backend endpoint /api/v1/ai/chat is required for this to work.
    const response = await $fetch(`${apiBaseUrl}/ai-providers/chat`, {
      method: "POST",
      body: {
        provider: selectedTestProvider.value,
        model: selectedTestModel.value,
        prompt: messageContent,
      },
    });

    chatHistory.value.push({
      role: "assistant",
      content: response.content || "No content received.",
    });
  } catch (error) {
    console.error("Error sending chat message:", error);
    chatHistory.value.push({
      role: "assistant",
      content: `Error: ${error.data?.message || "Could not get response."}`,
    });
  } finally {
    isModelResponding.value = false;
  }
}
</script>
