import os
from dotenv import load_dotenv, dotenv_values


class Settings:
    _config: dict[str, str | None] | None = None

    def _load(self) -> dict[str, str | None]:
        if self._config is None:
            load_dotenv()
            self._config = dotenv_values(".env")
        return self._config

    def _get(self, key: str, default: str) -> str:
        val = os.getenv(key)
        if val is not None:
            return val
        cfg = self._load()
        return cfg.get(key) or default

    @property
    def SERVER_PORT(self) -> int:
        return int(self._get("AI_SERVER_PORT", "8092"))

    @property
    def SERVER_HOST(self) -> str:
        return self._get("AI_SERVER_HOST", "0.0.0.0")

    @property
    def JWT_SECRET(self) -> str:
        return self._get("JWT_SECRET", "medilinktunisia2025SecretKeyForJWTTokenGenerationAndValidation")

    @property
    def EUREKA_SERVER(self) -> str:
        return self._get("EUREKA_SERVER", "http://localhost:8761/eureka/")

    @property
    def EUREKA_APP_NAME(self) -> str:
        return "AI-SERVICE"

    @property
    def EUREKA_INSTANCE_PORT(self) -> int:
        return int(self._get("AI_SERVER_PORT", "8092"))

    @property
    def EUREKA_INSTANCE_HOST(self) -> str:
        return self._get("EUREKA_INSTANCE_HOST", "localhost")

    @property
    def LLM_PROVIDER(self) -> str:
        return self._get("LLM_PROVIDER", "gemini")

    @property
    def GEMINI_API_KEY(self) -> str:
        return self._get("GEMINI_API_KEY", "")

    @property
    def LLM_MODEL(self) -> str:
        return self._get("LLM_MODEL", "gemini-3.5-flash")

    @property
    def LLM_MAX_TOKENS(self) -> int:
        return int(self._get("LLM_MAX_TOKENS", "512"))

    @property
    def LLM_MAX_TOKENS_OCR(self) -> int:
        return int(self._get("LLM_MAX_TOKENS_OCR", "65536"))

    @property
    def LLM_TEMPERATURE(self) -> float:
        return float(self._get("LLM_TEMPERATURE", "0.5"))

    @property
    def DATABASE_URL(self) -> str:
        return self._get("DATABASE_URL", "postgresql+asyncpg://postgres:postgres@localhost:5432/medilink_ai")

    @property
    def CHROMA_DB_PATH(self) -> str:
        return self._get("CHROMA_DB_PATH", "./chroma_db")

    @property
    def API_GATEWAY_URL(self) -> str:
        return self._get("API_GATEWAY_URL", "http://localhost:8765/api")


settings = Settings()
