<template>
  <div class="space-y-3">
    <div class="text-[12px] text-slate-300">
      Gerencie etapas e SLAs da esteira.
    </div>
    <div>
      <button
        class="bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded-md text-xs"
        @click="addAndBuild"
      >
        <i class="fa-solid fa-plus mr-1"></i>Nova Etapa
      </button>
    </div>
    <div
      v-for="(stage, i) in localStages"
      :key="stage.id"
      class="flex items-center gap-3"
    >
      <input
        v-model="stage.title"
        class="bg-slate-800/70 border border-slate-700/60 text-slate-200 rounded-md px-3 py-2 flex-1 text-sm"
        placeholder="Título da etapa"
      />
      <!-- Preview chips (edição no modal) -->
      <div class="flex items-center gap-2">
        <span
          class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-[11px] bg-slate-800 border border-slate-700 text-slate-300"
        >
          <i class="fa-solid fa-stopwatch"></i>
          {{ Number(stage.slaDays || 0) }}d
        </span>
        <span
          class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-[11px] bg-slate-800 border border-slate-700 text-slate-300"
        >
          <span
            class="w-3 h-3 rounded-full border border-white/20 color-dot"
            :class="colorClass(stage.color)"
            :data-tip="stage.color || 'sky'"
          ></span>
        </span>
        <span
          class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-[11px] bg-slate-800 border border-slate-700 text-slate-300"
          :title="`${formCount(stage.id)} campos`"
        >
          <i class="fa-regular fa-rectangle-list"></i>
          {{ formCount(stage.id) }}
        </span>
      </div>
      <button
        class="text-orange-400 hover:text-orange-300 text-sm"
        @click="openBuilder(i)"
      >
        Inputs
      </button>
      <button
        class="text-red-400 hover:text-red-300 text-sm"
        @click="remove(i)"
      >
        Remover
      </button>
    </div>

    <!-- Builder Modal -->
    <BaseModal
      v-model="showBuilder"
      title="Configurar Inputs da Etapa"
      size="lg"
      :z-index="60"
      :key="`builder-${currentStage?.id || 'new'}`"
    >
      <StageFormBuilder
        v-model="builderFields"
        :stage-id="currentStage?.id || ''"
        v-model:stageName="builderStageName"
        v-model:stageSla="builderStageSla"
        v-model:stageColor="builderStageColor"
      />
      <template #footer>
        <div class="flex items-center justify-end gap-2">
          <button
            class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-md text-sm"
            @click="closeBuilder"
          >
            Cancelar
          </button>
          <button
            class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-2 rounded-md text-sm"
            @click="saveBuilder"
          >
            Salvar
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
  <Teleport to="body">
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 z-[70] bg-slate-800 text-slate-100 px-3 py-2 rounded-md border border-slate-700 shadow"
    >
      {{ toast.text }}
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch, computed, onMounted, nextTick } from "vue";
import BaseModal from "~/components/ui/BaseModal.vue";
import StageFormBuilder from "~/components/admin/StageFormBuilder.vue";
import { isApiEnabled } from '~/utils/api/index'
import { fetchStageFieldsApi, saveStageFieldsApi } from '~/composables/useStageFields'
import { fetchStagesApi } from '~/composables/useStages'

const props = defineProps({
  stages: { type: Array, default: () => [] },
  pipelineKey: { type: String, default: "quotaequity" },
});
const emit = defineEmits(["update:stages"]);

const localStages = ref(JSON.parse(JSON.stringify(props.stages || [])));

function deepClone(v) {
  try {
    return JSON.parse(JSON.stringify(v));
  } catch (_) {
    return v;
  }
}
function isEqual(a, b) {
  try {
    return JSON.stringify(a) === JSON.stringify(b);
  } catch (_) {
    return false;
  }
}

let syncingFromProps = false;

watch(
  () => props.stages,
  (v) => {
    syncingFromProps = true;
    localStages.value = deepClone(v || []);
    syncingFromProps = false;
  }
);

watch(
  localStages,
  (v) => {
    if (syncingFromProps) return;
    // Evita loop quando valor não muda semanticamente
    if (isEqual(v, props.stages)) return;
    emit("update:stages", v);
  },
  { deep: true }
);

function add() {
  const id = Date.now().toString();
  localStages.value.push({ id, title: "Nova Etapa", slaDays: 1, color: "sky" });
  return id;
}
async function addAndBuild() {
  const id = add();
  await nextTick();
  // try to find index by id after reactive updates
  const idx = localStages.value.findIndex((s) => s.id === id);
  openBuilder(idx >= 0 ? idx : localStages.value.length - 1);
}
function remove(i) {
  localStages.value.splice(i, 1);
}

// Available colors with ring class for preview
const colors = [
  { name: "sky", ring: "bg-sky-500" },
  { name: "indigo", ring: "bg-indigo-500" },
  { name: "amber", ring: "bg-amber-500" },
  { name: "rose", ring: "bg-rose-500" },
  { name: "green", ring: "bg-green-500" },
  { name: "purple", ring: "bg-purple-500" },
  { name: "blue", ring: "bg-blue-500" },
  { name: "orange", ring: "bg-orange-500" },
];

const colorMap = {
  sky: "bg-sky-500",
  indigo: "bg-indigo-500",
  amber: "bg-amber-500",
  rose: "bg-rose-500",
  green: "bg-green-500",
  purple: "bg-purple-500",
  blue: "bg-blue-500",
  orange: "bg-orange-500",
  teal: "bg-teal-500",
  cyan: "bg-cyan-500",
  lime: "bg-lime-500",
  emerald: "bg-emerald-500",
  fuchsia: "bg-fuchsia-500",
  violet: "bg-violet-500",
  pink: "bg-pink-500",
  red: "bg-red-500",
  yellow: "bg-yellow-500",
  slate: "bg-slate-500",
  stone: "bg-stone-500",
  zinc: "bg-zinc-500",
};
function colorClass(name) {
  return colorMap[name] || "bg-sky-500";
}

// Stage input builder state
const showBuilder = ref(false);
const builderIndex = ref(-1);
const builderFields = ref([]);
const builderStageName = ref("");
const builderStageSla = ref(0);
const builderStageColor = ref("sky");
const builderOriginalTitle = ref("");

const currentStage = computed(
  () => localStages.value[builderIndex.value] || null
);

async function openBuilder(index) {
  builderIndex.value = index;
  // Open first to guarantee visibility, then hydrate contents nextTick
  showBuilder.value = true;
  await nextTick();
  const st = localStages.value[index];
  if (st) {
    builderStageName.value = st.title || "";
    builderStageSla.value = Number(st.slaDays || 0);
    builderStageColor.value = st.color || "sky";
    builderOriginalTitle.value = String(st.title || "");
    const map = loadForms();
    builderFields.value = map[st.id] || [];
    // If API is enabled, refresh stage id from backend before fetching fields
    if (isApiEnabled()) {
      let stageId = st.id
      try {
        const remoteStages = await fetchStagesApi(props.pipelineKey)
        if (Array.isArray(remoteStages) && remoteStages.length) {
          const sameId = typeof stageId === 'string' && /^[a-fA-F0-9]{24}$/.test(stageId) && remoteStages.find(r => r.id === stageId)
          if (!sameId) {
            const byIndex = remoteStages[index]
            if (byIndex?.id) stageId = byIndex.id
            else {
              const byTitle = remoteStages.find(r => (r.title || '').trim() === builderOriginalTitle.value.trim())
              if (byTitle?.id) stageId = byTitle.id
            }
          }
        }
      } catch (_) {}
      const isMongoId = typeof stageId === 'string' && /^[a-fA-F0-9]{24}$/.test(stageId)
      if (isMongoId) {
        try {
          const remote = await fetchStageFieldsApi(stageId)
          if (Array.isArray(remote) && remote.length) {
            builderFields.value = remote.map(r => ({
              id: r.id || String(Date.now()),
              label: r.label,
              type: r.type,
              required: !!r.required,
              placeholder: r.placeholder || '',
              options: Array.isArray(r.options) ? r.options : [],
              optionsText: Array.isArray(r.options) ? r.options.join(', ') : ''
            }))
          }
        } catch (_) {}
      }
    }
  } else {
    builderStageName.value = "";
    builderStageSla.value = 0;
    builderStageColor.value = "sky";
    builderFields.value = [];
  }
}
function closeBuilder() {
  showBuilder.value = false;
}
async function saveBuilder() {
  const st = currentStage.value;
  if (!st) return;
  st.title = builderStageName.value || st.title;
  st.slaDays = Number(builderStageSla.value || 0);
  st.color = builderStageColor.value || "sky";
  const map = loadForms();
  map[st.id] = builderFields.value;
  saveForms(map);
  // If API is enabled and the stage is persisted, mirror to backend
  try {
    if (isApiEnabled()) {
      // Refresh stage id to avoid stale ObjectId after replaceForProcess
      let stageId = st.id
      const isMongoId = typeof stageId === 'string' && /^[a-fA-F0-9]{24}$/.test(stageId)
      try {
        const remote = await fetchStagesApi(props.pipelineKey)
        if (Array.isArray(remote) && remote.length) {
          // Prefer exact id match; else try by index, else by title
          const sameId = isMongoId && remote.find(r => r.id === stageId)
          if (!sameId) {
            const byIndex = remote[builderIndex.value]
            if (byIndex?.id) stageId = byIndex.id
            else {
              const byTitle = remote.find(r => (r.title || '').trim() === (builderOriginalTitle.value || '').trim())
              if (byTitle?.id) stageId = byTitle.id
            }
          }
        }
      } catch (_) {}

      const finalIsMongoId = typeof stageId === 'string' && /^[a-fA-F0-9]{24}$/.test(stageId)
      if (finalIsMongoId) {
        const ordered = (builderFields.value || []).map((f, i) => ({ ...f, order: i }))
        saveStageFieldsApi(stageId, ordered)
      }
    }
  } catch (_) {}
  showBuilder.value = false;
}

function loadForms() {
  try {
    const raw = localStorage.getItem(
      `pipeline_stage_forms__${props.pipelineKey}`
    );
    if (raw) return JSON.parse(raw) || {};
  } catch (_) {}
  return {};
}
function saveForms(map) {
  try {
    localStorage.setItem(
      `pipeline_stage_forms__${props.pipelineKey}`,
      JSON.stringify(map)
    );
  } catch (_) {}
}

function formCount(id) {
  try {
    const raw = localStorage.getItem(`pipeline_stage_forms__${props.pipelineKey}`);
    if (!raw) return 0;
    const map = JSON.parse(raw) || {};
    const arr = map[id];
    return Array.isArray(arr) ? arr.length : 0;
  } catch (_) { return 0 }
}

// expose forms loader for parent (future use)
defineExpose({ loadForms });

// Toast helper
const toast = ref({ show: false, text: "" });
function notify(msg) {
  toast.value = { show: true, text: msg };
  setTimeout(() => (toast.value.show = false), 2200);
}
</script>

<style scoped>
.color-dot {
  position: relative;
}
.color-dot::after {
  content: attr(data-tip);
  position: absolute;
  top: -30px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 10px;
  color: #e5e7eb; /* slate-200 */
  background: rgba(15, 23, 42, 0.9); /* slate-900/90 */
  border: 1px solid rgba(51, 65, 85, 0.8); /* slate-700/80 */
  padding: 2px 6px;
  border-radius: 6px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.12s ease;
}
.color-dot:hover::after,
.color-dot:focus-visible::after {
  opacity: 1;
}
</style>
