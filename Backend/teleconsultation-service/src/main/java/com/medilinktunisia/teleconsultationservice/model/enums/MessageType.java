package com.medilinktunisia.teleconsultationservice.model.enums;

public enum MessageType {
    TEXT,           // Message texte
    CHAT,           // Message de chat
    FILE,           // Fichier partagé
    IMAGE,          // Image
    VIDEO_OFFER,    // Offre WebRTC
    VIDEO_ANSWER,   // Réponse WebRTC
    ICE_CANDIDATE,  // Candidat ICE pour WebRTC
    SYSTEM,         // Message système
    JOIN,           // User joined session
    LEAVE           // User left session
}
