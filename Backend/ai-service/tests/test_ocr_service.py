"""Tests pour le service OCR."""

import pytest
from unittest.mock import AsyncMock, MagicMock, patch

from app.ocr_service import OcrService, OCR_SYSTEM_PROMPT


@pytest.fixture
def mock_llm_client():
    client = MagicMock()
    client.generate_with_image = AsyncMock()
    return client


@pytest.fixture
def ocr_service(mock_llm_client):
    return OcrService(mock_llm_client)


@pytest.mark.asyncio
async def test_extract_bilan_data_success(ocr_service, mock_llm_client):
    mock_llm_client.generate_with_image.return_value = """
    {
        "type_bilan": "Biochimie",
        "format": "nouveau_patient",
        "date_bilan": "2026-06-15",
        "laboratoire": "Laboratoire Tunis",
        "resultats": [
            {
                "test": "Glycemie",
                "valeur": "5.2",
                "unite": "mmol/L",
                "reference_min": 3.9,
                "reference_max": 6.1,
                "reference_text": "3.9 - 6.1",
                "confiance": "haute"
            }
        ]
    }
    """

    result = await ocr_service.extract_bilan_data("base64image")

    assert result["success"] is True
    assert result["error"] is None
    assert result["data"]["type_bilan"] == "Biochimie"
    assert len(result["data"]["resultats"]) == 1
    assert result["data"]["resultats"][0]["test"] == "Glycemie"
    assert result["data"]["resultats"][0]["valeur"] == "5.2"
    assert result["data"]["resultats"][0]["confiance"] == "haute"


@pytest.mark.asyncio
async def test_extract_bilan_data_with_old_patient(ocr_service, mock_llm_client):
    mock_llm_client.generate_with_image.return_value = """
    {
        "type_bilan": "Hematologie",
        "format": "ancien_patient",
        "date_bilan": "2026-05-20",
        "laboratoire": "Labo Centre",
        "resultats": [
            {
                "test": "Hemoglobine",
                "valeur": "13.2",
                "unite": "g/dL",
                "reference_min": 11.5,
                "reference_max": 15.5,
                "reference_text": "11.5 - 15.5",
                "valeur_ancienne": "12.8",
                "date_ancienne": "2025-11-10",
                "confiance": "haute"
            }
        ]
    }
    """

    result = await ocr_service.extract_bilan_data("base64image")

    assert result["success"] is True
    assert result["data"]["resultats"][0]["valeur_ancienne"] == "12.8"
    assert result["data"]["resultats"][0]["date_ancienne"] == "2025-11-10"


@pytest.mark.asyncio
async def test_extract_bilan_data_returns_error_on_invalid_json(ocr_service, mock_llm_client):
    mock_llm_client.generate_with_image.return_value = "Réponse non-JSON"

    result = await ocr_service.extract_bilan_data("base64image")

    assert result["success"] is False
    assert "invalide" in result["error"]
    assert result["data"] is None


@pytest.mark.asyncio
async def test_extract_bilan_data_returns_error_on_exception(ocr_service, mock_llm_client):
    mock_llm_client.generate_with_image.side_effect = Exception("LLM crash")

    result = await ocr_service.extract_bilan_data("base64image")

    assert result["success"] is False
    assert "LLM crash" in result["error"]


@pytest.mark.asyncio
async def test_extract_json_with_code_block(ocr_service):
    text = '```json\n{"test": "valeur"}\n```'
    result = ocr_service._extract_json(text)
    assert result == {"test": "valeur"}


@pytest.mark.asyncio
async def test_extract_json_with_braces(ocr_service):
    text = "Voici le resultat: {\"test\": \"valeur\"} fin"
    result = ocr_service._extract_json(text)
    assert result == {"test": "valeur"}


@pytest.mark.asyncio
async def test_extract_json_returns_none_on_empty(ocr_service):
    assert ocr_service._extract_json("") is None
    assert ocr_service._extract_json("   ") is None
    assert ocr_service._extract_json(None) is None


@pytest.mark.asyncio
async def test_validate_data_filters_invalid_results(ocr_service):
    data = {
        "type_bilan": "Biochimie",
        "format": "nouveau_patient",
        "resultats": [
            {"test": "Glycemie", "valeur": "5.2"},
            {"pas_de_test": "valeur"},
            "ceci est une string"
        ]
    }

    validated = ocr_service._validate_data(data)

    assert len(validated["resultats"]) == 1
    assert validated["resultats"][0]["test"] == "Glycemie"


@pytest.mark.asyncio
async def test_compute_confiance_high_with_full_range(ocr_service):
    item = {"valeur": "5.0", "reference_min": 3.0, "reference_max": 7.0}
    assert ocr_service._compute_confiance(item) == "haute"


@pytest.mark.asyncio
async def test_compute_confiance_low_with_threshold_value(ocr_service):
    item = {"valeur": "<5", "reference_min": None, "reference_max": None}
    assert ocr_service._compute_confiance(item) == "faible"


@pytest.mark.asyncio
async def test_compute_confiance_moyenne_with_text_ref(ocr_service):
    item = {"valeur": "Positif", "reference_text": "Negatif"}
    assert ocr_service._compute_confiance(item) == "moyenne"


@pytest.mark.asyncio
async def test_to_float_handles_none(ocr_service):
    assert ocr_service._to_float(None) is None


@pytest.mark.asyncio
async def test_to_float_handles_number_string(ocr_service):
    assert ocr_service._to_float("12.5") == 12.5


@pytest.mark.asyncio
async def test_to_float_handles_invalid(ocr_service):
    assert ocr_service._to_float("not-a-number") is None
