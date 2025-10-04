package com.ecogrid.ems.device.repository;

import com.ecogrid.ems.device.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Site entity operations
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    /**
     * Find site by name
     */
    Optional<Site> findByName(String name);

    /**
     * Check if site exists by name
     */
    boolean existsByName(String name);

    /**
     * Find sites by status
     */
    List<Site> findByStatus(Site.SiteStatus status);

    /**
     * Find sites within a geographic area
     */
    @Query("SELECT s FROM Site s WHERE " +
           "s.locationLat BETWEEN :minLat AND :maxLat AND " +
           "s.locationLng BETWEEN :minLng AND :maxLng")
    List<Site> findSitesInArea(@Param("minLat") BigDecimal minLat,
                               @Param("maxLat") BigDecimal maxLat,
                               @Param("minLng") BigDecimal minLng,
                               @Param("maxLng") BigDecimal maxLng);

    /**
     * Find sites by capacity range
     */
    @Query("SELECT s FROM Site s WHERE s.capacityMw BETWEEN :minCapacity AND :maxCapacity")
    List<Site> findByCapacityRange(@Param("minCapacity") BigDecimal minCapacity,
                                   @Param("maxCapacity") BigDecimal maxCapacity);

    /**
     * Search sites by name containing (case insensitive)
     */
    @Query("SELECT s FROM Site s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Site> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get total capacity across all active sites
     */
    @Query("SELECT COALESCE(SUM(s.capacityMw), 0) FROM Site s WHERE s.status = 'ACTIVE'")
    BigDecimal getTotalActiveCapacity();

    /**
     * Count sites by status
     */
    @Query("SELECT COUNT(s) FROM Site s WHERE s.status = :status")
    long countByStatus(@Param("status") Site.SiteStatus status);

    /**
     * Find sites with devices count
     */
    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.devices WHERE s.id = :siteId")
    Optional<Site> findByIdWithDevices(@Param("siteId") Long siteId);
}