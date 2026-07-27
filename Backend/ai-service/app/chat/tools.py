import logging
import httpx
from app.config import settings

logger = logging.getLogger(__name__)


AUTH_SERVICE_URL = "http://localhost:8084"

SPECIALTY_MAP = {
    "Neurologue": "Neurologie",
    "Cardiologue": "Cardiologie",
    "Dermatologue": "Dermatologie",
    "Gastro-entérologue": "Gastro-enterologie",
    "Orthopédiste": "Orthopedie",
    "Ophtalmologue": "Ophtalmologie",
    "ORL": "ORL",
    "Endocrinologue": "Endocrinologie",
    "Gynécologue": "Gynecologie",
    "Pédiatre": "Pédiatrie",
    "Psychologue": "Psychiatrie",
    "Néphrologue": "Nephrologie",
    "Pneumologue": "Pneumologie",
    "Rhumatologue": "Rhumatologie",
    "Oncologue": "Oncologie",
    "Médecin généraliste": "Medecine Generale",
    "Dentiste": "Dentiste",
    "Hématologue": "Hematologie",
    "Allergologue": "Allergologie",
    "Infectiologue": "Infectiologie",
    "Chirurgien": "Chirurgien",
    "Radiologue": "Radiologie",
}


async def search_doctors(specialty: str, city: str | None = None) -> list[dict]:
    try:
        db_specialty = SPECIALTY_MAP.get(specialty, specialty)
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"{AUTH_SERVICE_URL}/api/auth/doctors",
                timeout=10,
            )
            if resp.status_code != 200:
                return []
            doctors = resp.json()
        result = []
        for d in doctors:
            if d.get("specialty", "").lower() != db_specialty.lower():
                continue
            if city:
                hospital = d.get("hospital", "") or ""
                if city.lower() not in hospital.lower():
                    continue
            result.append({
                "id": d.get("id"),
                "name": f"{d.get('firstName', '')} {d.get('lastName', '')}",
                "specialty": d.get("specialty", specialty),
                "city": d.get("hospital", ""),
                "rating": d.get("rating", "N/A"),
            })
        return result
    except Exception as e:
        logger.error("search_doctors error: %s", e)
        return []


async def check_availability(doctor_id: int) -> list[dict]:
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"{settings.API_GATEWAY_URL}/patients/appointments/available-slots",
                params={"doctorId": doctor_id},
                timeout=10,
            )
            if resp.status_code == 200:
                slots = resp.json()
                return [{"time": s.get("time"), "available": s.get("available", False)} for s in slots]
            return []
    except Exception as e:
        logger.error("check_availability error: %s", e)
        return []


async def book_appointment(doctor_id: int, patient_id: int, date: str) -> dict | None:
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.post(
                f"{settings.API_GATEWAY_URL}/patients/appointments",
                json={
                    "doctorId": doctor_id,
                    "patientId": patient_id,
                    "dateTime": date,
                },
                timeout=10,
            )
            if resp.status_code in (200, 201):
                return resp.json()
            return None
    except Exception as e:
        logger.error("book_appointment error: %s", e)
        return None

