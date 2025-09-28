<template>
  <component :is="Inner" :pipeline-key="pipelineKey" />
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "#imports";
import { useRouter } from "vue-router";
import { sanitizeProcessKey } from "~/utils/strings/sanitize";
import { useProcessSubmenu } from "~/composables/useProcessMenu";

useHead({ title: "Esteira" });
definePageMeta({ layout: "sidebar" });

const route = useRoute();
const router = useRouter();
const { setLastKey } = useProcessSubmenu();
const pipelineKey = computed(
  () =>
    sanitizeProcessKey(String(route.params.process || "quotaequity")) ||
    "quotaequity"
);

onMounted(() => {
  const current = String(route.params.process || "");
  const normalized = pipelineKey.value;
  if (current && current !== normalized) {
    router.replace({ path: `/esteira/${normalized}`, query: route.query });
  }
  setLastKey(normalized);
});

// Wrapper component that lazy-loads the previous index page logic and passes the key
const Inner = defineAsyncComponent(() =>
  import("~/pages/esteira/index.vue").then((m) => m.default || m)
);

// No extra UI here; the inner page renders the full UI
</script>
