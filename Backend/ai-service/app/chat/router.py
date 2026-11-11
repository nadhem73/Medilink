from fastapi import APIRouter, Depends
from app.models import ChatRequest, ChatResponse, ErrorResponse
from app.chat.service import process_message
from app.security import get_current_user

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
