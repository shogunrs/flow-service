import { ref } from 'vue';
import { useToast } from '~/composables/useToast';

export interface DocumentApprovalFile {
  fileType: string;
  filename: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  uploadedAt?: string;
  fileSize?: number;
  contentType?: string;
  reviewerId?: string;
  reviewerName?: string;
  reviewedAt?: string;
  reviewNotes?: string;
  downloadPath: string;
}

export interface DocumentApprovalSummary {
  success: boolean;
  data: DocumentApprovalFile[];
}

export function useDocumentApproval() {
  const { error: toastError, success: toastSuccess } = useToast();
  const loading = ref(false);

  const listDocumentFiles = async (entityType: string, entityId: string): Promise<DocumentApprovalFile[]> => {
    try {
      loading.value = true;
      const { apiFetch } = await import('~/utils/api/index');
      const response = await apiFetch<{
        success: boolean;
        data: DocumentApprovalFile[];
        error?: string;
      }>(`/api/v1/document-approvals/${entityType}/${entityId}/files`, {
        method: 'GET',
        silent: true,
      });

      if (!response?.success) {
        throw new Error(response?.error || 'Erro ao carregar documentos');
      }

      return response.data || [];
    } catch (err) {
      console.error('DocumentApproval:list error', err);
      toastError('Não foi possível carregar os documentos para revisão.');
      return [];
    } finally {
      loading.value = false;
    }
  };

  const decideDocument = async (
    entityType: string,
    entityId: string,
    fileType: string,
    status: 'APPROVED' | 'REJECTED',
    options: { reviewerId?: string; reviewerName?: string; reviewNotes?: string } = {}
  ) => {
    try {
      const { apiFetch } = await import('~/utils/api/index');
      await apiFetch(`/api/v1/document-approvals/${entityType}/${entityId}/${fileType}`, {
        method: 'PATCH',
        body: {
          status,
          reviewerId: options.reviewerId,
          reviewerName: options.reviewerName,
          reviewNotes: options.reviewNotes,
        },
      });

      toastSuccess(
        status === 'APPROVED'
          ? 'Documento aprovado com sucesso.'
          : 'Documento reprovado com sucesso.'
      );
    } catch (err) {
      console.error('DocumentApproval:decision error', err);
      toastError('Não foi possível atualizar o status do documento.');
      throw err;
    }
  };

  const getDownloadUrl = async (entityType: string, entityId: string, fileType: string): Promise<string | null> => {
    try {
      const { apiFetch } = await import('~/utils/api/index');
      const response = await apiFetch<{
        success: boolean;
        data: { downloadUrl: string };
        error?: string;
      }>(`/api/v1/document-approvals/${entityType}/${entityId}/${fileType}/download`, {
        method: 'GET',
        silent: true,
      });

      if (!response?.success) {
        throw new Error(response?.error || 'Erro ao gerar link de download');
      }

      return response.data?.downloadUrl || null;
    } catch (err) {
      console.error('DocumentApproval:download error', err);
      toastError('Não foi possível abrir o documento.');
      return null;
    }
  };

  return {
    loading,
    listDocumentFiles,
    decideDocument,
    getDownloadUrl,
  };
}
