import { ref, computed } from "vue";

interface Process {
  key: string;
  name: string;
  active: boolean;
}

const processes = ref<Process[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

const CACHE_KEY = "esteira-processes";
const CACHE_TIMESTAMP_KEY = "esteira-processes-timestamp";
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutos

export const useProcesses = () => {
  // Carregar do cache se existir e for válido
  const loadFromCache = () => {
    if (typeof window === "undefined") return false;

    const cached = localStorage.getItem(CACHE_KEY);
    const timestamp = localStorage.getItem(CACHE_TIMESTAMP_KEY);

    if (cached && timestamp) {
      const age = Date.now() - parseInt(timestamp);
      if (age < CACHE_DURATION) {
        processes.value = JSON.parse(cached);
        console.log("🗄️ Processos carregados do cache");
        return true;
      }
    }
    return false;
  };

  // Salvar no cache
  const saveToCache = (data: Process[]) => {
    if (typeof window === "undefined") return;

    localStorage.setItem(CACHE_KEY, JSON.stringify(data));
    localStorage.setItem(CACHE_TIMESTAMP_KEY, Date.now().toString());
  };

  // Limpar cache
  const clearCache = () => {
    if (typeof window === "undefined") return;

    localStorage.removeItem(CACHE_KEY);
    localStorage.removeItem(CACHE_TIMESTAMP_KEY);
    processes.value = [];
    console.log("🧹 Cache de processos limpo");
  };

  const fetchProcesses = async () => {
    // Se não forçar refresh, tentar carregar do cache primeiro
    /* if (!forceRefresh && loadFromCache()) {
      return
    }
 */
    loading.value = true;
    error.value = null;

    try {
      // Usar a URL completa da API base configurada
      const config = useRuntimeConfig();
      const apiUrl = `${config.public.FLOW_API_BASE}/api/v1/processes`;

      console.log("🔄 Buscando processos da API:", apiUrl);

      const response = await $fetch<Process[]>(apiUrl, {
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
      });

      console.log("📦 Resposta da API:", response);
      console.log("📦 Tipo da resposta:", typeof response);
      console.log("📦 É array?", Array.isArray(response));

      if (Array.isArray(response) && response.length > 0) {
        console.log("📦 Primeiro item:", response[0]);
        console.log("📦 Propriedades do primeiro item:", Object.keys(response[0] || {}));
      }

      if (Array.isArray(response)) {
        processes.value = response;
        saveToCache(response);
        console.log(
          `✅ ${response.length} processos carregados e salvos no cache`
        );
      } else {
        console.warn("⚠️ API não retornou um array:", response);
        processes.value = [];
      }
    } catch (err) {
      console.error("❌ Erro ao buscar processos:", err);
      error.value = "Erro ao carregar processos";
      processes.value = [];
    } finally {
      loading.value = false;
    }
  };

  // Apenas processos ativos
  const activeProcesses = computed(() => {
    return processes.value.filter((process) => process.active === true);
  });

  // Converter processos para formato de menu
  const processMenuItems = computed(() => {
    return activeProcesses.value.map((process) => ({
      label: process.name,
      path: `/esteira/${process.key}`,
      icon: "fa-solid fa-stream",
      description: `Esteira do processo ${process.name}`,
    }));
  });

  return {
    processes,
    activeProcesses,
    processMenuItems,
    loading,
    error,
    fetchProcesses,
    clearCache,
    loadFromCache,
  };
};
