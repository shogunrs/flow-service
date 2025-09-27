// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-04-03',
  devtools: { enabled: false },
  ssr: false,
  components: false,
  pages: true,
  modules: ['@nuxtjs/tailwindcss'],
  css: ['~/assets/styles/globals.css', '~/assets/styles/main.css'],
  app: {
    layoutTransition: { name: 'layout', mode: 'out-in' }
  },
  runtimeConfig: {
    // server-only
    GEMINI_API_KEY: process.env.GEMINI_API_KEY,
    BANK_API_URL: process.env.BANK_API_URL,
    BANK_API_TOKEN: process.env.BANK_API_TOKEN,
    // do not expose secrets in public runtime config
    public: {
      FLOW_API_BASE: process.env.FLOW_API_BASE || 'http://localhost:8080/api/v1'
    }
  }
})
