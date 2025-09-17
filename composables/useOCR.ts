import Tesseract from 'tesseract.js'

export interface OCRResult {
  text: string
  confidence: number
  documentType?: 'CPF' | 'RG' | 'CNH' | 'CNPJ' | 'UNKNOWN'
  extractedData?: {
    // Dados pessoais comuns
    nome?: string
    sobrenome?: string
    nomeCompleto?: string
    cpf?: string
    rg?: string
    dataEmissao?: string
    orgaoExpedidor?: string

    // Dados específicos CNH
    numeroCnh?: string
    validadeCnh?: string
    dataNascimento?: string
    localNascimento?: string
    nacionalidade?: string
    filiacao?: {
      pai?: string
      mae?: string
    }

    // Dados específicos RG
    numeroIdentidade?: string
    orgaoEmissor?: string

    // Dados CNPJ
    cnpj?: string
    razaoSocial?: string
  }
}

export interface DocumentValidationResult {
  isValid: boolean
  errors: string[]
  warnings: string[]
  extractedPhoto?: string // Base64 da foto extraída
  faceEmbedding?: string // Embedding facial para comparação
  confidenceScore: number
}

export function useOCR() {
  const isProcessing = ref(false)
  const progress = ref(0)

  const extractText = async (imageFile: File | string, language = 'por'): Promise<OCRResult> => {
    isProcessing.value = true
    progress.value = 0

    try {
      const { data: { text, confidence } } = await Tesseract.recognize(
        imageFile,
        language,
        {
          logger: (m) => {
            if (m.status === 'recognizing text') {
              progress.value = Math.round(m.progress * 100)
            }
          }
        }
      )

      const cleanText = text.trim()
      const documentType = detectDocumentType(cleanText)
      const extractedData = parseDocumentByType(cleanText, documentType)

      console.log('🔍 OCR Analysis:', {
        documentType,
        confidence,
        extractedData,
        rawText: cleanText.substring(0, 200) + '...'
      })

      return {
        text: cleanText,
        confidence,
        documentType,
        extractedData
      }
    } catch (error) {
      console.error('OCR Error:', error)
      return { text: '', confidence: 0, documentType: 'UNKNOWN' }
    } finally {
      isProcessing.value = false
      progress.value = 0
    }
  }

  // Detecta automaticamente o tipo de documento baseado no conteúdo
  const detectDocumentType = (text: string): 'CPF' | 'RG' | 'CNH' | 'CNPJ' | 'UNKNOWN' => {
    const lowerText = text.toLowerCase()

    // CNH - Carteira Nacional de Habilitação
    if (lowerText.includes('carteira nacional') ||
        lowerText.includes('habilitacao') ||
        lowerText.includes('habilitação') ||
        lowerText.includes('cnh') ||
        lowerText.includes('condutor') ||
        lowerText.includes('categoria') ||
        lowerText.includes('validade')) {
      console.log('🚗 Documento detectado: CNH')
      return 'CNH'
    }

    // RG - Registro Geral / Carteira de Identidade
    if (lowerText.includes('carteira de identidade') ||
        lowerText.includes('identidade civil') ||
        lowerText.includes('registro geral') ||
        lowerText.includes('documento de identidade') ||
        (lowerText.includes('rg') && !lowerText.includes('orgao'))) {
      console.log('🆔 Documento detectado: RG')
      return 'RG'
    }

    // CPF - Cadastro de Pessoa Física
    if (lowerText.includes('receita federal') ||
        lowerText.includes('cadastro de pessoa') ||
        lowerText.includes('pessoa fisica') ||
        (lowerText.includes('cpf') && lowerText.includes('brasil'))) {
      console.log('📄 Documento detectado: CPF')
      return 'CPF'
    }

    // CNPJ - Cadastro Nacional da Pessoa Jurídica
    if (lowerText.includes('pessoa juridica') ||
        lowerText.includes('cnpj') ||
        lowerText.includes('razao social') ||
        lowerText.includes('empresarial')) {
      console.log('🏢 Documento detectado: CNPJ')
      return 'CNPJ'
    }

    console.log('❓ Tipo de documento não identificado')
    return 'UNKNOWN'
  }

  // Parse de documento baseado no tipo detectado
  const parseDocumentByType = (text: string, documentType: string) => {
    switch (documentType) {
      case 'CNH':
        return parseCNHDocument(text)
      case 'RG':
        return parseRGDocument(text)
      case 'CPF':
        return parseCPFDocument(text)
      case 'CNPJ':
        return parseCNPJDocument(text)
      default:
        return parseGenericDocument(text)
    }
  }

  // Extração específica para CNH
  const extractCNHSpecificData = (normalizedText: string, rawText: string, data: any) => {
    console.log('🚗 Extraindo dados específicos da CNH...')

    // Registro CNH (11 dígitos)
    const registroMatch = normalizedText.match(/REGISTRO\s*(\d{11})/) || normalizedText.match(/(\d{11})/)
    if (registroMatch) {
      data.numeroCnh = registroMatch[1]
      console.log('🚗 Registro CNH:', data.numeroCnh)
    }

    // Todas as datas (nascimento, emissão, validade)
    const allDates = normalizedText.match(/\d{2}\/\d{2}\/\d{4}/g) || []
    if (allDates.length >= 2) {
      // CNH geralmente tem: nascimento, emissão, validade
      data.dataNascimento = allDates[0]
      if (allDates.length >= 3) {
        data.dataEmissao = allDates[1]
        data.validadeCnh = allDates[2]
      } else {
        data.validadeCnh = allDates[1] // Se só tem 2 datas, segunda é validade
      }
      console.log('📅 Datas CNH:', { nascimento: data.dataNascimento, emissao: data.dataEmissao, validade: data.validadeCnh })
    }

    // RG/Identidade
    const rgMatch = normalizedText.match(/RG[:\s]*(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/) ||
                   normalizedText.match(/IDENTIDADE[:\s]*(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/)
    if (rgMatch) {
      data.rg = rgMatch[1]
      data.numeroIdentidade = rgMatch[1]
      console.log('🆔 RG/Identidade:', data.rg)
    }

    // Órgão expedidor
    const orgaoMatch = normalizedText.match(/(SSP|DETRAN|SESP|PC|IIRGD)[\s\/\-]*([A-Z]{2})/)
    if (orgaoMatch) {
      data.orgaoExpedidor = `${orgaoMatch[1]}/${orgaoMatch[2]}`
      data.orgaoEmissor = data.orgaoExpedidor
      console.log('🏛️ Órgão expedidor:', data.orgaoExpedidor)
    }

    // Nacionalidade
    if (normalizedText.includes('BRASIL')) {
      data.nacionalidade = 'BRASILEIRA'
      console.log('🇧🇷 Nacionalidade:', data.nacionalidade)
    }

    // Filiação - busca por pai e mãe
    const filiacao: any = {}
    const paiMatch = normalizedText.match(/PAI[:\s]*([A-Z\s]+)/) || normalizedText.match(/FILIACAO\s*P[:\s]*([A-Z\s]+)/)
    if (paiMatch) {
      filiacao.pai = paiMatch[1].trim()
      console.log('👨 Pai:', filiacao.pai)
    }

    const maeMatch = normalizedText.match(/MAE[:\s]*([A-Z\s]+)/) || normalizedText.match(/FILIACAO\s*M[:\s]*([A-Z\s]+)/)
    if (maeMatch) {
      filiacao.mae = maeMatch[1].trim()
      console.log('👩 Mãe:', filiacao.mae)
    }

    if (filiacao.pai || filiacao.mae) {
      data.filiacao = filiacao
    }
  }

  // Extração específica para RG
  const extractRGSpecificData = (normalizedText: string, rawText: string, data: any) => {
    console.log('🆔 Extraindo dados específicos do RG...')

    // Número do RG
    const rgMatch = normalizedText.match(/RG[:\s]*([\d\.]+)/) || normalizedText.match(/(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/)
    if (rgMatch) {
      data.rg = rgMatch[1]
      data.numeroIdentidade = rgMatch[1]
      console.log('🆔 Número RG:', data.rg)
    }

    // Órgão expedidor
    const orgaoMatch = normalizedText.match(/ORGAO EXPEDIDOR[:\s]*([A-Z]{2,5}\/[A-Z]{2})/) ||
                      normalizedText.match(/(SSP|DETRAN|SESP|PC|IIRGD)[\s\/\-]*([A-Z]{2})/)
    if (orgaoMatch) {
      data.orgaoExpedidor = orgaoMatch[1] || `${orgaoMatch[1]}/${orgaoMatch[2]}`
      data.orgaoEmissor = data.orgaoExpedidor
      console.log('🏛️ Órgão expedidor:', data.orgaoExpedidor)
    }

    // Filiação
    const filiacao: any = {}
    const paiMatch = normalizedText.match(/FILIACAO\s*P[AI]*[:\s]*([A-Z\s]+)/)
    if (paiMatch) {
      filiacao.pai = paiMatch[1].trim()
      console.log('👨 Pai:', filiacao.pai)
    }

    const maeMatch = normalizedText.match(/FILIACAO\s*M[AE]*[:\s]*([A-Z\s]+)/)
    if (maeMatch) {
      filiacao.mae = maeMatch[1].trim()
      console.log('👩 Mãe:', filiacao.mae)
    }

    if (filiacao.pai || filiacao.mae) {
      data.filiacao = filiacao
    }
  }

  // Extração específica para CNPJ
  const extractCNPJSpecificData = (normalizedText: string, rawText: string, data: any) => {
    console.log('🏢 Extraindo dados específicos do CNPJ...')

    // CNPJ
    const cnpjMatch = normalizedText.match(/\d{2}\.?\d{3}\.?\d{3}\/?\d{4}[-\.]?\d{2}/)
    if (cnpjMatch) {
      data.cnpj = cnpjMatch[0].replace(/[^\d]/g, '').replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
      console.log('🏢 CNPJ:', data.cnpj)
    }

    // Razão social
    const razaoMatch = normalizedText.match(/RAZAO SOCIAL[:\s]*([A-Z\s]+)/)
    if (razaoMatch) {
      data.razaoSocial = razaoMatch[1].trim()
      console.log('🏢 Razão Social:', data.razaoSocial)
    }
  }

  // Parse específico para CNH brasileira seguindo estrutura oficial
  const parseCNHDocument = (text: string) => {
    console.log('🚗 Processando CNH brasileira:', text.substring(0, 100) + '...')
    const cleanText = text.replace(/\n+/g, ' ').replace(/\s+/g, ' ')
    const lines = text.split('\n').map(line => line.trim()).filter(line => line.length > 0)
    const extractedData: any = {}

    console.log('📋 Linhas detectadas na CNH:', lines.slice(0, 15)) // Primeiras 15 linhas para debug

    // ============ CAMPOS DA CNH BRASILEIRA ============

    // CAMPO 1 e 2: NOME E SOBRENOME (estratégia melhorada)
    // Estratégia 1: Procura por "NOME" + próxima linha (mais robusta)
    console.log('🔍 Estratégia simplificada: buscando linha "NOME" + próxima linha...')

    // Debug: mostra todas as linhas para entender o layout
    console.log('📋 TODAS as linhas para debug:')
    lines.forEach((line, index) => {
      console.log(`Linha ${index + 1}: "${line}"`)
    })

    for (let i = 0; i < lines.length - 1; i++) {
      const currentLine = lines[i]
      const nextLine = lines[i + 1]

      if (!currentLine || !nextLine) continue

      // Verifica se a linha atual contém "NOME" (case insensitive)
      // Aceita "NOME", "NOME E SOBRENOME", etc.
      if (currentLine.toLowerCase().includes('nome')) {

        console.log(`✅ Encontrou "NOME" na linha ${i + 1}: "${currentLine}"`)
        console.log(`📝 Próxima linha: "${nextLine}"`)

        // Limpa a próxima linha removendo caracteres especiais mas mantendo acentos
        let fullName = nextLine.trim()

        // Remove caracteres especiais como |, [, ], números e outros símbolos
        // Mas mantém letras (com acentos) e espaços
        fullName = fullName.replace(/[|\[\]0-9\/\-\(\)\.]+/g, ' ')
        fullName = fullName.replace(/[^A-ZÁÀÂÃÉÊÍÔÕÚÇa-záàâãéêíôõúç\s]/g, ' ')

        // Remove espaços múltiplos
        fullName = fullName.replace(/\s+/g, ' ').trim()

        console.log(`🧹 Nome após limpeza: "${fullName}"`)

        // Validação mais flexível: apenas verifica se não está vazio e tem pelo menos 3 caracteres
        if (fullName.length >= 3 && fullName.split(' ').length >= 1) {

          extractedData.nomeCompleto = fullName
          // Usa o nome completo no campo "nome" sem separar
          extractedData.nome = fullName
          extractedData.sobrenome = '' // Deixa vazio

          console.log('👤 Nome completo extraído (estratégia simples):', fullName)
          console.log('👤 Nome (completo):', extractedData.nome)
          console.log('👤 Sobrenome:', extractedData.sobrenome)
          break
        } else {
          console.log(`❌ Linha seguinte não parece ser um nome válido: "${fullName}"`)
        }
      }
    }

    // Estratégia 2: Busca por múltiplas linhas consecutivas (caso o nome esteja quebrado)
    if (!extractedData.nomeCompleto) {
      console.log('🔄 Estratégia 2: buscando nome em múltiplas linhas consecutivas...')

      for (let i = 0; i < lines.length - 2; i++) {
        const currentLine = lines[i]
        const nextLine = lines[i + 1]
        const thirdLine = lines[i + 2]

        if (!currentLine || !nextLine) continue

        // Verifica se a linha atual contém "NOME"
        // Aceita "NOME", "NOME E SOBRENOME", etc.
        if (currentLine.toLowerCase().includes('nome')) {

          console.log(`✅ Encontrou "NOME" na linha ${i + 1}: "${currentLine}"`)

          // Tenta combinar as próximas 2 linhas
          let fullName = ''

          // Adiciona a segunda linha
          if (nextLine && nextLine.trim().length > 0) {
            let cleanedNext = nextLine.trim()
            // Aplica a mesma limpeza robusta
            cleanedNext = cleanedNext.replace(/[|\[\]0-9\/\-\(\)\.]+/g, ' ')
            cleanedNext = cleanedNext.replace(/[^A-ZÁÀÂÃÉÊÍÔÕÚÇa-záàâãéêíôõúç\s]/g, ' ')
            cleanedNext = cleanedNext.replace(/\s+/g, ' ').trim()
            if (cleanedNext.length > 0) {
              fullName += cleanedNext + ' '
            }
          }

          // Adiciona a terceira linha se parecer com nome
          if (thirdLine && thirdLine.trim().length > 0) {
            let cleanedThird = thirdLine.trim()
            // Aplica a mesma limpeza robusta
            cleanedThird = cleanedThird.replace(/[|\[\]0-9\/\-\(\)\.]+/g, ' ')
            cleanedThird = cleanedThird.replace(/[^A-ZÁÀÂÃÉÊÍÔÕÚÇa-záàâãéêíôõúç\s]/g, ' ')
            cleanedThird = cleanedThird.replace(/\s+/g, ' ').trim()

            // Só adiciona se não contém números ou parece ser continuação do nome
            if (cleanedThird.length > 0 && !cleanedThird.match(/\d/) && cleanedThird.length < 50) {
              fullName += cleanedThird + ' '
            }
          }

          fullName = fullName.trim()

          console.log(`🧹 Nome combinado após limpeza: "${fullName}"`)

          if (fullName.length >= 3) {
            extractedData.nomeCompleto = fullName
            // Usa o nome completo no campo "nome" sem separar
            extractedData.nome = fullName
            extractedData.sobrenome = '' // Deixa vazio

            console.log('👤 Nome completo extraído (estratégia múltiplas linhas):', fullName)
            console.log('👤 Nome (completo):', extractedData.nome)
            console.log('👤 Sobrenome:', extractedData.sobrenome)
            break
          }
        }
      }
    }

    // Estratégia 3: Fallback - padrões na mesma linha
    if (!extractedData.nomeCompleto) {
      console.log('🔄 Estratégia 3: buscando nome na mesma linha...')

      const nomeInlinePatterns = [
        /NOME[:\s]+([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,80})/i,
        /PORTADOR[:\s]+([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,80})/i,
        /CONDUTOR[:\s]+([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,80})/i
      ]

      for (const pattern of nomeInlinePatterns) {
        const nameMatch = cleanText.match(pattern)
        if (nameMatch) {
          const fullName = nameMatch[1]?.trim()
          if (fullName && fullName.split(' ').length >= 2) {
            extractedData.nomeCompleto = fullName
            // Usa o nome completo no campo "nome" sem separar
            extractedData.nome = fullName
            extractedData.sobrenome = '' // Deixa vazio
            console.log('👤 Nome completo extraído (fallback inline):', fullName)
            console.log('👤 Nome (completo):', extractedData.nome)
            console.log('👤 Sobrenome:', extractedData.sobrenome)
            break
          }
        }
      }
    }

    // CAMPO 3: DATA, LOCAL E UF DE NASCIMENTO
    const nascimentoPatterns = [
      // Data nascimento (DD/MM/AAAA)
      /(\d{2}\/\d{2}\/\d{4})/g,
      /(?:NASCIMENTO[:\s]+)?(\d{2}\/\d{2}\/\d{4})/i,
      /(?:NASC[:\s]+)?(\d{2}\/\d{2}\/\d{4})/i
    ]

    for (const pattern of nascimentoPatterns) {
      const birthMatch = cleanText.match(pattern)
      if (birthMatch) {
        extractedData.dataNascimento = birthMatch[1] || birthMatch[0]
        console.log('🎂 Data nascimento:', extractedData.dataNascimento)
        break
      }
    }

    // Local de nascimento (cidade e UF)
    const localPatterns = [
      /(?:NASCIMENTO[:\s]+).*?([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s\-]{3,40}[-\/\s]?[A-Z]{2})/i,
      /(?:LOCAL[:\s]+NASC[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s\-]{3,40}[-\/\s]?[A-Z]{2})/i,
      /(?:NATURALIDADE[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s\-]{3,40}[-\/\s]?[A-Z]{2})/i,
      // Busca por padrão: CIDADE-UF ou CIDADE/UF
      /([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,30}[-\/\s][A-Z]{2})\b/
    ]

    for (const pattern of localPatterns) {
      const localMatch = cleanText.match(pattern)
      if (localMatch) {
        extractedData.localNascimento = localMatch[1]?.trim()
        console.log('🌍 Local nascimento:', extractedData.localNascimento)
        break
      }
    }

    // CAMPO 4A: DATA DE EMISSÃO
    // CAMPO 4B: VALIDADE
    // Melhoria na identificação de datas para evitar confusão

    const allDates = cleanText.match(/\d{2}\/\d{2}\/\d{4}/g) || []
    console.log('📅 Todas as datas encontradas:', allDates)

    // Tenta identificar datas pelo contexto
    const dataEmissaoPatterns = [
      /(?:EMISSÃO[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:EMITIDO[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:DATA[:\s]+EMISSÃO[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:4A[:\s]+)(\d{2}\/\d{2}\/\d{4})/i
    ]

    const validadePatterns = [
      /(?:VALIDADE[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:VÁLIDA[:\s]+ATÉ[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:VENCIMENTO[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:VENC[:\s]+)(\d{2}\/\d{2}\/\d{4})/i,
      /(?:4B[:\s]+)(\d{2}\/\d{2}\/\d{4})/i
    ]

    // Busca data de emissão por contexto
    for (const pattern of dataEmissaoPatterns) {
      const emissaoMatch = cleanText.match(pattern)
      if (emissaoMatch) {
        extractedData.dataEmissao = emissaoMatch[1]
        console.log('📅 Data emissão (contexto):', extractedData.dataEmissao)
        break
      }
    }

    // Busca validade por contexto
    for (const pattern of validadePatterns) {
      const validadeMatch = cleanText.match(pattern)
      if (validadeMatch) {
        extractedData.validadeCnh = validadeMatch[1]
        console.log('📅 Validade CNH (contexto):', extractedData.validadeCnh)
        break
      }
    }

    // Se não conseguiu identificar por contexto, usa lógica posicional melhorada
    if ((!extractedData.dataEmissao || !extractedData.validadeCnh) && allDates.length >= 2) {
      console.log('🔄 Usando lógica posicional para datas...')

      // Remove a data de nascimento das possibilidades (se já foi identificada)
      let availableDates = allDates
      if (extractedData.dataNascimento) {
        availableDates = allDates.filter(date => date !== extractedData.dataNascimento)
      }

      if (availableDates.length >= 2) {
        // Lógica: em CNH, geralmente emissão vem antes da validade
        // E validade é sempre posterior à emissão
        const sortedDates = availableDates.sort((a, b) => {
          const [dayA, monthA, yearA] = a.split('/').map(Number)
          const [dayB, monthB, yearB] = b.split('/').map(Number)
          const dateA = new Date(yearA, monthA - 1, dayA)
          const dateB = new Date(yearB, monthB - 1, dayB)
          return dateA.getTime() - dateB.getTime()
        })

        if (!extractedData.dataEmissao) {
          extractedData.dataEmissao = sortedDates[0]
          console.log('📅 Data emissão (posicional):', extractedData.dataEmissao)
        }

        if (!extractedData.validadeCnh && sortedDates.length > 1) {
          extractedData.validadeCnh = sortedDates[sortedDates.length - 1] // Última data (mais recente)
          console.log('📅 Validade CNH (posicional):', extractedData.validadeCnh)
        }
      }
    }

    // CAMPO 4C: DOC IDENTIDADE / ÓRGÃO EMISSOR / UF
    // Melhorada para evitar confusão com CNH e outras datas

    const rgExplicitoPatterns = [
      /(?:RG[:\s]+)(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/i,
      /(?:IDENTIDADE[:\s]+)(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/i,
      /(?:DOC[:\s]+IDENTIDADE[:\s]+)(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/i,
      /(?:4C[:\s]+)(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})/i
    ]

    // Primeiro tenta padrões explícitos
    for (const pattern of rgExplicitoPatterns) {
      const rgMatch = cleanText.match(pattern)
      if (rgMatch) {
        extractedData.rg = rgMatch[1]
        extractedData.numeroIdentidade = rgMatch[1]
        console.log('🆔 RG/Identidade (explícito):', extractedData.rg)
        break
      }
    }

    // Se não encontrou explicitamente, busca por padrão de RG mas com validações
    if (!extractedData.rg) {
      const rgGenericoPatterns = [
        /\b(\d{1,2}\.?\d{3}\.?\d{3}[-\.]?\w{1,2})\b/,
        /\b(\d{2}\.\d{3}\.\d{3}-\w)\b/,
        /\b(\d{8,9}[-]?\w)\b/
      ]

      for (const pattern of rgGenericoPatterns) {
        const rgMatches = cleanText.match(new RegExp(pattern.source, 'g'))
        if (rgMatches) {
          for (const potentialRG of rgMatches) {
            // Valida se não é CNH (11 dígitos), CPF ou data
            if (!potentialRG.match(/^\d{11}$/) &&  // Não é CNH
                !potentialRG.match(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/) &&  // Não é CPF
                !potentialRG.match(/^\d{2}\/\d{2}\/\d{4}$/) &&  // Não é data
                potentialRG.length <= 12) {  // Tamanho razoável para RG

              extractedData.rg = potentialRG
              extractedData.numeroIdentidade = potentialRG
              console.log('🆔 RG/Identidade (genérico):', extractedData.rg)
              break
            }
          }
          if (extractedData.rg) break
        }
      }
    }

    // Órgão emissor e UF
    const orgaoPatterns = [
      /\b(SSP[\s\/\-]*[A-Z]{2})\b/i,
      /\b(DETRAN[\s\/\-]*[A-Z]{2})\b/i,
      /\b(SESP[\s\/\-]*[A-Z]{2})\b/i,
      /\b(PC[\s\/\-]*[A-Z]{2})\b/i,
      /\b(IIRGD[\s\/\-]*[A-Z]{2})\b/i
    ]

    for (const pattern of orgaoPatterns) {
      const orgaoMatch = cleanText.match(pattern)
      if (orgaoMatch) {
        extractedData.orgaoExpedidor = orgaoMatch[1]?.toUpperCase()
        extractedData.orgaoEmissor = extractedData.orgaoExpedidor
        console.log('🏛️ Órgão emissor:', extractedData.orgaoExpedidor)
        break
      }
    }

    // CAMPO 4D: CPF
    const cpfMatch = cleanText.match(/\b\d{3}\.?\d{3}\.?\d{3}[-\.]?\d{2}\b/)
    if (cpfMatch) {
      extractedData.cpf = cpfMatch[0].replace(/[^\d]/g, '').replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
      console.log('📄 CPF:', extractedData.cpf)
    }

    // CAMPO 5: Nº REGISTRO (CNH) - NACIONALIDADE
    const cnhPatterns = [
      /\b(\d{11})\b/, // 11 dígitos seguidos
      /(?:CNH|REGISTRO|HABILITACAO)[:\s]*(\d{11})/i,
      /(?:N[ºº]?[\s]*REGISTRO)[:\s]*(\d{11})/i,
      /(?:NUMERO)[:\s]*(\d{11})/i
    ]

    for (const pattern of cnhPatterns) {
      const cnhMatch = cleanText.match(pattern)
      if (cnhMatch) {
        extractedData.numeroCnh = cnhMatch[1]
        console.log('🚗 Número CNH:', extractedData.numeroCnh)
        break
      }
    }

    // Nacionalidade
    if (cleanText.toLowerCase().includes('brasil')) {
      extractedData.nacionalidade = 'BRASILEIRA'
      console.log('🇧🇷 Nacionalidade:', extractedData.nacionalidade)
    }

    // FILIAÇÃO: NOME DO PAI E MÃE (cada um em uma linha)
    const filiacao: any = {}

    // Busca por filiação nas linhas do documento
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      if (!line) continue

      // Detecta linha de filiação
      if (line.toLowerCase().includes('filiacao') ||
          line.toLowerCase().includes('filiação') ||
          line.toLowerCase().includes('pai') ||
          line.toLowerCase().includes('mae') ||
          line.toLowerCase().includes('mãe')) {

        // As próximas 2-3 linhas geralmente contêm os nomes dos pais
        for (let j = i + 1; j <= Math.min(i + 3, lines.length - 1); j++) {
          const parentLine = lines[j]
          if (parentLine && parentLine.match(/^[A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{6,60}$/) && !parentLine.match(/\d/)) {
            if (!filiacao.pai) {
              filiacao.pai = parentLine.trim()
              console.log('👨 Nome do pai:', filiacao.pai)
            } else if (!filiacao.mae) {
              filiacao.mae = parentLine.trim()
              console.log('👩 Nome da mãe:', filiacao.mae)
              break
            }
          }
        }
        break
      }
    }

    // Padrões alternativos para filiação
    if (!filiacao.pai || !filiacao.mae) {
      const paiPatterns = [
        /(?:PAI[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{6,60})/i,
        /(?:FILIACAO[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{6,60})/i
      ]

      const maePatterns = [
        /(?:MAE[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{6,60})/i,
        /(?:MÃE[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{6,60})/i
      ]

      for (const pattern of paiPatterns) {
        const paiMatch = cleanText.match(pattern)
        if (paiMatch && !filiacao.pai) {
          filiacao.pai = paiMatch[1]?.trim()
          console.log('👨 Nome do pai (padrão):', filiacao.pai)
          break
        }
      }

      for (const pattern of maePatterns) {
        const maeMatch = cleanText.match(pattern)
        if (maeMatch && !filiacao.mae) {
          filiacao.mae = maeMatch[1]?.trim()
          console.log('👩 Nome da mãe (padrão):', filiacao.mae)
          break
        }
      }
    }

    if (filiacao.pai || filiacao.mae) {
      extractedData.filiacao = filiacao
    }

    console.log('✅ CNH brasileira processada:', extractedData)
    return extractedData
  }

  // Parse específico para RG
  const parseRGDocument = (text: string) => {
    console.log('🆔 Processando RG:', text.substring(0, 100) + '...')
    const cleanText = text.replace(/\n+/g, ' ').replace(/\s+/g, ' ')
    const extractedData: any = {}

    // Número do RG
    const rgPatterns = [
      /\b\d{2}\.?\d{3}\.?\d{3}-?\d{1}\b/,
      /\b\d{1,2}\.?\d{3}\.?\d{3}-?\w{1,2}\b/,
      /(?:RG[:\s]*)(\d{1,2}\.?\d{3}\.?\d{3}-?\w{1,2})/i,
      /(?:IDENTIDADE[:\s]*)(\d{1,2}\.?\d{3}\.?\d{3}-?\w{1,2})/i
    ]

    for (const pattern of rgPatterns) {
      const rgMatch = cleanText.match(pattern)
      if (rgMatch) {
        extractedData.rg = rgMatch[1] || rgMatch[0]
        extractedData.numeroIdentidade = extractedData.rg
        console.log('🆔 RG:', extractedData.rg)
        break
      }
    }

    // Nome
    const namePatterns = [
      /(?:NOME[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,60})/i,
      /(?:PORTADOR[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,60})/i
    ]

    for (const pattern of namePatterns) {
      const nameMatch = cleanText.match(pattern)
      if (nameMatch) {
        const fullName = nameMatch[1]?.trim()
        if (fullName) {
          extractedData.nomeCompleto = fullName
          const nameParts = fullName.split(' ')
          extractedData.nome = nameParts[0]
          if (nameParts.length > 1) {
            extractedData.sobrenome = nameParts.slice(1).join(' ')
          }
        }
        console.log('👤 Nome:', extractedData.nomeCompleto)
        break
      }
    }

    // CPF
    const cpfMatch = cleanText.match(/\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b/)
    if (cpfMatch) {
      extractedData.cpf = cpfMatch[0].replace(/[^\d]/g, '').replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
      console.log('📄 CPF:', extractedData.cpf)
    }

    // Data de emissão
    const datePatterns = [
      /(?:EMISSÃO[:\s]+)?(\d{2}\/\d{2}\/\d{4})/i,
      /(?:DATA[:\s]+)?(\d{2}\/\d{2}\/\d{4})/i
    ]

    for (const pattern of datePatterns) {
      const dateMatch = cleanText.match(pattern)
      if (dateMatch) {
        extractedData.dataEmissao = dateMatch[1]
        console.log('📅 Data emissão:', extractedData.dataEmissao)
        break
      }
    }

    // Órgão expedidor
    const orgaoPatterns = [
      /\b(SSP[\s\/]*[A-Z]{2})\b/i,
      /\b(DETRAN[\s\/]*[A-Z]{2})\b/i,
      /\b(SESP[\s\/]*[A-Z]{2})\b/i,
      /\b(PC[\s\/]*[A-Z]{2})\b/i,
      /\b(SSP)\b/i
    ]

    for (const pattern of orgaoPatterns) {
      const orgaoMatch = cleanText.match(pattern)
      if (orgaoMatch) {
        extractedData.orgaoExpedidor = orgaoMatch[1]?.toUpperCase()
        extractedData.orgaoEmissor = extractedData.orgaoExpedidor
        console.log('🏛️ Órgão:', extractedData.orgaoExpedidor)
        break
      }
    }

    console.log('✅ RG processado:', extractedData)
    return extractedData
  }

  // Parse específico para CPF
  const parseCPFDocument = (text: string) => {
    console.log('📄 Processando CPF:', text.substring(0, 100) + '...')
    const lines = text.split('\n').map(line => line.trim())
    const extractedData: any = {}

    // CPF
    for (const line of lines) {
      if (line.match(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/)) {
        extractedData.cpf = line
        console.log('📄 CPF:', extractedData.cpf)
        break
      }
    }

    // Nome (geralmente está na linha acima ou abaixo do CPF)
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      if (line && line.includes('CPF') && i > 0) {
        const previousLine = lines[i - 1]
        if (previousLine && previousLine.match(/^[A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]+$/)) {
          extractedData.nomeCompleto = previousLine
          const nameParts = previousLine.split(' ')
          extractedData.nome = nameParts[0]
          if (nameParts.length > 1) {
            extractedData.sobrenome = nameParts.slice(1).join(' ')
          }
          console.log('👤 Nome:', extractedData.nomeCompleto)
          break
        }
      }
    }

    console.log('✅ CPF processado:', extractedData)
    return extractedData
  }

  // Parse específico para CNPJ
  const parseCNPJDocument = (text: string) => {
    console.log('🏢 Processando CNPJ:', text.substring(0, 100) + '...')
    const cleanText = text.replace(/\n+/g, ' ').replace(/\s+/g, ' ')
    const extractedData: any = {}

    // CNPJ
    const cnpjMatch = cleanText.match(/\b\d{2}\.?\d{3}\.?\d{3}\/?\d{4}-?\d{2}\b/)
    if (cnpjMatch) {
      extractedData.cnpj = cnpjMatch[0].replace(/[^\d]/g, '').replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
      console.log('🏢 CNPJ:', extractedData.cnpj)
    }

    // Razão Social
    const razaoPatterns = [
      /(?:RAZAO[:\s]+SOCIAL[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,60})/i,
      /(?:EMPRESA[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{3,60})/i
    ]

    for (const pattern of razaoPatterns) {
      const razaoMatch = cleanText.match(pattern)
      if (razaoMatch) {
        extractedData.razaoSocial = razaoMatch[1]?.trim()
        console.log('🏢 Razão Social:', extractedData.razaoSocial)
        break
      }
    }

    console.log('✅ CNPJ processado:', extractedData)
    return extractedData
  }

  // Parse genérico para documentos não identificados
  const parseGenericDocument = (text: string) => {
    console.log('❓ Processando documento genérico:', text.substring(0, 100) + '...')
    return parseDocumentData(text)
  }

  const parseDocumentData = (text: string) => {
    const cleanText = text.replace(/\n+/g, ' ').replace(/\s+/g, ' ')
    const extractedData: any = {}

    // Extrair CPF (formato: 000.000.000-00 ou 00000000000)
    const cpfMatch = cleanText.match(/\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b/)
    if (cpfMatch) {
      extractedData.cpf = cpfMatch[0].replace(/[^\d]/g, '').replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
    }

    // Extrair RG (formato variado: 00.000.000-0, 000000000, etc.)
    const rgPatterns = [
      /\b\d{2}\.?\d{3}\.?\d{3}-?\d{1}\b/,
      /\b\d{1,2}\.?\d{3}\.?\d{3}-?\w{1,2}\b/,
      /RG[:\s]*(\d{1,2}\.?\d{3}\.?\d{3}-?\w{1,2})/i
    ]

    for (const pattern of rgPatterns) {
      const rgMatch = cleanText.match(pattern)
      if (rgMatch) {
        extractedData.rg = rgMatch[0].replace(/^RG[:\s]*/i, '')
        break
      }
    }

    // Extrair nome (heurística: linha que contém palavras em maiúscula, geralmente após "NOME" ou similar)
    const namePatterns = [
      /(?:NOME[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{2,50})/i,
      /(?:PORTADOR[:\s]+)([A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{2,50})/i,
      /\b([A-ZÁÀÂÃÉÊÍÔÕÚÇ]{2,}\s+[A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]{2,50})\b/
    ]

    for (const pattern of namePatterns) {
      const nameMatch = cleanText.match(pattern)
      if (nameMatch) {
        const name = nameMatch[1].trim()
        // Filtrar nomes muito curtos ou que parecem não ser nomes
        if (name.length > 5 && !name.match(/\d/) && name.split(' ').length >= 2) {
          extractedData.nome = name
          break
        }
      }
    }

    // Extrair data de emissão (formato: DD/MM/AAAA)
    const datePatterns = [
      /(?:EMISSÃO[:\s]+)?(\d{2}\/\d{2}\/\d{4})/i,
      /(?:DATA[:\s]+)?(\d{2}\/\d{2}\/\d{4})/i
    ]

    for (const pattern of datePatterns) {
      const dateMatch = cleanText.match(pattern)
      if (dateMatch) {
        extractedData.dataEmissao = dateMatch[1]
        break
      }
    }

    // Extrair órgão expedidor (SSP, DETRAN, etc.)
    const orgaoPatterns = [
      /\b(SSP[\/\s]*[A-Z]{2})\b/i,
      /\b(DETRAN[\/\s]*[A-Z]{2})\b/i,
      /\b(SESP[\/\s]*[A-Z]{2})\b/i,
      /\b(PC[\/\s]*[A-Z]{2})\b/i
    ]

    for (const pattern of orgaoPatterns) {
      const orgaoMatch = cleanText.match(pattern)
      if (orgaoMatch) {
        extractedData.orgaoExpedidor = orgaoMatch[1]?.toUpperCase()
        break
      }
    }

    return extractedData
  }

  const extractFromCPF = async (imageFile: File | string): Promise<OCRResult> => {
    const result = await extractText(imageFile)

    // Post-processamento específico para CPF
    if (result.extractedData) {
      const text = result.text.toLowerCase()

      // Se detectar palavras-chave do CPF, ajustar parsing
      if (text.includes('receita federal') || text.includes('cpf') || text.includes('cadastro')) {
        // Lógica específica para documento de CPF
        const cpfSpecificData = parseSpecificCPFDocument(result.text)
        result.extractedData = { ...result.extractedData, ...cpfSpecificData }
      }
    }

    return result
  }

  const extractFromRG = async (imageFile: File | string): Promise<OCRResult> => {
    const result = await extractText(imageFile)

    // Post-processamento específico para RG
    if (result.extractedData) {
      const text = result.text.toLowerCase()

      // Se detectar palavras-chave do RG, ajustar parsing
      if (text.includes('carteira de identidade') || text.includes('identidade civil') || text.includes('rg')) {
        const rgSpecificData = parseSpecificRGDocument(result.text)
        result.extractedData = { ...result.extractedData, ...rgSpecificData }
      }
    }

    return result
  }

  const parseSpecificCPFDocument = (text: string) => {
    const data: any = {}
    const lines = text.split('\n').map(line => line.trim())

    // CPF geralmente está em uma linha própria ou após "CPF:"
    for (const line of lines) {
      if (line.match(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/)) {
        data.cpf = line
        break
      }
    }

    // Nome geralmente está na linha acima ou abaixo do CPF
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      if (line.includes('CPF') && i > 0) {
        const previousLine = lines[i - 1]
        if (previousLine.match(/^[A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]+$/)) {
          data.nome = previousLine
          break
        }
      }
    }

    return data
  }

  const parseSpecificRGDocument = (text: string) => {
    const data: any = {}
    const lines = text.split('\n').map(line => line.trim())

    // RG parsing específico baseado em layout comum de RG
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]

      // Nome geralmente vem depois de "NOME:" ou é a linha mais longa em maiúsculas
      if (line && line.toUpperCase().includes('NOME') && i + 1 < lines.length) {
        const nextLine = lines[i + 1]
        if (nextLine) {
          data.nome = nextLine.replace(/[^A-ZÁÀÂÃÉÊÍÔÕÚÇ\s]/g, '').trim()
        }
      }

      // RG número
      if (line && line.match(/\d{2}\.\d{3}\.\d{3}-\d/)) {
        data.rg = line.match(/\d{2}\.\d{3}\.\d{3}-\d/)?.[0]
      }
    }

    return data
  }

  // Função para extrair foto do documento (CNH, RG, etc.)
  const extractPhotoFromDocument = async (imageFile: File): Promise<string | null> => {
    try {
      return new Promise((resolve) => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        const img = new Image()

        img.onload = () => {
          canvas.width = img.width
          canvas.height = img.height
          ctx?.drawImage(img, 0, 0)

          // Para CNH brasileira, a foto geralmente fica no canto superior direito
          // Vamos tentar extrair uma região aproximada onde costuma ficar a foto
          const photoWidth = Math.floor(img.width * 0.25) // 25% da largura
          const photoHeight = Math.floor(img.height * 0.3) // 30% da altura
          const photoX = img.width - photoWidth - Math.floor(img.width * 0.05) // 5% da margem direita
          const photoY = Math.floor(img.height * 0.1) // 10% do topo

          const photoCanvas = document.createElement('canvas')
          const photoCtx = photoCanvas.getContext('2d')
          photoCanvas.width = photoWidth
          photoCanvas.height = photoHeight

          if (photoCtx && ctx) {
            const imageData = ctx.getImageData(photoX, photoY, photoWidth, photoHeight)
            photoCtx.putImageData(imageData, 0, 0)

            // Converte para base64
            const photoBase64 = photoCanvas.toDataURL('image/jpeg', 0.8)
            resolve(photoBase64)
          } else {
            resolve(null)
          }
        }

        img.onerror = () => resolve(null)
        img.src = URL.createObjectURL(imageFile)
      })
    } catch (error) {
      console.error('Erro ao extrair foto do documento:', error)
      return null
    }
  }

  // Função para validar dados do documento contra dados do formulário
  const validateDocumentData = async (
    formData: { nome: string; email: string; cpf?: string },
    ocrResult: OCRResult,
    documentFile: File
  ): Promise<DocumentValidationResult> => {
    const errors: string[] = []
    const warnings: string[] = []
    let confidenceScore = 0
    let extractedPhoto: string | null = null
    let faceEmbedding: string | null = null

    try {
      // Extrai foto do documento
      extractedPhoto = await extractPhotoFromDocument(documentFile)

      if (extractedPhoto) {
        // Converte base64 para File para usar no reconhecimento facial
        const photoBlob = await fetch(extractedPhoto).then(r => r.blob())
        const photoFile = new File([photoBlob], 'document_photo.jpg', { type: 'image/jpeg' })

        // Extrai embedding facial
        const { extractFaceEmbedding } = useFaceRecognition()
        faceEmbedding = await extractFaceEmbedding(photoFile)
      }

      // Validação do nome
      if (ocrResult.extractedData) {
        const ocrNome = ocrResult.extractedData.nome?.toLowerCase().trim()
        const formNome = formData.nome.toLowerCase().trim()

        if (ocrNome && formNome) {
          // Verifica se os nomes são similares (permite pequenas diferenças devido ao OCR)
          const similarity = calculateNameSimilarity(formNome, ocrNome)

          if (similarity > 0.8) {
            confidenceScore += 40 // 40 pontos para nome
          } else if (similarity > 0.6) {
            warnings.push(`Nome no documento (${ocrNome}) pode não corresponder ao informado (${formNome})`)
            confidenceScore += 20
          } else {
            errors.push(`Nome no documento (${ocrNome}) não corresponde ao informado (${formNome})`)
          }
        }

        // Validação do CPF (se fornecido)
        if (formData.cpf && ocrResult.extractedData.cpf) {
          const ocrCpf = ocrResult.extractedData.cpf.replace(/[^\d]/g, '')
          const formCpf = formData.cpf.replace(/[^\d]/g, '')

          if (ocrCpf === formCpf) {
            confidenceScore += 30 // 30 pontos para CPF
          } else {
            errors.push(`CPF no documento (${ocrResult.extractedData.cpf}) não corresponde ao informado (${formData.cpf})`)
          }
        }

        // Validação de integridade do documento
        if (ocrResult.extractedData.dataEmissao || ocrResult.extractedData.validadeCnh) {
          confidenceScore += 15 // 15 pontos para dados válidos do documento
        }

        // Validação da foto extraída
        if (extractedPhoto) {
          confidenceScore += 15 // 15 pontos por ter foto extraída
        } else {
          warnings.push('Não foi possível extrair a foto do documento para validação facial')
        }
      }

      return {
        isValid: errors.length === 0 && confidenceScore >= 60,
        errors,
        warnings,
        extractedPhoto: extractedPhoto || undefined,
        faceEmbedding: faceEmbedding || undefined,
        confidenceScore
      }

    } catch (error) {
      console.error('Erro na validação do documento:', error)
      return {
        isValid: false,
        errors: ['Erro interno na validação do documento'],
        warnings: [],
        confidenceScore: 0
      }
    }
  }

  // Função auxiliar para calcular similaridade entre nomes
  const calculateNameSimilarity = (name1: string, name2: string): number => {
    // Remove acentos e normaliza
    const normalize = (str: string) => str
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .replace(/[^a-z\s]/g, '')
      .trim()

    const n1 = normalize(name1)
    const n2 = normalize(name2)

    // Verifica se um nome contém o outro (para casos como "João Silva" vs "João Pedro Silva")
    if (n1.includes(n2) || n2.includes(n1)) {
      return 0.9
    }

    // Calcula distância de Levenshtein simplificada
    const words1 = n1.split(' ')
    const words2 = n2.split(' ')

    let matches = 0
    for (const word1 of words1) {
      for (const word2 of words2) {
        if (word1 === word2 || (word1.length > 3 && word2.length > 3 &&
            (word1.includes(word2) || word2.includes(word1)))) {
          matches++
          break
        }
      }
    }

    return matches / Math.max(words1.length, words2.length)
  }

  return {
    extractText,
    extractFromCPF,
    extractFromRG,
    extractPhotoFromDocument,
    validateDocumentData,
    isProcessing: readonly(isProcessing),
    progress: readonly(progress)
  }
}