import asyncio
import json
import logging
from typing import Any
import httpx
from app.config import settings

logger = logging.getLogger(__name__)


class LLMClient:
    def __init__(self):
        if not settings.GEMINI_API_KEY:
            logger.warning("GEMINI_API_KEY not set — LLM calls will fail")
        self.api_key = settings.GEMINI_API_KEY
        self.model = settings.LLM_MODEL
        self.base_url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent?key={self.api_key}"

    async def generate(
        self,
        system_prompt: str,
        messages: list[dict[str, str]],
        tools: list[dict[str, Any]] | None = None,
    ) -> str:
        contents = []
        for msg in messages:
            role = "model" if msg["role"] == "assistant" else msg["role"]
            contents.append({
                "role": role,
                "parts": [{"text": msg["content"]}],
            })

        body = {
            "contents": contents,
            "systemInstruction": {"parts": [{"text": system_prompt}]},
            "generationConfig": {
                "temperature": settings.LLM_TEMPERATURE,
                "maxOutputTokens": settings.LLM_MAX_TOKENS,
            },
        }

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                resp = await self._post_with_retry(client, body)
                data = resp.json()
                text = self._extract_text(data)
                return text if text else "Désolé, je n'ai pas pu traiter votre demande."
        except Exception as e:
            logger.error("Gemini API error: %s", e)
            return "Désolé, je n'ai pas pu traiter votre demande pour le moment. Veuillez réessayer."

    async def _post_with_retry(self, client: httpx.AsyncClient, body: dict, max_retries: int = 3) -> httpx.Response:
        for attempt in range(max_retries):
            resp = await client.post(self.base_url, json=body)
            if resp.status_code == 429 and attempt < max_retries - 1:
                wait = 2 ** (attempt + 1)
                logger.warning("Gemini 429 (rate limit), retrying in %ds (attempt %d/%d)", wait, attempt + 1, max_retries)
                await asyncio.sleep(wait)
                continue
            resp.raise_for_status()
            return resp
        raise httpx.HTTPStatusError("429 Too Many Requests — quota épuisé", request=None, response=resp)

    def _extract_text(self, data: dict) -> str | None:
        candidates = data.get("candidates", [])
        if not candidates:
            error = data.get("error", {})
            logger.error("Gemini returned no candidates: %s", error.get("message", "unknown"))
            return None

        finish_reason = candidates[0].get("finishReason", "unknown")
        if finish_reason == "MAX_TOKENS":
            logger.warning("Gemini response truncated (MAX_TOKENS)")

        parts = candidates[0].get("content", {}).get("parts", [])
        texts = [p["text"] for p in parts if p.get("text")]
        result = "".join(texts) if texts else None
        logger.info("Gemini finish_reason=%s, parts=%d, text_len=%s",
                    finish_reason, len(parts), len(result) if result else 0)
        return result
