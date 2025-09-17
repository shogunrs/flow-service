<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-900 to-slate-800 flex flex-col">
    <!-- Header -->
    <div class="bg-slate-800 px-4 py-3 shadow-lg">
      <h1 class="text-xl font-semibold text-white text-center">
        Reconhecimento Facial
      </h1>
      <p class="text-sm text-slate-300 text-center mt-1">
        Posicione seu rosto na área indicada
      </p>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col items-center justify-center p-4">
      <!-- Camera Container -->
      <div class="relative w-80 h-80 max-w-sm">
        <!-- Face Detection Frame -->
        <div class="absolute inset-0 border-4 border-dashed border-white rounded-full opacity-50 z-10"></div>

        <!-- Video Stream -->
        <video
          ref="videoElement"
          class="w-full h-full object-cover rounded-full bg-slate-700"
          autoplay
          muted
          playsinline
        ></video>

        <!-- Loading Overlay -->
        <div
          v-if="isLoading"
          class="absolute inset-0 bg-black bg-opacity-50 rounded-full flex items-center justify-center z-20"
        >
          <div class="text-center text-white">
            <i class="fa-solid fa-spinner fa-spin text-3xl mb-2"></i>
            <p class="text-sm">{{ loadingMessage }}</p>
          </div>
        </div>

        <!-- Success Overlay -->
        <div
          v-if="captureSuccess"
          class="absolute inset-0 bg-green-500 bg-opacity-20 rounded-full flex items-center justify-center z-20"
        >
          <i class="fa-solid fa-check text-green-400 text-4xl"></i>
        </div>
      </div>

      <!-- Instructions -->
      <div class="mt-6 text-center max-w-sm">
        <p class="text-white text-sm mb-4">
          {{ currentInstruction }}
        </p>

        <!-- Quality Indicators -->
        <div class="flex justify-center space-x-4 mb-4">
          <div class="flex items-center">
            <i :class="[
              'fa-solid fa-lightbulb mr-1',
              lightingGood ? 'text-green-400' : 'text-red-400'
            ]"></i>
            <span class="text-xs text-slate-300">Iluminação</span>
          </div>
          <div class="flex items-center">
            <i :class="[
              'fa-solid fa-user mr-1',
              faceDetected ? 'text-green-400' : 'text-red-400'
            ]"></i>
            <span class="text-xs text-slate-300">Rosto</span>
          </div>
          <div class="flex items-center">
            <i :class="[
              'fa-solid fa-camera mr-1',
              stablePosition ? 'text-green-400' : 'text-red-400'
            ]"></i>
            <span class="text-xs text-slate-300">Posição</span>
          </div>
        </div>
      </div>

      <!-- Capture Button -->
      <button
        v-if="!isProcessing && !captureSuccess"
        @click="capturePhoto"
        :disabled="!canCapture"
        class="mt-6 bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-slate-600 disabled:cursor-not-allowed text-white px-8 py-3 rounded-full text-lg font-semibold transition-colors"
      >
        <i class="fa-solid fa-camera mr-2"></i>
        Capturar
      </button>

      <!-- Processing Button -->
      <button
        v-if="isProcessing"
        disabled
        class="mt-6 bg-slate-600 text-white px-8 py-3 rounded-full text-lg font-semibold cursor-not-allowed"
      >
        <i class="fa-solid fa-spinner fa-spin mr-2"></i>
        Processando...
      </button>

      <!-- Success Button -->
      <button
        v-if="captureSuccess"
        @click="goToSuccess"
        class="mt-6 bg-green-500 hover:bg-green-600 text-white px-8 py-3 rounded-full text-lg font-semibold transition-colors"
      >
        <i class="fa-solid fa-check mr-2"></i>
        Continuar
      </button>

      <!-- Error Message -->
      <div v-if="errorMessage" class="mt-4 p-3 bg-red-500 bg-opacity-20 border border-red-500 rounded-lg">
        <p class="text-red-400 text-sm text-center">{{ errorMessage }}</p>
      </div>
    </div>

    <!-- Footer -->
    <div class="bg-slate-800 px-4 py-3 text-center">
      <p class="text-xs text-slate-400">
        Suas informações são protegidas e criptografadas
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const userId = route.params.userId;

const videoElement = ref();
const isLoading = ref(true);
const loadingMessage = ref('Iniciando câmera...');
const isProcessing = ref(false);
const captureSuccess = ref(false);
const errorMessage = ref('');

// Quality indicators
const lightingGood = ref(false);
const faceDetected = ref(false);
const stablePosition = ref(false);

const currentInstruction = ref('Posicione seu rosto dentro do círculo');

let stream = null;
let faceDetectionInterval = null;

const canCapture = computed(() => {
  return lightingGood.value && faceDetected.value && stablePosition.value && !isLoading.value;
});

onMounted(async () => {
  await initializeCamera();
  startFaceDetection();
});

onUnmounted(() => {
  cleanup();
});

async function initializeCamera() {
  try {
    loadingMessage.value = 'Acessando câmera...';

    const constraints = {
      video: {
        facingMode: 'user',
        width: { ideal: 640 },
        height: { ideal: 640 },
        frameRate: { ideal: 30 }
      }
    };

    stream = await navigator.mediaDevices.getUserMedia(constraints);

    if (videoElement.value) {
      videoElement.value.srcObject = stream;
      await videoElement.value.play();
    }

    isLoading.value = false;
    currentInstruction.value = 'Mantenha o rosto dentro do círculo e aguarde';

  } catch (error) {
    console.error('Camera error:', error);
    errorMessage.value = 'Não foi possível acessar a câmera. Verifique as permissões.';
    isLoading.value = false;
  }
}

function startFaceDetection() {
  // Simular detecção de rosto (em produção, usar FaceAPI.js ou similar)
  faceDetectionInterval = setInterval(() => {
    if (!isLoading.value && !isProcessing.value) {
      // Simular qualidade da detecção
      lightingGood.value = Math.random() > 0.3;
      faceDetected.value = Math.random() > 0.2;
      stablePosition.value = Math.random() > 0.4;

      if (canCapture.value) {
        currentInstruction.value = 'Perfeito! Clique em Capturar quando estiver pronto';
      } else {
        if (!lightingGood.value) {
          currentInstruction.value = 'Melhore a iluminação do ambiente';
        } else if (!faceDetected.value) {
          currentInstruction.value = 'Posicione seu rosto no centro do círculo';
        } else if (!stablePosition.value) {
          currentInstruction.value = 'Mantenha o rosto estável';
        }
      }
    }
  }, 500);
}

async function capturePhoto() {
  if (!canCapture.value) return;

  isProcessing.value = true;
  currentInstruction.value = 'Processando reconhecimento facial...';

  try {
    // Capturar frame do vídeo
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    canvas.width = videoElement.value.videoWidth;
    canvas.height = videoElement.value.videoHeight;

    context.drawImage(videoElement.value, 0, 0);

    // Converter para blob
    const blob = await new Promise(resolve => {
      canvas.toBlob(resolve, 'image/jpeg', 0.8);
    });

    // Simular processamento de embedding facial
    await new Promise(resolve => setTimeout(resolve, 2000));

    const faceEmbedding = generateMockFaceEmbedding();
    const location = await getCurrentLocation();

    // Enviar para o backend
    await processFaceRecognition(faceEmbedding, location);

    captureSuccess.value = true;
    currentInstruction.value = 'Reconhecimento facial realizado com sucesso!';

  } catch (error) {
    console.error('Capture error:', error);
    errorMessage.value = 'Erro durante o processamento. Tente novamente.';
  } finally {
    isProcessing.value = false;
  }
}

async function processFaceRecognition(faceEmbedding, location) {
  const { $api } = useNuxtApp();

  const response = await $api(`/api/v1/mobile/users/${userId}/face-recognition/process`, {
    method: 'POST',
    body: {
      faceEmbedding,
      location
    }
  });

  if (!response.success) {
    throw new Error(response.error || 'Erro no processamento');
  }
}

function generateMockFaceEmbedding() {
  // Em produção, usar biblioteca de reconhecimento facial real
  const embedding = Array.from({ length: 128 }, () => Math.random() * 2 - 1);
  return JSON.stringify(embedding);
}

async function getCurrentLocation() {
  return new Promise((resolve) => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          resolve(`${latitude},${longitude}`);
        },
        () => resolve(null),
        { timeout: 5000, enableHighAccuracy: false }
      );
    } else {
      resolve(null);
    }
  });
}

function goToSuccess() {
  router.push('/mobile/success');
}

function cleanup() {
  if (stream) {
    stream.getTracks().forEach(track => track.stop());
    stream = null;
  }

  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval);
    faceDetectionInterval = null;
  }
}
</script>