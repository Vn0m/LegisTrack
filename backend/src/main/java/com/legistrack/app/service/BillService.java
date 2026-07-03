package com.legistrack.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legistrack.app.dto.BillSummaryDto;
import com.legistrack.app.dto.SearchResponseDto;
import com.legistrack.app.dto.SemanticSearchResponseDto;
import com.legistrack.app.exception.UpstreamServiceException;
import com.legistrack.app.model.Bill;
import com.legistrack.app.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BillService {
    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    private static final ZoneId ALBANY = ZoneId.of("America/New_York");
    private static final Duration SEARCH_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration BILL_CACHE_TTL = Duration.ofHours(24);
    private static final Duration SEMANTIC_CACHE_TTL = Duration.ofHours(1);
    private static final int SEARCH_LIMIT = 20;
    private static final int SEMANTIC_LIMIT = 20;

    private final RestClient nySenate;
    private final String apiKey;
    private final CacheService cacheService;
    private final ObjectMapper mapper;
    private final BillRepository billRepository;
    private final BillEmbeddingService embeddingService;

    public BillService(@Qualifier("nySenateRestClient") RestClient nySenate,
                       @Value("${app.nysenate.apiKey}") String apiKey,
                       CacheService cacheService,
                       BillRepository billRepository,
                       ObjectMapper mapper,
                       BillEmbeddingService embeddingService) {
        this.nySenate = nySenate;
        this.apiKey = apiKey;
        this.cacheService = cacheService;
        this.billRepository = billRepository;
        this.mapper = mapper;
        this.embeddingService = embeddingService;
    }

    public SearchResponseDto search(String query, String year, String chamber, String status, String committee) {
        Integer sessionYear = parseSessionYear(year);

        List<Bill> apiBills = parseSearchItems(fetchSearchFromApi(query, sessionYear));
        ingestNewBills(apiBills);

        List<Bill> localBills = billRepository.searchBills(
            blankToNull(query), sessionYear, blankToNull(chamber), blankToNull(status), blankToNull(committee));

        return new SearchResponseDto(
            apiBills.stream().map(BillSummaryDto::from).toList(),
            localBills.stream().map(BillSummaryDto::from).toList());
    }

    public SemanticSearchResponseDto semanticSearch(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("q is required");
        }
        String normalized = query.trim();
        String cacheKey = "semantic:" + normalized.toLowerCase();
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            try {
                return mapper.readValue(cached.get(), SemanticSearchResponseDto.class);
            } catch (JsonProcessingException e) {
                logger.warn("Discarding unreadable semantic cache entry for '{}'", normalized);
            }
        }

        float[] queryEmbedding = embeddingService.generateEmbedding(normalized);
        List<BillSummaryDto> results = billRepository
            .findSimilarBills(BillEmbeddingService.toVectorLiteral(queryEmbedding), SEMANTIC_LIMIT)
            .stream().map(BillSummaryDto::from).toList();

        SemanticSearchResponseDto response = new SemanticSearchResponseDto(results);
        try {
            cacheService.set(cacheKey, mapper.writeValueAsString(response), SEMANTIC_CACHE_TTL);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache semantic results for '{}'", normalized);
        }
        return response;
    }

    public JsonNode getBill(String year, String billId) {
        String basePrintNoStr = billId + "-" + year;
        String cacheKey = "bill:" + basePrintNoStr;
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            try {
                return mapper.readTree(cached.get());
            } catch (JsonProcessingException e) {
                logger.warn("Discarding unreadable bill cache entry for {}", basePrintNoStr);
            }
        }

        JsonNode apiResult = nySenate.get()
            .uri(uriBuilder -> uriBuilder.path("/bills/{year}/{billId}")
                .queryParam("view", "with_refs")
                .queryParam("key", apiKey)
                .build(year, billId))
            .retrieve()
            .body(JsonNode.class);
        if (apiResult == null) {
            throw new UpstreamServiceException("Empty response from NY Senate API");
        }

        cacheService.set(cacheKey, apiResult.toString(), BILL_CACHE_TTL);
        upsertFromApi(apiResult.path("result"));
        return apiResult;
    }

    public String fetchFreshBillStatus(String year, String billId) {
        JsonNode apiResult = nySenate.get()
            .uri(uriBuilder -> uriBuilder.path("/bills/{year}/{billId}")
                .queryParam("key", apiKey)
                .build(year, billId))
            .retrieve()
            .body(JsonNode.class);
        if (apiResult == null) {
            return null;
        }
        JsonNode statusNode = apiResult.path("result").path("status").path("statusDesc");
        return statusNode.isMissingNode() ? null : statusNode.asText();
    }

    private JsonNode fetchSearchFromApi(String query, Integer sessionYear) {
        String cacheKey = "search:" + query + ":" + (sessionYear == null ? "" : sessionYear);
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            try {
                return mapper.readTree(cached.get());
            } catch (JsonProcessingException e) {
                logger.warn("Discarding unreadable search cache entry for '{}'", query);
            }
        }

        String path = sessionYear == null ? "/bills/search" : "/bills/" + sessionYear + "/search";
        JsonNode node = nySenate.get()
            .uri(uriBuilder -> uriBuilder.path(path)
                .queryParam("term", query)
                .queryParam("limit", SEARCH_LIMIT)
                .queryParam("key", apiKey)
                .build())
            .retrieve()
            .body(JsonNode.class);
        if (node == null) {
            throw new UpstreamServiceException("Empty response from NY Senate API");
        }
        cacheService.set(cacheKey, node.toString(), SEARCH_CACHE_TTL);
        return node;
    }

    private List<Bill> parseSearchItems(JsonNode apiNode) {
        List<Bill> bills = new ArrayList<>();
        JsonNode items = apiNode.path("result").path("items");
        if (!items.isArray()) {
            return bills;
        }
        for (JsonNode item : items) {
            JsonNode result = item.path("result");
            Bill bill = parseBill(result.isMissingNode() ? item : result);
            if (!bill.getBasePrintNoStr().isBlank() && !bill.getTitle().isBlank()) {
                bills.add(bill);
            }
        }
        return bills;
    }

    private void ingestNewBills(List<Bill> bills) {
        if (bills.isEmpty()) {
            return;
        }
        try {
            Set<String> keys = bills.stream().map(Bill::getBasePrintNoStr).collect(Collectors.toSet());
            Set<String> existing = billRepository.findByBasePrintNoStrIn(keys).stream()
                .map(Bill::getBasePrintNoStr).collect(Collectors.toSet());
            List<Bill> fresh = bills.stream()
                .filter(b -> !existing.contains(b.getBasePrintNoStr()))
                .toList();
            if (fresh.isEmpty()) {
                return;
            }
            List<Bill> saved = billRepository.saveAll(fresh);
            saved.forEach(embeddingService::generateAndStoreEmbedding);
            logger.info("Ingested {} new bills from search results", saved.size());
        } catch (DataAccessException e) {
            logger.warn("Failed to ingest search results: {}", e.getMessage());
        }
    }

    private void upsertFromApi(JsonNode billData) {
        try {
            Bill parsed = parseBill(billData);
            if (parsed.getBasePrintNoStr().isBlank()) {
                return;
            }
            Bill bill = billRepository.findByBasePrintNoStr(parsed.getBasePrintNoStr())
                .map(existing -> refresh(existing, parsed))
                .orElse(parsed);
            Bill saved = billRepository.save(bill);
            if (!Boolean.TRUE.equals(billRepository.hasEmbedding(saved.getId()))) {
                embeddingService.generateAndStoreEmbedding(saved);
            }
        } catch (DataAccessException e) {
            logger.warn("Failed to persist bill from API response: {}", e.getMessage());
        }
    }

    private Bill refresh(Bill existing, Bill latest) {
        existing.setTitle(latest.getTitle());
        existing.setSummary(latest.getSummary());
        if (latest.getMemo() != null && !latest.getMemo().isBlank()) {
            existing.setMemo(latest.getMemo());
        }
        existing.setStatus(latest.getStatus());
        existing.setCommitteeName(latest.getCommitteeName());
        existing.setSponsorName(latest.getSponsorName());
        existing.setUpdatedAt(OffsetDateTime.now());
        return existing;
    }

    private Bill parseBill(JsonNode billData) {
        String basePrintNoStr = billData.path("basePrintNoStr").asText("");
        String title = billData.path("title").asText("");
        String summary = billData.path("summary").asText("");

        String memo = billData.path("amendments").path("items")
            .path(billData.path("activeVersion").asText(""))
            .path("memo").asText("");

        String rawChamber = billData.path("billType").path("chamber").asText("");
        String chamber = rawChamber.isBlank()
            ? (basePrintNoStr.startsWith("S") ? "Senate" : "Assembly")
            : ("SENATE".equalsIgnoreCase(rawChamber) ? "Senate" : "Assembly");

        Integer sessionYear = billData.path("session").isInt() ? billData.path("session").asInt() : null;
        String sponsorName = billData.path("sponsor").path("member").path("fullName").asText("");
        String status = billData.path("status").path("statusDesc").asText("");
        String committeeName = billData.path("status").path("committeeName").asText("");
        OffsetDateTime publishedDate = parsePublishedDate(billData.path("publishedDateTime").asText(""));

        Bill bill = new Bill(basePrintNoStr, title, blankToNull(summary), blankToNull(memo),
            chamber, sessionYear, blankToNull(sponsorName), blankToNull(status), publishedDate);
        bill.setCommitteeName(blankToNull(committeeName));
        return bill;
    }

    private Integer parseSessionYear(String year) {
        if (year == null || year.isBlank()) {
            return null;
        }
        String trimmed = year.trim();
        if (!trimmed.matches("\\d{4}")) {
            throw new IllegalArgumentException("year must be a 4-digit number");
        }
        int y = Integer.parseInt(trimmed);
        return (y % 2 == 0) ? y - 1 : y;
    }

    private OffsetDateTime parsePublishedDate(String value) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value).atZone(ALBANY).toOffsetDateTime();
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(value);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
