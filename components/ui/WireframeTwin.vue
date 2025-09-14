<template>
  <div class="relative">
    <svg
      :width="width"
      :height="height"
      viewBox="0 0 800 360"
      fill="none"
      class="block"
    >
      <defs>
        <linearGradient id="wf-stroke" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#a78bfa" />
          <stop offset="50%" stop-color="#60a5fa" />
          <stop offset="100%" stop-color="#22d3ee" />
        </linearGradient>
        <linearGradient id="wf-glow" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#a78bfa33" />
          <stop offset="100%" stop-color="#22d3ee22" />
        </linearGradient>
      </defs>

      <!-- Center link -->
      <g class="wf">
        <line x1="300" y1="180" x2="500" y2="180" class="wf-line anim" />
      </g>

      <!-- Floating particles -->
      <g>
        <circle
          v-for="(p, idx) in parts"
          :key="idx"
          :cx="p.x"
          :cy="p.y"
          :r="p.r"
          fill="url(#wf-glow)"
        ></circle>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{ width?: number; height?: number; animate?: boolean }>(),
  {
    width: 800,
    height: 360,
    animate: true,
  }
);

const layers = 9;
const size = (i: number) => 30 + i * 16;

const animate = computed(() => props.animate);

const parts = Array.from({ length: 16 }).map((_, i) => ({
  x: 380 + Math.cos(i * 0.7) * (80 + (i % 5) * 12),
  y: 180 + Math.sin(i * 1.2) * (60 + (i % 4) * 10),
  r: 2 + (i % 3),
}));
</script>

<style scoped>
.wf-line {
  stroke: url(#wf-stroke);
  stroke-width: 1.4;
  fill: transparent;
  vector-effect: non-scaling-stroke;
  stroke-dasharray: 8 8;
}
.wf-line.anim {
  animation: dash 6s linear infinite;
}
.wf {
  filter: drop-shadow(0 0 18px rgba(167, 139, 250, 0.15));
}

@keyframes dash {
  to {
    stroke-dashoffset: -160;
  }
}
</style>
