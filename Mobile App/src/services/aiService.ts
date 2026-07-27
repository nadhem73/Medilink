import api from "./api";

export interface DoctorInfo {
  id: number;
  name: string;
  specialty: string;
  city: string;
  rating: string;
}

export interface ChatResponse {
  conversation_id: string;
  answer: string;
  urgency_level: string | null;
  recommended_specialty: string | null;
  doctors: DoctorInfo[];
}

export const aiService = {
  sendMessage: (message: string, conversationId?: string) =>
    api.post<ChatResponse>("/ai/chat", {
      message,
      conversation_id: conversationId || null,
    }).then(r => r.data),
};
