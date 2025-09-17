interface UserData {
  id?: string;
  name: string;
  email: string;
  roles: string[];
  cpf?: string;
  cnpj?: string;
  rg?: string;
  banco?: string;
  agencia?: string;
  conta?: string;
  tipoConta?: string;
  pixTipo?: string;
  pixChave?: string;
  fotoPerfilUrl?: string;
  comprovanteEnderecoUrl?: string;
  ultimoIpAcesso?: string;
  ultimaLocalizacao?: string;
  ipCadastro?: string;
  localizacaoCadastro?: string;
}

interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  roles: string[];
  cpf?: string;
  cnpj?: string;
  rg?: string;
  banco?: string;
  agencia?: string;
  conta?: string;
  tipoConta?: string;
  pixTipo?: string;
  pixChave?: string;
  fotoPerfilUrl?: string;
  comprovanteEnderecoUrl?: string;
  localizacaoCadastro?: string;
}

interface UpdateUserRequest {
  name: string;
  roles: string[];
  cpf?: string;
  cnpj?: string;
  rg?: string;
  banco?: string;
  agencia?: string;
  conta?: string;
  tipoConta?: string;
  pixTipo?: string;
  pixChave?: string;
}

export async function fetchUsersApi(): Promise<UserData[]> {
  const { apiFetch } = await import('~/utils/api/index');
  try {
    const response = await apiFetch<UserData[]>('/api/v1/users');
    return response || [];
  } catch (error) {
    console.error('Error fetching users:', error);
    return [];
  }
}

export async function createUserApi(userData: CreateUserRequest): Promise<UserData | null> {
  const { apiFetch } = await import('~/utils/api/index');
  try {
    const response = await apiFetch<UserData>('/api/v1/users', {
      method: 'POST',
      body: userData,
    });
    return response;
  } catch (error) {
    console.error('Error creating user:', error);
    throw error;
  }
}

export async function updateUserApi(userId: string, userData: UpdateUserRequest): Promise<UserData | null> {
  const { apiFetch } = await import('~/utils/api/index');
  try {
    const response = await apiFetch<UserData>(`/api/v1/users/${userId}`, {
      method: 'PUT',
      body: userData,
    });
    return response;
  } catch (error) {
    console.error('Error updating user:', error);
    throw error;
  }
}

export async function deleteUserApi(userId: string): Promise<boolean> {
  const { apiFetch } = await import('~/utils/api/index');
  try {
    await apiFetch(`/api/v1/users/${userId}`, {
      method: 'DELETE',
    });
    return true;
  } catch (error) {
    console.error('Error deleting user:', error);
    return false;
  }
}

export async function checkEmailAvailabilityApi(email: string, excludeUserId?: string): Promise<boolean> {
  const { apiFetch } = await import('~/utils/api/index');
  try {
    const params = new URLSearchParams({ email });
    if (excludeUserId) {
      params.append('excludeUserId', excludeUserId);
    }
    const response = await apiFetch<{ available: boolean }>(`/api/v1/users/check-email?${params}`);
    return response?.available || false;
  } catch (error) {
    console.error('Error checking email availability:', error);
    return false;
  }
}

export async function createBasicUserApi(name: string, email: string): Promise<{ id: string } | null> {
  const { apiFetch } = await import('~/utils/api/index');
  try {
    const response = await apiFetch<{ id: string }>('/api/v1/users/create-basic', {
      method: 'POST',
      body: {
        name,
        email,
        password: 'temp123' // Senha temporária
      }
    });
    return response;
  } catch (error) {
    console.error('Error creating basic user:', error);
    throw error;
  }
}