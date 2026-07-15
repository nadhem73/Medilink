import api from "./api";

export interface MedicalRecord {
  id: number;
  bloodGroup: string;
  height: number;
  weight: number;
  allergies: string;
  chronicDiseases: string;
  currentTreatments: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  insuranceCompany: string;
  insuranceNumber: string;
}

export interface Appointment {
  id: number;
  patientId: number;
  doctorId: number;
  dateTime: string;
  status: string;
  mode: string;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DoctorInfo {
  id: number;
  firstName: string;
  lastName: string;
  specialty: string;
  hospital: string;
}

export interface AvailableSlot {
  time: string;
  available: boolean;
}

export interface PrescriptionItemResponse {
  id: number;
  medicamentId: number;
  medicamentName: string;
  dosage: string;
  forme: string;
  posologie: string;
  dureeTraitement: number | null;
  voieAdministration: string;
  instructions: string;
  quantitePrescrite: number | null;
}

export interface PrescriptionResponse {
  id: number;
  consultationId: number | null;
  patientId: number;
  doctorId: number | null;
  pharmacieId: number | null;
  status: string;
  notes: string | null;
  items: PrescriptionItemResponse[];
  createdAt: string;
  updatedAt: string;
  pickupCode: string | null;
}

export const patientService = {
  getMyMedicalRecord: () =>
    api.get<MedicalRecord>("/patients/me/medical-record").then((r) => r.data),

  updateMedicalRecord: (data: Partial<MedicalRecord>) =>
    api.put<MedicalRecord>("/patients/me/medical-record", data).then((r) => r.data),

  getMyAppointments: () =>
    api.get<Appointment[]>("/patients/appointments").then((r) => r.data),

  bookAppointment: (data: {
    doctorId: number;
    dateTime: string;
    mode: string;
    notes?: string;
  }) => api.post("/patients/appointments", data).then((r) => r.data),

  cancelAppointment: (id: number) =>
    api.put(`/patients/appointments/${id}/cancel`).then((r) => r.data),

  getAvailableSlots: (doctorId: number, date: string) =>
    api
      .get<string[]>("/patients/appointments/available-slots", {
        params: { doctorId, date },
      })
      .then((r) => (r.data || []).map((time) => ({ time, available: true }))),

  getDoctors: () => api.get<DoctorInfo[]>("/auth/doctors").then((r) => r.data),

  getPatientPrescriptions: (patientId: number) =>
    api.get<PrescriptionResponse[]>(`/prescriptions/patient/${patientId}`).then((r) => r.data),
};
