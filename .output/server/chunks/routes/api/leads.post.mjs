import { d as defineEventHandler, r as readBody } from '../../nitro/nitro.mjs';
import 'node:http';
import 'node:https';
import 'node:events';
import 'node:buffer';
import 'node:fs';
import 'node:path';
import 'node:crypto';
import 'node:url';

const leads_post = defineEventHandler(async (event) => {
  const body = await readBody(event);
  if (!(body == null ? void 0 : body.name) || !(body == null ? void 0 : body.email) || !(body == null ? void 0 : body.goal) || !(body == null ? void 0 : body.consent)) {
    return event.node.res.writeHead(400).end(JSON.stringify({ message: "Dados insuficientes." }));
  }
  const lead = { ...body, receivedAt: (/* @__PURE__ */ new Date()).toISOString(), ip: event.node.req.socket.remoteAddress };
  console.info("[Lead] Novo lead recebido:", lead);
  return { ok: true };
});

export { leads_post as default };
//# sourceMappingURL=leads.post.mjs.map
