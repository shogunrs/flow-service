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

    <BaseModal
      v-model="showCropper"
      title="Ajustar foto"
      size="lg"
      :close-on-backdrop="false"
      :z-index="2500"
    >
      <div class="space-y-3 cropper-wrapper">
        <p class="text-xs text-slate-300">
          Utilize os controles para ajustar o enquadramento. O recorte será aplicado antes do envio.
        </p>
        <div class="relative cropper-shell overflow-hidden rounded-2xl border border-slate-600 bg-slate-900/80">
          <div v-if="cropperProcessing" class="absolute inset-0 z-10 flex items-center justify-center bg-slate-900/70">
            <i class="fa-solid fa-spinner fa-spin text-lg text-indigo-400"></i>
          </div>
          <img
            v-if="cropperImageSrc"
            ref="cropperImageRef"
            :src="cropperImageSrc"
            alt="Editor de recorte"
            class="max-w-full"
          />
        </div>
        <div class="flex flex-wrap justify-center gap-3 text-xs">
          <button
            type="button"
            class="inline-flex items-center gap-1 px-3 py-2 rounded-md bg-slate-700 hover:bg-slate-600 text-slate-100"
            @click="zoomOut"
            :disabled="cropperProcessing"
          >
            <i class="fa-solid fa-magnifying-glass-minus"></i>
            Zoom -
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-1 px-3 py-2 rounded-md bg-slate-700 hover:bg-slate-600 text-slate-100"
            @click="zoomIn"
            :disabled="cropperProcessing"
          >
            <i class="fa-solid fa-magnifying-glass-plus"></i>
            Zoom +
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-1 px-3 py-2 rounded-md bg-slate-700 hover:bg-slate-600 text-slate-100"
            @click="resetCrop"
            :disabled="cropperProcessing"
          >
            <i class="fa-solid fa-arrows-rotate"></i>
            Resetar
          </button>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <button
            type="button"
            class="px-3 py-2 text-xs font-semibold rounded-md bg-slate-700 hover:bg-slate-600 text-slate-100"
            @click="cancelCrop"
            :disabled="cropperProcessing"
          >
            Cancelar
          </button>
          <button
            type="button"
            class="px-3 py-2 text-xs font-semibold rounded-md bg-indigo-600 hover:bg-indigo-500 text-white flex items-center gap-1 disabled:opacity-60"
            @click="confirmCrop"
            :disabled="cropperProcessing"
          >
            <i v-if="cropperProcessing" class="fa-solid fa-spinner fa-spin text-xs"></i>
            <span>Usar imagem</span>
          </button>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
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
    validator: (value) => [
      'profileImage', 'cpfImage', 'rgImage', 'cnpjImage', 'enderecoImage', 'telefoneImage', 'bancoImage', 'pixImage',
      'PROFILE_PHOTO', 'ADDRESS_PROOF', 'DOCUMENT', 'RG', 'CNH', 'IDENTITY',
      'CARTAO_CNPJ', 'CONTRATO_SOCIAL', 'QUALIFICACAO_SOCIOS'
    ].includes(value)
  },
  enableOcr: {
    type: Boolean,
    default: false
  },
  enableCrop: {
    type: Boolean,
    default: false
  },
  cropAspectRatio: {
    type: Number,
    default: 1
  },
  cropWidth: {
    type: Number,
    default: 600
  },
  cropHeight: {
    type: Number,
    default: 600
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

const showCropper = ref(false);
const cropperImageSrc = ref('');
const cropperImageRef = ref(null);
const cropper = ref(null);
const pendingFile = ref(null);
const cropperProcessing = ref(false);

const { uploadUserFile } = useUserFileUpload();
const {
  extractFromCPF,
  extractFromRG,
  extractText,
  validateDocumentData,
  isProcessing,
  progress,
} = useOCR();

// Watch OCR progress
watch(isProcessing, (processing) => {
  ocrProcessing.value = processing
})

watch(progress, (newProgress) => {
  ocrProgress.value = newProgress
})

watch(showCropper, async (visible) => {
  if (visible) {
    await nextTick();
    cropperProcessing.value = false;
    if (cropper.value) {
      cropper.value.destroy();
      cropper.value = null;
    }
    if (cropperImageRef.value) {
      cropper.value = new Cropper(cropperImageRef.value, {
        aspectRatio: props.cropAspectRatio || 1,
        viewMode: 1,
        autoCropArea: 0.9,
        responsive: true,
        background: false,
        dragMode: 'move',
        movable: true,
        zoomable: true,
        scalable: false,
        rotatable: false,
        ready() {
          this.cropper.setDragMode('move');
        },
      });
    }
  } else {
    if (cropper.value) {
      cropper.value.destroy();
      cropper.value = null;
    }
    cropperImageSrc.value = '';
    pendingFile.value = null;
    cropperProcessing.value = false;
  }
});

onBeforeUnmount(() => {
  if (cropper.value) {
    cropper.value.destroy();
    cropper.value = null;
  }
});

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

  const isImage = file.type.startsWith('image/');

  if (props.enableCrop && isImage) {
    pendingFile.value = file;
    try {
      cropperImageSrc.value = await readFileAsDataURL(file);
      showCropper.value = true;
    } catch (err) {
      console.error('Crop preview error:', err);
      error.value = err.message || 'Não foi possível carregar a imagem para recorte';
      selectedFile.value = null;
      pendingFile.value = null;
    }
    return;
  }

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
    const hasUser = Boolean(props.userId) && !isTemporarySession(props.userId);

    if (hasUser) {
      uploadData = await uploadUserFile(props.userId, file, props.fileType);
    } else {
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

function readFileAsDataURL(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        resolve(reader.result);
      } else {
        reject(new Error('Formato de arquivo inválido'));
      }
    };
    reader.onerror = () => reject(new Error('Erro ao ler arquivo'));
    reader.readAsDataURL(file);
  });
}

function cancelCrop() {
  if (cropperProcessing.value) return;
  showCropper.value = false;
  pendingFile.value = null;
  selectedFile.value = null;
  error.value = '';
  success.value = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

async function confirmCrop() {
  if (!cropper.value || !pendingFile.value) return;

  cropperProcessing.value = true;

  try {
    const canvas = cropper.value.getCroppedCanvas({
      width: props.cropWidth || undefined,
      height: props.cropHeight || undefined,
      imageSmoothingQuality: 'high',
    });

    if (!canvas) {
      throw new Error('Não foi possível gerar a imagem recortada');
    }

    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob((blobResult) => {
        if (blobResult) {
          resolve(blobResult);
        } else {
          reject(new Error('Não foi possível gerar a imagem recortada'));
        }
      }, pendingFile.value.type || 'image/jpeg', 0.95);
    });

    const croppedFileName = pendingFile.value.name || 'profile-photo.jpg';
    const croppedFileType = blob.type || pendingFile.value.type || 'image/jpeg';
    const croppedFile = new File([blob], croppedFileName, {
      type: croppedFileType,
      lastModified: Date.now(),
    });

    showCropper.value = false;
    selectedFile.value = croppedFile;
    emit('file-selected', croppedFile);
    pendingFile.value = null;

    if (props.enableOcr && isDocumentFile(croppedFile)) {
      await processOCR(croppedFile);
    }

    await uploadFile(croppedFile);
  } catch (err) {
    console.error('Crop error:', err);
    error.value = err.message || 'Erro ao recortar a imagem';
  } finally {
    cropperProcessing.value = false;
  }
}

function zoomIn() {
  cropper.value?.zoom(0.1);
}

function zoomOut() {
  cropper.value?.zoom(-0.1);
}

function resetCrop() {
  if (!cropper.value) return;
  cropper.value.reset();
}

// Helper functions ---------------------------------------------------------
const ocrSupportedTypes = new Set([
  'DOCUMENT',
  'IDENTITY',
  'CPF',
  'CPFIMAGE',
  'RG',
  'RGIMAGE',
  'CNH',
  'CNHIMAGE',
  'ADDRESS_PROOF',
  'ENDERECOIMAGE',
  'CARTAO_CNPJ',
  'CNPJIMAGE',
  'CONTRATO_SOCIAL',
  'QUALIFICACAO_SOCIOS',
]);

function normalizeFileType(value) {
  if (!value) return '';
  return String(value).trim().toUpperCase();
}

function isTemporarySession(id) {
  if (typeof id !== 'string') return false;
  return id.startsWith('temp_') || id.startsWith('session_');
}

function isDocumentFile(file) {
  const normalizedType = normalizeFileType(props.fileType);
  return ocrSupportedTypes.has(normalizedType) &&
         (file.type.startsWith('image/') || file.type === 'application/pdf');
}

// OCR Processing function
async function processOCR(file) {
  try {
    ocrResult.value = null;
    error.value = '';

    let result;

    // Escolhe o método OCR com base no tipo do arquivo solicitado
    const normalizedType = normalizeFileType(props.fileType);
    switch (normalizedType) {
      case 'CPF':
      case 'CPFIMAGE':
        result = await extractFromCPF(file);
        break;
      case 'RG':
      case 'RGIMAGE':
      case 'DOCUMENT':
      case 'IDENTITY':
      case 'CNH':
      case 'CNHIMAGE':
        result = await extractFromRG(file);
        break;
      case 'CARTAO_CNPJ':
      case 'CNPJIMAGE':
      case 'CONTRATO_SOCIAL':
      case 'QUALIFICACAO_SOCIOS':
      case 'ADDRESS_PROOF':
      case 'ENDERECOIMAGE':
        result = await extractText(file);
        break;
      default:
        result = await extractText(file);
        break;
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

<style scoped>
.cropper-shell {
  position: relative;
  background: rgba(15, 23, 42, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  width: clamp(320px, 70vw, 560px);
  height: clamp(320px, 70vw, 560px);
  margin: 0 auto;
  border-radius: 1.25rem;
  border: 1px solid rgba(148, 163, 184, 0.25);
}

:deep(.cropper-container) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.cropper-container img) {
  max-width: 100% !important;
  max-height: 100% !important;
  display: block;
}

:deep(.cropper-view-box) {
  border-radius: 0;
  box-shadow: none;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

:deep(.cropper-face) {
  background-color: transparent;
}
</style>
