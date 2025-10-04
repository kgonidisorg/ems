package com.ecogrid.ems.device.controller;

import com.ecogrid.ems.device.dto.SiteRequest;
import com.ecogrid.ems.device.dto.SiteResponse;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.service.SiteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Site management endpoints
 */
@RestController
@RequestMapping("/api/v1/sites")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SiteController {

    private static final Logger logger = LoggerFactory.getLogger(SiteController.class);

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    /**
     * Create a new site
     */
    @PostMapping
    public ResponseEntity<?> createSite(@Valid @RequestBody SiteRequest request) {
        try {
            SiteResponse siteResponse = siteService.createSite(request);
            logger.info("Site created successfully: {}", siteResponse.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(siteResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("Site creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Site creation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create site"));
        }
    }

    /**
     * Get site by ID
     */
    @GetMapping("/{siteId}")
    public ResponseEntity<?> getSiteById(@PathVariable Long siteId) {
        try {
            Optional<SiteResponse> siteResponse = siteService.getSiteById(siteId);
            if (siteResponse.isPresent()) {
                return ResponseEntity.ok(siteResponse.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get site by ID: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve site"));
        }
    }

    /**
     * Get all sites with pagination
     */
    @GetMapping
    public ResponseEntity<?> getAllSites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<SiteResponse> sitePage;
            if (search != null && !search.trim().isEmpty()) {
                sitePage = siteService.searchSitesByName(search.trim(), pageable);
            } else {
                sitePage = siteService.getAllSites(pageable);
            }

            return ResponseEntity.ok(Map.of(
                    "content", sitePage.getContent(),
                    "totalElements", sitePage.getTotalElements(),
                    "totalPages", sitePage.getTotalPages(),
                    "currentPage", sitePage.getNumber(),
                    "size", sitePage.getSize(),
                    "hasNext", sitePage.hasNext(),
                    "hasPrevious", sitePage.hasPrevious()
            ));
        } catch (Exception e) {
            logger.error("Failed to get sites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve sites"));
        }
    }

    /**
     * Get sites by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getSitesByStatus(@PathVariable String status) {
        try {
            Site.SiteStatus siteStatus = Site.SiteStatus.valueOf(status.toUpperCase());
            List<SiteResponse> sites = siteService.getSitesByStatus(siteStatus);
            return ResponseEntity.ok(sites);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid site status: " + status));
        } catch (Exception e) {
            logger.error("Failed to get sites by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve sites"));
        }
    }

    /**
     * Update site
     */
    @PutMapping("/{siteId}")
    public ResponseEntity<?> updateSite(@PathVariable Long siteId, 
                                       @Valid @RequestBody SiteRequest request) {
        try {
            SiteResponse siteResponse = siteService.updateSite(siteId, request);
            logger.info("Site updated successfully: {}", siteResponse.name());
            return ResponseEntity.ok(siteResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("Site update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Site update failed for ID: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update site"));
        }
    }

    /**
     * Delete site
     */
    @DeleteMapping("/{siteId}")
    public ResponseEntity<?> deleteSite(@PathVariable Long siteId) {
        try {
            siteService.deleteSite(siteId);
            logger.info("Site deleted successfully: {}", siteId);
            return ResponseEntity.ok(Map.of("message", "Site deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Site deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Site deletion blocked: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Site deletion failed for ID: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete site"));
        }
    }

    /**
     * Get site statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getSiteStatistics() {
        try {
            SiteService.SiteStatistics statistics = siteService.getSiteStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get site statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve site statistics"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "device-service-sites",
                "timestamp", System.currentTimeMillis()
        ));
    }
}