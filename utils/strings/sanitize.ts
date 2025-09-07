export function sanitizeProcessKey(input: string): string {
  if (!input) return ''
  // remove acentos, lowercase, tira espaços e caracteres inválidos
  const noAccents = input
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
  return noAccents
    .toLowerCase()
    .trim()
    .replace(/\s+/g, '') // sem espaços
    .replace(/[^a-z0-9_-]/g, '') // somente a-z, 0-9, underscore, hífen
}

