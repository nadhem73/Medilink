from fastapi import APIRouter
from app.models import OcrRequest, OcrResponse
from app.ocr.service import OcrService
from app.ocr.engine import create_ocr_engine
from app.config import settings

router = APIRouter(prefix="/api/ai", tags=["AI OCR"])

_ocr_service = None


def _get_ocr_service() -> OcrService:
    global _ocr_service
    if _ocr_service is None:
        engine = create_ocr_engine(
            settings.OCR_ENGINE,
            settings.TESSERACT_PATH,
            settings.GROK_API_KEY,
            settings.GROK_MODEL,
            settings.GEMINI_API_KEY,
            settings.GEMINI_VISION_MODEL,
            settings.GROQ_API_KEY,
            settings.GROQ_MODEL,
        )
        _ocr_service = OcrService(engine)
    return _ocr_service


@router.post(
    "/ocr",
    response_model=OcrResponse,
    summary="OCR d'une image de bilan médical (extraction de texte + parsing structuré)",
)
async def ocr_bilan(request: OcrRequest):
    service = _get_ocr_service()
    try:
        result = await service.extract_bilan_data(request.image)
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
