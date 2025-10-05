package com.ecogrid.ems.device.repository;

import com.ecogrid.ems.device.entity.DeviceStatusCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeviceStatusCache entity
 */
@Repository
public interface DeviceStatusCacheRepository extends JpaRepository<DeviceStatusCache, Long> {

    /**
     * Find device status by device ID
     */
    Optional<DeviceStatusCache> findByDeviceId(Long deviceId);

    /**
     * Find all device status for a site
     */
    @Query("SELECT dsc FROM DeviceStatusCache dsc JOIN Device d ON dsc.deviceId = d.id WHERE d.site.id = :siteId")
    List<DeviceStatusCache> findBySiteId(@Param("siteId") Long siteId);

    /**
     * Find devices by status for a site
     */
    @Query("SELECT dsc FROM DeviceStatusCache dsc JOIN Device d ON dsc.deviceId = d.id WHERE d.site.id = :siteId AND dsc.status = :status")
    List<DeviceStatusCache> findBySiteIdAndStatus(@Param("siteId") Long siteId, @Param("status") DeviceStatusCache.DeviceStatus status);

    /**
     * Find devices with active alerts for a site
     */
    @Query("SELECT dsc FROM DeviceStatusCache dsc JOIN Device d ON dsc.deviceId = d.id WHERE d.site.id = :siteId AND dsc.alertCount > 0")
    List<DeviceStatusCache> findBySiteIdWithActiveAlerts(@Param("siteId") Long siteId);

    /**
     * Update alert count for a device
     */
    @Modifying
    @Query("UPDATE DeviceStatusCache dsc SET dsc.alertCount = :alertCount, dsc.updatedAt = :updatedAt " +
           "WHERE dsc.deviceId = :deviceId")
    void updateAlertCount(@Param("deviceId") Long deviceId, @Param("alertCount") Integer alertCount, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update device status and last seen time
     */
    @Modifying
    @Query("UPDATE DeviceStatusCache dsc SET dsc.status = :status, dsc.lastSeen = :lastSeen, dsc.updatedAt = :updatedAt " +
           "WHERE dsc.deviceId = :deviceId")
    void updateStatusAndLastSeen(@Param("deviceId") Long deviceId, @Param("status") DeviceStatusCache.DeviceStatus status, 
                                @Param("lastSeen") LocalDateTime lastSeen, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Count devices by status for a site
     */
    @Query("SELECT COUNT(dsc) FROM DeviceStatusCache dsc JOIN Device d ON dsc.deviceId = d.id WHERE d.site.id = :siteId AND dsc.status = :status")
    Long countBySiteIdAndStatus(@Param("siteId") Long siteId, @Param("status") DeviceStatusCache.DeviceStatus status);

    /**
     * Find devices that haven't been seen since a certain time (for offline detection)
     */
    @Query("SELECT dsc FROM DeviceStatusCache dsc WHERE dsc.lastSeen < :cutoffTime AND dsc.status != 'OFFLINE'")
    List<DeviceStatusCache> findDevicesNotSeenSince(@Param("cutoffTime") LocalDateTime cutoffTime);
}