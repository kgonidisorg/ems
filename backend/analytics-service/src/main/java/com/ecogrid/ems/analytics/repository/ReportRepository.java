package com.ecogrid.ems.analytics.repository;

import com.ecogrid.ems.analytics.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Report entity
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Find reports by type
     */
    List<Report> findByReportType(String reportType);

    /**
     * Find reports by status
     */
    List<Report> findByStatus(String status);

    /**
     * Find reports by site ID
     */
    List<Report> findBySiteId(Long siteId);

    /**
     * Find reports by created by user
     */
    List<Report> findByCreatedBy(String createdBy);

    /**
     * Find reports created within date range
     */
    @Query("SELECT r FROM Report r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<Report> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find reports by type and status
     */
    List<Report> findByReportTypeAndStatus(String reportType, String status);

    /**
     * Find reports by site ID with pagination
     */
    Page<Report> findBySiteId(Long siteId, Pageable pageable);

    /**
     * Find scheduled reports that are ready for execution
     */
    @Query("SELECT r FROM Report r WHERE r.scheduledFrequency != 'ONCE' AND r.status = 'COMPLETED'")
    List<Report> findScheduledReports();

    /**
     * Count reports by status
     */
    long countByStatus(String status);

    /**
     * Count reports by type
     */
    long countByReportType(String reportType);

    /**
     * Find latest reports by type
     */
    @Query("SELECT r FROM Report r WHERE r.reportType = :reportType ORDER BY r.createdAt DESC")
    List<Report> findLatestByReportType(@Param("reportType") String reportType, Pageable pageable);

    /**
     * Search reports by name (case-insensitive)
     */
    @Query("SELECT r FROM Report r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Report> searchByName(@Param("name") String name, Pageable pageable);
}