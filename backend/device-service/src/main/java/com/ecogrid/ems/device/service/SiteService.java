package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.dto.SiteRequest;
import com.ecogrid.ems.device.dto.SiteResponse;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Site management operations
 */
@Service
@Transactional
public class SiteService {

    private static final Logger logger = LoggerFactory.getLogger(SiteService.class);

    private final SiteRepository siteRepository;
    private final DeviceRepository deviceRepository;

    public SiteService(SiteRepository siteRepository, DeviceRepository deviceRepository) {
        this.siteRepository = siteRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * Create a new site
     */
    public SiteResponse createSite(SiteRequest request) {
        // Check if site name already exists
        if (siteRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Site with name '" + request.name() + "' already exists");
        }

        Site site = new Site();
        site.setName(request.name());
        site.setDescription(request.description());
        site.setLocationLat(request.locationLat());
        site.setLocationLng(request.locationLng());
        site.setCapacityMw(request.capacityMw());
        site.setTimezone(request.timezone() != null ? request.timezone() : "UTC");
        site.setAddress(request.address());
        site.setContactPerson(request.contactPerson());
        site.setContactEmail(request.contactEmail());
        site.setContactPhone(request.contactPhone());

        // Set status if provided
        if (request.status() != null && !request.status().isEmpty()) {
            try {
                site.setStatus(Site.SiteStatus.valueOf(request.status().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid site status: " + request.status());
            }
        }

        Site savedSite = siteRepository.save(site);
        logger.info("Created new site: {} (ID: {})", savedSite.getName(), savedSite.getId());

        return mapToSiteResponse(savedSite);
    }

    /**
     * Get site by ID
     */
    @Transactional(readOnly = true)
    public Optional<SiteResponse> getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .map(this::mapToSiteResponse);
    }

    /**
     * Get site by name
     */
    @Transactional(readOnly = true)
    public Optional<SiteResponse> getSiteByName(String name) {
        return siteRepository.findByName(name)
                .map(this::mapToSiteResponse);
    }

    /**
     * Get all sites with pagination
     */
    @Transactional(readOnly = true)
    public Page<SiteResponse> getAllSites(Pageable pageable) {
        Page<Site> sitePage = siteRepository.findAll(pageable);
        List<SiteResponse> siteResponses = sitePage.getContent().stream()
                .map(this::mapToSiteResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(siteResponses, pageable, sitePage.getTotalElements());
    }

    /**
     * Search sites by name
     */
    @Transactional(readOnly = true)
    public Page<SiteResponse> searchSitesByName(String searchTerm, Pageable pageable) {
        Page<Site> sitePage = siteRepository.searchByName(searchTerm, pageable);
        List<SiteResponse> siteResponses = sitePage.getContent().stream()
                .map(this::mapToSiteResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(siteResponses, pageable, sitePage.getTotalElements());
    }

    /**
     * Get sites by status
     */
    @Transactional(readOnly = true)
    public List<SiteResponse> getSitesByStatus(Site.SiteStatus status) {
        return siteRepository.findByStatus(status).stream()
                .map(this::mapToSiteResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update site
     */
    public SiteResponse updateSite(Long siteId, SiteRequest request) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        // Check if new name conflicts with existing site
        if (!site.getName().equals(request.name()) && siteRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Site with name '" + request.name() + "' already exists");
        }

        // Update site fields
        site.setName(request.name());
        site.setDescription(request.description());
        site.setLocationLat(request.locationLat());
        site.setLocationLng(request.locationLng());
        site.setCapacityMw(request.capacityMw());
        site.setTimezone(request.timezone() != null ? request.timezone() : site.getTimezone());
        site.setAddress(request.address());
        site.setContactPerson(request.contactPerson());
        site.setContactEmail(request.contactEmail());
        site.setContactPhone(request.contactPhone());

        // Update status if provided
        if (request.status() != null && !request.status().isEmpty()) {
            try {
                site.setStatus(Site.SiteStatus.valueOf(request.status().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid site status: " + request.status());
            }
        }

        Site updatedSite = siteRepository.save(site);
        logger.info("Updated site: {} (ID: {})", updatedSite.getName(), updatedSite.getId());

        return mapToSiteResponse(updatedSite);
    }

    /**
     * Delete site
     */
    public void deleteSite(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        // Check if site has devices
        long deviceCount = deviceRepository.countBySiteId(siteId);
        if (deviceCount > 0) {
            throw new IllegalStateException("Cannot delete site with " + deviceCount + " devices. Please remove or reassign devices first.");
        }

        siteRepository.delete(site);
        logger.info("Deleted site: {} (ID: {})", site.getName(), site.getId());
    }

    /**
     * Get total active capacity across all sites
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalActiveCapacity() {
        return siteRepository.getTotalActiveCapacity();
    }

    /**
     * Get site statistics
     */
    @Transactional(readOnly = true)
    public SiteStatistics getSiteStatistics() {
        long totalSites = siteRepository.count();
        long activeSites = siteRepository.countByStatus(Site.SiteStatus.ACTIVE);
        long inactiveSites = siteRepository.countByStatus(Site.SiteStatus.INACTIVE);
        long maintenanceSites = siteRepository.countByStatus(Site.SiteStatus.MAINTENANCE);
        long offlineSites = siteRepository.countByStatus(Site.SiteStatus.OFFLINE);
        BigDecimal totalCapacity = getTotalActiveCapacity();

        return new SiteStatistics(totalSites, activeSites, inactiveSites, maintenanceSites, offlineSites, totalCapacity);
    }

    /**
     * Map Site entity to SiteResponse DTO
     */
    private SiteResponse mapToSiteResponse(Site site) {
        long deviceCount = deviceRepository.countBySiteId(site.getId());
        
        return new SiteResponse(
                site.getId(),
                site.getName(),
                site.getDescription(),
                site.getLocationLat(),
                site.getLocationLng(),
                site.getCapacityMw(),
                site.getStatus().name(),
                site.getTimezone(),
                site.getAddress(),
                site.getContactPerson(),
                site.getContactEmail(),
                site.getContactPhone(),
                deviceCount,
                site.getCreatedAt(),
                site.getUpdatedAt()
        );
    }

    /**
     * Site statistics record
     */
    public record SiteStatistics(
            long totalSites,
            long activeSites,
            long inactiveSites,
            long maintenanceSites,
            long offlineSites,
            BigDecimal totalCapacity
    ) {}
}