package com.medilinktunisia.prescriptionservice.client;

import com.medilinktunisia.prescriptionservice.dto.StockCheckResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "pharmacy-service", path = "/api/pharmacy")
public interface PharmacyServiceClient {

    @GetMapping("/medicaments/{id}")
    Map<String, Object> getMedicament(@PathVariable("id") Long id);

    @GetMapping("/stock/medicament/{medicamentId}/total")
    Map<String, Object> getStockTotal(@PathVariable("medicamentId") Long medicamentId);

    @PostMapping("/medicaments/stock-check")
    Map<Long, Integer> checkStock(@RequestBody List<Long> medicamentIds);
}
