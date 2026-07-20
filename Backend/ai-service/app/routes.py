from fastapi import APIRouter, Depends, HTTPException, Request
from app.models import ChatRequest, ChatResponse, ErrorResponse, HealthResponse, OcrRequest, OcrResponse
from app.ai_service import process_message
from app.ocr_service import OcrService
from app.security import get_current_user
from app.config import settings
from app.llm_client import LLMClient

router = APIRouter(prefix="/api/ai", tags=["AI Chat"])


@router.post(
    "/chat",
    response_model=ChatResponse,
    responses={401: {"model": ErrorResponse}, 422: {"model": ErrorResponse}},
    summary="Envoyer un message à l'assistant médical",
)
async def chat(
    request: ChatRequest,
    user: dict = Depends(get_current_user),
):
    patient_id = int(user.get("userId", 0))
    return await process_message(
        conversation_id=request.conversation_id,
        message=request.message,
        patient_id=patient_id,
    )


@router.post(
    "/ocr",
    response_model=OcrResponse,
    summary="OCR d'une image de bilan médical via Gemini Vision",
)
async def ocr_bilan(request: OcrRequest):
    llm_client = LLMClient()
    ocr_service = OcrService(llm_client)
    try:
        result = await ocr_service.extract_bilan_data(request.image)
        return OcrResponse(
            success=result["success"],
            error=result.get("error"),
            data=result.get("data"),
        )
    except Exception as e:
        return OcrResponse(
            success=False,
            error=str(e),
            data=None,
        )


@router.get("/health", response_model=HealthResponse)
async def health():
    return HealthResponse(
        status="UP",
        service="AI-SERVICE",
        llm_provider=settings.LLM_PROVIDER,
        model=settings.LLM_MODEL,
    )
