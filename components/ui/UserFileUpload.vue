<template>
  <div class="space-y-2">
    <label class="text-[12px] text-slate-300">{{ label }}</label>
    <div
      class="border-2 border-dashed border-slate-600 rounded-lg p-4 text-center cursor-pointer hover:border-slate-500 transition-colors"
      @click="triggerFileInput"
      @dragover.prevent
      @drop.prevent="handleDrop"
    >
      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        class="hidden"
        @change="handleFileSelect"
      />

      <!-- Upload State -->
      <div v-if="!selectedFile && !uploading">
        <i class="fa-solid fa-cloud-arrow-up text-2xl text-slate-400 mb-2"></i>
        <p class="text-sm text-slate-300">
          Clique para selecionar ou arraste o arquivo aqui
        </p>
        <p class="text-xs text-slate-400 mt-1">{{ acceptText }}</p>
      </div>

      <!-- OCR Processing State -->
      <div v-else-if="ocrProcessing" class="py-2">
        <i class="fa-solid fa-brain text-xl text-blue-500 mb-2"></i>
        <p class="text-sm text-slate-300">Extraindo dados do documento...</p>
        <div class="w-full bg-slate-700 rounded-full h-2 mt-2">
          <div
            class="bg-blue-500 h-2 rounded-full transition-all duration-300"
            :style="{ width: ocrProgress + '%' }"
          ></div>
        </div>
        <p class="text-xs text-slate-400 mt-1">{{ ocrProgress }}% concluído</p>
      </div>

      <!-- Uploading State -->
      <div v-else-if="uploading" class="py-2">
        <i class="fa-solid fa-spinner fa-spin text-xl text-orange-500 mb-2"></i>
        <p class="text-sm text-slate-300">Enviando arquivo...</p>
        <div class="w-full bg-slate-700 rounded-full h-2 mt-2">
          <div
            class="bg-indigo-600 h-2 rounded-full transition-all duration-300"
            :style="{ width: uploadProgress + '%' }"
          ></div>
        </div>
      </div>

      <!-- File Selected State -->
      <div v-else-if="selectedFile" class="py-2">
        <i class="fa-solid fa-file text-xl text-green-500 mb-2"></i>
        <p class="text-sm text-slate-300 truncate">{{ selectedFile.name }}</p>
        <p class="text-xs text-slate-400">{{ formatFileSize(selectedFile.size) }}</p>

        <!-- OCR Results -->
        <div v-if="ocrResult && ocrResult.extractedData" class="mt-2 p-2 bg-blue-900/20 border border-blue-700/30 rounded text-xs">
          <p class="text-blue-400 font-medium mb-1">
            <i class="fa-solid fa-magic-wand-sparkles mr-1"></i>
            Dados extraídos automaticamente:
          </p>

          <!-- Foto extraída do documento -->
          <div v-if="ocrResult.extractedPhoto" class="mb-2 flex items-center space-x-2">
            <img :src="ocrResult.extractedPhoto" alt="Foto extraída do documento" class="w-16 h-16 object-cover rounded border border-blue-700/50">
            <div class="text-xs">
              <p class="text-green-400">
                <i class="fa-solid fa-camera mr-1"></i>
                Foto extraída para validação facial
              </p>
            </div>
          </div>

          <div class="space-y-1">
            <div v-if="ocrResult.extractedData.nome" class="text-slate-300">
              <span class="text-slate-400">Nome:</span> {{ ocrResult.extractedData.nome }}
            </div>
            <div v-if="ocrResult.extractedData.cpf" class="text-slate-300">
              <span class="text-slate-400">CPF:</span> {{ ocrResult.extractedData.cpf }}
            </div>
            <div v-if="ocrResult.extractedData.rg" class="text-slate-300">
              <span class="text-slate-400">RG:</span> {{ ocrResult.extractedData.rg }}
            </div>
            <div v-if="ocrResult.extractedData.orgaoExpedidor" class="text-slate-300">
              <span class="text-slate-400">Órgão:</span> {{ ocrResult.extractedData.orgaoExpedidor }}
            </div>
            <div v-if="ocrResult.extractedData.dataEmissao" class="text-slate-300">
              <span class="text-slate-400">Emissão:</span> {{ ocrResult.extractedData.dataEmissao }}
            </div>
          </div>

          <!-- Resultado da validação cruzada -->
          <div v-if="ocrResult.validation" class="mt-2 pt-2 border-t border-blue-700/30">
            <div v-if="ocrResult.validation.isValid" class="text-green-400">
              <i class="fa-solid fa-check-circle mr-1"></i>
              Documento validado ({{ ocrResult.validation.confidenceScore }}% confiança)
            </div>
            <div v-else class="text-red-400">
              <i class="fa-solid fa-exclamation-triangle mr-1"></i>
              Validação falhou
            </div>

            <!-- Warnings -->
            <div v-if="ocrResult.validation.warnings.length > 0" class="mt-1">
              <div v-for="warning in ocrResult.validation.warnings" :key="warning" class="text-yellow-400 text-xs">
                <i class="fa-solid fa-warning mr-1"></i>
                {{ warning }}
              </div>
            </div>
          </div>

          <p class="text-slate-500 mt-1">Confiança OCR: {{ Math.round(ocrResult.confidence) }}%</p>
        </div>

        <button
          class="mt-2 text-red-400 hover:text-red-300 text-xs"
          @click.stop="removeFile"
        >
          <i class="fa-solid fa-trash mr-1"></i>
          Remover
        </button>
      </div>

      <!-- Error State -->
      <div v-if="error" class="mt-2 text-red-400 text-xs">
        {{ error }}
      </div>

      <!-- Success State -->
      <div v-if="success" class="mt-2 text-green-400 text-xs">
        <i class="fa-solid fa-check mr-1"></i>
        {{ success }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useUserFileUpload } from '~/composables/useUserFileUpload';
import { useOCR } from '~/composables/useOCR';

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  accept: {
    type: String,
    default: 'image/*,application/pdf',
  },
  maxSize: {
    type: Number,
    default: 5 * 1024 * 1024, // 5MB
  },
  modelValue: {
    type: String,
    default: '',
  },
  userId: {
    type: String,
    required: false,
  },
  fileType: {
    type: String,
    required: true,
    validator: (value) => ['profileImage', 'cpfImage', 'rgImage', 'cnpjImage', 'enderecoImage', 'telefoneImage', 'bancoImage', 'pixImage', 'PROFILE_PHOTO', 'ADDRESS_PROOF', 'DOCUMENT', 'RG', 'CNH', 'IDENTITY'].includes(value)
  },
  enableOcr: {
    type: Boolean,
    default: false
  },
  temporaryMode: {
    type: Boolean,
    default: false,
  },
  sessionId: {
    type: String,
    required: false,
  },
  formData: {
    type: Object,
    default: () => ({}),
    validator: (value) => typeof value === 'object'
  }
});

const emit = defineEmits(['update:modelValue', 'file-selected', 'file-uploaded', 'ocr-result']);

const fileInput = ref();
const selectedFile = ref(null);
const uploading = ref(false);
const uploadProgress = ref(0);
const error = ref('');
const success = ref('');
const ocrProcessing = ref(false);
const ocrProgress = ref(0);
const ocrResult = ref(null);

const { uploadUserFile } = useUserFileUpload();
const { extractFromCPF, extractFromRG, extractText, validateDocumentData, extractPhotoFromDocument, isProcessing, progress } = useOCR();

// Watch OCR progress
watch(isProcessing, (processing) => {
  ocrProcessing.value = processing
})

watch(progress, (newProgress) => {
  ocrProgress.value = newProgress
})

const acceptText = computed(() => {
  if (props.accept.includes('image/*')) {
    return 'Aceita imagens (JPG, PNG, etc.) e PDFs';
  }
  return 'Tipos aceitos: ' + props.accept;
});

function triggerFileInput() {
  if (uploading.value) return;
  fileInput.value?.click();
}

function handleFileSelect(event) {
  const file = event.target.files[0];
  if (file) {
    validateAndSetFile(file);
  }
}

function handleDrop(event) {
  const file = event.dataTransfer.files[0];
  if (file) {
    validateAndSetFile(file);
  }
}

async function validateAndSetFile(file) {
  error.value = '';
  success.value = '';

  // Validar tamanho
  if (file.size > props.maxSize) {
    error.value = `Arquivo muito grande. Máximo: ${formatFileSize(props.maxSize)}`;
    return;
  }

  // Validar tipo
  const acceptTypes = props.accept.split(',').map(type => type.trim());
  const isValidType = acceptTypes.some(type => {
    if (type === 'image/*') {
      return file.type.startsWith('image/');
    }
    if (type === 'application/*') {
      return file.type.startsWith('application/');
    }
    return file.type === type;
  });

  if (!isValidType) {
    error.value = 'Tipo de arquivo não permitido';
    return;
  }

  selectedFile.value = file;
  emit('file-selected', file);

  // Processamento OCR antes do upload
  if (props.enableOcr && isDocumentFile(file)) {
    await processOCR(file);
  }

  uploadFile(file);
}

async function uploadFile(file) {
  uploading.value = true;
  uploadProgress.value = 0;

  try {
    uploadProgress.value = 25;

    let uploadData;

    // Verifica se temos um userId para fazer upload normal
    if (props.userId) {
      uploadData = await uploadUserFile(props.userId, file, props.fileType);
    } else {
      // Se não tem userId, não pode fazer upload (usuário deve ser criado primeiro)
      error.value = 'Usuário deve ser salvo antes de enviar arquivos. Preencha nome e email válidos.';
      return;
    }
    success.value = 'Arquivo enviado com sucesso!';

    uploadProgress.value = 75;
    uploadProgress.value = 100;

    // Emitir dados do upload
    emit('update:modelValue', uploadData.publicUrl);
    emit('file-uploaded', uploadData);

  } catch (err) {
    console.error('Upload error:', err);
    error.value = err.message || 'Erro ao fazer upload do arquivo';
    selectedFile.value = null;
  } finally {
    uploading.value = false;
  }
}

function removeFile() {
  selectedFile.value = null;
  error.value = '';
  success.value = '';
  emit('update:modelValue', '');
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Helper function to check if file is a document that supports OCR
function isDocumentFile(file) {
  // Agora suporta qualquer tipo de documento que seja imagem ou PDF
  const documentTypes = ['cpfImage', 'rgImage', 'cnpjImage', 'profileImage', 'enderecoImage', 'telefoneImage', 'bancoImage'];
  return documentTypes.includes(props.fileType) &&
         (file.type.startsWith('image/') || file.type === 'application/pdf');
}

// OCR Processing function
async function processOCR(file) {
  try {
    ocrResult.value = null;
    error.value = '';

    let result;

    // Choose OCR method based on file type
    switch (props.fileType) {
      case 'cpfImage':
        result = await extractFromCPF(file);
        break;
      case 'rgImage':
        result = await extractFromRG(file);
        break;
      case 'cnpjImage':
        result = await extractText(file);
        break;
      default:
        result = await extractText(file);
    }

    if (result && result.extractedData) {
      ocrResult.value = result;

      // Validação cruzada se temos dados do formulário
      let validationResult = null;
      if (props.formData && (props.formData.nome || props.formData.email)) {
        console.log('🔍 Iniciando validação cruzada dos dados...');

        try {
          validationResult = await validateDocumentData(props.formData, result, file);

          console.log('✅ Resultado da validação:', validationResult);

          // Mostra resultado da validação
          if (validationResult.isValid) {
            success.value = `✅ Documento válido! Confiança: ${validationResult.confidenceScore}%`;
          } else {
            error.value = `❌ Problemas na validação: ${validationResult.errors.join(', ')}`;
          }

          // Mostra warnings se houver
          if (validationResult.warnings.length > 0) {
            console.warn('⚠️ Warnings:', validationResult.warnings);
          }

        } catch (validationError) {
          console.error('Erro na validação cruzada:', validationError);
        }
      }

      // Emit OCR result to parent component com dados de validação
      emit('ocr-result', {
        fileType: props.fileType,
        extractedData: result.extractedData,
        confidence: result.confidence,
        fullText: result.text,
        validation: validationResult, // Dados da validação cruzada
        extractedPhoto: validationResult?.extractedPhoto // Foto extraída do documento
      });

      // Show success message se não houve validação
      if (!validationResult) {
        const extractedFields = Object.keys(result.extractedData).length;
        if (extractedFields > 0) {
          success.value = `OCR: ${extractedFields} campo(s) extraído(s) automaticamente`;
        }
      }
    } else {
      console.warn('OCR não encontrou dados estruturados');
    }

  } catch (error) {
    console.error('Erro no processamento OCR:', error);
    // Não mostrar erro de OCR para o usuário, só continuar com upload normal
  }
}
</script>