import { d as defineEventHandler, a as readBody, u as useRuntimeConfig } from '../../nitro/nitro.mjs';
import 'node:http';
import 'node:https';
import 'node:events';
import 'node:buffer';
import 'node:fs';
import 'node:path';
import 'node:crypto';
import 'node:url';

function stripMarkdown(input) {
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
function splitSentences(text) {
  const parts = (text || "").split(/([.!?]+\s+)/);
  const out = [];
  for (let i = 0; i < parts.length; i += 2) {
    const a = parts[i] || "";
    const b = parts[i + 1] || "";
    const sentence = (a + b).trim();
    if (sentence) out.push(sentence);
  }
  return out;
}
const ai_post = defineEventHandler(async (event) => {
  var _a, _b, _c, _d, _e;
  const body = await readBody(event);
  const prompt = ((body == null ? void 0 : body.text) || (body == null ? void 0 : body.prompt) || "").toString().trim();
  const attachments = (body == null ? void 0 : body.attachments) || [];
  const providerReq = ((body == null ? void 0 : body.provider) || "").toString().trim().toLowerCase();
  if (!prompt && attachments.length === 0) {
    return {
      provider: "none",
      text: "Hmm... sem texto e sem anexo eu fico s\xF3 no pensamento coletivo. Manda algo pra eu consorciar ideias!"
    };
  }
  const config = useRuntimeConfig();
  const geminiKey = config.GEMINI_API_KEY;
  const bankUrl = config.BANK_API_URL;
  const bankToken = config.BANK_API_TOKEN;
  let provider = providerReq;
  if (!provider) {
    provider = geminiKey ? "gemini" : bankUrl ? "bank" : "none";
  }
  let knowledgeContext = "";
  try {
    const knowledgeResponse = await $fetch("/api/ai-knowledge", {
      method: "POST",
      body: { question: prompt }
    });
    if (knowledgeResponse == null ? void 0 : knowledgeResponse.context) {
      knowledgeContext = `

[Documenta\xE7\xE3o de Refer\xEAncia - ${knowledgeResponse.fileName}]
${knowledgeResponse.context}`;
    }
  } catch (error) {
  }
  const history = ((body == null ? void 0 : body.history) || []).filter((m) => typeof (m == null ? void 0 : m.content) === "string" && (m == null ? void 0 : m.content));
  const historySummary = history.length ? `

[Contexto]
${history.slice(-10).map((m) => `${m.role === "user" ? "Usu\xE1rio" : "Assistente"}: ${m.content}`).join("\n")}` : "";
  const attachSummary = attachments.length ? `

[Anexos]
${attachments.map(
    (a, i) => `(${i + 1}) ${a.name || "arquivo"}${a.type ? ` [${a.type}]` : ""}${a.url ? ` -> ${a.url}` : ""}`
  ).join("\n")}` : "";
  if (provider === "gemini") {
    if (!geminiKey) {
      return {
        provider: "none",
        text: "Ops! Estou offline do meu provedor de IA. Configure as credenciais para eu responder de verdade."
      };
    }
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${encodeURIComponent(
      geminiKey
    )}`;
    const systemPreamble = "Voc\xEA \xE9 a ConsorIA, assistente focada em cons\xF3rcio imobili\xE1rio. Responda de forma objetiva e \xFAtil. N\xE3o ultrapasse mais de 500 caracteres";
    const userText = `${prompt}${historySummary}${attachSummary}${knowledgeContext}`;
    const payload = {
      contents: [
        { role: "user", parts: [{ text: systemPreamble }] },
        { role: "user", parts: [{ text: userText }] }
      ]
    };
    try {
      const res = await $fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: payload
      });
      let text = ((_e = (_d = (_c = (_b = (_a = res == null ? void 0 : res.candidates) == null ? void 0 : _a[0]) == null ? void 0 : _b.content) == null ? void 0 : _c.parts) == null ? void 0 : _d[0]) == null ? void 0 : _e.text) || "N\xE3o consegui gerar uma resposta agora.";
      text = stripMarkdown(text);
      const limited = splitSentences(text).slice(0, 10).join(" ");
      return { provider: "gemini", text: limited };
    } catch (err) {
      return {
        provider: "none",
        text: "Tentei falar com o provedor de IA, mas o sinal caiu no meio da assembleia. Verifique a conex\xE3o/credenciais e tento de novo \u{1F609}"
      };
    }
  }
  if (provider === "bank") {
    if (!bankUrl) {
      return {
        provider: "none",
        text: "Sem conex\xE3o com o backend. Parece que o lance n\xE3o entrou. Configure a integra\xE7\xE3o para continuarmos."
      };
    }
    try {
      const res = await $fetch(bankUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...bankToken ? { Authorization: `Bearer ${bankToken}` } : {}
        },
        body: { prompt: `${prompt}${historySummary}${attachSummary}${knowledgeContext}`, attachments }
      });
      let text = (res == null ? void 0 : res.text) || (res == null ? void 0 : res.message) || JSON.stringify(res);
      text = stripMarkdown(text);
      const limited = splitSentences(text).slice(0, 10).join(" ");
      return { provider: "bank", text: limited };
    } catch (err) {
      return {
        provider: "none",
        text: "Fui falar com o backend, mas a linha ocupou. Quando ele atender, eu respondo bonito. \u{1F643}"
      };
    }
  }
  return {
    provider: "none",
    text: "Estou offline igual cons\xF3rcio sem assembleia. Configure Gemini ou o backend para eu trabalhar!"
  };
});

export { ai_post as default };
//# sourceMappingURL=ai.post.mjs.map
