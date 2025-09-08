<template>
  <div class="input-wrapper" :class="wrapperClasses">
    <label v-if="label" :for="inputId" class="input-label">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <div class="input-container" :class="containerClasses">
      <input
        :id="inputId"
        v-model="maskedValue"
        type="text"
        :placeholder="placeholder || '00000-000'"
        :required="required"
        :disabled="disabled"
        class="input-field input-field--xs"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        maxlength="9"
      />

      <div class="input-suffix">
        <i
          v-if="isValid === true"
          class="fa-solid fa-check text-green-500 input-icon"
        ></i>
        <i
          v-else-if="isValid === false"
          class="fa-solid fa-times text-red-500 input-icon"
        ></i>
        <i
          v-else-if="isValidating"
          class="fa-solid fa-spinner fa-spin text-slate-400 input-icon"
        ></i>
      </div>
    </div>

    <div
      v-if="
        errorMessage ||
        addressInfo ||
        (showValidationInfo && maskedValue && isValid !== null)
      "
      class="input-help"
    >
      <p v-if="errorMessage" class="input-error">
        {{ errorMessage }}
      </p>
      <div
        v-if="addressInfo && isValid === true"
        class="text-[10px] text-slate-400 mt-1"
      >
        <div class="bg-slate-900/50 p-2 rounded border border-slate-700/40">
          <div class="font-medium text-slate-300">
            {{ addressInfo.logradouro }}
          </div>
          <div>
            {{ addressInfo.bairro }}, {{ addressInfo.localidade }}/{{
              addressInfo.uf
            }}
          </div>
        </div>
      </div>
      <div
        v-if="showValidationInfo && maskedValue && isValid !== null"
        class="text-[10px] text-slate-400 mt-1"
      >
        <div class="space-y-0.5">
          <div
            :class="validationChecks.format ? 'text-green-400' : 'text-red-400'"
          >
            <i
              class="fa-solid fa-check mr-1"
              v-if="validationChecks.format"
            ></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            Formato válido
          </div>
          <div
            :class="validationChecks.exists ? 'text-green-400' : 'text-red-400'"
            v-if="validationChecks.exists !== null"
          >
            <i
              class="fa-solid fa-check mr-1"
              v-if="validationChecks.exists"
            ></i>
            <i class="fa-solid fa-times mr-1" v-else></i>
            CEP existe
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from "vue";

const props = defineProps({
  modelValue: { type: String, default: "" },
  label: { type: String, default: "" },
  placeholder: { type: String, default: "" },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  showValidationInfo: { type: Boolean, default: false },
  autoFetchAddress: { type: Boolean, default: true },
});

const emit = defineEmits(["update:modelValue", "validation", "address"]);

const inputId = computed(() => `cep-${Math.random().toString(36).slice(2, 8)}`);
const maskedValue = ref("");
const isValid = ref(null);
const isValidating = ref(false);
const errorMessage = ref("");
const addressInfo = ref(null);
const validationChecks = ref({
  format: false,
  exists: null,
});

// Classes do BaseInput
const wrapperClasses = computed(() => {
  const classes = [];
  if (props.disabled) classes.push("input-wrapper--disabled");
  if (isValid.value === false) classes.push("input-wrapper--error");
  if (isValid.value === true) classes.push("input-wrapper--success");
  return classes;
});

const containerClasses = computed(() => {
  const classes = [];
  classes.push("input-container--xs");
  /* classes.push('input-container--default') */
  if (props.disabled) classes.push("input-container--disabled");
  if (isValid.value === false) classes.push("input-container--error");
  if (isValid.value === true) classes.push("input-container--success");
  return classes;
});

// Aplicar máscara ao CEP
function applyCepMask(value) {
  const numbers = value.replace(/\D/g, "");
  if (numbers.length <= 5) return numbers;
  return `${numbers.slice(0, 5)}-${numbers.slice(5, 8)}`;
}

// Remover máscara para obter apenas números
function removeCepMask(value) {
  return value.replace(/\D/g, "");
}

// Buscar endereço via ViaCEP
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
      localidade: data.localidade,
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

// Validação completa do CEP
async function validateCep(value) {
  if (!value.trim()) {
    isValid.value = null;
    errorMessage.value = "";
    addressInfo.value = null;
    validationChecks.value = { format: false, exists: null };
    return null;
  }

  isValidating.value = true;

  const numbers = removeCepMask(value);

  // Validação de formato
  const formatValid = /^\d{8}$/.test(numbers);
  validationChecks.value.format = formatValid;

  if (!formatValid) {
    isValid.value = false;
    errorMessage.value = "CEP deve ter 8 dígitos";
    addressInfo.value = null;
    validationChecks.value.exists = null;
    isValidating.value = false;
    emit("validation", {
      valid: false,
      cep: numbers,
      checks: validationChecks.value,
    });
    return false;
  }

  // Buscar endereço se habilitado
  if (props.autoFetchAddress) {
    const address = await fetchAddress(numbers);

    if (address) {
      validationChecks.value.exists = true;
      addressInfo.value = address;
      isValid.value = true;
      errorMessage.value = "";
      emit("address", address);
    } else {
      validationChecks.value.exists = false;
      addressInfo.value = null;
      isValid.value = false;
      errorMessage.value = "CEP não encontrado";
    }
  } else {
    // Validação simples sem busca de endereço
    validationChecks.value.exists = true;
    isValid.value = true;
    errorMessage.value = "";
  }

  isValidating.value = false;
  emit("validation", {
    valid: isValid.value,
    cep: numbers,
    address: addressInfo.value,
    checks: validationChecks.value,
  });

  return isValid.value;
}

function handleInput(event) {
  const value = event.target.value;
  const masked = applyCepMask(value);
  maskedValue.value = masked;

  const cleanValue = removeCepMask(masked);
  emit("update:modelValue", cleanValue);

  // Reset validation state durante digitação
  if (isValid.value !== null) {
    isValid.value = null;
    errorMessage.value = "";
    addressInfo.value = null;
  }
}

function handleBlur() {
  validateCep(maskedValue.value);
}

function handleFocus() {
  errorMessage.value = "";
}

// Watch para mudanças externas no modelValue
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== removeCepMask(maskedValue.value)) {
      maskedValue.value = applyCepMask(newValue || "");
    }
  },
  { immediate: true }
);
</script>
