package com.medilinktunisia.pharmacyservice.config;

import com.medilinktunisia.pharmacyservice.model.Medicament;
import com.medilinktunisia.pharmacyservice.model.Stock;
import com.medilinktunisia.pharmacyservice.repository.MedicamentRepository;
import com.medilinktunisia.pharmacyservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class StockSeeder implements CommandLineRunner {

    private static final String CSV_PATH = "data/stock_medicaments.csv";
    private static final int BATCH_SIZE = 500;

    private final StockRepository stockRepository;
    private final MedicamentRepository medicamentRepository;

    @Override
    public void run(String... args) {
        long existing = stockRepository.count();
        if (existing > 0) {
            log.info("Stock déjà présent ({} lots) — chargement ignoré.", existing);
            return;
        }

        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        if (!resource.exists()) {
            log.warn("Fichier {} introuvable — aucun stock chargé.", CSV_PATH);
            return;
        }

        List<Stock> buffer = new ArrayList<>(BATCH_SIZE);
        int total = 0;
        int skipped = 0;

        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = parseCsvLine(line);
                if (cols.length < 7 || cols[0].isBlank()) {
                    skipped++;
                    continue;
                }

                Long medicamentId = Long.parseLong(cols[0].trim());
                Medicament medicament = medicamentRepository.findById(medicamentId).orElse(null);
                if (medicament == null) {
                    skipped++;
                    continue;
                }

                LocalDate dateFab = parseDate(cols[3]);
                LocalDate dateExp = parseDate(cols[4]);
                String rawLot = trimToNull(cols[1]);
                if (rawLot == null) {
                    rawLot = String.format("LOT-%02d%02d-%02d%02d",
                            dateFab != null ? dateFab.getYear() % 100 : 0,
                            dateFab != null ? dateFab.getMonthValue() : 0,
                            dateExp != null ? dateExp.getYear() % 100 : 0,
                            dateExp != null ? dateExp.getMonthValue() : 0);
                }
                Stock s = new Stock();
                s.setMedicament(medicament);
                s.setNumeroLot(rawLot);
                s.setQuantiteEnStock(Integer.parseInt(cols[2].trim()));
                s.setDateFabrication(dateFab);
                s.setDateExpiration(dateExp);
                s.setEmplacement(trimToNull(cols[5]));
                s.setDernierReapprovisionnement(parseDate(cols[6]));
                buffer.add(s);

                if (buffer.size() >= BATCH_SIZE) {
                    stockRepository.saveAll(buffer);
                    total += buffer.size();
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                stockRepository.saveAll(buffer);
                total += buffer.size();
            }

            log.info("Stock chargé : {} lots insérés ({} ignorés).", total, skipped);

        } catch (Exception e) {
            log.error("Échec du chargement du stock : {}", e.getMessage(), e);
        }
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>(6);
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
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDate parseDate(String s) {
        String t = trimToNull(s);
        if (t == null) return null;
        try {
            return LocalDate.parse(t);
        } catch (Exception e) {
            return null;
        }
    }
}
