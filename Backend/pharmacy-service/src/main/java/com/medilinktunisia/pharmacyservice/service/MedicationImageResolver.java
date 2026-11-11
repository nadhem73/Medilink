package com.medilinktunisia.pharmacyservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.pharmacyservice.model.Medicament;
import com.medilinktunisia.pharmacyservice.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationImageResolver {

    private static final String COMMONS_API = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=%s&gsrnamespace=6&prop=imageinfo&iiprop=url&format=json&gsrlimit=20";
    private static final String WIKIPEDIA_API = "https://fr.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=%s&prop=pageimages&format=json&pithumbsize=300&gsrlimit=1";
    private static final long THROTTLE_MS = 200;

    private final MedicamentRepository medicamentRepository;
    private final RestTemplate restTemplate = createRestTemplate();

    private static RestTemplate createRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "MediLinkTunisia/1.0 (medilinktunisia@example.com)");
            return execution.execute(request, body);
        }));
        return rt;
    }
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void resetAll() {
        medicamentRepository.resetImageUrls();
        log.info("Toutes les image_url ont été réinitialisées à null");
    }

    public int resolveAll() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Résolution déjà en cours, ignoré");
            return -1;
        }
        try {
        List<Medicament> missing = medicamentRepository.findByImageUrlIsNull();
        log.info("Résolution d'images pour {} médicaments…", missing.size());

        int resolved = 0;
        int skipped = 0;

        for (int i = 0; i < missing.size(); i++) {
            Medicament m = missing.get(i);

            String term = m.getDci();
            if (term == null || term.isBlank()) {
                skipped++;
                continue;
            }

            try {
                String url = fetchThumbnail(term);
                if (url != null) {
                    m.setImageUrl(url);
                    medicamentRepository.save(m);
                    resolved++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                log.warn("Échec image [{}] {} : {}", i, term, e.getMessage());
                skipped++;
            }

            if (i % 10 == 0 && i > 0) {
                log.info("Progression : {}/{} résolues", resolved, i);
            }

            try { Thread.sleep(THROTTLE_MS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }

        log.info("Résolution terminée : {} images trouvées sur {} tentatives", resolved, missing.size());
        return resolved;
        } finally {
            running.set(false);
        }
    }

    private String fetchThumbnail(String term) throws Exception {
        String packageUrl = fetchCommonsPackageImage(term);
        if (packageUrl != null) return packageUrl;

        return fetchWikipediaPageImage(term);
    }

    private String fetchCommonsPackageImage(String term) throws Exception {
        String searchTerm = "\"" + term.replace("+", " ").replace("/", " ").trim() + "\"";
        String urlStr = String.format(COMMONS_API, URLEncoder.encode(searchTerm, StandardCharsets.UTF_8));
        URI uri = new URI(urlStr);
        String json = restTemplate.getForObject(uri, String.class);
        if (json == null) return null;

        JsonNode root = objectMapper.readTree(json);
        JsonNode pages = root.path("query").path("pages");

        if (pages.isEmpty() || !pages.fieldNames().hasNext()) return null;

        Iterator<String> it = pages.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            JsonNode page = pages.get(key);
            String title = page.path("title").asText("");

            String lower = title.toLowerCase();
            if (lower.contains("chemical") || lower.contains("structure") || lower.contains("molecule")
                    || lower.contains("skeletal") || lower.contains("synth") || lower.contains("ball-and-stick")
                    || lower.contains("from-xtal") || lower.contains("3d") || lower.contains("formula")
                    || lower.contains("substance photo") || lower.contains("crystalline")
                    || lower.endsWith(".svg") || lower.endsWith(".gif")) {
                continue;
            }

            String ext = title.substring(title.lastIndexOf('.') + 1).toLowerCase();
            if (!"jpg".equals(ext) && !"jpeg".equals(ext) && !"png".equals(ext)) continue;

            JsonNode imageInfo = page.path("imageinfo");
            if (imageInfo.isArray() && imageInfo.size() > 0) {
                String imageUrl = imageInfo.get(0).path("url").asText();
                if (!imageUrl.isBlank()) {
                    log.debug("Image boîte Commons pour {} -> {}", term, imageUrl);
                    return imageUrl;
                }
            }
        }

        log.debug("Aucune image boîte Commons pour {}", term);
        return null;
    }

    private String fetchWikipediaPageImage(String term) throws Exception {
        String urlStr = String.format(WIKIPEDIA_API, URLEncoder.encode(term, StandardCharsets.UTF_8));
        URI uri = new URI(urlStr);
        String json = restTemplate.getForObject(uri, String.class);
        if (json == null) return null;

        JsonNode root = objectMapper.readTree(json);
        JsonNode pages = root.path("query").path("pages");

        if (pages.isEmpty() || !pages.fieldNames().hasNext()) {
            log.debug("Aucune page Wikipedia trouvée pour {}", term);
            return null;
        }

        String firstKey = pages.fieldNames().next();
        JsonNode page = pages.get(firstKey);
        JsonNode thumbnail = page.path("thumbnail").path("source");
        if (thumbnail.isMissingNode()) {
            log.debug("Page Wikipedia sans thumbnail pour {} (title={})", term, page.path("title").asText());
            return null;
        }
        String imageUrl = thumbnail.asText();
        log.debug("Image Wikipedia pour {} -> {}", term, imageUrl);
        return imageUrl;
    }
}
