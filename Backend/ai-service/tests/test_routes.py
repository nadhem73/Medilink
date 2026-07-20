"""Tests pour les routes API FastAPI."""

import pytest
from httpx import AsyncClient, ASGITransport
from unittest.mock import AsyncMock, MagicMock, patch

from app.main import app
from app.models import OcrResponse, HealthResponse


@pytest.fixture
def client():
    transport = ASGITransport(app=app)
    return AsyncClient(transport=transport, base_url="http://test")


@pytest.mark.asyncio
async def test_health_endpoint(client):
    response = await client.get("/api/ai/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "AI-SERVICE"
    assert "model" in data


@pytest.mark.asyncio
async def test_ocr_endpoint_success(client):
    mock_data = {
        "type_bilan": "Biochimie",
        "format": "nouveau_patient",
        "date_bilan": "2026-06-15",
        "laboratoire": "Labo",
        "resultats": [
            {"test": "Glycemie", "valeur": "5.2", "unite": "mmol/L",
             "reference_min": 3.9, "reference_max": 6.1,
             "reference_text": "3.9 - 6.1", "confiance": "haute"}
        ]
    }

    with patch("app.routes.OcrService") as MockOcrService:
        mock_instance = MagicMock()
        mock_instance.extract_bilan_data = AsyncMock(return_value={
            "success": True, "error": None, "data": mock_data
        })
        MockOcrService.return_value = mock_instance

        response = await client.post(
            "/api/ai/ocr",
            json={"image": "base64encoded"}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["type_bilan"] == "Biochimie"
        assert len(data["data"]["resultats"]) == 1


@pytest.mark.asyncio
async def test_ocr_endpoint_error(client):
    with patch("app.routes.OcrService") as MockOcrService:
        mock_instance = MagicMock()
        mock_instance.extract_bilan_data = AsyncMock(return_value={
            "success": False,
            "error": "L'IA n'a pas pu extraire les données",
            "data": None
        })
        MockOcrService.return_value = mock_instance

        response = await client.post(
            "/api/ai/ocr",
            json={"image": "base64encoded"}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False
        assert data["data"] is None


@pytest.mark.asyncio
async def test_ocr_endpoint_validates_image_field(client):
    response = await client.post(
        "/api/ai/ocr",
        json={}
    )
    assert response.status_code == 422


@pytest.mark.asyncio
async def test_ocr_endpoint_with_empty_image(client):
    response = await client.post(
        "/api/ai/ocr",
        json={"image": ""}
    )
    assert response.status_code in (200, 422)


@pytest.mark.asyncio
async def test_ocr_endpoint_handles_llm_exception(client):
    with patch("app.routes.OcrService") as MockOcrService:
        mock_instance = MagicMock()
        mock_instance.extract_bilan_data = AsyncMock(side_effect=Exception("LLM error"))
        MockOcrService.return_value = mock_instance

        response = await client.post(
            "/api/ai/ocr",
            json={"image": "base64encoded"}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False


@pytest.mark.asyncio
async def test_chat_endpoint_requires_auth(client):
    response = await client.post(
        "/api/ai/chat",
        json={"message": "Bonjour"}
    )
    assert response.status_code == 403
