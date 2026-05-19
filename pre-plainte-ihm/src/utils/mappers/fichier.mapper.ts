import type {FichierDTO} from "@/types/preplainte-payload-interface.ts";

/**
 * Mapper pour les fichiers
 */
export class FichierMapper {

  /**
   * Transforme un fichier front (File) en DTO pour le backend
   */
  static async fileToDTO(file: File): Promise<FichierDTO> {
    const [dto] = await this.filesToDTO([file]);
    return dto;
  }

  /**
   * Transforme les fichiers front (File[]) en DTO pour le backend
   */
  static async filesToDTO(files: File[]): Promise<FichierDTO[]> {
    return Promise.all(
      files.map(file => new Promise<FichierDTO>((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
          const base64 = (reader.result as string).split(',')[1].replaceAll(/\s/g, '');
          resolve({
            nom: file.name,
            typeMime: file.type,
            contenuBase64: base64
          });
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
      }))
    );
  }

}
