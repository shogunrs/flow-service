        <!-- ========== SEÇÃO 1: DADOS PESSOAIS ========== -->
        <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/50">
          <h3 class="text-lg font-semibold text-slate-200 mb-4 flex items-center">
            <i class="fa-solid fa-user mr-2 text-blue-400"></i>
            Dados Pessoais
          </h3>

          <!-- Dados básicos -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div class="md:col-span-2">
              <label class="text-[12px] text-slate-300">Nome Completo *</label>
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
                    class="p-1.5 rounded-md hover:bg-slate-700/50 transition-all duration-200"
                    :class="allowEmailEdit ? 'text-indigo-400 hover:text-indigo-300' : 'text-slate-400 hover:text-slate-300'"
                    :title="allowEmailEdit ? 'Bloquear edição do email' : 'Permitir edição do email'"
                  >
                    <i
                      :class="allowEmailEdit ? 'fa-solid fa-unlock' : 'fa-solid fa-lock'"
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
                  :disabled="(editingUser && !allowEmailEdit) || (!editingUser && currentUserId)"
                  @blur="checkEmailAvailability"
                  @input="onEmailInput"
                />
                <div v-if="emailCheckLoading" class="absolute right-3 top-1/2 transform -translate-y-1/2">
                  <i class="fa-solid fa-spinner fa-spin text-orange-500"></i>
                </div>
                <div v-else-if="emailAvailable === true" class="absolute right-3 top-1/2 transform -translate-y-1/2">
                  <i class="fa-solid fa-check text-green-500"></i>
                </div>
                <div v-else-if="emailAvailable === false" class="absolute right-3 top-1/2 transform -translate-y-1/2">
                  <i class="fa-solid fa-times text-red-500"></i>
                </div>
              </div>
              <div v-if="emailError" class="text-[11px] text-red-400 mt-1">
                {{ emailError }}
              </div>
              <div v-else-if="emailAvailable === true" class="text-[11px] text-green-400 mt-1">
                Email disponível
              </div>
              <div v-else-if="emailAvailable === false" class="text-[11px] text-red-400 mt-1">
                Email já está em uso
              </div>
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Telefone</label>
              <input
                v-model="userForm.telefone"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="(00) 00000-0000"
                v-telefone
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Data de Nascimento</label>
              <input
                v-model="userForm.dataNascimento"
                type="date"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Tipo de Documento de Identidade</label>
              <select
                v-model="userForm.tipoDocumento"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
              >
                <option value="">Selecione o tipo</option>
                <option value="rg">RG</option>
                <option value="cnh">CNH</option>
                <option value="passaporte">Passaporte</option>
              </select>
            </div>
          </div>

          <!-- Endereço -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Endereço Residencial</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="md:col-span-2">
                <label class="text-[12px] text-slate-300">Endereço Completo</label>
                <textarea
                  v-model="userForm.endereco"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="Rua, número, bairro, cidade, estado"
                  rows="2"
                ></textarea>
              </div>
              <div>
                <label class="text-[12px] text-slate-300">CEP</label>
                <input
                  v-model="userForm.cep"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="00000-000"
                  v-cep
                />
              </div>
            </div>
          </div>

          <!-- Upload de Documentos Pessoais -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Documentos Pessoais</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <UserFileUpload
                  label="Documento de Identidade (RG/CNH/Passaporte)"
                  accept="image/*,application/pdf"
                  v-model="userForm.documentoIdentidade"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="DOCUMENT"
                  :is-temporary="!currentUserId"
                  :enable-ocr="true"
                  :form-data="{ nome: userForm.name, email: userForm.email }"
                  @ocr-result="onOcrResult"
                />
              </div>
              <div>
                <UserFileUpload
                  label="Foto de Perfil"
                  accept="image/*"
                  v-model="userForm.profileImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="PROFILE_PHOTO"
                  :is-temporary="!currentUserId"
                />
              </div>
            </div>
          </div>

          <!-- Acesso ao Sistema -->
          <div class="border-t border-slate-700/50 pt-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Acesso ao Sistema</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-[12px] text-slate-300">
                  Senha {{ editingUser ? '(deixe em branco para manter)' : '*' }}
                </label>
                <input
                  v-model="userForm.password"
                  type="password"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="Digite a senha"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Permissões</label>
                <div class="mt-1 space-y-2">
                  <label
                    v-for="role in roleOptions"
                    :key="role.value"
                    class="flex items-center gap-2 cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      :value="role.value"
                      v-model="userForm.roles"
                      class="accent-indigo-500"
                    />
                    <span class="text-xs text-slate-300">{{ role.label }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== SEÇÃO 2: DADOS DA EMPRESA ========== -->
        <div class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/50">
          <h3 class="text-lg font-semibold text-slate-200 mb-4 flex items-center">
            <i class="fa-solid fa-building mr-2 text-green-400"></i>
            Dados da Empresa
          </h3>

          <!-- Informações da Empresa -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label class="text-[12px] text-slate-300">Razão Social</label>
              <input
                v-model="userForm.razaoSocial"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="Razão social da empresa"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Nome Fantasia</label>
              <input
                v-model="userForm.nomeFantasia"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="Nome fantasia"
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">CNPJ</label>
              <input
                v-model="userForm.cnpj"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                placeholder="00.000.000/0000-00"
                v-cnpj
              />
            </div>
            <div>
              <label class="text-[12px] text-slate-300">Quantidade de Sócios</label>
              <select
                v-model="userForm.quantidadeSocios"
                @change="onQuantidadeSociosChange"
                class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
              >
                <option value="">Selecione</option>
                <option v-for="n in 10" :key="n" :value="n">{{ n }} sócio{{ n > 1 ? 's' : '' }}</option>
              </select>
            </div>
          </div>

          <!-- Endereço da Empresa -->
          <div class="border-t border-slate-700/50 pt-4 mb-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Endereço da Empresa</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="md:col-span-2">
                <label class="text-[12px] text-slate-300">Endereço Completo</label>
                <textarea
                  v-model="userForm.enderecoEmpresa"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="Endereço da empresa"
                  rows="2"
                ></textarea>
              </div>
              <div>
                <label class="text-[12px] text-slate-300">CEP</label>
                <input
                  v-model="userForm.cepEmpresa"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="00000-000"
                  v-cep
                />
              </div>
            </div>
          </div>

          <!-- Documentos da Empresa -->
          <div class="border-t border-slate-700/50 pt-4">
            <h4 class="text-sm font-medium text-slate-300 mb-3">Documentos da Empresa</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <UserFileUpload
                  label="Cartão CNPJ"
                  accept="image/*,application/pdf"
                  v-model="userForm.cartaoCnpjImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="cnpjImage"
                  :is-temporary="!currentUserId"
                />
              </div>
              <div>
                <UserFileUpload
                  label="Contrato Social"
                  accept="image/*,application/pdf"
                  v-model="userForm.contratoSocialImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="enderecoImage"
                  :is-temporary="!currentUserId"
                />
              </div>
              <div v-if="userForm.quantidadeSocios > 1">
                <UserFileUpload
                  label="Qualificação dos Sócios"
                  accept="image/*,application/pdf"
                  v-model="userForm.qualificacaoSociosImage"
                  :user-id="currentUserId || tempUploadSessionId"
                  file-type="bancoImage"
                  :is-temporary="!currentUserId"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- ========== SEÇÃO 3: DADOS DOS SÓCIOS (DINÂMICA) ========== -->
        <div v-if="userForm.quantidadeSocios > 1" class="bg-slate-800/30 rounded-lg p-4 border border-slate-700/50">
          <h3 class="text-lg font-semibold text-slate-200 mb-4 flex items-center">
            <i class="fa-solid fa-users mr-2 text-purple-400"></i>
            Dados dos Demais Sócios ({{ userForm.quantidadeSocios - 1 }})
          </h3>

          <div
            v-for="(socio, index) in socios"
            :key="index"
            class="border border-slate-700/50 rounded-lg p-4 mb-4"
          >
            <h4 class="text-md font-medium text-slate-300 mb-3 flex items-center justify-between">
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
                <label class="text-[12px] text-slate-300">Nome Completo *</label>
                <input
                  v-model="socio.nome"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="Nome completo do sócio"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Email</label>
                <input
                  v-model="socio.email"
                  type="email"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="email@exemplo.com"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Telefone</label>
                <input
                  v-model="socio.telefone"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                  placeholder="(00) 00000-0000"
                  v-telefone
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Data de Nascimento</label>
                <input
                  v-model="socio.dataNascimento"
                  type="date"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label class="text-[12px] text-slate-300">Tipo de Documento</label>
                <select
                  v-model="socio.tipoDocumento"
                  class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
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
              <h5 class="text-sm font-medium text-slate-300 mb-3">Endereço do Sócio</h5>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="md:col-span-2">
                  <label class="text-[12px] text-slate-300">Endereço Completo</label>
                  <textarea
                    v-model="socio.endereco"
                    class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                    placeholder="Endereço do sócio"
                    rows="2"
                  ></textarea>
                </div>
                <div>
                  <label class="text-[12px] text-slate-300">CEP</label>
                  <input
                    v-model="socio.cep"
                    class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
                    placeholder="00000-000"
                    v-cep
                  />
                </div>
              </div>
            </div>

            <!-- Documentos do sócio -->
            <div class="border-t border-slate-700/50 pt-4">
              <h5 class="text-sm font-medium text-slate-300 mb-3">Documentos do Sócio</h5>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <UserFileUpload
                    :label="`Documento de Identidade - Sócio ${index + 2}`"
                    accept="image/*,application/pdf"
                    v-model="socio.documentoIdentidade"
                    :user-id="currentUserId || tempUploadSessionId"
                    file-type="DOCUMENT"
                    :is-temporary="!currentUserId"
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
                    :is-temporary="!currentUserId"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>