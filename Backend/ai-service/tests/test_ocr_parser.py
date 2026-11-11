import pytest
from app.ocr.parser import BilanParser


@pytest.fixture
def parser():
    return BilanParser()


class TestParseLine:

    def test_simple_line(self, parser):
        line = "Glycemie 5.2 mmol/l 3.9-6.1"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["test"] == "glycemie"
        assert row["valeur"] == "5.2"
        assert row["unite"] == "mmol/l"
        assert row["reference_min"] == 3.9
        assert row["reference_max"] == 6.1

    def test_line_with_threshold(self, parser):
        line = "CRP <5 mg/l"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["valeur"] == "<5"
        assert row["unite"] == "mg/l"
        assert row["confiance"] == "faible"

    def test_ancien_patient_with_old_value(self, parser):
        line = "Hemoglobine 13.2 12.8 g/dl 11.5-15.5"
        row = parser._parse_line(line, "ancien_patient")
        assert row is not None
        assert row["test"] == "hemoglobine"
        assert row["valeur"] == "13.2"
        assert row["valeur_ancienne"] == "12.8"
        assert row["unite"] == "g/dl"
        assert row["reference_min"] == 11.5
        assert row["reference_max"] == 15.5

    def test_nouveau_patient_ignores_second_number(self, parser):
        line = "Glycemie 5.2 5.0 mmol/l 3.9-6.1"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["valeur"] == "5.2"
        assert row["valeur_ancienne"] is None

    def test_fuzzy_match_ocr_noise(self, parser):
        line = "Hémates 4.91 M/mm3 4.0-5.2"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["test"] == "hematies"

    def test_fuzzy_match_ocr_noise_trailing_digits(self, parser):
        line = "Hémoglobine 16e 12.6 g/dl"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["test"] == "hemoglobine"
        assert row["valeur"] == "16"

    def test_unknown_test_name_heuristic(self, parser):
        line = "Vitamine D 30 ng/ml 20-100"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["test"] == "Vitamine D"
        assert row["valeur"] == "30"
        assert row["unite"] == ""

    def test_garbage_header_line_filtered(self, parser):
        line = "Mahdia Le ns u Passer N 2O07EN"
        result = parser._parse_line(line, "ancien_patient")
        assert result is None

    def test_colon_label_line_filtered(self, parser):
        line = "Coue Panent : 214655"
        result = parser._parse_line(line, "ancien_patient")
        assert result is None

    def test_short_invalid_line(self, parser):
        line = "ab 1"
        result = parser._parse_line(line, "nouveau_patient")
        assert result is None

    def test_line_without_number(self, parser):
        line = "Glycemie normale"
        result = parser._parse_line(line, "nouveau_patient")
        assert result is None

    def test_comma_decimal_value(self, parser):
        line = "Glycemie 5,2 mmol/l 3.9-6.1"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["valeur"] == "5.2"

    def test_unit_alias_mapping(self, parser):
        line = "Cholesterol total 4.40 mmo/l"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["unite"] == "mmol/l"

    def test_reference_range_dash(self, parser):
        line = "Glycemie 5.2 mmol/l 3,9-6,1"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["reference_min"] == 3.9
        assert row["reference_max"] == 6.1

    def test_no_unit_in_line(self, parser):
        line = "Glycemie 5.2"
        row = parser._parse_line(line, "nouveau_patient")
        assert row is not None
        assert row["unite"] == ""
        assert row["reference_text"] is None


class TestParseFull:

    def test_biochimie_full(self, parser):
        text = """REPUBLIQUE TUNISIENNE
LABORATOIRE CENTRAL
BIOCHIMIE
Glycemie 5.2 mmol/l 3.9-6.1
Creatinine 80 umol/l 60-110
Uree 6.5 mmol/l 2.5-8.0
Cholesterol total 4.40 mmol/l 3.5-5.2
Signature du biologiste"""
        result = parser.parse(text)
        assert result["type_bilan"] == "Biochimie"
        assert len(result["resultats"]) == 4
        assert result["resultats"][0]["test"] == "glycemie"
        assert result["resultats"][-1]["test"] == "cholesterol total"
        assert result["resultats"][-1]["valeur"] == "4.40"
        assert result["resultats"][-1]["unite"] == "mmol/l"

    def test_hematologie_full(self, parser):
        text = """HEMATOLOGIE
HEMOGRAMME
Hematies 4.91 M/mm3 4.0-5.2
Hemoglobine 13.2 g/dl 11.5-15.5
Hematocrite 42 % 37-47
VGM 90 fl 80-100"""
        result = parser.parse(text)
        assert result["type_bilan"] == "Hematologie"
        assert len(result["resultats"]) == 4
        assert result["resultats"][1]["test"] == "hemoglobine"

    def test_empty_text(self, parser):
        result = parser.parse("")
        assert len(result["resultats"]) == 0

    def test_only_header_lines(self, parser):
        text = """REPUBLIQUE TUNISIENNE
MINISTERE DE LA SANTE
LABORATOIRE CENTRAL"""
        result = parser.parse(text)
        assert len(result["resultats"]) == 0
