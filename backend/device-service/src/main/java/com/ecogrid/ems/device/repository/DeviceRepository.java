package com.ecogrid.ems.device.repository;

import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceType;
import com.ecogrid.ems.device.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Device entity operations
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * Find device by serial number
     */
    Optional<Device> findBySerialNumber(String serialNumber);

    /**
     * Check if device exists by serial number
     */
    boolean existsBySerialNumber(String serialNumber);

    /**
     * Find devices by site
     */
    List<Device> findBySite(Site site);

    /**
     * Find devices by site ID
     */
    List<Device> findBySiteId(Long siteId);

    /**
     * Find devices by type
     */
    List<Device> findByDeviceType(DeviceType deviceType);

    /**
     * Find devices by status
     */
    List<Device> findByStatus(Device.DeviceStatus status);

    /**
     * Find devices by site and status
     */
    List<Device> findBySiteAndStatus(Site site, Device.DeviceStatus status);

    /**
     * Find devices by manufacturer
     */
    List<Device> findByManufacturer(String manufacturer);

    /**
     * Search devices by name containing (case insensitive)
     */
    @Query("SELECT d FROM Device d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Device> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find devices that haven't communicated recently
     */
    @Query("SELECT d FROM Device d WHERE d.lastCommunication < :threshold OR d.lastCommunication IS NULL")
    List<Device> findStaleDevices(@Param("threshold") LocalDateTime threshold);

    /**
     * Find devices due for maintenance
     */
    @Query("SELECT d FROM Device d WHERE d.lastMaintenance < :threshold OR d.lastMaintenance IS NULL")
    List<Device> findDevicesDueForMaintenance(@Param("threshold") LocalDateTime threshold);

    /**
     * Update device status
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = :status, d.updatedAt = :updatedAt WHERE d.id = :deviceId")
    void updateStatus(@Param("deviceId") Long deviceId, 
                      @Param("status") Device.DeviceStatus status,
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update last communication timestamp
     */
    @Modifying
    @Query("UPDATE Device d SET d.lastCommunication = :timestamp WHERE d.id = :deviceId")
    void updateLastCommunication(@Param("deviceId") Long deviceId, @Param("timestamp") LocalDateTime timestamp);

    /**
     * Count devices by status
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    long countByStatus(@Param("status") Device.DeviceStatus status);

    /**
     * Count devices by site
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.site.id = :siteId")
    long countBySiteId(@Param("siteId") Long siteId);

    /**
     * Count devices by type
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.deviceType = :deviceType")
    long countByDeviceType(@Param("deviceType") DeviceType deviceType);

    /**
     * Find devices with their site information
     */
    @Query("SELECT d FROM Device d JOIN FETCH d.site WHERE d.id = :deviceId")
    Optional<Device> findByIdWithSite(@Param("deviceId") Long deviceId);
}