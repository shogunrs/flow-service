import { defineEventHandler, readBody } from 'h3'

export default defineEventHandler(async (event) => {
  const body = await readBody<{
    name?: string
    email?: string
    phone?: string
    income?: string
    goal?: string
    amount?: string
    term?: string
    consent?: boolean
  }>(event)

  if (!body?.name || !body?.email || !body?.goal || !body?.consent) {
    return event.node.res.writeHead(400).end(JSON.stringify({ message: 'Dados insuficientes.' }))
  }

  // Minimal processing: log to server. Replace with CRM integration or persistence.
  const lead = { ...body, receivedAt: new Date().toISOString(), ip: event.node.req.socket.remoteAddress }
  console.info('[Lead] Novo lead recebido:', lead)

  return { ok: true }
})

