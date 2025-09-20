<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex flex-col">
    <!-- Modern Header -->
    <div class="relative bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 px-4 py-4 shadow-lg">
      <div class="absolute inset-0 bg-gradient-to-r from-green-600/10 to-emerald-600/10"></div>
      <div class="relative flex items-center justify-center gap-3">
        <div class="p-2 bg-gradient-to-br from-green-600 to-emerald-600 rounded-xl shadow-lg">
          <i class="fa-solid fa-check text-white text-sm"></i>
        </div>
        <h1 class="text-xl font-bold text-white">
          Processo Concluído
        </h1>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col items-center justify-center p-6">
      <!-- Success Icon -->
      <div class="w-24 h-24 bg-green-500 rounded-full flex items-center justify-center mb-6">
        <i class="fa-solid fa-check text-white text-4xl"></i>
      </div>

      <!-- Success Message -->
      <h2 class="text-2xl font-bold text-white text-center mb-4">
        Sucesso!
      </h2>

      <p class="text-green-100 text-center text-lg mb-8 max-w-sm">
        Todos os seus documentos foram enviados e processados com sucesso.
      </p>

      <!-- Completed Items -->
      <div class="w-full max-w-sm space-y-3 mb-8">
        <div class="flex items-center text-green-100">
          <i class="fa-solid fa-check-circle text-green-400 mr-3"></i>
          <span>Documentos enviados</span>
        </div>
        <div class="flex items-center text-green-100">
          <i class="fa-solid fa-check-circle text-green-400 mr-3"></i>
          <span>Reconhecimento facial</span>
        </div>
        <div class="flex items-center text-green-100">
          <i class="fa-solid fa-check-circle text-green-400 mr-3"></i>
          <span>Verificação de localização</span>
        </div>
        <div class="flex items-center text-green-100">
          <i class="fa-solid fa-check-circle text-green-400 mr-3"></i>
          <span>Dados salvos com segurança</span>
        </div>
      </div>

      <!-- Next Steps -->
      <div class="w-full max-w-sm bg-green-700 bg-opacity-50 rounded-lg p-4 mb-6">
        <h3 class="text-white font-semibold mb-2">Próximos passos:</h3>
        <ul class="text-green-100 text-sm space-y-1">
          <li>• Seus documentos serão analisados</li>
          <li>• Você receberá um email de confirmação</li>
          <li>• O processo de verificação pode levar até 24h</li>
        </ul>
      </div>

      <!-- Action Buttons -->
      <div class="w-full max-w-sm space-y-3">
        <button
          @click="goToMainApp"
          class="w-full bg-white text-green-800 font-semibold py-3 px-6 rounded-lg hover:bg-green-50 transition-colors"
        >
          <i class="fa-solid fa-home mr-2"></i>
          Ir para o App
        </button>

        <button
          @click="shareSuccess"
          class="w-full bg-green-600 text-white font-semibold py-3 px-6 rounded-lg hover:bg-green-700 transition-colors"
        >
          <i class="fa-solid fa-share mr-2"></i>
          Compartilhar
        </button>
      </div>
    </div>

    <!-- Footer -->
    <div class="bg-green-800 px-4 py-4 text-center">
      <p class="text-xs text-green-200 mb-2">
        Obrigado por confiar em nossos serviços!
      </p>
      <p class="text-xs text-green-300">
        Em caso de dúvidas, entre em contato com nosso suporte
      </p>
    </div>

    <!-- Confetti Animation -->
    <div v-if="showConfetti" class="fixed inset-0 pointer-events-none z-50">
      <div
        v-for="i in 50"
        :key="i"
        class="absolute animate-bounce"
        :style="getConfettiStyle(i)"
      >
        🎉
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const showConfetti = ref(false);

onMounted(() => {
  // Mostrar confetti por alguns segundos
  showConfetti.value = true;
  setTimeout(() => {
    showConfetti.value = false;
  }, 3000);
});

function goToMainApp() {
  // Redirecionar para a aplicação principal
  window.location.href = '/admin';
}

function shareSuccess() {
  if (navigator.share) {
    navigator.share({
      title: 'Processo de Verificação Concluído',
      text: 'Acabei de completar meu processo de verificação de documentos!',
      url: window.location.origin
    }).catch(() => {
      // Fallback se share não funcionar
      copyToClipboard();
    });
  } else {
    copyToClipboard();
  }
}

function copyToClipboard() {
  const text = `Acabei de completar meu processo de verificação de documentos! ${window.location.origin}`;

  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => {
      alert('Link copiado para a área de transferência!');
    });
  } else {
    // Fallback para dispositivos mais antigos
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    alert('Link copiado para a área de transferência!');
  }
}

function getConfettiStyle(index) {
  const randomX = Math.random() * 100;
  const randomDelay = Math.random() * 2;
  const randomDuration = 1 + Math.random() * 2;

  return {
    left: `${randomX}%`,
    animationDelay: `${randomDelay}s`,
    animationDuration: `${randomDuration}s`,
    fontSize: '1.2rem'
  };
}

// Definir título da página
useHead({
  title: 'Processo Concluído - Mobile'
});
</script>

<style scoped>
@keyframes bounce {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: translateY(-100px) rotate(180deg);
    opacity: 0.7;
  }
}

.animate-bounce {
  animation: bounce 3s ease-in-out infinite;
}
</style>