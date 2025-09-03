import { defineEventHandler, getQuery, createError } from 'h3'

export default defineEventHandler(async (event) => {
  const query = getQuery(event)
  const cepRaw = String(query.cep || '').replace(/\D/g, '')

  if (!cepRaw || cepRaw.length !== 8) {
    throw createError({ statusCode: 400, statusMessage: 'CEP inválido. Use 8 dígitos.' })
  }

  try {
    const url = `https://viacep.com.br/ws/${cepRaw}/json/`
    const data: any = await $fetch(url)

    if (data?.erro) {
      throw createError({ statusCode: 404, statusMessage: 'CEP não encontrado.' })
    }

    return {
      cep: cepRaw,
      state: data.uf,
      city: data.localidade,
      neighborhood: data.bairro,
      street: data.logradouro,
      service: 'viacep'
    }
  } catch (err: any) {
    throw createError({ statusCode: err?.statusCode || 500, statusMessage: err?.statusMessage || 'Falha ao consultar CEP' })
  }
})

