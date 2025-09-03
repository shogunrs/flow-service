# Mapeamento de Componentes Reutilizáveis

Este documento mapeia todos os componentes identificados nos arquivos HTML existentes e sua migração para a estrutura Nuxt.js.

## Análise dos Arquivos HTML

### 1. esteira.html - Sistema Kanban de Aprovação de Crédito

**Funcionalidades principais:**
- Sistema Kanban drag-and-drop para gestão de propostas
- Visualização em lista e kanban
- Modais para edição de propostas
- Formulários dinâmicos por etapa
- Sistema de histórico e SLA
- Filtros e busca

### 2. login.html - Página de Autenticação

**Funcionalidades principais:**
- Formulário de login com validação
- Design responsivo com backdrop blur
- Campos de email e senha
- Opção "lembrar-me"
- Link para recuperação de senha

### 3. paineladm.html - Painel Administrativo

**Funcionalidades principais:**
- Gestão de usuários e permissões
- Configuração de esteiras por produto
- Sistema de notificações
- Configuração de integrações
- Painel de comunicação arrastável
- Gestão de documentos

## Componentes Reutilizáveis Identificados

### UI Components (components/ui/)

#### Buttons
- **BaseButton** - Botão base com variantes (primary, secondary, danger)
- **IconButton** - Botão com ícone
- **FloatingActionButton** - Botão flutuante (usado no painel admin)

#### Inputs
- **BaseInput** - Input base com ícones e validação
- **BaseSelect** - Select customizado
- **BaseTextarea** - Textarea com contador de caracteres
- **BaseCheckbox** - Checkbox customizado
- **FileUploader** - Componente de upload de arquivos
- **SearchInput** - Input de busca com ícone

#### Cards
- **KanbanCard** - Card para o sistema kanban
- **UserCard** - Card para exibição de usuários
- **NotificationCard** - Card para notificações
- **StatsCard** - Card para estatísticas

#### Modals
- **BaseModal** - Modal base reutilizável
- **ConfirmModal** - Modal de confirmação
- **AlertModal** - Modal de alerta
- **FormModal** - Modal com formulário

#### Navigation
- **TabNavigation** - Navegação por abas
- **Breadcrumb** - Navegação breadcrumb
- **Sidebar** - Barra lateral de navegação

### Form Components (components/forms/)

#### Form Elements
- **FormField** - Wrapper para campos de formulário
- **FormSection** - Seção de formulário com título
- **FormActions** - Ações do formulário (salvar, cancelar)
- **DynamicForm** - Formulário dinâmico baseado em schema

#### Specific Forms
- **LoginForm** - Formulário de login
- **UserForm** - Formulário de usuário
- **ProposalForm** - Formulário de proposta
- **StageConfigForm** - Formulário de configuração de etapa

### Layout Components (components/layout/)

#### Headers
- **AppHeader** - Cabeçalho principal da aplicação
- **PageHeader** - Cabeçalho de página com título e ações
- **ModalHeader** - Cabeçalho de modal

#### Containers
- **PageContainer** - Container principal de página
- **ContentSection** - Seção de conteúdo
- **SidePanel** - Painel lateral

### Feature Components (components/features/)

#### Kanban System
- **KanbanBoard** - Board principal do kanban
- **KanbanColumn** - Coluna do kanban
- **KanbanCard** - Card arrastável
- **ProposalModal** - Modal de edição de proposta
- **ProposalHistory** - Histórico da proposta

#### User Management
- **UserTable** - Tabela de usuários
- **UserModal** - Modal de edição de usuário
- **UserDocuments** - Gestão de documentos do usuário
- **UserHierarchy** - Hierarquia de usuários

#### Admin Panel
- **ProductStageConfig** - Configuração de etapas por produto
- **NotificationSettings** - Configurações de notificação
- **IntegrationConfig** - Configuração de integrações
- **CommunicationPanel** - Painel de comunicação arrastável

#### Authentication
- **LoginCard** - Card de login
- **AuthLayout** - Layout de autenticação

### Common Components (components/common/)

#### Data Display
- **DataTable** - Tabela de dados reutilizável
- **StatusBadge** - Badge de status
- **ProgressBar** - Barra de progresso
- **Timeline** - Linha do tempo
- **SlaIndicator** - Indicador de SLA

#### Feedback
- **LoadingSpinner** - Spinner de carregamento
- **EmptyState** - Estado vazio
- **ErrorBoundary** - Tratamento de erros
- **Toast** - Notificações toast

#### Utilities
- **DraggableContainer** - Container arrastável
- **ResizablePanel** - Painel redimensionável
- **FilterPanel** - Painel de filtros
- **SearchBar** - Barra de busca

## Estrutura de Páginas Nuxt

### Páginas Principais

1. **pages/index.vue** - Dashboard principal (redirecionamento)
2. **pages/login.vue** - Página de login
3. **pages/esteira/index.vue** - Sistema kanban
4. **pages/admin/index.vue** - Painel administrativo

### Layouts

1. **layouts/default.vue** - Layout padrão com sidebar
2. **layouts/auth.vue** - Layout para páginas de autenticação
3. **layouts/admin.vue** - Layout para páginas administrativas

## Stores (Pinia)

### Estados Globais

1. **stores/auth.js** - Autenticação e usuário logado
2. **stores/proposals.js** - Gestão de propostas
3. **stores/users.js** - Gestão de usuários
4. **stores/stages.js** - Configuração de etapas
5. **stores/notifications.js** - Sistema de notificações
6. **stores/ui.js** - Estado da interface (modais, filtros, etc.)

## Composables

### Funcionalidades Reutilizáveis

1. **composables/useAuth.js** - Lógica de autenticação
2. **composables/useKanban.js** - Lógica do sistema kanban
3. **composables/useModal.js** - Gestão de modais
4. **composables/useFilters.js** - Sistema de filtros
5. **composables/useSla.js** - Cálculos de SLA
6. **composables/useDragDrop.js** - Funcionalidade drag and drop
7. **composables/useNotifications.js** - Sistema de notificações
8. **composables/useValidation.js** - Validação de formulários

## Utilitários

### Helpers

1. **utils/api/client.js** - Cliente HTTP
2. **utils/validation/rules.js** - Regras de validação
3. **utils/formatting/currency.js** - Formatação de moeda
4. **utils/formatting/date.js** - Formatação de datas
5. **utils/constants/status.js** - Constantes de status
6. **utils/constants/colors.js** - Paleta de cores

## Middleware

1. **middleware/auth.js** - Verificação de autenticação
2. **middleware/admin.js** - Verificação de permissões admin
3. **middleware/guest.js** - Redirecionamento para usuários logados

## Plugins

1. **plugins/draggable.client.js** - Plugin para drag and drop
2. **plugins/toast.client.js** - Plugin para notificações
3. **plugins/api.js** - Plugin para cliente HTTP

## Próximos Passos

1. ✅ Criar estrutura de componentes base
2. ⏳ Implementar páginas Nuxt
3. ⏳ Migrar estilos CSS
4. ⏳ Implementar stores Pinia
5. ⏳ Criar composables
6. ⏳ Configurar middleware e plugins
7. ⏳ Testes e refinamentos

## Observações Técnicas

### Dependências Identificadas
- Vue 3 (já configurado no Nuxt 3)
- Tailwind CSS (para estilização)
- VueDraggable (para drag and drop)
- Pinia (para gerenciamento de estado)
- VeeValidate (para validação de formulários)
- Axios/Fetch (para requisições HTTP)

### Padrões de Design
- Tema escuro como padrão
- Cor primária: Orange (#f97316)
- Tipografia: Inter
- Componentes modulares e reutilizáveis
- Responsividade mobile-first
- Acessibilidade (ARIA labels, keyboard navigation)