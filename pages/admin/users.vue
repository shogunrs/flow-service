<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900"
  >
    <!-- Modern Header with Glass Effect -->
    <header
      class="app-header relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 p-4"
    >
      <div class="absolute inset-0 bg-white/[0.02] backdrop-blur-3xl"></div>
      <div class="relative flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="relative group">
            <div
              class="absolute inset-0 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl blur-lg opacity-50 group-hover:opacity-75 transition-opacity duration-300"
            ></div>
            <div>
              <i class="fa-solid fa-user-group text-white text-lg"></i>
            </div>
          </div>
          <div>
            <p class="app-header-subtitle">Gerencie usuários do sistema</p>
          </div>
        </div>
        <button class="p-2 rounded-xl" @click="openNewUserModal">
          <i class="fa-solid fa-user-plus text-purple-300"></i>
        </button>
      </div>
    </header>

    <main class="p-3">
      <div
        class="bg-slate-900/60 backdrop-blur-xl border border-slate-700/50 rounded-lg shadow-lg"
      >
        <div v-if="usersList.length === 0" class="p-6 text-center">
          <i class="fa-solid fa-users text-sm text-slate-400 mb-2"></i>
          <p class="text-slate-300 text-sm">Nenhum usuário cadastrado</p>
          <p class="text-slate-400 text-xs">
            Clique em "Novo Usuário" para começar
          </p>
        </div>
        <div v-else class="p-4">
          <div
            class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3"
          >
            <div
              v-for="(user, index) in usersList"
              :key="user.id || index"
              class="group relative overflow-hidden rounded-xl border border-slate-800/60 bg-slate-900/80 p-3 transition-all duration-300 hover:border-emerald-500/50 hover:shadow-[0_10px_25px_rgba(16,185,129,0.25)] cursor-pointer"
              :style="{ animationDelay: `${index * 25}ms` }"
              @click="editUser(user)"
            >
              <div
                class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                :style="{
                  background:
                    'radial-gradient(circle at top right, rgba(16, 185, 129, 0.15), transparent 55%)',
                }"
              ></div>

              <div class="relative flex items-start justify-between">
                <div class="space-y-1">
                  <h3 class="text-sm font-semibold text-white truncate">
                    {{ user.name || 'Usuário' }}
                  </h3>
                  <p class="text-xs text-slate-400 truncate">
                    {{ user.email || 'sem email' }}
                  </p>
                </div>
                <div class="flex items-center">
                  <button
                    class="rounded-md border border-slate-700/70 bg-slate-900/80 p-2 text-red-300 hover:text-red-200 hover:border-red-400/60 transition"
                    @click.stop="deleteUser(user.id || '')"
                    title="Excluir"
                  >
                    <i class="fa-solid fa-trash text-[11px]"></i>
                  </button>
                </div>
              </div>

              <div class="relative mt-3 flex flex-wrap gap-1.5">
                <!-- Super User Badge -->
                <span
                  v-if="user.superUser"
                  class="text-[10px] px-2 py-0.5 rounded-full bg-yellow-500/20 text-yellow-300 border border-yellow-500/40 flex items-center gap-1"
                >
                  <i class="fa-solid fa-crown text-[8px]"></i>
                  Super User
                </span>
                <!-- Roles -->
                <span
                  v-for="role in (user.roles?.slice(0, user.superUser ? 1 : 2) || [])"
                  :key="role"
                  class="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700/70"
                >
                  {{ role }}
                </span>
                <span
                  v-if="(user.roles?.length || 0) > (user.superUser ? 1 : 2)"
                  class="text-[10px] px-2 py-0.5 rounded-full bg-slate-800/60 text-slate-400 border border-slate-700/60"
                >
                  +{{ user.roles.length - (user.superUser ? 1 : 2) }}
                </span>
              </div>

              <div class="relative mt-3 space-y-1 text-xs text-slate-300">
                <div class="flex items-center gap-2 truncate">
                  <i class="fa-solid fa-id-card text-slate-500 text-[10px]"></i>
                  <span>
                    {{ user.cpf ? `CPF: ${formatCPF(user.cpf)}` : user.cnpj ? `CNPJ: ${formatCNPJ(user.cnpj)}` : 'Documento não informado' }}
                  </span>
                </div>
                <div class="flex items-center gap-2 truncate">
                  <i class="fa-solid fa-wallet text-slate-500 text-[10px]"></i>
                  <span>
                    {{ user.pixTipo && user.pixChave ? `${user.pixTipo}: ${user.pixChave}` : 'PIX não informado' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal: User Management -->
    <BaseModal
      v-model="showUserModal"
      :title="editingUser ? 'Editar Usuário' : 'Novo Usuário'"
      size="lg"
      :z-index="70"
    >
      <div class="space-y-8 max-h-[75vh] md:max-h-[82vh] overflow-y-auto pr-1">
        <!-- ========== SEÇÃO 1: DADOS PESSOAIS ========== -->
        <div class="relative overflow-hidden rounded-xl border border-slate-800 bg-slate-950/85 p-5">
          <div class="absolute inset-0 bg-gradient-to-br from-emerald-500/12 via-slate-900/0 to-slate-900/0 pointer-events-none"></div>
          <div class="relative space-y-5">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-sm font-semibold text-white flex items-center gap-2">
                <i class="fa-solid fa-user text-emerald-300"></i>
                Informações pessoais
              </h3>
              <p class="text-xs text-slate-400">Dados básicos e de contato do usuário.</p>
            </div>
          </div>

          <!-- Dados básicos -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div class="md:col-span-2">
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Nome completo *</label>
              <input
                v-model="userForm.name"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Nome completo"
              />
            </div>
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Email *</label>
              <div class="relative">
                <input
                  v-model="userForm.email"
                  type="email"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none pr-10"
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
            <div>
              <label class="text-[12px] text-slate-300">Telefone</label>
              <input
                v-model="userForm.telefone"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="(00) 00000-0000"
                v-telefone
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300"
                >Data de Nascimento</label
              >
              <input
                v-model="userForm.dataNascimento"
                type="date"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300"
                >Tipo de Documento de Identidade</label
              >
              <select
                v-model="userForm.tipoDocumento"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
              >
                <option value="">Selecione o tipo</option>
                <option value="rg">RG</option>
                <option value="cnh">CNH</option>
                <option value="passaporte">Passaporte</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-[auto,1fr] items-start gap-6 mb-4">
            <div class="flex flex-col items-center gap-3 mx-auto md:mx-0">
              <div class="relative group">
                <!-- Avatar Preview -->
                <div class="relative">
                  <div
                    :class="[
                      'w-24 h-24 sm:w-28 sm:h-28 rounded-full border-3 transition-all duration-300',
                      profilePreview
                        ? 'border-emerald-400/60 shadow-lg shadow-emerald-500/25'
                        : 'border-slate-600/40',
                      'bg-gradient-to-br from-slate-800/90 to-slate-900/90 flex items-center justify-center overflow-hidden'
                    ]"
                  >
                    <!-- Image Preview -->
                    <img
                      v-if="profilePreview"
                      :src="profilePreview"
                      alt="Prévia da foto de perfil"
                      class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                      @error="onImageError"
                    />
                    <!-- Placeholder Icon -->
                    <div v-else class="flex flex-col items-center gap-1">
                      <i class="fa-solid fa-user text-slate-500 text-3xl"></i>
                      <span class="text-[10px] text-slate-500 font-medium">Sem foto</span>
                    </div>

                    <!-- Loading Overlay -->
                    <div
                      v-if="imageLoading"
                      class="absolute inset-0 bg-slate-900/80 flex items-center justify-center"
                    >
                      <div class="animate-spin rounded-full h-6 w-6 border-2 border-emerald-400 border-t-transparent"></div>
                    </div>
                  </div>

                  <!-- Status Badge -->
                  <div class="absolute -bottom-1 -right-1">
                    <div
                      v-if="profilePreview"
                      class="w-6 h-6 bg-emerald-500 rounded-full flex items-center justify-center border-2 border-slate-900"
                      title="Foto carregada"
                    >
                      <i class="fa-solid fa-check text-white text-xs"></i>
                    </div>
                    <div
                      v-else
                      class="w-6 h-6 bg-slate-600 rounded-full flex items-center justify-center border-2 border-slate-900"
                      title="Nenhuma foto"
                    >
                      <i class="fa-solid fa-camera text-slate-400 text-xs"></i>
                    </div>
                  </div>
                </div>

                <!-- Preview Label -->
                <div class="text-center mt-2">
                  <span class="text-[11px] text-slate-400 font-medium">
                    {{ profilePreview ? 'Foto carregada' : 'Aguardando foto' }}
                  </span>
                </div>
              </div>
              <p class="text-[11px] text-slate-500 text-center max-w-[8rem]">
                Utilize uma foto frontal e bem iluminada.
              </p>
            </div>

            <div class="w-full md:max-w-xs">
              <UserFileUpload
                label="Foto de perfil"
                accept="image/*"
                v-model="userForm.profileImage"
                :user-id="currentUserId || tempUploadSessionId"
                file-type="PROFILE_PHOTO"
                :temporary-mode="!currentUserId"
                @file-selected="onProfileFileSelected"
                @file-uploaded="onProfileFileUploaded"
              />
            </div>
          </div>

          <div
            v-if="userForm.nomeCompleto && userForm.nomeCompleto !== userForm.name"
            class="mb-4 flex flex-wrap items-center justify-between gap-2 rounded-lg border border-emerald-500/40 bg-emerald-500/10 px-3 py-2 text-xs text-emerald-200"
          >
            <div class="flex items-center gap-2">
              <i class="fa-solid fa-wand-magic-sparkles"></i>
              <span>
                Tesseract sugeriu: <strong class="text-emerald-100">{{ userForm.nomeCompleto }}</strong>
              </span>
            </div>
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded-full bg-emerald-500/20 px-3 py-1 font-semibold hover:bg-emerald-500/30"
              @click.stop="applyOcrSuggestion"
            >
              Aplicar sugestão
            </button>
          </div>

          <div class="mt-4">
            <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Observações</label>
            <textarea
              v-model="userForm.observacoes"
              class="mt-2 w-full h-24 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none resize-none"
              placeholder="Notas internas sobre o usuário, validações adicionais, contexto do cadastro..."
            ></textarea>
          </div>

          <!-- Dados de Documentos -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Documentos</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">CPF</label>
                <input
                  v-model="userForm.cpf"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="000.000.000-00"
                  v-cpf
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">RG</label>
                <input
                  v-model="userForm.rg"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="00.000.000-0"
                />
              </div>
            </div>
          </div>

          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Dados bancários</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Banco</label>
                <input
                  v-model="userForm.banco"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Banco"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Agência</label>
                <input
                  v-model="userForm.agencia"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="0000"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Conta</label>
                <input
                  v-model="userForm.conta"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="000000-0"
                />
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mt-3">
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Tipo de conta</label>
                <select
                  v-model="userForm.tipoConta"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                >
                  <option v-for="opt in accountTypeOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Tipo de PIX</label>
                <select
                  v-model="userForm.pixTipo"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                >
                  <option v-for="opt in pixTypeOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Chave PIX</label>
                <input
                  v-model="userForm.pixChave"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="CPF, email, telefone..."
                />
              </div>
            </div>
          </div>

          <!-- Endereço -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">
              Endereço Residencial
            </h4>
          <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">CEP</label>
              <div class="relative">
                <input
                  v-model="userForm.cep"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 pr-10 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="00000-000"
                  v-cep
                />
                <span
                  v-if="personalCepLoading"
                  class="absolute inset-y-0 right-3 flex items-center text-xs text-indigo-400"
                >
                  <i class="fa-solid fa-spinner fa-spin"></i>
                </span>
                <span
                  v-else-if="personalCepError"
                  class="absolute inset-y-0 right-3 flex items-center text-xs text-red-400"
                >
                  <i class="fa-solid fa-triangle-exclamation"></i>
                </span>
                <span
                  v-else-if="personalCepValid"
                  class="absolute inset-y-0 right-3 flex items-center text-xs text-emerald-400"
                >
                  <i class="fa-solid fa-check"></i>
                </span>
              </div>
              <p v-if="personalCepError" class="mt-1 text-[11px] text-red-400">
                {{ personalCepError }}
              </p>
            </div>
            <div class="md:col-span-3">
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Rua</label>
              <input
                v-model="userForm.enderecoRua"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Rua"
              />
            </div>
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Número</label>
              <input
                v-model="userForm.enderecoNumero"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="123"
              />
            </div>
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Complemento</label>
              <input
                v-model="userForm.enderecoComplemento"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Apto, bloco..."
              />
            </div>
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Bairro</label>
              <input
                v-model="userForm.enderecoBairro"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Bairro"
              />
            </div>
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Município</label>
              <input
                v-model="userForm.enderecoCidade"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Cidade"
              />
            </div>
            <div>
              <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">UF</label>
              <input
                v-model="userForm.enderecoEstado"
                maxlength="2"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none uppercase"
                placeholder="UF"
              />
            </div>
          </div>
          </div>

          <!-- Upload de Documentos Pessoais -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">
              Documentos Pessoais
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <UserFileUpload
                label="Documento de identidade (RG/CNH/Passaporte)"
                accept="image/*,application/pdf"
                v-model="userForm.documentoIdentidade"
                :user-id="currentUserId || tempUploadSessionId"
                file-type="DOCUMENT"
                :temporary-mode="!currentUserId"
                :enable-ocr="true"
                :form-data="{ nome: userForm.name, email: userForm.email }"
                @ocr-result="onOcrResult"
              />
              <div class="rounded-lg border border-slate-700/60 bg-slate-900/70 px-4 py-3 text-xs text-slate-300">
                <p class="text-slate-200 font-semibold flex items-center gap-2">
                  <i class="fa-solid fa-lightbulb text-amber-400"></i>
                  Dica de validação
                </p>
                <p class="mt-2">
                  Após o upload, o Tesseract fará a leitura automática do nome e CPF para comparar com as informações digitadas.
                </p>
                <p class="mt-2 text-slate-400">
                  Utilize documentos legíveis, sem cortes, para maximizar a precisão da leitura.
                </p>
              </div>
            </div>
          </div>

          <!-- Acesso ao Sistema -->
          <div class="border-t border-slate-700/50 pt-4">
            <div class="flex items-center justify-between mb-3">
              <h4 class="text-sm font-medium text-slate-300">
                Acesso ao Sistema
              </h4>
              <button
                @click="toggleSuperUser"
                :class="[
                  'group relative px-3 py-2 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                  userForm.superUser
                    ? 'border-yellow-500 bg-yellow-500/10 shadow-lg shadow-yellow-500/25'
                    : 'border-slate-600/30 bg-slate-800/40',
                ]"
                :title="userForm.superUser ? 'Super User Ativo' : 'Ativar Super User'"
              >
                <div class="flex flex-col items-center text-center">
                  <i :class="[
                    'fa-solid fa-crown text-base mb-1',
                    userForm.superUser ? 'text-white' : 'text-slate-400'
                  ]"></i>
                  <div :class="[
                    'text-xs font-medium leading-tight',
                    userForm.superUser ? 'text-white' : 'text-slate-300'
                  ]">
                    Super User
                  </div>
                </div>
                <div
                  v-if="userForm.superUser"
                  class="absolute -top-1 -right-1 w-3 h-3 bg-yellow-500 rounded-full flex items-center justify-center"
                >
                  <i class="fa-solid fa-check text-white text-[6px]"></i>
                </div>
              </button>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-[12px] text-slate-300">
                  Senha
                  {{ editingUser ? "(deixe em branco para manter)" : "*" }}
                </label>
                <input
                  v-model="userForm.password"
                  type="password"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Digite a senha"
                />
              </div>
            </div>
          </div>

          <!-- Permissões -->
          <div class="border-t border-slate-700/50 pt-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Permissões</h4>
            <div class="grid grid-cols-2 gap-3">
              <button
                v-for="role in roleOptions"
                :key="role.value"
                @click="toggleRole(role.value)"
                :class="[
                  'group relative px-3 py-2 rounded-lg border-2 transition-all duration-300 hover:scale-105',
                  userForm.roles.includes(role.value)
                    ? role.activeClass
                    : 'border-slate-600/30 bg-slate-800/40',
                ]"
                :title="userForm.roles.includes(role.value) ? `${role.label} Ativo` : `Ativar ${role.label}`"
              >
                <div class="flex flex-col items-center text-center">
                  <i :class="[
                    'fa-solid',
                    role.icon,
                    'text-base mb-1',
                    userForm.roles.includes(role.value) ? 'text-white' : 'text-slate-400'
                  ]"></i>
                  <div :class="[
                    'text-xs font-medium leading-tight',
                    userForm.roles.includes(role.value) ? 'text-white' : 'text-slate-300'
                  ]">
                    {{ role.label }}
                  </div>
                </div>
                <div
                  v-if="userForm.roles.includes(role.value)"
                  :class="['absolute -top-1 -right-1 w-3 h-3 rounded-full flex items-center justify-center', role.checkClass]"
                >
                  <i class="fa-solid fa-check text-white text-[6px]"></i>
                </div>
              </button>
            </div>
          </div>

          <!-- Espaçamento adicional -->
          <div class="pt-6"></div>
        </div>

        <!-- ========== SEÇÃO 2: DADOS DA EMPRESA ========== -->
        <div class="relative overflow-hidden rounded-xl border border-slate-800 bg-slate-950/85 p-5">
          <div class="absolute inset-0 bg-gradient-to-br from-sky-500/12 via-slate-900/0 to-slate-900/0 pointer-events-none"></div>
          <div class="relative">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h3 class="text-sm font-semibold text-white flex items-center gap-2">
                <i class="fa-solid fa-building text-sky-300"></i>
                Dados da empresa
              </h3>
              <p class="text-xs text-slate-400">Informações jurídicas e endereço empresarial.</p>
            </div>
          </div>

          <!-- Informações da Empresa -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label class="text-[12px] text-slate-300">Razão Social</label>
              <input
                v-model="userForm.razaoSocial"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Razão social da empresa"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Nome Fantasia</label>
              <input
                v-model="userForm.nomeFantasia"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Nome fantasia"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">CNPJ</label>
              <input
                v-model="userForm.cnpj"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="00.000.000/0000-00"
                v-cnpj
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300"
                >Quantidade de Sócios</label
              >
              <select
                v-model="userForm.quantidadeSocios"
                @change="onQuantidadeSociosChange"
                class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
              >
                <option value="">Selecione</option>
                <option v-for="n in 10" :key="n" :value="n">
                  {{ n }} sócio{{ n > 1 ? "s" : "" }}
                </option>
              </select>
            </div>
          </div>

          <!-- Endereço da Empresa -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">
              Endereço da Empresa
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
              <div class="md:col-span-2">
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Rua</label>
                <input
                  v-model="userForm.enderecoEmpresaRua"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Rua"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Número</label>
                <input
                  v-model="userForm.enderecoEmpresaNumero"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="123"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Complemento</label>
                <input
                  v-model="userForm.enderecoEmpresaComplemento"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Sala, conjunto..."
                />
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-4 gap-3 mt-3">
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Bairro</label>
                <input
                  v-model="userForm.enderecoEmpresaBairro"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Bairro"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">Município</label>
                <input
                  v-model="userForm.enderecoEmpresaCidade"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Cidade"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">UF</label>
                <input
                  v-model="userForm.enderecoEmpresaEstado"
                  maxlength="2"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none uppercase"
                  placeholder="UF"
                />
              </div>
              <div>
                <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">CEP</label>
                <div class="relative">
                  <input
                    v-model="userForm.cepEmpresa"
                    class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 pr-10 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                    placeholder="00000-000"
                    v-cep
                  />
                  <span
                    v-if="companyCepLoading"
                    class="absolute inset-y-0 right-3 flex items-center text-xs text-indigo-400"
                  >
                    <i class="fa-solid fa-spinner fa-spin"></i>
                  </span>
                  <span
                    v-else-if="companyCepError"
                    class="absolute inset-y-0 right-3 flex items-center text-xs text-red-400"
                  >
                    <i class="fa-solid fa-triangle-exclamation"></i>
                  </span>
                  <span
                    v-else-if="companyCepValid"
                    class="absolute inset-y-0 right-3 flex items-center text-xs text-emerald-400"
                  >
                    <i class="fa-solid fa-check"></i>
                  </span>
                </div>
                <p v-if="companyCepError" class="mt-1 text-[11px] text-red-400">
                  {{ companyCepError }}
                </p>
              </div>
            </div>
          </div>

          <div class="mt-4">
            <label class="text-[11px] font-medium text-slate-300 uppercase tracking-wide">
              Observações internas
            </label>
            <textarea
              v-model="userForm.observacoesEmpresa"
              class="mt-2 w-full h-24 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none resize-none"
              placeholder="Observações sobre o relacionamento com a empresa, condições comerciais, etc."
            ></textarea>
          </div>

          <!-- Documentos da Empresa -->
          <div class="border-t border-slate-700/50 pt-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">
              Documentos da Empresa
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <UserFileUpload
                  label="Cartão CNPJ"
                  accept="image/*,application/pdf"
                  v-model="userForm.cartaoCnpjImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="cnpjImage"
                  :temporary-mode="!currentUserId"
                />
              </div>
              <div>
                <UserFileUpload
                  label="Contrato Social"
                  accept="image/*,application/pdf"
                  v-model="userForm.contratoSocialImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="enderecoImage"
                  :temporary-mode="!currentUserId"
                />
              </div>
            </div>
            <div v-if="userForm.quantidadeSocios > 1" class="mt-4">
              <div class="max-w-md">
                <UserFileUpload
                  label="Qualificação dos Sócios"
                  accept="image/*,application/pdf"
                  v-model="userForm.qualificacaoSociosImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="bancoImage"
                  :temporary-mode="!currentUserId"
                />
              </div>
            </div>
          </div>
          </div>
        </div>

        <!-- ========== SEÇÃO 3: DADOS DOS SÓCIOS (DINÂMICA) ========== -->
        <div
          v-if="userForm.quantidadeSocios > 1"
          class="relative overflow-hidden rounded-xl border border-slate-800 bg-slate-950/85 p-5"
        >
          <div class="absolute inset-0 bg-gradient-to-br from-purple-500/12 via-slate-900/0 to-slate-900/0 pointer-events-none"></div>
          <div class="relative">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h3 class="text-sm font-semibold text-white flex items-center gap-2">
                <i class="fa-solid fa-users text-purple-300"></i>
                Sócios adicionais ({{ userForm.quantidadeSocios - 1 }})
              </h3>
              <p class="text-xs text-slate-400">Cadastre os dados e documentos dos demais sócios.</p>
            </div>
          </div>

          <div
            v-for="(socio, index) in socios"
            :key="index"
            class="border border-slate-700/50 rounded-lg p-4 mb-4"
          >
            <h4
              class="text-md font-medium text-slate-300 mb-3 flex items-center justify-between"
            >
              <span>
                <i class="fa-solid fa-user-tie mr-2 text-purple-400"></i>
                Sócio {{ index + 2 }}
              </span>
              <button
                @click="removeSocio(index)"
                class="text-red-400 hover:text-red-300 p-1"
                title="Remover sócio"
              >
                <i class="fa-solid fa-trash text-xs"></i>
              </button>
            </h4>

            <!-- Dados pessoais do sócio -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
              <div class="md:col-span-2">
                <label class="text-[12px] text-slate-300"
                  >Nome Completo *</label
                >
                <input
                  v-model="socio.nome"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Nome completo do sócio"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Email</label>
                <input
                  v-model="socio.email"
                  type="email"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="email@exemplo.com"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Telefone</label>
                <input
                  v-model="socio.telefone"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="(00) 00000-0000"
                  v-telefone
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300"
                  >Data de Nascimento</label
                >
                <input
                  v-model="socio.dataNascimento"
                  type="date"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300"
                  >Tipo de Documento</label
                >
                <select
                  v-model="socio.tipoDocumento"
                  class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                >
                  <option value="">Selecione o tipo</option>
                  <option value="rg">RG</option>
                  <option value="cnh">CNH</option>
                  <option value="passaporte">Passaporte</option>
                </select>
              </div>
            </div>

            <!-- Endereço do sócio -->
            <div class="border-t border-slate-700/50 pt-4 mb-4">
              <h5 class="text-sm font-medium text-slate-300 mb-3">
                Endereço do Sócio
              </h5>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="md:col-span-2">
                  <label class="text-[12px] text-slate-300"
                    >Endereço Completo</label
                  >
                  <textarea
                    v-model="socio.endereco"
                    class="mt-2 w-full h-20 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none resize-none"
                    placeholder="Endereço do sócio"
                  ></textarea>
                </div>
                <div>
                  <label class="text-[12px] text-slate-300">CEP</label>
                  <input
                    v-model="socio.cep"
                    class="mt-2 w-full h-10 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                    placeholder="00000-000"
                    v-cep
                  />
                </div>
              </div>
            </div>

            <!-- Documentos do sócio -->
            <div class="border-t border-slate-700/50 pt-4">
              <h5 class="text-sm font-medium text-slate-300 mb-3">
                Documentos do Sócio
              </h5>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <UserFileUpload
                    :label="`Documento de Identidade - Sócio ${index + 2}`"
                    accept="image/*,application/pdf"
                    v-model="socio.documentoIdentidade"
                    :user-id="currentUserId || tempUploadSessionId"
                    file-type="DOCUMENT"
                    :temporary-mode="!currentUserId"
                    :enable-ocr="true"
                    :form-data="{ nome: socio.nome, email: socio.email }"
                    @ocr-result="(result) => onSocioOcrResult(result, index)"
                  />
                </div>
                <div>
                  <UserFileUpload
                    :label="`Foto de Perfil - Sócio ${index + 2}`"
                    accept="image/*"
                    v-model="socio.profileImage"
                    :user-id="currentUserId || tempUploadSessionId"
                    file-type="PROFILE_PHOTO"
                    :temporary-mode="!currentUserId"
                  />
                </div>
              </div>
            </div>
          </div>
          </div>
        </div>
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
            class="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-2 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
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
import { ref, onMounted, computed, watch } from "vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import UserFileUpload from "~/components/ui/UserFileUpload.vue";
import {
  fetchUsersApi,
  updateUserApi,
  deleteUserApi,
  checkEmailAvailabilityApi,
  createBasicUserApi,
} from "~/composables/useUsersApi";
import { useGeolocation } from "~/composables/useGeolocation";
import { useToast } from "~/composables/useToast";

definePageMeta({
  layout: "sidebar",
  title: "Usuários - Admin",
});

// Composables
const { getCurrentLocation, formatLocation } = useGeolocation();
const { success: toastSuccess, error: toastError } = useToast();

// Users state
const usersList = ref([]);
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
  telefone: "",
  endereco: "",
  enderecoRua: "",
  enderecoNumero: "",
  enderecoComplemento: "",
  enderecoBairro: "",
  enderecoCidade: "",
  enderecoEstado: "",
  banco: "",
  agencia: "",
  conta: "",
  tipoConta: "",
  pixTipo: "",
  pixChave: "",
  profileImage: null,
  cpfImage: null,
  rgImage: null,
  cnpjImage: null,
  enderecoImage: null,
  telefoneImage: null,
  bancoImage: null,
  pixImage: null,
  location: null,
  observacoes: "",

  // Novos campos para OCR
  sobrenome: "",
  nomeCompleto: "",
  numeroCnh: "",
  validadeCnh: "",
  dataNascimento: "",
  localNascimento: "",
  nacionalidade: "",
  nomePai: "",
  nomeMae: "",
  numeroIdentidade: "",
  orgaoEmissor: "",
  dataEmissao: "",
  razaoSocial: "",

  // Novos campos para estrutura reorganizada
  tipoDocumento: "",
  cep: "",
  nomeFantasia: "",
  quantidadeSocios: "",
  enderecoEmpresa: "",
  enderecoEmpresaRua: "",
  enderecoEmpresaNumero: "",
  enderecoEmpresaComplemento: "",
  enderecoEmpresaBairro: "",
  enderecoEmpresaCidade: "",
  enderecoEmpresaEstado: "",
  cepEmpresa: "",
  observacoesEmpresa: "",
  documentoIdentidade: null,
  cartaoCnpjImage: null,
  contratoSocialImage: null,
  qualificacaoSociosImage: null,
  superUser: false,
});

const editingUser = ref(false);
const allowEmailEdit = ref(false);

// Email validation
const emailAvailable = ref(null);
const emailCheckLoading = ref(false);
const emailError = ref("");

// ID para upload de arquivos (usa o ID real do usuário ou um temporário)
const currentUserId = computed(() => {
  return userForm.value.id;
});

// Session ID para uploads temporários durante criação de usuário
const tempUploadSessionId = ref(null);

// Estados para preview da imagem
const imageLoading = ref(false);
const imageError = ref(false);
const imageUploaded = ref(false);

const profilePreview = computed(() => {
  const img = userForm.value.profileImage;
  if (!img) return '';

  // Se é uma string (URL), usa diretamente
  if (typeof img === 'string') {
    return img;
  }

  // Se é um objeto (resposta do upload), busca a URL
  if (typeof img === 'object' && img !== null) {
    return img.previewUrl || img.url || img.secure_url || img.publicUrl || img.thumbnailUrl || '';
  }

  return '';
});

// Função para lidar com erro de carregamento da imagem
function onImageError() {
  imageError.value = true;
  imageLoading.value = false;
  console.warn('Erro ao carregar preview da imagem de perfil');
}

// Função chamada quando arquivo é selecionado (antes do upload)
function onProfileFileSelected(file) {
  imageLoading.value = true;
  imageError.value = false;
  imageUploaded.value = false;

  // Criar preview temporário com File objeto
  if (file && file instanceof File) {
    const reader = new FileReader();
    reader.onload = (e) => {
      // Temporariamente atualizar o preview com o arquivo local
      const tempPreview = {
        previewUrl: e.target.result,
        name: file.name,
        size: file.size,
        isTemp: true
      };
      userForm.value.profileImage = tempPreview;
      imageLoading.value = false;
    };
    reader.readAsDataURL(file);
  }
}

// Função chamada quando upload é concluído
function onProfileFileUploaded(uploadData) {
  imageLoading.value = false;
  imageError.value = false;
  imageUploaded.value = true;

  console.log('Upload concluído:', uploadData);

  // Atualizar com os dados reais do upload
  userForm.value.profileImage = uploadData;

  // Mostrar notificação de sucesso
  toastSuccess('Foto de perfil enviada com sucesso!');
}


// Watch para resetar estados quando mudar a imagem
watch(() => userForm.value.profileImage, (newValue) => {
  // Se mudou para uma string/URL válida, resetar estados
  if ((typeof newValue === 'string' && newValue) ||
      (typeof newValue === 'object' && newValue && newValue.previewUrl)) {
    imageError.value = false;
    imageLoading.value = false;
    imageUploaded.value = true;
  }
  // Se foi limpo, resetar tudo
  else if (!newValue) {
    imageError.value = false;
    imageLoading.value = false;
    imageUploaded.value = false;
  }
});

const personalCepLoading = ref(false);
const personalCepError = ref('');
const lastPersonalCep = ref('');

const companyCepLoading = ref(false);
const companyCepError = ref('');
const lastCompanyCep = ref('');

watch(
  () => userForm.value.cep,
  (val) => {
    const digits = String(val || '').replace(/\D/g, '');
    if (
      digits.length === 8 &&
      digits !== lastPersonalCep.value &&
      !personalCepLoading.value
    ) {
      lastPersonalCep.value = digits;
      fetchPersonalCep();
    }
    if (digits.length < 8) {
      personalCepError.value = '';
      lastPersonalCep.value = '';
    }
  }
);

watch(
  () => userForm.value.cepEmpresa,
  (val) => {
    const digits = String(val || '').replace(/\D/g, '');
    if (
      digits.length === 8 &&
      digits !== lastCompanyCep.value &&
      !companyCepLoading.value
    ) {
      lastCompanyCep.value = digits;
      fetchCompanyCep();
    }
    if (digits.length < 8) {
      companyCepError.value = '';
      lastCompanyCep.value = '';
    }
  }
);

const personalCepValid = computed(() => {
  const digits = String(userForm.value.cep || '').replace(/\D/g, '');
  return digits.length === 8 && !personalCepLoading.value && !personalCepError.value;
});

const companyCepValid = computed(() => {
  const digits = String(userForm.value.cepEmpresa || '').replace(/\D/g, '');
  return digits.length === 8 && !companyCepLoading.value && !companyCepError.value;
});

const applyAddressData = (target, res, base = 'endereco') => {
  if (!target || !base) return;
  const set = (suffix, value) => {
    const key = `${base}${suffix}`;
    if (suffix === '') {
      target[base] = value;
    } else {
      target[key] = value;
    }
  };

  set('Rua', res.street || '');
  set('Bairro', res.neighborhood || '');
  set('Cidade', res.city || '');
  set('Estado', res.state || '');

  const numeroKey = `${base}Numero`;
  const complementoKey = `${base}Complemento`;
  if (target[numeroKey] === undefined) target[numeroKey] = '';
  if (target[complementoKey] === undefined) target[complementoKey] = '';

  const composed = [res.street, res.neighborhood, res.city && res.state ? `${res.city}/${res.state}` : res.city, !res.city && res.state ? res.state : '']
    .filter(Boolean)
    .join(', ');
  set('', composed);
};

const fetchPersonalCep = async () => {
  personalCepError.value = '';
  const digits = String(userForm.value.cep || '').replace(/\D/g, '');
  if (digits.length !== 8) {
    personalCepError.value = 'Informe os 8 dígitos do CEP';
    return;
  }
  personalCepLoading.value = true;
  try {
    const res = await $fetch(`/api/cep?cep=${digits}`);
    if (res) {
      applyAddressData(userForm.value, res, 'endereco');
    }
  } catch (error) {
    personalCepError.value =
      error?.statusMessage || error?.message || 'CEP não encontrado';
  } finally {
    personalCepLoading.value = false;
  }
};

const fetchCompanyCep = async () => {
  companyCepError.value = '';
  const digits = String(userForm.value.cepEmpresa || '').replace(/\D/g, '');
  if (digits.length !== 8) {
    companyCepError.value = 'Informe os 8 dígitos do CEP';
    return;
  }
  companyCepLoading.value = true;
  try {
    const res = await $fetch(`/api/cep?cep=${digits}`);
    if (res) {
      applyAddressData(userForm.value, res, 'enderecoEmpresa');
    }
  } catch (error) {
    companyCepError.value =
      error?.statusMessage || error?.message || 'CEP não encontrado';
  } finally {
    companyCepLoading.value = false;
  }
};

// Array de sócios dinâmicos
const socios = ref([]);

// Role options
const roleOptions = [
  {
    value: "admin",
    label: "Administrador",
    icon: "fa-shield-halved",
    activeClass: "border-red-500 bg-red-500/10 shadow-lg shadow-red-500/25",
    checkClass: "bg-red-500"
  },
  {
    value: "user",
    label: "Usuário",
    icon: "fa-user",
    activeClass: "border-blue-500 bg-blue-500/10 shadow-lg shadow-blue-500/25",
    checkClass: "bg-blue-500"
  },
  {
    value: "manager",
    label: "Gerente",
    icon: "fa-briefcase",
    activeClass: "border-emerald-500 bg-emerald-500/10 shadow-lg shadow-emerald-500/25",
    checkClass: "bg-emerald-500"
  },
  {
    value: "analyst",
    label: "Analista",
    icon: "fa-chart-line",
    activeClass: "border-purple-500 bg-purple-500/10 shadow-lg shadow-purple-500/25",
    checkClass: "bg-purple-500"
  },
];

// PIX type options
const pixTypeOptions = [
  { value: "cpf", label: "CPF" },
  { value: "cnpj", label: "CNPJ" },
  { value: "email", label: "Email" },
  { value: "telefone", label: "Telefone" },
  { value: "chave_aleatoria", label: "Chave Aleatória" },
];

// Account type options
const accountTypeOptions = [
  { value: "corrente", label: "Conta Corrente" },
  { value: "poupanca", label: "Conta Poupança" },
  { value: "salario", label: "Conta Salário" },
];

// Functions
async function loadUsersList() {
  try {
    console.log("Loading users list...");
    usersList.value = await fetchUsersApi();
    console.log("Users list loaded:", usersList.value.length, "items");
  } catch (error) {
    console.error("Error loading users:", error);
    toastError("Erro ao carregar lista de usuários");
  }
}

async function openNewUserModal() {
  // Capturar localização ao criar novo usuário
  let location = null;
  try {
    const locationData = await getCurrentLocation();
    location = formatLocation(locationData);
  } catch (error) {
    console.warn("Could not get location:", error);
  }

  // Gerar session ID temporário para uploads
  tempUploadSessionId.value = `temp_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

  userForm.value = {
    id: null,
    name: "",
    email: "",
    password: "",
    roles: ["user"],
    cpf: "",
    cnpj: "",
    rg: "",
    telefone: "",
    endereco: "",
    enderecoRua: "",
    enderecoNumero: "",
    enderecoComplemento: "",
    enderecoBairro: "",
    enderecoCidade: "",
    enderecoEstado: "",
    banco: "",
    agencia: "",
    conta: "",
    tipoConta: "corrente",
    pixTipo: "cpf",
    pixChave: "",
    profileImage: null,
    cpfImage: null,
    rgImage: null,
    cnpjImage: null,
    enderecoImage: null,
    telefoneImage: null,
    bancoImage: null,
    pixImage: null,
    location,
    observacoes: "",
    nomeCompleto: "",
    observacoesEmpresa: "",
    enderecoEmpresaRua: "",
    enderecoEmpresaNumero: "",
    enderecoEmpresaComplemento: "",
    enderecoEmpresaBairro: "",
    enderecoEmpresaCidade: "",
    enderecoEmpresaEstado: "",
    enderecoEmpresa: "",
    cepEmpresa: "",
    quantidadeSocios: "",
    razaoSocial: "",
    nomeFantasia: "",
    cartaoCnpjImage: null,
    contratoSocialImage: null,
    qualificacaoSociosImage: null,
    documentoIdentidade: null,
    superUser: false,
  };
  editingUser.value = false;
  allowEmailEdit.value = false;
  emailAvailable.value = null;
  emailError.value = "";
  personalCepError.value = '';
  companyCepError.value = '';
  personalCepLoading.value = false;
  companyCepLoading.value = false;
  lastPersonalCep.value = '';
  lastCompanyCep.value = '';

  // Resetar estados da imagem para novo usuário
  imageError.value = false;
  imageLoading.value = false;
  imageUploaded.value = false;

  showUserModal.value = true;
}

function applyOcrSuggestion() {
  if (!userForm.value.nomeCompleto) return;
  const suggestion = userForm.value.nomeCompleto.trim();
  userForm.value.name = suggestion;
  userForm.value.nomeCompleto = suggestion;
  toastSuccess('Nome atualizado com sugestão do OCR');
}

async function editUser(user) {
  userForm.value = {
    id: user.id,
    name: user.name,
    email: user.email,
    password: "",
    roles: user.roles || ["user"],
    cpf: user.cpf || "",
    cnpj: user.cnpj || "",
    rg: user.rg || "",
    telefone: user.telefone || "",
    endereco: user.endereco || "",
    enderecoRua: user.enderecoRua || "",
    enderecoNumero: user.enderecoNumero || "",
    enderecoComplemento: user.enderecoComplemento || "",
    enderecoBairro: user.enderecoBairro || "",
    enderecoCidade: user.enderecoCidade || "",
    enderecoEstado: user.enderecoEstado || "",
    cep: user.cep || "",
    banco: user.banco || "",
    agencia: user.agencia || "",
    conta: user.conta || "",
    tipoConta: user.tipoConta || "corrente",
    pixTipo: user.pixTipo || "cpf",
    pixChave: user.pixChave || "",
    profileImage: user.fotoPerfilUrl || user.profileImage,
    cpfImage: user.cpfImage,
    rgImage: user.rgImage,
    cnpjImage: user.cnpjImage,
    enderecoImage: user.enderecoImage,
    telefoneImage: user.telefoneImage,
    bancoImage: user.bancoImage,
    pixImage: user.pixImage,
    location: user.location,
    observacoes: user.observacoes || "",
    nomeCompleto: user.nomeCompleto || "",
    documentoIdentidade: user.documentoIdentidade || null,
    quantidadeSocios: user.quantidadeSocios || "",
    enderecoEmpresa: user.enderecoEmpresa || "",
    enderecoEmpresaRua: user.enderecoEmpresaRua || "",
    enderecoEmpresaNumero: user.enderecoEmpresaNumero || "",
    enderecoEmpresaComplemento: user.enderecoEmpresaComplemento || "",
    enderecoEmpresaBairro: user.enderecoEmpresaBairro || "",
    enderecoEmpresaCidade: user.enderecoEmpresaCidade || "",
    enderecoEmpresaEstado: user.enderecoEmpresaEstado || "",
    cepEmpresa: user.cepEmpresa || "",
    observacoesEmpresa: user.observacoesEmpresa || "",
    razaoSocial: user.razaoSocial || "",
    nomeFantasia: user.nomeFantasia || "",
    cartaoCnpjImage: user.cartaoCnpjImage || null,
    contratoSocialImage: user.contratoSocialImage || null,
    qualificacaoSociosImage: user.qualificacaoSociosImage || null,
    superUser: user.superUser || false,
  };
  editingUser.value = true;
  allowEmailEdit.value = false;
  emailAvailable.value = null;
  emailError.value = "";
  personalCepError.value = '';
  companyCepError.value = '';
  lastPersonalCep.value = '';
  lastCompanyCep.value = '';

  // Buscar URL presigned para a foto se existir
  await loadUserProfilePhotoUrl(user.fotoPerfilUrl);

  showUserModal.value = true;
}

async function saveUser() {
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
      telefone,
    endereco,
    enderecoRua,
    enderecoNumero,
    enderecoComplemento,
    enderecoBairro,
    enderecoCidade,
    enderecoEstado,
    banco,
    agencia,
    conta,
    tipoConta,
    pixTipo,
    pixChave,
    profileImage,
    cpfImage,
    rgImage,
    cnpjImage,
    enderecoImage,
    telefoneImage,
    bancoImage,
    pixImage,
    location,
    cep,
    observacoes,
    nomeCompleto,
    quantidadeSocios,
    enderecoEmpresa,
    enderecoEmpresaRua,
    enderecoEmpresaNumero,
    enderecoEmpresaComplemento,
    enderecoEmpresaBairro,
    enderecoEmpresaCidade,
    enderecoEmpresaEstado,
    cepEmpresa,
    observacoesEmpresa,
    razaoSocial,
    nomeFantasia,
    cartaoCnpjImage,
    contratoSocialImage,
    qualificacaoSociosImage,
    documentoIdentidade,
    superUser,
  } = userForm.value;

    // Validação básica
    if (!name.trim() || !email.trim()) {
      toastError("Nome e email são obrigatórios");
      return;
    }

    // Para novos usuários, verificar se já tem ID (criado via create-basic)
    if (!editingUser.value && !id) {
      toastError("Usuário deve ser criado primeiro. Verifique nome e email.");
      return;
    }

    if (emailAvailable.value === false) {
      toastError("Email já está em uso");
      return;
    }

    const enderecoCompleto = [
      enderecoRua,
      enderecoNumero,
      enderecoComplemento,
      enderecoBairro,
      enderecoCidade && enderecoEstado ? `${enderecoCidade}/${enderecoEstado}` : enderecoCidade,
      !enderecoCidade && enderecoEstado ? enderecoEstado : ''
    ].filter(Boolean).join(', ');

    const enderecoEmpresaCompleto = [
      enderecoEmpresaRua,
      enderecoEmpresaNumero,
      enderecoEmpresaComplemento,
      enderecoEmpresaBairro,
      enderecoEmpresaCidade && enderecoEmpresaEstado ? `${enderecoEmpresaCidade}/${enderecoEmpresaEstado}` : enderecoEmpresaCidade,
      !enderecoEmpresaCidade && enderecoEmpresaEstado ? enderecoEmpresaEstado : ''
    ].filter(Boolean).join(', ');

    const toNullableString = (value) => {
      if (typeof value !== 'string') {
        return value ?? null;
      }
      const trimmed = value.trim();
      return trimmed.length ? trimmed : null;
    };

    const normalizedRoles = Array.isArray(roles)
      ? roles
          .map((role) => (role == null ? null : String(role).trim()))
          .filter((role) => role && role.length > 0)
      : [];

    const sociosNumber =
      quantidadeSocios && !Number.isNaN(Number(quantidadeSocios))
        ? Number(quantidadeSocios)
        : null;

    const userData = {
      name: name.trim(),
      email: email.trim().toLowerCase(),
      roles: normalizedRoles.length ? normalizedRoles : ["user"],
      superUser: Boolean(superUser),
      cpf: toNullableString(cpf),
      cnpj: toNullableString(cnpj),
      rg: toNullableString(rg),
      telefone: toNullableString(telefone),
      endereco: toNullableString(enderecoCompleto || endereco),
      enderecoRua: toNullableString(enderecoRua),
      enderecoNumero: toNullableString(enderecoNumero),
      enderecoComplemento: toNullableString(enderecoComplemento),
      enderecoBairro: toNullableString(enderecoBairro),
      enderecoCidade: toNullableString(enderecoCidade),
      enderecoEstado: toNullableString(enderecoEstado),
      cep: toNullableString(cep),
      banco: toNullableString(banco),
      agencia: toNullableString(agencia),
      conta: toNullableString(conta),
      tipoConta: toNullableString(tipoConta),
      pixTipo: toNullableString(pixTipo),
      pixChave: toNullableString(pixChave),
      profileImage: profileImage || null,
      fotoPerfilUrl: profileImage || null,
      comprovanteEnderecoUrl: enderecoImage || null,
      documentoIdentidade: documentoIdentidade || null,
      cpfImage: cpfImage || null,
      rgImage: rgImage || null,
      cnpjImage: cnpjImage || null,
      enderecoImage: enderecoImage || null,
      telefoneImage: telefoneImage || null,
      bancoImage: bancoImage || null,
      pixImage: pixImage || null,
      location: toNullableString(location),
      observacoes: toNullableString(observacoes),
      nomeCompleto: toNullableString(nomeCompleto),
      quantidadeSocios: sociosNumber,
      enderecoEmpresa: toNullableString(enderecoEmpresaCompleto || enderecoEmpresa),
      enderecoEmpresaRua: toNullableString(enderecoEmpresaRua),
      enderecoEmpresaNumero: toNullableString(enderecoEmpresaNumero),
      enderecoEmpresaComplemento: toNullableString(enderecoEmpresaComplemento),
      enderecoEmpresaBairro: toNullableString(enderecoEmpresaBairro),
      enderecoEmpresaCidade: toNullableString(enderecoEmpresaCidade),
      enderecoEmpresaEstado: toNullableString(enderecoEmpresaEstado),
      cepEmpresa: toNullableString(cepEmpresa),
      observacoesEmpresa: toNullableString(observacoesEmpresa),
      razaoSocial: toNullableString(razaoSocial),
      nomeFantasia: toNullableString(nomeFantasia),
      cartaoCnpjImage: cartaoCnpjImage || null,
      contratoSocialImage: contratoSocialImage || null,
      qualificacaoSociosImage: qualificacaoSociosImage || null,
      localizacaoCadastro: toNullableString(location),
    };

    // Adiciona senha apenas se fornecida
    if (password.trim()) {
      userData.password = password.trim();
    }

    if (editingUser.value) {
      await updateUserApi(id, userData);
      toastSuccess("Usuário atualizado com sucesso");
    } else {
      // Para novos usuários, usar updateUserApi com o ID já criado
      await updateUserApi(id, userData);
      toastSuccess("Usuário atualizado com sucesso");
    }

    await loadUsersList();
    closeUserModal();
  } catch (error) {
    console.error("Erro ao salvar usuário:", error);
    toastError(
      editingUser.value ? "Erro ao atualizar usuário" : "Erro ao salvar usuário"
    );
  }
}

async function deleteUser(userId) {
  if (!confirm("Tem certeza que deseja excluir este usuário?")) {
    return;
  }

  try {
    await deleteUserApi(userId);
    await loadUsersList();
    toastSuccess("Usuário excluído com sucesso");
  } catch (error) {
    console.error("Erro ao excluir usuário:", error);
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
    telefone: "",
    endereco: "",
    banco: "",
    agencia: "",
    conta: "",
    tipoConta: "",
    pixTipo: "",
    pixChave: "",
    profileImage: null,
    cpfImage: null,
    rgImage: null,
    cnpjImage: null,
    enderecoImage: null,
    telefoneImage: null,
    bancoImage: null,
    pixImage: null,
    location: null,

    // Novos campos para OCR
    sobrenome: "",
    nomeCompleto: "",
    numeroCnh: "",
    validadeCnh: "",
    dataNascimento: "",
    localNascimento: "",
    nacionalidade: "",
    nomePai: "",
    nomeMae: "",
    numeroIdentidade: "",
    orgaoEmissor: "",
    dataEmissao: "",
    razaoSocial: "",
  };
  editingUser.value = false;
  allowEmailEdit.value = false;
  emailAvailable.value = null;
  emailError.value = "";
  tempUploadSessionId.value = null;
}

// Create basic user when email is validated
async function createBasicUser() {
  const name = userForm.value.name.trim();
  const email = userForm.value.email.trim();

  if (!name || !email || emailAvailable.value !== true) {
    return;
  }

  try {
    const response = await createBasicUserApi(name, email);

    if (response?.id) {
      // Usuário criado com sucesso - agora podemos usar uploads normais
      userForm.value.id = response.id;

      toastSuccess(`Usuário ${name} salvo. Continue preenchendo os dados.`);

      console.log("Usuário básico criado:", response);
    }
  } catch (error) {
    console.error("Erro ao criar usuário básico:", error);
    toastError("Erro ao salvar usuário. Tente novamente.");
  }
}

// Email validation functions
function onEmailInput() {
  emailAvailable.value = null;
  emailError.value = "";
}

async function checkEmailAvailability() {
  const email = userForm.value.email.trim();
  if (!email) {
    emailAvailable.value = null;
    emailError.value = "";
    return;
  }

  // Skip check if editing user and email hasn't changed
  if (editingUser.value && !allowEmailEdit.value) {
    return;
  }

  emailCheckLoading.value = true;
  emailError.value = "";

  try {
    const available = await checkEmailAvailabilityApi(
      email,
      editingUser.value ? userForm.value.id : null
    );
    emailAvailable.value = available;
    if (!available) {
      emailError.value = "Este email já está em uso";
    } else {
      // Se temos nome e email válidos, criar usuário básico automaticamente
      const name = userForm.value.name.trim();
      if (name && name.length >= 2 && !editingUser.value) {
        await createBasicUser();
      }
    }
  } catch (error) {
    console.error("Erro ao verificar disponibilidade do email:", error);
    emailError.value = "Erro ao verificar email";
    emailAvailable.value = null;
  } finally {
    emailCheckLoading.value = false;
  }
}

// OCR Result Handler - Inteligente para múltiplos tipos de documento
// Função para gerenciar quantidade de sócios
function onQuantidadeSociosChange() {
  const quantidade = parseInt(userForm.value.quantidadeSocios) || 0;

  // Ajusta o array de sócios conforme a quantidade selecionada
  // O primeiro sócio é o próprio usuário principal, então criamos quantidade - 1
  const sociosNecessarios = Math.max(0, quantidade - 1);

  // Remove sócios extras
  if (socios.value.length > sociosNecessarios) {
    socios.value = socios.value.slice(0, sociosNecessarios);
  }

  // Adiciona novos sócios se necessário
  while (socios.value.length < sociosNecessarios) {
    socios.value.push({
      nome: "",
      email: "",
      telefone: "",
      dataNascimento: "",
      tipoDocumento: "",
      endereco: "",
      cep: "",
      documentoIdentidade: null,
      profileImage: null,
    });
  }
}

// Função para remover um sócio específico
function removeSocio(index) {
  socios.value.splice(index, 1);
  // Atualiza a quantidade de sócios no formulário
  userForm.value.quantidadeSocios = socios.value.length + 1;
}

// Função para lidar com resultados de OCR dos sócios
function onSocioOcrResult(result, socioIndex) {
  if (!result.extractedData || !socios.value[socioIndex]) return;

  const socio = socios.value[socioIndex];
  const data = result.extractedData;

  // Preenche os dados do sócio com os dados extraídos do OCR
  if (data.nome && !socio.nome) {
    socio.nome = data.nome;
  }

  if (data.dataNascimento && !socio.dataNascimento) {
    socio.dataNascimento = data.dataNascimento;
  }

  console.log(`🔍 OCR Sócio ${socioIndex + 2}:`, data);
}

function onOcrResult(result) {
  console.log("🔍 OCR Result received:", {
    documentType: result.documentType,
    confidence: result.confidence,
    extractedData: result.extractedData,
  });

  if (!result.extractedData) {
    console.warn("❌ Nenhum dado extraído do documento");
    return;
  }

  const extractedFields = [];
  const data = result.extractedData;

  // ============ DADOS PESSOAIS BÁSICOS ============

  // Nome e sobrenome
  if (data.nome && !userForm.value.name) {
    userForm.value.name = data.nome.trim();
    extractedFields.push("nome");
    console.log("👤 Nome preenchido:", data.nome);
  }

  if (data.sobrenome && !userForm.value.sobrenome) {
    userForm.value.sobrenome = data.sobrenome.trim();
    extractedFields.push("sobrenome");
    console.log("👤 Sobrenome preenchido:", data.sobrenome);
  }

  if (data.nomeCompleto && !userForm.value.nomeCompleto) {
    userForm.value.nomeCompleto = data.nomeCompleto.trim();
    // Se nome completo foi extraído mas nome individual não, preencher o nome principal
    if (!userForm.value.name) {
      const nameParts = data.nomeCompleto.split(" ");
      userForm.value.name = nameParts[0];
      extractedFields.push("nome completo");
    }
    console.log("👤 Nome completo preenchido:", data.nomeCompleto);
  }

  // CPF
  if (data.cpf && !userForm.value.cpf) {
    userForm.value.cpf = data.cpf.trim();
    extractedFields.push("CPF");
    console.log("📄 CPF preenchido:", data.cpf);
  }

  // RG
  if (data.rg && !userForm.value.rg) {
    userForm.value.rg = data.rg.trim();
    extractedFields.push("RG");
    console.log("🆔 RG preenchido:", data.rg);
  }

  if (data.numeroIdentidade && !userForm.value.numeroIdentidade) {
    userForm.value.numeroIdentidade = data.numeroIdentidade.trim();
    extractedFields.push("número identidade");
    console.log("🆔 Número identidade preenchido:", data.numeroIdentidade);
  }

  // ============ DOCUMENTOS ESPECÍFICOS ============

  // CNH - Carteira Nacional de Habilitação
  if (data.numeroCnh && !userForm.value.numeroCnh) {
    userForm.value.numeroCnh = data.numeroCnh.trim();
    extractedFields.push("número CNH");
    console.log("🚗 Número CNH preenchido:", data.numeroCnh);
  }

  if (data.validadeCnh && !userForm.value.validadeCnh) {
    userForm.value.validadeCnh = data.validadeCnh.trim();
    extractedFields.push("validade CNH");
    console.log("📅 Validade CNH preenchida:", data.validadeCnh);
  }

  // Data de nascimento
  if (data.dataNascimento && !userForm.value.dataNascimento) {
    userForm.value.dataNascimento = data.dataNascimento.trim();
    extractedFields.push("data nascimento");
    console.log("🎂 Data nascimento preenchida:", data.dataNascimento);
  }

  // Local de nascimento
  if (data.localNascimento && !userForm.value.localNascimento) {
    userForm.value.localNascimento = data.localNascimento.trim();
    extractedFields.push("local nascimento");
    console.log("🌍 Local nascimento preenchido:", data.localNascimento);
  }

  // Nacionalidade
  if (data.nacionalidade && !userForm.value.nacionalidade) {
    userForm.value.nacionalidade = data.nacionalidade.trim();
    extractedFields.push("nacionalidade");
    console.log("🇧🇷 Nacionalidade preenchida:", data.nacionalidade);
  }

  // Filiação (pais)
  if (data.filiacao) {
    if (data.filiacao.pai && !userForm.value.nomePai) {
      userForm.value.nomePai = data.filiacao.pai.trim();
      extractedFields.push("nome do pai");
      console.log("👨 Nome do pai preenchido:", data.filiacao.pai);
    }

    if (data.filiacao.mae && !userForm.value.nomeMae) {
      userForm.value.nomeMae = data.filiacao.mae.trim();
      extractedFields.push("nome da mãe");
      console.log("👩 Nome da mãe preenchido:", data.filiacao.mae);
    }
  }

  // ============ DADOS DE EMISSÃO ============

  // Data de emissão do documento
  if (data.dataEmissao && !userForm.value.dataEmissao) {
    userForm.value.dataEmissao = data.dataEmissao.trim();
    extractedFields.push("data emissão");
    console.log("📅 Data emissão preenchida:", data.dataEmissao);
  }

  // Órgão expedidor/emissor
  if (data.orgaoExpedidor && !userForm.value.orgaoEmissor) {
    userForm.value.orgaoEmissor = data.orgaoExpedidor.trim();
    extractedFields.push("órgão emissor");
    console.log("🏛️ Órgão emissor preenchido:", data.orgaoExpedidor);
  }

  if (data.orgaoEmissor && !userForm.value.orgaoEmissor) {
    userForm.value.orgaoEmissor = data.orgaoEmissor.trim();
    extractedFields.push("órgão emissor");
    console.log("🏛️ Órgão emissor preenchido:", data.orgaoEmissor);
  }

  // ============ DADOS EMPRESARIAIS (CNPJ) ============

  if (data.cnpj && !userForm.value.cnpj) {
    userForm.value.cnpj = data.cnpj.trim();
    extractedFields.push("CNPJ");
    console.log("🏢 CNPJ preenchido:", data.cnpj);
  }

  if (data.razaoSocial && !userForm.value.razaoSocial) {
    userForm.value.razaoSocial = data.razaoSocial.trim();
    extractedFields.push("razão social");
    console.log("🏢 Razão social preenchida:", data.razaoSocial);
  }

  // ============ NOTIFICAÇÃO DE SUCESSO ============

  if (extractedFields.length > 0) {
    const documentTypeMessage = result.documentType
      ? `${result.documentType} - `
      : "";
    const confidenceMessage = result.confidence
      ? ` (${Math.round(result.confidence)}% confiança)`
      : "";

    toastSuccess(
      `${documentTypeMessage}${extractedFields.length} campo(s) extraído(s): ${extractedFields.join(", ")}${confidenceMessage}`
    );

    console.log("✅ OCR concluído com sucesso:", {
      documentType: result.documentType,
      fieldsExtracted: extractedFields.length,
      fields: extractedFields,
      confidence: result.confidence,
    });
  } else {
    console.warn(
      "⚠️ OCR processado mas nenhum campo foi preenchido (todos os campos já tinham valores)"
    );
    toastSuccess(
      `Documento ${result.documentType || "identificado"} mas todos os campos já estavam preenchidos`
    );
  }
}

// Toggle role function
function toggleRole(roleValue) {
  const index = userForm.value.roles.indexOf(roleValue);
  if (index > -1) {
    // Remove role
    userForm.value.roles.splice(index, 1);
  } else {
    // Add role
    userForm.value.roles.push(roleValue);
  }
}

// Toggle super user function
function toggleSuperUser() {
  userForm.value.superUser = !userForm.value.superUser;
}

// Format functions
const formatCPF = (cpf) => {
  if (!cpf) return "";
  return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
};

const formatCNPJ = (cnpj) => {
  if (!cnpj) return "";
  return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
};

async function loadUserProfilePhotoUrl(fotoPerfilUrl) {
  if (!fotoPerfilUrl) return;

  try {
    // Extrair a chave do objeto removendo o prefixo /api/v1/files/
    const objectKey = fotoPerfilUrl.replace('/api/v1/files/', '');

    // Fazer requisição para obter URL presigned
    const response = await $fetch('/api/v1/files/presign-download', {
      method: 'GET',
      query: { key: objectKey }
    });

    if (response && response.url) {
      userForm.value.profileImage = response.url;
    }
  } catch (error) {
    console.error('Erro ao carregar foto do perfil:', error);
  }
}

// Load users on mount
onMounted(() => {
  loadUsersList();
});
</script>
