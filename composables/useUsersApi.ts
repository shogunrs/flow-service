interface UserData {
  id?: string;
  name: string;
  email: string;
  roles?: string[];
  superUser?: boolean;
  cpf?: string | null;
  cnpj?: string | null;
  rg?: string | null;
  telefone?: string | null;
  endereco?: string | null;
  enderecoRua?: string | null;
  enderecoNumero?: string | null;
  enderecoComplemento?: string | null;
  enderecoBairro?: string | null;
  enderecoCidade?: string | null;
  enderecoEstado?: string | null;
  cep?: string | null;
  banco?: string | null;
  agencia?: string | null;
  conta?: string | null;
  tipoConta?: string | null;
  pixTipo?: string | null;
  pixChave?: string | null;
  profileImage?: string | null;
  fotoPerfilUrl?: string | null;
  comprovanteEnderecoUrl?: string | null;
  documentoIdentidade?: string | null;
  cpfImage?: string | null;
  rgImage?: string | null;
  cnpjImage?: string | null;
  enderecoImage?: string | null;
  telefoneImage?: string | null;
  bancoImage?: string | null;
  pixImage?: string | null;
  location?: string | null;
  ultimoIpAcesso?: string | null;
  ultimaLocalizacao?: string | null;
  observacoes?: string | null;
  nomeCompleto?: string | null;
  quantidadeSocios?: number | null;
  enderecoEmpresa?: string | null;
  enderecoEmpresaRua?: string | null;
  enderecoEmpresaNumero?: string | null;
  enderecoEmpresaComplemento?: string | null;
  enderecoEmpresaBairro?: string | null;
  enderecoEmpresaCidade?: string | null;
  enderecoEmpresaEstado?: string | null;
  cepEmpresa?: string | null;
  observacoesEmpresa?: string | null;
  razaoSocial?: string | null;
  nomeFantasia?: string | null;
  cartaoCnpjImage?: string | null;
  contratoSocialImage?: string | null;
  qualificacaoSociosImage?: string | null;
  ipCadastro?: string | null;
  localizacaoCadastro?: string | null;
}

interface BaseUserPayload {
  name: string;
  email: string;
  roles: string[];
  superUser?: boolean;
  cpf?: string | null;
  cnpj?: string | null;
  rg?: string | null;
  telefone?: string | null;
  endereco?: string | null;
  enderecoRua?: string | null;
  enderecoNumero?: string | null;
  enderecoComplemento?: string | null;
  enderecoBairro?: string | null;
  enderecoCidade?: string | null;
  enderecoEstado?: string | null;
  cep?: string | null;
  banco?: string | null;
  agencia?: string | null;
  conta?: string | null;
  tipoConta?: string | null;
  pixTipo?: string | null;
  pixChave?: string | null;
  profileImage?: string | null;
  fotoPerfilUrl?: string | null;
  comprovanteEnderecoUrl?: string | null;
  documentoIdentidade?: string | null;
  cpfImage?: string | null;
  rgImage?: string | null;
  cnpjImage?: string | null;
  enderecoImage?: string | null;
  telefoneImage?: string | null;
  bancoImage?: string | null;
  pixImage?: string | null;
  location?: string | null;
  observacoes?: string | null;
  nomeCompleto?: string | null;
  quantidadeSocios?: number | null;
  enderecoEmpresa?: string | null;
  enderecoEmpresaRua?: string | null;
  enderecoEmpresaNumero?: string | null;
  enderecoEmpresaComplemento?: string | null;
  enderecoEmpresaBairro?: string | null;
  enderecoEmpresaCidade?: string | null;
  enderecoEmpresaEstado?: string | null;
  cepEmpresa?: string | null;
  observacoesEmpresa?: string | null;
  razaoSocial?: string | null;
  nomeFantasia?: string | null;
  cartaoCnpjImage?: string | null;
  contratoSocialImage?: string | null;
  qualificacaoSociosImage?: string | null;
  localizacaoCadastro?: string | null;
}

interface CreateUserRequest extends BaseUserPayload {
  password: string;
}

interface UpdateUserRequest extends BaseUserPayload {
  password?: string;
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
