<template>
  <div class="min-h-screen bg-slate-950">
    <!-- Header -->
    <div class="bg-slate-900 border-b border-slate-800">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-4">
          <div class="flex items-center">
            <button
              @click="$router.back()"
              class="mr-4 p-2 text-slate-400 hover:text-slate-300 hover:bg-slate-800 rounded-lg transition-colors"
            >
              <i class="fa-solid fa-arrow-left"></i>
            </button>
            <h1 class="text-xl font-semibold text-slate-100">Meu Perfil</h1>
          </div>
          <button
            @click="saveProfile"
            :disabled="saving"
            class="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          >
            <i v-if="saving" class="fa-solid fa-spinner fa-spin mr-2"></i>
            {{ saving ? 'Salvando...' : 'Salvar Alterações' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-slate-900 rounded-lg border border-slate-800 overflow-hidden">

        <!-- Profile Header -->
        <div class="p-6 border-b border-slate-800">
          <div class="flex items-center space-x-4">
            <div class="relative">
              <div class="w-20 h-20 bg-slate-800 rounded-full flex items-center justify-center overflow-hidden">
                <img
                  v-if="displayProfileImage"
                  :src="displayProfileImage"
                  alt="Foto do Perfil"
                  class="w-full h-full object-cover"
                  :class="{ 'opacity-75': uploading }"
                  @error="handleImageError"
                />
                <i v-else class="fa-solid fa-user text-2xl text-slate-400"></i>
              </div>
              <!-- Upload loading indicator -->
              <div
                v-if="uploading"
                class="absolute inset-0 bg-black/50 rounded-full flex items-center justify-center"
              >
                <i class="fa-solid fa-spinner fa-spin text-white"></i>
              </div>
              <button
                @click="openImageUpload"
                class="absolute -bottom-1 -right-1 w-6 h-6 bg-indigo-600 hover:bg-indigo-700 rounded-full flex items-center justify-center text-white text-xs transition-colors"
              >
                <i class="fa-solid fa-camera"></i>
              </button>
            </div>
            <div>
              <h2 class="text-xl font-semibold text-slate-100">{{ profileForm.name || 'Nome não informado' }}</h2>
              <p class="text-slate-400">{{ profileForm.email }}</p>
              <div class="flex items-center mt-1">
                <span
                  v-for="role in profileForm.roles"
                  :key="role"
                  class="inline-block bg-slate-800 text-slate-300 px-2 py-1 rounded text-xs mr-2"
                >
                  {{ getRoleDisplayName(role) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Form Sections -->
        <div class="p-6 space-y-8">

          <!-- Informações Básicas -->
          <section>
            <h3 class="text-lg font-medium text-slate-100 mb-4 flex items-center">
              <i class="fa-solid fa-user mr-2 text-indigo-400"></i>
              Informações Básicas
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-slate-300 block mb-2">Nome Completo *</label>
                <input
                  v-model="profileForm.name"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Seu nome completo"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Email *</label>
                <input
                  v-model="profileForm.email"
                  type="email"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="seu@email.com"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">CPF</label>
                <input
                  v-model="profileForm.cpf"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="000.000.000-00"
                  @input="formatCpf"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">RG</label>
                <input
                  v-model="profileForm.rg"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="00.000.000-0"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Telefone</label>
                <input
                  v-model="profileForm.telefone"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="(11) 99999-9999"
                  @input="formatPhone"
                />
              </div>
            </div>
          </section>

          <!-- Alteração de Senha -->
          <section>
            <h3 class="text-lg font-medium text-slate-100 mb-4 flex items-center">
              <i class="fa-solid fa-lock mr-2 text-indigo-400"></i>
              Alterar Senha
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-slate-300 block mb-2">Nova Senha</label>
                <div class="relative">
                  <input
                    v-model="profileForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 pr-10 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                    :class="{ 'border-red-500': passwordError }"
                    placeholder="Deixe em branco para manter atual"
                    @input="validatePasswords"
                  />
                  <button
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-300"
                  >
                    <i :class="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'" class="text-sm"></i>
                  </button>
                </div>
                <p v-if="passwordError" class="text-red-400 text-xs mt-1">{{ passwordError }}</p>
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Confirmar Nova Senha</label>
                <div class="relative">
                  <input
                    v-model="confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 pr-10 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                    :class="{ 'border-red-500': confirmPasswordError }"
                    placeholder="Confirme a nova senha"
                    @input="validatePasswords"
                  />
                  <button
                    type="button"
                    @click="showConfirmPassword = !showConfirmPassword"
                    class="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-300"
                  >
                    <i :class="showConfirmPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'" class="text-sm"></i>
                  </button>
                </div>
                <p v-if="confirmPasswordError" class="text-red-400 text-xs mt-1">{{ confirmPasswordError }}</p>
              </div>
            </div>
          </section>

          <!-- Endereço -->
          <section>
            <h3 class="text-lg font-medium text-slate-100 mb-4 flex items-center">
              <i class="fa-solid fa-home mr-2 text-indigo-400"></i>
              Endereço
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div class="md:col-span-2">
                <label class="text-sm text-slate-300 block mb-2">Rua/Avenida</label>
                <input
                  v-model="profileForm.enderecoRua"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Nome da rua ou avenida"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Número</label>
                <input
                  v-model="profileForm.enderecoNumero"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="123"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Complemento</label>
                <input
                  v-model="profileForm.enderecoComplemento"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Apto, Bloco, etc"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Bairro</label>
                <input
                  v-model="profileForm.enderecoBairro"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Nome do bairro"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Cidade</label>
                <input
                  v-model="profileForm.enderecoCidade"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Nome da cidade"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Estado</label>
                <select
                  v-model="profileForm.enderecoEstado"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
                >
                  <option value="">Selecione</option>
                  <option value="AC">Acre</option>
                  <option value="AL">Alagoas</option>
                  <option value="AP">Amapá</option>
                  <option value="AM">Amazonas</option>
                  <option value="BA">Bahia</option>
                  <option value="CE">Ceará</option>
                  <option value="DF">Distrito Federal</option>
                  <option value="ES">Espírito Santo</option>
                  <option value="GO">Goiás</option>
                  <option value="MA">Maranhão</option>
                  <option value="MT">Mato Grosso</option>
                  <option value="MS">Mato Grosso do Sul</option>
                  <option value="MG">Minas Gerais</option>
                  <option value="PA">Pará</option>
                  <option value="PB">Paraíba</option>
                  <option value="PR">Paraná</option>
                  <option value="PE">Pernambuco</option>
                  <option value="PI">Piauí</option>
                  <option value="RJ">Rio de Janeiro</option>
                  <option value="RN">Rio Grande do Norte</option>
                  <option value="RS">Rio Grande do Sul</option>
                  <option value="RO">Rondônia</option>
                  <option value="RR">Roraima</option>
                  <option value="SC">Santa Catarina</option>
                  <option value="SP">São Paulo</option>
                  <option value="SE">Sergipe</option>
                  <option value="TO">Tocantins</option>
                </select>
              </div>
            </div>
          </section>

          <!-- Dados Bancários -->
          <section>
            <h3 class="text-lg font-medium text-slate-100 mb-4 flex items-center">
              <i class="fa-solid fa-building-columns mr-2 text-indigo-400"></i>
              Dados Bancários
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div>
                <label class="text-sm text-slate-300 block mb-2">Banco</label>
                <input
                  v-model="profileForm.banco"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Nome do banco"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Agência</label>
                <input
                  v-model="profileForm.agencia"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="0000"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Conta</label>
                <input
                  v-model="profileForm.conta"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="00000-0"
                />
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Tipo de Conta</label>
                <select
                  v-model="profileForm.tipoConta"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
                >
                  <option value="corrente">Corrente</option>
                  <option value="poupanca">Poupança</option>
                </select>
              </div>
            </div>
          </section>

          <!-- PIX -->
          <section>
            <h3 class="text-lg font-medium text-slate-100 mb-4 flex items-center">
              <i class="fa-solid fa-qrcode mr-2 text-indigo-400"></i>
              PIX
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-slate-300 block mb-2">Tipo de Chave PIX</label>
                <select
                  v-model="profileForm.pixTipo"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
                >
                  <option value="cpf">CPF</option>
                  <option value="email">Email</option>
                  <option value="telefone">Telefone</option>
                  <option value="aleatoria">Chave Aleatória</option>
                </select>
              </div>
              <div>
                <label class="text-sm text-slate-300 block mb-2">Chave PIX</label>
                <input
                  v-model="profileForm.pixChave"
                  type="text"
                  class="w-full h-10 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  placeholder="Sua chave PIX"
                />
              </div>
            </div>
          </section>

          <!-- Observações -->
          <section>
            <h3 class="text-lg font-medium text-slate-100 mb-4 flex items-center">
              <i class="fa-solid fa-sticky-note mr-2 text-indigo-400"></i>
              Observações
            </h3>
            <div>
              <textarea
                v-model="profileForm.observacoes"
                rows="4"
                class="w-full rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-indigo-500 focus:outline-none resize-none"
                placeholder="Informações adicionais sobre seu perfil..."
              ></textarea>
            </div>
          </section>

        </div>
      </div>
    </div>

    <!-- Image Upload Modal -->
    <div
      v-if="showImageUploadModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="closeImageUpload"
    >
      <div class="bg-slate-900 rounded-lg border border-slate-800 p-4 max-w-sm w-full mx-4">
        <div class="flex justify-between items-center mb-3">
          <h3 class="text-base font-medium text-slate-100">Alterar Foto</h3>
          <button
            @click="closeImageUpload"
            class="text-slate-400 hover:text-slate-300 text-sm"
          >
            <i class="fa-solid fa-times"></i>
          </button>
        </div>

        <div class="space-y-3">
          <!-- File Input -->
          <div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              @change="handleFileSelect"
              class="w-full text-xs text-slate-300 file:mr-2 file:py-1.5 file:px-3 file:rounded file:border-0 file:text-xs file:font-medium file:bg-indigo-600 file:text-white hover:file:bg-indigo-700"
            />
          </div>

          <!-- Preview -->
          <div v-if="previewImage" class="text-center">
            <img
              :src="previewImage"
              alt="Preview"
              class="w-20 h-20 rounded-full object-cover mx-auto border-2 border-slate-700"
            />
          </div>

          <!-- Upload Button -->
          <div class="flex gap-2">
            <button
              @click="uploadImage"
              :disabled="!selectedFile || uploading"
              class="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-3 py-1.5 rounded text-xs font-medium transition-colors"
            >
              <i v-if="uploading" class="fa-solid fa-spinner fa-spin mr-1"></i>
              {{ uploading ? 'Enviando...' : 'Enviar' }}
            </button>
            <button
              @click="closeImageUpload"
              class="px-3 py-1.5 text-slate-400 hover:text-slate-300 text-xs font-medium"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCurrentUser } from '~/composables/useCurrentUser'
import { getApiBase } from '~/utils/api/index'
import { useToast } from '~/composables/useToast'

// Page meta
definePageMeta({
  title: 'Meu Perfil',
  layout: 'sidebar'
})

// Composables
const { success: toastSuccess, error: toastError } = useToast()

// Reactive data
const saving = ref(false)

// Password fields
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordError = ref('')
const confirmPasswordError = ref('')

// Image upload
const showImageUploadModal = ref(false)
const selectedFile = ref<File | null>(null)
const previewImage = ref('')
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const tempImagePreview = ref('')

// Profile form
const profileForm = ref({
  id: null as string | null,
  name: '',
  email: '',
  password: '',
  cpf: '',
  rg: '',
  telefone: '',
  enderecoRua: '',
  enderecoNumero: '',
  enderecoComplemento: '',
  enderecoBairro: '',
  enderecoCidade: '',
  enderecoEstado: '',
  banco: '',
  agencia: '',
  conta: '',
  tipoConta: 'corrente',
  pixTipo: 'cpf',
  pixChave: '',
  fotoPerfilUrl: '',
  observacoes: '',
  roles: [] as string[]
})

// Storage URL resolution
const apiBaseUrl = getApiBase()?.replace(/\/$/, '') || ''

const resolveStorageUrl = (rawUrl: any) => {
  if (!rawUrl) return ''
  if (typeof rawUrl !== 'string') return ''
  const trimmed = rawUrl.trim()
  if (!trimmed) return ''

  // If it's already a full URL, return as is
  if (/^(https?:|data:|blob:)/i.test(trimmed)) {
    console.log('🔗 Full URL detected:', trimmed)
    return trimmed
  }

  if (!apiBaseUrl) {
    console.warn('⚠️ No API base URL configured, returning raw URL:', trimmed)
    return trimmed
  }

  let resolvedUrl: string

  // If URL starts with /api, it's a direct API endpoint - use as is
  if (trimmed.startsWith('/api')) {
    resolvedUrl = `${apiBaseUrl}${trimmed}`
    console.log('🔗 API endpoint URL:', { input: trimmed, output: resolvedUrl })
  } else {
    // Otherwise, it's a storage path - add /storage prefix
    resolvedUrl = `${apiBaseUrl}/storage/${trimmed}`
    console.log('🔗 Storage URL:', { input: trimmed, output: resolvedUrl })
  }

  return resolvedUrl
}

const resolveProfileSource = (source: any) => {
  if (!source) return ''
  if (typeof source === 'string') {
    return resolveStorageUrl(source)
  }
  if (typeof source === 'object') {
    const candidate =
      source.previewUrl ||
      source.url ||
      source.secure_url ||
      source.publicUrl ||
      source.thumbnailUrl ||
      ''
    console.log('📸 Resolving object source:', { source, candidate })
    return resolveStorageUrl(candidate)
  }
  return ''
}

// Image refresh counter for cache busting
const imageRefreshCounter = ref(0)

// Computed for profile image display
const displayProfileImage = computed(() => {
  // Show temporary preview during upload
  if (tempImagePreview.value && uploading.value) {
    console.log('📸 Using temporary preview during upload')
    return tempImagePreview.value
  }

  const baseUrl = resolveProfileSource(profileForm.value.fotoPerfilUrl)
  if (!baseUrl) {
    console.log('📸 No profile image URL available')
    return ''
  }

  // Add cache busting parameter if we have refreshed the image
  if (imageRefreshCounter.value > 0) {
    const separator = baseUrl.includes('?') ? '&' : '?'
    const finalUrl = `${baseUrl}${separator}v=${imageRefreshCounter.value}`
    console.log('📸 Profile image with cache busting:', { baseUrl, finalUrl })
    return finalUrl
  }

  console.log('📸 Profile image URL:', baseUrl)
  return baseUrl
})

// Load current user data
onMounted(async () => {
  await loadCurrentUserProfile()
})

async function loadCurrentUserProfile() {
  try {
    // Get current user from composable
    const { user, load } = useCurrentUser()
    await load()

    // Get detailed user data from users API
    const { apiFetch } = await import('~/utils/api/index')
    const userId = user.value.id

    if (userId && userId !== 'u-guest') {
      const detailedUser = await apiFetch(`/api/v1/users/${userId}`) as any

      if (detailedUser) {
        profileForm.value = {
          id: detailedUser.id,
          name: detailedUser.name || '',
          email: detailedUser.email || '',
          password: '',
          cpf: detailedUser.cpf || '',
          rg: detailedUser.rg || '',
          telefone: detailedUser.telefone || '',
          enderecoRua: detailedUser.enderecoRua || '',
          enderecoNumero: detailedUser.enderecoNumero || '',
          enderecoComplemento: detailedUser.enderecoComplemento || '',
          enderecoBairro: detailedUser.enderecoBairro || '',
          enderecoCidade: detailedUser.enderecoCidade || '',
          enderecoEstado: detailedUser.enderecoEstado || '',
          banco: detailedUser.banco || '',
          agencia: detailedUser.agencia || '',
          conta: detailedUser.conta || '',
          tipoConta: detailedUser.tipoConta || 'corrente',
          pixTipo: detailedUser.pixTipo || 'cpf',
          pixChave: detailedUser.pixChave || '',
          fotoPerfilUrl: detailedUser.fotoPerfilUrl || '',
          observacoes: detailedUser.observacoes || '',
          roles: detailedUser.roles || []
        }
      }
    }
  } catch (error) {
    console.error('Error loading user profile:', error)
    toastError('Erro ao carregar perfil do usuário')
  }
}

function validatePasswords() {
  const password = profileForm.value.password.trim()
  const confirm = confirmPassword.value.trim()

  // Reset errors
  passwordError.value = ''
  confirmPasswordError.value = ''

  // Skip validation if passwords are empty (keeping existing password)
  if (!password && !confirm) {
    return true
  }

  // Password strength validation
  if (password && password.length < 6) {
    passwordError.value = 'A senha deve ter pelo menos 6 caracteres'
    return false
  }

  // Confirm password validation
  if (password && !confirm) {
    confirmPasswordError.value = 'Confirme a nova senha'
    return false
  }

  // Check if passwords match
  if (password && confirm && password !== confirm) {
    confirmPasswordError.value = 'As senhas não coincidem'
    return false
  }

  return true
}

async function saveProfile() {
  if (!validatePasswords()) {
    return
  }

  saving.value = true

  try {
    const { updateUserApi } = await import('~/composables/useUsersApi')

    // Prepare data for update (remove password if empty)
    const updateData: any = { ...profileForm.value }
    if (!updateData.password.trim()) {
      delete updateData.password
    }

    await updateUserApi(profileForm.value.id!, updateData)

    toastSuccess('Perfil atualizado com sucesso!')

    // Clear password fields after successful update
    profileForm.value.password = ''
    confirmPassword.value = ''

  } catch (error) {
    console.error('Error updating profile:', error)
    toastError('Erro ao atualizar perfil. Tente novamente.')
  } finally {
    saving.value = false
  }
}

function formatCpf() {
  let value = profileForm.value.cpf.replace(/\D/g, '')
  if (value.length <= 11) {
    value = value.replace(/(\d{3})(\d)/, '$1.$2')
    value = value.replace(/(\d{3})(\d)/, '$1.$2')
    value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2')
  }
  profileForm.value.cpf = value
}

function formatPhone() {
  let value = profileForm.value.telefone.replace(/\D/g, '')
  if (value.length <= 11) {
    if (value.length <= 10) {
      value = value.replace(/(\d{2})(\d)/, '($1) $2')
      value = value.replace(/(\d{4})(\d)/, '$1-$2')
    } else {
      value = value.replace(/(\d{2})(\d)/, '($1) $2')
      value = value.replace(/(\d{5})(\d)/, '$1-$2')
    }
  }
  profileForm.value.telefone = value
}

function getRoleDisplayName(role: string) {
  const roleNames: Record<string, string> = {
    admin: 'Administrador',
    manager: 'Gerente',
    analyst: 'Analista',
    user: 'Usuário',
    viewer: 'Visualizador'
  }
  return roleNames[role] || role
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  const failedUrl = img?.src

  console.warn('Failed to load profile image:', {
    url: failedUrl,
    originalUrl: profileForm.value.fotoPerfilUrl,
    resolvedUrl: displayProfileImage.value
  })

  // Clear the failed image to show fallback icon
  if (profileForm.value.fotoPerfilUrl === failedUrl) {
    profileForm.value.fotoPerfilUrl = ''
  }

  // Reset refresh counter to avoid infinite loops
  imageRefreshCounter.value = 0
}

function openImageUpload() {
  showImageUploadModal.value = true
}

function closeImageUpload() {
  showImageUploadModal.value = false
  selectedFile.value = null
  previewImage.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (file) {
    selectedFile.value = file

    // Create preview
    const reader = new FileReader()
    reader.onload = (e) => {
      previewImage.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

async function uploadImage() {
  if (!selectedFile.value || !profileForm.value.id) return

  uploading.value = true

  // Set temporary preview for instant visual feedback
  tempImagePreview.value = previewImage.value

  try {
    const { apiFetch } = await import('~/utils/api/index')
    const userId = profileForm.value.id
    const file = selectedFile.value

    // Step 1: Request pre-signed upload URL
    const uploadRequest = {
      filename: file.name,
      contentType: file.type,
      fileType: 'PROFILE_PHOTO'
    }

    const urlResponse = await apiFetch(`/api/v1/users/${userId}/files/upload-url`, {
      method: 'POST',
      body: uploadRequest
    }) as any

    if (!urlResponse?.success || !urlResponse?.data) {
      throw new Error('Failed to get upload URL')
    }

    const { uploadUrl, objectKey, publicUrl } = urlResponse.data

    // Step 2: Upload file directly to storage using pre-signed URL
    const uploadResponse = await fetch(uploadUrl, {
      method: 'PUT',
      body: file,
      headers: {
        'Content-Type': file.type
      }
    })

    if (!uploadResponse.ok) {
      throw new Error('Failed to upload file to storage')
    }

    // Step 3: Confirm upload with backend
    const confirmData = {
      objectKey,
      publicUrl,
      filename: file.name,
      fileType: 'PROFILE_PHOTO',
      contentType: file.type,
      fileSize: file.size,
      isMobileUpload: false,
      location: null // Could add geolocation if needed
    }

    await apiFetch(`/api/v1/users/${userId}/files/confirm-upload`, {
      method: 'POST',
      body: confirmData
    })

    // Update profile form with new image URL immediately
    profileForm.value.fotoPerfilUrl = publicUrl

    // Force image refresh with cache busting
    imageRefreshCounter.value = Date.now()

    // Save profile to persist the change (in background)
    saveProfile().catch(error => {
      console.error('Error saving profile after image upload:', error)
    })

    // Refresh current user to update sidebar and other components (in background)
    const { refresh: refreshCurrentUser } = useCurrentUser()
    refreshCurrentUser().catch(error => {
      console.error('Error refreshing current user:', error)
    })

    toastSuccess('Foto de perfil atualizada com sucesso!')
    closeImageUpload()

  } catch (error) {
    console.error('Error uploading image:', error)
    toastError('Erro ao enviar foto. Tente novamente.')
  } finally {
    uploading.value = false
    tempImagePreview.value = '' // Clear temporary preview
  }
}

</script>