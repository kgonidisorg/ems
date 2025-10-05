package com.ecogrid.ems.device.repository;

import com.ecogrid.ems.device.entity.DeviceTelemetry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for DeviceTelemetry entity
 */
@Repository
public interface DeviceTelemetryRepository extends JpaRepository<DeviceTelemetry, Long> {

    /**
     * Find telemetry data for a device within a time range
     */
    @Query("SELECT dt FROM DeviceTelemetry dt WHERE dt.device.id = :deviceId " +
           "AND dt.timestamp BETWEEN :startTime AND :endTime ORDER BY dt.timestamp DESC")
    List<DeviceTelemetry> findByDeviceIdAndTimestampBetween(
        @Param("deviceId") Long deviceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find latest telemetry data for a device
     */
    @Query("SELECT dt FROM DeviceTelemetry dt WHERE dt.device.id = :deviceId " +
           "ORDER BY dt.timestamp DESC")
    Page<DeviceTelemetry> findLatestByDeviceId(@Param("deviceId") Long deviceId, Pageable pageable);

    /**
     * Find telemetry data for all devices in a site within a time range
     */
    @Query("SELECT dt FROM DeviceTelemetry dt WHERE dt.device.site.id = :siteId " +
           "AND dt.timestamp BETWEEN :startTime AND :endTime ORDER BY dt.timestamp DESC")
    List<DeviceTelemetry> findBySiteIdAndTimestampBetween(
        @Param("siteId") Long siteId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find telemetry data for devices of a specific type within a time range
     */
    @Query("SELECT dt FROM DeviceTelemetry dt WHERE dt.device.deviceType.name = :deviceTypeName " +
           "AND dt.device.site.id = :siteId " +
           "AND dt.timestamp BETWEEN :startTime AND :endTime ORDER BY dt.timestamp DESC")
    List<DeviceTelemetry> findByDeviceTypeAndSiteIdAndTimestampBetween(
        @Param("deviceTypeName") String deviceTypeName,
        @Param("siteId") Long siteId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Delete old telemetry data before a certain date (for cleanup)
     */
    void deleteByTimestampBefore(LocalDateTime cutoffTime);

    /**
     * Count telemetry records for a device within a time range
     */
    @Query("SELECT COUNT(dt) FROM DeviceTelemetry dt WHERE dt.device.id = :deviceId " +
           "AND dt.timestamp BETWEEN :startTime AND :endTime")
    Long countByDeviceIdAndTimestampBetween(
        @Param("deviceId") Long deviceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}