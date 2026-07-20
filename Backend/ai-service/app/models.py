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


class OcrRequest(BaseModel):
    image: str = Field(..., description="Image en base64")


class OcrResultItem(BaseModel):
    test: str | None = None
    valeur: str | None = None
    unite: str | None = None
    reference_min: float | None = None
    reference_max: float | None = None
    reference_text: str | None = None
    valeur_ancienne: str | None = None
    date_ancienne: str | None = None
    confiance: str | None = None


class OcrResultData(BaseModel):
    type_bilan: str | None = None
    format: str | None = None
    date_bilan: str | None = None
    laboratoire: str | None = None
    resultats: list[OcrResultItem] = []


class OcrResponse(BaseModel):
    success: bool
    error: str | None = None
    data: OcrResultData | None = None


class ErrorResponse(BaseModel):
    detail: str


class HealthResponse(BaseModel):
    status: str
    service: str
    llm_provider: str
    model: str
