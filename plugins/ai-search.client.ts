import AiSearch from '~/components/ui/AiSearch.vue'

export default defineNuxtPlugin((nuxtApp) => {
  // Register globally as <AiSearch />
  nuxtApp.vueApp.component('AiSearch', AiSearch)
})

