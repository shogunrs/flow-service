// Diretivas personalizadas para validação e máscara
export default defineNuxtPlugin((nuxtApp) => {
  // Diretiva para máscara de CPF
  nuxtApp.vueApp.directive('cpf', {
    mounted(el) {
      function applyCpfMask(value) {
        const numbers = value.replace(/\D/g, '')
        if (numbers.length <= 3) return numbers
        if (numbers.length <= 6) return `${numbers.slice(0, 3)}.${numbers.slice(3)}`
        if (numbers.length <= 9) return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6)}`
        return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6, 9)}-${numbers.slice(9, 11)}`
      }

      function validateCpf(cpf) {
        const numbers = cpf.replace(/\D/g, '')
        if (numbers.length !== 11) return false

        // Verifica se todos os dígitos são iguais
        if (/^(\d)\1{10}$/.test(numbers)) return false

        // Cálculo do primeiro dígito verificador
        let sum = 0
        for (let i = 0; i < 9; i++) {
          sum += parseInt(numbers[i]) * (10 - i)
        }
        let remainder = sum % 11
        const firstDigit = remainder < 2 ? 0 : 11 - remainder

        if (parseInt(numbers[9]) !== firstDigit) return false

        // Cálculo do segundo dígito verificador
        sum = 0
        for (let i = 0; i < 10; i++) {
          sum += parseInt(numbers[i]) * (11 - i)
        }
        remainder = sum % 11
        const secondDigit = remainder < 2 ? 0 : 11 - remainder

        return parseInt(numbers[10]) === secondDigit
      }

      el.addEventListener('input', (e) => {
        const originalValue = e.target.value
        const maskedValue = applyCpfMask(originalValue)

        if (maskedValue !== originalValue) {
          e.target.value = maskedValue
          // Disparar evento de mudança para o v-model
          e.target.dispatchEvent(new Event('input', { bubbles: true }))
        }
      })

      el.addEventListener('blur', (e) => {
        const isValid = validateCpf(e.target.value)

        // Adicionar/remover classes de validação
        if (e.target.value && !isValid) {
          e.target.classList.add('border-red-500', 'focus:border-red-500')
          e.target.classList.remove('border-slate-700', 'focus:border-indigo-500')
        } else {
          e.target.classList.remove('border-red-500', 'focus:border-red-500')
          e.target.classList.add('border-slate-700', 'focus:border-indigo-500')
        }
      })
    }
  })

  // Diretiva para máscara de CNPJ
  nuxtApp.vueApp.directive('cnpj', {
    mounted(el) {
      function applyCnpjMask(value) {
        const numbers = value.replace(/\D/g, '')
        if (numbers.length <= 2) return numbers
        if (numbers.length <= 5) return `${numbers.slice(0, 2)}.${numbers.slice(2)}`
        if (numbers.length <= 8) return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5)}`
        if (numbers.length <= 12) return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8)}`
        return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8, 12)}-${numbers.slice(12, 14)}`
      }

      function validateCnpj(cnpj) {
        const numbers = cnpj.replace(/\D/g, '')
        if (numbers.length !== 14) return false

        // Verifica se todos os dígitos são iguais
        if (/^(\d)\1{13}$/.test(numbers)) return false

        // Cálculo do primeiro dígito verificador
        let sum = 0
        let weight = 5
        for (let i = 0; i < 12; i++) {
          sum += parseInt(numbers[i]) * weight
          weight = weight === 2 ? 9 : weight - 1
        }
        let remainder = sum % 11
        const firstDigit = remainder < 2 ? 0 : 11 - remainder

        if (parseInt(numbers[12]) !== firstDigit) return false

        // Cálculo do segundo dígito verificador
        sum = 0
        weight = 6
        for (let i = 0; i < 13; i++) {
          sum += parseInt(numbers[i]) * weight
          weight = weight === 2 ? 9 : weight - 1
        }
        remainder = sum % 11
        const secondDigit = remainder < 2 ? 0 : 11 - remainder

        return parseInt(numbers[13]) === secondDigit
      }

      el.addEventListener('input', (e) => {
        const originalValue = e.target.value
        const maskedValue = applyCnpjMask(originalValue)

        if (maskedValue !== originalValue) {
          e.target.value = maskedValue
          e.target.dispatchEvent(new Event('input', { bubbles: true }))
        }
      })

      el.addEventListener('blur', (e) => {
        const isValid = validateCnpj(e.target.value)

        if (e.target.value && !isValid) {
          e.target.classList.add('border-red-500', 'focus:border-red-500')
          e.target.classList.remove('border-slate-700', 'focus:border-indigo-500')
        } else {
          e.target.classList.remove('border-red-500', 'focus:border-red-500')
          e.target.classList.add('border-slate-700', 'focus:border-indigo-500')
        }
      })
    }
  })

  // Diretiva para máscara de telefone
  nuxtApp.vueApp.directive('telefone', {
    mounted(el) {
      function applyTelefoneMask(value) {
        const numbers = value.replace(/\D/g, '')
        if (numbers.length <= 2) return `(${numbers}`
        if (numbers.length <= 6) return `(${numbers.slice(0, 2)}) ${numbers.slice(2)}`
        if (numbers.length <= 10) return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 6)}-${numbers.slice(6)}`
        return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 7)}-${numbers.slice(7, 11)}`
      }

      function validateTelefone(telefone) {
        const numbers = telefone.replace(/\D/g, '')
        return numbers.length === 10 || numbers.length === 11
      }

      el.addEventListener('input', (e) => {
        const originalValue = e.target.value
        const maskedValue = applyTelefoneMask(originalValue)

        if (maskedValue !== originalValue) {
          e.target.value = maskedValue
          e.target.dispatchEvent(new Event('input', { bubbles: true }))
        }
      })

      el.addEventListener('blur', (e) => {
        const isValid = validateTelefone(e.target.value)

        if (e.target.value && !isValid) {
          e.target.classList.add('border-red-500', 'focus:border-red-500')
          e.target.classList.remove('border-slate-700', 'focus:border-indigo-500')
        } else {
          e.target.classList.remove('border-red-500', 'focus:border-red-500')
          e.target.classList.add('border-slate-700', 'focus:border-indigo-500')
        }
      })
    }
  })
})