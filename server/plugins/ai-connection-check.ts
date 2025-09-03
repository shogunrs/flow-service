import { ofetch } from 'ofetch'
import { useRuntimeConfig } from '#imports'

export default (nitroApp: any) => {
  // Run after server is ready
  setTimeout(async () => {
    const cfg = useRuntimeConfig()
    const logPrefix = '[ConsorIA]'

    // Check Gemini connectivity
    if (cfg.GEMINI_API_KEY) {
      try {
        await ofetch(
          `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest`,
          { method: 'GET', query: { key: cfg.GEMINI_API_KEY }, timeout: 4000 }
        )
        console.info(`${logPrefix} Provedor de IA: conexão OK`)
      } catch (e: any) {
        console.warn(
          `${logPrefix} Provedor de IA: falha no teste de conexão (${e?.status || e?.code || 'erro'}).`)
      }
    } else {
      console.info(`${logPrefix} Provedor de IA: sem credenciais configuradas`)
    }

    // Check Bank backend connectivity
    if (cfg.BANK_API_URL) {
      try {
        await ofetch(cfg.BANK_API_URL as string, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(cfg.BANK_API_TOKEN ? { Authorization: `Bearer ${cfg.BANK_API_TOKEN}` } : {})
          },
          body: { prompt: 'ping' },
          timeout: 4000
        })
        console.info(`${logPrefix} Backend: conexão OK`)
      } catch (e: any) {
        console.warn(`${logPrefix} Backend: falha no teste de conexão (${e?.status || e?.code || 'erro'}).`)
      }
    } else {
      console.info(`${logPrefix} Backend: não configurado`)
    }
  }, 0)
}
