package com.medilinktunisia.pharmacyservice.config;

import com.medilinktunisia.pharmacyservice.model.Medicament;
import com.medilinktunisia.pharmacyservice.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Charge le référentiel des médicaments depuis {@code data/medicaments.csv}
 * (dataset national tunisien) au démarrage du service.
 * <p>
 * Idempotent : si la table {@code medicaments} contient déjà des lignes,
 * le chargement est ignoré. Pour recharger, vider la table au préalable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicamentSeeder implements CommandLineRunner {

    private static final String CSV_PATH = "data/medicaments.csv";
    private static final int BATCH_SIZE = 500;

    private final MedicamentRepository medicamentRepository;

    @Override
    public void run(String... args) {
        long existing = medicamentRepository.count();
        if (existing > 0) {
            log.info("Référentiel médicaments déjà présent ({} lignes) — chargement ignoré.", existing);
            return;
        }

        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        if (!resource.exists()) {
            log.warn("Fichier {} introuvable — aucun médicament chargé.", CSV_PATH);
            return;
        }

        List<Medicament> buffer = new ArrayList<>(BATCH_SIZE);
        int total = 0;
        int skipped = 0;

        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            reader.readLine(); // entête

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] cols = parseCsvLine(line);
                if (cols.length < 9 || cols[0].isBlank()) {
                    skipped++;
                    continue;
                }

                Medicament m = new Medicament();
                m.setName(trimToNull(cols[0]));
                m.setDosage(trimToNull(cols[1]));
                m.setForme(trimToNull(cols[2]));
                m.setPresentation(trimToNull(cols[3]));
                m.setPrice(parseDecimal(cols[4]));
                m.setRemboursement(parseDecimal(cols[5]));
                m.setDci(trimToNull(cols[6]));
                m.setType(trimToNull(cols[7]));
                m.setPrescriptionRequired("true".equalsIgnoreCase(cols[8].trim()));
                buffer.add(m);

                if (buffer.size() >= BATCH_SIZE) {
                    medicamentRepository.saveAll(buffer);
                    total += buffer.size();
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                medicamentRepository.saveAll(buffer);
                total += buffer.size();
            }

            log.info("Référentiel médicaments chargé : {} lignes insérées ({} ignorées).", total, skipped);

        } catch (Exception e) {
            log.error("Échec du chargement du référentiel médicaments : {}", e.getMessage(), e);
        }
    }

    /** Parse une ligne CSV (RFC 4180 simplifié : champs entre guillemets, «""» échappé). */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>(9);
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static BigDecimal parseDecimal(String s) {
        String t = trimToNull(s);
        if (t == null) {
            return null;
        }
        try {
            return new BigDecimal(t.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
