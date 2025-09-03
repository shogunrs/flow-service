<template>
  <div class="fd-wrapper">
    <label v-if="label" class="fd-label">{{ label }}</label>
    <div
      class="fd-area"
      :class="{ 'fd-area--drag': dragging }"
      @dragover.prevent="onDragOver"
      @dragleave.prevent="onDragLeave"
      @drop.prevent="onDrop"
      @click="openPicker"
    >
      <slot name="icon">
        <i class="fa-solid fa-cloud-arrow-up"></i>
      </slot>
      <div class="fd-text">
        <span class="fd-title">Arraste e solte seus arquivos</span>
        <span class="fd-sub">ou clique para selecionar</span>
      </div>
      <input ref="input" type="file" class="hidden" :multiple="multiple" :accept="accept" @change="onPick" />
    </div>
    <ul v-if="files.length" class="fd-list">
      <li v-for="(f, i) in files" :key="i" class="fd-item">
        <span class="fd-name">{{ f.name }}</span>
        <span class="fd-size">{{ formatSize(f.size) }}</span>
        <button type="button" class="fd-remove" @click.stop="remove(i)">Remover</button>
      </li>
    </ul>
    <p v-if="error" class="fd-error">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{ label?: string; multiple?: boolean; accept?: string; error?: string }>(), {
  label: '',
  multiple: true,
  accept: '',
  error: ''
})
const emit = defineEmits<{ files: [files: File[]] }>()

const input = ref<HTMLInputElement | null>(null)
const dragging = ref(false)
const files = ref<File[]>([])

const openPicker = () => input.value?.click()
const onPick = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (!target.files) return
  addFiles(Array.from(target.files))
  target.value = ''
}
const onDragOver = () => (dragging.value = true)
const onDragLeave = () => (dragging.value = false)
const onDrop = (e: DragEvent) => {
  dragging.value = false
  const list = e.dataTransfer?.files
  if (!list) return
  addFiles(Array.from(list))
}

function addFiles(list: File[]) {
  const next = props.multiple ? [...files.value, ...list] : [list[0]]
  files.value = next
  emit('files', next)
}

const remove = (i: number) => {
  files.value.splice(i, 1)
  emit('files', files.value)
}

const formatSize = (s: number) => {
  if (!s) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(s) / Math.log(1024))
  const val = (s / Math.pow(1024, i)).toFixed(1)
  return `${val} ${units[i]}`
}
</script>

<style scoped>
.fd-label { @apply block text-[10px] text-slate-300 mb-1; }
.fd-area { @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm text-slate-200 flex items-center justify-center gap-2 py-4 cursor-pointer transition; }
.fd-area--drag { @apply ring-1 ring-orange-500/60 border-orange-400/50; }
.fd-text { @apply flex flex-col items-center; }
.fd-title { @apply text-[12px]; }
.fd-sub { @apply text-[10px] text-slate-400; }
.fd-list { @apply mt-2 space-y-1; }
.fd-item { @apply flex items-center justify-between text-[12px] bg-slate-700/50 border border-slate-600/50 rounded-sm px-2 py-1; }
.fd-name { @apply truncate max-w-[60%]; }
.fd-size { @apply text-slate-400; }
.fd-remove { @apply text-[11px] text-orange-400 hover:text-orange-300; }
</style>
