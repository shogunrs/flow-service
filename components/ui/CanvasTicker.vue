<template>
  <canvas ref="canvas" class="absolute inset-x-0 bottom-0 w-full h-full pointer-events-none"></canvas>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  text?: string
  messages?: string[]
  speed?: number          // css px per frame
  color?: string          // text fill color
  glow?: number           // blur radius in px
  fontSize?: number       // css px (scaled by dpr)
  weight?: number | string
  align?: 'top' | 'center' | 'bottom'
  offset?: number         // css px from chosen edge
  fontFamily?: string     // canvas font family
}>(), {
  text: '',
  messages: () => ['Taxas especiais hoje', 'Atendimento 24h', 'Segurança e transparência'],
  speed: 0.8,
  color: '#00E5FF',
  glow: 6,
  fontSize: 13,
  weight: 700,
  align: 'bottom',
  offset: 6,
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif'
})

const canvas = ref<HTMLCanvasElement | null>(null)
let raf = 0
let dpr = 1
let x = 0
let textWidth = 0
let gap = 80 // css px gap between repeats

function getMessage(): string {
  return (props.text && props.text.trim().length)
    ? props.text
    : props.messages.join('   •   ')
}

function resize() {
  const c = canvas.value!
  dpr = Math.max(1, window.devicePixelRatio || 1)
  const rect = c.parentElement?.getBoundingClientRect() || { width: 800, height: 80 }
  c.width = Math.floor(rect.width * dpr)
  c.height = Math.floor(rect.height * dpr)
  c.style.width = rect.width + 'px'
  c.style.height = rect.height + 'px'
  measure()
}

function measure() {
  const c = canvas.value!
  const ctx = c.getContext('2d')!
  ctx.resetTransform?.()
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  ctx.font = `${props.weight} ${props.fontSize}px ${props.fontFamily}`
  textWidth = ctx.measureText(getMessage()).width
  x = 0
}

function draw() {
  const c = canvas.value!
  const ctx = c.getContext('2d')!
  ctx.clearRect(0,0,c.width,c.height)

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  const h = c.height / dpr
  const y = props.align === 'top' ? (props.offset + props.fontSize)
           : props.align === 'center' ? (h/2 + props.fontSize/2)
           : (h - props.offset)

  // subtle baseline line (optional aesthetic)
  ctx.strokeStyle = 'rgba(0,229,255,0.12)'
  ctx.lineWidth = 1
  ctx.beginPath(); ctx.moveTo(0, y + 4); ctx.lineTo(c.width, y + 4); ctx.stroke()

  // glow text
  ctx.fillStyle = props.color
  ctx.shadowColor = props.color
  ctx.shadowBlur = props.glow
  ctx.font = `${props.weight} ${props.fontSize}px ${props.fontFamily}`

  // tile twice for seamless scroll
  const msg = getMessage()
  const total = textWidth + gap
  let start = x
  while (start < c.width / dpr + total) {
    ctx.fillText(msg, start, y)
    start += total
  }

  // advance
  x -= props.speed
  if (x <= -total) x += total

  raf = requestAnimationFrame(draw)
}

onMounted(() => { resize(); window.addEventListener('resize', resize); draw() })
onBeforeUnmount(() => { cancelAnimationFrame(raf); window.removeEventListener('resize', resize) })

watch(() => [props.text, props.messages, props.fontSize, props.weight], () => { measure() })
</script>

<style scoped>
:host, canvas { display: block }
</style>
