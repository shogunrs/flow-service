<template>
  <div class="space-y-4">
    <div class="text-center">
      <h3 class="text-lg font-semibold text-slate-200 mb-2">
        {{ mode === 'register' ? 'Cadastrar Face' : 'Reconhecimento Facial' }}
      </h3>
      <p class="text-sm text-slate-400">
        {{ mode === 'register'
           ? 'Tire uma foto para registrar seu rosto'
           : 'Posicione seu rosto na câmera para autenticação'
        }}
      </p>
    </div>

    <div class="flex flex-col items-center space-y-4">
      <!-- Camera/Preview Area -->
      <div class="relative bg-slate-800 rounded-lg overflow-hidden" style="width: 320px; height: 240px;">
        <!-- Video Stream -->
        <video
          ref="videoElement"
          v-show="!capturedImage && cameraActive"
          class="w-full h-full object-cover"
          autoplay
          muted
          playsinline
        />

        <!-- Captured Image Preview -->
        <img
          v-if="capturedImage"
          :src="capturedImage"
          class="w-full h-full object-cover"
          alt="Captured face"
        />

        <!-- Camera inactive placeholder -->
        <div
          v-if="!cameraActive && !capturedImage"
          class="w-full h-full flex items-center justify-center"
        >
          <div class="text-center text-slate-400">
            <i class="fa-solid fa-camera text-4xl mb-2"></i>
            <p class="text-sm">Câmera inativa</p>
          </div>
        </div>

        <!-- Face Detection Overlay -->
        <div
          v-if="cameraActive && !capturedImage"
          class="absolute inset-0 pointer-events-none"
        >
          <div class="w-full h-full border-2 border-dashed border-indigo-500/50 rounded-lg flex items-center justify-center">
            <div class="w-32 h-32 border-2 border-indigo-500 rounded-full animate-pulse"></div>
          </div>
        </div>

        <!-- Loading Overlay -->
        <div
          v-if="processing"
          class="absolute inset-0 bg-black/60 flex items-center justify-center"
        >
          <div class="text-center text-white">
            <i class="fa-solid fa-spinner fa-spin text-2xl mb-2"></i>
            <p class="text-sm">{{ mode === 'register' ? 'Registrando...' : 'Reconhecendo...' }}</p>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="text-red-400 text-sm text-center">
        <i class="fa-solid fa-exclamation-triangle mr-1"></i>
        {{ error }}
      </div>

      <!-- Success Message -->
      <div v-if="success" class="text-green-400 text-sm text-center">
        <i class="fa-solid fa-check-circle mr-1"></i>
        {{ success }}
      </div>

      <!-- Controls -->
      <div class="flex gap-3">
        <button
          v-if="!cameraActive && !capturedImage"
          @click="startCamera"
          class="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md text-sm flex items-center gap-2"
          :disabled="processing"
        >
          <i class="fa-solid fa-camera"></i>
          Iniciar Câmera
        </button>

        <button
          v-if="cameraActive && !capturedImage"
          @click="capturePhoto"
          class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-md text-sm flex items-center gap-2"
          :disabled="processing"
        >
          <i class="fa-solid fa-camera-retro"></i>
          Capturar
        </button>

        <button
          v-if="capturedImage && !processing"
          @click="processImage"
          class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm flex items-center gap-2"
        >
          <i class="fa-solid fa-brain"></i>
          {{ mode === 'register' ? 'Registrar' : 'Autenticar' }}
        </button>

        <button
          v-if="capturedImage || cameraActive"
          @click="reset"
          class="bg-slate-600 hover:bg-slate-700 text-white px-4 py-2 rounded-md text-sm flex items-center gap-2"
          :disabled="processing"
        >
          <i class="fa-solid fa-redo"></i>
          Refazer
        </button>
      </div>

      <!-- Location Permission -->
      <div v-if="locationStatus === 'requesting'" class="text-sm text-slate-400 text-center">
        <i class="fa-solid fa-location-dot mr-1"></i>
        Solicitando permissão de localização...
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useGeolocation } from '~/composables/useGeolocation';
import { useFaceRecognition } from '~/composables/useFaceRecognition';

const props = defineProps({
  mode: {
    type: String,
    default: 'register', // 'register' or 'authenticate'
    validator: (value) => ['register', 'authenticate'].includes(value)
  },
  userId: {
    type: String,
    default: null
  }
});

const emit = defineEmits(['success', 'error']);

const videoElement = ref();
const cameraActive = ref(false);
const capturedImage = ref(null);
const processing = ref(false);
const error = ref('');
const success = ref('');
const locationStatus = ref('idle'); // 'idle', 'requesting', 'granted', 'denied'

let mediaStream = null;

const { getCurrentLocation, formatLocation } = useGeolocation();
const { registerFace, authenticateByFace, extractFaceEmbedding } = useFaceRecognition();

onMounted(() => {
  requestLocationPermission();
});

onUnmounted(() => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
  }
});

async function requestLocationPermission() {
  locationStatus.value = 'requesting';
  try {
    await getCurrentLocation();
    locationStatus.value = 'granted';
  } catch (err) {
    locationStatus.value = 'denied';
    console.warn('Location access denied:', err.message);
  }
}

async function startCamera() {
  try {
    error.value = '';
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { width: 320, height: 240 }
    });

    if (videoElement.value) {
      videoElement.value.srcObject = mediaStream;
      cameraActive.value = true;
    }
  } catch (err) {
    error.value = 'Erro ao acessar câmera. Verifique as permissões.';
    console.error('Camera error:', err);
  }
}

function capturePhoto() {
  if (!videoElement.value || !cameraActive.value) return;

  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');

  canvas.width = videoElement.value.videoWidth;
  canvas.height = videoElement.value.videoHeight;

  context.drawImage(videoElement.value, 0, 0);
  capturedImage.value = canvas.toDataURL('image/jpeg', 0.8);

  // Para a câmera após capturar
  stopCamera();
}

function stopCamera() {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
    mediaStream = null;
  }
  cameraActive.value = false;
}

async function processImage() {
  if (!capturedImage.value) return;

  processing.value = true;
  error.value = '';
  success.value = '';

  try {
    // Converter base64 para File object
    const response = await fetch(capturedImage.value);
    const blob = await response.blob();
    const file = new File([blob], 'face.jpg', { type: 'image/jpeg' });

    // Extrair embedding facial
    const faceEmbedding = await extractFaceEmbedding(file);

    // Obter localização
    let location = null;
    if (locationStatus.value === 'granted') {
      try {
        const locationData = await getCurrentLocation();
        location = formatLocation(locationData);
      } catch (err) {
        console.warn('Failed to get location:', err.message);
      }
    }

    const data = {
      faceEmbedding,
      ipAddress: '', // Será capturado pelo backend
      location
    };

    if (props.mode === 'register') {
      if (!props.userId) {
        throw new Error('User ID é obrigatório para registro');
      }

      const result = await registerFace(props.userId, data);
      success.value = 'Face registrada com sucesso!';
      emit('success', result);
    } else {
      const result = await authenticateByFace(data);
      success.value = `Bem-vindo, ${result.name}!`;
      emit('success', result);
    }

  } catch (err) {
    error.value = err.message || 'Erro durante o processamento';
    emit('error', err);
  } finally {
    processing.value = false;
  }
}

function reset() {
  stopCamera();
  capturedImage.value = null;
  error.value = '';
  success.value = '';
  processing.value = false;
}
</script>