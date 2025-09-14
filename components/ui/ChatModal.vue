<template>
  <Teleport to="body">
    <transition name="fade">
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-center justify-center"
      >
        <div
          class="absolute inset-0 bg-black/60 backdrop-blur-[2px]"
          @click="emitClose"
        ></div>
        <div
          class="relative z-10 bg-slate-800/90 text-white overflow-hidden flex flex-col w-full h-full rounded-none sm:w-auto sm:h-auto sm:max-h-[85vh] sm:rounded-2xl border border-slate-700/60 shadow-2xl backdrop-blur-md"
          :class="sizeClass"
        >
          <!-- Header -->
          <div
            class="flex items-center justify-between px-3 py-2 border-b border-slate-700/60 bg-gradient-to-r from-slate-900/60 to-slate-800/40"
          >
            <div class="flex items-center gap-2">
              <i
                class="fa-solid fa-robot text-cyan-400 text-base drop-shadow"
              ></i>
              <h3 class="text-[13px] font-semibold tracking-wide">
                {{ title }}
              </h3>
            </div>
            <button
              class="text-slate-300 hover:text-white p-1 rounded-lg hover:bg-white/5 transition-colors"
              @click="emitClose"
              aria-label="Fechar"
            >
              <i class="fa-solid fa-xmark text-sm"></i>
            </button>
          </div>

          <!-- Messages -->
          <div
            ref="chatScrollRef"
            class="flex-1 overflow-y-auto px-3 py-2 space-y-2"
            :class="{ 'drag-target': allowDrop }"
            @dragover.prevent="allowDrop && true"
            @drop.prevent="allowDrop && onDrop"
          >
            <div
              v-for="(m, idx) in messages"
              :key="idx"
              class="flex"
              :class="m.role === 'user' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="rounded-2xl px-3 py-2 max-w-[80%] text-[12px] leading-snug shadow-md"
                :class="
                  m.role === 'user'
                    ? 'bg-gradient-to-br from-cyan-500 to-blue-600 text-white'
                    : 'bg-gradient-to-br from-slate-700/80 to-slate-800/80 text-slate-100 border border-slate-600/40'
                "
              >
                <template v-if="m.content && m.content.length">{{
                  m.content
                }}</template>
                <!-- Attachments inside message -->
                <div
                  v-if="m.attachments?.length"
                  class="mt-2 grid grid-cols-3 gap-2"
                >
                  <template v-for="(a, ai) in m.attachments" :key="ai">
                    <a
                      v-if="a.type?.startsWith('image/')"
                      :href="a.url"
                      target="_blank"
                      rel="noreferrer"
                      class="block"
                    >
                      <img
                        :src="a.url"
                        :alt="a.name"
                        class="w-full h-20 object-cover rounded-xl shadow"
                      />
                    </a>
                    <a
                      v-else
                      :href="a.url"
                      target="_blank"
                      rel="noreferrer"
                      class="text-[11px] underline break-all"
                    >
                      {{ a.name || a.url }}
                    </a>
                  </template>
                </div>
              </div>
            </div>
            <!-- Assistant loading bubble (text-based) -->
            <div v-if="typing" class="flex justify-start">
              <div
                class="rounded-2xl px-3 py-2 max-w-[80%] bg-gradient-to-br from-slate-700/70 to-slate-800/70 border border-slate-600/30 shadow-md text-[12px] text-slate-200 flex items-center gap-1"
              >
                <span>escrevendo</span>
                <span class="typing-dots" aria-hidden="true">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </span>
              </div>
            </div>
          </div>

          <!-- Input row -->
          <div
            class="border-t border-slate-700/60 p-2 flex items-center gap-2 bg-slate-900/30"
          >
            <!-- Hidden file input -->
            <input
              v-if="allowFileUpload"
              ref="chatFileInput"
              type="file"
              class="hidden"
              :multiple="allowMultiple"
              :accept="accept"
              @change="onFilesSelected"
            />
            <!-- Attach button -->
            <button
              v-if="allowFileUpload"
              class="bg-slate-700/70 hover:bg-slate-700 text-slate-200 px-2 h-9 rounded-lg text-xs shadow"
              title="Anexar arquivos"
              @click="openPicker"
            >
              <i class="fa-solid fa-paperclip"></i>
            </button>
            <input
              v-model="input"
              type="text"
              class="flex-1 bg-slate-700/70 border border-slate-600/60 rounded-xl h-9 px-3 text-[13px] outline-none focus:ring-[1.5px] focus:ring-cyan-400/60 focus:border-cyan-400/50 shadow-inner"
              :placeholder="placeholder"
              @keydown.enter.prevent="emitSend"
              @paste="allowPaste ? onPaste : undefined"
            />
            <button
              class="bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-500 hover:to-cyan-600 text-white px-3 h-9 rounded-lg text-sm disabled:opacity-50 shadow"
              :disabled="!input.trim() && attachments.length === 0"
              @click="emitSend"
            >
              Enviar
            </button>
          </div>

          <!-- Attachments preview (to be sent) -->
          <div v-if="attachments.length" class="px-3 pb-2">
            <div class="text-[11px] text-slate-400 mb-1">
              Anexos prontos para enviar
            </div>
            <div class="grid grid-cols-4 gap-2">
              <div
                v-for="(a, i) in attachments"
                :key="i"
                class="relative group"
              >
                <img
                  v-if="a.type.startsWith('image/')"
                  :src="a.url"
                  :alt="a.name"
                  class="w-full h-20 object-cover rounded-xl border border-slate-700 shadow"
                />
                <div
                  v-else
                  class="w-full h-20 text-[11px] p-2 rounded-xl border border-slate-700 bg-slate-700/40 flex items-center justify-center text-center break-all shadow"
                >
                  {{ a.name }}
                </div>
                <button
                  class="absolute -top-2 -right-2 bg-slate-800 border border-slate-600 text-white rounded-full w-5 h-5 opacity-80 hover:opacity-100 shadow"
                  @click="removeAttachment(i)"
                  aria-label="Remover anexo"
                >
                  <i class="fa-solid fa-xmark text-[10px]"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, computed } from "vue";

const props = defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: "ConsorIA" },
  messages: { type: Array, default: () => [] },
  typing: { type: Boolean, default: false },
  size: { type: String, default: "sm" }, // xs|sm|md|lg
  placeholder: { type: String, default: "Pergunte sobre seu consórcio…" },
  accept: {
    type: String,
    default: "image/*,application/pdf,application/*,text/*",
  },
  allowFileUpload: { type: Boolean, default: true },
  allowPaste: { type: Boolean, default: true },
  allowDrop: { type: Boolean, default: true },
  allowMultiple: { type: Boolean, default: true },
});

const emit = defineEmits(["update:show", "send", "close"]);

const input = ref("");
const attachments = ref([]);
const chatScrollRef = ref(null);
const chatFileInput = ref(null);

const show = computed({
  get: () => props.show,
  set: (v) => emit("update:show", v),
});

const sizeClass = computed(() => {
  switch (props.size) {
    case "xs":
      return "sm:max-w-[20rem] md:max-w-sm";
    case "md":
      return "sm:max-w-md md:max-w-lg";
    case "lg":
      return "sm:max-w-lg md:max-w-xl";
    default:
      return "sm:max-w-sm md:max-w-md";
  }
});

watch(
  () => props.messages?.length,
  () => nextTickScroll()
);

function nextTickScroll() {
  requestAnimationFrame(() => {
    const el = chatScrollRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

function emitClose() {
  emit("update:show", false);
  emit("close");
}

function openPicker() {
  chatFileInput.value?.click();
}

function onFilesSelected(e) {
  const files = Array.from(e?.target?.files || []);
  addFiles(files);
  if (e?.target) e.target.value = "";
}

function onPaste(e) {
  const items = Array.from(e.clipboardData?.items || []);
  const files = items
    .filter((it) => it.kind === "file")
    .map((it) => it.getAsFile())
    .filter(Boolean);
  if (files.length) addFiles(files);
}

function onDrop(e) {
  const files = Array.from(e.dataTransfer?.files || []);
  if (files.length) addFiles(files);
}

function addFiles(files) {
  files.forEach((f) => {
    try {
      const url = URL.createObjectURL(f);
      attachments.value.push({
        name: f.name,
        type: f.type || "application/octet-stream",
        url,
        size: f.size || 0,
      });
    } catch (_) {}
  });
  nextTickScroll();
}

function removeAttachment(i) {
  const a = attachments.value[i];
  if (a?.url) URL.revokeObjectURL(a.url);
  attachments.value.splice(i, 1);
}

function emitSend() {
  const text = input.value.trim();
  if (!text && attachments.value.length === 0) return;
  emit("send", { text, attachments: [...attachments.value] });
  input.value = "";
  attachments.value.forEach((a) => a.url && URL.revokeObjectURL(a.url));
  attachments.value = [];
  nextTickScroll();
}

defineExpose({ openPicker, nextTickScroll });
</script>

<style scoped>
.drag-target {
  outline: none;
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.25;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-1px);
  }
}

.typing-dots {
  display: inline-flex;
  gap: 3px;
  margin-left: 2px;
}
.typing-dots .dot {
  width: 4px;
  height: 4px;
  border-radius: 9999px;
  background: #e5e7eb;
  opacity: 0.6;
  animation: blink 1.2s infinite ease-in-out;
}
.typing-dots .dot:nth-child(2) {
  animation-delay: 0.2s;
}
.typing-dots .dot:nth-child(3) {
  animation-delay: 0.4s;
}
</style>
