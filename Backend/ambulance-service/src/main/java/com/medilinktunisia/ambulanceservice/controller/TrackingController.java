package com.medilinktunisia.ambulanceservice.controller;

import com.medilinktunisia.ambulanceservice.model.dto.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Contrôleur WebSocket pour le tracking en temps réel
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    @MessageMapping("/ambulance/location")
    @SendTo("/topic/ambulance/locations")
    public LocationUpdateRequest broadcastLocation(LocationUpdateRequest location) {
        log.debug("Broadcasting location update for ambulance: {}", location.getAmbulanceId());
        return location;
    }

    @MessageMapping("/emergency/status")
    @SendTo("/topic/emergency/updates")
    public String broadcastEmergencyUpdate(String message) {
        log.debug("Broadcasting emergency update: {}", message);
        return message;
    }
}
