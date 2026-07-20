import logging
import json
import re
from app.llm_client import LLMClient

logger = logging.getLogger(__name__)

OCR_SYSTEM_PROMPT = """Tu es un assistant médical spécialisé dans l'extraction de données de bilans médicaux tunisiens.

Analyse cette image de bilan et retourne UNIQUEMENT un JSON valide, sans texte avant ou après.

Structure attendue :
{
  "type_bilan": "Hématologie" | "Biochimie" | "Autre",
  "format": "nouveau_patient" | "ancien_patient",
  "date_bilan": "YYYY-MM-DD" ou null si non trouvé,
  "laboratoire": "nom du laboratoire" ou null,
  "resultats": [
    {
      "test": "Nom exact du test en français",
      "valeur": "12.5",
      "unite": "g/dL",
      "reference_min": 11.5,
      "reference_max": 15.5,
      "reference_text": "11.5 - 15.5" ou null,
      "valeur_ancienne": null,
      "date_ancienne": null,
      "confiance": "haute"
    }
  ]
}

RÈGLES IMPORTANTES :
1. Si c'est un format "nouveau_patient", laisse valeur_ancienne et date_ancienne à null.
2. Si c'est "ancien_patient", remplis valeur_ancienne et date_ancienne si visibles.
3. Ne invente PAS de données. Utilise null pour les champs non visibles.
4. Sois précis sur le nom des tests en français (ex: "Globules Rouges", "Hémoglobine", "Hématocrite").
5. Pour les valeurs de référence, si c'est une plage "X - Y", extrais X, Y comme nombres ET mets le texte complet dans reference_text (ex: "11.5 - 15.5").
6. Si la valeur de référence est un texte comme "Négatif", "< 0.5" ou "> 10", mets reference_min et reference_max à null et le texte dans reference_text.
7. Le champ "valeur" doit être une chaîne pour préserver les formats comme "<5" ou ">100".
8. **IMPORTANT - Séparateur de milliers** : N'utilise JAMAIS d'espace comme séparateur de milliers. Écris "1000" pas "1 000". Écris "100000" pas "100 000". Utilise le point comme séparateur décimal uniquement.
9. Détecte automatiquement si le bilan est pour un nouveau patient (que des valeurs actuelles) ou un ancien patient (avec anciennes valeurs).
10. Ajoute toujours le champ "confiance" avec la valeur "haute"."""


class OcrService:
    def __init__(self, llm_client: LLMClient):
        self.llm_client = llm_client

    async def extract_bilan_data(self, base64_image: str) -> dict:
        try:
            result_text = await self.llm_client.generate_with_image(
                system_prompt=OCR_SYSTEM_PROMPT,
                base64_image=base64_image,
            )

            json_data = self._extract_json(result_text)
            if json_data is None:
                return {
                    "success": False,
                    "error": "L'IA n'a pas pu extraire les données du bilan. Format de réponse invalide.",
                    "data": None,
                }

            validated = self._validate_data(json_data)
            return {
                "success": True,
                "error": None,
                "data": validated,
            }

        except Exception as e:
            logger.error("OCR extraction failed: %s", e)
            return {
                "success": False,
                "error": f"Erreur lors du traitement OCR: {str(e)}",
                "data": None,
            }

    def _extract_json(self, text: str) -> dict | None:
        if not text:
            return None

        text = text.strip()

        json_match = re.search(r"```(?:json|JSON)?\s*(\{.*\})\s*```", text, re.DOTALL)
        if json_match:
            try:
                return json.loads(json_match.group(1))
            except json.JSONDecodeError:
                pass

        brace_start = text.find("{")
        brace_end = text.rfind("}")
        if brace_start != -1 and brace_end > brace_start:
            candidate = text[brace_start : brace_end + 1]
            try:
                return json.loads(candidate)
            except json.JSONDecodeError:
                logger.warning("Brace extraction failed for JSON (length=%d). First 200: %s",
                               len(candidate), candidate[:200])

        try:
            return json.loads(text)
        except json.JSONDecodeError:
            logger.warning("Failed to parse Gemini response as JSON. Full response (%d chars): %s",
                           len(text), text)
            # Tentative de réparation : extraire entre premier { et dernier } 
            # et essayer avec des crochets fermants ajoutés si le JSON est tronqué
            try:
                brace_start = text.find("{")
                if brace_start != -1:
                    partial = text[brace_start:]
                    # Ajouter fermetures manquantes par tentative
                    for closer in ["}", "}]", "}]}", "}]}]"]:
                        try:
                            return json.loads(partial + closer)
                        except json.JSONDecodeError:
                            continue
            except Exception:
                pass
            return None

    def _validate_data(self, data: dict) -> dict:
        validated = {
            "type_bilan": data.get("type_bilan"),
            "format": data.get("format", "nouveau_patient"),
            "date_bilan": data.get("date_bilan"),
            "laboratoire": data.get("laboratoire"),
            "resultats": [],
        }

        resultats = data.get("resultats", [])
        if isinstance(resultats, list):
            for item in resultats:
                if isinstance(item, dict) and item.get("test"):
                    confiance = self._compute_confiance(item)

                    validated["resultats"].append(
                        {
                            "test": item.get("test"),
                            "valeur": item.get("valeur"),
                            "unite": item.get("unite"),
                            "reference_min": self._to_float(item.get("reference_min")),
                            "reference_max": self._to_float(item.get("reference_max")),
                            "reference_text": item.get("reference_text"),
                            "valeur_ancienne": item.get("valeur_ancienne"),
                            "date_ancienne": item.get("date_ancienne"),
                            "confiance": confiance,
                        }
                    )

        return validated

    def _compute_confiance(self, item: dict) -> str:
        valeur = item.get("valeur")
        ref_min = item.get("reference_min")
        ref_max = item.get("reference_max")
        ref_text = item.get("reference_text")

        # Valeur imprécise (seuil) → confiance faible
        if valeur and any(op in valeur for op in ("<", ">")):
            return "faible"

        # Référence avec plage complète → haute confiance
        if ref_min is not None and ref_max is not None:
            return "haute"

        # Référence textuelle avec seuil (> X, < X) → moyenne
        if ref_text and any(op in ref_text for op in ("<", ">", "≥", "≤")):
            return "moyenne"

        # Référence textuelle uniquement (ex: "Négatif") → moyenne
        if ref_text:
            return "moyenne"

        # Aucune référence → moyenne
        return "moyenne"

    def _to_float(self, value) -> float | None:
        if value is None:
            return None
        try:
            return float(value)
        except (ValueError, TypeError):
            return None
