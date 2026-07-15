import uuid
import logging
import re
from app.llm_client import LLMClient
from app.rag import rag
from app.prompts import INTERNAL_PROMPT
from app.tools import search_doctors, check_availability, book_appointment
from app.database import Conversation, Message, AIAnalysis, AsyncSessionLocal
from app.models import ChatResponse, DoctorInfo

logger = logging.getLogger(__name__)
llm = LLMClient()


def _extract_urgency(text: str) -> str | None:
    match = re.search(
        r"(URGENCE|HAUTE|MOYENNE|BASSE)",
        text.upper(),
    )
    return match.group(1) if match else None


TUNISIAN_CITIES = [
    "Tunis", "Sfax", "Sousse", "Nabeul", "Bizerte", "Gabès", "Kairouan",
    "Ariana", "Ben Arous", "Manouba", "Monastir", "Mahdia", "Médenine",
    "Gafsa", "Tozeur", "Kébili", "Tataouine", "Béja", "Jendouba",
    "Le Kef", "Siliana", "Kasserine", "Zaghouan",
]


def _extract_city(text: str) -> str | None:
    for c in TUNISIAN_CITIES:
        if c.lower() in text.lower():
            return c
    return None


def _extract_specialty(text: str) -> str | None:
    specialties = [
        "Neurologue", "Cardiologue", "Dermatologue", "Gastro-entérologue",
        "Orthopédiste", "Ophtalmologue", "ORL", "Dentiste",
        "Endocrinologue", "Gynécologue", "Pédiatre", "Psychologue",
        "Néphrologue", "Pneumologue", "Rhumatologue", "Hématologue",
        "Oncologue", "Allergologue", "Infectiologue", "Chirurgien",
        "Médecin généraliste", "Radiologue",
    ]
    for s in specialties:
        if s.lower() in text.lower():
            return s
    return None


async def process_message(conversation_id: str | None, message: str, patient_id: int) -> ChatResponse:
    async with AsyncSessionLocal() as session:
        if not conversation_id:
            conversation_id = str(uuid.uuid4())
            conversation = Conversation(id=conversation_id, patient_id=patient_id)
            session.add(conversation)
            await session.commit()

        user_msg = Message(
            conversation_id=conversation_id,
            sender="user",
            content=message,
        )
        session.add(user_msg)
        await session.commit()

        history_messages = await session.execute(
            Message.__table__.select()
            .where(Message.conversation_id == conversation_id)
            .order_by(Message.timestamp)
        )
        rows = history_messages.fetchall()

        history = [
            {"role": "user" if r.sender == "user" else "assistant", "content": r.content}
            for r in rows
        ]

        rag_context = rag.search(message)
        context_text = ""
        if rag_context:
            context_text = "Informations médicales pertinentes :\n" + "\n".join(
                f"- [{c['specialty']}] {c['content']}" for c in rag_context
            )

        full_prompt = INTERNAL_PROMPT + "\n\n" + context_text if context_text else INTERNAL_PROMPT

        answer = await llm.generate(system_prompt=full_prompt, messages=history)

        urgency = _extract_urgency(answer)
        specialty = _extract_specialty(answer)

        analysis = AIAnalysis(
            conversation_id=conversation_id,
            symptoms=message[:500],
            urgency=urgency,
            specialty=specialty,
        )
        session.add(analysis)

        bot_msg = Message(
            conversation_id=conversation_id,
            sender="assistant",
            content=answer,
        )
        session.add(bot_msg)
        await session.commit()

    doctors = []
    if specialty:
        city = _extract_city(message)
        doctors_data = await search_doctors(specialty, city)
        doctors = [DoctorInfo(**d) for d in doctors_data]

    return ChatResponse(
        conversation_id=conversation_id,
        answer=answer,
        urgency_level=urgency,
        recommended_specialty=specialty,
        doctors=doctors,
    )
