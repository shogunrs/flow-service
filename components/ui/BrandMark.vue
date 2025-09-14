<template>
  <div class="flex items-center gap-2 select-none">
    <img
      :src="src"
      :srcset="srcset"
      :alt="alt"
      :width="pixel"
      :height="pixel"
      class="rounded-[6px] shadow-[0_0_24px_rgba(0,229,255,0.15)]"
      :class="imgClass"
      loading="lazy"
      decoding="async"
    />
    <span
      v-if="label"
      class="text-sm font-semibold tracking-wide text-slate-200"
    >
      {{ label }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    size?: "xs" | "sm" | "md" | "lg" | number;
    label?: string;
    alt?: string;
  }>(),
  {
    size: "md",
    alt: "FourCon",
  }
);

const pixel = computed(() => {
  if (typeof props.size === "number") return props.size;
  switch (props.size) {
    case "xs":
      return 20;
    case "sm":
      return 24;
    case "md":
      return 32;
    case "lg":
      return 70;
    default:
      return 32;
  }
});

const src = "/brand/logo.png";
const srcset = "/brand/logo.png 1x";
const imgClass = computed(() => {
  const p = pixel.value;
  // subtle cyan glow for dark background
  return `bg-transparent` + (p >= 100 ? " ring-1" : "");
});
</script>

<style scoped></style>
