<template>
  <canvas ref="canvas" class="absolute inset-0 w-full h-full pointer-events-none"></canvas>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'

// Financial quotes ribbon (symbols, price, change) scrolling horizontally
const props = withDefaults(defineProps<{
  speed?: number      // px per frame
  symbols?: string[]  // e.g. ['VALE3','PETR4','ITUB4']
  fontSize?: number
  fontFamily?: string
}>(), {
  speed: 0.8,
  symbols: () => ['VALE3', 'PETR4', 'ITUB4', 'BBDC4', 'BBAS3', 'MGLU3', 'WEGE3', 'ABEV3'],
  fontSize: 12,
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif'
})

const canvas = ref<HTMLCanvasElement | null>(null)
let raf = 0
let dpr = 1

type Quote = { sym:string; price:number; chg:number; w:number }
let quotes: Quote[] = []
let offset = 0
const gap = 24 // css px between pills

function rand(min:number,max:number){ return Math.random()*(max-min)+min }

function resize(){
  const c = canvas.value!
  dpr = Math.max(1, window.devicePixelRatio || 1)
  const rect = c.parentElement?.getBoundingClientRect() || { width: 800, height: 80 }
  c.width = Math.floor(rect.width * dpr)
  c.height = Math.floor(rect.height * dpr)
  c.style.width = rect.width + 'px'
  c.style.height = rect.height + 'px'
  initQuotes()
}

function initQuotes(){
  const c = canvas.value!
  const ctx = c.getContext('2d')!
  ctx.setTransform(dpr,0,0,dpr,0,0)
  ctx.font = `600 ${props.fontSize}px ${props.fontFamily}`
  quotes = props.symbols.map(sym => ({
    sym,
    price: rand(10,200),
    chg: rand(-2,2),
    w: 0
  }))
  for (const q of quotes) q.w = pillWidth(ctx, q)
}

function pillWidth(ctx:CanvasRenderingContext2D, q:Quote){
  const txt = `${q.sym}  ${q.price.toFixed(2)}  ${(q.chg>=0?'+':'')}${q.chg.toFixed(2)}%`
  return Math.ceil(ctx.measureText(txt).width) + 34 /* paddings & icon */
}

let lastUpdate = 0
function updateQuotes(time:number){
  if (time - lastUpdate < 900) return
  lastUpdate = time
  // random small variation
  for (const q of quotes){
    const delta = rand(-0.4, 0.4)
    q.price = Math.max(0.01, q.price + delta)
    q.chg = Math.min(9, Math.max(-9, q.chg + delta*0.1))
  }
}

function draw(time:number){
  const c = canvas.value!
  const ctx = c.getContext('2d')!
  ctx.clearRect(0,0,c.width,c.height)
  ctx.setTransform(dpr,0,0,dpr,0,0)
  updateQuotes(time)

  const h = c.height/dpr
  const y = Math.max(props.fontSize + 8, h*0.7)

  // scroll
  offset -= props.speed
  const totalWidths = quotes.reduce((acc,q)=>acc+q.w+gap, 0)
  if (-offset > totalWidths) offset += totalWidths

  let x = offset
  const GREEN = '#22c55e', RED = '#ef4444', BORDER = 'rgba(148,163,184,0.25)'
  ctx.font = `600 ${props.fontSize}px ${props.fontFamily}`
  while (x < c.width/dpr + totalWidths){
    for (const q of quotes){
      const up = q.chg >= 0
      const bg = up ? 'rgba(34,197,94,0.12)' : 'rgba(239,68,68,0.12)'
      const fg = up ? GREEN : RED
      const pillW = q.w
      const pillH = props.fontSize + 10
      const rx = 10

      // pill container
      roundRect(ctx, x+6, y - pillH + 2, pillW, pillH, rx)
      ctx.fillStyle = bg
      ctx.fill()
      ctx.strokeStyle = BORDER
      ctx.stroke()

      // symbol
      ctx.fillStyle = '#e5e7eb'
      ctx.fillText(q.sym, x+14, y-6)

      // price
      ctx.fillStyle = '#cbd5e1'
      ctx.fillText(q.price.toFixed(2), x+14 + ctx.measureText(q.sym+'  ').width, y-6)

      // change
      const arrow = up ? '▲' : '▼'
      const chgTxt = `  ${arrow} ${(up?'+':'')}${q.chg.toFixed(2)}%`
      ctx.fillStyle = fg
      ctx.fillText(chgTxt, x+14 + ctx.measureText(`${q.sym}  ${q.price.toFixed(2)}`).width, y-6)

      x += pillW + gap
    }
  }

  raf = requestAnimationFrame(draw)
}

function roundRect(ctx:CanvasRenderingContext2D, x:number, y:number, w:number, h:number, r:number){
  const rr = Math.min(r, h/2, w/2)
  ctx.beginPath()
  ctx.moveTo(x+rr,y)
  ctx.arcTo(x+w, y, x+w, y+h, rr)
  ctx.arcTo(x+w, y+h, x, y+h, rr)
  ctx.arcTo(x, y+h, x, y, rr)
  ctx.arcTo(x, y, x+w, y, rr)
  ctx.closePath()
}

onMounted(()=>{ resize(); window.addEventListener('resize', resize); raf = requestAnimationFrame(draw) })
onBeforeUnmount(()=>{ cancelAnimationFrame(raf); window.removeEventListener('resize', resize) })

watch(() => props.symbols, () => initQuotes())
</script>

<style scoped>
:host, canvas { display:block }
</style>
