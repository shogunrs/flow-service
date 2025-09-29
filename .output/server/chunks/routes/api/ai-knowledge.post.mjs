import { d as defineEventHandler, g as getHeader, r as readMultipartFormData, c as createError, a as readBody } from '../../nitro/nitro.mjs';
import { promises } from 'fs';
import { join } from 'path';
import 'node:http';
import 'node:https';
import 'node:events';
import 'node:buffer';
import 'node:fs';
import 'node:path';
import 'node:crypto';
import 'node:url';

const KNOWLEDGE_DIR = join(process.cwd(), "storage", "knowledge");
const INDEX_FILE = join(KNOWLEDGE_DIR, "index.json");
async function ensureKnowledgeDir() {
  try {
    await promises.mkdir(KNOWLEDGE_DIR, { recursive: true });
  } catch (error) {
  }
}
async function loadIndex() {
  try {
    await ensureKnowledgeDir();
    const indexRaw = await promises.readFile(INDEX_FILE, "utf-8").catch(() => "[]");
    return JSON.parse(indexRaw);
  } catch (error) {
    return [];
  }
}
async function saveIndex(index) {
  try {
    await ensureKnowledgeDir();
    await promises.writeFile(INDEX_FILE, JSON.stringify(index, null, 2), "utf-8");
    return true;
  } catch (error) {
    console.error("Erro ao salvar \xEDndice:", error);
    return false;
  }
}
function generateFileName(originalName) {
  const timestamp = (/* @__PURE__ */ new Date()).toISOString().replace(/[:.]/g, "-");
  const name = originalName.replace(/[^a-zA-Z0-9.-]/g, "_");
  return `${timestamp}_${name}`;
}
async function loadAllKnowledge() {
  try {
    const index = await loadIndex();
    const knowledgeFiles = await Promise.all(
      index.map(async (entry) => {
        try {
          const content = await promises.readFile(join(KNOWLEDGE_DIR, entry.fileName), "utf-8");
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
async function saveKnowledgeFile(content, originalFileName) {
  try {
    const fileName = generateFileName(originalFileName);
    const filePath = join(KNOWLEDGE_DIR, fileName);
    await promises.writeFile(filePath, content, "utf-8");
    const index = await loadIndex();
    index.push({
      fileName,
      originalName: originalFileName,
      uploadedAt: (/* @__PURE__ */ new Date()).toISOString(),
      size: content.length
    });
    await saveIndex(index);
    return true;
  } catch (error) {
    console.error("Erro ao salvar arquivo de conhecimento:", error);
    return false;
  }
}
async function clearAllKnowledge() {
  try {
    const index = await loadIndex();
    await Promise.all(
      index.map(
        (entry) => promises.unlink(join(KNOWLEDGE_DIR, entry.fileName)).catch(() => {
        })
      )
    );
    await promises.unlink(INDEX_FILE).catch(() => {
    });
    return true;
  } catch (error) {
    console.error("Erro ao limpar base de conhecimento:", error);
    return false;
  }
}
const aiKnowledge_post = defineEventHandler(async (event) => {
  var _a, _b, _c, _d;
  const contentType = getHeader(event, "content-type") || "";
  if (contentType.includes("multipart/form-data")) {
    const formData = await readMultipartFormData(event);
    if (!formData || formData.length === 0) {
      throw createError({
        statusCode: 400,
        statusMessage: "Nenhum arquivo enviado"
      });
    }
    const file = formData[0];
    if (!((_a = file.filename) == null ? void 0 : _a.endsWith(".md"))) {
      throw createError({
        statusCode: 400,
        statusMessage: "Apenas arquivos .md s\xE3o suportados"
      });
    }
    const content = file.data.toString("utf-8");
    const saved = await saveKnowledgeFile(content, file.filename);
    if (!saved) {
      throw createError({
        statusCode: 500,
        statusMessage: "Erro ao salvar arquivo"
      });
    }
    return {
      success: true,
      message: `Documenta\xE7\xE3o "${file.filename}" carregada com sucesso!`,
      fileName: file.filename,
      size: content.length
    };
  } else {
    const body = await readBody(event);
    if (body.action === "get") {
      const index2 = await loadIndex();
      const totalFiles = index2.length;
      const totalSize = index2.reduce((acc, entry) => acc + entry.size, 0);
      return {
        hasKnowledge: totalFiles > 0,
        fileName: totalFiles > 1 ? `${totalFiles} arquivos` : ((_b = index2[0]) == null ? void 0 : _b.originalName) || "",
        fileCount: totalFiles,
        size: totalSize,
        preview: totalFiles > 0 ? `${totalFiles} arquivo(s) de documenta\xE7\xE3o carregado(s)` : "Nenhuma documenta\xE7\xE3o carregada"
      };
    }
    if (body.action === "clear") {
      await clearAllKnowledge();
      return {
        success: true,
        message: "Base de conhecimento limpa"
      };
    }
    if (body.question) {
      const knowledgeFiles = await loadAllKnowledge();
      const combinedContext = knowledgeFiles.map((file) => `# ${file.fileName}

${file.content}`).join("\n\n---\n\n");
      return {
        context: combinedContext,
        fileName: knowledgeFiles.length > 1 ? `${knowledgeFiles.length} arquivos` : ((_c = knowledgeFiles[0]) == null ? void 0 : _c.fileName) || ""
      };
    }
    const index = await loadIndex();
    return {
      hasKnowledge: index.length > 0,
      fileName: index.length > 1 ? `${index.length} arquivos` : ((_d = index[0]) == null ? void 0 : _d.originalName) || "",
      fileCount: index.length
    };
  }
});

export { aiKnowledge_post as default };
//# sourceMappingURL=ai-knowledge.post.mjs.map
