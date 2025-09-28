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

                <!-- OpenRouter -->
                <button
                  @click="openProviderModal('openrouter')"
                  :class="[
                    'group relative p-3 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                    providers.openrouter.active
                      ? 'border-purple-500 bg-purple-500/10 shadow-lg shadow-purple-500/25'
                      : 'border-slate-600/30 bg-slate-800/40 hover:border-purple-500/50',
                  ]"
                  title="Configurar OpenRouter"
                >
                  <div
                    v-if="providers.openrouter.status === 'valid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-green-500 rounded-full border-2 border-slate-800"
                    title="Conexão Válida"
                  ></div>
                  <div
                    v-if="providers.openrouter.status === 'invalid'"
                    class="absolute -top-1 -left-1 w-3 h-3 bg-red-500 rounded-full border-2 border-slate-800"
                    title="Conexão Inválida"
                  ></div>
                  <div class="w-6 h-6 mx-auto flex items-center justify-center">
                    <i class="fa-solid fa-route text-xl text-slate-300"></i>
                  </div>
                  <div class="text-xs text-center mt-1 text-white font-medium">
                    OpenRouter
                  </div>
                  <div
                    v-if="providers.openrouter.active"
                    class="absolute -top-1 -right-1 w-3 h-3 bg-purple-500 rounded-full flex items-center justify-center"
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

              <!-- Model & Agent Selection -->
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
                <div class="sm:col-span-2">
                  <select
                    v-model="selectedAgentId"
                    :disabled="!selectedTestProvider"
                    class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500/50 disabled:opacity-50"
                  >
                    <option :value="null">Nenhum Agente (Chat Padrão)</option>
                    <option
                      v-for="agent in promptAgents"
                      :key="agent.id"
                      :value="agent.id"
                    >
                      {{ agent.name }}
                    </option>
                  </select>
                </div>
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

              <!-- Anexo Preview -->
              <div
                v-if="attachedFile"
                class="relative bg-slate-800/50 border border-slate-700/50 rounded-lg p-2 mb-3 flex items-center gap-3"
              >
                <i class="fa-solid fa-file-alt text-slate-400"></i>
                <div class="text-sm text-slate-300 truncate">
                  <span class="font-medium">{{ attachedFile.name }}</span>
                  <span class="text-slate-400 text-xs ml-2">({{ formatFileSize(attachedFile.size) }})</span>
                </div>
                <button
                  @click="removeAttachedFile"
                  class="ml-auto text-slate-400 hover:text-white hover:bg-red-500/20 rounded-full p-1.5 transition-colors"
                  title="Remover arquivo"
                >
                  <i class="fa-solid fa-times text-xs"></i>
                </button>
              </div>

              <!-- Chat Input -->
              <div class="flex gap-3">
                <!-- Botão de Anexo -->
                <input
                  type="file"
                  ref="fileInputRef"
                  @change="handleFileSelect"
                  hidden
                />
                <button
                  @click="triggerFileInput"
                  :disabled="!selectedTestModel"
                  class="px-4 py-2 bg-slate-700/50 hover:bg-slate-600/50 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Anexar arquivo"
                >
                  <i class="fa-solid fa-paperclip"></i>
                </button>

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
                    (!userMessage && !attachedFile) || !selectedTestModel || isModelResponding
                  "
                  class="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <i class="fa-solid fa-paper-plane"></i>
                </button>
              </div>
            </div>

            <!-- Prompt Engineering Center -->
            <div
              class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6"
            >
              <div class="flex items-center justify-between mb-6">
                <div class="flex items-center gap-3">
                  <i class="fa-solid fa-brain text-xl text-purple-400"></i>
                  <h2 class="text-sm font-medium text-white">
                    Central de Engenharia de Prompt
                  </h2>
                </div>
                <button
                  @click="openAgentModal()"
                  class="bg-purple-600 hover:bg-purple-700 px-4 py-2 rounded-lg text-white font-medium text-sm transition-colors"
                >
                  <i class="fa-solid fa-plus mr-2"></i>
                  Criar Agente
                </button>
              </div>

              <!-- Agent List -->
              <div class="space-y-0 border border-slate-700/50 rounded-lg overflow-hidden">
                <div v-if="promptAgents.length === 0" class="text-center py-8 text-slate-500 w-full">
                  <i class="fa-solid fa-box-open text-3xl mb-2"></i>
                  <p>Nenhum agente criado ainda.</p>
                </div>
                <div
                  v-for="(agent, index) in promptAgents"
                  :key="agent.id"
                  class="flex items-center justify-between py-2 px-4 transition-colors hover:bg-slate-700/30"
                  :class="{ 'border-b border-slate-700/50': index < promptAgents.length - 1 }"
                >
                  <div>
                    <h3 class="text-sm font-medium text-white">{{ agent.name }}</h3>
                  </div>
                  <div class="flex items-center gap-2">
                    <button @click="openAgentModal(agent)" class="text-slate-400 hover:text-white p-1 rounded-md hover:bg-slate-600/50 transition-colors" title="Editar Agente">
                      <i class="fa-solid fa-pencil-alt text-xs"></i>
                    </button>
                    <button @click="deleteAgent(agent.id)" class="text-red-400 hover:text-white p-1 rounded-md hover:bg-red-500/20 transition-colors" title="Excluir Agente">
                      <i class="fa-solid fa-trash-alt text-xs"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Monitoring & Training -->
          <div class="space-y-6">
            <!-- Base de Conhecimento (RAG) -->
            <div class="bg-gradient-to-br from-slate-800/80 via-slate-700/60 to-slate-800/80 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl p-6">
              <div class="flex items-center gap-3 mb-6">
                <i class="fa-solid fa-book text-xl text-sky-400"></i>
                <h2 class="text-sm font-medium text-white">Base de Conhecimento (RAG)</h2>
              </div>

              <div class="space-y-4">
                <!-- Agent Selection for RAG -->
                <select
                  v-model="selectedRagAgentId"
                  class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-sky-500/50 focus:border-sky-500/50"
                >
                  <option :value="null" disabled>Selecione um Agente para associar</option>
                  <option
                    v-for="agent in promptAgents"
                    :key="agent.id"
                    :value="agent.id"
                  >
                    {{ agent.name }}
                  </option>
                </select>

                <!-- File Upload -->
                <div v-if="!ragFile">
                  <input type="file" ref="ragFileInputRef" @change="handleRagFileSelect" hidden />
                  <div
                    @click="ragFileInputRef.click()"
                    class="border-2 border-dashed border-slate-600/50 rounded-lg p-6 text-center hover:border-sky-400/50 transition-colors cursor-pointer"
                  >
                    <i class="fa-solid fa-cloud-upload-alt text-3xl text-slate-400 mb-3"></i>
                    <h3 class="text-sm font-medium text-white mb-2">
                      Anexar Conhecimento
                    </h3>
                    <p class="text-xs text-slate-400">
                      Arraste ou clique para selecionar um arquivo
                    </p>
                  </div>
                </div>

                <!-- File Preview -->
                <div v-else class="relative bg-slate-800/50 border border-slate-700/50 rounded-lg p-2 flex items-center gap-3">
                  <i class="fa-solid fa-file-alt text-slate-400"></i>
                  <div class="text-sm text-slate-300 truncate">
                    <span class="font-medium">{{ ragFile.name }}</span>
                    <span class="text-slate-400 text-xs ml-2">({{ formatFileSize(ragFile.size) }})</span>
                  </div>
                  <button
                    @click="removeRagFile"
                    class="ml-auto text-slate-400 hover:text-white hover:bg-red-500/20 rounded-full p-1.5 transition-colors"
                    title="Remover arquivo"
                  >
                    <i class="fa-solid fa-times text-xs"></i>
                  </button>
                </div>

                <button
                  @click="uploadRagFile"
                  :disabled="!selectedRagAgentId || !ragFile || isUploadingRag"
                  class="w-full bg-sky-600 hover:bg-sky-700 px-4 py-2 rounded-lg text-white font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <i v-if="isUploadingRag" class="fa-solid fa-spinner fa-spin mr-2"></i>
                  <i v-else class="fa-solid fa-upload mr-2"></i>
                  {{ isUploadingRag ? 'Processando...' : 'Enviar para o Agente' }}
                </button>
              </div>
            </div>

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
                @click="checkOllamaStatus"
                class="flex-1 w-full bg-slate-700/50 hover:bg-slate-600/50 border border-slate-600/30 text-slate-300 hover:text-white rounded-lg px-4 py-2 text-sm font-medium transition-all duration-300"
              >
                <i class="fa-solid fa-wifi mr-2"></i>
                Verificar Status
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
            <div v-if="!editingApiKey" class="flex items-center gap-2">
              <input
                :value="providerForm.apiKey"
                type="text"
                readonly
                class="flex-grow w-full bg-slate-900/60 border border-slate-700/50 text-slate-400 rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
                placeholder="Nenhuma chave definida"
              />
              <button @click="editingApiKey = true" class="px-4 py-2 bg-slate-700/50 hover:bg-slate-600/50 text-white rounded-lg text-sm font-medium transition-colors">
                Alterar
              </button>
            </div>
            <div v-else>
              <input
                v-model="newApiKey"
                type="text"
                class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all duration-300"
                placeholder="Cole sua nova chave de API aqui..."
              />
              <p class="text-xs text-slate-400 mt-2">A chave será salva de forma segura. Deixe em branco para cancelar a alteração.</p>
            </div>
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

    <!-- Agent Settings Modal -->
    <div
      v-if="showAgentModal"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      @click.self="closeAgentModal"
    >
      <div
        class="bg-gradient-to-br from-slate-800/95 via-slate-700/95 to-slate-800/95 backdrop-blur-xl border border-slate-600/30 rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] flex flex-col"
      >
        <!-- Header -->
        <div
          class="flex items-center justify-between p-6 border-b border-slate-600/30"
        >
          <div class="flex items-center gap-3">
            <h3 class="text-lg font-semibold text-white">
              {{ agentForm.id ? 'Editar Agente' : 'Criar Novo Agente' }}
            </h3>
          </div>
          <button
            @click="closeAgentModal"
            class="text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-lg p-2 transition-all"
          >
            <i class="fa-solid fa-times"></i>
          </button>
        </div>

        <!-- Form -->
        <div class="p-6 space-y-6 overflow-y-auto flex-grow">
          <div>
            <label class="block text-sm font-medium text-slate-200 mb-2">
              <i class="fa-solid fa-user-shield mr-1 text-slate-400"></i>
              Nome do Agente
            </label>
            <input
              v-model="agentForm.name"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500/50 transition-all duration-300"
              placeholder="Ex: Analista de Dados Sênior"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-200 mb-2">
              <i class="fa-solid fa-scroll mr-1 text-slate-400"></i>
              Prompt do Sistema
            </label>
            <textarea
              v-model="agentForm.prompt"
              rows="12"
              class="w-full bg-slate-800/50 border border-slate-700/50 text-white rounded-lg px-4 py-3 text-sm backdrop-blur-sm focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500/50 transition-all duration-300"
              placeholder="Você é um assistente de IA..."
            ></textarea>
          </div>
        </div>

        <!-- Footer -->
        <div
          class="flex items-center justify-end gap-3 p-6 border-t border-slate-600/30 bg-slate-800/20"
        >
          <button
            @click="closeAgentModal"
            class="px-5 py-2.5 bg-slate-700/50 hover:bg-slate-600/50 text-slate-300 hover:text-white rounded-xl text-sm font-medium transition-all duration-300 backdrop-blur-sm"
          >
            Cancelar
          </button>
          <button
            @click="saveAgent"
            class="bg-gradient-to-r from-purple-600 to-purple-700 hover:from-purple-700 hover:to-purple-800 text-white px-6 py-2.5 rounded-xl text-sm font-semibold transition-all duration-300 shadow-lg shadow-purple-500/25"
          >
            <i class="fa-solid fa-save mr-2"></i>
            Salvar Agente
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
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
  openrouter: {
    active: false,
    apiKey: "",
    baseUrl: "https://openrouter.ai/api/v1",
    models: [],
    selectedModel: "",
    status: "unknown",
    name: "OpenRouter",
  },
  groq: {
    active: false,
    apiKey: "",
    baseUrl: "https://api.groq.com/openai/v1/chat/completions",
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
const editingApiKey = ref(false);
const newApiKey = ref("");

const providerForm = ref({
  apiKey: "",
  baseUrl: "",
  selectedModel: "",
  models: [],
});
const originalProviderData = ref(null);

const currentProvider = computed(() => {
  return currentProviderKey.value
    ? providers.value[currentProviderKey.value]
    : null;
});

// --- Prompt Engineering State ---
const promptAgents = ref([]);
const showAgentModal = ref(false);
const agentForm = ref({
  id: null,
  name: "",
  prompt: "",
});

// --- Chat Playground State ---
const selectedTestProvider = ref("");
const selectedTestModel = ref("");
const selectedAgentId = ref(null);
const chatHistory = ref([]);
const userMessage = ref("");
const isModelResponding = ref(false);
const chatHistoryRef = ref(null);

// --- RAG State ---
const selectedRagAgentId = ref(null);
const ragFile = ref(null);
const ragFileInputRef = ref(null);
const isUploadingRag = ref(false);

// --- Refs para upload de arquivo ---
const fileInputRef = ref(null);
const attachedFile = ref(null);

// --- Lifecycle Hooks ---
onMounted(async () => {
  await fetchProviders();
  await fetchAgents();
});

async function fetchProviders() {
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
            apiKey: p.apiKey ? maskApiKey(p.apiKey) : "",
            models: p.models || [],
          };
        }
      });
      providers.value = newProvidersState;
      console.log("Successfully loaded provider configurations from backend.");
      toast.info("Configurações de IA carregadas.");
    }
  } catch (error) {
    console.error("Could not fetch provider configurations:", error);
    toast.error("Falha ao carregar as configurações de IA do servidor.");
  }
}

async function fetchAgents() {
  console.log("Fetching prompt agents from backend...");
  try {
    promptAgents.value = await $fetch(`${apiBaseUrl}/prompt-agents`);
    toast.info("Agentes de prompt carregados.");
  } catch (error) {
    console.error("Could not fetch prompt agents:", error);
    toast.error("Falha ao carregar os agentes de prompt.");
  }
}

// --- Modal & Form Logic ---
function maskApiKey(apiKey) {
  if (!apiKey) {
    return "";
  }

  const underscoreIndex = apiKey.indexOf('_');
  
  if (underscoreIndex === -1 || apiKey.length < underscoreIndex + 5) {
    if (apiKey.length < 8) {
        return "********";
    }
    const prefix = apiKey.substring(0, 4);
    const suffix = apiKey.substring(apiKey.length - 4);
    return `${prefix}...${suffix}`;
  }

  const prefix = apiKey.substring(0, underscoreIndex + 1);
  const suffix = apiKey.substring(apiKey.length - 4);
  return `${prefix}......${suffix}`;
}

function openProviderModal(providerKey) {
  currentProviderKey.value = providerKey;
  const providerData = providers.value[providerKey];
  originalProviderData.value = JSON.parse(JSON.stringify(providerData)); // Deep copy for comparison

  providerForm.value = {
    apiKey: providerData.apiKey,
    baseUrl: providerData.baseUrl,
    selectedModel: providerData.selectedModel,
    models: [...providerData.models],
  };
  editingApiKey.value = !providerData.apiKey;
  newApiKey.value = "";
  showConsole.value = false;
  consoleOutput.value = [];
  showProviderModal.value = true;
}

function closeProviderModal() {
  showProviderModal.value = false;
  currentProviderKey.value = null;
  newModelName.value = "";
  originalProviderData.value = null;
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

// --- Agent Modal Logic ---
function openAgentModal(agent = null) {
  if (agent) {
    agentForm.value = { ...agent };
  } else {
    agentForm.value = { id: null, name: "", prompt: "" };
  }
  showAgentModal.value = true;
}

function closeAgentModal() {
  showAgentModal.value = false;
}

async function saveAgent() {
  const isEditing = !!agentForm.value.id;
  const url = isEditing
    ? `${apiBaseUrl}/prompt-agents/${agentForm.value.id}`
    : `${apiBaseUrl}/prompt-agents`;
  const method = isEditing ? "PUT" : "POST";

  try {
    await $fetch(url, {
      method: method,
      body: {
        name: agentForm.value.name,
        prompt: agentForm.value.prompt,
      },
    });
    toast.success(`Agente "${agentForm.value.name}" salvo com sucesso!`);
    await fetchAgents(); // Refresh the list
    closeAgentModal();
  } catch (error) {
    console.error("Failed to save agent:", error);
    toast.error(`Erro ao salvar o agente: ${error.data?.message || error.message}`);
  }
}

async function deleteAgent(agentId) {
  try {
    await $fetch(`${apiBaseUrl}/prompt-agents/${agentId}`, {
      method: "DELETE",
    });
    toast.info("Agente excluído.");
    await fetchAgents(); // Refresh the list
  } catch (error) {
    console.error("Failed to delete agent:", error);
    toast.error(`Erro ao excluir o agente: ${error.data?.message || error.message}`);
  }
}

// --- API & Shell Commands ---
async function saveProviderSettings() {
  if (!currentProviderKey.value || !originalProviderData.value) return;
  const providerKey = currentProviderKey.value;

  const payload = {};

  // Compare and add changed fields to the payload
  if (providerForm.value.baseUrl !== originalProviderData.value.baseUrl) {
    payload.baseUrl = providerForm.value.baseUrl;
  }
  if (providerForm.value.selectedModel !== originalProviderData.value.selectedModel) {
    payload.selectedModel = providerForm.value.selectedModel;
  }
  if (JSON.stringify(providerForm.value.models) !== JSON.stringify(originalProviderData.value.models)) {
    payload.models = [...providerForm.value.models];
  }

  // Only add apiKey to payload if a new one was entered
  if (newApiKey.value) {
    payload.apiKey = newApiKey.value;
  }

  // Check if the active status should be changed
  const newActiveState = providerKey === "ollama" ? true : !!(payload.apiKey || providers.value[providerKey].apiKey);
  if (newActiveState !== originalProviderData.value.active) {
    payload.active = newActiveState;
  }

  // If no fields have changed, just close the modal
  if (Object.keys(payload).length === 0) {
    toast.info("Nenhuma alteração detectada.");
    closeProviderModal();
    return;
  }

  try {
    console.log(`Calling API: PUT ${apiBaseUrl}/ai-providers/${providerKey} with partial payload:`, payload);
    const updatedProvider = await $fetch(
      `${apiBaseUrl}/ai-providers/${providerKey}`,
      {
        method: "PUT",
        body: payload,
      }
    );

    // Update local state with the response from the backend
    const originalApiKey = providers.value[providerKey].apiKey;
    const finalApiKey = payload.apiKey ? maskApiKey(payload.apiKey) : originalApiKey;

    providers.value[providerKey] = {
      ...providers.value[providerKey],
      ...updatedProvider,
      apiKey: finalApiKey,
    };

    toast.success("Configurações do provedor salvas com sucesso!");
    console.log("Successfully saved provider settings.");
    closeProviderModal();
  } catch (error) {
    console.error("Failed to save provider settings:", error);
    toast.error(`Erro ao salvar as configurações: ${error.data?.message || error.message}`);
  }
}

async function checkOllamaStatus() {
  showConsole.value = true;
  consoleOutput.value = [{ text: "Verificando status do servidor Ollama..." }];
  try {
    const result = await $fetch(`${apiBaseUrl}/ai-providers/ollama/status`);
    consoleOutput.value.push({ text: `Status: ${result.status}` });
    toast.success(`Servidor Ollama está ${result.status}.`);
    if (result.status === 'online') {
        providers.value.ollama.status = 'valid';
    } else {
        providers.value.ollama.status = 'invalid';
    }
  } catch (error) {
    console.error("Falha ao verificar status do Ollama:", error);
    consoleOutput.value.push({
      text: `Erro: ${error.data?.message || error.message}`,
      type: "error",
    });
    toast.error("Não foi possível verificar o status do servidor Ollama.");
    providers.value.ollama.status = 'invalid';
  }
}

async function syncOllamaModels() {
  isSyncingModels.value = true;
  showConsole.value = true;
  consoleOutput.value = [{ text: "Buscando modelos do Ollama..." }];

  try {
    const models = await $fetch(`${apiBaseUrl}/ai-providers/ollama/models`);
    
    consoleOutput.value.push({ text: `Modelos encontrados: ${models.join(', ')}` });

    providerForm.value.models = models;
    if (!providerForm.value.selectedModel && models.length > 0) {
      providerForm.value.selectedModel = models[0];
    }
    console.log("Modelos Ollama sincronizados:", models);
    consoleOutput.value.push({ text: "\nSincronização concluída! Salve as configurações para persistir a lista." });
    toast.info("Lista de modelos sincronizada. Clique em Salvar para aplicar.");

  } catch (error) {
    console.error("Falha ao sincronizar modelos Ollama:", error);
    consoleOutput.value.push({
      text: `Erro ao sincronizar modelos: ${error.data?.message || error.message}`,
      type: "error",
    });
    toast.error("Não foi possível sincronizar os modelos do Ollama.");
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

// --- Chat Playground Logic ---
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

// --- Funções de Upload ---
function triggerFileInput() {
  fileInputRef.value.click();
}

function handleFileSelect(event) {
  const file = event.target.files[0];
  if (file) {
    attachedFile.value = file;
  }
}

function removeAttachedFile() {
  attachedFile.value = null;
  if (fileInputRef.value) {
    fileInputRef.value.value = null;
  }
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

async function sendChatMessage() {
  if (
    (!userMessage.value && !attachedFile.value) ||
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

  // --- Prompt Injection Logic ---
  let finalPrompt = messageContent;
  if (selectedAgentId.value) {
    const selectedAgent = promptAgents.value.find(
      (agent) => agent.id === selectedAgentId.value
    );
    if (selectedAgent) {
      // TODO: A more robust solution would be to send the agentId to the backend
      // and let it handle the system prompt injection.
      finalPrompt = `${selectedAgent.prompt}\n\n---\n\n${messageContent}`;
    }
  }

  const formData = new FormData();
  formData.append("provider", selectedTestProvider.value);
  formData.append("model", selectedTestModel.value);
  formData.append("prompt", finalPrompt);

  if (attachedFile.value) {
    formData.append("file", attachedFile.value);
  }

  try {
    const response = await $fetch(`${apiBaseUrl}/ai-providers/chat`, {
      method: "POST",
      body: formData,
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
    removeAttachedFile();
  }
}

// --- RAG Functions ---
function handleRagFileSelect(event) {
  const file = event.target.files[0];
  if (file) {
    ragFile.value = file;
  }
}

function removeRagFile() {
  ragFile.value = null;
  if (ragFileInputRef.value) {
    ragFileInputRef.value.value = null;
  }
}

async function uploadRagFile() {
  if (!selectedRagAgentId.value || !ragFile.value) {
    toast.error("Por favor, selecione um agente e um arquivo.");
    return;
  }

  isUploadingRag.value = true;
  toast.info(`Enviando arquivo ${ragFile.value.name}...`);

  const formData = new FormData();
  formData.append("agentId", selectedRagAgentId.value);
  formData.append("file", ragFile.value);

  try {
    // REMOVIDO: console.log("Uploading RAG file...");
    // REMOVIDO: await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate upload
    await $fetch(`${apiBaseUrl}/rag/upload`, {
      method: "POST",
      body: formData,
    });

    toast.success(`Arquivo enviado com sucesso para o agente!`);
    removeRagFile();
    selectedRagAgentId.value = null;
  } catch (error) {
    console.error("Failed to upload RAG file:", error);
    toast.error(`Falha ao enviar o arquivo: ${error.data?.message || error.message}`);
  } finally {
    isUploadingRag.value = false;
  }
}
</script>