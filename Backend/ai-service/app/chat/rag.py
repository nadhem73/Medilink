import logging
import chromadb
from chromadb.config import Settings as ChromaSettings
from chromadb.utils import embedding_functions
from app.config import settings

logger = logging.getLogger(__name__)

MEDICAL_KNOWLEDGE = [
    {
        "specialty": "Neurologue",
        "symptoms": "migraine mal de tête vertige étourdissement céphalée",
        "info": "Les maux de tête fréquents, migraines, vertiges ou étourdissements peuvent nécessiter l'avis d'un neurologue. Consultez si les symptômes persistent ou s'aggravent.",
    },
    {
        "specialty": "Cardiologue",
        "symptoms": "douleur poitrine coeur palpitation essoufflement thorax",
        "info": "Les douleurs thoraciques, palpitations ou essoufflements doivent être évalués par un cardiologue. En cas de douleur intense, appelez les urgences.",
    },
    {
        "specialty": "Dermatologue",
        "symptoms": "peau éruption démangeaison rougeur bouton eczéma psoriasis",
        "info": "Les problèmes de peau comme les éruptions, démangeaisons ou rougeurs persistantes relèvent d'un dermatologue.",
    },
    {
        "specialty": "Gastro-entérologue",
        "symptoms": "estomac ventre digestion nausée vomissement diarrhée constipation",
        "info": "Les troubles digestifs chroniques, nausées, diarrhées ou constipations doivent être examinés par un gastro-entérologue.",
    },
    {
        "specialty": "Orthopédiste",
        "symptoms": "os articulation fracture entorse muscle tendon dos",
        "info": "Les douleurs articulaires, fractures, entorses ou problèmes de dos sont traités par un orthopédiste.",
    },
    {
        "specialty": "Ophtalmologue",
        "symptoms": "oeil yeux vision vue lunette conjonctivite",
        "info": "Les problèmes de vision, douleurs oculaires ou conjonctivites nécessitent un examen par un ophtalmologue.",
    },
    {
        "specialty": "ORL",
        "symptoms": "oreille nez gorge audition sinus rhume grippe",
        "info": "Les infections ORL, problèmes d'audition, sinusites ou rhinites sont pris en charge par un ORL.",
    },
    {
        "specialty": "Pédiatre",
        "symptoms": "enfant bébé nourrisson pédiatrie vaccin fièvre enfant",
        "info": "Les enfants et nourrissons doivent être suivis par un pédiatre pour les maladies infantiles, vaccins et suivi de croissance.",
    },
    {
        "specialty": "Gynécologue",
        "symptoms": "grossesse enceinte accouchement fertilité contraception règles",
        "info": "Le suivi gynécologique, la grossesse, la contraception et les problèmes de fertilité relèvent d'un gynécologue.",
    },
    {
        "specialty": "Psychologue",
        "symptoms": "anxiété dépression stress moral tristesse psychologie sommeil",
        "info": "Les troubles de l'humeur, l'anxiété, la dépression ou le stress chronique peuvent être pris en charge par un psychologue.",
    },
    {
        "specialty": "Pneumologue",
        "symptoms": "poumon respiration asthme bronchite toux essoufflement",
        "info": "Les problèmes respiratoires, l'asthme, les bronchites chroniques ou la toux persistante nécessitent un pneumologue.",
    },
    {
        "specialty": "Endocrinologue",
        "symptoms": "diabète sucre thyroïde hormone poids obésité métabolisme",
        "info": "Les problèmes de thyroïde, diabète, obésité ou troubles hormonaux sont traités par un endocrinologue.",
    },
    {
        "specialty": "Rhumatologue",
        "symptoms": "articulation rhumatisme arthrite arthrose dos inflammation",
        "info": "Les douleurs articulaires chroniques, rhumatismes ou arthroses sont pris en charge par un rhumatologue.",
    },
    {
        "specialty": "Néphrologue",
        "symptoms": "rein urine vessie infection urinaire calcul rénal",
        "info": "Les problèmes rénaux, infections urinaires récurrentes ou calculs rénaux nécessitent un néphrologue.",
    },
    {
        "specialty": "Allergologue",
        "symptoms": "allergie éternuement rhinite urticaire allergène",
        "info": "Les allergies, rhinites allergiques ou urticaires sont diagnostiquées par un allergologue.",
    },
    {
        "specialty": "Hématologue",
        "symptoms": "sang anémie hémoglobine coagulation globule",
        "info": "Les maladies du sang, anémies ou troubles de la coagulation sont pris en charge par un hématologue.",
    },
    {
        "specialty": "Oncologue",
        "symptoms": "cancer tumeur oncologie chimiothérapie radiothérapie",
        "info": "Les cancers et tumeurs nécessitent un suivi par un oncologue.",
    },
    {
        "specialty": "Dentiste",
        "symptoms": "dent dentaire gencive carie bouche mâchoire",
        "info": "Les problèmes dentaires, caries, douleurs aux gencives ou à la mâchoire sont traités par un dentiste.",
    },
    {
        "specialty": "Urgences",
        "symptoms": "urgence grave détresse inconscience hémorragie blessure grave accident",
        "info": "En cas d'urgence médicale, appelez immédiatement le SAMU (190 en Tunisie) ou rendez-vous aux urgences.",
    },
]


class MedicalRAG:
    def __init__(self):
        self.client = chromadb.Client(ChromaSettings(
            persist_directory=settings.CHROMA_DB_PATH,
            is_persistent=True,
        ))
        self.embedder = embedding_functions.DefaultEmbeddingFunction()
        self.collection_name = "medical_knowledge"
        self._init_collection()

    def _init_collection(self):
        try:
            self.collection = self.client.get_collection(self.collection_name)
        except (ValueError, chromadb.errors.NotFoundError):
            self.collection = self.client.create_collection(self.collection_name)
            self._seed_data()

    def _seed_data(self):
        texts = [doc["info"] for doc in MEDICAL_KNOWLEDGE]
        metadatas = [
            {"specialty": doc["specialty"], "symptoms": doc["symptoms"]}
            for doc in MEDICAL_KNOWLEDGE
        ]
        ids = [f"med_{i}" for i in range(len(MEDICAL_KNOWLEDGE))]
        self.collection.add(
            documents=texts,
            metadatas=metadatas,
            ids=ids,
        )

    def search(self, query: str, n_results: int = 3) -> list[dict]:
        results = self.collection.query(
            query_texts=[query],
            n_results=n_results,
        )
        docs = []
        if results["documents"]:
            for i, doc in enumerate(results["documents"][0]):
                docs.append({
                    "content": doc,
                    "specialty": results["metadatas"][0][i]["specialty"],
                    "symptoms": results["metadatas"][0][i]["symptoms"],
                })
        return docs


rag = MedicalRAG()
