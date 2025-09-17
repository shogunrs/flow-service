<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Page header -->
    <header
      class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-6 py-4"
    >
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
            Usuários
          </h1>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Gerencie usuários do sistema
          </p>
        </div>
        <button
          class="bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-white px-4 py-2 rounded-md text-sm flex items-center gap-2"
          @click="openNewUserModal"
        >
          <i class="fa-solid fa-user-plus"></i>
          Novo Usuário
        </button>
      </div>
    </header>

    <main class="p-6">
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
                <th class="px-3 md:px-6 py-3 min-w-[140px]">Dados Bancários</th>
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
    </main>

    <!-- Modal: User Management -->
    <BaseModal
      v-model="showUserModal"
      :title="editingUser ? 'Editar Usuário' : 'Novo Usuário'"
      size="lg"
      :z-index="70"
    >
      <div class="space-y-6 max-h-[70vh] md:max-h-[80vh] overflow-y-auto pr-1">
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
import { ref, onMounted, computed } from "vue";
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

  // Novos campos para estrutura reorganizada
  tipoDocumento: "",
  cep: "",
  nomeFantasia: "",
  quantidadeSocios: "",
  enderecoEmpresa: "",
  cepEmpresa: "",
  documentoIdentidade: null,
  cartaoCnpjImage: null,
  contratoSocialImage: null,
  qualificacaoSociosImage: null,
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

// Array de sócios dinâmicos
const socios = ref([]);

// Role options
const roleOptions = [
  { value: "admin", label: "Administrador" },
  { value: "user", label: "Usuário" },
  { value: "manager", label: "Gerente" },
  { value: "analyst", label: "Analista" },
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
  };
  editingUser.value = false;
  allowEmailEdit.value = false;
  emailAvailable.value = null;
  emailError.value = "";
  showUserModal.value = true;
}

function editUser(user) {
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
    banco: user.banco || "",
    agencia: user.agencia || "",
    conta: user.conta || "",
    tipoConta: user.tipoConta || "corrente",
    pixTipo: user.pixTipo || "cpf",
    pixChave: user.pixChave || "",
    profileImage: user.profileImage,
    cpfImage: user.cpfImage,
    rgImage: user.rgImage,
    cnpjImage: user.cnpjImage,
    enderecoImage: user.enderecoImage,
    telefoneImage: user.telefoneImage,
    bancoImage: user.bancoImage,
    pixImage: user.pixImage,
    location: user.location,
  };
  editingUser.value = true;
  allowEmailEdit.value = false;
  emailAvailable.value = null;
  emailError.value = "";
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

    const userData = {
      name: name.trim(),
      email: email.trim(),
      roles: roles || ["user"],
      cpf: cpf?.trim() || null,
      cnpj: cnpj?.trim() || null,
      rg: rg?.trim() || null,
      telefone: telefone?.trim() || null,
      endereco: endereco?.trim() || null,
      banco: banco?.trim() || null,
      agencia: agencia?.trim() || null,
      conta: conta?.trim() || null,
      tipoConta: tipoConta || null,
      pixTipo: pixTipo || null,
      pixChave: pixChave?.trim() || null,
      profileImage,
      cpfImage,
      rgImage,
      cnpjImage,
      enderecoImage,
      telefoneImage,
      bancoImage,
      pixImage,
      location,
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

// Format functions
const formatCPF = (cpf) => {
  if (!cpf) return "";
  return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
};

const formatCNPJ = (cnpj) => {
  if (!cnpj) return "";
  return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
};

// Load users on mount
onMounted(() => {
  loadUsersList();
});
</script>
