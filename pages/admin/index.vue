<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 relative overflow-hidden"
  >
    <!-- Animated background particles -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div
        class="absolute -top-40 -right-40 w-80 h-80 bg-purple-500/5 rounded-full blur-3xl animate-pulse"
      ></div>
      <div
        class="absolute -bottom-40 -left-40 w-80 h-80 bg-indigo-500/5 rounded-full blur-3xl animate-pulse"
        style="animation-delay: 2s"
      ></div>
      <div
        class="absolute top-1/2 left-1/3 w-60 h-60 bg-cyan-500/3 rounded-full blur-3xl animate-pulse"
        style="animation-delay: 4s"
      ></div>
    </div>

    <!-- Modern Header with Enhanced Glass Effect -->
    <header
      class="app-header relative backdrop-blur-2xl border-b border-white/5 p-4 shadow-2xl"
    >
      <!-- Glass morphism background with gradient -->
      <div
        class="absolute inset-0 bg-gradient-to-r from-slate-900/40 via-slate-800/50 to-slate-900/40 backdrop-blur-3xl"
      ></div>
      <!-- Shimmer effect -->
      <div
        class="absolute inset-0 bg-gradient-to-r from-transparent via-white/[0.02] to-transparent animate-shimmer"
      ></div>

      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-6">
          <!-- Enhanced icon with glow -->
          <div class="relative group">
            <div
              class="absolute inset-0 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl blur-lg opacity-50 group-hover:opacity-75 transition-opacity duration-300"
            ></div>
            <div
              class="relative p-4 bg-gradient-to-br from-purple-600 via-purple-500 to-indigo-600 rounded-2xl shadow-2xl transform group-hover:scale-105 transition-transform duration-300"
            >
              <i class="fa-solid fa-cog text-white text-lg"></i>
            </div>
          </div>

          <div class="space-y-1">
            <h1 class="app-header-title">Painel Administrativo</h1>
            <p class="app-header-subtitle">
              Gerencie usuários, configurações e sistema com total controle
            </p>
          </div>
        </div>
      </div>
    </header>

    <!-- Enhanced Admin navigation tabs -->
    <div class="relative backdrop-blur-xl border-b border-white/10">
      <!-- Glass morphism background -->
      <div
        class="absolute inset-0 bg-gradient-to-r from-slate-900/30 via-slate-800/40 to-slate-900/30"
      ></div>

      <nav class="relative px-6">
        <div class="flex space-x-8 overflow-x-auto">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="relative py-4 px-2 border-b-2 font-medium text-sm whitespace-nowrap transition-all duration-300"
            :class="
              activeTab === tab.value
                ? 'border-purple-500 text-purple-400'
                : 'border-transparent text-slate-400 hover:text-slate-300 hover:border-slate-600'
            "
            @click="activeTab = tab.value"
          >
            <i :class="tab.icon" class="mr-2"></i>
            {{ tab.label }}
            <!-- Active tab glow -->
            <div
              v-if="activeTab === tab.value"
              class="absolute inset-0 bg-gradient-to-r from-purple-600/10 to-indigo-600/10 rounded-t-lg -z-10"
            ></div>
          </button>
        </div>
      </nav>
    </div>

    <main class="relative z-10 p-6">
      <section v-if="activeTab === 'users'">
        <div
          class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4 gap-3"
        >
          <h2 class="text-xl font-semibold">Usuários</h2>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-4 py-3 md:px-3 md:py-2 rounded-md text-sm flex items-center gap-2 w-full sm:w-auto justify-center disabled:opacity-60 disabled:cursor-not-allowed"
            @click="openNewUserModal"
            :disabled="!canManageUsers"
          >
            <i class="fa-solid fa-user-plus"></i>
            Novo Usuário
          </button>
        </div>

        <div class="bg-gray-800 rounded-lg overflow-hidden">
          <div v-if="usersList.length === 0" class="p-8 text-center">
            <i class="fa-solid fa-users text-4xl text-slate-400 mb-3"></i>
            <p class="text-slate-300">Nenhum usuário cadastrado</p>
            <p class="text-slate-400 text-sm">
              Clique em "Novo Usuário" para começar
            </p>
          </div>
          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm text-left min-w-[800px]">
              <thead class="bg-gray-700/60 text-xs text-slate-300 uppercase">
                <tr>
                  <th class="px-3 md:px-6 py-3 min-w-[120px]">Nome</th>
                  <th class="px-3 md:px-6 py-3 min-w-[150px]">Email</th>
                  <th class="px-3 md:px-6 py-3 min-w-[100px]">Roles</th>
                  <th class="px-3 md:px-6 py-3 min-w-[140px]">Documentos</th>
                  <th class="px-3 md:px-6 py-3 min-w-[140px]">
                    Dados Bancários
                  </th>
                  <th class="px-3 md:px-6 py-3 min-w-[120px]">PIX</th>
                  <th class="px-3 md:px-6 py-3 min-w-[100px]">Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="user in usersList"
                  :key="user.id"
                  class="border-b border-gray-700 hover:bg-slate-700/30"
                >
                  <td class="px-3 md:px-6 py-3 font-medium">{{ user.name }}</td>
                  <td class="px-3 md:px-6 py-3 text-slate-300">
                    {{ user.email }}
                  </td>
                  <td class="px-3 md:px-6 py-3">
                    <div class="flex gap-1 flex-wrap">
                      <span
                        v-for="role in user.roles"
                        :key="role"
                        class="text-[10px] px-2 py-0.5 bg-slate-700/60 text-slate-300 rounded-md"
                      >
                        {{ role }}
                      </span>
                    </div>
                  </td>
                  <td class="px-3 md:px-6 py-3">
                    <div class="text-xs text-slate-400">
                      <div v-if="user.cpf">CPF: {{ formatCPF(user.cpf) }}</div>
                      <div v-if="user.cnpj">
                        CNPJ: {{ formatCNPJ(user.cnpj) }}
                      </div>
                      <div v-if="user.rg">RG: {{ user.rg }}</div>
                      <div
                        v-if="!user.cpf && !user.cnpj && !user.rg"
                        class="text-slate-500"
                      >
                        -
                      </div>
                    </div>
                  </td>
                  <td class="px-3 md:px-6 py-3">
                    <div class="text-xs text-slate-400">
                      <div v-if="user.banco">{{ user.banco }}</div>
                      <div v-if="user.agencia && user.conta">
                        Ag: {{ user.agencia }} / CC: {{ user.conta }}
                      </div>
                      <div v-if="user.tipoConta">{{ user.tipoConta }}</div>
                      <div
                        v-if="!user.banco && !user.agencia && !user.conta"
                        class="text-slate-500"
                      >
                        -
                      </div>
                    </div>
                  </td>
                  <td class="px-3 md:px-6 py-3">
                    <div class="text-xs text-slate-400">
                      <div v-if="user.pixTipo && user.pixChave">
                        {{ user.pixTipo }}: {{ user.pixChave }}
                      </div>
                      <div v-else class="text-slate-500">-</div>
                    </div>
                  </td>
                  <td class="px-3 md:px-6 py-3">
                    <div class="flex items-center gap-1 md:gap-2">
                      <button
                        class="text-slate-400 hover:text-white p-1.5 md:p-2 rounded-md hover:bg-slate-600 transition-colors"
                        @click="editUser(user)"
                        title="Editar"
                      >
                        <i class="fa-solid fa-pen text-sm"></i>
                      </button>
                      <button
                        class="text-red-400 hover:text-red-300 p-1.5 md:p-2 rounded-md hover:bg-red-900/20 transition-colors"
                        @click="deleteUser(user.id || '')"
                        title="Excluir"
                      >
                        <i class="fa-solid fa-trash text-sm"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-else-if="activeTab === 'pipeline'">
        <h2 class="text-xl font-semibold mb-2">Configurações da Esteira</h2>
        <div class="bg-gray-800 rounded-lg p-4 space-y-3">
          <div class="space-y-3">
            <div class="flex flex-wrap gap-3 items-end">
              <div class="min-w-[16rem] flex-1 sm:flex-none">
                <label class="text-[12px] text-slate-300"
                  >Novo processo (nome)</label
                >
                <input
                  v-model="newProcName"
                  class="mt-1 bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="ex.: ConsorEquity"
                />
              </div>
              <div class="flex-1 min-w-[16rem]">
                <label class="text-[12px] text-slate-300">Tipo do processo</label>
                <div class="mt-2 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2">
                  <button
                    v-for="option in processTypeOptions"
                    :key="option.value"
                    type="button"
                    @click="newProcessType = option.value"
                    :class="[
                      'relative flex items-start gap-3 px-3 py-3 rounded-xl border transition-all text-left',
                      newProcessType === option.value
                        ? 'border-indigo-500 bg-indigo-500/10 shadow-lg shadow-indigo-500/20'
                        : 'border-slate-700/60 bg-slate-900/40 hover:border-indigo-500/40'
                    ]"
                  >
                    <div
                      class="w-9 h-9 rounded-lg bg-slate-900/70 flex items-center justify-center text-base"
                    >
                      <i :class="option.icon"></i>
                    </div>
                    <div class="flex-1">
                      <p class="text-sm font-medium text-slate-100">
                        {{ option.label }}
                      </p>
                      <p class="text-[11px] text-slate-400">
                        {{ option.description }}
                      </p>
                    </div>
                    <i
                      v-if="newProcessType === option.value"
                      class="fa-solid fa-circle-check text-indigo-400 absolute top-2 right-2"
                    ></i>
                  </button>
                </div>
              </div>
              <div class="flex-1 min-w-[16rem]">
                <label class="text-[12px] text-slate-300">Usuários autorizados</label>
                <div class="mt-2 flex flex-wrap gap-2">
                  <span
                    v-for="user in newProcessUserDetails"
                    :key="user.id"
                    class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
                  >
                    <span class="truncate max-w-[160px]">{{ user.label }}</span>
                    <button
                      class="text-indigo-200/80 hover:text-white"
                      type="button"
                      @click="removeNewProcessUser(user.id)"
                    >
                      <i class="fa-solid fa-xmark text-[10px]"></i>
                    </button>
                  </span>
                  <button
                    type="button"
                    class="inline-flex items-center gap-2 rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-[12px] text-slate-200 hover:border-indigo-500/50 hover:text-white"
                    @click="openUserSelector('create')"
                  >
                    <i class="fa-solid fa-user-plus text-xs"></i>
                    Gerenciar acesso
                  </button>
                </div>
                <p class="text-[11px] text-slate-400 mt-1">
                  Deixe vazio para liberar o acesso a todos.
                </p>
              </div>
              <div class="flex items-center gap-2">
                <button
                  class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white p-2 rounded-md text-sm disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center w-9 h-9"
                  :disabled="creatingProcess"
                  @click="createProcess"
                  title="Adicionar processo"
                  aria-label="Adicionar processo"
                >
                  <i
                    v-if="creatingProcess"
                    class="fa-solid fa-spinner fa-spin"
                  ></i>
                  <i v-else class="fa-solid fa-plus"></i>
                </button>
                <button
                  class="bg-slate-700 hover:bg-slate-600 text-white p-2 rounded-md text-sm w-9 h-9 flex items-center justify-center"
                  @click="openResetModal"
                  title="Zerar dados locais"
                  aria-label="Zerar dados locais"
                >
                  <i class="fa-solid fa-rotate-left"></i>
                </button>
              </div>
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Processos</label>
              <div class="mt-1 space-y-2">
                <div
                  v-for="p in processes"
                  :key="p.key"
                  class="flex items-center justify-between bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2"
                >
                  <label
                    class="inline-flex items-center gap-2 cursor-pointer"
                    @click="currentProcessKey = p.key"
                  >
                    <input
                      type="radio"
                      class="accent-indigo-500"
                      :checked="currentProcessKey === p.key"
                    />
                    <span class="text-sm font-medium">{{ p.name }}</span>
                    <span class="text-[11px] text-slate-400"
                      >({{ p.key }})</span
                    >
                    <span class="text-[11px] text-indigo-300">
                      {{ resolveProcessTypeLabel(p.type, p.isFinanceiro) }}
                    </span>
                  </label>
                  <div class="flex items-center gap-2">
                    <span
                      class="text-[11px]"
                      :class="
                        p.active !== false ? 'text-green-300' : 'text-slate-400'
                      "
                      >{{ p.active !== false ? "Ativo" : "Inativo" }}</span
                    >
                    <button
                      class="p-2 text-slate-300 hover:text-white transition-colors"
                      @click.stop="toggleActive(p)"
                      :title="p.active !== false ? 'Desativar' : 'Ativar'"
                      :aria-label="p.active !== false ? 'Desativar' : 'Ativar'"
                    >
                      <i
                        :class="
                          p.active !== false
                            ? 'fa-solid fa-power-off text-sky-400'
                            : 'fa-solid fa-play text-sky-400'
                        "
                      />
                    </button>
                    <button
                      class="p-2 text-slate-300 hover:text-sky-400 transition-colors"
                      @click.stop="editProcess(p)"
                      title="Editar"
                      aria-label="Editar"
                    >
                      <i class="fa-regular fa-pen-to-square"></i>
                    </button>
                    <button
                      class="p-2 text-red-400 hover:text-red-300 transition-colors"
                      @click.stop="openDeleteModal(p)"
                      title="Excluir"
                      aria-label="Excluir"
                    >
                      <i class="fa-regular fa-trash-can"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="text-[12px] text-slate-300">
            Selecione um processo acima e clique em
            <span class="text-indigo-400 font-medium">Editar</span> para
            configurar colunas e inputs no modal.
          </div>
        </div>
      </section>

      <section v-else-if="activeTab === 'status'">
        <div
          class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4 gap-3"
        >
          <h2 class="text-xl font-semibold">Gerenciar Status</h2>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-4 py-3 md:px-3 md:py-2 rounded-md text-sm flex items-center gap-2 w-full sm:w-auto justify-center"
            @click="openNewStatusModal"
          >
            <i class="fa-solid fa-plus"></i>
            Novo Status
          </button>
        </div>

        <!-- Category Filter -->
        <div class="mb-4">
          <div class="flex flex-wrap gap-2">
            <button
              class="px-3 py-2 md:py-1.5 text-sm rounded-md border transition-colors"
              :class="
                selectedCategory === null
                  ? 'bg-indigo-600 text-white border-indigo-500'
                  : 'bg-slate-700 text-slate-300 border-slate-600 hover:bg-slate-600'
              "
              @click="filterByCategory(null)"
            >
              Todos
            </button>
            <button
              v-for="cat in statusCategories"
              :key="cat.value"
              class="px-3 py-2 md:py-1.5 text-sm rounded-md border transition-colors"
              :class="
                selectedCategory === cat.value
                  ? 'bg-indigo-600 text-white border-indigo-500'
                  : 'bg-slate-700 text-slate-300 border-slate-600 hover:bg-slate-600'
              "
              @click="filterByCategory(cat.value)"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>

        <div class="bg-gray-800 rounded-lg overflow-hidden">
          <div v-if="filteredStatusList.length === 0" class="p-8 text-center">
            <i class="fa-solid fa-circle-info text-4xl text-slate-400 mb-3"></i>
            <p class="text-slate-300" v-if="statusList.length === 0">
              Nenhum status cadastrado
            </p>
            <p class="text-slate-300" v-else>
              Nenhum status encontrado para esta categoria
            </p>
            <p class="text-slate-400 text-sm" v-if="statusList.length === 0">
              Clique em "Novo Status" para começar
            </p>
          </div>
          <div v-else class="divide-y divide-slate-700">
            <div
              v-for="status in filteredStatusList"
              :key="status.id"
              class="flex items-center justify-between px-4 py-3 hover:bg-slate-700/30 group"
            >
              <div class="flex items-center gap-3">
                <!-- Status Tag (dynamic color effect) -->
                <span
                  class="inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-1 rounded-lg transition-all duration-300 hover:scale-105 whitespace-nowrap"
                  :style="getStatusStyle(status)"
                >
                  <div
                    class="w-1.5 h-1.5 rounded-full bg-current opacity-60 flex-shrink-0"
                  ></div>
                  <span class="truncate">{{ status.name }}</span>
                </span>

                <div class="text-xs text-slate-400">{{ status.color }}</div>

                <!-- Category Badge -->
                <span
                  class="text-[10px] px-2 py-0.5 bg-slate-700/60 text-slate-300 rounded-md"
                >
                  {{ getCategoryLabel(status.category) }}
                </span>
              </div>

              <div class="flex items-center gap-2">
                <button
                  class="text-slate-400 hover:text-white p-2 rounded-md hover:bg-slate-600 transition-colors"
                  @click="editStatus(status)"
                  title="Editar"
                >
                  <i class="fa-solid fa-pen text-sm"></i>
                </button>
                <button
                  class="text-red-400 hover:text-red-300 p-2 rounded-md hover:bg-red-900/20 transition-colors"
                  @click="deleteStatus(status.id)"
                  title="Excluir"
                >
                  <i class="fa-solid fa-trash text-sm"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-else>
        <h2 class="text-xl font-semibold mb-4">Notificações</h2>
        <div class="bg-gray-800 rounded-lg p-4">
          <p class="text-slate-300">Configurações simples de exemplo.</p>
        </div>
      </section>
    </main>

    <!-- Modal: Gestão da Esteira (BaseModal) -->
    <BaseModal
      v-model="showPipelineModal"
      title="Gestão da Esteira"
      size="lg"
      :z-index="55"
    >
      <div class="space-y-3 max-h-[70vh] overflow-y-auto pr-1">
        <div>
          <label class="text-[12px] text-slate-300">Nome do Processo</label>
          <input
            v-model="processName"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="ex.: ConsorEquity"
          />
        </div>
        <div>
          <label class="text-[12px] text-slate-300">Usuários com acesso à esteira</label>
          <div class="mt-2 flex flex-wrap gap-2">
            <span
              v-for="user in selectedProcessUserDetails"
              :key="user.id"
              class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
            >
              <span class="truncate max-w-[160px]">{{ user.label }}</span>
              <button
                class="text-indigo-200/80 hover:text-white"
                type="button"
                @click="removeSelectedProcessUser(user.id)"
              >
                <i class="fa-solid fa-xmark text-[10px]"></i>
              </button>
            </span>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-[12px] text-slate-200 hover:border-indigo-500/50 hover:text-white"
              @click="openUserSelector('edit')"
            >
              <i class="fa-solid fa-user-plus text-xs"></i>
              Gerenciar acesso
            </button>
          </div>
          <p class="text-[11px] text-slate-400 mt-1">
            Sem seleção, todos os usuários com acesso ao sistema enxergam esta esteira.
          </p>
        </div>
        <PipelineManager
          v-model:stages="pipelineStages"
          :pipeline-key="currentProcessKey"
        />
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="closePipelineModal"
          >
            Fechar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-3 py-2 rounded-md text-sm"
            @click="savePipelineModal"
          >
            Salvar
          </button>
        </div>
      </template>
    </BaseModal>

    <BaseModal
      v-model="showUserSelectorModal"
      title="Selecionar usuários"
      size="md"
    >
      <div class="space-y-3">
        <input
          v-model="userSearch"
          type="search"
          class="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
          placeholder="Buscar por nome ou e-mail"
        />
        <div class="max-h-64 overflow-y-auto rounded-lg border border-slate-800 bg-slate-900/60">
          <div
            v-if="!filteredModalUsers.length"
            class="px-3 py-4 text-center text-sm text-slate-500"
          >
            Nenhum usuário encontrado.
          </div>
          <button
            v-for="user in filteredModalUsers"
            :key="user.id"
            type="button"
            :class="[
              'w-full flex items-center justify-between px-3 py-2 text-sm transition-colors border-b border-slate-800 last:border-b-0',
              modalSelectionSet.has(user.id)
                ? 'bg-indigo-500/10 text-indigo-200 border-indigo-500/30'
                : 'text-slate-200 hover:bg-slate-800/60'
            ]"
            @click="toggleUserInSelection(user.id)"
          >
            <span class="truncate max-w-[220px] text-left">{{ user.label }}</span>
            <i
              :class="modalSelectionSet.has(user.id)
                ? 'fa-solid fa-check text-indigo-300'
                : 'fa-regular fa-circle text-slate-500'"
            ></i>
          </button>
        </div>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="id in userSelectorSelection"
            :key="id"
            class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
          >
            <span class="truncate max-w-[160px]">
              {{ userMap.get(id)?.label || id }}
            </span>
            <button
              class="text-indigo-200/80 hover:text-white"
              type="button"
              @click="toggleUserInSelection(id)"
            >
              <i class="fa-solid fa-xmark text-[10px]"></i>
            </button>
          </span>
        </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-300 hover:bg-slate-800"
            @click="closeUserSelectorModal"
          >
            Cancelar
          </button>
          <button
            class="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-500"
            @click="confirmUserSelection"
          >
            Confirmar
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Confirmar Exclusão de Processo -->
    <BaseModal
      v-model="showDeleteModal"
      title="Confirmar exclusão"
      size="sm"
      :z-index="60"
    >
      <div class="space-y-2">
        <p class="text-sm text-slate-200">
          Para confirmar, digite exatamente o nome do processo abaixo.
        </p>
        <div
          class="rounded-md border border-slate-700/60 bg-slate-900/40 px-3 py-2"
        >
          <div class="text-white text-sm font-semibold">
            {{ deleteTarget?.name || deleteTarget?.key }}
          </div>
          <div class="text-[11px] text-slate-400">{{ deleteTarget?.key }}</div>
        </div>
        <div>
          <label class="text-[12px] text-slate-300"
            >Digite o nome do processo</label
          >
          <input
            v-model="deleteConfirm"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            :placeholder="deleteTarget?.name || ''"
            autocomplete="off"
            autocapitalize="off"
            spellcheck="false"
            @paste.prevent
            @drop.prevent
            @copy.prevent
            @cut.prevent
            @contextmenu.prevent
            @keydown="onDeleteConfirmKeydown"
          />
          <div
            v-if="deleteConfirm && !canConfirmDelete"
            class="text-[11px] text-red-400 mt-1"
          >
            O nome não confere.
          </div>
        </div>
        <p class="text-[12px] text-slate-300">
          Esta ação remove as colunas, inputs e cards salvos localmente para
          este processo.
        </p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="cancelDelete"
          >
            Cancelar
          </button>
          <button
            class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!canConfirmDelete"
            @click="confirmDelete"
          >
            Excluir
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Resetar dados locais -->
    <BaseModal
      v-model="showResetModal"
      title="Zerar dados locais"
      size="sm"
      :z-index="60"
    >
      <div class="space-y-2">
        <p class="text-sm text-slate-200">
          Isto apagará todos os processos, colunas, inputs e cards salvos no
          navegador.
        </p>
        <p class="text-[12px] text-slate-300">
          Use esta opção apenas para testes locais. Não há como desfazer.
        </p>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="cancelReset"
          >
            Cancelar
          </button>
          <button
            class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm"
            @click="confirmReset"
          >
            Zerar
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: Status Management -->
    <BaseModal
      v-model="showStatusModal"
      :title="editingStatus ? 'Editar Status' : 'Novo Status'"
      size="sm"
      :z-index="70"
    >
      <div class="space-y-4">
        <div>
          <label class="text-[12px] text-slate-300">Nome do Status</label>
          <input
            v-model="statusForm.name"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="Ex.: Em análise"
          />
        </div>

        <div>
          <label class="text-[12px] text-slate-300">Categoria</label>
          <select
            v-model="statusForm.category"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
          >
            <option
              v-for="cat in statusCategories"
              :key="cat.value"
              :value="cat.value"
            >
              {{ cat.label }}
            </option>
          </select>
          <p class="text-[11px] text-slate-400 mt-1">
            Define onde este status será usado (esteira, usuários ou sistema)
          </p>
        </div>

        <div>
          <label class="text-[12px] text-slate-300">Cor</label>
          <div class="mt-2 grid grid-cols-5 gap-2">
            <button
              v-for="color in statusColors"
              :key="color.value"
              class="w-8 h-8 rounded-full border-2 hover:scale-110 transition-transform"
              :class="
                statusForm.color === color.value
                  ? 'border-white'
                  : 'border-slate-600'
              "
              :style="{ backgroundColor: color.value }"
              :title="color.name"
              @click="statusForm.color = color.value"
            />
          </div>
        </div>

        <div class="mt-4 p-3 bg-slate-800/40 rounded-lg">
          <div class="text-[12px] text-slate-300 mb-2">Pré-visualização:</div>
          <div class="flex items-center gap-3">
            <div
              class="w-3 h-3 rounded-full"
              :style="{ backgroundColor: statusForm.color }"
            ></div>
            <span class="text-sm text-white">{{
              statusForm.name || "Nome do Status"
            }}</span>
            <span
              class="text-[10px] px-2 py-0.5 bg-slate-700/60 text-slate-300 rounded-md"
            >
              {{ getCategoryLabel(statusForm.category) }}
            </span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="closeStatusModal"
          >
            Cancelar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-3 py-2 rounded-md text-sm"
            @click="saveStatus"
          >
            {{ editingStatus ? "Atualizar" : "Criar" }}
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Modal: User Management -->
    <BaseModal
      v-model="showUserModal"
      :title="editingUser ? 'Editar Usuário' : 'Novo Usuário'"
      size="lg"
      :z-index="70"
    >
      <div class="space-y-4 max-h-[60vh] md:max-h-[70vh] overflow-y-auto pr-1">
        <!-- Dados básicos -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="text-[12px] text-slate-300">Nome *</label>
            <input
              v-model="userForm.name"
              class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
              placeholder="Nome completo"
            />
          </div>
          <div>
            <div class="flex items-center justify-between">
              <label class="text-[12px] text-slate-300">Email *</label>
              <div v-if="editingUser" class="flex items-center">
                <button
                  type="button"
                  @click="allowEmailEdit = !allowEmailEdit"
                  class="p-1 text-slate-400 hover:text-indigo-400 transition-colors"
                  :title="
                    allowEmailEdit
                      ? 'Desabilitar edição do email'
                      : 'Habilitar edição do email'
                  "
                >
                  <i
                    :class="
                      allowEmailEdit
                        ? 'fa-regular fa-envelope'
                        : 'fa-solid fa-envelope-circle-check'
                    "
                    class="text-sm"
                  ></i>
                </button>
              </div>
            </div>
            <div class="relative">
              <input
                v-model="userForm.email"
                type="email"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm pr-10"
                placeholder="email@exemplo.com"
                :disabled="
                  (editingUser && !allowEmailEdit) ||
                  (!editingUser && currentUserId)
                "
                @blur="checkEmailAvailability"
                @input="onEmailInput"
              />
              <div
                v-if="emailCheckLoading"
                class="absolute right-3 top-1/2 transform -translate-y-1/2"
              >
                <i class="fa-solid fa-spinner fa-spin text-orange-500"></i>
              </div>
              <div
                v-else-if="emailAvailable === true"
                class="absolute right-3 top-1/2 transform -translate-y-1/2"
              >
                <i class="fa-solid fa-check text-green-500"></i>
              </div>
              <div
                v-else-if="emailAvailable === false"
                class="absolute right-3 top-1/2 transform -translate-y-1/2"
              >
                <i class="fa-solid fa-times text-red-500"></i>
              </div>
            </div>
            <div v-if="emailError" class="text-[11px] text-red-400 mt-1">
              {{ emailError }}
            </div>
            <div
              v-else-if="emailAvailable === true"
              class="text-[11px] text-green-400 mt-1"
            >
              Email disponível
            </div>
            <div
              v-else-if="emailAvailable === false"
              class="text-[11px] text-red-400 mt-1"
            >
              Email já está em uso
            </div>
          </div>
        </div>

        <!-- Foto de Perfil -->
        <div class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            Foto de Perfil
          </h4>
          <UserFileUpload
            label="Foto de Perfil"
            accept="image/*"
            v-model="userForm.fotoPerfilUrl"
            :userId="currentUserId"
            :temporaryMode="!currentUserId"
            :sessionId="tempUploadSessionId"
            fileType="PROFILE_PHOTO"
            @file-selected="onProfilePhotoSelected"
            @file-uploaded="onProfilePhotoUploaded"
          />
        </div>

        <!-- Documentos de Identificação -->
        <div class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            Documento de Identificação
          </h4>

          <!-- Tipo de Documento -->
          <div class="mb-3">
            <label class="text-[12px] text-slate-300">Tipo de Documento</label>
            <select
              v-model="userForm.tipoIdentificacao"
              class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            >
              <option value="">Selecione o tipo</option>
              <option value="RG">RG - Registro Geral</option>
              <option value="CNH">
                CNH - Carteira Nacional de Habilitação
              </option>
            </select>
          </div>

          <!-- Upload do Documento -->
          <UserFileUpload
            :label="`${userForm.tipoIdentificacao || 'Documento de Identificação'}`"
            accept="image/*,application/pdf"
            v-model="userForm.documentoIdentificacaoUrl"
            :userId="currentUserId"
            :temporaryMode="!currentUserId"
            :sessionId="tempUploadSessionId"
            :fileType="userForm.tipoIdentificacao || 'IDENTITY'"
            @file-selected="onIdentityDocumentSelected"
            @file-uploaded="onIdentityDocumentUploaded"
          />
        </div>

        <!-- Comprovante de Endereço -->
        <div class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            Comprovante de Endereço
          </h4>
          <p class="text-xs text-slate-400 mb-3">
            Aceita conta de luz, água, telefone, internet, extrato bancário,
            etc.
          </p>
          <UserFileUpload
            label="Comprovante de Endereço"
            accept="image/*,application/pdf"
            v-model="userForm.comprovanteEnderecoUrl"
            :userId="currentUserId"
            :temporaryMode="!currentUserId"
            :sessionId="tempUploadSessionId"
            fileType="ADDRESS_PROOF"
            @file-selected="onAddressProofSelected"
            @file-uploaded="onAddressProofUploaded"
          />
        </div>

        <div v-if="!editingUser">
          <label class="text-[12px] text-slate-300">Senha *</label>
          <input
            v-model="userForm.password"
            type="password"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="Mínimo 6 caracteres"
          />
        </div>

        <div>
          <label class="text-[12px] text-slate-300">Roles *</label>
          <div class="mt-2 space-y-2">
            <label
              v-for="role in roleOptions"
              :key="role.value"
              class="flex items-center gap-2 cursor-pointer"
            >
              <input
                type="checkbox"
                :checked="userForm.roles.includes(role.value)"
                @change="toggleRole(role.value)"
                :disabled="!canToggleRole(role.value)"
                class="accent-indigo-500"
              />
              <span class="text-sm text-slate-200">{{ role.label }}</span>
              <span
                v-if="!canToggleRole(role.value) && userForm.roles.includes(role.value)"
                class="text-[10px] text-slate-400"
              >
                (bloqueado)
              </span>
            </label>
            <div v-if="extraRoles.length" class="text-[10px] text-slate-400">
              Roles adicionais: {{ extraRoles.join(", ") }}
            </div>
          </div>
        </div>

        <!-- Documentos (opcionais) -->
        <div class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            Documentos (opcionais)
          </h4>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label class="text-[12px] text-slate-300">CPF</label>
              <input
                v-model="userForm.cpf"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="000.000.000-00"
                @input="formatCPFInput"
                maxlength="14"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">CNPJ</label>
              <input
                v-model="userForm.cnpj"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="00.000.000/0000-00"
                @input="formatCNPJInput"
                maxlength="18"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">RG</label>
              <input
                v-model="userForm.rg"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="00.000.000-0"
              />
            </div>
          </div>

          <!-- Comprovante de Endereço -->
          <UserFileUpload
            label="Comprovante de Endereço"
            accept="image/*,application/pdf"
            v-model="userForm.comprovanteEnderecoUrl"
            :userId="currentUserId"
            :temporaryMode="!currentUserId"
            :sessionId="tempUploadSessionId"
            fileType="ADDRESS_PROOF"
            @file-selected="onAddressProofSelected"
            @file-uploaded="onAddressProofUploaded"
          />
        </div>

        <!-- Dados bancários (opcionais) -->
        <div class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            Dados Bancários (opcionais)
          </h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-[12px] text-slate-300">Banco</label>
              <input
                v-model="userForm.banco"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="Ex: Banco do Brasil"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Tipo de Conta</label>
              <select
                v-model="userForm.tipoConta"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
              >
                <option value="">Selecione...</option>
                <option
                  v-for="tipo in tiposContas"
                  :key="tipo.value"
                  :value="tipo.value"
                >
                  {{ tipo.label }}
                </option>
              </select>
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Agência</label>
              <input
                v-model="userForm.agencia"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="0000"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Conta</label>
              <input
                v-model="userForm.conta"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="00000-0"
              />
            </div>
          </div>
        </div>

        <!-- PIX (opcionais) -->
        <div class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            PIX (opcionais)
          </h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-[12px] text-slate-300"
                >Tipo de Chave PIX</label
              >
              <select
                v-model="userForm.pixTipo"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
              >
                <option value="">Selecione...</option>
                <option
                  v-for="tipo in tiposPix"
                  :key="tipo.value"
                  :value="tipo.value"
                >
                  {{ tipo.label }}
                </option>
              </select>
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Chave PIX</label>
              <input
                v-model="userForm.pixChave"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                :placeholder="getPixPlaceholder()"
              />
            </div>
          </div>
        </div>

        <!-- Reconhecimento Facial (apenas para edição) -->
        <div v-if="editingUser" class="border-t border-slate-700 pt-4">
          <h4 class="text-sm font-medium text-slate-300 mb-3">
            Reconhecimento Facial
          </h4>
          <FaceRecognition
            mode="register"
            :userId="userForm.id"
            @success="onFaceRegistrationSuccess"
            @error="onFaceRegistrationError"
          />
        </div>
      </div>

      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="closeUserModal"
          >
            Cancelar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-3 py-2 rounded-md text-sm"
            @click="saveUser"
          >
            {{ editingUser ? "Atualizar" : "Criar" }}
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from "vue";

import BaseModal from "~/components/ui/BaseModal.vue";
import UserFileUpload from "~/components/ui/UserFileUpload.vue";
import FaceRecognition from "~/components/ui/FaceRecognition.vue";
import { useRoute } from "#imports";
import PipelineManager from "~/components/admin/PipelineManager.vue";

import { useGeolocation } from "~/composables/useGeolocation";
import { useTemporaryFileUpload } from "~/composables/useTemporaryFileUpload";
import { useCurrentUser } from "~/composables/useCurrentUser";
// CanvasFinance removido do Admin; ticker ficará apenas no Dashboard

definePageMeta({
  layout: "sidebar",
});
import {
  listProcesses,
  addProcess,
  setProcessActive,
  sanitizeProcessKey,
  removeProcess,
  setProcessName,
  renameProcessKey,
  applyProcessBlueprint,
} from "~/composables/usePipeline";
import {
  fetchStagesApi,
  saveStagesPreservingIdsApi,
} from "~/composables/useStages";
import { isApiEnabled } from "~/utils/api/index";
import { useProcessSubmenu } from "~/composables/useProcessMenu";

import { useToast } from "~/composables/useToast";
import {
  fetchStageFieldsApi,
  saveStageFieldsApi,
} from "~/composables/useStageFields";
import {
  fetchStatusesApi,
  createStatusApi,
  updateStatusApi,
  deleteStatusApi,
} from "~/composables/useStatusesApi";
import { STATUS_CATEGORIES } from "~/composables/useStatuses";
import {
  fetchUsersApi,
  createUserApi,
  updateUserApi,
  deleteUserApi,
} from "~/composables/useUsersApi";

useHead({ title: "Admin" });

const route = useRoute();
// Header shows only the market quotes ticker (CanvasFinance)
const {
  setLastKey,
  processes: menuProcesses,
  getLastKey,
} = useProcessSubmenu();
const activeTab = ref("users");
const mobileMenuOpen = ref(false);
const usersList = ref([]);

// Dados da navegação para o menu mobile
const tabs = [
  { value: "users", label: "Usuários", icon: "fa-solid fa-users" },
  { value: "pipeline", label: "Esteira", icon: "fa-solid fa-sitemap" },
  { value: "status", label: "Status", icon: "fa-solid fa-circle-info" },
  { value: "notifications", label: "Notificações", icon: "fa-solid fa-bell" },
];
const showUserModal = ref(false);
const userForm = ref({
  id: null,
  name: "",
  email: "",
  password: "",
  roles: [],
  cpf: "",
  cnpj: "",
  rg: "",
  banco: "",
  agencia: "",
  conta: "",
  tipoConta: "",
  pixTipo: "",
  pixChave: "",
  fotoPerfilUrl: "",
  comprovanteEnderecoUrl: "",
  localizacaoCadastro: "",
  // Novos campos para documentos
  tipoIdentificacao: "",
  documentoIdentificacaoUrl: "",
  documentoIdentificacaoObjectKey: "",
});

// Estado para validação de email
const emailCheckLoading = ref(false);
const emailAvailable = ref(null);
const emailError = ref("");
const emailCheckTimeout = ref(null);
const editingUser = ref(false);
const allowEmailEdit = ref(false);

// ID para upload de arquivos (usa o ID real do usuário ou um temporário)
const currentUserId = computed(() => {
  return userForm.value.id;
});

// Session ID para uploads temporários durante criação de usuário
const tempUploadSessionId = ref(null);

// Array para armazenar arquivos temporários enviados
const temporaryUploadedFiles = ref([]);

const { user: currentUser, load: loadCurrentUser } = useCurrentUser();

const roleOptions = [
  { value: "admin", label: "Administrador" },
  { value: "manager", label: "Gerente" },
  { value: "analyst", label: "Analista" },
  { value: "user", label: "Usuário" },
  { value: "viewer", label: "Visualizador" },
];

const normalizedCurrentRoles = computed(() => {
  const source = Array.isArray(currentUser.value?.roles)
    ? currentUser.value.roles
    : [];
  return new Set(
    source
      .filter((role) => typeof role === "string")
      .map((role) => role.trim().toLowerCase())
      .filter(Boolean)
  );
});

const isCurrentSuperUser = computed(() => currentUser.value?.superUser === true);

const hasIdentifiedActor = computed(() => {
  if (isCurrentSuperUser.value) {
    return true;
  }
  const roles = normalizedCurrentRoles.value;
  if (roles.size === 0) {
    return false;
  }
  return !(roles.size === 1 && roles.has("guest"));
});

const canManageUsers = computed(() => {
  if (isCurrentSuperUser.value) {
    return true;
  }
  if (!hasIdentifiedActor.value) {
    return true;
  }
  const roles = normalizedCurrentRoles.value;
  return roles.has("admin") || roles.has("manager");
});

const assignableRoleOptions = computed(() => {
  if (isCurrentSuperUser.value) {
    return roleOptions;
  }
  if (!hasIdentifiedActor.value) {
    return roleOptions;
  }
  const roles = normalizedCurrentRoles.value;
  if (roles.has("admin")) {
    return roleOptions.filter((role) =>
      ["user", "manager", "analyst"].includes(role.value)
    );
  }
  if (roles.has("manager")) {
    return roleOptions.filter((role) => role.value === "analyst");
  }
  return roleOptions;
});

const assignableRoleValues = computed(
  () => new Set(assignableRoleOptions.value.map((role) => role.value))
);

const canToggleRole = (roleValue) => {
  if (isCurrentSuperUser.value) {
    return true;
  }
  if (!hasIdentifiedActor.value) {
    return true;
  }
  return assignableRoleValues.value.has(roleValue);
};

const extraRoles = computed(() => {
  const currentRoles = Array.isArray(userForm.value?.roles)
    ? userForm.value.roles
    : [];
  return currentRoles.filter(
    (role) => !roleOptions.some((option) => option.value === role)
  );
});

const tiposContas = [
  { value: "CORRENTE", label: "Conta Corrente" },
  { value: "POUPANCA", label: "Poupança" },
];

const tiposPix = [
  { value: "CPF", label: "CPF" },
  { value: "CNPJ", label: "CNPJ" },
  { value: "EMAIL", label: "Email" },
  { value: "TELEFONE", label: "Telefone" },
  { value: "CHAVE_ALEATORIA", label: "Chave Aleatória" },
  { value: "OUTROS", label: "Outros" },
];

const {
  success: toastSuccess,
  error: toastError,
  info: toastInfo,
} = useToast();

// Cleaned up unused sidebar references

// Header breadcrumb current process name
const currentProcessName = computed(() => {
  const p = processes.value.find((x) => x.key === currentProcessKey.value);
  return p?.name || "";
});

// Process registry
const processes = ref(listProcesses());
const currentProcessKey = ref(processes.value[0]?.key || "");
const newProcName = ref("");
const newProcessType = ref('GENERIC');
const creatingProcess = ref(false);
const newProcessUsers = ref([]);
const selectedProcessUsers = ref([]);
const showUserSelectorModal = ref(false);
const userSelectorMode = ref('create');
const userSelectorSelection = ref([]);
const userSearch = ref('');

const processTypeOptions = [
  {
    value: 'GENERIC',
    label: 'Pipeline Livre',
    description: 'Fluxo flexível para diferentes contextos.',
    icon: 'fa-solid fa-diagram-project text-slate-300',
  },
  {
    value: 'FINANCIAL',
    label: 'Financeiro',
    description: 'Controle de etapas com indicadores financeiros.',
    icon: 'fa-solid fa-dollar-sign text-emerald-400',
  },
  {
    value: 'TODO_LIST',
    label: 'To-do List',
    description: 'Gestão de tarefas e checklists colaborativos.',
    icon: 'fa-solid fa-list-check text-sky-400',
  },
  {
    value: 'LEAD_QUALIFICATION',
    label: 'Qualificação de Leads',
    description: 'Integração direta com o cadastro de leads.',
    icon: 'fa-solid fa-user-check text-indigo-400',
  },
];

const processTypeLabels = {
  GENERIC: 'Pipeline Livre',
  FINANCIAL: 'Financeiro',
  TODO_LIST: 'To-do List',
  LEAD_QUALIFICATION: 'Qualificação de Leads',
};

const DEFAULT_PIPELINES = {
  GENERIC: [
    { id: 'briefing', title: 'Briefing Inicial', slaDays: 2, color: 'sky' },
    { id: 'execucao', title: 'Execução', slaDays: 5, color: 'violet' },
    { id: 'revisao', title: 'Revisão', slaDays: 3, color: 'amber' },
  ],
  FINANCIAL: [
    { id: 'entrada', title: 'Entrada de Dados', slaDays: 2, color: 'emerald' },
    { id: 'analise_credito', title: 'Análise de Crédito', slaDays: 3, color: 'cyan' },
    { id: 'formalizacao', title: 'Formalização', slaDays: 4, color: 'indigo' },
  ],
  TODO_LIST: [
    { id: 'planejamento', title: 'Planejamento', slaDays: 1, color: 'blue' },
    { id: 'execucao_tarefas', title: 'Execução de Tarefas', slaDays: 5, color: 'purple' },
    { id: 'validacao', title: 'Validação', slaDays: 2, color: 'lime' },
  ],
  LEAD_QUALIFICATION: [
    { id: 'captacao', title: 'Captação', slaDays: 1, color: 'blue', defaultStatus: 'Novo Lead' },
    { id: 'contato', title: 'Contato Inicial', slaDays: 2, color: 'emerald', defaultStatus: 'Contato Realizado' },
    { id: 'qualificacao', title: 'Qualificação', slaDays: 3, color: 'violet', defaultStatus: 'Qualificado' },
    { id: 'negociacao', title: 'Negociação', slaDays: 2, color: 'amber', defaultStatus: 'Em Negociação' },
  ],
};

function resolveProcessTypeLabel(type, isFinanceiro) {
  const normalized = typeof type === 'string' ? type.trim().toUpperCase() : undefined;
  if (!normalized) {
    return isFinanceiro ? processTypeLabels.FINANCIAL : processTypeLabels.GENERIC;
  }
  return processTypeLabels[normalized] || processTypeLabels.GENERIC;
}

async function createProcess() {
  const name = newProcName.value.trim();
  if (!name) return;
  // Gera UUID opaco para o processo (mais seguro e estável)
  const key =
    globalThis.crypto?.randomUUID?.() ||
    Math.random().toString(36).slice(2) + Date.now().toString(36);
  if (creatingProcess.value) return;
  creatingProcess.value = true;
  const selectedType = newProcessType.value;
  const blueprint = DEFAULT_PIPELINES[selectedType]
    ? [...DEFAULT_PIPELINES[selectedType]]
    : [];
  const allowedIds = sanitizeUserSelection(newProcessUsers.value);
  // Otimista: atualiza DOM primeiro
  processes.value = [
    ...processes.value,
    {
      key,
      name,
      active: true,
      type: selectedType,
      isFinanceiro: selectedType === 'FINANCIAL',
      allowedUserIds: allowedIds,
    },
  ];
  const ok = await addProcess(key, name || key, selectedType, allowedIds);
  if (ok) {
    currentProcessKey.value = key;
    setLastKey(key);
    if (blueprint.length) {
      try {
        await applyProcessBlueprint(key, blueprint);
        pipelineStages.value = [...blueprint];
      } catch (error) {
        console.error('Failed to apply blueprint:', error);
      }
    }
    newProcName.value = "";
    newProcessType.value = 'GENERIC';
    newProcessUsers.value = [];
    selectedProcessUsers.value = sanitizeUserSelection(allowedIds);
    toastSuccess("Processo criado");
  } else {
    // rollback visual
    processes.value = processes.value.filter((p) => p.key !== key);
    toastError("Falha ao criar processo. Tente novamente.");
  }
  creatingProcess.value = false;
}

const pipelineStages = ref([...DEFAULT_PIPELINES.GENERIC]);
const processName = ref("");

const userSelectionOptions = computed(() =>
  (usersList.value || [])
    .filter((user) => typeof user?.id === "string" && user.id)
    .map((user) => ({
      id: String(user.id),
      label: [user.name, user.email]
        .filter((value) => typeof value === "string" && value.trim())
        .join(" — ") || String(user.id),
    }))
);

const userMap = computed(() => {
  const map = new Map();
  for (const user of usersList.value || []) {
    if (!user?.id) continue;
    map.set(String(user.id), {
      id: String(user.id),
      name: user.name,
      email: user.email,
      label:
        [user.name, user.email]
          .filter((value) => typeof value === "string" && value.trim())
          .join(" — ") || String(user.id),
    });
  }
  return map;
});

const newProcessUserDetails = computed(() =>
  sanitizeUserSelection(newProcessUsers.value).map((id) =>
    userMap.value.get(id) || { id, label: id }
  )
);

const selectedProcessUserDetails = computed(() =>
  sanitizeUserSelection(selectedProcessUsers.value).map((id) =>
    userMap.value.get(id) || { id, label: id }
  )
);

function syncCurrentProcessUsers() {
  const current = processes.value.find((p) => p.key === currentProcessKey.value);
  const allowed = sanitizeUserSelection(current?.allowedUserIds || []);
  const validIds = new Set(userSelectionOptions.value.map((user) => user.id));
  selectedProcessUsers.value = allowed.filter((id) => validIds.size ? validIds.has(id) : true);
}

function sanitizeUserSelection(ids) {
  return Array.from(
    new Set(
      (ids || [])
        .filter((id) => typeof id === "string")
        .map((id) => id.trim())
        .filter(Boolean)
    )
  );
}

watch(userSelectionOptions, () => {
  const validIds = new Set(userSelectionOptions.value.map((user) => user.id));
  newProcessUsers.value = sanitizeUserSelection(
    newProcessUsers.value.filter((id) => (validIds.size ? validIds.has(id) : true))
  );
  selectedProcessUsers.value = sanitizeUserSelection(
    selectedProcessUsers.value.filter((id) => (validIds.size ? validIds.has(id) : true))
  );
});

watch(currentProcessKey, () => {
  syncCurrentProcessUsers();
});

watch(processes, () => {
  syncCurrentProcessUsers();
});

syncCurrentProcessUsers();

async function openUserSelector(mode) {
  if (!usersList.value.length) {
    try {
      await loadUsersList();
    } catch (_) {}
  }
  userSelectorMode.value = mode;
  userSelectorSelection.value = mode === 'create'
    ? [...sanitizeUserSelection(newProcessUsers.value)]
    : [...sanitizeUserSelection(selectedProcessUsers.value)];
  userSearch.value = '';
  showUserSelectorModal.value = true;
}

function closeUserSelectorModal() {
  showUserSelectorModal.value = false;
  userSelectorSelection.value = [];
  userSearch.value = '';
}

function toggleUserInSelection(id) {
  const normalized = String(id);
  const list = new Set(userSelectorSelection.value.map((value) => String(value)));
  if (list.has(normalized)) {
    list.delete(normalized);
  } else {
    list.add(normalized);
  }
  userSelectorSelection.value = Array.from(list);
}

function confirmUserSelection() {
  const sanitized = sanitizeUserSelection(userSelectorSelection.value);
  if (userSelectorMode.value === 'create') {
    newProcessUsers.value = sanitized;
  } else {
    selectedProcessUsers.value = sanitized;
  }
  closeUserSelectorModal();
}

function removeNewProcessUser(id) {
  newProcessUsers.value = sanitizeUserSelection(
    newProcessUsers.value.filter((value) => value !== id)
  );
}

function removeSelectedProcessUser(id) {
  selectedProcessUsers.value = sanitizeUserSelection(
    selectedProcessUsers.value.filter((value) => value !== id)
  );
}

const filteredModalUsers = computed(() => {
  const term = userSearch.value.trim().toLowerCase();
  if (!term) return userSelectionOptions.value;
  return userSelectionOptions.value.filter((user) =>
    user.label.toLowerCase().includes(term)
  );
});

const modalSelectionSet = computed(() => new Set(userSelectorSelection.value));

// Modal Gestão da Esteira
const showPipelineModal = ref(false);
function openPipelineModal() {
  showPipelineModal.value = true;
}
function closePipelineModal() {
  showPipelineModal.value = false;
}
async function savePipelineModal() {
  if (currentProcessKey.value) {
    // Quando backend ativo, não renomeamos a key (a API ainda não suporta). Apenas atualiza nome.
    if (!isApiEnabled()) {
      const base = sanitizeProcessKey(
        processName.value || currentProcessKey.value
      );
      const ensureUnique = (k) => {
        const existing = new Set(listProcesses().map((p) => p.key));
        if (!existing.has(k) || k === currentProcessKey.value) return k;
        let i = 2;
        while (existing.has(`${k}-${i}`)) i++;
        return `${k}-${i}`;
      };
      const desiredKey = ensureUnique(base);
      if (desiredKey !== currentProcessKey.value) {
        const res = renameProcessKey(currentProcessKey.value, desiredKey);
        if (!res.ok) return;
        currentProcessKey.value = desiredKey;
        setLastKey(desiredKey);
      }
    }
    const newName = processName.value || currentProcessKey.value;
    const sanitizedSelection = sanitizeUserSelection(selectedProcessUsers.value);
    const okSettings = await setProcessName(currentProcessKey.value, {
      name: newName,
      allowedUserIds: sanitizedSelection,
    });
    if (okSettings) {
      // Atualiza lista local sem novo GET
      processes.value = processes.value.map((p) =>
        p.key === currentProcessKey.value
          ? { ...p, name: newName, allowedUserIds: sanitizedSelection }
          : p
      );
    }
    try {
      // snapshot antes de salvar para migrar IDs de campos
      const prevStages = (pipelineStages.value || []).map((s) => ({
        id: s.id,
        title: s.title,
      }));
      const saved = await saveStagesPreservingIdsApi(
        currentProcessKey.value,
        prevStages,
        pipelineStages.value
      );
      if (Array.isArray(saved) && saved.length) {
        // Atualiza local com IDs reais do backend
        const asClient = saved.map((s) => ({
          id: s.id,
          title: s.title,
          slaDays: s.slaDays,
          color: s.color,
        }));
        pipelineStages.value = asClient;

        // Migra mapa de forms local dos IDs antigos para os novos e persiste no backend
        try {
          const storageKey = `pipeline_stage_forms__${currentProcessKey.value}`;
          const raw = localStorage.getItem(storageKey);
          const map = raw ? JSON.parse(raw) || {} : {};
          const nextMap = { ...map };
          for (let i = 0; i < asClient.length; i++) {
            const oldId = prevStages[i]?.id;
            const newId = asClient[i]?.id;
            if (!oldId || !newId || oldId === newId) continue;
            if (map[oldId]) {
              nextMap[newId] = map[oldId];
              delete nextMap[oldId];
              // também envia para o backend, preservando ordem
              if (isApiEnabled() && /^[a-fA-F0-9]{24}$/.test(newId)) {
                const ordered = (nextMap[newId] || []).map((f, idx) => ({
                  ...f,
                  order: idx,
                }));
                try {
                  await saveStageFieldsApi(newId, ordered);
                } catch (e) {}
              }
            }
          }
          localStorage.setItem(storageKey, JSON.stringify(nextMap));
        } catch (_) {}
      }
      if (okSettings) toastSuccess("Esteira salva");
      else toastInfo("Etapas salvas; falha ao atualizar dados do processo");
      showPipelineModal.value = false;
    } catch (e) {
      toastError("Falha ao salvar etapas. Verifique a conexão.");
      // mantém o modal aberto para o usuário tentar novamente
      return;
    }
  } else {
    showPipelineModal.value = false;
  }
}

// Sync tab with query (?tab=users|pipeline|status|notifications)
watch(
  () => route.query.tab,
  (t) => {
    const tab = String(t || "users");
    if (["users", "pipeline", "status", "notifications"].includes(tab))
      activeTab.value = tab;
  },
  { immediate: true }
);

// Load/save pipeline config locally
onMounted(async () => {
  loadCurrentUser();
  // Load status list independent of process
  await loadStatusList();

  // Load users list
  await loadUsersList();

  if (!currentProcessKey.value) return;
  const loaded = await fetchStagesApi(currentProcessKey.value);
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : [];
  await prefetchStageFields();
});

watch(currentProcessKey, async (k) => {
  if (!k) {
    pipelineStages.value = [];
    return;
  }
  const loaded = await fetchStagesApi(k);
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : [];
  setLastKey(k);
  const p = processes.value.find((x) => x.key === k);
  processName.value = p?.name || k;
  await prefetchStageFields();
});

// Persistiremos as etapas somente ao salvar no modal

function toggleActive(p) {
  setProcessActive(p.key, !(p.active !== false));
  processes.value = listProcesses();
}

async function editProcess(p) {
  currentProcessKey.value = p.key;
  const loaded = await fetchStagesApi(p.key);
  pipelineStages.value = Array.isArray(loaded)
    ? loaded.map((s) => ({
        id: s.id,
        title: s.title,
        slaDays: s.slaDays,
        color: s.color,
      }))
    : [];
  if (!pipelineStages.value.length && p.type && DEFAULT_PIPELINES[p.type]) {
    pipelineStages.value = [...DEFAULT_PIPELINES[p.type]];
  }
  setLastKey(p.key);
  processName.value = p.name || p.key;
  selectedProcessUsers.value = sanitizeUserSelection(p.allowedUserIds);
  await prefetchStageFields();
  openPipelineModal();
}

// Busca os fields do backend por etapa (para mostrar contagem e já aquecer o builder)
async function prefetchStageFields() {
  if (!isApiEnabled()) return;
  try {
    const storageKey = `pipeline_stage_forms__${currentProcessKey.value}`;
    const raw = localStorage.getItem(storageKey);
    const map = raw ? JSON.parse(raw) || {} : {};
    const nextMap = { ...map };
    for (const st of pipelineStages.value || []) {
      const sid = st?.id;
      if (!sid || !/^[a-fA-F0-9]{24}$/.test(sid)) continue;
      try {
        const arr = await fetchStageFieldsApi(sid);
        if (Array.isArray(arr)) {
          nextMap[sid] = arr.map((r) => ({
            id: r.id || String(Date.now()),
            label: r.label,
            type: r.type,
            required: !!r.required,
            placeholder: r.placeholder || "",
            options: Array.isArray(r.options) ? r.options : [],
          }));
        }
      } catch (_) {}
    }
    localStorage.setItem(storageKey, JSON.stringify(nextMap));
  } catch (_) {}
}

// Delete modal state/handlers
const showDeleteModal = ref(false);
const deleteTarget = ref(null);
const deleteConfirm = ref("");
const canConfirmDelete = computed(() => {
  const target = (deleteTarget.value?.name || "").trim().toLowerCase();
  const typed = deleteConfirm.value.trim().toLowerCase();
  return !!target && typed === target;
});
const showResetModal = ref(false);
function openDeleteModal(p) {
  deleteTarget.value = p;
  deleteConfirm.value = "";
  showDeleteModal.value = true;
}
function cancelDelete() {
  showDeleteModal.value = false;
  deleteTarget.value = null;
  deleteConfirm.value = "";
}
async function confirmDelete() {
  const p = deleteTarget.value;
  if (!p || !canConfirmDelete.value) return;

  // Remoção otimista no DOM
  const key = p.key;
  processes.value = processes.value.filter((x) => x.key !== key);
  if (currentProcessKey.value === key) {
    currentProcessKey.value = processes.value[0]?.key || "";
    pipelineStages.value = [];
    if (currentProcessKey.value) setLastKey(currentProcessKey.value);
  }

  // Efetiva no backend e re-sincroniza silenciosamente
  const ok = await removeProcess(key);
  if (ok) toastSuccess("Processo excluído");
  else {
    // rollback
    processes.value = [...processes.value, p];
    toastError("Falha ao excluir. Tente novamente.");
  }
  // Não recarrega toda a lista para evitar GET extra; o evento já atualizará quem escuta

  cancelDelete();
}

function onDeleteConfirmKeydown(e) {
  // Block paste shortcuts (Cmd/Ctrl+V) and select-all paste combos
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "v") {
    e.preventDefault();
    return;
  }
}

function openResetModal() {
  showResetModal.value = true;
}
function cancelReset() {
  showResetModal.value = false;
}
function confirmReset() {
  // Remove todos os processos e dados locais relacionados
  try {
    const keys = processes.value.map((p) => p.key);
    keys.forEach((k) => removeProcess(k));
    // também limpa o registro explicitamente
    localStorage.removeItem("pipeline_processes");
  } catch (_) {}
  processes.value = [];
  currentProcessKey.value = "";
  pipelineStages.value = [];
  setLastKey("");
  cancelReset();
}

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

// Enhanced function to get dynamic status style with modern glassmorphism effects
function getStatusStyle(status) {
  if (!status?.color) {
    // Fallback style for status without color
    return `
      background: linear-gradient(135deg, rgba(156, 163, 175, 0.15), rgba(156, 163, 175, 0.05));
      color: #9ca3af;
      border: 1px solid rgba(156, 163, 175, 0.3);
      box-shadow: 0 2px 8px rgba(156, 163, 175, 0.15);
      backdrop-filter: blur(4px);
    `
      .replace(/\s+/g, " ")
      .trim();
  }

  const baseColor = status.color;

  // Helper function to convert hex to rgba
  const hexToRgba = (hex, alpha = 0.2) => {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };

  return `
    background: linear-gradient(135deg, ${hexToRgba(baseColor, 0.15)}, ${hexToRgba(baseColor, 0.05)});
    color: ${baseColor};
    border: 1px solid ${hexToRgba(baseColor, 0.3)};
    box-shadow: 0 2px 8px ${hexToRgba(baseColor, 0.15)};
    backdrop-filter: blur(4px);
  `
    .replace(/\s+/g, " ")
    .trim();
}

// User management functions
async function loadUsersList() {
  try {
    console.log("Loading users list...");
    usersList.value = await fetchUsersApi();
    console.log("Users list loaded:", usersList.value.length, "items");
  } catch (error) {
    console.error("Failed to load users list:", error);
    toastError("Erro ao carregar usuários");
  }
  syncCurrentProcessUsers();
}

const { getCurrentLocation, formatLocation } = useGeolocation();
const { migrateTemporaryFileToUser } = useTemporaryFileUpload();

async function openNewUserModal() {
  if (!canManageUsers.value) {
    toastError("Você não tem permissão para criar usuários");
    return;
  }
  // Capturar localização ao criar novo usuário
  let location = null;
  try {
    const locationData = await getCurrentLocation();
    location = formatLocation(locationData);
  } catch (error) {
    console.warn("Não foi possível obter localização:", error.message);
  }

  userForm.value = {
    id: null,
    name: "",
    email: "",
    password: "",
    roles: [],
    cpf: "",
    cnpj: "",
    rg: "",
    banco: "",
    agencia: "",
    conta: "",
    tipoConta: "",
    pixTipo: "",
    pixChave: "",
    fotoPerfilUrl: "",
    comprovanteEnderecoUrl: "",
    localizacaoCadastro: location || "",
    // Novos campos para documentos
    tipoIdentificacao: "",
    documentoIdentificacaoUrl: "",
    documentoIdentificacaoObjectKey: "",
  };
  editingUser.value = false;

  // Reset email validation state
  emailCheckLoading.value = false;
  emailAvailable.value = null;
  emailError.value = "";
  allowEmailEdit.value = false;

  // Gerar novo sessionId para uploads temporários
  tempUploadSessionId.value = `session_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`;

  // Limpar array de arquivos temporários
  temporaryUploadedFiles.value = [];

  showUserModal.value = true;
}

function editUser(user) {
  if (!canManageUsers.value) {
    toastError("Você não tem permissão para editar usuários");
    return;
  }
  userForm.value = {
    id: user.id,
    name: user.name,
    email: user.email,
    password: "",
    roles: [...user.roles],
    cpf: user.cpf || "",
    cnpj: user.cnpj || "",
    rg: user.rg || "",
    banco: user.banco || "",
    agencia: user.agencia || "",
    conta: user.conta || "",
    tipoConta: user.tipoConta || "",
    pixTipo: user.pixTipo || "",
    pixChave: user.pixChave || "",
    fotoPerfilUrl: user.fotoPerfilUrl || "",
    comprovanteEnderecoUrl: user.comprovanteEnderecoUrl || "",
    localizacaoCadastro: user.localizacaoCadastro || "",
    // Novos campos para documentos
    tipoIdentificacao: user.tipoIdentificacao || "",
    documentoIdentificacaoUrl: user.documentoIdentificacaoUrl || "",
  };
  editingUser.value = true;
  allowEmailEdit.value = false;
  showUserModal.value = true;
}

async function saveUser() {
  if (!canManageUsers.value) {
    toastError("Você não tem permissão para salvar usuários");
    return;
  }
  try {
    const {
      id,
      name,
      email,
      password,
      roles,
      cpf,
      cnpj,
      rg,
      banco,
      agencia,
      conta,
      tipoConta,
      pixTipo,
      pixChave,
    } = userForm.value;

    if (!name.trim()) {
      toastError("Nome é obrigatório");
      return;
    }

    if (!email.trim()) {
      toastError("Email é obrigatório");
      return;
    }

    if (roles.length === 0) {
      toastError("Selecione pelo menos uma role");
      return;
    }

    // Se editando usuário existente OU se já foi criado incrementalmente
    if (editingUser.value || id) {
      // Update existing user
      const updated = await updateUserApi(id, {
        name: name.trim(),
        email: email.trim(),
        roles,
        cpf: cpf || undefined,
        cnpj: cnpj || undefined,
        rg: rg || undefined,
        banco: banco || undefined,
        agencia: agencia || undefined,
        conta: conta || undefined,
        tipoConta: tipoConta || undefined,
        pixTipo: pixTipo || undefined,
        pixChave: pixChave || undefined,
      });
      if (updated) {
        toastSuccess("Usuário atualizado com sucesso");
        await loadUsersList();
        showUserModal.value = false;
      }
    } else {
      // Fluxo antigo: criar usuário se não foi criado incrementalmente
      if (!password.trim()) {
        toastError("Senha é obrigatória");
        return;
      }

      if (password.trim().length < 6) {
        toastError("Senha deve ter no mínimo 6 caracteres");
        return;
      }

      const created = await createUserApi({
        name: name.trim(),
        email: email.trim(),
        password: password.trim(),
        roles,
        cpf: cpf || undefined,
        cnpj: cnpj || undefined,
        rg: rg || undefined,
        banco: banco || undefined,
        agencia: agencia || undefined,
        conta: conta || undefined,
        tipoConta: tipoConta || undefined,
        pixTipo: pixTipo || undefined,
        pixChave: pixChave || undefined,
        fotoPerfilUrl: userForm.value.fotoPerfilUrl || undefined,
        comprovanteEnderecoUrl:
          userForm.value.comprovanteEnderecoUrl || undefined,
        localizacaoCadastro: userForm.value.localizacaoCadastro || undefined,
      });
      if (created) {
        // Migrar arquivos temporários para o usuário criado (fluxo antigo)
        if (temporaryUploadedFiles.value.length > 0) {
          console.log(
            `Migrando ${temporaryUploadedFiles.value.length} arquivos temporários para usuário ${created.id}`
          );

          for (const tempFile of temporaryUploadedFiles.value) {
            try {
              const migratedFile = await migrateTemporaryFileToUser(
                created.id,
                tempFile.tempObjectKey,
                tempFile.filename,
                tempFile.type
              );
              console.log(`Arquivo migrado com sucesso:`, migratedFile);
            } catch (error) {
              console.error(
                `Erro ao migrar arquivo ${tempFile.filename}:`,
                error
              );
              // Não falhar o processo inteiro por um arquivo
            }
          }
        }

        toastSuccess("Usuário criado com sucesso");
        await loadUsersList();
        showUserModal.value = false;
      }
    }
  } catch (error) {
    console.error("Error saving user:", error);
    toastError("Erro ao salvar usuário");
  }
}

async function deleteUser(userId) {
  if (!canManageUsers.value) {
    toastError("Você não tem permissão para excluir usuários");
    return;
  }
  if (!confirm("Tem certeza que deseja excluir este usuário?")) {
    return;
  }

  try {
    const success = await deleteUserApi(userId);
    if (success) {
      toastSuccess("Usuário excluído com sucesso");
      await loadUsersList();
    } else {
      toastError("Erro ao excluir usuário");
    }
  } catch (error) {
    console.error("Error deleting user:", error);
    toastError("Erro ao excluir usuário");
  }
}

function closeUserModal() {
  showUserModal.value = false;
  userForm.value = {
    id: null,
    name: "",
    email: "",
    password: "",
    roles: [],
    cpf: "",
    cnpj: "",
    rg: "",
    banco: "",
    agencia: "",
    conta: "",
    tipoConta: "",
    pixTipo: "",
    pixChave: "",
    fotoPerfilUrl: "",
    comprovanteEnderecoUrl: "",
    localizacaoCadastro: "",
    // Novos campos para documentos
    tipoIdentificacao: "",
    documentoIdentificacaoUrl: "",
    documentoIdentificacaoObjectKey: "",
  };
  editingUser.value = false;
}

// Funções de upload de arquivos
function onProfilePhotoSelected(file) {
  console.log("Foto de perfil selecionada:", file.name);
}

function onProfilePhotoUploaded(uploadData) {
  console.log("Foto de perfil enviada:", uploadData);
  userForm.value.fotoPerfilUrl = uploadData.publicUrl;
  userForm.value.fotoPerfilObjectKey = uploadData.objectKey;

  // Se é upload temporário, armazenar para migração posterior
  if (uploadData.isTemporary) {
    temporaryUploadedFiles.value.push({
      type: "PROFILE_PHOTO",
      tempObjectKey: uploadData.objectKey,
      filename: uploadData.filename,
      sessionId: uploadData.sessionId,
    });
  }
}

function onAddressProofSelected(file) {
  console.log("Comprovante de endereço selecionado:", file.name);
}

function onAddressProofUploaded(uploadData) {
  console.log("Comprovante de endereço enviado:", uploadData);
  userForm.value.comprovanteEnderecoUrl = uploadData.publicUrl;
  userForm.value.comprovanteEnderecoObjectKey = uploadData.objectKey;

  // Se é upload temporário, armazenar para migração posterior
  if (uploadData.isTemporary) {
    temporaryUploadedFiles.value.push({
      type: "ADDRESS_PROOF",
      tempObjectKey: uploadData.objectKey,
      filename: uploadData.filename,
      sessionId: uploadData.sessionId,
    });
  }
}

function onIdentityDocumentSelected(file) {
  console.log("Documento de identificação selecionado:", file.name);
}

function onIdentityDocumentUploaded(uploadData) {
  console.log("Documento de identificação enviado:", uploadData);
  userForm.value.documentoIdentificacaoUrl = uploadData.publicUrl;
  userForm.value.documentoIdentificacaoObjectKey = uploadData.objectKey;

  // Se é upload temporário, armazenar para migração posterior
  if (uploadData.isTemporary) {
    temporaryUploadedFiles.value.push({
      type: userForm.value.tipoIdentificacao || "IDENTITY",
      tempObjectKey: uploadData.objectKey,
      filename: uploadData.filename,
      sessionId: uploadData.sessionId,
    });
  }
}

// Funções de reconhecimento facial
function onFaceRegistrationSuccess(result) {
  toastSuccess("Face registrada com sucesso!");
  console.log("Face registration success:", result);
}

function onFaceRegistrationError(error) {
  toastError("Erro ao registrar face: " + error.message);
  console.error("Face registration error:", error);
}

function toggleRole(roleValue) {
  if (!canToggleRole(roleValue)) {
    return;
  }
  const roles = userForm.value.roles;
  const index = roles.indexOf(roleValue);
  if (index > -1) {
    roles.splice(index, 1);
  } else {
    roles.push(roleValue);
  }
}

function formatCPF(cpf) {
  if (!cpf) return "";
  return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}

function formatCNPJ(cnpj) {
  if (!cnpj) return "";
  return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
}

function formatCPFInput(event) {
  let value = event.target.value.replace(/\D/g, "");
  if (value.length <= 11) {
    value = value.replace(/(\d{3})(\d)/, "$1.$2");
    value = value.replace(/(\d{3})(\d)/, "$1.$2");
    value = value.replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    userForm.value.cpf = value;
  }
}

function formatCNPJInput(event) {
  let value = event.target.value.replace(/\D/g, "");
  if (value.length <= 14) {
    value = value.replace(/(\d{2})(\d)/, "$1.$2");
    value = value.replace(/(\d{3})(\d)/, "$1.$2");
    value = value.replace(/(\d{3})(\d)/, "$1/$2");
    value = value.replace(/(\d{4})(\d{1,2})$/, "$1-$2");
    userForm.value.cnpj = value;
  }
}

function getPixPlaceholder() {
  const pixTipo = userForm.value.pixTipo;
  switch (pixTipo) {
    case "CPF":
      return "000.000.000-00";
    case "CNPJ":
      return "00.000.000/0000-00";
    case "EMAIL":
      return "exemplo@email.com";
    case "TELEFONE":
      return "+5511999999999";
    case "CHAVE_ALEATORIA":
      return "chave-aleatoria-uuid";
    default:
      return "Digite a chave PIX";
  }
}

// Funções para validação de email e criação incremental
function onEmailInput() {
  emailAvailable.value = null;
  emailError.value = "";

  // Clear previous timeout
  if (emailCheckTimeout.value) {
    clearTimeout(emailCheckTimeout.value);
  }

  // Only check if we have a valid email format and it's not empty
  const email = userForm.value.email.trim();
  if (
    email &&
    email.includes("@") &&
    !editingUser.value &&
    !currentUserId.value
  ) {
    // Debounce email check by 500ms
    emailCheckTimeout.value = setTimeout(() => {
      checkEmailAvailability();
    }, 500);
  }
}

async function checkEmailAvailability() {
  const email = userForm.value.email.trim();
  const name = userForm.value.name.trim();

  // Don't check if editing user or user already created
  if (editingUser.value || currentUserId.value) {
    return;
  }

  // Don't check if email is empty or invalid
  if (!email || !email.includes("@")) {
    emailAvailable.value = null;
    emailError.value = "";
    return;
  }

  try {
    emailCheckLoading.value = true;
    emailError.value = "";

    const { apiFetch } = await import("~/utils/api/index");
    const response = await apiFetch(
      `/api/v1/users/check-email?email=${encodeURIComponent(email)}`
    );

    if (response.available) {
      emailAvailable.value = true;

      // Se temos nome e email válidos, criar usuário básico automaticamente
      if (name && name.length >= 2) {
        await createBasicUser();
      }
    } else {
      emailAvailable.value = false;
    }
  } catch (error) {
    console.error("Erro ao verificar email:", error);
    emailError.value = "Erro ao verificar email";
    emailAvailable.value = null;
  } finally {
    emailCheckLoading.value = false;
  }
}

async function createBasicUser() {
  const name = userForm.value.name.trim();
  const email = userForm.value.email.trim();

  if (!name || !email || emailAvailable.value !== true) {
    return;
  }

  try {
    const { apiFetch } = await import("~/utils/api/index");

    const response = await apiFetch("/api/v1/users/create-basic", {
      method: "POST",
      body: {
        name,
        email,
        password: "temp123", // Senha temporária
      },
    });

    if (response.id) {
      // Usuário criado com sucesso - agora podemos usar uploads normais
      userForm.value.id = response.id;

      toastInfo(`Usuário ${name} salvo. Continue preenchendo os dados.`);

      console.log("Usuário básico criado:", response);
    }
  } catch (error) {
    console.error("Erro ao criar usuário básico:", error);
    toastError("Erro ao salvar usuário. Tente novamente.");
  }
}
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
.group:hover .group-hover\:scale-105 {
  transform: scale(1.05);
}

/* Card glow effects */
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

/* Responsive animations - reduce motion for accessibility */
@media (prefers-reduced-motion: reduce) {
  .animate-shimmer,
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
  .border-white\/5 {
    border-color: rgba(255, 255, 255, 0.2);
  }

  .border-white\/10 {
    border-color: rgba(255, 255, 255, 0.3);
  }
}

body {
  font-family:
    Inter,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    Roboto,
    sans-serif;
}
</style>
