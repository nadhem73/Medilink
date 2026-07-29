"""Tests pour le client LLM Gemini."""

import httpx
import pytest
from unittest.mock import AsyncMock, patch, MagicMock

from app.llm_client import LLMClient


@pytest.fixture
def llm_client():
    return LLMClient()


@pytest.mark.asyncio
async def test_generate_returns_text_on_success(llm_client):
    mock_data = {
        "candidates": [
            {
                "content": {
                    "parts": [{"text": "Voici la réponse médicale."}]
                },
                "finishReason": "STOP"
            }
        ]
    }

    with patch("httpx.AsyncClient.post") as mock_post:
        mock_response = MagicMock()
        mock_response.json.return_value = mock_data
        mock_response.raise_for_status.return_value = None
        mock_post.return_value = mock_response

        result = await llm_client.generate(
            system_prompt="Soyez un assistant médical.",
            messages=[{"role": "user", "content": "J'ai mal à la tête."}]
        )

        assert result == "Voici la réponse médicale."


@pytest.mark.asyncio
async def test_generate_returns_fallback_on_exception(llm_client):
    with patch("httpx.AsyncClient.post") as mock_post:
        mock_post.side_effect = httpx.RequestError("Connection failed")

        result = await llm_client.generate(
            system_prompt="Soyez un assistant médical.",
            messages=[{"role": "user", "content": "J'ai mal à la tête."}]
        )

        assert "Désolé" in result


@pytest.mark.asyncio
async def test_generate_with_image_returns_text(llm_client):
    mock_data = {
        "candidates": [
            {
                "content": {
                    "parts": [{"text": '{"type_bilan": "Biochimie"}'}]
                },
                "finishReason": "STOP"
            }
        ],
        "usageMetadata": {
            "promptTokenCount": 100,
            "candidatesTokenCount": 50
        }
    }

    with patch("httpx.AsyncClient.post") as mock_post:
        mock_response = MagicMock()
        mock_response.json.return_value = mock_data
        mock_response.status_code = 200
        mock_response.raise_for_status.return_value = None
        mock_post.return_value = mock_response

        result = await llm_client.generate_with_image(
            system_prompt="Extrais les données du bilan.",
            base64_image="base64encodedimage"
        )

        assert "type_bilan" in result


@pytest.mark.asyncio
async def test_generate_with_image_raises_on_http_error(llm_client):
    with patch("httpx.AsyncClient.post") as mock_post:
        mock_response = MagicMock()
        mock_response.status_code = 500
        mock_response.raise_for_status.side_effect = httpx.HTTPStatusError(
            "500 Error", request=MagicMock(), response=mock_response
        )
        mock_post.return_value = mock_response

        with pytest.raises(httpx.HTTPStatusError):
            await llm_client.generate_with_image(
                system_prompt="Extrais les données du bilan.",
                base64_image="test"
            )


@pytest.mark.asyncio
async def test_generate_with_image_retries_on_429(llm_client):
    mock_data = {
        "candidates": [
            {
                "content": {
                    "parts": [{"text": "OK"}]
                },
                "finishReason": "STOP"
            }
        ],
        "usageMetadata": {}
    }

    mock_429 = MagicMock()
    mock_429.status_code = 429
    mock_429.raise_for_status.side_effect = httpx.HTTPStatusError(
        "429 Too Many Requests", request=MagicMock(), response=mock_429
    )

    mock_ok = MagicMock()
    mock_ok.status_code = 200
    mock_ok.json.return_value = mock_data
    mock_ok.raise_for_status.return_value = None

    with patch("httpx.AsyncClient.post") as mock_post:
        mock_post.side_effect = [mock_429, mock_ok]

        result = await llm_client.generate_with_image(
            system_prompt="Extrais",
            base64_image="test"
        )

        assert result == "OK"
        assert mock_post.call_count == 2


@pytest.mark.asyncio
async def test_extract_text_returns_none_on_no_candidates(llm_client):
    data = {"candidates": []}
    result = llm_client._extract_text(data)
    assert result is None


@pytest.mark.asyncio
async def test_extract_text_returns_text_from_parts(llm_client):
    data = {
        "candidates": [
            {
                "content": {
                    "parts": [{"text": "Part1"}, {"text": "Part2"}]
                },
                "finishReason": "STOP"
            }
        ]
    }
    result = llm_client._extract_text(data)
    assert result == "Part1Part2"


@pytest.mark.asyncio
async def test_generate_constructs_correct_request_body(llm_client):
    with patch("httpx.AsyncClient.post") as mock_post:
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "candidates": [{"content": {"parts": [{"text": "OK"}]}}]
        }
        mock_response.raise_for_status.return_value = None
        mock_post.return_value = mock_response

        await llm_client.generate(
            system_prompt="Soyez utile.",
            messages=[
                {"role": "user", "content": "Bonjour"},
                {"role": "assistant", "content": "Bonjour, comment puis-je vous aider ?"}
            ]
        )

        call_kwargs = mock_post.call_args[1]
        body = call_kwargs["json"]

        assert body["systemInstruction"]["parts"][0]["text"] == "Soyez utile."
        assert body["contents"][0]["role"] == "user"
        assert body["contents"][0]["parts"][0]["text"] == "Bonjour"
        assert body["contents"][1]["role"] == "model"
