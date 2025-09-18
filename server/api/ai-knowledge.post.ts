import { defineEventHandler, readBody, readMultipartFormData, getHeader, createError } from "h3";
import { promises as fs } from 'fs';
import { join } from 'path';

// Diretório para armazenar a base de conhecimento
const KNOWLEDGE_DIR = join(process.cwd(), 'storage', 'knowledge');
const INDEX_FILE = join(KNOWLEDGE_DIR, 'index.json');

// Garantir que o diretório existe
async function ensureKnowledgeDir() {
  try {
    await fs.mkdir(KNOWLEDGE_DIR, { recursive: true });
  } catch (error) {
    // Diretório já existe
  }
}

// Carregar índice de arquivos
async function loadIndex() {
  try {
    await ensureKnowledgeDir();
    const indexRaw = await fs.readFile(INDEX_FILE, 'utf-8').catch(() => '[]');
    return JSON.parse(indexRaw);
  } catch (error) {
    return [];
  }
}

// Salvar índice de arquivos
async function saveIndex(index) {
  try {
    await ensureKnowledgeDir();
    await fs.writeFile(INDEX_FILE, JSON.stringify(index, null, 2), 'utf-8');
    return true;
  } catch (error) {
    console.error('Erro ao salvar índice:', error);
    return false;
  }
}

// Gerar nome de arquivo único
function generateFileName(originalName) {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
  const name = originalName.replace(/[^a-zA-Z0-9.-]/g, '_');
  return `${timestamp}_${name}`;
}

// Carregar toda a base de conhecimento
async function loadAllKnowledge() {
  try {
    const index = await loadIndex();
    const knowledgeFiles = await Promise.all(
      index.map(async (entry) => {
        try {
          const content = await fs.readFile(join(KNOWLEDGE_DIR, entry.fileName), 'utf-8');
          return {
            fileName: entry.originalName,
            content,
            uploadedAt: entry.uploadedAt
          };
        } catch (error) {
          return null;
        }
      })
    );

    return knowledgeFiles.filter(Boolean);
  } catch (error) {
    return [];
  }
}

// Salvar novo arquivo na base de conhecimento
async function saveKnowledgeFile(content, originalFileName) {
  try {
    const fileName = generateFileName(originalFileName);
    const filePath = join(KNOWLEDGE_DIR, fileName);

    // Salvar arquivo
    await fs.writeFile(filePath, content, 'utf-8');

    // Atualizar índice
    const index = await loadIndex();
    index.push({
      fileName,
      originalName: originalFileName,
      uploadedAt: new Date().toISOString(),
      size: content.length
    });

    await saveIndex(index);
    return true;
  } catch (error) {
    console.error('Erro ao salvar arquivo de conhecimento:', error);
    return false;
  }
}

// Limpar toda a base de conhecimento
async function clearAllKnowledge() {
  try {
    const index = await loadIndex();

    // Remover todos os arquivos
    await Promise.all(
      index.map(entry =>
        fs.unlink(join(KNOWLEDGE_DIR, entry.fileName)).catch(() => {})
      )
    );

    // Limpar índice
    await fs.unlink(INDEX_FILE).catch(() => {});

    return true;
  } catch (error) {
    console.error('Erro ao limpar base de conhecimento:', error);
    return false;
  }
}

export default defineEventHandler(async (event) => {
  const contentType = getHeader(event, 'content-type') || '';

  if (contentType.includes('multipart/form-data')) {
    // Upload de arquivo MD
    const formData = await readMultipartFormData(event);

    if (!formData || formData.length === 0) {
      throw createError({
        statusCode: 400,
        statusMessage: 'Nenhum arquivo enviado'
      });
    }

    const file = formData[0];
    if (!file.filename?.endsWith('.md')) {
      throw createError({
        statusCode: 400,
        statusMessage: 'Apenas arquivos .md são suportados'
      });
    }

    // Ler conteúdo do arquivo
    const content = file.data.toString('utf-8');

    // Salvar no disco
    const saved = await saveKnowledgeFile(content, file.filename);
    if (!saved) {
      throw createError({
        statusCode: 500,
        statusMessage: 'Erro ao salvar arquivo'
      });
    }

    return {
      success: true,
      message: `Documentação "${file.filename}" carregada com sucesso!`,
      fileName: file.filename,
      size: content.length
    };
  } else {
    // Requisição para obter a base de conhecimento atual
    const body = await readBody(event);

    if (body.action === 'get') {
      const index = await loadIndex();
      const totalFiles = index.length;
      const totalSize = index.reduce((acc, entry) => acc + entry.size, 0);

      return {
        hasKnowledge: totalFiles > 0,
        fileName: totalFiles > 1 ? `${totalFiles} arquivos` : (index[0]?.originalName || ''),
        fileCount: totalFiles,
        size: totalSize,
        preview: totalFiles > 0 ? `${totalFiles} arquivo(s) de documentação carregado(s)` : 'Nenhuma documentação carregada'
      };
    }

    if (body.action === 'clear') {
      await clearAllKnowledge();
      return {
        success: true,
        message: 'Base de conhecimento limpa'
      };
    }

    // Obter contexto para uma pergunta
    if (body.question) {
      const knowledgeFiles = await loadAllKnowledge();
      const combinedContext = knowledgeFiles
        .map(file => `# ${file.fileName}\n\n${file.content}`)
        .join('\n\n---\n\n');

      return {
        context: combinedContext,
        fileName: knowledgeFiles.length > 1
          ? `${knowledgeFiles.length} arquivos`
          : knowledgeFiles[0]?.fileName || ''
      };
    }

    // Status geral
    const index = await loadIndex();
    return {
      hasKnowledge: index.length > 0,
      fileName: index.length > 1 ? `${index.length} arquivos` : (index[0]?.originalName || ''),
      fileCount: index.length
    };
  }
});