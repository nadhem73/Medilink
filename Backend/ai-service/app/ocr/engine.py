import base64
import io
import json
import logging
from abc import ABC, abstractmethod
from pathlib import Path

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """Tu es un expert en extraction de données de bilans médicaux tunisiens.
Analyse l'image du bilan et retourne UNIQUEMENT un JSON valide respectant exactement ce schéma :

{
  "type_bilan": "Biochimie" | "Hematologie" | null,
  "format": "nouveau_patient" | "ancien_patient",
  "date_bilan": "YYYY-MM-DD" | null,
  "laboratoire": "nom du laboratoire" | null,
  "resultats": [
    {
      "test": "nom exact du test",
      "valeur": "valeur numérique",
      "unite": "unité" | "",
      "reference_min": nombre | null,
      "reference_max": nombre | null,
      "reference_text": "texte complet de référence" | null,
      "valeur_ancienne": "valeur précédente" | null,
      "date_ancienne": "YYYY-MM-DD" | null
    }
  ]
}

Règles :
- Extrais TOUS les tests visibles
- Pour "ancien_patient", chaque ligne peut avoir valeur_actuelle ET valeur_ancienne
- Les dates sont au format français (JJ/MM/AAAA) ou ISO (AAAA-MM-JJ)
- Les unités courantes : g/l, g/dl, mmol/l, umol/l, mg/l, ui/l, %, fl, pg, mm, /mm3
- Utilise des points (pas de virgules) pour les décimales dans reference_min/reference_max
- Si une valeur de test est absente, mets null
- Ne rajoute JAMAIS de texte avant ou après le JSON"""


class BaseOCREngine(ABC):
    @abstractmethod
    async def extract_text(self, base64_image: str) -> str: ...

    async def extract_structured(self, b64: str) -> dict | None:
        return None


class TesseractEngine(BaseOCREngine):
    def __init__(self, tesseract_path: str | None = None):
        import pytesseract
        import shutil
        if tesseract_path:
            pytesseract.pytesseract.tesseract_cmd = tesseract_path
        elif not shutil.which("tesseract"):
            guessed = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
            if Path(guessed).exists():
                pytesseract.pytesseract.tesseract_cmd = guessed
        self._tesseract = pytesseract

    async def extract_text(self, b64: str) -> str:
        from PIL import Image, ImageFilter, ImageOps
        img = Image.open(io.BytesIO(base64.b64decode(b64)))
        img = self._preprocess(img)

        configs = [
            ("--psm 4 --oem 1", "PSM 4"),
            ("--psm 3 --oem 1", "PSM 3"),
        ]
        results = []
        for cfg, label in configs:
            text = self._tesseract.image_to_string(img, lang="fra", config=cfg).strip()
            results.append((len(text), text, label))

        results.sort(key=lambda x: x[0], reverse=True)
        best_len, best_text, best_label = results[0]

        logger.info("Tesseract (%s) extracted %d chars", best_label, best_len)
        return best_text or "[Tesseract] Aucun texte extrait"

    def _preprocess(self, img) -> "Image.Image":
        from PIL import Image, ImageFilter, ImageOps

        gray = img.convert("L")
        enhanced = ImageOps.autocontrast(gray, cutoff=2)
        upscaled = enhanced.resize((enhanced.width * 2, enhanced.height * 2), Image.LANCZOS)
        denoised = upscaled.filter(ImageFilter.MedianFilter(size=3))
        return denoised


class GeminiVisionEngine(BaseOCREngine):
    def __init__(self, api_key: str, model: str = "gemini-flash-lite-latest"):
        self._api_key = api_key
        self._model = model
        self._url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"

    async def extract_structured(self, b64: str) -> dict | None:
        import httpx

        payload = {
            "contents": [
                {
                    "role": "user",
                    "parts": [
                        {"text": "Extrais les données de ce bilan médical au format JSON."},
                        {"inlineData": {"mimeType": "image/jpeg", "data": b64}},
                    ],
                }
            ],
            "systemInstruction": {"parts": [{"text": SYSTEM_PROMPT}]},
            "generationConfig": {
                "temperature": 0.1,
                "maxOutputTokens": 4096,
                "response_mime_type": "application/json",
            },
        }

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                resp = await client.post(self._url, json=payload)
                resp.raise_for_status()
                data = resp.json()
                candidates = data.get("candidates", [])
                if not candidates:
                    logger.error("Gemini Vision returned no candidates: %s", data.get("error", {}).get("message", ""))
                    return None
                parts = candidates[0].get("content", {}).get("parts", [])
                text = "".join(p.get("text", "") for p in parts if p.get("text"))
                if not text:
                    logger.error("Gemini Vision returned empty text")
                    return None
                parsed = json.loads(text)
                logger.info("Gemini Vision extracted %d results", len(parsed.get("resultats", [])))
                return parsed
        except Exception as e:
            logger.error("Gemini Vision API error: %s", e)
            return None

    async def extract_text(self, b64: str) -> str:
        result = await self.extract_structured(b64)
        if result:
            return json.dumps(result, ensure_ascii=False)
        tesseract = TesseractEngine()
        return await tesseract.extract_text(b64)


class GroqEngine(BaseOCREngine):
    def __init__(self, api_key: str, model: str = "llama-3.2-90b-vision-preview"):
        self._api_key = api_key
        self._model = model

    async def extract_structured(self, b64: str) -> dict | None:
        import httpx

        data_url = f"data:image/jpeg;base64,{b64}"

        payload = {
            "model": self._model,
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": "Extrais les données de ce bilan médical au format JSON. Réponds UNIQUEMENT avec le JSON, sans texte avant ni après."},
                        {"type": "image_url", "image_url": {"url": data_url}},
                    ],
                },
            ],
            "max_tokens": 4096,
            "temperature": 0.1,
        }

        headers = {
            "Authorization": f"Bearer {self._api_key}",
            "Content-Type": "application/json",
        }

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                resp = await client.post(
                    "https://api.groq.com/openai/v1/chat/completions",
                    json=payload,
                    headers=headers,
                )
                resp.raise_for_status()
                data = resp.json()
                content = data["choices"][0]["message"]["content"]
                content = content.strip()
                if content.startswith("```"):
                    lines = content.split("\n")
                    content = "\n".join(lines[1:-1]) if len(lines) > 2 else lines[-1]
                parsed = json.loads(content)
                logger.info("Groq extracted %d results", len(parsed.get("resultats", [])))
                return parsed
        except Exception as e:
            logger.error("Groq API error: %s", e)
            return None

    async def extract_text(self, b64: str) -> str:
        result = await self.extract_structured(b64)
        if result:
            return json.dumps(result, ensure_ascii=False)
        return "[Groq] Aucun texte extrait"


class GrokEngine(BaseOCREngine):
    def __init__(self, api_key: str, model: str = "grok-2-vision-1212"):
        self._api_key = api_key
        self._model = model

    async def extract_structured(self, b64: str) -> dict | None:
        import httpx

        data_url = f"data:image/jpeg;base64,{b64}"

        payload = {
            "model": self._model,
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": "Extrais les données de ce bilan médical au format JSON."},
                        {"type": "image_url", "image_url": {"url": data_url}},
                    ],
                },
            ],
            "response_format": {"type": "json_object"},
            "max_tokens": 4096,
            "temperature": 0.1,
        }

        headers = {
            "Authorization": f"Bearer {self._api_key}",
            "Content-Type": "application/json",
        }

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                resp = await client.post(
                    "https://api.x.ai/v1/chat/completions",
                    json=payload,
                    headers=headers,
                )
                resp.raise_for_status()
                data = resp.json()
                content = data["choices"][0]["message"]["content"]
                parsed = json.loads(content)
                logger.info("Grok extracted %d results", len(parsed.get("resultats", [])))
                return parsed
        except Exception as e:
            logger.error("Grok API error: %s", e)
            return None

    async def extract_text(self, b64: str) -> str:
        result = await self.extract_structured(b64)
        if result:
            return json.dumps(result, ensure_ascii=False)
        return "[Grok] Aucun texte extrait"


class EasyOCREngine(BaseOCREngine):
    _reader = None

    async def extract_text(self, b64: str) -> str:
        import easyocr
        import numpy as np
        from PIL import Image
        if EasyOCREngine._reader is None:
            EasyOCREngine._reader = easyocr.Reader(["fr"], gpu=False)
        img = Image.open(io.BytesIO(base64.b64decode(b64)))
        lines = EasyOCREngine._reader.readtext(np.array(img), detail=0)
        result = "\n".join(lines)
        logger.info("EasyOCR extracted %d chars", len(result))
        return result or "[EasyOCR] Aucun texte extrait"


class PaddleOCREngine(BaseOCREngine):
    _ocr = None

    async def extract_text(self, b64: str) -> str:
        from paddleocr import PaddleOCR
        import numpy as np
        from PIL import Image
        if PaddleOCREngine._ocr is None:
            PaddleOCREngine._ocr = PaddleOCR(lang="fr", use_angle_cls=True, show_log=False)
        img = Image.open(io.BytesIO(base64.b64decode(b64)))
        result = PaddleOCREngine._ocr.ocr(np.array(img), cls=False)
        lines = []
        if result and result[0]:
            for line in result[0]:
                lines.append(line[1][0])
        text = "\n".join(lines)
        logger.info("PaddleOCR extracted %d chars", len(text))
        return text or "[PaddleOCR] Aucun texte extrait"


def create_ocr_engine(name: str, tesseract_path: str | None = None,
                       grok_api_key: str = "", grok_model: str = "",
                       gemini_api_key: str = "", gemini_model: str = "",
                       groq_api_key: str = "", groq_model: str = "") -> BaseOCREngine:
    engines = {
        "tesseract": lambda: TesseractEngine(tesseract_path),
        "easyocr": lambda: EasyOCREngine(),
        "paddleocr": lambda: PaddleOCREngine(),
        "grok": lambda: GrokEngine(grok_api_key, grok_model) if grok_api_key
                  else TesseractEngine(tesseract_path),
        "gemini": lambda: GeminiVisionEngine(gemini_api_key, gemini_model) if gemini_api_key
                   else TesseractEngine(tesseract_path),
        "groq": lambda: GroqEngine(groq_api_key, groq_model) if groq_api_key
                  else TesseractEngine(tesseract_path),
    }
    engine_fn = engines.get(name)
    if not engine_fn:
        raise ValueError(f"OCR engine inconnu: {name}. Choisir parmi: {list(engines.keys())}")
    engine = engine_fn()
    if name == "grok" and not grok_api_key:
        logger.warning("GROK_API_KEY vide, fallback vers Tesseract")
    if name == "gemini" and not gemini_api_key:
        logger.warning("GEMINI_API_KEY vide, fallback vers Tesseract")
    if name == "groq" and not groq_api_key:
        logger.warning("GROQ_API_KEY vide, fallback vers Tesseract")
    logger.info("Using OCR engine: %s -> %s", name, type(engine).__name__)
    return engine
