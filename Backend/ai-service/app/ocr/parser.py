import re
import logging
import unicodedata
from difflib import SequenceMatcher

logger = logging.getLogger(__name__)


def _strip_accents(s: str) -> str:
    nfkd = unicodedata.normalize("NFKD", s)
    return "".join(c for c in nfkd if not unicodedata.combining(c))


def _normalize_name(s: str) -> str:
    return _strip_accents(s.lower().strip().replace("-", " "))


KNOWN_TESTS = {
    "globules rouges": {"unites": ["/mm3", "t/l", "m/mm3", "10^6/mm3"]},
    "hematies": {"unites": ["/mm3", "t/l", "m/mm3", "10^6/mm3"]},
    "hemoglobine": {"unites": ["g/dl", "g/100ml", "g/l"]},
    "hematocrite": {"unites": ["%"]},
    "vgm": {"unites": ["fl", "um3"]},
    "tcmh": {"unites": ["pg"]},
    "ccmh": {"unites": ["%", "g/dl"]},
    "globules blancs": {"unites": ["/mm3", "g/l", "10^3/mm3"]},
    "leucocytes": {"unites": ["/mm3", "g/l", "10^3/mm3"]},
    "plaquettes": {"unites": ["/mm3", "g/l", "10^3/mm3"]},
    "thrombocytes": {"unites": ["/mm3", "g/l", "10^3/mm3"]},
    "vs": {"unites": ["mm"]},
    "glycemie": {"unites": ["g/l", "mmol/l", "mg/dl"]},
    "creatinine": {"unites": ["mg/l", "umol/l", "mmol/l"]},
    "uree": {"unites": ["g/l", "mmol/l"]},
    "cholesterol total": {"unites": ["g/l", "mmol/l"]},
    "hdl cholesterol": {"unites": ["g/l", "mmol/l"]},
    "ldl cholesterol": {"unites": ["g/l", "mmol/l"]},
    "triglycerides": {"unites": ["g/l", "mmol/l"]},
    "fer serique": {"unites": ["ug/dl", "umol/l"]},
    "acide urique": {"unites": ["mg/l", "umol/l", "mmol/l"]},
    "bilirubine totale": {"unites": ["mg/l", "umol/l"]},
    "bilirubine directe": {"unites": ["mg/l", "umol/l"]},
    "bilirubine indirecte": {"unites": ["mg/l", "umol/l"]},
    "asat": {"unites": ["ui/l", "ukat/l"]},
    "alat": {"unites": ["ui/l", "ukat/l"]},
    "gamma gt": {"unites": ["ui/l", "ukat/l"]},
    "pal": {"unites": ["ui/l", "ukat/l"]},
    "proteines totales": {"unites": ["g/l"]},
    "albumine": {"unites": ["g/l"]},
    "sodium": {"unites": ["mmol/l", "meq/l"]},
    "potassium": {"unites": ["mmol/l", "meq/l"]},
    "calcium": {"unites": ["mg/l", "mmol/l"]},
    "tsh": {"unites": ["uui/ml", "mui/l"]},
    "crp": {"unites": ["mg/l"]},
    "proteine c reactive": {"unites": ["mg/l"]},
    "hemoglobine glycosylee": {"unites": ["%", "mmol/l"]},
    "hba1c": {"unites": ["%", "mmol/l"]},
    "polynucleaires neutrophiles": {"unites": ["%", "/mm3"]},
    "polynucleaires eosinophiles": {"unites": ["%", "/mm3"]},
    "polynucleaires basophiles": {"unites": ["%", "/mm3"]},
    "lymphocytes": {"unites": ["%", "/mm3"]},
    "monocytes": {"unites": ["%", "/mm3"]},
}

KNOWN_UNITS = sorted(
    set(u for info in KNOWN_TESTS.values() for u in info["unites"]),
    key=len,
    reverse=True,
)

UNIT_ALIASES = {
    "mmol/l": ["mmol/l", "mmoli", "mmoi", "mmo", "mmol", "mmo/l", "mmoi/l", "mul", "mmoi"],
    "umol/l": ["umol/l", "umoli", "umo", "umo/l", "umol", "jmoli", "jmol", "wmol", "umolt"],
    "g/l": ["g/l", "g/l."],
    "g/dl": ["g/dl", "g/dl.", "g/100ml"],
    "mg/l": ["mg/l", "mg/l."],
    "mg/dl": ["mg/dl", "mg/dl."],
    "ug/dl": ["ug/dl", "\u00b5g/dl", "ug/dl."],
    "ui/l": ["ui/l", "ui/l.", "uit", "u/l", "ul", "ui", "un"],
    "pg/ml": ["pg/ml", "pg/ml."],
    "ng/dl": ["ng/dl", "ng/dl."],
    "uui/ml": ["uui/ml", "\u00b5ui/ml", "uui/ml."],
    "mui/l": ["mui/l", "mui/l."],
    "%": ["%"],
    "fl": ["fl", "fl."],
    "pg": ["pg", "pg."],
    "mm": ["mm", "mm."],
    "/mm3": ["/mm3", "/mm3.", "mmi", "muni", "mm3"],
    "meq/l": ["meq/l", "meq/l."],
}

RANGE_PATTERN = r"(\d+[.,]?\d*)\s*[-\u2013]\s*(\d+[.,]?\d*)"
RANGE_PAREN_PATTERN = r"[\(\{\[]\s*(\d+[.,]?\d*)\s*[-\u2013]?\s*(\d+[.,]?\d*)\s*[\)\}\]]"
THRESHOLD_PATTERN = r"([<\u2265\u2264>])\s*(\d+[.,]?\d*)"
DATE_STANDARD = r"(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})"
DATE_PREFIX = r"(?:le|du)\s+(\d{1,2})[/\s-]+(\d{1,2})[/\s-]+(\d{2,4})"
YEAR_MONTH_PATTERN = r"(\d{4})-(\d{2})-(\d{2})"
NUMBER_PATTERN = r"^\d+[.,]?\d*$"

TRASH_KEYWORDS = [
    "rue", "avenue", "boulevard", "mahdia", "sousse", "tunis", "monastir",
    "sfax", "bizerte", "nabeul", "kairouan", "gabes", "gafsa", "kasserine",
    "medenine", "kebili", "tozeur", "tataouine", "ben arous", "ariana",
    "manouba", "lac", "berger", "cit\u00e9", "citerne", "imm", "immeuble",
    "residence", "appartement", "b.p.", "bp", "boite postale", "code postal",
    "cin", "n\u00b0", "n\u00b0", "passer", "dossier", "matricule",
    "patient", "nom", "prenom", "age", "sexe", "date naissance",
    "resultat", "analyse", "analyses", "examen", "examens",
    "service", "docteur", "medecin", "biologiste",
    "telephone", "tel", "fax", "email", "adresse",
    "code", "panxnt", "panent", "patien", "ceste",
]

SEPARATOR_PATTERN = re.compile(r"^[-=*_\.\s]{3,}$")


class BilanParser:

    def parse(self, raw_text: str) -> dict:
        lines = self._normalize(raw_text)
        metadata = self._extract_metadata(lines)
        format_type = self._detect_format(lines)
        rows = self._parse_rows(lines, format_type)
        return self._build_output(metadata, format_type, rows)

    def _normalize(self, text: str) -> list[str]:
        text = text.replace("\r\n", "\n").replace("\r", "\n")
        text = text.replace("\ufeff", "")
        text = re.sub(r"[ \t]+", " ", text)
        return [l.strip() for l in text.split("\n") if l.strip()]

    def _extract_date(self, line: str) -> str | None:
        pats = [DATE_STANDARD, DATE_PREFIX, YEAR_MONTH_PATTERN]
        for pat in pats:
            m = re.search(pat, line, re.IGNORECASE)
            if m:
                try:
                    g = m.groups()
                    if len(g) == 3:
                        a, b, c = g[0], g[1], g[2]
                        if len(c) == 4:
                            y, mo, d = c, a, b
                        elif len(a) == 4:
                            y, mo, d = a, b, c
                        else:
                            d, mo, y = a, b, c
                            if len(y) == 2:
                                y = "20" + y
                        return f"{y}-{mo.zfill(2)}-{d.zfill(2)}"
                except Exception:
                    pass
        return None

    def _extract_metadata(self, lines: list[str]) -> dict:
        metadata = {"type_bilan": None, "date_bilan": None, "laboratoire": None}
        labo_keywords = [_normalize_name(kw) for kw in
                         ["laboratoire", "labo", "dr ", "cabinet", "clinique",
                          "hopital", "centre"]]
        bilan_types = {"hematologie": "Hematologie", "biochimie": "Biochimie"}

        for line in lines[:15]:
            norm = _normalize_name(line)
            for kw, val in bilan_types.items():
                if kw in norm:
                    metadata["type_bilan"] = val
            for lk in labo_keywords:
                if norm.startswith(lk):
                    metadata["laboratoire"] = line.strip()
                    break

        for line in lines:
            d = self._extract_date(line)
            if d:
                metadata["date_bilan"] = d
                break
        return metadata

    def _detect_format(self, lines: list[str]) -> str:
        count = 0
        for line in lines:
            for t in line.split():
                c = t.replace(",", ".").strip("<>")
                if re.match(NUMBER_PATTERN, c):
                    count += 1
        return "ancien_patient" if count > 30 else "nouveau_patient"

    def _match_unit(self, token: str) -> str | None:
        key = token.lower().replace(",", ".").strip(".:;,!?")
        for canonical, aliases in UNIT_ALIASES.items():
            if key in aliases or key in [a.strip(".") for a in aliases]:
                return canonical
        for u in KNOWN_UNITS:
            if key == u.lower().strip("."):
                return u
        return None

    def _is_number(self, token: str) -> bool:
        return bool(re.match(NUMBER_PATTERN, token.replace(",", ".")))

    def _starts_with_number(self, token: str) -> bool:
        cleaned = token.replace(",", ".").lstrip("<>")
        return bool(re.match(r"\d+[.,]?\d*", cleaned))

    def _is_threshold(self, token: str) -> bool:
        return bool(re.match(r"^[<>]+\s*\d+[.,]?\d*$", token.strip()))

    def _fuzzy_match_known(self, norm_name: str) -> str | None:
        best_key = None
        best_ratio = 0.0
        for known_key in KNOWN_TESTS:
            ratio = SequenceMatcher(None, norm_name, known_key).ratio()
            if ratio > best_ratio:
                best_ratio = ratio
                best_key = known_key
        return best_key if best_ratio >= 0.72 else None

    def _is_trash_line(self, line: str) -> bool:
        norm = _normalize_name(line)
        if any(kw in norm for kw in TRASH_KEYWORDS):
            return True
        parts = norm.split()
        if len(parts) > 8:
            return True
        if len(line) > 70:
            return True
        return False

    def _parse_rows(self, lines: list[str], format_type: str) -> list[dict]:
        rows = []
        in_results = False

        for line in lines:
            norm = _normalize_name(line)
            lower = line.lower().strip()

            if not in_results:
                if self._is_header_line(norm):
                    continue
                if SEPARATOR_PATTERN.match(line):
                    continue
                if len(line) < 4:
                    continue
                in_results = True

            if len(line) < 4:
                continue
            if SEPARATOR_PATTERN.match(line):
                continue
            if any(kw in lower for kw in ["signature", "valide", "biologiste",
                                           "medecin", "pharmacie"]):
                break
            if lower.startswith(("le biologiste", "le medecin")):
                break
            if self._is_trash_line(line):
                continue

            row = self._parse_line(line, format_type)
            if row:
                rows.append(row)

        return rows

    def _is_header_line(self, norm: str) -> bool:
        headers = ["laboratoire", "labo", "dr ", "cabinet", "patient",
                    "nom", "prenom", "age", "sexe", "service", "biologiste",
                    "medecin", "docteur", "signature", "valide", "resultats",
                    "reference", "unite", "test", "examen", "valeur",
                    "observation", "hemogramme", "formule", "numerotion",
                    "vitesse de sedimentation", "sedime", "hopital",
                    "ministere", "republique", "tunisien", "tunisie",
                    "le biologiste", "le medecin"]
        return any(norm.startswith(_normalize_name(h)) for h in headers)

    def _parse_line(self, line: str, format_type: str) -> dict | None:
        tokens = line.split()
        if len(tokens) < 2:
            return None

        test_name, rest = self._extract_test_name(tokens)
        if not test_name:
            return None

        valeurs, new_rest = self._extract_values(rest, format_type)
        if not valeurs:
            return None

        unite, after_unit = self._extract_unit(new_rest)
        reference = self._extract_reference(after_unit)

        ancien_date = None
        remaining_text = " ".join(new_rest)
        d = self._extract_date(remaining_text)
        if d:
            ancien_date = d

        confiance = self._compute_confiance(valeurs["actuelle"], reference)

        return {
            "test": test_name,
            "valeur": valeurs["actuelle"],
            "unite": unite,
            "reference_min": reference["min"],
            "reference_max": reference["max"],
            "reference_text": reference["text"],
            "valeur_ancienne": valeurs.get("ancienne"),
            "date_ancienne": valeurs.get("date_ancienne") or ancien_date,
            "confiance": confiance,
        }

    def _extract_test_name(self, tokens: list[str]) -> tuple[str | None, list[str]]:
        value_idx = None
        for i, t in enumerate(tokens):
            stripped = t.replace(",", ".").strip("<>")
            if re.match(NUMBER_PATTERN, stripped) or self._is_threshold(t) or self._starts_with_number(t):
                value_idx = i
                break

        if value_idx is None or value_idx == 0:
            return None, tokens

        raw_name_tokens = [t for t in tokens[:value_idx]
                           if not re.match(r"^[^\w\s]+$", t)]
        rest = tokens[value_idx:]

        while raw_name_tokens and re.search(r"\d", raw_name_tokens[-1]):
            raw_name_tokens = raw_name_tokens[:-1]

        raw_name = " ".join(raw_name_tokens).strip(",:;!?#-")
        if len(raw_name) < 3:
            return None, tokens

        norm_name = _normalize_name(raw_name)

        matched = self._fuzzy_match_known(norm_name)
        if matched:
            return matched, rest

        if self._is_trash_name(raw_name, norm_name):
            return None, tokens

        return raw_name, rest

    def _is_trash_name(self, raw_name: str, norm_name: str) -> bool:
        if len(raw_name) > 50:
            return True
        parts = norm_name.split()
        if len(parts) > 6:
            return True
        if any(kw in norm_name for kw in TRASH_KEYWORDS):
            return True
        return False

    def _extract_values(self, tokens: list[str], format_type: str) -> tuple[dict | None, list[str]]:
        valeur = None
        valeur_ancienne = None
        value_indices = []

        for i, token in enumerate(tokens):
            cleaned = token.replace(",", ".").strip("<>")
            if self._is_number(token) or self._is_threshold(token) or self._starts_with_number(token):
                if valeur is None:
                    if self._is_threshold(token):
                        valeur = token
                    elif self._is_number(token):
                        valeur = cleaned
                    else:
                        m = re.match(r"(-?\d+[.,]?\d*)", cleaned)
                        valeur = m.group(1) if m else cleaned
                    value_indices.append(i)
                elif format_type == "ancien_patient" and valeur_ancienne is None:
                    valeur_ancienne = cleaned
                    value_indices.append(i)

        if valeur is None:
            return None, tokens

        rest = [t for i, t in enumerate(tokens) if i not in value_indices]
        return {"actuelle": valeur, "ancienne": valeur_ancienne, "date_ancienne": None}, rest

    def _extract_unit(self, tokens: list[str]) -> tuple[str, list[str]]:
        unit_idx = None
        matched_unit = ""
        for i, token in enumerate(tokens):
            m = self._match_unit(token)
            if m:
                matched_unit = m
                unit_idx = i
                break
        if unit_idx is not None:
            rest = [t for j, t in enumerate(tokens) if j != unit_idx]
        else:
            rest = tokens[:]
        return matched_unit, rest

    def _extract_reference(self, tokens: list[str]) -> dict:
        text = " ".join(tokens)

        m = re.search(RANGE_PATTERN, text, re.IGNORECASE)
        if m:
            try:
                return {
                    "min": float(m.group(1).replace(",", ".")),
                    "max": float(m.group(2).replace(",", ".")),
                    "text": m.group(0),
                }
            except ValueError:
                pass

        m = re.search(RANGE_PAREN_PATTERN, text, re.IGNORECASE)
        if m:
            try:
                return {
                    "min": float(m.group(1).replace(",", ".")),
                    "max": float(m.group(2).replace(",", ".")),
                    "text": m.group(0),
                }
            except ValueError:
                pass

        m = re.search(THRESHOLD_PATTERN, text, re.IGNORECASE)
        if m:
            return {"min": None, "max": None, "text": m.group(0)}

        for token in tokens:
            if _strip_accents(token.lower()) in ("negatif", "positif", "normal", "anormal", "absence", "presence"):
                return {"min": None, "max": None, "text": token}

        rest = text.strip()
        if rest:
            return {"min": None, "max": None, "text": rest}
        return {"min": None, "max": None, "text": None}

    def _compute_confiance(self, valeur: str | None, reference: dict) -> str:
        if not valeur:
            return "moyenne"
        if any(op in valeur for op in ("<", ">")):
            return "faible"
        if reference.get("min") is not None and reference.get("max") is not None:
            return "haute"
        return "moyenne"

    def _build_output(self, metadata: dict, format_type: str, rows: list[dict]) -> dict:
        deduped = self._deduplicate(rows)
        return {
            "type_bilan": metadata.get("type_bilan"),
            "format": format_type,
            "date_bilan": metadata.get("date_bilan"),
            "laboratoire": metadata.get("laboratoire"),
            "resultats": deduped,
        }

    def _deduplicate(self, rows: list[dict]) -> list[dict]:
        seen = set()
        unique = []
        for row in rows:
            key = (row["test"], row["valeur"])
            if key not in seen:
                seen.add(key)
                unique.append(row)
        return unique
