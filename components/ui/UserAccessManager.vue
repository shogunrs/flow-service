<template>
  <div class="space-y-2">
    <div class="flex items-center justify-between">
      <label class="text-[12px] text-slate-300">{{ label }}</label>
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-[12px] text-slate-200 hover:border-indigo-500/50 hover:text-white disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="disabled"
        @click="openModal"
      >
        <i class="fa-solid fa-user-plus text-xs"></i>
        {{ buttonText }}
      </button>
    </div>
    <div
      v-if="viewerDetails.length"
      class="flex flex-wrap gap-2"
    >
      <span
        v-for="user in viewerDetails"
        :key="user.id"
        class="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 border border-indigo-500/30 px-2 py-1 text-[11px] text-indigo-200"
      >
        <span class="truncate max-w-[160px]">{{ user.label }}</span>
        <button
          v-if="!disabled"
          class="text-indigo-200/80 hover:text-white"
          type="button"
          @click="removeViewer(user.id)"
        >
          <i class="fa-solid fa-xmark text-[10px]"></i>
        </button>
      </span>
    </div>
    <p v-if="description" class="text-[11px] text-slate-400">
      {{ description }}
    </p>

    <div v-if="allowExternalContacts && externalContactDetails.length" class="space-y-1">
      <label class="text-[11px] uppercase tracking-wide text-slate-400">{{ contactLabel }}</label>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="(contact, index) in externalContactDetails"
          :key="index + contact.email + contact.phone"
          class="inline-flex items-center gap-1 rounded-full bg-amber-500/10 border border-amber-500/30 px-2 py-1 text-[11px] text-amber-200"
        >
          <span class="truncate max-w-[200px]">
            {{ contact.label }}
          </span>
          <button
            v-if="!disabled"
            class="text-amber-200/80 hover:text-white"
            type="button"
            @click="removeExternalContact(index)"
          >
            <i class="fa-solid fa-xmark text-[10px]"></i>
          </button>
        </span>
      </div>
    </div>

    <BaseModal
      v-model="showModal"
      :title="modalTitle"
      size="md"
    >
      <div class="space-y-4">
        <input
          v-model="search"
          type="search"
          class="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
          placeholder="Buscar por nome ou e-mail"
        />

        <div class="max-h-64 overflow-y-auto rounded-lg border border-slate-800 bg-slate-900/60">
          <div
            v-if="!filteredUsers.length"
            class="px-3 py-4 text-center text-sm text-slate-500"
          >
            Nenhum usuário encontrado.
          </div>
          <button
            v-for="user in filteredUsers"
            :key="user.id"
            type="button"
            :class="[
              'w-full flex items-center justify-between px-3 py-2 text-sm transition-colors border-b border-slate-800 last:border-b-0',
              internalSelectionSet.has(user.id)
                ? 'bg-indigo-500/10 text-indigo-200 border-indigo-500/30'
                : 'text-slate-200 hover:bg-slate-800/60'
            ]"
            @click="toggleUser(user.id)"
          >
            <span class="truncate max-w-[240px] text-left">{{ user.label }}</span>
            <i
              :class="internalSelectionSet.has(user.id)
                ? 'fa-solid fa-check text-indigo-300'
                : 'fa-regular fa-circle text-slate-500'"
            ></i>
          </button>
        </div>

        <div v-if="allowExternalContacts" class="space-y-3">
          <div class="space-y-2">
            <label class="text-[12px] text-slate-300 uppercase tracking-wide">Adicionar contato externo</label>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <input
                v-model="newContact.name"
                type="text"
                class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Nome"
              />
              <input
                v-model="newContact.email"
                type="email"
                class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Email"
              />
              <input
                v-model="newContact.phone"
                type="tel"
                class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="Telefone"
              />
              <input
                v-model="newContact.cpf"
                type="text"
                class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="CPF"
              />
              <input
                v-model="newContact.cnpj"
                type="text"
                class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                placeholder="CNPJ"
              />
            </div>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-md bg-amber-500/20 px-3 py-1.5 text-[12px] text-amber-200 hover:bg-amber-500/30"
                @click="addExternalContact"
              >
                <i class="fa-solid fa-plus text-xs"></i>
                Adicionar contato
              </button>
              <span v-if="contactError" class="text-[11px] text-amber-300">{{ contactError }}</span>
            </div>
          </div>

          <div v-if="internalExternalContacts.length" class="space-y-1">
            <label class="text-[11px] uppercase tracking-wide text-slate-400">{{ contactLabel }}</label>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="(contact, index) in internalExternalContacts"
                :key="index + contact.email + contact.phone"
                class="inline-flex items-center gap-1 rounded-full bg-amber-500/10 border border-amber-500/30 px-2 py-1 text-[11px] text-amber-200"
              >
                <span class="truncate max-w-[200px]">
                  {{ contact.label }}
                </span>
                <button
                  class="text-amber-200/80 hover:text-white"
                  type="button"
                  @click="removeExternalContact(index)"
                >
                  <i class="fa-solid fa-xmark text-[10px]"></i>
                </button>
              </span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-300 hover:bg-slate-800"
            @click="handleCancel"
          >
            Cancelar
          </button>
          <button
            class="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-500"
            @click="handleConfirm"
          >
            Confirmar
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'

const props = defineProps({
  users: { type: Array, default: () => [] },
  modelValue: { type: Array, default: () => [] },
  label: { type: String, default: 'Usuários autorizados' },
  description: { type: String, default: '' },
  buttonText: { type: String, default: 'Gerenciar acesso' },
  modalTitle: { type: String, default: 'Selecionar usuários' },
  allowExternalContacts: { type: Boolean, default: false },
  externalContacts: { type: Array, default: () => [] },
  contactLabel: { type: String, default: 'Contatos externos' },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'update:externalContacts'])

const showModal = ref(false)
const search = ref('')
const internalSelection = ref(sanitize(props.modelValue))
const internalExternalContacts = ref(normalizeContacts(props.externalContacts))
const contactError = ref('')
const newContact = reactive({ name: '', email: '', phone: '', cpf: '', cnpj: '' })

watch(
  () => props.modelValue,
  (next) => {
    internalSelection.value = sanitize(next)
  }
)

watch(
  () => props.externalContacts,
  (next) => {
    internalExternalContacts.value = normalizeContacts(next)
  }
)

const userMap = computed(() => {
  const map = new Map()
  for (const raw of props.users || []) {
    if (!raw || !raw.id) continue
    const id = String(raw.id)
    const label = buildUserLabel(raw)
    map.set(id, {
      id,
      label,
      raw,
    })
  }
  return map
})

const viewerDetails = computed(() =>
  sanitize(internalSelection.value).map((id) => userMap.value.get(id) || { id, label: id })
)

const externalContactDetails = computed(() => internalExternalContacts.value.map(displayContact))

const filteredUsers = computed(() => {
  const term = search.value.trim().toLowerCase()
  if (!term) return Array.from(userMap.value.values())
  return Array.from(userMap.value.values()).filter((user) =>
    user.label.toLowerCase().includes(term)
  )
})

const internalSelectionSet = computed(() => new Set(internalSelection.value))

function openModal() {
  if (props.disabled) return
  showModal.value = true
  search.value = ''
  contactError.value = ''
}

function toggleUser(id) {
  const normalized = String(id)
  const next = new Set(internalSelection.value)
  if (next.has(normalized)) next.delete(normalized)
  else next.add(normalized)
  internalSelection.value = Array.from(next)
}

function removeViewer(id) {
  if (props.disabled) return
  internalSelection.value = internalSelection.value.filter((value) => value !== id)
  emit('update:modelValue', sanitize(internalSelection.value))
}

function handleCancel() {
  showModal.value = false
  internalSelection.value = sanitize(props.modelValue)
  internalExternalContacts.value = normalizeContacts(props.externalContacts)
  search.value = ''
  contactError.value = ''
  resetNewContact()
}

function handleConfirm() {
  emit('update:modelValue', sanitize(internalSelection.value))
  if (props.allowExternalContacts) {
    emit('update:externalContacts', internalExternalContacts.value.map(stripContact))
  }
  showModal.value = false
  search.value = ''
  contactError.value = ''
  resetNewContact()
}

function addExternalContact() {
  const contact = {
    name: newContact.name.trim(),
    email: newContact.email.trim(),
    phone: newContact.phone.trim(),
    cpf: newContact.cpf.trim(),
    cnpj: newContact.cnpj.trim(),
  }
  if (!contact.name) {
    contactError.value = 'Informe o nome do contato.'
    return
  }
  if (!contact.email && !contact.phone && !contact.cpf && !contact.cnpj) {
    contactError.value = 'Informe pelo menos e-mail, telefone, CPF ou CNPJ.'
    return
  }
  contactError.value = ''
  internalExternalContacts.value = [
    ...internalExternalContacts.value,
    contact,
  ]
  resetNewContact()
}

function removeExternalContact(index) {
  if (props.disabled) return
  internalExternalContacts.value = internalExternalContacts.value.filter((_, i) => i !== index)
  if (!showModal.value) {
    emit('update:externalContacts', internalExternalContacts.value.map(stripContact))
  }
}

function resetNewContact() {
  newContact.name = ''
  newContact.email = ''
  newContact.phone = ''
  newContact.cpf = ''
  newContact.cnpj = ''
}

function sanitize(ids) {
  return Array.from(
    new Set(
      (ids || [])
        .filter((value) => typeof value === 'string')
        .map((value) => value.trim())
        .filter(Boolean)
    )
  )
}

function buildUserLabel(user) {
  const pieces = [user.name, user.email]
    .filter((value) => typeof value === 'string' && value.trim())
  return pieces.length ? pieces.join(' — ') : String(user.id)
}

function normalizeContacts(contacts) {
  return (contacts || [])
    .filter(Boolean)
    .map(stripContact)
}

function stripContact(contact) {
  return {
    name: (contact?.name || '').trim(),
    email: (contact?.email || '').trim(),
    phone: (contact?.phone || '').trim(),
    cpf: (contact?.cpf || '').trim(),
    cnpj: (contact?.cnpj || '').trim(),
  }
}

function displayContact(contact) {
  const labelParts = [contact.name, contact.email, contact.phone, contact.cpf, contact.cnpj]
    .filter((value) => typeof value === 'string' && value.trim())
  return {
    ...contact,
    label: labelParts.length ? labelParts.join(' • ') : contact.name || 'Contato',
  }
}
</script>

<style scoped>
</style>
