from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    conversation_id: str | None = Field(None, description="ID de conversation existante ou null pour nouvelle conversation")
    message: str = Field(..., min_length=1, max_length=2000, description="Message de l'utilisateur")


class DoctorInfo(BaseModel):
    id: int
    name: str
    specialty: str
    city: str
    rating: str


class ChatResponse(BaseModel):
    conversation_id: str
    answer: str
    urgency_level: str | None = None
    recommended_specialty: str | None = None
    doctors: list[DoctorInfo] = []


class ErrorResponse(BaseModel):
    detail: str


class HealthResponse(BaseModel):
    status: str
    service: str
    llm_provider: str
    model: str
