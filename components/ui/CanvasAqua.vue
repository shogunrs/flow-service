<template>
  <canvas ref="canvas" class="absolute inset-0 w-full h-full pointer-events-none"></canvas>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'

const props = withDefaults(defineProps<{ density?: number }>(), { density: 28 })
const canvas = ref<HTMLCanvasElement | null>(null)
let raf = 0

type P = { x:number; y:number; vx:number; vy:number; r:number; c:string }
let parts: P[] = []

function rand(min:number, max:number){ return Math.random()*(max-min)+min }

function resize(){
  const c = canvas.value!
  const dpr = Math.max(1, window.devicePixelRatio || 1)
  const rect = c.parentElement?.getBoundingClientRect()
  if (!rect) return
  c.width = Math.floor(rect.width * dpr)
  c.height = Math.floor(rect.height * dpr)
  c.style.width = rect.width + 'px'
  c.style.height = rect.height + 'px'
}

function init(){
  const c = canvas.value!
  const ctx = c.getContext('2d')!
  const cols = ['#00E5FF', '#3B82F6', '#06B6D4']
  parts = Array.from({ length: props.density }, () => ({
    x: rand(0, c.width), y: rand(0, c.height),
    vx: rand(-0.12, 0.12), vy: rand(-0.12, 0.12),
    r: rand(6, 18), c: cols[Math.floor(Math.random()*cols.length)]
  }))

  const loop = () => {
    raf = requestAnimationFrame(loop)
    ctx.clearRect(0,0,c.width,c.height)
    ctx.globalCompositeOperation = 'lighter'
    for (const p of parts){
      p.x += p.vx; p.y += p.vy
      if (p.x < -20) p.x = c.width+20; if (p.x>c.width+20) p.x = -20
      if (p.y < -20) p.y = c.height+20; if (p.y>c.height+20) p.y = -20
      const g = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.r)
      g.addColorStop(0, p.c + 'AA')
      g.addColorStop(1, p.c + '00')
      ctx.fillStyle = g
      ctx.beginPath(); ctx.arc(p.x,p.y,p.r,0,Math.PI*2); ctx.fill()
    }
  }
  loop()
}

onMounted(() => { resize(); init(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { cancelAnimationFrame(raf); window.removeEventListener('resize', resize) })
</script>

<style scoped>
:host, canvas { display: block }
</style>

