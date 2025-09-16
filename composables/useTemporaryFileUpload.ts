export interface TemporaryFileUploadData {
  objectKey: string;
  publicUrl: string;
  filename: string;
  fileType: string;
  sessionId: string;
  isTemporary: boolean;
}

export interface FileMigrationData {
  objectKey: string;
  publicUrl: string;
  migrated: boolean;
}

export function useTemporaryFileUpload() {
  const uploadTemporaryFile = async (
    file: File,
    fileType:
      | "PROFILE_PHOTO"
      | "ADDRESS_PROOF"
      | "DOCUMENT"
      | "RG"
      | "CNH"
      | "IDENTITY",
    sessionId?: string
  ): Promise<TemporaryFileUploadData> => {
    try {
      // 1. Obter URL de upload temporário presignado do backend
      const { apiFetch } = await import("~/utils/api/index");

      const params = new URLSearchParams({
        filename: file.name,
        contentType: file.type,
        fileType: fileType,
      });

      if (sessionId) {
        params.append("sessionId", sessionId);
      }

      const uploadResponse = await apiFetch<any>(
        `/api/v1/temp/files/upload-url?${params.toString()}`,
        {
          method: "POST",
        }
      );

      if (!uploadResponse.success) {
        throw new Error(
          uploadResponse.error || "Erro ao obter URL de upload temporário"
        );
      }

      // 2. Upload direto para MinIO usando URL presignada
      const uploadUrl = uploadResponse.data.uploadUrl;
      const objectKey = uploadResponse.data.objectKey;
      const publicUrl = uploadResponse.data.publicUrl;
      const returnedSessionId = uploadResponse.sessionId;

      // Fazer upload via PUT para a URL presignada
      const uploadResult = await fetch(uploadUrl, {
        method: "PUT",
        body: file,
        headers: {
          "Content-Type": file.type,
        },
      });

      if (!uploadResult.ok) {
        throw new Error("Erro durante upload do arquivo temporário");
      }

      // 3. Retornar dados do arquivo temporário
      return {
        objectKey,
        publicUrl,
        filename: file.name,
        fileType,
        sessionId: returnedSessionId,
        isTemporary: true,
      };
    } catch (error) {
      console.error("Temporary upload error:", error);
      throw error;
    }
  };

  const migrateTemporaryFileToUser = async (
    userId: string,
    tempObjectKey: string,
    filename: string,
    fileType: string
  ): Promise<FileMigrationData> => {
    try {
      const { apiFetch } = await import("~/utils/api/index");

      const params = new URLSearchParams({
        userId,
        tempObjectKey,
        filename,
        fileType,
      });

      const response = await apiFetch<any>(
        `/api/v1/temp/files/migrate?${params.toString()}`,
        {
          method: "POST",
        }
      );

      if (!response.success) {
        throw new Error(response.error || "Erro ao migrar arquivo temporário");
      }

      return {
        objectKey: response.data.objectKey,
        publicUrl: response.data.publicUrl,
        migrated: response.data.migrated,
      };
    } catch (error) {
      console.error("File migration error:", error);
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

  return {
    migrateTemporaryFileToUser,
    getCurrentLocation,
  };
}
