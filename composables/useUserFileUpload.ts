export type DocumentReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface FileUploadData {
  objectKey: string;
  publicUrl: string;
  filename: string;
  fileType: string;
  contentType?: string;
  fileSize?: number;
}

export interface UserFileReference extends FileUploadData {
  contentType: string;
  fileSize: number;
  uploadedAt: string;
  uploadedFromIp?: string;
  uploadedFromLocation?: string;
  isMobileUpload: boolean;
  status: DocumentReviewStatus;
  reviewerId?: string;
  reviewerName?: string;
  reviewedAt?: string;
  reviewNotes?: string;
}

export function useUserFileUpload() {
  const uploadUserFile = async (
    userId: string,
    file: File,
    fileType: string
  ): Promise<FileUploadData> => {
    try {
      // 1. Obter URL de upload presignada do backend
      const { apiFetch } = await import("~/utils/api/index");

      const uploadResponse = await apiFetch<any>(
        `/api/v1/users/${userId}/files/upload-url`,
        {
          method: "POST",
          body: {
            filename: file.name,
            contentType: file.type,
            fileType: fileType,
          },
        }
      );

      if (!uploadResponse.success) {
        throw new Error(uploadResponse.error || "Erro ao obter URL de upload");
      }

      // 2. Upload direto para MinIO usando URL presignada
      const uploadUrl = uploadResponse.data.uploadUrl;
      const objectKey = uploadResponse.data.objectKey;
      const publicUrl = uploadResponse.data.publicUrl;

      // Fazer upload via PUT para a URL presignada
      const uploadResult = await fetch(uploadUrl, {
        method: "PUT",
        body: file,
        headers: {
          "Content-Type": file.type,
        },
      });

      if (!uploadResult.ok) {
        throw new Error("Erro durante upload do arquivo");
      }

      // 3. Confirmar o upload e salvar referência no banco
      await confirmFileUpload(userId, {
        objectKey,
        publicUrl,
        filename: file.name,
        fileType,
        contentType: file.type,
        fileSize: file.size,
        isMobileUpload: false,
        location: await getCurrentLocation(),
      });

      // 4. Retornar dados do arquivo
      return {
        objectKey,
        publicUrl,
        filename: file.name,
        fileType,
        contentType: file.type,
        fileSize: file.size,
      };
    } catch (error) {
      console.error("Upload error:", error);
      throw error;
    }
  };

  const getDownloadUrl = async (
    userId: string,
    objectKey: string
  ): Promise<string> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const downloadResponse = await apiFetch<any>(
        `/api/v1/users/${userId}/files/${objectKey}/download-url`
      );

      if (!downloadResponse.success) {
        throw new Error(
          downloadResponse.error || "Erro ao obter URL de download"
        );
      }

      return downloadResponse.data.downloadUrl;
    } catch (error) {
      console.error("Download URL error:", error);
      throw error;
    }
  };

  const confirmFileUpload = async (
    userId: string,
    fileData: any
  ): Promise<void> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const confirmResponse = await apiFetch<any>(
        `/api/v1/users/${userId}/files/confirm-upload`,
        {
          method: "POST",
          body: fileData,
        }
      );

      if (!confirmResponse.success) {
        throw new Error(confirmResponse.error || "Erro ao confirmar upload");
      }
    } catch (error) {
      console.error("Confirm upload error:", error);
      throw error;
    }
  };

  const getCurrentLocation = async (): Promise<string | null> => {
    return new Promise((resolve) => {
      if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            resolve(`${latitude},${longitude}`);
          },
          (error) => {
            console.warn("Geolocation error:", error);
            resolve(null);
          },
          { timeout: 5000, enableHighAccuracy: false }
        );
      } else {
        resolve(null);
      }
    });
  };

  const getUserFiles = async (userId: string): Promise<UserFileReference[]> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const response = await apiFetch<any>(`/api/v1/users/${userId}/files`);

      if (!response.success) {
        throw new Error(response.error || "Erro ao buscar arquivos do usuário");
      }

      return response.data as UserFileReference[];
    } catch (error) {
      console.error("Get user files error:", error);
      throw error;
    }
  };

  const getUserFileByType = async (
    userId: string,
    fileType: string
  ): Promise<UserFileReference | null> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const response = await apiFetch<any>(
        `/api/v1/users/${userId}/files/${fileType.toUpperCase()}`
      );

      if (response.success) {
        return response.data;
      }

      return null;
    } catch (error: any) {
      const status = error?.response?.status ?? error?.statusCode ?? error?.status;
      if (status === 404) {
        return null;
      }
      console.error("Get user file by type error:", error);
      return null;
    }
  };

  const updateFileStatus = async (
    userId: string,
    fileType: string,
    status: DocumentReviewStatus,
    options: { reviewerId?: string; reviewerName?: string; reviewNotes?: string } = {}
  ): Promise<void> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      await apiFetch(`/api/v1/users/${userId}/files/${fileType.toUpperCase()}/status`, {
        method: 'PATCH',
        body: {
          status,
          reviewerId: options.reviewerId,
          reviewerName: options.reviewerName,
          reviewNotes: options.reviewNotes,
        },
      });
    } catch (error) {
      console.error('Update file status error:', error);
      throw error;
    }
  };

  return {
    uploadUserFile,
    getDownloadUrl,
    confirmFileUpload,
    getCurrentLocation,
    getUserFiles,
    getUserFileByType,
    updateFileStatus,
  };
}
