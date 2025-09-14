<template>
  <canvas
    ref="canvas"
    class="absolute inset-0 w-full h-full pointer-events-none"
  ></canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from "vue";

type Item = { text: string; color?: string };

const props = withDefaults(
  defineProps<{
    items: Item[];
    speed?: number; // px per frame
    fontSize?: number; // css px
    fontFamily?: string;
    weight?: number | string;
    glow?: number;
    align?: "top" | "center" | "bottom";
    offset?: number;
  }>(),
  {
    items: () => [],
    speed: 0.8,
    fontSize: 13,
    fontFamily: "Inter, system-ui, -apple-system, sans-serif",
    weight: 700,
    glow: 5,
    align: "bottom",
    offset: 4,
  }
);

const canvas = ref<HTMLCanvasElement | null>(null);
let dpr = 1;
let raf = 0;
let x = 0;
let widths: number[] = [];
const gap = 32; // px

function resize() {
  const c = canvas.value!;
  dpr = Math.max(1, window.devicePixelRatio || 1);
  const rect = c.parentElement?.getBoundingClientRect() || {
    width: 800,
    height: 64,
  };
  c.width = Math.floor(rect.width * dpr);
  c.height = Math.floor(rect.height * dpr);
  c.style.width = rect.width + "px";
  c.style.height = rect.height + "px";
  measure();
}

function measure() {
  const c = canvas.value!;
  const ctx = c.getContext("2d")!;
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  ctx.font = `${props.weight} ${props.fontSize}px ${props.fontFamily}`;
  widths = props.items.map((i) => Math.ceil(ctx.measureText(i.text).width));
  x = 0;
}

function draw() {
  const c = canvas.value!;
  const ctx = c.getContext("2d")!;
  ctx.clearRect(0, 0, c.width, c.height);
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

  const h = c.height / dpr;
  const y =
    props.align === "top"
      ? props.offset + props.fontSize
      : props.align === "center"
        ? h / 2 + props.fontSize / 2
        : h - props.offset;

  ctx.font = `${props.weight} ${props.fontSize}px ${props.fontFamily}`;
  ctx.shadowBlur = props.glow;

  const total = widths.reduce((a, b) => a + b, 0) + gap * props.items.length;
  let start = x;
  const items = props.items;
  while (start < c.width / dpr + total) {
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (!item) continue;
      const w = widths[i] ?? 0;
      ctx.fillStyle = item.color || "#00E5FF";
      ctx.shadowColor = item.color || "#00E5FF";
      ctx.fillText(item.text, start, y);
      start += w + gap;
    }
  }

  x -= props.speed;
  if (-x > total) x += total;

  raf = requestAnimationFrame(draw);
}

onMounted(() => {
  resize();
  window.addEventListener("resize", resize);
  raf = requestAnimationFrame(draw);
});
onBeforeUnmount(() => {
  cancelAnimationFrame(raf);
  window.removeEventListener("resize", resize);
});

watch(
  () => [props.items, props.fontSize, props.fontFamily, props.weight],
  () => measure(),
  { deep: true }
);
</script>

<style scoped>
:host,
canvas {
  display: block;
}
</style>
