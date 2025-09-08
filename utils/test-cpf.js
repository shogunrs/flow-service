// Script de teste para as funcionalidades do CPF
console.log('🧪 Testando validação de CPF...\n');

// Função de validação matemática do CPF (igual ao componente)
function validateCpfMath(cpf) {
  const numbers = cpf.replace(/\D/g, '')
  if (numbers.length !== 11) return false

  // Verifica se todos os dígitos são iguais (caso comum de fraude)
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

// Função de validação antifraude (igual ao componente)
function validateAntiFraude(cpf) {
  const numbers = cpf.replace(/\D/g, '')
  if (numbers.length !== 11) return false

  // Lista de CPFs conhecidos como inválidos/teste
  const blacklist = [
    '00000000000', '11111111111', '22222222222', '33333333333',
    '44444444444', '55555555555', '66666666666', '77777777777',
    '88888888888', '99999999999'
  ]
  
  if (blacklist.includes(numbers)) return false

  // Padrões sequenciais
  if (/^(\d{3})\1{2}\d{2}$/.test(numbers)) return false // 123123123XX
  if (/^123456789\d{2}$/.test(numbers)) return false // 123456789XX
  if (/^987654321\d{2}$/.test(numbers)) return false // 987654321XX

  // Verifica se não é uma sequência numérica crescente ou decrescente
  let isSequential = true
  for (let i = 1; i < 11; i++) {
    if (parseInt(numbers[i]) !== parseInt(numbers[i-1]) + 1) {
      isSequential = false
      break
    }
  }
  if (isSequential) return false

  isSequential = true
  for (let i = 1; i < 11; i++) {
    if (parseInt(numbers[i]) !== parseInt(numbers[i-1]) - 1) {
      isSequential = false
      break
    }
  }
  if (isSequential) return false

  return true
}

// Função para aplicar máscara do CPF
function applyCpfMask(value) {
  const numbers = value.replace(/\D/g, '')
  if (numbers.length <= 3) return numbers
  if (numbers.length <= 6) return `${numbers.slice(0, 3)}.${numbers.slice(3)}`
  if (numbers.length <= 9) return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6)}`
  return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6, 9)}-${numbers.slice(9, 11)}`
}

// Casos de teste
const testCases = [
  // CPFs válidos
  { cpf: '11144477735', expected: { math: true, fraud: true }, desc: 'CPF válido comum' },
  { cpf: '12345678909', expected: { math: true, fraud: true }, desc: 'CPF válido gerado' },
  
  // CPFs com formato diferente
  { cpf: '111.444.777-35', expected: { math: true, fraud: true }, desc: 'CPF válido com máscara' },
  
  // CPFs inválidos matematicamente
  { cpf: '11111111111', expected: { math: false, fraud: false }, desc: 'CPF com todos dígitos iguais' },
  { cpf: '12345678901', expected: { math: false, fraud: true }, desc: 'CPF com dígito verificador inválido' },
  
  // CPFs bloqueados por antifraude
  { cpf: '00000000000', expected: { math: false, fraud: false }, desc: 'CPF na blacklist (zeros)' },
  { cpf: '12345678912', expected: { math: true, fraud: false }, desc: 'CPF sequencial 123456789XX' },
  { cpf: '98765432112', expected: { math: true, fraud: false }, desc: 'CPF sequencial 987654321XX' },
  
  // Casos extremos
  { cpf: '123', expected: { math: false, fraud: false }, desc: 'CPF incompleto' },
  { cpf: '1234567890123', expected: { math: false, fraud: false }, desc: 'CPF muito longo' },
  { cpf: 'abc123def45', expected: { math: false, fraud: false }, desc: 'CPF com letras' },
]

console.log('📋 Executando casos de teste:\n');

let passed = 0;
let failed = 0;

testCases.forEach((test, index) => {
  const { cpf, expected, desc } = test;
  
  const mathResult = validateCpfMath(cpf);
  const fraudResult = validateAntiFraude(cpf);
  const maskedCpf = applyCpfMask(cpf);
  
  const mathPass = mathResult === expected.math;
  const fraudPass = fraudResult === expected.fraud;
  const testPass = mathPass && fraudPass;
  
  if (testPass) {
    console.log(`✅ Teste ${index + 1}: ${desc}`);
    console.log(`   CPF: ${cpf} → ${maskedCpf}`);
    console.log(`   Matemática: ${mathResult ? '✓' : '✗'} | Antifraude: ${fraudResult ? '✓' : '✗'}\n`);
    passed++;
  } else {
    console.log(`❌ Teste ${index + 1}: ${desc}`);
    console.log(`   CPF: ${cpf} → ${maskedCpf}`);
    console.log(`   Matemática: ${mathResult ? '✓' : '✗'} (esperado: ${expected.math ? '✓' : '✗'})`);
    console.log(`   Antifraude: ${fraudResult ? '✓' : '✗'} (esperado: ${expected.fraud ? '✓' : '✗'})\n`);
    failed++;
  }
});

console.log(`📊 Resultados dos testes:`);
console.log(`   ✅ Passou: ${passed}`);
console.log(`   ❌ Falhou: ${failed}`);
console.log(`   📈 Taxa de sucesso: ${((passed / testCases.length) * 100).toFixed(1)}%\n`);

// Teste da máscara
console.log('🎭 Testando aplicação da máscara:\n');

const maskTests = [
  '123',
  '123456',
  '123456789',
  '12345678901',
  '111444777359999' // deve ser truncado
];

maskTests.forEach(input => {
  const masked = applyCpfMask(input);
  console.log(`   ${input.padEnd(15)} → ${masked}`);
});

console.log('\n🎯 Teste concluído!');

// Demonstração de funcionalidades específicas
console.log('\n🔍 Demonstrações específicas:');
console.log('\n1. Detecção de padrões sequenciais:');
['12345678901', '98765432109', '12312312312'].forEach(cpf => {
  const fraud = validateAntiFraude(cpf);
  console.log(`   ${cpf}: ${fraud ? 'Aceito' : 'Bloqueado por padrão sequencial'}`);
});

console.log('\n2. CPFs válidos reais:');
['11144477735', '12345678909'].forEach(cpf => {
  const math = validateCpfMath(cpf);
  const fraud = validateAntiFraude(cpf);
  const status = math && fraud ? '✅ Válido' : '❌ Inválido';
  console.log(`   ${applyCpfMask(cpf)}: ${status}`);
});

console.log('\n3. CPFs bloqueados pela blacklist:');
['11111111111', '00000000000', '99999999999'].forEach(cpf => {
  const fraud = validateAntiFraude(cpf);
  console.log(`   ${applyCpfMask(cpf)}: ${fraud ? 'Aceito' : '🚫 Bloqueado pela blacklist'}`);
});