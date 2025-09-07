<template>
  <div class="space-y-3">
    <!-- Preview card -->
    <div
      class="rounded-xl border border-slate-700/60 bg-slate-900/40 p-3 flex items-center justify-between"
    >
      <div class="flex items-center gap-2">
        <span
          class="w-2 h-6 rounded-full"
          :class="colorClass(localColor)"
        ></span>
        <div>
          <div class="text-white text-sm font-semibold">
            {{ localName || "Nova Etapa" }}
          </div>
          <div class="text-[11px] text-slate-300">
            SLA {{ Number(localSla || 0) }}d
          </div>
        </div>
      </div>
      <div class="text-[11px] text-slate-400">Pré-visualização</div>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
      <div>
        <label class="text-[12px] text-slate-300">Nome da Etapa</label>
        <input
          v-model="localName"
          class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
          placeholder="Ex.: Documentação"
        />
      </div>
      <div>
        <label class="text-[12px] text-slate-300">ID (somente leitura)</label>
        <input
          :value="stageId"
          disabled
          class="mt-1 w-full bg-slate-800/40 border border-slate-700/60 text-slate-400 rounded-md px-3 py-2 text-sm"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-2">
      <div>
        <label class="text-[12px] text-slate-300">SLA (dias)</label>
        <input
          v-model="localSla"
          type="text"
          min="0"
          class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
          v-numeric="{ allowDecimal: false, maxLength: 3 }"
        />
      </div>
      <div class="md:col-span-2">
        <label class="text-[12px] text-slate-300">Cor da Coluna</label>
        <div class="mt-1 flex items-center gap-2 flex-wrap">
          <button
            v-for="c in colors"
            :key="c.name"
            class="w-7 h-7 rounded-full border border-slate-600 outline-none focus:ring-2 focus:ring-white/30 color-dot"
            :class="[
              c.ring,
              localColor === c.name
                ? 'ring-2 ring-white/60 scale-110'
                : 'opacity-85',
            ]"
            :title="c.name"
            :data-tip="c.name"
            @click="localColor = c.name"
          />
        </div>
      </div>
    </div>

    <div class="flex items-center justify-between mt-1">
      <div class="text-[12px] text-slate-300">Campos do formulário</div>
      <button
        class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
        @click="addField"
      >
        <i class="fa-solid fa-plus mr-1"></i>Novo Campo
      </button>
    </div>

    <div v-if="localFields.length === 0" class="text-[12px] text-slate-400">
      Nenhum campo adicionado.
    </div>

    <div
      v-for="(f, i) in localFields"
      :key="f.id"
      class="rounded-lg border border-slate-700/60 bg-slate-900/40 p-3 space-y-2"
    >
      <div class="grid grid-cols-1 md:grid-cols-3 gap-2">
        <div>
          <label class="text-[12px] text-slate-300">Rótulo</label>
          <input
            v-model="f.label"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="Ex.: CPF"
          />
        </div>
        <div>
          <label class="text-[12px] text-slate-300">Tipo</label>
          <select
            v-model="f.type"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
          >
            <option v-for="t in types" :key="t.value" :value="t.value">
              {{ t.label }}
            </option>
          </select>
        </div>
        <div class="flex items-end gap-2">
          <label
            class="inline-flex items-center gap-2 text-[12px] text-slate-300"
          >
            <input
              type="checkbox"
              v-model="f.required"
              class="accent-orange-500"
            />
            Obrigatório
          </label>
          <button
            class="ml-auto text-red-400 hover:text-red-300 text-sm"
            @click="removeField(i)"
          >
            Remover
          </button>
        </div>
      </div>
      <div v-if="f.type === 'select'" class="grid grid-cols-1 gap-2">
        <div>
          <label class="text-[12px] text-slate-300"
            >Opções (separadas por vírgula)</label
          >
          <input
            v-model="f.optionsText"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
            placeholder="Ex.: Aprovado, Pendente, Reprovado"
          />
        </div>
      </div>
      <div
        v-if="f.type === 'text' || f.type === 'number'"
        class="grid grid-cols-1 md:grid-cols-2 gap-2"
      >
        <div>
          <label class="text-[12px] text-slate-300">Placeholder</label>
          <input
            v-model="f.placeholder"
            class="mt-1 w-full bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 text-sm"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  stageName: { type: String, default: "" },
  stageId: { type: String, required: true },
  stageSla: { type: Number, default: 0 },
  stageColor: { type: String, default: "sky" },
});
const emit = defineEmits([
  "update:modelValue",
  "update:stageName",
  "update:stageSla",
  "update:stageColor",
]);

const localFields = ref((props.modelValue || []).map(normalize));

function deepClone(v) {
  try { return JSON.parse(JSON.stringify(v)) } catch (_) { return v }
}
function isEqual(a, b) {
  try { return JSON.stringify(a) === JSON.stringify(b) } catch (_) { return false }
}

let syncingFromProps = false
const localName = ref(props.stageName || "");
const localSla = ref(String(props.stageSla ?? ''));
const localColor = ref(props.stageColor || "sky");

watch(
  () => props.modelValue,
  (v) => {
    syncingFromProps = true
    localFields.value = (v || []).map(normalize)
    syncingFromProps = false
  },
  { deep: true }
);
watch(
  () => props.stageName,
  (v) => {
    localName.value = v || "";
  }
);
watch(
  () => props.stageSla,
  (v) => {
    localSla.value = String(v ?? '');
  }
);
watch(
  () => props.stageColor,
  (v) => {
    localColor.value = v || "sky";
  }
);
watch(
  localFields,
  (v) => {
    if (syncingFromProps) return
    const next = v.map(denormalize)
    if (isEqual(next, props.modelValue)) return
    emit('update:modelValue', next)
  },
  { deep: true }
);
watch(localName, (v) => emit("update:stageName", v));
watch(localSla, (v) => emit("update:stageSla", Number(v || 0)));
watch(localColor, (v) => emit("update:stageColor", v || "sky"));

const types = [
  { value: "text", label: "Texto" },
  { value: "number", label: "Número" },
  { value: "date", label: "Data" },
  { value: "select", label: "Lista (Select)" },
  { value: "file", label: "Arquivo" },
];

// Paleta de cores disponível (bullet-only)
const colors = [
  { name: 'sky', ring: 'bg-sky-500' },
  { name: 'indigo', ring: 'bg-indigo-500' },
  { name: 'amber', ring: 'bg-amber-500' },
  { name: 'rose', ring: 'bg-rose-500' },
  { name: 'green', ring: 'bg-green-500' },
  { name: 'purple', ring: 'bg-purple-500' },
  { name: 'blue', ring: 'bg-blue-500' },
  { name: 'orange', ring: 'bg-orange-500' },
  { name: 'teal', ring: 'bg-teal-500' },
  { name: 'cyan', ring: 'bg-cyan-500' },
  { name: 'lime', ring: 'bg-lime-500' },
  { name: 'emerald', ring: 'bg-emerald-500' },
  { name: 'fuchsia', ring: 'bg-fuchsia-500' },
  { name: 'violet', ring: 'bg-violet-500' },
  { name: 'pink', ring: 'bg-pink-500' },
  { name: 'red', ring: 'bg-red-500' },
  { name: 'yellow', ring: 'bg-yellow-500' },
  { name: 'slate', ring: 'bg-slate-500' },
  { name: 'stone', ring: 'bg-stone-500' },
  { name: 'zinc', ring: 'bg-zinc-500' }
]

function addField() {
  localFields.value.push(
    normalize({
      id: String(Date.now()),
      label: "Novo Campo",
      type: "text",
      required: false,
    })
  );
}
function removeField(i) {
  localFields.value.splice(i, 1);
}

function normalize(f) {
  return {
    id: f.id || String(Date.now()),
    label: f.label || "",
    type: f.type || "text",
    required: !!f.required,
    placeholder: f.placeholder || "",
    options: Array.isArray(f.options) ? f.options : [],
    optionsText: Array.isArray(f.options)
      ? f.options.join(", ")
      : f.optionsText || "",
  };
}
function denormalize(f) {
  return {
    id: f.id,
    label: f.label,
    type: f.type,
    required: !!f.required,
    placeholder: f.placeholder || "",
    options:
      f.type === "select"
        ? (f.optionsText || "")
            .split(",")
            .map((s) => s.trim())
            .filter(Boolean)
        : [],
  };
}

const colorMap = {
  sky: 'bg-sky-500', indigo: 'bg-indigo-500', amber: 'bg-amber-500', rose: 'bg-rose-500',
  green: 'bg-green-500', purple: 'bg-purple-500', blue: 'bg-blue-500', orange: 'bg-orange-500',
  teal: 'bg-teal-500', cyan: 'bg-cyan-500', lime: 'bg-lime-500', emerald: 'bg-emerald-500',
  fuchsia: 'bg-fuchsia-500', violet: 'bg-violet-500', pink: 'bg-pink-500', red: 'bg-red-500',
  yellow: 'bg-yellow-500', slate: 'bg-slate-500', stone: 'bg-stone-500', zinc: 'bg-zinc-500'
}
function colorClass(name) { return colorMap[name] || 'bg-sky-500' }
</script>

<style scoped>
.color-dot { position: relative; }
.color-dot::after {
  content: attr(data-tip);
  position: absolute;
  top: -30px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 10px;
  color: #e5e7eb; /* slate-200 */
  background: rgba(15,23,42,0.9); /* slate-900/90 */
  border: 1px solid rgba(51,65,85,0.8); /* slate-700/80 */
  padding: 2px 6px;
  border-radius: 6px;
  opacity: 0;
  pointer-events: none;
  transition: opacity .12s ease;
}
.color-dot:hover::after,
.color-dot:focus-visible::after { opacity: 1; }
</style>
