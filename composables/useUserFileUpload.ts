export interface FileUploadData {
  objectKey: string;
  publicUrl: string;
  filename: string;
  fileType: string;
}

export function useUserFileUpload() {
  const uploadUserFile = async (
    userId: string,
    file: File,
    fileType: "PROFILE_PHOTO" | "ADDRESS_PROOF" | "DOCUMENT"
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

  const getUserFiles = async (userId: string): Promise<any[]> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const response = await apiFetch<any>(`/api/v1/users/${userId}/files`);

      if (!response.success) {
        throw new Error(response.error || "Erro ao buscar arquivos do usuário");
      }

      return response.data;
    } catch (error) {
      console.error("Get user files error:", error);
      throw error;
    }
  };

  const getUserFileByType = async (
    userId: string,
    fileType: string
  ): Promise<any | null> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const response = await apiFetch<any>(
        `/api/v1/users/${userId}/files/${fileType}`
      );

      if (response.success) {
        return response.data;
      }

      return null;
    } catch (error) {
      console.error("Get user file by type error:", error);
      return null;
    }
  };

  return {
    uploadUserFile,
    getDownloadUrl,
    confirmFileUpload,
    getCurrentLocation,
    getUserFiles,
    getUserFileByType,
  };
}
