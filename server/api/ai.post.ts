import { defineEventHandler, readBody } from "h3";

function stripMarkdown(input: string): string {
  let s = input || "";
  s = s.replace(/```[\s\S]*?```/g, "");
  s = s.replace(/`([^`]+)`/g, "$1");
  s = s.replace(/!\[[^\]]*\]\([^\)]*\)/g, "");
  s = s.replace(/\[([^\]]+)\]\([^\)]*\)/g, "$1");
  s = s.replace(/^\s{0,3}#{1,6}\s+/gm, "");
  s = s.replace(/^\s{0,3}[-*+]\s+/gm, "");
  s = s.replace(/^\s{0,3}>\s?/gm, "");
  s = s.replace(/\|/g, " ");
  s = s.replace(/[*_]{1,3}([^*_]+)[*_]{1,3}/g, "$1");
  s = s.replace(/\s+\n/g, "\n").replace(/\n{3,}/g, "\n\n");
  return s.trim();
}

function splitSentences(text: string): string[] {
  const parts = (text || "").split(/([.!?]+\s+)/);
  const out: string[] = [];
  for (let i = 0; i < parts.length; i += 2) {
    const a = parts[i] || "";
    const b = parts[i + 1] || "";
    const sentence = (a + b).trim();
    if (sentence) out.push(sentence);
  }
  return out;
}

type ChatAttachment = {
  name?: string;
  type?: string;
  url?: string;
  size?: number;
};

export default defineEventHandler(async (event) => {
  const body = await readBody<{
    text?: string;
    prompt?: string;
    attachments?: ChatAttachment[];
    provider?: string;
    history?: Array<{ role: string; content: string }>;
  }>(event);
  const prompt = (body?.text || body?.prompt || "").toString().trim();
  const attachments = body?.attachments || [];
  const providerReq = (body?.provider || "").toString().trim().toLowerCase();

  if (!prompt && attachments.length === 0) {
    return {
      provider: "none",
      text: "Hmm... sem texto e sem anexo eu fico só no pensamento coletivo. Manda algo pra eu consorciar ideias!",
    };
  }

  const config = useRuntimeConfig();
  const geminiKey = config.GEMINI_API_KEY as string | undefined;
  const bankUrl = config.BANK_API_URL as string | undefined;
  const bankToken = config.BANK_API_TOKEN as string | undefined;

  // Decide provider
  let provider = providerReq;
  if (!provider) {
    provider = geminiKey ? "gemini" : bankUrl ? "bank" : "none";
  }

  // Build context from history + attachments (simple textual summary)
  const history = (body?.history || []).filter((m) => typeof m?.content === 'string' && m?.content);
  const historySummary = history.length
    ? `\n\n[Contexto]\n${history
        .slice(-10)
        .map((m) => `${m.role === 'user' ? 'Usuário' : 'Assistente'}: ${m.content}`)
        .join('\n')}`
    : '';
  const attachSummary = attachments.length
    ? `\n\n[Anexos]\n${attachments
        .map(
          (a, i) =>
            `(${i + 1}) ${a.name || "arquivo"}${a.type ? ` [${a.type}]` : ""}${a.url ? ` -> ${a.url}` : ""}`
        )
        .join("\n")}`
    : "";

  if (provider === "gemini") {
    if (!geminiKey) {
      return {
        provider: "none",
        text: "Ops! Estou offline do meu provedor de IA. Configure as credenciais para eu responder de verdade.",
      };
    }
    // Minimal REST call to Gemini
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${encodeURIComponent(
      geminiKey
    )}`;
    const systemPreamble =
      "Você é a ConsorIA, assistente focada em consórcio imobiliário. Responda de forma objetiva e útil. Não ultrapasse mais de 500 caracteres";
    const userText = `${prompt}${historySummary}${attachSummary}`;
    const payload = {
      contents: [
        { role: "user", parts: [{ text: systemPreamble }] },
        { role: "user", parts: [{ text: userText }] },
      ],
    };
    try {
      const res: any = await $fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: payload,
      });
      let text: string =
        res?.candidates?.[0]?.content?.parts?.[0]?.text ||
        "Não consegui gerar uma resposta agora.";
      text = stripMarkdown(text);
      const limited = splitSentences(text).slice(0, 10).join(" ");
      return { provider: "gemini", text: limited };
    } catch (err: any) {
      return {
        provider: "none",
        text: "Tentei falar com o provedor de IA, mas o sinal caiu no meio da assembleia. Verifique a conexão/credenciais e tento de novo 😉",
      };
    }
  }

  if (provider === "bank") {
    if (!bankUrl) {
      return {
        provider: "none",
        text: "Sem conexão com o backend. Parece que o lance não entrou. Configure a integração para continuarmos.",
      };
    }
    try {
      const res: any = await $fetch(bankUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(bankToken ? { Authorization: `Bearer ${bankToken}` } : {}),
        },
        body: { prompt: `${prompt}${historySummary}${attachSummary}`, attachments },
      });
      let text: string = res?.text || res?.message || JSON.stringify(res);
      text = stripMarkdown(text);
      const limited = splitSentences(text).slice(0, 10).join(" ");
      return { provider: "bank", text: limited };
    } catch (err: any) {
      return {
        provider: "none",
        text: "Fui falar com o backend, mas a linha ocupou. Quando ele atender, eu respondo bonito. 🙃",
      };
    }
  }

  // Sem provedor configurado: resposta divertida padrão
  return {
    provider: "none",
    text: "Estou offline igual consórcio sem assembleia. Configure Gemini ou o backend para eu trabalhar!",
  };
});
