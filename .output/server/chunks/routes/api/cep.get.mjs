import { d as defineEventHandler, b as getQuery, c as createError } from '../../nitro/nitro.mjs';
import 'node:http';
import 'node:https';
import 'node:events';
import 'node:buffer';
import 'node:fs';
import 'node:path';
import 'node:crypto';
import 'node:url';

const cep_get = defineEventHandler(async (event) => {
  const query = getQuery(event);
  const cepRaw = String(query.cep || "").replace(/\D/g, "");
  if (!cepRaw || cepRaw.length !== 8) {
    throw createError({ statusCode: 400, statusMessage: "CEP inv\xE1lido. Use 8 d\xEDgitos." });
  }
  try {
    const url = `https://viacep.com.br/ws/${cepRaw}/json/`;
    const data = await $fetch(url);
    if (data == null ? void 0 : data.erro) {
      throw createError({ statusCode: 404, statusMessage: "CEP n\xE3o encontrado." });
    }
    return {
      cep: cepRaw,
      state: data.uf,
      city: data.localidade,
      neighborhood: data.bairro,
      street: data.logradouro,
      service: "viacep"
    };
  } catch (err) {
    throw createError({ statusCode: (err == null ? void 0 : err.statusCode) || 500, statusMessage: (err == null ? void 0 : err.statusMessage) || "Falha ao consultar CEP" });
  }
});

export { cep_get as default };
//# sourceMappingURL=cep.get.mjs.map
