"""Configuration pytest pour les tests ai-service."""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

# Désactiver l'initialisation lourde pour les tests
os.environ.setdefault("GEMINI_API_KEY", "test-key")
os.environ.setdefault("LLM_MODEL", "test-model")
os.environ.setdefault("EUREKA_SERVER", "")
os.environ.setdefault("DATABASE_URL", "sqlite+aiosqlite:///:memory:")
