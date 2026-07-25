import api from "./api";

export interface BilanResultDto {
  id: number;
  testName: string;
  valeur: string;
  unite: string;
  referenceMin: number | null;
  referenceMax: number | null;
  referenceText: string | null;
  valeurAncienne: string | null;
  dateAncienne: string | null;
  status: string;
  confiance?: string;
}

export interface ScanBilanResponse {
  id: string;
  typeBilan: string;
  format: string;
  dateBilan: string;
  laboratoire: string;
  status: string;
  reviewStatus: string | null;
  patientId: number;
  doctorId: number | null;
  resultats: BilanResultDto[];
}

export interface BilanSummary {
  id: string;
  typeBilan: string;
  dateBilan: string;
  status: string;
  resultCount: number;
  abnormalCount: number;
  reviewStatus: string | null;
  patientId: number;
  doctorId: number | null;
}

export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  specialty: string;
  hospital: string;
  licenseNumber: string;
}

export const bilanService = {
  fetchDoctors: () =>
    api.get<DoctorDto[]>("/auth/doctors").then((r) => r.data),

  assignDoctor: (bilanId: string, doctorId: number) =>
    api
      .put<ScanBilanResponse>(`/bilans/${bilanId}/assign-doctor`, { doctorId })
      .then((r) => r.data),

  scan: (imageUri: string) => {
    const formData = new FormData();
    formData.append("image", {
      uri: imageUri,
      type: "image/jpeg",
      name: "bilan.jpg",
    } as any);
    return api
      .post<ScanBilanResponse>("/bilans/scan", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then((r) => r.data);
  },

  confirm: (id: string) =>
    api.put<ScanBilanResponse>(`/bilans/${id}/confirm`).then((r) => r.data),

  getBilans: (page = 0, size = 20) =>
    api
      .get<{ content: BilanSummary[] }>(`/bilans?page=${page}&size=${size}`)
      .then((r) => r.data),

  getBilan: (id: string) =>
    api.get<ScanBilanResponse>(`/bilans/${id}`).then((r) => r.data),

  deleteBilan: (id: string) => api.delete(`/bilans/${id}`).then((r) => r.data),

  updateResult: (
    bilanId: string,
    resultId: number,
    data: { valeur?: string; valeurAncienne?: string; referenceMin?: number | null; referenceMax?: number | null; referenceText?: string | null }
  ) =>
    api
      .put<ScanBilanResponse>(`/bilans/${bilanId}/results/${resultId}`, data)
      .then((r) => r.data),
};
