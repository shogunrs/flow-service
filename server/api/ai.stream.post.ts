import { defineEventHandler, readBody } from 'h3'

type ChatAttachment = { name?: string; type?: string; url?: string; size?: number }

function stripMarkdown(input: string): string {
  let s = input || ''
  // code blocks / inline code
  s = s.replace(/```[\s\S]*?```/g, '')
  s = s.replace(/`([^`]+)`/g, '$1')
  // images ![alt](url) and links [text](url)
  s = s.replace(/!\[[^\]]*\]\([^\)]*\)/g, '')
  s = s.replace(/\[([^\]]+)\]\([^\)]*\)/g, '$1')
  // headings, lists, blockquotes, tables
  s = s.replace(/^\s{0,3}#{1,6}\s+/gm, '')
  s = s.replace(/^\s{0,3}[-*+]\s+/gm, '')
  s = s.replace(/^\s{0,3}>\s?/gm, '')
  s = s.replace(/\|/g, ' ')
  // emphasis
  s = s.replace(/[*_]{1,3}([^*_]+)[*_]{1,3}/g, '$1')
  // collapse whitespace
  s = s.replace(/\s+\n/g, '\n').replace(/\n{3,}/g, '\n\n')
  return s.trim()
}

function splitSentences(text: string): string[] {
  const parts = (text || '').split(/([.!?]+\s+)/)
  const out: string[] = []
  for (let i = 0; i < parts.length; i += 2) {
    const a = parts[i] || ''
    const b = parts[i + 1] || ''
    const sentence = (a + b).trim()
    if (sentence) out.push(sentence)
  }
  return out
}

export default defineEventHandler(async (event) => {
  const body = await readBody<{ text?: string; attachments?: ChatAttachment[] }>(event)
  const prompt = (body?.text || '').toString().trim()
  const attachments = body?.attachments || []

  const cfg = useRuntimeConfig()
  const geminiKey = cfg.GEMINI_API_KEY as string | undefined
  const bankUrl = cfg.BANK_API_URL as string | undefined
  const bankToken = cfg.BANK_API_TOKEN as string | undefined

  // Prepare response for streaming (NDJSON chunks)
  const res = event.node.res
  res.setHeader('Content-Type', 'text/plain; charset=utf-8')
  res.setHeader('Transfer-Encoding', 'chunked')
  res.setHeader('Cache-Control', 'no-cache')

  const write = (obj: any) => {
    res.write(JSON.stringify(obj) + '\n')
  }

  // Nothing to process
  if (!prompt && attachments.length === 0) {
    write({ type: 'text', delta: 'Me manda algo para eu consorciar ideias! ' })
    write({ type: 'done' })
    res.end()
    return
  }

  // Summarize attachments for provider context only (not streamed)
  const attachSummary = attachments.length
    ? `\n\n[Anexos]\n${attachments
        .map((a, i) => `(${i + 1}) ${a.name || 'arquivo'}${a.type ? ` [${a.type}]` : ''}${a.url ? ` -> ${a.url}` : ''}`)
        .join('\n')}`
    : ''

  // Helper to enforce plain text + 10 sentence limit while streaming
  let sentenceCount = 0
  const streamText = (chunk: string) => {
    if (!chunk) return
    const clean = stripMarkdown(chunk)
    if (!clean) return
    const sentences = splitSentences(clean)
    for (const s of sentences) {
      if (sentenceCount >= 10) break
      sentenceCount++
      write({ type: 'text', delta: s + ' ' })
    }
  }

  // Provider selection: prefer Gemini, then bank, else offline message
  if (geminiKey) {
    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:streamGenerateContent?alt=sse&key=${encodeURIComponent(
        geminiKey
      )}`
      const systemPreamble =
        'Você é uma assistente focada em consórcio e esteira. Responda em texto simples (sem Markdown), direto e objetivo.'
      const payload = {
        contents: [
          { role: 'user', parts: [{ text: systemPreamble }] },
          { role: 'user', parts: [{ text: prompt + attachSummary }] }
        ]
      }

      const upstream = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })

      if (!upstream.body) throw new Error('No stream body from provider')

      const reader = upstream.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        let idx
        while ((idx = buffer.indexOf('\n')) >= 0) {
          const line = buffer.slice(0, idx).trim()
          buffer = buffer.slice(idx + 1)
          if (!line) continue
          if (line.startsWith('data:')) {
            const jsonStr = line.slice(5).trim()
            if (jsonStr === '[DONE]') continue
            try {
              const ev = JSON.parse(jsonStr)
              const text = ev?.candidates?.[0]?.content?.parts?.[0]?.text || ''
              if (text) streamText(text)
              if (sentenceCount >= 10) break
            } catch (_) {}
          }
        }
        if (sentenceCount >= 10) break
      }
      write({ type: 'done' })
      res.end()
      return
    } catch (_) {
      // fall through to offline message
    }
  }

  if (bankUrl) {
    try {
      const result: any = await $fetch(bankUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(bankToken ? { Authorization: `Bearer ${bankToken}` } : {})
        },
        body: { prompt: prompt + attachSummary }
      })
      const full = stripMarkdown(result?.text || result?.message || JSON.stringify(result))
      const sentences = splitSentences(full).slice(0, 10)
      for (const s of sentences) write({ type: 'text', delta: s + ' ' })
      write({ type: 'done' })
      res.end()
      return
    } catch (_) {
      // fall through to offline message
    }
  }

  // Offline humorous response (streamed)
  const offline = 'Estou offline como assembleia sem quórum. Configure a integração e eu respondo voando.'
  const parts = splitSentences(offline)
  for (const p of parts) write({ type: 'text', delta: p + ' ' })
  write({ type: 'done' })
  res.end()
})

