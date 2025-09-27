<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900"
  >
    <!-- Header (sem busca/IA; serão acionados pela sidebar) -->
    <header
      class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 px-4 py-4"
    >
      <div
        class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"
      ></div>
      <div
        class="relative w-full flex items-center justify-between gap-3 flex-wrap"
      >
        <!--    <!-- Left: Brand + Financial Stats -->
        <div class="flex items-center gap-4">
          <FinancialStats
            :pipeline-key="pipelineKey"
            :is-financial-process="isFinancialProcess"
            :proposals="proposals"
            ref="financialStatsRef"
          />
        </div>

        <!-- Right: controls -->
        <div class="flex items-center gap-2">
          <!-- View Mode Toggle -->
          <div class="bg-slate-800/80 p-0.5 rounded-md flex items-center">
            <button
              @click="viewMode = 'kanban'"
              :class="
                viewMode === 'kanban'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-300 hover:bg-slate-700/80'
              "
              class="px-2 py-0.5 rounded-md text-[11px] font-medium transition-colors flex items-center gap-1"
            >
              <i class="fa-solid fa-grip-vertical"></i> Kanban
            </button>
            <button
              @click="viewMode = 'list'"
              :class="
                viewMode === 'list'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-300 hover:bg-slate-700/80'
              "
              class="px-2 py-0.5 rounded-md text-[11px] font-medium transition-colors flex items-center gap-1"
            >
              <i class="fa-solid fa-list"></i> Lista
            </button>
          </div>

          <!-- Sort Options -->
          <div class="bg-slate-800/80 rounded-md flex items-center">
            <select
              v-model="sortOption"
              class="bg-transparent border-none text-slate-300 text-[11px] px-2 py-1 rounded-md focus:outline-none focus:ring-0 cursor-pointer"
            >
              <option value="default" class="bg-slate-800 text-slate-300">
                Padrão
              </option>
              <option value="name-asc" class="bg-slate-800 text-slate-300">
                Nome (A-Z)
              </option>
              <option value="name-desc" class="bg-slate-800 text-slate-300">
                Nome (Z-A)
              </option>
              <option value="amount-asc" class="bg-slate-800 text-slate-300">
                Valor (Menor)
              </option>
              <option value="amount-desc" class="bg-slate-800 text-slate-300">
                Valor (Maior)
              </option>
              <option value="date-newest" class="bg-slate-800 text-slate-300">
                Mais Recente
              </option>
              <option value="date-oldest" class="bg-slate-800 text-slate-300">
                Mais Antigo
              </option>
            </select>
          </div>

          <!-- ConsorIA Chat Button -->
          <button
            @click="openAiChat"
            class="consoria-btn bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-400 text-white font-medium px-2.5 py-1 rounded-md transition-all duration-300 flex items-center gap-1.5 text-xs relative overflow-hidden group"
          >
            <!-- Shimmer effect -->
            <div
              class="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/20 to-transparent group-hover:translate-x-full transition-transform duration-700"
            ></div>
            <!-- Pulsing dot -->
            <div
              class="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"
            ></div>
            <!-- Robot icon with animation -->
            <i
              class="fa-solid fa-robot text-[11px] group-hover:scale-110 transition-transform duration-300"
            ></i>
            <span class="hidden sm:inline relative z-10">ConsorIA</span>
          </button>

          <!-- New proposal -->
          <button
            @click="openGlobalNewRecordModal"
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white font-medium px-2.5 py-1 rounded-md transition-colors flex items-center gap-1 text-xs"
          >
            <i class="fa-solid fa-plus text-[10px]"></i>
            <span class="hidden sm:inline">Novo Registro</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="p-3 sm:p-6">
      <!-- Kanban View -->
      <div v-if="viewMode === 'kanban'" class="overflow-x-auto kanban-scroll">
        <transition-group
          name="kanban-col"
          tag="div"
          class="grid grid-flow-col auto-cols-[14rem] sm:auto-cols-[15rem] md:auto-cols-[16rem] gap-3 min-h-[10rem] pb-2"
        >
          <div
            v-for="(stage, i) in stages"
            :key="stage.id"
            class="w-full bg-slate-800 rounded-md p-1.5 transition-colors"
            :class="[
              dragOverStage === stage.id
                ? 'ring-2 ring-blue-400 ring-offset-0'
                : '',
              isDraggingStage && columnDragOverIndex === i
                ? 'ring-2 ring-indigo-400'
                : '',
              isDraggingStage && draggingStageId === stage.id
                ? 'opacity-90'
                : '',
            ]"
            @dragover.prevent="onColumnDragOver($event, i)"
            @dragenter.prevent="onDragEnter(stage.id)"
            @dragleave="onDragLeave(stage.id)"
            @drop.capture="onColumnDrop($event, i)"
            @drop="onDrop(stage.id)"
          >
            <div
              :class="['h-1 rounded-md mb-2', stageStripClass(stage.id)]"
            ></div>
            <!-- Column header -->
            <div class="flex items-center justify-between mb-1.5">
              <div class="flex items-center gap-2">
                <button
                  class="text-slate-300/80 hover:text-white px-1 py-0.5 rounded"
                  title="Reordenar coluna"
                  :draggable="!isTouch"
                  @dragstart="onStageDragStart($event, stage, i)"
                  @dragend="onStageDragEnd"
                >
                  <i class="fa-solid fa-grip-vertical"></i>
                </button>
                <h3 class="font-semibold text-xs">{{ stage.title }}</h3>
                <span
                  v-if="stage.auto"
                  class="text-[10px] px-1.5 py-0.5 rounded-full bg-slate-700 text-slate-300 border border-slate-600/60"
                  >auto</span
                >
                <span class="text-[11px] text-slate-400"
                  >SLA {{ stage.slaDays }}d</span
                >
                <!-- Stage Sum for Financial Processes -->
                <span
                  v-if="isFinancialProcess && formatStageSum(stage.id)"
                  class="text-[10px] font-bold text-emerald-400"
                >
                  {{ formatStageSum(stage.id) }}
                </span>
              </div>
              <span
                :class="[
                  'text-[10px] font-semibold px-2 py-0.5 rounded-full',
                  stageBadgeClass(stage.id),
                ]"
                >{{ filteredByStage(stage.id).length }}</span
              >
            </div>
            <!-- Cards -->
            <div class="space-y-2.5 min-h-10">
              <div
                v-for="p in filteredByStage(stage.id)"
                :key="p.id"
                class="bg-slate-700 rounded-lg p-3 shadow-sm cursor-move border-l-4 hover:bg-slate-600/50 transition-colors"
                :class="[
                  draggedId === p.id ? 'opacity-60' : '',
                  stageBorderClass(p.stageId),
                ]"
                :draggable="!isTouch"
                @dragstart="onDragStart($event, p)"
                @click="openCardForm(p)"
                @dragend="onDragEnd"
              >
                <!-- Nome do cliente -->
                <div class="mb-3">
                  <div class="font-medium leading-tight text-sm text-white">
                    {{ p.name }}
                  </div>
                  <div v-if="isFinancialProcess" class="text-xs text-slate-300 mt-1">
                    R$ {{ p.amount.toLocaleString("pt-BR") }}
                  </div>
                </div>

                <!-- Bottom row: SLA esquerda, Status direita -->
                <div class="flex items-center justify-between">
                  <!-- SLA tags à esquerda -->
                  <div class="flex items-center gap-1">
                    <span
                      :class="[
                        'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[8px] font-medium',
                        slaBadgeClass(p),
                      ]"
                    >
                      <i class="fa-regular fa-calendar text-[7px]"></i>
                      {{ slaLabel(p) }}
                    </span>
                    <span
                      v-if="getSlaInfo(p).status === 'overdue'"
                      class="inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[8px] font-medium bg-red-900/60 text-red-300"
                    >
                      <i class="fa-solid fa-flag text-[7px]"></i>
                      Atraso
                    </span>
                  </div>

                  <!-- Status tag à direita (Kanban) -->
                  <span
                    :class="statusPillClass(p.status)"
                    :style="getStatusStyle(p.status)"
                    class="inline-flex items-center gap-1 text-[9px] font-medium px-2 py-0.5 rounded-md transition-all duration-300 hover:scale-105 whitespace-nowrap cursor-pointer hover:brightness-110"
                    @click="openStatusDropdown(p, $event)"
                    title="Clique para alterar o status"
                  >
                    <div class="w-1 h-1 rounded-full bg-current opacity-70 flex-shrink-0"></div>
                    <span class="truncate">{{ p.status }}</span>
                  </span>
                </div>
              </div>
              <div
                v-if="filteredByStage(stage.id).length === 0"
                class="text-sm text-slate-400"
              >
                Sem propostas
              </div>
            </div>
          </div>
        </transition-group>
      </div>

      <!-- List View -->
      <div v-else class="bg-slate-800 rounded-lg">
        <!-- Mobile: cards -->
        <div class="sm:hidden">
          <ul class="divide-y divide-slate-700">
            <li v-for="p in sortedFilteredProposals" :key="p.id" class="p-4">
              <div class="space-y-3">
                <!-- Nome e valor -->
                <div>
                  <div class="font-medium text-sm text-white">{{ p.name }}</div>
                  <div v-if="isFinancialProcess" class="mt-1 text-xs text-slate-300">
                    R$ {{ p.amount.toLocaleString("pt-BR") }}
                  </div>
                </div>

                <!-- Bottom row: Etapa esquerda, Status direita -->
                <div class="flex items-center justify-between">
                  <!-- Info da etapa à esquerda -->
                  <div class="text-[8px] text-slate-500">
                    <i class="fa-solid fa-layer-group text-[7px]"></i>
                    {{ stageTitle(p.stageId) }}
                  </div>

                  <!-- Status tag à direita (Mobile) -->
                  <span
                    :class="statusPillClass(p.status)"
                    :style="getStatusStyle(p.status)"
                    class="inline-flex items-center gap-1 text-[9px] font-medium px-2 py-0.5 rounded-md transition-all duration-300 hover:scale-105 whitespace-nowrap cursor-pointer hover:brightness-110"
                    @click="openStatusDropdown(p, $event)"
                    title="Clique para alterar o status"
                  >
                    <div class="w-1 h-1 rounded-full bg-current opacity-70 flex-shrink-0"></div>
                    <span class="truncate">{{ p.status }}</span>
                  </span>
                </div>
              </div>
            </li>
          </ul>
          <div
            v-if="filteredProposals.length === 0"
            class="p-3 text-slate-400 text-sm"
          >
            Sem registros
          </div>
        </div>

        <!-- Desktop: table -->
        <div class="hidden sm:block overflow-x-auto">
          <table class="w-full text-sm text-left">
            <thead class="bg-slate-700/60 text-xs text-slate-300 uppercase">
              <tr>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Cliente</th>
                <th v-if="isFinancialProcess" class="px-3 sm:px-6 py-2 sm:py-3">Valor</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Etapa</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3">Status</th>
                <th class="px-3 sm:px-6 py-2 sm:py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="p in sortedFilteredProposals"
                :key="p.id"
                class="border-b border-slate-700 hover:bg-slate-700/50"
              >
                <td class="px-3 sm:px-6 py-2 sm:py-3">{{ p.name }}</td>
                <td
                  v-if="isFinancialProcess"
                  class="px-3 sm:px-6 py-2 sm:py-3"
                >
                  R$ {{ p.amount.toLocaleString("pt-BR") }}
                </td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  {{ stageTitle(p.stageId) }}
                </td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  <span
                    :class="statusPillClass(p.status)"
                    :style="getStatusStyle(p.status)"
                    class="inline-flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1.5 rounded-lg transition-all duration-300 hover:scale-105 whitespace-nowrap cursor-pointer hover:brightness-110"
                    @click="openStatusDropdown(p, $event)"
                    title="Clique para alterar o status"
                  >
                    <div class="w-2 h-2 rounded-full bg-current opacity-70 flex-shrink-0"></div>
                    <span class="truncate">{{ p.status }}</span>
                  </span>
                </td>
                <td class="px-3 sm:px-6 py-2 sm:py-3">
                  <div
                    class="flex items-center gap-2 justify-end text-slate-300"
                  >
                    <button
                      class="hover:text-white"
                      title="Ver/Editar"
                      @click="openCardForm(p)"
                    >
                      <i class="fa-regular fa-pen-to-square"></i>
                    </button>
                    <button
                      class="hover:text-white"
                      title="Mover"
                      @click="openMoveModal(p)"
                    >
                      <i class="fa-solid fa-arrow-right-arrow-left"></i>
                    </button>
                    <button
                      class="text-red-400 hover:text-red-300"
                      title="Excluir"
                      @click="openDeleteProposal(p)"
                    >
                      <i class="fa-regular fa-trash-can"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <!-- New Proposal Modal using BaseModal -->
    <BaseModal
      v-model="showNewModal"
      title="Novo Registro"
      size="xxl"
      :body-class="'new-proposal-modal'"
    >
      <div class="space-y-2">
        <!-- Info da primeira etapa (sempre fixa) -->
        <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/40">
          <h4 class="text-sm font-semibold text-slate-200 mb-3">
            Nova Proposta
          </h4>
          <div class="text-xs text-slate-400">
            <span class="inline-flex items-center gap-2">
              <span
                class="w-2 h-4 rounded-full"
                :class="getStageColor(stages[0]?.id)"
              ></span>
              Etapa: {{ stages[0]?.title || "Primeira Etapa" }}
            </span>
          </div>
        </div>
        <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/40">
          <h4 class="text-sm font-semibold text-slate-200 mb-3">
            Campos do Processo
          </h4>

          <!-- Grid para inputs normais (text, number, date, select) -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            <template v-for="f in stageDynamicFields" :key="f.id">
              <div
                v-if="
                  f.type !== 'file' &&
                  f.type !== 'endereco' &&
                  f.type !== 'pessoa_fisica'
                "
              >
                <BaseInput
                  v-if="f.type === 'text' || f.type === 'number'"
                  v-model="extraFieldValues[f.id]"
                  :type="f.type === 'number' ? 'text' : 'text'"
                  :numeric="f.type === 'number'"
                  :label="f.label"
                  size="xs"
                  :placeholder="f.placeholder || ''"
                />
                <CpfInput
                  v-else-if="f.type === 'cpf'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :placeholder="f.placeholder || '000.000.000-00'"
                  :required="f.required"
                  :show-validation-info="true"
                />
                <CnpjInput
                  v-else-if="f.type === 'cnpj'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :placeholder="f.placeholder || '00.000.000/0000-00'"
                  :required="f.required"
                  :show-validation-info="true"
                />
                <RgInput
                  v-else-if="f.type === 'rg'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :placeholder="f.placeholder || '00.000.000-0'"
                  :required="f.required"
                  :show-validation-info="true"
                />
                <CepInput
                  v-else-if="f.type === 'cep'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :placeholder="f.placeholder || '00000-000'"
                  :required="f.required"
                  :show-validation-info="true"
                  :auto-fetch-address="true"
                />
                <TelefoneInput
                  v-else-if="f.type === 'telefone'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :placeholder="f.placeholder || '(00) 00000-0000'"
                  :required="f.required"
                  :show-validation-info="true"
                />
                <BaseInput
                  v-else-if="f.type === 'date'"
                  v-model="extraFieldValues[f.id]"
                  type="date"
                  :label="f.label"
                  size="xs"
                />
                <BaseSelect
                  v-else-if="f.type === 'select'"
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  size="xs"
                  :options="
                    (f.options || []).map((o) => ({ label: o, value: o }))
                  "
                />
                <div
                  v-if="extraFieldErrors[f.id]"
                  class="text-[11px] text-red-400 mt-1"
                >
                  {{ extraFieldErrors[f.id] }}
                </div>
              </div>
            </template>
          </div>

          <!-- Inputs de upload ocupam largura completa -->
          <div class="space-y-3 mt-4 pt-4 border-t border-slate-700/40">
            <template v-for="f in stageDynamicFields" :key="f.id">
              <div v-if="f.type === 'file'" class="w-full">
                <FormField :label="f.label" :dense="true">
                  <FileDrop @files="(files) => onExtraFile(f.id, files)" />
                </FormField>
                <div
                  v-if="extraFieldErrors[f.id]"
                  class="text-[11px] text-red-400 mt-1"
                >
                  {{ extraFieldErrors[f.id] }}
                </div>
              </div>
            </template>
          </div>

          <!-- Endereço e Pessoa Física ocupam largura completa, fora do grid -->
          <div class="space-y-3 mt-4">
            <template v-for="f in stageDynamicFields" :key="f.id">
              <div v-if="f.type === 'endereco'" class="w-full">
                <EnderecoInput
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :required="f.required"
                />
                <div
                  v-if="extraFieldErrors[f.id]"
                  class="text-[11px] text-red-400 mt-1"
                >
                  {{ extraFieldErrors[f.id] }}
                </div>
              </div>
              <div v-else-if="f.type === 'pessoa_fisica'" class="w-full">
                <PessoaFisicaInput
                  v-model="extraFieldValues[f.id]"
                  :label="f.label"
                  :required="f.required"
                />
                <div
                  v-if="extraFieldErrors[f.id]"
                  class="text-[11px] text-red-400 mt-1"
                >
                  {{ extraFieldErrors[f.id] }}
                </div>
              </div>
            </template>
          </div>
        </div>
        <!-- Linha 1: Nome, CPF, CEP (abaixo do Nome), Administradora (abaixo do CPF) -->
        <div v-if="false" class="grid grid-cols-12 gap-2">
          <div class="col-span-12 md:col-span-6">
            <BaseInput
              v-model="newProposal.name"
              label="Nome"
              size="xs"
              placeholder="Digite aqui..."
              :error-message="formErrors.name"
            />
          </div>
          <div class="col-span-12 md:col-span-6">
            <BaseInput
              v-model="newProposal.cpf"
              label="CPF"
              size="xs"
              placeholder="000.000.000-00"
              :error-message="formErrors.cpf"
            />
          </div>
          <div class="col-span-12 md:col-span-6">
            <BaseInputGroup
              v-model="newProposal.cep"
              label="CEP"
              size="xs"
              placeholder="00000-000"
              append-text="Buscar"
              :loading="cepLoading"
              :error-message="cepError"
              @append-click="fetchCep"
            />
          </div>
          <div class="col-span-12 md:col-span-6">
            <BaseSelect
              v-model="newProposal.administrator"
              label="Administradora"
              size="xs"
              :options="adminOptions.map((opt) => ({ label: opt, value: opt }))"
            />
          </div>
        </div>

        <!-- Linha 2: Cidade, Estado, Banco de Cotas, Fundo -->
        <div v-if="false" class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <BaseInput
            v-model="newProposal.city"
            label="Cidade"
            size="xs"
            placeholder="Cidade"
          />
          <BaseSelect
            v-model="newProposal.state"
            label="Estado"
            size="xs"
            :options="states.map((uf) => ({ label: uf, value: uf }))"
          />
          <BaseSelect
            v-model="newProposal.bank"
            label="Banco de Cotas"
            size="xs"
            :options="bankOptions.map((opt) => ({ label: opt, value: opt }))"
          />
          <BaseSelect
            v-model="newProposal.fund"
            label="Fundo"
            size="xs"
            :options="fundOptions.map((opt) => ({ label: opt, value: opt }))"
          />
        </div>

        <!-- Toggle de detalhes -->
        <div v-if="false" class="flex items-center justify-end">
          <button
            type="button"
            class="text-[11px] text-slate-300 hover:text-white inline-flex items-center gap-1"
            @click="showAdvanced = !showAdvanced"
          >
            <i
              :class="
                showAdvanced
                  ? 'fa-solid fa-chevron-up'
                  : 'fa-solid fa-chevron-down'
              "
            ></i>
            <span>{{
              showAdvanced ? "Ocultar detalhes" : "Mais detalhes"
            }}</span>
          </button>
        </div>

        <div v-if="false && showAdvanced" class="space-y-2">
          <!-- Campos da Etapa (dinâmicos) -->
          <div v-if="stageDynamicFields.length" class="space-y-2">
            <h4 class="text-xs font-semibold text-slate-200">
              Campos do processo
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
              <template v-for="f in stageDynamicFields" :key="f.id">
                <div>
                  <BaseInput
                    v-if="f.type === 'text' || f.type === 'number'"
                    v-model="extraFieldValues[f.id]"
                    :type="'text'"
                    :numeric="f.type === 'number'"
                    :label="f.label"
                    size="xs"
                    :placeholder="f.placeholder || ''"
                  />
                  <BaseInput
                    v-else-if="f.type === 'date'"
                    v-model="extraFieldValues[f.id]"
                    type="date"
                    :label="f.label"
                    size="xs"
                  />
                  <BaseSelect
                    v-else-if="f.type === 'select'"
                    v-model="extraFieldValues[f.id]"
                    :label="f.label"
                    size="xs"
                    :options="
                      (f.options || []).map((o) => ({ label: o, value: o }))
                    "
                  />
                  <div v-else-if="f.type === 'file'">
                    <FormField :label="f.label" :dense="true">
                      <FileDrop @files="(files) => onExtraFile(f.id, files)" />
                    </FormField>
                  </div>
                  <div
                    v-if="extraFieldErrors[f.id]"
                    class="text-[10px] text-red-400 mt-0.5"
                  >
                    {{ extraFieldErrors[f.id] }}
                  </div>
                </div>
              </template>
            </div>
          </div>
          <!-- Linha 3: Tipo de Produto -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <BaseSelect
              v-model="newProposal.productType"
              label="Tipo de Produto"
              size="xs"
              :options="productTypes.map((opt) => ({ label: opt, value: opt }))"
            />
          </div>

          <!-- Resumo -->
          <div>
            <label class="form-label">Resumo do Objetivo do Crédito</label>
            <textarea
              v-model="newProposal.summary"
              rows="2"
              class="form-textarea mt-1"
              placeholder="Digite o resumo aqui..."
            ></textarea>
          </div>

          <hr class="border-slate-700" />

          <!-- Dados da Operação -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <BaseInput
              v-model="newProposal.startDate"
              type="date"
              label="Data de Início"
              size="xs"
            />
            <BaseInput
              v-model="newProposal.amountDesired"
              type="text"
              mask="currency"
              locale="pt-BR"
              label="Valor Desejado (R$)"
              size="xs"
              placeholder="0,00"
            />
            <BaseInput
              v-model="newProposal.guaranteeValue"
              type="text"
              mask="currency"
              locale="pt-BR"
              label="Valor da Garantia (R$)"
              size="xs"
              placeholder="0,00"
            />
          </div>

          <!-- Documentos -->
          <div>
            <FormField label="Documentos (arraste e solte)" :dense="true">
              <FileDrop
                accept="image/*,application/pdf"
                multiple
                @files="(f) => (uploadedDocs.value = f)"
              />
            </FormField>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <BaseSelect
              v-model="newProposal.guaranteeType"
              label="Tipo de Garantia"
              size="xs"
              :options="guaranteeTypes.map((o) => ({ label: o, value: o }))"
            />
            <BaseSelect
              v-model="newProposal.objective"
              label="Objetivo"
              size="xs"
              :options="objectives.map((o) => ({ label: o, value: o }))"
            />
          </div>

          <hr class="border-slate-700" />

          <!-- Responsáveis -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <BaseSelect
              v-model="newProposal.representative"
              label="Representante"
              size="xs"
              :options="representatives.map((o) => ({ label: o, value: o }))"
            />
            <BaseSelect
              v-model="newProposal.manager"
              label="Gerente"
              size="xs"
              :options="managerOptions.map((o) => ({ label: o, value: o }))"
            />
          </div>
        </div>
        <div v-if="false" class="grid grid-cols-1 md:grid-cols-2 gap-2"></div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
            @click="closeNewProposalModal"
          >
            Cancelar
          </button>
          <button
            class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-2 py-1 rounded-md text-xs"
            @click="saveNewProposal"
          >
            <i class="fa-solid fa-check mr-1"></i>Salvar
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Delete Proposal Modal -->
    <BaseModal
      v-model="showDeleteProposalModal"
      title="Excluir Registro"
      size="sm"
      :z-index="80"
    >
      <p class="text-sm text-slate-200">
        Tem certeza que deseja excluir este registro?
      </p>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
            @click="cancelDeleteProposal"
          >
            Cancelar
          </button>
          <button
            class="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded-md text-xs"
            @click="confirmDeleteProposal"
          >
            Excluir
          </button>
        </div>
      </template>
    </BaseModal>

    <!-- Stage Form Modal (per card) -->
    <BaseModal
      v-model="showStageFormModal"
      title="Formulário da Etapa"
      size="max"
      :z-index="70"
    >
      <div class="space-y-2">
        <div class="flex items-center justify-between">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2 flex-1">
            <div class="text-[12px] text-slate-300">Etapa atual</div>
            <div class="text-[12px] text-slate-100">
              {{ stageTitle(selectedProposal?.stageId) }}
            </div>
          </div>
          <button
            v-if="selectedProposal"
            @click="openProcessFilesModal"
            class="text-slate-400 hover:text-slate-200 flex items-center gap-2 px-2 py-1 rounded-md hover:bg-slate-700/50 transition-colors text-sm ml-4"
            title="Ver arquivos do processo"
          >
            <i class="fa-solid fa-folder-open text-sm"></i>
            <span class="text-xs">{{ processFilesCount }}</span>
          </button>
        </div>
        <!-- Grid para inputs normais -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          <template v-for="f in stageFormFields" :key="f.id">
            <div
              v-if="
                f.type !== 'file' &&
                f.type !== 'endereco' &&
                f.type !== 'pessoa_fisica'
              "
            >
              <BaseInput
                v-if="f.type === 'text' || f.type === 'number'"
                v-model="stageFormValues[f.id]"
                :type="f.type === 'number' ? 'text' : 'text'"
                :numeric="f.type === 'number'"
                :label="f.label"
                size="xs"
                :placeholder="f.placeholder || ''"
              />
              <CpfInput
                v-else-if="f.type === 'cpf'"
                v-model="stageFormValues[f.id]"
                :label="f.label"
                :placeholder="f.placeholder || '000.000.000-00'"
                :required="f.required"
                :show-validation-info="true"
              />
              <CnpjInput
                v-else-if="f.type === 'cnpj'"
                v-model="stageFormValues[f.id]"
                :label="f.label"
                :placeholder="f.placeholder || '00.000.000/0000-00'"
                :required="f.required"
                :show-validation-info="true"
              />
              <RgInput
                v-else-if="f.type === 'rg'"
                v-model="stageFormValues[f.id]"
                :label="f.label"
                :placeholder="f.placeholder || '00.000.000-0'"
                :required="f.required"
                :show-validation-info="true"
              />
              <CepInput
                v-else-if="f.type === 'cep'"
                v-model="stageFormValues[f.id]"
                :label="f.label"
                :placeholder="f.placeholder || '00000-000'"
                :required="f.required"
                :show-validation-info="true"
                :auto-fetch-address="true"
              />
              <TelefoneInput
                v-else-if="f.type === 'telefone'"
                v-model="stageFormValues[f.id]"
                :label="f.label"
                :placeholder="f.placeholder || '(00) 00000-0000'"
                :required="f.required"
                :show-validation-info="true"
              />
              <BaseInput
                v-else-if="f.type === 'date'"
                v-model="stageFormValues[f.id]"
                type="date"
                :label="f.label"
                size="xs"
              />
              <BaseSelect
                v-else-if="f.type === 'select'"
                v-model="stageFormValues[f.id]"
                :label="f.label"
                size="xs"
                :options="
                  (f.options || []).map((o) => ({ label: o, value: o }))
                "
              />
            </div>
          </template>
        </div>

        <!-- Inputs de upload e endereço ocupam largura completa -->
        <div class="space-y-3">
          <template v-for="f in stageFormFields" :key="f.id">
            <div v-if="f.type === 'file'" class="w-full">
              <FormField :label="f.label" :dense="true">
                <FileDrop @files="(files) => onStageFormFile(f.id, files)" />
              </FormField>
            </div>
            <div v-else-if="f.type === 'endereco'" class="w-full">
              <EnderecoInput
                v-model="stageFormValues[f.id]"
                :label="f.label"
                :required="f.required"
                :auto-fill-from-cep="true"
              />
            </div>
            <div v-else-if="f.type === 'pessoa_fisica'" class="w-full">
              <FormField :label="f.label" :dense="true">
                <div class="flex items-center gap-2">
                  <button
                    type="button"
                    class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
                    @click="openPessoaFisicaForField(f.id)"
                  >
                    Preencher Pessoa Física
                  </button>
                  <span
                    v-if="stageFormValues[f.id]"
                    class="text-[12px] text-green-300"
                    >Preenchido</span
                  >
                  <span v-else class="text-[12px] text-slate-300"
                    >Não preenchido</span
                  >
                </div>
              </FormField>
            </div>
          </template>
        </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-between gap-2">
          <!-- Status selector on the left -->
          <div class="flex items-center gap-2">
            <label class="text-xs text-slate-300">Status:</label>
            <div class="relative">
              <select
                v-model="selectedProposalStatus"
                class="bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-2 py-1 text-xs min-w-[120px] pr-8"
                @change="updateProposalStatus"
              >
                <option
                  v-for="status in statusOptionsWithColors.length
                    ? statusOptionsWithColors
                    : statusOptions.map((name) => ({ name }))"
                  :key="status.name || status"
                  :value="status.name || status"
                >
                  {{ status.name || status }}
                </option>
              </select>
              <!-- Color indicator -->
              <div
                v-if="getStatusColor(selectedProposalStatus)"
                class="absolute right-2 top-1/2 transform -translate-y-1/2 w-2 h-2 rounded-full pointer-events-none"
                :style="{
                  backgroundColor: getStatusColor(selectedProposalStatus),
                }"
              ></div>
            </div>
          </div>

          <!-- Buttons on the right -->
          <div class="flex items-center gap-2">
            <button
              class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
              @click="showStageFormModal = false"
            >
              Fechar
            </button>
            <button
              class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-2 py-1 rounded-md text-xs"
              @click="saveStageForm"
            >
              Salvar
            </button>
          </div>
        </div>
      </template>
    </BaseModal>

    <ChatModal
      v-model:show="showAiModal"
      :messages="aiMessages"
      :typing="aiTyping"
      title="ConsorIA"
      size="sm"
      @send="onAiSend"
    />
    <BaseModal
      v-model="showSearchModal"
      title="Pesquisar"
      size="sm"
      :z-index="80"
    >
      <AiSearch v-model="searchText" :persist-on-click="false" />
    </BaseModal>

    <!-- PessoaFisica Modal -->
    <PessoaFisica
      :show="showPessoaFisicaModal"
      :initialData="
        currentPessoaFisicaFieldId
          ? stageFormValues[currentPessoaFisicaFieldId] || {}
          : {}
      "
      @close="closePessoaFisicaModal"
      @submit="onPessoaFisicaSubmit"
    />

    <!-- Process Files Approval Modal -->
    <ProcessFilesApprovalView
      :show="showProcessFilesApprovalModal"
      :process-key="selectedProposal?.processExternalId || pipelineKey"
      :files="processFiles"
      @close="showProcessFilesApprovalModal = false"
      @refresh="refreshProposalFiles"
      @approve-file="onApproveFile"
      @reject-file="onRejectFile"
      @download-file="onDownloadFile"
    />

    <!-- Quick Status Change Dropdown with Glassmorphism -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition-all duration-300 ease-out"
        enter-from-class="opacity-0 scale-95 translate-y-2"
        enter-to-class="opacity-100 scale-100 translate-y-0"
        leave-active-class="transition-all duration-200 ease-in"
        leave-from-class="opacity-100 scale-100 translate-y-0"
        leave-to-class="opacity-0 scale-95 translate-y-2"
      >
        <div
          v-if="showStatusDropdown"
          class="fixed inset-0 z-50"
          @click="closeStatusDropdown"
        >
          <div
            class="absolute bg-slate-900/90 backdrop-blur-xl border border-white/20 rounded-lg shadow-xl w-auto min-w-[140px] touch-manipulation"
            :style="{
              top: statusDropdownPosition.top + 'px',
              left: statusDropdownPosition.left + 'px'
            }"
            @click.stop
            @touchstart.passive
          >
            <!-- Minimal glass morphism background -->
            <div class="absolute inset-0 bg-gradient-to-br from-slate-800/40 to-slate-900/60 rounded-lg"></div>

            <!-- Compact dropdown content -->
            <div class="relative p-2">
              <!-- Status options list - minimalist -->
              <div class="space-y-0.5">
                <button
                  v-for="status in statusOptionsWithColors"
                  :key="status.name"
                  :disabled="status.name === statusDropdownProposal?.status || quickStatusLoading"
                  class="group relative w-full px-2 py-1.5 rounded transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed touch-manipulation min-h-[32px] active:scale-95 flex items-center justify-start"
                  :class="[
                    status.name === statusDropdownProposal?.status
                      ? 'bg-white/10 cursor-not-allowed'
                      : 'hover:bg-white/10 active:bg-white/15'
                  ]"
                  @click="updateQuickStatus(status.name)"
                  @touchstart.passive
                >
                  <!-- Neon status dot -->
                  <div
                    class="w-2 h-2 rounded-full mr-2 flex-shrink-0 transition-all duration-300"
                    :style="{
                      backgroundColor: status.color,
                      boxShadow: `0 0 6px ${status.color}, 0 0 12px ${status.color}40, inset 0 0 3px ${status.color}80`
                    }"
                  ></div>

                  <!-- Status name with tag font size - aligned left -->
                  <span
                    class="text-[9px] font-medium text-white whitespace-nowrap"
                    :class="status.name === statusDropdownProposal?.status ? 'opacity-60' : ''"
                  >
                    {{ status.name }}
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  watch,
  onMounted,
  onBeforeUnmount,
  onActivated,
  nextTick,
} from "vue";
import FinancialStats from "~/components/esteira/FinancialStats.vue";

definePageMeta({
  layout: "sidebar",
});
import {
  loadProposals,
  saveProposals,
  fetchProposalsApi,
  createProposalApi,
  fetchProposalFormsApi,
  saveProposalStageFormApi,
  updateProposalApi,
  useProposalsReactive,
} from "~/composables/useProposals";
import { useProcessSubmenu } from "~/composables/useProcessMenu";
import ChatModal from "~/components/ui/ChatModal.vue";
import BaseInput from "~/components/ui/BaseInput.vue";
import BaseSelect from "~/components/ui/BaseSelect.vue";
import BaseInputGroup from "~/components/ui/BaseInputGroup.vue";
import FileDrop from "~/components/ui/FileDrop.vue";
import CpfInput from "~/components/ui/CpfInput.vue";
import CnpjInput from "~/components/ui/CnpjInput.vue";
import RgInput from "~/components/ui/RgInput.vue";
import CepInput from "~/components/ui/CepInput.vue";
import TelefoneInput from "~/components/ui/TelefoneInput.vue";
import EnderecoInput from "~/components/ui/EnderecoInput.vue";
import PessoaFisica from "~/components/ui/PessoaFisica.vue";
import PessoaFisicaInput from "~/components/ui/PessoaFisicaInput.vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import FormField from "~/components/ui/FormField.vue";
import ProcessFilesApprovalView from "~/components/ui/ProcessFilesApprovalView.vue";
import { isApiEnabled } from "~/utils/api/index";
import { saveStagesPreservingIdsApi } from "~/composables/useStages";
import { useToast } from "~/composables/useToast";
import { fetchStageFieldsApi } from "~/composables/useStageFields";
import { uploadFileViaPresign } from "~/composables/useFiles";
import { useNewRecordModal } from "~/composables/useNewRecordModal";
import { useProcessFiles } from "~/composables/useProcessFiles";
import { useProposalFiles } from "~/composables/useProposalFiles";
import {
  fetchStatuses,
  fetchDetailedStatuses,
} from "~/composables/useStatuses";

useHead({ title: "Esteira" });

// Pipeline key (padrão: quotaequity). Pode ser passado como prop quando usado dentro de [process].vue
const props = defineProps({
  pipelineKey: { type: String, default: "quotaequity" },
});
const pipelineKey = computed(() => props.pipelineKey || "quotaequity");

// Verificar se o processo é financeiro
const currentProcessInfo = ref(null);
const currentProcessType = computed(() => {
  const info = currentProcessInfo.value || {};
  if (typeof info.type === 'string') {
    return info.type.trim().toUpperCase();
  }
  return info.isFinanceiro ? 'FINANCIAL' : 'GENERIC';
});

const isFinancialProcess = computed(() => currentProcessType.value === 'FINANCIAL');
const isLeadQualificationProcess = computed(
  () => currentProcessType.value === 'LEAD_QUALIFICATION'
);
const financialStatsRef = ref(null);
let refreshTimeout;

const leadOptions = ref([]);
let leadOptionsLoadedFor = '';

const resetLeadOptions = () => {
  leadOptions.value = [];
  leadOptionsLoadedFor = '';
};

const loadLeadOptions = async (force = false) => {
  if (!isLeadQualificationProcess.value || !isApiEnabled()) {
    resetLeadOptions();
    return;
  }

  const key = pipelineKey.value || '';
  if (!force && leadOptionsLoadedFor === key && leadOptions.value.length) {
    return;
  }

  try {
    const { fetchLeadsApi } = await import('~/composables/useLeadsApi');
    const leads = await fetchLeadsApi();
    leadOptions.value = Array.isArray(leads)
      ? leads.map((lead) => ({
          id: lead.id,
          name: lead.name || '',
          email: lead.email || '',
          phone: lead.phone || '',
        }))
      : [];
    leadOptionsLoadedFor = key;
  } catch (error) {
    console.error('Failed to load leads for modal:', error);
    resetLeadOptions();
  }
};

watch(
  () => [pipelineKey.value, isLeadQualificationProcess.value],
  ([, isLead]) => {
    if (!isLead) {
      resetLeadOptions();
      return;
    }
    loadLeadOptions();
  },
  { immediate: true }
);

// Carregar informações do processo
const loadProcessInfo = async () => {
  if (pipelineKey.value) {
    try {
      const { getProcessInfo } = await import("~/composables/usePipeline");
      currentProcessInfo.value = await getProcessInfo(pipelineKey.value);
    } catch (error) {
      console.error("Error loading process info:", error);
    }
  }
};

// Modal global de novo registro
const { openModal: openGlobalModal } = useNewRecordModal();
// Modal de pesquisa (acionado pelo item da sidebar)
const showSearchModal = ref(false);
const searchText = ref("");

// Proposal files composable
const { files: proposalFiles, extractFilesFromProposal } = useProposalFiles();

// Process files for download functionality
const {
  files: processFiles,
  loadProcessFiles,
  downloadFile: downloadFileFromAPI,
} = useProcessFiles();

// Detecta dispositivos touch para desabilitar drag & drop (melhor usabilidade no mobile)
const isTouch = ref(false);
onMounted(() => {
  try {
    isTouch.value =
      "ontouchstart" in window ||
      (navigator && (navigator.maxTouchPoints || 0) > 0);
  } catch (_) {
    isTouch.value = false;
  }
});

// Colunas da esteira (carregadas por processo). Mantemos vazio por padrão
// e populamos via configuração salva ou a partir dos estágios presentes nas propostas.
const stages = ref([]);
const lastStagesSaveAt = ref(0);

// Load pipeline config: API-first, fallback local
async function loadPipelineConfig() {
  try {
    const { fetchStagesApi } = await import("~/composables/useStages");
    const { isApiEnabled } = await import("~/utils/api/index");
    if (isApiEnabled()) {
      const apiStages = await fetchStagesApi(pipelineKey.value);
      if (Array.isArray(apiStages)) {
        stages.value = apiStages.map((s) => ({
          id:
            s.id ||
            s.key ||
            s._id ||
            (s.title || "").toLowerCase().replace(/\s+/g, "_"),
          title: s.title || s.name || "Etapa",
          slaDays: Number(s.slaDays ?? 0),
          color: s.color || "sky",
          status: "Offline",
        }));
        return;
      }
    }
  } catch (_) {}
  // Fallback local (somente se API off)
  try {
    const raw = localStorage.getItem(`pipeline_config__${pipelineKey.value}`);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        const statusById = Object.fromEntries(
          stages.value.map((s) => [s.id, s.status])
        );
        stages.value = parsed.map((s) => ({
          id: s.id,
          title: s.title,
          slaDays: s.slaDays ?? 0,
          color: s.color || "sky",
          status: statusById[s.id] || "Offline",
        }));
      }
    }
  } catch (_) {}
  ensureStagesCoverProposals();
}

async function syncProposalsFromApi() {
  if (!isApiEnabled()) return;
  try {
    const arr = await fetchProposalsApi(pipelineKey.value);
    if (Array.isArray(arr)) proposals.value = arr;
  } catch {}
}

onMounted(async () => {
  loadPipelineConfig();
  loadProcessInfo(); // Carregar informações do processo
  syncProposalsFromApi();
  try {
    window.addEventListener("focus", syncProposalsFromApi);
  } catch {}

  // Load status options from backend - only ESTEIRA category for pipeline cards
  try {
    // Load status names for backward compatibility (all status)
    statusOptions.value = await fetchStatuses();

    // Load status with colors for modal display - filtered by ESTEIRA category
    statusOptionsWithColors.value = await fetchDetailedStatuses("ESTEIRA");

    // Force UI refresh after loading colors
    nextTick(() => {
      console.log(
        "Status colors loaded (ESTEIRA category):",
        statusOptionsWithColors.value.length,
        "items"
      );
    });
  } catch (error) {
    console.error("Failed to load status options:", error);
    // Fallback to default status options
    statusOptions.value = ["Api Offline"];
    statusOptionsWithColors.value = [];
  }
});
onBeforeUnmount(() => {
  try {
    window.removeEventListener("focus", syncProposalsFromApi);
  } catch {}
});
onActivated(() => {
  syncProposalsFromApi();
});
watch(
  () => pipelineKey.value,
  () => {
    loadPipelineConfig();
    loadProcessInfo(); // Recarregar informações do processo
    loadStageForms();
    initStageFields();
    if (isApiEnabled()) {
      fetchProposalsApi(pipelineKey.value)
        .then((arr) => {
          if (Array.isArray(arr)) proposals.value = arr;
        })
        .catch(() => {});
    }
  }
);

// Propostas por processo usando useFetch com reatividade automática
const {
  proposals,
  loading: proposalsLoading,
  refresh: refreshProposals,
} = useProposalsReactive(pipelineKey.value);

// Função para atualizar dados após operações (estatísticas + propostas)
const refreshData = () => {
  // Atualizar propostas apenas se usando API
  if (isApiEnabled()) {
    refreshProposals();
  }
  // Estatísticas são calculadas automaticamente e reativamente das propostas locais
  // Não precisamos mais disparar refresh manual das estatísticas
};

// Watcher para atualizar estatísticas automaticamente quando propostas mudarem
watch(
  () => proposals.value,
  () => {
    // Debounce para evitar muitas atualizações desnecessárias das estatísticas
    if (refreshTimeout) clearTimeout(refreshTimeout);
    refreshTimeout = setTimeout(() => {
      // Só atualizar estatísticas, não propostas (para evitar loop)
      if (isFinancialProcess.value && financialStatsRef.value) {
        financialStatsRef.value.refresh();
      }
    }, 300);
  },
  { deep: true }
);
watch(
  () => pipelineKey.value,
  (k) => {
    const loaded = loadProposals(k);
    proposals.value = Array.isArray(loaded) ? loaded : [];
    if (isApiEnabled()) {
      fetchProposalsApi(k)
        .then((arr) => {
          if (Array.isArray(arr)) proposals.value = arr;
        })
        .catch(() => {});
    }
  }
);
const persistProposals = () => {
  try {
    saveProposals(pipelineKey.value, proposals.value);
  } catch (_) {}
};

// Ensure Kanban has columns for all proposal stageIds
function titleFromId(id) {
  try {
    const pretty = String(id || "").replace(/_/g, " ");
    return pretty.replace(/\b\w/g, (m) => m.toUpperCase());
  } catch {
    return id;
  }
}
function ensureStagesCoverProposals() {
  const existing = new Set(stages.value.map((s) => s.id));
  const toAdd = new Set();
  for (const p of proposals.value) {
    if (p?.stageId && !existing.has(p.stageId)) toAdd.add(p.stageId);
  }
  if (toAdd.size) {
    const now = Array.from(toAdd).map((id) => ({
      id,
      title: titleFromId(id),
      slaDays: 0,
      status: "Offline",
      color: "sky",
      auto: true,
    }));
    stages.value = stages.value.concat(now);
  }
}

// Recalcula colunas apenas quando o conjunto de stageIds muda
watch(
  () => proposals.value.map((p) => p.stageId).join("|"),
  () => ensureStagesCoverProposals()
);

const filterText = ref("");
const filterStatus = ref("");
const showArchived = ref(false);
const viewMode = ref("kanban");
const sortOption = ref("default");

// Função para ordenar propostas
const sortProposals = (proposals) => {
  const sorted = [...proposals];

  switch (sortOption.value) {
    case "name-asc":
      return sorted.sort((a, b) => a.name.localeCompare(b.name));
    case "name-desc":
      return sorted.sort((a, b) => b.name.localeCompare(a.name));
    case "amount-asc":
      return sorted.sort((a, b) => (a.amount || 0) - (b.amount || 0));
    case "amount-desc":
      return sorted.sort((a, b) => (b.amount || 0) - (a.amount || 0));
    case "date-newest":
      return sorted.sort(
        (a, b) =>
          new Date(b.stageEnteredAt || b.createdAt || 0) -
          new Date(a.stageEnteredAt || a.createdAt || 0)
      );
    case "date-oldest":
      return sorted.sort(
        (a, b) =>
          new Date(a.stageEnteredAt || a.createdAt || 0) -
          new Date(b.stageEnteredAt || b.createdAt || 0)
      );
    default:
      return sorted; // Ordem padrão (como inserido)
  }
};

/* const statusList = [
  "Offline",
  "Em Análise",
  "Aguardando",
  "Em Processamento",
  "Aprovado",
  "Reprovado",
  "Finalizado",
]; */

const filteredProposals = computed(() => {
  return proposals.value.filter((p) => {
    const nameMatch = p.name
      .toLowerCase()
      .includes(filterText.value.toLowerCase());
    const statusMatch = !filterStatus.value || p.status === filterStatus.value;
    const archivedMatch = showArchived.value ? true : !p.isArchived;
    return nameMatch && statusMatch && archivedMatch;
  });
});

// Propostas ordenadas para a view de lista
const sortedFilteredProposals = computed(() => {
  return sortProposals(filteredProposals.value);
});

async function openCardForm(p) {
  selectedProposal.value = p;
  // load fields for current stage
  try {
    stageFormFields.value = await fetchStageFieldsApi(String(p.stageId));
  } catch {
    stageFormFields.value = [];
  }
  // load saved values from backend
  stageFormValues.value = {};
  if (isApiEnabled()) {
    try {
      const all = await fetchProposalFormsApi(pipelineKey.value, String(p.id));
      stageFormValues.value =
        all && all[String(p.stageId)] ? { ...all[String(p.stageId)] } : {};
    } catch {}
  }

  // Load files from backend using the new endpoint
  try {
    if (p.processExternalId) {
      await loadProcessFiles(p.processExternalId);
    }
  } catch (err) {
    console.warn("Failed to load process files:", err);
  }

  // Initialize status selector with current proposal status
  selectedProposalStatus.value = p.status || "Offline";

  // Refresh status options to ensure latest status are available - filtered by ESTEIRA category
  try {
    statusOptionsWithColors.value = await fetchDetailedStatuses("ESTEIRA");
    console.log(
      "Status options refreshed on card open (ESTEIRA category):",
      statusOptionsWithColors.value.length,
      "items"
    );
  } catch (error) {
    console.error("Failed to refresh status options:", error);
  }

  showStageFormModal.value = true;
}

async function saveStageForm() {
  const p = selectedProposal.value;
  if (!p) {
    showStageFormModal.value = false;
    return;
  }
  try {
    if (isApiEnabled()) {
      await saveProposalStageFormApi(
        pipelineKey.value,
        String(p.id),
        String(p.stageId),
        stageFormValues.value
      );
    }
  } catch {}
  showStageFormModal.value = false;
}

function openDeleteProposal(p) {
  deleteProposalTarget.value = p;
  showDeleteProposalModal.value = true;
}
function cancelDeleteProposal() {
  showDeleteProposalModal.value = false;
  deleteProposalTarget.value = null;
}
async function confirmDeleteProposal() {
  const p = deleteProposalTarget.value;
  if (!p) return cancelDeleteProposal();
  // optimistic UI update
  const prev = proposals.value.slice();
  proposals.value = proposals.value.filter((x) => x.id !== p.id);
  try {
    if (isApiEnabled()) {
      const ok = await deleteProposalApi(pipelineKey.value, String(p.id));
      if (!ok) proposals.value = prev;
      else refreshData(); // Atualizar estatísticas após delete bem-sucedido
    } else {
      persistProposals();
    }
  } catch {
    proposals.value = prev;
  }
  cancelDeleteProposal();
}

async function updateProposalStatus() {
  const proposal = selectedProposal.value;
  if (!proposal) return;

  // Store original status for rollback
  const originalStatus = proposal.status;
  const { success, error: showError } = useToast();

  // Update the status immediately in the UI
  proposal.status = selectedProposalStatus.value;

  // Update in the proposals array
  const index = proposals.value.findIndex((p) => p.id === proposal.id);
  if (index !== -1) {
    proposals.value[index].status = selectedProposalStatus.value;
  }

  // Persist to backend if API is enabled
  try {
    if (isApiEnabled()) {
      await updateProposalApi(
        pipelineKey.value,
        String(proposal.id),
        { status: selectedProposalStatus.value } // Pass as patch object
      );
      // Show success toast
      success("Status atualizado com sucesso!");
      refreshData(); // Atualizar estatísticas após update
    } else {
      // Save to localStorage
      persistProposals();
      success("Status atualizado!");
    }
  } catch (error) {
    console.error("Failed to update proposal status:", error);
    showError("Erro ao atualizar status. Tente novamente.");
    // Revert UI changes on error
    proposal.status = originalStatus;
    const index = proposals.value.findIndex((p) => p.id === proposal.id);
    if (index !== -1) {
      proposals.value[index].status = originalStatus;
    }
  }
}

// Helper function to convert hex to rgba
function hexToRgba(hex, alpha = 0.2) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Function to get the color of a status with transparency
function getStatusColor(statusName) {
  if (!statusName || !statusOptionsWithColors.value.length) return null;

  // Try exact match first
  const exactMatch = statusOptionsWithColors.value.find(
    (s) => s.name === statusName
  );
  if (exactMatch?.color) return hexToRgba(exactMatch.color, 0.6);

  // Try case-insensitive match
  const caseInsensitiveMatch = statusOptionsWithColors.value.find(
    (s) => s.name?.toLowerCase() === statusName.toLowerCase()
  );
  if (caseInsensitiveMatch?.color)
    return hexToRgba(caseInsensitiveMatch.color, 0.2);

  return null;
}

// Enhanced function to get dynamic status style with modern glassmorphism effects
function getStatusStyle(statusName) {
  if (!statusName || !statusOptionsWithColors.value.length) return "";

  const exactMatch = statusOptionsWithColors.value.find(
    (s) => s.name === statusName
  );

  if (exactMatch?.color) {
    const baseColor = exactMatch.color;

    // Modern glassmorphism effect with enhanced styling
    return `
      background: linear-gradient(135deg, ${hexToRgba(baseColor, 0.15)}, ${hexToRgba(baseColor, 0.05)});
      color: ${baseColor};
      border: 1px solid ${hexToRgba(baseColor, 0.3)};
      box-shadow: 0 2px 8px ${hexToRgba(baseColor, 0.15)};
      backdrop-filter: blur(4px);
    `.replace(/\s+/g, ' ').trim();
  }

  // Fallback for status without color
  return `
    background: linear-gradient(135deg, rgba(156, 163, 175, 0.15), rgba(156, 163, 175, 0.05));
    color: #9ca3af;
    border: 1px solid rgba(156, 163, 175, 0.3);
    box-shadow: 0 2px 8px rgba(156, 163, 175, 0.15);
    backdrop-filter: blur(4px);
  `.replace(/\s+/g, ' ').trim();
}

function openMoveModal(p) {
  // For now open the stage form as edit; future: implement a quick move popup
  openCardForm(p);
}

// Stage form modal state
const showStageFormModal = ref(false);
const selectedProposal = ref(null);
const stageFormFields = ref([]);
const stageFormValues = ref({});
const showDeleteProposalModal = ref(false);
const deleteProposalTarget = ref(null);
const showPessoaFisicaModal = ref(false);
const currentPessoaFisicaFieldId = ref(null);

// Quick Status Change Dropdown
const showStatusDropdown = ref(false);
const statusDropdownProposal = ref(null);
const statusDropdownPosition = ref({ top: 0, left: 0 });
const quickStatusLoading = ref(false);
const showProcessFilesApprovalModal = ref(false);
const selectedProposalStatus = ref("");
const statusOptions = ref([]);
const statusOptionsWithColors = ref([]);

// Process Files composable for modal
const {
  files: modalProcessFiles,
  loading: processFilesLoading,
  error: processFilesError,
  loadProcessFiles: loadModalProcessFiles,
  downloadFileFromAPI: downloadModalFile,
} = useProcessFiles();

const processFilesCount = computed(() => processFiles.value.length);

const filteredByStage = (stageId) => {
  const proposalsInStage = filteredProposals.value.filter(
    (p) => p.stageId === stageId
  );
  return sortProposals(proposalsInStage);
};
const stageTitle = (id) => stages.value.find((s) => s.id === id)?.title || id;

// Calculate stage financial sum
const getStageSum = (stageId) => {
  if (!isFinancialProcess.value) return 0;

  const stageProposals = filteredByStage(stageId);
  return stageProposals.reduce((sum, proposal) => {
    return sum + (Number(proposal.amount) || 0);
  }, 0);
};

// Format currency for stage sum
const formatStageSum = (stageId) => {
  const sum = getStageSum(stageId);
  if (sum === 0) return "";

  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(sum);
};

const statusPillClass = (status) => {
  // Get color from database
  const statusColor = getStatusColor(status);

  if (statusColor) {
    // Use dynamic color effect based on status color
    return `ring-1 ring-inset transition-colors`;
  }

  // Fallback to hardcoded colors if not found in database

  if (!status)
    return "bg-slate-700 text-slate-300 ring-1 ring-inset ring-slate-500/20";
};

// Stage color styles derived from color field
const colorStyles = {
  sky: {
    border: "border-sky-500",
    strip: "bg-sky-500",
    badge: "bg-sky-900/50 text-sky-300 ring-1 ring-inset ring-sky-500/20",
  },
  indigo: {
    border: "border-indigo-500",
    strip: "bg-indigo-500",
    badge:
      "bg-indigo-900/50 text-indigo-300 ring-1 ring-inset ring-indigo-500/20",
  },
  amber: {
    border: "border-amber-500",
    strip: "bg-amber-500",
    badge: "bg-amber-900/50 text-amber-300 ring-1 ring-inset ring-amber-500/20",
  },
  rose: {
    border: "border-rose-500",
    strip: "bg-rose-500",
    badge: "bg-rose-900/50 text-rose-300 ring-1 ring-inset ring-rose-500/20",
  },
  green: {
    border: "border-green-500",
    strip: "bg-green-500",
    badge: "bg-green-900/50 text-green-300 ring-1 ring-inset ring-green-500/20",
  },
  purple: {
    border: "border-purple-500",
    strip: "bg-purple-500",
    badge:
      "bg-purple-900/50 text-purple-300 ring-1 ring-inset ring-purple-500/20",
  },
  blue: {
    border: "border-blue-500",
    strip: "bg-blue-500",
    badge: "bg-blue-900/50 text-blue-300 ring-1 ring-inset ring-blue-500/20",
  },
  orange: {
    border: "border-indigo-500",
    strip: "bg-indigo-600",
    badge:
      "bg-indigo-900/50 text-indigo-300 ring-1 ring-inset ring-indigo-500/20",
  },
  teal: {
    border: "border-teal-500",
    strip: "bg-teal-500",
    badge: "bg-teal-900/50 text-teal-300 ring-1 ring-inset ring-teal-500/20",
  },
  cyan: {
    border: "border-cyan-500",
    strip: "bg-cyan-500",
    badge: "bg-cyan-900/50 text-cyan-300 ring-1 ring-inset ring-cyan-500/20",
  },
  lime: {
    border: "border-lime-500",
    strip: "bg-lime-500",
    badge: "bg-lime-900/50 text-lime-300 ring-1 ring-inset ring-lime-500/20",
  },
  emerald: {
    border: "border-emerald-500",
    strip: "bg-emerald-500",
    badge:
      "bg-emerald-900/50 text-emerald-300 ring-1 ring-inset ring-emerald-500/20",
  },
  fuchsia: {
    border: "border-fuchsia-500",
    strip: "bg-fuchsia-500",
    badge:
      "bg-fuchsia-900/50 text-fuchsia-300 ring-1 ring-inset ring-fuchsia-500/20",
  },
  violet: {
    border: "border-violet-500",
    strip: "bg-violet-500",
    badge:
      "bg-violet-900/50 text-violet-300 ring-1 ring-inset ring-violet-500/20",
  },
  pink: {
    border: "border-pink-500",
    strip: "bg-pink-500",
    badge: "bg-pink-900/50 text-pink-300 ring-1 ring-inset ring-pink-500/20",
  },
  red: {
    border: "border-red-500",
    strip: "bg-red-500",
    badge: "bg-red-900/50 text-red-300 ring-1 ring-inset ring-red-500/20",
  },
  yellow: {
    border: "border-yellow-500",
    strip: "bg-yellow-500",
    badge:
      "bg-yellow-900/50 text-yellow-300 ring-1 ring-inset ring-yellow-500/20",
  },
  slate: {
    border: "border-slate-500",
    strip: "bg-slate-500",
    badge: "bg-slate-900/50 text-slate-300 ring-1 ring-inset ring-slate-500/20",
  },
  stone: {
    border: "border-stone-500",
    strip: "bg-stone-500",
    badge: "bg-stone-900/50 text-stone-300 ring-1 ring-inset ring-stone-500/20",
  },
  zinc: {
    border: "border-zinc-500",
    strip: "bg-zinc-500",
    badge: "bg-zinc-900/50 text-zinc-300 ring-1 ring-inset ring-zinc-500/20",
  },
};

const stageBorderClass = (stageId) => {
  const st = stages.value.find((s) => s.id === stageId);
  return (st && colorStyles[st.color]?.border) || "border-slate-500";
};
const stageStripClass = (stageId) => {
  const st = stages.value.find((s) => s.id === stageId);
  return (st && colorStyles[st.color]?.strip) || "bg-slate-500";
};
const stageBadgeClass = (stageId) => {
  const st = stages.value.find((s) => s.id === stageId);
  return (
    (st && colorStyles[st.color]?.badge) ||
    "bg-slate-700 text-slate-300 ring-1 ring-inset ring-slate-500/20"
  );
};

// SLA helpers
const getSlaInfo = (p) => {
  const stage = stages.value.find((s) => s.id === p.stageId);
  const sla = stage?.slaDays ?? 0;
  if (!sla || !p.stageEnteredAt)
    return { days: 0, sla, remaining: 0, status: "ok" };
  const entered = new Date(p.stageEnteredAt);
  const now = new Date();
  const msPerDay = 24 * 60 * 60 * 1000;
  const days = Math.max(
    0,
    Math.ceil((now.getTime() - entered.getTime()) / msPerDay)
  );
  const remaining = sla - days;
  let status = "ok";
  if (days > sla) status = "overdue";
  else if (days >= Math.max(1, Math.floor(sla * 0.8))) status = "warning";
  return { days, sla, remaining, status };
};

const slaBadgeClass = (p) => {
  const { status } = getSlaInfo(p);
  if (status === "overdue") return "bg-red-900/60 text-red-300";
  if (status === "warning") return "bg-amber-900/60 text-amber-300";
  return "bg-slate-700 text-slate-300";
};

const slaLabel = (p) => {
  const { days, sla } = getSlaInfo(p);
  if (!sla) return "SLA N/A";
  return `${days}/${sla}d`;
};

// Drag and Drop state/handlers
const draggedId = ref(null);
const dragOverStage = ref("");
const draggingStageId = ref("");
const columnDragOverIndex = ref(-1);
const isDraggingStage = computed(() => !!draggingStageId.value);

const onDragStart = (event, proposal) => {
  draggedId.value = proposal.id;
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/plain", String(proposal.id));
  }
};

const onDragEnd = () => {
  draggedId.value = null;
  dragOverStage.value = "";
};

const onDragEnter = (stageId) => {
  if (isDraggingStage.value) return;
  dragOverStage.value = stageId;
};

const onDragLeave = (stageId) => {
  if (isDraggingStage.value) return;
  if (dragOverStage.value === stageId) dragOverStage.value = "";
};

const onDrop = (stageId) => {
  // If dragging a column, ignore card drop handler
  // (column drop is handled in capture phase)
  if (isDraggingStage.value) return;
  if (!draggedId.value) return;
  const idx = proposals.value.findIndex((p) => p.id === draggedId.value);
  if (idx === -1) return onDragEnd();

  const stage = stages.value.find((s) => s.id === stageId);
  if (!stage) return onDragEnd();

  // Update stage and default status when moved
  const newStatus =
    stage.defaultStatus && stage.defaultStatus.trim() !== ""
      ? stage.defaultStatus
      : proposals.value[idx].status || "Offline";

  const updated = {
    ...proposals.value[idx],
    stageId,
    status: newStatus,
    stageEnteredAt: new Date().toISOString(),
  };
  const next = proposals.value.slice();
  next[idx] = updated;
  proposals.value = next;
  if (isApiEnabled()) {
    const pid = String(updated.id);
    const prev = proposals.value[idx];
    updateProposalApi(pipelineKey.value, pid, {
      stageId: stageId,
      status: newStatus,
    })
      .then(() => {
        // Estatísticas são atualizadas automaticamente via reatividade
        // refreshData() não é mais necessário para mudanças de estágio
      })
      .catch(() => {
        // rollback on failure
        const arr = proposals.value.slice();
        arr[idx] = { ...prev };
        proposals.value = arr;
      });
  } else {
    persistProposals();
  }

  onDragEnd();
};

// Column reorder (DnD)
const STAGE_MIME = "application/x-stage-id";
function onStageDragStart(event, stage, index) {
  draggingStageId.value = stage.id;
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    try {
      event.dataTransfer.setData(STAGE_MIME, stage.id);
    } catch (_) {}
  }
}
function onStageDragEnd() {
  draggingStageId.value = "";
  columnDragOverIndex.value = -1;
}
function onColumnDragOver(event, overIndex) {
  if (!draggingStageId.value) return;
  const types = Array.from(event?.dataTransfer?.types || []);
  if (!types.includes(STAGE_MIME)) return;
  event.preventDefault();
  columnDragOverIndex.value = overIndex;
}
function persistStages() {
  if (isApiEnabled()) {
    const { success, error } = useToast();
    // Preserva snapshot anterior para comparação de IDs
    const previousStages = stages.value.map((s) => ({
      id: s.id,
      title: s.title,
    }));
    saveStagesPreservingIdsApi(pipelineKey.value, previousStages, stages.value)
      .then((saved) => {
        if (Array.isArray(saved) && saved.length) {
          // preserva status por título quando possível
          const statusByTitle = {};
          try {
            stages.value.forEach((s) => {
              statusByTitle[s.title] = s.status || "Offline";
            });
          } catch {}
          stages.value = saved.map((s) => ({
            id: s.id,
            title: s.title,
            slaDays: s.slaDays,
            color: s.color,
            status: statusByTitle[s.title] || "Offline",
          }));
        }
        const now = Date.now();
        if (now - lastStagesSaveAt.value > 1500) {
          success("Colunas atualizadas");
          lastStagesSaveAt.value = now;
        }
      })
      .catch(() => error("Falha ao salvar colunas"));
    return;
  }
  try {
    localStorage.setItem(
      `pipeline_config__${pipelineKey.value}`,
      JSON.stringify(stages.value)
    );
  } catch (_) {}
}
function onColumnDrop(event, dropIndex) {
  const types = Array.from(event?.dataTransfer?.types || []);
  if (!types.includes(STAGE_MIME)) return;
  event.preventDefault();
  event.stopPropagation();
  const dragged =
    event.dataTransfer.getData(STAGE_MIME) || draggingStageId.value;
  const fromIndex = stages.value.findIndex((s) => s.id === dragged);
  if (fromIndex < 0 || dropIndex < 0 || fromIndex === dropIndex) {
    onStageDragEnd();
    return;
  }
  const arr = stages.value.slice();
  const [moved] = arr.splice(fromIndex, 1);
  arr.splice(dropIndex, 0, moved);
  stages.value = arr;
  persistStages();
  onStageDragEnd();
}

// New Proposal Modal (mocked options; replace with API later)
const showNewModal = ref(false);
const states = [
  "AC",
  "AL",
  "AP",
  "AM",
  "BA",
  "CE",
  "DF",
  "ES",
  "GO",
  "MA",
  "MT",
  "MS",
  "MG",
  "PA",
  "PB",
  "PR",
  "PE",
  "PI",
  "RJ",
  "RN",
  "RS",
  "RO",
  "RR",
  "SC",
  "SP",
  "SE",
  "TO",
];
const bankOptions = [
  "TOCO",
  "LANCE",
  "AMAURI",
  "Tramonta",
  "Fraga & Bitello",
  "SI",
  "Larban",
  "DW Consórcios",
];
const fundOptions = ["CREMONA", "SAVEL", "FOUR CAPITAL", "VALOR", "LARBAN"];
const productTypes = ["ConsorEquity", "Fundo Inter", "Flex", "Outro"];
const adminOptions = ["Itaú", "HS", "Bradesco"];
const guaranteeTypes = ["Imóvel", "Veículo", "Aplicação", "Outro"];
const objectives = ["Compra de imóvel", "Reforma", "Capital de giro", "Outros"];
const representatives = ["Sem representante", "Ana", "Bruno", "Carlos"];
const managerOptions = [
  "Sem gerente",
  "Juliana Rezende",
  "Rafael Santos",
  "Carla Lima",
];

const newProposal = ref({
  name: "",
  cpf: "",
  cep: "",
  city: "",
  state: "",
  administrator: "",
  bank: "",
  fund: "",
  productType: "",
  summary: "",
  startDate: "",
  amountDesired: 0,
  guaranteeValue: 0,
  guaranteeType: "",
  objective: "",
  representative: representatives[0],
  manager: managerOptions[0],
  stageId: stages.value[0]?.id || "dados_basicos",
});
const formErrors = ref({ name: "", cpf: "", amountDesired: "" });

// Função para abrir o modal global
const openGlobalNewRecordModal = async () => {
  // Sempre garantir que use a primeira etapa
  const firstStageId = stages.value[0]?.id || "dados_basicos";

  // Carregar campos dinâmicos da primeira etapa
  initStageFields();

  if (isLeadQualificationProcess.value) {
    await loadLeadOptions(true);
  }

  // Abrir modal global
  openGlobalModal({
    stages: stages.value,
    stageFields: stageDynamicFields.value,
    pipelineKey: pipelineKey.value,
    isFinancial: isFinancialProcess.value,
    leadOptions: isLeadQualificationProcess.value ? leadOptions.value : [],
    onSave: (recordData) => {
      handleGlobalModalSave(recordData);
    },
  });

  // Mostrar informação sobre os campos carregados
  const { info } = useToast();
  if (stageDynamicFields.value.length > 0) {
    info(
      `Carregados ${stageDynamicFields.value.length} campos da primeira etapa: ${stages.value[0]?.title || "Primeira Etapa"}`
    );
  } else {
    info(
      `Abrindo formulário da primeira etapa: ${stages.value[0]?.title || "Primeira Etapa"}`
    );
  }
};

// Função para abrir o modal de aprovação de arquivos
const openProcessFilesModal = () => {
  showProcessFilesApprovalModal.value = true;
};

// Função para atualizar arquivos da proposta
const refreshProposalFiles = async () => {
  if (selectedProposal.value && selectedProposal.value.processExternalId) {
    try {
      await loadProcessFiles(selectedProposal.value.processExternalId);
    } catch (err) {
      console.warn("Failed to refresh process files:", err);
    }
  }
};

// Função para aprovar arquivo
const onApproveFile = ({ file, approvalBy }) => {
  // TODO: Implementar chamada para API de aprovação
  console.log(`Aprovando arquivo ${file.id} por ${approvalBy}`);
  const { success } = useToast();
  success(`Arquivo "${file.originalName}" aprovado por ${approvalBy}`);
};

// Função para reprovar arquivo
const onRejectFile = ({ file }) => {
  // TODO: Implementar chamada para API de reprovação
  console.log(`Reprovando arquivo ${file.id}`);
  const { warning } = useToast();
  warning(`Arquivo "${file.originalName}" reprovado`);
};

// Função para fazer download do arquivo
const onDownloadFile = async ({ file }) => {
  try {
    await downloadFileFromAPI(file);
  } catch (err) {
    const { error } = useToast();
    error(`Erro ao baixar arquivo "${file.originalName}"`);
    console.error("Download error:", err);
  }
};

// Função para tratar salvamento do modal global
const handleGlobalModalSave = (recordData) => {
  const { success, error } = useToast();

  // Criar proposta com dados do modal global
  const stage = stages.value.find((s) => s.id === recordData.stageId);

  const newId = Date.now().toString();
  const newProposal = {
    id: newId,
    name: recordData.name,
    amount: recordData.amount,
    stageId: recordData.stageId,
    status: recordData.status,
    isArchived: false,
    stageEnteredAt: new Date().toISOString(),
    stageValues: recordData.fieldValues || {},
    files: recordData.fieldFiles || {},
    details: recordData.leadId ? { leadId: recordData.leadId } : undefined,
  };

  // Adicionar à lista local
  proposals.value.push(newProposal);
  saveProposals(pipelineKey.value, proposals.value);

  // Salvar via API se habilitado
  if (isApiEnabled()) {
    const payload = {
      name: recordData.name,
      amount: recordData.amount,
      stageId: recordData.stageId,
      status: recordData.status,
    };
    if (recordData.leadId) {
      payload.details = { leadId: recordData.leadId };
    }
    console.log("Creating proposal via API with payload:", payload);
    createProposalApi(pipelineKey.value, payload)
      .then(async (created) => {
        if (created?.id) {
          const createdId = String(created.id);
          const localIndex = proposals.value.findIndex((p) => p.id === newId);
          if (localIndex >= 0) {
            proposals.value[localIndex].id = createdId;
            proposals.value[localIndex].details = created.details || proposals.value[localIndex].details;
            saveProposals(pipelineKey.value, proposals.value);
            refreshData(); // Atualizar estatísticas após criar proposta
          }

          // Upload de arquivos se necessário
          for (const [fieldId, files] of Object.entries(
            recordData.fieldFiles || {}
          )) {
            if (files && files.length > 0) {
              try {
                await uploadFileViaPresign(createdId, files[0]);
              } catch (error) {
                console.warn(
                  `Erro ao fazer upload do arquivo ${fieldId}:`,
                  error
                );
              }
            }
          }
        }
        success("Registro criado com sucesso!");
      })
      .catch((error) => {
        console.error("Erro ao criar registro via API:", error);
        error("Erro ao salvar registro na API, mantido localmente");
      });
  } else {
    success("Registro criado com sucesso!");
  }
};

const closeNewProposalModal = () => {
  showNewModal.value = false;
};

const validateNewProposal = () => {
  // Validação agora é totalmente guiada pelos campos dinâmicos
  formErrors.value = { name: "", cpf: "", amountDesired: "" };
  return true;
};

const saveNewProposal = () => {
  const { warning } = useToast();
  if (!validateNewProposal()) return;
  // validate dynamic required fields
  extraFieldErrors.value = {};
  let okExtra = true;
  for (const f of stageDynamicFields.value) {
    if (!f?.required) continue;
    if (f.type === "file") {
      const hasFile = !!extraFieldFiles.value?.[f.id]?.length;
      if (!hasFile) {
        extraFieldErrors.value[f.id] = "Obrigatório";
        okExtra = false;
      }
    } else {
      const val = (extraFieldValues.value?.[f.id] ?? "").toString().trim();
      if (!val) {
        extraFieldErrors.value[f.id] = "Obrigatório";
        okExtra = false;
      }
    }
  }
  if (!okExtra) {
    showAdvanced.value = true;
    warning("Preencha os campos obrigatórios da etapa");
    return;
  }
  const stage = stages.value.find((s) => s.id === newProposal.value.stageId);
  console.log(
    "Creating new card - Stage found:",
    stage,
    "defaultStatus:",
    stage?.defaultStatus
  );
  const parseCurrency = (s) => {
    const str = String(s || "").trim();
    if (!str) return 0;
    const normalized = str.replace(/\./g, "").replace(/,/g, ".");
    const n = Number(normalized);
    return isNaN(n) ? 0 : n;
  };
  // Deriva 'name' e 'amount' a partir dos campos dinâmicos
  const deriveName = () => {
    for (const f of stageDynamicFields.value) {
      const lbl = (f.label || "").toLowerCase();
      if (lbl.includes("nome")) {
        const v = (extraFieldValues.value?.[f.id] || "").toString().trim();
        if (v) return v;
      }
    }
    const firstText = stageDynamicFields.value.find((f) => f.type === "text");
    return (
      (firstText
        ? (extraFieldValues.value?.[firstText.id] || "").toString().trim()
        : "") || "Registro"
    );
  };
  const deriveAmount = () => {
    for (const f of stageDynamicFields.value) {
      if (f.type === "number") {
        const lbl = (f.label || "").toLowerCase();
        if (
          lbl.includes("valor") ||
          lbl.includes("amount") ||
          lbl.includes("preço") ||
          lbl.includes("preco")
        ) {
          return parseCurrency(extraFieldValues.value?.[f.id]);
        }
      }
    }
    return 0;
  };
  const payload = {
    name: deriveName(),
    amount: deriveAmount(),
    stageId: newProposal.value.stageId,
    status: stage?.defaultStatus || "Offline",
  };
  const stageValues = (() => {
    const out = {};
    try {
      for (const f of stageDynamicFields.value) {
        out[f.id] = extraFieldValues.value?.[f.id] ?? "";
      }
    } catch {}
    return out;
  })();
  if (isApiEnabled()) {
    createProposalApi(pipelineKey.value, payload)
      .then(async (created) => {
        if (created) {
          try {
            await saveProposalStageFormApi(
              pipelineKey.value,
              String(created.id),
              String(created.stageId),
              stageValues
            );
          } catch {}
          proposals.value = [created, ...proposals.value];
          refreshData(); // Atualizar estatísticas após criar proposta
        }
      })
      .catch(() => {});
  } else {
    const id = Date.now();
    proposals.value.unshift({
      id,
      name: payload.name,
      amount: payload.amount,
      stageId: payload.stageId,
      status: payload.status,
      isArchived: false,
      stageEnteredAt: new Date().toISOString(),
      details: undefined,
    });
    persistProposals();
    refreshData(); // Atualizar estatísticas após criar proposta local
  }
  closeNewProposalModal();
};

// CEP lookup via SSR API (server route)
const cepLoading = ref(false);
const cepError = ref("");
const showAdvanced = ref(false);

// Dispara busca de CEP automaticamente ao digitar o 8º dígito
const lastCepQueried = ref("");
watch(
  () => newProposal.value.cep,
  (val) => {
    const digits = String(val || "").replace(/\D/g, "");
    if (
      digits.length === 8 &&
      digits !== lastCepQueried.value &&
      !cepLoading.value
    ) {
      lastCepQueried.value = digits;
      fetchCep();
    }
    if (digits.length < 8) {
      cepError.value = "";
    }
  }
);
const fetchCep = async () => {
  cepError.value = "";
  const digits = (newProposal.value.cep || "").replace(/\D/g, "");
  if (digits.length !== 8) {
    cepError.value = "Informe 8 dígitos";
    return;
  }
  cepLoading.value = true;
  try {
    const res = await $fetch("/api/cep?cep=" + digits);
    if (res) {
      // res is plain object from our SSR route
      newProposal.value.city = res.city || "";
      newProposal.value.state = res.state || "";
    }
  } catch (e) {
    cepError.value = e?.statusMessage || "Erro ao buscar CEP";
  } finally {
    cepLoading.value = false;
  }
};

// Upload de documentos para anexos no modal (exemplo, sem enviar ao backend ainda)
const uploadedDocs = ref([]);

// Dynamic fields per stage (from Admin builder)
const stageFormsMap = ref({});
const stageDynamicFields = ref([]);
const extraFieldValues = ref({});
const extraFieldFiles = ref({});
const extraFieldErrors = ref({});

function loadStageForms() {
  try {
    const raw = localStorage.getItem(
      `pipeline_stage_forms__${pipelineKey.value}`
    );
    if (raw) stageFormsMap.value = JSON.parse(raw) || {};
  } catch (_) {
    stageFormsMap.value = {};
  }
}

function initStageFields() {
  const sid = newProposal.value.stageId;
  const fallback = () => {
    const arr = Array.isArray(stageFormsMap.value[sid])
      ? stageFormsMap.value[sid]
      : [];
    stageDynamicFields.value = arr;
    const vals = {};
    arr.forEach((f) => {
      if (f.type !== "file") vals[f.id] = "";
    });
    extraFieldValues.value = vals;
    extraFieldFiles.value = {};
    extraFieldErrors.value = {};
  };
  // If API is enabled and stage id looks like Mongo ObjectId, load fields from backend
  const isMongoId = typeof sid === "string" && /^[a-fA-F0-9]{24}$/.test(sid);
  if (isApiEnabled() && isMongoId) {
    fetchStageFieldsApi(sid)
      .then((remote) => {
        const arr = Array.isArray(remote)
          ? remote.map((r) => ({
              id: r.id || r.label,
              label: r.label,
              type: r.type,
              required: !!r.required,
              placeholder: r.placeholder || "",
              options: Array.isArray(r.options) ? r.options : [],
            }))
          : [];
        stageDynamicFields.value = arr;
        const vals = {};
        arr.forEach((f) => {
          if (f.type !== "file") vals[f.id] = "";
        });
        extraFieldValues.value = vals;
        extraFieldFiles.value = {};
        extraFieldErrors.value = {};
      })
      .catch(() => fallback());
  } else {
    fallback();
  }
}

function onExtraFile(id, files) {
  extraFieldFiles.value[id] = files;
  // Upload immediately if API is enabled and store object keys as values
  try {
    if (isApiEnabled()) {
      const arr = Array.from(files || []);
      console.log(
        "Uploading files:",
        arr.map((f) => f.name)
      );
      Promise.all(arr.map((f) => uploadFileViaPresign(f, `proposals/tmp`)))
        .then((results) => {
          console.log("Upload success:", results);
          // store serialized list of keys or first key depending on expected shape
          const keys = results.map((r) => r.objectKey);
          extraFieldValues.value[id] = arr.length <= 1 ? keys[0] : keys;
        })
        .catch((error) => {
          console.error("Upload failed:", error);
          // Fallback: just store file names locally for now
          const names = arr.map((f) => f.name);
          extraFieldValues.value[id] = arr.length <= 1 ? names[0] : names;
        });
    }
  } catch (e) {
    console.error("onExtraFile error:", e);
  }
}

function onStageFormFile(id, files) {
  try {
    if (isApiEnabled()) {
      const arr = Array.from(files || []);
      Promise.all(
        arr.map((f) =>
          uploadFileViaPresign(
            f,
            `proposals/${selectedProposal.value?.id || "unknown"}`
          )
        )
      )
        .then((results) => {
          const keys = results.map((r) => r.objectKey);
          stageFormValues.value[id] = arr.length <= 1 ? keys[0] : keys;
        })
        .catch(() => {});
    }
  } catch {}
}

// Comentado: O modal sempre deve usar a primeira etapa, sem permitir mudança
// watch(
//   () => newProposal.value.stageId,
//   () => initStageFields()
// );
// Quick Status Change Functions
function openStatusDropdown(proposal, event) {
  // Evita abrir o form principal
  event.stopPropagation();

  // Calcula posição do dropdown com verificação de viewport
  const rect = event.target.getBoundingClientRect();
  const dropdownWidth = 140; // width mínimo do dropdown compacto
  const dropdownHeight = 160; // altura estimada reduzida

  let top = rect.bottom + window.scrollY + 8;
  let left = rect.left + window.scrollX;

  // Mobile-specific adjustments
  const isMobile = window.innerWidth < 768;

  if (isMobile) {
    // Em mobile, centraliza o dropdown
    left = Math.max(16, (window.innerWidth - dropdownWidth) / 2);
    // Posiciona mais baixo para não interferir com o toque
    top = rect.bottom + window.scrollY + 12;
  } else {
    // Ajusta posição horizontal se sair da tela
    if (left + dropdownWidth > window.innerWidth) {
      left = rect.right + window.scrollX - dropdownWidth;
    }

    // Ajusta posição vertical se sair da tela
    if (top + dropdownHeight > window.innerHeight + window.scrollY) {
      top = rect.top + window.scrollY - dropdownHeight - 8;
    }

    // Garante que não sai das bordas da tela
    left = Math.max(16, Math.min(left, window.innerWidth - dropdownWidth - 16));
    top = Math.max(16, top);
  }

  statusDropdownPosition.value = { top, left };
  statusDropdownProposal.value = proposal;
  showStatusDropdown.value = true;

  // Fecha dropdown ao clicar fora (removido para usar o Teleport click handler)
}

async function updateQuickStatus(newStatus) {
  if (!statusDropdownProposal.value || quickStatusLoading.value) return;

  quickStatusLoading.value = true;
  const originalStatus = statusDropdownProposal.value.status;

  try {
    const proposal = statusDropdownProposal.value;

    // Update locally first for immediate UI feedback
    proposal.status = newStatus;

    if (isApiEnabled()) {
      // Update via API with PUT request to the correct endpoint
      // URL: http://localhost:8080/api/v1/processes/{processId}/proposals/{proposalId}
      await updateProposalApi(pipelineKey.value, proposal.id, {
        status: newStatus
      });
    } else {
      // Update in localStorage
      const allProposals = await loadProposals();
      const index = allProposals.findIndex(p => p.id === proposal.id);
      if (index !== -1) {
        allProposals[index] = {
          ...allProposals[index],
          status: newStatus,
          updatedAt: new Date().toISOString()
        };
        await saveProposals(allProposals);
      }
    }

    // Update the proposal in the current stage as well for real-time UI sync
    const currentStage = stages.value.find(stage =>
      stage.proposals?.some(p => p.id === proposal.id)
    );
    if (currentStage) {
      const proposalIndex = currentStage.proposals.findIndex(p => p.id === proposal.id);
      if (proposalIndex !== -1) {
        currentStage.proposals[proposalIndex] = {
          ...currentStage.proposals[proposalIndex],
          status: newStatus,
          updatedAt: new Date().toISOString()
        };
      }
    }

    closeStatusDropdown();

    // Show success feedback
    useToast().add({
      title: 'Status atualizado!',
      description: `Status alterado para "${newStatus}"`,
      color: 'green'
    });

  } catch (error) {
    console.error('Erro ao atualizar status:', error);

    // Revert local change on error
    statusDropdownProposal.value.status = originalStatus;

    // Revert in stages array as well
    const currentStage = stages.value.find(stage =>
      stage.proposals?.some(p => p.id === statusDropdownProposal.value.id)
    );
    if (currentStage) {
      const proposalIndex = currentStage.proposals.findIndex(p => p.id === statusDropdownProposal.value.id);
      if (proposalIndex !== -1) {
        currentStage.proposals[proposalIndex].status = originalStatus;
      }
    }

    useToast().add({
      title: 'Erro',
      description: 'Não foi possível atualizar o status. Tente novamente.',
      color: 'red'
    });
  } finally {
    quickStatusLoading.value = false;
  }
}

function closeStatusDropdown() {
  showStatusDropdown.value = false;
  statusDropdownProposal.value = null;
}

onMounted(() => {
  loadStageForms();
  initStageFields();
});

// IA Chat modal (reutilizável via componente)
const showAiModal = ref(false);
const CHAT_STORE_KEY = "consoria_chat_history";
const aiMessages = ref([
  {
    role: "assistant",
    content: "Olá! Eu sou a ConsorIA. Como posso ajudar hoje?",
  },
]);
const aiTyping = ref(false);

const openAiChat = () => {
  try {
    const raw = localStorage.getItem(CHAT_STORE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (
        Array.isArray(parsed) &&
        parsed.every((m) => m && typeof m.role === "string")
      ) {
        aiMessages.value = parsed;
      }
    }
  } catch (_) {}
  showAiModal.value = true;
};
const closeAiChat = () => {
  showAiModal.value = false;
};

// PessoaFisica modal functions
const openPessoaFisicaModal = () => {
  showPessoaFisicaModal.value = true;
};

const openPessoaFisicaForField = (fieldId) => {
  currentPessoaFisicaFieldId.value = fieldId;
  showPessoaFisicaModal.value = true;
};

const closePessoaFisicaModal = () => {
  showPessoaFisicaModal.value = false;
};

const onPessoaFisicaSubmit = async (data) => {
  try {
    // Persistir no campo correspondente do formulário da etapa
    if (currentPessoaFisicaFieldId.value) {
      stageFormValues.value[currentPessoaFisicaFieldId.value] = data;
    }
    // Fechar o modal e limpar estado
    closePessoaFisicaModal();
    currentPessoaFisicaFieldId.value = null;
  } catch (error) {
    console.error("Erro ao salvar qualificação:", error);
  }
};
const onAiSend = async ({ text, attachments }) => {
  aiMessages.value.push({ role: "user", content: text, attachments });
  aiTyping.value = true;
  try {
    const history = aiMessages.value
      .slice(-8)
      .map((m) => ({ role: m.role, content: m.content }));
    const res = await $fetch("/api/ai", {
      method: "POST",
      body: { text, attachments, history },
    });
    aiMessages.value.push({
      role: "assistant",
      content: res?.["text"] || res?.["message"] || "Sem resposta.",
    });
  } catch (e) {
    aiMessages.value.push({
      role: "assistant",
      content: "Falha ao obter resposta da IA. Verifique sua configuração.",
    });
  } finally {
    aiTyping.value = false;
  }
};

// Persist chat history locally
// Persiste histórico apenas quando o tamanho da lista muda
watch(
  () => aiMessages.value.length,
  () => {
    try {
      localStorage.setItem(
        CHAT_STORE_KEY,
        JSON.stringify(aiMessages.value.slice(-50))
      );
    } catch (_) {}
  }
);
</script>

<style scoped>
/* Smooth column reordering */
.kanban-col-move {
  transition:
    transform 180ms ease,
    opacity 180ms ease;
}
.kanban-col-enter-active,
.kanban-col-leave-active {
  transition: all 180ms ease;
}
.kanban-col-enter-from,
.kanban-col-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

/* Mantém a aparência próxima ao HTML original */

/* Horizontal scrollbar styling for Kanban */
.kanban-scroll {
  scrollbar-width: thin;
  scrollbar-color: #475569 #0f172a; /* thumb slate-600, track slate-900 */
}
.kanban-scroll::-webkit-scrollbar {
  height: 8px;
}
.kanban-scroll::-webkit-scrollbar-track {
  background: #febf00; /* slate-800 */
  border-radius: 8px;
}
.kanban-scroll::-webkit-scrollbar-thumb {
  background: #475569; /* slate-600 */
  border-radius: 8px;
}
.kanban-scroll::-webkit-scrollbar-thumb:hover {
  background: #64748b; /* slate-500 */
}

/* Minimal, harmonious form controls for modal */
.new-proposal-modal .form-label {
  @apply text-[11px] text-slate-300;
}
.new-proposal-modal .form-label {
  @apply text-[10px];
}
.new-proposal-modal .form-input {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm h-5 px-1 text-[10px] leading-none outline-none focus:ring-[0.5px] focus:ring-indigo-500/60 focus:border-indigo-400/50 transition;
}
.new-proposal-modal .form-select {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm h-5 px-1 text-[10px] leading-none outline-none focus:ring-[0.5px] focus:ring-indigo-500/60 focus:border-indigo-400/50 transition;
}
.new-proposal-modal .form-textarea {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm px-1.5 py-1 text-[10px] outline-none focus:ring-[0.5px] focus:ring-indigo-500/60 focus:border-indigo-400/50 transition;
}
.new-proposal-modal .cep-group {
  @apply flex items-center gap-2;
}
.new-proposal-modal .btn {
  @apply px-1.5 py-[2px] rounded text-[10px] transition-colors;
}
.new-proposal-modal .cep-group .btn {
  @apply h-5;
}
.new-proposal-modal .btn-neutral {
  @apply bg-slate-700 hover:bg-slate-600 text-slate-200;
}
.new-proposal-modal .btn-primary {
  @apply bg-indigo-600 hover:bg-indigo-700 text-white;
}
/* tighten grid gaps globally in modal */
.new-proposal-modal .grid {
  gap: 0.5rem;
}

/* Harmonize BaseInput (xs) with modal dark compact style */
.new-proposal-modal .input-label {
  @apply text-[10px] text-slate-300;
}
.new-proposal-modal .input-container--xs {
  @apply h-7;
}
.new-proposal-modal .input-container--default {
  @apply bg-slate-700/70 border border-slate-600/60 rounded-sm;
}
.new-proposal-modal .input-container--focused {
  @apply ring-[0.5px] ring-indigo-500/60 border-indigo-400/50;
}
.new-proposal-modal .input-field {
  @apply text-slate-200 placeholder-gray-400;
}
.new-proposal-modal .input-field--xs {
  @apply text-[12px] leading-tight px-2;
}
/* Ensure selects inside modal always show value */
.new-proposal-modal select {
  color: #e5e7eb !important;
  -webkit-text-fill-color: #e5e7eb !important;
}
.new-proposal-modal option {
  color: #e5e7eb !important;
  background-color: #1f2937 !important;
}

/* ConsorIA Button Styles */
.consoria-btn {
  position: relative;
  box-shadow: 0 4px 14px 0 rgba(147, 51, 234, 0.3);
  transition: all 0.3s ease;
}

.consoria-btn:hover {
  box-shadow: 0 6px 20px 0 rgba(147, 51, 234, 0.5);
  transform: translateY(-1px);
}

.consoria-btn:active {
  transform: translateY(0);
}

/* Floating animation for the pulsing dot */
@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-2px);
  }
}

.consoria-btn .animate-pulse {
  animation:
    pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite,
    float 3s ease-in-out infinite;
}

/* Glowing effect on hover */
@keyframes glow {
  0%,
  100% {
    box-shadow: 0 0 5px rgba(147, 51, 234, 0.5);
  }
  50% {
    box-shadow:
      0 0 15px rgba(147, 51, 234, 0.8),
      0 0 25px rgba(147, 51, 234, 0.6);
  }
}

.consoria-btn:hover {
  animation: glow 2s ease-in-out infinite;
}

/* Enhanced shimmer effect */
.consoria-btn .group-hover\:translate-x-full {
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.4) 50%,
    transparent 100%
  );
}
</style>
