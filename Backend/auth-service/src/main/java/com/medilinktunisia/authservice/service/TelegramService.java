package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.repository.PatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TelegramService {

    @Value("${telegram.bot-token}")
    private String botToken;

    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate;
    private String savedWebhookUrl;

    public TelegramService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    public synchronized Map<String, Object> autoLink(String email) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + email));

        if (patient.getTelegramChatId() != null && !patient.getTelegramChatId().isEmpty()) {
            return Map.of("telegramChatId", patient.getTelegramChatId(), "success", true);
        }

        savedWebhookUrl = getWebhookUrl();
        deleteWebhook(false);

        // Tentative immediate (sans long polling)
        String chatId = pollOnce(patient);
        if (chatId != null) {
            restoreWebhook();
            return Map.of("telegramChatId", chatId, "success", true);
        }

        return Map.of("telegramChatId", "", "success", false,
                "message", "Cliquez sur Verifier, puis envoyez un message au bot, puis cliquez sur Continuer.",
                "webhookDeleted", true);
    }

    public synchronized Map<String, Object> completeLinking(String email) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + email));

        try {
            String chatId = pollOnce(patient);
            if (chatId != null) {
                return Map.of("telegramChatId", chatId, "success", true);
            }
            return Map.of("telegramChatId", "", "success", false,
                    "message", "Aucun message trouve. Envoyez un message au bot puis reessayez.");
        } finally {
            restoreWebhook();
        }
    }

    private String pollOnce(Patient patient) {
        try {
            String base = "https://api.telegram.org/bot" + botToken;
            ResponseEntity<Map> response = restTemplate.getForEntity(base + "/getUpdates", Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Telegram getUpdates failed: {}", response.getStatusCode());
                return null;
            }

            List<Map<String, Object>> updates = (List<Map<String, Object>>) response.getBody().get("result");
            if (updates == null || updates.isEmpty()) {
                log.info("No Telegram updates found");
                return null;
            }

            Map<String, Object> lastUpdate = updates.get(updates.size() - 1);
            Map<String, Object> message = (Map<String, Object>) lastUpdate.get("message");
            if (message == null) {
                log.info("No message in last update");
                return null;
            }

            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            if (chat == null) return null;

            Object chatIdObj = chat.get("id");
            String chatId = chatIdObj != null ? String.valueOf(chatIdObj) : null;

            if (chatId != null) {
                patient.setTelegramChatId(chatId);
                patientRepository.save(patient);
                log.info("Telegram linked for patient {} with chatId {}", patient.getEmail(), chatId);
            }

            return chatId;
        } catch (Exception e) {
            log.error("Error polling Telegram: {}", e.getMessage());
            return null;
        }
    }

    private String getWebhookUrl() {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/getWebhookInfo";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                if (result != null) {
                    Object urlObj = result.get("url");
                    String u = urlObj != null ? urlObj.toString() : "";
                    return u.isEmpty() || u.equals("null") ? null : u;
                }
            }
        } catch (Exception e) {
            log.warn("Could not get webhook info: {}", e.getMessage());
        }
        return null;
    }

    private void deleteWebhook(boolean dropPending) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/deleteWebhook";
            if (dropPending) url += "?drop_pending_updates=true";
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, null, Map.class);
            log.info("Telegram webhook deleted (dropPending={})", dropPending);
        } catch (Exception e) {
            log.warn("Could not delete webhook: {}", e.getMessage());
        }
    }

    private void restoreWebhook() {
        if (savedWebhookUrl == null) return;
        try {
            String encoded = URLEncoder.encode(savedWebhookUrl, StandardCharsets.UTF_8);
            String url = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + encoded;
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, null, Map.class);
            if (resp.getBody() != null && Boolean.FALSE.equals(resp.getBody().get("ok"))) {
                log.warn("Could not restore webhook: {} - {}", resp.getBody().get("error_code"), resp.getBody().get("description"));
                log.warn("n8n Telegram workflow may need re-activation. Open n8n UI, Deactivate/Activate the Telegram Trigger.");
            } else {
                log.info("Telegram webhook restored to: {}", savedWebhookUrl);
            }
            savedWebhookUrl = null;
        } catch (Exception e) {
            log.warn("Could not restore webhook: {}", e.getMessage());
        }
    }
}
