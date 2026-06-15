"""
Nettoyage et standardisation du CSV des medecins (MediLink Tunisia).

Operations appliquees :
  - suppression des espaces superflus (trim) sur chaque champ
  - emails en minuscules
  - nom / prenom / specialite / hopital en casse normalisee (Title Case)
  - telephones au format unifie : +216 XX XXX XXX
  - statut / role / disponibilite en minuscules
  - tarif force a 3 decimales (ex : 80.000)
  - suppression des doublons (par id puis par email)
  - tri par nom puis prenom
  - sortie en UTF-8 avec fin de ligne standard
"""
import csv
import re
import sys

SRC = "medecins.csv"
DST = "medecins_clean.csv"

# Particules gardees en minuscule au milieu d'un libelle
MINUSCULES = {"de", "du", "des"}


def _cap_mot(mot: str) -> str:
    """Capitalise un mot en gerant traits d'union et apostrophes (d'Enfants)."""
    for sep in ("-", "'", "’"):
        if sep in mot:
            parts = mot.split(sep)
            # une particule elidee d'une lettre reste minuscule : d'Enfants, l'Ariana
            return sep.join(
                p.lower() if i == 0 and len(p) == 1 else p.capitalize()
                for i, p in enumerate(parts)
            )
    return mot.capitalize()


def title_case(value: str) -> str:
    """Title Case en respectant traits d'union, apostrophes, particules et acronymes."""
    mots = []
    for i, mot in enumerate(value.split()):
        # un token entierement en majuscules est un acronyme : on le garde (ORL, CHU...)
        if mot.isupper() and len(mot) >= 2:
            mots.append(mot)
            continue
        bas = mot.lower()
        if i > 0 and bas in MINUSCULES:
            mots.append(bas)
        else:
            mots.append(_cap_mot(bas))
    return " ".join(mots)


def normaliser_tel(value: str) -> str:
    """Reformate un numero tunisien en '+216 XX XXX XXX'."""
    chiffres = re.sub(r"\D", "", value)
    if chiffres.startswith("216"):
        chiffres = chiffres[3:]
    if len(chiffres) == 8:
        return f"+216 {chiffres[0:2]} {chiffres[2:5]} {chiffres[5:8]}"
    return value.strip()  # on laisse tel quel si format inattendu


def normaliser_tarif(value: str) -> str:
    try:
        return f"{float(value.replace(',', '.')):.3f}"
    except ValueError:
        return value.strip()


def main() -> int:
    with open(SRC, newline="", encoding="utf-8") as f:
        lignes = list(csv.DictReader(f))

    vus_id, vus_email, propres = set(), set(), []
    for row in lignes:
        row = {k: (v or "").strip() for k, v in row.items()}

        row["email"] = row["email"].lower()
        row["nom"] = title_case(row["nom"])
        row["prenom"] = title_case(row["prenom"])
        row["specialite"] = title_case(row["specialite"])
        row["hopital"] = title_case(row["hopital"])
        row["telephone"] = normaliser_tel(row["telephone"])
        row["statut"] = row["statut"].lower()
        row["role"] = row["role"].lower()
        row["disponibilite"] = row["disponibilite"].lower()
        row["tarif"] = normaliser_tarif(row["tarif"])

        if row["id"] in vus_id or row["email"] in vus_email:
            continue  # doublon ignore
        vus_id.add(row["id"])
        vus_email.add(row["email"])
        propres.append(row)

    propres.sort(key=lambda r: (r["nom"].lower(), r["prenom"].lower()))

    entetes = lignes[0].keys()
    with open(DST, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=entetes)
        writer.writeheader()
        writer.writerows(propres)

    print(f"{len(propres)} lignes propres ecrites dans {DST} "
          f"({len(lignes) - len(propres)} doublon(s) supprime(s)).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
