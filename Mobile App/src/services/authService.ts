import api from "./api";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserDto;
}

export interface UserDto {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  birthDate: string;
  gender: string;
  status: string;
  emailVerified: boolean;
  roles: string[];
  createdAt: string;
  pharmacieId: number | null;
}

export interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
  cin: string;
  birthDate?: string;
  gender?: string;
  address?: string;
  bloodGroup?: string;
  height?: number;
  weight?: number;
  allergies?: string;
  chronicDiseases?: string;
  currentTreatments?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  insuranceCompany?: string;
  insuranceNumber?: string;
}

export interface MessageResponse {
  message: string;
  success: boolean;
}

export const authService = {
  login: (identifier: string, password: string) =>
    api.post<AuthResponse>("/auth/login", {
      email: identifier,
      cin: identifier,
      password,
    }).then((r) => r.data),

  register: (data: RegisterData) =>
    api.post<MessageResponse>("/auth/register", data).then((r) => r.data),

  refreshToken: (refreshToken: string) =>
    api.post<AuthResponse>("/auth/refresh", { refreshToken }).then((r) => r.data),

  getMe: () => api.get<UserDto>("/auth/me").then((r) => r.data),

  linkTelegram: (email: string, telegramChatId: string) =>
    api.post<MessageResponse>("/auth/patients/telegram", { email, telegramChatId }).then((r) => r.data),

  requestEmailVerification: () =>
    api.post<MessageResponse>("/auth/verify-email/request").then((r) => r.data),

  verifyEmail: (code: string) =>
    api.post<UserDto>("/auth/verify-email/verify", { code }).then((r) => r.data),
};
