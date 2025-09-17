import { d as defineEventHandler, r as readBody, u as useRuntimeConfig } from '../../nitro/nitro.mjs';
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
const ai_stream_post = defineEventHandler(async (event) => {
  var _a, _b, _c, _d, _e;
  const body = await readBody(event);
  const prompt = ((body == null ? void 0 : body.text) || "").toString().trim();
  const attachments = (body == null ? void 0 : body.attachments) || [];
  const cfg = useRuntimeConfig();
  const geminiKey = cfg.GEMINI_API_KEY;
  const bankUrl = cfg.BANK_API_URL;
  const bankToken = cfg.BANK_API_TOKEN;
  const res = event.node.res;
  res.setHeader("Content-Type", "text/plain; charset=utf-8");
  res.setHeader("Transfer-Encoding", "chunked");
  res.setHeader("Cache-Control", "no-cache");
  const write = (obj) => {
    res.write(JSON.stringify(obj) + "\n");
  };
  if (!prompt && attachments.length === 0) {
    write({ type: "text", delta: "Me manda algo para eu consorciar ideias! " });
    write({ type: "done" });
    res.end();
    return;
  }
  const attachSummary = attachments.length ? `

[Anexos]
${attachments.map((a, i) => `(${i + 1}) ${a.name || "arquivo"}${a.type ? ` [${a.type}]` : ""}${a.url ? ` -> ${a.url}` : ""}`).join("\n")}` : "";
  let sentenceCount = 0;
  const streamText = (chunk) => {
    if (!chunk) return;
    const clean = stripMarkdown(chunk);
    if (!clean) return;
    const sentences = splitSentences(clean);
    for (const s of sentences) {
      if (sentenceCount >= 10) break;
      sentenceCount++;
      write({ type: "text", delta: s + " " });
    }
  };
  if (geminiKey) {
    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:streamGenerateContent?alt=sse&key=${encodeURIComponent(
        geminiKey
      )}`;
      const systemPreamble = "Voc\xEA \xE9 uma assistente focada em cons\xF3rcio e esteira. Responda em texto simples (sem Markdown), direto e objetivo.";
      const payload = {
        contents: [
          { role: "user", parts: [{ text: systemPreamble }] },
          { role: "user", parts: [{ text: prompt + attachSummary }] }
        ]
      };
      const upstream = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!upstream.body) throw new Error("No stream body from provider");
      const reader = upstream.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        let idx;
        while ((idx = buffer.indexOf("\n")) >= 0) {
          const line = buffer.slice(0, idx).trim();
          buffer = buffer.slice(idx + 1);
          if (!line) continue;
          if (line.startsWith("data:")) {
            const jsonStr = line.slice(5).trim();
            if (jsonStr === "[DONE]") continue;
            try {
              const ev = JSON.parse(jsonStr);
              const text = ((_e = (_d = (_c = (_b = (_a = ev == null ? void 0 : ev.candidates) == null ? void 0 : _a[0]) == null ? void 0 : _b.content) == null ? void 0 : _c.parts) == null ? void 0 : _d[0]) == null ? void 0 : _e.text) || "";
              if (text) streamText(text);
              if (sentenceCount >= 10) break;
            } catch (_) {
            }
          }
        }
        if (sentenceCount >= 10) break;
      }
      write({ type: "done" });
      res.end();
      return;
    } catch (_) {
    }
  }
  if (bankUrl) {
    try {
      const result = await $fetch(bankUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...bankToken ? { Authorization: `Bearer ${bankToken}` } : {}
        },
        body: { prompt: prompt + attachSummary }
      });
      const full = stripMarkdown((result == null ? void 0 : result.text) || (result == null ? void 0 : result.message) || JSON.stringify(result));
      const sentences = splitSentences(full).slice(0, 10);
      for (const s of sentences) write({ type: "text", delta: s + " " });
      write({ type: "done" });
      res.end();
      return;
    } catch (_) {
    }
  }
  const offline = "Estou offline como assembleia sem qu\xF3rum. Configure a integra\xE7\xE3o e eu respondo voando.";
  const parts = splitSentences(offline);
  for (const p of parts) write({ type: "text", delta: p + " " });
  write({ type: "done" });
  res.end();
});

export { ai_stream_post as default };
//# sourceMappingURL=ai.stream.post.mjs.map
