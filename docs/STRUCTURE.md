# Estrutura do Projeto Frontend

Este documento descreve a organização e estrutura de pastas do projeto frontend Nuxt.js.

## 📁 Estrutura de Diretórios

```
flow-service/
├── app/                    # Aplicação principal Nuxt
│   └── app.vue            # Componente raiz
├── assets/                # Recursos estáticos
│   ├── styles/           # Estilos CSS
│   │   ├── components/   # Estilos de componentes
│   │   ├── layouts/      # Estilos de layouts
│   │   ├── pages/        # Estilos de páginas
│   │   └── utils/        # Variáveis e mixins
│   ├── images/           # Imagens
│   │   ├── logos/        # Logotipos
│   │   ├── backgrounds/  # Imagens de fundo
│   │   └── illustrations/ # Ilustrações
│   └── icons/            # Ícones
│       ├── svg/          # Ícones SVG
│       └── png/          # Ícones PNG
├── components/            # Componentes Vue reutilizáveis
│   ├── ui/               # Componentes de interface
│   │   ├── buttons/      # Botões
│   │   ├── inputs/       # Campos de entrada
│   │   ├── modals/       # Modais
│   │   ├── cards/        # Cards
│   │   └── navigation/   # Navegação
│   ├── forms/            # Formulários
│   │   ├── auth/         # Formulários de autenticação
│   │   ├── contact/      # Formulários de contato
│   │   └── search/       # Formulários de busca
│   ├── layout/           # Componentes de layout
│   │   ├── header/       # Cabeçalho
│   │   ├── footer/       # Rodapé
│   │   ├── sidebar/      # Barra lateral
│   │   └── navigation/   # Navegação
│   ├── common/           # Componentes comuns
│   │   ├── loading/      # Estados de carregamento
│   │   ├── error/        # Estados de erro
│   │   └── empty-state/  # Estados vazios
│   └── features/         # Componentes específicos de funcionalidades
│       ├── dashboard/    # Dashboard
│       ├── profile/      # Perfil
│       └── settings/     # Configurações
├── composables/          # Composables Vue
├── layouts/              # Layouts Nuxt
├── middleware/           # Middleware Nuxt
├── pages/                # Páginas Nuxt (roteamento automático)
├── plugins/              # Plugins Nuxt
├── public/               # Arquivos públicos estáticos
├── server/               # API server-side
│   ├── api/              # Endpoints da API
│   └── middleware/       # Middleware do servidor
├── stores/               # Gerenciamento de estado
│   ├── auth/             # Estado de autenticação
│   ├── user/             # Estado do usuário
│   └── app/              # Estado da aplicação
├── types/                # Definições TypeScript
│   ├── api/              # Tipos da API
│   ├── components/       # Tipos de componentes
│   ├── stores/           # Tipos dos stores
│   └── utils/            # Tipos utilitários
└── utils/                # Funções utilitárias
    ├── api/              # Utilitários da API
    ├── validation/       # Validações
    ├── formatting/       # Formatação
    └── constants/        # Constantes
```

## 🎯 Convenções de Nomenclatura

### Arquivos e Pastas

- **Pastas**: kebab-case (ex: `user-profile`)
- **Componentes Vue**: PascalCase (ex: `UserProfile.vue`)
- **Composables**: camelCase com prefixo `use` (ex: `useAuth.ts`)
- **Stores**: camelCase (ex: `userStore.ts`)
- **Utilitários**: camelCase (ex: `formatDate.ts`)
- **Tipos**: PascalCase (ex: `UserProfile.ts`)

### Componentes

- **UI Components**: Nomes descritivos (ex: `BaseButton.vue`, `AppModal.vue`)
- **Feature Components**: Prefixo da funcionalidade (ex: `DashboardChart.vue`)
- **Layout Components**: Prefixo `Layout` (ex: `LayoutHeader.vue`)

## 📋 Padrões de Organização

### Componentes

- **Atomic Design**: Organizados por complexidade (ui → forms → features)
- **Single Responsibility**: Cada componente tem uma responsabilidade específica
- **Reutilização**: Componentes UI são altamente reutilizáveis

### Estilos

- **CSS Variables**: Uso de variáveis CSS para consistência
- **Utility Classes**: Classes utilitárias para espaçamento e layout
- **Component Scoped**: Estilos específicos de componentes são scoped

### TypeScript

- **Tipagem Forte**: Todos os dados são tipados
- **Interfaces**: Uso de interfaces para contratos de dados
- **Generics**: Uso de generics para reutilização de tipos

### Estado

- **Pinia**: Gerenciamento de estado reativo
- **Modularização**: Estados separados por domínio
- **Composables**: Lógica reutilizável em composables

## 🚀 Boas Práticas

1. **Modularidade**: Mantenha componentes pequenos e focados
2. **Reutilização**: Crie componentes reutilizáveis na pasta `ui/`
3. **Tipagem**: Sempre defina tipos TypeScript
4. **Documentação**: Documente componentes complexos
5. **Performance**: Use lazy loading para componentes pesados
6. **Acessibilidade**: Implemente práticas de acessibilidade
7. **Testes**: Escreva testes para componentes críticos

## 🔧 Configuração

- **Nuxt Config**: Configurações em `nuxt.config.ts`
- **TypeScript**: Configurações em `tsconfig.json`
- **ESLint**: Configurações em `eslint.config.mjs`
- **Estilos**: Importação global em `assets/styles/main.css`

## 📚 Recursos Adicionais

- [Documentação Nuxt.js](https://nuxt.com/docs)
- [Vue.js Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)
- [Pinia Store](https://pinia.vuejs.org/)
- [TypeScript](https://www.typescriptlang.org/docs/)
