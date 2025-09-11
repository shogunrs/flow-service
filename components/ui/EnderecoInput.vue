<template>
  <div class="endereco-container space-y-1">
    <div
      class="text-[12px] font-medium text-slate-300 mb-1 flex items-center gap-1"
    >
      <i class="fa-solid fa-building text-blue-400 text-xs"></i>
      {{ label || "Qualificação Completa" }}
      <span v-if="required" class="text-red-500">*</span>
    </div>

    <!-- Dados da Pessoa/Empresa -->
    <div class="space-y-1 mb-2">
      <!-- Nome/Razão Social - Ocupa toda largura -->
      <div>
        <label :for="nomeInputId" class="text-[12px] text-slate-300">
          {{ tipoNome }}
          <span v-if="required" class="text-red-500">*</span>
        </label>
        <div
          class="input-container input-container--xs input-container--default"
        >
          <input
            :id="nomeInputId"
            v-model="dados.nome"
            type="text"
            :placeholder="
              tipoNome === 'Nome Completo'
                ? 'Nome completo da pessoa...'
                : 'Razão social da empresa...'
            "
            :required="required"
            :disabled="disabled"
            class="input-field input-field--xs"
            @input="updateDadosValue"
          />
        </div>
      </div>

      <!-- Grid para CPF + Data de Nascimento (se CPF validado) -->
      <div
        :class="
          documentValid === true && tipoPessoa === 'PF'
            ? 'grid grid-cols-1 md:grid-cols-2 gap-1'
            : 'grid grid-cols-1 gap-1'
        "
      >
        <!-- CPF/CNPJ -->
        <div>
          <label :for="documentoInputId" class="text-[12px] text-slate-300">
            {{ tipoDocumento }}
            <span v-if="required" class="text-red-500">*</span>
          </label>
          <div
            class="input-container input-container--xs input-container--default"
            :class="documentValidationClasses"
          >
            <input
              :id="documentoInputId"
              v-model="documentMasked"
              type="text"
              :placeholder="
                tipoPessoa === 'PJ'
                  ? '00.000.000/0000-00'
                  : tipoPessoa === 'PF'
                    ? '000.000.000-00'
                    : 'CPF ou CNPJ'
              "
              :required="required"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="handleDocumentInput"
              @blur="handleDocumentBlur"
              @focus="handleDocumentFocus"
              maxlength="18"
            />
          </div>
        </div>

        <!-- Data de Nascimento - Aparece apenas se CPF validado -->
        <div v-show="documentValid === true && tipoPessoa === 'PF'">
          <label :for="dataNascimentoInputId" class="text-[12px] text-slate-300"
            >Data de Nascimento</label
          >
          <div
            class="input-container input-container--xs input-container--default"
            :class="dataNascimentoValidationClasses"
          >
            <input
              :id="dataNascimentoInputId"
              v-model="dataNascimentoMasked"
              type="text"
              placeholder="dd/mm/aaaa"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="handleDataNascimentoInput"
              @blur="handleDataNascimentoBlur"
              maxlength="10"
            />
          </div>
        </div>
      </div>

      <!-- Grid para outros campos -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-1">
        <!-- E-mail -->
        <div>
          <label :for="emailInputId" class="text-[12px] text-slate-300"
            >E-mail</label
          >
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="emailInputId"
              v-model="dados.email"
              type="email"
              placeholder="email@exemplo.com"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateDadosValue"
            />
          </div>
        </div>

        <!-- Telefone -->
        <div>
          <label :for="telefoneInputId" class="text-[12px] text-slate-300"
            >Celular</label
          >
          <div
            class="input-container input-container--xs input-container--default"
            :class="telefoneValidationClasses"
          >
            <input
              :id="telefoneInputId"
              v-model="telefoneMasked"
              type="text"
              placeholder="(00) 00000-0000"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="handleTelefoneInput"
              @blur="handleTelefoneBlur"
              maxlength="15"
            />
          </div>
        </div>

        <!-- Telefone 2 -->
        <div>
          <label :for="telefone2InputId" class="text-[12px] text-slate-300"
            >Fixo</label
          >
          <div
            class="input-container input-container--xs input-container--default"
            :class="telefone2ValidationClasses"
          >
            <input
              :id="telefone2InputId"
              v-model="telefone2Masked"
              type="text"
              placeholder="(00) 00000-0000"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="handleTelefone2Input"
              @blur="handleTelefone2Blur"
              maxlength="15"
            />
          </div>
        </div>

        <!-- Campos específicos para PJ -->
        <div v-show="tipoPessoa === 'PJ'">
          <label
            :for="inscricaoEstadualInputId"
            class="text-[12px] text-slate-300"
            >Inscrição Estadual</label
          >
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="inscricaoEstadualInputId"
              v-model="dados.inscricaoEstadual"
              type="text"
              placeholder="000.000.000.000"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateDadosValue"
            />
          </div>
        </div>

        <!-- Nome da Mãe - Aparece apenas se CPF validado -->
        <div v-show="tipoPessoa === 'PF' && documentValid === true">
          <label :for="nomeMaeInputId" class="text-[12px] text-slate-300"
            >Nome da Mãe</label
          >
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="nomeMaeInputId"
              v-model="dados.nomeMae"
              type="text"
              placeholder="Nome completo da mãe..."
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateDadosValue"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Campos de endereço que aparecem sempre -->
    <div class="address-fields space-y-1">
      <!-- Grid responsivo para todos os campos -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-1">
        <!-- CEP - Campo principal que busca o endereço -->
        <div class="cep-field">
          <label :for="cepInputId" class="text-[12px] text-slate-300"
            >CEP</label
          >
          <div
            class="input-container input-container--xs input-container--default"
            :class="cepValidationClasses"
          >
            <input
              :id="cepInputId"
              v-model="cepMasked"
              type="text"
              placeholder="00000-000"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="handleCepInput"
              @blur="handleCepBlur"
              @focus="handleCepFocus"
              maxlength="9"
            />
          </div>
        </div>

        <!-- Cidade -->
        <div>
          <label :for="cidadeInputId" class="text-[12px] text-slate-300">
            Cidade
            <span v-if="required" class="text-red-500">*</span>
          </label>
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="cidadeInputId"
              v-model="endereco.cidade"
              type="text"
              placeholder="São Paulo"
              :required="required"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateEnderecoValue"
            />
          </div>
        </div>

        <!-- UF -->
        <div>
          <label :for="ufInputId" class="text-[12px] text-slate-300">
            UF
            <span v-if="required" class="text-red-500">*</span>
          </label>
          <div
            class="input-container input-container--xs input-container--default"
          >
            <select
              :id="ufInputId"
              v-model="endereco.uf"
              :required="required"
              :disabled="disabled"
              class="input-field input-field--xs"
              @change="updateEnderecoValue"
            >
              <option value="">UF</option>
              <option value="AC">AC</option>
              <option value="AL">AL</option>
              <option value="AP">AP</option>
              <option value="AM">AM</option>
              <option value="BA">BA</option>
              <option value="CE">CE</option>
              <option value="DF">DF</option>
              <option value="ES">ES</option>
              <option value="GO">GO</option>
              <option value="MA">MA</option>
              <option value="MT">MT</option>
              <option value="MS">MS</option>
              <option value="MG">MG</option>
              <option value="PA">PA</option>
              <option value="PB">PB</option>
              <option value="PR">PR</option>
              <option value="PE">PE</option>
              <option value="PI">PI</option>
              <option value="RJ">RJ</option>
              <option value="RN">RN</option>
              <option value="RS">RS</option>
              <option value="RO">RO</option>
              <option value="RR">RR</option>
              <option value="SC">SC</option>
              <option value="SP">SP</option>
              <option value="SE">SE</option>
              <option value="TO">TO</option>
            </select>
          </div>
        </div>

        <!-- Logradouro - ocupa 3 colunas -->
        <div class="lg:col-span-3 sm:col-span-2">
          <label :for="logradouroInputId" class="text-[12px] text-slate-300">
            Logradouro
            <span v-if="required" class="text-red-500">*</span>
          </label>
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="logradouroInputId"
              v-model="endereco.logradouro"
              type="text"
              placeholder="Rua, Avenida..."
              :required="required"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateEnderecoValue"
            />
          </div>
        </div>

        <!-- Número -->
        <div>
          <label :for="numeroInputId" class="text-[12px] text-slate-300">
            Número
            <span v-if="required" class="text-red-500">*</span>
          </label>
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="numeroInputId"
              v-model="endereco.numero"
              type="text"
              placeholder="123"
              :required="required"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateEnderecoValue"
            />
          </div>
        </div>

        <!-- Complemento -->
        <div>
          <label :for="complementoInputId" class="text-[12px] text-slate-300"
            >Complemento</label
          >
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="complementoInputId"
              v-model="endereco.complemento"
              type="text"
              placeholder="Apto, Bloco..."
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateEnderecoValue"
            />
          </div>
        </div>

        <!-- Bairro -->
        <div>
          <label :for="bairroInputId" class="text-[12px] text-slate-300">
            Bairro
            <span v-if="required" class="text-red-500">*</span>
          </label>
          <div
            class="input-container input-container--xs input-container--default"
          >
            <input
              :id="bairroInputId"
              v-model="endereco.bairro"
              type="text"
              placeholder="Centro..."
              :required="required"
              :disabled="disabled"
              class="input-field input-field--xs"
              @input="updateEnderecoValue"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Campo de Observações -->
    <div class="mt-3">
      <label :for="observacoesInputId" class="text-[12px] text-slate-300"
        >Observações</label
      >
      <div
        class="input-container input-container--textarea input-container--default"
      >
        <textarea
          :id="observacoesInputId"
          v-model="dados.observacoes"
          placeholder="Digite suas observações (máximo 500 caracteres)..."
          :disabled="disabled"
          class="input-field input-field--textarea"
          rows="4"
          maxlength="500"
          @input="updateDadosValue"
        ></textarea>
      </div>
      <div class="flex justify-between items-center mt-1">
        <div class="text-xs text-slate-400">Máximo 500 caracteres</div>
        <div class="text-xs text-slate-400">
          {{ (dados.observacoes || "").length }}/500
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  label: { type: String, default: "Endereço" },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  autoFillFromCep: { type: Boolean, default: true },
});

const emit = defineEmits(["update:modelValue", "validation", "cep-found"]);

// IDs únicos para os campos - gerados uma única vez
const componentId = Math.random().toString(36).slice(2, 8);
const documentoInputId = `endereco-documento-${componentId}`;
const nomeInputId = `endereco-nome-${componentId}`;
const emailInputId = `endereco-email-${componentId}`;
const telefoneInputId = `endereco-telefone-${componentId}`;
const telefone2InputId = `endereco-telefone2-${componentId}`;
const inscricaoEstadualInputId = `endereco-ie-${componentId}`;
const dataNascimentoInputId = `endereco-nascimento-${componentId}`;
const nomeMaeInputId = `endereco-mae-${componentId}`;
const observacoesInputId = `endereco-observacoes-${componentId}`;
const cepInputId = `endereco-cep-${componentId}`;
const logradouroInputId = `endereco-logradouro-${componentId}`;
const numeroInputId = `endereco-numero-${componentId}`;
const complementoInputId = `endereco-complemento-${componentId}`;
const bairroInputId = `endereco-bairro-${componentId}`;
const cidadeInputId = `endereco-cidade-${componentId}`;
const ufInputId = `endereco-uf-${componentId}`;

// Estados do documento
const documentMasked = ref("");
const documentValid = ref(null);
const tipoPessoa = ref(""); // 'PF' ou 'PJ'

// Estados do telefone
const telefoneMasked = ref("");
const telefoneValid = ref(null);

// Estados do segundo telefone
const telefone2Masked = ref("");
const telefone2Valid = ref(null);

// Estados da data de nascimento
const dataNascimentoMasked = ref("");
const dataNascimentoValid = ref(null);

// Estados do CEP
const cepMasked = ref("");
const cepValid = ref(null);
const isLoadingAddress = ref(false);
const showAddressFields = ref(true); // Sempre mostrar campos

// Dados completos (pessoa + endereço)
const dados = ref({
  // Dados da pessoa/empresa
  documento: "",
  nome: "",
  email: "",
  telefone: "",
  telefone2: "",
  inscricaoEstadual: "",
  dataNascimento: "",
  nomeMae: "",
  observacoes: "",
  // Dados do endereço
  cep: "",
  logradouro: "",
  numero: "",
  complemento: "",
  bairro: "",
  cidade: "",
  uf: "",
});

// Objeto separado para facilitar manipulação do endereço
const endereco = ref({
  cep: "",
  logradouro: "",
  numero: "",
  complemento: "",
  bairro: "",
  cidade: "",
  uf: "",
});

// Computed properties para interface dinâmica
const tipoDocumento = computed(() => {
  if (tipoPessoa.value === "PJ") return "CNPJ";
  if (tipoPessoa.value === "PF") return "CPF";
  return "Numero Fiscal";
});

const tipoNome = computed(() => {
  if (tipoPessoa.value === "PJ") return "Razão Social";
  if (tipoPessoa.value === "PF") return "Nome Completo";
  return "Nome/Razão Social";
});

// Classes de validação para o documento
const documentValidationClasses = computed(() => {
  const classes = [];
  if (props.disabled) classes.push("input-container--disabled");
  if (documentValid.value === false) classes.push("input-container--error");
  if (documentValid.value === true) classes.push("input-container--success");
  return classes;
});

// Classes de validação para o telefone
const telefoneValidationClasses = computed(() => {
  const classes = [];
  if (props.disabled) classes.push("input-container--disabled");
  if (telefoneValid.value === false) classes.push("input-container--error");
  if (telefoneValid.value === true) classes.push("input-container--success");
  return classes;
});

// Classes de validação para o telefone2
const telefone2ValidationClasses = computed(() => {
  const classes = [];
  if (props.disabled) classes.push("input-container--disabled");
  if (telefone2Valid.value === false) classes.push("input-container--error");
  if (telefone2Valid.value === true) classes.push("input-container--success");
  return classes;
});

// Classes de validação para a data de nascimento
const dataNascimentoValidationClasses = computed(() => {
  const classes = [];
  if (props.disabled) classes.push("input-container--disabled");
  if (dataNascimentoValid.value === false) classes.push("input-container--error");
  if (dataNascimentoValid.value === true) classes.push("input-container--success");
  return classes;
});

// Classes de validação para o CEP
const cepValidationClasses = computed(() => {
  const classes = [];
  if (props.disabled) classes.push("input-container--disabled");
  if (cepValid.value === false) classes.push("input-container--error");
  if (cepValid.value === true) classes.push("input-container--success");
  return classes;
});

// Funções para detectar tipo de documento
function detectDocumentType(value) {
  const numbers = value.replace(/\D/g, "");
  if (numbers.length === 11) return "PF";
  if (numbers.length === 14) return "PJ";
  if (numbers.length >= 12) return "PJ"; // 12 ou mais dígitos = CNPJ
  if (numbers.length >= 1) return "PF"; // 1 a 11 dígitos = CPF
  return ""; // Vazio
}

// Função para aplicar máscara dinâmica no documento
function applyDocumentMask(value) {
  const numbers = value.replace(/\D/g, "");

  if (numbers.length <= 11) {
    // Máscara CPF: 000.000.000-00
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 6)
      return `${numbers.slice(0, 3)}.${numbers.slice(3)}`;
    if (numbers.length <= 9)
      return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6)}`;
    return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6, 9)}-${numbers.slice(9, 11)}`;
  } else {
    // Máscara CNPJ: 00.000.000/0000-00
    if (numbers.length <= 2) return numbers;
    if (numbers.length <= 5)
      return `${numbers.slice(0, 2)}.${numbers.slice(2)}`;
    if (numbers.length <= 8)
      return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5)}`;
    if (numbers.length <= 12)
      return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8)}`;
    return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8, 12)}-${numbers.slice(12, 14)}`;
  }
}

// Validação simples de CPF
function validarCPF(cpf) {
  cpf = cpf.replace(/\D/g, "");
  if (cpf.length !== 11) return false;
  if (/^(\d)\1{10}$/.test(cpf)) return false; // Números repetidos

  let sum = 0;
  for (let i = 0; i < 9; i++) sum += parseInt(cpf.charAt(i)) * (10 - i);
  let remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(cpf.charAt(9))) return false;

  sum = 0;
  for (let i = 0; i < 10; i++) sum += parseInt(cpf.charAt(i)) * (11 - i);
  remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  return remainder === parseInt(cpf.charAt(10));
}

// Validação simples de CNPJ
function validarCNPJ(cnpj) {
  cnpj = cnpj.replace(/\D/g, "");
  if (cnpj.length !== 14) return false;
  if (/^(\d)\1{13}$/.test(cnpj)) return false; // Números repetidos

  let length = cnpj.length - 2;
  let numbers = cnpj.substring(0, length);
  let digits = cnpj.substring(length);
  let sum = 0;
  let pos = length - 7;

  for (let i = length; i >= 1; i--) {
    sum += numbers.charAt(length - i) * pos--;
    if (pos < 2) pos = 9;
  }

  let result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
  if (result !== parseInt(digits.charAt(0))) return false;

  length = length + 1;
  numbers = cnpj.substring(0, length);
  sum = 0;
  pos = length - 7;

  for (let i = length; i >= 1; i--) {
    sum += numbers.charAt(length - i) * pos--;
    if (pos < 2) pos = 9;
  }

  result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
  return result === parseInt(digits.charAt(1));
}

// Função para aplicar máscara no CEP
function applyCepMask(value) {
  const numbers = value.replace(/\D/g, "");
  if (numbers.length <= 5) return numbers;
  return `${numbers.slice(0, 5)}-${numbers.slice(5, 8)}`;
}

// Função para buscar endereço via ViaCEP
async function fetchAddress(cep) {
  try {
    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    const data = await response.json();

    if (data.erro) {
      return null;
    }

    return {
      cep: data.cep,
      logradouro: data.logradouro,
      complemento: data.complemento,
      bairro: data.bairro,
      cidade: data.localidade,
      uf: data.uf,
      ibge: data.ibge,
      gia: data.gia,
      ddd: data.ddd,
      siafi: data.siafi,
    };
  } catch (error) {
    console.warn("Erro ao buscar CEP:", error);
    return null;
  }
}

// Validação do CEP
async function validateCep(value) {
  if (!value.trim()) {
    cepValid.value = null;
    // Não resetar endereço se não houver CEP - permitir preenchimento manual
    endereco.value.cep = "";
    updateEnderecoValue();
    return null;
  }

  isLoadingAddress.value = true;

  const numbers = value.replace(/\D/g, "");

  // Validação de formato
  if (!/^\d{8}$/.test(numbers)) {
    cepValid.value = false;
    isLoadingAddress.value = false;
    endereco.value.cep = numbers;
    updateEnderecoValue();
    return false;
  }

  // Buscar endereço se habilitado
  if (props.autoFillFromCep) {
    const address = await fetchAddress(numbers);

    if (address) {
      cepValid.value = true;

      // Preencher dados do endereço
      endereco.value = {
        cep: numbers,
        logradouro: address.logradouro,
        numero: endereco.value.numero || "", // Manter número se já preenchido
        complemento: address.complemento || endereco.value.complemento || "",
        bairro: address.bairro,
        cidade: address.cidade,
        uf: address.uf,
      };

      updateEnderecoValue();
      emit("cep-found", address);
    } else {
      cepValid.value = false;
      endereco.value.cep = numbers;
      updateEnderecoValue();
    }
  } else {
    cepValid.value = true;
    endereco.value.cep = numbers;
    updateEnderecoValue();
  }

  isLoadingAddress.value = false;
  return cepValid.value;
}

// Handlers do documento
function handleDocumentInput(event) {
  const value = event.target.value;
  const masked = applyDocumentMask(value);
  documentMasked.value = masked;

  // Detectar tipo de pessoa
  const newTipo = detectDocumentType(value);
  if (tipoPessoa.value !== newTipo) {
    tipoPessoa.value = newTipo;
  }

  // Validar durante digitação
  validateDocument(masked);
}

function handleDocumentBlur() {
  try {
    validateDocument(documentMasked.value);
  } catch (error) {
    console.warn('Erro em handleDocumentBlur:', error);
  }
}

function handleDocumentFocus() {
  // Focus handler - apenas para consistência
}

// Validação do documento
function validateDocument(value) {
  try {
    if (!value || !dados.value) return null;
    
    const numbers = value.replace(/\D/g, "");

    if (!numbers.trim()) {
      documentValid.value = null;
      dados.value.documento = "";
      updateDadosValue();
      return null;
    }

    // Validação apenas se tiver o número completo de dígitos
    if (tipoPessoa.value === "PF" && numbers.length === 11) {
      // Validar CPF
      if (validarCPF(numbers)) {
        documentValid.value = true;
      } else {
        documentValid.value = false;
      }
    } else if (tipoPessoa.value === "PJ" && numbers.length === 14) {
      // Validar CNPJ
      if (validarCNPJ(numbers)) {
        documentValid.value = true;
      } else {
        documentValid.value = false;
      }
    } else {
      // Ainda digitando
      documentValid.value = null;
    }

    dados.value.documento = numbers;
    updateDadosValue();

    return documentValid.value;
  } catch (error) {
    console.warn('Erro em validateDocument:', error);
    return null;
  }
}

// Função para aplicar máscara no telefone
function applyPhoneMask(value) {
  const numbers = value.replace(/\D/g, "");

  if (numbers.length <= 2) return numbers;
  if (numbers.length <= 6)
    return `(${numbers.slice(0, 2)}) ${numbers.slice(2)}`;
  if (numbers.length <= 10)
    return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 6)}-${numbers.slice(6)}`;
  return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 7)}-${numbers.slice(7, 11)}`;
}

// Validação básica do telefone
function validateTelefone(value) {
  const numbers = value.replace(/\D/g, "");

  if (!numbers.trim()) {
    telefoneValid.value = null;
    dados.value.telefone = "";
    updateDadosValue();
    return null;
  }

  // Validação apenas se tiver pelo menos 10 dígitos
  if (numbers.length >= 10 && numbers.length <= 11) {
    telefoneValid.value = true;
  } else if (numbers.length > 11) {
    telefoneValid.value = false;
  } else {
    // Ainda digitando
    telefoneValid.value = null;
  }

  dados.value.telefone = numbers;
  updateDadosValue();
  return telefoneValid.value;
}

// Handlers do telefone
function handleTelefoneInput(event) {
  const value = event.target.value;
  const masked = applyPhoneMask(value);
  telefoneMasked.value = masked;

  // Validar durante digitação
  validateTelefone(masked);
}

function handleTelefoneBlur() {
  validateTelefone(telefoneMasked.value);
}

// Validação básica do segundo telefone
function validateTelefone2(value) {
  const numbers = value.replace(/\D/g, "");

  if (!numbers.trim()) {
    telefone2Valid.value = null;
    dados.value.telefone2 = "";
    updateDadosValue();
    return null;
  }

  // Validação apenas se tiver pelo menos 10 dígitos
  if (numbers.length >= 10 && numbers.length <= 11) {
    telefone2Valid.value = true;
  } else if (numbers.length > 11) {
    telefone2Valid.value = false;
  } else {
    // Ainda digitando
    telefone2Valid.value = null;
  }

  dados.value.telefone2 = numbers;
  updateDadosValue();
  return telefone2Valid.value;
}

// Handlers do segundo telefone
function handleTelefone2Input(event) {
  const value = event.target.value;
  const masked = applyPhoneMask(value);
  telefone2Masked.value = masked;

  // Validar durante digitação
  validateTelefone2(masked);
}

function handleTelefone2Blur() {
  validateTelefone2(telefone2Masked.value);
}

// Função para aplicar máscara na data brasileira
function applyDateMask(value) {
  const numbers = value.replace(/\D/g, "");
  
  if (numbers.length <= 2) return numbers;
  if (numbers.length <= 4) return `${numbers.slice(0, 2)}/${numbers.slice(2)}`;
  return `${numbers.slice(0, 2)}/${numbers.slice(2, 4)}/${numbers.slice(4, 8)}`;
}

// Validação da data brasileira
function validateDataNascimento(value) {
  const numbers = value.replace(/\D/g, "");

  if (!numbers.trim()) {
    dataNascimentoValid.value = null;
    dados.value.dataNascimento = "";
    updateDadosValue();
    return null;
  }

  // Validação apenas se tiver 8 dígitos
  if (numbers.length === 8) {
    const day = parseInt(numbers.slice(0, 2));
    const month = parseInt(numbers.slice(2, 4));
    const year = parseInt(numbers.slice(4, 8));

    // Validação básica
    if (day >= 1 && day <= 31 && month >= 1 && month <= 12 && year >= 1900 && year <= new Date().getFullYear()) {
      // Tentar criar data para validação mais rigorosa
      const date = new Date(year, month - 1, day);
      if (date.getDate() === day && date.getMonth() === month - 1 && date.getFullYear() === year) {
        dataNascimentoValid.value = true;
        dados.value.dataNascimento = `${numbers.slice(0, 2)}/${numbers.slice(2, 4)}/${numbers.slice(4, 8)}`;
      } else {
        dataNascimentoValid.value = false;
        dados.value.dataNascimento = value;
      }
    } else {
      dataNascimentoValid.value = false;
      dados.value.dataNascimento = value;
    }
  } else {
    // Ainda digitando
    dataNascimentoValid.value = null;
    dados.value.dataNascimento = value;
  }

  updateDadosValue();
  return dataNascimentoValid.value;
}

// Handlers da data de nascimento
function handleDataNascimentoInput(event) {
  const value = event.target.value;
  const masked = applyDateMask(value);
  dataNascimentoMasked.value = masked;

  // Validar durante digitação
  validateDataNascimento(masked);
}

function handleDataNascimentoBlur() {
  validateDataNascimento(dataNascimentoMasked.value);
}

// Handlers do CEP
function handleCepInput(event) {
  const value = event.target.value;
  const masked = applyCepMask(value);
  cepMasked.value = masked;

  // Reset validation state durante digitação
  if (cepValid.value !== null) {
    cepValid.value = null;
  }
}

function handleCepBlur() {
  validateCep(cepMasked.value);
}

function handleCepFocus() {
  // Focus handler - apenas para consistência
}

// Atualizar valor do componente (agora integrado)
function updateDadosValue() {
  try {
    if (!dados.value) return;
    
    emit("update:modelValue", { ...dados.value });

    // Validação completa
    const isValid =
      dados.value.documento &&
      dados.value.nome &&
      dados.value.cep &&
      dados.value.logradouro &&
      dados.value.numero &&
      dados.value.bairro &&
      dados.value.cidade &&
      dados.value.uf;

    emit("validation", {
      valid: isValid,
      dados: dados.value,
      tipoPessoa: tipoPessoa.value,
      tipoDocumento: tipoDocumento.value,
    });
  } catch (error) {
    console.warn('Erro em updateDadosValue:', error);
  }
}

// Manter função legacy para compatibilidade com código do CEP
function updateEnderecoValue() {
  // Sincronizar dados do endereço
  dados.value.cep = endereco.value.cep;
  dados.value.logradouro = endereco.value.logradouro;
  dados.value.numero = endereco.value.numero;
  dados.value.complemento = endereco.value.complemento;
  dados.value.bairro = endereco.value.bairro;
  dados.value.cidade = endereco.value.cidade;
  dados.value.uf = endereco.value.uf;

  updateDadosValue();
}

// Watch para mudanças externas no modelValue
watch(
  () => props.modelValue,
  (newValue) => {
    if (!newValue || typeof newValue !== "object") return;
    
    try {
      // Atualizar dados integrados de forma defensiva
      dados.value = { 
        ...dados.value, 
        ...Object.fromEntries(
          Object.entries(newValue).filter(([_, v]) => v !== undefined && v !== null)
        )
      };

      // Sincronizar endereço separado
      endereco.value = {
        cep: newValue.cep || "",
        logradouro: newValue.logradouro || "",
        numero: newValue.numero || "",
        complemento: newValue.complemento || "",
        bairro: newValue.bairro || "",
        cidade: newValue.cidade || "",
        uf: newValue.uf || "",
      };

      // Atualizar máscaras e tipo de pessoa
      if (newValue.documento) {
        documentMasked.value = applyDocumentMask(newValue.documento);
        tipoPessoa.value = detectDocumentType(newValue.documento);
      }

      if (newValue.telefone) {
        telefoneMasked.value = applyPhoneMask(newValue.telefone);
      }

      if (newValue.telefone2) {
        telefone2Masked.value = applyPhoneMask(newValue.telefone2);
      }

      if (newValue.cep) {
        cepMasked.value = applyCepMask(newValue.cep);
        showAddressFields.value = true;
      }

      if (newValue.dataNascimento) {
        dataNascimentoMasked.value = newValue.dataNascimento;
      }
    } catch (error) {
      console.warn('Erro ao atualizar EnderecoInput:', error);
    }
  },
  { immediate: true, deep: true }
);

// Inicialização
onMounted(() => {
  try {
    // Se já tem dados, preencher
    const modelValue = props.modelValue;
    if (
      modelValue &&
      typeof modelValue === "object" &&
      Object.keys(modelValue).length > 0
    ) {
      dados.value = { 
        ...dados.value, 
        ...Object.fromEntries(
          Object.entries(modelValue).filter(([_, v]) => v !== undefined && v !== null)
        )
      };

      // Sincronizar endereço
      endereco.value = {
        cep: modelValue.cep || "",
        logradouro: modelValue.logradouro || "",
        numero: modelValue.numero || "",
        complemento: modelValue.complemento || "",
        bairro: modelValue.bairro || "",
        cidade: modelValue.cidade || "",
        uf: modelValue.uf || "",
      };

      // Configurar máscaras
      if (modelValue.documento) {
        documentMasked.value = applyDocumentMask(modelValue.documento);
        tipoPessoa.value = detectDocumentType(modelValue.documento);
      }

      if (modelValue.telefone) {
        telefoneMasked.value = applyPhoneMask(modelValue.telefone);
      }

      if (modelValue.telefone2) {
        telefone2Masked.value = applyPhoneMask(modelValue.telefone2);
      }

      if (modelValue.cep) {
        cepMasked.value = applyCepMask(modelValue.cep);
      }

      if (modelValue.dataNascimento) {
        dataNascimentoMasked.value = modelValue.dataNascimento;
      }
    }
  } catch (error) {
    console.warn('Erro na inicialização do EnderecoInput:', error);
  }
});
</script>

<style scoped="postcss">
.endereco-container {
  @apply w-full;
}

.address-fields {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    max-height: 500px;
    transform: translateY(0);
  }
}

/* Estilos do BaseInput */
.input-label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.input-container {
  @apply relative flex items-center;
}

.input-container *:focus {
  outline: none !important;
}

.input-container input:focus {
  outline: none !important;
  box-shadow: none !important;
}

.input-container select:focus {
  outline: none !important;
  box-shadow: none !important;
}

.input-container--xs {
  @apply h-7;
}

.input-container--textarea {
  @apply min-h-[100px] items-start py-2;
}

.input-container--default {
  @apply border border-gray-300 rounded-md bg-white;
}

.input-container--disabled {
  @apply bg-gray-100 border-gray-200 cursor-not-allowed;
}

.input-container--error {
  @apply border-red-500 !important;
}

.input-container--success {
  @apply border-green-500 !important;
}

.input-field {
  @apply flex-1 border-0 bg-transparent outline-none placeholder-gray-400;
}

.input-field:focus {
  outline: none !important;
  box-shadow: none !important;
  border: none !important;
}

.input-field:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

.input-container:focus-within {
  @apply border-blue-400 shadow-sm;
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.3);
}

.input-field--xs {
  @apply text-[12px] px-2 leading-tight;
}

.input-field--textarea {
  @apply text-[12px] px-2 py-2 leading-tight resize-none;
}

.input-field--textarea:focus {
  outline: none !important;
  box-shadow: none !important;
}

.input-field:disabled {
  @apply cursor-not-allowed text-gray-500;
}

/* Estilos para autocomplete do navegador */
.input-field:-webkit-autofill,
.input-field:-webkit-autofill:hover,
.input-field:-webkit-autofill:focus,
.input-field:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px white inset !important;
  -webkit-text-fill-color: #374151 !important;
  transition: background-color 5000s ease-in-out 0s;
  font-size: 12px !important;
  height: 28px !important;
  line-height: 1 !important;
  padding: 0 8px !important;
}

.input-field:autofill {
  font-size: 12px !important;
  height: 28px !important;
  line-height: 1 !important;
  padding: 0 8px !important;
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .input-label {
    @apply text-gray-300;
  }

  .input-container--default {
    @apply border-slate-600/60 bg-slate-700/70 rounded-sm;
  }

  .input-container--disabled {
    @apply bg-gray-700 border-gray-600;
  }

  .input-container--error {
    @apply border-red-500 !important;
  }

  .input-container--success {
    @apply border-green-500 !important;
  }

  .input-field {
    @apply text-white placeholder-gray-400;
  }

  .input-field:disabled {
    @apply text-gray-400;
  }

  .input-field--textarea {
    @apply text-white placeholder-gray-400;
  }

  .input-container:focus-within {
    @apply border-blue-400;
    box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.4);
  }

  /* Estilos para autocomplete no dark mode */
  .input-field:-webkit-autofill,
  .input-field:-webkit-autofill:hover,
  .input-field:-webkit-autofill:focus,
  .input-field:-webkit-autofill:active {
    -webkit-box-shadow: 0 0 0 30px #374151 inset !important;
    -webkit-text-fill-color: #ffffff !important;
    transition: background-color 5000s ease-in-out 0s;
    font-size: 12px !important;
    height: 28px !important;
    line-height: 1 !important;
    padding: 0 8px !important;
  }
}
</style>
