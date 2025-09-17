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
        <p class="text-xs text-slate-400">
          {{ formatFileSize(selectedFile.size) }}
        </p>
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  accept: {
    type: String,
    default: "image/*,application/pdf",
  },
  maxSize: {
    type: Number,
    default: 5 * 1024 * 1024, // 5MB
  },
  modelValue: {
    type: String,
    default: "",
  },
});

const emit = defineEmits([
  "update:modelValue",
  "file-selected",
  "file-uploaded",
]);

const fileInput = ref();
const selectedFile = ref(null);
const uploading = ref(false);
const uploadProgress = ref(0);
const error = ref("");

const acceptText = computed(() => {
  if (props.accept.includes("image/*")) {
    return "Aceita imagens (JPG, PNG, etc.) e PDFs";
  }
  return "Tipos aceitos: " + props.accept;
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

function validateAndSetFile(file) {
  error.value = "";

  // Validar tamanho
  if (file.size > props.maxSize) {
    error.value = `Arquivo muito grande. Máximo: ${formatFileSize(props.maxSize)}`;
    return;
  }

  // Validar tipo
  const acceptTypes = props.accept.split(",").map((type) => type.trim());
  const isValidType = acceptTypes.some((type) => {
    if (type === "image/*") {
      return file.type.startsWith("image/");
    }
    if (type === "application/*") {
      return file.type.startsWith("application/");
    }
    return file.type === type;
  });

  if (!isValidType) {
    error.value = "Tipo de arquivo não permitido";
    return;
  }

  selectedFile.value = file;
  emit("file-selected", file);
  uploadFile(file);
}

/* async function uploadFile(file) {
  uploading.value = true;
  uploadProgress.value = 0;

  try {
    // 1. Obter URL de upload presignada do backend
    const { apiFetch } = await import('~/utils/api/index');

    // Determinar o tipo de arquivo baseado no label
    let fileType = 'DOCUMENT';
    if (props.label.toLowerCase().includes('foto') || props.label.toLowerCase().includes('perfil')) {
      fileType = 'PROFILE_PHOTO';
    } else if (props.label.toLowerCase().includes('endereço') || props.label.toLowerCase().includes('comprovante')) {
      fileType = 'ADDRESS_PROOF';
    }

    const uploadResponse = await apiFetch(`/api/v1/users/temp/files/upload-url`, {
      method: 'POST',
      body: {
        filename: file.name,
        contentType: file.type,
        fileType: fileType
      }
    });

    if (!uploadResponse.success) {
      throw new Error(uploadResponse.error || 'Erro ao obter URL de upload');
    }

    uploadProgress.value = 25;

    // 2. Upload direto para MinIO usando URL presignada
    const uploadUrl = uploadResponse.data.uploadUrl;
    const objectKey = uploadResponse.data.objectKey;
    const publicUrl = uploadResponse.data.publicUrl;

    // Fazer upload via PUT para a URL presignada
    const uploadResult = await fetch(uploadUrl, {
      method: 'PUT',
      body: file,
      headers: {
        'Content-Type': file.type,
      }
    });

    uploadProgress.value = 75;

    if (!uploadResult.ok) {
      throw new Error('Erro durante upload do arquivo');
    }

    uploadProgress.value = 100;

    // 3. Emitir a URL pública para o componente pai
    emit('update:modelValue', publicUrl);
    emit('file-uploaded', {
      objectKey: objectKey,
      publicUrl: publicUrl,
      filename: file.name,
      fileType: fileType
    });

  } catch (err) {
    console.error('Upload error:', err);
    error.value = err.message || 'Erro ao fazer upload do arquivo';
    selectedFile.value = null;
  } finally {
    uploading.value = false;
  }
} */

function removeFile() {
  selectedFile.value = null;
  error.value = "";
  emit("update:modelValue", "");
  if (fileInput.value) {
    fileInput.value.value = "";
  }
}

function formatFileSize(bytes) {
  if (bytes === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}
</script>
