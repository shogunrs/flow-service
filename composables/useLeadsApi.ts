export type LeadOrigin =
  | 'INDICACAO'
  | 'EVENTO'
  | 'SITE'
  | 'ANUNCIO'
  | 'SOCIAL_MEDIA'
  | 'PARCERIA'
  | 'COLD_CALL'
  | 'EMAIL_MARKETING'
  | 'RETORNO_CLIENTE'
  | 'OUTROS';

export interface LeadPayload {
  name: string;
  email: string;
  phone?: string | null;
  origin?: LeadOrigin | null;
  type?: 'CLIENT' | 'ANALYST_CANDIDATE';
}

export interface LeadData extends LeadPayload {
  id: string;
  createdAt?: string;
  updatedAt?: string;
}

export async function fetchLeadsApi(): Promise<LeadData[]> {
  const { apiFetch } = await import('~/utils/api/index');
  return apiFetch<LeadData[]>('/api/v1/leads');
}

export async function createLeadApi(payload: LeadPayload): Promise<LeadData> {
  const { apiFetch } = await import('~/utils/api/index');
  return apiFetch<LeadData>('/api/v1/leads', {
    method: 'POST',
    body: payload,
  });
}

export async function updateLeadApi(id: string, payload: LeadPayload): Promise<LeadData> {
  const { apiFetch } = await import('~/utils/api/index');
  return apiFetch<LeadData>(`/api/v1/leads/${id}`, {
    method: 'PUT',
    body: payload,
  });
}

export async function deleteLeadApi(id: string): Promise<void> {
  const { apiFetch } = await import('~/utils/api/index');
  await apiFetch(`/api/v1/leads/${id}`, {
    method: 'DELETE',
  });
}
