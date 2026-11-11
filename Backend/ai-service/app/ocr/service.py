import logging
import json
from app.ocr.engine import BaseOCREngine
from app.ocr.parser import BilanParser

logger = logging.getLogger(__name__)


class OcrService:
    def __init__(self, ocr_engine: BaseOCREngine):
        self.ocr_engine = ocr_engine
        self.parser = BilanParser()

    async def extract_bilan_data(self, base64_image: str) -> dict:
        try:
            structured = await self.ocr_engine.extract_structured(base64_image)
            if structured and structured.get("resultats"):
                logger.info("OCR structuré: %d résultats", len(structured["resultats"]))
                return {"success": True, "error": None, "data": structured}

            raw_text = await self.ocr_engine.extract_text(base64_image)
            if not raw_text or raw_text.startswith("["):
                return {
                    "success": False,
                    "error": f"L'OCR n'a pas pu extraire de texte: {raw_text}",
                    "data": None,
                }

            logger.info("Texte OCR extrait (%d caractères)", len(raw_text))

            parsed = self.parser.parse(raw_text)
            if not parsed.get("resultats"):
                return {
                    "success": False,
                    "error": f"Aucun résultat de bilan reconnu. Texte extrait (début): {raw_text[:300]}",
                    "data": None,
                }

            logger.info("Parsing réussi: %d résultats, format=%s", len(parsed["resultats"]), parsed.get("format"))
            return {"success": True, "error": None, "data": parsed}

        except Exception as e:
            logger.error("OCR extraction failed: %s", e)
            return {
                "success": False,
                "error": f"Erreur lors du traitement OCR: {str(e)}",
                "data": None,
            }
