import { ofetch } from "ofetch";
import { useRuntimeConfig } from "#imports";

export default (nitroApp: any) => {
  // Run after server is ready
  setTimeout(async () => {
    const cfg = useRuntimeConfig();
    const logPrefix = "[ConsorIA]";

    // Check Gemini connectivity
    if (cfg.GEMINI_API_KEY) {
      try {
        await ofetch(
          `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest`,
          { method: "GET", query: { key: cfg.GEMINI_API_KEY }, timeout: 4000 }
        );
        console.info(`${logPrefix} Provedor de IA: conexão OK`);
      } catch (e: any) {
        console.warn(
          `${logPrefix} Provedor de IA: falha no teste de conexão (${e?.status || e?.code || "erro"}).`
        );
      }
    } else {
      console.info(`${logPrefix} Provedor de IA: sem credenciais configuradas`);
    }

    // Check Flow API connectivity (public runtime config)
    const apiBase = (cfg as any)?.public?.FLOW_API_BASE as string | undefined;
    if (apiBase) {
      const base = apiBase.replace(/\/$/, "");
      try {
        await ofetch(`${base}/api/v1/processes`, { timeout: 3500 });
        console.info(`${logPrefix} Flow API: conexão OK`);
      } catch (e: any) {
        console.warn(
          `${logPrefix} Flow API: falha no teste de conexão (${e?.status || e?.code || "erro"}).`
        );
      }
    } else {
      console.info(
        `${logPrefix} Flow API: não configurada (FLOW_API_BASE ausente)`
      );
    }
  }, 0);
};
